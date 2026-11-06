'use strict';
const express = require('express');
const multer = require('multer');
const path = require('path');
const fs = require('fs');
const db = require('./lib/db');
const auth = require('./lib/auth');

const PORT = process.env.PORT || 3000;
const HOST = process.env.HOST || '0.0.0.0';
const app = express();

app.use(express.json({ limit: '1mb' }));

// minimal cookie parsing (no extra dependency)
app.use((req, res, next) => {
  req.cookies = {};
  const raw = req.headers.cookie;
  if (raw) {
    for (const part of raw.split(';')) {
      const idx = part.indexOf('=');
      if (idx > -1) req.cookies[part.slice(0, idx).trim()] = decodeURIComponent(part.slice(idx + 1).trim());
    }
  }
  next();
});

const upload = multer({
  storage: multer.diskStorage({
    destination: (req, file, cb) => cb(null, db.storageDir),
    filename: (req, file, cb) => {
      const safe = path.basename(file.originalname).replace(/[^\w.\-]/g, '_');
      cb(null, `${Date.now()}-${safe}`);
    },
  }),
  limits: { fileSize: 200 * 1024 * 1024 },
});

// ---------- public ----------
app.use(express.static(path.join(__dirname, 'public')));

app.get('/api/site', (req, res) => res.json(db.publicContent()));

app.get('/api/health', (req, res) => res.json({ ok: true, status: db.published.releaseStatus }));

// Download endpoint: only serves a file that is approved AND actually stored.
// Approved external URLs redirect. Otherwise 404 — the UI shows COMING SOON instead.
app.get('/api/download/:kind', (req, res) => {
  const d = db.published.downloads[req.params.kind];
  if (!d || d.status !== 'available') return res.status(404).json({ error: 'Not available yet.' });
  if (d.storedFile) {
    const file = db.resolveStored(d.storedFile);
    if (fs.existsSync(file)) return res.download(file, d.fileName);
    return res.status(404).json({ error: 'File missing on server.' });
  }
  if (d.externalUrl) return res.redirect(d.externalUrl);
  return res.status(404).json({ error: 'No file or URL configured.' });
});

// ---------- admin auth ----------
app.post('/api/admin/login', (req, res) => {
  const result = auth.login(req.body && req.body.password);
  if (!result.ok) return res.status(401).json(result);
  res.setHeader('Set-Cookie', `azyron_admin=${result.token}; HttpOnly; SameSite=Strict; Path=/; Max-Age=43200`);
  res.json({ ok: true });
});

app.post('/api/admin/logout', (req, res) => {
  auth.logout(req);
  res.setHeader('Set-Cookie', 'azyron_admin=; HttpOnly; SameSite=Strict; Path=/; Max-Age=0');
  res.json({ ok: true });
});

app.get('/api/admin/me', (req, res) => res.json({ admin: auth.isAdmin(req) }));

// ---------- admin API (owner only) ----------
app.get('/api/admin/draft', auth.requireAdmin, (req, res) => {
  const draft = JSON.parse(JSON.stringify(db.draft));
  res.json({ draft, publishedAt: db.publishedAt || null });
});

app.post('/api/admin/publish', auth.requireAdmin, (req, res) => {
  res.json({ ok: true, publishedAt: db.publish() });
});

app.post('/api/admin/release-status', auth.requireAdmin, (req, res) => {
  const allowed = ['development', 'pre-release', 'rc', 'official'];
  const status = req.body && req.body.status;
  if (!allowed.includes(status)) return res.status(400).json({ error: 'Invalid status.' });
  db.draft.releaseStatus = status;
  db.save();
  res.json({ ok: true, status });
});

app.post('/api/admin/meta/:kind', auth.requireAdmin, (req, res) => {
  const d = db.draft.downloads[req.params.kind];
  if (!d) return res.status(404).json({ error: 'Unknown download kind.' });
  const b = req.body || {};
  for (const k of ['name', 'fileName', 'description', 'version', 'minecraftVersion', 'loader', 'updated']) {
    if (b[k] !== undefined) d[k] = String(b[k]);
  }
  if (b.status !== undefined) {
    if (!['coming_soon', 'available'].includes(b.status)) return res.status(400).json({ error: 'Invalid status.' });
    d.status = b.status;
  }
  db.save();
  res.json({ ok: true, download: d });
});

app.post('/api/admin/download-url/:kind', auth.requireAdmin, (req, res) => {
  const d = db.draft.downloads[req.params.kind];
  if (!d) return res.status(404).json({ error: 'Unknown download kind.' });
  const url = req.body && req.body.url;
  if (url !== null && url !== '' && !/^https:\/\/[\w.\-]+(\.[\w.\-]+)+([\w\-\/._?=&%#]*)?$/.test(String(url))) {
    return res.status(400).json({ error: 'Only https URLs are allowed.' });
  }
  d.externalUrl = url || null;
  db.save();
  res.json({ ok: true, externalUrl: d.externalUrl });
});

app.post('/api/admin/upload/:kind', auth.requireAdmin, upload.single('file'), (req, res) => {
  const d = db.draft.downloads[req.params.kind];
  if (!d) return res.status(404).json({ error: 'Unknown download kind.' });
  if (!req.file) return res.status(400).json({ error: 'No file uploaded.' });
  const allowed = { client: ['.jar'], modpack: ['.mrpack', '.zip'] }[req.params.kind];
  const ext = path.extname(req.file.originalname).toLowerCase();
  if (!allowed.includes(ext)) {
    fs.unlinkSync(req.file.path);
    return res.status(400).json({ error: `Invalid file type. Allowed: ${allowed.join(', ')}` });
  }
  if (d.storedFile && d.storedFile !== path.basename(req.file.path)) {
    const old = db.resolveStored(d.storedFile);
    if (fs.existsSync(old)) fs.unlinkSync(old);
  }
  d.storedFile = path.basename(req.file.path);
  d.fileName = path.basename(req.file.originalname);
  d.sizeBytes = req.file.size;
  d.updated = new Date().toISOString().slice(0, 10);
  d.externalUrl = null;
  db.save();
  res.json({ ok: true, download: d });
});

app.post('/api/admin/changelog/:kind', auth.requireAdmin, (req, res) => {
  const d = db.draft.downloads[req.params.kind];
  if (!d) return res.status(404).json({ error: 'Unknown download kind.' });
  const { action, entry, index } = req.body || {};
  if (action === 'add') {
    if (!entry || !entry.version) return res.status(400).json({ error: 'version required' });
    d.changelog.unshift(entry);
  } else if (action === 'delete') {
    d.changelog.splice(index, 1);
  } else if (action === 'edit') {
    d.changelog[index] = entry;
  } else {
    return res.status(400).json({ error: 'Unknown action.' });
  }
  db.save();
  res.json({ ok: true, changelog: d.changelog });
});

app.post('/api/admin/screenshots', auth.requireAdmin, upload.array('files', 12), (req, res) => {
  const files = req.files || [];
  for (const f of files) {
    const ext = path.extname(f.originalname).toLowerCase();
    if (!['.png', '.jpg', '.jpeg', '.webp'].includes(ext)) { fs.unlinkSync(f.path); continue; }
    db.draft.screenshots.push({ file: path.basename(f.path), caption: path.basename(f.originalname, ext) });
  }
  db.save();
  res.json({ ok: true, screenshots: db.draft.screenshots });
});

app.delete('/api/admin/screenshots/:file', auth.requireAdmin, (req, res) => {
  const name = path.basename(req.params.file);
  const i = db.draft.screenshots.findIndex(s => s.file === name);
  if (i > -1) {
    db.draft.screenshots.splice(i, 1);
    const f = db.resolveStored(name);
    if (fs.existsSync(f)) fs.unlinkSync(f);
    db.save();
  }
  res.json({ ok: true });
});

app.get('/shots/:file', (req, res) => {
  const f = db.resolveStored(req.params.file);
  if (fs.existsSync(f)) return res.sendFile(f);
  res.status(404).end();
});

app.post('/api/admin/announcements', auth.requireAdmin, (req, res) => {
  const { action, index, entry } = req.body || {};
  if (action === 'add') db.draft.announcements.unshift(entry);
  else if (action === 'delete') db.draft.announcements.splice(index, 1);
  else if (action === 'edit') db.draft.announcements[index] = entry;
  else return res.status(400).json({ error: 'Unknown action.' });
  db.save();
  res.json({ ok: true, announcements: db.draft.announcements });
});

app.post('/api/admin/content', auth.requireAdmin, (req, res) => {
  const b = req.body || {};
  if (b.features) db.draft.features = b.features;
  if (b.faqs) db.draft.faqs = b.faqs;
  if (b.installSteps) db.draft.installSteps = b.installSteps;
  if (b.social) db.draft.social = { ...db.draft.social, ...b.social };
  if (b.brand) db.draft.brand = { ...db.draft.brand, ...b.brand };
  db.save();
  res.json({ ok: true });
});

// SPA fallback for admin page
app.get('/admin', (req, res) => res.sendFile(path.join(__dirname, 'public', 'admin.html')));

app.listen(PORT, HOST, () => {
  console.log(`AZYRON CLIENT website listening on http://${HOST}:${PORT}`);
  console.log(`Admin panel: http://localhost:${PORT}/admin (owner-only, AZYRON_ADMIN_PASSWORD required)`);
});

'use strict';
// Owner-only authentication. The password comes ONLY from the AZYRON_ADMIN_PASSWORD
// environment variable and is never written to the repo or sent to the client.
const crypto = require('crypto');

const sessions = new Map(); // token -> { createdAt }

function adminPasswordHash() {
  const pw = process.env.AZYRON_ADMIN_PASSWORD;
  if (!pw) return null;
  return crypto.createHash('sha256').update(pw).digest('hex');
}

function login(password) {
  const expected = adminPasswordHash();
  if (!expected) return { ok: false, reason: 'Admin access is not configured on this server (AZYRON_ADMIN_PASSWORD is not set).' };
  const given = crypto.createHash('sha256').update(String(password)).digest('hex');
  const a = Buffer.from(expected);
  const b = Buffer.from(given);
  if (a.length !== b.length || !crypto.timingSafeEqual(a, b)) {
    return { ok: false, reason: 'Invalid credentials.' };
  }
  const token = crypto.randomBytes(32).toString('hex');
  sessions.set(token, { createdAt: Date.now() });
  return { ok: true, token };
}

function isAdmin(req) {
  const token = req.cookies && req.cookies.azyron_admin;
  if (!token || !sessions.has(token)) return false;
  const s = sessions.get(token);
  if (Date.now() - s.createdAt > 12 * 60 * 60 * 1000) { sessions.delete(token); return false; }
  return true;
}

function logout(req) {
  const token = req.cookies && req.cookies.azyron_admin;
  if (token) sessions.delete(token);
}

function requireAdmin(req, res, next) {
  if (!isAdmin(req)) return res.status(401).json({ error: 'Owner authentication required.' });
  next();
}

module.exports = { login, logout, isAdmin, requireAdmin };

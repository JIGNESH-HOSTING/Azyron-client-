/* Owner-only admin panel. Talks to /api/admin/* with the HttpOnly session cookie. */
let DRAFT = null;

async function api(path, opts = {}) {
  const res = await fetch(path, {
    headers: { 'Content-Type': 'application/json' },
    credentials: 'same-origin',
    ...opts,
  });
  if (res.status === 401) { showLogin(); throw new Error('unauthorized'); }
  return res.json();
}

function showLogin() {
  document.getElementById('loginWrap').style.display = 'block';
  document.getElementById('dash').style.display = 'none';
}

async function boot() {
  try {
    const me = await fetch('/api/admin/me', { credentials: 'same-origin' }).then(r => r.json());
    if (me.admin) { await loadDash(); } else { showLogin(); }
  } catch (e) { showLogin(); }

  document.getElementById('loginForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const r = await fetch('/api/admin/login', {
      method: 'POST', headers: { 'Content-Type': 'application/json' },
      credentials: 'same-origin', body: JSON.stringify({ password: document.getElementById('pw').value }),
    }).then(r => r.json());
    if (r.ok) { document.getElementById('loginError').textContent = ''; await loadDash(); }
    else document.getElementById('loginError').textContent = r.reason || 'Invalid credentials.';
  });
}

async function loadDash() {
  document.getElementById('loginWrap').style.display = 'none';
  document.getElementById('dash').style.display = 'block';
  const data = await api('/api/admin/draft');
  DRAFT = data.draft;
  document.getElementById('pubStatus').textContent = DRAFT.releaseStatus;
  document.getElementById('pubAt').textContent = data.publishedAt || 'never';
  document.getElementById('releaseStatus').value = DRAFT.releaseStatus;
  renderDownloadPanel('client');
  renderDownloadPanel('modpack');
  renderShots();
  renderAnns();
  document.getElementById('contentJson').value = JSON.stringify({
    features: DRAFT.features, faqs: DRAFT.faqs, installSteps: DRAFT.installSteps, social: DRAFT.social, brand: DRAFT.brand,
  }, null, 2);
}

document.getElementById('dashTabs').addEventListener('click', (e) => {
  const t = e.target.closest('.dash-tab');
  if (!t) return;
  document.querySelectorAll('.dash-tab').forEach(x => x.classList.remove('active'));
  document.querySelectorAll('.panel').forEach(x => x.classList.remove('active'));
  t.classList.add('active');
  document.getElementById(t.dataset.panel).classList.add('active');
});

async function saveStatus() {
  await api('/api/admin/release-status', { method: 'POST', body: JSON.stringify({ status: document.getElementById('releaseStatus').value }) });
  await loadDash();
}

function renderDownloadPanel(kind) {
  const d = DRAFT.downloads[kind];
  const el = document.getElementById('p-' + kind);
  el.innerHTML = `
    <h2>${kind === 'client' ? 'AZYRON CLIENT (JAR)' : 'AZYRON MODPACK (MRPACK)'}</h2>
    <div class="row">
      <div><label class="note">Name</label><input class="admin-input" id="${kind}-name" value="${d.name}"></div>
      <div><label class="note">Version</label><input class="admin-input" id="${kind}-version" value="${d.version}"></div>
      <div><label class="note">File name</label><input class="admin-input" id="${kind}-fileName" value="${d.fileName}"></div>
    </div>
    <label class="note">Description</label>
    <textarea class="admin-input" id="${kind}-description">${d.description}</textarea>
    <div class="row">
      <div><label class="note">Minecraft</label><input class="admin-input" id="${kind}-mc" value="${d.minecraftVersion}"></div>
      <div><label class="note">Loader</label><input class="admin-input" id="${kind}-loader" value="${d.loader}"></div>
      <div><label class="note">Last updated</label><input class="admin-input" id="${kind}-updated" value="${d.updated || ''}" placeholder="YYYY-MM-DD"></div>
      <div><label class="note">Status</label>
        <select id="${kind}-status">
          <option value="coming_soon" ${d.status === 'coming_soon' ? 'selected' : ''}>Coming Soon (private)</option>
          <option value="available" ${d.status === 'available' ? 'selected' : ''}>Available (public download)</option>
        </select>
      </div>
    </div>
    <div class="row" style="margin-bottom:14px">
      <button class="admin-btn" onclick="saveMeta('${kind}')">SAVE METADATA</button>
    </div>
    <h3>Upload file</h3>
    <p class="note">Client accepts .jar · Modpack accepts .mrpack / .zip. Stored server-side; never exposed unless status = Available.</p>
    <div class="row">
      <input type="file" id="${kind}-file" style="color:var(--muted)">
      <button class="admin-btn" onclick="uploadFile('${kind}')">UPLOAD FILE</button>
    </div>
    <p class="note" style="margin-top:8px">Stored file: <strong>${d.storedFile || 'none'}</strong> · size: ${d.sizeBytes ? (d.sizeBytes / 1024).toFixed(1) + ' KB' : '—'}</p>
    <h3>External download URL (optional)</h3>
    <div class="row">
      <input class="admin-input" id="${kind}-url" value="${d.externalUrl || ''}" placeholder="https://… (owner-approved URL only)">
      <button class="admin-btn secondary" onclick="saveUrl('${kind}')">SET URL</button>
    </div>
    <h3>Changelog</h3>
    <div class="row">
      <input class="admin-input" id="${kind}-clv" placeholder="Version" style="max-width:120px">
      <input class="admin-input" id="${kind}-cld" placeholder="Date" style="max-width:140px">
      <input class="admin-input" id="${kind}-cln" placeholder="Notes">
      <button class="admin-btn" onclick="addCl('${kind}')">ADD</button>
    </div>
    <div style="margin-top:12px" id="${kind}-cl"></div>
  `;
  d.changelog.forEach((c, i) => {
    const row = document.createElement('div');
    row.className = 'cl-row';
    row.innerHTML = `<strong style="color:var(--blue)">v${c.version}</strong><span>${(c.notes || '').slice(0, 80)}</span>`;
    const btn = document.createElement('button');
    btn.className = 'admin-btn danger mini';
    btn.textContent = 'DELETE';
    btn.onclick = () => delCl(kind, i);
    row.appendChild(btn);
    document.getElementById(kind + '-cl').appendChild(row);
  });
}

async function saveMeta(kind) {
  const g = (s) => document.getElementById(kind + '-' + s).value;
  await api('/api/admin/meta/' + kind, {
    method: 'POST',
    body: JSON.stringify({ name: g('name'), version: g('version'), fileName: g('fileName'), description: g('description'), minecraftVersion: g('mc'), loader: g('loader'), updated: g('updated'), status: g('status') }),
  });
  await loadDash();
}

async function uploadFile(kind) {
  const f = document.getElementById(kind + '-file').files[0];
  if (!f) return alert('Choose a file first.');
  const fd = new FormData();
  fd.append('file', f);
  const res = await fetch('/api/admin/upload/' + kind, { method: 'POST', credentials: 'same-origin', body: fd });
  const r = await res.json();
  if (!res.ok) return alert(r.error || 'Upload failed');
  await loadDash();
}

async function saveUrl(kind) {
  const url = document.getElementById(kind + '-url').value.trim();
  await api('/api/admin/download-url/' + kind, { method: 'POST', body: JSON.stringify({ url: url || null }) });
  await loadDash();
}

async function addCl(kind) {
  const entry = {
    version: document.getElementById(kind + '-clv').value,
    date: document.getElementById(kind + '-cld').value,
    notes: document.getElementById(kind + '-cln').value,
  };
  await api('/api/admin/changelog/' + kind, { method: 'POST', body: JSON.stringify({ action: 'add', entry }) });
  await loadDash();
}

async function delCl(kind, index) {
  await api('/api/admin/changelog/' + kind, { method: 'POST', body: JSON.stringify({ action: 'delete', index }) });
  await loadDash();
}

async function uploadShots() {
  const files = document.getElementById('shotFiles').files;
  if (!files.length) return alert('Choose images first.');
  const fd = new FormData();
  for (const f of files) fd.append('files', f);
  await fetch('/api/admin/screenshots', { method: 'POST', credentials: 'same-origin', body: fd });
  await loadDash();
}

function renderShots() {
  const el = document.getElementById('shotsList');
  el.innerHTML = '';
  (DRAFT.screenshots || []).forEach(s => {
    const wrap = document.createElement('div');
    wrap.innerHTML = `<img class="shot-thumb" src="/shots/${encodeURIComponent(s.file)}"><div class="note">${s.caption}</div>`;
    const btn = document.createElement('button');
    btn.className = 'admin-btn danger mini';
    btn.textContent = 'DELETE';
    btn.onclick = async () => { await fetch('/api/admin/screenshots/' + encodeURIComponent(s.file), { method: 'DELETE', credentials: 'same-origin' }); await loadDash(); };
    wrap.appendChild(btn);
    el.appendChild(wrap);
  });
}

function renderAnns() {
  const el = document.getElementById('annList');
  el.innerHTML = '';
  (DRAFT.announcements || []).forEach((a, i) => {
    const row = document.createElement('div');
    row.className = 'cl-row';
    row.innerHTML = `<span>${a.text} <em>(${a.date || ''})</em></span>`;
    const btn = document.createElement('button');
    btn.className = 'admin-btn danger mini';
    btn.textContent = 'DELETE';
    btn.onclick = async () => { await api('/api/admin/announcements', { method: 'POST', body: JSON.stringify({ action: 'delete', index: i }) }); await loadDash(); };
    row.appendChild(btn);
    el.appendChild(row);
  });
}

async function addAnn() {
  await api('/api/admin/announcements', {
    method: 'POST',
    body: JSON.stringify({ action: 'add', entry: { text: document.getElementById('annText').value, date: document.getElementById('annDate').value } }),
  });
  await loadDash();
}

async function saveContent() {
  try {
    const parsed = JSON.parse(document.getElementById('contentJson').value);
    await api('/api/admin/content', { method: 'POST', body: JSON.stringify(parsed) });
    document.getElementById('contentMsg').textContent = 'Saved to draft.';
    await loadDash();
  } catch (e) { document.getElementById('contentMsg').textContent = 'Invalid JSON: ' + e.message; }
}

async function publish() {
  const r = await api('/api/admin/publish', { method: 'POST', body: '{}' });
  document.getElementById('pubMsg').textContent = 'Published at ' + r.publishedAt;
  await loadDash();
}

async function logout() {
  await fetch('/api/admin/logout', { method: 'POST', credentials: 'same-origin' });
  showLogin();
}

boot();

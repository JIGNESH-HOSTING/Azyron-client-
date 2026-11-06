/* AZYRON CLIENT public site — renders from /api/site (published content only). */
let SITE = null;
let CHANGELOG_TAB = 'client';

const fmtSize = (b) => {
  if (b === null || b === undefined) return '—';
  if (b < 1024) return b + ' B';
  if (b < 1024 * 1024) return (b / 1024).toFixed(1) + ' KB';
  return (b / (1024 * 1024)).toFixed(2) + ' MB';
};

const STATUS_LABEL = {
  development: 'Development',
  'pre-release': 'Pre-Release',
  rc: 'Release Candidate',
  official: 'Official Release',
};

function esc(s) {
  return String(s ?? '').replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
}

function renderBadge() {
  const el = document.getElementById('releaseBadge');
  const st = SITE.releaseStatus || 'development';
  el.textContent = STATUS_LABEL[st] || st;
  el.className = 'release-badge ' + st;
}

function renderDownloads() {
  for (const kind of ['client', 'modpack']) {
    const d = SITE.downloads[kind];
    const card = document.getElementById('card-' + kind);
    const available = d.available === true;
    card.innerHTML = `
      <h3>${esc(d.name)}</h3>
      <p class="dl-desc">${esc(d.description)}</p>
      <span class="dl-status ${available ? 'available' : 'soon'}">${available ? '● AVAILABLE' : '○ COMING SOON'}</span>
      <div class="dl-meta">
        <div>Version<strong>v${esc(d.version)}</strong></div>
        <div>File size<strong>${fmtSize(d.sizeBytes)}</strong></div>
        <div>Minecraft<strong>${esc(d.minecraftVersion)}</strong></div>
        <div>Loader<strong>${esc(d.loader)}</strong></div>
        <div>Last updated<strong>${esc(d.updated || '—')}</strong></div>
        <div>File<strong>${esc(d.fileName)}</strong></div>
      </div>
      ${available
        ? `<a class="dl-btn live" href="/api/download/${kind}" download>⬇ DOWNLOAD ${kind === 'client' ? 'CLIENT' : 'MODPACK'}</a>`
        : `<span class="dl-btn soon">COMING SOON</span>`}
      <p class="dl-file">${esc(d.fileName)}</p>
    `;
  }
}

function renderChangelog() {
  const list = document.getElementById('changelogList');
  const entries = (SITE.downloads[CHANGELOG_TAB] || {}).changelog || [];
  list.innerHTML = entries.length
    ? entries.map(e => `<div class="cl-entry"><h4>v${esc(e.version)} — ${esc(e.date || '')}</h4><p>${esc(e.notes)}</p></div>`).join('')
    : '<p class="note">No changelog entries yet.</p>';
}

function renderAll() {
  renderBadge();
  document.getElementById('heroVersion').textContent = 'Client v' + (SITE.downloads.client.version || '1.0');
  document.getElementById('featuresGrid').innerHTML = SITE.features.map((f, i) => `
    <div class="feature-card reveal"><h3>${esc(f.title)}</h3><p>${esc(f.text)}</p></div>`).join('');
  const shots = SITE.screenshots || [];
  document.getElementById('shotsGrid').innerHTML = shots.length
    ? shots.map(s => `<figure class="shot reveal"><img src="/shots/${encodeURIComponent(s.file)}" alt="${esc(s.caption)}" loading="lazy"><figcaption>${esc(s.caption)}</figcaption></figure>`).join('')
    : '<div class="shots-empty reveal">Screenshots will appear here once published by the developer.</div>';
  const preview = ['/assets/preview-1.jpg', '/assets/preview-2.jpg', '/assets/preview-3.jpg'];
  document.getElementById('previewGrid').innerHTML = preview.map((p, i) =>
    `<figure class="shot reveal"><img src="${p}" alt="AZYRON CLIENT preview ${i + 1}" loading="lazy"></figure>`).join('');
  document.getElementById('installSteps').innerHTML = SITE.installSteps.map((s, i) => `
    <div class="step reveal"><div class="step-num">${i + 1}</div><div><h4>${esc(s.title)}</h4><p>${esc(s.text)}</p></div></div>`).join('');
  document.getElementById('faqList').innerHTML = SITE.faqs.map(f => `
    <div class="faq-item"><button class="faq-q">${esc(f.q)}</button><div class="faq-a">${esc(f.a)}</div></div>`).join('');
  const anns = SITE.announcements || [];
  document.getElementById('announcements').innerHTML = anns.length
    ? anns.map(a => `<div class="ann">${esc(a.text)}<small>${esc(a.date || '')}</small></div>`).join('')
    : '<p class="ann-empty">No announcements yet.</p>';
  document.getElementById('linkDiscord').href = SITE.social.discord || '#';
  document.getElementById('linkGithub').href = SITE.social.github || '#';
  document.getElementById('linkTwitter').href = SITE.social.twitter || '#';
  document.getElementById('linkYoutube').href = SITE.social.youtube || '#';
  renderDownloads();
  renderChangelog();
  observeReveals();
}

function observeReveals() {
  const obs = new IntersectionObserver(entries => {
    entries.forEach(e => { if (e.isIntersecting) { e.target.classList.add('visible'); obs.unobserve(e.target); } });
  }, { threshold: 0.12 });
  document.querySelectorAll('.reveal:not(.visible)').forEach(el => obs.observe(el));
}

async function boot() {
  const res = await fetch('/api/site');
  SITE = await res.json();
  renderAll();

  document.querySelectorAll('.tab').forEach(t => t.addEventListener('click', () => {
    document.querySelectorAll('.tab').forEach(x => x.classList.remove('active'));
    t.classList.add('active');
    CHANGELOG_TAB = t.dataset.tab;
    renderChangelog();
  }));
  document.getElementById('faqList').addEventListener('click', e => {
    const q = e.target.closest('.faq-q');
    if (q) q.parentElement.classList.toggle('open');
  });
  document.getElementById('navToggle').addEventListener('click', () =>
    document.getElementById('navLinks').classList.toggle('open'));
  window.addEventListener('scroll', () =>
    document.getElementById('navbar').classList.toggle('scrolled', window.scrollY > 40));
}

boot();

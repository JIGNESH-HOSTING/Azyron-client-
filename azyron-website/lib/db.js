'use strict';
// JSON-file backed store with a draft/published model.
// Public visitors only ever see `published`; the owner edits `draft` and approves publishing.
const fs = require('fs');
const path = require('path');

const DATA_DIR = path.join(__dirname, '..', 'data');
const DB_FILE = path.join(DATA_DIR, 'db.json');

function defaultContent() {
  return {
    brand: {
      name: 'AZYRON CLIENT',
      developer: 'JIGNESH GAMING',
      tagline: 'Performance. Smoothness. Style.',
      heading: 'BOOST YOUR MINECRAFT EXPERIENCE',
      minecraftVersion: '1.21.11',
      loader: 'Fabric',
    },
    releaseStatus: 'development', // development | pre-release | rc | official
    downloads: {
      client: {
        kind: 'client',
        name: 'AZYRON CLIENT',
        fileName: 'Azyron-Client-1.0.jar',
        description: 'Official AZYRON Minecraft Java Client.',
        version: '1.0',
        minecraftVersion: '1.21.11',
        loader: 'Fabric',
        sizeBytes: null,
        updated: null,
        externalUrl: null,
        status: 'coming_soon', // coming_soon | available
        changelog: [
          { version: '1.0', date: '2026-11-06', notes: 'Initial development build: custom main menu, HUD (FPS/CPS/ping/coords/keystrokes/armor), FPS boost profiles, Potato/Balanced/Smooth presets. Private testing only.' },
        ],
      },
      modpack: {
        kind: 'modpack',
        name: 'AZYRON MODPACK',
        fileName: 'Azyron-Modpack-1.0.mrpack',
        description: 'Official AZYRON performance and optimization modpack (Modrinth .mrpack).',
        version: '1.0',
        minecraftVersion: '1.21.11',
        loader: 'Fabric',
        sizeBytes: null,
        updated: null,
        externalUrl: null,
        status: 'coming_soon',
        changelog: [
          { version: '1.0', date: '2026-11-06', notes: 'Sodium, Lithium, FerriteCore, ImmediatelyFast, EntityCulling, MoreCulling, ModernFix-mvus, Dynamic FPS + Fabric API — all resolved for MC 1.21.11. Private testing only.' },
        ],
      },
    },
    features: [
      { title: 'Maximum FPS', text: 'Sodium-powered rendering, FerriteCore memory tuning and Dynamic FPS keep frames high without stutter.' },
      { title: 'Smooth Gameplay', text: 'Lithium, ModernFix and entity culling reduce lag spikes and chunk-loading hitches.' },
      { title: 'Low RAM & CPU', text: 'FerriteCore + culling mods cut memory use; idle FPS scaling lowers CPU load automatically.' },
      { title: 'Stability First', text: 'Only 1.21.11-verified mods, crash-safe mixins and conservative defaults — stability over fancy effects.' },
      { title: 'Premium HUD', text: 'FPS, CPS, ping, coordinates, keystrokes and armor HUD with drag-to-move positioning.' },
      { title: 'One-Click Optimize', text: 'Potato / Balanced / Smooth profiles plus a one-click optimization button.' },
    ],
    screenshots: [],
    installSteps: [
      { title: 'Install Fabric', text: 'Install Fabric Loader for Minecraft Java 1.21.11 (fabricmc.net), or use the AZYRON MODPACK .mrpack with Modrinth App / PojavLauncher.' },
      { title: 'Add the client', text: 'Place Azyron-Client-1.0.jar into the mods folder of your Fabric 1.21.11 instance.' },
      { title: 'Launch & play', text: 'Launch the Fabric 1.21.11 profile. AZYRON applies its low-graphics profile on first start; press Right Shift in-game for the AZYRON menu.' },
      { title: 'PojavLauncher (Android)', text: 'Install 1.21.11 + Fabric in PojavLauncher, copy the JAR into the instance mods folder, launch, and open the AZYRON menu with Right Shift.' },
    ],
    faqs: [
      { q: 'Which Minecraft version does AZYRON CLIENT support?', a: 'Minecraft Java 1.21.11 only, on Fabric Loader. No other version is supported.' },
      { q: 'Is it free?', a: 'Yes. AZYRON CLIENT is developed by JIGNESH GAMING and distributed privately until the official release is approved.' },
      { q: 'Why does the download say COMING SOON?', a: 'Builds stay private until the developer approves a release stage. Only approved files get a working download button — we never link to missing files.' },
      { q: 'Does it work on PojavLauncher?', a: 'Yes — PojavLauncher runs the full Java stack on Android, so the Fabric client works there. Use the Potato profile on weaker devices.' },
      { q: 'Will it get me banned?', a: 'AZYRON CLIENT only changes rendering, HUD and local settings — no cheats, no combat advantages, no server-side modifications.' },
    ],
    announcements: [],
    social: { discord: '#', twitter: '#', youtube: '#', github: 'https://github.com/JIGNESH-HOSTING/Azyron-client-' },
  };
}

function load() {
  fs.mkdirSync(DATA_DIR, { recursive: true });
  if (!fs.existsSync(DB_FILE)) {
    const seed = { published: defaultContent(), draft: defaultContent(), publishedAt: null };
    fs.writeFileSync(DB_FILE, JSON.stringify(seed, null, 2));
    return seed;
  }
  return JSON.parse(fs.readFileSync(DB_FILE, 'utf8'));
}

let db = load();

function save() {
  const tmp = DB_FILE + '.tmp';
  fs.writeFileSync(tmp, JSON.stringify(db, null, 2));
  fs.renameSync(tmp, DB_FILE);
}

function publicContent() {
  const c = JSON.parse(JSON.stringify(db.published));
  for (const kind of ['client', 'modpack']) {
    const d = c.downloads[kind];
    // Never advertise a file that is not actually stored/approved.
    const hasFile = d.status === 'available' && d.storedFile && fs.existsSync(path.join(__dirname, '..', 'storage', d.storedFile));
    const hasUrl = d.status === 'available' && !!d.externalUrl;
    d.available = hasFile || hasUrl;
    if (!d.available) d.status = 'coming_soon';
    delete d.storedFile;
  }
  return c;
}

module.exports = {
  get published() { return db.published; },
  get draft() { return db.draft; },
  publicContent,
  save,
  publish() {
    db.published = JSON.parse(JSON.stringify(db.draft));
    db.publishedAt = new Date().toISOString();
    save();
    return db.publishedAt;
  },
  storageDir: path.join(__dirname, '..', 'storage'),
  resolveStored(storedFile) {
    return path.join(__dirname, '..', 'storage', path.basename(storedFile));
  },
};

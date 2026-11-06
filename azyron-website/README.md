# AZYRON CLIENT — Official Website

Premium dark-themed official site for AZYRON CLIENT (Minecraft Java 1.21.11, Fabric).
Developer / release authority: **JIGNESH GAMING**.

## Run

```bash
cd azyron-website
npm install
AZYRON_ADMIN_PASSWORD='<your-strong-password>' npm start   # http://localhost:3000
```

- Public site: `/` — renders only the **published** content from `data/db.json`.
- Owner admin panel: `/admin` — requires `AZYRON_ADMIN_PASSWORD` (env var only; never in code).

## Release control (enforced in code)

- Downloads default to **COMING SOON**. A download button only appears when the owner
  sets the file's status to **Available** in the admin panel *and* a file/URL exists.
- `/api/download/:kind` returns 404 unless approved — the UI never links to missing files.
- Admin edits go to a **draft**; the public site changes only after **PUBLISH**.
- Release stages: Development → Pre-Release → RC → Official Release (owner-only).
- CI only ever updates a **private draft release** on GitHub; nothing is published
  publicly without explicit developer approval.

## Admin features

Upload JAR / MRPACK, set external download URLs (https-only), edit versions, file sizes,
release dates, per-file changelogs, screenshots, announcements, site content, social
links, release status, and preview-before-publish.

## Files

- `server.js` — Express backend: public API, owner-only admin API, secure downloads.
- `lib/db.js` — JSON store with draft/published model. `lib/auth.js` — session auth.
- `public/` — site + admin frontend. Uploads go to `storage/` (gitignored).

#!/usr/bin/env python3
"""Assembles the AZYRON CLIENT modpack ZIP.

Downloads the AZYRON client JAR plus every required performance mod and
dependency, resolving the newest file on Modrinth that explicitly supports
Minecraft 1.21.11 + Fabric Loader at build time (so the modpack can never ship
an incompatible mod).
"""
import json
import os
import shutil
import urllib.request
import zipfile

MC = "1.21.11"
LOADERS = json.dumps(["fabric"])
GAME_VERSIONS = json.dumps([MC])

# slug -> include in modpack
MODS = {
    "fabric-api": "Fabric API (required dependency of AZYRON CLIENT)",
    "sodium": "Sodium — rendering engine optimization",
    "lithium": "Lithium — server/game logic optimization",
    "ferritecore": "FerriteCore — memory usage reduction",
    "immediatelyfast": "ImmediatelyFast — UI/render batching",
    "entityculling": "Entity Culling — skips hidden entities",
    "morecull": "More Culling — extra culling heuristics",
    "modernfix": "ModernFix — general fixes & optimizations",
    "dynamic-fps": "Dynamic FPS — lowers FPS when idle",
    "modmenu": "Mod Menu — in-game mods list",
}

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
HERE = os.path.join(ROOT, "azyron-client")
PACK = os.path.join(HERE, "modpack")
MODS_DIR = os.path.join(PACK, "mods")
ZIP_PATH = os.path.join(PACK, "Azyron-Client-Modpack-1.0.zip")
CLIENT_JAR = os.path.join(HERE, "build", "libs", "azyron-client-1.0.jar")

README = f"""AZYRON CLIENT 1.0 — Modpack for Minecraft Java {MC} (Fabric)
Developer: JIGNESH GAMING
================================================================

INSTALL (standard Fabric / PojavLauncher):
  1. Install Fabric Loader for {MC} (PojavLauncher: use the Fabric
     installer / "Install Fabric" option for the {MC} profile).
  2. Extract this ZIP so that the "mods" folder lands inside your
     Minecraft folder (.minecraft on PC, or the PojavLauncher
     instance folder on Android).
  3. Launch Minecraft {MC} with the Fabric profile and play.

CONTENTS:
  mods/Azyron-Client-1.0.jar ....... the AZYRON CLIENT mod (menu, HUD, FPS boost)
"""


def fetch_versions(slug):
    url = (
        f"https://api.modrinth.com/v2/project/{slug}/version"
        f"?loaders={urllib.request.quote(LOADERS)}&game_versions={urllib.request.quote(GAME_VERSIONS)}"
    )
    print(f"Querying Modrinth for '{slug}' ...")
    try:
        with urllib.request.urlopen(url, timeout=30) as r:
            return json.load(r)
    except urllib.error.HTTPError as e:
        if e.code == 404:
            # fall back to the search endpoint in case the slug differs
            search = f"https://api.modrinth.com/v2/search?query={urllib.request.quote(slug)}&limit=5"
            with urllib.request.urlopen(search, timeout=30) as r:
                hits = json.load(r).get("hits", [])
            for hit in hits:
                if slug.replace("-", "") in hit["slug"].replace("-", "") or slug.replace("-", "") in hit["project_id"]:
                    print(f"Retrying with slug '{hit['slug']}'")
                    return fetch_versions(hit["slug"])
        raise


def pick_file(slug):
    versions = fetch_versions(slug)
    if not versions:
        raise RuntimeError(f"No Modrinth version of '{slug}' supports {MC} + Fabric")
    releases = [v for v in versions if v.get("version_type") == "release"]
    candidates = releases or versions
    for version in candidates:
        files = [f for f in version.get("files", []) if f.get("primary")] or version.get("files", [])
        if files:
            file = files[0]
            return version["version_number"], file["url"], file["filename"]
    raise RuntimeError(f"No downloadable file found for '{slug}' on {MC} + Fabric")


def main():
    os.makedirs(MODS_DIR, exist_ok=True)
    shutil.copy(CLIENT_JAR, os.path.join(MODS_DIR, "Azyron-Client-1.0.jar"))

    lines = [README]
    for slug, desc in MODS.items():
        ver, url, filename = pick_file(slug)
        dest = os.path.join(MODS_DIR, filename)
        print(f"Downloading {slug} {ver} -> {filename}")
        urllib.request.urlretrieve(url, dest)
        lines.append(f"  mods/{filename} ....... {slug} {ver} — {desc}\n")

    lines.append("""
All mods above were resolved from Modrinth at build time and explicitly
declare support for Minecraft 1.21.11 and the Fabric loader.
""")
    with open(os.path.join(PACK, "README.txt"), "w") as f:
        f.writelines(lines)

    if os.path.exists(ZIP_PATH):
        os.remove(ZIP_PATH)
    with zipfile.ZipFile(ZIP_PATH, "w", zipfile.ZIP_DEFLATED) as z:
        for base, _dirs, files in os.walk(PACK):
            for name in files:
                if name == os.path.basename(ZIP_PATH):
                    continue
                full = os.path.join(base, name)
                z.write(full, os.path.relpath(full, PACK))
    print(f"Modpack written: {ZIP_PATH} ({os.path.getsize(ZIP_PATH) // 1024} KB)")


if __name__ == "__main__":
    main()

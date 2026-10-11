# Scala/sbt Generated-File Ignore Rules

## Goal

Add a root `.gitignore` so generated Scala build-tool/editor state does not
clutter `git status`, while retaining reviewable VS Code workspace settings.

## Design

Ignore generated sbt output and Metals/Bloop/BSP state:

- Any `target/` directory.
- `.metals/`, `.bloop/`, and `.bsp/` directories.
- Generated `project/project/` and `project/metals.sbt`.

Do not ignore `.vscode/` as a directory. In particular, keep
`.vscode/settings.json` visible to Git so the project can decide whether those
workspace settings should be shared.

## Validation

Run `git status --short` after adding the rules. Existing generated build and
language-server paths should no longer be listed, while `.vscode/settings.json`
remains visible. Do not delete generated files as part of this change.

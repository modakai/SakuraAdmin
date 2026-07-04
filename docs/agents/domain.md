# Domain Docs

How the engineering skills should consume this repo's domain documentation when exploring the codebase.

## Layout

This repo uses a single-context domain-doc layout:

- `CONTEXT.md` at the repo root for project vocabulary, domain boundaries, and stable module facts.
- `docs/adr/` for architectural decision records.

`naive-vue-app` is the active frontend boundary. Skills should treat removed legacy frontend directories as historical context only, not as valid implementation targets.

## Before exploring, read these

- `CONTEXT.md` at the repo root, if it exists.
- Relevant ADRs under `docs/adr/`, if they exist.

If these files do not exist, proceed silently. Do not flag their absence or suggest creating them upfront. The domain-modeling flow can create them lazily when terms or decisions actually get resolved.

## Use the glossary's vocabulary

When output names a domain concept in an issue title, refactor proposal, hypothesis, or test name, use the term as defined in `CONTEXT.md`.

If the concept is missing from the glossary, treat that as a signal: either the output is inventing language the project does not use, or the glossary has a real gap that should be handled through domain modeling.

## Flag ADR conflicts

If output contradicts an existing ADR, surface the conflict explicitly instead of silently overriding it.

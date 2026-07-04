## Frontend Test Rules

- 前端正式测试文件优先放在现有 `src/**/__tests__` 目录中。
- 若只是当前任务的临时验证测试，统一放在 `src/__temp_tests__` 或贴近目标模块的 `src/**/__tests__` 中。
- 约束：临时测试仅用于当前验证，用完必须删除，避免把测试参考代码提交、上传或作为后续实现参考。

## 其他要求
小功能改动的话，无需编写测试

## Agent skills

### Issue tracker

Issues and PRDs are tracked as local markdown files under `.scratch/<feature-slug>/`. See `docs/agents/issue-tracker.md`.

### Triage labels

Triage uses the default five-role vocabulary: `needs-triage`, `needs-info`, `ready-for-agent`, `ready-for-human`, and `wontfix`. See `docs/agents/triage-labels.md`.

### Domain docs

This repo uses a single-context domain-doc layout: root `CONTEXT.md` plus `docs/adr/`. The active frontend boundary is `naive-vue-app`. See `docs/agents/domain.md`.

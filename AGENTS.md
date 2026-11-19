# Agent instructions

## State of development

- The project is in an early, fast-feedback phase. Prefer quick deployments over perfect quality:
  the shortest path to something testable in a real game wins, and it does not have to be 100%
  polished.
- Only run checks that can be automated (format, typecheck, lint, tests, builds and Playwright
  checks in the agent's own environment). Don't block on checks that need real devices or other
  browsers than the ones available in the sandbox, and don't ask the user to verify visuals
  before a change is committed.
- Device and browser checks happen asynchronously: the user plays test games on the target
  devices and reports findings as GitHub issues. Fix those in follow-up changes.
- The user can do manual preview checks in Chrome on request or when convenient. Treat this as
  optional in most cases, not a quality gate: ask for it only when a change is hard to judge
  without seeing it (for example a deliberate visual change), and don't wait for it otherwise.
- When something was not checked, say so in the summary, but don't treat it as a blocker.

## Git workflow

- All work goes through a branch and a pull request. Never commit or push directly to `main`:
  a push to `main` runs `deploy.yml` and deploys to production.
- Work on a local branch `feature/<topic>` (`git switch -c feature/<topic>`), branched from the
  local `main`. Keep one topic per branch.
- The user pushes the branch and opens the pull request against `main`. CI (`ci.yml`) has to
  pass; branch protection requires the `ci-ok` check.

## Remote access

- Git remote actions (`fetch`, `pull`, `push`) and GitHub interactions (`gh`, pull requests, issues)
  with the user's credentials are not available to coding agents. Work locally on a branch and
  commit when asked; the user pushes and opens the pull request.

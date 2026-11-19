# Agent instructions

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

# Agent instructions

## Git workflow

- All work goes through a branch and a pull request. Never commit or push directly to `main`:
  a push to `main` runs `deploy.yml` and deploys to production.
- Branch from an up-to-date `main` (`git switch main && git pull`, then
  `git switch -c feature/<topic>`). Keep one topic per branch.
- Open the pull request against `main` (`gh pr create`). CI (`ci.yml`) has to pass; branch
  protection requires the `ci-ok` check.

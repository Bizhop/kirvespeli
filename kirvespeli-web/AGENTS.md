# Agent instructions (kirvespeli-web)

The repository-wide rules are in `../AGENTS.md`.

## Visual checks

Use the component preview (`deno task preview`, http://127.0.0.1:1235) with Playwright. Run
`deno task format:check`, `typecheck`, `lint`, `test`, `build` and `build:preview` first.

- Start the preview in the background and wait until `curl -s -o /dev/null -w "%{http_code}"
  http://127.0.0.1:1235` returns 200. Stop it when done.
- Playwright is not a project dependency. Install it outside the repo (for example in the
  scratchpad directory: `npm i playwright`) and write scripts there, not in `kirvespeli-web/`.
- Use an already installed browser: `~/.cache/ms-playwright/chromium-*/chrome-linux64/chrome`
  passed as `executablePath` to `chromium.launch`. Downloading browsers may be blocked. Only
  Chromium is normally available; Firefox and WebKit are not checked here.
- A page is selected by its title in the URL hash (`#Hand`, `#Seat`, `#Table`, `#ScoreSlip`).
  Variants are MUI tabs: `page.getByRole("tab", { name: "lastCard", exact: true }).click()`.
- Prefer computed styles over screenshots for exact values (`getComputedStyle(el).rotate`,
  `.translate`, `.scale`). Take screenshots for the overall look, clipped to the relevant area
  (`clip`), and view them.
- Check hover with `locator.hover()` and reduced motion with `page.emulateMedia({ reducedMotion:
  "reduce" })`. Listen to `pageerror`, console errors and 404 responses.
- Only automated checks are required. Checks in other browsers and on real devices happen
  asynchronously through test games and GitHub issues (see `../AGENTS.md`). Report what was and
  was not checked, but don't wait for it.

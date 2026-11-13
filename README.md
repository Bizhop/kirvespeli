# Kirvespeli monorepo

- [Web](kirvespeli-web/README.md): käynnistys, komponenttien esikatselu, pelipöytä ja käyttöliittymä.
- [Backend](kirvespeli-backend/README.md): käynnistys, testit, pelisäännöt ja tuotannon asetukset.

## CI/CD (GitHub Actions)

- Pull requestit: `ci.yml` ajaa vain muuttuneet osat (backendin `./gradlew build`, `dbTest` PostgreSQL 14:llä ja ARM64-kuvan rakennus,
  webin `format:check`, testit ja build). Haaran suojaukseen vaaditaan vain `ci-ok`.
- Push `main`-haaraan: `deploy.yml` ajaa koko CI:n ja julkaisee tuotantoon vain sen, mikä on muuttunut viimeisen onnistuneen julkaisun
  jälkeen (backend ensin, sitten web), ja tekee savutestin osoitteeseen `https://kirvespeli.bizhop.fi`. Epäonnistunutta julkaisua ei
  peruta automaattisesti: korjaus tehdään uudella pushilla, ja seuraava push yrittää backendia uudelleen.
- Pakotettu julkaisu: Actions → Deploy → Run workflow (`force_backend`, `force_web`).
- Vanhan version julkaisu: `aws/apps/kirvespeli/scripts/deploy-backend-image.sh <aiempi kuva>` tai revert `main`-haaraan.
- AWS-käyttö OIDC:llä (`production`-ympäristö), infrastruktuuri (`pulumi up`) ajetaan käsin `aws`-repositoriosta. Ympäristömuuttujat:
  `AWS_REGION`, `AWS_DEPLOY_ROLE_ARN`.

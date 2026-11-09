# Kirvespeli web

Vaatii Node 24 (`.nvmrc`, vähintään 22.22).

## Sovelluksen käynnistys
```
npm install
npm start
```

Sovellus käynnistyy osoitteeseen http://localhost:1234 ja käyttää backendiä osoitteessa http://localhost:8080 (`.env.development`).

## Muut komennot
```
npm run build            # tuotantoversio hakemistoon dist/
npm run preview          # komponenttien esikatselu osoitteeseen http://localhost:1235
npm run format           # prettier
```

## Komponenttien esikatselu
`src/preview/` on erillinen Parcel-sivu, joka näyttää komponentit staattisella datalla (`src/preview/data/`) ulkoasun tarkistamista varten. Jokainen komponentti on oma tiedostonsa `src/preview/pages/`-hakemistossa:
```js
export default { title: "Card", group: "Components", variants: { card: () => <Card ... /> } }
```
Uusi sivu lisätään listaan `src/preview/pages/index.js`. Variantit näytetään allekkain, jokainen omassa `MemoryRouter`issa. Callback-propseina käytetään `logAction("nimi")`, joka kirjoittaa kutsun konsoliin ja sivun alareunaan. Esikatselu ei ole mukana tuotantoversiossa.

## Huomioita
- SVG-kuvat tuodaan `url:`-etuliitteellä (Parcelin tapa saada tiedoston URL).
- `package.json`:ssa `@parcel/resolver-default.packageExports` on päällä. Ilman sitä Parcel käyttää MUI:n CommonJS-versiota eikä tree-shaking toimi (bundle ~2x).

## Osoitteet
- Kehitys: `.env.development` (backend http://localhost:8080, WebSocket `ws://localhost:8080/api/refresh`).
- Tuotanto: https://kirvespeli.bizhop.fi (`.env.production`). Backend on saman osoitteen `/api`-polun takana, WebSocket `wss://kirvespeli.bizhop.fi/api/refresh`. Proxy-vaatimukset ovat backendin README:ssä.

## Kirjautumistila
Käyttäjän tilassa `loading` on `true` automaattisen kirjautumisen ajan (`AUTO_LOGIN` -> `AUTO_LOGIN_SUCCESS` / `AUTO_LOGIN_FAIL`). Etusivulle ohjataan vasta kun `loading` on `false` eikä käyttäjää ole kirjautuneena. Jos tokenia ei ole, automaattista kirjautumista ei yritetä.

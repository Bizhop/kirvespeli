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
npm run storybook        # Storybook osoitteeseen http://localhost:6006
npm run build-storybook  # Storybook hakemistoon storybook-static/
npm run format           # prettier
```

## Huomioita
- SVG-kuvat tuodaan `url:`-etuliitteellä (Parcelin tapa saada tiedoston URL). Storybookin Vite-konfiguraatio (`.storybook/main.js`) poistaa etuliitteen.
- `package.json`:ssa `@parcel/resolver-default.packageExports` on päällä. Ilman sitä Parcel käyttää MUI:n CommonJS-versiota eikä tree-shaking toimi (bundle ~2x).

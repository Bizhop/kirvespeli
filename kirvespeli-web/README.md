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
npm test                 # käden järjestys ja alkuperäisten kortti-indeksien säilyminen
```

## Pelaajamäärä ja liittyminen

Aulan Pelaajia-sarake näyttää pelaajamäärän muodossa `n/6`. Enimmäismäärä tulee
backendin `maxPlayers`-kentästä. Täyteen peliin ei voi liittyä. Peli alkaa vasta, kun pelaajia
on vähintään kaksi; siihen asti pelinäkymässä näkyy teksti `Odotetaan pelaajia (vähintään 2)`. Liittymisyrityksen
jälkeen pelitilanne haetaan uudelleen myös virheen yhteydessä; hylätyn yrityksen
yhteydessä näytetään backendin virheviesti, esimerkiksi `Peli on täynnä`.
Aulan pelinumero on laatikoitu painikelinkki, joka avaa pelinäkymän. Varsinainen
liittyminen tehdään pelinäkymän `Liity peliin` -painikkeella.

## Pistetaulukko

`ScoreCard` näyttää pelaajat istumajärjestyksessä, päättyneet pelit ja meneillään olevan pelin
(korostettu rivi). Pisteet piirretään käsin piirretyn näköisinä merkkeinä (`ScoreMark`):
1 = ympyrä, 2 = + pystyviiva, 3 = + vaakaviiva. Häviäjän ruutuun piirretään keppi tai kirves,
erän päättävän kirveen jälkeen on paksu viiva, ja alarivillä on pelaajien kirveet ja avoin
keppi. Kirves käyttää annettua `shared/icons/kirves-valk.png`-grafiikkaa ja keppi sen
varresta erotettua `keppi.png`-kuvaa. Pistemerkit käyttävät `currentColor`ia; häviömerkin
värin voi vaihtaa CSS-muuttujalla
`--score-loss-color`. Häviäjä voi ennen nostoa vaihtaa jakajan napsauttamalla pelaajan laatikkoa.
`DealerSelector` näyttää ohjeen pöydän keskellä; valinta toimii myös näppäimistöllä.
Nykyinen jakaja on merkitty, eikä hänen valintansa lähetä uutta toimintoa. Jakajaa voi
valita vain `SET_DEALER`-toiminnon ollessa sallittu; uusinnassa valinta on poistettu käytöstä.
Säännöt ovat backendin README:ssä.

## Komponenttien esikatselu
`src/preview/` on erillinen Parcel-sivu, joka näyttää komponentit staattisella datalla (`src/preview/data/`) ulkoasun tarkistamista varten. Jokainen komponentti on oma tiedostonsa `src/preview/pages/`-hakemistossa:
```js
export default { title: "Card", group: "Components", variants: { card: () => <Card ... /> } }
```
Uusi sivu lisätään listaan `src/preview/pages/index.js`. Variantti valitaan välilehdistä;
vain valittu variantti on kerrallaan renderöitynä omassa `MemoryRouter`issaan.
Vaihto nollaa variantin paikallisen tilan. Callback-propseina käytetään
`logAction("nimi")`, joka kirjoittaa kutsun konsoliin ja sivun alareunaan.
Esikatselu ei ole mukana tuotantoversiossa. Esikatselu käyttää tavallista vierivää
taustaa ilman pakotettua kompositiotasoa korttien animaatioiden piirtohäiriöiden välttämiseksi.

## Huomioita
- SVG-kuvat tuodaan `url:`-etuliitteellä (Parcelin tapa saada tiedoston URL).
- `package.json`:ssa `@parcel/resolver-default.packageExports` on päällä. Ilman sitä Parcel käyttää MUI:n CommonJS-versiota eikä tree-shaking toimi (bundle ~2x).
- `package.json`:n `allowScripts` estää Parcelin riippuvuuksien (`@parcel/watcher`, `@swc/core`, `lmdb`, `msgpackr-extract`) asennusskriptit. Ne ovat vain varakeinoja (käännös lähdekoodista / wasm), koska valmiit natiivibinäärit tulevat alustakohtaisista optional-paketeista. Uuden skriptillisen riippuvuuden voi tarkistaa komennolla `npm install-scripts ls` (npm 12+).

## Osoitteet
- Kehitys: `.env.development` (backend http://localhost:8080, WebSocket `ws://localhost:8080/api/refresh`).
- Tuotanto: https://kirvespeli.bizhop.fi (`.env.production`). Backend on saman osoitteen `/api`-polun takana, WebSocket `wss://kirvespeli.bizhop.fi/api/refresh`. Proxy-vaatimukset ovat backendin README:ssä.

## Kirjautumistila
Käyttäjän tilassa `loading` on `true` automaattisen kirjautumisen ajan (`AUTO_LOGIN` -> `AUTO_LOGIN_SUCCESS` / `AUTO_LOGIN_FAIL`). Etusivulle ohjataan vasta kun `loading` on `false` eikä käyttäjää ole kirjautuneena. Jos tokenia ei ole, automaattista kirjautumista ei yritetä.

## Pelipöytä ja teema

`RenderGame` käyttää samaa `Table`-näkymää pelissä ja lokin uusinnassa. Paikat valitaan
`Table.jsx`:n `seatPresets`-taulukosta 1–6 pelaajalle. Katsoja on alhaalla, seuraava pelaaja
vasemmalla ja pelijärjestys jatkuu myötäpäivään. Sivustakatsoja ja uusinta käyttävät
backendin istumajärjestystä. Uusinnassa ei ole omaa kättä, toimintopalkkia tai valintoja.
Pisteet ja viestit ovat leveällä näytöllä oikealla, kapealla pöydän alapuolella.

Yhteinen MUI-teema on `src/theme.js`:ssä; sovellus ja esikatselu käyttävät sitä.
Vihreä huopatausta tehdään CSS-liukuvärillä. Abel ja Alegreya Sans SC 700 tulevat
`@fontsource`-paketeista paikallisina fonttitiedostoina ilman Google Fonts -pyyntöjä.

`shared/Icon.jsx`:n kuvakartta määrittää kuvakkeet ja suomenkieliset selitteet. Vanhan
mallin jakaja- ja puhekuvat ovat `shared/icons/`-hakemistossa 128 px PNG-kuvina.
Otsikon kirves säilyttää alkuperäisen kokonsa. Ilmoittautunut pelaaja (`declared.svg`) on vaihdettava
paikkamerkki; lopullisen kuvan voi vaihtaa kuvakarttaan.
Jakaja- ja puhekuvakkeet piilotetaan tikkejä pelattaessa. Vuoro näkyy pelaajan paikan
hehkukorostuksena, ja pisteet näytetään vain pistetaulukossa.
`folded` himmentää pakkaan menneen ja `inGame: false` sivussa olevan pelaajan.

Kortin leveys tulee CSS-muuttujasta `--card-w`, korkeus suhteesta 10/7. Käden, pöydälle
pelattujen korttien, pakan ja pelaajan korttiselkien koot säädetään `style.css`:ssä.
`SvgImage` käyttää oletuksena kokoa 140×200 vain ilman kokopropseja tai CSS-luokkaa.
Puhe- ja valttivalinnat näytetään saman toimintopalkin kuvakkeina käden vieressä.
`SelectionButton` antaa molemmille saman läpinäkyvän ulkoasun, selitteen ja orgaanisen
nosto-/kasvuanimaation. Ryhmien näkyvät otsikot ovat `Puhe` ja `Valitse valtti`.
Kuvakkeiden suomenkieliset selitteet näkyvät työkaluvihjeinä ja saavutettavina niminä.
Valinnat toimivat näppäimistöllä; vähennetyn liikkeen asetus poistaa siirtymäanimaation
ja pienentää korostusta. Valttivalinnasta jätetään nykyinen valtti pois.
`SPEAK` ja `SPEAK_SUIT` ovat edelleen erilliset backend-toiminnot.
Kun valtti on tiedossa, oma käsi järjestetään vasemmalta oikealle: punainen jokeri,
musta jokeri, jätkät (risti, pata, hertta, ruutu), muut valtit ässästä kakkoseen ja lopuksi
muut maat ässästä kakkoseen. Muiden maiden keskinäinen järjestys on aina risti, pata,
hertta, ruutu (valtti ohitetaan), joten se pysyy vakaana korttien poistuessa kädestä.
Ilman valttia näytetään saatu järjestys. Järjestys koskee vain näkymää: `handOrder.js`
säilyttää korttien alkuperäiset indeksit `PLAY_CARD`- ja `DISCARD`-kutsuja varten eikä
muuta backendin korttilistaa tai pöydälle pelattujen korttien järjestystä. Päätöstä odottava
ylimääräinen kortti pysyy erillisenä käden vieressä.
Käden kortteja voi valita vain `PLAY_CARD`- tai `DISCARD`-toiminnon ollessa sallittu.
Voitetut tikit ovat kuvapuoli alaspäin; viimeisen voittokortin voi paljastaa 1,5 sekunniksi.
Viiden tikin jälkeen Näytä kortit paljastaa kaikki pöytäkortit.

Esikatselun `Table` näyttää kaikki pelaajamäärät sekä odotuksen, noston, puheen,
valttivalinnan, jakajavalinnan, ylimääräisen kortin, hylkäyksen, sivustakatsojan ja uusinnan.
`Seat` näyttää pelaajan eri tilat ja `Icon` kaikki kuvakkeet.
`Hand` esittelee oman käden erikseen: pelattava käsi, vuoron odotus, hylkäys, ylimääräinen
kortti, viimeinen kortti ja tyhjä käsi. Esikatselu käyttää pelin korttikokoa ja käsiasettelua. Mukana ovat myös
jokerit ja jätkät, valintaa edeltävä järjestys sekä valtin vaihdon vaikutus käteen.

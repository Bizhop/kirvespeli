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
npm test                 # käden järjestys, alkuperäiset kortti-indeksit ja korttien liikkeen tilavertailu
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
Sivuvalikon (‹) ja otsikon välilehtineen (▴) voi piilottaa mobiiliasettelun tarkistamista
varten; yläkulmiin jäävät pienet palautuspainikkeet, ja valinta muistetaan selaimessa.
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
Poistumistaan kuittaamaton pelaaja (`departurePending`) näytetään tilalla Poistumassa
himmentämättä, jotta hänen korttinsa näkyvät.

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
Kolmeen pisteeseen päässyt pelaaja saa painikkeen Kuittaa poistuminen; muut näkevät tekstin
`Odotetaan poistumisen kuittausta: <nimet>`. Kaikille näytetään paikallisesti laskeva
`Peli jatkuu automaattisesti <n> s kuluttua`, ja pelin ylläpitäjä saa painikkeen Jatka peliä
(`CONTINUE_GAME`), joka ei korosta häntä vuorossa olevaksi. Kuittausta odotettaessa Näytä kortit on käytössä,
vaikka jako olisi päättynyt ennen viittä tikkiä.

Esikatselun `Table` näyttää kaikki pelaajamäärät sekä odotuksen, noston, puheen,
valttivalinnan, jakajavalinnan, ylimääräisen kortin, hylkäyksen, sivustakatsojan, uusinnan
ja poistumisen kuittauksen tilat (oma, muiden odotus, ylläpitäjän jatko, usea poistuja, kuitattu, ennenaikainen
jaon loppu ja pelin päättävä poistuminen).
`Seat` näyttää pelaajan eri tilat ja `Icon` kaikki kuvakkeet.
`Hand` esittelee oman käden erikseen: pelattava käsi, vuoron odotus, hylkäys, ylimääräinen
kortti, viimeinen kortti ja tyhjä käsi. Esikatselu käyttää pelin korttikokoa ja käsiasettelua. Mukana ovat myös
jokerit ja jätkät, valintaa edeltävä järjestys sekä valtin vaihdon vaikutus käteen.

## Korttien animaatiot

Animaatiot kuvaavat vain backendin vahvistamia tilannekuvia; Redux-tilaa tai toimintoja ei
viivytetä animaatioiden takia. `cardMotion.js` vertaa edellistä ja uutta tilannekuvaa ja
kertoo sallitut liikkeet, `CardMotion.jsx` mittaa korttien paikat pöydän suhteen ja toteuttaa
liikkeet Web Animations -rajapinnalla. Ajoitukset ovat `motion.js`:ssä.

- Oman käden kortit tunnistetaan kortin koodista; `PLAY_CARD`- ja `DISCARD`-indeksit ovat
  erillään. Kortit siirtyvät uusille paikoilleen valtin vaihtuessa ja korttien poistuessa.
- Oma pelattu kortti lentää kädestä omaan pöytäriviin vasta vahvistuksen jälkeen. Muiden
  pelaama kortti ilmestyy suoraan pöytäriviin lyhyellä häivytyksellä.
- Uusi jako tulee pakasta omaan käteen porrastetusti (enintään noin 0,6 s). Pidetty
  ylimääräinen kortti siirtyy käteen; hylätty ja pöydältä poistuva kortti häivytetään.
- Nosto- ja valttikortti häivytetään paikalleen, ja pakka siirtyy jakajan vaihtuessa.
- Kortti kääntyy kuva- ja selkäpuolen välillä (voitetut tikit, 1,5 s paljastus, Näytä kortit).
  Piilotetun kortin kuvaa ei ole DOM:ssa.

Yksityisyys: liikkeeseen rekisteröidään vain oma käsi, pöytärivit, pakka sekä nosto- ja
valttikortit. Muiden pelaajien korttiselät ja oman paikan korttimäärä päivittyvät heti ilman
liikettä, eikä mikään liike riipu toisen pelaajan piilotetuista korteista, niiden järjestyksestä
tai ylimääräisen kortin sijainnista. Testit tarkistavat, että samat julkiset muutokset antavat
kaikille katsojille samat julkiset ohjeet.

Ilman animaatiota näytetään: ensimmäinen tilannekuva, pelin tai pelaajien vaihtuminen,
WebSocket-yhteyden palautuminen (tilanne haetaan uudelleen), uusinta, piilossa oleva
välilehti sekä epäselvät muutokset (esim. pelaajalla useampi uusi pöytäkortti kerralla, eli
välistä puuttuu tilannekuvia). Backend voi pisteyttää ja jakaa uuden käden samassa
toiminnossa, jolloin viimeistä tikkiä ei näytetä vaan pöytä häivytetään ja uusi käsi jaetaan.
Uusi tilannekuva keskeyttää käynnissä olevat liikkeet, ja leveyden muutos mittaa paikat
uudelleen. Vähennetyn liikkeen asetuksella kaikki tilat näytetään heti ilman siirtoja tai
kääntöjä; asetuksen muutos huomioidaan heti.

Esikatselun `CardMotion` käy läpi tilannekuvasarjoja painikkeilla: pelatut kortit ja uusi
jako, valtin vaihto ja ylimääräinen kortti, nosto, paljastus, nopeat päivitykset sekä samat
julkiset tapahtumat kahden pelaajan ja sivustakatsojan näkymissä.

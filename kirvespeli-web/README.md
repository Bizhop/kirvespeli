# Kirvespeli web

Vaatii Deno 2 (`.dvmrc`). Riippuvuudet ja komennot ovat tiedostossa `deno.json`, lukitus `deno.lock`-tiedostossa; `package.json` ei ole käytössä.

## Sovelluksen käynnistys
```
deno install
deno task start
```

Sovellus käynnistyy osoitteeseen http://localhost:1234 ja käyttää backendiä osoitteessa http://localhost:8080 (`.env.development`).

## Muut komennot
```
deno task build           # tuotantoversio hakemistoon dist/
deno task preview         # komponenttien esikatselu osoitteeseen http://localhost:1235
deno task format          # prettier
deno task test          # käden järjestys, kortti-indeksit, liikkeen tilavertailu ja esikatseludata
```

## Pelaajamäärä ja liittyminen

Aulan Pelaajia-sarake näyttää pelaajamäärän muodossa `n/6`. Enimmäismäärä tulee
backendin `maxPlayers`-kentästä. Täyteen peliin ei voi liittyä. Peli alkaa vasta, kun pelaajia
on vähintään kaksi; siihen asti pelinäkymässä näkyy teksti `Odotetaan pelaajia (vähintään 2)`. Liittymisyrityksen
jälkeen pelitilanne haetaan uudelleen myös virheen yhteydessä; hylätyn yrityksen
yhteydessä näytetään backendin virheviesti, esimerkiksi `Peli on täynnä`.
Aulan pelilista voidaan suodattaa painikkeilla `Liityttävät` (`canJoin`), `Omat pelit` (`joined`: käyttäjä on pelaajana) ja `Kaikki` (oletus).
Aulan pelinumero on laatikoitu painikelinkki, joka avaa pelinäkymän. Varsinainen
liittyminen tehdään pelinäkymän `Liity peliin` -painikkeella.

## Pistetaulukko

`ScoreCard` näyttää pelaajat istumajärjestyksessä, päättyneet pelit ja meneillään olevan pelin
(korostettu rivi). Pisteet piirretään käsin piirretyn näköisinä merkkeinä (`ScoreMark`):
1 = ympyrä, 2 = + pystyviiva, 3 = + vaakaviiva. Häviäjän ruutuun piirretään keppi tai kirves,
ja erän päättävän kirveen jälkeen on paksu viiva. Kirves käyttää annettua `shared/icons/kirves-valk.png`-grafiikkaa ja keppi sen
varresta erotettua `keppi.png`-kuvaa. Pistemerkit käyttävät `currentColor`ia; häviömerkin
värin voi vaihtaa CSS-muuttujalla
`--score-loss-color`. Saman ruudun aiemmat vedot pysyvät samanmuotoisina pisteiden kasvaessa.

Pistetaulukko on paperilappuna pöydän oikeassa alakulmassa (`ScoreSlip`). Lapun napautus avaa
taulukon pöydän päälle; uusi napautus, napautus lapun ulkopuolelle tai Esc sulkee sen.
Kun vahvistettu tilannekuva tuo uusia pisteitä, lappu avautuu itsestään viimeisen tikin
näyttöajan (3 s) jälkeen, uudet pisteet piirretään vetona kerrallaan istumajärjestyksessä ja sen jälkeen keppi tai kirves
(`ScoreMark`: viivat `stroke-dashoffset`-animaatiolla, keppi ja kirves pyyhkäisten), ja lappu
sulkeutuu 2,5 s viimeisen merkin jälkeen, ellei katsoja ole avannut tai koskenut sitä.
`scoreChanges.js` päättelee uudet merkit ja niiden ajoituksen; ajoitukset ovat `motion.js`:ssä.
Merkit ovat DOM:ssa heti ja piilossa piirtoon asti, joten Redux-tilaa ei viivytetä.
Ilman piirtoa näytetään ensimmäinen tilannekuva, pelin tai pelaajien vaihtuminen,
WebSocket-yhteyden palautuminen, uusinta ja useamman pelin hyppäys kerralla. Vähennetyn
liikkeen asetuksella merkit ilmestyvät samaan aikaan ilman piirtoa ja lappu ilman siirtymää.

Pelin viestit avataan pelin otsikon vieressä olevasta kuvakkeesta (`MessagesButton`); piste
kuvakkeessa kertoo viesteistä, jotka ovat tulleet viimeisen katsomisen jälkeen. Uusinnassa
kuvake on Tilanne-otsikon vieressä.

Häviäjä voi ennen nostoa vaihtaa jakajan napsauttamalla pelaajan laatikkoa.
`DealerSelector` näyttää ohjeen pöydän keskellä; valinta toimii myös näppäimistöllä.
Nykyinen jakaja on merkitty, eikä hänen valintansa lähetä uutta toimintoa. Jakajaa voi
valita vain `SET_DEALER`-toiminnon ollessa sallittu; uusinnassa valinta on poistettu käytöstä.
Säännöt ovat backendin README:ssä.

## Komponenttien esikatselu
`src/preview/` on erillinen Vite-sivu (`vite.preview.config.js`, portti 1235), joka näyttää komponentit staattisella datalla (`src/preview/data/`) ulkoasun tarkistamista varten. Jokainen komponentti on oma tiedostonsa `src/preview/pages/`-hakemistossa:
```js
export default { title: "Card", group: "Components", variants: { card: () => <Card ... /> } }
```
Uusi sivu lisätään listaan `src/preview/pages/index.js`. Variantti valitaan välilehdistä;
vain valittu variantti on kerrallaan renderöitynä omassa `MemoryRouter`issaan.
Vaihto nollaa variantin paikallisen tilan. Callback-propseina käytetään
`logAction("nimi")`, joka kirjoittaa kutsun konsoliin ja sivun alareunaan.
Sivuvalikon (‹) ja otsikon välilehtineen (▴) voi piilottaa mobiiliasettelun tarkistamista
varten; yläkulmiin jäävät pienet palautuspainikkeet, ja valinta muistetaan selaimessa.
Esikatselun pelitilanteet (`src/preview/data/`) tehdään pelaamalla toimintoja pienellä
Kirves-moottorilla (`kirves.js`), joka noudattaa backendin sääntöjä: nosto, jako, ässä tai
kakkonen valttikorttina, hylkäys, puhe, valtin vaihto, tikit, pakkaan meno, pisteytys,
poistumiset ja pelin loppu. Korttimäärät, pakan koko, tikkien voittajat, vuorot, toiminnot,
pisteet, viestit ja toimintoloki seuraavat pelatuista toiminnoista. Sääntöjä rikkova toiminto
kaatuu jo datan latauksessa, ja `deno task test` tarkistaa kaikki tilanteet.
Esikatselu ei ole mukana tuotantoversiossa. Esikatselu käyttää tavallista vierivää
taustaa ilman pakotettua kompositiotasoa korttien animaatioiden piirtohäiriöiden välttämiseksi.

## Huomioita
- Vite ajetaan Denolla (`deno run -A npm:vite`); riippuvuudet asennetaan `node_modules`-hakemistoon (`nodeModulesDir: auto`). Testit ovat `node:test`-testejä, jotka `deno test` ajaa.
- Rakennustyökalu on Vite (`vite.config.js` sovellukselle, `vite.preview.config.js` esikatselulle). Sovelluksen juuri on `src/`, tuotos `dist/` ja `.env`-tiedostot luetaan projektin juuresta.
- `deno task build` tyhjentää `dist/`-hakemiston. Esikatselu ei kuulu tuotantoversioon: `src/preview/` ei ole sovelluksen tuontipolulla.
- Kuvat (SVG, PNG) tuodaan tavallisella tuonnilla (`import x from "./x.svg"`), ja `construct.sh` tuottaa saman muodon. `build.assetsInlineLimit` on 0, joten kuvat pysyvät erillisinä välimuistitettavina tiedostoina eivätkä paisuta JS-pakettia.
- Selainkohde on `build.target: "baseline-widely-available"` (Vite 8:n oletus, nimetty näkyviin). Parcelin `browserslist` ei ole enää käytössä.
- Ympäristömuuttujat luetaan `import.meta.env`-olion kautta, ja vain `VITE_`-alkuiset muuttujat päätyvät selaimeen.

## Osoitteet
- Kehitys: `.env.development` (`VITE_API_URL` http://localhost:8080, `VITE_WEB_SOCKET_URL` `ws://localhost:8080/api/refresh`).
- Tuotanto: https://kirvespeli.bizhop.fi (`.env.production`, samat `VITE_`-muuttujat). Backend on saman osoitteen `/api`-polun takana, WebSocket `wss://kirvespeli.bizhop.fi/api/refresh`. Proxy-vaatimukset ovat backendin README:ssä.

## Kirjautumistila
Käyttäjän tilassa `loading` on `true` automaattisen kirjautumisen ajan (`AUTO_LOGIN` -> `AUTO_LOGIN_SUCCESS` / `AUTO_LOGIN_FAIL`). Etusivulle ohjataan vasta kun `loading` on `false` eikä käyttäjää ole kirjautuneena. Jos tokenia ei ole, automaattista kirjautumista ei yritetä.

## Pelipöytä ja teema

`RenderGame` käyttää samaa `Table`-näkymää pelissä ja lokin uusinnassa. Paikat valitaan
`Table.jsx`:n `seatPresets`-taulukosta 1–6 pelaajalle. Katsoja on alhaalla, seuraava pelaaja
vasemmalla ja pelijärjestys jatkuu myötäpäivään. Sivustakatsoja ja uusinta käyttävät
backendin istumajärjestystä. Uusinnassa ei ole omaa kättä, toimintopalkkia tai valintoja.
Pöytä on koko pelialueen levyinen (enintään 1000 px); pisteet ovat pöydän lapussa ja viestit
otsikon kuvakkeen takana.

Live-peli välittää `fitViewport`-asetuksen `RenderGame`-komponentille. Puhelimessa ja
tabletilla pöytä käyttää otsikon jälkeen jäljellä olevan näkymäkorkeuden; käsi ja
toimintopalkki pysyvät samalla ruudulla. `RenderGame` mittaa pelialueen yläreunan
sivun alusta `--game-top`-muuttujaan ja päivittää sen, kun yläpuolinen sisältö (yläpalkki,
otsikko) muuttaa kokoaan. Käden ja toimintopalkin rivit varaavat kiinteän korkeuden, joten
pöytä ei muuta kokoaan jaossa, kortteja pelattaessa tai toimintojen vaihtuessa; vain
ylimääräisen kortin seliteteksti ja pitkät poistumisviestit voivat kasvattaa riviä. Pöydän
istuimet, pakka, keskikortit ja tikkikortit skaalautuvat pöydän mittojen mukaan nimetyssä
`card-table`-kokosäiliössä. Tietokoneella pöytä säilyttää 610 px korkeuden. Uusinnan
taulukoko pysyy kiinteänä.

Esikatselun `Table`-sivulla `fit ...` -variantit näyttävät samat tilanteet sovitettuna
näkymäkorkeuteen. `KirvesGame`-sivu näyttää yläpalkin ja otsikon kanssa koko pelisivun
sellaisena kuin se on live-pelissä. Mukana ovat 1–6 pelaajaa, puhe, valtin valinta, ylimääräinen kortti,
hylkäys ja poistumistilanteet. Tuettu mobiiliasettelu on puhelimessa pystysuunta ja iPad
Pro 11" (4. sukupolvi) pysty- ja vaakasuunta; puhelimen vaakasuuntaa ei ole sovitettu.
Esikatselun `ScoreSlip`-sivun `drawing`-variantit käyvät läpi jakojen pisteet, kepin ja
kirveen sääntöjen mukaisesti (Seuraava tilanne, Toista, Alusta), myös näkymäkorkeuteen
sovitettuna. `ScoreMark`-sivun `drawing` piirtää kaikki merkit, ja `MessagesButton`-sivu
näyttää viestikuvakkeen.

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
Pelatessa pyydettyä maata on tunnustettava: muut kortit himmennetään ja niiden valinta estetään,
jos kädessä on pyydettyä maata. Jätkät ja jokerit ovat aina valttia. Poisheitossa (`DISCARD`)
maapakkoa ei käytetä. Backend tarkistaa peluun myös palvelimella.
Tikin voittokortti pysyy kuvapuoli ylöspäin kolme sekuntia, korostuu ja pomppaa kahdesti.
Sen jälkeen se kääntyy selkäpuoli ylöspäin puolen sekunnin animaatiolla. Seuraavat päivitykset
eivät aloita korostusta uudelleen. Vähennetty liike poistaa pompun ja käännösanimaation,
mutta säilyttää kolmen sekunnin näyttöajan; lokitoistossa viivettä ei käytetä.
Viimeisen voittokortin voi paljastaa uudelleen 1,5 sekunniksi.
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
jokerit ja jätkät sekä valtin vaihdon vaikutus käteen.

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
  Nostossa saatu ylimääräinen kortti ei estä uusien käsikorttien jakoanimaatiota.
- Nosto- ja valttikortti häivytetään paikalleen, ja pakka siirtyy jakajan vaihtuessa.
- Kortti kääntyy kuva- ja selkäpuolen välillä (voitetut tikit, 1,5 s paljastus, Näytä kortit).
  Piilotetun kortin kuvaa ei ole DOM:ssa.

Yksityisyys: liikkeeseen rekisteröidään vain oma käsi, pöytärivit, pakka sekä nosto- ja
valttikortit. Muiden pelaajien korttiselät ja oman paikan korttimäärä päivittyvät heti ilman
liikettä, eikä mikään liike riipu toisen pelaajan piilotetuista korteista, niiden järjestyksestä
tai ylimääräisen kortin sijainnista. Testit tarkistavat, että samat julkiset muutokset antavat
kaikille katsojille samat julkiset ohjeet.

Ilman animaatiota näytetään: ensimmäinen tilannekuva, pelin tai pelaajien vaihtuminen,
WebSocket-yhteyden palautuminen (tilanne haetaan uudelleen), uusinta (myös korttien käännöt), piilossa oleva
välilehti sekä epäselvät muutokset (esim. pöydällä useampi uusi pelattu kortti kerralla, eli
välistä puuttuu tilannekuvia). Backend voi pisteyttää ja jakaa uuden käden samassa
toiminnossa, jolloin viimeistä tikkiä ei näytetä vaan pöytä häivytetään ja uusi käsi jaetaan.
Uusi tilannekuva keskeyttää käynnissä olevat liikkeet, ja leveyden muutos mittaa paikat
uudelleen. Vähennetyn liikkeen asetuksella kaikki tilat näytetään heti ilman siirtoja tai
kääntöjä; asetuksen muutos huomioidaan heti.

Esikatselun `CardMotion` käy läpi tilannekuvasarjoja painikkeilla: pelatut kortit ja uusi
jako, valtin vaihto ja ylimääräinen kortti, nosto, paljastus, nopeat päivitykset, uusintahypyt sekä samat
julkiset tapahtumat kahden pelaajan ja sivustakatsojan näkymissä.

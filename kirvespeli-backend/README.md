# Kirvespeli backend

Spring Boot 4.1, Java 25, Gradle 9, PostgreSQL 14.

## Käynnistys
```
export KIRVES_JWT_SECRET=...        # pakollinen
export KIRVES_GOOGLE_CLIENT_ID=...
./buildAndRun.sh                    # Docker image build and docker compose (backend + database)
```
`./gradlew test` runs the tests. The Docker image builds the application with Gradle.

## Pelaajamäärä

Pelissä voi olla 2–6 pelaajaa, pelin luoja mukaan lukien. Peli alkaa ensimmäisellä nostolla,
joka on mahdollinen vasta toisen pelaajan liityttyä: yksin pelin luonut ei saa
nostotoimintoa, ja nostoyritys alle kahdella pelaajalla hylätään HTTP 400 -virheellä
`Peliin tarvitaan vähintään 2 pelaajaa`. Kuudennen pelaajan
liityttyä peli sulkeutuu uusilta pelaajilta. Liittyminen sulkeutuu myös ensimmäiseen
nostoon. Aulan rajapinta palauttaa pelaajamäärän (`players`), enimmäismäärän
(`maxPlayers`) ja käyttäjäkohtaisen liittymistilan (`canJoin`). Täyteen peliin
liittyminen hylätään HTTP 400 -virheellä: `Peli on täynnä`.

## Maan tunnustaminen

Pyydettyä maata on pelattava, jos sitä on kädessä. Kaikki jätkät ja molemmat jokerit
kuuluvat valttimaahan, eivät korttiin painettuun maahan. Jos pyydettyä maata ei ole,
saa pelata minkä tahansa kortin. Virheellinen valinta hylätään HTTP 400 -virheellä
`Pitää pelata pyydettyä maata` ennen pelitilan muuttamista. Uuden tikin aloittaja saa
valita minkä tahansa kortin. Vanhojen toimintolokien toisto ja palautus säilyttävät
myös ennen maapakon tarkistusta hyväksytyt peluut; palautetun pelin uudet siirrot tarkistetaan.

## Pisteytys

- Jaon voittaja(t) saa pisteen. Kolmen pisteen pelaaja on sivussa, kunnes häviäjä selviää.
  Peli päättyy, kun pelissä on jäljellä yksi pelaaja: hän on häviäjä (viesti `Hävisi: <nimi>`).
  Useampi voittaja voi päästä kolmeen pisteeseen samassa jaossa.
- Kolmeen pisteeseen päässyt pelaaja kuittaa poistumisensa toiminnolla `ACKNOWLEDGE_DEPARTURE`.
  Siihen asti pöytä säilyy ennallaan: pelaajan kortit ja tikit pysyvät näkyvissä, eikä muilla
  ole toimintoja. Usean poistujan jaossa jokainen kuittaa erikseen, ja vasta viimeinen kuittaus
  vie peliä eteenpäin (seuraava jakaja ja nosto tai pelin päättyminen ja häviäjän kirjaus).
  Poistuneen pelaajan pöydälle pelatut kortit poistuvat seuraavassa nostossa muiden tapaan.
- Kuittaamattomat poistumiset kuitataan automaattisesti 60 sekunnin kuluttua jaon päättymisestä
  (viesti `Poistuminen kuitattiin automaattisesti`). Pelin ylläpitäjä voi sitä ennen jatkaa peliä
  toiminnolla `CONTINUE_GAME` (viesti `<nimi> jatkoi peliä`), joka kuittaa kaikki poistumiset.
  Ylläpitäjä saa toiminnon, kun joku muu kuin hän itse odottaa kuittausta. Määräaika
  (`departureDeadline`) tallentuu pelidataan, joten ajastus jatkuu palvelimen uudelleenkäynnistyksen
  jälkeen; jo umpeutunut määräaika kuitataan heti, kun peli ladataan. Pelitilanteen palautus
  antaa odottaville poistumisille uuden 60 sekunnin määräajan. Ajastettu kuittaus lähettää
  `/topic/refresh`-viestin kuten muutkin toiminnot.
- Erän ensimmäinen häviö on keppi, saman pelaajan toinen häviö kirves (viesti
  `<nimi> sai kirveen`). Kirves päättää erän: kaikkien kepit nollautuvat ja seuraava erä alkaa.
  Pelaamista voi jatkaa rajatta, ja koko historia säilyy.
- Häviäjä jakaa seuraavan pelin ensimmäisen jaon, mutta voi ennen nostoa valita jakajaksi kenet
  tahansa pelaajista toiminnolla `SET_DEALER` (`{"action": "SET_DEALER", "dealer": "<email>"}`).
  Valintaa voi vaihtaa nostoon asti.

Pelin tulosteessa `scores` on lista `{email, nickname, score}` istumajärjestyksessä (ei
katsojan mukaan kierrettynä), ja `scoresHistory` on lista päättyneitä pelejä
`{scores, loser, axe}`, jossa `loser` on häviäjän sähköposti ja `axe` kertoo kirveestä.
Tallennettu pelidata käyttää samaa muotoa. Pelaajan `inGame` kertoo, onko hän vielä mukana
pelissä. `folded` kertoo pakkaan menosta; kolmen pisteen takia sivussa oleva pelaaja
tunnistetaan `inGame: false` -arvosta, ja `departurePending` kertoo kuittaamattomasta
poistumisesta ja `departureSecondsLeft` sekunnit automaattiseen kuittaukseen. `players` sisältää aina kaikki pelaajat istumajärjestyksessä katsojasta alkaen,
myös sivussa olevat.

## Tietokantaskeema ja lempinimet

Liquibase-changelog on nollattu neljään taulukohtaiseen changesetiin. Vanhan changelogin
mukaiset paikalliset tietokannat pitää luoda uudelleen ennen käynnistystä: vanhan kannan
Liquibase-historia ei vastaa uutta changelogia. Esimerkiksi `docker compose down -v`
poistaa paikallisen Compose-tietokannan tiedot; `docker compose up -d db` luo tyhjän kannan.
Tämä poistaa kaikki paikalliset käyttäjät, pelit ja toimintolokit.

Changelog ei lisää testikäyttäjiä. Uusi käyttäjä saa yksilöllisen lempinimen muodossa
`Pelaaja-<6 satunnaista merkkiä>` (numerot ja kirjaimet a–f), joka näkyy heti kirjautumisen
jälkeen ja jonka voi vaihtaa profiilissa. Lempinimestä poistetaan ympäröivät välilyönnit;
tyhjä tai yli 100 merkin nimi hylätään. Sähköpostiosoitetta ei käytetä lempinimen varana.

## Tietokantatestit (PostgreSQL)
Valinnainen testisarja (`src/dbTest`) ajetaan oikeaa PostgreSQL:ää vasten. Se ei ole osa `build`/`check`/`test`-ajoa eikä Docker-buildia.
```
docker compose up -d db     # PostgreSQL 14 osoitteeseen localhost:5432
./gradlew dbTest
docker compose stop db
```
Ajo luo palvelimelle tilapäisen tietokannan `kirves_test_<satunnainen>`, ajaa siihen Liquibase-changelogin ja poistaa kannan ajon lopuksi. Testit ajetaan ilman testin omaa transaktiota (kuten sovellus), ja taulut tyhjennetään ennen jokaista testiä.

Yhteys: `KIRVES_DBTEST_URL` (oletus `jdbc:postgresql://localhost:5432/kirves`, käytetään `CREATE DATABASE`/`DROP DATABASE` -komentoihin), `KIRVES_DBTEST_USERNAME` ja `KIRVES_DBTEST_PASSWORD` (oletus `kirves`). Käyttäjällä pitää olla oikeus luoda tietokantoja. Jos ajo keskeytetään väkisin, kanta voi jäädä jäljelle: `DROP DATABASE kirves_test_... WITH (FORCE)`.

## Osoitteet
- REST-rajapinta on kokonaisuudessaan polun `/api` alla (`util/WebConfig.java`).
- WebSocket (STOMP, SockJS-varmistus) on osoitteessa `/api/refresh`. Viestit julkaistaan aiheeseen `/topic/refresh`.
- `GET /api/health/live` on julkinen elävyysvastaus (`{"status":"UP"}`). Se ei vaadi kirjautumista eikä käytä tietokantaa; julkaisun tarkistus ja valvonta käyttävät sitä.

## Asetukset
- `kirves.allowed-origins` (ympäristömuuttuja `KIRVES_ALLOWED_ORIGINS`): pilkulla erotettu lista sallituista origineista.
  Käytössä sekä CORSissa että WebSocketissa. Oletus `http://localhost:1234,http://localhost:8080`, tuotannossa `https://kirvespeli.bizhop.fi`.
  Origin pitää listata WebSocketille vaikka käyttö olisi samasta originista, koska proxyn takana Host-otsikko ei välttämättä täsmää.

## Tuotanto: https://kirvespeli.bizhop.fi
Web ja backend ovat samassa originissa, joten CORSia ei tarvita. Käänteisen proxyn pitää:
- välittää `/api/*` backendille poistamatta `/api`-etuliitettä
- välittää WebSocketin upgrade-otsikot (`Upgrade`, `Connection`) polulle `/api/refresh`
- tarjota muun sisällön webin `dist/`-hakemistosta

Google OAuth -clientin sallittuihin origineihin pitää lisätä `https://kirvespeli.bizhop.fi`.

Tuotantoympäristö (AWS: EC2, Caddy proxynä, jaettu Aurora-tietokanta) on määritelty erillisessä `aws`-repositoriossa, `../../aws`
(`apps/kirvespeli/README.md`). Julkaisu tapahtuu GitHub Actionsilla, kun muutokset yhdistetään `main`-haaraan (ks. juuren README).
Backendin elävyystarkistus on `GET /api/health/live`.

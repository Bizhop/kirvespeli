# Kirvespeli backend

Spring Boot 4.1, Java 25, Gradle 9, PostgreSQL 14.

## Käynnistys
```
export KIRVES_JWT_SECRET=...        # pakollinen
export KIRVES_GOOGLE_CLIENT_ID=...
./buildAndRun.sh                    # Docker image build and docker compose (backend + database)
```
`./gradlew test` runs the tests. The Docker image builds the application with Gradle.

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

# Kirvespeli backend

Spring Boot 4.1, Java 25, Gradle 9, PostgreSQL 14.

## Käynnistys
```
export KIRVES_JWT_SECRET=...        # pakollinen
export KIRVES_GOOGLE_CLIENT_ID=...
./buildAndRun.sh                    # Docker image build and docker compose (backend + database)
```
`./gradlew test` runs the tests. The Docker image builds the application with Gradle.

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

# Local dev setup (tested on an apple M1 machine)

### Requirements
* Java >= 11 (adopt OpenJDK HS JVM) (eg. 11.0.11.hs-adpt from sdkman)
* MySQL 5.7 (image -> amd64/mysql:5.7)

### bootRun and docker-compose

* bootRun for the fineract backend
```
./gradlew bootRun
```

* docker-compose for the dependencies - mysql and mifos-ui
* mysql data is persisted in the host machine (inside the repo) under `volume/mysql-data`
```
docker-compose -f local-dev/docker-compose.yml up

# for cleanup
docker-compose -f local-dev/docker-compose.yml down
```

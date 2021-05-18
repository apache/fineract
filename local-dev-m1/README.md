# Local dev setup (for apple M1)

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
docker-compose -f local-dev-m1/docker-compose.yml up

# for cleanup
docker-compose -f local-dev-m1/docker-compose.yml down
```

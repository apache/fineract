# Local dev setup (tested on an apple M1 machine)

### Requirements
* Java >= 11 (adopt OpenJDK HS JVM) (eg. 11.0.11.hs-adpt from sdkman)
* MySQL 5.7 (image -> amd64/mysql:5.7)

### BootRun and docker-compose

* bootRun for the fineract backend
```
./gradlew bootRun
```

* docker-compose for the dependencies - mysql and mifos-ui
* mysql data is persisted in the host machine (inside the repo) under `local-dev/volume/mysql-data`
```
docker-compose -f local-dev/docker-compose.yml up

# for cleanup
docker-compose -f local-dev/docker-compose.yml down
```

### Util scripts

Note: every scripts are executable from the root of the project. 

TODO: works for now, might have to make it more usable later on.

- for linting
```
./local-dev/lint.sh
```

- for doing a fast gradle build
```
./local-dev/build-fast.sh
```

- for running all the tests (including integration tests using a mysql image without data persistance after container destroy)
```
./local-dev/test.sh
```

### ChangeLog

All new LINUS specific updates done on top of Fineract are listed in the [CHANGELOG](CHANGELOG.md) file
package org.apache.fineract.infrastructure.core.boot.db;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;

import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DB;

public class MariaDB4jSetupService {

    private DB db;

    @Autowired
    public MariaDB4jSetupService(DB db) {
        this.db = db;
    }

    @PostConstruct
    protected void setUpDBs() throws ManagedProcessException {
        db.createDB(getTenantDBName());
        db.createDB("mifostenant-default");
        // Note that we don't need to initialize the DBs, because
        // the TenantDatabaseUpgradeService will do this in just a moment.
    }

    public String getTenantDBName() {
        return "mifosplatform-tenants";
    }

    @PreDestroy
    protected void stop() throws ManagedProcessException {
        db = null;
    }
}
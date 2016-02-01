/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.core.boot.db;

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
    protected void setUpMifosDBs() throws ManagedProcessException {
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
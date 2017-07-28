/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
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
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
package org.apache.fineract.cob.writer.probe;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDate;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

/**
 * Stand-in for a COB account in {@code COBWriterScanModeTest}: optimistically locked like {@code Loan}, with a counter
 * that each business step run increments so a doubly applied step shows. Lives in its own package so the test's
 * persistence unit scans exactly this entity.
 */
@Entity
@Table(name = "cob_probe_account")
public class CobProbeAccount extends AbstractPersistableCustom<Long> {

    @Version
    @Column(name = "version")
    private Long version;

    @Column(name = "runs")
    private int runs;

    @Column(name = "last_closed_business_date")
    private LocalDate lastClosedBusinessDate;

    public void recordRun() {
        runs++;
    }

    public void setLastClosedBusinessDate(LocalDate lastClosedBusinessDate) {
        this.lastClosedBusinessDate = lastClosedBusinessDate;
    }
}

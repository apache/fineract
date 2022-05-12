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
package org.apache.fineract.infrastructure.creditbureau.domain;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Table;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name = "m_creditreport")
public final class CreditReport extends AbstractPersistableCustom {

    @Column(name = "credit_bureau_id")
    private Long creditBureauId;

    @Column(name = "national_id")
    private String nationalId;

    @Basic(fetch = FetchType.LAZY)
    @Column(name = "credit_reports")
    private byte[] creditReports;

    private CreditReport() {}

    public static CreditReport instance(final Long creditBureauId, final String nationalId, final byte[] creditReports) {
        return new CreditReport(creditBureauId, nationalId, creditReports);
    }

    private CreditReport(final Long creditBureauId, final String nationalId, final byte[] creditReports) {
        this.creditBureauId = creditBureauId;
        this.nationalId = nationalId;
        this.creditReports = creditReports;

    }

    public byte[] getCreditReport() {
        return this.creditReports;
    }

}

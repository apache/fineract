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

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name = "m_creditreport")
@NoArgsConstructor
@Getter
@Setter
@Accessors(chain = true)
public class CreditReport extends AbstractPersistableCustom<Long> {

    @Column(name = "credit_bureau_id")
    private Long creditBureauId;

    @Column(name = "national_id")
    private String nationalId;

    @Basic(fetch = FetchType.LAZY)
    @Column(name = "credit_reports")
    private byte[] creditReports;

    public static CreditReport instance(final Long creditBureauId, final String nationalId, final byte[] creditReports) {
        return new CreditReport().setCreditBureauId(creditBureauId).setNationalId(nationalId).setCreditReports(creditReports);
    }
}

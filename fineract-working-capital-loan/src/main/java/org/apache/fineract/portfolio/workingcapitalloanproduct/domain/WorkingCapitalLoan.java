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
package org.apache.fineract.portfolio.workingcapitalloanproduct.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatusConverter;

@Entity
@Table(name = "m_wc_loan", uniqueConstraints = { @UniqueConstraint(columnNames = { "account_no" }, name = "wc_loan_account_no_UNIQUE"),
        @UniqueConstraint(columnNames = { "external_id" }, name = "wc_loan_externalid_UNIQUE") })
@Getter
public class WorkingCapitalLoan extends AbstractAuditableWithUTCDateTimeCustom<Long> {

    @Version
    int version;

    @Setter()
    @Column(name = "account_no", length = 20, unique = true, nullable = false)
    private String accountNumber;

    @Setter
    @Column(name = "last_closed_business_date")
    private LocalDate lastClosedBusinessDate;

    @Setter(AccessLevel.PACKAGE)
    @Column(name = "loan_status_id", nullable = false)
    @Convert(converter = LoanStatusConverter.class)
    private LoanStatus loanStatus;

    @Setter()
    @Column(name = "external_id")
    private ExternalId externalId;

    @Setter()
    @Column(name = "disbursedon_date")
    private LocalDate actualDisbursementDate;

}

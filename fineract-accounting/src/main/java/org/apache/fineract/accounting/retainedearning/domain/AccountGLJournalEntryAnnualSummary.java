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
package org.apache.fineract.accounting.retainedearning.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;
import org.apache.fineract.infrastructure.core.domain.ExternalId;

/**
 * Entity for retained earning summary.
 */
@Getter
@Setter
@Entity
@Table(name = "acc_gl_journal_entry_annual_summary")
public class AccountGLJournalEntryAnnualSummary extends AbstractAuditableWithUTCDateTimeCustom<Long> {

    @Column(name = "gl_code")
    private String glCode;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "office_id")
    private Long officeId;

    @Column(name = "opening_balance_amount")
    private BigDecimal openingBalanceAmount;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @Column(name = "owner_external_id")
    private ExternalId ownerExternalId;

    @Column(name = "manual_entry", nullable = false)
    private Boolean manualEntry = false;

    @Column(name = "year_end_date")
    private LocalDate yearEndDate;
}

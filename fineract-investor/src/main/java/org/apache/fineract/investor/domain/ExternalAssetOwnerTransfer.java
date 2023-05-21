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
package org.apache.fineract.investor.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.fineract.infrastructure.core.domain.ExternalId;

@Getter
@Setter
@Table(name = "m_external_asset_owner_transfer")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class ExternalAssetOwnerTransfer extends AbstractAuditableWithUTCDateTimeCustom {

    @Column(name = "owner_id")
    private Long ownerId;

    @ManyToOne
    @JoinColumn(name = "owner_id", insertable = false, updatable = false)
    private ExternalAssetOwner owner;

    @Column(name = "external_id", length = 100)
    private ExternalId externalId;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "purchase_price_ratio", precision = 19, scale = 6)
    private BigDecimal purchasePriceRatio;

    @Column(name = "settlement_date")
    private LocalDate settlementDate;

    @Column(name = "effective_date_from")
    private LocalDate effectiveDateFrom;

    @Column(name = "effective_date_to")
    private LocalDate effectiveDateTo;

    @Column(name = "loan_id", nullable = false)
    private Long loanId;

    @Column(name = "external_loan_id", length = 100)
    private ExternalId externalLoanId;

}

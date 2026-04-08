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

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;
import org.apache.fineract.infrastructure.core.service.MathUtil;

@Getter
@Table(name = "m_external_asset_owner_transfer_details")
@NoArgsConstructor
@Entity
public class ExternalAssetOwnerTransferDetails extends AbstractAuditableWithUTCDateTimeCustom<Long> {

    @Setter
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "asset_owner_transfer_id", referencedColumnName = "id")
    private ExternalAssetOwnerTransfer externalAssetOwnerTransfer;

    @Column(name = "total_outstanding_derived", scale = 6, precision = 19, nullable = false)
    private BigDecimal totalOutstanding;

    @Column(name = "principal_outstanding_derived", scale = 6, precision = 19, nullable = false)
    private BigDecimal totalPrincipalOutstanding;

    @Column(name = "interest_outstanding_derived", scale = 6, precision = 19, nullable = false)
    private BigDecimal totalInterestOutstanding;

    @Column(name = "fee_charges_outstanding_derived", scale = 6, precision = 19, nullable = false)
    private BigDecimal totalFeeChargesOutstanding;

    @Column(name = "penalty_charges_outstanding_derived", scale = 6, precision = 19, nullable = false)
    private BigDecimal totalPenaltyChargesOutstanding;

    @Column(name = "total_overpaid_derived", scale = 6, precision = 19, nullable = false)
    private BigDecimal totalOverpaid;

    public void setTotalPrincipalOutstanding(BigDecimal totalPrincipalOutstanding) {
        this.totalPrincipalOutstanding = Objects.requireNonNullElse(totalPrincipalOutstanding, BigDecimal.ZERO);
        updateTotalOutstanding();
    }

    public void setTotalInterestOutstanding(BigDecimal totalInterestOutstanding) {
        this.totalInterestOutstanding = Objects.requireNonNullElse(totalInterestOutstanding, BigDecimal.ZERO);
        updateTotalOutstanding();
    }

    public void setTotalFeeChargesOutstanding(BigDecimal totalFeeChargesOutstanding) {
        this.totalFeeChargesOutstanding = Objects.requireNonNullElse(totalFeeChargesOutstanding, BigDecimal.ZERO);
        updateTotalOutstanding();
    }

    public void setTotalPenaltyChargesOutstanding(BigDecimal totalPenaltyChargesOutstanding) {
        this.totalPenaltyChargesOutstanding = Objects.requireNonNullElse(totalPenaltyChargesOutstanding, BigDecimal.ZERO);
        updateTotalOutstanding();
    }

    private void updateTotalOutstanding() {
        this.totalOutstanding = MathUtil.add(getTotalPrincipalOutstanding(), getTotalInterestOutstanding(), getTotalFeeChargesOutstanding(),
                getTotalPenaltyChargesOutstanding());
    }

    public void setTotalOverpaid(BigDecimal totalOverpaid) {
        this.totalOverpaid = Objects.requireNonNullElse(totalOverpaid, BigDecimal.ZERO);
    }
}

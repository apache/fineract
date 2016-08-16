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
package org.apache.fineract.portfolio.shareproducts.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.fineract.infrastructure.core.domain.AbstractAuditableCustom;
import org.apache.fineract.portfolio.shareaccounts.domain.ShareAccountDividendDetails;
import org.apache.fineract.useradministration.domain.AppUser;
import org.joda.time.LocalDate;

@Entity
@Table(name = "m_share_product_dividend_pay_out")
public class ShareProductDividendPayOutDetails extends AbstractAuditableCustom<AppUser, Long> {

    @Column(name = "product_id", nullable = true)
    private Long shareProductId;

    @Column(name = "amount", scale = 6, precision = 19)
    private BigDecimal amount;

    @Column(name = "dividend_period_start_date")
    @Temporal(TemporalType.DATE)
    private Date dividendPeriodStartDate;

    @Column(name = "dividend_period_end_date")
    @Temporal(TemporalType.DATE)
    private Date dividendPeriodEndDate;

    @Column(name = "status", nullable = false)
    private Integer status;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch=FetchType.EAGER)
    @JoinColumn(name = "dividend_pay_out_id", referencedColumnName = "id", nullable = false)
    private List<ShareAccountDividendDetails> accountDividendDetails = new ArrayList<>();

    protected ShareProductDividendPayOutDetails() {

    }

    public ShareProductDividendPayOutDetails(final Long shareProductId, final BigDecimal amount, final Date dividendPeriodStartDate,
            final Date dividendPeriodEndDate) {
        this.shareProductId = shareProductId;
        this.amount = amount;
        this.dividendPeriodStartDate = dividendPeriodStartDate;
        this.dividendPeriodEndDate = dividendPeriodEndDate;
        this.status = ShareProductDividendStatusType.INITIATED.getValue();
    }

    public LocalDate getDividendPeriodEndDateAsLocalDate() {
        LocalDate dividendPeriodEndDate = null;
        if (this.dividendPeriodEndDate != null) {
            dividendPeriodEndDate = new LocalDate(this.dividendPeriodEndDate);
        }
        return dividendPeriodEndDate;
    }

    public List<ShareAccountDividendDetails> getAccountDividendDetails() {
        return this.accountDividendDetails;
    }

    public void approveDividendPayout() {
        this.status = ShareProductDividendStatusType.APPROVED.getValue();
    }

    public ShareProductDividendStatusType getStatus() {
        return ShareProductDividendStatusType.fromInt(this.status);
    }

}

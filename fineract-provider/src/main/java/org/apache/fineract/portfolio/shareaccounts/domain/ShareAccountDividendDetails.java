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
package org.apache.fineract.portfolio.shareaccounts.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.portfolio.shareproducts.domain.ShareProductDividendPayOutDetails;

@Entity
@Table(name = "m_share_account_dividend_details")
public class ShareAccountDividendDetails extends AbstractPersistableCustom<Long> {

    @Column(name = "account_id", nullable = false)
    private Long shareAccountId;

    @Column(name = "amount", scale = 6, precision = 19)
    private BigDecimal amount;

    @Column(name = "status")
    private Integer status;

    @Column(name = "savings_transaction_id")
    private Long savingsTransactionId;

    @ManyToOne
    @JoinColumn(name = "dividend_pay_out_id", nullable = false)
    private ShareProductDividendPayOutDetails productDividentPayOutDetails;

    protected ShareAccountDividendDetails() {

    }

    public ShareAccountDividendDetails(final Long shareAccountId, final BigDecimal amount,
            final ShareProductDividendPayOutDetails productDividentPayOutDetails) {
        this.shareAccountId = shareAccountId;
        this.amount = amount;
        this.productDividentPayOutDetails = productDividentPayOutDetails;
        this.status = ShareAccountDividendStatusType.INITIATED.getValue();
    }

    public void update(final Integer status, final Long savingsTransactionId) {
        this.status = status;
        this.savingsTransactionId = savingsTransactionId;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public ShareProductDividendPayOutDetails getProductDividentPayOutDetails() {
        return productDividentPayOutDetails;
    }
}

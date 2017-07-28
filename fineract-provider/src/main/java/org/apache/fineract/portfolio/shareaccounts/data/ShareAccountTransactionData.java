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
package org.apache.fineract.portfolio.shareaccounts.data;

import java.math.BigDecimal;

import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.joda.time.LocalDate;

@SuppressWarnings("unused")
public class ShareAccountTransactionData {

    private final Long id;

    private final Long accountId;

    private final LocalDate purchasedDate;

    private final Long numberOfShares;

    private final BigDecimal purchasedPrice;

    private final EnumOptionData status;

    private final EnumOptionData type;

    private final BigDecimal amount ;
    
    private final BigDecimal chargeAmount ;
    
    private final BigDecimal amountPaid ;
    
    public ShareAccountTransactionData(final Long id, final Long accountId, final LocalDate purchasedDate, final Long numberOfShares,
            final BigDecimal purchasedPrice, final EnumOptionData status, final EnumOptionData type, final BigDecimal amount, final BigDecimal chargeAmount,
            final BigDecimal amountPaid) {
        this.id = id;
        this.accountId = accountId;
        this.purchasedDate = purchasedDate;
        this.numberOfShares = numberOfShares;
        this.purchasedPrice = purchasedPrice;
        this.status = status;
        this.type = type;
        this.amount = amount ;
        this.chargeAmount = chargeAmount ;
        this.amountPaid = amountPaid ;
    }

    public LocalDate getPurchasedDate() {
        return this.purchasedDate;
    }

    public Long getNumberOfShares() {
        return this.numberOfShares;
    }

    public BigDecimal getPurchasedPrice() {
        return this.purchasedPrice;
    }

    public EnumOptionData getStatus() {
        return this.status;
    }

    public EnumOptionData getType() {
        return this.type;
    }
}

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
package org.apache.fineract.portfolio.collectionsheet.data;

import java.math.BigDecimal;

import org.apache.fineract.organisation.monetary.data.CurrencyData;

/**
 * Immutable data object for representing loan with dues (example: loan is due
 * for disbursement, repayments).
 */
public class SavingsDueData {

    @SuppressWarnings("unused")
    private final Long savingsId;
    @SuppressWarnings("unused")
    private final String accountId;
    @SuppressWarnings("unused")
    private final Integer accountStatusId;
    private final String productName;
    private final Long productId;
    @SuppressWarnings("unused")
    private final CurrencyData currency;
    @SuppressWarnings("unused")
    private BigDecimal dueAmount = BigDecimal.ZERO;
    @SuppressWarnings("unused")
    private String depositAccountType;

    public static SavingsDueData instance(final Long savingsId, final String accountId, final Integer accountStatusId,
            final String productName, final Long productId, final CurrencyData currency, final BigDecimal dueAmount, final String depositAccountType) {
        return new SavingsDueData(savingsId, accountId, accountStatusId, productName, productId, currency, dueAmount, depositAccountType);
    }

    private SavingsDueData(final Long savingsId, final String accountId, final Integer accountStatusId, final String productName,
            final Long productId, final CurrencyData currency, final BigDecimal dueAmount, final String depositAccountType) {
        this.savingsId = savingsId;
        this.accountId = accountId;
        this.accountStatusId = accountStatusId;
        this.productName = productName;
        this.productId = productId;
        this.currency = currency;
        this.dueAmount = dueAmount;
        this.depositAccountType = depositAccountType;
    }
    
    public String productName() {
        return this.productName;
    }
    
    public Long productId() {
        return this.productId;
    }

	public String getDepositAccountType() {
		return depositAccountType;
	}
    
}
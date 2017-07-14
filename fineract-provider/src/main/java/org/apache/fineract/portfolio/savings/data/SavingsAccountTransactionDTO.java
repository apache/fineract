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
package org.apache.fineract.portfolio.savings.data;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.fineract.portfolio.paymentdetail.domain.PaymentDetail;
import org.apache.fineract.useradministration.domain.AppUser;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;

public class SavingsAccountTransactionDTO {

    private final DateTimeFormatter formatter;
    private final LocalDate transactionDate;
    private final BigDecimal transactionAmount;
    private final PaymentDetail paymentDetail;
    private final Date createdDate;
    private final Long savingsAccountId;
    private final AppUser appUser;
    private final Integer depositAccountType;

    public SavingsAccountTransactionDTO(final DateTimeFormatter formatter, final LocalDate transactionDate,
            final BigDecimal transactionAmount, final PaymentDetail paymentDetail, final Date createdDate, final AppUser appUser, final Integer depositAccountType) {
        this.formatter = formatter;
        this.transactionDate = transactionDate;
        this.transactionAmount = transactionAmount;
        this.paymentDetail = paymentDetail;
        this.createdDate = createdDate;
        this.savingsAccountId = null;
        this.appUser = appUser;
        this.depositAccountType = depositAccountType;
    }

    /**
     * This constructor is used for bulk deposit transactions
     * 
     * @param formatter
     * @param transactionDate
     * @param transactionAmount
     * @param paymentDetail
     * @param createdDate
     * @param savingsAccountId
     */
    public SavingsAccountTransactionDTO(DateTimeFormatter formatter, LocalDate transactionDate, BigDecimal transactionAmount,
            PaymentDetail paymentDetail, Date createdDate, Long savingsAccountId, AppUser appUser, final Integer depositAccountType) {
        this.formatter = formatter;
        this.transactionDate = transactionDate;
        this.transactionAmount = transactionAmount;
        this.paymentDetail = paymentDetail;
        this.createdDate = createdDate;
        this.savingsAccountId = savingsAccountId;
        this.appUser = appUser;
        this.depositAccountType = depositAccountType;
    }

    public DateTimeFormatter getFormatter() {
        return this.formatter;
    }

    public LocalDate getTransactionDate() {
        return this.transactionDate;
    }

    public BigDecimal getTransactionAmount() {
        return this.transactionAmount;
    }

    public PaymentDetail getPaymentDetail() {
        return this.paymentDetail;
    }

    public Date getCreatedDate() {
        return this.createdDate;
    }

    public Long getSavingsAccountId() {
        return this.savingsAccountId;
    }

    public AppUser getAppUser() {
        return this.appUser;
    }

	public Integer getAccountType() {
		return this.depositAccountType;
	}
    
    
}

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
package org.apache.fineract.test.factory;

import java.math.BigDecimal;
import org.apache.fineract.client.models.PostSavingsAccountTransactionsRequest;
import org.apache.fineract.client.models.PostSavingsAccountsAccountIdRequest;
import org.apache.fineract.client.models.PostSavingsAccountsRequest;

public final class SavingsAccountRequestFactory {

    public static final String DATE_FORMAT = "dd MMMM yyyy";
    public static final String DEFAULT_LOCALE = "en";
    public static final String DEFAULT_TRANSACTION_DATE = "";
    public static final Long DEFAULT_CLIENT_ID = 1L;
    public static final String DEFAULT_SUBMITTED_ON_DATE = "";
    public static final String DEFAULT_APPROVED_ON_DATE = "";
    public static final String DEFAULT_ACTIVATED_ON_DATE = "";
    public static final BigDecimal DEFAULT_REPAYMENT_TRANSACTION_AMOUNT = new BigDecimal(1);
    public static final Integer DEFAULT_PAYMENT_TYPE_ID = 2;
    public static final Long EUR_SAVING_PRODUCT_ID = 1L;
    public static final Long USD_SAVING_PRODUCT_ID = 2L;

    private SavingsAccountRequestFactory() {}

    public static PostSavingsAccountsRequest defaultEURSavingsAccountRequest() {
        return new PostSavingsAccountsRequest()//
                .clientId(DEFAULT_CLIENT_ID)//
                .dateFormat(DATE_FORMAT)//
                .productId(EUR_SAVING_PRODUCT_ID)//
                .submittedOnDate(DEFAULT_SUBMITTED_ON_DATE)//
                .locale(DEFAULT_LOCALE);//
    }

    public static PostSavingsAccountsRequest defaultUSDSavingsAccountRequest() {
        return new PostSavingsAccountsRequest()//
                .clientId(DEFAULT_CLIENT_ID)//
                .dateFormat(DATE_FORMAT)//
                .productId(USD_SAVING_PRODUCT_ID)//
                .submittedOnDate(DEFAULT_SUBMITTED_ON_DATE)//
                .locale(DEFAULT_LOCALE);//
    }

    public static PostSavingsAccountsAccountIdRequest defaultApproveRequest() {
        return new PostSavingsAccountsAccountIdRequest()//
                .approvedOnDate(DEFAULT_APPROVED_ON_DATE)//
                .dateFormat(DATE_FORMAT)//
                .locale(DEFAULT_LOCALE);//
    }

    public static PostSavingsAccountsAccountIdRequest defaultActivateRequest() {
        return new PostSavingsAccountsAccountIdRequest()//
                .activatedOnDate(DEFAULT_ACTIVATED_ON_DATE)//
                .dateFormat(DATE_FORMAT)//
                .locale(DEFAULT_LOCALE);//
    }

    public static PostSavingsAccountTransactionsRequest defaultDepositRequest() {
        return new PostSavingsAccountTransactionsRequest()//
                .transactionDate(DEFAULT_TRANSACTION_DATE)//
                .transactionAmount(DEFAULT_REPAYMENT_TRANSACTION_AMOUNT)//
                .paymentTypeId(DEFAULT_PAYMENT_TYPE_ID)//
                .dateFormat(DATE_FORMAT)//
                .locale(DEFAULT_LOCALE);//
    }

    public static PostSavingsAccountTransactionsRequest defaultWithdrawRequest() {
        return new PostSavingsAccountTransactionsRequest()//
                .transactionDate(DEFAULT_TRANSACTION_DATE)//
                .transactionAmount(DEFAULT_REPAYMENT_TRANSACTION_AMOUNT)//
                .paymentTypeId(DEFAULT_PAYMENT_TYPE_ID)//
                .dateFormat(DATE_FORMAT)//
                .locale(DEFAULT_LOCALE);//
    }
}

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
package org.apache.fineract.integrationtests.client.feign.modules;

import org.apache.fineract.client.models.AccountTransferRequest;
import org.apache.fineract.portfolio.account.PortfolioAccountType;

public final class AccountTransferRequestBuilders {

    public static final Long HEAD_OFFICE_ID = 1L;

    private static final String TRANSFER_DESCRIPTION = "Transfer";

    /**
     * Account transfers are posted with {@code en_GB}, matching the locale the RestAssured
     * {@code AccountTransferHelper} used before the migration. The shared {@link FeignTestConstants#LOCALE} is plain
     * {@code en}; keeping the original locale here avoids changing how the server parses transfer amounts and dates.
     */
    private static final String TRANSFER_LOCALE = "en_GB";

    private AccountTransferRequestBuilders() {}

    /** A transfer between two accounts of the head office, e.g. a loan overpayment refunded into savings. */
    public static AccountTransferRequest transfer(String transferDate, Long fromClientId, Long fromAccountId,
            PortfolioAccountType fromAccountType, Long toClientId, Long toAccountId, PortfolioAccountType toAccountType,
            String transferAmount) {
        return new AccountTransferRequest()//
                .dateFormat(FeignTestConstants.DATETIME_PATTERN)//
                .locale(TRANSFER_LOCALE)//
                .fromClientId(String.valueOf(fromClientId))//
                .fromAccountId(String.valueOf(fromAccountId))//
                .fromAccountType(String.valueOf(fromAccountType.getValue()))//
                .fromOfficeId(String.valueOf(HEAD_OFFICE_ID))//
                .toClientId(String.valueOf(toClientId))//
                .toAccountId(String.valueOf(toAccountId))//
                .toAccountType(String.valueOf(toAccountType.getValue()))//
                .toOfficeId(String.valueOf(HEAD_OFFICE_ID))//
                .transferDate(transferDate)//
                .transferAmount(transferAmount)//
                .transferDescription(TRANSFER_DESCRIPTION);
    }
}

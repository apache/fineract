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
package org.apache.fineract.integrationtests.common.savings;

import java.util.HashMap;

import org.apache.fineract.integrationtests.common.Utils;

import com.google.gson.Gson;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

public class AccountTransferHelper {

    private static final String ACCOUNT_TRANSFER_URL = "/fineract-provider/api/v1/accounttransfers";
    private static final String LOAN_REFUND_BY_TRANSFER_URL = "/fineract-provider/api/v1/accounttransfers/refundByTransfer";
    private static final String LOCALE = "en_GB";
    private static final String OFFICE_ID = "1";
    private static final String TRANSFER_DESCRIPTION = "Transfer";
    public static final String ACCOUNT_TRANSFER_DATE = "01 March 2013";

    private String transferDate = "";
    private String officeId = OFFICE_ID;
    private String transferDescription = TRANSFER_DESCRIPTION;

    private RequestSpecification requestSpec;
    private ResponseSpecification responseSpec;

    public AccountTransferHelper(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        this.requestSpec = requestSpec;
        this.responseSpec = responseSpec;
    }

    public String build(final String fromAccountId, final String fromClientId, final String toAccountId, final String toClientId,
            final String fromAccountType, final String toAccountType, final String transferAmount) {

        final HashMap<String, String> map = new HashMap<>();
        map.put("dateFormat", "dd MMMM yyyy");
        map.put("locale", LOCALE);
        map.put("fromClientId", fromClientId);
        map.put("fromAccountId", fromAccountId);
        map.put("fromAccountType", fromAccountType);
        map.put("fromOfficeId", this.officeId);
        map.put("toClientId", toClientId);
        map.put("toAccountId", toAccountId);
        map.put("toAccountType", toAccountType);
        map.put("toOfficeId", this.officeId);
        map.put("transferDate", this.transferDate);
        map.put("transferAmount", transferAmount);
        map.put("transferDescription", this.transferDescription);
        String savingsApplicationJSON = new Gson().toJson(map);
        System.out.println(savingsApplicationJSON);
        return savingsApplicationJSON;
    }

    public AccountTransferHelper withTransferOnDate(final String savingsAccountTransferDate) {
        this.transferDate = savingsAccountTransferDate;
        return this;
    }

    public Integer accountTransfer(final Integer fromClientId, final Integer fromAccountId, final Integer toClientId,
            final Integer toAccountId, final String fromAccountType, final String toAccountType, final String transferAmount) {
        System.out.println("--------------------------------ACCOUNT TRANSFER--------------------------------");
        final String accountTransferJSON = new AccountTransferHelper(this.requestSpec, this.responseSpec) //
                .withTransferOnDate(ACCOUNT_TRANSFER_DATE) //
                .build(fromAccountId.toString(), fromClientId.toString(), toAccountId.toString(), toClientId.toString(), fromAccountType,
                        toAccountType, transferAmount);
        return Utils.performServerPost(this.requestSpec, this.responseSpec, ACCOUNT_TRANSFER_URL + "?" + Utils.TENANT_IDENTIFIER,
                accountTransferJSON, "savingsId");
    }
    
    public Integer refundLoanByTransfer(final String date, final Integer fromClientId, final Integer fromAccountId, final Integer toClientId,
            final Integer toAccountId, final String fromAccountType, final String toAccountType, final String transferAmount) {
        System.out.println("--------------------------------ACCOUNT TRANSFER--------------------------------");
        final String accountTransferJSON = new AccountTransferHelper(this.requestSpec, this.responseSpec) //
                .withTransferOnDate(date) //
                .build(fromAccountId.toString(), fromClientId.toString(), toAccountId.toString(), toClientId.toString(), fromAccountType,
                        toAccountType, transferAmount);
        return Utils.performServerPost(this.requestSpec, this.responseSpec, LOAN_REFUND_BY_TRANSFER_URL + "?" + Utils.TENANT_IDENTIFIER,
                accountTransferJSON, "savingsId");
    }
}
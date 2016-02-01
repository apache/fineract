/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.integrationtests.common.savings;

import java.util.HashMap;

import org.mifosplatform.integrationtests.common.Utils;

import com.google.gson.Gson;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

public class AccountTransferHelper {

    private static final String ACCOUNT_TRANSFER_URL = "/mifosng-provider/api/v1/accounttransfers";
    private static final String LOAN_REFUND_BY_TRANSFER_URL = "/mifosng-provider/api/v1/accounttransfers/refundByTransfer";
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
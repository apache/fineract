/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.account.api;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.mifosplatform.portfolio.account.AccountDetailConstants;
import org.mifosplatform.portfolio.account.data.AccountTransferData;

public class AccountTransfersApiConstants {

    public static final String ACCOUNT_TRANSFER_RESOURCE_NAME = "accounttransfer";

    // transaction parameters
    public static final String transferDateParamName = "transferDate";
    public static final String transferAmountParamName = "transferAmount";
    public static final String transferDescriptionParamName = "transferDescription";

    public static final Set<String> REQUEST_DATA_PARAMETERS = new HashSet<>(Arrays.asList(AccountDetailConstants.localeParamName,
            AccountDetailConstants.dateFormatParamName, AccountDetailConstants.fromOfficeIdParamName,
            AccountDetailConstants.fromClientIdParamName, AccountDetailConstants.fromAccountTypeParamName,
            AccountDetailConstants.fromAccountIdParamName, AccountDetailConstants.toOfficeIdParamName,
            AccountDetailConstants.toClientIdParamName, AccountDetailConstants.toAccountTypeParamName,
            AccountDetailConstants.toAccountIdParamName, transferDateParamName, transferAmountParamName, transferDescriptionParamName));

    /**
     * These parameters will match the class level parameters of
     * {@link AccountTransferData}. Where possible, we try to get response
     * parameters to match those of request parameters.
     */
    public static final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<>(Arrays.asList(AccountDetailConstants.idParamName,
            transferDescriptionParamName, "currency"));
}
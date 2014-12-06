/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.client.domain;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.mifosplatform.infrastructure.accountnumberformat.domain.AccountNumberFormat;
import org.mifosplatform.infrastructure.accountnumberformat.domain.AccountNumberFormatEnumerations.AccountNumberPrefixType;
import org.mifosplatform.infrastructure.codes.domain.CodeValue;
import org.mifosplatform.portfolio.loanaccount.domain.Loan;
import org.mifosplatform.portfolio.savings.domain.SavingsAccount;
import org.springframework.stereotype.Component;

/**
 * Example {@link AccountNumberGenerator} for clients that takes an entities
 * auto generated database id and zero fills it ensuring the identifier is
 * always of a given <code>maxLength</code>.
 */
@Component
public class AccountNumberGenerator {

    private final static int maxLength = 9;

    private final static String ID = "id";
    private final static String CLIENT_TYPE = "clientType";
    private final static String OFFICE_NAME = "officeName";
    private final static String LOAN_PRODUCT_SHORT_NAME = "loanProductShortName";
    private final static String SAVINGS_PRODUCT_SHORT_NAME = "savingsProductShortName";

    public String generate(Client client, AccountNumberFormat accountNumberFormat) {
        Map<String, String> propertyMap = new HashMap<>();
        propertyMap.put(ID, client.getId().toString());
        propertyMap.put(OFFICE_NAME, client.getOffice().getName());
        CodeValue clientType = client.clientType();
        if (clientType != null) {
            propertyMap.put(CLIENT_TYPE, clientType.label());
        }
        return generateAccountNumber(propertyMap, accountNumberFormat);
    }

    public String generate(Loan loan, AccountNumberFormat accountNumberFormat) {
        Map<String, String> propertyMap = new HashMap<>();
        propertyMap.put(ID, loan.getId().toString());
        propertyMap.put(OFFICE_NAME, loan.getOffice().getName());
        propertyMap.put(LOAN_PRODUCT_SHORT_NAME, loan.loanProduct().getShortName());
        return generateAccountNumber(propertyMap, accountNumberFormat);
    }

    public String generate(SavingsAccount savingsAccount, AccountNumberFormat accountNumberFormat) {
        Map<String, String> propertyMap = new HashMap<>();
        propertyMap.put(ID, savingsAccount.getId().toString());
        propertyMap.put(OFFICE_NAME, savingsAccount.office().getName());
        propertyMap.put(SAVINGS_PRODUCT_SHORT_NAME, savingsAccount.savingsProduct().getShortName());
        return generateAccountNumber(propertyMap, accountNumberFormat);
    }

    private String generateAccountNumber(Map<String, String> propertyMap, AccountNumberFormat accountNumberFormat) {
        String accountNumber = StringUtils.leftPad(propertyMap.get(ID), AccountNumberGenerator.maxLength, '0');
        if (accountNumberFormat != null && accountNumberFormat.getPrefixEnum() != null) {
            AccountNumberPrefixType accountNumberPrefixType = AccountNumberPrefixType.fromInt(accountNumberFormat.getPrefixEnum());
            String prefix = null;
            switch (accountNumberPrefixType) {
                case CLIENT_TYPE:
                    prefix = propertyMap.get(CLIENT_TYPE);
                break;

                case OFFICE_NAME:
                    prefix = propertyMap.get(OFFICE_NAME);
                break;

                case LOAN_PRODUCT_SHORT_NAME:
                    prefix = propertyMap.get(LOAN_PRODUCT_SHORT_NAME);
                break;

                case SAVINGS_PRODUCT_SHORT_NAME:
                    prefix = propertyMap.get(SAVINGS_PRODUCT_SHORT_NAME);
                break;

                default:
                break;

            }
            accountNumber = StringUtils.overlay(accountNumber, prefix, 0, 0);
        }
        return accountNumber;
    }

}
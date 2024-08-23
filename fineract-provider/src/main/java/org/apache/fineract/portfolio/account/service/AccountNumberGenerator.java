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
package org.apache.fineract.portfolio.account.service;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.accountnumberformat.domain.AccountNumberFormat;
import org.apache.fineract.infrastructure.accountnumberformat.domain.AccountNumberFormatEnumerations.AccountNumberPrefixType;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.configuration.data.GlobalConfigurationPropertyData;
import org.apache.fineract.infrastructure.configuration.service.ConfigurationReadPlatformService;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientRepository;
import org.apache.fineract.portfolio.group.domain.Group;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepository;
import org.apache.fineract.portfolio.shareaccounts.domain.ShareAccount;
import org.springframework.stereotype.Component;

/**
 * Example {@link AccountNumberGenerator} for clients that takes an entities auto generated database id and zero fills
 * it ensuring the identifier is always of a given <code>maxLength</code>.
 */
@Component
@AllArgsConstructor
public class AccountNumberGenerator {

    private static final int maxLength = 9;

    private static final String ID = "id";
    private static final String ENTITY_TYPE = "entityType";
    private static final String CLIENT_TYPE = "clientType";
    private static final String OFFICE_NAME = "officeName";
    private static final String LOAN_PRODUCT_SHORT_NAME = "loanProductShortName";
    private static final String SAVINGS_PRODUCT_SHORT_NAME = "savingsProductShortName";
    private static final String SHARE_PRODUCT_SHORT_NAME = "sharesProductShortName";
    private static final String PREFIX_SHORT_NAME = "prefixShortName";
    private final ConfigurationReadPlatformService configurationReadPlatformService;
    private final ClientRepository clientRepository;
    private final LoanRepository loanRepository;
    private final SavingsAccountRepository savingsAccountRepository;

    public String generate(Client client, AccountNumberFormat accountNumberFormat) {
        Map<String, String> propertyMap = new HashMap<>();
        propertyMap.put(ID, client.getId().toString());
        propertyMap.put(OFFICE_NAME, client.getOffice().getName());
        propertyMap.put(ENTITY_TYPE, "client");
        CodeValue clientType = client.clientType();
        if (clientType != null) {
            propertyMap.put(CLIENT_TYPE, clientType.getLabel());
        }
        return generateAccountNumber(propertyMap, accountNumberFormat);
    }

    public String generate(Loan loan, AccountNumberFormat accountNumberFormat) {
        Map<String, String> propertyMap = new HashMap<>();
        propertyMap.put(ID, loan.getId().toString());
        propertyMap.put(OFFICE_NAME, loan.getOffice().getName());
        propertyMap.put(LOAN_PRODUCT_SHORT_NAME, loan.loanProduct().getShortName());
        propertyMap.put(ENTITY_TYPE, "loan");
        return generateAccountNumber(propertyMap, accountNumberFormat);
    }

    public String generate(SavingsAccount savingsAccount, AccountNumberFormat accountNumberFormat) {
        Map<String, String> propertyMap = new HashMap<>();
        propertyMap.put(ID, savingsAccount.getId().toString());
        propertyMap.put(OFFICE_NAME, savingsAccount.office().getName());
        propertyMap.put(SAVINGS_PRODUCT_SHORT_NAME, savingsAccount.savingsProduct().getShortName());
        propertyMap.put(ENTITY_TYPE, "savingsAccount");
        return generateAccountNumber(propertyMap, accountNumberFormat);
    }

    public String generate(ShareAccount shareaccount, AccountNumberFormat accountNumberFormat) {
        Map<String, String> propertyMap = new HashMap<>();
        propertyMap.put(ID, shareaccount.getId().toString());
        propertyMap.put(SHARE_PRODUCT_SHORT_NAME, shareaccount.getShareProduct().getShortName());
        return generateAccountNumber(propertyMap, accountNumberFormat);
    }

    private String generateAccountNumber(Map<String, String> propertyMap, AccountNumberFormat accountNumberFormat) {
        int accountMaxLength = AccountNumberGenerator.maxLength;
        String accountNumber = StringUtils.leftPad(propertyMap.get(ID), accountMaxLength, '0');

        // find if the custom length is defined
        final GlobalConfigurationPropertyData customLength = this.configurationReadPlatformService
                .retrieveGlobalConfiguration("custom-account-number-length");

        if (customLength.isEnabled()) {
            // if it is enabled, and has the value, get it from the repository.
            if (customLength.getValue() != null) {
                accountMaxLength = customLength.getValue().intValue();
            }
        }

        final GlobalConfigurationPropertyData randomAccountNumber = this.configurationReadPlatformService
                .retrieveGlobalConfiguration("random-account-number");

        if (randomAccountNumber.isEnabled()) {
            accountNumber = randomNumberGenerator(accountMaxLength, propertyMap);
        }

        accountNumber = StringUtils.leftPad(accountNumber, accountMaxLength, '0');
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

                case PREFIX_SHORT_NAME:
                    generatePrefix(propertyMap, propertyMap.get(ID), accountMaxLength, accountNumberFormat);
                    prefix = propertyMap.get(PREFIX_SHORT_NAME);
                break;
            }

            // FINERACT-590
            // Because account_no is limited to 20 chars, we can only use the
            // first 10 chars of prefix - trim if necessary
            if (prefix != null) {
                prefix = prefix.substring(0, Math.min(prefix.length(), 10));
            }
            if (accountNumberPrefixType.getValue().equals(AccountNumberPrefixType.PREFIX_SHORT_NAME.getValue())) {
                Integer prefixLength = prefix.length();

                if (randomAccountNumber.isEnabled()) {
                    accountNumber = accountNumber.substring(prefixLength);
                } else {
                    Integer numberLength = accountMaxLength - prefixLength;
                    accountNumber = StringUtils.leftPad(propertyMap.get(ID), numberLength, '0');
                }
            } else {
                accountNumber = StringUtils.leftPad(accountNumber, Integer.valueOf(propertyMap.get(ID).length()), '0');
            }

            accountNumber = StringUtils.overlay(accountNumber, prefix, 0, 0);
        }

        if (randomAccountNumber.isEnabled()) { // calling the main function itself until new randomNo.
            Boolean randomNumberConflict = checkAccountNumberConflict(propertyMap, accountNumberFormat, accountNumber);
            if (randomNumberConflict) {
                accountNumber = generateAccountNumber(propertyMap, accountNumberFormat);
            }
        }
        return accountNumber;
    }

    private String randomNumberGenerator(int accountMaxLength, Map<String, String> propertyMap) {
        String randomNumber = RandomStringUtils.random(accountMaxLength, false, true); // NOSONAR

        BigInteger accNumber = new BigInteger(randomNumber);
        if (accNumber.equals(BigInteger.ZERO)) { // to avoid account no. 00 in randomisation
            randomNumber = randomNumberGenerator(accountMaxLength, propertyMap);
        }

        String accountNumber = randomNumber.substring(0, accountMaxLength);
        return accountNumber;
    }

    private Boolean checkAccountNumberConflict(Map<String, String> propertyMap, AccountNumberFormat accountNumberFormat,
            String accountNumber) {

        String entityType = propertyMap.get(ENTITY_TYPE);
        Boolean randomNumberConflict = false;
        if (entityType.equals("client")) { // avoid duplication it will loop until it finds new random account no.

            Client client = this.clientRepository.getClientByAccountNumber(accountNumber);
            if (client != null) {
                randomNumberConflict = true;
            }
        } else if (entityType.equals("loan")) {
            Loan loan = this.loanRepository.findLoanAccountByAccountNumber(accountNumber);
            if (loan != null) {
                randomNumberConflict = true;
            }
        } else if (entityType.equals("savingsAccount")) {
            SavingsAccount savingsAccount = this.savingsAccountRepository.findSavingsAccountByAccountNumber(accountNumber);
            if (savingsAccount != null) {
                randomNumberConflict = true;
            }
        }
        return randomNumberConflict;
    }

    private Map<String, String> generatePrefix(Map<String, String> propertyMap, String accountNumber, Integer accountMaxLength,
            AccountNumberFormat accountNumberFormat) {

        String prefix = accountNumberFormat.getPrefixCharacter();
        Integer prefixLength = prefix.length();

        Integer totalLength = prefixLength + Integer.valueOf(propertyMap.get(ID).length());

        prefixLength = totalLength - accountMaxLength;

        if (prefixLength > 0) {
            prefix = prefix.substring(0, prefix.length() - prefixLength);
        }

        propertyMap.put(PREFIX_SHORT_NAME, prefix);

        return propertyMap;
    }

    public String generateGroupAccountNumber(Group group, AccountNumberFormat accountNumberFormat) {
        Map<String, String> propertyMap = new HashMap<>();
        propertyMap.put(ID, group.getId().toString());
        propertyMap.put(OFFICE_NAME, group.getOffice().getName());
        return generateAccountNumber(propertyMap, accountNumberFormat);
    }

    public String generateCenterAccountNumber(Group group, AccountNumberFormat accountNumberFormat) {
        Map<String, String> propertyMap = new HashMap<>();
        propertyMap.put(ID, group.getId().toString());
        propertyMap.put(OFFICE_NAME, group.getOffice().getName());
        return generateAccountNumber(propertyMap, accountNumberFormat);
    }

}

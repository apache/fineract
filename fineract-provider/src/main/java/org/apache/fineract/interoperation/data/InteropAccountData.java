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
package org.apache.fineract.interoperation.data;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.interoperation.domain.InteropIdentifier;
import org.apache.fineract.portfolio.accountdetails.domain.AccountType;
import org.apache.fineract.portfolio.savings.DepositAccountType;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountStatusType;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountSubStatusEnum;
import org.apache.fineract.portfolio.savings.domain.SavingsProduct;

public class InteropAccountData extends CommandProcessingResult {

    @NotNull
    private final String accountId;
    @NotNull
    private final String savingProductId;
    @NotNull
    private final String productName;
    @NotNull
    private final String shortProductName;
    @NotNull
    private final String currency;
    @NotNull
    private final BigDecimal accountBalance;
    @NotNull
    private final BigDecimal availableBalance;
    @NotNull
    private final SavingsAccountStatusType status;
    private final SavingsAccountSubStatusEnum subStatus;

    private final AccountType accountType; // differentiate Individual, JLG or
                                           // Group account
    private final DepositAccountType depositType; // differentiate deposit
                                                  // accounts Savings, FD and RD
                                                  // accounts
    @NotNull
    private final LocalDate activatedOn;
    private final LocalDate statusUpdateOn;
    private final LocalDate withdrawnOn;
    private final LocalDate balanceOn;
    @NotNull
    private List<InteropIdentifierData> identifiers;

    InteropAccountData(Long resourceId, Long officeId, Long commandId, Map<String, Object> changesOnly, String accountId, String productId,
            String productName, String shortProductName, String currency, BigDecimal accountBalance, BigDecimal availableBalance,
            SavingsAccountStatusType status, SavingsAccountSubStatusEnum subStatus, AccountType accountType, DepositAccountType depositType,
            LocalDate activatedOn, LocalDate statusUpdateOn, LocalDate withdrawnOn, LocalDate balanceOn,
            List<InteropIdentifierData> identifiers, long clientId) {
        super(resourceId, officeId, commandId, changesOnly, clientId);
        this.accountId = accountId;
        this.savingProductId = productId;
        this.productName = productName;
        this.shortProductName = shortProductName;
        this.currency = currency;
        this.accountBalance = accountBalance;
        this.availableBalance = availableBalance;
        this.status = status;
        this.subStatus = subStatus;
        this.accountType = accountType;
        this.depositType = depositType;
        this.activatedOn = activatedOn;
        this.statusUpdateOn = statusUpdateOn;
        this.withdrawnOn = withdrawnOn;
        this.balanceOn = balanceOn;
        this.identifiers = identifiers;
    }

    InteropAccountData(String accountId, String productId, String productName, String shortProductName, String currency,
            BigDecimal accountBalance, BigDecimal availableBalance, SavingsAccountStatusType status, SavingsAccountSubStatusEnum subStatus,
            AccountType accountType, DepositAccountType depositType, LocalDate activatedOn, LocalDate statusUpdateOn, LocalDate withdrawnOn,
            LocalDate balanceOn, List<InteropIdentifierData> identifiers, long clientId) {
        this(null, null, null, null, accountId, productId, productName, shortProductName, currency, accountBalance, availableBalance,
                status, subStatus, accountType, depositType, activatedOn, statusUpdateOn, withdrawnOn, balanceOn, identifiers, clientId);
    }

    public static InteropAccountData build(SavingsAccount account) {
        if (account == null) {
            return null;
        }

        List<InteropIdentifierData> ids = new ArrayList<>();
        for (InteropIdentifier identifier : account.getIdentifiers()) {
            ids.add(InteropIdentifierData.build(identifier));
        }

        SavingsProduct product = account.savingsProduct();
        SavingsAccountSubStatusEnum subStatus = SavingsAccountSubStatusEnum.fromInt(account.getSubStatus());

        return new InteropAccountData(account.getExternalId().getValue(), product.getId().toString(), product.getName(),
                product.getShortName(), account.getCurrency().getCode(), account.getAccountBalance(), account.getWithdrawableBalance(),
                account.getStatus(), subStatus, account.getAccountType(), account.depositAccountType(), account.getActivationDate(),
                calcStatusUpdateOn(account), account.getWithdrawnOnDate(), account.retrieveLastTransactionDate(), ids,
                account.getClient().getId());
    }

    private static LocalDate calcStatusUpdateOn(@NotNull SavingsAccount account) {
        if (account.getClosedOnDate() != null) {
            return account.getClosedOnDate();
        }
        if (account.getWithdrawnOnDate() != null) {
            return account.getWithdrawnOnDate();
        }
        if (account.getActivationDate() != null) {
            return account.getActivationDate();
        }
        if (account.getRejectedOnDate() != null) {
            return account.getRejectedOnDate();
        }
        if (account.getApprovedOnDate() != null) {
            return account.getApprovedOnDate();
        }
        if (account.getSubmittedOnDate() != null) {
            return account.getSubmittedOnDate();
        }
        return null;
    }
}

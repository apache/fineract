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
package org.apache.fineract.portfolio.shareproducts.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.products.service.ProductReadPlatformService;
import org.apache.fineract.portfolio.shareaccounts.data.ShareAccountData;
import org.apache.fineract.portfolio.shareaccounts.data.ShareAccountTransactionData;
import org.apache.fineract.portfolio.shareaccounts.domain.PurchasedSharesStatusType;
import org.apache.fineract.portfolio.shareaccounts.domain.ShareAccountDividendDetails;
import org.apache.fineract.portfolio.shareaccounts.service.ShareAccountReadPlatformService;
import org.apache.fineract.portfolio.shareproducts.data.ShareProductData;
import org.apache.fineract.portfolio.shareproducts.domain.ShareProductDividendPayOutDetails;
import org.apache.fineract.portfolio.shareproducts.exception.ShareAccountsNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ShareProductDividendAssembler {

    private final ProductReadPlatformService shareProductReadPlatformService;
    private final ShareAccountReadPlatformService shareAccountReadPlatformService;

    @Autowired
    public ShareProductDividendAssembler(final ShareProductReadPlatformServiceImpl shareProductReadPlatformService,
            final ShareAccountReadPlatformService shareAccountReadPlatformService) {
        this.shareProductReadPlatformService = shareProductReadPlatformService;
        this.shareAccountReadPlatformService = shareAccountReadPlatformService;
    }

    public ShareProductDividendPayOutDetails calculateDividends(final Long productId, final BigDecimal amount,
            final LocalDate dividendPeriodStartDate, final LocalDate dividendPeriodEndDate) {

        ShareProductData product = (ShareProductData) this.shareProductReadPlatformService.retrieveOne(productId, false);
        MonetaryCurrency currency = new MonetaryCurrency(product.getCurrency().code(), product.getCurrency().decimalPlaces(),
                product.getCurrency().currencyInMultiplesOf());
        Collection<ShareAccountData> shareAccountDatas = this.shareAccountReadPlatformService.retrieveAllShareAccountDataForDividends(
                productId, product.getAllowDividendCalculationForInactiveClients(), dividendPeriodStartDate);
        if (shareAccountDatas == null || shareAccountDatas.isEmpty()) {
            throw new ShareAccountsNotFoundException(product.getId());
        }

        ShareProductDividendPayOutDetails productDividendPayOutDetails = null;
        int minimumActivePeriod = 0;
        if (product.getMinimumActivePeriod() != null) { // minimum active period
                                                        // may be null
            minimumActivePeriod = product.getMinimumActivePeriod();
        }
        final Map<Long, Long> numberOfSharesdaysPerAccount = new HashMap<>();
        long numberOfShareDays = calculateNumberOfShareDays(dividendPeriodEndDate, dividendPeriodStartDate, minimumActivePeriod,
                shareAccountDatas, numberOfSharesdaysPerAccount);

        if (numberOfShareDays > 0) {
            double amountPerShareDay = amount.doubleValue() / numberOfShareDays;
            productDividendPayOutDetails = new ShareProductDividendPayOutDetails(productId, Money.of(currency, amount).getAmount(),
                    Date.from(dividendPeriodStartDate.atStartOfDay(ZoneId.systemDefault()).toInstant()),
                    Date.from(dividendPeriodEndDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            for (ShareAccountData accountData : shareAccountDatas) {
                long numberOfShareDaysPerAccount = numberOfSharesdaysPerAccount.get(accountData.getId());
                double amountForAccount = numberOfShareDaysPerAccount * amountPerShareDay;
                final Money accountAmount = Money.of(currency, BigDecimal.valueOf(amountForAccount));
                ShareAccountDividendDetails dividendDetails = new ShareAccountDividendDetails(accountData.getId(),
                        accountAmount.getAmount(), productDividendPayOutDetails);
                productDividendPayOutDetails.getAccountDividendDetails().add(dividendDetails);
            }
        }

        return productDividendPayOutDetails;
    }

    private long calculateNumberOfShareDays(final LocalDate postingDate, final LocalDate lastDividendPostDate,
            final int minimumActivePeriod, final Collection<ShareAccountData> shareAccountDatas,
            final Map<Long, Long> numberOfSharesdaysPerAccount) {
        long numberOfShareDays = 0;
        for (ShareAccountData accountData : shareAccountDatas) {
            long numberOfShareDaysPerAccount = 0;
            Collection<ShareAccountTransactionData> purchasedShares = accountData.getPurchasedShares();
            long numberOfShares = 0;
            LocalDate lastDividendAppliedDate = null;
            for (ShareAccountTransactionData purchasedSharesData : purchasedShares) {
                final PurchasedSharesStatusType status = PurchasedSharesStatusType
                        .fromInt(purchasedSharesData.getStatus().getId().intValue());
                final PurchasedSharesStatusType type = PurchasedSharesStatusType.fromInt(purchasedSharesData.getType().getId().intValue());
                if (status.isApproved() && !type.isChargePayment()) {

                    LocalDate shareStartDate = purchasedSharesData.getPurchasedDate();
                    if (shareStartDate.isBefore(lastDividendPostDate)) {
                        shareStartDate = lastDividendPostDate;
                    }
                    int numberOfPurchseDays = Math.toIntExact(ChronoUnit.DAYS.between(shareStartDate, postingDate));
                    if (type.isPurchased() && numberOfPurchseDays < minimumActivePeriod) {
                        continue;
                    }

                    if (lastDividendAppliedDate != null) {
                        numberOfShareDaysPerAccount += Math.toIntExact(ChronoUnit.DAYS.between(lastDividendAppliedDate, shareStartDate))
                                * numberOfShares;
                    }
                    lastDividendAppliedDate = shareStartDate;
                    if (type.isPurchased()) {
                        numberOfShares += purchasedSharesData.getNumberOfShares();
                    } else {
                        numberOfShares -= purchasedSharesData.getNumberOfShares();
                    }

                }
            }
            if (lastDividendAppliedDate != null) {
                numberOfShareDaysPerAccount += Math.toIntExact(ChronoUnit.DAYS.between(lastDividendAppliedDate, postingDate))
                        * numberOfShares;
            }
            numberOfShareDays += numberOfShareDaysPerAccount;
            numberOfSharesdaysPerAccount.put(accountData.getId(), numberOfShareDaysPerAccount);
        }
        return numberOfShareDays;
    }

}

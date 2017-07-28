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
package org.apache.fineract.portfolio.shareproducts.data;

import java.math.BigDecimal;
import java.util.Collection;

import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.portfolio.shareaccounts.data.ShareAccountDividendData;
import org.joda.time.LocalDate;

public class ShareProductDividendPayOutData {

    @SuppressWarnings("unused")
    private final Long id;
    @SuppressWarnings("unused")
    private final ShareProductData productData;
    @SuppressWarnings("unused")
    private final BigDecimal amount;
    @SuppressWarnings("unused")
    private final EnumOptionData status;
    @SuppressWarnings("unused")
    private final LocalDate dividendPeriodStartDate;
    @SuppressWarnings("unused")
    private final LocalDate dividendPeriodEndDate;
    @SuppressWarnings("unused")
    private final Collection<ShareAccountDividendData> accountDividendsData;

    public ShareProductDividendPayOutData(final Long id, final ShareProductData productData, final BigDecimal amount,
            LocalDate dividendStartDate, final LocalDate dividendEndDate, final Collection<ShareAccountDividendData> accountDividendsData,
            final EnumOptionData status) {
        this.id = id;
        this.productData = productData;
        this.amount = amount;
        this.dividendPeriodEndDate = dividendEndDate;
        this.accountDividendsData = accountDividendsData;
        this.dividendPeriodStartDate = dividendStartDate;
        this.status = status;
    }
}

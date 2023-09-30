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
package org.apache.fineract.investor.domain.search;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.investor.data.ExternalTransferStatus;
import org.apache.fineract.investor.data.ExternalTransferSubStatus;

@Getter
@RequiredArgsConstructor
public class SearchedExternalAssetOwner {

    private final Long transferId;
    private final Long loanId;
    private final ExternalId externalLoanId;

    private final ExternalId owner;
    private final ExternalId transferExternalId;

    private final ExternalTransferStatus status;
    private final ExternalTransferSubStatus subStatus;

    private final String purchasePriceRatio;
    private final LocalDate settlementDate;
    private final LocalDate effectiveFrom;
    private final LocalDate effectiveTo;

    private final Long detailsId;
    private final BigDecimal totalOutstanding;
    private final BigDecimal principalOutstanding;
    private final BigDecimal interestOutstanding;
    private final BigDecimal feeOutstanding;
    private final BigDecimal penaltyOutstanding;
    private final BigDecimal totalOverpaid;
}

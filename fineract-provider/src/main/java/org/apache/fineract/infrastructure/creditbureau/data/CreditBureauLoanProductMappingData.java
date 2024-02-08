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
package org.apache.fineract.infrastructure.creditbureau.data;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public final class CreditBureauLoanProductMappingData {

    private long creditbureauLoanProductMappingId;

    private long organisationCreditBureauId;

    private String alias;

    private String creditbureauSummary;

    private String loanProductName;

    private long loanProductId;

    private boolean isCreditCheckMandatory;

    private boolean skipCrediCheckInFailure;

    private long stalePeriod;

    private boolean active;

    public static CreditBureauLoanProductMappingData instance(final long creditbureauLoanProductMappingId,
            final long organisationCreditBureauId, final String alias, final String creditbureauSummary, final String loanProductName,
            final long loanProductId, final boolean isCreditCheckMandatory, final boolean skipCrediCheckInFailure, final long stalePeriod,
            final boolean active) {
        return new CreditBureauLoanProductMappingData().setCreditbureauLoanProductMappingId(creditbureauLoanProductMappingId)
                .setOrganisationCreditBureauId(organisationCreditBureauId).setAlias(alias).setCreditbureauSummary(creditbureauSummary)
                .setLoanProductName(loanProductName).setLoanProductId(loanProductId).setCreditCheckMandatory(isCreditCheckMandatory)
                .setSkipCrediCheckInFailure(skipCrediCheckInFailure).setStalePeriod(stalePeriod).setActive(active);
    }

    public static CreditBureauLoanProductMappingData instance1(final String loanProductName, final long loanProductId) {
        return new CreditBureauLoanProductMappingData().setCreditbureauLoanProductMappingId(0).setOrganisationCreditBureauId(0).setAlias("")
                .setCreditbureauSummary("").setLoanProductName(loanProductName).setLoanProductId(loanProductId)
                .setCreditCheckMandatory(false).setSkipCrediCheckInFailure(false).setStalePeriod(0).setActive(false);
    }

}

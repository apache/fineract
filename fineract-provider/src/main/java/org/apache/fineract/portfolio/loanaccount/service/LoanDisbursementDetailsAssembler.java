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
package org.apache.fineract.portfolio.loanaccount.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.loanaccount.api.LoanApiConstants;
import org.apache.fineract.portfolio.loanaccount.domain.LoanDisbursementDetails;

@RequiredArgsConstructor
public class LoanDisbursementDetailsAssembler {

    private final FromJsonHelper fromApiJsonHelper;

    public List<LoanDisbursementDetails> fetchDisbursementData(final JsonObject command) {
        final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(command);
        final String dateFormat = this.fromApiJsonHelper.extractDateFormatParameter(command);
        List<LoanDisbursementDetails> disbursementDatas = new ArrayList<>();
        JsonArray disbursementDataArray = command.getAsJsonArray(LoanApiConstants.disbursementDataParameterName);
        if (disbursementDataArray != null && !disbursementDataArray.isEmpty()) {
            disbursementDataArray.forEach(jsonElement -> {
                LocalDate expectedDisbursementDate = this.fromApiJsonHelper
                        .extractLocalDateNamed(LoanApiConstants.expectedDisbursementDateParameterName, jsonElement, dateFormat, locale);
                BigDecimal principal = this.fromApiJsonHelper.extractBigDecimalNamed(LoanApiConstants.disbursementPrincipalParameterName,
                        jsonElement, locale);
                BigDecimal netDisbursalAmount = this.fromApiJsonHelper
                        .extractBigDecimalNamed(LoanApiConstants.disbursementNetDisbursalAmountParameterName, jsonElement, locale);
                boolean isReversed = Boolean.TRUE.equals(
                        this.fromApiJsonHelper.extractBooleanNamed(LoanApiConstants.disbursementReversedParameterName, jsonElement));
                disbursementDatas
                        .add(new LoanDisbursementDetails(expectedDisbursementDate, null, principal, netDisbursalAmount, isReversed));
            });
        }
        return disbursementDatas;
    }
}

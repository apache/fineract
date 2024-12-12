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
package org.apache.fineract.portfolio.interestpauses.service;

import java.util.List;
import java.util.Locale;
import lombok.AllArgsConstructor;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.interestpauses.data.InterestPauseResponseDto;
import org.apache.fineract.portfolio.loanaccount.data.LoanTermVariationsData;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTermVariationType;
import org.apache.fineract.portfolio.loanaccount.rescheduleloan.domain.LoanTermVariationsRepository;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
@Transactional(readOnly = true)
public class InterestPauseReadPlatformServiceImpl implements InterestPauseReadPlatformService {

    private final LoanTermVariationsRepository loanTermVariationsRepository;

    @Override
    public List<InterestPauseResponseDto> retrieveInterestPauses(Long loanId) {
        List<LoanTermVariationsData> variations = this.loanTermVariationsRepository.findLoanTermVariationsByLoanIdAndTermType(loanId,
                LoanTermVariationType.INTEREST_PAUSE.getValue());

        return mapToInterestPauseResponse(variations);
    }

    @Override
    public List<InterestPauseResponseDto> retrieveInterestPauses(String loanExternalId) {
        List<LoanTermVariationsData> variations = this.loanTermVariationsRepository.findLoanTermVariationsByExternalLoanIdAndTermType(
                new ExternalId(loanExternalId), LoanTermVariationType.INTEREST_PAUSE.getValue());

        return mapToInterestPauseResponse(variations);
    }

    private List<InterestPauseResponseDto> mapToInterestPauseResponse(List<LoanTermVariationsData> variations) {
        return variations.stream()
                .map(variation -> new InterestPauseResponseDto(variation.getId(), variation.getTermVariationApplicableFrom(),
                        variation.getDateValue(), DateUtils.DEFAULT_DATE_FORMAT, Locale.getDefault().toString()))
                .toList();
    }
}

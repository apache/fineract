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

package org.apache.fineract.portfolio.interestratechart.service;

import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.portfolio.interestratechart.data.InterestRateChartSlabsCreateRequest;
import org.apache.fineract.portfolio.interestratechart.data.InterestRateChartSlabsCreateResponse;
import org.apache.fineract.portfolio.interestratechart.data.InterestRateChartSlabsDeleteRequest;
import org.apache.fineract.portfolio.interestratechart.data.InterestRateChartSlabsDeleteResponse;
import org.apache.fineract.portfolio.interestratechart.data.InterestRateChartSlabsUpdateRequest;
import org.apache.fineract.portfolio.interestratechart.data.InterestRateChartSlabsUpdateResponse;
import org.apache.fineract.portfolio.interestratechart.domain.InterestIncentives;
import org.apache.fineract.portfolio.interestratechart.domain.InterestIncentivesFields;
import org.apache.fineract.portfolio.interestratechart.domain.InterestRateChart;
import org.apache.fineract.portfolio.interestratechart.domain.InterestRateChartRepositoryWrapper;
import org.apache.fineract.portfolio.interestratechart.domain.InterestRateChartSlab;
import org.apache.fineract.portfolio.interestratechart.domain.InterestRateChartSlabFields;
import org.apache.fineract.portfolio.interestratechart.domain.InterestRateChartSlabRepository;
import org.apache.fineract.portfolio.interestratechart.exception.InterestRateChartSlabNotFoundException;
import org.apache.fineract.portfolio.savings.SavingsPeriodFrequencyType;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class InterestRateChartSlabsWriteServiceImpl implements InterestRateChartSlabsWriteService {

    private final InterestRateChartRepositoryWrapper interestRateChartRepository;

    private final InterestRateChartSlabRepository chartSlabRepository;

    @Override
    @Transactional
    public InterestRateChartSlabsCreateResponse createInterestRateChartSlab(InterestRateChartSlabsCreateRequest request) {
        final InterestRateChart chart = interestRateChartRepository.findOneWithNotFoundDetection(request.getChartId());

        final SavingsPeriodFrequencyType periodType = SavingsPeriodFrequencyType.fromInt(request.getPeriodType());
        final BigDecimal annualInterestRate = request.getAnnualInterestRate() != null ? BigDecimal.valueOf(request.getAnnualInterestRate())
                : null;

        final InterestRateChartSlabFields slabFields = InterestRateChartSlabFields.createNew(request.getDescription(), periodType,
                request.getFromPeriod(), request.getToPeriod(), null, null, annualInterestRate, null);

        final InterestRateChartSlab interestRateChartSlab = InterestRateChartSlab.createNew(slabFields, chart);

        if (request.getIncentives() != null) {
            assembleIncentives(request.getIncentives(), interestRateChartSlab);
        }

        chartSlabRepository.saveAndFlush(interestRateChartSlab);

        return InterestRateChartSlabsCreateResponse.builder().resourceId(interestRateChartSlab.getId()).build();
    }

    @Override
    @Transactional
    public InterestRateChartSlabsUpdateResponse updateInterestRateChartSlab(InterestRateChartSlabsUpdateRequest request) {
        final InterestRateChart chart = interestRateChartRepository.findOneWithNotFoundDetection(request.getChartId());
        final InterestRateChartSlab chartSlab = chart.findChartSlab(request.getChartSlabId());

        if (chartSlab == null) {
            throw new InterestRateChartSlabNotFoundException(request.getChartSlabId(), request.getChartId());
        }

        if (request.getAnnualInterestRate() != null) {
            chartSlab.slabFields().setAnnualInterestRate(BigDecimal.valueOf(request.getAnnualInterestRate()));
        }

        if (request.getDescription() != null) {
            chartSlab.slabFields().setDescription(request.getDescription());
        }

        chartSlabRepository.saveAndFlush(chartSlab);

        return InterestRateChartSlabsUpdateResponse.builder().resourceId(request.getChartId()).build();
    }

    @Override
    @Transactional
    public InterestRateChartSlabsDeleteResponse deleteInterestRateChartSlab(InterestRateChartSlabsDeleteRequest request) {
        final InterestRateChart chart = interestRateChartRepository.findOneWithNotFoundDetection(request.getChartId());
        final InterestRateChartSlab chartSlab = chart.findChartSlab(request.getChartSlabId());

        if (chartSlab == null) {
            throw new InterestRateChartSlabNotFoundException(request.getChartSlabId(), request.getChartId());
        }

        chartSlabRepository.delete(chartSlab);

        return InterestRateChartSlabsDeleteResponse.builder().resourceId(request.getChartSlabId()).build();
    }

    private void assembleIncentives(List<InterestRateChartSlabsCreateRequest.Incentive> incentives,
            InterestRateChartSlab interestRateChartSlab) {
        for (InterestRateChartSlabsCreateRequest.Incentive incentive : incentives) {
            final InterestIncentivesFields fields = InterestIncentivesFields.createNew(incentive.getEntityType(),
                    incentive.getAttributeName(), incentive.getConditionType(), incentive.getAttributeValue(), incentive.getIncentiveType(),
                    incentive.getAmount(), null);
            // InterestIncentives constructor already calls addInterestIncentive on the slab
            new InterestIncentives(interestRateChartSlab, fields);
        }
    }
}

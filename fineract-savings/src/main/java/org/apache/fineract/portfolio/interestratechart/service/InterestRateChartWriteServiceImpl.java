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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.portfolio.interestratechart.data.InterestRateChartCreateRequest;
import org.apache.fineract.portfolio.interestratechart.data.InterestRateChartCreateResponse;
import org.apache.fineract.portfolio.interestratechart.data.InterestRateChartDeleteRequest;
import org.apache.fineract.portfolio.interestratechart.data.InterestRateChartDeleteResponse;
import org.apache.fineract.portfolio.interestratechart.data.InterestRateChartUpdateRequest;
import org.apache.fineract.portfolio.interestratechart.data.InterestRateChartUpdateResponse;
import org.apache.fineract.portfolio.interestratechart.domain.InterestRateChart;
import org.apache.fineract.portfolio.interestratechart.domain.InterestRateChartFields;
import org.apache.fineract.portfolio.interestratechart.domain.InterestRateChartRepositoryWrapper;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class InterestRateChartWriteServiceImpl implements InterestRateChartWriteService {

    private final InterestRateChartRepositoryWrapper interestRateChartRepository;

    @Override
    @Transactional
    public InterestRateChartCreateResponse createInterestRateChart(InterestRateChartCreateRequest request) {
        final String dateFormat = request.getDateFormat() != null ? request.getDateFormat() : "dd MMMM yyyy";
        final Locale locale = request.getLocale() != null ? Locale.forLanguageTag(request.getLocale()) : Locale.getDefault();
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat, locale);
        final LocalDate fromDate = request.getFromDate() != null ? LocalDate.parse(request.getFromDate(), formatter) : null;

        final InterestRateChartFields fields = InterestRateChartFields.createNew(request.getName(), request.getDescription(), fromDate,
                null, false);
        final InterestRateChart interestRateChart = InterestRateChart.createNew(fields, new ArrayList<>());

        interestRateChartRepository.saveAndFlush(interestRateChart);

        return InterestRateChartCreateResponse.builder().resourceId(interestRateChart.getId()).build();
    }

    @Override
    @Transactional
    public InterestRateChartUpdateResponse updateInterestRateChart(InterestRateChartUpdateRequest request) {
        final InterestRateChart interestRateChart = interestRateChartRepository.findOneWithNotFoundDetection(request.getId());

        if (request.getName() != null && !request.getName().equals(interestRateChart.chartFields().getName())) {
            interestRateChart.chartFields().setName(request.getName());
        }

        if (request.getDescription() != null && !request.getDescription().equals(interestRateChart.chartFields().getDescription())) {
            interestRateChart.chartFields().setDescription(request.getDescription());
        }

        interestRateChartRepository.saveAndFlush(interestRateChart);

        return InterestRateChartUpdateResponse.builder().resourceId(interestRateChart.getId()).build();
    }

    @Override
    @Transactional
    public InterestRateChartDeleteResponse deleteInterestRateChart(InterestRateChartDeleteRequest request) {
        final InterestRateChart chart = interestRateChartRepository.findOneWithNotFoundDetection(request.getChartId());

        interestRateChartRepository.delete(chart);

        return InterestRateChartDeleteResponse.builder().resourceId(chart.getId()).build();
    }
}

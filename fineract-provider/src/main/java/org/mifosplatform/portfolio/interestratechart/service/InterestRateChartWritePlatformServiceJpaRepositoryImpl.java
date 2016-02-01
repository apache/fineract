/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.interestratechart.service;

import java.util.LinkedHashMap;
import java.util.Map;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.interestratechart.data.InterestRateChartDataValidator;
import org.mifosplatform.portfolio.interestratechart.data.InterestRateChartRepositoryWrapper;
import org.mifosplatform.portfolio.interestratechart.domain.InterestRateChart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InterestRateChartWritePlatformServiceJpaRepositoryImpl implements InterestRateChartWritePlatformService {

    @SuppressWarnings("unused")
    private final static Logger logger = LoggerFactory.getLogger(InterestRateChartWritePlatformServiceJpaRepositoryImpl.class);
    @SuppressWarnings("unused")
    private final PlatformSecurityContext context;
    private final InterestRateChartDataValidator interestRateChartDataValidator;
    private final InterestRateChartAssembler interestRateChartAssembler;
    private final InterestRateChartRepositoryWrapper interestRateChartRepository;

    @Autowired
    public InterestRateChartWritePlatformServiceJpaRepositoryImpl(PlatformSecurityContext context,
            final InterestRateChartDataValidator interestRateChartDataValidator,
            final InterestRateChartAssembler interestRateChartAssembler,
            final InterestRateChartRepositoryWrapper interestRateChartRepository) {
        this.context = context;
        this.interestRateChartDataValidator = interestRateChartDataValidator;
        this.interestRateChartAssembler = interestRateChartAssembler;
        this.interestRateChartRepository = interestRateChartRepository;
    }

    @Override
    @Transactional
    public CommandProcessingResult create(JsonCommand command) {
        this.interestRateChartDataValidator.validateForCreate(command.json());

        final InterestRateChart interestRateChart = this.interestRateChartAssembler.assembleFrom(command);

        this.interestRateChartRepository.saveAndFlush(interestRateChart);

        final Long interestRateChartId = interestRateChart.getId();

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(interestRateChartId) //
                .build();
    }

    @Override
    @Transactional
    public CommandProcessingResult update(Long interestRateChartId, JsonCommand command) {
        this.interestRateChartDataValidator.validateUpdate(command.json());
        final Map<String, Object> changes = new LinkedHashMap<>(20);
        final InterestRateChart interestRateChart = this.interestRateChartAssembler.assembleFrom(interestRateChartId);

        interestRateChart.update(command, changes);

        this.interestRateChartRepository.saveAndFlush(interestRateChart);

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(interestRateChartId) //
                .with(changes).build();
    }

    @Override
    @Transactional
    public CommandProcessingResult deleteChart(Long chartId) {
        final InterestRateChart chart = this.interestRateChartRepository.findOneWithNotFoundDetection(chartId);
        // validate if chart is associated with any accounts

        this.interestRateChartRepository.delete(chart);
        return new CommandProcessingResultBuilder() //
                .withEntityId(chartId) //
                .build();
    }

}
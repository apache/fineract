/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.interestratechart.service;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.interestratechart.data.InterestRateChartRepositoryWrapper;
import org.mifosplatform.portfolio.interestratechart.data.InterestRateChartSlabDataValidator;
import org.mifosplatform.portfolio.interestratechart.data.InterestRateChartSlabRepository;
import org.mifosplatform.portfolio.interestratechart.domain.InterestRateChartSlab;
import org.mifosplatform.portfolio.savings.domain.SavingsProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InterestRateChartSlabWritePlatformServiceJpaRepositoryImpl implements InterestRateChartSlabWritePlatformService {

    @SuppressWarnings("unused")
    private final static Logger logger = LoggerFactory.getLogger(InterestRateChartSlabWritePlatformServiceJpaRepositoryImpl.class);
    @SuppressWarnings("unused")
    private final PlatformSecurityContext context;
    private final InterestRateChartSlabDataValidator interestRateChartSlabDataValidator;
    @SuppressWarnings("unused")
    private final InterestRateChartAssembler interestRateChartAssembler;
    private final InterestRateChartSlabAssembler interestRateChartSlabAssembler;
    @SuppressWarnings("unused")
    private final InterestRateChartRepositoryWrapper interestRateChartRepository;
    private final InterestRateChartSlabRepository chartSlabRepository;
    @SuppressWarnings("unused")
    private final SavingsProductRepository savingsProductRepository;

    @Autowired
    public InterestRateChartSlabWritePlatformServiceJpaRepositoryImpl(PlatformSecurityContext context,
            final InterestRateChartSlabDataValidator interestRateChartSlabDataValidator,
            final InterestRateChartAssembler interestRateChartAssembler,
            final InterestRateChartRepositoryWrapper interestRateChartRepository, final SavingsProductRepository savingsProductRepository,
            final InterestRateChartSlabRepository chartSlabRepository,
            final InterestRateChartSlabAssembler interestRateChartSlabAssembler) {
        this.context = context;
        this.interestRateChartSlabDataValidator = interestRateChartSlabDataValidator;
        this.interestRateChartAssembler = interestRateChartAssembler;
        this.interestRateChartRepository = interestRateChartRepository;
        this.savingsProductRepository = savingsProductRepository;
        this.chartSlabRepository = chartSlabRepository;
        this.interestRateChartSlabAssembler = interestRateChartSlabAssembler;
    }

    @Override
    @Transactional
    public CommandProcessingResult create(JsonCommand command) {
        this.interestRateChartSlabDataValidator.validateCreate(command.json());

        final InterestRateChartSlab interestRateChartSlab = this.interestRateChartSlabAssembler.assembleFrom(command);

        this.chartSlabRepository.save(interestRateChartSlab);

        final Long interestRateChartId = interestRateChartSlab.getId();

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(interestRateChartId) //
                .build();
    }

    @Override
    @Transactional
    public CommandProcessingResult update(Long chartSlabId, Long interestRateChartId, JsonCommand command) {
        this.interestRateChartSlabDataValidator.validateUpdate(command.json());
        final Map<String, Object> changes = new LinkedHashMap<>(20);
        final InterestRateChartSlab updateChartSlabs = this.interestRateChartSlabAssembler.assembleFrom(chartSlabId,
                interestRateChartId);
        final Locale locale = command.extractLocale();
        updateChartSlabs.update(command, changes,locale);

        this.chartSlabRepository.saveAndFlush(updateChartSlabs);

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(interestRateChartId) //
                .with(changes).build();
    }

    @Override
    @Transactional
    public CommandProcessingResult deleteChartSlab(Long chartSlabId, Long interestRateChartId) {
        final InterestRateChartSlab deleteChartSlabs = this.interestRateChartSlabAssembler.assembleFrom(chartSlabId,
                interestRateChartId);
        this.chartSlabRepository.delete(deleteChartSlabs);
        return new CommandProcessingResultBuilder() //
                .withEntityId(chartSlabId) //
                .build();
    }

}
/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.collateral.service;

import java.util.Map;

import org.mifosplatform.infrastructure.codes.domain.CodeValue;
import org.mifosplatform.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.mifosplatform.infrastructure.codes.exception.CodeValueNotFoundException;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.collateral.command.CollateralCommand;
import org.mifosplatform.portfolio.collateral.domain.LoanCollateral;
import org.mifosplatform.portfolio.collateral.domain.LoanCollateralRepository;
import org.mifosplatform.portfolio.collateral.exception.CollateralNotFoundException;
import org.mifosplatform.portfolio.collateral.serialization.CollateralCommandFromApiJsonDeserializer;
import org.mifosplatform.portfolio.loanaccount.domain.Loan;
import org.mifosplatform.portfolio.loanaccount.domain.LoanRepository;
import org.mifosplatform.portfolio.loanaccount.exception.LoanNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CollateralWritePlatformServiceJpaRepositoryImpl implements CollateralWritePlatformService {

    private final static Logger logger = LoggerFactory.getLogger(CollateralWritePlatformServiceJpaRepositoryImpl.class);

    private final PlatformSecurityContext context;
    private final LoanRepository loanRepository;
    private final LoanCollateralRepository collateralRepository;
    private final CodeValueRepositoryWrapper codeValueRepository;
    private final CollateralCommandFromApiJsonDeserializer collateralCommandFromApiJsonDeserializer;

    @Autowired
    public CollateralWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context, final LoanRepository loanRepository,
            final LoanCollateralRepository collateralRepository, final CodeValueRepositoryWrapper codeValueRepository,
            final CollateralCommandFromApiJsonDeserializer collateralCommandFromApiJsonDeserializer) {
        this.context = context;
        this.loanRepository = loanRepository;
        this.collateralRepository = collateralRepository;
        this.codeValueRepository = codeValueRepository;
        this.collateralCommandFromApiJsonDeserializer = collateralCommandFromApiJsonDeserializer;
    }

    @Transactional
    @Override
    public CommandProcessingResult addCollateral(final Long loanId, final JsonCommand command) {

        this.context.authenticatedUser();
        final CollateralCommand collateralCommand = this.collateralCommandFromApiJsonDeserializer.commandFromApiJson(command.json());
        collateralCommand.validateForCreate();

        try {
            final Loan loan = this.loanRepository.findOne(loanId);
            if (loan == null) { throw new LoanNotFoundException(loanId); }

            // TODO Vishwas, need to check if the codevalue is of the right code
            // (same fix needed in client Identifiers)
            final CodeValue collateralType = this.codeValueRepository.findOneWithNotFoundDetection(collateralCommand.getCollateralTypeId());
            final LoanCollateral collateral = LoanCollateral.fromJson(loan, collateralType, command);

            this.collateralRepository.save(collateral);

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withLoanId(loan.getId())//
                    .withEntityId(collateral.getId()) //
                    .build();
        } catch (final DataIntegrityViolationException dve) {
            handleCollateralDataIntegrityViolation(dve);
            return CommandProcessingResult.empty();
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult updateCollateral(final Long loanId, final Long collateralId, final JsonCommand command) {

        this.context.authenticatedUser();
        final CollateralCommand collateralCommand = this.collateralCommandFromApiJsonDeserializer.commandFromApiJson(command.json());
        collateralCommand.validateForUpdate();

        Long collateralTypeId = collateralCommand.getCollateralTypeId();
        try {
            final Loan loan = this.loanRepository.findOne(loanId);
            if (loan == null) { throw new LoanNotFoundException(loanId); }

            CodeValue collateralType = null;

            final LoanCollateral collateralForUpdate = this.collateralRepository.findOne(collateralId);
            if (collateralForUpdate == null) { throw new CollateralNotFoundException(loanId, collateralId); }

            final Map<String, Object> changes = collateralForUpdate.update(command);

            if (changes.containsKey("collateralTypeId")) {
                // TODO Vishwas, need to check if the codevalue is of the right
                // code
                // (same fix needed in client Identifiers)
                collateralType = this.codeValueRepository.findOneWithNotFoundDetection(collateralTypeId);
                if (collateralType == null) { throw new CodeValueNotFoundException(collateralTypeId); }
                collateralTypeId = collateralType.getId();
                collateralForUpdate.setCollateralType(collateralType);
            }

            if (!changes.isEmpty()) {
                this.collateralRepository.saveAndFlush(collateralForUpdate);
            }

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withLoanId(command.getLoanId())//
                    .withEntityId(collateralId) //
                    .with(changes) //
                    .build();
        } catch (final DataIntegrityViolationException dve) {
            handleCollateralDataIntegrityViolation(dve);
            return new CommandProcessingResult(Long.valueOf(-1));
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult deleteCollateral(final Long loanId, final Long collateralId, final Long commandId) {
        // TODO vishwas fetch collateral by loan and collateral Id
        final Loan loan = this.loanRepository.findOne(loanId);
        if (loan == null) { throw new LoanNotFoundException(loanId); }
        final LoanCollateral collateral = this.collateralRepository.findOne(collateralId);
        if (collateral == null) { throw new CollateralNotFoundException(loanId, collateralId); }
        loan.getCollateral().remove(collateral);
        this.collateralRepository.delete(collateral);

        return new CommandProcessingResultBuilder().withCommandId(commandId).withLoanId(loanId).withEntityId(collateralId).build();
    }

    private void handleCollateralDataIntegrityViolation(final DataIntegrityViolationException dve) {
        logAsErrorUnexpectedDataIntegrityException(dve);
        throw new PlatformDataIntegrityException("error.msg.collateral.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource.");
    }

    private void logAsErrorUnexpectedDataIntegrityException(final DataIntegrityViolationException dve) {
        logger.error(dve.getMessage(), dve);
    }

}
/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.collateral.service;

import java.util.Map;

import org.mifosplatform.infrastructure.codes.domain.CodeValue;
import org.mifosplatform.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.collateral.api.CollateralApiConstants;
import org.mifosplatform.portfolio.collateral.api.CollateralApiConstants.COLLATERAL_JSON_INPUT_PARAMS;
import org.mifosplatform.portfolio.collateral.command.CollateralCommand;
import org.mifosplatform.portfolio.collateral.domain.LoanCollateral;
import org.mifosplatform.portfolio.collateral.domain.LoanCollateralRepository;
import org.mifosplatform.portfolio.collateral.exception.CollateralCannotBeCreatedException;
import org.mifosplatform.portfolio.collateral.exception.CollateralCannotBeCreatedException.LOAN_COLLATERAL_CANNOT_BE_CREATED_REASON;
import org.mifosplatform.portfolio.collateral.exception.CollateralCannotBeDeletedException;
import org.mifosplatform.portfolio.collateral.exception.CollateralCannotBeDeletedException.LOAN_COLLATERAL_CANNOT_BE_DELETED_REASON;
import org.mifosplatform.portfolio.collateral.exception.CollateralCannotBeUpdatedException;
import org.mifosplatform.portfolio.collateral.exception.CollateralCannotBeUpdatedException.LOAN_COLLATERAL_CANNOT_BE_UPDATED_REASON;
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

            final CodeValue collateralType = this.codeValueRepository.findOneByCodeNameAndIdWithNotFoundDetection(
                    CollateralApiConstants.COLLATERAL_CODE_NAME, collateralCommand.getCollateralTypeId());
            final LoanCollateral collateral = LoanCollateral.fromJson(loan, collateralType, command);

            /**
             * Collaterals may be added only when the loan associated with them
             * are yet to be approved
             **/
            if (!loan.status().isSubmittedAndPendingApproval()) { throw new CollateralCannotBeCreatedException(
                    LOAN_COLLATERAL_CANNOT_BE_CREATED_REASON.LOAN_NOT_IN_SUBMITTED_AND_PENDING_APPROVAL_STAGE, loan.getId()); }

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

        final Long collateralTypeId = collateralCommand.getCollateralTypeId();
        try {
            final Loan loan = this.loanRepository.findOne(loanId);
            if (loan == null) { throw new LoanNotFoundException(loanId); }

            CodeValue collateralType = null;

            final LoanCollateral collateralForUpdate = this.collateralRepository.findOne(collateralId);
            if (collateralForUpdate == null) { throw new CollateralNotFoundException(loanId, collateralId); }

            final Map<String, Object> changes = collateralForUpdate.update(command);

            if (changes.containsKey(COLLATERAL_JSON_INPUT_PARAMS.COLLATERAL_TYPE_ID.getValue())) {

                collateralType = this.codeValueRepository.findOneByCodeNameAndIdWithNotFoundDetection(
                        CollateralApiConstants.COLLATERAL_CODE_NAME, collateralTypeId);
                collateralForUpdate.setCollateralType(collateralType);
            }

            /**
             * Collaterals may be updated only when the loan associated with
             * them are yet to be approved
             **/
            if (!loan.status().isSubmittedAndPendingApproval()) { throw new CollateralCannotBeUpdatedException(
                    LOAN_COLLATERAL_CANNOT_BE_UPDATED_REASON.LOAN_NOT_IN_SUBMITTED_AND_PENDING_APPROVAL_STAGE, loan.getId()); }

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
        final Loan loan = this.loanRepository.findOne(loanId);
        if (loan == null) { throw new LoanNotFoundException(loanId); }
        final LoanCollateral collateral = this.collateralRepository.findByLoanIdAndId(loanId, collateralId);
        if (collateral == null) { throw new CollateralNotFoundException(loanId, collateralId); }

        /**
         * Collaterals may be deleted only when the loan associated with them
         * are yet to be approved
         **/
        if (!loan.status().isSubmittedAndPendingApproval()) { throw new CollateralCannotBeDeletedException(
                LOAN_COLLATERAL_CANNOT_BE_DELETED_REASON.LOAN_NOT_IN_SUBMITTED_AND_PENDING_APPROVAL_STAGE, loanId, collateralId); }

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
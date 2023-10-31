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
package org.apache.fineract.portfolio.collateral.service;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.ErrorHandler;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.collateral.api.CollateralApiConstants;
import org.apache.fineract.portfolio.collateral.api.CollateralApiConstants.CollateralJSONinputParams;
import org.apache.fineract.portfolio.collateral.command.CollateralCommand;
import org.apache.fineract.portfolio.collateral.domain.LoanCollateral;
import org.apache.fineract.portfolio.collateral.domain.LoanCollateralRepository;
import org.apache.fineract.portfolio.collateral.exception.CollateralCannotBeCreatedException;
import org.apache.fineract.portfolio.collateral.exception.CollateralCannotBeCreatedException.LoanCollateralCannotBeCreatedReason;
import org.apache.fineract.portfolio.collateral.exception.CollateralCannotBeDeletedException;
import org.apache.fineract.portfolio.collateral.exception.CollateralCannotBeDeletedException.LoanCollateralCannotBeDeletedReason;
import org.apache.fineract.portfolio.collateral.exception.CollateralCannotBeUpdatedException;
import org.apache.fineract.portfolio.collateral.exception.CollateralCannotBeUpdatedException.LoanCollateralCannotBeUpdatedReason;
import org.apache.fineract.portfolio.collateral.exception.CollateralNotFoundException;
import org.apache.fineract.portfolio.collateral.serialization.CollateralCommandFromApiJsonDeserializer;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
public class CollateralWritePlatformServiceJpaRepositoryImpl implements CollateralWritePlatformService {

    private final PlatformSecurityContext context;
    private final LoanRepositoryWrapper loanRepositoryWrapper;
    private final LoanCollateralRepository collateralRepository;
    private final CodeValueRepositoryWrapper codeValueRepository;
    private final CollateralCommandFromApiJsonDeserializer collateralCommandFromApiJsonDeserializer;

    @Transactional
    @Override
    public CommandProcessingResult addCollateral(final Long loanId, final JsonCommand command) {

        this.context.authenticatedUser();
        final CollateralCommand collateralCommand = this.collateralCommandFromApiJsonDeserializer.commandFromApiJson(command.json());
        collateralCommand.validateForCreate();

        try {
            final Loan loan = this.loanRepositoryWrapper.findOneWithNotFoundDetection(loanId, true);
            final CodeValue collateralType = this.codeValueRepository.findOneByCodeNameAndIdWithNotFoundDetection(
                    CollateralApiConstants.COLLATERAL_CODE_NAME, collateralCommand.getCollateralTypeId());
            final LoanCollateral collateral = LoanCollateral.fromJson(loan, collateralType, command);

            /**
             * Collaterals may be added only when the loan associated with them are yet to be approved
             **/
            if (!loan.getStatus().isSubmittedAndPendingApproval()) {
                throw new CollateralCannotBeCreatedException(
                        LoanCollateralCannotBeCreatedReason.LOAN_NOT_IN_SUBMITTED_AND_PENDING_APPROVAL_STAGE, loan.getId());
            }

            this.collateralRepository.saveAndFlush(collateral);

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withLoanId(loan.getId())//
                    .withEntityId(collateral.getId()) //
                    .build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
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
            final Loan loan = this.loanRepositoryWrapper.findOneWithNotFoundDetection(loanId, true);
            CodeValue collateralType = null;

            final LoanCollateral collateralForUpdate = this.collateralRepository.findById(collateralId)
                    .orElseThrow(() -> new CollateralNotFoundException(loanId, collateralId));

            final Map<String, Object> changes = collateralForUpdate.update(command);

            if (changes.containsKey(CollateralJSONinputParams.COLLATERAL_TYPE_ID.getValue())) {

                collateralType = this.codeValueRepository
                        .findOneByCodeNameAndIdWithNotFoundDetection(CollateralApiConstants.COLLATERAL_CODE_NAME, collateralTypeId);
                collateralForUpdate.setCollateralType(collateralType);
            }

            /**
             * Collaterals may be updated only when the loan associated with them are yet to be approved
             **/
            if (!loan.getStatus().isSubmittedAndPendingApproval()) {
                throw new CollateralCannotBeUpdatedException(
                        LoanCollateralCannotBeUpdatedReason.LOAN_NOT_IN_SUBMITTED_AND_PENDING_APPROVAL_STAGE, loan.getId());
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
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleCollateralDataIntegrityViolation(dve);
            return CommandProcessingResult.resourceResult(-1L);
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult deleteCollateral(final Long loanId, final Long collateralId, final Long commandId) {
        final Loan loan = this.loanRepositoryWrapper.findOneWithNotFoundDetection(loanId, true);
        final LoanCollateral collateral = this.collateralRepository.findByLoanIdAndId(loanId, collateralId);
        if (collateral == null) {
            throw new CollateralNotFoundException(loanId, collateralId);
        }

        /**
         * Collaterals may be deleted only when the loan associated with them are yet to be approved
         **/
        if (!loan.getStatus().isSubmittedAndPendingApproval()) {
            throw new CollateralCannotBeDeletedException(
                    LoanCollateralCannotBeDeletedReason.LOAN_NOT_IN_SUBMITTED_AND_PENDING_APPROVAL_STAGE, loanId, collateralId);
        }

        this.collateralRepository.delete(collateral);
        return new CommandProcessingResultBuilder().withCommandId(commandId).withLoanId(loanId).withEntityId(collateralId).build();
    }

    private void handleCollateralDataIntegrityViolation(final NonTransientDataAccessException dve) {
        log.error("Error occured.", dve);
        throw ErrorHandler.getMappable(dve, "error.msg.collateral.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource.");
    }
}

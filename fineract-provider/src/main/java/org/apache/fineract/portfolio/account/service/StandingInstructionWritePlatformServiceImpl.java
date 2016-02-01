/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.account.service;

import static org.mifosplatform.portfolio.account.AccountDetailConstants.fromAccountTypeParamName;
import static org.mifosplatform.portfolio.account.AccountDetailConstants.fromClientIdParamName;
import static org.mifosplatform.portfolio.account.AccountDetailConstants.toAccountTypeParamName;
import static org.mifosplatform.portfolio.account.api.StandingInstructionApiConstants.statusParamName;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.core.exception.AbstractPlatformServiceUnavailableException;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.core.service.RoutingDataSource;
import org.mifosplatform.infrastructure.jobs.annotation.CronTarget;
import org.mifosplatform.infrastructure.jobs.exception.JobExecutionException;
import org.mifosplatform.infrastructure.jobs.service.JobName;
import org.mifosplatform.portfolio.account.PortfolioAccountType;
import org.mifosplatform.portfolio.account.api.StandingInstructionApiConstants;
import org.mifosplatform.portfolio.account.data.AccountTransferDTO;
import org.mifosplatform.portfolio.account.data.StandingInstructionData;
import org.mifosplatform.portfolio.account.data.StandingInstructionDataValidator;
import org.mifosplatform.portfolio.account.data.StandingInstructionDuesData;
import org.mifosplatform.portfolio.account.domain.AccountTransferDetailRepository;
import org.mifosplatform.portfolio.account.domain.AccountTransferDetails;
import org.mifosplatform.portfolio.account.domain.AccountTransferRecurrenceType;
import org.mifosplatform.portfolio.account.domain.AccountTransferStandingInstruction;
import org.mifosplatform.portfolio.account.domain.StandingInstructionAssembler;
import org.mifosplatform.portfolio.account.domain.StandingInstructionRepository;
import org.mifosplatform.portfolio.account.domain.StandingInstructionStatus;
import org.mifosplatform.portfolio.account.domain.StandingInstructionType;
import org.mifosplatform.portfolio.account.exception.StandingInstructionNotFoundException;
import org.mifosplatform.portfolio.common.domain.PeriodFrequencyType;
import org.mifosplatform.portfolio.loanaccount.loanschedule.domain.DefaultScheduledDateGenerator;
import org.mifosplatform.portfolio.loanaccount.loanschedule.domain.ScheduledDateGenerator;
import org.mifosplatform.portfolio.savings.domain.SavingsAccount;
import org.mifosplatform.portfolio.savings.exception.InsufficientAccountBalanceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StandingInstructionWritePlatformServiceImpl implements StandingInstructionWritePlatformService {

    private final static Logger logger = LoggerFactory.getLogger(StandingInstructionWritePlatformServiceImpl.class);

    private final StandingInstructionDataValidator standingInstructionDataValidator;
    private final StandingInstructionAssembler standingInstructionAssembler;
    private final AccountTransferDetailRepository accountTransferDetailRepository;
    private final StandingInstructionRepository standingInstructionRepository;
    private final StandingInstructionReadPlatformService standingInstructionReadPlatformService;
    private final AccountTransfersWritePlatformService accountTransfersWritePlatformService;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public StandingInstructionWritePlatformServiceImpl(final StandingInstructionDataValidator standingInstructionDataValidator,
            final StandingInstructionAssembler standingInstructionAssembler,
            final AccountTransferDetailRepository accountTransferDetailRepository,
            final StandingInstructionRepository standingInstructionRepository,
            final StandingInstructionReadPlatformService standingInstructionReadPlatformService,
            final AccountTransfersWritePlatformService accountTransfersWritePlatformService, final RoutingDataSource dataSource) {
        this.standingInstructionDataValidator = standingInstructionDataValidator;
        this.standingInstructionAssembler = standingInstructionAssembler;
        this.accountTransferDetailRepository = accountTransferDetailRepository;
        this.standingInstructionRepository = standingInstructionRepository;
        this.standingInstructionReadPlatformService = standingInstructionReadPlatformService;
        this.accountTransfersWritePlatformService = accountTransfersWritePlatformService;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Transactional
    @Override
    public CommandProcessingResult create(final JsonCommand command) {

        this.standingInstructionDataValidator.validateForCreate(command);

        final Integer fromAccountTypeId = command.integerValueSansLocaleOfParameterNamed(fromAccountTypeParamName);
        final PortfolioAccountType fromAccountType = PortfolioAccountType.fromInt(fromAccountTypeId);

        final Integer toAccountTypeId = command.integerValueSansLocaleOfParameterNamed(toAccountTypeParamName);
        final PortfolioAccountType toAccountType = PortfolioAccountType.fromInt(toAccountTypeId);

        final Long fromClientId = command.longValueOfParameterNamed(fromClientIdParamName);

        Long standingInstructionId = null;
        try {
            if (isSavingsToSavingsAccountTransfer(fromAccountType, toAccountType)) {
                final AccountTransferDetails standingInstruction = this.standingInstructionAssembler
                        .assembleSavingsToSavingsTransfer(command);
                this.accountTransferDetailRepository.save(standingInstruction);
                standingInstructionId = standingInstruction.accountTransferStandingInstruction().getId();
            } else if (isSavingsToLoanAccountTransfer(fromAccountType, toAccountType)) {
                final AccountTransferDetails standingInstruction = this.standingInstructionAssembler.assembleSavingsToLoanTransfer(command);
                this.accountTransferDetailRepository.save(standingInstruction);
                standingInstructionId = standingInstruction.accountTransferStandingInstruction().getId();
            } else if (isLoanToSavingsAccountTransfer(fromAccountType, toAccountType)) {

                final AccountTransferDetails standingInstruction = this.standingInstructionAssembler.assembleLoanToSavingsTransfer(command);
                this.accountTransferDetailRepository.save(standingInstruction);
                standingInstructionId = standingInstruction.accountTransferStandingInstruction().getId();

            }
        } catch (final DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve);
            return CommandProcessingResult.empty();
        }
        final CommandProcessingResultBuilder builder = new CommandProcessingResultBuilder().withEntityId(standingInstructionId)
                .withClientId(fromClientId);
        return builder.build();
    }

    private void handleDataIntegrityIssues(final JsonCommand command, final DataIntegrityViolationException dve) {

        final Throwable realCause = dve.getMostSpecificCause();
        if (realCause.getMessage().contains("name")) {
            final String name = command.stringValueOfParameterNamed(StandingInstructionApiConstants.nameParamName);
            throw new PlatformDataIntegrityException("error.msg.standinginstruction.duplicate.name", "Standinginstruction with name `"
                    + name + "` already exists", "name", name);
        }
        logger.error(dve.getMessage(), dve);
        throw new PlatformDataIntegrityException("error.msg.client.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource.");
    }

    private boolean isLoanToSavingsAccountTransfer(final PortfolioAccountType fromAccountType, final PortfolioAccountType toAccountType) {
        return fromAccountType.isLoanAccount() && toAccountType.isSavingsAccount();
    }

    private boolean isSavingsToLoanAccountTransfer(final PortfolioAccountType fromAccountType, final PortfolioAccountType toAccountType) {
        return fromAccountType.isSavingsAccount() && toAccountType.isLoanAccount();
    }

    private boolean isSavingsToSavingsAccountTransfer(final PortfolioAccountType fromAccountType, final PortfolioAccountType toAccountType) {
        return fromAccountType.isSavingsAccount() && toAccountType.isSavingsAccount();
    }

    @Override
    public CommandProcessingResult update(final Long id, final JsonCommand command) {
        this.standingInstructionDataValidator.validateForUpdate(command);
        AccountTransferStandingInstruction standingInstructionsForUpdate = this.standingInstructionRepository.findOne(id);
        if (standingInstructionsForUpdate == null) { throw new StandingInstructionNotFoundException(id); }
        final Map<String, Object> actualChanges = standingInstructionsForUpdate.update(command);
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(id) //
                .with(actualChanges) //
                .build();
    }

    @Override
    public CommandProcessingResult delete(final Long id) {
        AccountTransferStandingInstruction standingInstructionsForUpdate = this.standingInstructionRepository.findOne(id);
        standingInstructionsForUpdate.updateStatus(StandingInstructionStatus.DELETED.getValue());
        final Map<String, Object> actualChanges = new HashMap<>();
        actualChanges.put(statusParamName, StandingInstructionStatus.DELETED.getValue());
        return new CommandProcessingResultBuilder() //
                .withEntityId(id) //
                .with(actualChanges) //
                .build();
    }

    @Override
    @CronTarget(jobName = JobName.EXECUTE_STANDING_INSTRUCTIONS)
    public void executeStandingInstructions() throws JobExecutionException {
        Collection<StandingInstructionData> instructionDatas = this.standingInstructionReadPlatformService
                .retrieveAll(StandingInstructionStatus.ACTIVE.getValue());
        final StringBuilder sb = new StringBuilder();
        for (StandingInstructionData data : instructionDatas) {
            boolean isDueForTransfer = false;
            AccountTransferRecurrenceType recurrenceType = data.recurrenceType();
            StandingInstructionType instructionType = data.instructionType();
            LocalDate transactionDate = new LocalDate();
            if (recurrenceType.isPeriodicRecurrence()) {
                final ScheduledDateGenerator scheduledDateGenerator = new DefaultScheduledDateGenerator();
                PeriodFrequencyType frequencyType = data.recurrenceFrequency();
                LocalDate startDate = data.validFrom();
                if (frequencyType.isMonthly()) {
                    startDate = startDate.withDayOfMonth(data.recurrenceOnDay());
                    if (startDate.isBefore(data.validFrom())) {
                        startDate = startDate.plusMonths(1);
                    }
                } else if (frequencyType.isYearly()) {
                    startDate = startDate.withDayOfMonth(data.recurrenceOnDay()).withMonthOfYear(data.recurrenceOnMonth());
                    if (startDate.isBefore(data.validFrom())) {
                        startDate = startDate.plusYears(1);
                    }
                }
                isDueForTransfer = scheduledDateGenerator.isDateFallsInSchedule(frequencyType, data.recurrenceInterval(), startDate,
                        transactionDate);

            }
            BigDecimal transactionAmount = data.amount();
            if (data.toAccountType().isLoanAccount()
                    && (recurrenceType.isDuesRecurrence() || (isDueForTransfer && instructionType.isDuesAmoutTransfer()))) {
                StandingInstructionDuesData standingInstructionDuesData = this.standingInstructionReadPlatformService
                        .retriveLoanDuesData(data.toAccount().accountId());
                if (data.instructionType().isDuesAmoutTransfer()) {
                    transactionAmount = standingInstructionDuesData.totalDueAmount();
                }
                if (recurrenceType.isDuesRecurrence()) {
                    isDueForTransfer = new LocalDate().equals(standingInstructionDuesData.dueDate());
                }
            }

            if (isDueForTransfer && transactionAmount != null && transactionAmount.compareTo(BigDecimal.ZERO) > 0) {
                final AccountTransferDetails accountTransferDetails = this.accountTransferDetailRepository.findOne(data.accountDetailId());
                final SavingsAccount fromSavingsAccount = null;
                final boolean isRegularTransaction = true;
                final boolean isExceptionForBalanceCheck = false;
                accountTransferDetails.accountTransferStandingInstruction().updateLatsRunDate(transactionDate.toDate());
                AccountTransferDTO accountTransferDTO = new AccountTransferDTO(transactionDate, transactionAmount, data.fromAccountType(),
                        data.toAccountType(), data.fromAccount().accountId(), data.toAccount().accountId(), data.name()
                                + " Standing instruction trasfer ", null, null, null, null, data.toTransferType(), null, null, data
                                .transferType().getValue(), accountTransferDetails, null, null, null, null, fromSavingsAccount,
                        isRegularTransaction, isExceptionForBalanceCheck);
                transferAmount(sb, accountTransferDTO, data.getId());
            }
        }
        if (sb.length() > 0) { throw new JobExecutionException(sb.toString()); }

    }

    /**
     * @param sb
     * @param accountTransferDTO
     */
    private void transferAmount(final StringBuilder sb, final AccountTransferDTO accountTransferDTO, final Long instructionId) {
        StringBuffer errorLog = new StringBuffer();
        StringBuffer updateQuery = new StringBuffer(
                "INSERT INTO `m_account_transfer_standing_instructions_history` (`standing_instruction_id`, `status`, `amount`,`execution_time`, `error_log`) VALUES (");
        try {
            this.accountTransfersWritePlatformService.transferFunds(accountTransferDTO);
        } catch (final PlatformApiDataValidationException e) {
            sb.append("Validation exception while trasfering funds for standing Instruction id").append(instructionId).append(" from ")
                    .append(accountTransferDTO.getFromAccountId()).append(" to ").append(accountTransferDTO.getToAccountId())
                    .append("--------");
            errorLog.append("Validation exception while trasfering funds " + e.getDefaultUserMessage());
        } catch (final InsufficientAccountBalanceException e) {
            sb.append("InsufficientAccountBalance Exception while trasfering funds for standing Instruction id").append(instructionId)
                    .append(" from ").append(accountTransferDTO.getFromAccountId()).append(" to ")
                    .append(accountTransferDTO.getToAccountId()).append("--------");
            errorLog.append("InsufficientAccountBalance Exception ");
        } catch (final AbstractPlatformServiceUnavailableException e) {
            sb.append("Platform exception while trasfering funds for standing Instruction id").append(instructionId).append(" from ")
                    .append(accountTransferDTO.getFromAccountId()).append(" to ").append(accountTransferDTO.getToAccountId())
                    .append("--------");
            errorLog.append("Platform exception while trasfering funds " + e.getDefaultUserMessage());
        } catch (Exception e) {
            sb.append("Exception while trasfering funds for standing Instruction id").append(instructionId).append(" from ")
                    .append(accountTransferDTO.getFromAccountId()).append(" to ").append(accountTransferDTO.getToAccountId())
                    .append("--------");
            errorLog.append("Exception while trasfering funds " + e.getMessage());

        }
        updateQuery.append(instructionId).append(",");
        if (errorLog.length() > 0) {
            updateQuery.append("'failed'").append(",");
        } else {
            updateQuery.append("'success'").append(",");
        }
        updateQuery.append(accountTransferDTO.getTransactionAmount().doubleValue());
        updateQuery.append(", now(),");
        updateQuery.append("'").append(errorLog.toString()).append("')");
        this.jdbcTemplate.update(updateQuery.toString());

    }
}
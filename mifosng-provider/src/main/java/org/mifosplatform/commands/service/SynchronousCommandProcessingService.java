/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.commands.service;

import java.util.Map;

import org.joda.time.DateTime;
import org.mifosplatform.commands.domain.CommandSource;
import org.mifosplatform.commands.domain.CommandSourceRepository;
import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.exception.RollbackTransactionAsCommandIsNotApprovedByCheckerException;
import org.mifosplatform.commands.exception.UnsupportedCommandException;
import org.mifosplatform.commands.handler.NewCommandSourceHandler;
import org.mifosplatform.infrastructure.configuration.domain.ConfigurationDomainService;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.core.serialization.ToApiJsonSerializer;
import org.mifosplatform.infrastructure.core.service.ThreadLocalContextUtil;
import org.mifosplatform.infrastructure.hooks.event.HookEvent;
import org.mifosplatform.infrastructure.hooks.event.HookEventSource;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SynchronousCommandProcessingService implements CommandProcessingService {

    private PlatformSecurityContext context;
    private final ApplicationContext applicationContext;
    private final ToApiJsonSerializer<Map<String, Object>> toApiJsonSerializer;
    private final ToApiJsonSerializer<CommandProcessingResult> toApiResultJsonSerializer;
    private CommandSourceRepository commandSourceRepository;
    private final ConfigurationDomainService configurationDomainService;

    @Autowired
    public SynchronousCommandProcessingService(final PlatformSecurityContext context, final ApplicationContext applicationContext,
            final ToApiJsonSerializer<Map<String, Object>> toApiJsonSerializer,
            final ToApiJsonSerializer<CommandProcessingResult> toApiResultJsonSerializer,
            final CommandSourceRepository commandSourceRepository, final ConfigurationDomainService configurationDomainService) {
        this.context = context;
        this.context = context;
        this.applicationContext = applicationContext;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.toApiResultJsonSerializer = toApiResultJsonSerializer;
        this.commandSourceRepository = commandSourceRepository;
        this.commandSourceRepository = commandSourceRepository;
        this.configurationDomainService = configurationDomainService;
    }

    @Transactional
    @Override
    public CommandProcessingResult processAndLogCommand(final CommandWrapper wrapper, final JsonCommand command,
            final boolean isApprovedByChecker) {

        final boolean rollbackTransaction = this.configurationDomainService.isMakerCheckerEnabledForTask(wrapper.taskPermissionName());

        final NewCommandSourceHandler handler = findCommandHandler(wrapper);
        final CommandProcessingResult result = handler.processCommand(command);

        final AppUser maker = this.context.authenticatedUser(wrapper);

        CommandSource commandSourceResult = null;
        if (command.commandId() != null) {
            commandSourceResult = this.commandSourceRepository.findOne(command.commandId());
            commandSourceResult.markAsChecked(maker, DateTime.now());
        } else {
            commandSourceResult = CommandSource.fullEntryFrom(wrapper, command, maker);
        }
        commandSourceResult.updateResourceId(result.resourceId());
        commandSourceResult.updateForAudit(result.getOfficeId(), result.getGroupId(), result.getClientId(), result.getLoanId(),
                result.getSavingsId(), result.getProductId(), result.getTransactionId());

        String changesOnlyJson = null;
        if (result.hasChanges()) {
            changesOnlyJson = this.toApiJsonSerializer.serializeResult(result.getChanges());
            commandSourceResult.updateJsonTo(changesOnlyJson);
        }

        if (!result.hasChanges() && wrapper.isUpdateOperation() && !wrapper.isUpdateDatatable()) {
            commandSourceResult.updateJsonTo(null);
        }

        if (commandSourceResult.hasJson()) {
            this.commandSourceRepository.save(commandSourceResult);
        }

        if ((rollbackTransaction || result.isRollbackTransaction()) && !isApprovedByChecker) {
            /*
             * JournalEntry will generate a new transactionId every time.
             * Updating the transactionId with old transactionId, because as
             * there are no entries are created with new transactionId, will
             * throw an error when checker approves the transaction
             */
            commandSourceResult.updateTransaction(command.getTransactionId());
            /*
             * Update CommandSource json data with JsonCommand json data, line
             * 77 and 81 may update the json data
             */
            commandSourceResult.updateJsonTo(command.json());
            throw new RollbackTransactionAsCommandIsNotApprovedByCheckerException(commandSourceResult);
        }
        result.setRollbackTransaction(null);

        publishEvent(wrapper.entityName(), wrapper.actionName(), result);

        return result;
    }

    @Transactional
    @Override
    public CommandProcessingResult logCommand(CommandSource commandSourceResult) {

        commandSourceResult.markAsAwaitingApproval();
        commandSourceResult = this.commandSourceRepository.save(commandSourceResult);

        return new CommandProcessingResultBuilder().withCommandId(commandSourceResult.getId())
                .withEntityId(commandSourceResult.getResourceId()).build();
    }

    private NewCommandSourceHandler findCommandHandler(final CommandWrapper wrapper) {
        NewCommandSourceHandler handler = null;

        if (wrapper.isAccountTransferResource()) {
            if (wrapper.isCreate()) {
                handler = this.applicationContext.getBean("createAccountTransferCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isRefundByTransfer()) {
                handler = this.applicationContext.getBean("refundByTransferCommandHandler", NewCommandSourceHandler.class);
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }
        } else if (wrapper.isConfigurationResource()) {
            handler = this.applicationContext.getBean("updateGlobalConfigurationCommandHandler", NewCommandSourceHandler.class);
        } else if (wrapper.isDatatableResource()) {
            if (wrapper.isCreateDatatable()) {
                handler = this.applicationContext.getBean("createDatatableCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isDeleteDatatable()) {
                handler = this.applicationContext.getBean("deleteDatatableCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUpdateDatatable()) {
                handler = this.applicationContext.getBean("updateDatatableCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isCreate()) {
                handler = this.applicationContext.getBean("createDatatableEntryCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUpdateMultiple()) {
                handler = this.applicationContext.getBean("updateOneToManyDatatableEntryCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUpdateOneToOne()) {
                handler = this.applicationContext.getBean("updateOneToOneDatatableEntryCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isDeleteMultiple()) {
                handler = this.applicationContext.getBean("deleteOneToManyDatatableEntryCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isDeleteOneToOne()) {
                handler = this.applicationContext.getBean("deleteOneToOneDatatableEntryCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isRegisterDatatable()) {
                handler = this.applicationContext.getBean("registerDatatableCommandHandler", NewCommandSourceHandler.class);
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }
        } else if (wrapper.isNoteResource()) {
            if (wrapper.isCreate()) {
                handler = this.applicationContext.getBean("createNoteCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUpdate()) {
                handler = this.applicationContext.getBean("updateNoteCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isDelete()) {
                handler = this.applicationContext.getBean("deleteNoteCommandHandler", NewCommandSourceHandler.class);
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }
        } else if (wrapper.isClientIdentifierResource()) {
            if (wrapper.isCreate()) {
                handler = this.applicationContext.getBean("createClientIdentifierCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUpdate()) {
                handler = this.applicationContext.getBean("updateClientIdentifierCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isDelete()) {
                handler = this.applicationContext.getBean("deleteClientIdentifierCommandHandler", NewCommandSourceHandler.class);
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }
        } else if (wrapper.isClientResource() && !wrapper.isClientNoteResource() && !wrapper.isClientIdentifierResource()) {
            if (wrapper.isCreate()) {
                handler = this.applicationContext.getBean("createClientCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUpdate()) {
                handler = this.applicationContext.getBean("updateClientCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isDelete()) {
                handler = this.applicationContext.getBean("deleteClientCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isClientActivation()) {
                handler = this.applicationContext.getBean("activateClientCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isClientUnassignStaff()) {
                handler = this.applicationContext.getBean("unassignClientStaffCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isClientAssignStaff()) {
                handler = this.applicationContext.getBean("assignClientStaffCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isClientClose()) {
                handler = this.applicationContext.getBean("closeClientCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isProposeClientTransfer()) {
                handler = this.applicationContext.getBean("proposeClientTransferCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isProposeAndAcceptClientTransfer()) {
                handler = this.applicationContext.getBean("proposeAndAcceptClientTransferCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isWithdrawClientTransfer()) {
                handler = this.applicationContext.getBean("withdrawClientTransferCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isAcceptClientTransfer()) {
                handler = this.applicationContext.getBean("acceptClientTransferCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isRejectClientTransfer()) {
                handler = this.applicationContext.getBean("rejectClientTransferCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUpdateClientSavingsAccount()) {
                handler = this.applicationContext.getBean("updateClientSavingsAccountCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isReject()) {
                handler = this.applicationContext.getBean("rejectClientCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isWithdrawn()) {
                handler = this.applicationContext.getBean("withdrawClientCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isReactivated()) {
                handler = this.applicationContext.getBean("reActivateClientCommandHandler", NewCommandSourceHandler.class);
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }
            // end of client
        } else if (wrapper.isUpdateRolePermissions()) {
            handler = this.applicationContext.getBean("updateRolePermissionsCommandHandler", NewCommandSourceHandler.class);
        } else if (wrapper.isPermissionResource()) {
            handler = this.applicationContext.getBean("updateMakerCheckerPermissionsCommandHandler", NewCommandSourceHandler.class);
        } else if (wrapper.isRoleResource()) {
            if (wrapper.isCreate()) {
                handler = this.applicationContext.getBean("createRoleCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUpdate()) {
                handler = this.applicationContext.getBean("updateRoleCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isDelete()) {
                handler = this.applicationContext.getBean("deleteRoleCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isDisable()) {
                handler = this.applicationContext.getBean("disableRoleCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isEnable()) {
                handler = this.applicationContext.getBean("enableRoleCommandHandler", NewCommandSourceHandler.class);
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }
        } else if (wrapper.isUserResource()) {
            if (wrapper.isCreate()) {
                handler = this.applicationContext.getBean("createUserCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUpdate()) {
                handler = this.applicationContext.getBean("updateUserCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isDelete()) {
                handler = this.applicationContext.getBean("deleteUserCommandHandler", NewCommandSourceHandler.class);
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }
        } else if (wrapper.isHookResource()) {
            if (wrapper.isCreate()) {
                handler = this.applicationContext.getBean("createHookCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUpdate()) {
                handler = this.applicationContext.getBean("updateHookCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isDelete()) {
                handler = this.applicationContext.getBean("deleteHookCommandHandler", NewCommandSourceHandler.class);
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }
        } else if (wrapper.isStaffResource()) {
            if (wrapper.isCreate()) {
                handler = this.applicationContext.getBean("createStaffCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUpdate()) {
                handler = this.applicationContext.getBean("updateStaffCommandHandler", NewCommandSourceHandler.class);
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }
        } else if (wrapper.isGuarantorResource()) {
            if (wrapper.isCreate()) {
                handler = this.applicationContext.getBean("createGuarantorCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUpdate()) {
                handler = this.applicationContext.getBean("updateGuarantorCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isDelete()) {
                handler = this.applicationContext.getBean("deleteGuarantorCommandHandler", NewCommandSourceHandler.class);
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }
        } else if (wrapper.isTellerResource()) {
            if (wrapper.isCreate()) {
                handler = this.applicationContext.getBean("createTellerCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUpdate()) {
                handler = this.applicationContext.getBean("updateTellerCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isDelete()) {
                handler = this.applicationContext.getBean("deleteTellerCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isAllocateCashier()) {
                handler = this.applicationContext.getBean("allocateCashierToTellerCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUpdateCashierAllocation()) {
                handler = this.applicationContext.getBean("updateCashierAllocationCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isDeleteCashierAllocation()) {
                handler = this.applicationContext.getBean("deleteCashierAllocationCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isAllocateCashToCashier()) {
                handler = this.applicationContext.getBean("allocateCashToCashierCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isSettleCashFromCashier()) {
                handler = this.applicationContext.getBean("settleCashFromCashierCommandHandler", NewCommandSourceHandler.class);
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }
        } else if (wrapper.isCollateralResource()) {
            if (wrapper.isCreate()) {
                handler = this.applicationContext.getBean("createCollateralCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUpdate()) {
                handler = this.applicationContext.getBean("updateCollateralCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isDelete()) {
                handler = this.applicationContext.getBean("deleteCollateralCommandHandler", NewCommandSourceHandler.class);
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }
        } else if (wrapper.isCodeResource()) {
            if (wrapper.isCreate()) {
                handler = this.applicationContext.getBean("createCodeCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUpdate()) {
                handler = this.applicationContext.getBean("updateCodeCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isDelete()) {
                handler = this.applicationContext.getBean("deleteCodeCommandHandler", NewCommandSourceHandler.class);
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }
        } else if (wrapper.isCodeValueResource()) {
            if (wrapper.isCreate()) {
                handler = this.applicationContext.getBean("createCodeValueCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUpdate()) {
                handler = this.applicationContext.getBean("updateCodeValueCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isDelete()) {
                handler = this.applicationContext.getBean("deleteCodeValueCommandHandler", NewCommandSourceHandler.class);
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }
        } else if (wrapper.isSmsResource()) {
            if (wrapper.isCreate()) {
                handler = this.applicationContext.getBean("createSmsCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUpdate()) {
                handler = this.applicationContext.getBean("updateSmsCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isDelete()) {
                handler = this.applicationContext.getBean("deleteSmsCommandHandler", NewCommandSourceHandler.class);
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }
        } else if (wrapper.isCurrencyResource()) {
            handler = this.applicationContext.getBean("updateCurrencyCommandHandler", NewCommandSourceHandler.class);
        } else if (wrapper.isFundResource()) {
            if (wrapper.isCreate()) {
                handler = this.applicationContext.getBean("createFundCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUpdate()) {
                handler = this.applicationContext.getBean("updateFundCommandHandler", NewCommandSourceHandler.class);
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }
        } else if (wrapper.isOfficeResource()) {
            if (wrapper.isCreate()) {
                handler = this.applicationContext.getBean("createOfficeCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUpdate()) {
                handler = this.applicationContext.getBean("updateOfficeCommandHandler", NewCommandSourceHandler.class);
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }
        } else if (wrapper.isOfficeTransactionResource()) {
            if (wrapper.isCreate()) {
                handler = this.applicationContext.getBean("createOfficeTransactionCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isDelete()) {
                handler = this.applicationContext.getBean("deleteOfficeTransactionCommandHandler", NewCommandSourceHandler.class);
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }
        } else if (wrapper.isChargeDefinitionResource()) {
            if (wrapper.isCreate()) {
                handler = this.applicationContext.getBean("createChargeDefinitionCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUpdate()) {
                handler = this.applicationContext.getBean("updateChargeDefinitionCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isDelete()) {
                handler = this.applicationContext.getBean("deleteChargeDefinitionCommandHandler", NewCommandSourceHandler.class);
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }
        } else if (wrapper.isLoanProductResource()) {
            if (wrapper.isCreate()) {
                handler = this.applicationContext.getBean("createLoanProductCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUpdate()) {
                handler = this.applicationContext.getBean("updateLoanProductCommandHandler", NewCommandSourceHandler.class);
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }
        } else if (wrapper.isLoanResource()) {
            if (wrapper.isApproveLoanApplication()) {
                handler = this.applicationContext.getBean("loanApplicationApprovalCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUndoApprovalOfLoanApplication()) {
                handler = this.applicationContext.getBean("loanApplicationApprovalUndoCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isApplicantWithdrawalFromLoanApplication()) {
                handler = this.applicationContext.getBean("loanApplicationWithdrawnByApplicantCommandHandler",
                        NewCommandSourceHandler.class);
            } else if (wrapper.isRejectionOfLoanApplication()) {
                handler = this.applicationContext.getBean("loanApplicationRejectedCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isDisbursementOfLoan()) {
                handler = this.applicationContext.getBean("disburseLoanCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isDisbursementOfLoanToSavings()) {
                handler = this.applicationContext.getBean("disburseLoanToSavingsCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUndoDisbursementOfLoan()) {
                handler = this.applicationContext.getBean("undoDisbursalLoanCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isLoanRepayment()) {
                handler = this.applicationContext.getBean("loanRepaymentCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isLoanRecoveryPayment()) {
                handler = this.applicationContext.getBean("loanRecoveryPaymentCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isLoanRepaymentAdjustment()) {
                handler = this.applicationContext.getBean("loanRepaymentAdjustmentCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isWaiveInterestPortionOnLoan()) {
                handler = this.applicationContext.getBean("waiveInterestPortionOnLoanCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isLoanWriteOff()) {
                handler = this.applicationContext.getBean("writeOffLoanCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isCloseLoanAsObligationsMet()) {
                handler = this.applicationContext.getBean("closeLoanCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isCloseLoanAsRescheduled()) {
                handler = this.applicationContext.getBean("closeLoanAsRescheduledCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUpdateLoanOfficer()) {
                handler = this.applicationContext.getBean("updateLoanOfficerCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isRemoveLoanOfficer()) {
                handler = this.applicationContext.getBean("removeLoanOfficerCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isBulkUpdateLoanOfficer()) {
                handler = this.applicationContext.getBean("bulkUpdateLoanOfficerCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isCreate()) {
                handler = this.applicationContext.getBean("loanApplicationSubmittalCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUpdate()) {
                handler = this.applicationContext.getBean("loanApplicationModificationCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isDelete()) {
                handler = this.applicationContext.getBean("loanApplicationDeletionCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUndoLoanWriteOff()) {
                handler = this.applicationContext.getBean("undoWriteOffLoanCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isGuaranteeRecovery()) {
                handler = this.applicationContext.getBean("recoverFromGuarantorCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isLoanRefundByCash()) {
                handler = this.applicationContext.getBean("loanRefundByCashCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUndoLoanRefund()) {
                handler = this.applicationContext.getBean("loanRefundAdjustmentCommandHandler", NewCommandSourceHandler.class);
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }
        } else if (wrapper.isLoanChargeResource()) {
            if (wrapper.isAddLoanCharge()) {
                handler = this.applicationContext.getBean("addLoanChargeCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isDeleteLoanCharge()) {
                handler = this.applicationContext.getBean("deleteLoanChargeCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUpdateLoanCharge()) {
                handler = this.applicationContext.getBean("updateLoanChargeCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isWaiveLoanCharge()) {
                handler = this.applicationContext.getBean("waiveLoanChargeCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isPayLoanCharge()) {
                handler = this.applicationContext.getBean("payLoanChargeCommandHandler", NewCommandSourceHandler.class);
            }
        } else if (wrapper.isLoanDisburseDetailResource()) {
            if (wrapper.isUpdateDisbursementDate()) {
                handler = this.applicationContext.getBean("updateLoanDisbuseDateCommandHandler", NewCommandSourceHandler.class);
            }
            if (wrapper.addAndDeleteDisbursementDetails()) {
                handler = this.applicationContext.getBean("addAndDeleteLoanDisburseDetailsCommandHandler", NewCommandSourceHandler.class);
            }
        } else if (wrapper.isGLAccountResource()) {
            if (wrapper.isCreate()) {
                handler = this.applicationContext.getBean("createGLAccountCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUpdate()) {
                handler = this.applicationContext.getBean("updateGLAccountCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isDelete()) {
                handler = this.applicationContext.getBean("deleteGLAccountCommandHandler", NewCommandSourceHandler.class);
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }
        } else if (wrapper.isGLClosureResource()) {
            if (wrapper.isCreate()) {
                handler = this.applicationContext.getBean("createGLClosureCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUpdate()) {
                handler = this.applicationContext.getBean("updateGLClosureCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isDelete()) {
                handler = this.applicationContext.getBean("deleteGLClosureCommandHandler", NewCommandSourceHandler.class);
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }
        } else if (wrapper.isJournalEntryResource()) {
            if (wrapper.isCreate()) {
                handler = this.applicationContext.getBean("createJournalEntryCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isRevertJournalEntry()) {
                handler = this.applicationContext.getBean("reverseJournalEntryCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUpdateRunningbalance()) {
                handler = this.applicationContext.getBean("updateRunningBalanceCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isDefineOpeningalance()) {
                handler = this.applicationContext.getBean("defineOpeningBalanceCommandHandler", NewCommandSourceHandler.class);
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }
        } else if (wrapper.isPeriodicAccrualResource()) {
            if (wrapper.isExecute()) {
                handler = this.applicationContext.getBean("executePeriodicAccrualCommandHandler", NewCommandSourceHandler.class);
            }
        } else if (wrapper.isSavingsProductResource()) {
            if (wrapper.isCreate()) {
                handler = this.applicationContext.getBean("createSavingsProductCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUpdate()) {
                handler = this.applicationContext.getBean("updateSavingsProductCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isDelete()) {
                handler = this.applicationContext.getBean("deleteSavingsProductCommandHandler", NewCommandSourceHandler.class);
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }
        } else if (wrapper.isFixedDepositProductResource()) {
            if (wrapper.isCreate()) {
                handler = this.applicationContext.getBean("createFixedDepositProductCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUpdate()) {
                handler = this.applicationContext.getBean("updateFixedDepositProductCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isDelete()) {
                handler = this.applicationContext.getBean("deleteFixedDepositProductCommandHandler", NewCommandSourceHandler.class);
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }
        } else if (wrapper.isRecurringDepositProductResource()) {
            if (wrapper.isCreate()) {
                handler = this.applicationContext.getBean("createRecurringDepositProductCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUpdate()) {
                handler = this.applicationContext.getBean("updateRecurringDepositProductCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isDelete()) {
                handler = this.applicationContext.getBean("deleteRecurringDepositProductCommandHandler", NewCommandSourceHandler.class);
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }
        } else if (wrapper.isSavingsAccountResource()) {
            if (wrapper.isCreate()) {
                handler = this.applicationContext
                        .getBean("savingsAccountApplicationSubmittalCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUpdate()) {
                handler = this.applicationContext.getBean("savingsAccountApplicationModificationCommandHandler",
                        NewCommandSourceHandler.class);
            } else if (wrapper.isDelete()) {
                handler = this.applicationContext.getBean("savingsAccountApplicationDeletionCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isRejectionOfSavingsAccountApplication()) {
                handler = this.applicationContext.getBean("savingsAccountApplicationRejectedCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isWithdrawFromSavingsAccountApplicationByApplicant()) {
                handler = this.applicationContext.getBean("savingsAccountApplicationWithdrawnByApplicantCommandHandler",
                        NewCommandSourceHandler.class);
            } else if (wrapper.isApprovalOfSavingsAccountApplication()) {
                handler = this.applicationContext.getBean("savingsAccountApplicationApprovalCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUndoApprovalOfSavingsAccountApplication()) {
                handler = this.applicationContext.getBean("savingsAccountApplicationApprovalUndoCommandHandler",
                        NewCommandSourceHandler.class);
            } else if (wrapper.isSavingsAccountDeposit()) {
                handler = this.applicationContext.getBean("depositSavingsAccountCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isSavingsAccountWithdrawal()) {
                handler = this.applicationContext.getBean("withdrawSavingsAccountCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isSavingsAccountActivation()) {
                handler = this.applicationContext.getBean("activateSavingsAccountCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isSavingsAccountInterestCalculation()) {
                handler = this.applicationContext.getBean("calculateInterestSavingsAccountCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isSavingsAccountInterestPosting()) {
                handler = this.applicationContext.getBean("postInterestSavingsAccountCommandHandler", NewCommandSourceHandler.class);
            } /*
               * else if (wrapper.isSavingsAccountApplyAnnualFee()) { handler =
               * this.applicationContext.getBean(
               * "applyAnnualFeeSavingsAccountCommandHandler",
               * NewCommandSourceHandler.class); }
               */else if (wrapper.isSavingsAccountUndoTransaction()) {
                handler = this.applicationContext.getBean("undoTransactionSavingsAccountCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isAdjustSavingsAccountTransaction()) {
                handler = this.applicationContext.getBean("savingsTransactionAdjustmentCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isSavingsAccountClose()) {
                handler = this.applicationContext.getBean("closeSavingsAccountCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUpdateSavingsOfficer()) {
                handler = this.applicationContext.getBean("updateSavingsOfficerCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isRemoveSavingsOfficer()) {
                handler = this.applicationContext.getBean("removeSavingsOfficerCommandHandler", NewCommandSourceHandler.class);
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }
        } else if (wrapper.isSavingsAccountChargeResource()) {
            if (wrapper.isAddSavingsAccountCharge()) {
                handler = this.applicationContext.getBean("addSavingsAccountChargeCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isDeleteSavingsAccountCharge()) {
                handler = this.applicationContext.getBean("deleteSavingsAccountChargeCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUpdateSavingsAccountCharge()) {
                handler = this.applicationContext.getBean("updateSavingsAccountChargeCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isWaiveSavingsAccountCharge()) {
                handler = this.applicationContext.getBean("waiveSavingsAccountChargeCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isPaySavingsAccountCharge()) {
                handler = this.applicationContext.getBean("paySavingsAccountChargeCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isInactivateSavingsAccountCharge()) {
                handler = this.applicationContext.getBean("inactivateSavingsAccountChargeCommandHandler", NewCommandSourceHandler.class);
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }
        } else if (wrapper.isFixedDepositAccountResource()) {
            if (wrapper.isCreate()) {
                handler = this.applicationContext.getBean("fixedDepositAccountApplicationSubmittalCommandHandler",
                        NewCommandSourceHandler.class);
            } else if (wrapper.isUpdate()) {
                handler = this.applicationContext.getBean("fixedDepositAccountApplicationModificationCommandHandler",
                        NewCommandSourceHandler.class);
            } else if (wrapper.isDelete()) {
                handler = this.applicationContext.getBean("fixedDepositAccountApplicationDeletionCommandHandler",
                        NewCommandSourceHandler.class);
            } else if (wrapper.isRejectionOfFixedDepositAccountApplication()) {
                handler = this.applicationContext.getBean("fixedDepositAccountApplicationRejectedCommandHandler",
                        NewCommandSourceHandler.class);
            } else if (wrapper.isWithdrawFixedDepositAccountApplicationByApplicant()) {
                handler = this.applicationContext.getBean("fixedDepositAccountApplicationWithdrawnByApplicantCommandHandler",
                        NewCommandSourceHandler.class);
            } else if (wrapper.isApprovalOfFixedDepositAccountApplication()) {
                handler = this.applicationContext.getBean("fixedDepositAccountApplicationApprovalCommandHandler",
                        NewCommandSourceHandler.class);
            } else if (wrapper.isUndoApprovalOfFixedDepositAccountApplication()) {
                handler = this.applicationContext.getBean("fixedDepositAccountApplicationApprovalUndoCommandHandler",
                        NewCommandSourceHandler.class);
            } else if (wrapper.isDeposit()) {
                handler = this.applicationContext.getBean("fixedDepositAccountDepositCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isWithdrawal()) {
                handler = this.applicationContext.getBean("withdrawalFixedDepositAccountCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isActivation()) {
                handler = this.applicationContext.getBean("activateFixedDepositAccountCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isInterestCalculation()) {
                handler = this.applicationContext.getBean("calculateInterestFixedDepositAccountCommandHandler",
                        NewCommandSourceHandler.class);
            } else if (wrapper.isInterestPosting()) {
                handler = this.applicationContext.getBean("postInterestFixedDepositAccountCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUndoTransaction()) {
                handler = this.applicationContext
                        .getBean("undoTransactionFixedDepositAccountCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isAdjustTransaction()) {
                handler = this.applicationContext.getBean("fixedDepositTransactionAdjustmentCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isDepositAccountClose()) {
                handler = this.applicationContext.getBean("closeFixedDepositAccountCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isDepositAccountPrematureClose()) {
                handler = this.applicationContext.getBean("prematureCloseFixedDepositAccountCommandHandler", NewCommandSourceHandler.class);
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }
        } else if (wrapper.isRecurringDepositAccountResource()) {
            if (wrapper.isCreate()) {
                handler = this.applicationContext.getBean("recurringDepositAccountApplicationSubmittalCommandHandler",
                        NewCommandSourceHandler.class);
            } else if (wrapper.isUpdate()) {
                handler = this.applicationContext.getBean("recurringDepositAccountApplicationModificationCommandHandler",
                        NewCommandSourceHandler.class);
            } else if (wrapper.isDelete()) {
                handler = this.applicationContext.getBean("recurringDepositAccountApplicationDeletionCommandHandler",
                        NewCommandSourceHandler.class);
            } else if (wrapper.isRejectionOfRecurringDepositAccountApplication()) {
                handler = this.applicationContext.getBean("recurringDepositAccountApplicationRejectedCommandHandler",
                        NewCommandSourceHandler.class);
            } else if (wrapper.isWithdrawRecurringDepositAccountApplicationByApplicant()) {
                handler = this.applicationContext.getBean("recurringDepositAccountApplicationWithdrawnByApplicantCommandHandler",
                        NewCommandSourceHandler.class);
            } else if (wrapper.isApprovalOfRecurringDepositAccountApplication()) {
                handler = this.applicationContext.getBean("recurringDepositAccountApplicationApprovalCommandHandler",
                        NewCommandSourceHandler.class);
            } else if (wrapper.isUndoApprovalOfRecurringDepositAccountApplication()) {
                handler = this.applicationContext.getBean("recurringDepositAccountApplicationApprovalUndoCommandHandler",
                        NewCommandSourceHandler.class);
            } else if (wrapper.isDepositAmountUpdateForRecurringDepositAccount()) {
                handler = this.applicationContext.getBean("recurringDepositAccountUpdateDepositAmountCommandHandler",
                        NewCommandSourceHandler.class);
            } else if (wrapper.isDeposit()) {
                handler = this.applicationContext.getBean("recurringDepositAccountDepositCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isWithdrawal()) {
                handler = this.applicationContext.getBean("withdrawalRecurringDepositAccountCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isActivation()) {
                handler = this.applicationContext.getBean("activateRecurringDepositAccountCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isInterestCalculation()) {
                handler = this.applicationContext.getBean("calculateInterestRecurringDepositAccountCommandHandler",
                        NewCommandSourceHandler.class);
            } else if (wrapper.isInterestPosting()) {
                handler = this.applicationContext.getBean("postInterestRecurringDepositAccountCommandHandler",
                        NewCommandSourceHandler.class);
            } else if (wrapper.isUndoTransaction()) {
                handler = this.applicationContext.getBean("undoTransactionRecurringDepositAccountCommandHandler",
                        NewCommandSourceHandler.class);
            } else if (wrapper.isAdjustTransaction()) {
                handler = this.applicationContext.getBean("recurringDepositTransactionAdjustmentCommandHandler",
                        NewCommandSourceHandler.class);
            } else if (wrapper.isDepositAccountClose()) {
                handler = this.applicationContext.getBean("closeRecurringDepositAccountCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isDepositAccountPrematureClose()) {
                handler = this.applicationContext.getBean("prematureCloseRecurringDepositAccountCommandHandler",
                        NewCommandSourceHandler.class);
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }
        } else if (wrapper.isInterestRateChartResource()) {
            if (wrapper.isCreate()) {
                handler = this.applicationContext.getBean("createInterestRateChartCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUpdate()) {
                handler = this.applicationContext.getBean("updateInterestRateChartCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isDelete()) {
                handler = this.applicationContext.getBean("deleteInterestRateChartCommandHandler", NewCommandSourceHandler.class);
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }
        } else if (wrapper.isInterestRateChartSlabResource()) {
            if (wrapper.isCreate()) {
                handler = this.applicationContext.getBean("createInterestRateChartSlabCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUpdate()) {
                handler = this.applicationContext.getBean("updateInterestRateChartSlabCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isDelete()) {
                handler = this.applicationContext.getBean("deleteInterestRateChartSlabCommandHandler", NewCommandSourceHandler.class);
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }
        } else if (wrapper.isCalendarResource()) {
            if (wrapper.isCreate()) {
                handler = this.applicationContext.getBean("createCalendarCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUpdate()) {
                handler = this.applicationContext.getBean("updateCalendarCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isDelete()) {
                handler = this.applicationContext.getBean("deleteCalendarCommandHandler", NewCommandSourceHandler.class);
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }
        } else if (wrapper.isGroupResource()) {
            if (wrapper.isCreate()) {
                handler = this.applicationContext.getBean("createGroupCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUpdate()) {
                handler = this.applicationContext.getBean("updateGroupCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUnassignStaff()) {
                handler = this.applicationContext.getBean("unassignGroupStaffCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isDelete()) {
                handler = this.applicationContext.getBean("deleteGroupCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isGroupActivation()) {
                handler = this.applicationContext.getBean("activateGroupCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isAssociateClients()) {
                handler = this.applicationContext.getBean("associateClientsToGroupCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isDisassociateClients()) {
                handler = this.applicationContext.getBean("disassociateClientsFromGroupCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isSaveGroupCollectionSheet()) {
                handler = this.applicationContext.getBean("saveGroupCollectionSheetCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isAssignGroupRole()) {
                handler = this.applicationContext.getBean("assignRoleCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUnAssignGroupRole()) {
                handler = this.applicationContext.getBean("unassignRoleCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUpdateGroupRole()) {
                handler = this.applicationContext.getBean("updateGroupRoleCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isAssignStaff()) {
                handler = this.applicationContext.getBean("assignGroupStaffCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isTransferClientsBetweenGroups()) {
                handler = this.applicationContext.getBean("transferClientsBetweenGroupsCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isGroupClose()) {
                handler = this.applicationContext.getBean("closeGroupCommandHandler", NewCommandSourceHandler.class);
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }
        } else if (wrapper.isCenterResource()) {
            if (wrapper.isCreate()) {
                handler = this.applicationContext.getBean("createCenterCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUpdate()) {
                handler = this.applicationContext.getBean("updateCenterCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isDelete()) {
                handler = this.applicationContext.getBean("deleteCenterCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isCenterActivation()) {
                handler = this.applicationContext.getBean("activateCenterCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isSaveCenterCollectionSheet()) {
                handler = this.applicationContext.getBean("saveCenterCollectionSheetCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isCenterClose()) {
                handler = this.applicationContext.getBean("closeCenterCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isCenterDisassociateGroups()) {
                handler = this.applicationContext.getBean("disassociateGroupsFromCenterCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isCenterAssociateGroups()) {
                handler = this.applicationContext.getBean("associateGroupsToCenterCommandHandler", NewCommandSourceHandler.class);
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }
        } else if (wrapper.isCollectionSheetResource()) {
            if (wrapper.isUpdate()) {
                handler = this.applicationContext.getBean("updateCollectionSheetCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isSaveIndividualCollectionSheet()) {
                handler = this.applicationContext.getBean("saveIndividualCollectionSheetCommandHandler", NewCommandSourceHandler.class);
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }
        } else if (wrapper.isReportResource()) {
            if (wrapper.isCreate()) {
                handler = this.applicationContext.getBean("createReportCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUpdate()) {
                handler = this.applicationContext.getBean("updateReportCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isDelete()) {
                handler = this.applicationContext.getBean("deleteReportCommandHandler", NewCommandSourceHandler.class);
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }
        } else if (wrapper.isAccountingRuleResource()) {
            if (wrapper.isCreate()) {
                handler = this.applicationContext.getBean("createAccountingRuleCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUpdate()) {
                handler = this.applicationContext.getBean("updateAccountingRuleCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isDelete()) {
                handler = this.applicationContext.getBean("deleteAccountingRuleCommandHandler", NewCommandSourceHandler.class);
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }
        } else if (wrapper.isXBRLMappingResource()) {
            if (wrapper.isUpdate()) {
                handler = this.applicationContext.getBean("updateTaxonomyMappingCommandHandler", NewCommandSourceHandler.class);
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }
        } else if (wrapper.isHolidayResource()) {
            if (wrapper.isCreate()) {
                handler = this.applicationContext.getBean("createHolidayCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isHolidayActivation()) {
                handler = this.applicationContext.getBean("activateHolidayCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUpdate()) {
                handler = this.applicationContext.getBean("updateHolidayCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isDelete()) {
                handler = this.applicationContext.getBean("deleteHolidayCommandHandler", NewCommandSourceHandler.class);
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }
        } else if (wrapper.isProductMixResource()) {
            if (wrapper.isCreate()) {
                handler = this.applicationContext.getBean("createProductMixCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUpdateOperation()) {
                handler = this.applicationContext.getBean("updateProductMixCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isDeleteOperation()) {
                handler = this.applicationContext.getBean("deleteProductMixCommandHandler", NewCommandSourceHandler.class);
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }
        } else if (wrapper.isSchedulerResource()) {
            if (wrapper.isUpdate()) {
                handler = this.applicationContext.getBean("updateJobDetailCommandhandler", NewCommandSourceHandler.class);
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }
        } else if (wrapper.isCacheResource()) {
            if (wrapper.isUpdateOperation()) {
                handler = this.applicationContext.getBean("updateCacheCommandHandler", NewCommandSourceHandler.class);
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }
        } else if (wrapper.isMeetingResource()) {
            if (wrapper.isCreate()) {
                handler = this.applicationContext.getBean("createMeetingCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUpdate()) {
                handler = this.applicationContext.getBean("updateMeetingCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isDelete()) {
                handler = this.applicationContext.getBean("deleteMeetingCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isSaveOrUpdateAttendance()) {
                handler = this.applicationContext.getBean("updateMeetingAttendanceCommandHandler", NewCommandSourceHandler.class);
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }
        } else if (wrapper.isTemplateRessource()) {

            if (wrapper.isCreate()) {
                handler = this.applicationContext.getBean("createTemplateCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUpdate()) {
                handler = this.applicationContext.getBean("updateTemplateCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isDelete()) {
                handler = this.applicationContext.getBean("deleteTemplateCommandHandler", NewCommandSourceHandler.class);
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }

        } else if (wrapper.isTemplateRessource()) {
            if (wrapper.isCreate()) {
                handler = this.applicationContext.getBean("createTemplateCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUpdate()) {
                handler = this.applicationContext.getBean("updateTemplateCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isDelete()) {
                handler = this.applicationContext.getBean("deleteTemplateCommandHandler", NewCommandSourceHandler.class);
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }

        } else if (wrapper.isStandingInstruction()) {
            if (wrapper.isCreate()) {
                handler = this.applicationContext.getBean("createStandingInstructionCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUpdate()) {
                handler = this.applicationContext.getBean("updateStandingInstructionCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isDelete()) {
                handler = this.applicationContext.getBean("deleteStandingInstructionCommandHandler", NewCommandSourceHandler.class);
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }

        } else if (wrapper.isFinancialActivityAccountMapping()) {
            if (wrapper.isCreate()) {
                handler = this.applicationContext.getBean("createFinancialActivityAccountHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUpdate()) {
                handler = this.applicationContext.getBean("updateFinancialActivityAccountCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isDelete()) {
                handler = this.applicationContext.getBean("deleteFinancialActivityAccountCommandHandler", NewCommandSourceHandler.class);
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }
        } else if (wrapper.isLikelihoodResource()) {
            if (wrapper.isUpdateLikelihood()) {
                handler = this.applicationContext.getBean("updateLikelihoodCommandHandler", NewCommandSourceHandler.class);
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }
        } else if (wrapper.isSurveyResource()) {
            if (wrapper.isRegisterSurvey()) {
                handler = this.applicationContext.getBean("registerSurveyCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isFullFilSurvey()) {
                handler = this.applicationContext.getBean("fullFilSurveyCommandHandler", NewCommandSourceHandler.class);
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }
        } else if (wrapper.isLoanRescheduleResource()) {
            if (wrapper.isCreate()) {
                handler = this.applicationContext.getBean("createLoanRescheduleRequestCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isApprove()) {
                handler = this.applicationContext.getBean("approveLoanRescheduleRequestCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isReject()) {
                handler = this.applicationContext.getBean("rejectLoanRescheduleRequestCommandHandler", NewCommandSourceHandler.class);
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }
        } else if (wrapper.isAccountNumberFormatResource()) {
            if (wrapper.isCreate()) {
                handler = this.applicationContext.getBean("createAccountNumberFormatCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUpdate()) {
                handler = this.applicationContext.getBean("updateAccountNumberFormatCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isDelete()) {
                handler = this.applicationContext.getBean("deleteAccountNumberFormatCommandHandler", NewCommandSourceHandler.class);
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }
        } else if (wrapper.isEntityMappingResource()) {
            if (wrapper.isCreate()) {
                handler = this.applicationContext.getBean("createEntityToEntityMappingCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUpdate()) {
                handler = this.applicationContext.getBean("updateEntityToEntityMappingCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isDelete()) {
                handler = this.applicationContext.getBean("deleteEntityToEntityMappingCommandHandler", NewCommandSourceHandler.class);
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }
        } else if (wrapper.isWorkingDaysResource()) {
            if (wrapper.isUpdate()) {
                handler = this.applicationContext.getBean("updateWorkingDaysCommandHandler", NewCommandSourceHandler.class);
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }
        } else if (wrapper.isPasswordPreferencesResource()) {
            if (wrapper.isUpdate()) {
                handler = this.applicationContext.getBean("updatePasswordPreferencesCommandHandler", NewCommandSourceHandler.class);

            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }

        } else if (wrapper.isPaymentTypeResource()) {
            if (wrapper.isCreate()) {
                handler = this.applicationContext.getBean("createPaymentTypeCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUpdate()) {
                handler = this.applicationContext.getBean("updatePaymentTypeCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isDelete()) {
                handler = this.applicationContext.getBean("deletePaymentTypeCommandHandler", NewCommandSourceHandler.class);
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }

        } else {

            throw new UnsupportedCommandException(wrapper.commandName());
        }

        return handler;
    }

    @Override
    public boolean validateCommand(final CommandWrapper commandWrapper, final AppUser user) {
        boolean rollbackTransaction = this.configurationDomainService.isMakerCheckerEnabledForTask(commandWrapper.taskPermissionName());
        user.validateHasPermissionTo(commandWrapper.getTaskPermissionName());
        return rollbackTransaction;
    }

    private void publishEvent(final String entityName, final String actionName, final CommandProcessingResult result) {

        final String authToken = ThreadLocalContextUtil.getAuthToken();
        final String tenantIdentifier = ThreadLocalContextUtil.getTenant().getTenantIdentifier();
        final AppUser appUser = this.context.authenticatedUser();

        final HookEventSource hookEventSource = new HookEventSource(entityName, actionName);

        final String serializedResult = this.toApiResultJsonSerializer.serialize(result);

        final HookEvent applicationEvent = new HookEvent(hookEventSource, serializedResult, tenantIdentifier, appUser, authToken);

        applicationContext.publishEvent(applicationEvent);
    }
}

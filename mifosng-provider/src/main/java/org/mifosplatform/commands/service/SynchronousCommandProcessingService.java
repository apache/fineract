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
    private CommandSourceRepository commandSourceRepository;
    private final ConfigurationDomainService configurationDomainService;

    @Autowired
    public SynchronousCommandProcessingService(final PlatformSecurityContext context, final ApplicationContext applicationContext,
            final ToApiJsonSerializer<Map<String, Object>> toApiJsonSerializer, final CommandSourceRepository commandSourceRepository,
            final ConfigurationDomainService configurationDomainService) {
        this.context = context;
        this.context = context;
        this.applicationContext = applicationContext;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.commandSourceRepository = commandSourceRepository;
        this.commandSourceRepository = commandSourceRepository;
        this.configurationDomainService = configurationDomainService;
    }

    @Transactional
    @Override
    public CommandProcessingResult processAndLogCommand(final CommandWrapper wrapper, final JsonCommand command,
            final boolean isApprovedByChecker) {

        final boolean rollbackTransaction = this.configurationDomainService.isMakerCheckerEnabledForTask(wrapper.taskPermissionName())
                && !isApprovedByChecker;

        final NewCommandSourceHandler handler = findCommandHandler(wrapper);
        final CommandProcessingResult result = handler.processCommand(command);

        final AppUser maker = this.context.authenticatedUser();

        CommandSource commandSourceResult = null;
        if (command.commandId() != null) {
            commandSourceResult = this.commandSourceRepository.findOne(command.commandId());
            commandSourceResult.markAsChecked(maker, DateTime.now());
        } else {
            commandSourceResult = CommandSource.fullEntryFrom(wrapper, command, maker);
        }
        commandSourceResult.updateResourceId(result.resourceId());
        commandSourceResult.updateForAudit(result.getOfficeId(), result.getGroupId(), result.getClientId(), result.getLoanId(),
                result.getSavingsId(), result.getProductId());

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

        if (rollbackTransaction) { throw new RollbackTransactionAsCommandIsNotApprovedByCheckerException(commandSourceResult); }

        return result;
    }

    @Transactional
    @Override
    public CommandProcessingResult logCommand(final CommandSource commandSourceResult) {

        commandSourceResult.markAsAwaitingApproval();
        this.commandSourceRepository.save(commandSourceResult);

        return new CommandProcessingResultBuilder().withCommandId(commandSourceResult.getId())
                .withEntityId(commandSourceResult.getResourceId()).build();
    }

    private NewCommandSourceHandler findCommandHandler(final CommandWrapper wrapper) {
        NewCommandSourceHandler handler = null;

        if (wrapper.isAccountTransferResource()) {
            if (wrapper.isCreate()) {
                handler = this.applicationContext.getBean("createAccountTransferCommandHandler", NewCommandSourceHandler.class);
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }
        }
        else if (wrapper.isConfigurationResource()) {
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
            } else if (wrapper.isWithdrawClientTransfer()) {
                handler = this.applicationContext.getBean("withdrawClientTransferCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isAcceptClientTransfer()) {
                handler = this.applicationContext.getBean("acceptClientTransferCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isRejectClientTransfer()) {
                handler = this.applicationContext.getBean("rejectClientTransferCommandHandler", NewCommandSourceHandler.class);
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
            } else if (wrapper.isUndoDisbursementOfLoan()) {
                handler = this.applicationContext.getBean("undoDisbursalLoanCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isLoanRepayment()) {
                handler = this.applicationContext.getBean("loanRepaymentCommandHandler", NewCommandSourceHandler.class);
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
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
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
            } else if (wrapper.isSavingsAccountApplyAnnualFee()) {
                handler = this.applicationContext.getBean("applyAnnualFeeSavingsAccountCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isSavingsAccountUndoTransaction()) {
                handler = this.applicationContext.getBean("undoTransactionSavingsAccountCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isAdjustSavingsAccountTransaction()) {
                handler = this.applicationContext.getBean("savingsTransactionAdjustmentCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isSavingsAccountClose()) {
                handler = this.applicationContext.getBean("closeSavingsAccountCommandHandler", NewCommandSourceHandler.class);
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
            }  else if (wrapper.isAssignStaff()) {
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
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }
        } else if (wrapper.isCollectionSheetResource()) {

            if (wrapper.isUpdate()) {
                handler = this.applicationContext.getBean("updateCollectionSheetCommandHandler", NewCommandSourceHandler.class);
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

        } else if (wrapper.isHolidayResource()) {

            if (wrapper.isCreate()) {
                handler = this.applicationContext.getBean("createHolidayCommandHandler", NewCommandSourceHandler.class);
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
        } else {
            throw new UnsupportedCommandException(wrapper.commandName());
        }

        return handler;
    }
}
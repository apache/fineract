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

        final AppUser maker = context.authenticatedUser();

        CommandSource commandSourceResult = null;
        if (command.commandId() != null) {
            commandSourceResult = this.commandSourceRepository.findOne(command.commandId());
            commandSourceResult.markAsChecked(maker, DateTime.now());
        } else {
            commandSourceResult = CommandSource.fullEntryFrom(wrapper, command, maker);
        }
        commandSourceResult.updateResourceId(result.resourceId());
        commandSourceResult.updateForAudit(result.getOfficeId(), result.getGroupId(), result.getClientId(), result.getLoanId());

        String changesOnlyJson = null;
        if (result.hasChanges()) {
            changesOnlyJson = this.toApiJsonSerializer.serializeResult(result.getChanges());
            commandSourceResult.updateJsonTo(changesOnlyJson);
        }

        if (!result.hasChanges() && wrapper.isUpdate()) {
            commandSourceResult.updateJsonTo(null);
        }

        if (commandSourceResult.hasJson()) {
            commandSourceRepository.save(commandSourceResult);
        }

        if (rollbackTransaction) { throw new RollbackTransactionAsCommandIsNotApprovedByCheckerException(changesOnlyJson); }

        return result;
    }

    @Transactional
    @Override
    public CommandProcessingResult logCommand(final CommandWrapper wrapper, final JsonCommand command) {

        final AppUser maker = context.authenticatedUser();

        final CommandSource commandSourceResult = CommandSource.fullEntryFrom(wrapper, command, maker);
        if (commandSourceResult.hasJson()) {
            commandSourceResult.markAsAwaitingApproval();
            commandSourceRepository.save(commandSourceResult);
        }

        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(commandSourceResult.resourceId())
                .build();
    }

    private NewCommandSourceHandler findCommandHandler(final CommandWrapper wrapper) {
        NewCommandSourceHandler handler = null;

        if (wrapper.isConfigurationResource()) {
            handler = applicationContext.getBean("updateGlobalConfigurationCommandHandler", NewCommandSourceHandler.class);
        } else if (wrapper.isDatatableResource()) {
            if (wrapper.isCreate()) {
                handler = applicationContext.getBean("createDatatableEntryCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUpdateMultiple()) {
                handler = applicationContext.getBean("updateOneToManyDatatableEntryCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUpdateOneToOne()) {
                handler = applicationContext.getBean("updateOneToOneDatatableEntryCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isDeleteMultiple()) {
                handler = applicationContext.getBean("deleteOneToManyDatatableEntryCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isDeleteOneToOne()) {
                handler = applicationContext.getBean("deleteOneToOneDatatableEntryCommandHandler", NewCommandSourceHandler.class);
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }
        } else if (wrapper.isClientNoteResource()) {
            if (wrapper.isCreate()) {
                handler = applicationContext.getBean("createClientNoteCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUpdate()) {
                handler = applicationContext.getBean("updateClientNoteCommandHandler", NewCommandSourceHandler.class);
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }
        } else if (wrapper.isClientIdentifierResource()) {

            if (wrapper.isCreate()) {
                handler = applicationContext.getBean("createClientIdentifierCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUpdate()) {
                handler = applicationContext.getBean("updateClientIdentifierCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isDelete()) {
                handler = applicationContext.getBean("deleteClientIdentifierCommandHandler", NewCommandSourceHandler.class);
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }
        } else if (wrapper.isClientResource() && !wrapper.isClientNoteResource() && !wrapper.isClientIdentifierResource()) {
            if (wrapper.isCreate()) {
                handler = applicationContext.getBean("createClientCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUpdate()) {
                handler = applicationContext.getBean("updateClientCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isDelete()) {
                handler = applicationContext.getBean("deleteClientCommandHandler", NewCommandSourceHandler.class);
            }
            // end of client
        } else if (wrapper.isUpdateRolePermissions()) {
            handler = applicationContext.getBean("updateRolePermissionsCommandHandler", NewCommandSourceHandler.class);
        } else if (wrapper.isPermissionResource()) {
            handler = applicationContext.getBean("updateMakerCheckerPermissionsCommandHandler", NewCommandSourceHandler.class);
        } else if (wrapper.isRoleResource()) {

            if (wrapper.isCreate()) {
                handler = applicationContext.getBean("createRoleCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUpdate()) {
                handler = applicationContext.getBean("updateRoleCommandHandler", NewCommandSourceHandler.class);
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }

        } else if (wrapper.isUserResource()) {
            if (wrapper.isCreate()) {
                handler = applicationContext.getBean("createUserCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUpdate()) {
                handler = applicationContext.getBean("updateUserCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isDelete()) {
                handler = applicationContext.getBean("deleteUserCommandHandler", NewCommandSourceHandler.class);
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }
        } else if (wrapper.isStaffResource()) {
            if (wrapper.isCreate()) {
                handler = applicationContext.getBean("createStaffCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUpdate()) {
                handler = applicationContext.getBean("updateStaffCommandHandler", NewCommandSourceHandler.class);
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }
        } else if (wrapper.isGuarantorResource()) {
            if (wrapper.isCreate()) {
                handler = applicationContext.getBean("createGuarantorCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUpdate()) {
                handler = applicationContext.getBean("updateGuarantorCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isDelete()) {
                handler = applicationContext.getBean("deleteGuarantorCommandHandler", NewCommandSourceHandler.class);
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }
        } else if (wrapper.isCodeResource()) {
            if (wrapper.isCreate()) {
                handler = applicationContext.getBean("createCodeCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUpdate()) {
                handler = applicationContext.getBean("updateCodeCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isDelete()) {
                handler = applicationContext.getBean("deleteCodeCommandHandler", NewCommandSourceHandler.class);
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }
        } else if (wrapper.isCodeValueResource()) {
            if (wrapper.isCreate()) {
                handler = applicationContext.getBean("createCodeValueCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUpdate()) {
                handler = applicationContext.getBean("updateCodeValueCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isDelete()) {
                handler = applicationContext.getBean("deleteCodeValueCommandHandler", NewCommandSourceHandler.class);
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }
        } else if (wrapper.isCurrencyResource()) {
            handler = applicationContext.getBean("updateCurrencyCommandHandler", NewCommandSourceHandler.class);
        } else if (wrapper.isFundResource()) {
            if (wrapper.isCreate()) {
                handler = applicationContext.getBean("createFundCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUpdate()) {
                handler = applicationContext.getBean("updateFundCommandHandler", NewCommandSourceHandler.class);
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }
        } else if (wrapper.isOfficeResource()) {
            if (wrapper.isCreate()) {
                handler = applicationContext.getBean("createOfficeCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUpdate()) {
                handler = applicationContext.getBean("updateOfficeCommandHandler", NewCommandSourceHandler.class);
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }
        } else if (wrapper.isOfficeTransactionResource()) {
            if (wrapper.isCreate()) {
                handler = applicationContext.getBean("createOfficeTransactionCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isDelete()) {
                handler = applicationContext.getBean("deleteOfficeTransactionCommandHandler", NewCommandSourceHandler.class);
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }
        } else if (wrapper.isChargeDefinitionResource()) {
            if (wrapper.isCreate()) {
                handler = applicationContext.getBean("createChargeDefinitionCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUpdate()) {
                handler = applicationContext.getBean("updateChargeDefinitionCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isDelete()) {
                handler = applicationContext.getBean("deleteChargeDefinitionCommandHandler", NewCommandSourceHandler.class);
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }
        } else if (wrapper.isLoanProductResource()) {
            if (wrapper.isCreate()) {
                handler = applicationContext.getBean("createLoanProductCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUpdate()) {
                handler = applicationContext.getBean("updateLoanProductCommandHandler", NewCommandSourceHandler.class);
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }
        } else if (wrapper.isLoanResource()) {
            if (wrapper.isApproveLoanApplication()) {
                handler = applicationContext.getBean("loanApplicationApprovalCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUndoApprovalOfLoanApplication()) {
                handler = applicationContext.getBean("loanApplicationApprovalUndoCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isApplicantWithdrawalFromLoanApplication()) {
                handler = applicationContext.getBean("loanApplicationWithdrawnByApplicantCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isRejectionOfLoanApplication()) {
                handler = applicationContext.getBean("loanApplicationRejectedCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isDisbursementOfLoan()) {
                handler = applicationContext.getBean("disburseLoanCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUndoDisbursementOfLoan()) {
                handler = applicationContext.getBean("undoDisbursalLoanCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isLoanRepayment()) {
                handler = applicationContext.getBean("loanRepaymentCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isLoanRepaymentAdjustment()) {
                handler = applicationContext.getBean("loanRepaymentAdjustmentCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isWaiveInterestPortionOnLoan()) {
                handler = applicationContext.getBean("waiveInterestPortionOnLoanCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isLoanWriteOff()) {
                handler = applicationContext.getBean("writeOffLoanCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isCloseLoanAsObligationsMet()) {
                handler = applicationContext.getBean("closeLoanCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isCloseLoanAsRescheduled()) {
                handler = applicationContext.getBean("closeLoanAsRescheduledCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUpdateLoanOfficer()) {
                handler = applicationContext.getBean("updateLoanOfficerCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isRemoveLoanOfficer()) {
                handler = applicationContext.getBean("removeLoanOfficerCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isBulkUpdateLoanOfficer()) {
                handler = applicationContext.getBean("bulkUpdateLoanOfficerCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isCreate()) {
                handler = applicationContext.getBean("loanApplicationSubmittalCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUpdate()) {
                handler = applicationContext.getBean("loanApplicationModificationCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isDelete()) {
                handler = applicationContext.getBean("loanApplicationDeletionCommandHandler", NewCommandSourceHandler.class);
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }
        } else if (wrapper.isLoanChargeResource()) {
            if (wrapper.isAddLoanCharge()) {
                handler = applicationContext.getBean("addLoanChargeCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isDeleteLoanCharge()) {
                handler = applicationContext.getBean("deleteLoanChargeCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUpdateLoanCharge()) {
                handler = applicationContext.getBean("updateLoanChargeCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isWaiveLoanCharge()) {
                handler = applicationContext.getBean("waiveLoanChargeCommandHandler", NewCommandSourceHandler.class);
            }
        } else {
            throw new UnsupportedCommandException(wrapper.commandName());
        }

        return handler;
    }
}
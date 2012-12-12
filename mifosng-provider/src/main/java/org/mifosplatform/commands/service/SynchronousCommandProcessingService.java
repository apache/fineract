package org.mifosplatform.commands.service;

import java.util.Map;

import org.joda.time.DateTime;
import org.mifosplatform.commands.domain.CommandSource;
import org.mifosplatform.commands.domain.CommandSourceRepository;
import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.exception.UnsupportedCommandException;
import org.mifosplatform.commands.handler.NewCommandSourceHandler;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.EntityIdentifier;
import org.mifosplatform.infrastructure.core.serialization.ToApiJsonSerializer;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.monetary.service.ConfigurationDomainService;
import org.mifosplatform.portfolio.client.service.RollbackTransactionAsCommandIsNotApprovedByCheckerException;
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
    public EntityIdentifier processAndLogCommand(final CommandWrapper wrapper, final JsonCommand command, final boolean isApprovedByChecker) {

        final NewCommandSourceHandler handler = findCommandHandler(wrapper);
        final EntityIdentifier result = handler.processCommand(command);

        final AppUser maker = context.authenticatedUser();
        
        CommandSource commandSourceResult = null;
        if (command.commandId() != null) {
            commandSourceResult = this.commandSourceRepository.findOne(command.commandId());
            commandSourceResult.markAsChecked(maker, DateTime.now());
        } else {
            commandSourceResult = CommandSource.fullEntryFrom(wrapper, command, maker);
        }
        commandSourceResult.updateResourceId(result.resourceId());
        commandSourceResult.updateSubResourceId(result.subResourceId());
        
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

        if (this.configurationDomainService.isMakerCheckerEnabledForTask(wrapper.taskPermissionName()) && !isApprovedByChecker) { throw new RollbackTransactionAsCommandIsNotApprovedByCheckerException(changesOnlyJson); }

        return result;
    }

    @Transactional
    @Override
    public EntityIdentifier logCommand(final CommandWrapper wrapper, final JsonCommand command) {

        final AppUser maker = context.authenticatedUser();

        final CommandSource commandSourceResult = CommandSource.fullEntryFrom(wrapper, command, maker);
        if (commandSourceResult.hasJson()) {
            commandSourceRepository.save(commandSourceResult);
        }

        return EntityIdentifier.resourceResult(commandSourceResult.resourceId(), commandSourceResult.getId());
    }

    private NewCommandSourceHandler findCommandHandler(final CommandWrapper wrapper) {
        NewCommandSourceHandler handler = null;

        if (wrapper.isClientNoteResource()) {
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
        } else if (wrapper.isClientResource() && !wrapper.isNotesSubResource() && !wrapper.isIdentifiersSubResource()) {
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
        } else {
            throw new UnsupportedCommandException(wrapper.commandName());
        }

        return handler;
    }
}
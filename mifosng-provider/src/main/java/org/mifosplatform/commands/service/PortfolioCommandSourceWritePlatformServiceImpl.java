package org.mifosplatform.commands.service;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.commands.domain.CommandSource;
import org.mifosplatform.commands.domain.CommandSourceRepository;
import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.exception.RollbackTransactionAsCommandIsNotApprovedByCheckerException;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonElement;

@Service
public class PortfolioCommandSourceWritePlatformServiceImpl implements PortfolioCommandSourceWritePlatformService {

    private final PlatformSecurityContext context;
    private final CommandSourceRepository commandSourceRepository;
    private final FromJsonHelper fromApiJsonHelper;
    private final CommandProcessingService processAndLogCommandService;

    @Autowired
    public PortfolioCommandSourceWritePlatformServiceImpl(final PlatformSecurityContext context,
            final CommandSourceRepository commandSourceRepository, final FromJsonHelper fromApiJsonHelper,
            final CommandProcessingService processAndLogCommandService) {
        this.context = context;
        this.commandSourceRepository = commandSourceRepository;
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.processAndLogCommandService = processAndLogCommandService;
    }

    @Override
    public CommandProcessingResult logCommandSource(final CommandWrapper wrapper) {
        
        boolean isApprovedByChecker = false;
        // check if is update of own account details
        if (wrapper.isUpdateOfOwnUserDetails(context.authenticatedUser().getId())) {
            // then allow this operation to proceed.
            // maker checker doesnt mean anything here.
            isApprovedByChecker = true; // set to true in case permissions have been maker-checker enabled by accident.
        } else {
            // if not user changing their own details - check user has permission to perform specific task.
            context.authenticatedUser().validateHasPermissionTo(wrapper.getTaskPermissionName());
        }

        final String json = wrapper.getJson();
        CommandProcessingResult result = null;
        try {
            final JsonElement parsedCommand = this.fromApiJsonHelper.parse(json);
            final JsonCommand command = JsonCommand.from(json, parsedCommand, this.fromApiJsonHelper, wrapper.getEntityName(),
                    wrapper.getEntityId(), wrapper.getGroupId(), wrapper.getClientId(), wrapper.getLoanId(), wrapper.getApptableId(),
                    wrapper.getDatatableId(), wrapper.getCodeId());

            result = this.processAndLogCommandService.processAndLogCommand(wrapper, command, isApprovedByChecker);
        } catch (RollbackTransactionAsCommandIsNotApprovedByCheckerException e) {

            final String jsonToUse = StringUtils.defaultIfEmpty(e.getJsonOfChangesOnly(), json);

            final JsonElement parsedCommand = this.fromApiJsonHelper.parse(jsonToUse);
            final JsonCommand command = JsonCommand.from(jsonToUse, parsedCommand, this.fromApiJsonHelper, wrapper.getEntityName(),
                    wrapper.getEntityId(), wrapper.getGroupId(), wrapper.getClientId(), wrapper.getLoanId(), wrapper.getApptableId(),
                    wrapper.getDatatableId(), wrapper.getCodeId());

            result = this.processAndLogCommandService.logCommand(wrapper, command);
        }

        return result;
    }

    @Override
    public CommandProcessingResult approveEntry(final Long commandId) {

        final CommandSource commandSourceInput = this.commandSourceRepository.findOne(commandId);

        context.authenticatedUser().validateHasCheckerPermissionTo(commandSourceInput.getPermissionCode());

        final CommandWrapper wrapper = CommandWrapper.fromExistingCommand(commandId, commandSourceInput.getActionName(),
                commandSourceInput.getEntityName(), commandSourceInput.resourceId());

        final JsonElement parsedCommand = this.fromApiJsonHelper.parse(commandSourceInput.json());
        final JsonCommand command = JsonCommand.fromExistingCommand(commandId, commandSourceInput.json(), parsedCommand,
                this.fromApiJsonHelper, commandSourceInput.resourceId());

        final boolean makerCheckerApproval = true;
        return this.processAndLogCommandService.processAndLogCommand(wrapper, command, makerCheckerApproval);
    }

    @Transactional
    @Override
    public Long deleteEntry(final Long makerCheckerId) {

        context.authenticatedUser();

        this.commandSourceRepository.delete(makerCheckerId);

        return makerCheckerId;
    }
}
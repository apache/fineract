package org.mifosplatform.commands.service;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.commands.domain.CommandSource;
import org.mifosplatform.commands.domain.CommandSourceRepository;
import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.EntityIdentifier;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.client.service.RollbackTransactionAsCommandIsNotApprovedByCheckerException;
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
    public EntityIdentifier logCommandSource(final String actionName, final String entityName, final String apiOperation,
            final String resource, final Long resourceId, final String subResource, final Long subRescourceId, final String json) {

        final String taskPermissionName = actionName + "_" + entityName;
        context.authenticatedUser().validateHasPermissionTo(taskPermissionName);

        final CommandWrapper wrapper = CommandWrapper.wrap(actionName, entityName, apiOperation, resource, resourceId, subResource,
                subRescourceId);
        EntityIdentifier result = null;
        try {
            final JsonElement parsedCommand = this.fromApiJsonHelper.parse(json);
            final JsonCommand command = JsonCommand.from(json, parsedCommand, this.fromApiJsonHelper, entityName, resourceId, subRescourceId);
            final boolean isApprovedByChecker = false;
            result = this.processAndLogCommandService.processAndLogCommand(wrapper, command, isApprovedByChecker);
        } catch (RollbackTransactionAsCommandIsNotApprovedByCheckerException e) {

            final String jsonToUse = StringUtils.defaultIfEmpty(e.getJsonOfChangesOnly(), json);

            final JsonElement parsedCommand = this.fromApiJsonHelper.parse(jsonToUse);
            final JsonCommand command = JsonCommand.from(jsonToUse, parsedCommand, this.fromApiJsonHelper, entityName, resourceId, subRescourceId);

            result = this.processAndLogCommandService.logCommand(wrapper, command);
        }

        return result;
    }

    @Override
    public EntityIdentifier logCommandSource(final String actionName, final String entityName, final String apiOperation, final String resource,
            final Long resourceId, final String json) {

        final String taskPermissionName = actionName + "_" + entityName;
        context.authenticatedUser().validateHasPermissionTo(taskPermissionName);

        final CommandWrapper wrapper = CommandWrapper.wrap(actionName, entityName, apiOperation, resource, resourceId);
        
        EntityIdentifier result = null;
        try {
            final JsonElement parsedCommand = this.fromApiJsonHelper.parse(json);
            final JsonCommand command = JsonCommand.from(json, parsedCommand, this.fromApiJsonHelper, entityName, resourceId, null);

            final boolean isApprovedByChecker = false;
            result = this.processAndLogCommandService.processAndLogCommand(wrapper, command, isApprovedByChecker);
        } catch (RollbackTransactionAsCommandIsNotApprovedByCheckerException e) {

            final String jsonToUse = StringUtils.defaultIfEmpty(e.getJsonOfChangesOnly(), json);

            final JsonElement parsedCommand = this.fromApiJsonHelper.parse(jsonToUse);
            final JsonCommand command = JsonCommand.from(jsonToUse, parsedCommand, this.fromApiJsonHelper, entityName, resourceId, null);

            result = this.processAndLogCommandService.logCommand(wrapper, command);
        }

        return result;
    }

    @Override
    public EntityIdentifier approveEntry(final Long commandId) {

        final CommandSource commandSourceInput = this.commandSourceRepository.findOne(commandId);

        context.authenticatedUser().validateHasCheckerPermissionTo(commandSourceInput.getPermissionCode());

        final CommandWrapper wrapper = CommandWrapper.fromExistingCommand(commandId, commandSourceInput.getActionName(),
                commandSourceInput.getEntityName(), commandSourceInput.operation(), commandSourceInput.resourceName(),
                commandSourceInput.resourceId(), commandSourceInput.getSubResource(), commandSourceInput.getSubResourceId());

        final JsonElement parsedCommand = this.fromApiJsonHelper.parse(commandSourceInput.json());
        final JsonCommand command = JsonCommand.fromExistingCommand(commandId, commandSourceInput.json(), parsedCommand,
                this.fromApiJsonHelper, commandSourceInput.resourceId(), commandSourceInput.getSubResourceId());

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
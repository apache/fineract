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
package org.apache.fineract.commands.mvc.service;

import com.google.gson.JsonElement;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.commands.domain.CommandSource;
import org.apache.fineract.commands.domain.CommandSourceRepository;
import org.apache.fineract.commands.exception.CommandNotAwaitingApprovalException;
import org.apache.fineract.commands.exception.CommandNotFoundException;
import org.apache.fineract.commands.exception.UnsupportedCommandException;
import org.apache.fineract.commands.mvc.domain.CommandTypeWrapper;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.api.mvc.ProfileMvc;
import org.apache.fineract.infrastructure.core.api.mvc.TypeCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.jobs.service.SchedulerJobRunnerReadService;
import org.apache.fineract.infrastructure.security.service.mvc.PlatformSecurityContext;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@ProfileMvc
@Service("mvcPortfolioCommandSourceWritePlatformService")
@RequiredArgsConstructor
@Slf4j
public class PortfolioCommandSourceWritePlatformServiceImpl implements PortfolioCommandSourceWritePlatformService {

    private final PlatformSecurityContext context;
    private final CommandSourceRepository commandSourceRepository;
    private final FromJsonHelper fromApiJsonHelper;
    private final CommandProcessingService processAndLogCommandService;
    private final SchedulerJobRunnerReadService schedulerJobRunnerReadService;
    private final ConfigurationDomainService configurationService;

    @Override
    public <T> CommandProcessingResult logCommandSource(final CommandTypeWrapper<T> wrapper) {
        boolean isApprovedByChecker = false;

        // check if is update of own account details
        if (wrapper.isUpdateOfOwnUserDetails(this.context.authenticatedUser(wrapper).getId())) {
            // then allow this operation to proceed.
            // maker checker doesnt mean anything here.
            isApprovedByChecker = true; // set to true in case permissions have
            // been maker-checker enabled by
            // accident.
        } else {
            // if not user changing their own details - check user has
            // permission to perform specific task.
            this.context.authenticatedUser(wrapper).validateHasPermissionTo(wrapper.getTaskPermissionName());
        }
        validateIsUpdateAllowed();

        TypeCommand<T> command = TypeCommand.from(wrapper.getRequest(), wrapper.getEntityName(), wrapper.getEntityId(),
                wrapper.getSubentityId(), wrapper.getGroupId(), wrapper.getClientId(), wrapper.getLoanId(), wrapper.getSavingsId(),
                wrapper.getTransactionId(), wrapper.getHref(), wrapper.getProductId(), wrapper.getCreditBureauId(),
                wrapper.getOrganisationCreditBureauId(), wrapper.getJobName());

        // TODO: This is temporarily added to support the use of old jsonCommand during the migration process from
        // jsonCommand to typeCommand.
        // Some logic still uses jsonCommand. Once the migration is complete and all logic has been updated to use
        // typeCommand, this method should be removed.
        command.setJsonCommand(getJsonCommand(wrapper));
        return this.processAndLogCommandService.executeCommand(wrapper, command, isApprovedByChecker);

    }

    @Override
    public CommandProcessingResult approveEntry(final Long makerCheckerId) {
        // TODO: This method hasn't been rewritten due to an implementation challenge.
        // It requires the correct type from the database to deserialize the correct request body.
        // Currently, this is not possible because CommandSource does not have the request type, only the JSON body.
        // This method needs to be updated once a solution for the deserialization issue is found.
        throw new UnsupportedOperationException("Approve entry is not implemented yet.");
    }

    @Transactional
    @Override
    public Long deleteEntry(final Long makerCheckerId) {

        validateMakerCheckerTransaction(makerCheckerId);
        validateIsUpdateAllowed();

        this.commandSourceRepository.deleteById(makerCheckerId);

        return makerCheckerId;
    }

    private CommandSource validateMakerCheckerTransaction(final Long makerCheckerId) {
        final CommandSource commandSource = this.commandSourceRepository.findById(makerCheckerId)
                .orElseThrow(() -> new CommandNotFoundException(makerCheckerId));
        if (!commandSource.isMarkedAsAwaitingApproval()) {
            throw new CommandNotAwaitingApprovalException(makerCheckerId);
        }
        AppUser appUser = this.context.authenticatedUser();
        String permissionCode = commandSource.getPermissionCode();
        appUser.validateHasCheckerPermissionTo(permissionCode);
        if (!configurationService.isSameMakerCheckerEnabled() && !appUser.isCheckerSuperUser()) {
            AppUser maker = commandSource.getMaker();
            if (maker == null) {
                throw new UnsupportedCommandException(permissionCode, "Maker user is missing.");
            }
            if (Objects.equals(appUser.getId(), maker.getId())) {
                throw new UnsupportedCommandException(permissionCode, "Can not be checked by the same user.");
            }
        }
        return commandSource;
    }

    private void validateIsUpdateAllowed() {
        this.schedulerJobRunnerReadService.isUpdatesAllowed();
    }

    @Override
    public Long rejectEntry(final Long makerCheckerId) {
        final CommandSource commandSourceInput = validateMakerCheckerTransaction(makerCheckerId);
        validateIsUpdateAllowed();
        final AppUser maker = this.context.authenticatedUser();
        commandSourceInput.markAsRejected(maker);
        this.commandSourceRepository.save(commandSourceInput);
        return makerCheckerId;
    }

    // TODO: Remove after MVC migration
    private JsonCommand getJsonCommand(CommandTypeWrapper<?> wrapper) {
        final String json = wrapper.getJsonWrapper().getJson();
        final JsonElement parsedCommand = this.fromApiJsonHelper.parse(json);
        return JsonCommand.from(json, parsedCommand, this.fromApiJsonHelper, wrapper.getEntityName(), wrapper.getEntityId(),
                wrapper.getSubentityId(), wrapper.getGroupId(), wrapper.getClientId(), wrapper.getLoanId(), wrapper.getSavingsId(),
                wrapper.getTransactionId(), wrapper.getHref(), wrapper.getProductId(), wrapper.getCreditBureauId(),
                wrapper.getOrganisationCreditBureauId(), wrapper.getJobName());
    }
}

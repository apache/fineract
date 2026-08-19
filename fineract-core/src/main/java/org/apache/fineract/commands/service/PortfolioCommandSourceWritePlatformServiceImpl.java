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
package org.apache.fineract.commands.service;

import com.google.gson.JsonElement;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.commands.domain.CommandProcessingResultType;
import org.apache.fineract.commands.domain.CommandSource;
import org.apache.fineract.commands.domain.CommandSourceRepository;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.exception.CommandNotAwaitingApprovalException;
import org.apache.fineract.commands.exception.CommandNotFoundException;
import org.apache.fineract.commands.exception.MakerCheckerCheckerOnlyInitiationException;
import org.apache.fineract.commands.exception.MakerCheckerDuplicatePendingSubmissionException;
import org.apache.fineract.commands.exception.UnsupportedCommandException;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.dataqueries.service.CleanupService;
import org.apache.fineract.infrastructure.jobs.service.SchedulerJobRunnerReadService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioCommandSourceWritePlatformServiceImpl implements PortfolioCommandSourceWritePlatformService {

    private final PlatformSecurityContext context;
    private final CommandSourceRepository commandSourceRepository;
    private final FromJsonHelper fromApiJsonHelper;
    private final CommandProcessingService processAndLogCommandService;
    private final SchedulerJobRunnerReadService schedulerJobRunnerReadService;
    private final ConfigurationDomainService configurationService;
    private final List<CleanupService> cleanupServices;

    @Override
    public CommandProcessingResult logCommandSource(final CommandWrapper wrapper) {
        boolean isApprovedByChecker = false;
        final AppUser currentUser = this.context.authenticatedUser(wrapper);

        // check if is update of own account details
        if (wrapper.isChangeOfOwnUserDetails(currentUser.getId())) {
            // then allow this operation to proceed.
            // maker checker doesnt mean anything here.
            isApprovedByChecker = true; // set to true in case permissions have
                                        // been maker-checker enabled by
                                        // accident.
        } else {
            final String taskPermission = wrapper.getTaskPermissionName();
            final boolean hasBasePermission = !currentUser.hasNotPermissionForAnyOf(taskPermission);
            final boolean hasCheckerPermission = !currentUser.hasNotPermissionForAnyOf("CHECKER_SUPER_USER", taskPermission + "_CHECKER");

            if (!hasBasePermission && hasCheckerPermission) {
                // Checker-only user: find and approve the pending entry for this action+entity+resource
                final Long resourceId = resolveResourceId(wrapper);
                final List<Long> pendingIds = findPendingCommandIdsByResource(wrapper, resourceId);
                if (!pendingIds.isEmpty()) {
                    final Long pendingCommandId = pendingIds.get(0);
                    log.debug("Checker-only user {} auto-approving pending command id={} for {}/{}", currentUser.getUsername(),
                            pendingCommandId, wrapper.entityName(), wrapper.actionName());
                    return approveEntry(pendingCommandId);
                } else {
                    throw new MakerCheckerCheckerOnlyInitiationException(taskPermission);
                }
            } else {
                currentUser.validateHasPermissionTo(taskPermission);

                if (!hasCheckerPermission && configurationService.isMakerCheckerEnabledForTask(taskPermission)) {
                    final Long resourceId = resolveResourceId(wrapper);
                    final List<Long> pendingIds = findPendingCommandIdsByResource(wrapper, resourceId);
                    if (!pendingIds.isEmpty()) {
                        log.warn("Maker {} attempted duplicate submission for {}/{} - pending id={}", currentUser.getUsername(),
                                wrapper.entityName(), wrapper.actionName(), pendingIds.get(0));
                        throw new MakerCheckerDuplicatePendingSubmissionException(wrapper.actionName(), wrapper.entityName());
                    }
                }
            }
        }
        validateIsUpdateAllowed();

        final String json = wrapper.getJson();
        final JsonElement parsedCommand = this.fromApiJsonHelper.parse(json);
        JsonCommand command = JsonCommand.from(json, parsedCommand, this.fromApiJsonHelper, wrapper.getEntityName(), wrapper.getEntityId(),
                wrapper.getSubentityId(), wrapper.getGroupId(), wrapper.getClientId(), wrapper.getLoanId(), wrapper.getSavingsId(),
                wrapper.getTransactionId(), wrapper.getHref(), wrapper.getProductId(), wrapper.getCreditBureauId(),
                wrapper.getOrganisationCreditBureauId(), wrapper.getJobName(), wrapper.getLoanExternalId());

        return this.processAndLogCommandService.executeCommand(wrapper, command, isApprovedByChecker);
    }

    @Override
    public CommandProcessingResult approveEntry(final Long makerCheckerId) {
        final CommandSource commandSourceInput = validateMakerCheckerTransaction(makerCheckerId);
        validateIsUpdateAllowed();

        final CommandWrapper wrapper = CommandWrapper.fromExistingCommand(makerCheckerId, commandSourceInput.getActionName(),
                commandSourceInput.getEntityName(), commandSourceInput.getResourceId(), commandSourceInput.getSubResourceId(),
                commandSourceInput.getResourceGetUrl(), commandSourceInput.getProductId(), commandSourceInput.getOfficeId(),
                commandSourceInput.getGroupId(), commandSourceInput.getClientId(), commandSourceInput.getLoanId(),
                commandSourceInput.getSavingsId(), commandSourceInput.getTransactionId(), commandSourceInput.getCreditBureauId(),
                commandSourceInput.getOrganisationCreditBureauId(), commandSourceInput.getIdempotencyKey(),
                commandSourceInput.getLoanExternalId());
        final JsonElement parsedCommand = this.fromApiJsonHelper.parse(commandSourceInput.getCommandAsJson());
        final JsonCommand command = JsonCommand.fromExistingCommand(makerCheckerId, commandSourceInput.getCommandAsJson(), parsedCommand,
                this.fromApiJsonHelper, commandSourceInput.getEntityName(), commandSourceInput.getResourceId(),
                commandSourceInput.getSubResourceId(), commandSourceInput.getGroupId(), commandSourceInput.getClientId(),
                commandSourceInput.getLoanId(), commandSourceInput.getSavingsId(), commandSourceInput.getTransactionId(),
                commandSourceInput.getResourceGetUrl(), commandSourceInput.getProductId(), commandSourceInput.getCreditBureauId(),
                commandSourceInput.getOrganisationCreditBureauId(), commandSourceInput.getJobName(),
                commandSourceInput.getLoanExternalId());

        return this.processAndLogCommandService.executeCommand(wrapper, command, true);
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
        if (!commandSource.isAwaitingApproval()) {
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

    private List<Long> findPendingCommandIdsByResource(final CommandWrapper wrapper, final Long resourceId) {
        if (resourceId == null) {
            return List.of();
        }
        return this.commandSourceRepository.findPendingIdsByActionAndEntityAndResource(wrapper.actionName(), wrapper.entityName(),
                resourceId, CommandProcessingResultType.AWAITING_APPROVAL.getValue());
    }

    private Long resolveResourceId(final CommandWrapper wrapper) {
        if (wrapper.getEntityId() != null) {
            return wrapper.getEntityId();
        }
        if (wrapper.getLoanId() != null) {
            return wrapper.getLoanId();
        }
        if (wrapper.getSavingsId() != null) {
            return wrapper.getSavingsId();
        }
        if (wrapper.getClientId() != null) {
            return wrapper.getClientId();
        }
        if (wrapper.getGroupId() != null) {
            return wrapper.getGroupId();
        }
        return null;
    }

    @Override
    public Long rejectEntry(final Long makerCheckerId) {
        final CommandSource commandSourceInput = validateMakerCheckerTransaction(makerCheckerId);
        validateIsUpdateAllowed();
        final AppUser maker = this.context.authenticatedUser();
        commandSourceInput.markAsRejected(maker);
        this.commandSourceRepository.save(commandSourceInput);
        if (cleanupServices != null) {
            for (CleanupService cleanupService : cleanupServices) {
                cleanupService.cleanup(commandSourceInput);
            }
        }
        return makerCheckerId;
    }
}

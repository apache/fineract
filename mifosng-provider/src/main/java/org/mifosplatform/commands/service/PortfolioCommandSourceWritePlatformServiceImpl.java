/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.commands.service;

import java.util.Random;

import org.joda.time.DateTime;
import org.mifosplatform.commands.domain.CommandSource;
import org.mifosplatform.commands.domain.CommandSourceRepository;
import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.exception.CommandNotAwaitingApprovalException;
import org.mifosplatform.commands.exception.CommandNotFoundException;
import org.mifosplatform.commands.exception.RollbackTransactionAsCommandIsNotApprovedByCheckerException;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.infrastructure.core.service.ThreadLocalContextUtil;
import org.mifosplatform.infrastructure.jobs.service.SchedulerJobRunnerReadService;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.useradministration.domain.AppUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonElement;

@Service
public class PortfolioCommandSourceWritePlatformServiceImpl implements PortfolioCommandSourceWritePlatformService {

    private final PlatformSecurityContext context;
    private final CommandSourceRepository commandSourceRepository;
    private final FromJsonHelper fromApiJsonHelper;
    private final CommandProcessingService processAndLogCommandService;
    private final SchedulerJobRunnerReadService schedulerJobRunnerReadService;
    private final static Logger logger = LoggerFactory.getLogger(PortfolioCommandSourceWritePlatformServiceImpl.class);

    @Autowired
    public PortfolioCommandSourceWritePlatformServiceImpl(final PlatformSecurityContext context,
            final CommandSourceRepository commandSourceRepository, final FromJsonHelper fromApiJsonHelper,
            final CommandProcessingService processAndLogCommandService, final SchedulerJobRunnerReadService schedulerJobRunnerReadService) {
        this.context = context;
        this.commandSourceRepository = commandSourceRepository;
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.processAndLogCommandService = processAndLogCommandService;
        this.schedulerJobRunnerReadService = schedulerJobRunnerReadService;
    }

    @Override
    public CommandProcessingResult logCommandSource(final CommandWrapper wrapper) {

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

        final String json = wrapper.getJson();
        CommandProcessingResult result = null;
        JsonCommand command = null;
        Integer numberOfRetries = 0;
        Integer maxNumberOfRetries = ThreadLocalContextUtil.getTenant().getMaxRetriesOnDeadlock();
        Integer maxIntervalBetweenRetries = ThreadLocalContextUtil.getTenant().getMaxIntervalBetweenRetries();
        final JsonElement parsedCommand = this.fromApiJsonHelper.parse(json);
        command = JsonCommand.from(json, parsedCommand, this.fromApiJsonHelper, wrapper.getEntityName(), wrapper.getEntityId(),
                wrapper.getSubentityId(), wrapper.getGroupId(), wrapper.getClientId(), wrapper.getLoanId(), wrapper.getSavingsId(),
                wrapper.getTransactionId(), wrapper.getHref(), wrapper.getProductId());
        while (numberOfRetries <= maxNumberOfRetries) {
            try {
                result = this.processAndLogCommandService.processAndLogCommand(wrapper, command, isApprovedByChecker);
                numberOfRetries = maxNumberOfRetries + 1;
            } catch (CannotAcquireLockException | ObjectOptimisticLockingFailureException exception) {
                logger.info("The following command " + command.json() + " has been retried  " + numberOfRetries + " time(s)");
                /***
                 * Fail if the transaction has been retired for
                 * maxNumberOfRetries
                 **/
                if (numberOfRetries >= maxNumberOfRetries) {
                    logger.warn("The following command " + command.json() + " has been retried for the max allowed attempts of "
                            + numberOfRetries + " and will be rolled back");
                    throw (exception);
                }
                /***
                 * Else sleep for a random time (between 1 to 10 seconds) and
                 * continue
                 **/
                try {
                    Random random = new Random();
                    int randomNum = random.nextInt(maxIntervalBetweenRetries + 1);
                    Thread.sleep(1000 + (randomNum * 1000));
                    numberOfRetries = numberOfRetries + 1;
                } catch (InterruptedException e) {
                    throw (exception);
                }
            } catch (final RollbackTransactionAsCommandIsNotApprovedByCheckerException e) {
                numberOfRetries = maxNumberOfRetries + 1;
                result = this.processAndLogCommandService.logCommand(e.getCommandSourceResult());
            }
        }

        return result;
    }

    @Override
    public CommandProcessingResult approveEntry(final Long makerCheckerId) {

        final CommandSource commandSourceInput = validateMakerCheckerTransaction(makerCheckerId);
        validateIsUpdateAllowed();

        final CommandWrapper wrapper = CommandWrapper.fromExistingCommand(makerCheckerId, commandSourceInput.getActionName(),
                commandSourceInput.getEntityName(), commandSourceInput.resourceId(), commandSourceInput.subresourceId(),
                commandSourceInput.getResourceGetUrl(), commandSourceInput.getProductId(), commandSourceInput.getOfficeId(),
                commandSourceInput.getGroupId(), commandSourceInput.getClientId(), commandSourceInput.getLoanId(),
                commandSourceInput.getSavingsId(), commandSourceInput.getTransactionId());
        final JsonElement parsedCommand = this.fromApiJsonHelper.parse(commandSourceInput.json());
        final JsonCommand command = JsonCommand.fromExistingCommand(makerCheckerId, commandSourceInput.json(), parsedCommand,
                this.fromApiJsonHelper, commandSourceInput.getEntityName(), commandSourceInput.resourceId(),
                commandSourceInput.subresourceId(), commandSourceInput.getGroupId(), commandSourceInput.getClientId(),
                commandSourceInput.getLoanId(), commandSourceInput.getSavingsId(), commandSourceInput.getTransactionId(),
                commandSourceInput.getResourceGetUrl(), commandSourceInput.getProductId());

        final boolean makerCheckerApproval = true;
        return this.processAndLogCommandService.processAndLogCommand(wrapper, command, makerCheckerApproval);
    }

    @Transactional
    @Override
    public Long deleteEntry(final Long makerCheckerId) {

        validateMakerCheckerTransaction(makerCheckerId);
        validateIsUpdateAllowed();

        this.commandSourceRepository.delete(makerCheckerId);

        return makerCheckerId;
    }

    private CommandSource validateMakerCheckerTransaction(final Long makerCheckerId) {

        final CommandSource commandSourceInput = this.commandSourceRepository.findOne(makerCheckerId);
        if (commandSourceInput == null) { throw new CommandNotFoundException(makerCheckerId); }
        if (!(commandSourceInput.isMarkedAsAwaitingApproval())) { throw new CommandNotAwaitingApprovalException(makerCheckerId); }

        this.context.authenticatedUser().validateHasCheckerPermissionTo(commandSourceInput.getPermissionCode());

        return commandSourceInput;
    }

    private boolean validateIsUpdateAllowed() {
        return this.schedulerJobRunnerReadService.isUpdatesAllowed();

    }

    @Override
    public Long rejectEntry(final Long makerCheckerId) {
        final CommandSource commandSourceInput = validateMakerCheckerTransaction(makerCheckerId);
        validateIsUpdateAllowed();
        final AppUser maker = this.context.authenticatedUser();
        commandSourceInput.markAsRejected(maker, DateTime.now());
        this.commandSourceRepository.save(commandSourceInput);
        return makerCheckerId;
    }
}
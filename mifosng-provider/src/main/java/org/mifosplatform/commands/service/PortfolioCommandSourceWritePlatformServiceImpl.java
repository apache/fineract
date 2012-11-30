package org.mifosplatform.commands.service;

import org.joda.time.LocalDate;
import org.mifosng.platform.api.data.EntityIdentifier;
import org.mifosplatform.commands.domain.CommandSource;
import org.mifosplatform.commands.domain.CommandSourceRepository;
import org.mifosplatform.commands.handler.CommandSourceHandlerDelegator;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.infrastructure.user.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PortfolioCommandSourceWritePlatformServiceImpl implements PortfolioCommandSourceWritePlatformService {

    private final PlatformSecurityContext context;
    private final CommandSourceRepository commandSourceRepository;
    private final CommandSourceHandlerDelegator commandSourceHandlerDelegator;

    @Autowired
    public PortfolioCommandSourceWritePlatformServiceImpl(final PlatformSecurityContext context,
            final CommandSourceRepository makerCheckerRepository, final CommandSourceHandlerDelegator commandSourceHandlerDelegator) {
        this.context = context;
        this.commandSourceRepository = makerCheckerRepository;
        this.commandSourceHandlerDelegator = commandSourceHandlerDelegator;
    }

    // NOT TRANSACTIONAL BY DESIGN FOR NOW
    @Override
    public EntityIdentifier logCommandSource(final String apiOperation, final String resource, final Long resourceId,
            final String commandSerializedAsJson) {

        final AppUser maker = context.authenticatedUser();
        final LocalDate asToday = new LocalDate();

        final CommandSource commandSourceInput = CommandSource.createdBy(apiOperation, resource, resourceId, commandSerializedAsJson, maker, asToday);
        final CommandSource commandSourceResult = this.commandSourceHandlerDelegator.handle(commandSourceInput, commandSerializedAsJson);
        commandSourceRepository.save(commandSourceResult);

        return EntityIdentifier.makerChecker(commandSourceResult.resourceId(), commandSourceResult.getId());
    }

    @Transactional
    @Override
    public EntityIdentifier approveEntry(final Long id) {

        context.authenticatedUser();

        final CommandSource commandSourceInput = this.commandSourceRepository.findOne(id);
        final CommandSource commandSourceResult = this.commandSourceHandlerDelegator.handleExistingCommand(commandSourceInput);
        commandSourceRepository.save(commandSourceResult);

        return EntityIdentifier.makerChecker(commandSourceResult.resourceId(), commandSourceResult.getId());
    }

    @Transactional
    @Override
    public Long deleteEntry(final Long makerCheckerId) {

        context.authenticatedUser();

        this.commandSourceRepository.delete(makerCheckerId);

        return makerCheckerId;
    }
}
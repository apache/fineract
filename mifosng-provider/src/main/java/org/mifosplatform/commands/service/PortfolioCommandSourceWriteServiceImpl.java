package org.mifosplatform.commands.service;

import org.joda.time.LocalDate;
import org.mifosng.platform.api.data.EntityIdentifier;
import org.mifosng.platform.security.PlatformSecurityContext;
import org.mifosplatform.commands.domain.CommandSource;
import org.mifosplatform.commands.domain.CommandSourceRepository;
import org.mifosplatform.commands.handler.CommandSourceHandlerDelegator;
import org.mifosplatform.infrastructure.user.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// TODO RENAME SO matches interface name
@Service
public class PortfolioCommandSourceWriteServiceImpl implements PortfolioCommandSourceWritePlatformService {

    private final PlatformSecurityContext context;
    private final CommandSourceRepository commandSourceRepository;
    private final CommandSourceHandlerDelegator commandSourceHandlerDelegator;

    @Autowired
    public PortfolioCommandSourceWriteServiceImpl(final PlatformSecurityContext context,
            final CommandSourceRepository makerCheckerRepository, final CommandSourceHandlerDelegator commandSourceHandlerDelegator) {
        this.context = context;
        this.commandSourceRepository = makerCheckerRepository;
        this.commandSourceHandlerDelegator = commandSourceHandlerDelegator;
    }

    // NOT TRANSACTIONAL BY DESIGN FOR NOW
    @Override
    public EntityIdentifier logCommandSource(final String apiOperation, final String resource, final Long resourceId,
            final String apiRequestBodyInJson) {

        final AppUser maker = context.authenticatedUser();
        final LocalDate asToday = new LocalDate();

        final CommandSource commandSourceInput = CommandSource.createdBy(apiOperation, resource, resourceId, maker, asToday);
        final CommandSource commandSourceResult = this.commandSourceHandlerDelegator.handle(commandSourceInput, apiRequestBodyInJson);
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
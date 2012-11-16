package org.mifosng.platform.makerchecker.service;

import org.joda.time.LocalDate;
import org.mifosng.platform.api.commands.ClientCommand;
import org.mifosng.platform.api.data.EntityIdentifier;
import org.mifosng.platform.api.infrastructure.PortfolioApiDataConversionService;
import org.mifosng.platform.client.service.ClientWritePlatformService;
import org.mifosng.platform.client.service.RollbackTransactionAsCommandIsNotApprovedByCheckerException;
import org.mifosng.platform.makerchecker.domain.CommandSource;
import org.mifosng.platform.makerchecker.domain.CommandSourceRepository;
import org.mifosng.platform.security.PlatformSecurityContext;
import org.mifosng.platform.user.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 */
@Service
public class PortfolioCommandSourceWriteServiceImpl implements PortfolioCommandSourceWritePlatformService {

	private final PlatformSecurityContext context;
	private final CommandSourceRepository commandSourceRepository;
	private final PortfolioApiDataConversionService apiDataConversionService;
	private final ClientWritePlatformService clientWritePlatformService;

	@Autowired
	public PortfolioCommandSourceWriteServiceImpl(final PlatformSecurityContext context, final CommandSourceRepository makerCheckerRepository,
			final PortfolioApiDataConversionService apiDataConversionService,
			final ClientWritePlatformService clientWritePlatformService) {
		this.context = context;
		this.commandSourceRepository = makerCheckerRepository;
		this.apiDataConversionService = apiDataConversionService;
		this.clientWritePlatformService = clientWritePlatformService;
	}

	// NOT TRANSACTIONAL BY DESIGN FOR NOW
	@Override
	public EntityIdentifier logCommandSource(final String taskOperation, final String taskEntity, final Long entityId, final String taskJson) {
		
		final AppUser maker = context.authenticatedUser();
		
		final LocalDate asToday = new LocalDate();
		
		final CommandSource entity = CommandSource.createdBy(taskOperation, taskEntity, entityId, taskJson, maker, asToday);
		
		final boolean makerCheckerApproval = false;
		Long resourceId = null;
		if (entity.isClientResource()) {
			final ClientCommand command = this.apiDataConversionService.convertJsonToClientCommand(entityId, entity.json(), makerCheckerApproval);
			if (entity.isCreate()) {
				try {
					resourceId = this.clientWritePlatformService.createClient(command);
					entity.markAsChecked(maker, asToday);
					entity.updateResourceId(resourceId);
					} catch (RollbackTransactionAsCommandIsNotApprovedByCheckerException e) {
						// swallow this rollback transaction by design
					}
			} else if (entity.isUpdate()) {
				try {
					EntityIdentifier result = this.clientWritePlatformService.updateClientDetails(command);
					resourceId = result.getEntityId();
					entity.markAsChecked(maker, asToday);
				} catch (RollbackTransactionAsCommandIsNotApprovedByCheckerException e) {
					// swallow this rollback transaction by design
				}
			} else if (entity.isDelete()) {
				try {
					EntityIdentifier result = this.clientWritePlatformService.deleteClient(command);
					resourceId = result.getEntityId();
					entity.markAsChecked(maker, asToday);
				} catch (RollbackTransactionAsCommandIsNotApprovedByCheckerException e) {
					// swallow this rollback transaction by design
				}
			}
		}
		
		commandSourceRepository.save(entity);
		
		return EntityIdentifier.makerChecker(resourceId, entity.getId());
	}

	@Transactional
	@Override
	public EntityIdentifier approveEntry(final Long id) {
		
		final AppUser checker = context.authenticatedUser();
		
		final CommandSource entity = this.commandSourceRepository.findOne(id);
		entity.markAsChecked(checker, new LocalDate());
		
		Long resourceId = null;
		if (entity.isClientResource()) {
			final ClientCommand command = this.apiDataConversionService.convertJsonToClientCommand(entity.resourceId(), entity.json(), true);
			if (entity.isCreate()) {
				resourceId = this.clientWritePlatformService.createClient(command);
			} else if (entity.isUpdate()) {
				EntityIdentifier result = this.clientWritePlatformService.updateClientDetails(command);
				resourceId = result.getEntityId();
			} else if (entity.isDelete()) {
				EntityIdentifier result = this.clientWritePlatformService.deleteClient(command);
				resourceId = result.getEntityId();
			}
		}
		
		entity.updateResourceId(resourceId);
		
		commandSourceRepository.save(entity);
		
		return EntityIdentifier.makerChecker(resourceId, entity.getId());
	}

	@Transactional
	@Override
	public Long deleteEntry(final Long makerCheckerId) {
		
		context.authenticatedUser();
		
		this.commandSourceRepository.delete(makerCheckerId);
		
		return makerCheckerId;
	}
}
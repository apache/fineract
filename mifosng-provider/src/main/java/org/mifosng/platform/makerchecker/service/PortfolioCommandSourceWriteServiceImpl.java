package org.mifosng.platform.makerchecker.service;

import org.joda.time.LocalDate;
import org.mifosng.platform.api.commands.ClientCommand;
import org.mifosng.platform.api.data.ClientData;
import org.mifosng.platform.api.data.EntityIdentifier;
import org.mifosng.platform.api.infrastructure.PortfolioApiDataConversionService;
import org.mifosng.platform.api.infrastructure.PortfolioApiJsonSerializerService;
import org.mifosng.platform.client.service.ClientReadPlatformService;
import org.mifosng.platform.client.service.ClientWritePlatformService;
import org.mifosng.platform.client.service.RollbackTransactionAsCommandIsNotApprovedByCheckerException;
import org.mifosng.platform.makerchecker.domain.CommandSource;
import org.mifosng.platform.makerchecker.domain.CommandSourceRepository;
import org.mifosng.platform.security.PlatformSecurityContext;
import org.mifosng.platform.user.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PortfolioCommandSourceWriteServiceImpl implements PortfolioCommandSourceWritePlatformService {

	private final PlatformSecurityContext context;
	private final CommandSourceRepository commandSourceRepository;
	private final PortfolioApiDataConversionService apiDataConversionService;
	private final PortfolioApiJsonSerializerService apiJsonSerializerService;
	private final ClientWritePlatformService clientWritePlatformService;
	private final ClientReadPlatformService clientReadPlatformService;

	@Autowired
	public PortfolioCommandSourceWriteServiceImpl(final PlatformSecurityContext context, final CommandSourceRepository makerCheckerRepository,
			final PortfolioApiDataConversionService apiDataConversionService,
			final PortfolioApiJsonSerializerService apiJsonSerializerService,
			final ClientWritePlatformService clientWritePlatformService,
			final ClientReadPlatformService clientReadPlatformService) {
		this.context = context;
		this.commandSourceRepository = makerCheckerRepository;
		this.apiDataConversionService = apiDataConversionService;
		this.apiJsonSerializerService = apiJsonSerializerService;
		this.clientWritePlatformService = clientWritePlatformService;
		this.clientReadPlatformService = clientReadPlatformService;
	}

	// NOT TRANSACTIONAL BY DESIGN FOR NOW
	@Override
	public EntityIdentifier logCommandSource(final String apiOperation, final String resource, final Long resourceId, final String jsonRequestBody) {
		
		final AppUser maker = context.authenticatedUser();
		
		final LocalDate asToday = new LocalDate();
		
		final CommandSource commandSource = CommandSource.createdBy(apiOperation, resource, resourceId, maker, asToday);
		
		Long newResourceId = null;
		if (commandSource.isClientResource()) {
			
			// translate incoming api request json into java object catering for local and dateFormat parts of api
			final ClientCommand command = this.apiDataConversionService.convertApiRequestJsonToClientCommand(resourceId, jsonRequestBody);
			// produce serialized json of internal java object representation for persistence.
			final String internalCommandSerializedAsJson = this.apiJsonSerializerService.serializeClientCommandToJson(command);
			commandSource.updateJsonTo(internalCommandSerializedAsJson);
			
			if (commandSource.isCreate()) {
				try {
					newResourceId = this.clientWritePlatformService.createClient(command);
					commandSource.markAsChecked(maker, asToday);
					commandSource.updateResourceId(newResourceId);
					} catch (RollbackTransactionAsCommandIsNotApprovedByCheckerException e) {
						// swallow this rollback transaction by design
					}
			} else if (commandSource.isUpdate()) {
				try {
					// useful to employ change detection on update scenario to only store what details have changed
					final ClientData originalClient = this.clientReadPlatformService.retrieveIndividualClient(resourceId);
					final ClientData changedClient = this.apiDataConversionService.convertInternalJsonFormatToClientDataChange(resourceId, internalCommandSerializedAsJson);

					final String baseJson = this.apiJsonSerializerService.serializeClientDataToJson(originalClient);
					final String workingJson = this.apiJsonSerializerService.serializeClientDataToJson(changedClient);
					final ClientCommand changesOnly = this.apiDataConversionService.detectChanges(resourceId, baseJson, workingJson);

					final String changesOnlyJson = this.apiJsonSerializerService.serializeClientCommandToJson(changesOnly);
					commandSource.updateJsonTo(changesOnlyJson);
					
					EntityIdentifier result = this.clientWritePlatformService.updateClientDetails(changesOnly);
					newResourceId = result.getEntityId();
					
					commandSource.markAsChecked(maker, asToday);
				} catch (RollbackTransactionAsCommandIsNotApprovedByCheckerException e) {
					// swallow this rollback transaction by design
				}
			} else if (commandSource.isDelete()) {
				try {
					EntityIdentifier result = this.clientWritePlatformService.deleteClient(command);
					newResourceId = result.getEntityId();
					commandSource.markAsChecked(maker, asToday);
				} catch (RollbackTransactionAsCommandIsNotApprovedByCheckerException e) {
					// swallow this rollback transaction by design
				}
			}
		}
		
		commandSourceRepository.save(commandSource);
		
		return EntityIdentifier.makerChecker(newResourceId, commandSource.getId());
	}

	@Transactional
	@Override
	public EntityIdentifier approveEntry(final Long id) {
		
		final AppUser checker = context.authenticatedUser();
		
		final CommandSource entity = this.commandSourceRepository.findOne(id);
		entity.markAsChecked(checker, new LocalDate());
		
		Long resourceId = null;
		if (entity.isClientResource()) {
			final ClientCommand command = this.apiDataConversionService.convertInternalJsonFormatToClientCommand(entity.resourceId(), entity.json(), true);
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
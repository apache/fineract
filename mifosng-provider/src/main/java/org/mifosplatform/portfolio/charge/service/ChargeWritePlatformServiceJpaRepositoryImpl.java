package org.mifosplatform.portfolio.charge.service;

import java.util.Map;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.charge.domain.Charge;
import org.mifosplatform.portfolio.charge.domain.ChargeRepository;
import org.mifosplatform.portfolio.charge.exception.ChargeNotFoundException;
import org.mifosplatform.portfolio.charge.serialization.ChargeDefinitionCommandFromApiJsonDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChargeWritePlatformServiceJpaRepositoryImpl implements ChargeWritePlatformService {

    private final static Logger logger = LoggerFactory.getLogger(ChargeWritePlatformServiceJpaRepositoryImpl.class);

    private final PlatformSecurityContext context;
    private final ChargeDefinitionCommandFromApiJsonDeserializer fromApiJsonDeserializer;
    private final ChargeRepository chargeRepository;

    @Autowired
    public ChargeWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context,
            final ChargeDefinitionCommandFromApiJsonDeserializer fromApiJsonDeserializer, final ChargeRepository chargeRepository) {
        this.context = context;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.chargeRepository = chargeRepository;
    }

    @Transactional
    @Override
    public CommandProcessingResult createCharge(final JsonCommand command) {
        try {
            this.context.authenticatedUser();

            this.fromApiJsonDeserializer.validateForCreate(command.json());

            final Charge charge = Charge.fromJson(command);
            this.chargeRepository.save(charge);

            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(charge.getId()).build();
        } catch (DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve);
            return CommandProcessingResult.empty();
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult updateCharge(final Long chargeId, final JsonCommand command) {

        try {
            this.context.authenticatedUser();

            this.fromApiJsonDeserializer.validateForUpdate(command.json());

            final Charge chargeForUpdate = this.chargeRepository.findOne(chargeId);
            if (chargeForUpdate == null) { throw new ChargeNotFoundException(chargeId); }

            final Map<String, Object> changes = chargeForUpdate.update(command);

            if (!changes.isEmpty()) {
                this.chargeRepository.save(chargeForUpdate);
            }

            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(chargeId).with(changes).build();
        } catch (DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve);
            return CommandProcessingResult.empty();
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult deleteCharge(final Long chargeId) {

        this.context.authenticatedUser();

        final Charge chargeForDelete = this.chargeRepository.findOne(chargeId);
        if (chargeForDelete == null || chargeForDelete.isDeleted()) { throw new ChargeNotFoundException(chargeId); }

        chargeForDelete.delete();

        chargeRepository.save(chargeForDelete);

        return new CommandProcessingResultBuilder().withEntityId(chargeForDelete.getId()).build();
    }

    /*
     * Guaranteed to throw an exception no matter what the data integrity issue
     * is.
     */
    private void handleDataIntegrityIssues(final JsonCommand command, final DataIntegrityViolationException dve) {

        Throwable realCause = dve.getMostSpecificCause();
        if (realCause.getMessage().contains("name")) {
            final String name = command.stringValueOfParameterNamed("name");
            throw new PlatformDataIntegrityException("error.msg.charge.duplicate.name", "Charge with name `" + name + "` already exists",
                    "name", name);
        }

        logger.error(dve.getMessage(), dve);
        throw new PlatformDataIntegrityException("error.msg.charge.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource: " + realCause.getMessage());
    }
}

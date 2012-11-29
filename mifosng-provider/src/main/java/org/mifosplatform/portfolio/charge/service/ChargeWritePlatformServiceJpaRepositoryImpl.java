package org.mifosplatform.portfolio.charge.service;

import org.mifosng.platform.exceptions.ChargeNotFoundException;
import org.mifosng.platform.exceptions.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.charge.command.ChargeCommand;
import org.mifosplatform.portfolio.charge.command.ChargeCommandValidator;
import org.mifosplatform.portfolio.charge.domain.Charge;
import org.mifosplatform.portfolio.charge.domain.ChargeAppliesTo;
import org.mifosplatform.portfolio.charge.domain.ChargeCalculationType;
import org.mifosplatform.portfolio.charge.domain.ChargeRepository;
import org.mifosplatform.portfolio.charge.domain.ChargeTimeType;
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
    private final ChargeRepository chargeRepository;

    @Autowired
    public ChargeWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context, final ChargeRepository chargeRepository) {
        this.context = context;
        this.chargeRepository = chargeRepository;
    }

    @Transactional
    @Override
    public Long createCharge(final ChargeCommand command) {
        try {
            this.context.authenticatedUser();

            final ChargeCommandValidator validator = new ChargeCommandValidator(command);
            validator.validateForCreate();

            final ChargeAppliesTo chargeAppliesTo = ChargeAppliesTo.fromInt(command.getChargeAppliesTo());
            final ChargeTimeType chargeTimeType = ChargeTimeType.fromInt(command.getChargeTimeType());
            final ChargeCalculationType chargeCalculationType = ChargeCalculationType.fromInt(command.getChargeCalculationType());

            final Charge charge = Charge.createNew(command.getName(), command.getAmount(), command.getCurrencyCode(), chargeAppliesTo,
                    chargeTimeType, chargeCalculationType, command.isPenalty(), command.isActive());

            this.chargeRepository.saveAndFlush(charge);

            return charge.getId();
        } catch (DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve);
            return Long.valueOf(-1);
        }
    }

    @Transactional
    @Override
    public Long updateCharge(final ChargeCommand command) {

        try {

            this.context.authenticatedUser();
            ChargeCommandValidator validator = new ChargeCommandValidator(command);
            validator.validateForUpdate();

            final Long chargeId = command.getId();
            final Charge chargeForUpdate = this.chargeRepository.findOne(chargeId);

            if (chargeForUpdate == null) { throw new ChargeNotFoundException(chargeId); }
            chargeForUpdate.update(command);

            this.chargeRepository.saveAndFlush(chargeForUpdate);

            return chargeForUpdate.getId();
        } catch (DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve);
            return Long.valueOf(-1);
        }
    }

    @Transactional
    @Override
    public Long deleteCharge(Long chargeId) {

        this.context.authenticatedUser();

        final Charge chargeForDelete = this.chargeRepository.findOne(chargeId);
        if (chargeForDelete == null || chargeForDelete.isDeleted()) { throw new ChargeNotFoundException(chargeId); }
        chargeForDelete.delete();

        chargeRepository.saveAndFlush(chargeForDelete);

        return chargeForDelete.getId();
    }

    /*
     * Guaranteed to throw an exception no matter what the data integrity issue
     * is.
     */
    private void handleDataIntegrityIssues(final ChargeCommand command, final DataIntegrityViolationException dve) {

        Throwable realCause = dve.getMostSpecificCause();
        if (realCause.getMessage().contains("name")) { throw new PlatformDataIntegrityException("error.msg.charge.duplicate.name",
                "Charge with name {0} already exists", "name", command.getName()); }

        logger.error(dve.getMessage(), dve);
        throw new PlatformDataIntegrityException("error.msg.charge.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource: " + realCause.getMessage());
    }
}

package org.mifosng.platform.charge.service;

import org.mifosng.platform.api.commands.ChargeCommand;
import org.mifosng.platform.charge.domain.*;
import org.mifosng.platform.exceptions.ChargeNotFoundException;
import org.mifosng.platform.exceptions.PlatformDataIntegrityException;
import org.mifosng.platform.security.PlatformSecurityContext;
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
    public ChargeWritePlatformServiceJpaRepositoryImpl(PlatformSecurityContext context, ChargeRepository chargeRepository) {
        this.context = context;
        this.chargeRepository = chargeRepository;
    }

    @Transactional
    @Override
    public Long createCharge(final ChargeCommand command) {
        try {
            this.context.authenticatedUser();

            ChargeCommandValidator validator = new ChargeCommandValidator(command);
            validator.validateForCreate();

            ChargeAppliesTo chargeAppliesTo = ChargeAppliesTo.fromInt(command.getChargeAppliesTo());
            ChargeTimeType chargeTimeType = ChargeTimeType.fromInt(command.getChargeTimeType());
            ChargeCalculationMethod chargeCalculationMethod = ChargeCalculationMethod.fromInt(command.getChargeCalculationType());

            Charge charge = Charge.createNew(command.getName(), command.getAmount(), command.getCurrencyCode(),
                    chargeAppliesTo, chargeTimeType, chargeCalculationMethod, command.isActive());

            this.chargeRepository.saveAndFlush(charge);

            return charge.getId();
        } catch (DataIntegrityViolationException dve) {
            handleFundDataIntegrityIssues(command, dve);
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
            Charge chargeForUpdate = this.chargeRepository.findOne(chargeId);

            if (chargeForUpdate == null){
                throw new ChargeNotFoundException(chargeId);
            }
            chargeForUpdate.update(command);

            this.chargeRepository.saveAndFlush(chargeForUpdate);

            return chargeForUpdate.getId();
        } catch (DataIntegrityViolationException dve) {
            handleFundDataIntegrityIssues(command, dve);
            return Long.valueOf(-1);
        }
    }

    @Transactional
    @Override
    public Long deleteCharge(Long chargeId) {

        this.context.authenticatedUser();

        Charge chargeForDelete = this.chargeRepository.findOne(chargeId);
        if (chargeForDelete == null || chargeForDelete.isDeleted()){
            throw new ChargeNotFoundException(chargeId);
        }
        chargeForDelete.delete();

        chargeRepository.saveAndFlush(chargeForDelete);

        return chargeForDelete.getId();
    }

    /*
    * Guaranteed to throw an exception no matter what the data integrity issue is.
    */
    private void handleFundDataIntegrityIssues(final ChargeCommand command, DataIntegrityViolationException dve)  {

        Throwable realCause = dve.getMostSpecificCause();
        if (realCause.getMessage().contains("name")) {
            throw new PlatformDataIntegrityException("error.msg.charge.duplicate.name", "Charge with name {0} already exists", "name", command.getName());
        }

        logger.error(dve.getMessage(), dve);
        throw new PlatformDataIntegrityException("error.msg.charge.unknown.data.integrity.issue", "Unknown data integrity issue with resource: " + realCause.getMessage());
    }
}

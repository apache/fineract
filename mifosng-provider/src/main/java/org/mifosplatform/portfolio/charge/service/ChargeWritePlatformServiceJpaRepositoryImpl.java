package org.mifosplatform.portfolio.charge.service;

import org.mifosng.platform.client.service.RollbackTransactionAsCommandIsNotApprovedByCheckerException;
import org.mifosng.platform.exceptions.ChargeNotFoundException;
import org.mifosng.platform.exceptions.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.configuration.service.ConfigurationDomainService;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.charge.command.ChargeDefinitionCommand;
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
    private final ConfigurationDomainService configurationDomainService;

    @Autowired
    public ChargeWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context, final ChargeRepository chargeRepository, final ConfigurationDomainService configurationDomainService) {
        this.context = context;
        this.chargeRepository = chargeRepository;
        this.configurationDomainService = configurationDomainService;
    }

    @Transactional
    @Override
    public Long createCharge(final ChargeDefinitionCommand command) {
        try {
            this.context.authenticatedUser();
            command.validateForCreate();

            final ChargeAppliesTo chargeAppliesTo = ChargeAppliesTo.fromInt(command.getChargeAppliesTo());
            final ChargeTimeType chargeTimeType = ChargeTimeType.fromInt(command.getChargeTimeType());
            final ChargeCalculationType chargeCalculationType = ChargeCalculationType.fromInt(command.getChargeCalculationType());

            final Charge charge = Charge.createNew(command.getName(), command.getAmount(), command.getCurrencyCode(), chargeAppliesTo,
                    chargeTimeType, chargeCalculationType, command.isPenalty(), command.isActive());

            this.chargeRepository.save(charge);

            if (this.configurationDomainService.isMakerCheckerEnabledForTask("CREATE_CHARGE") && !command.isApprovedByChecker()) { throw new RollbackTransactionAsCommandIsNotApprovedByCheckerException(); }
            
            return charge.getId();
        } catch (DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve);
            return Long.valueOf(-1);
        }
    }

    @Transactional
    @Override
    public Long updateCharge(final ChargeDefinitionCommand command) {

        try {
            this.context.authenticatedUser();
            command.validateForUpdate();

            final Long chargeId = command.getId();
            final Charge chargeForUpdate = this.chargeRepository.findOne(chargeId);
            if (chargeForUpdate == null) { throw new ChargeNotFoundException(chargeId); }
            
            chargeForUpdate.update(command);

            this.chargeRepository.save(chargeForUpdate);

            if (this.configurationDomainService.isMakerCheckerEnabledForTask("UPDATE_CHARGE") && !command.isApprovedByChecker()) { throw new RollbackTransactionAsCommandIsNotApprovedByCheckerException(); }
            
            return chargeForUpdate.getId();
        } catch (DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve);
            return Long.valueOf(-1);
        }
    }

    @Transactional
    @Override
    public Long deleteCharge(final ChargeDefinitionCommand command) {

        this.context.authenticatedUser();

        final Long chargeId = command.getId();
        final Charge chargeForDelete = this.chargeRepository.findOne(chargeId);
        if (chargeForDelete == null || chargeForDelete.isDeleted()) { throw new ChargeNotFoundException(chargeId); }
        
        chargeForDelete.delete();

        chargeRepository.save(chargeForDelete);

        if (this.configurationDomainService.isMakerCheckerEnabledForTask("DELETE_CHARGE") && !command.isApprovedByChecker()) { throw new RollbackTransactionAsCommandIsNotApprovedByCheckerException(); }
        
        return chargeForDelete.getId();
    }

    /*
     * Guaranteed to throw an exception no matter what the data integrity issue
     * is.
     */
    private void handleDataIntegrityIssues(final ChargeDefinitionCommand command, final DataIntegrityViolationException dve) {

        Throwable realCause = dve.getMostSpecificCause();
        if (realCause.getMessage().contains("name")) { throw new PlatformDataIntegrityException("error.msg.charge.duplicate.name",
                "Charge with name {0} already exists", "name", command.getName()); }

        logger.error(dve.getMessage(), dve);
        throw new PlatformDataIntegrityException("error.msg.charge.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource: " + realCause.getMessage());
    }
}

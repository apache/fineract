package org.mifosplatform.portfolio.savingsdepositproduct.service;

import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.portfolio.loanproduct.domain.PeriodFrequencyType;
import org.mifosplatform.portfolio.savingsdepositproduct.command.DepositProductCommand;
import org.mifosplatform.portfolio.savingsdepositproduct.domain.DepositProduct;
import org.mifosplatform.portfolio.savingsdepositproduct.domain.DepositProductRepository;
import org.mifosplatform.portfolio.savingsdepositproduct.exception.DepositProductNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DepositProductWritePlatformServiceJpaRepositoryImpl implements DepositProductWritePlatformService {

    private final static Logger logger = LoggerFactory.getLogger(DepositProductWritePlatformServiceJpaRepositoryImpl.class);

    private final PlatformSecurityContext context;
    private final DepositProductRepository depositProductRepository;

    @Autowired
    public DepositProductWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context,
            final DepositProductRepository depositProductRepository) {
        this.context = context;
        this.depositProductRepository = depositProductRepository;
    }

    /*
     * Guaranteed to throw an exception no matter what the data integrity issue
     * is.
     */
    private void handleDataIntegrityIssues(final DepositProductCommand command, final DataIntegrityViolationException dve) {

        Throwable realCause = dve.getMostSpecificCause();
        if (realCause.getMessage().contains("name_deposit_product")) { throw new PlatformDataIntegrityException(
                "error.msg.desposit.product.duplicate.name", "Deposit product with name: " + command.getName() + " already exists", "name",
                command.getName()); }
        if (realCause.getMessage().contains("externalid_deposit_product")) { throw new PlatformDataIntegrityException(
                "error.msg.desposit.product.duplicate.externalId", "Deposit product with externalId " + command.getExternalId()
                        + " already exists", "externalId", command.getExternalId()); }

        logger.error(dve.getMessage(), dve);
        throw new PlatformDataIntegrityException("error.msg.deposit.product.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource.");
    }

    @Transactional
    @Override
    public CommandProcessingResult createDepositProduct(final DepositProductCommand command) {

        try {
            this.context.authenticatedUser();
            DepositProductCommandValidator validator = new DepositProductCommandValidator(command);
            validator.validateForCreate();

            PeriodFrequencyType interestCompoundingPeriodType = PeriodFrequencyType.fromInt(command.getInterestCompoundedEveryPeriodType());
            PeriodFrequencyType lockinPeriodType = PeriodFrequencyType.fromInt(command.getLockinPeriodType());
            MonetaryCurrency currency = new MonetaryCurrency(command.getCurrencyCode(), command.getDigitsAfterDecimal());
            DepositProduct product = new DepositProduct(command.getName(), command.getExternalId(), command.getDescription(), currency,
                    command.getMinimumBalance(), command.getMaximumBalance(), command.getTenureInMonths(),
                    command.getMaturityDefaultInterestRate(), command.getMaturityMinInterestRate(), command.getMaturityMaxInterestRate(),
                    command.getInterestCompoundedEvery(), interestCompoundingPeriodType, command.isRenewalAllowed(),
                    command.isPreClosureAllowed(), command.getPreClosureInterestRate(), command.isInterestCompoundingAllowed(),
                    command.isLockinPeriodAllowed(), command.getLockinPeriod(), lockinPeriodType);
            this.depositProductRepository.save(product);

            return new CommandProcessingResultBuilder() //
                    .withEntityId(product.getId()) //
                    .build();
        } catch (DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve);
            return new CommandProcessingResult(Long.valueOf(-1));
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult updateDepositProduct(final DepositProductCommand command) {

        try {
            this.context.authenticatedUser();
            DepositProductCommandValidator validator = new DepositProductCommandValidator(command);
            validator.validateForUpdate();

            DepositProduct product = this.depositProductRepository.findOne(command.getId());
            if (product == null) { throw new DepositProductNotFoundException(command.getId()); }

            PeriodFrequencyType interestCompoundingFrequency = null;
            if (command.isInterestCompoundedEveryPeriodTypeChanged()) {
                interestCompoundingFrequency = PeriodFrequencyType.fromInt(command.getInterestCompoundedEveryPeriodType());
            }

            PeriodFrequencyType lockinPeriodType = null;
            if (command.isLockinPeriodTypeChanged()) {
                lockinPeriodType = PeriodFrequencyType.fromInt(command.getLockinPeriodType());
            }

            product.update(command, interestCompoundingFrequency, lockinPeriodType);
            this.depositProductRepository.save(product);

            return new CommandProcessingResultBuilder() //
                    .withEntityId(product.getId()) //
                    .build();
        } catch (DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve);
            return new CommandProcessingResult(Long.valueOf(-1));
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult deleteDepositProduct(final Long productId) {

        this.context.authenticatedUser();
        DepositProduct product = this.depositProductRepository.findOne(productId);
        if (product == null) { throw new DepositProductNotFoundException(productId); }
        product.delete();

        this.depositProductRepository.save(product);

        return new CommandProcessingResultBuilder() //
                .withEntityId(product.getId()) //
                .build();
    }
}
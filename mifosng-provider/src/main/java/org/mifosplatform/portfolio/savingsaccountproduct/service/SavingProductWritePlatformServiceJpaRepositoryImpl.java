/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savingsaccountproduct.service;

import java.util.Map;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.security.exception.NoAuthorizationException;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.portfolio.loanproduct.domain.PeriodFrequencyType;
import org.mifosplatform.portfolio.savingsaccountproduct.domain.SavingFrequencyType;
import org.mifosplatform.portfolio.savingsaccountproduct.domain.SavingInterestCalculationMethod;
import org.mifosplatform.portfolio.savingsaccountproduct.domain.SavingProduct;
import org.mifosplatform.portfolio.savingsaccountproduct.domain.SavingProductRepository;
import org.mifosplatform.portfolio.savingsaccountproduct.domain.SavingProductType;
import org.mifosplatform.portfolio.savingsaccountproduct.domain.SavingsInterestType;
import org.mifosplatform.portfolio.savingsaccountproduct.exception.SavingProductNotFoundException;
import org.mifosplatform.portfolio.savingsaccountproduct.exception.SavingsProductNotFoundException;
import org.mifosplatform.portfolio.savingsaccountproduct.serialization.SavingProductCommandFromApiJsonDeserializer;
import org.mifosplatform.portfolio.savingsdepositproduct.domain.TenureTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SavingProductWritePlatformServiceJpaRepositoryImpl implements SavingProductWritePlatformService {

    private final PlatformSecurityContext context;
    private final SavingProductRepository savingProductRepository;
    private final SavingProductCommandFromApiJsonDeserializer fromApiJsonDeserializer;

    @Autowired
    public SavingProductWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context,
            final SavingProductRepository savingProductRepository, final SavingProductCommandFromApiJsonDeserializer fromApiJsonDeserializer) {
        this.context = context;
        this.savingProductRepository = savingProductRepository;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
    }

    @Transactional
    @Override
    public CommandProcessingResult createSavingProduct(final JsonCommand command) {

        this.context.authenticatedUser();
        this.fromApiJsonDeserializer.validateForCreate(command.json());

        final Integer savingProductTypeCommandValue = command.integerValueOfParameterNamed("savingProductType");
        SavingProductType savingProductType = SavingProductType.fromInt(savingProductTypeCommandValue);
        final Integer tenureTypeCommandValue = command.integerValueOfParameterNamed("tenureType");
        TenureTypeEnum tenureType = TenureTypeEnum.fromInt(tenureTypeCommandValue);
        final Integer frequencyCommandValue = command.integerValueOfParameterNamed("frequency");
        SavingFrequencyType savingFrequencyType = SavingFrequencyType.fromInt(frequencyCommandValue);
        final Integer interestTypeCommandValue = command.integerValueOfParameterNamed("interestType");
        SavingsInterestType interestType = SavingsInterestType.fromInt(interestTypeCommandValue);
        final Integer lockinPeriodTypeCommandValue = command.integerValueOfParameterNamed("lockinPeriodType");
        PeriodFrequencyType lockinPeriodType = PeriodFrequencyType.fromInt(lockinPeriodTypeCommandValue);
        final Integer interestCalculationMethodCommandValue = command.integerValueOfParameterNamed("interestCalculationMethod");
        SavingInterestCalculationMethod savingInterestCalculationMethod = SavingInterestCalculationMethod
                .fromInt(interestCalculationMethodCommandValue);
        final String currencyCode = command.stringValueOfParameterNamed("currencyCode");
        final Integer digitsAfterDecimal = command.integerValueOfParameterNamed("digitsAfterDecimal");
        MonetaryCurrency currency = new MonetaryCurrency(currencyCode, digitsAfterDecimal);

        if (savingProductType.equals(SavingProductType.INVALID) || tenureType.equals(TenureTypeEnum.INVALID)
                || savingFrequencyType.equals(SavingFrequencyType.INVALID) || interestType.equals(SavingsInterestType.INVALID)
                || lockinPeriodType.equals(PeriodFrequencyType.INVALID)
                || savingInterestCalculationMethod.equals(SavingInterestCalculationMethod.INVALID)) { throw new NoAuthorizationException(
                "Please select a valid types"); }

        SavingProduct product = SavingProduct.assembleFromJson(command, currency, savingProductType, tenureType, savingFrequencyType,
                interestType, savingInterestCalculationMethod, lockinPeriodType);

        this.savingProductRepository.save(product);
        return new CommandProcessingResultBuilder() //
                .withEntityId(product.getId()) //
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult updateSavingProduct(final Long productId, final JsonCommand command) {

        this.context.authenticatedUser();
        this.fromApiJsonDeserializer.validateForUpdate(command.json());

        final SavingProduct product = this.savingProductRepository.findOne(productId);

        if (product == null || product.isDeleted()) { throw new SavingProductNotFoundException(productId); }

        final Map<String, Object> changes = product.update(command);

        this.savingProductRepository.save(product);

        return new CommandProcessingResultBuilder() //
                .withEntityId(product.getId()) //
                .with(changes).build();
    }

    @Transactional
    @Override
    public CommandProcessingResult deleteSavingProduct(final Long productId) {

        this.context.authenticatedUser();
        SavingProduct product = this.savingProductRepository.findOne(productId);
        if (product == null || product.isDeleted()) { throw new SavingsProductNotFoundException(productId); }
        product.delete();
        this.savingProductRepository.save(product);
        return new CommandProcessingResultBuilder() //
                .withEntityId(product.getId()) //
                .build();
    }
}
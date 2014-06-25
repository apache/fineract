/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.domain;

import static org.mifosplatform.portfolio.savings.SavingsApiConstants.SAVINGS_ACCOUNT_RESOURCE_NAME;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.amountParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.chargeCalculationTypeParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.chargeIdParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.chargeTimeTypeParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.chargesParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.dueAsOfDateParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.feeOnMonthDayParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.feeIntervalParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.idParamName;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.joda.time.LocalDate;
import org.joda.time.MonthDay;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.portfolio.charge.domain.Charge;
import org.mifosplatform.portfolio.charge.domain.ChargeCalculationType;
import org.mifosplatform.portfolio.charge.domain.ChargeRepositoryWrapper;
import org.mifosplatform.portfolio.charge.domain.ChargeTimeType;
import org.mifosplatform.portfolio.charge.exception.ChargeCannotBeAppliedToException;
import org.mifosplatform.portfolio.charge.exception.SavingsAccountChargeNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@Service
public class SavingsAccountChargeAssembler {

    private final FromJsonHelper fromApiJsonHelper;
    private final ChargeRepositoryWrapper chargeRepository;
    private final SavingsAccountChargeRepository savingsAccountChargeRepository;

    @Autowired
    public SavingsAccountChargeAssembler(final FromJsonHelper fromApiJsonHelper, final ChargeRepositoryWrapper chargeRepository,
            final SavingsAccountChargeRepository savingsAccountChargeRepository) {
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.chargeRepository = chargeRepository;
        this.savingsAccountChargeRepository = savingsAccountChargeRepository;
    }

    public Set<SavingsAccountCharge> fromParsedJson(final JsonElement element, final String productCurrencyCode) {

        final Set<SavingsAccountCharge> savingsAccountCharges = new HashSet<>();

        if (element.isJsonObject()) {
            final JsonObject topLevelJsonElement = element.getAsJsonObject();
            final String dateFormat = this.fromApiJsonHelper.extractDateFormatParameter(topLevelJsonElement);
            final String monthDayFormat = this.fromApiJsonHelper.extractMonthDayFormatParameter(topLevelJsonElement);
            final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(topLevelJsonElement);
            if (topLevelJsonElement.has(chargesParamName) && topLevelJsonElement.get(chargesParamName).isJsonArray()) {
                final JsonArray array = topLevelJsonElement.get(chargesParamName).getAsJsonArray();
                for (int i = 0; i < array.size(); i++) {

                    final JsonObject savingsChargeElement = array.get(i).getAsJsonObject();

                    final Long id = this.fromApiJsonHelper.extractLongNamed(idParamName, savingsChargeElement);
                    final Long chargeId = this.fromApiJsonHelper.extractLongNamed(chargeIdParamName, savingsChargeElement);
                    final BigDecimal amount = this.fromApiJsonHelper.extractBigDecimalNamed(amountParamName, savingsChargeElement, locale);
                    final Integer chargeTimeType = this.fromApiJsonHelper.extractIntegerNamed(chargeTimeTypeParamName,
                            savingsChargeElement, locale);
                    final Integer chargeCalculationType = this.fromApiJsonHelper.extractIntegerNamed(chargeCalculationTypeParamName,
                            savingsChargeElement, locale);
                    final LocalDate dueDate = this.fromApiJsonHelper.extractLocalDateNamed(dueAsOfDateParamName, savingsChargeElement,
                            dateFormat, locale);

                    final MonthDay feeOnMonthDay = this.fromApiJsonHelper.extractMonthDayNamed(feeOnMonthDayParamName,
                            savingsChargeElement, monthDayFormat, locale);
                    final Integer feeInterval = this.fromApiJsonHelper.extractIntegerNamed(feeIntervalParamName, savingsChargeElement,
                            locale);

                    if (id == null) {
                        final Charge chargeDefinition = this.chargeRepository.findOneWithNotFoundDetection(chargeId);

                        if (!chargeDefinition.isSavingsCharge()) {
                            final String errorMessage = "Charge with identifier " + chargeDefinition.getId()
                                    + " cannot be applied to Savings product.";
                            throw new ChargeCannotBeAppliedToException("savings.product", errorMessage, chargeDefinition.getId());
                        }

                        ChargeTimeType chargeTime = null;
                        if (chargeTimeType != null) {
                            chargeTime = ChargeTimeType.fromInt(chargeTimeType);
                        }

                        ChargeCalculationType chargeCalculation = null;
                        if (chargeCalculationType != null) {
                            chargeCalculation = ChargeCalculationType.fromInt(chargeCalculationType);
                        }

                        final boolean status = true;
                        final SavingsAccountCharge savingsAccountCharge = SavingsAccountCharge.createNewWithoutSavingsAccount(
                                chargeDefinition, amount, chargeTime, chargeCalculation, dueDate, status, feeOnMonthDay, feeInterval);
                        savingsAccountCharges.add(savingsAccountCharge);
                    } else {
                        final Long savingsAccountChargeId = id;
                        final SavingsAccountCharge savingsAccountCharge = this.savingsAccountChargeRepository
                                .findOne(savingsAccountChargeId);
                        if (savingsAccountCharge == null) { throw new SavingsAccountChargeNotFoundException(savingsAccountChargeId); }

                        savingsAccountCharge.update(amount, dueDate, feeOnMonthDay, feeInterval);

                        savingsAccountCharges.add(savingsAccountCharge);
                    }
                }
            }
        }

        this.validateSavingsCharges(savingsAccountCharges, productCurrencyCode);
        return savingsAccountCharges;
    }

    public Set<SavingsAccountCharge> fromSavingsProduct(final SavingsProduct savingsProduct) {

        final Set<SavingsAccountCharge> savingsAccountCharges = new HashSet<>();
        Set<Charge> productCharges = savingsProduct.charges();
        for (Charge charge : productCharges) {
            ChargeTimeType chargeTime = null;
            if (charge.getChargeTime() != null) {
                chargeTime = ChargeTimeType.fromInt(charge.getChargeTime());
            }
            if (chargeTime != null && chargeTime.isOnSpecifiedDueDate()) {
                continue;
            }

            ChargeCalculationType chargeCalculation = null;
            if (charge.getChargeCalculation() != null) {
                chargeCalculation = ChargeCalculationType.fromInt(charge.getChargeCalculation());
            }
            final boolean status = true;
            final SavingsAccountCharge savingsAccountCharge = SavingsAccountCharge.createNewWithoutSavingsAccount(charge,
                    charge.getAmount(), chargeTime, chargeCalculation, null, status, charge.getFeeOnMonthDay(), charge.feeInterval());
            savingsAccountCharges.add(savingsAccountCharge);
        }
        return savingsAccountCharges;
    }

    private void validateSavingsCharges(final Set<SavingsAccountCharge> charges, final String productCurrencyCode) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(SAVINGS_ACCOUNT_RESOURCE_NAME);
        boolean isOneWithdrawalPresent = false;
        boolean isOneAnnualPresent = false;
        for (SavingsAccountCharge charge : charges) {
            if (!charge.hasCurrencyCodeOf(productCurrencyCode)) {
                baseDataValidator.reset().parameter("currency").value(charge.getCharge().getId())
                        .failWithCodeNoParameterAddedToErrorCode("currency.and.charge.currency.not.same");
            }

            if (charge.isWithdrawalFee()) {
                if (isOneWithdrawalPresent) {
                    baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("multiple.withdrawal.fee.per.account.not.supported");
                }
                isOneWithdrawalPresent = true;
            }

            if (charge.isAnnualFee()) {
                if (isOneAnnualPresent) {
                    baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("multiple.annual.fee.per.account.not.supported");
                }
                isOneAnnualPresent = true;
            }
        }
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
    }
}
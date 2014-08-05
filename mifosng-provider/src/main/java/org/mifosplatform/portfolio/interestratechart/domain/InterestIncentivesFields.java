/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.interestratechart.domain;

import static org.mifosplatform.portfolio.interestratechart.InterestIncentiveApiConstants.amountParamName;
import static org.mifosplatform.portfolio.interestratechart.InterestIncentiveApiConstants.attributeNameParamName;
import static org.mifosplatform.portfolio.interestratechart.InterestIncentiveApiConstants.attributeValueParamName;
import static org.mifosplatform.portfolio.interestratechart.InterestIncentiveApiConstants.conditionTypeParamName;
import static org.mifosplatform.portfolio.interestratechart.InterestIncentiveApiConstants.entityTypeParamName;
import static org.mifosplatform.portfolio.interestratechart.InterestIncentiveApiConstants.incentiveTypeparamName;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.portfolio.common.domain.ConditionType;
import org.mifosplatform.portfolio.interestratechart.incentive.InterestIncentiveAttributeName;
import org.mifosplatform.portfolio.interestratechart.incentive.InterestIncentiveEntityType;
import org.mifosplatform.portfolio.interestratechart.incentive.InterestIncentiveType;

@Embeddable
public class InterestIncentivesFields {

    @Column(name = "entiry_type", nullable = false)
    private Integer entityType;

    @Column(name = "attribute_name", nullable = false)
    private Integer attributeName;

    @Column(name = "condition_type", nullable = false)
    private Integer conditionType;

    @Column(name = "attribute_value", nullable = false)
    private String attributeValue;

    @Column(name = "incentive_type", nullable = false)
    private Integer incentiveType;

    @Column(name = "amount", scale = 6, precision = 19, nullable = false)
    private BigDecimal amount;

    protected InterestIncentivesFields() {

    }

    public static InterestIncentivesFields createNew(final Integer entityType, final Integer attributeName, final Integer conditionType,
            final String attributeValue, final Integer incentiveType, final BigDecimal amount, final DataValidatorBuilder baseDataValidator) {
        return new InterestIncentivesFields(entityType, attributeName, conditionType, attributeValue, incentiveType, amount,
                baseDataValidator);
    }

    private InterestIncentivesFields(final Integer entityType, final Integer attributeName, final Integer conditionType,
            final String attributeValue, final Integer incentiveType, final BigDecimal amount, final DataValidatorBuilder baseDataValidator) {
        this.entityType = entityType;
        this.attributeName = attributeName;
        this.attributeValue = attributeValue;
        this.conditionType = conditionType;
        this.incentiveType = incentiveType;
        this.amount = amount;
        validateIncentiveData(baseDataValidator);
    }

    public InterestIncentiveAttributeName attributeName() {
        return InterestIncentiveAttributeName.fromInt(this.attributeName);
    }

    public ConditionType conditionType() {
        return ConditionType.fromInt(this.conditionType);
    }

    public String attributeValue() {
        return this.attributeValue;
    }

    public InterestIncentiveType incentiveType() {
        return InterestIncentiveType.fromInt(this.incentiveType);
    }

    public BigDecimal amount() {
        return this.amount;
    }

    public InterestIncentiveEntityType entiryType() {
        return InterestIncentiveEntityType.fromInt(this.entityType);
    }

    public void update(final JsonCommand command, final Map<String, Object> actualChanges, final DataValidatorBuilder baseDataValidator,
            final Locale locale) {
        if (command.isChangeInIntegerParameterNamed(entityTypeParamName, this.entityType, locale)) {
            final Integer newValue = command.integerValueOfParameterNamed(entityTypeParamName, locale);
            actualChanges.put(entityTypeParamName, newValue);
            this.entityType = newValue;
        }

        if (command.isChangeInIntegerParameterNamed(attributeNameParamName, this.attributeName, locale)) {
            final Integer newValue = command.integerValueOfParameterNamed(attributeNameParamName, locale);
            actualChanges.put(attributeNameParamName, newValue);
            this.attributeName = newValue;
        }

        if (command.isChangeInIntegerParameterNamed(conditionTypeParamName, this.conditionType, locale)) {
            final Integer newValue = command.integerValueOfParameterNamed(conditionTypeParamName, locale);
            actualChanges.put(conditionTypeParamName, newValue);
            this.conditionType = newValue;
        }

        if (command.isChangeInStringParameterNamed(attributeValueParamName, this.attributeValue)) {
            final String newValue = command.stringValueOfParameterNamed(attributeValueParamName);
            actualChanges.put(attributeValueParamName, newValue);
            this.attributeValue = newValue;
        }

        if (command.isChangeInIntegerParameterNamed(incentiveTypeparamName, this.incentiveType, locale)) {
            final Integer newValue = command.integerValueOfParameterNamed(incentiveTypeparamName, locale);
            actualChanges.put(incentiveTypeparamName, newValue);
            this.incentiveType = newValue;
        }

        if (command.isChangeInBigDecimalParameterNamed(amountParamName, this.amount, locale)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(amountParamName, locale);
            actualChanges.put(amountParamName, newValue);
            this.amount = newValue;
        }

        validateIncentiveData(baseDataValidator);

    }

    public void validateIncentiveData(final DataValidatorBuilder baseDataValidator) {

        switch (attributeName()) {
            case GENDER:
                baseDataValidator.reset().parameter(attributeValueParamName).value(this.attributeValue).longGreaterThanZero();
                baseDataValidator.reset().parameter(conditionTypeParamName).value(this.conditionType)
                        .isOneOfTheseValues(ConditionType.EQUAL.getValue(), ConditionType.NOT_EQUAL.getValue());
            break;
            case AGE:
                baseDataValidator.reset().parameter(attributeValueParamName).value(this.attributeValue).longGreaterThanZero();
            break;
            case CLIENT_CLASSIFICATION:
                baseDataValidator.reset().parameter(attributeValueParamName).value(this.attributeValue).longGreaterThanZero();
                baseDataValidator.reset().parameter(conditionTypeParamName).value(this.conditionType)
                        .isOneOfTheseValues(ConditionType.EQUAL.getValue(), ConditionType.NOT_EQUAL.getValue());
            break;
            case CLIENT_TYPE:
                baseDataValidator.reset().parameter(attributeValueParamName).value(this.attributeValue).longGreaterThanZero();
                baseDataValidator.reset().parameter(conditionTypeParamName).value(this.conditionType)
                        .isOneOfTheseValues(ConditionType.EQUAL.getValue(), ConditionType.NOT_EQUAL.getValue());
            break;

            default:
            break;
        }
    }

}

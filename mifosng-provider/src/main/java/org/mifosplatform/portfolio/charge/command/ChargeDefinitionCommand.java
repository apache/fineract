package org.mifosplatform.portfolio.charge.command;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;

/**
 * Immutable data object for creating and modifying defined charges.
 */
public class ChargeDefinitionCommand {

    private final String name;
    private final BigDecimal amount;
    private final String currencyCode;
    private final Integer chargeTimeType;
    private final Integer chargeAppliesTo;
    private final Integer chargeCalculationType;
    private final Boolean penalty;
    private final Boolean active;

    private final transient Set<String> parametersPassedInRequest;
    private final transient boolean makerCheckerApproval;
    private final transient Long id;

    public ChargeDefinitionCommand(final Set<String> parametersPassedInRequest, final boolean makerCheckerApproval, final Long id,
            final String name, final BigDecimal amount, final String currencyCode, final Integer chargeTimeType,
            final Integer chargeAppliesTo, final Integer chargeCalculationType, final Boolean penalty, final Boolean active) {
        this.makerCheckerApproval = makerCheckerApproval;
        this.id = id;
        this.name = name;
        this.amount = amount;
        this.currencyCode = currencyCode;
        this.chargeTimeType = chargeTimeType;
        this.chargeAppliesTo = chargeAppliesTo;
        this.chargeCalculationType = chargeCalculationType;
        this.parametersPassedInRequest = parametersPassedInRequest;
        this.penalty = penalty;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Set<String> getModifiedParameters() {
        return parametersPassedInRequest;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public Integer getChargeCalculationType() {
        return chargeCalculationType;
    }

    public Integer getChargeAppliesTo() {
        return chargeAppliesTo;
    }

    public Integer getChargeTimeType() {
        return chargeTimeType;
    }

    public boolean isPenalty() {
        return penalty;
    }

    public boolean isFee() {
        return !penalty;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isNameChanged() {
        return this.parametersPassedInRequest.contains("name");
    }

    public boolean isAmountChanged() {
        return this.parametersPassedInRequest.contains("amount");
    }

    public boolean isCurrencyCodeChanged() {
        return this.parametersPassedInRequest.contains("currencyCode");
    }

    public boolean isChargeTimeTypeChanged() {
        return this.parametersPassedInRequest.contains("chargeTimeType");
    }

    public boolean isChargeAppliesToChanged() {
        return this.parametersPassedInRequest.contains("chargeAppliesTo");
    }

    public boolean isChargeCalculationTypeChanged() {
        return this.parametersPassedInRequest.contains("chargeCalculationType");
    }

    public boolean isPenaltyChanged() {
        return this.parametersPassedInRequest.contains("penalty");
    }

    public boolean isActiveChanged() {
        return this.parametersPassedInRequest.contains("active");
    }

    public boolean isApprovedByChecker() {
        return this.makerCheckerApproval;
    }

    public void validateForCreate() {
        List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("charge");

        baseDataValidator.reset().parameter("name").value(this.name).notBlank().notExceedingLengthOf(100);
        baseDataValidator.reset().parameter("amount").value(this.amount).notNull().positiveAmount();
        baseDataValidator.reset().parameter("currencyCode").value(this.currencyCode).notBlank();
        baseDataValidator.reset().parameter("chargeAppliesTo").value(this.chargeAppliesTo).notNull().inMinMaxRange(1, 1);
        baseDataValidator.reset().parameter("chargeTimeType").value(this.chargeTimeType).notNull().inMinMaxRange(1, 2);
        baseDataValidator.reset().parameter("chargeCalculationType").value(this.chargeCalculationType).notNull().inMinMaxRange(1, 4);

        if (this.penalty && this.chargeTimeType.equals(Integer.valueOf(1))) {
            // FIXME - KW - cannot have penalty charge at time of disbursement
        }

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }

    public void validateForUpdate() {
        List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("charge");

        baseDataValidator.reset().parameter("id").value(this.id).notNull();
        baseDataValidator.reset().parameter("name").value(this.name).ignoreIfNull().notBlank().notExceedingLengthOf(100);
        baseDataValidator.reset().parameter("amount").value(this.amount).ignoreIfNull().positiveAmount();
        baseDataValidator.reset().parameter("currencyCode").value(this.currencyCode).ignoreIfNull().notBlank();
        baseDataValidator.reset().parameter("chargeAppliesTo").value(this.chargeAppliesTo).ignoreIfNull().notNull().inMinMaxRange(1, 1);
        baseDataValidator.reset().parameter("chargeTimeType").value(this.chargeTimeType).ignoreIfNull().notNull().inMinMaxRange(1, 2);
        baseDataValidator.reset().parameter("chargeCalculationType").value(this.chargeCalculationType).ignoreIfNull().notNull()
                .inMinMaxRange(1, 4);

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }
}
package org.mifosplatform.organisation.office.command;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;

/**
 * Immutable command for transferring money between two branches.
 */
public class BranchMoneyTransferCommand {

    private final Long fromOfficeId;
    private final Long toOfficeId;
    private final LocalDate transactionDate;
    private final String currencyCode;
    private final BigDecimal transactionAmount;
    private final String description;

    private final transient Set<String> parametersPassedInRequest;
    private final transient boolean makerCheckerApproval;

    public BranchMoneyTransferCommand(final Set<String> parametersPassedInRequest, final boolean makerCheckerApproval,
            final Long fromOfficeId, final Long toOfficeId, final LocalDate transactionDate, final String currencyCode,
            final BigDecimal transactionAmount, final String description) {
        this.parametersPassedInRequest = parametersPassedInRequest;
        this.makerCheckerApproval = makerCheckerApproval;
        this.fromOfficeId = fromOfficeId;
        this.toOfficeId = toOfficeId;
        this.transactionDate = transactionDate;
        this.currencyCode = currencyCode;
        this.transactionAmount = transactionAmount;
        this.description = description;
    }

    public Long getFromOfficeId() {
        return fromOfficeId;
    }

    public Long getToOfficeId() {
        return toOfficeId;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public BigDecimal getTransactionAmount() {
        return transactionAmount;
    }

    public String getDescription() {
        return description;
    }

    public boolean isFromOfficeIdChanged() {
        return this.parametersPassedInRequest.contains("fromOfficeId");
    }

    public boolean isApprovedByChecker() {
        return this.makerCheckerApproval;
    }

    public void validateBranchTransfer() {
        List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("office.money.transfer");

        baseDataValidator.reset().parameter("fromOfficeId").value(this.fromOfficeId).ignoreIfNull().integerGreaterThanZero();
        baseDataValidator.reset().parameter("toOfficeId").value(this.toOfficeId).ignoreIfNull().integerGreaterThanZero();

        if (this.fromOfficeId == null && this.toOfficeId == null) {
            baseDataValidator.reset().parameter("toOfficeId").value(this.toOfficeId).notNull();
        }

        if (this.fromOfficeId != null && this.toOfficeId != null) {
            baseDataValidator.reset().parameter("fromOfficeId").value(this.fromOfficeId).notSameAsParameter("toOfficeId", this.toOfficeId);
        }

        baseDataValidator.reset().parameter("transactionDate").value(this.transactionDate).notNull();
        baseDataValidator.reset().parameter("currencyCode").value(this.currencyCode).notBlank();
        baseDataValidator.reset().parameter("transactionAmount").value(this.transactionAmount).notNull();
        baseDataValidator.reset().parameter("description").value(this.description).notExceedingLengthOf(100);

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }
}
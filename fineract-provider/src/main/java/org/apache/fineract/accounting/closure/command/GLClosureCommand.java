/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.accounting.closure.command;

import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.accounting.closure.api.GLClosureJsonInputParams;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.joda.time.LocalDate;

/**
 * Immutable command for adding an accounting closure
 */
public class GLClosureCommand {

    @SuppressWarnings("unused")
    private final Long id;
    private final Long officeId;
    private final LocalDate closingDate;
    private final String comments;
    private final Boolean bookOffIncomeAndExpense;
    private final Long equityGlAccountId;
    private final String currencyCode;
    private final Boolean reverseIncomeAndExpenseBooking;
    private final Boolean subBranches;
    private final String incomeAndExpenseComments;

    public GLClosureCommand(final Long id, final Long officeId, final LocalDate closingDate, final String comments,
            final Boolean bookOffIncomeAndExpense, final Long equityGlAccountId,
            final String currencyCode,final Boolean reverseIncomeAndExpenseBooking,
            final Boolean subBranches,final String incomeAndExpenseComments
            )
    {
        this.id = id;
        this.officeId = officeId;
        this.closingDate = closingDate;
        this.comments = comments;
        this.bookOffIncomeAndExpense = bookOffIncomeAndExpense == null ? false : bookOffIncomeAndExpense;
        this.equityGlAccountId = equityGlAccountId;
        this.currencyCode = currencyCode;
        this.reverseIncomeAndExpenseBooking = reverseIncomeAndExpenseBooking == null ? false : reverseIncomeAndExpenseBooking;
        this.subBranches = subBranches == null ? false : subBranches;
        this.incomeAndExpenseComments = incomeAndExpenseComments;
    }

    public void validateForCreate() {

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("GLClosure");

        baseDataValidator.reset().parameter(GLClosureJsonInputParams.CLOSING_DATE.getValue()).value(this.closingDate).notBlank();
        baseDataValidator.reset().parameter(GLClosureJsonInputParams.OFFICE_ID.getValue()).value(this.officeId).notNull()
                .integerGreaterThanZero();
        baseDataValidator.reset().parameter(GLClosureJsonInputParams.COMMENTS.getValue()).value(this.comments).ignoreIfNull()
                .notExceedingLengthOf(500);
        if(this.bookOffIncomeAndExpense){
            baseDataValidator.reset().parameter(GLClosureJsonInputParams.EQUITY_GL_ACCOUNT_ID.getValue()).value(this.equityGlAccountId).notNull();
            baseDataValidator.reset().parameter(GLClosureJsonInputParams.CURRENCY_CODE.getValue()).value(this.currencyCode).notNull();
            baseDataValidator.reset().parameter(GLClosureJsonInputParams.COMMENTS.getValue()).value(this.incomeAndExpenseComments).ignoreIfNull()
                    .notExceedingLengthOf(500);
        }

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }

    public void validateForUpdate() {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("GLClosure");

        baseDataValidator.reset().parameter(GLClosureJsonInputParams.COMMENTS.getValue()).value(this.comments).ignoreIfNull()
                .notExceedingLengthOf(500);
        baseDataValidator.reset().anyOfNotNull(this.comments);

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }

    public Boolean getBookOffIncomeAndExpense() {
        return this.bookOffIncomeAndExpense;
    }

    public Long getEquityGlAccountId() {
        return this.equityGlAccountId;
    }

    public String getCurrencyCode() {
        return this.currencyCode;
    }

    public Boolean getSubBranches() {
        return this.subBranches;
    }

    public Boolean getReverseIncomeAndExpenseBooking() {
        return this.reverseIncomeAndExpenseBooking;
    }

    public String getIncomeAndExpenseComments() {
        return this.incomeAndExpenseComments;
    }

    public Long getOfficeId() {
        return this.officeId;
    }

    public LocalDate getClosingDate() {
        return this.closingDate;
    }

    public String getComments() {
        return this.comments;
    }

}
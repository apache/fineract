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
package org.apache.fineract.portfolio.loanaccount.command;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;

/**
 * Immutable data object for updating relationship between loan officer and a loan.
 */
public class UpdateLoanOfficerCommand {

    private final Long fromLoanOfficerId;
    private final Long toLoanOfficerId;
    private final LocalDate assignmentDate;

    private final String[] loans;

    public UpdateLoanOfficerCommand(final Long fromLoanOfficerId, final Long toLoanOfficerId, final LocalDate assignmentDate) {
        this.fromLoanOfficerId = fromLoanOfficerId;
        this.toLoanOfficerId = toLoanOfficerId;
        this.assignmentDate = assignmentDate;
        this.loans = null;
    }

    public UpdateLoanOfficerCommand(final Long fromLoanOfficerId, final Long toLoanOfficerId, final LocalDate assignmentDate,
            final String[] loans) {
        this.fromLoanOfficerId = fromLoanOfficerId;
        this.toLoanOfficerId = toLoanOfficerId;
        this.assignmentDate = assignmentDate;
        this.loans = loans;
    }

    public void validateForBulkLoanReassignment() {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loans.reassignment");

        baseDataValidator.reset().parameter("fromLoanOfficerId").value(this.fromLoanOfficerId).notNull().integerGreaterThanZero();
        baseDataValidator.reset().parameter("toLoanOfficerId").value(this.toLoanOfficerId).notNull().integerGreaterThanZero()
                .notSameAsParameter("fromLoanOfficerId", this.fromLoanOfficerId);

        baseDataValidator.reset().parameter("assignmentDate").value(this.assignmentDate).notNull();

        baseDataValidator.reset().parameter("loans").value(this.loans).arrayNotEmpty();

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
    }

    public void validateForLoanReassignment() {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loans.reassignment");

        baseDataValidator.reset().parameter("toLoanOfficerId").value(this.toLoanOfficerId).notNull().integerGreaterThanZero()
                .notSameAsParameter("fromLoanOfficerId", this.fromLoanOfficerId);

        baseDataValidator.reset().parameter("assignmentDate").value(this.assignmentDate).notNull();

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
    }
}

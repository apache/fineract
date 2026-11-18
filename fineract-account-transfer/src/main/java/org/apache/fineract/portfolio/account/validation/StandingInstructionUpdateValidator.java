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
package org.apache.fineract.portfolio.account.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.fineract.portfolio.account.data.request.StandingInstructionUpdateRequest;

/**
 * Rejects a {@code validFrom}, {@code validTill} or {@code recurrenceOnMonthDay} that is present in an update request
 * but does not parse. Without it the unparseable value reads as "not sent" and the update answers 200 without applying
 * it.
 */
public class StandingInstructionUpdateValidator
        implements ConstraintValidator<ValidStandingInstructionUpdate, StandingInstructionUpdateRequest> {

    private static final String MSG_PREFIX = "{org.apache.fineract.portfolio.account.standinginstruction.";

    @Override
    public boolean isValid(final StandingInstructionUpdateRequest request, final ConstraintValidatorContext context) {
        if (request == null) {
            return true;
        }
        context.disableDefaultConstraintViolation();

        boolean valid = true;
        if (request.getValidFrom() != null && request.validFromAsDate() == null) {
            valid = violation(context, MSG_PREFIX + "valid-from.invalid}", "validFrom");
        }
        if (request.getValidTill() != null && request.validTillAsDate() == null) {
            valid = violation(context, MSG_PREFIX + "valid-till.invalid}", "validTill");
        }
        if (request.getRecurrenceOnMonthDay() != null && request.recurrenceOnMonthDayAsMonthDay() == null) {
            valid = violation(context, MSG_PREFIX + "recurrence-on-month-day.invalid}", "recurrenceOnMonthDay");
        }
        return valid;
    }

    private static boolean violation(final ConstraintValidatorContext context, final String template, final String property) {
        context.buildConstraintViolationWithTemplate(template).addPropertyNode(property).addConstraintViolation();
        return false;
    }
}

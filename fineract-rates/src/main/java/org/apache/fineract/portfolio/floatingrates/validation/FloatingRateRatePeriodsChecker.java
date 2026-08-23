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
package org.apache.fineract.portfolio.floatingrates.validation;

import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.floatingrates.data.FloatingRatePeriodRequest;

/**
 * Rate-period rules shared by the create and update floating rate constraints.
 */
final class FloatingRateRatePeriodsChecker {

    private FloatingRateRatePeriodsChecker() {}

    static boolean checkRatePeriods(final List<FloatingRatePeriodRequest> ratePeriods, final boolean isBaseLendingRate,
            final boolean baseLendingRateAvailable, final ConstraintValidatorContext context) {
        if (ratePeriods == null || ratePeriods.isEmpty()) {
            return true;
        }
        boolean valid = true;
        final List<LocalDate> fromDates = new ArrayList<>();
        for (int i = 0; i < ratePeriods.size(); i++) {
            final FloatingRatePeriodRequest ratePeriod = ratePeriods.get(i);
            if (StringUtils.isNotBlank(ratePeriod.getFromDate())) {
                LocalDate fromDate = null;
                try {
                    fromDate = ratePeriod.fromDateAsLocalDate();
                } catch (final DateTimeParseException e) {
                    addRatePeriodViolation(context, "{org.apache.fineract.portfolio.floatingrate.from-date.invalid}", "fromDate", i);
                    valid = false;
                }
                if (fromDate != null) {
                    if (!DateUtils.isAfter(fromDate, DateUtils.getBusinessLocalDate())) {
                        addRatePeriodViolation(context, "{org.apache.fineract.portfolio.floatingrate.from-date.future}", "fromDate", i);
                        valid = false;
                    }
                    fromDates.add(fromDate);
                }
            }
            if (Boolean.TRUE.equals(ratePeriod.getIsDifferentialToBaseLendingRate())) {
                if (!baseLendingRateAvailable) {
                    addRatePeriodViolation(context,
                            "{org.apache.fineract.portfolio.floatingrate.differential-to-base-lending-rate.not-available}",
                            "isDifferentialToBaseLendingRate", i);
                    valid = false;
                }
                if (isBaseLendingRate) {
                    addRatePeriodViolation(context,
                            "{org.apache.fineract.portfolio.floatingrate.differential-to-base-lending-rate.invalid-for-base-lending-rate}",
                            "isDifferentialToBaseLendingRate", i);
                    valid = false;
                }
            }
        }
        if (fromDates.size() != new HashSet<>(fromDates).size()) {
            context.buildConstraintViolationWithTemplate("{org.apache.fineract.portfolio.floatingrate.from-date.duplicate}")
                    .addPropertyNode("ratePeriods").addConstraintViolation();
            valid = false;
        }
        return valid;
    }

    private static void addRatePeriodViolation(final ConstraintValidatorContext context, final String template, final String property,
            final int index) {
        context.buildConstraintViolationWithTemplate(template).addPropertyNode("ratePeriods").addPropertyNode(property).inIterable()
                .atIndex(index).addConstraintViolation();
    }
}

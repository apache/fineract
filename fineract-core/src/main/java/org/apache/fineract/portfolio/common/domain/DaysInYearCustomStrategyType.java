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
package org.apache.fineract.portfolio.common.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.api.ApiFacingEnum;

/**
 * Defines the method of leap year calculation for determining the number of days in a year.
 * <p>
 * This enum has two modes:
 * </p>
 * <ul>
 * <li>{@link #FULL_LEAP_YEAR} - Always considers 366 days in a leap year.</li>
 * <li>{@link #FEB_29_PERIOD_ONLY} - Considers 366 days only if the period includes February 29th; otherwise, uses 365
 * days.</li>
 * </ul>
 * <p>
 * When using {@link DaysInYearType#ACTUAL}, the {@code FEB_29_PERIOD_ONLY} rule applies:
 * </p>
 * <ul>
 * <li>If the period includes February 29th, the calculation considers 366 days.</li>
 * <li>Otherwise, even if the year is a leap year, the calculation uses 365 days.</li>
 * </ul>
 * <p>
 * <b>Examples:</b>
 * </p>
 * <ul>
 * <li><b>366 Days:</b> A period from February 10th to March 10th in a leap year will consider 366 days, since February
 * 29th is within the period.</li>
 * <li><b>365 Days:</b> A period from March 10th to April 10th in a leap year will consider 365 days, as February 29th
 * is not within the period.</li>
 * </ul>
 */
@Getter
@RequiredArgsConstructor
public enum DaysInYearCustomStrategyType implements ApiFacingEnum<DaysInYearCustomStrategyType> {

    /** Always considers 366 days in a leap year. */
    FULL_LEAP_YEAR("DaysInYearCustomStrategyType.fullLeapYear", "Full Leap Year"), //

    /** Considers 366 days only if the period includes February 29th; otherwise, uses 365 days. */
    FEB_29_PERIOD_ONLY("DaysInYearCustomStrategyType.feb29PeriodOnly", "Feb 29 Period Only"); //

    private final String code;
    private final String humanReadableName;

}

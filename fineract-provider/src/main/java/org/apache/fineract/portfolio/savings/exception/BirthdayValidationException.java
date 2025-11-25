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
package org.apache.fineract.portfolio.savings.exception;


import org.apache.fineract.infrastructure.core.exception.UnrecognizedQueryParamException;


public class BirthdayValidationException extends UnrecognizedQueryParamException {

    private BirthdayValidationException(final String queryParamKey, final String queryParamValue,  final Object[] supportedParams) {
        super(queryParamKey,  queryParamValue, supportedParams);
    }

    public static BirthdayValidationException invalidMonth(String month) {
        return new BirthdayValidationException(
            "birthMonth", month, new Object[] { "birthMonth must be a integer between 1 to 12" });
    }

    public static BirthdayValidationException invalidDay(String day) {
        return new BirthdayValidationException(
            "birthDay", day, new Object[] { "birthDay must be an integer greater than 0" }
        );
    }

    public static BirthdayValidationException invalidDayForMonth(String month, String day, String maxDay) {
        return new BirthdayValidationException(
            "birthDay", day, new Object[] { "birthDay for month " + month + " must be between 1 and " + maxDay }
        );
    }

    public static BirthdayValidationException incomplete(String missingField) {
        return new BirthdayValidationException(
            missingField, "", new Object[] {"Both birthMonth and birthDay must be provided together"}
        );
    }
}
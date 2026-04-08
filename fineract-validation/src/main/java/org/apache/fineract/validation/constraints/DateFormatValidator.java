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
package org.apache.fineract.validation.constraints;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.format.DateTimeFormatter;
import org.apache.commons.lang3.StringUtils;

public class DateFormatValidator implements ConstraintValidator<DateFormat, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (StringUtils.isBlank(value)) {
            return true; // blank is allowed; use @NotBlank if required
        }
        return isValidPattern(value);
    }

    /**
     * Checks whether the given string is a valid {@link DateTimeFormatter} pattern. Can be used by validators that are
     * not annotation-based (e.g. when validating JSON commands that do not bind to a DTO).
     *
     * @param pattern
     *            the candidate pattern string
     * @return {@code true} if the pattern is valid, {@code false} otherwise
     */
    public static boolean isValidPattern(String pattern) {
        if (StringUtils.isBlank(pattern)) {
            return true;
        }
        try {
            DateTimeFormatter.ofPattern(pattern);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}

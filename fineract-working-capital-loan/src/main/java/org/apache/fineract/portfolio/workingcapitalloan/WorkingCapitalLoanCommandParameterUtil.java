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
package org.apache.fineract.portfolio.workingcapitalloan;

import java.util.Locale;
import org.apache.commons.lang3.StringUtils;

/**
 * Resolves the Working Capital Loan {@code command} / {@code templateType} query parameter regardless of the naming
 * convention the caller uses: {@code chargeoff}, {@code chargeOff}, {@code charge-off} and {@code CHARGEOFF} all select
 * the same command. Only the comparison is normalized; the incoming value itself is never rewritten.
 */
public final class WorkingCapitalLoanCommandParameterUtil {

    private WorkingCapitalLoanCommandParameterUtil() {}

    public static boolean is(final String commandParam, final String commandValue) {
        final String normalizedParam = normalize(commandParam);
        return !normalizedParam.isEmpty() && normalizedParam.equals(normalize(commandValue));
    }

    // Locale.ROOT keeps the result stable on JVMs with a non-English default locale (e.g. the Turkish dotless i)
    static String normalize(final String value) {
        return value == null ? "" : StringUtils.remove(value.trim(), '-').toLowerCase(Locale.ROOT);
    }
}

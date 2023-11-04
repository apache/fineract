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
package org.apache.fineract.portfolio.savings;

import java.util.Arrays;

/**
 * An enumeration of supported calendar periods used in savings.
 */
public enum PreClosurePenalInterestOnType {

    INVALID(0, "preClosurePenalInterestOnType.invalid"), WHOLE_TERM(1, "preClosurePenalInterestOnType.wholeTerm"), //
    TILL_PREMATURE_WITHDRAWAL(2, "preClosurePenalInterestOnType.tillPrematureWithdrawal"); //

    private final Integer value;
    private final String code;

    PreClosurePenalInterestOnType(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public static PreClosurePenalInterestOnType fromInt(final Integer v) {
        if (v == null) {
            return INVALID;
        }

        switch (v) {
            case 1:
                return WHOLE_TERM;
            case 2:
                return TILL_PREMATURE_WITHDRAWAL;
            default:
                return INVALID;
        }
    }

    // TODO: do we really need this?!?
    public static Object[] integerValues() {
        return Arrays.stream(values()).filter(value -> !INVALID.equals(value)).map(value -> value.value).toList().toArray();
    }

    // TODO: why not just use the enum values... just more boilerplate code here!!
    public boolean isInvalid() {
        return this.equals(INVALID);
    }

    // TODO: why not just use the enum values... just more boilerplate code here!!
    public boolean isWholeTerm() {
        return this.equals(WHOLE_TERM);
    }

    // TODO: why not just use the enum values... just more boilerplate code here!!
    public boolean isTillPrematureWithdrawal() {
        return this.equals(TILL_PREMATURE_WITHDRAWAL);
    }
}

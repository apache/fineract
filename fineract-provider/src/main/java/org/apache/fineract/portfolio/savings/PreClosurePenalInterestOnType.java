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

import java.util.ArrayList;
import java.util.List;

/**
 * An enumeration of supported calendar periods used in savings.
 */
public enum PreClosurePenalInterestOnType {
    INVALID(0, "preClosurePenalInterestOnType.invalid"), WHOLE_TERM(1, "preClosurePenalInterestOnType.wholeTerm"), //
    TILL_PREMATURE_WITHDRAWAL(2, "preClosurePenalInterestOnType.tillPrematureWithdrawal"); //

    private final Integer value;
    private final String code;

    private PreClosurePenalInterestOnType(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public static PreClosurePenalInterestOnType fromInt(final Integer type) {
        PreClosurePenalInterestOnType penalInterestType = PreClosurePenalInterestOnType.INVALID;
        if (type != null) {
            switch (type) {
                case 1:
                    penalInterestType = PreClosurePenalInterestOnType.WHOLE_TERM;
                break;
                case 2:
                    penalInterestType = PreClosurePenalInterestOnType.TILL_PREMATURE_WITHDRAWAL;
                break;
            }
        }
        return penalInterestType;
    }

    public static Object[] integerValues() {
        final List<Integer> values = new ArrayList<>();
        for (final PreClosurePenalInterestOnType enumType : values()) {
            if (enumType.getValue() > 0) {
                values.add(enumType.getValue());
            }
        }

        return values.toArray();
    }

    public boolean isInvalid() {
        return this.value.equals(PreClosurePenalInterestOnType.INVALID.value);
    }

    public boolean isWholeTerm() {
        return this.value.equals(PreClosurePenalInterestOnType.WHOLE_TERM.getValue());
    }

    public boolean isTillPrematureWithdrawal() {
        return this.value.equals(PreClosurePenalInterestOnType.TILL_PREMATURE_WITHDRAWAL.getValue());
    }
}

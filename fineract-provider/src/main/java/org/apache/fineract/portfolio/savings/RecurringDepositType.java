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
public enum RecurringDepositType {
    INVALID(0, "recurringDepositType.invalid"), VOLUNTARY(1, "recurringDepositType.voluntary"), //
    MANDATORY(2, "recurringDepositType.mandatory"); //

    private final Integer value;
    private final String code;

    private RecurringDepositType(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public static RecurringDepositType fromInt(final Integer type) {
        RecurringDepositType rdType = RecurringDepositType.INVALID;
        if (type != null) {
            switch (type) {
                case 1:
                    rdType = RecurringDepositType.VOLUNTARY;
                break;
                case 2:
                    rdType = RecurringDepositType.MANDATORY;
                break;
            }
        }
        return rdType;
    }

    public static Object[] integerValues() {
        final List<Integer> values = new ArrayList<>();
        for (final RecurringDepositType enumType : values()) {
            if (enumType.getValue() > 0) {
                values.add(enumType.getValue());
            }
        }

        return values.toArray();
    }

    public boolean isInvalid() {
        return this.value.equals(RecurringDepositType.INVALID.value);
    }
}

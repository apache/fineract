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
package org.apache.fineract.infrastructure.dataqueries.data;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;

public enum StatusEnum {

    CREATE("statusEnum.create", 100), //
    APPROVE("statusEnum.approve", 200), //
    ACTIVATE("statusEnum.activate", 300), //
    WITHDRAWN("statusEnum.withdraw", 400), //
    REJECTED("statusEnum.reject", 500), //
    CLOSE("statusEnum.close", 600), //
    WRITE_OFF("statusEnum.write off", 601), //
    RESCHEDULE("statusEnum.reschedule", 602), //
    OVERPAY("statusEnum.overpay", 700), //
    DISBURSE("statusEnum.disburse", 800), //
    ;

    private static final StatusEnum[] VALUES = values();

    private static final Map<Integer, StatusEnum> BY_ID = Arrays.stream(VALUES).collect(Collectors.toMap(StatusEnum::getValue, v -> v));

    private final String code;

    private final Integer value;

    public Integer getValue() {
        return value;
    }

    StatusEnum(String code, Integer value) {
        this.code = code;
        this.value = value;
    }

    public static StatusEnum fromInt(final Integer value) {
        return BY_ID.get(value);
    }

    public static EnumOptionData toEnumOptionData(final Integer id) {
        return toEnumOptionData(StatusEnum.fromInt(id));
    }

    public static EnumOptionData toEnumOptionData(final StatusEnum statusType) {
        return statusType == null ? null : statusType.toEnumOptionData();
    }

    public EnumOptionData toEnumOptionData() {
        return new EnumOptionData(getValue().longValue(), code, name());
    }
}

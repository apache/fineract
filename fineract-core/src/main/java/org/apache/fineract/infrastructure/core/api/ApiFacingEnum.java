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
package org.apache.fineract.infrastructure.core.api;

import java.util.Arrays;
import java.util.List;
import org.apache.fineract.infrastructure.core.data.StringEnumOptionData;

public interface ApiFacingEnum<T extends Enum<T> & ApiFacingEnum<T>> {

    static <T extends Enum<T> & ApiFacingEnum<T>> List<StringEnumOptionData> getValuesAsStringEnumOptionDataList(Class<T> clazz) {
        return Arrays.stream(clazz.getEnumConstants()).map(v -> new StringEnumOptionData(v.name(), v.getCode(), v.getHumanReadableName()))
                .toList();
    }

    static <T extends Enum<T> & ApiFacingEnum<T>> StringEnumOptionData getStringEnumOptionData(Class<T> clazz, String name) {
        return name == null || name.trim().isEmpty() ? null : Enum.valueOf(clazz, name).getValueAsStringEnumOptionData();
    }

    default StringEnumOptionData getValueAsStringEnumOptionData() {
        return new StringEnumOptionData(((Enum<T>) this).name(), getCode(), getHumanReadableName());
    }

    String getCode();

    String getHumanReadableName();
}

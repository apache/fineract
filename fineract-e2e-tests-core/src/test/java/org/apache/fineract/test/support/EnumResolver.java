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
package org.apache.fineract.test.support;

import java.util.Arrays;
import java.util.function.Function;

public final class EnumResolver {

    private EnumResolver() {}

    public static <T extends Enum<T>> T from(Class<T> clazz, String str, Function<T, String> fn) {
        return Arrays.stream(clazz.getEnumConstants()).filter(c -> fn.apply(c).equals(str)).findAny()
                .orElseThrow(() -> new IllegalArgumentException("Enum not found for string [%s]".formatted(str)));
    }

    public static <T extends Enum<T>> T fromString(Class<T> clazz, String str) {
        return Enum.valueOf(clazz, str.trim().toUpperCase());
    }
}

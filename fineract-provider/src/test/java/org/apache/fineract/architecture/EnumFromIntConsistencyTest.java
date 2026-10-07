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
package org.apache.fineract.architecture;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Every enum exposing a static {@code fromInt} factory and a {@code getValue()} accessor must round-trip:
 * {@code fromInt(c.getValue()) == c} for each constant.
 */
class EnumFromIntConsistencyTest {

    private static final String BASE_PACKAGE = "org.apache.fineract";

    private static final Set<Class<?>> INT_TYPES = Set.of(Integer.class, int.class);

    static Stream<Class<?>> enumsWithFromInt() {
        try (ScanResult scanResult = new ClassGraph().enableClassInfo().acceptPackages(BASE_PACKAGE).scan()) {
            return scanResult.getAllEnums().loadClasses().stream() //
                    .filter(type -> findFromInt(type) != null && findGetValue(type) != null) //
                    .toList().stream();
        }
    }

    @Test
    void scanFindsEnums() {
        assertThat(enumsWithFromInt()).isNotEmpty();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("enumsWithFromInt")
    void fromIntReturnsTheConstantOwningTheValue(Class<?> enumType) throws ReflectiveOperationException {
        Method fromInt = findFromInt(enumType);
        Method getValue = findGetValue(enumType);
        fromInt.setAccessible(true);
        getValue.setAccessible(true);

        List<String> mismatches = new ArrayList<>();
        for (Object constant : enumType.getEnumConstants()) {
            Object value = getValue.invoke(constant);
            Object resolved = fromInt.invoke(null, value);
            if (resolved != constant) {
                mismatches.add(constant + "(" + value + ") -> " + resolved);
            }
        }
        assertThat(mismatches).as("fromInt mismatches in %s", enumType.getName()).isEmpty();
    }

    private static Method findFromInt(Class<?> enumType) {
        return Arrays.stream(enumType.getDeclaredMethods()) //
                .filter(method -> "fromInt".equals(method.getName()) && Modifier.isStatic(method.getModifiers())) //
                .filter(method -> method.getReturnType() == enumType) //
                .filter(method -> method.getParameterCount() == 1 && INT_TYPES.contains(method.getParameterTypes()[0])) //
                .findFirst().orElse(null);
    }

    private static Method findGetValue(Class<?> enumType) {
        return Arrays.stream(enumType.getMethods()) //
                .filter(method -> "getValue".equals(method.getName()) && method.getParameterCount() == 0) //
                .filter(method -> INT_TYPES.contains(method.getReturnType())) //
                .findFirst().orElse(null);
    }
}

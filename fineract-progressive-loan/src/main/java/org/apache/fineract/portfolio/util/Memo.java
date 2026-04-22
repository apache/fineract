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
package org.apache.fineract.portfolio.util;

import java.util.function.Supplier;

/**
 * Memo (Object value cache) for calculations
 *
 */
public final class Memo<T> {

    private final Object lock = new Object();
    private final Supplier<? extends T> supplier;
    private final Supplier<Object> dependenciesGetter;
    private final boolean useReferenceCheck;

    private volatile T value;
    private volatile int[] dependencyHashCodes = new int[0];

    private Memo(Supplier<? extends T> supplier, Supplier<Object> dependenciesGetter, boolean useReferenceCheck) {
        this.supplier = supplier;
        this.dependenciesGetter = dependenciesGetter;
        this.useReferenceCheck = useReferenceCheck;
    }

    public T get() {
        Object actualDependencies = dependenciesGetter != null ? dependenciesGetter.get() : null;
        if (actualDependencies == null && value != null) {
            return value;
        }
        synchronized (lock) {
            if (checkDependencyChangedAndUpdate(actualDependencies)) {
                value = supplier.get();
            }
        }
        return value;
    }

    private boolean checkDependencyChangedAndUpdate(Object actualDependencies) {
        if (actualDependencies == null) {
            return true;
        }
        if (actualDependencies instanceof Object[] actualDependencyList) {
            boolean isSame = dependencyHashCodes.length == actualDependencyList.length;
            int[] actualDependencyHashCodes = new int[actualDependencyList.length];
            for (int i = 0; i < actualDependencyList.length; i++) {
                actualDependencyHashCodes[i] = getHashCode(actualDependencyList[i]);
                if (isSame) {
                    isSame = dependencyHashCodes[i] == actualDependencyHashCodes[i];
                }
            }
            if (!isSame) {
                dependencyHashCodes = actualDependencyHashCodes;
            }
            return !isSame;
        } else {
            final int[] actualDependencyHashCodes = { getHashCode(actualDependencies) };
            final boolean isSame = dependencyHashCodes.length == actualDependencyHashCodes.length
                    && dependencyHashCodes[0] == actualDependencyHashCodes[0];
            if (!isSame) {
                dependencyHashCodes = actualDependencyHashCodes;
                return true;
            }
        }
        return false;
    }

    private int getHashCode(Object dependency) {
        if (dependency == null) {
            return 0;
        }
        return useReferenceCheck ? System.identityHashCode(dependency) : dependency.hashCode();
    }

    public static <T> Memo<T> of(Supplier<? extends T> supplier) {
        return new Memo<>(supplier, null, false);
    }

    public static <T> Memo<T> of(Supplier<? extends T> supplier, Supplier<Object> dependenciesFunction) {
        return new Memo<>(supplier, dependenciesFunction, false);
    }

    public static <T> Memo<T> of(Supplier<? extends T> supplier, Supplier<Object> dependenciesFunction, boolean useReferenceCheck) {
        return new Memo<>(supplier, dependenciesFunction, useReferenceCheck);
    }
}

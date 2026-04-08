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

import static java.lang.ThreadLocal.withInitial;

import java.util.HashMap;
import java.util.Map;

public enum TestContext {

    INSTANCE, //
    GLOBAL;//

    @SuppressWarnings("ImmutableEnumChecker")
    private final ThreadLocal<Map<String, Object>> testContexts = withInitial(HashMap::new);

    public <T> T get(String name) {
        Object storedValue = testContexts.get().get(name);
        return (T) storedValue;
    }

    public Map<String, Object> get() {
        return testContexts.get();
    }

    public <T> void set(String name, T object) {
        testContexts.get().put(name, object);
    }

    public void reset() {
        testContexts.get().clear();
    }
}

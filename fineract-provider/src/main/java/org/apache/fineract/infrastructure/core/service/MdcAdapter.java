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

package org.apache.fineract.infrastructure.core.service;

import java.util.Map;
import org.slf4j.MDC;
import org.slf4j.spi.MDCAdapter;

public class MdcAdapter implements MDCAdapter {

    @Override
    public void put(String key, String val) {
        MDC.put(key, val);
    }

    @Override
    public String get(String key) {
        return MDC.get(key);
    }

    @Override
    public void remove(String key) {
        MDC.remove(key);
    }

    @Override
    public void clear() {
        MDC.clear();
    }

    @Override
    public Map<String, String> getCopyOfContextMap() {
        return MDC.getCopyOfContextMap();
    }

    @Override
    public void setContextMap(Map<String, String> map) {
        MDC.setContextMap(map);
    }
}

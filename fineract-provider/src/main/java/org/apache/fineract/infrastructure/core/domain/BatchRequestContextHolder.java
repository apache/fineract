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
package org.apache.fineract.infrastructure.core.domain;

import java.util.Map;
import org.springframework.core.NamedThreadLocal;

public final class BatchRequestContextHolder {

    private BatchRequestContextHolder() {}

    private static final ThreadLocal<Map<String, Object>> batchAttributes = new NamedThreadLocal<>("BatchAttributesForProcessing");

    /**
     * True if the batch attributes are set
     *
     * @return true if the batch attributes are set
     */
    public static boolean isBatchRequest() {
        return batchAttributes.get() != null;
    }

    /**
     * Set the batch attributes for the current thread.
     *
     * @param requestAttributes
     *            the new batch attributes
     */
    public static void setRequestAttributes(Map<String, Object> requestAttributes) {
        batchAttributes.set(requestAttributes);
    }

    /**
     * Returns the batch attributes for the current thread.
     *
     * @return the batch attributes for the current thread, cna be null
     */
    public static Map<String, Object> getRequestAttributes() {
        return batchAttributes.get();
    }

    /**
     * Reset the batch attributes for the current thread.
     */
    public static void resetRequestAttributes() {
        batchAttributes.remove();
    }
}

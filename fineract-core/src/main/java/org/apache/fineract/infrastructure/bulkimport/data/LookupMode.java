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
package org.apache.fineract.infrastructure.bulkimport.data;

/**
 * Controls whether a bulk-import download template embeds the tenant-wide lookup sheets (clients, offices) and their
 * in-sheet dropdown/VLOOKUP machinery. See FINERACT-2668.
 */
public enum LookupMode {

    /** Include the lookup sheets in full — every client/account is fetched (the default; unchanged behaviour). */
    INCLUDE,
    /** Never include the lookup sheets — a lean template with typed keys only (no client/account fetch). */
    EXCLUDE;

    /**
     * Maps the optional {@code includeLookups} REST query parameter to a mode: {@code null} or {@code true} →
     * {@link #INCLUDE} (the default full template), {@code false} → {@link #EXCLUDE} (lean). It is all-or-nothing —
     * either every entry is fetched or none.
     */
    public static LookupMode fromIncludeLookups(final Boolean includeLookups) {
        return includeLookups == null || includeLookups ? INCLUDE : EXCLUDE;
    }
}

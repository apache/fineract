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
package org.apache.fineract.infrastructure.codes.data;

import java.io.Serializable;

/**
 * Immutable data object representing a code.
 */
public class CodeData implements Serializable {

    private final Long id;
    @SuppressWarnings("unused")
    private final String name;
    @SuppressWarnings("unused")
    private final boolean systemDefined;

    public static CodeData instance(final Long id, final String name, final boolean systemDefined) {
        return new CodeData(id, name, systemDefined);
    }

    private CodeData(final Long id, final String name, final boolean systemDefined) {
        this.id = id;
        this.name = name;
        this.systemDefined = systemDefined;
    }

    public Long getCodeId() {
        return this.id;
    }
}
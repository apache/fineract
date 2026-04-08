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
package org.apache.fineract.interoperation.data;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.fineract.interoperation.domain.InteropIdentifier;
import org.apache.fineract.interoperation.domain.InteropIdentifierType;

@Getter
@AllArgsConstructor
public class InteropIdentifierData {

    @NotNull
    private final InteropIdentifierType idType;

    @NotNull
    private final String idValue;

    private final String subIdOrType;

    protected InteropIdentifierData(@NotNull InteropIdentifierType idType, @NotNull String idValue) {
        this(idType, idValue, null);
    }

    public static InteropIdentifierData build(InteropIdentifier identifier) {
        return new InteropIdentifierData(identifier.getType(), identifier.getValue(), identifier.getSubType());
    }
}

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
package org.apache.fineract.portfolio.fund.data;

import java.io.Serializable;
import lombok.Getter;

/**
 * Immutable data object to represent fund data.
 */
@Getter
public final class FundData implements Serializable {

    @SuppressWarnings("unused")
    private final Long id;
    @SuppressWarnings("unused")
    private final String name;
    @SuppressWarnings("unused")
    private final String externalId;

    private final Boolean isActive;

    public static FundData instance(final Long id, final String name, final String externalId, final Boolean isActive) {
        return new FundData(id, name, externalId, isActive);
    }

    private FundData(final Long id, final String name, final String externalId, final Boolean isActive) {
        this.id = id;
        this.name = name;
        this.externalId = externalId;
        this.isActive = isActive;
    }
    public String getName(){return this.name; } //for testing
}

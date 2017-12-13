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

/**
 * Immutable data object to represent fund data.
 */
public class FundData implements Serializable {

    @SuppressWarnings("unused")
    private final Long id;
    @SuppressWarnings("unused")
    private final String name;
    @SuppressWarnings("unused")
    private final String externalId;

    public static FundData instance(final Long id, final String name, final String externalId) {
        return new FundData(id, name, externalId);
    }

    private FundData(final Long id, final String name, final String externalId) {
        this.id = id;
        this.name = name;
        this.externalId = externalId;
    }

    public String getName() {
        return name;
    }

    public Long getId() {
        return id;
    }
}
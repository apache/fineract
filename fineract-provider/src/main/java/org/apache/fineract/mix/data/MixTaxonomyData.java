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
package org.apache.fineract.mix.data;

public class MixTaxonomyData {

    public static final Integer PORTFOLIO = 0;
    public static final Integer BALANCESHEET = 1;
    public static final Integer INCOME = 2;
    public static final Integer EXPENSE = 3;

    @SuppressWarnings("unused")
    private final Long id;
    private final String name;
    private final String namespace;
    private final String dimension;
    private final Integer type;
    @SuppressWarnings("unused")
    private final String description;

    public MixTaxonomyData(final Long id, final String name, final String namespace, final String dimension, final Integer type,
            final String description) {

        this.id = id;
        this.name = name;
        this.namespace = namespace;
        this.dimension = dimension;
        this.type = type;
        this.description = description;
    }

    public String getName() {
        return this.name;
    }

    public String getNamespace() {
        return this.namespace;
    }

    public String getDimension() {
        return this.dimension;
    }

    public Integer getType() {
        return this.type;
    }

    public boolean isPortfolio() {
        return this.type == 5;
    }
}
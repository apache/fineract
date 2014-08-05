/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.mix.data;

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
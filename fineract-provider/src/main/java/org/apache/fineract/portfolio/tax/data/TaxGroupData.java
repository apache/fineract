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
package org.apache.fineract.portfolio.tax.data;

import java.util.Collection;

public class TaxGroupData {

    private final Long id;
    private final String name;
    private final Collection<TaxGroupMappingsData> taxAssociations;

    // Template options
    @SuppressWarnings("unused")
    private final Collection<TaxComponentData> taxComponents;

    public static TaxGroupData instance(final Long id, final String name, final Collection<TaxGroupMappingsData> taxAssociations) {
        final Collection<TaxComponentData> taxComponents = null;
        return new TaxGroupData(id, name, taxAssociations, taxComponents);
    }

    public static TaxGroupData lookup(final Long id, final String name) {
        final Collection<TaxComponentData> taxComponents = null;
        final Collection<TaxGroupMappingsData> taxAssociations = null;
        return new TaxGroupData(id, name, taxAssociations, taxComponents);
    }

    public static TaxGroupData template(final Collection<TaxComponentData> taxComponents) {
        final Long id = null;
        final String name = null;
        final Collection<TaxGroupMappingsData> taxAssociations = null;
        return new TaxGroupData(id, name, taxAssociations, taxComponents);
    }

    public static TaxGroupData template(final TaxGroupData taxGroupData, final Collection<TaxComponentData> taxComponents) {
        return new TaxGroupData(taxGroupData.id, taxGroupData.name, taxGroupData.taxAssociations, taxComponents);
    }

    private TaxGroupData(final Long id, final String name, final Collection<TaxGroupMappingsData> taxAssociations,
            final Collection<TaxComponentData> taxComponents) {
        this.id = id;
        this.name = name;
        this.taxAssociations = taxAssociations;
        this.taxComponents = taxComponents;
    }

}

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
package org.apache.fineract.mix.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name = "mix_taxonomy_mapping")
public class MixTaxonomyMapping extends AbstractPersistableCustom<Long> {

    @Column(name = "identifier")
    private String identifier;

    @Column(name = "config")
    private String config;

    @Column(name = "currency")
    private String currency;

    protected MixTaxonomyMapping() {
        // default
    }

    private MixTaxonomyMapping(final String identifier, final String config, final String currency) {
        this.identifier = StringUtils.defaultIfEmpty(identifier, null);
        this.config = StringUtils.defaultIfEmpty(config, null);
        this.currency = StringUtils.defaultIfEmpty(currency, null);
    }

    public static MixTaxonomyMapping fromJson(final JsonCommand command) {
        final String identifier = command.stringValueOfParameterNamed("identifier");
        final String config = command.stringValueOfParameterNamed("config");
        final String currency = command.stringValueOfParameterNamed("currency");
        return new MixTaxonomyMapping(identifier, config, currency);
    }

    public void update(final JsonCommand command) {

        this.identifier = command.stringValueOfParameterNamed("identifier");
        this.config = command.stringValueOfParameterNamed("config");
        this.currency = command.stringValueOfParameterNamed("currency");

    }

}

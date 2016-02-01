/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.mix.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "mix_taxonomy_mapping")
public class MixTaxonomyMapping extends AbstractPersistable<Long> {

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

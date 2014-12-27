/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.template.domain;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.springframework.data.jpa.domain.AbstractPersistable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

@Entity
@Table(name = "m_template", uniqueConstraints = {@UniqueConstraint(columnNames = {"name"}, name = "unq_name")})
public class Template extends AbstractPersistable<Long> {

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Enumerated
    @JsonSerialize(using = TemplateEntitySerializer.class)
    private TemplateEntity entity;

    @Enumerated
    @JsonSerialize(using = TemplateTypeSerializer.class)
    private TemplateType type;

    @Column(name = "text", columnDefinition = "longtext", nullable = false)
    private String text;

    @OrderBy(value = "mapperorder")
    @OneToMany(targetEntity = TemplateMapper.class, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<TemplateMapper> mappers;

    public Template(final String name, final String text,
            final TemplateEntity entity, final TemplateType type,
            final List<TemplateMapper> mappers) {
        this.name = StringUtils.defaultIfEmpty(name, null);
        this.entity = entity;
        this.type = type;
        this.text = StringUtils.defaultIfEmpty(text, null);
        this.mappers = mappers;
    }

    protected Template() {
        //
    }

    public static Template fromJson(final JsonCommand command) {
        final String name = command.stringValueOfParameterNamed("name");
        final String text = command.stringValueOfParameterNamed("text");
        final TemplateEntity entity = TemplateEntity.values()[command
                .integerValueSansLocaleOfParameterNamed("entity")];
        final int templateTypeId = command
                .integerValueSansLocaleOfParameterNamed("type");
        TemplateType type = null;
        switch (templateTypeId) {
            case 0 :
                type = TemplateType.DOCUMENT;
                break;
            case 2 :
                type = TemplateType.SMS;
                break;
        }

        final JsonArray array = command.arrayOfParameterNamed("mappers");

        final List<TemplateMapper> mappersList = new ArrayList<>();

        for (final JsonElement element : array) {
            mappersList.add(new TemplateMapper(element.getAsJsonObject()
                    .get("mappersorder").getAsInt(), element.getAsJsonObject()
                    .get("mapperskey").getAsString(), element.getAsJsonObject()
                    .get("mappersvalue").getAsString()));
        }

        return new Template(name, text, entity, type, mappersList);
    }

    public LinkedHashMap<String, String> getMappersAsMap() {
        final LinkedHashMap<String, String> map = new LinkedHashMap<>();
        for (final TemplateMapper mapper : getMappers()) {
            map.put(mapper.getMapperkey(), mapper.getMappervalue());
        }
        return map;
    }

    public List<TemplateMapper> getMappers() {
        return this.mappers;
    }

    public void setMappers(final List<TemplateMapper> mappers) {
        this.mappers = mappers;
    }

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public TemplateEntity getEntity() {
        return this.entity;
    }

    public void setEntity(final TemplateEntity entity) {
        this.entity = entity;
    }

    public TemplateType getType() {
        return this.type;
    }

    public void setType(final TemplateType type) {
        this.type = type;
    }

    public String getText() {
        return this.text;
    }

    public void setText(final String text) {
        this.text = text;
    }
}
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
package org.apache.fineract.template.domain;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javax.persistence.*;

import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

@Entity
@Table(name = "m_template", uniqueConstraints = {@UniqueConstraint(columnNames = {"name"}, name = "unq_name")})
public class Template extends AbstractPersistableCustom<Long> {

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
    @JoinTable(name="m_template_m_templatemappers",
        joinColumns={@JoinColumn(name="m_template_id", referencedColumnName="id")},
        inverseJoinColumns={@JoinColumn(name="mappers_id", referencedColumnName="id", unique=true)}
    )
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
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
package org.apache.fineract.infrastructure.hooks.data;

import org.joda.time.LocalDate;

import java.io.Serializable;
import java.util.List;

public class HookData implements Serializable {

    private final Long id;
    private final String name;
    private final String displayName;
    private final Boolean isActive;
    private final LocalDate createdAt;
    private final LocalDate updatedAt;
    private final Long templateId;
    private final String templateName;

    // associations
    private final List<Event> events;
    private final List<Field> config;

    // template data
    private final List<HookTemplateData> templates;
    private final List<Grouping> groupings;

    public static HookData instance(final Long id, final String name,
            final String displayName, final boolean isActive,
            final LocalDate createdAt, final LocalDate updatedAt,
            final Long templateId, final List<Event> registeredEvents,
            final List<Field> config, final String templateName) {
        return new HookData(id, name, displayName, isActive, createdAt,
                updatedAt, templateId, registeredEvents, config, templateName,
                null, null);
    }

    public static HookData template(final List<HookTemplateData> templates,
            final List<Grouping> groupings) {
        return new HookData(null, null, null, null, null, null, null, null,
                null, null, templates, groupings);
    }

    public static HookData templateExisting(final HookData hookData,
            final List<HookTemplateData> templates,
            final List<Grouping> groupings) {
        return new HookData(hookData.id, hookData.name, hookData.displayName,
                hookData.isActive, hookData.createdAt, hookData.updatedAt,
                hookData.templateId, hookData.events, hookData.config,
                hookData.templateName, templates, groupings);
    }

    private HookData(final Long id, final String name,
            final String displayName, final Boolean isActive,
            final LocalDate createdAt, final LocalDate updatedAt,
            final Long templateId, final List<Event> events,
            final List<Field> config, final String templateName,
            final List<HookTemplateData> templates,
            final List<Grouping> groupings) {
        this.id = id;
        this.name = name;
        this.displayName = displayName;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.templateId = templateId;
        this.templateName = templateName;

        // associations
        this.events = events;
        this.config = config;

        // template
        this.templates = templates;
        this.groupings = groupings;

    }

    public Long getHookId() {
        return this.id;
    }

    public List<HookTemplateData> getTemplates() {
        return this.templates;
    }

    public List<Grouping> getGroupings() {
        return this.groupings;
    }

    public String getTemplateName() {
        return this.name;
    }

}

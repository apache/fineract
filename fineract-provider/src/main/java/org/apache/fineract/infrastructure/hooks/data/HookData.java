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

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public final class HookData implements Serializable {

    private static final long serialVersionUID = 1L;
    private Long id;
    private String name;
    private String displayName;
    private Boolean isActive;
    private LocalDate createdAt;
    private LocalDate updatedAt;
    private Long templateId;
    private String templateName;

    // associations
    private List<Event> events;
    private List<Field> config;

    // template data
    private List<HookTemplateData> templates;
    private List<Grouping> groupings;

    public static HookData instance(final Long id, final String name, final String displayName, final boolean isActive,
            final LocalDate createdAt, final LocalDate updatedAt, final Long templateId, final List<Event> registeredEvents,
            final List<Field> config, final String templateName) {
        return new HookData().setId(id).setName(name).setDisplayName(displayName).setIsActive(isActive).setCreatedAt(createdAt)
                .setUpdatedAt(updatedAt).setTemplateId(templateId).setTemplateName(templateName).setEvents(registeredEvents)
                .setConfig(config);
    }

    public static HookData template(final List<HookTemplateData> templates, final List<Grouping> groupings) {
        return new HookData().setTemplates(templates).setGroupings(groupings);
    }

    public static HookData templateExisting(final HookData hookData, final List<HookTemplateData> templates,
            final List<Grouping> groupings) {
        return new HookData().setId(hookData.id).setName(hookData.name).setDisplayName(hookData.displayName).setIsActive(hookData.isActive)
                .setCreatedAt(hookData.createdAt).setUpdatedAt(hookData.updatedAt).setTemplateId(hookData.templateId)
                .setTemplateName(hookData.templateName).setEvents(hookData.events).setConfig(hookData.config).setTemplates(templates)
                .setGroupings(groupings);
    }
}

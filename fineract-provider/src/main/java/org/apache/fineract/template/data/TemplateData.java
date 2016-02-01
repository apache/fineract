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
package org.apache.fineract.template.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.fineract.template.domain.Template;
import org.apache.fineract.template.domain.TemplateEntity;
import org.apache.fineract.template.domain.TemplateType;

public class TemplateData {

    @SuppressWarnings("unused")
    private final List<Map<String, Object>> entities;
    @SuppressWarnings("unused")
    private final List<Map<String, Object>> types;
    @SuppressWarnings("unused")
    private final Template template;

    private TemplateData(final Template template) {
        this.template = template;
        this.entities = getEntites();
        this.types = getTypes();
    }

    public static TemplateData template(final Template template) {
        return new TemplateData(template);
    }

    public static TemplateData template() {
        return new TemplateData(null);
    }

    private List<Map<String, Object>> getEntites() {
        final List<Map<String, Object>> l = new ArrayList<>();
        for (final TemplateEntity e : TemplateEntity.values()) {
            final Map<String, Object> m = new HashMap<>();
            m.put("id", e.getId());
            m.put("name", e.getName());
            l.add(m);
        }
        return l;
    }

    private List<Map<String, Object>> getTypes() {
        final List<Map<String, Object>> l = new ArrayList<>();
        for (final TemplateType e : TemplateType.values()) {
            final Map<String, Object> m = new HashMap<>();
            m.put("id", e.getId());
            m.put("name", e.getName());
            l.add(m);
        }
        return l;
    }
}

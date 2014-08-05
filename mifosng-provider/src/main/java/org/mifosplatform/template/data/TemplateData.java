/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.template.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mifosplatform.template.domain.Template;
import org.mifosplatform.template.domain.TemplateEntity;
import org.mifosplatform.template.domain.TemplateType;

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

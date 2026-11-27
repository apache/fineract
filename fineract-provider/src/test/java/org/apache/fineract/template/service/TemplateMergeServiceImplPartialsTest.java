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
package org.apache.fineract.template.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.mustachejava.MustacheException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.template.data.TemplateData;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class TemplateMergeServiceImplPartialsTest {

    private final TemplateMergeServiceImpl service = new TemplateMergeServiceImpl(Mockito.mock(FineractProperties.class));

    @Test
    void plainVariablesStillRender() {
        final Map<String, Object> scopes = new HashMap<>();
        scopes.put("name", "Alice");

        final String result = service.compile(template("Hello {{name}}"), scopes);

        assertEquals("Hello Alice", result);
    }

    @Test
    void partialsAreRejected() {
        assertThrows(MustacheException.class, () -> service.compile(template("{{>/etc/hostname}}"), new HashMap<>()));
    }

    private static TemplateData template(final String text) {
        return TemplateData.builder().name("t").text(text).mappers(List.of()).build();
    }
}

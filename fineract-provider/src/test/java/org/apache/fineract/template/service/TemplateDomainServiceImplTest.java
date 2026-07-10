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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import org.apache.fineract.command.jdbc.store.mapping.CommandMapper;
import org.apache.fineract.template.data.TemplateData;
import org.apache.fineract.template.data.TemplateMapperData;
import org.apache.fineract.template.domain.Template;
import org.apache.fineract.template.domain.TemplateEntity;
import org.apache.fineract.template.domain.TemplateRepository;
import org.apache.fineract.template.domain.TemplateType;
import org.apache.fineract.template.mapper.TemplateMapper;
import org.apache.fineract.template.mapper.TemplateMapperDataMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TemplateDomainServiceImplTest {

    @Mock
    private CommandMapper commandMapper;

    @Mock
    private TemplateRepository templateRepository;

    @Mock
    private TemplateMapper templateMapper;

    @Mock
    private TemplateMapperDataMapper templateMapperDataMapper;

    @InjectMocks
    private TemplateDomainServiceImpl service;

    @Test
    void getAllReturnsFullTemplateDataFromMapper() {
        Template template = new Template().setName("anvay");
        List<Template> templates = List.of(template);
        List<TemplateData> mappedTemplates = List.of(templateData());
        when(templateRepository.findAll()).thenReturn(templates);
        when(templateMapper.map(templates)).thenReturn(mappedTemplates);

        List<TemplateData> response = service.getAll();

        assertThat(response).isSameAs(mappedTemplates);
        assertThat(response.getFirst().getName()).isEqualTo("anvay");
        assertThat(response.getFirst().getEntity()).isEqualTo("loan");
        assertThat(response.getFirst().getType()).isEqualTo("Document");
        assertThat(response.getFirst().getText()).isEqualTo("<p>anything</p>");
        assertThat(response.getFirst().getMappers()).hasSize(1);
    }

    @Test
    void getAllByEntityAndTypeReturnsFullTemplateDataFromMapper() {
        Template template = new Template().setName("anvay");
        List<Template> templates = List.of(template);
        List<TemplateData> mappedTemplates = List.of(templateData());
        when(templateRepository.findByEntityAndType(TemplateEntity.LOAN, TemplateType.DOCUMENT)).thenReturn(templates);
        when(templateMapper.map(templates)).thenReturn(mappedTemplates);

        List<TemplateData> response = service.getAllByEntityAndType(TemplateEntity.LOAN, TemplateType.DOCUMENT);

        assertThat(response).isSameAs(mappedTemplates);
        assertThat(response.getFirst().getName()).isEqualTo("anvay");
        assertThat(response.getFirst().getEntity()).isEqualTo("loan");
        assertThat(response.getFirst().getType()).isEqualTo("Document");
    }

    private TemplateData templateData() {
        return TemplateData.builder().id(2L).name("anvay").entity("loan").type("Document").text("<p>anything</p>")
                .mappers(List.of(TemplateMapperData.builder().mapperorder(0).mapperkey("loan")
                        .mappervalue("loans/{{loanId}}?associations=all&tenantIdentifier=default").build()))
                .build();
    }
}

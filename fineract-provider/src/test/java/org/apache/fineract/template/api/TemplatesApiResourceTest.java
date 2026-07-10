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
package org.apache.fineract.template.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.apache.fineract.command.core.CommandDispatcher;
import org.apache.fineract.template.data.TemplateData;
import org.apache.fineract.template.data.TemplateDetailsData;
import org.apache.fineract.template.data.TemplateItemData;
import org.apache.fineract.template.data.TemplateMapperData;
import org.apache.fineract.template.domain.TemplateEntity;
import org.apache.fineract.template.domain.TemplateType;
import org.apache.fineract.template.service.TemplateDomainService;
import org.apache.fineract.template.service.TemplateMergeServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TemplatesApiResourceTest {

    @Mock
    private TemplateDomainService templateService;

    @Mock
    private TemplateMergeServiceImpl templateMergeService;

    @Mock
    private CommandDispatcher dispatcher;

    @InjectMocks
    private TemplatesApiResource resource;

    @Test
    void retrieveAllTemplatesFiltersByApiTypeAndEntityIds() {
        TemplateData template = TemplateData.builder().id(3L).name("sms-template").entity("loan").type("SMS").text("hello")
                .mappers(List.of(TemplateMapperData.builder().mapperorder(0).mapperkey("loan").mappervalue("loans/{{loanId}}").build()))
                .build();
        when(templateService.getAllByEntityAndType(TemplateEntity.LOAN, TemplateType.SMS)).thenReturn(List.of(template));

        List<TemplateData> response = resource.retrieveAllTemplates(2, 1);

        verify(templateService).getAllByEntityAndType(TemplateEntity.LOAN, TemplateType.SMS);
        assertThat(response).hasSize(1);
        assertThat(response.getFirst().getId()).isEqualTo(3L);
        assertThat(response.getFirst().getName()).isEqualTo("sms-template");
        assertThat(response.getFirst().getEntity()).isEqualTo("loan");
        assertThat(response.getFirst().getType()).isEqualTo("SMS");
        assertThat(response.getFirst().getText()).isEqualTo("hello");
        assertThat(response.getFirst().getMappers()).hasSize(1);
    }

    @Test
    void retrieveTemplateByIdReturnsEditWrapperWithOptionsAndTemplate() {
        TemplateData template = template();
        when(templateService.findOneById(2L)).thenReturn(template);

        TemplateDetailsData response = resource.retrieveTemplateById(2L);

        assertThat(response.getTemplate()).isSameAs(template);
        assertThat(response.getTemplate().getEntity()).isEqualTo("loan");
        assertThat(response.getTemplate().getType()).isEqualTo("Document");
        assertThat(response.getTemplate().getMappers()).hasSize(1);
        assertThat(response.getEntities()).extracting(TemplateItemData::getName).containsExactly("client", "loan");
        assertThat(response.getTypes()).extracting(TemplateItemData::getName).containsExactly("Document", "SMS");
        assertThat(response.getTypes()).extracting(TemplateItemData::getId).containsExactly(0, 2);
    }

    @Test
    void retrieveTemplateDetailsReturnsCreateWrapperOptions() {
        TemplateDetailsData response = resource.retrieveTemplateDetails();

        assertThat(response.getTemplate()).isNull();
        assertThat(response.getEntities()).extracting(TemplateItemData::getName).containsExactly("client", "loan");
        assertThat(response.getTypes()).extracting(TemplateItemData::getName).containsExactly("Document", "SMS");
    }

    @Test
    void retrieveOneTemplateKeepsRawTemplateResponse() {
        TemplateData template = template();
        when(templateService.findOneById(2L)).thenReturn(template);

        TemplateData response = resource.retrieveOneTemplate(2L);

        assertThat(response).isSameAs(template);
        assertThat(response.getEntity()).isEqualTo("loan");
        assertThat(response.getType()).isEqualTo("Document");
    }

    private TemplateData template() {
        return TemplateData.builder().id(2L).name("anvay").entity("loan").type("Document").text("<p>anything</p>")
                .mappers(List.of(TemplateMapperData.builder().mapperorder(0).mapperkey("loan")
                        .mappervalue("loans/{{loanId}}?associations=all&tenantIdentifier=default").build()))
                .build();
    }
}

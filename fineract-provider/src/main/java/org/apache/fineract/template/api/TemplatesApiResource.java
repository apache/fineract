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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.command.core.CommandDispatcher;
import org.apache.fineract.infrastructure.core.annotation.AlternativeOperationId;
import org.apache.fineract.template.command.TemplateCreateCommand;
import org.apache.fineract.template.command.TemplateDeleteCommand;
import org.apache.fineract.template.command.TemplateUpdateCommand;
import org.apache.fineract.template.data.TemplateCreateRequest;
import org.apache.fineract.template.data.TemplateCreateResponse;
import org.apache.fineract.template.data.TemplateData;
import org.apache.fineract.template.data.TemplateDeleteRequest;
import org.apache.fineract.template.data.TemplateDeleteResponse;
import org.apache.fineract.template.data.TemplateDetailsData;
import org.apache.fineract.template.data.TemplateItemData;
import org.apache.fineract.template.data.TemplateUpdateRequest;
import org.apache.fineract.template.data.TemplateUpdateResponse;
import org.apache.fineract.template.domain.TemplateEntity;
import org.apache.fineract.template.domain.TemplateType;
import org.apache.fineract.template.service.TemplateDomainService;
import org.apache.fineract.template.service.TemplateMergeServiceImpl;
import org.springframework.stereotype.Component;

@Path("/v1/templates")
@Consumes({ MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_JSON })
@Component
@Tag(name = "templates", description = """
        User Generated Documents(alternatively, Templates) are used for end-user features such as custom user defined document generation (AKA UGD). They are based on {{ moustache }} templates. Think of them as a sort of built-in 'mail merge' functionality
        User Generated Documents (and other types of templates) can aggregate data from several Apache Fineract back-end API calls via mappers. Mappers can even access non-Apache Fineract REST services from other servers. UGDs can render such data in tables, show images, etc. TBD: Please have a look at some of the Example UGDs included in Apache Fineract (or the Wiki page, for now.).
        UGDs can be assigned to an entity like client or loan and be of a type like Document or SMS. The entity and type of a UGD is only there for the convenience of user agents (UIs), in order to know where to show UGDs for the user (i.e. which tab). The Template Engine back-end runner does not actually need this metadata.""")
@RequiredArgsConstructor
public class TemplatesApiResource {

    public static final String ID = "id";
    public static final String PARAM_TEMPLATE = "template";

    private final TemplateDomainService templateService;
    private final TemplateMergeServiceImpl templateMergeService;
    private final CommandDispatcher dispatcher;

    @GET
    @Operation(operationId = "retrieveAllTemplates", summary = "Retrieve all UGDs", description = """
            It is possible to get specific UGDs by entity and type:

            templates?type=0&entity=0

            Entity ID:

            - client: 0
            - loan: 1

            Type ID:

            - Document: 0
            - E-Mail (not yet): 1
            - SMS: 2""")
    @AlternativeOperationId("retrieveAll_40")
    public List<TemplateData> retrieveAllTemplates(
            @DefaultValue("-1") @QueryParam("typeId") @Parameter(description = "typeId") final int typeId,
            @DefaultValue("-1") @QueryParam("entityId") @Parameter(description = "entityId") final int entityId) {
        if (typeId != -1 && entityId != -1) {
            return templateService.getAllByEntityAndType(findTemplateEntity(entityId), findTemplateType(typeId));
        } else {
            return templateService.getAll();
        }
    }

    @GET
    @Path(PARAM_TEMPLATE)
    @Operation(operationId = "retrieveTemplateDetails", summary = "Retrieve UGD Details Template", description = """
            This is a convenience resource. It can be useful when building maintenance user interface screens for UGDs. The UGD data returned consists of any or all of:

            - name
            - entity
            - type
            - text
            - mappers

            Example Request:

            templates/template
            """, responses = @ApiResponse(responseCode = "default", content = @Content(schema = @Schema(implementation = TemplateData.class))))
    @AlternativeOperationId("template_20")
    public TemplateDetailsData retrieveTemplateDetails() {
        return templateDetails(null);
    }

    @GET
    @Path("{templateId}")
    @Operation(operationId = "retrieveOneTemplate", summary = "Retrieve a UGD", description = """
            Example Requests:

            - templates/1""")
    @AlternativeOperationId("retrieveOne_30")
    public TemplateData retrieveOneTemplate(@PathParam("templateId") @Parameter(description = "templateId") final Long templateId) {
        return templateService.findOneById(templateId);
    }

    @GET
    @Path("{templateId}/template")
    @Operation(operationId = "retrieveTemplateById", responses = @ApiResponse(responseCode = "default", content = @Content(schema = @Schema(implementation = TemplateData.class))))
    @AlternativeOperationId("getTemplateByTemplate")
    public TemplateDetailsData retrieveTemplateById(@PathParam("templateId") final Long templateId) {
        return templateDetails(templateService.findOneById(templateId));
    }

    @POST
    @Operation(summary = "Add a UGD", description = """
            Adds a new UGD.

            Mandatory Fields:
            - name

            Example Requests:

            - templates/1""")
    public TemplateCreateResponse createTemplate(@RequestBody(required = true) @Valid final TemplateCreateRequest request) {
        final var command = new TemplateCreateCommand();
        command.setPayload(request);

        final Supplier<TemplateCreateResponse> response = dispatcher.dispatch(command);

        return response.get();
    }

    @PUT
    @Path("{templateId}")
    @Operation(summary = "Update a UGD", description = "")
    public TemplateUpdateResponse saveTemplate(@PathParam("templateId") @Parameter(description = "templateId") final Long templateId,
            @RequestBody(required = true) @Valid final TemplateUpdateRequest request) {
        final var command = new TemplateUpdateCommand();
        command.setPayload(request);

        final Supplier<TemplateUpdateResponse> response = dispatcher.dispatch(command);

        return response.get();
    }

    @DELETE
    @Path("{templateId}")
    @Operation(summary = "Delete a UGD", description = "")
    public TemplateDeleteResponse deleteTemplate(@PathParam("templateId") @Parameter(description = "templateId") final Long templateId) {
        final var command = new TemplateDeleteCommand();
        command.setPayload(TemplateDeleteRequest.builder().id(templateId).build());

        final Supplier<TemplateDeleteResponse> response = dispatcher.dispatch(command);

        return response.get();
    }

    @POST
    @Path("{templateId}")
    @Produces({ MediaType.TEXT_HTML })
    public String mergeTemplate(@PathParam("templateId") @Parameter(description = "templateId") final Long templateId,
            @Context final UriInfo uriInfo, @RequestBody(required = true) final Map<String, Object> result) {

        var template = templateService.findOneById(templateId);

        final MultivaluedMap<String, String> parameters = uriInfo.getQueryParameters();
        final Map<String, Object> parametersMap = new HashMap<>();
        for (final Map.Entry<String, List<String>> entry : parameters.entrySet()) {
            if (entry.getValue().size() == 1) {
                parametersMap.put(entry.getKey(), entry.getValue().getFirst());
            } else {
                parametersMap.put(entry.getKey(), entry.getValue());
            }
        }

        parametersMap.put("BASE_URI", uriInfo.getBaseUri());
        parametersMap.putAll(result);

        return this.templateMergeService.compile(template, parametersMap);
    }

    private TemplateDetailsData templateDetails(final TemplateData template) {
        return TemplateDetailsData.builder().entities(retrieveEntities()).types(retrieveTypes()).template(template).build();
    }

    private List<TemplateItemData> retrieveEntities() {
        return Stream.of(TemplateEntity.values())
                .map(entity -> TemplateItemData.builder().id(entity.getId()).name(entity.getName()).build()).toList();
    }

    private List<TemplateItemData> retrieveTypes() {
        return Stream.of(TemplateType.values()).map(type -> TemplateItemData.builder().id(type.getId()).name(type.getName()).build())
                .toList();
    }

    private TemplateEntity findTemplateEntity(final int entityId) {
        return Stream.of(TemplateEntity.values()).filter(entity -> entity.getId() == entityId).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid template entity id: " + entityId));
    }

    private TemplateType findTemplateType(final int typeId) {
        return Stream.of(TemplateType.values()).filter(type -> type.getId() == typeId).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid template type id: " + typeId));
    }
}

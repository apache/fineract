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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.template.data.TemplateData;
import org.apache.fineract.template.domain.Template;
import org.apache.fineract.template.domain.TemplateEntity;
import org.apache.fineract.template.domain.TemplateType;
import org.apache.fineract.template.service.TemplateDomainService;
import org.apache.fineract.template.service.TemplateMergeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/templates")
@Consumes({ MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_JSON })
@Component
@Scope("singleton")

@Tag(name = "User Generated Documents", description = "User Generated Documents(alternatively, Templates) are used for end-user features such as custom user defined document generation (AKA UGD). They are based on {{ moustache }} templates. Think of them as a sort of built-in \"mail merge\" functionality.\n"
        + "\n"
        + "User Generated Documents (and other types of templates) can aggregate data from several Apache Fineract back-end API calls via mappers. Mappers can even access non-Apache Fineract REST services from other servers. UGDs can render such data in tables, show images, etc. TBD: Please have a look at some of the Example UGDs included in Apache Fineract (or the Wiki page, for now.).\n"
        + "\n"
        + "UGDs can be assigned to an entity like client or loan and be of a type like Document or SMS. The entity and type of a UGD is only there for the convenience of user agents (UIs), in order to know where to show UGDs for the user (i.e. which tab). The Template Engine back-end runner does not actually need this metadata.")
public class TemplatesApiResource {

    private final Set<String> responseTemplatesDataParameters = new HashSet<>(Arrays.asList("id"));
    private final Set<String> responseTemplateDataParameters = new HashSet<>(Arrays.asList("id", "entities", "types", "template"));
    private final String resourceNameForPermission = "template";

    private final PlatformSecurityContext context;
    private final DefaultToApiJsonSerializer<Template> toApiJsonSerializer;
    private final DefaultToApiJsonSerializer<TemplateData> templateDataApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final TemplateDomainService templateService;
    private final TemplateMergeService templateMergeService;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @Autowired
    public TemplatesApiResource(final PlatformSecurityContext context, final DefaultToApiJsonSerializer<Template> toApiJsonSerializer,
            final DefaultToApiJsonSerializer<TemplateData> templateDataApiJsonSerializer,
            final ApiRequestParameterHelper apiRequestParameterHelper, final TemplateDomainService templateService,
            final TemplateMergeService templateMergeService,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService) {

        this.context = context;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.templateDataApiJsonSerializer = templateDataApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.templateService = templateService;
        this.templateMergeService = templateMergeService;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
    }

    @GET
    @Operation(summary = "Retrieve all UGDs", description = "Example Requests:\n" + "\n" + "templates\n" + "\n"
            + "It is also possible to get specific UGDs by entity and type:\n" + "\n" + "templates?type=0&entity=0\n"
            + "[Entity: Id]\n\n\n\n" + "\n\n" + "client: 0, loan: 1" + "\n\n" + "[Type: Id]\n\n\n\n"
            + "Document: 0, E-Mail (not yet): 1,  SMS: 2")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = TemplatesApiResourcesSwagger.GetTemplatesResponse.class))) })
    public String retrieveAll(@DefaultValue("-1") @QueryParam("typeId") @Parameter(description = "typeId") final int typeId,
            @DefaultValue("-1") @QueryParam("entityId") @Parameter(description = "entityId") final int entityId,
            @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermission);

        // FIXME - we dont use the ORM when doing fetches - we write SQL and
        // fetch through JDBC returning data to be serialized to JSON
        List<Template> templates = new ArrayList<>();

        if (typeId != -1 && entityId != -1) {
            templates = this.templateService.getAllByEntityAndType(TemplateEntity.values()[entityId], TemplateType.values()[typeId]);
        } else {
            templates = this.templateService.getAll();
        }

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, templates, this.responseTemplatesDataParameters);
    }

    @GET
    @Path("template")
    @Operation(summary = "Retrieve UGD Details Template", description = "This is a convenience resource. It can be useful when building maintenance user interface screens for UGDs. The UGD data returned consists of any or all of:\n"
            + "\n" + "ARGUMENTS\n" + "name String entity String type String text String optional mappers Mapper optional\n"
            + "Example Request:\n" + "\n" + "templates/template")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = TemplatesApiResourcesSwagger.GetTemplatesTemplateResponse.class))) })
    public String template(@Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermission);

        final TemplateData templateData = TemplateData.template();

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.templateDataApiJsonSerializer.serialize(settings, templateData, this.responseTemplatesDataParameters);
    }

    @POST
    @Operation(summary = "Add a UGD", description = "Adds a new UGD.\n" + "\n" + "Mandatory Fields\n" + "name\n\n\n\n"
            + "Example Requests:\n" + "\n" + "templates/1")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = TemplatesApiResourcesSwagger.PostTemplatesRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = TemplatesApiResourcesSwagger.PostTemplatesResponse.class))) })
    public String createTemplate(@Parameter(hidden = true) final String apiRequestBodyAsJson) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder().createTemplate().withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @GET
    @Path("{templateId}")
    @Operation(summary = "Retrieve a UGD", description = "Example Requests:\n" + "\n" + "templates/1")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = TemplatesApiResourcesSwagger.GetTemplatesTemplateIdResponse.class))) })
    public String retrieveOne(@PathParam("templateId") @Parameter(description = "templateId") final Long templateId,
            @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermission);

        final Template template = this.templateService.findOneById(templateId);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, template, this.responseTemplatesDataParameters);
    }

    @GET
    @Path("{templateId}/template")
    public String getTemplateByTemplate(@PathParam("templateId") final Long templateId, @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermission);

        final TemplateData template = TemplateData.template(this.templateService.findOneById(templateId));

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.templateDataApiJsonSerializer.serialize(settings, template, this.responseTemplateDataParameters);
    }

    @PUT
    @Path("{templateId}")
    @Operation(summary = "Update a UGD", description = "")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = TemplatesApiResourcesSwagger.PutTemplatesTemplateIdRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = TemplatesApiResourcesSwagger.PutTemplatesTemplateIdResponse.class))) })
    public String saveTemplate(@PathParam("templateId") @Parameter(description = "templateId") final Long templateId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateTemplate(templateId).withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @DELETE
    @Path("{templateId}")
    @Operation(summary = "Delete a UGD", description = "")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = TemplatesApiResourcesSwagger.DeleteTemplatesTemplateIdResponse.class))) })
    public String deleteTemplate(@PathParam("templateId") @Parameter(description = "templateId") final Long templateId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteTemplate(templateId).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @POST
    @Path("{templateId}")
    @Produces({ MediaType.TEXT_HTML })
    public String mergeTemplate(@PathParam("templateId") final Long templateId, @Context final UriInfo uriInfo,
            final String apiRequestBodyAsJson) throws MalformedURLException, IOException {

        final Template template = this.templateService.findOneById(templateId);

        @SuppressWarnings("unchecked")
        final HashMap<String, Object> result = new ObjectMapper().readValue(apiRequestBodyAsJson, HashMap.class);

        final MultivaluedMap<String, String> parameters = uriInfo.getQueryParameters();
        final Map<String, Object> parametersMap = new HashMap<>();
        for (final Map.Entry<String, List<String>> entry : parameters.entrySet()) {

            if (entry.getValue().size() == 1) {
                parametersMap.put(entry.getKey(), entry.getValue().get(0));
            } else {
                parametersMap.put(entry.getKey(), entry.getValue());
            }
        }

        parametersMap.put("BASE_URI", uriInfo.getBaseUri());
        parametersMap.putAll(result);
        return this.templateMergeService.compile(template, parametersMap);
    }
}

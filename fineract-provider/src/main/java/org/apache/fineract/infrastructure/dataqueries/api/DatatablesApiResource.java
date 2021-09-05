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
package org.apache.fineract.infrastructure.dataqueries.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.HashMap;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.infrastructure.dataqueries.data.DatatableData;
import org.apache.fineract.infrastructure.dataqueries.data.GenericResultsetData;
import org.apache.fineract.infrastructure.dataqueries.service.GenericDataService;
import org.apache.fineract.infrastructure.dataqueries.service.ReadWriteNonCoreDataService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/datatables")
@Component
@Scope("singleton")
@Tag(name = "Data Tables", description = "The datatables API allows you to plug-in your own tables (MySql) that have a relationship to a Apache Fineract core table. For example, you might want to add some extra client fields and record information about each of the clients' family members. Via the API you can create, read, update and delete entries for each 'plugged-in' table. The API checks for permission and for 'data scoping' (only data within the users' office hierarchy can be managed by the user).\n"
        + "\n"
        + "The Apache Fineract Reference App uses a JQuery plug-in called stretchydatatables (which in turn uses this datatables resource) to provide a pretty flexible CRUD (Create, Read, Update, Delete) User Interface.")
public class DatatablesApiResource {

    private final PlatformSecurityContext context;
    private final GenericDataService genericDataService;
    private final ReadWriteNonCoreDataService readWriteNonCoreDataService;
    private final ToApiJsonSerializer<GenericResultsetData> toApiJsonSerializer;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(DatatablesApiResource.class);

    @Autowired
    public DatatablesApiResource(final PlatformSecurityContext context, final GenericDataService genericDataService,
            final ReadWriteNonCoreDataService readWriteNonCoreDataService,
            final ToApiJsonSerializer<GenericResultsetData> toApiJsonSerializer,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService) {
        this.context = context;
        this.genericDataService = genericDataService;
        this.readWriteNonCoreDataService = readWriteNonCoreDataService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List Data Tables", description = "Lists registered data tables and the Apache Fineract Core application table they are registered to.\n"
            + "\n" + "ARGUMENTS\n" + "\n" + "apptable  - optional" + "\n" + "The Apache Fineract core application table.\n" + "\n"
            + "Example Requests:\n" + "\n" + "datatables?apptable=m_client\n" + "\n" + "\n" + "datatables")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = DatatablesApiResourceSwagger.GetDataTablesResponse.class)))) })
    public String getDatatables(@QueryParam("apptable") @Parameter(description = "apptable") final String apptable,
            @Context final UriInfo uriInfo) {

        final List<DatatableData> result = this.readWriteNonCoreDataService.retrieveDatatableNames(apptable);

        final boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serializePretty(prettyPrint, result);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create Data Table", description = "Create a new data table and registers it with the Apache Fineract Core application table.\n"
            + "\n" + "Field Descriptions\n" + "\n" + "Mandatory - datatableName : \n" + "\nThe name of the Data Table.\n" + "\n"
            + "Mandatory - apptableName\n" + "\n" + "Application table name. Must be one of the following:\n" + "\n" + "m_client\n" + "\n"
            + "m_group" + "\n" + "\n" + "m_loan" + "\n" + "\n" + "m_office" + "\n" + "\n" + "m_saving_account" + "\n" + "\n"
            + "m_product_loan" + "\n" + "\n" + "m_savings_product" + "\n" + "\n" + "Mandatory - columns  " + "\n"
            + "An array of columns in the new Data Table." + "\n" + "\n" + "Optional - multiRow" + "\n" + "\n"
            + "Allows to create multiple entries in the Data Table. Optional, defaults to false. If this property is not provided Data Table will allow only one entry."
            + "\n" + "\n" + "Field Descriptions - columns" + "\n" + "\n" + "Mandatory - name" + "\n" + "\n"
            + "Name of the created column. Can contain only alphanumeric characters, underscores and spaces, but cannot start with a number. Cannot start or end with an underscore or space."
            + "\n" + "\n" + "Mandatory - type" + "\n" + "\n" + "Column type. Must be one of the following:" + "\n" + "\n" + "Boolean" + "\n"
            + "\n" + "Date" + "\n" + "\n" + "DateTime" + "\n" + "\n" + "Decimal" + "\n" + "\n" + "Dropdown" + "\n" + "\n" + "\n" + "Number"
            + "\n" + "\n" + "String" + "\n" + "\n" + "Text" + "\n" + "\n" + "Mandatory [type = Dropdown] - code" + "\n" + "\n"
            + "Used in Code description fields. Column name becomes: code_cd_name. Mandatory if using type Dropdown, otherwise an error is returned."
            + "\n" + "\n" + "Optional - mandatory" + "\n" + "\n"
            + "Determines whether this column must have a value in every entry. Optional, defaults to false." + "\n" + "\n"
            + "Mandatory [type = String] - length" + "\n" + "\n"
            + "Length of the text field. Mandatory if type String is used, otherwise an error is returned.")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = DatatablesApiResourceSwagger.PostDataTablesRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = DatatablesApiResourceSwagger.PostDataTablesResponse.class))) })
    public String createDatatable(@Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createDBDatatable(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return this.toApiJsonSerializer.serialize(result);
    }

    @PUT
    @Path("{datatableName}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update Data Table", description = "Modifies fields of a data table. If the apptableName parameter is passed, data table is deregistered and registered with the new application table.")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = DatatablesApiResourceSwagger.PutDataTablesRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = DatatablesApiResourceSwagger.PutDataTablesResponse.class))) })
    public String updateDatatable(@PathParam("datatableName") @Parameter(description = "datatableName") final String datatableName,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateDBDatatable(datatableName, apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return this.toApiJsonSerializer.serialize(result);
    }

    @DELETE
    @Path("{datatableName}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Delete Data Table", description = "Deletes a data table and deregisters it from the Apache Fineract Core application table.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = DatatablesApiResourceSwagger.DeleteDataTablesResponse.class))) })
    public String deleteDatatable(@PathParam("datatableName") @Parameter(description = "datatableName") final String datatableName,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteDBDatatable(datatableName, apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return this.toApiJsonSerializer.serialize(result);
    }

    @POST
    @Path("register/{datatable}/{apptable}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Register Data Table", description = "Registers a data table with the Apache Fineract Core application table. This allows the data table to be maintained through the API. In case the datatable is a PPI (survey table), a parameter category should be pass along with the request. The API currently support one category (200)")
    @RequestBody(content = @Content(schema = @Schema(implementation = DatatablesApiResourceSwagger.PostDataTablesRegisterDatatableAppTable.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = DatatablesApiResourceSwagger.PutDataTablesResponse.class))) })
    public String registerDatatable(@PathParam("datatable") @Parameter(description = "datatable") final String datatable,
            @PathParam("apptable") @Parameter(description = "apptable") final String apptable,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().registerDBDatatable(datatable, apptable)
                .withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @POST
    @Path("deregister/{datatable}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Deregister Data Table", description = "Deregisters a data table. It will no longer be available through the API.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = DatatablesApiResourceSwagger.PutDataTablesResponse.class))) })
    public String deregisterDatatable(@PathParam("datatable") @Parameter(description = "datatable") final String datatable) {

        this.readWriteNonCoreDataService.deregisterDatatable(datatable);

        final CommandProcessingResult result = new CommandProcessingResultBuilder().withResourceIdAsString(datatable).build();

        return this.toApiJsonSerializer.serialize(result);
    }

    @GET
    @Path("{datatable}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Data Table Details", description = "Lists a registered data table details and the Apache Fineract Core application table they are registered to.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = DatatablesApiResourceSwagger.GetDataTablesResponse.class))) })
    public String getDatatable(@PathParam("datatable") @Parameter(description = "datatable") final String datatable,
            @Context final UriInfo uriInfo) {

        final DatatableData result = this.readWriteNonCoreDataService.retrieveDatatable(datatable);

        final boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serializePretty(prettyPrint, result);
    }

    @GET
    @Path("{datatable}/{apptableId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Entry(s) from Data Table", description = "Gets the entry (if it exists) for data tables that are one to one with the application table. \n"
            + "Gets the entries (if they exist) for data tables that are one to many with the application table.\n" + "\n"
            + "Note: The 'fields' parameter is not available for datatables.\n" + "\n" + "ARGUMENTS\n"
            + "orderoptional Specifies the order in which data is returned.genericResultSetoptional, defaults to false If 'true' an optimised JSON format is returned suitable for tabular display of data. This format is used by the default data tables UI functionality.\n"
            + "Example Requests:\n" + "\n" + "datatables/extra_client_details/1\n" + "\n" + "\n"
            + "datatables/extra_family_details/1?order=`Date of Birth` desc\n" + "\n" + "\n"
            + "datatables/extra_client_details/1?genericResultSet=true")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = HashMap.class))) })
    public String getDatatable(@PathParam("datatable") @Parameter(description = "datatable") final String datatable,
            @PathParam("apptableId") @Parameter(description = "apptableId") final Long apptableId,
            @QueryParam("order") @Parameter(description = "order") final String order, @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasDatatableReadPermission(datatable);

        final GenericResultsetData results = this.readWriteNonCoreDataService.retrieveDataTableGenericResultSet(datatable, apptableId,
                order, null);

        String json = "";
        final boolean genericResultSet = ApiParameterHelper.genericResultSet(uriInfo.getQueryParameters());
        if (genericResultSet) {
            final boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());
            json = this.toApiJsonSerializer.serializePretty(prettyPrint, results);
        } else {
            json = this.genericDataService.generateJsonFromGenericResultsetData(results);
        }

        return json;
    }

    @GET
    @Path("{datatable}/{apptableId}/{datatableId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String getDatatableManyEntry(@PathParam("datatable") final String datatable, @PathParam("apptableId") final Long apptableId,
            @PathParam("datatableId") final Long datatableId, @QueryParam("order") final String order, @Context final UriInfo uriInfo) {

        LOG.debug("::1 we came in the getDatatbleManyEntry apiRessource method");

        this.context.authenticatedUser().validateHasDatatableReadPermission(datatable);

        final GenericResultsetData results = this.readWriteNonCoreDataService.retrieveDataTableGenericResultSet(datatable, apptableId,
                order, datatableId);

        String json = "";
        final boolean genericResultSet = ApiParameterHelper.genericResultSet(uriInfo.getQueryParameters());
        if (genericResultSet) {
            final boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());
            json = this.toApiJsonSerializer.serializePretty(prettyPrint, results);
        } else {
            json = this.genericDataService.generateJsonFromGenericResultsetData(results);
        }

        return json;
    }

    @POST
    @Path("{datatable}/{apptableId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create Entry in Data Table", description = "Adds a row to the data table.\n" + "\n"
            + "Note that the default datatable UI functionality converts any field name containing spaces to underscores when using the API. This means the field name \"Business Description\" is considered the same as \"Business_Description\". So you shouldn't have both \"versions\" in any data table.")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = String.class)), description = "{\n  \"BusinessDescription\": \"Livestock sales\",\n  \"Comment\": \"First comment made\",\n  \"Education_cv\": \"Primary\",\n  \"Gender_cd\": 6,\n  \"HighestRatePaid\": 8.5,\n  \"NextVisit\": \"01 October 2012\",\n  \"YearsinBusiness\": 5,\n  \"dateFormat\": \"dd MMMM yyyy\",\n  \"locale\": \"en\"\n}")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = DatatablesApiResourceSwagger.PostDataTablesAppTableIdResponse.class))) })
    public String createDatatableEntry(@PathParam("datatable") @Parameter(description = "datatable") final String datatable,
            @PathParam("apptableId") @Parameter(description = "apptableId") final Long apptableId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .createDatatable(datatable, apptableId, null) //
                .withJson(apiRequestBodyAsJson) //
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @PUT
    @Path("{datatable}/{apptableId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update Entry in Data Table (One to One)", description = "Updates the row (if it exists) of the data table.")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = DatatablesApiResourceSwagger.PutDataTablesAppTableIdRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = DatatablesApiResourceSwagger.PutDataTablesAppTableIdResponse.class))) })
    public String updateDatatableEntryOnetoOne(@PathParam("datatable") @Parameter(description = "datatable") final String datatable,
            @PathParam("apptableId") @Parameter(description = "apptableId") final Long apptableId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .updateDatatable(datatable, apptableId, null) //
                .withJson(apiRequestBodyAsJson) //
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @PUT
    @Path("{datatable}/{apptableId}/{datatableId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update Entry in Data Table (One to Many)", description = "Updates the row (if it exists) of the data table.")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = DatatablesApiResourceSwagger.PutDataTablesAppTableIdDatatableIdRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = DatatablesApiResourceSwagger.PutDataTablesAppTableIdDatatableIdResponse.class))) })
    public String updateDatatableEntryOneToMany(@PathParam("datatable") @Parameter(description = "datatable") final String datatable,
            @PathParam("apptableId") @Parameter(description = "apptableId") final Long apptableId,
            @PathParam("datatableId") @Parameter(description = "datatableId") final Long datatableId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .updateDatatable(datatable, apptableId, datatableId) //
                .withJson(apiRequestBodyAsJson) //
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @DELETE
    @Path("{datatable}/{apptableId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Delete Entry(s) in Data Table", description = "Deletes the entry (if it exists) for data tables that are one-to-one with the application table. \n"
            + "Deletes the entries (if they exist) for data tables that are one-to-many with the application table.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = DatatablesApiResourceSwagger.DeleteDataTablesDatatableAppTableIdResponse.class))) })
    public String deleteDatatableEntries(@PathParam("datatable") @Parameter(description = "datatable") final String datatable,
            @PathParam("apptableId") @Parameter(description = "apptableId") final Long apptableId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .deleteDatatable(datatable, apptableId, null) //
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @DELETE
    @Path("{datatable}/{apptableId}/{datatableId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Delete Entry in Datatable (One to Many)", description = "Deletes the entry (if it exists) for data tables that are one to many with the application table.\n"
            + "\n")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = DatatablesApiResourceSwagger.DeleteDataTablesDatatableAppTableIdDatatableIdResponse.class))) })
    public String deleteDatatableEntries(
            @PathParam("datatable") @Parameter(description = "datatable", example = "{}") final String datatable,
            @PathParam("apptableId") @Parameter(description = "apptableId") final Long apptableId,
            @PathParam("datatableId") @Parameter(description = "datatableId") final Long datatableId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .deleteDatatable(datatable, apptableId, datatableId) //
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }
}

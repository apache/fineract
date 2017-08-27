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

import io.swagger.annotations.*;
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

//import org.slf4j.Logger;

@Path("/datatables")
@Component
@Scope("singleton")
@Api(value = "Data Tables", description = "The datatables API allows you to plug-in your own tables (MySql) that have a relationship to a Apache Fineract core table. For example, you might want to add some extra client fields and record information about each of the clients' family members. Via the API you can create, read, update and delete entries for each 'plugged-in' table. The API checks for permission and for 'data scoping' (only data within the users' office hierarchy can be managed by the user).\n" + "\n" + "The Apache Fineract Reference App uses a JQuery plug-in called stretchydatatables (which in turn uses this datatables resource) to provide a pretty flexible CRUD (Create, Read, Update, Delete) User Interface.")
public class DatatablesApiResource {

    private final PlatformSecurityContext context;
    private final GenericDataService genericDataService;
    private final ReadWriteNonCoreDataService readWriteNonCoreDataService;
    private final ToApiJsonSerializer<GenericResultsetData> toApiJsonSerializer;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final static org.slf4j.Logger logger = LoggerFactory.getLogger(DatatablesApiResource.class);

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
    @ApiOperation(value = "List Data Tables", notes = "Lists registered data tables and the Apache Fineract Core application table they are registered to.\n" + "\n" + "ARGUMENTS\n" + "\n" + "apptable  - optional" + "\n"+ "The Apache Fineract core application table.\n" + "\n" + "Example Requests:\n" + "\n" + "datatables?apptable=m_client\n" + "\n" + "\n" + "datatables",
    responseContainer = "List", response = DatatablesApiResourceSwagger.GetDataTablesResponse.class)
    @ApiResponses({@ApiResponse(code = 200, message = "", response = DatatablesApiResourceSwagger.GetDataTablesResponse.class, responseContainer = "list")})
    public String getDatatables(@QueryParam("apptable") @ApiParam(value = "apptable") final String apptable, @Context final UriInfo uriInfo) {

        final List<DatatableData> result = this.readWriteNonCoreDataService.retrieveDatatableNames(apptable);

        final boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serializePretty(prettyPrint, result);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Create Data Table", notes = "Create a new data table and registers it with the Apache Fineract Core application table.\n" + "\n" + "Field Descriptions\n" + "\n" + "Mandatory - datatableName : \n"+ "\nThe name of the Data Table.\n" + "\n" + "Mandatory - apptableName\n" + "\n" + "Application table name. Must be one of the following:\n" + "\n" + "m_client\n" + "\n" + "m_group" +"\n" + "\n" + "m_loan" + "\n" + "\n" + "m_office" + "\n" + "\n" + "m_saving_account" + "\n" + "\n" + "m_product_loan" + "\n" + "\n" + "m_savings_product" + "\n" + "\n" + "Mandatory - columns  " + "\n" + "An array of columns in the new Data Table." + "\n" + "\n" + "Optional - multiRow" + "\n" +
            "\n" + "Allows to create multiple entries in the Data Table. Optional, defaults to false. If this property is not provided Data Table will allow only one entry." + "\n" + "\n" + "Field Descriptions - columns" + "\n" + "\n" + "Mandatory - name" + "\n" + "\n" + "Name of the created column. Can contain only alphanumeric characters, underscores and spaces, but cannot start with a number. Cannot start or end with an underscore or space." + "\n" + "\n" + "Mandatory - type" + "\n" + "\n" + "Column type. Must be one of the following:" + "\n" + "\n" + "Boolean" + "\n" + "\n" + "Date" + "\n" + "\n" + "DateTime" + "\n" + "\n" + "Decimal" + "\n" + "\n" + "Dropdown" + "\n" +
            "\n" + "\n" + "Number" + "\n" + "\n" + "String" + "\n" + "\n" + "Text" + "\n" + "\n" + "Mandatory [type = Dropdown] - code" + "\n" + "\n" + "Used in Code Value fields. Column name becomes: code_cd_name. Mandatory if using type Dropdown, otherwise an error is returned." + "\n" + "\n" + "Optional - mandatory" + "\n" + "\n" +
            "Determines whether this column must have a value in every entry. Optional, defaults to false." + "\n" + "\n" + "Mandatory [type = String] - length" + "\n" + "\n" + "Length of the text field. Mandatory if type String is used, otherwise an error is returned.")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = DatatablesApiResourceSwagger.PostDataTablesRequest.class )})
    @ApiResponses({@ApiResponse(code = 200, message = "", response = DatatablesApiResourceSwagger.PostDataTablesResponse.class)})
    public String createDatatable(@ApiParam(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createDBDatatable(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return this.toApiJsonSerializer.serialize(result);
    }

    @PUT
    @Path("{datatableName}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Update Data Table", notes = "Modifies fields of a data table. If the apptableName parameter is passed, data table is deregistered and registered with the new application table.")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = DatatablesApiResourceSwagger.PutDataTablesRequest.class )})
    @ApiResponses({@ApiResponse(code = 200, message = "", response = DatatablesApiResourceSwagger.PutDataTablesResponse.class)})
    public String updateDatatable(@PathParam("datatableName") @ApiParam(value = "datatableName") final String datatableName, @ApiParam(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateDBDatatable(datatableName, apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return this.toApiJsonSerializer.serialize(result);
    }

    @DELETE
    @Path("{datatableName}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Delete Data Table", notes = "Deletes a data table and deregisters it from the Apache Fineract Core application table.")
    @ApiResponses({@ApiResponse(code = 200, message = "", response = DatatablesApiResourceSwagger.DeleteDataTablesResponse.class)})
    public String deleteDatatable(@PathParam("datatableName") @ApiParam(value = "datatableName") final String datatableName, @ApiParam(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteDBDatatable(datatableName, apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return this.toApiJsonSerializer.serialize(result);
    }

    @POST
    @Path("register/{datatable}/{apptable}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Register Data Table", notes = "Registers a data table with the Apache Fineract Core application table. This allows the data table to be maintained through the API. In case the datatable is a PPI (survey table), a parameter category should be pass along with the request. The API currently support one category (200)")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", paramType = "body", dataType = "body", format = "body", dataTypeClass = DatatablesApiResourceSwagger.PostDataTablesRegisterDatatableAppTable.class )})
    @ApiResponses({@ApiResponse(code = 200, message = "", response = DatatablesApiResourceSwagger.PutDataTablesResponse.class)})
    public String registerDatatable(@PathParam("datatable") @ApiParam(value = "datatable") final String datatable, @PathParam("apptable") @ApiParam(value = "apptable") final String apptable,
            @ApiParam(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().registerDBDatatable(datatable, apptable)
                .withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @POST
    @Path("deregister/{datatable}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Deregister Data Table", notes = "Deregisters a data table. It will no longer be available through the API.")
    @ApiResponses({@ApiResponse(code = 200, message = "", response = DatatablesApiResourceSwagger.PutDataTablesResponse.class)})
    public String deregisterDatatable(@PathParam("datatable") @ApiParam(value = "datatable") final String datatable) {

        this.readWriteNonCoreDataService.deregisterDatatable(datatable);

        final CommandProcessingResult result = new CommandProcessingResultBuilder().withResourceIdAsString(datatable).build();

        return this.toApiJsonSerializer.serialize(result);
    }

    @GET
    @Path("{datatable}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Retrieve Data Table Details", notes = "Lists a registered data table details and the Apache Fineract Core application table they are registered to.")
    @ApiResponses({@ApiResponse(code = 200, message = "", response = DatatablesApiResourceSwagger.GetDataTablesResponse.class)})
    public String getDatatable(@PathParam("datatable") @ApiParam(value = "datatable") final String datatable, @Context final UriInfo uriInfo) {

        final DatatableData result = this.readWriteNonCoreDataService.retrieveDatatable(datatable);

        final boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serializePretty(prettyPrint, result);
    }

    @GET
    @Path("{datatable}/{apptableId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Retrieve Entry(s) from Data Table", notes = "Gets the entry (if it exists) for data tables that are one to one with the application table. \n" + "Gets the entries (if they exist) for data tables that are one to many with the application table.\n" + "\n" + "Note: The 'fields' parameter is not available for datatables.\n" + "\n" + "ARGUMENTS\n" + "orderoptional Specifies the order in which data is returned.genericResultSetoptional, defaults to false If 'true' an optimised JSON format is returned suitable for tabular display of data. This format is used by the default data tables UI functionality.\n" + "Example Requests:\n" + "\n" + "datatables/extra_client_details/1\n" + "\n" + "\n" + "datatables/extra_family_details/1?order=`Date of Birth` desc\n" + "\n" + "\n" + "datatables/extra_client_details/1?genericResultSet=true")
    @ApiResponses({@ApiResponse(code = 200, message = "", response = DatatablesApiResourceSwagger.GetDataTablesAppTableIdResponse.class)})
    public String getDatatable(@PathParam("datatable") @ApiParam(value = "datatable") final String datatable, @PathParam("apptableId") @ApiParam(value = "apptableId") final Long apptableId,
            @QueryParam("order") @ApiParam(value = "order") final String order, @Context final UriInfo uriInfo) {

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

        logger.debug("::1 we came in the getDatatbleManyEntry apiRessource method");

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
    @ApiOperation(value = "Create Entry in Data Table", notes = "Adds a row to the data table.\n" + "\n" + "Note that the default datatable UI functionality converts any field name containing spaces to underscores when using the API. This means the field name \"Business Description\" is considered the same as \"Business_Description\". So you shouldn't have both \"versions\" in any data table.")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = DatatablesApiResourceSwagger.PostDataTablesAppTableIdRequest.class )})
    @ApiResponses({@ApiResponse(code = 200, message = "", response = DatatablesApiResourceSwagger.PostDataTablesAppTableIdResponse.class)})
    public String createDatatableEntry(@PathParam("datatable") @ApiParam(value = "datatable") final String datatable, @PathParam("apptableId") @ApiParam(value = "apptableId") final Long apptableId,
                                       @ApiParam(hidden = true) final String apiRequestBodyAsJson) {

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
    @ApiOperation(value = "Update Entry in Data Table (One to One)", notes = "Updates the row (if it exists) of the data table.")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = DatatablesApiResourceSwagger.PutDataTablesAppTableIdRequest.class )})
    @ApiResponses({@ApiResponse(code = 200, message = "", response = DatatablesApiResourceSwagger.PutDataTablesAppTableIdResponse.class)})
    public String updateDatatableEntryOnetoOne(@PathParam("datatable") @ApiParam(value = "datatable") final String datatable,
            @PathParam("apptableId") @ApiParam(value = "apptableId") final Long apptableId, @ApiParam(hidden = true) final String apiRequestBodyAsJson) {

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
    @ApiOperation(value = "Update Entry in Data Table (One to Many)", notes = "Updates the row (if it exists) of the data table.")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = DatatablesApiResourceSwagger.PutDataTablesAppTableIdDatatableIdRequest.class )})
    @ApiResponses({@ApiResponse(code = 200, message = "", response = DatatablesApiResourceSwagger.PutDataTablesAppTableIdDatatableIdResponse.class)})
    public String updateDatatableEntryOneToMany(@PathParam("datatable") @ApiParam(value = "datatable") final String datatable,
            @PathParam("apptableId") @ApiParam(value = "apptableId") final Long apptableId, @PathParam("datatableId") @ApiParam(value = "datatableId") final Long datatableId,
            @ApiParam(hidden = true) final String apiRequestBodyAsJson) {

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
    @ApiOperation(value = "Delete Entry(s) in Data Table", notes = "Deletes the entry (if it exists) for data tables that are one-to-one with the application table. \n" + "Deletes the entries (if they exist) for data tables that are one-to-many with the application table.")
    @ApiResponses({@ApiResponse(code = 200, message = "", response = DatatablesApiResourceSwagger.DeleteDataTablesDatatableAppTableIdResponse.class)})
    public String deleteDatatableEntries(@PathParam("datatable") @ApiParam(value = "datatable") final String datatable, @PathParam("apptableId") @ApiParam(value = "apptableId") final Long apptableId) {

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
    @ApiOperation(value = "Delete Entry in Datatable (One to Many)", notes = "Deletes the entry (if it exists) for data tables that are one to many with the application table.\n" + "\n")
    @ApiResponses({@ApiResponse(code = 200, message = "", response = DatatablesApiResourceSwagger.DeleteDataTablesDatatableAppTableIdDatatableIdResponse.class)})
    public String deleteDatatableEntries(@PathParam("datatable") @ApiParam(value = "datatable", type = "body", example = "{}") final String datatable, @PathParam("apptableId") @ApiParam(value = "apptableId") final Long apptableId,
            @PathParam("datatableId") @ApiParam(value = "datatableId") final Long datatableId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .deleteDatatable(datatable, apptableId, datatableId) //
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }
}
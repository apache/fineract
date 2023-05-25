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
package org.apache.fineract.portfolio.self.runreport;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.dataqueries.api.RunreportsApiResource;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.stereotype.Component;

@Path("/v1/self/runreports")
@Component
@Tag(name = "Self Run Report", description = "This resource allows you to run and receive output from pre-defined Apache Fineract reports.\n"
        + "\n"
        + "The default output is a JSON formatted \"Generic Resultset\". The Generic Resultset contains Column Heading as well as Data information. However, you can export to CSV format by simply adding \"&exportCSV=true\" to the end of your URL.\n"
        + "\n"
        + "If Pentaho reports have been pre-defined, they can also be run through this resource. Pentaho reports can return HTML, PDF or CSV formats.\n"
        + "\n"
        + "The Apache Fineract reference application uses a JQuery plugin called stretchyreporting which, itself, uses this reports resource to provide a pretty flexible reporting User Interface (UI).\n"
        + "\n" + "ARGUMENTS\n"
        + "R_'parameter names' ... optional, No defaults The number and names of the parameters depend on the specific report and how it has been configured. R_officeId is an example parameter name.Note: the prefix R_ stands for ReportinggenericResultSetoptional, defaults to true If 'true' an optimised JSON format is returned suitable for tabular display of data. If 'false' a simple JSON format is returned. parameterType optional, The only valid value is 'true'. If any other value is provided the argument will be ignored Determines whether the request looks in the list of reports or the list of parameters for its data. Doesn't apply to Pentaho reports.exportCSV optional, The only valid value is 'true'. If any other value is provided the argument will be ignored Output will be delivered as a CSV file instead of JSON. Doesn't apply to Pentaho reports.output-type optional, Defaults to HTML. Valid Values are HTML, XLS, XSLX, CSV and PDF for html, Excel, Excel 2007+, CSV and PDF formats respectively.Only applies to Pentaho reports.locale optional Any valid locale Ex: en_US, en_IN, fr_FR etcOnly applies to Pentaho reports.")
@RequiredArgsConstructor
public class SelfRunReportApiResource {

    private final PlatformSecurityContext context;
    private final RunreportsApiResource runreportsApiResource;

    @GET
    @Path("{reportName}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON, "text/csv", "application/vnd.ms-excel", "application/pdf", "text/html" })
    @Operation(summary = "Running A Report", description = "" + "Example Requests:\n" + "\n" + "\n"
            + "self/runreports/Client%20Details?R_officeId=1" + "\n" + "\n" + "\n"
            + "self/runreports/Client%20Details?R_officeId=1&exportCSV=true")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = SelfRunReportApiResourceSwagger.GetRunReportResponse.class))) })
    public Response runReport(@PathParam("reportName") @Parameter(description = "reportName") final String reportName,
            @Context final UriInfo uriInfo) {
        this.context.authenticatedUser();
        final boolean isSelfServiceUserReport = true;
        return this.runreportsApiResource.runReport(reportName, uriInfo, isSelfServiceUserReport);
    }

}

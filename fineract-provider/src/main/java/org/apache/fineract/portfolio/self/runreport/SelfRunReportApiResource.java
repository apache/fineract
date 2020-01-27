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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.apache.fineract.infrastructure.dataqueries.api.RunreportsApiResource;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/self/runreports")
@Component
@Scope("singleton")
@Api(tags = {"Self Run Report "})
@SwaggerDefinition(tags = {
        @Tag(name = "Self Run Report", description = "This resource allows you to run and receive output from pre-defined Apache Fineract reports.\n" +
                "\n" +
                "The default output is a JSON formatted \"Generic Resultset\". The Generic Resultset contains Column Heading as well as Data information. However, you can export to CSV format by simply adding \"&exportCSV=true\" to the end of your URL.\n" +
                "\n" +
                "If Pentaho reports have been pre-defined, they can also be run through this resource. Pentaho reports can return HTML, PDF or CSV formats.\n" +
                "\n" +
                "The Apache Fineract reference application uses a JQuery plugin called stretchyreporting which, itself, uses this reports resource to provide a pretty flexible reporting User Interface (UI).\n" +
                "\n" +
                "ARGUMENTS\n" +
                "R_'parameter names' ... optional, No defaults The number and names of the parameters depend on the specific report and how it has been configured. R_officeId is an example parameter name.Note: the prefix R_ stands for ReportinggenericResultSetoptional, defaults to true If 'true' an optimised JSON format is returned suitable for tabular display of data. If 'false' a simple JSON format is returned. parameterType optional, The only valid value is 'true'. If any other value is provided the argument will be ignored Determines whether the request looks in the list of reports or the list of parameters for its data. Doesn't apply to Pentaho reports.exportCSV optional, The only valid value is 'true'. If any other value is provided the argument will be ignored Output will be delivered as a CSV file instead of JSON. Doesn't apply to Pentaho reports.output-type optional, Defaults to HTML. Valid Values are HTML, XLS, XSLX, CSV and PDF for html, Excel, Excel 2007+, CSV and PDF formats respectively.Only applies to Pentaho reports.locale optional Any valid locale Ex: en_US, en_IN, fr_FR etcOnly applies to Pentaho reports.")
})
public class SelfRunReportApiResource {

    private final PlatformSecurityContext context;
    private final RunreportsApiResource runreportsApiResource;

    @Autowired
    public SelfRunReportApiResource(final PlatformSecurityContext context, final RunreportsApiResource runreportsApiResource) {
        this.context = context;
        this.runreportsApiResource = runreportsApiResource;
    }

    @GET
    @Path("{reportName}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON, "text/csv", "application/vnd.ms-excel", "application/pdf", "text/html" })
    @ApiOperation(value = "Running A Report", httpMethod = "GET", notes = "" + "Example Requests:\n" + "\n"  + "\n" + "self/runreports/Client%20Details?R_officeId=1" + "\n"  + "\n" + "\n" +
            "self/runreports/Client%20Details?R_officeId=1&exportCSV=true")
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response =  SelfRunReportApiResourceSwagger.GetRunReportResponse.class)})
    public Response runReport(@PathParam("reportName") @ApiParam(value = "reportName") final String reportName, @Context final UriInfo uriInfo) {
        this.context.authenticatedUser();
        final boolean isSelfServiceUserReport = true;
        return this.runreportsApiResource.runReport(reportName, uriInfo, isSelfServiceUserReport);
    }

}

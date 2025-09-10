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
package org.apache.fineract.infrastructure.dataqueries.data;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;

public final class ReportParameters {

    // Private constructor to prevent instantiation
    private ReportParameters() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    private static final String IS_SELF_SERVICE_USER_REPORT = "isSelfServiceUserReport";
    private static final String EXPORT_CSV = "exportCSV";
    private static final String PARAMETER_TYPE = "parameterType";
    private static final String OUTPUT_TYPE = "output-type";
    private static final String ENABLE_BUSINESS_DATE = "enable-business-date";
    private static final String OBLIG_DATE_TYPE = "obligDateType";
    private static final String DECIMAL_CHOICE = "decimalChoice";
    private static final String PORTFOLIO_RISK_BRANCH = "Portfolio at Risk by Branch";

    public static final String FULL_DESCRIPTION = "This resource allows you to run and receive output from pre-defined Apache Fineract reports.\n"
            + "\n" + "Reports can also be used to provide data for searching and workflow functionality.\n" + "\n"
            + "The default output is a JSON formatted \"Generic Resultset\". The Generic Resultset contains Column Heading as well as Data information. However, you can export to CSV format by simply adding \"&exportCSV=true\" to the end of your URL.\n"
            + "\n"
            + "If Pentaho reports have been pre-defined, they can also be run through this resource. Pentaho reports can return HTML, PDF or CSV formats.\n"
            + "\n"
            + "The Apache Fineract reference application uses a JQuery plugin called stretchy reporting which, itself, uses this reports resource to provide a pretty flexible reporting User Interface (UI).\n\n"
            + "\n" + "\n" + "Example Requests:\n" + "\n" + "runreports/Client%20Listing?R_officeId=1\n" + "\n" + "\n"
            + "runreports/Client%20Listing?R_officeId=1&exportCSV=true\n" + "\n" + "\n"
            + "runreports/OfficeIdSelectOne?R_officeId=1&parameterType=true\n" + "\n" + "\n"
            + "runreports/OfficeIdSelectOne?R_officeId=1&parameterType=true&exportCSV=true\n" + "\n" + "\n"
            + "runreports/Expected%20Payments%20By%20Date%20-%20Formatted?R_endDate=2013-04-30&R_loanOfficerId=-1&R_officeId=1&R_startDate=2013-04-16&output-type=HTML&R_officeId=1\n"
            + "\n" + "\n"
            + "runreports/Expected%20Payments%20By%20Date%20-%20Formatted?R_endDate=2013-04-30&R_loanOfficerId=-1&R_officeId=1&R_startDate=2013-04-16&output-type=XLS&R_officeId=1\n"
            + "\n" + "\n"
            + "runreports/Expected%20Payments%20By%20Date%20-%20Formatted?R_endDate=2013-04-30&R_loanOfficerId=-1&R_officeId=1&R_startDate=2013-04-16&output-type=CSV&R_officeId=1\n"
            + "\n" + "\n"
            + "runreports/Expected%20Payments%20By%20Date%20-%20Formatted?R_endDate=2013-04-30&R_loanOfficerId=-1&R_officeId=1&R_startDate=2013-04-16&output-type=PDF&R_officeId=1"
            + "\n\n**Available Parameters (All Optional):**\n\n" + "**Common Control Parameters:**\n"
            + "- `isSelfServiceUserReport`: Indicates if this is a self-service user report (default: false)\n"
            + "- `exportCSV`: Set to true to export results as CSV (default: false)\n"
            + "- `parameterType`: Indicates if this is a parameter type request (default: false)\n"
            + "- `output-type`: Output format type (HTML, XLS, CSV, PDF)\n" + "- `enable-business-date`: Enable business date filtering\n"
            + "- `obligDateType`: Obligation date type\n" + "- `decimalChoice`: Decimal formatting choice\n"
            + "- `Portfolio at Risk by Branch`: Portfolio risk parameter\n\n"

            + "**Common Report Parameters (R_ prefixed):**\n" + "- `R_officeId`: Office ID filter\n"
            + "- `R_loanOfficerId`: Loan officer ID filter\n" + "- `R_currencyId`: Currency ID filter\n"
            + "- `R_fromDate`, `R_toDate`: Date range filters (yyyy-MM-dd)\n" + "- `R_accountNo`: Account number filter\n"
            + "- `R_transactionId`: Transaction ID filter\n" + "- `R_centerId`: Center ID filter\n" + "- `R_branch`: Branch filter\n"
            + "- `R_ondate`: Specific date filter\n" + "- `R_cycleX`, `R_cycleY`: Cycle filters\n" + "- `R_fromX`, `R_toY`: Range filters\n"
            + "- `R_overdueX`, `R_overdueY`: Overdue filters\n" + "- `R_endDate`: End date filter\n\n"

            + "**Other Common Parameters:**\n" + "- `OfficeId`: Office ID filter (alternative)\n"
            + "- `loanOfficerId`: Loan officer ID filter (alternative)\n" + "- `currencyId`: Currency ID filter (alternative)\n"
            + "- `fundId`: Fund ID filter\n" + "- `loanProductId`: Loan product ID filter\n" + "- `loanPurposeId`: Loan purpose ID filter\n"
            + "- `parType`: Portfolio at risk type\n" + "- `SelectGLAccountNO`: GL account number selection\n"
            + "- `SavingsAccountSubStatus`: Savings account status\n" + "- `SelectLoanType`: Loan type selection\n\n"

            + "**Note:** All parameters are optional and report-specific. \n"
            + "The exact parameters required depend on the specific report being executed.\n"
            + "Some reports may accept additional parameters not listed here.";

    @Parameters({
            @Parameter(name = IS_SELF_SERVICE_USER_REPORT, description = "Optional - Indicates if this is a self-service user report", example = "false"),
            @Parameter(name = EXPORT_CSV, description = "Optional - Set to true to export results as CSV", example = "true"),
            @Parameter(name = PARAMETER_TYPE, description = "Optional - Indicates if this is a parameter type request", example = "false"),
            @Parameter(name = OUTPUT_TYPE, description = "Optional - Output format type (HTML, XLS, CSV, PDF)", example = "HTML"),
            @Parameter(name = ENABLE_BUSINESS_DATE, description = "Optional - Enable business date filtering", example = "true"),
            @Parameter(name = OBLIG_DATE_TYPE, description = "Optional - Obligation date type", example = "due"),
            @Parameter(name = DECIMAL_CHOICE, description = "Optional - Decimal formatting choice", example = "2"),
            @Parameter(name = PORTFOLIO_RISK_BRANCH, description = "Optional - Portfolio at Risk by Branch parameter", example = "30"),

            @Parameter(name = "R_officeId", description = " Office ID filter", example = "1"),
            @Parameter(name = "R_loanOfficerId", description = "Optional - Loan officer ID filter", example = "5"),
            @Parameter(name = "R_currencyId", description = "Optional - Currency ID filter", example = "USD"),
            @Parameter(name = "R_fromDate", description = "Optional - Start date filter (yyyy-MM-dd)", example = "2023-01-01"),
            @Parameter(name = "R_toDate", description = "Optional - End date filter (yyyy-MM-dd)", example = "2023-12-31"),
            @Parameter(name = "R_accountNo", description = "Optional - Account number filter", example = "00010001"),
            @Parameter(name = "R_transactionId", description = "Optional - Transaction ID filter", example = "12345"),
            @Parameter(name = "R_centerId", description = "Optional - Center ID filter", example = "10"),
            @Parameter(name = "R_branch", description = "Optional - Branch filter", example = "Main"),
            @Parameter(name = "R_ondate", description = "Optional - Specific date filter", example = "2023-06-15"),
            @Parameter(name = "R_cycleX", description = "Optional - Cycle X filter", example = "1"),
            @Parameter(name = "R_cycleY", description = "Optional - Cycle Y filter", example = "12"),
            @Parameter(name = "R_fromX", description = "Optional - From X value filter", example = "0"),
            @Parameter(name = "R_toY", description = "Optional - To Y value filter", example = "100"),
            @Parameter(name = "R_overdueX", description = "Optional - Overdue X days filter", example = "30"),
            @Parameter(name = "R_overdueY", description = "Optional - Overdue Y days filter", example = "90"),
            @Parameter(name = "R_endDate", description = "Optional - End date filter", example = "2023-12-31"),

            @Parameter(name = "OfficeId", description = "Optional - Office ID filter (alternative)", example = "1"),
            @Parameter(name = "loanOfficerId", description = "Optional - Loan officer ID filter (alternative)", example = "5"),
            @Parameter(name = "currencyId", description = "Optional - Currency ID filter (alternative)", example = "USD"),
            @Parameter(name = "fundId", description = "Optional - Fund ID filter", example = "1"),
            @Parameter(name = "loanProductId", description = "Optional - Loan product ID filter", example = "2"),
            @Parameter(name = "loanPurposeId", description = "Optional - Loan purpose ID filter", example = "3"),
            @Parameter(name = "parType", description = "Optional - Portfolio at risk type", example = "30"),
            @Parameter(name = "SelectGLAccountNO", description = "Optional - GL account number selection", example = "11001"),
            @Parameter(name = "SavingsAccountSubStatus", description = "Optional - Savings account sub-status", example = "active"),
            @Parameter(name = "SelectLoanType", description = "Optional - Loan type selection", example = "individual"),

            @Parameter(name = "R_*", description = "Optional - Additional report-specific parameters prefixed with 'R_'") })

    public static void getOpenApiParameters() {

    }

    public static String getIsSelfServiceUserReport() {
        return IS_SELF_SERVICE_USER_REPORT;
    }

    public static String getExportCsv() {
        return EXPORT_CSV;
    }

    public static String getParameterType() {
        return PARAMETER_TYPE;
    }

    public static String getOutputType() {
        return OUTPUT_TYPE;
    }

    public static String getEnableBusinessDate() {
        return ENABLE_BUSINESS_DATE;
    }

    public static String getObligDateType() {
        return OBLIG_DATE_TYPE;
    }

    public static String getDecimalChoice() {
        return DECIMAL_CHOICE;
    }

    public static String getPortfolioRiskBranch() {
        return PORTFOLIO_RISK_BRANCH;
    }

    public static String getFullDescription() {
        return FULL_DESCRIPTION;
    }

    @Parameters({
            @Parameter(name = IS_SELF_SERVICE_USER_REPORT, description = "Optional - Indicates if this is a self-service user report", example = "false"),
            @Parameter(name = EXPORT_CSV, description = "Optional - Set to true to export results as CSV", example = "true"),
            @Parameter(name = PARAMETER_TYPE, description = "Optional - Indicates if this is a parameter type request", example = "false"),
            @Parameter(name = OUTPUT_TYPE, description = "Optional - Output format type (HTML, XLS, CSV, PDF)", example = "HTML"),
            @Parameter(name = "R_*", description = "Optional - Report-specific parameters prefixed with 'R_'") })
    public static void getMinimalOpenApiParameters() {

    }
}

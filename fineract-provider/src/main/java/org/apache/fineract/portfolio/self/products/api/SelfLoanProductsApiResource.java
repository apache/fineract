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

package org.apache.fineract.portfolio.self.products.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import org.apache.fineract.portfolio.loanaccount.api.LoanApiConstants;
import org.apache.fineract.portfolio.loanproduct.api.LoanProductsApiResource;
import org.apache.fineract.portfolio.self.client.service.AppuserClientMapperReadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/self/loanproducts")
@Component
@Scope("singleton")
@Api(tags = {"Self Loan Products"})
@SwaggerDefinition(tags = {
  @Tag(name = "Self Loan Products", description = "A Loan product is a template that is used when creating a loan. Much of the template definition can be overridden during loan creation.\n" + "\n" +
    "Field Descriptions\n" +
    "name\n" +
    "Name associated with loan product on system.\n" +
    "shortName\n" +
    "Short name associated with a loan product. \n" +
    "An abbreviated version of the name, used in reports or menus where space is limited, such as Collection Sheets.\n" +
    "description\n" +
    "For providing helpful description of product offering.\n" +
    "fundId\n" +
    "For associating a loan product with a given fund by default.\n" +
    "includeInBorrowerCycle\n" +
    "It is a flag, Used to denote whether the loans should include in loan cycle counter or not.\n" +
    "useBorrowerCycle\n" +
    "It is a flag, Used to denote whether the loans should depend on \n" +
    "borrower loan cycle counter or not.\n" +
    "currencyCode\n" +
    "A three letter ISO code of currency.\n" +
    "digitsAfterDecimal\n" +
    "Override the currency default value for digitsAfterDecimal.\n" +
    "inMultiplesOf\n" +
    "Override the default value for rounding currency to multiples of value provided.\n" +
    "installmentAmountInMultiplesOf\n" +
    "Override the default value for rounding instalment amount to multiples of value provided.\n" +
    "principal\n" +
    "The loan amount to be disbursed to through loan.\n" +
    "numberOfRepayments\n" +
    "Number of installments to repay.\n" +
    "Used like: numberOfRepayments Every repaymentEvery repaymentFrequencyType\n" +
    "e.g. 10 (repayments) Every 12 Weeks\n" +
    "repaymentEvery\n" +
    "Used like: numberOfRepayments Every repaymentEvery repaymentFrequencyType\n" +
    "e.g. 10 (repayments) Every 12 Weeks\n" +
    "repaymentFrequencyType\n" +
    "Used like: numberOfRepayments Every repaymentEvery repaymentFrequencyType\n" +
    "e.g. 10 (repayments) Every 12 Weeks \n" +
    "Example Values: 0=Days, 1=Weeks, 2=Months\n" +
    "interestRatePerPeriod\n" +
    "Interest Rate.\n" +
    "Used like: interestRatePerPeriod % interestRateFrequencyType - interestType\n" +
    "e.g. 12.0000% Per year - Declining Balance\n" +
    "interestRateFrequencyType\n" +
    "Used like: interestRatePerPeriod% interestRateFrequencyType - interestType\n" +
    "e.g. 12.0000% Per year - Declining Balance \n" +
    "Example Values: 2=Per month, 3=Per year\n" +
    "amortizationType\n" +
    "Example Values: 0=Equal principle payments, 1=Equal installments\n" +
    "interestType\n" +
    "Used like: interestRatePerPeriod% interestRateFrequencyType - interestType\n" +
    "e.g. 12.0000% Per year - Declining Balance \n" +
    "Example Values: 0=Declining Balance, 1=Flat\n" +
    "interestCalculationPeriodType\n" +
    "Example Values: 0=Daily, 1=Same as repayment period\n" +
    "allowPartialPeriodInterestCalcualtion\n" +
    "This value will be supported along with interestCalculationPeriodType as Same as repayment period to calculate interest for partial periods. Example: Interest charged from is 5th of April , Principal is 10000 and interest is 1% per month then the interest will be (10000 * 1%)* (25/30) , it calculates for the month first then calculates exact periods between start date and end date(can be a decimal)\n" +
    "inArrearsTolerance\n" +
    "The amount that can be 'waived' at end of all loan payments because it is too small to worry about.\n" +
    "This is also the tolerance amount assessed when determining if a loan is in arrears.\n" +
    "principalVariationsForBorrowerCycle,interestRateVariationsForBorrowerCycle,\n" +
    "numberOfRepaymentVariationsForBorrowerCycle\n" +
    "Variations for loan, based on borrower cycle number\n" +
    "minimumDaysBetweenDisbursalAndFirstRepayment\n" +
    "The minimum number of days allowed between a Loan disbursal and its first repayment.\n" +
    "principalThresholdForLastInstalment\n" +
    "Field represents percentage of current instalment principal amount for comparing against principal outstanding to add another repayment instalment. If the outstanding principal amount is less then calculated amount, remaining outstanding amount will be added to current instalment. Default value for multi disburse loan is 50% and non-multi disburse loan is 0%\n" +
    "canDefineInstallmentAmount\n" +
    "if provided as true, then fixed instalment amount can be provided from loan account.\n" +
    "transactionProcessingStrategyId\n" +
    "An enumeration that indicates the type of transaction processing strategy to be used. This relates to functionality that is also known as Payment Application Logic.\n" +
    "A number of out of the box approaches exist, some are custom to specific MFIs, some are more general and indicate the order in which payments are processed.\n" +
    "\n" +
    "Refer to the Payment Application Logic / Transaction Processing Strategy section in the appendix for more detailed overview of each available payment application logic provided out of the box.\n" +
    "\n" +
    "List of current approaches:\n" +
    "1 = Mifos style (Similar to Old Mifos)\n" +
    "2 = Heavensfamily (Custom MFI approach)\n" +
    "3 = Creocore (Custom MFI approach)\n" +
    "4 = RBI (India)\n" +
    "5 = Principal Interest Penalties Fees Order\n" +
    "6 = Interest Principal Penalties Fees Order\n" +
    "7 = Early Payment Strategy\n" +
    "graceOnPrincipalPayment\n" +
    "Optional: Integer - represents the number of repayment periods that grace should apply to the principal component of a repayment period.\n" +
    "graceOnInterestPayment\n" +
    "Optional: Integer - represents the number of repayment periods that grace should apply to the interest component of a repayment period. Interest is still calculated but offset to later repayment periods.\n" +
    "graceOnInterestCharged\n" +
    "Optional: Integer - represents the number of repayment periods that should be interest-free.\n" +
    "graceOnArrearsAgeing\n" +
    "Optional: Integer - Used in Arrears calculation to only take into account loans that are more than graceOnArrearsAgeing days overdue.\n" +
    "overdueDaysForNPA\n" +
    "Optional: Integer - represents the maximum number of days a Loan may be overdue before being classified as a NPA (non performing asset)\n" +
    "accountMovesOutOfNPAOnlyOnArrearsCompletion\n" +
    "Optional: Boolean - if provided as true, Loan Account moves out of NPA state only when all arrears are cleared\n" +
    "accountingRule\n" +
    "Specifies if accounting is enabled for the particular product and the type of the accounting rule to be used Example Values:1=NONE, 2=CASH_BASED, 3=ACCRUAL_PERIODIC, 4=ACCRUAL_UPFRONT\n" +
    "isInterestRecalculationEnabled\n" +
    "It is a flag, Used to denote whether interest recalculation is enabled or disabled for the particular product\n" +
    "daysInYearType\n" +
    "Specifies the number of days in a year. \n" +
    "Example Values:1=ACTUAL(Actual number of days in year), 360=360 DAYS, 364=364 DAYS(52 WEEKS), 365=365 DAYS\n" +
    "daysInMonthType\n" +
    "Specifies the number of days in a month. \n" +
    "Example Values:1=ACTUAL(Actual number of days in month), 30=30 DAYS\n" +
    "interestRecalculationCompoundingMethod\n" +
    "Specifies which amount portion should be added to principal for interest recalculation. \n" +
    "Example Values:0=NONE(Only on principal), 1=INTEREST(Principal+Interest), 2=FEE(Principal+Fee), 3=FEE And INTEREST (Principal+Fee+Interest)\n" +
    "rescheduleStrategyMethod\n" +
    "Specifies what action should perform on loan repayment schedule for advance payments. \n" +
    "Example Values:1=Reschedule next repayments, 2=Reduce number of installments, 3=Reduce EMI amount\n" +
    "recalculationCompoundingFrequencyType\n" +
    "Specifies effective date from which the compounding of interest or fee amounts will be considered in recalculation on late payment.\n" +
    "Example Values:1=Same as repayment period, 2=Daily, 3=Weekly, 4=Monthly\n" +
    "recalculationCompoundingFrequencyInterval\n" +
    "Specifies compounding frequency interval for interest recalculation.\n" +
    "recalculationCompoundingFrequencyDate\n" +
    "Specifies compounding frequency start date for interest recalculation.\n" +
    "recalculationRestFrequencyType\n" +
    "Specifies effective date from which the late or advanced payment amounts will be considered in recalculation.\n" +
    "Example Values:1=Same as repayment period, 2=Daily, 3=Weekly, 4=Monthly\n" +
    "recalculationRestFrequencyInterval\n" +
    "Specifies rest frequency interval for interest recalculation.\n" +
    "recalculationRestFrequencyDate\n" +
    "Specifies rest frequency start date for interest recalculation.\n" +
    "preClosureInterestCalculationStrategy\n" +
    "Specifies applicable days for interest calculation on pre closure of a loan.\n" +
    "Example Values:1=Calculate till pre closure date, 2=Calculate till rest frequency date\n" +
    "isArrearsBasedOnOriginalSchedule\n" +
    "If Specified as true, arrears will be identified based on original schedule.\n" +
    "allowAttributeOverrides\n" +
    "Specifies if select attributes may be overridden for individual loan accounts.")
})
public class SelfLoanProductsApiResource {

    private final LoanProductsApiResource loanProductsApiResource;
    private final AppuserClientMapperReadService appUserClientMapperReadService;

    @Autowired
    public SelfLoanProductsApiResource(final LoanProductsApiResource loanProductsApiResource,
            final AppuserClientMapperReadService appUserClientMapperReadService) {
        this.loanProductsApiResource = loanProductsApiResource;
        this.appUserClientMapperReadService = appUserClientMapperReadService;

    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveAllLoanProducts(@QueryParam(LoanApiConstants.clientIdParameterName) final Long clientId,
            @Context final UriInfo uriInfo) {

        this.appUserClientMapperReadService.validateAppuserClientsMapping(clientId);
        return this.loanProductsApiResource.retrieveAllLoanProducts(uriInfo);

    }

    @GET
    @Path("{productId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveLoanProductDetails(@QueryParam(LoanApiConstants.clientIdParameterName) final Long clientId,
            @PathParam(LoanApiConstants.productIdParameterName) final Long productId, @Context final UriInfo uriInfo) {

        this.appUserClientMapperReadService.validateAppuserClientsMapping(clientId);
        return this.loanProductsApiResource.retrieveLoanProductDetails(productId, uriInfo);
    }

}

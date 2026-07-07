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
package org.apache.fineract.portfolio.workingcapitalloan.data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.fineract.infrastructure.core.data.StringEnumOptionData;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.portfolio.client.data.ClientData;
import org.apache.fineract.portfolio.delinquency.data.DelinquencyBucketData;
import org.apache.fineract.portfolio.loanaccount.data.LoanApplicationTimelineData;
import org.apache.fineract.portfolio.loanaccount.data.LoanStatusEnumData;
import org.apache.fineract.portfolio.loanorigination.data.LoanOriginatorData;
import org.apache.fineract.portfolio.workingcapitalloanbreach.data.WorkingCapitalBreachData;
import org.apache.fineract.portfolio.workingcapitalloannearbreach.data.WorkingCapitalNearBreachData;
import org.apache.fineract.portfolio.workingcapitalloanproduct.data.WorkingCapitalLoanProductData;
import org.apache.fineract.portfolio.workingcapitalloanproduct.data.WorkingCapitalPaymentAllocationData;

/**
 * Data Transfer Object for Working Capital Loan (application/summary).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkingCapitalLoanData implements Serializable {

    private Long id;
    private String accountNo;
    private ExternalId externalId;
    private ClientData client;
    private Long clientId;
    private String clientAccountNo;
    private String clientName;
    private ExternalId clientExternalId;
    private Long clientOfficeId;
    private Long fundId;
    private String fundName;
    private WorkingCapitalLoanProductData product;
    private Long loanProductId;
    private String loanProductName;
    private String loanProductDescription;
    private LoanStatusEnumData status;
    private BigDecimal proposedPrincipal;
    private BigDecimal approvedPrincipal;
    private BigDecimal principal;
    private BigDecimal netDisbursalAmount;
    private StringEnumOptionData amortizationType;
    private Integer npvDayCount;
    private Integer loanProductCounter;
    private List<WorkingCapitalLoanChargeData> charges;

    private CurrencyData currency;
    private BigDecimal paymentRate;
    private Integer repaymentEvery;
    private StringEnumOptionData repaymentFrequencyType;
    private BigDecimal discountFee;
    private BigDecimal proposedDiscountFee;
    private BigDecimal approvedDiscountFee;
    private Integer numberOfRepayments;
    private BigDecimal periodPaymentAmount;
    private BigDecimal dailyEir;
    private BigDecimal calculatedAnnualEir;
    private DelinquencyBucketData delinquencyBucket;
    private WorkingCapitalBreachData breach;
    private WorkingCapitalNearBreachData nearBreach;
    private LocalDate lastClosedBusinessDate;
    private List<WorkingCapitalPaymentAllocationData> paymentAllocation;
    private LoanApplicationTimelineData timeline;
    private List<WorkingCapitalLoanDisbursementDetailData> disbursementDetails;
    private WorkingCapitalLoanBalanceData balance;
    private Integer delinquencyGraceDays;
    private StringEnumOptionData delinquencyStartType;
    private Integer breachGraceDays;
    private BigDecimal totalPaymentVolume;
    private LocalDate delinquencyStartDate;
    private LocalDate breachStartDate;
    private WorkingCapitalLoanCollectionData delinquent;
    private Boolean enableInstallmentLevelDelinquency;
    private WorkingCapitalLoanSummaryData summary;
    private List<LoanOriginatorData> originators;
    private Boolean fraud;
    private Boolean chargedOff;
}

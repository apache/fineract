package org.mifosplatform.portfolio.loanproduct;


public interface LoanProductConstants {
    
    public static final String useBorrowerCycleParameterName = "useBorrowerCycle";
    
    public static final String principalVariationsForBorrowerCycleParameterName = "principalVariationsForBorrowerCycle";
    public static final String interestRateVariationsForBorrowerCycleParameterName = "interestRateVariationsForBorrowerCycle";
    public static final String numberOfRepaymentVariationsForBorrowerCycleParameterName = "numberOfRepaymentVariationsForBorrowerCycle";

    public static final String defaultValueParameterName = "defaultValue";
    public static final String minValueParameterName = "minValue";
    public static final String maxValueParameterName = "maxValue";
    public static final String valueConditionTypeParamName = "valueConditionType";
    public static final String borrowerCycleNumberParamName = "borrowerCycleNumber";
    public static final String borrowerCycleIdParameterName = "id";
    
    
    public static final String principalPerCycleParameterName = "principalPerCycle";
    public static final String minPrincipalPerCycleParameterName = "minPrincipalPerCycle";
    public static final String maxPrincipalPerCycleParameterName = "maxPrincipalPerCycle";
    public static final String principalValueUsageConditionParamName = "principalValueUsageCondition";
    public static final String principalCycleNumbersParamName = "principalCycleNumbers";
    
    public static final String numberOfRepaymentsPerCycleParameterName = "numberOfRepaymentsPerCycle";
    public static final String minNumberOfRepaymentsPerCycleParameterName = "minNumberOfRepaymentsPerCycle";
    public static final String maxNumberOfRepaymentsPerCycleParameterName = "maxNumberOfRepaymentsPerCycle";
    public static final String repaymentValueUsageConditionParamName = "repaymentValueUsageCondition";
    public static final String repaymentCycleNumberParamName = "repaymentCycleNumber";
    
    public static final String interestRatePerPeriodPerCycleParameterName = "interestRatePerPeriodPerCycle";
    public static final String minInterestRatePerPeriodPerCycleParameterName = "minInterestRatePerPeriodPerCycle";
    public static final String maxInterestRatePerPeriodPerCycleParameterName = "maxInterestRatePerPeriodPerCycle";
    public static final String interestRateValueUsageConditionParamName = "interestRateValueUsageCondition";
    public static final String interestRateCycleNumberParamName = "interestRateCycleNumber";
    
    public static final String principal = "principal";
    public static final String minPrincipal = "minPrincipal";
    public static final String maxPrincipal = "maxPrincipalValue";
    
    public static final String interestRatePerPeriod = "interestRatePerPeriod";
    public static final String minInterestRatePerPeriod = "minInterestRatePerPeriod";
    public static final String maxInterestRatePerPeriod = "maxInterestRatePerPeriod";
    
    public static final String numberOfRepayments = "numberOfRepayments";
    public static final String minNumberOfRepayments = "minNumberOfRepayments";
    public static final String maxNumberOfRepayments = "maxNumberOfRepayments";
    
    public static final String VALUE_CONDITION_END_WITH_ERROR = "condition.type.must.end.with.greterthan";
    public static final String VALUE_CONDITION_START_WITH_ERROR = "condition.type.must.start.with.equal";
}

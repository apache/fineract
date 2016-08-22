package org.apache.fineract.CreditCheck.data;

public class CreditBureauLpMappingData {

    private final long creditbureauLoanProductMappingId;

    private final long organisationCreditBureauId;
    
    private final String alias;
    
    private final String creditbureauSummary;
    
    private final String loanProductName;

    private final long loanProductId;

    private final boolean isCreditCheckMandatory;

    private final boolean skipCrediCheckInFailure;

    private final long stalePeriod;

    private final boolean is_active;

    private CreditBureauLpMappingData(final long creditbureauLoanProductMappingId, final long organisationCreditBureauId,
            final String alias,final String creditbureauSummary,final String loanProductName,final long loanProductId, final boolean isCreditCheckMandatory, final boolean skipCrediCheckInFailure, final long stalePeriod,
            final boolean is_active) {
        this.creditbureauLoanProductMappingId = creditbureauLoanProductMappingId;
        this.organisationCreditBureauId = organisationCreditBureauId;
        this.alias=alias;
        this.creditbureauSummary=creditbureauSummary;
        this.loanProductName=loanProductName;
        this.loanProductId = loanProductId;
        this.isCreditCheckMandatory = isCreditCheckMandatory;
        this.skipCrediCheckInFailure = skipCrediCheckInFailure;
        this.stalePeriod = stalePeriod;
        this.is_active = is_active;
    }

    public static CreditBureauLpMappingData instance(final long creditbureauLoanProductMappingId, final long organisationCreditBureauId,
            final String alias,final String creditbureauSummary,final String loanProductName,final long loanProductId, final boolean isCreditCheckMandatory, final boolean skipCrediCheckInFailure, final long stalePeriod,
            final boolean is_active) {
        return new CreditBureauLpMappingData(creditbureauLoanProductMappingId,organisationCreditBureauId,alias,creditbureauSummary,loanProductName,loanProductId,
                isCreditCheckMandatory, skipCrediCheckInFailure, stalePeriod, is_active);
    }
    
    public static CreditBureauLpMappingData instance1(final String loanProductName,final long loanProductId) {
        return new CreditBureauLpMappingData(0,0,"","",loanProductName,loanProductId,
                false, false,0, false);
    }

    public long getCreditbureauLoanProductMappingId() {
        return this.creditbureauLoanProductMappingId;
    }
    

    
    public String getAlias() {
        return this.alias;
    }

    
    public String getCreditbureauSummary() {
        return this.creditbureauSummary;
    }

    
    public String getLoanProductName() {
        return this.loanProductName;
    }

    public long getOrganisationCreditBureauId() {
        return this.organisationCreditBureauId;
    }

    public long getLoanProductId() {
        return this.loanProductId;
    }

    public boolean isCreditCheckMandatory() {
        return this.isCreditCheckMandatory;
    }

    public boolean isSkipCrediCheckInFailure() {
        return this.skipCrediCheckInFailure;
    }

    public long getStalePeriod() {
        return this.stalePeriod;
    }

    public boolean isIs_active() {
        return this.is_active;
    }

}

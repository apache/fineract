package org.mifosng.platform.api.commands;

import java.math.BigDecimal;
import java.util.Set;

public class LoanChargeCommand {

    private final Long id;
    private final Long chargeId;
    private final Long loanId;
    private final BigDecimal amount;

    private final Integer chargeTimeType;
    private final Integer chargeCalculationType;

    private final Set<String> modifiedParameters;

    public LoanChargeCommand(Set<String> modifiedParameters, Long id, Long loanId, Long chargeId, BigDecimal amount, Integer chargeTimeType, Integer chargeCalculationType) {
        this.modifiedParameters = modifiedParameters;
        this.id = id;
        this.chargeId = chargeId;
        this.loanId = loanId;
        this.amount = amount;
        this.chargeTimeType = chargeTimeType;
        this.chargeCalculationType = chargeCalculationType;
    }

    public Long getId() {
        return id;
    }

    public Long getChargeId() {
        return chargeId;
    }

    public Long getLoanId() {
        return loanId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Integer getChargeTimeType() {
        return chargeTimeType;
    }

    public Integer getChargeCalculationType() {
        return chargeCalculationType;
    }

    public boolean isAmountChanged(){
        return this.modifiedParameters.contains("amount");
    }

    public boolean isChargeTimeTypeChanged(){
        return this.modifiedParameters.contains("chargeTimeType");
    }

    public boolean isChargeCalculationTypeChanged(){
        return this.modifiedParameters.contains("chargeCalculationType");
    }
}

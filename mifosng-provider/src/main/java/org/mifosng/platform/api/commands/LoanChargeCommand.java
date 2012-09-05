package org.mifosng.platform.api.commands;

import java.math.BigDecimal;
import java.util.Set;

public class LoanChargeCommand {

    private final Long id;
    private final BigDecimal amount;

    private final Integer chargeTimeType;
    private final Integer chargeCalculationType;

    private final Set<String> modifiedParameters;

    public LoanChargeCommand(final Set<String> modifiedParameters, final Long id, final BigDecimal amount,
                             final Integer chargeTimeType, final Integer chargeCalculationType) {
        this.modifiedParameters = modifiedParameters;
        this.id = id;
        this.amount = amount;
        this.chargeTimeType = chargeTimeType;
        this.chargeCalculationType = chargeCalculationType;
    }

    public Long getId() {
        return id;
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

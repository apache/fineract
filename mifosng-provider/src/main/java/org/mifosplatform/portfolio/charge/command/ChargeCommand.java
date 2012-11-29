package org.mifosplatform.portfolio.charge.command;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Immutable data object for creating and modifying defined charges.
 */
public class ChargeCommand {

    private final Long id;
    private final String name;
    private final BigDecimal amount;
    private final String currencyCode;

    private final Integer chargeTimeType;
    private final Integer chargeAppliesTo;
    private final Integer chargeCalculationType;

    private final boolean penalty;
    private final boolean active;

    private final Set<String> modifiedParameters;

    public ChargeCommand(
    		final Set<String> modifiedParameters, 
    		final Long id, 
    		final String name, 
    		final BigDecimal amount, 
    		final String currencyCode, 
    		final Integer chargeTimeType, 
    		final Integer chargeAppliesTo, 
    		final Integer chargeCalculationType,
    		final boolean penalty,
    		final boolean active) {
        this.id = id;
        this.name = name;
        this.amount = amount;
        this.currencyCode = currencyCode;
        this.chargeTimeType = chargeTimeType;
        this.chargeAppliesTo = chargeAppliesTo;
        this.chargeCalculationType = chargeCalculationType;
        this.modifiedParameters = modifiedParameters;
        this.penalty = penalty;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Set<String> getModifiedParameters() {
        return modifiedParameters;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public Integer getChargeCalculationType() {
        return chargeCalculationType;
    }

    public Integer getChargeAppliesTo() {
        return chargeAppliesTo;
    }

    public Integer getChargeTimeType() {
        return chargeTimeType;
    }
    
    public boolean isPenalty() {
		return penalty;
	}
    
    public boolean isFee() {
		return !penalty;
	}

	public boolean isActive() {
        return active;
    }

    public boolean isNameChanged(){
        return this.modifiedParameters.contains("name");
    }

    public boolean isAmountChanged(){
        return this.modifiedParameters.contains("amount");
    }

    public boolean isCurrencyCodeChanged(){
        return this.modifiedParameters.contains("currencyCode");
    }

    public boolean isChargeTimeTypeChanged(){
        return this.modifiedParameters.contains("chargeTimeType");
    }

    public boolean isChargeAppliesToChanged(){
        return this.modifiedParameters.contains("chargeAppliesTo");
    }

    public boolean isChargeCalculationTypeChanged(){
        return this.modifiedParameters.contains("chargeCalculationType");
    }

    public boolean isPenaltyChanged(){
        return this.modifiedParameters.contains("penalty");
    }
    
    public boolean isActiveChanged(){
        return this.modifiedParameters.contains("active");
    }
}
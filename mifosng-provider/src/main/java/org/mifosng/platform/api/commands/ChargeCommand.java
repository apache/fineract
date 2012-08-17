package org.mifosng.platform.api.commands;

import java.math.BigDecimal;
import java.util.Set;

public class ChargeCommand {

    private final Long id;
    private final String name;
    private final BigDecimal amount;

    private final Set<String> modifiedParameters;

    public ChargeCommand(Set<String> modifiedParameters, Long id, String name, BigDecimal amount) {
        this.id = id;
        this.name = name;
        this.amount = amount;
        this.modifiedParameters = modifiedParameters;
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

    public boolean isNameChanged(){
        return this.modifiedParameters.contains("name");
    }

    public boolean isAmountChanged(){
        return this.modifiedParameters.contains("amount");
    }
}

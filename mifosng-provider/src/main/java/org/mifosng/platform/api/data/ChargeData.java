package org.mifosng.platform.api.data;

import java.math.BigDecimal;
import java.util.List;

public class ChargeData {

    private Long id;
    private String name;
    private BigDecimal amount;
    private String currencyCode;
    private EnumOptionData chargeTimeType;
    private EnumOptionData chargeAppliesTo;
    private EnumOptionData chargeCalculationType;

    private boolean active;

    private List<CurrencyData> currencyOptions;
    private List<EnumOptionData> chargeCalculationTypeOptions;

    public ChargeData(Long id, String name, BigDecimal amount, String currencyCode, EnumOptionData chargeTimeType,
                      EnumOptionData chargeAppliesTo, EnumOptionData chargeCalculationType, boolean active) {
        this.id = id;
        this.name = name;
        this.amount = amount;
        this.currencyCode = currencyCode;
        this.chargeTimeType = chargeTimeType;
        this.chargeAppliesTo = chargeAppliesTo;
        this.chargeCalculationType = chargeCalculationType;
        this.active = active;
    }

    public ChargeData(EnumOptionData chargeTimeType, EnumOptionData chargeAppliesTo, List<CurrencyData> currencyOptions, List<EnumOptionData> chargeCalculationTypeOptions) {
        this.chargeTimeType = chargeTimeType;
        this.chargeAppliesTo = chargeAppliesTo;
        this.currencyOptions = currencyOptions;
        this.chargeCalculationTypeOptions = chargeCalculationTypeOptions;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public boolean isActive() {
        return active;
    }
}

package org.mifosng.platform.api.data;

import java.math.BigDecimal;

/**
 * Immutable data object for loan charge data.
 */
public class LoanChargeData {

    private final Long id;
    private final Long chargeId;

    private final String name;
    private final CurrencyData currency;

    private final BigDecimal amount;
    private final EnumOptionData chargeTimeType;
    private final EnumOptionData chargeCalculationType;

    public LoanChargeData(Long id, Long chargeId, String name, CurrencyData currency, BigDecimal amount,
                          EnumOptionData chargeTimeType, EnumOptionData chargeCalculationType) {
        this.id = id;
        this.chargeId = chargeId;
        this.name = name;
        this.currency = currency;
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

    public String getName() {
        return name;
    }

    public CurrencyData getCurrency() {
        return currency;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public EnumOptionData getChargeTimeType() {
        return chargeTimeType;
    }

    public EnumOptionData getChargeCalculationType() {
        return chargeCalculationType;
    }
}

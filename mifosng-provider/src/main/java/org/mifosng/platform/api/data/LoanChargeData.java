package org.mifosng.platform.api.data;

import java.math.BigDecimal;
import java.util.Collection;

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

    private final Collection<ChargeData> chargeOptions;

    public static LoanChargeData template(final Collection<ChargeData> chargeOptions){
        return new LoanChargeData(null,null,null,null,null,null,null, chargeOptions);
    }

    public LoanChargeData(Long id, Long chargeId, String name, CurrencyData currency, BigDecimal amount,
                          EnumOptionData chargeTimeType, EnumOptionData chargeCalculationType) {
        this.id = id;
        this.chargeId = chargeId;
        this.name = name;
        this.currency = currency;
        this.amount = amount;
        this.chargeTimeType = chargeTimeType;
        this.chargeCalculationType = chargeCalculationType;

        this.chargeOptions = null;
    }

    public LoanChargeData(Long id, Long chargeId, String name, CurrencyData currency, BigDecimal amount,
                          EnumOptionData chargeTimeType, EnumOptionData chargeCalculationType,
                          Collection<ChargeData> chargeOptions) {
        this.id = id;
        this.chargeId = chargeId;
        this.name = name;
        this.currency = currency;
        this.amount = amount;
        this.chargeTimeType = chargeTimeType;
        this.chargeCalculationType = chargeCalculationType;
        this.chargeOptions = chargeOptions;
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

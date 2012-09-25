package org.mifosng.platform.api.data;

import java.math.BigDecimal;
import java.util.Collection;

/**
 * Immutable data object for loan charge data.
 */
public class LoanChargeData {

	@SuppressWarnings("unused")
    private final Long id;
	@SuppressWarnings("unused")
    private final Long chargeId;
	@SuppressWarnings("unused")
    private final String name;
	@SuppressWarnings("unused")
    private final CurrencyData currency;
	@SuppressWarnings("unused")
    private final BigDecimal amount;
	@SuppressWarnings("unused")
    private final EnumOptionData chargeTimeType;
	@SuppressWarnings("unused")
    private final EnumOptionData chargeCalculationType;

    @SuppressWarnings("unused")
	private final Collection<ChargeData> chargeOptions;

    public static LoanChargeData template(final Collection<ChargeData> chargeOptions){
        return new LoanChargeData(null,null,null,null,null,null,null, chargeOptions);
    }

    public LoanChargeData(
    		final Long id, 
    		final Long chargeId, 
    		final String name, 
    		final CurrencyData currency, 
    		final BigDecimal amount,
    		final EnumOptionData chargeTimeType, 
    		final EnumOptionData chargeCalculationType) {
        this.id = id;
        this.chargeId = chargeId;
        this.name = name;
        this.currency = currency;
        this.amount = amount;
        this.chargeTimeType = chargeTimeType;
        this.chargeCalculationType = chargeCalculationType;

        this.chargeOptions = null;
    }

    public LoanChargeData(
    		final Long id, 
    		final Long chargeId, 
    		final String name, 
    		final CurrencyData currency, 
    		final BigDecimal amount,
    		final EnumOptionData chargeTimeType, 
    		final EnumOptionData chargeCalculationType,
            final Collection<ChargeData> chargeOptions) {
        this.id = id;
        this.chargeId = chargeId;
        this.name = name;
        this.currency = currency;
        this.amount = amount;
        this.chargeTimeType = chargeTimeType;
        this.chargeCalculationType = chargeCalculationType;
        this.chargeOptions = chargeOptions;
    }
}
package org.mifosng.platform.api.data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Immutable data object for charge data.
 */
public class ChargeData {

    private final Long id;
    private final String name;
    private final boolean active;
    private final CurrencyData currency;
    private final BigDecimal amount;
    private final EnumOptionData chargeTimeType;
    private final EnumOptionData chargeAppliesTo;
    private final EnumOptionData chargeCalculationType;

    private final List<CurrencyData> currencyOptions;
    private final List<EnumOptionData> chargeCalculationTypeOptions;
    
    public static ChargeData template(
    		final EnumOptionData chargeTimeType,
    		final EnumOptionData chargeAppliesToType,
    		final EnumOptionData chargeCalculationType,
    		final List<CurrencyData> currencyOptions,
    		final List<EnumOptionData> chargeCalculationTypeOptions) {
		return new ChargeData(null, null, null, null, chargeTimeType, chargeAppliesToType, chargeCalculationType, false, currencyOptions, chargeCalculationTypeOptions);
	}

	public ChargeData(final Long id, final String name,
			final BigDecimal amount, final CurrencyData currency,
			final EnumOptionData chargeTimeType,
			final EnumOptionData chargeAppliesTo,
			final EnumOptionData chargeCalculationType, final boolean active) {
		this.id = id;
		this.name = name;
		this.amount = amount;
		this.currency = currency;
		this.chargeTimeType = chargeTimeType;
		this.chargeAppliesTo = chargeAppliesTo;
		this.chargeCalculationType = chargeCalculationType;
		this.active = active;
		this.currencyOptions = null;
		this.chargeCalculationTypeOptions = null;
	}

	public ChargeData(final Long id, final String name,
			final BigDecimal amount, final CurrencyData currency,
			final EnumOptionData chargeTimeType,
			final EnumOptionData chargeAppliesTo,
			final EnumOptionData chargeCalculationType, final boolean active,
			final List<CurrencyData> currencyOptions,
			final List<EnumOptionData> chargeCalculationTypeOptions) {
		this.id = id;
		this.name = name;
		this.amount = amount;
		this.currency = currency;
		this.chargeTimeType = chargeTimeType;
		this.chargeAppliesTo = chargeAppliesTo;
		this.chargeCalculationType = chargeCalculationType;
		this.active = active;
		this.currencyOptions = currencyOptions;
		this.chargeCalculationTypeOptions = chargeCalculationTypeOptions;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public boolean isActive() {
		return active;
	}

	public BigDecimal getAmount() {
		return amount;
	}
	
	public CurrencyData getCurrency() {
		return currency;
	}

	public EnumOptionData getChargeTimeType() {
		return chargeTimeType;
	}

	public EnumOptionData getChargeAppliesTo() {
		return chargeAppliesTo;
	}

	public EnumOptionData getChargeCalculationType() {
		return chargeCalculationType;
	}

	public List<CurrencyData> getCurrencyOptions() {
		return currencyOptions;
	}

	public List<EnumOptionData> getChargeCalculationTypeOptions() {
		return chargeCalculationTypeOptions;
	}
}
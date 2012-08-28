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
    private final List<EnumOptionData> chargeAppliesToOptions;
    private final List<EnumOptionData> chargeTimeTypeOptions;
    
    public static ChargeData template(
            final CurrencyData currency,
    		final EnumOptionData chargeTimeType,
    		final EnumOptionData chargeAppliesToType,
    		final EnumOptionData chargeCalculationType,
    		final List<CurrencyData> currencyOptions,
    		final List<EnumOptionData> chargeCalculationTypeOptions,
            final List<EnumOptionData> chargeAppliesToOptions,
            final List<EnumOptionData> chargeTimeTypeOptions) {
		return new ChargeData(null, null, null, currency, chargeTimeType, chargeAppliesToType, chargeCalculationType, false,
                currencyOptions, chargeCalculationTypeOptions, chargeAppliesToOptions, chargeTimeTypeOptions);
	}

    public ChargeData(ChargeData charge, ChargeData template){
        this(charge.getId(), charge.getName(), charge.getAmount(), charge.getCurrency(), charge.getChargeTimeType(),
                charge.getChargeAppliesTo(), charge.getChargeCalculationType(), charge.isActive(),
                template.getCurrencyOptions(), template.getChargeCalculationTypeOptions(),
                template.getChargeAppliesToOptions(), template.getChargeTimeTypeOptions());
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
        this.chargeAppliesToOptions = null;
        this.chargeTimeTypeOptions = null;
	}

	public ChargeData(final Long id, final String name,
			final BigDecimal amount, final CurrencyData currency,
			final EnumOptionData chargeTimeType,
			final EnumOptionData chargeAppliesTo,
			final EnumOptionData chargeCalculationType, final boolean active,
			final List<CurrencyData> currencyOptions,
			final List<EnumOptionData> chargeCalculationTypeOptions,
            final List<EnumOptionData> chargeAppliesToOptions,
            final List<EnumOptionData> chargeTimeTypeOptions) {
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
        this.chargeAppliesToOptions = chargeAppliesToOptions;
        this.chargeTimeTypeOptions = chargeTimeTypeOptions;
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

    public List<EnumOptionData> getChargeAppliesToOptions() {
        return chargeAppliesToOptions;
    }

    public List<EnumOptionData> getChargeTimeTypeOptions() {
        return chargeTimeTypeOptions;
    }
}
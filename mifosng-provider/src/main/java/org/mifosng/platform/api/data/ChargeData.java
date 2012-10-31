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
    private final boolean penalty;
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
            final List<CurrencyData> currencyOptions,
            final List<EnumOptionData> chargeCalculationTypeOptions,
            final List<EnumOptionData> chargeAppliesToOptions,
            final List<EnumOptionData> chargeTimeTypeOptions) {
    	
        return new ChargeData(null, null, null, null, null, null, null, false, false,
                currencyOptions, chargeCalculationTypeOptions, chargeAppliesToOptions, chargeTimeTypeOptions);
    }

    public ChargeData(final ChargeData charge, final ChargeData template){
        this(charge.id, charge.name, charge.amount, charge.currency, charge.chargeTimeType,
                charge.chargeAppliesTo, charge.chargeCalculationType, 
                charge.penalty, charge.active,
                template.currencyOptions, template.chargeCalculationTypeOptions,
                template.chargeAppliesToOptions, template.chargeTimeTypeOptions);
    }

	public ChargeData(final Long id, final String name,
			final BigDecimal amount, final CurrencyData currency,
			final EnumOptionData chargeTimeType,
			final EnumOptionData chargeAppliesTo,
			final EnumOptionData chargeCalculationType, final boolean penalty, final boolean active) {
		this.id = id;
		this.name = name;
		this.amount = amount;
		this.currency = currency;
		this.chargeTimeType = chargeTimeType;
		this.chargeAppliesTo = chargeAppliesTo;
		this.chargeCalculationType = chargeCalculationType;
		this.penalty = penalty;
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
			final EnumOptionData chargeCalculationType, 
			final boolean penalty,
			final boolean active,
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
		this.penalty = penalty;
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

	public boolean isPenalty() {
		return this.penalty;
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

	public EnumOptionData getChargeCalculationType() {
		return chargeCalculationType;
	}

    @Override
    public boolean equals(Object obj) {
        ChargeData chargeData = (ChargeData) obj;
        return this.id.equals(chargeData.id);
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }
}
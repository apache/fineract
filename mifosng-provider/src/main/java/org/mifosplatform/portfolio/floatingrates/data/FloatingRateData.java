/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.floatingrates.data;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;

public class FloatingRateData implements Comparable<FloatingRateData> {

	private final Long id;
	private final String name;
	private final boolean isBaseLendingRate;
	private final boolean isActive;
	private final String createdBy;
	private final Date createdOn;
	private final String modifiedBy;
	private final Date modifiedOn;
	private final List<FloatingRatePeriodData> ratePeriods;
	@SuppressWarnings("unused")
	private final List<EnumOptionData> interestRateFrequencyTypeOptions;

	public FloatingRateData(Long id, String name, boolean isBaseLendingRate,
			boolean isActive, String createdBy, Date createdOn,
			String modifiedBy, Date modifiedOn,
			List<FloatingRatePeriodData> ratePeriods,
			List<EnumOptionData> interestRateFrequencyTypeOptions) {
		this.id = id;
		this.name = name;
		this.isBaseLendingRate = isBaseLendingRate;
		this.isActive = isActive;
		this.createdBy = createdBy;
		this.createdOn = createdOn;
		this.modifiedBy = modifiedBy;
		this.modifiedOn = modifiedOn;
		this.ratePeriods = ratePeriods;
		this.interestRateFrequencyTypeOptions = interestRateFrequencyTypeOptions;
	}

	public Long getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public boolean isBaseLendingRate() {
		return this.isBaseLendingRate;
	}

	public boolean isActive() {
		return this.isActive;
	}

	public String getCreatedBy() {
		return this.createdBy;
	}

	public Date getCreatedOn() {
		return this.createdOn;
	}

	public String getModifiedBy() {
		return this.modifiedBy;
	}

	public Date getModifiedOn() {
		return this.modifiedOn;
	}

	public List<FloatingRatePeriodData> getRatePeriods() {
		return this.ratePeriods;
	}

	@Override
	public int compareTo(final FloatingRateData obj) {
		if (obj == null) {
			return -1;
		}
		return new CompareToBuilder() //
				.append(this.id, obj.id) //
				.append(this.name, obj.name) //
				.append(this.isBaseLendingRate, obj.isBaseLendingRate) //
				.append(this.isActive, obj.isActive) //
				.toComparison();
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (obj.getClass() != getClass()) {
			return false;
		}
		final FloatingRateData rhs = (FloatingRateData) obj;
		return new EqualsBuilder() //
				.append(this.id, rhs.id) //
				.append(this.name, rhs.name) //
				.append(this.isBaseLendingRate, rhs.isBaseLendingRate) //
				.append(this.isActive, rhs.isActive) //
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37) //
				.append(this.id) //
				.append(this.name) //
				.append(this.isBaseLendingRate) //
				.append(this.isActive) //
				.toHashCode();
	}

	public static FloatingRateData toTemplate(
			List<EnumOptionData> interestRateFrequencyTypeOptions) {
		// TODO Auto-generated method stub
		return new FloatingRateData(null, null, false, true, null, null, null,
				null, null, interestRateFrequencyTypeOptions);
	}
}

/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.floatingrates.data;

import java.math.BigDecimal;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.joda.time.LocalDate;

public class FloatingRatePeriodData implements
		Comparable<FloatingRatePeriodData> {

	private Long id;
	private LocalDate fromDate;
	private BigDecimal interestRate;
	private boolean isDifferentialToBaseLendingRate;
	private boolean isActive;
	private String createdBy;
	private LocalDate createdOn;
	private String modifiedBy;
	private LocalDate modifiedOn;

	public FloatingRatePeriodData(Long id, LocalDate fromDate,
			BigDecimal interestRate, boolean isDifferentialToBaseLendingRate,
			boolean isActive, String createdBy, LocalDate createdOn,
			String modifiedBy, LocalDate modifiedOn) {
		this.id = id;
		this.fromDate = fromDate;
		this.interestRate = interestRate;
		this.isDifferentialToBaseLendingRate = isDifferentialToBaseLendingRate;
		this.isActive = isActive;
		this.createdBy = createdBy;
		this.createdOn = createdOn;
		this.modifiedBy = modifiedBy;
		this.modifiedOn = modifiedOn;
	}

	public Long getId() {
		return this.id;
	}

	public LocalDate getFromDate() {
		return this.fromDate;
	}

	public LocalDate getFromDateAsLocalDate() {
		return new LocalDate(this.fromDate);
	}

	public BigDecimal getInterestRate() {
		return this.interestRate;
	}

	public boolean isDifferentialToBaseLendingRate() {
		return this.isDifferentialToBaseLendingRate;
	}

	public boolean isActive() {
		return this.isActive;
	}

	public String getCreatedBy() {
		return this.createdBy;
	}

	public LocalDate getCreatedOn() {
		return this.createdOn;
	}

	public String getModifiedBy() {
		return this.modifiedBy;
	}

	public LocalDate getModifiedOn() {
		return this.modifiedOn;
	}

	@Override
	public int compareTo(final FloatingRatePeriodData obj) {
		if (obj == null) {
			return -1;
		}
		return new CompareToBuilder() //
				.append(this.id, obj.id) //
				.append(this.fromDate, obj.fromDate) //
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
		final FloatingRatePeriodData rhs = (FloatingRatePeriodData) obj;
		return new EqualsBuilder() //
				.append(this.id, rhs.id) //
				.append(this.fromDate, rhs.fromDate) //
				.append(this.isActive, rhs.isActive) //
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37) //
				.append(this.id) //
				.append(this.fromDate) //
				.append(this.isActive) //
				.toHashCode();
	}
}

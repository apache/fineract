/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.floatingrates.domain;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.joda.time.LocalDate;
import org.mifosplatform.portfolio.floatingrates.data.FloatingRateDTO;
import org.mifosplatform.portfolio.floatingrates.data.FloatingRatePeriodData;
import org.mifosplatform.useradministration.domain.AppUser;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_floating_rates_periods")
public class FloatingRatePeriod extends AbstractPersistable<Long> {

	@ManyToOne
	@JoinColumn(name = "floating_rates_id", nullable = false)
	private FloatingRate floatingRate;

	@Column(name = "from_date", nullable = false)
	private Date fromDate;

	@Column(name = "interest_rate", scale = 6, precision = 19, nullable = false)
	private BigDecimal interestRate;

	@Column(name = "is_differential_to_base_lending_rate", nullable = false)
	private boolean isDifferentialToBaseLendingRate;

	@Column(name = "is_active", nullable = false)
	private boolean isActive;

	@ManyToOne(optional = true)
	@JoinColumn(name = "createdby_id", nullable = false)
	private AppUser createdBy;

	@ManyToOne(optional = true)
	@JoinColumn(name = "lastmodifiedby_id", nullable = false)
	private AppUser modifiedBy;

	@Column(name = "created_date", nullable = false)
	private Date createdOn;

	@Column(name = "lastmodified_date", nullable = false)
	private Date modifiedOn;

	public FloatingRatePeriod() {

	}

	public FloatingRatePeriod(Date fromDate, BigDecimal interestRate,
			boolean isDifferentialToBaseLendingRate, boolean isActive,
			AppUser createdBy, AppUser modifiedBy, Date createdOn,
			Date modifiedOn) {
		this.fromDate = fromDate;
		this.interestRate = interestRate;
		this.isDifferentialToBaseLendingRate = isDifferentialToBaseLendingRate;
		this.isActive = isActive;
		this.createdBy = createdBy;
		this.modifiedBy = modifiedBy;
		this.createdOn = createdOn;
		this.modifiedOn = modifiedOn;
	}

	public void updateFloatingRate(FloatingRate floatingRate) {
		this.floatingRate = floatingRate;
	}

	public FloatingRate getFloatingRatesId() {
		return this.floatingRate;
	}

	public Date getFromDate() {
		return this.fromDate;
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

	public AppUser getCreatedBy() {
		return this.createdBy;
	}

	public AppUser getModifiedBy() {
		return this.modifiedBy;
	}

	public Date getCreatedOn() {
		return this.createdOn;
	}

	public Date getModifiedOn() {
		return this.modifiedOn;
	}

	public void setModifiedBy(AppUser modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public void setModifiedOn(Date modifiedOn) {
		this.modifiedOn = modifiedOn;
	}

	public void setActive(boolean b) {
		this.isActive = b;
	}

	public LocalDate fetchFromDate() {
		return new LocalDate(this.fromDate);
	}

	public FloatingRatePeriodData toData(final FloatingRateDTO floatingRateDTO) {

		BigDecimal interest = getInterestRate().add(
				floatingRateDTO.getInterestRateDiff());
		if (isDifferentialToBaseLendingRate()) {
			interest = interest.add(floatingRateDTO
					.fetchBaseRate(fetchFromDate()));
		}

		return new FloatingRatePeriodData(getId(), getFromDate(), interest,
				isDifferentialToBaseLendingRate(), isActive(), getCreatedBy()
						.getUsername(), getCreatedOn(), getModifiedBy()
						.getUsername(), getModifiedOn());
	}

}

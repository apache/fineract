/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.portfolio.floatingrates.data;

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

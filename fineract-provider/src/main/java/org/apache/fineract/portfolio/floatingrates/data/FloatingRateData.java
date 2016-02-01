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

import java.util.List;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.joda.time.LocalDate;

public class FloatingRateData implements Comparable<FloatingRateData> {

	private final Long id;
	private final String name;
	private final boolean isBaseLendingRate;
	private final boolean isActive;
	private final String createdBy;
	private final LocalDate createdOn;
	private final String modifiedBy;
	private final LocalDate modifiedOn;
	private final List<FloatingRatePeriodData> ratePeriods;
	@SuppressWarnings("unused")
	private final List<EnumOptionData> interestRateFrequencyTypeOptions;

	public FloatingRateData(Long id, String name, boolean isBaseLendingRate,
			boolean isActive, String createdBy, LocalDate createdOn,
			String modifiedBy, LocalDate modifiedOn,
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

	public LocalDate getCreatedOn() {
		return this.createdOn;
	}

	public String getModifiedBy() {
		return this.modifiedBy;
	}

	public LocalDate getModifiedOn() {
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

/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.floatingrates.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.serialization.JsonParserHelper;
import org.mifosplatform.infrastructure.core.service.DateUtils;
import org.mifosplatform.portfolio.floatingrates.data.FloatingRateDTO;
import org.mifosplatform.portfolio.floatingrates.data.FloatingRatePeriodData;
import org.mifosplatform.useradministration.domain.AppUser;
import org.springframework.data.jpa.domain.AbstractPersistable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@Entity
@Table(name = "m_floating_rates", uniqueConstraints = { @UniqueConstraint(columnNames = { "name" }, name = "unq_name") })
public class FloatingRate extends AbstractPersistable<Long> {

	@Column(name = "name", length = 200, unique = true, nullable = false)
	private String name;

	@Column(name = "is_base_lending_rate", nullable = false)
	private boolean isBaseLendingRate;

	@Column(name = "is_active", nullable = false)
	private boolean isActive;

	@OrderBy(value = "fromDate,id")
	@LazyCollection(LazyCollectionOption.FALSE)
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "floatingRate", orphanRemoval = true)
	private Set<FloatingRatePeriod> floatingRatePeriods;

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

	public FloatingRate() {

	}

	public FloatingRate(String name, boolean isBaseLendingRate,
			boolean isActive, Set<FloatingRatePeriod> floatingRatePeriods,
			AppUser createdBy, AppUser modifiedBy, Date createdOn,
			Date modifiedOn) {
		this.name = name;
		this.isBaseLendingRate = isBaseLendingRate;
		this.isActive = isActive;
		this.floatingRatePeriods = floatingRatePeriods;
		this.createdBy = createdBy;
		this.createdOn = createdOn;
		this.modifiedBy = modifiedBy;
		this.modifiedOn = modifiedOn;
		if (floatingRatePeriods != null) {
			for (FloatingRatePeriod ratePeriod : floatingRatePeriods) {
				ratePeriod.updateFloatingRate(this);
			}
		}
	}

	public static FloatingRate createNew(AppUser currentUser,
			JsonCommand command) {

		final String name = command.stringValueOfParameterNamed("name");
		final boolean isBaseLendingRate = command
				.parameterExists("isBaseLendingRate") ? command
				.booleanPrimitiveValueOfParameterNamed("isBaseLendingRate")
				: false;
		final boolean isActive = command.parameterExists("isActive") ? command
				.booleanPrimitiveValueOfParameterNamed("isActive") : true;
		final Set<FloatingRatePeriod> floatingRatePeriods = getRatePeriods(
				currentUser, command);
		final LocalDate currentDate = DateUtils.getLocalDateOfTenant();

		return new FloatingRate(name, isBaseLendingRate, isActive,
				floatingRatePeriods, currentUser, currentUser,
				currentDate.toDate(), currentDate.toDate());
	}

	private static Set<FloatingRatePeriod> getRatePeriods(
			final AppUser currentUser, final JsonCommand command) {
		if (!command.parameterExists("ratePeriods")) {
			return null;
		}
		Set<FloatingRatePeriod> ratePeriods = new HashSet<>();
		JsonArray arrayOfParameterNamed = command
				.arrayOfParameterNamed("ratePeriods");
		for (final JsonElement ratePeriod : arrayOfParameterNamed) {
			final JsonObject ratePeriodObject = ratePeriod.getAsJsonObject();
			final JsonParserHelper helper = new JsonParserHelper();
			final Date fromDate = helper.extractLocalDateNamed("fromDate",
					ratePeriod, new HashSet<String>()).toDate();
			final BigDecimal interestRate = ratePeriodObject
					.get("interestRate").getAsBigDecimal();
			final boolean isDifferentialToBaseLendingRate = helper
					.parameterExists("isDifferentialToBaseLendingRate",
							ratePeriod) ? ratePeriodObject.get(
					"isDifferentialToBaseLendingRate").getAsBoolean() : false;
			final boolean isActive = true;
			final Date currentDate = DateUtils.getDateOfTenant();
			ratePeriods.add(new FloatingRatePeriod(fromDate, interestRate,
					isDifferentialToBaseLendingRate, isActive, currentUser,
					currentUser, currentDate, currentDate));
		}

		return ratePeriods;
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

	public Set<FloatingRatePeriod> getFloatingRatePeriods() {
		return this.floatingRatePeriods;
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

	public Map<String, Object> update(final JsonCommand command,
			final AppUser appUser) {

		final Map<String, Object> actualChanges = new LinkedHashMap<>(9);

		if (command.isChangeInStringParameterNamed("name", this.name)) {
			final String newValue = command.stringValueOfParameterNamed("name");
			actualChanges.put("name", newValue);
			this.name = newValue;
		}

		if (command.isChangeInBooleanParameterNamed("isBaseLendingRate",
				this.isBaseLendingRate)) {
			final boolean newValue = command
					.booleanPrimitiveValueOfParameterNamed("isBaseLendingRate");
			actualChanges.put("isBaseLendingRate", newValue);
			this.isBaseLendingRate = newValue;
		}

		if (command.isChangeInBooleanParameterNamed("isActive", this.isActive)) {
			final boolean newValue = command
					.booleanPrimitiveValueOfParameterNamed("isActive");
			actualChanges.put("isActive", newValue);
			this.isActive = newValue;
		}

		final Set<FloatingRatePeriod> newRatePeriods = getRatePeriods(appUser,
				command);
		if (newRatePeriods != null && !newRatePeriods.isEmpty()) {
			updateRatePeriods(newRatePeriods, appUser);
			actualChanges.put("ratePeriods",
					command.jsonFragment("ratePeriods"));
		}

		return actualChanges;
	}

	private void updateRatePeriods(
			final Set<FloatingRatePeriod> newRatePeriods, final AppUser appUser) {
		final LocalDate today = DateUtils.getLocalDateOfTenant();
		if (this.floatingRatePeriods != null) {
			for (FloatingRatePeriod ratePeriod : this.floatingRatePeriods) {
				LocalDate fromDate = LocalDate.fromDateFields(ratePeriod
						.getFromDate());
				if (fromDate.isAfter(today)) {
					ratePeriod.setActive(false);
					ratePeriod.setModifiedBy(appUser);
					ratePeriod.setModifiedOn(today.toDate());
				}
			}
		}
		for (FloatingRatePeriod newRatePeriod : newRatePeriods) {
			newRatePeriod.updateFloatingRate(this);
			this.floatingRatePeriods.add(newRatePeriod);
		}
	}

	public Collection<FloatingRatePeriodData> fetchInterestRates(
			final FloatingRateDTO floatingRateDTO) {
		Collection<FloatingRatePeriodData> applicableRates = new ArrayList<>();
		FloatingRatePeriod previousPeriod = null;
		boolean addPeriodData = false;
		for (FloatingRatePeriod floatingRatePeriod : this.floatingRatePeriods) {
			if (floatingRatePeriod.isActive()) {
				// will enter
				if (applicableRates.isEmpty()
						&& floatingRateDTO.getStartDate().isBefore(
								floatingRatePeriod.fetchFromDate())) {
					if (floatingRateDTO.isFloatingInterestRate()) {
						addPeriodData = true;
					}
					if (previousPeriod != null) {
						applicableRates.add(previousPeriod
								.toData(floatingRateDTO));
					} else if (!addPeriodData) {
						applicableRates.add(floatingRatePeriod
								.toData(floatingRateDTO));
					}
				}
				if (addPeriodData) {
					applicableRates.add(floatingRatePeriod
							.toData(floatingRateDTO));
				}
				previousPeriod = floatingRatePeriod;
			}
		}
		if (applicableRates.isEmpty() && previousPeriod != null) {
			applicableRates.add(previousPeriod.toData(floatingRateDTO));
		}
		return applicableRates;
	}

}

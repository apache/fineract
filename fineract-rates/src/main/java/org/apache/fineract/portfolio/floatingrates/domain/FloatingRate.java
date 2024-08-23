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
package org.apache.fineract.portfolio.floatingrates.domain;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;
import org.apache.fineract.infrastructure.core.serialization.JsonParserHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.floatingrates.data.FloatingRateDTO;
import org.apache.fineract.portfolio.floatingrates.data.FloatingRatePeriodData;

@Entity
@Table(name = "m_floating_rates", uniqueConstraints = { @UniqueConstraint(columnNames = { "name" }, name = "unq_name") })
public class FloatingRate extends AbstractAuditableWithUTCDateTimeCustom<Long> {

    @Column(name = "name", length = 200, unique = true, nullable = false)
    private String name;

    @Column(name = "is_base_lending_rate", nullable = false)
    private boolean isBaseLendingRate;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @OrderBy(value = "fromDate,id")
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "floatingRate", orphanRemoval = true, fetch = FetchType.EAGER)
    private List<FloatingRatePeriod> floatingRatePeriods;

    /*
     * Deprecated since common Auditable fields were introduced. Columns and data left untouched to help migration.
     *
     * @Column(name = "created_date", nullable = false) private LocalDateTime createdOn;
     *
     * @Column(name = "lastmodified_date", nullable = false) private LocalDateTime modifiedOn;
     */

    public FloatingRate() {

    }

    public FloatingRate(String name, boolean isBaseLendingRate, boolean isActive, List<FloatingRatePeriod> floatingRatePeriods) {
        this.name = name;
        this.isBaseLendingRate = isBaseLendingRate;
        this.isActive = isActive;
        this.floatingRatePeriods = floatingRatePeriods;
        if (floatingRatePeriods != null) {
            for (FloatingRatePeriod ratePeriod : floatingRatePeriods) {
                ratePeriod.updateFloatingRate(this);
            }
        }
    }

    public static FloatingRate createNew(JsonCommand command) {

        final String name = command.stringValueOfParameterNamed("name");
        final boolean isBaseLendingRate = command.parameterExists("isBaseLendingRate")
                && command.booleanPrimitiveValueOfParameterNamed("isBaseLendingRate");
        final boolean isActive = !command.parameterExists("isActive") || command.booleanPrimitiveValueOfParameterNamed("isActive");
        final List<FloatingRatePeriod> floatingRatePeriods = getRatePeriods(command);

        return new FloatingRate(name, isBaseLendingRate, isActive, floatingRatePeriods);
    }

    private static List<FloatingRatePeriod> getRatePeriods(final JsonCommand command) {
        if (!command.parameterExists("ratePeriods")) {
            return null;
        }
        List<FloatingRatePeriod> ratePeriods = new ArrayList<>();
        JsonArray arrayOfParameterNamed = command.arrayOfParameterNamed("ratePeriods");
        for (final JsonElement ratePeriod : arrayOfParameterNamed) {
            final JsonObject ratePeriodObject = ratePeriod.getAsJsonObject();
            final JsonParserHelper helper = new JsonParserHelper();
            final LocalDate fromDate = helper.extractLocalDateNamed("fromDate", ratePeriod, new HashSet<String>());
            final BigDecimal interestRate = ratePeriodObject.get("interestRate").getAsBigDecimal();
            final boolean isDifferentialToBaseLendingRate = helper.parameterExists("isDifferentialToBaseLendingRate", ratePeriod)
                    && ratePeriodObject.get("isDifferentialToBaseLendingRate").getAsBoolean();
            final boolean isActive = true;
            ratePeriods.add(new FloatingRatePeriod(fromDate, interestRate, isDifferentialToBaseLendingRate, isActive));
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

    public List<FloatingRatePeriod> getFloatingRatePeriods() {
        return this.floatingRatePeriods;
    }

    public Map<String, Object> update(final JsonCommand command) {

        final Map<String, Object> actualChanges = new LinkedHashMap<>(9);

        if (command.isChangeInStringParameterNamed("name", this.name)) {
            final String newValue = command.stringValueOfParameterNamed("name");
            actualChanges.put("name", newValue);
            this.name = newValue;
        }

        if (command.isChangeInBooleanParameterNamed("isBaseLendingRate", this.isBaseLendingRate)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed("isBaseLendingRate");
            actualChanges.put("isBaseLendingRate", newValue);
            this.isBaseLendingRate = newValue;
        }

        if (command.isChangeInBooleanParameterNamed("isActive", this.isActive)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed("isActive");
            actualChanges.put("isActive", newValue);
            this.isActive = newValue;
        }

        final List<FloatingRatePeriod> newRatePeriods = getRatePeriods(command);
        if (newRatePeriods != null && !newRatePeriods.isEmpty()) {
            updateRatePeriods(newRatePeriods);
            actualChanges.put("ratePeriods", command.jsonFragment("ratePeriods"));
        }

        return actualChanges;
    }

    private void updateRatePeriods(final List<FloatingRatePeriod> newRatePeriods) {
        final LocalDate today = DateUtils.getBusinessLocalDate();
        if (this.floatingRatePeriods != null) {
            for (FloatingRatePeriod ratePeriod : this.floatingRatePeriods) {
                LocalDate fromDate = ratePeriod.getFromDate();
                if (DateUtils.isAfter(fromDate, today)) {
                    ratePeriod.setActive(false);
                }
            }
        }
        for (FloatingRatePeriod newRatePeriod : newRatePeriods) {
            newRatePeriod.updateFloatingRate(this);
            this.floatingRatePeriods.add(newRatePeriod);
        }
    }

    public Collection<FloatingRatePeriodData> fetchInterestRates(final FloatingRateDTO floatingRateDTO) {
        Collection<FloatingRatePeriodData> applicableRates = new ArrayList<>();
        FloatingRatePeriod previousPeriod = null;
        boolean addPeriodData = false;
        for (FloatingRatePeriod floatingRatePeriod : this.floatingRatePeriods) {
            if (floatingRatePeriod.isActive()) {
                // will enter
                if (applicableRates.isEmpty() && DateUtils.isBefore(floatingRateDTO.getStartDate(), floatingRatePeriod.fetchFromDate())) {
                    if (floatingRateDTO.isFloatingInterestRate()) {
                        addPeriodData = true;
                    }
                    if (previousPeriod != null) {
                        applicableRates.add(previousPeriod.toData(floatingRateDTO));
                    } else if (!addPeriodData) {
                        applicableRates.add(floatingRatePeriod.toData(floatingRateDTO));
                    }
                }
                if (addPeriodData) {
                    applicableRates.add(floatingRatePeriod.toData(floatingRateDTO));
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

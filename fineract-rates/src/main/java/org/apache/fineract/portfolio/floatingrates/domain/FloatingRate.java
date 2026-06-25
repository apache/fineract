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
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.floatingrates.data.FloatingRateDTO;
import org.apache.fineract.portfolio.floatingrates.data.FloatingRatePeriodData;
import org.apache.fineract.portfolio.floatingrates.data.FloatingRatePeriodRequest;
import org.apache.fineract.portfolio.floatingrates.data.FloatingRateRequest;
import org.apache.fineract.portfolio.floatingrates.data.FloatingRateUpdateRequest;

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

    public static FloatingRate createNew(final FloatingRateRequest request) {

        final String name = request.getName();
        final boolean isBaseLendingRate = Boolean.TRUE.equals(request.getIsBaseLendingRate());
        final boolean isActive = request.getIsActive() == null || request.getIsActive();
        final List<FloatingRatePeriod> floatingRatePeriods = getRatePeriods(request.getRatePeriods());

        return new FloatingRate(name, isBaseLendingRate, isActive, floatingRatePeriods);
    }

    private static List<FloatingRatePeriod> getRatePeriods(final List<FloatingRatePeriodRequest> ratePeriodRequests) {
        if (ratePeriodRequests == null) {
            return null;
        }
        final List<FloatingRatePeriod> ratePeriods = new ArrayList<>();
        for (final FloatingRatePeriodRequest ratePeriod : ratePeriodRequests) {
            final LocalDate fromDate = parseDate(ratePeriod.getFromDate(), ratePeriod.getDateFormat(), ratePeriod.getLocale());
            final BigDecimal interestRate = ratePeriod.getInterestRate();
            final boolean isDifferentialToBaseLendingRate = Boolean.TRUE.equals(ratePeriod.getIsDifferentialToBaseLendingRate());
            final boolean isActive = true;
            ratePeriods.add(new FloatingRatePeriod(fromDate, interestRate, isDifferentialToBaseLendingRate, isActive));
        }

        return ratePeriods;
    }

    private static LocalDate parseDate(final String value, final String dateFormat, final String locale) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        if (StringUtils.isBlank(dateFormat)) {
            return LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE);
        }
        final Locale loc = StringUtils.isBlank(locale) ? Locale.getDefault() : Locale.forLanguageTag(locale.replace('_', '-'));
        return LocalDate.parse(value, new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern(dateFormat).toFormatter(loc));
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

    public Map<String, Object> update(final FloatingRateUpdateRequest request) {

        final Map<String, Object> actualChanges = new LinkedHashMap<>(9);

        if (request.getName() != null && !request.getName().equals(this.name)) {
            this.name = request.getName();
            actualChanges.put("name", this.name);
        }

        if (request.getIsBaseLendingRate() != null && request.getIsBaseLendingRate() != this.isBaseLendingRate) {
            this.isBaseLendingRate = request.getIsBaseLendingRate();
            actualChanges.put("isBaseLendingRate", this.isBaseLendingRate);
        }

        if (request.getIsActive() != null && request.getIsActive() != this.isActive) {
            this.isActive = request.getIsActive();
            actualChanges.put("isActive", this.isActive);
        }

        final List<FloatingRatePeriod> newRatePeriods = getRatePeriods(request.getRatePeriods());
        if (newRatePeriods != null && !newRatePeriods.isEmpty()) {
            updateRatePeriods(newRatePeriods);
            actualChanges.put("ratePeriods", request.getRatePeriods());
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

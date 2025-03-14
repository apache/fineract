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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import lombok.Getter;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FloatingRatePeriodData implements Comparable<FloatingRatePeriodData>, Serializable {

    private Long id;
    private LocalDate fromDate;
    private BigDecimal interestRate;
    private Boolean isDifferentialToBaseLendingRate;
    private Boolean isActive;
    private String createdBy;
    private OffsetDateTime createdOn;
    private String modifiedBy;
    private OffsetDateTime modifiedOn;

    public FloatingRatePeriodData(Long id, LocalDate fromDate, BigDecimal interestRate, Boolean isDifferentialToBaseLendingRate,
            Boolean isActive, String createdBy, OffsetDateTime createdOn, String modifiedBy, OffsetDateTime modifiedOn) {
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

    public FloatingRatePeriodData(Long id, LocalDate fromDate, BigDecimal interestRate, Boolean isDifferentialToBaseLendingRate,
            Boolean isActive) {
        this.id = id;
        this.fromDate = fromDate;
        this.interestRate = interestRate;
        this.isDifferentialToBaseLendingRate = isDifferentialToBaseLendingRate;
        this.isActive = isActive;
    }

    @JsonIgnore
    public LocalDate getFromDateAsLocalDate() {
        return this.fromDate;
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
        if (!(obj instanceof FloatingRatePeriodData)) {
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

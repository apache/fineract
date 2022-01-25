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
package org.apache.fineract.portfolio.tax.data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

public class TaxComponentHistoryData implements Serializable {

    @SuppressWarnings("unused")
    private final BigDecimal percentage;
    @SuppressWarnings("unused")
    private final LocalDate startDate;
    @SuppressWarnings("unused")
    private final LocalDate endDate;

    public TaxComponentHistoryData(final BigDecimal percentage, final LocalDate startDate, final LocalDate endDate) {
        this.percentage = percentage;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public boolean occursOnDayFromAndUpToAndIncluding(final LocalDate target) {
        if (this.endDate == null) {
            return target != null && target.isAfter(startDate());
        }
        return target != null && target.isAfter(startDate()) && !target.isAfter(endDate());
    }

    public LocalDate startDate() {
        LocalDate startDate = null;
        if (this.startDate != null) {
            startDate = this.startDate;
        }
        return startDate;
    }

    public LocalDate endDate() {
        LocalDate endDate = null;
        if (this.endDate != null) {
            endDate = this.endDate;
        }
        return endDate;
    }

    public BigDecimal getPercentage() {
        return this.percentage;
    }
}

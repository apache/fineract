/**
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. The ASF licenses this file to you under the Apache
 * License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.apache.org/licenses/LICENSE-2.0. Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under the License.
 */
package org.apache.fineract.nsimbi.userroles.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.Objects;
import lombok.Getter;

@Entity
@Table(name = "nsimbi_user_monetary_authority")
@Getter
public class NsimbiUserMonetaryAuthority {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "appuser_id", nullable = false)
    private Long appUserId;
    @Enumerated(EnumType.STRING)
    @Column(name = "authority_type", nullable = false)
    private MonetaryAuthorityType authorityType;
    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;
    @Column(name = "minimum_amount")
    private BigDecimal minimumAmount;
    @Column(name = "maximum_amount")
    private BigDecimal maximumAmount;

    protected NsimbiUserMonetaryAuthority() {}

    public NsimbiUserMonetaryAuthority(Long userId, MonetaryAuthorityType type, String currency, BigDecimal minimum, BigDecimal maximum) {
        this.appUserId = Objects.requireNonNull(userId, "userId must not be null");
        this.authorityType = Objects.requireNonNull(type, "authorityType must not be null");
        this.currencyCode = validateCurrency(currency);
        validateRange(minimum, maximum);
        this.minimumAmount = minimum;
        this.maximumAmount = maximum;
    }

    public boolean isConfigured() { return this.minimumAmount != null || this.maximumAmount != null; }

    public boolean allows(BigDecimal amount) {
        return isConfigured() && amount != null && (this.minimumAmount == null || amount.compareTo(this.minimumAmount) >= 0)
                && (this.maximumAmount == null || amount.compareTo(this.maximumAmount) <= 0);
    }

    private static String validateCurrency(String currency) {
        if (currency == null || !currency.matches("[A-Z]{3}")) {
            throw new IllegalArgumentException("currency must be a three-letter uppercase ISO 4217 code");
        }
        return currency;
    }

    private static void validateRange(BigDecimal minimum, BigDecimal maximum) {
        if (minimum != null && maximum != null && minimum.compareTo(maximum) > 0) {
            throw new IllegalArgumentException("minimumAmount must not be greater than maximumAmount");
        }
    }
}

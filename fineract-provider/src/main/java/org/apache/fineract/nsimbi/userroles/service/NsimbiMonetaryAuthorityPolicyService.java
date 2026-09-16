/**
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. The ASF licenses this file to you under the Apache
 * License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.apache.org/licenses/LICENSE-2.0. Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under the License.
 */
package org.apache.fineract.nsimbi.userroles.service;

import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.nsimbi.userroles.domain.MonetaryAuthorityType;
import org.apache.fineract.nsimbi.userroles.domain.NsimbiUserMonetaryAuthorityRepository;
import org.springframework.stereotype.Service;

/** Reusable policy only. Transaction handlers are deliberately not enrolled in phase 1. */
@Service
@RequiredArgsConstructor
public class NsimbiMonetaryAuthorityPolicyService {

    private final NsimbiUserMonetaryAuthorityRepository authorities;

    public boolean allows(Long userId, MonetaryAuthorityType type, String currency, BigDecimal amount) {
        if (userId == null || type == null || currency == null || amount == null) {
            return false;
        }
        return this.authorities.findByAppUserIdAndAuthorityTypeAndCurrencyCode(userId, type, currency).map(a -> a.allows(amount))
                .orElse(false);
    }
}

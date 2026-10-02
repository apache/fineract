/**
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. The ASF licenses this file to you under the Apache
 * License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.apache.org/licenses/LICENSE-2.0. Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under the License.
 */
package org.apache.fineract.nsimbi.userroles.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;
import org.apache.fineract.nsimbi.userroles.domain.MonetaryAuthorityType;
import org.apache.fineract.nsimbi.userroles.domain.NsimbiUserMonetaryAuthorityRepository;
import org.junit.jupiter.api.Test;

class NsimbiMonetaryAuthorityPolicyServiceTest {

    @Test
    void deniesWhenNoAuthorityExists() {
        NsimbiUserMonetaryAuthorityRepository repository = org.mockito.Mockito.mock(NsimbiUserMonetaryAuthorityRepository.class);
        when(repository.findByAppUserIdAndAuthorityTypeAndCurrencyCode(1L, MonetaryAuthorityType.TRANSFER, "UGX"))
                .thenReturn(Optional.empty());

        NsimbiMonetaryAuthorityPolicyService service = new NsimbiMonetaryAuthorityPolicyService(repository);

        assertFalse(service.allows(1L, MonetaryAuthorityType.TRANSFER, "UGX", BigDecimal.ONE));
    }
}

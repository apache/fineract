/**
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. The ASF licenses this file to you under the Apache
 * License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.apache.org/licenses/LICENSE-2.0. Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under the License.
 */
package org.apache.fineract.nsimbi.userroles.domain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class NsimbiUserMonetaryAuthorityTest {

    @Test
    void allowsInclusiveMinimumAndMaximumOnly() {
        NsimbiUserMonetaryAuthority authority = new NsimbiUserMonetaryAuthority(1L, MonetaryAuthorityType.DISBURSEMENT, "UGX",
                new BigDecimal("100"), new BigDecimal("200"));

        assertTrue(authority.allows(new BigDecimal("100")));
        assertTrue(authority.allows(new BigDecimal("150")));
        assertTrue(authority.allows(new BigDecimal("200")));
        assertFalse(authority.allows(new BigDecimal("99.99")));
        assertFalse(authority.allows(new BigDecimal("200.01")));
    }

    @Test
    void rejectsInvalidRange() {
        assertThrows(IllegalArgumentException.class,
                () -> new NsimbiUserMonetaryAuthority(1L, MonetaryAuthorityType.DISBURSEMENT, "UGX", new BigDecimal("201"),
                        new BigDecimal("200")));
    }
}

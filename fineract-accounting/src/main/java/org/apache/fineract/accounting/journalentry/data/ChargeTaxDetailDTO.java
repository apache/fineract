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
package org.apache.fineract.accounting.journalentry.data;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Carries the pro-rated tax amount for a single TaxComponent when a LoanCharge is (partially) paid. Used to propagate
 * tax details from the domain layer to the accounting bridge.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChargeTaxDetailDTO {

    /** GL account to credit (tax liability account from TaxComponent.creditAccount). */
    private Long creditAccountId;

    /** Pro-rated tax amount for this component in this payment. */
    private BigDecimal amount;
}

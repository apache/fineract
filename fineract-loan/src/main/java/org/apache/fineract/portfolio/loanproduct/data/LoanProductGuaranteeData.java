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
package org.apache.fineract.portfolio.loanproduct.data;

import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public final class LoanProductGuaranteeData implements Serializable {

    private final Long id;
    private final Long productId;
    private final BigDecimal mandatoryGuarantee;
    private final BigDecimal minimumGuaranteeFromOwnFunds;
    private final BigDecimal minimumGuaranteeFromGuarantor;

    public static LoanProductGuaranteeData instance(final Long id, final Long productId, final BigDecimal mandatoryGuarantee,
            final BigDecimal minimumGuaranteeFromOwnFunds, final BigDecimal minimumGuaranteeFromGuarantor) {
        return new LoanProductGuaranteeData(id, productId, mandatoryGuarantee, minimumGuaranteeFromOwnFunds, minimumGuaranteeFromGuarantor);
    }

    public static LoanProductGuaranteeData sensibleDefaultsForNewLoanProductCreation() {
        return new LoanProductGuaranteeData(null, null, null, null, null);
    }

}

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
package org.apache.fineract.accounting.producttoaccountmapping.service;

import java.util.List;
import java.util.Set;
import org.apache.fineract.portfolio.charge.data.ChargeSummaryData;

/**
 * Charge lookups needed by the accounting module for charge to GL account mappings.
 * <p>
 * The contract lives in fineract-core, next to the charge data it returns, so that neither fineract-accounting nor
 * fineract-charge depends on the other. fineract-charge provides the implementation.
 */
public interface AccountingChargeReadService {

    /**
     * Ensures that a charge can be mapped to a GL account: it must exist, must not be deleted and must be active.
     *
     * @throws org.apache.fineract.infrastructure.core.exception.AbstractPlatformResourceNotFoundException
     *             if the charge does not exist or is deleted
     * @throws org.apache.fineract.infrastructure.core.exception.AbstractPlatformDomainRuleException
     *             if the charge is not active
     */
    void validateChargeForMapping(Long chargeId);

    /**
     * Returns the id, name and penalty flag of the given charges. Ids that do not match a charge are ignored.
     */
    List<ChargeSummaryData> findChargesByIds(Set<Long> chargeIds);
}

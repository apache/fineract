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
package org.apache.fineract.portfolio.tax.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import org.apache.fineract.portfolio.tax.domain.TaxComponent;
import org.apache.fineract.portfolio.tax.domain.TaxGroup;
import org.springframework.stereotype.Service;

@Service
// TODO: migrate all use of this function to TaxUtils.computeTax; leaving for now, too many changes!
@Deprecated(forRemoval = true)
public class ChargeTaxApplicationServiceImpl implements ChargeTaxApplicationService {

    @Override
    public Map<TaxComponent, BigDecimal> computeTax(final TaxGroup taxGroup, final BigDecimal baseAmount, final LocalDate effectiveDate,
            final int scale) {
        return TaxUtils.computeTax(taxGroup, baseAmount, effectiveDate, scale);
    }
}

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
package org.apache.fineract.portfolio.workingcapitalloan.product.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.List;
import org.apache.fineract.infrastructure.core.data.GenericEnumListConverter;

/**
 * Converter for WorkingCapitalLoanPaymentAllocationType list to/from comma-separated string. Follows the same pattern
 * as PaymentAllocationTypeListConverter in LoanProduct.
 */
@Converter(autoApply = true)
public class WorkingCapitalLoanPaymentAllocationTypeListConverter extends GenericEnumListConverter<WorkingCapitalLoanPaymentAllocationType>
        implements AttributeConverter<List<WorkingCapitalLoanPaymentAllocationType>, String> {

    @Override
    public boolean isUnique() {
        return true;
    }

    public WorkingCapitalLoanPaymentAllocationTypeListConverter() {
        super(WorkingCapitalLoanPaymentAllocationType.class);
    }
}

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
package org.apache.fineract.portfolio.workingcapitalloan.domain;

import java.math.BigDecimal;
import org.apache.fineract.infrastructure.core.service.MathUtil;

/**
 * How an outstanding bucket is derived from what was charged, paid and written off.
 *
 * <p>
 * Two callers share this: {@link WorkingCapitalLoanBalance}, which answers for the current snapshot, and the as-of-date
 * quote, which answers for a past date by substituting a date-scoped charged figure. Keeping the arithmetic in one
 * place is what stops the two drifting apart - a quote that floors differently from the balance it is quoting would be
 * wrong in a way no test naturally catches.
 */
public final class WorkingCapitalLoanOutstandingMath {

    private WorkingCapitalLoanOutstandingMath() {
        // Prevent instantiation
    }

    /**
     * What is still owed on a bucket. Floored at zero: paying or writing off more than was charged leaves nothing
     * outstanding rather than a negative amount that would eat into the other buckets once they are summed.
     */
    public static BigDecimal bucketOutstanding(final BigDecimal charged, final BigDecimal paid, final BigDecimal writtenOff) {
        return MathUtil.subtract(charged, paid, writtenOff).max(BigDecimal.ZERO);
    }

    /**
     * How much of a written-off amount is still recoverable. A recovery may not exceed this, so successive recoveries
     * cannot add up past what was written off.
     */
    public static BigDecimal writtenOffOutstanding(final BigDecimal totalWrittenOff, final BigDecimal totalRecovered) {
        return MathUtil.subtract(totalWrittenOff, totalRecovered).max(BigDecimal.ZERO);
    }
}

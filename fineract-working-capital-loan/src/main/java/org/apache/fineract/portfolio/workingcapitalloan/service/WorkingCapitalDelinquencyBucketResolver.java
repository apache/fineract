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
package org.apache.fineract.portfolio.workingcapitalloan.service;

import static org.apache.fineract.portfolio.delinquency.api.DelinquencyApiConstants.DELINQUENCY_BUCKET_ID_MUST_BE_OF_TYPE_MESSAGE;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.portfolio.delinquency.data.DelinquencyBucketData;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyBucket;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyBucketRepository;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyBucketType;
import org.apache.fineract.portfolio.delinquency.exception.DelinquencyBucketNotFoundException;
import org.apache.fineract.portfolio.delinquency.service.DelinquencyReadPlatformService;
import org.apache.fineract.portfolio.workingcapitalloanproduct.WorkingCapitalLoanProductConstants;
import org.springframework.stereotype.Component;

/**
 * Resolves and validates delinquency buckets for Working Capital products and loans. Only
 * {@link DelinquencyBucketType#WORKING_CAPITAL} buckets are allowed.
 */
@Component
@RequiredArgsConstructor
public class WorkingCapitalDelinquencyBucketResolver {

    private final DelinquencyBucketRepository delinquencyBucketRepository;
    private final DelinquencyReadPlatformService delinquencyReadPlatformService;

    public Collection<DelinquencyBucketData> retrieveWorkingCapitalDelinquencyBucketOptions() {
        return this.delinquencyReadPlatformService.retrieveDelinquencyBucketsByType(DelinquencyBucketType.WORKING_CAPITAL);
    }

    public DelinquencyBucket findWorkingCapitalBucketByIdIfProvided(final Long delinquencyBucketId) {
        if (delinquencyBucketId == null) {
            return null;
        }
        final DelinquencyBucket bucket = this.delinquencyBucketRepository.findById(delinquencyBucketId)
                .orElseThrow(() -> DelinquencyBucketNotFoundException.notFound(delinquencyBucketId));
        validateIsWorkingCapitalType(bucket);
        return bucket;
    }

    private void validateIsWorkingCapitalType(final DelinquencyBucket bucket) {
        if (bucket != null && !DelinquencyBucketType.WORKING_CAPITAL.equals(bucket.getBucketType())) {
            final String expectedType = DelinquencyBucketType.WORKING_CAPITAL.name();
            throw new PlatformApiDataValidationException(List.of(ApiParameterError.parameterError(
                    WorkingCapitalLoanProductConstants.DELINQUENCY_BUCKET_ID_MUST_BE_OF_TYPE_VALIDATION_CODE,
                    MessageFormat.format(DELINQUENCY_BUCKET_ID_MUST_BE_OF_TYPE_MESSAGE, expectedType),
                    WorkingCapitalLoanProductConstants.delinquencyBucketIdParamName, new Object[] { expectedType })));
        }
    }
}

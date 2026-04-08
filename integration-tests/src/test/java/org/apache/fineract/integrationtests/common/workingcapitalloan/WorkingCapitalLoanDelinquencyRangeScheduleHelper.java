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
package org.apache.fineract.integrationtests.common.workingcapitalloan;

import static org.apache.fineract.client.feign.util.FeignCalls.ok;

import java.math.BigDecimal;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.DeleteDelinquencyBucketResponse;
import org.apache.fineract.client.models.DelinquencyBucketRequest;
import org.apache.fineract.client.models.DelinquencyBucketResponse;
import org.apache.fineract.client.models.MinimumPaymentPeriodAndRule;
import org.apache.fineract.client.models.PostDelinquencyBucketResponse;
import org.apache.fineract.client.models.PutDelinquencyBucketResponse;
import org.apache.fineract.client.models.WorkingCapitalLoanDelinquencyRangeScheduleData;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;

@Slf4j
public final class WorkingCapitalLoanDelinquencyRangeScheduleHelper {

    private static final String[] FREQUENCY_TYPE_NAMES = { "DAYS", "WEEKS", "MONTHS", "YEARS" };
    private static final String[] MINIMUM_PAYMENT_TYPE_NAMES = { null, "PERCENTAGE", "FLAT" };

    private WorkingCapitalLoanDelinquencyRangeScheduleHelper() {}

    public static List<WorkingCapitalLoanDelinquencyRangeScheduleData> getDelinquencyRangeSchedule(final Long loanId) {
        return ok(() -> FineractFeignClientHelper.getFineractFeignClient().workingCapitalLoanDelinquencyRangeSchedule()
                .retrieveDelinquencyRangeSchedule(loanId));
    }

    public static PostDelinquencyBucketResponse createWorkingCapitalLoanDelinquencyBucket(final List<Long> rangeIds, final int frequency,
            final int frequencyType, final BigDecimal minimumPayment, final int minimumPaymentType) {
        final DelinquencyBucketRequest request = buildWorkingCapitalLoanBucketRequest(rangeIds, frequency, frequencyType, minimumPayment,
                minimumPaymentType);
        return ok(() -> FineractFeignClientHelper.getFineractFeignClient().delinquencyRangeAndBucketsManagement().createBucket(request));
    }

    public static PutDelinquencyBucketResponse updateWorkingCapitalLoanDelinquencyBucket(final Long resourceId, final List<Long> rangeIds,
            final int frequency, final int frequencyType, final BigDecimal minimumPayment, final int minimumPaymentType) {
        final DelinquencyBucketRequest request = buildWorkingCapitalLoanBucketRequest(rangeIds, frequency, frequencyType, minimumPayment,
                minimumPaymentType);
        return ok(() -> FineractFeignClientHelper.getFineractFeignClient().delinquencyRangeAndBucketsManagement().updateBucket(resourceId,
                request));
    }

    public static DelinquencyBucketResponse getBucket(final Long resourceId) {
        return ok(() -> FineractFeignClientHelper.getFineractFeignClient().delinquencyRangeAndBucketsManagement().getBucket(resourceId));
    }

    public static DeleteDelinquencyBucketResponse deleteBucket(final Long resourceId) {
        return ok(() -> FineractFeignClientHelper.getFineractFeignClient().delinquencyRangeAndBucketsManagement().deleteBucket(resourceId));
    }

    private static DelinquencyBucketRequest buildWorkingCapitalLoanBucketRequest(final List<Long> rangeIds, final int frequency,
            final int frequencyType, final BigDecimal minimumPayment, final int minimumPaymentType) {
        final MinimumPaymentPeriodAndRule rule = new MinimumPaymentPeriodAndRule().frequency(frequency)
                .frequencyType(FREQUENCY_TYPE_NAMES[frequencyType]).minimumPayment(minimumPayment)
                .minimumPaymentType(MINIMUM_PAYMENT_TYPE_NAMES[minimumPaymentType]);
        return new DelinquencyBucketRequest()
                .name(org.apache.fineract.integrationtests.common.Utils.uniqueRandomStringGenerator("WC_Delinquency_Bucket_", 4))
                .ranges(rangeIds).bucketType("WORKING_CAPITAL").minimumPaymentPeriodAndRule(rule);
    }
}

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
package org.apache.fineract.integrationtests.common.products;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.fineract.client.models.DeleteDelinquencyBucketResponse;
import org.apache.fineract.client.models.DelinquencyBucketRequest;
import org.apache.fineract.client.models.DelinquencyBucketResponse;
import org.apache.fineract.client.models.DelinquencyRangeData;
import org.apache.fineract.client.models.DelinquencyRangeRequest;
import org.apache.fineract.client.models.GetLoansLoanIdDelinquencySummary;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostDelinquencyBucketResponse;
import org.apache.fineract.client.models.PutDelinquencyBucketResponse;
import org.apache.fineract.client.util.Calls;
import org.apache.fineract.integrationtests.common.FineractClientHelper;
import org.apache.fineract.integrationtests.common.Utils;

@Slf4j
public class DelinquencyBucketsHelper {

    protected DelinquencyBucketsHelper() {}

    public static List<DelinquencyBucketResponse> getBuckets() {
        return Calls.ok(FineractClientHelper.getFineractClient().delinquencyRangeAndBucketsManagement.getBuckets());
    }

    public static DelinquencyBucketResponse getBucket(Long id) {
        return Calls.ok(FineractClientHelper.getFineractClient().delinquencyRangeAndBucketsManagement.getBucket(id));
    }

    public static Long createDefaultBucket() {
        Long range1Id = Calls
                .ok(FineractClientHelper.getFineractClient().delinquencyRangeAndBucketsManagement.createRange(new DelinquencyRangeRequest()
                        .classification(Utils.randomStringGenerator("DLQ_R_", 10)).minimumAgeDays(1).maximumAgeDays(3).locale("en")))
                .getResourceId();
        Long range2Id = Calls
                .ok(FineractClientHelper.getFineractClient().delinquencyRangeAndBucketsManagement.createRange(new DelinquencyRangeRequest()
                        .classification(Utils.randomStringGenerator("DLQ_R_", 10)).minimumAgeDays(4).maximumAgeDays(60).locale("en")))
                .getResourceId();
        return Calls
                .ok(FineractClientHelper.getFineractClient().delinquencyRangeAndBucketsManagement.createBucket(
                        new DelinquencyBucketRequest().name(Utils.randomStringGenerator("DLQ_B_", 10)).ranges(List.of(range1Id, range2Id))))
                .getResourceId();
    }

    public static PostDelinquencyBucketResponse createBucket(DelinquencyBucketRequest bucket) {
        return Calls.ok(FineractClientHelper.getFineractClient().delinquencyRangeAndBucketsManagement.createBucket(bucket));
    }

    public static Long createBucket(List<Pair<Integer, Integer>> rangesDef) {
        List<Long> rangeIds = new ArrayList<>();
        rangesDef.forEach(range -> {
            rangeIds.add(Calls.ok(FineractClientHelper.getFineractClient().delinquencyRangeAndBucketsManagement
                    .createRange(new DelinquencyRangeRequest().classification(Utils.randomStringGenerator("DLQ_R_", 10))
                            .minimumAgeDays(range.getLeft()).maximumAgeDays(range.getRight()).locale("en")))
                    .getResourceId());
        });
        return createBucket(new DelinquencyBucketRequest().name(Utils.randomStringGenerator("DLQ_B_", 10)).ranges(rangeIds))
                .getResourceId();
    }

    public static PutDelinquencyBucketResponse updateBucket(Long id, DelinquencyBucketRequest bucket) {
        return Calls.ok(FineractClientHelper.getFineractClient().delinquencyRangeAndBucketsManagement.updateBucket(id, bucket));
    }

    public static DeleteDelinquencyBucketResponse deleteBucket(Long id) {
        return Calls.ok(FineractClientHelper.getFineractClient().delinquencyRangeAndBucketsManagement.deleteBucket(id));
    }

    public static void evaluateLoanCollectionData(GetLoansLoanIdResponse getLoansLoanIdResponse, Integer pastDueDays,
            Double amountExpected) {
        GetLoansLoanIdDelinquencySummary getCollectionData = getLoansLoanIdResponse.getDelinquent();
        if (getCollectionData != null) {
            log.info("Loan Delinquency Data in Days {} and Amount {}", getCollectionData.getPastDueDays(),
                    getCollectionData.getDelinquentAmount());
            assertEquals(pastDueDays, getCollectionData.getPastDueDays(), "Past due days");
            assertEquals(amountExpected, Utils.getDoubleValue(getCollectionData.getDelinquentAmount()), "Amount expected");
        } else {
            log.info("Loan Delinquency Data is null");
        }

        DelinquencyRangeData delinquencyRange = getLoansLoanIdResponse.getDelinquencyRange();
        if (delinquencyRange != null) {
            log.info("Loan Delinquency Classification is {} : ({} - {}) {}", delinquencyRange.getClassification(),
                    delinquencyRange.getMinimumAgeDays(), delinquencyRange.getMaximumAgeDays(), pastDueDays);
            assertTrue(delinquencyRange.getMinimumAgeDays() <= pastDueDays, "Min Age Days");
            assertTrue(delinquencyRange.getMaximumAgeDays() >= pastDueDays, "Max Age Days");
        } else {
            log.info("Loan Delinquency Classification is null");
        }
    }

}

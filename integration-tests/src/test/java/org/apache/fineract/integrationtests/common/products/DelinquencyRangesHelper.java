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

import java.util.List;
import org.apache.fineract.client.models.DeleteDelinquencyRangeResponse;
import org.apache.fineract.client.models.DelinquencyRangeRequest;
import org.apache.fineract.client.models.DelinquencyRangeResponse;
import org.apache.fineract.client.models.PostDelinquencyRangeResponse;
import org.apache.fineract.client.models.PutDelinquencyRangeResponse;
import org.apache.fineract.client.util.Calls;
import org.apache.fineract.integrationtests.common.FineractClientHelper;

public class DelinquencyRangesHelper {

    protected DelinquencyRangesHelper() {}

    public static List<DelinquencyRangeResponse> getRanges() {
        return Calls.ok(FineractClientHelper.getFineractClient().delinquencyRangeAndBucketsManagement.getRanges());
    }

    public static DelinquencyRangeResponse getRange(Long id) {
        return Calls.ok(FineractClientHelper.getFineractClient().delinquencyRangeAndBucketsManagement.getRange(id));
    }

    public static PostDelinquencyRangeResponse createRange(DelinquencyRangeRequest delinquencyRangeData) {
        return Calls.ok(FineractClientHelper.getFineractClient().delinquencyRangeAndBucketsManagement.createRange(delinquencyRangeData));
    }

    public static PutDelinquencyRangeResponse updateRange(Long id, DelinquencyRangeRequest delinquencyRangeData) {
        return Calls
                .ok(FineractClientHelper.getFineractClient().delinquencyRangeAndBucketsManagement.updateRange(id, delinquencyRangeData));
    }

    public static DeleteDelinquencyRangeResponse deleteRange(Long id) {
        return Calls.ok(FineractClientHelper.getFineractClient().delinquencyRangeAndBucketsManagement.deleteRange(id));
    }
}

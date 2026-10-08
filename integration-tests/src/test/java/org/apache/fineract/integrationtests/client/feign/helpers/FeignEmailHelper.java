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
package org.apache.fineract.integrationtests.client.feign.helpers;

import static org.apache.fineract.client.feign.util.FeignCalls.fail;
import static org.apache.fineract.client.feign.util.FeignCalls.ok;

import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.CommandProcessingResult;
import org.apache.fineract.client.models.EmailData;
import org.apache.fineract.client.models.PostEmailRequest;
import org.apache.fineract.client.models.PutEmailRequest;

public class FeignEmailHelper {

    private final FineractFeignClient fineractClient;

    public FeignEmailHelper(FineractFeignClient fineractClient) {
        this.fineractClient = fineractClient;
    }

    public CommandProcessingResult createEmail(PostEmailRequest request) {
        return ok(() -> fineractClient.defaultApi().createEmail(request));
    }

    public CallFailedRuntimeException createEmailExpectingError(PostEmailRequest request) {
        return fail(() -> fineractClient.defaultApi().createEmail(request));
    }

    public EmailData retrieveEmail(Long emailId) {
        return ok(() -> fineractClient.defaultApi().retrieveOneEmail(emailId));
    }

    public CallFailedRuntimeException retrieveEmailExpectingError(Long emailId) {
        return fail(() -> fineractClient.defaultApi().retrieveOneEmail(emailId));
    }

    public CommandProcessingResult updateEmail(Long emailId, PutEmailRequest request) {
        return ok(() -> fineractClient.defaultApi().updateEmail(emailId, request));
    }

    public CommandProcessingResult deleteEmail(Long emailId) {
        return ok(() -> fineractClient.defaultApi().deleteEmail(emailId));
    }
}

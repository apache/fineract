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
package org.apache.fineract.infrastructure.security.data;

import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.joda.time.DateTime;

public class OTPRequest {

    private final String token;
    private final OTPMetadata metadata;

    public OTPRequest(String token, OTPMetadata metadata) {
        this.token = token;
        this.metadata = metadata;
    }

    public static OTPRequest create(String token, int tokenLiveTimeInSec, boolean extendedAccessToken,
                                    OTPDeliveryMethod deliveryMethod) {
        final OTPMetadata metadata = new OTPMetadata(DateUtils.getLocalDateTimeOfTenant().toDateTime(),
                tokenLiveTimeInSec, extendedAccessToken, deliveryMethod);
        return new OTPRequest(token, metadata);
    }

    public String getToken() {
        return token;
    }

    public OTPMetadata getMetadata() {
        return metadata;
    }

    public boolean isValid() {
        DateTime expireTime = metadata.getRequestTime().plusSeconds(metadata.getTokenLiveTimeInSec());
        return DateTime.now().isBefore(expireTime);
    }
}

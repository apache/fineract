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

import java.time.ZonedDateTime;

public class OTPMetadata {

    private final ZonedDateTime requestTime;
    private final int tokenLiveTimeInSec;
    private final boolean extendedAccessToken;
    private final OTPDeliveryMethod deliveryMethod;

    public OTPMetadata(ZonedDateTime requestTime, int tokenLiveTimeInSec, boolean extendedAccessToken, OTPDeliveryMethod deliveryMethod) {
        this.requestTime = requestTime;
        this.tokenLiveTimeInSec = tokenLiveTimeInSec;
        this.extendedAccessToken = extendedAccessToken;
        this.deliveryMethod = deliveryMethod;
    }

    public ZonedDateTime getRequestTime() {
        return requestTime;
    }

    public int getTokenLiveTimeInSec() {
        return tokenLiveTimeInSec;
    }

    public boolean isExtendedAccessToken() {
        return extendedAccessToken;
    }

    public OTPDeliveryMethod getDeliveryMethod() {
        return deliveryMethod;
    }
}

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
package org.apache.fineract.infrastructure.security.domain;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.fineract.infrastructure.security.data.OTPRequest;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

@Repository
@Profile("twofactor")
public class OTPRequestRepository {

    private final ConcurrentHashMap<Long, OTPRequest> OTPrequests = new ConcurrentHashMap<>();


    public OTPRequest getOTPRequestForUser(AppUser user) {
        Assert.notNull(user);

        return this.OTPrequests.get(user.getId());
    }

    public void addOTPRequest(AppUser user, OTPRequest request) {
        Assert.notNull(user);
        Assert.notNull(request);
        this.OTPrequests.put(user.getId(), request);
    }

    public void deleteOTPRequestForUser(AppUser user) {
        this.OTPrequests.remove(user.getId());
    }
}

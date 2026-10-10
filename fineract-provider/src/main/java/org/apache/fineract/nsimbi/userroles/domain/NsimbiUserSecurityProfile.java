/**
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. The ASF licenses this file to you under the Apache
 * License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.apache.org/licenses/LICENSE-2.0. Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under the License.
 */
package org.apache.fineract.nsimbi.userroles.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.Getter;

@Entity
@Table(name = "nsimbi_user_security_profile")
@Getter
public class NsimbiUserSecurityProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "appuser_id", nullable = false, unique = true)
    private Long appUserId;
    @Column(nullable = false)
    private boolean suspended;
    private OffsetDateTime suspendedOn;
    private Long suspendedByAppuserId;
    private OffsetDateTime lastLoginAt;

    protected NsimbiUserSecurityProfile() {}

    public NsimbiUserSecurityProfile(Long appUserId) { this.appUserId = appUserId; }
    public void suspend(Long byUserId, OffsetDateTime at) { suspended = true; suspendedByAppuserId = byUserId; suspendedOn = at; }
    public void reactivate() { suspended = false; suspendedByAppuserId = null; suspendedOn = null; }
    public void recordSuccessfulLogin(OffsetDateTime at) { lastLoginAt = at; }
}

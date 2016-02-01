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
package org.apache.fineract.infrastructure.core.domain;

import org.apache.fineract.infrastructure.security.domain.PlatformUser;
import org.apache.fineract.infrastructure.security.service.PlatformPasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.authentication.dao.SaltSource;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.stereotype.Service;

@SuppressWarnings("deprecation")
@Service(value = "applicationPasswordEncoder")
@Scope("singleton")
public class DefaultPlatformPasswordEncoder implements PlatformPasswordEncoder {

    private final PasswordEncoder passwordEncoder;
    private final SaltSource saltSource;

    @Autowired
    public DefaultPlatformPasswordEncoder(final PasswordEncoder passwordEncoder, final SaltSource saltSource) {
        this.passwordEncoder = passwordEncoder;
        this.saltSource = saltSource;
    }

    @Override
    public String encode(final PlatformUser appUser) {
        return this.passwordEncoder.encodePassword(appUser.getPassword(), this.saltSource.getSalt(appUser));
    }
}
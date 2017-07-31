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
package org.apache.fineract.portfolio.self.registration.data;

import org.apache.fineract.portfolio.client.domain.Client;
import org.joda.time.LocalDate;

public class SelfServiceRegistrationData {

    @SuppressWarnings("unused")
    private final Long id;
    @SuppressWarnings("unused")
    private Client client;
    @SuppressWarnings("unused")
    private String firstName;
    @SuppressWarnings("unused")
    private String lastName;
    @SuppressWarnings("unused")
    private String mobileNumber;
    @SuppressWarnings("unused")
    private String email;
    @SuppressWarnings("unused")
    private String authenticationToken;
    @SuppressWarnings("unused")
    private String username;
    @SuppressWarnings("unused")
    private String password;
    @SuppressWarnings("unused")
    private LocalDate createdDate;

    public SelfServiceRegistrationData(final Long id, final Client client, final String firstName, final String lastName,
            final String mobileNumber, final String email, final String authenticationToken, final String username, final String password,
            final LocalDate createdDate) {
        this.id = id;
        this.client = client;
        this.firstName = firstName;
        this.lastName = lastName;
        this.mobileNumber = mobileNumber;
        this.email = email;
        this.authenticationToken = authenticationToken;
        this.username = username;
        this.password = password;
        this.createdDate = createdDate;
    }

    public static SelfServiceRegistrationData getData(final Long id, final Client client, final String firstName, final String lastName,
            final String mobileNumber, final String email, final String authenticationToken, final String username, final String password,
            final LocalDate createdDate) {
        return new SelfServiceRegistrationData(id, client, firstName, lastName, mobileNumber, email, authenticationToken, username,
                password, createdDate);

    }

}

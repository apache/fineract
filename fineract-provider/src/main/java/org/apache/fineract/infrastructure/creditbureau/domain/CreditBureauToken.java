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

package org.apache.fineract.infrastructure.creditbureau.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
@Table(name = "m_creditbureau_token")
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class CreditBureauToken extends AbstractPersistableCustom<Long> {

    private static final Logger LOG = LoggerFactory.getLogger(CreditBureauToken.class);

    @Column(name = "username")
    private String userName;

    @Column(name = "token")
    private String accessToken;

    @Column(name = "token_type")
    private String tokenType;

    @Column(name = "expires_in")
    private String expiresIn;

    @Column(name = "issued")
    private String issued;

    @Column(name = "expiry_date")
    private LocalDate expires;

    public static CreditBureauToken fromJson(final JsonCommand command) {
        final String userName = command.stringValueOfParameterNamed("userName");
        final String accessToken = command.stringValueOfParameterNamed("access_token");
        final String tokenType = command.stringValueOfParameterNamed("token_type");
        final String expiresIn = command.stringValueOfParameterNamed("expires_in");
        final String issued = command.stringValueOfParameterNamed(".issued");
        final String expiry = command.stringValueOfParameterNamed(".expires");

        DateTimeFormatter dateformat = new DateTimeFormatterBuilder().appendPattern("EEE, dd MMM yyyy kk:mm:ss zzz").toFormatter();

        LocalDate expires = LocalDate.parse(expiry, dateformat);

        return new CreditBureauToken().setUserName(userName).setAccessToken(accessToken).setTokenType(tokenType).setExpiresIn(expiresIn)
                .setIssued(issued).setExpires(expires);
    }
}

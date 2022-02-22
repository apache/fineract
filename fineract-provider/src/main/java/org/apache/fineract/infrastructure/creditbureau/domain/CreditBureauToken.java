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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
@Table(name = "m_creditbureau_token")
public class CreditBureauToken extends AbstractPersistableCustom {

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
    private Date expires;

    public static CreditBureauToken fromJson(final JsonCommand command) {
        final String userName = command.stringValueOfParameterNamed("userName");
        final String accessToken = command.stringValueOfParameterNamed("access_token");
        final String tokenType = command.stringValueOfParameterNamed("token_type");
        final String expiresIn = command.stringValueOfParameterNamed("expires_in");
        final String issued = command.stringValueOfParameterNamed(".issued");
        final String expiry = command.stringValueOfParameterNamed(".expires");

        SimpleDateFormat dateformat = new SimpleDateFormat("EEE, dd MMM yyyy kk:mm:ss zzz", Locale.ENGLISH);

        Date expires = null;
        try {
            expires = dateformat.parse(expiry);
        } catch (ParseException Ex) {
            LOG.error("Error occured while converting Date(String) to SimpleDateFormat", Ex);
        }

        return new CreditBureauToken(userName, accessToken, tokenType, expiresIn, issued, expires);
    }

    public CreditBureauToken(String userName, String accessToken, String tokenType, String expiresIn, String issued, Date expires) {
        this.userName = userName;
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
        this.issued = issued;
        this.expires = expires;
    }

    public CreditBureauToken() {
        this.userName = null;
        this.accessToken = null;
        this.tokenType = null;
        this.expiresIn = null;
        this.issued = null;
        this.expires = null;
    }

    public String getUserName() {
        return this.userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getCurrentToken() {
        return this.accessToken;
    }

    public void setTokens(String tokens) {
        this.accessToken = tokens;
    }

    public Date getTokenExpiryDate() {
        return this.expires;
    }

}

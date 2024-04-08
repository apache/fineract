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
package org.apache.fineract.test.factory;

import org.apache.fineract.client.models.PostClientsRequest;
import org.apache.fineract.test.helper.Utils;
import org.springframework.stereotype.Component;

@Component
public class ClientRequestFactory {

    private static final Long HEAD_OFFICE_ID = 1L;
    private static final Long LEGAL_FORM_ID_PERSON = 1L;
    public static final String DATE_FORMAT = "dd MMMM yyyy";
    public static final String DEFAULT_LOCALE = "en";

    public PostClientsRequest defaultClientCreationRequest() {
        return new PostClientsRequest()//
                .officeId(HEAD_OFFICE_ID)//
                .legalFormId(LEGAL_FORM_ID_PERSON)//
                .firstname(Utils.randomNameGenerator("Client_FirstName_", 5))//
                .lastname(Utils.randomNameGenerator("Client_LastName_", 5))//
                .externalId(randomClientId("ID_", 7))//
                .dateFormat(DATE_FORMAT)//
                .locale(DEFAULT_LOCALE)//
                .active(true)//
                .activationDate("04 March 2011");//
    }

    private String randomClientId(final String prefix, final int lenOfRandomSuffix) {
        return Utils.randomStringGenerator(prefix, lenOfRandomSuffix, "ABCDEFGHIJKLMNOPQRSTUVWXYZ");
    }
}

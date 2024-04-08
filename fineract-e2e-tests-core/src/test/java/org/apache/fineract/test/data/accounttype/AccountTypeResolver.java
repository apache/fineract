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
package org.apache.fineract.test.data.accounttype;

import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.GetGLAccountsResponse;
import org.apache.fineract.client.services.GeneralLedgerAccountApi;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import retrofit2.Response;

@Component
@RequiredArgsConstructor
@Slf4j
public class AccountTypeResolver {

    private final GeneralLedgerAccountApi glaApi;

    @Cacheable(key = "#accountType.getName()", value = "accountTypesByName")
    public long resolve(AccountType accountType) {
        try {
            String accountTypeName = accountType.getName();
            log.debug("Resolving account type by name [{}]", accountTypeName);
            Response<List<GetGLAccountsResponse>> response = glaApi.retrieveAllAccounts(null, "", 1, true, false, false).execute();
            if (!response.isSuccessful()) {
                throw new IllegalStateException("Unable to get account types. Status code was HTTP " + response.code());
            }
            List<GetGLAccountsResponse> accountTypeResponses = response.body();
            GetGLAccountsResponse foundAtr = accountTypeResponses.stream()//
                    .filter(atr -> accountTypeName.equals(atr.getName()))//
                    .findAny()//
                    .orElseThrow(() -> new IllegalArgumentException("Account type [%s] not found".formatted(accountTypeName)));//

            return foundAtr.getId();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

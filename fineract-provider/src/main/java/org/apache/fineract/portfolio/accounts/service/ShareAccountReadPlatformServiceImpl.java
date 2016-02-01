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
package org.apache.fineract.portfolio.accounts.service;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.fineract.portfolio.accounts.constants.AccountsApiConstants;
import org.apache.fineract.portfolio.accounts.data.AccountData;
import org.apache.fineract.portfolio.accounts.domain.ShareAccount;
import org.apache.fineract.portfolio.accounts.domain.ShareAccountTempRepository;
import org.springframework.stereotype.Service;

@Service(value = "share"+AccountsApiConstants.READPLATFORM_NAME)
public class ShareAccountReadPlatformServiceImpl implements AccountReadPlatformService{

    @Override
    public AccountData retrieveOne(Long id) {
        return ShareAccountTempRepository.getInstance().findOne(id).toData();
    }

    @Override
    public Collection<AccountData> retrieveAll() {
        Collection<ShareAccount> collection = ShareAccountTempRepository.getInstance().findAll() ;
        Set<AccountData> set = new HashSet<>() ;
        for(ShareAccount data: collection) {
            set.add(data.toData()) ;
        }
        return set;
    }

    @Override
    public Set<String> getResponseDataParams() {
        return null;
    }
}

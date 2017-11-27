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
package org.apache.fineract.infrastructure.campaigns.email.service;

import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.campaigns.email.data.EmailData;

import java.util.Collection;
import java.util.Date;
import java.util.List;

public interface EmailReadPlatformService {

    Collection<EmailData> retrieveAll();

    EmailData retrieveOne(Long resourceId);
    
    Collection<EmailData> retrieveAllPending(final SearchParameters searchParameters);
    
    Collection<EmailData> retrieveAllSent(final SearchParameters searchParameters);
    
    Collection<EmailData> retrieveAllDelivered(Integer limit);
    
    Collection<EmailData> retrieveAllFailed(final SearchParameters searchParameters);

    Page<EmailData> retrieveEmailByStatus(Integer limit, Integer status, Date dateFrom, Date dateTo);
    
    List<Long> retrieveExternalIdsOfAllSent(Integer limit);
}

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
package org.apache.fineract.infrastructure.campaigns.sms.domain;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SmsCampaignRepository extends JpaRepository<SmsCampaign, Long>, JpaSpecificationExecutor<SmsCampaign> {

    List<SmsCampaign> findByCampaignType(Integer campaignType);

    Collection<SmsCampaign> findByCampaignTypeAndTriggerTypeAndStatus(Integer campaignType, Integer triggerType, Integer status);

    Collection<SmsCampaign> findByTriggerTypeAndStatus(Integer triggerType, Integer status);

    Collection<SmsCampaign> findByTriggerType(Integer triggerType);

    @Query("SELECT campaign FROM SmsCampaign campaign WHERE campaign.paramValue LIKE :reportPattern AND campaign.triggerType=:triggerType AND campaign.status=300")
    List<SmsCampaign> findActiveSmsCampaigns(@Param("reportPattern") String reportPattern, @Param("triggerType") Integer triggerType);
}

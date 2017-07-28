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
package org.apache.fineract.infrastructure.campaigns.helper;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.UriBuilder;

import org.apache.fineract.infrastructure.campaigns.sms.constants.SmsCampaignConstants;
import org.apache.fineract.infrastructure.campaigns.sms.data.MessageGatewayConfigurationData;
import org.apache.fineract.infrastructure.configuration.service.ExternalServicesPropertiesReadPlatformService;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
public class SmsConfigUtils {

	@Autowired
	  private ExternalServicesPropertiesReadPlatformService propertiesReadPlatformService;
	
	//This method will return uri and HttpEntry objects with keys as uri and entity
    public Map<String, Object> getMessageGateWayRequestURI(final String apiEndPoint, String apiQueueResourceDatas) {
        Map<String, Object> httpRequestdetails = new HashMap<>();
        MessageGatewayConfigurationData messageGatewayConfigurationData = this.propertiesReadPlatformService.getSMSGateway();
        final FineractPlatformTenant tenant = ThreadLocalContextUtil.getTenant();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(SmsCampaignConstants.FINERACT_PLATFORM_TENANT_ID, tenant.getTenantIdentifier());
        headers.add(SmsCampaignConstants.FINERACT_TENANT_APP_KEY, messageGatewayConfigurationData.getTenantAppKey());
        StringBuilder pathBuilder = new StringBuilder();
        String endPoint = (messageGatewayConfigurationData.getEndPoint() == null || messageGatewayConfigurationData.getEndPoint().equals(
                "/")) ? "" : messageGatewayConfigurationData.getEndPoint();
        pathBuilder = (messageGatewayConfigurationData.getEndPoint() == null || messageGatewayConfigurationData.getEndPoint().equals("/")) ? pathBuilder
                .append("{apiEndPoint}") : pathBuilder.append("{endPoint}/{apiEndPoint}");
        // pathBuilder.append("{endPoint}/{apiEndPoint}") ;
        UriBuilder builder = UriBuilder.fromPath(pathBuilder.toString()).host(messageGatewayConfigurationData.getHostName()).scheme("http")
                .port(messageGatewayConfigurationData.getPortNumber());
        URI uri = (messageGatewayConfigurationData.getEndPoint() == null || messageGatewayConfigurationData.getEndPoint().equals("/")) ? builder
                .build(apiEndPoint) : builder.build(endPoint, apiEndPoint);
        HttpEntity<?> entity = null;
        if (apiQueueResourceDatas != null) {
            entity = new HttpEntity<>(apiQueueResourceDatas, headers);
        } else {
            entity = new HttpEntity<>(headers);
        }

        httpRequestdetails.put("uri", uri);
        httpRequestdetails.put("entity", entity);

        return httpRequestdetails;
    }
	
}

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
package org.apache.fineract.notification.eventandlistener;

import java.util.List;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.security.service.BasicAuthTenantDetailsService;
import org.apache.fineract.notification.data.NotificationData;
import org.apache.fineract.notification.service.NotificationWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.listener.SessionAwareMessageListener;

public class NotificationEventListener implements SessionAwareMessageListener<Message> {

	private final BasicAuthTenantDetailsService basicAuthTenantDetailsService;
	private final NotificationWritePlatformService notificationWritePlatformService;
	
	@Autowired
	public NotificationEventListener(BasicAuthTenantDetailsService basicAuthTenantDetailsService,
			NotificationWritePlatformService notificationWritePlatformService) {
		this.basicAuthTenantDetailsService = basicAuthTenantDetailsService;
		this.notificationWritePlatformService = notificationWritePlatformService;
	}

	@Override
	public void onMessage(Message message, Session session) throws JMSException {
		if (message instanceof ObjectMessage) {
			NotificationData notificationData = (NotificationData) ((ObjectMessage) message).getObject();
			
			final FineractPlatformTenant tenant = this.basicAuthTenantDetailsService.loadTenantById(notificationData.getTenantIdentifier(), false);
			ThreadLocalContextUtil.setTenant(tenant);
			
			Long appUserId = notificationData.getActorId();
			List<Long> userIds = notificationData.getUserIds();
						
			if (userIds.contains(appUserId)) {
				userIds.remove(appUserId);
			}
			
			notificationWritePlatformService.notify(
					userIds,
					notificationData.getObjectType(),
					notificationData.getObjectIdentfier(),
					notificationData.getAction(),
					notificationData.getActorId(),
					notificationData.getContent(),
					notificationData.isSystemGenerated()
			);
			
		}
	}
}

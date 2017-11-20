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
package org.apache.fineract.infrastructure.gcm.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.fineract.infrastructure.configuration.service.ExternalServicesPropertiesReadPlatformService;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.gcm.GcmConstants;
import org.apache.fineract.infrastructure.gcm.domain.DeviceRegistration;
import org.apache.fineract.infrastructure.gcm.domain.DeviceRegistrationRepositoryWrapper;
import org.apache.fineract.infrastructure.gcm.domain.Message;
import org.apache.fineract.infrastructure.gcm.domain.Message.Builder;
import org.apache.fineract.infrastructure.gcm.domain.Message.Priority;
import org.apache.fineract.infrastructure.gcm.domain.Notification;
import org.apache.fineract.infrastructure.gcm.domain.NotificationConfigurationData;
import org.apache.fineract.infrastructure.gcm.domain.Result;
import org.apache.fineract.infrastructure.gcm.domain.Sender;
import org.apache.fineract.infrastructure.sms.domain.SmsMessage;
import org.apache.fineract.infrastructure.sms.domain.SmsMessageRepository;
import org.apache.fineract.infrastructure.sms.domain.SmsMessageStatusType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationSenderService {

	private final DeviceRegistrationRepositoryWrapper deviceRegistrationRepositoryWrapper;
	private final SmsMessageRepository smsMessageRepository;
	private ExternalServicesPropertiesReadPlatformService propertiesReadPlatformService;

	@Autowired
	public NotificationSenderService(
			final DeviceRegistrationRepositoryWrapper deviceRegistrationRepositoryWrapper,
			final SmsMessageRepository smsMessageRepository, final ExternalServicesPropertiesReadPlatformService propertiesReadPlatformService) {
		this.deviceRegistrationRepositoryWrapper = deviceRegistrationRepositoryWrapper;
		this.smsMessageRepository = smsMessageRepository;
		this.propertiesReadPlatformService = propertiesReadPlatformService;
	}

	public void sendNotification(List<SmsMessage> smsMessages) {
		Map<Long, List<SmsMessage>> notificationByEachClient = getNotificationListByClient(smsMessages);
		for (Map.Entry<Long, List<SmsMessage>> entry : notificationByEachClient
				.entrySet()) {
			this.sendNotifiaction(entry.getKey(), entry.getValue());
		}
	}

	public Map<Long, List<SmsMessage>> getNotificationListByClient(
			List<SmsMessage> smsMessages) {
		Map<Long, List<SmsMessage>> notificationByEachClient = new HashMap<>();
		for (SmsMessage smsMessage : smsMessages) {
			if (smsMessage.getClient() != null) {
				Long clientId = smsMessage.getClient().getId();
				if (notificationByEachClient.containsKey(clientId)) {
					notificationByEachClient.get(clientId).add(smsMessage);
				} else {
					List<SmsMessage> msgList = new ArrayList<>(
							Arrays.asList(smsMessage));
					notificationByEachClient.put(clientId, msgList);
				}

			}
		}
		return notificationByEachClient;
	}

	public void sendNotifiaction(Long clientId, List<SmsMessage> smsList) {

		DeviceRegistration deviceRegistration = this.deviceRegistrationRepositoryWrapper
				.findDeviceRegistrationByClientId(clientId);
		NotificationConfigurationData notificationConfigurationData = this.propertiesReadPlatformService.getNotificationConfiguration();
		String registrationId = null;
		if (deviceRegistration != null) {
			registrationId = deviceRegistration.getRegistrationId();
		}
		for (SmsMessage smsMessage : smsList) {
			try {
				Notification notification = new Notification.Builder(
						GcmConstants.defaultIcon).title(GcmConstants.title)
						.body(smsMessage.getMessage()).build();
				Builder b = new Builder();
				b.notification(notification);
				b.dryRun(false);
				b.contentAvailable(true);
				b.timeToLive(GcmConstants.TIME_TO_LIVE);
				b.priority(Priority.HIGH);
				b.delayWhileIdle(true);
				Message msg = b.build();
				Sender s = new Sender(notificationConfigurationData.getServerKey(),notificationConfigurationData.getFcmEndPoint());
				Result res;

				res = s.send(msg, registrationId, 3);
				if (res.getSuccess() != null && res.getSuccess()>0) {
					smsMessage.setStatusType(SmsMessageStatusType.SENT
							.getValue());
					smsMessage.setDeliveredOnDate(DateUtils.getLocalDateOfTenant().toDate());
				} else if (res.getFailure() != null && res.getFailure()>0) {
					smsMessage.setStatusType(SmsMessageStatusType.FAILED
							.getValue());
				}
			} catch (IOException e) {
				smsMessage
						.setStatusType(SmsMessageStatusType.FAILED.getValue());
			}
		}

		this.smsMessageRepository.save(smsList);

	}

}

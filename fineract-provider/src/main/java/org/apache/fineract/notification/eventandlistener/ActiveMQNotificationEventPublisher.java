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

import jakarta.jms.Queue;
import lombok.RequiredArgsConstructor;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.fineract.infrastructure.core.condition.EnableFineractEventsCondition;
import org.apache.fineract.notification.data.NotificationData;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
@Profile("activeMqEnabled")
@Conditional(EnableFineractEventsCondition.class)
@RequiredArgsConstructor
public class ActiveMQNotificationEventPublisher implements NotificationEventPublisher {

    private final JmsTemplate jmsTemplate;

    @Override
    public void broadcastNotification(NotificationData notificationData) {
        Queue queue = new ActiveMQQueue("NotificationQueue");
        this.jmsTemplate.send(queue, session -> session.createObjectMessage(notificationData));
    }
}

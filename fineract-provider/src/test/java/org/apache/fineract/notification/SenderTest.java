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
package org.apache.fineract.notification;

import com.mockrunner.mock.jms.MockQueue;
import org.apache.fineract.notification.data.NotificationData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

@ContextConfiguration(locations = {
        "classpath:META-INF/testNotificationContext.xml",
})
@RunWith(SpringJUnit4ClassRunner.class)
public class SenderTest {

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private MockQueue mockQueue;

    @Test
    public void notificationCreation() {

        String objectType = "CLIENT";
        Long objectIdentifier = 1L;
        String action = "created";
        Long actorId = 1L;
        String notificationContent = "A client was created";

        NotificationData notificationData = new NotificationData(
                objectType,
                objectIdentifier,
                action,
                actorId,
                notificationContent,
                false,
                false,
                null,
                null,
                null
        );

        jmsTemplate.send(mockQueue, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                System.out.println("Message send successfully");
                return session.createObjectMessage(notificationData);
            }
        });
    }
}

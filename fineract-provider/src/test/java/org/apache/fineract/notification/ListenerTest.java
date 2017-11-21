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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;

@RunWith(MockitoJUnitRunner.class)
public class ListenerTest {

    private Listener listener;
    private Session session;
    private TextMessage textMessageMock;

    @Before
    public void setUp() {
        listener = new Listener();
        session = Mockito.mock(Session.class);
        textMessageMock = Mockito.mock(TextMessage.class);
    }

    @Test
    public void testListener() throws JMSException {
        Mockito.when(textMessageMock.getText()).thenReturn("content");
        listener.onMessage(textMessageMock, session);
        Mockito.verify(textMessageMock).getText();
    }
}

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
package org.apache.fineract.portfolio.group.domain;

import org.apache.fineract.portfolio.client.domain.Client;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;

/**
 * Tests {@link Group}.
 */
public class GroupTest {

    @Test
    public void testIsNotActive() {
        final Group group = new Group();
        assertTrue(group.isNotActive());
    }

    @Test
    public void testIsActive() {
        final Group group = new Group();
        assertFalse(group.isActive());
    }

    @Test
    public void testIsPending() throws NoSuchFieldException, IllegalAccessException {
        Group group = new Group();
        Field statusField = Group.class.getDeclaredField("status");
        statusField.setAccessible(true);
        statusField.set(group, 100);
        assertTrue(group.isPending());
    }

    @Test
    public void testIsNotPending() throws NoSuchFieldException, IllegalAccessException {
        Group group = new Group();
        Field statusField = Group.class.getDeclaredField("status");
        statusField.setAccessible(true);
        statusField.set(group, 300);
        assertFalse(group.isPending());
    }

    @Test
    public void testHasClientAsMember() throws NoSuchFieldException, IllegalAccessException {
        Group group = new Group();

        Client client1 = mock(Client.class);
        Client client2 = mock(Client.class);
        Client client3 = mock(Client.class);

        Field field = Group.class.getDeclaredField("clientMembers");
        field.setAccessible(true);
        Set<Client> clientMembers = new HashSet<>();
        clientMembers.add(client1);
        clientMembers.add(client2);
        field.set(group, clientMembers);

        assertTrue(group.hasClientAsMember(client1));
        assertTrue(group.hasClientAsMember(client2));
        assertFalse(group.hasClientAsMember(client3));
    }

}
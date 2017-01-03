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
package org.apache.fineract.notification.service;

import org.apache.fineract.notification.domain.Notification;
import org.apache.fineract.notification.domain.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationGeneratorReadRepositoryWrapperImpl implements NotificationGeneratorReadRepositoryWrapper {

    private final NotificationRepository notificationRepository;

    @Autowired
    public NotificationGeneratorReadRepositoryWrapperImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public Notification findById(Long id) {
        return this.notificationRepository.findOne(id);
    }

    @Override
    public List<Notification> fetchAllNotifications() {
        return this.notificationRepository.findAll();
    }

    @Override
    public void delete(Long id) {
       this.notificationRepository.delete(id);
    }

}

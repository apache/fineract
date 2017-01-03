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

import org.apache.fineract.notification.domain.NotificationMapper;
import org.apache.fineract.notification.domain.NotificationMapperRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationMapperReadRepositoryWrapperImpl implements NotificationMapperReadRepositoryWrapper {

    private final NotificationMapperRepository notificationMapperRepository;

    @Autowired
    public NotificationMapperReadRepositoryWrapperImpl(NotificationMapperRepository notificationMapperRepository) {
        this.notificationMapperRepository = notificationMapperRepository;
    }

    @Override
    public NotificationMapper findById(Long id) {
        return this.notificationMapperRepository.findOne(id);
    }

    @Override
    public List<NotificationMapper> fetchAllNotifications() {
        return this.notificationMapperRepository.findAll();
    }

    @Override
    public void delete(Long id) {
        this.notificationMapperRepository.delete(id);
    }

}

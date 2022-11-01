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
package org.apache.fineract.notification.domain;

import java.util.Collection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface NotificationMapperRepository extends PagingAndSortingRepository<NotificationMapper, Long> {

    @Transactional
    @Modifying(flushAutomatically = true)
    @Query("UPDATE NotificationMapper n SET n.isRead = true WHERE n.userId.id = :userId")
    void markAllNotificationsForAUserAsRead(@Param("userId") Long userId);

    @Transactional
    @Modifying(flushAutomatically = true)
    @Query("UPDATE NotificationMapper n SET n.isRead = true WHERE n.userId.id = :userId AND n.notification.id = :notificationId")
    void markASingleNotificationForAUserAsRead(@Param("userId") Long userId, @Param("notificationId") Long notificationId);

    @Query("SELECT n FROM NotificationMapper n WHERE n.userId.id = :userId AND n.isRead=false")
    Collection<NotificationMapper> getUnreadNotificationsForAUser(@Param("userId") Long userId);

    @Query("SELECT n FROM NotificationMapper n WHERE n.userId.id = :userId")
    Collection<NotificationMapper> getAllNotificationsForAUser(@Param("userId") Long userId);

    @Query("SELECT n.notification FROM NotificationMapper n WHERE n.userId.id = :userId")
    Page<Notification> getAllNotificationsForAUserWithParameters(Long userId, Pageable pageable);

    @Query("SELECT n.notification FROM NotificationMapper n WHERE n.userId.id = :userId AND n.isRead = false")
    Page<Notification> getUnreadNotificationsForAUserWithParameters(Long userId, Pageable pageable);

    void saveAndFlush(NotificationMapper notificationMapper);

}

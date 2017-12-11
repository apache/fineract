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

import java.util.Map;

import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.useradministration.domain.AppUser;
import org.apache.fineract.useradministration.domain.Role;

public interface TopicDomainService {
	
	public void createTopic( Office newOffice );
	
	public void createTopic( Role newRole );
	
	public void updateTopic( Office updatedOffice, Map<String, Object> changes );
	
	public void updateTopic( String previousRolename, Role updatedRole, Map<String, Object> changes );
	
	public void deleteTopic( Role role );
	
	public void subscribeUserToTopic( AppUser newUser );
	
	public void updateUserSubscription( AppUser userToUpdate, Map<String, Object> changes );
	
	public void unsubcribeUserFromTopic( AppUser user );
	
}
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

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.fineract.notification.domain.Topic;
import org.apache.fineract.notification.domain.TopicRepository;
import org.apache.fineract.notification.domain.TopicSubscriber;
import org.apache.fineract.notification.domain.TopicSubscriberRepository;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.organisation.office.domain.OfficeRepository;
import org.apache.fineract.useradministration.domain.AppUser;
import org.apache.fineract.useradministration.domain.Role;
import org.apache.fineract.useradministration.domain.RoleRepository;
import org.apache.fineract.useradministration.exception.RoleNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Service
public class TopicDomainServiceImpl implements TopicDomainService {

	private final RoleRepository roleRepository;
	private final TopicRepository topicRepository;
    private final OfficeRepository officeRepository;
    private final TopicSubscriberRepository topicSubscriberRepository;
	
	@Autowired
	public TopicDomainServiceImpl(RoleRepository roleRepository, TopicRepository topicRepository,
			OfficeRepository officeRepository, TopicSubscriberRepository topicSubscriberRepository) {
		
		this.roleRepository = roleRepository;
		this.topicRepository = topicRepository;
		this.officeRepository = officeRepository;
		this.topicSubscriberRepository = topicSubscriberRepository;
	}
	
	@Override
	public void createTopic( Office newOffice ) {
		
		Long entityId = newOffice.getId();
        String entityType = this.getEntityType(newOffice);
        
        List<Role> allRoles = roleRepository.findAll();
        for(Role role : allRoles) {
        	String memberType = role.getName().toUpperCase();
        	String title = role.getName() + " of " + newOffice.getName();
        	Topic newTopic = new Topic(title, true, entityId, entityType, memberType);
        	topicRepository.save(newTopic);
        }
	}
	
	@Override
	public void createTopic( Role newRole ) {
		
		List<Office> offices = officeRepository.findAll();
		
        for (Office office : offices){
        	String entityType = this.getEntityType(office);
        	String title = newRole.getName() + " of " + office.getName();
        	Topic newTopic = new Topic(title, true, office.getId(), entityType, newRole.getName().toUpperCase());
        	topicRepository.save(newTopic);
        }
	}
	
	@Override
	public void updateTopic( Office updatedOffice, Map<String, Object> changes ) {
		
		List<Topic> entityTopics = topicRepository.findByEntityId(updatedOffice.getId());
		
		if (changes.containsKey("parentId")) {
			
            String entityType = this.getEntityType(updatedOffice);
            for (Topic topic : entityTopics) {
            	topic.setEntityType(entityType);
            	topicRepository.save(topic);
            }
		}
		if (changes.containsKey("name")) {
			
        	for (Topic topic: entityTopics) {
        		Role role = roleRepository.getRoleByName(topic.getMemberType());
                String title = role.getName() + " of " + updatedOffice.getName();
                topic.setTitle(title);
                topicRepository.save(topic);
        	}
        }
	}
	
	@Override
	public void updateTopic( String previousRolename, Role updatedRole, Map<String, Object> changes ) {

        if (changes.containsKey("name")) {
        	List<Topic> entityTopics = topicRepository.findByMemberType(previousRolename);
        	for (Topic topic : entityTopics) {
        		Office office = officeRepository.findOne(topic.getEntityId());
        		String title = updatedRole.getName() + " of " + office.getName();
        		topic.setTitle(title);
            	topic.setMemberType(updatedRole.getName().toUpperCase());
            	topicRepository.save(topic);
        	}
        }
	}
	
	@Override
	public void deleteTopic( Role role ) {
		
		List<Topic> topics = topicRepository.findByMemberType(role.getName().toUpperCase());
        for (Topic topic : topics) {
        	topicRepository.delete(topic);
        }
	}
	
	@Override
	public void subscribeUserToTopic( AppUser newUser ) {
		
		List<Topic> possibleTopics = topicRepository.findByEntityId(newUser.getOffice().getId());
        
        if (!possibleTopics.isEmpty()) {
        	Set<Role> userRoles = newUser.getRoles();
        	for (Role role : userRoles) {
        		for (Topic topic : possibleTopics) {
        			if(role.getName().compareToIgnoreCase(topic.getMemberType()) == 0) {
        				TopicSubscriber topicSubscriber = new TopicSubscriber(topic, newUser, new Date());
        				topicSubscriberRepository.save(topicSubscriber);
        			}
        		}
        	}
        }
	}
	
	@Override
	public void updateUserSubscription( AppUser userToUpdate, Map<String, Object> changes ) {
		
		List<TopicSubscriber> oldSubscriptions = topicSubscriberRepository.findBySubscriber(userToUpdate);
		if (changes.containsKey("officeId")) {
			final Long oldOfficeId = userToUpdate.getOffice().getId();
	        final Long newOfficeId = (Long) changes.get("officeId");
	        List<Topic> oldTopics = topicRepository.findByEntityId(oldOfficeId);
            List<Topic> newTopics = topicRepository.findByEntityId(newOfficeId);
            
            for (TopicSubscriber subscriber : oldSubscriptions) {
            	for (Topic topic : oldTopics) {
            		if (subscriber.getTopic().getId() == topic.getId()) {
            			topicSubscriberRepository.delete(subscriber);
            		}
            	}
            }
            
            Set<Role> userRoles = userToUpdate.getRoles();
        	for (Role role : userRoles) {
        		for (Topic topic : newTopics) {
        			if (role.getName().compareToIgnoreCase(topic.getMemberType()) == 0) {
        				TopicSubscriber newSubscription = new TopicSubscriber(topic, userToUpdate, new Date());
        				topicSubscriberRepository.save(newSubscription);
        			}
        		}
        	}
		}
		
		if (changes.containsKey("roles")) {
			
			final Set<Role> oldRoles = userToUpdate.getRoles() ;
            final Set<Role> tempOldRoles = new HashSet<>(oldRoles);
            
            final String[] roleIds = (String[]) changes.get("roles");
            final Set<Role> updatedRoles = assembleSetOfRoles(roleIds);
            final Set<Role> tempUpdatedRoles = new HashSet<>(updatedRoles);
            
            tempOldRoles.removeAll(updatedRoles);
            for (TopicSubscriber subscriber : oldSubscriptions) {
            	Topic topic = subscriber.getTopic();
            	for (Role role : tempOldRoles) {
            		if (role.getName().compareToIgnoreCase(topic.getMemberType()) == 0) {
            			topicSubscriberRepository.delete(subscriber);
            		}
            	}
            }
            
            tempUpdatedRoles.removeAll(oldRoles);
            List<Topic> newTopics = topicRepository.findByEntityId(userToUpdate.getOffice().getId());
            for (Topic topic : newTopics) {
            	for (Role role : tempUpdatedRoles) {
            		if (role.getName().compareToIgnoreCase(topic.getMemberType()) == 0) {
            			TopicSubscriber topicSubscriber = new TopicSubscriber(topic, userToUpdate, new Date());
        				topicSubscriberRepository.save(topicSubscriber);
            		}
            	}
            }
		}
	}
	
	@Override
	public void unsubcribeUserFromTopic( AppUser user ) {
		
		List<TopicSubscriber> subscriptions = topicSubscriberRepository.findBySubscriber(user);
        for (TopicSubscriber subscription : subscriptions) {
        	topicSubscriberRepository.delete(subscription);
        }
	}
	

	private String getEntityType( Office office ) {
		
        if (office.getParent() == null) {
        	return "OFFICE";
        } else {
        	return "BRANCH";
        }
	}
	
	private Set<Role> assembleSetOfRoles(final String[] rolesArray) {

        final Set<Role> allRoles = new HashSet<>();

        if (!ObjectUtils.isEmpty(rolesArray)) {
            for (final String roleId : rolesArray) {
                final Long id = Long.valueOf(roleId);
                final Role role = this.roleRepository.findOne(id);
                if (role == null) { throw new RoleNotFoundException(id); }
                allRoles.add(role);
            }
        }

        return allRoles;
    }
	
}

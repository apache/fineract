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

import org.apache.fineract.notification.exception.TopicNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Wrapper for {@link TopicRepository} that adds NULL checking and Error
 * handling capabilities
 * </p>
 */
@Service
public class TopicRepositoryWrapper {
	
	private final TopicRepository topicRepository;

	@Autowired
	public TopicRepositoryWrapper(TopicRepository topicRepository) {
		this.topicRepository = topicRepository;
	}
	
	public Topic findOneWithNotFoundDetection(final long id) {
		final Topic topic = this.topicRepository.findOne(id);
		if (topic == null) { throw new TopicNotFoundException(id); }
		return topic;
	}
	
	public Topic save(final Topic topic) {
		return this.topicRepository.save(topic);
	}
	
}

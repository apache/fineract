package org.apache.fineract.notification.service;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;

public interface TopicWritePlatformService {
	
	CommandProcessingResult createTopic(JsonCommand command);
	CommandProcessingResult updateTopic(JsonCommand command);
	CommandProcessingResult activateTopic(final Long topicId);
	CommandProcessingResult deactivateTopic(final Long topicId);
	
}

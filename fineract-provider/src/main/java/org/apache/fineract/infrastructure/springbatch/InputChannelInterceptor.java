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
package org.apache.fineract.infrastructure.springbatch;

import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.batch.integration.partition.StepExecutionRequest;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.support.ExecutorChannelInterceptor;
import org.springframework.messaging.support.GenericMessage;

public class InputChannelInterceptor implements ExecutorChannelInterceptor {

    @Override
    public Message<StepExecutionRequest> beforeHandle(Message<?> message, @NotNull MessageChannel channel,
            @NotNull MessageHandler handler) {
        return beforeHandleMessage(message);
    }

    public Message<StepExecutionRequest> beforeHandleMessage(Message<?> message) {
        return new GenericMessage<>(beforeHandleMessage((ContextualMessage) message.getPayload()));
    }

    public StepExecutionRequest beforeHandleMessage(ContextualMessage contextualMessage) {
        ThreadLocalContextUtil.init(contextualMessage.getContext());
        ThreadLocalContextUtil.setActionContext(ActionContext.COB);
        return contextualMessage.getStepExecutionRequest();
    }
}

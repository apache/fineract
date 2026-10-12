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
package org.apache.fineract.cob.loan;

import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.domain.FineractContext;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.springframework.core.task.TaskDecorator;
import org.springframework.lang.NonNull;

/**
 * Task decorator to ensure proper thread context propagation and cleanup
 */
@Slf4j
public class ContextAwareTaskDecorator implements TaskDecorator {

    @NonNull
    @Override
    public Runnable decorate(@NonNull final Runnable runnable) {
        final FineractContext context = ThreadLocalContextUtil.getContext();
        return () -> {
            try {
                log.debug("Initializing thread context for decorated task");
                ThreadLocalContextUtil.init(context);
                runnable.run();
            } finally {
                ThreadLocalContextUtil.reset();
            }
        };
    }

}

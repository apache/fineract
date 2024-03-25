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
package org.apache.fineract.test.messaging.event;

import java.lang.reflect.Constructor;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

@Component
public class EventFactory {

    public <T extends Event<?>> T create(Class<T> eventClass) {
        try {
            Constructor<T> constructor = ReflectionUtils.accessibleConstructor(eventClass);
            return constructor.newInstance();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("No no-arg constructor is available for class " + eventClass.getSimpleName(), e);
        } catch (Exception e) {
            throw new RuntimeException("Error while instantiating event " + eventClass.getSimpleName(), e);
        }
    }
}

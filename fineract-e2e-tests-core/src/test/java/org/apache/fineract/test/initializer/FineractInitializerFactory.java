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
package org.apache.fineract.test.initializer;

import java.util.Set;
import org.apache.fineract.test.initializer.base.FineractInitializer;
import org.apache.fineract.test.support.loader.FineractConfigLoader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public final class FineractInitializerFactory {

    private FineractInitializerFactory() {}

    private static final class Holder {

        private Holder() {}

        static final FineractInitializer INSTANCE;

        static {
            Set<Class<?>> initializerConfigurationClasses = FineractConfigLoader.getInitializerConfigurationClasses();
            ApplicationContext context = new AnnotationConfigApplicationContext(initializerConfigurationClasses.toArray(new Class<?>[0]));
            INSTANCE = context.getBean(FineractInitializer.class);
        }
    }

    public static FineractInitializer get() {
        return Holder.INSTANCE;
    }
}

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
package org.apache.fineract.gradle

import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters

/**
 * Marker build service that exists purely for its parallelism limit.
 *
 * The OpenAPI generator plugin runs generation through the worker API with classloader
 * isolation, i.e. inside the Gradle daemon JVM. Two generate tasks running concurrently
 * therefore share one heap and, on the Fineract spec, exhaust it. Registering this service
 * with {@code maxParallelUsages = 1} and declaring it on every {@code GenerateTask} makes
 * Gradle serialize them across the whole build without giving up parallelism elsewhere.
 */
abstract class OpenApiGeneratorLock implements BuildService<BuildServiceParameters.None> {
}

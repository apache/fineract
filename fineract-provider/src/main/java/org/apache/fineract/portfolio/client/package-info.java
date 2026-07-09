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

/**
 * Declares the client feature as a Spring Modulith application module so its outbound dependencies can be verified
 * against the modularization outline. No {@code allowedDependencies} are declared: the boundary is checked by detecting
 * references to other feature modules' types, and any violation is fixed at the source (fineract-core DTOs /
 * read-services per the outline) rather than by widening an allow-list here.
 */
@org.springframework.modulith.ApplicationModule(displayName = "Client")
package org.apache.fineract.portfolio.client;

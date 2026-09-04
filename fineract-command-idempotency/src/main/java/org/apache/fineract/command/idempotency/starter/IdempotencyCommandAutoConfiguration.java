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
package org.apache.fineract.command.idempotency.starter;

import static org.apache.fineract.command.idempotency.IdempotencyCommandConstants.COMMAND_IDEMPOTENCY_HOOK_BASE_PACKAGE;
import static org.apache.fineract.command.idempotency.IdempotencyCommandConstants.COMMAND_IDEMPOTENCY_PROPERTY_ENABLED;

import org.apache.fineract.command.idempotency.IdempotencyCommandProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

@AutoConfiguration
@EnableConfigurationProperties({ IdempotencyCommandProperties.class })
@ComponentScan(COMMAND_IDEMPOTENCY_HOOK_BASE_PACKAGE)
@ConditionalOnProperty(value = COMMAND_IDEMPOTENCY_PROPERTY_ENABLED, havingValue = "true")
public class IdempotencyCommandAutoConfiguration {}

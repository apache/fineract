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

import static org.apache.fineract.command.idempotency.IdempotencyCommandConstants.COMMAND_IDEMPOTENCY_CHECK_BASE_PACKAGE;

import org.apache.fineract.command.audit.AuditCommandProperties;
import org.apache.fineract.command.idempotency.IdempotencyCommandProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

// note: deliberately not conditional on the idempotency properties; the check has to see the combination that would
// otherwise switch this module off, and it binds the audit properties itself so that it can read them while the audit
// module is disabled and its own auto-configuration has backed off
@AutoConfiguration
@EnableConfigurationProperties({ IdempotencyCommandProperties.class, AuditCommandProperties.class })
@ComponentScan(COMMAND_IDEMPOTENCY_CHECK_BASE_PACKAGE)
public class IdempotencyCommandPropertyCheckAutoConfiguration {}

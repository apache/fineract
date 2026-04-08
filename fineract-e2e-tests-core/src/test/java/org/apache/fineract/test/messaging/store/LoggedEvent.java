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
package org.apache.fineract.test.messaging.store;

import java.time.format.DateTimeFormatter;
import org.apache.fineract.test.messaging.EventMessage;

public class LoggedEvent {

    private final String type;
    private final String businessDate;
    private final String idempotencyKey;

    public LoggedEvent(EventMessage<?> message) {
        this.type = message.getType();
        this.businessDate = DateTimeFormatter.ISO_LOCAL_DATE.format(message.getBusinessDate());
        this.idempotencyKey = message.getIdempotencyKey();
    }

    // Don't use Lombok @ToString since the class name isn't included in the msg
    @Override
    public String toString() {
        return "{" + "idempotencyKey='" + idempotencyKey + '\'' + ", type='" + type + "', businessDate='" + businessDate + '\'' + '}';
    }
}

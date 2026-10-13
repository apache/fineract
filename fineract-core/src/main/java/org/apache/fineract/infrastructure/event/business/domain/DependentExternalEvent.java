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
package org.apache.fineract.infrastructure.event.business.domain;

import java.util.Optional;

/**
 * A business event whose external posting is governed by another external event type on top of its own.
 * <p>
 * The transaction adjustment events are the motivating case: there is one adjustment event type for every kind of
 * transaction, so disabling, say, the re-age transaction event alone would still let the adjustment of a re-age
 * transaction reach the consumer. An event implementing this interface names the event type it depends on, and the
 * notifier only posts it externally when that type is enabled as well. Internal listeners are unaffected: this only
 * decides whether an external event gets created.
 */
public interface DependentExternalEvent {

    /**
     * @return the external event type whose configuration also governs this event, or empty when it stands alone (for
     *         instance when the adjusted transaction type has no event of its own)
     */
    Optional<String> getGoverningExternalEventType();
}

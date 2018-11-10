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
package org.apache.fineract.infrastructure.bulkimport.importhandler.helper;

import com.google.gson.*;
import org.apache.fineract.portfolio.client.data.ClientData;

import java.lang.reflect.Type;
import java.util.Collection;

public class ClientIdSerializer implements JsonSerializer<Collection<ClientData>> {

    @Override
    public JsonElement serialize(Collection<ClientData> src, Type typeOfSrc, JsonSerializationContext context) {
        JsonArray clientIdJsonArray=new JsonArray();
        for (ClientData client:src) {
            JsonElement clientIdElment=new JsonPrimitive(client.id().toString());
            clientIdJsonArray.add(clientIdElment);
        }
        return clientIdJsonArray;
    }
}
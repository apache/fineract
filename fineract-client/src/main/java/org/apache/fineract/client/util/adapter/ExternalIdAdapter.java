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
package org.apache.fineract.client.util.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import org.apache.fineract.client.models.ExternalId;

public class ExternalIdAdapter extends TypeAdapter<ExternalId> {

    @Override
    public void write(JsonWriter out, ExternalId value) throws IOException {
        if (value != null && Boolean.FALSE.equals(value.getEmpty())) {
            out.value(value.getValue());
        } else {
            out.nullValue();
        }
    }

    @Override
    public ExternalId read(JsonReader in) throws IOException {
        ExternalId result = new ExternalId().empty(true);
        switch (in.peek()) {
            case NULL:
                in.nextNull();
                return result;
            default:
                String value = in.nextString();
                return new ExternalId().empty(false).value(value);
        }
    }
}

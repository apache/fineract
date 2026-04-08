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
package org.apache.fineract.infrastructure.core.config;

import com.google.common.collect.Maps;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class FineractPropertiesTest {

    @Test
    public void testKafkaProperties() {
        // valid configs
        testKafkaPropertiesParse("|", "=", null, Map.of());
        testKafkaPropertiesParse("|", "=", "", Map.of());
        testKafkaPropertiesParse("|", "=", "key1=value1", Map.of("key1", "value1"));
        testKafkaPropertiesParse("|", "=", "key1=value1|key2=value2", Map.of("key1", "value1", "key2", "value2"));
        testKafkaPropertiesParse(";", ":", "key1:value1;key2:value2", Map.of("key1", "value1", "key2", "value2"));

        // invalid configs
        testKafkaPropertiesParse("||", "=", "key1=value1", Map.of());
        testKafkaPropertiesParse("|", "", "key1=value1", Map.of());
        testKafkaPropertiesParse("", "", "key1=value1", Map.of());
        testKafkaPropertiesParse("|", "=", "key1=value1=value2", Map.of());
    }

    private void testKafkaPropertiesParse(String lineSep, String keyValueSep, String property, Map<String, String> expected) {
        FineractProperties.KafkaProperties kafkaProperties = new FineractProperties.KafkaProperties();
        kafkaProperties.setExtraProperties(property);
        kafkaProperties.setExtraPropertiesSeparator(lineSep);
        kafkaProperties.setExtraPropertiesKeyValueSeparator(keyValueSep);
        Assertions.assertTrue(Maps.difference(expected, kafkaProperties.getExtraPropertiesMap()).areEqual());
    }

}

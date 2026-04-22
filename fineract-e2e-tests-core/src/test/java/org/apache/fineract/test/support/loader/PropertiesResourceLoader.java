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
package org.apache.fineract.test.support.loader;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.util.StringUtils;

@SuppressFBWarnings({ "VA_FORMAT_STRING_USES_NEWLINE" })
public final class PropertiesResourceLoader {

    private PropertiesResourceLoader() {}

    public static Map<String, List<String>> load(String resourceLocation, ClassLoader classLoader) {

        Map<String, List<String>> result = new HashMap<>();
        try {
            Enumeration<URL> urls = classLoader.getResources(resourceLocation);
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                UrlResource resource = new UrlResource(url);
                Properties properties = PropertiesLoaderUtils.loadProperties(resource);
                for (Map.Entry<?, ?> entry : properties.entrySet()) {
                    String factoryTypeName = ((String) entry.getKey()).trim();
                    String[] names = StringUtils.commaDelimitedListToStringArray((String) entry.getValue());
                    for (String name : names) {
                        result.computeIfAbsent(factoryTypeName, key -> new ArrayList<>()).add(name.trim());
                    }
                }
            }

            result.replaceAll((type, implementations) -> implementations.stream().distinct()
                    .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList)));
        } catch (IOException ex) {
            throw new IllegalArgumentException("""
                    Unable to load properties from location [%s]
                    """.formatted(resourceLocation), ex);
        }
        return result;
    }

}

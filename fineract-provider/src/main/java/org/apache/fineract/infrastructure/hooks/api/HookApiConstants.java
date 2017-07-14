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
package org.apache.fineract.infrastructure.hooks.api;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class HookApiConstants {

    public static final String HOOK_RESOURCE_NAME = "HOOK";

    public static final String nameParamName = "name";

    public static final String displayNameParamName = "displayName";

    public static final String isActiveParamName = "isActive";

    public static final String webTemplateName = "Web";

    public static final String smsTemplateName = "SMS Bridge";

    public static final String payloadURLName = "Payload URL";

    public static final String contentTypeName = "Content Type";

    public static final String smsProviderName = "SMS Provider";

    public static final String smsProviderAccountIdName = "SMS Provider Account Id";

    public static final String smsProviderTokenIdName = "SMS Provider Token";

    public static final String phoneNumberName = "Phone Number";

    public static final String apiKeyName = "Api Key";

    public static final String configParamName = "config";

    public static final String eventsParamName = "events";

    public static final String entityNameParamName = "entityName";

    public static final String actionNameParamName = "actionName";

    public static final String templateIdParamName = "templateId";

    public static final String templateNameParamName = "templateName";

    protected static final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<>(
            Arrays.asList(nameParamName, displayNameParamName,
                    templateIdParamName, isActiveParamName, configParamName,
                    eventsParamName, templateNameParamName));

    protected static final Set<String> UPDATE_REQUEST_DATA_PARAMETERS = new HashSet<>(
            Arrays.asList(nameParamName, displayNameParamName,
                    templateIdParamName, isActiveParamName, configParamName,
                    eventsParamName, templateNameParamName));

}

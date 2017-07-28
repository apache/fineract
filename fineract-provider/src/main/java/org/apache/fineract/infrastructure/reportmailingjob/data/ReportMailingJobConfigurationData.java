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
package org.apache.fineract.infrastructure.reportmailingjob.data;

/** 
 * Immutable data object representing report mailing job configuration data. 
 **/
public class ReportMailingJobConfigurationData {
    private final int id;
    private final String name;
    private final String value;
    
    /** 
     * ReportMailingJobConfigurationData private constructor 
     **/
    private ReportMailingJobConfigurationData(final int id, final String name, final String value) {
        this.id = id;
        this.name = name;
        this.value = value;
    }
    
    /** 
     * creates an instance of the ReportMailingJobConfigurationData class
     * 
     * @return ReportMailingJobConfigurationData object
     **/
    public static ReportMailingJobConfigurationData newInstance(final int id, final String name, final String value) {
        return new ReportMailingJobConfigurationData(id, name, value);
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }
}

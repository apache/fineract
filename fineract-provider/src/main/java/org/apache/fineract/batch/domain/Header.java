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
package org.apache.fineract.batch.domain;

/**
 * Provides an object to handle HTTP headers as name and value pairs for Batch
 * API. It is used in {@link BatchRequest} and {@link BatchResponse} to store
 * the information regarding the headers in incoming and outgoing JSON Strings.
 * 
 * @author Rishabh Shukla
 * 
 * @see BatchRequest
 * @see BatchResponse
 */
public class Header {

    private String name;
    private String value;

    /**
     * Constructs a 'Header' with the name and value of HTTP headers.
     * 
     * @param name
     *            of the HTTP header.
     * @param value
     *            of the HTTP header.
     */
    public Header(String name, String value) {
        this.name = name;
        this.value = value;
    }

    /**
     * Constructs a default constructor of 'Header'
     */
    public Header() {

    }

    /**
     * Returns the 'name' data field of the object of this class.
     * 
     * @return name data field of this class
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets 'name' data field of this class.
     * 
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the 'value' data field of the object of this class.
     * 
     * @return value data field of this class
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Sets 'value' data field of this class.
     * 
     * @param value
     */
    public void setValue(String value) {
        this.value = value;
    }

}

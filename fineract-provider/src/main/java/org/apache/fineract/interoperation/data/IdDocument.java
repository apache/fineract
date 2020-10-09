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
package org.apache.fineract.interoperation.data;

import java.io.Serializable;

public class IdDocument implements Serializable {

    String idType;
    String idNumber;
    String issuerCountry;
    String otherIdDescription;

    public IdDocument(String idType, String idNumber, String issuerCountry, String otherIdDescription) {
        this.idType = idType;
        this.idNumber = idNumber;
        this.issuerCountry = issuerCountry;
        this.otherIdDescription = otherIdDescription;
    }

    public String getIdType() {
        return idType;
    }

    public void setIdType(String idType) {
        this.idType = idType;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public String getIssuerCountry() {
        return issuerCountry;
    }

    public void setIssuerCountry(String issuerCountry) {
        this.issuerCountry = issuerCountry;
    }

    public String getOtherIdDescription() {
        return otherIdDescription;
    }

    public void setOtherIdDescription(String otherIdDescription) {
        this.otherIdDescription = otherIdDescription;
    }

}

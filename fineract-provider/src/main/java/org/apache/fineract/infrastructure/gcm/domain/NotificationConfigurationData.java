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
package org.apache.fineract.infrastructure.gcm.domain;

public class NotificationConfigurationData {
	
	private final Long id;
    private final String serverKey;
    private final String gcmEndPoint;
    private final String fcmEndPoint;
	public NotificationConfigurationData(Long id, String serverKey,final String gcmEndPoint,final String fcmEndPoint) {
		this.id = id;
		this.serverKey = serverKey;
		this.gcmEndPoint = gcmEndPoint;
		this.fcmEndPoint = fcmEndPoint;
	}
	public Long getId() {
		return id;
	}
	public String getServerKey() {
		return serverKey;
	}
	
	public String getGcmEndPoint() {
		return gcmEndPoint;
	}
	public String getFcmEndPoint() {
		return fcmEndPoint;
	}
    
    

}

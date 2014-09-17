/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.hooks.processor.data;

import static org.mifosplatform.infrastructure.hooks.api.HookApiConstants.payloadURLName;
import static org.mifosplatform.infrastructure.hooks.api.HookApiConstants.phoneNumberName;
import static org.mifosplatform.infrastructure.hooks.api.HookApiConstants.smsProviderAccountIdName;
import static org.mifosplatform.infrastructure.hooks.api.HookApiConstants.smsProviderName;
import static org.mifosplatform.infrastructure.hooks.api.HookApiConstants.smsProviderTokenIdName;

import java.util.Set;

import org.mifosplatform.infrastructure.hooks.domain.HookConfiguration;

public class SmsProviderData {

	private String url;
	private String phoneNo;
	private String smsProvider;
	private String smsProviderAccountId;
	private String smsProviderToken;
	
	private String tenantId;
	private String mifosToken;
	private String endpoint;
	
	public SmsProviderData(final Set<HookConfiguration> config) {
		
		for (final HookConfiguration conf : config) {
			final String fieldName = conf.getFieldName();
			if (fieldName.equals(payloadURLName)) {
				this.url = conf.getFieldValue();
			}
			if (fieldName.equals(smsProviderName)) {
				this.smsProvider = conf.getFieldValue();
			}
			if (fieldName.equals(smsProviderAccountIdName)) {
				this.smsProviderAccountId = conf.getFieldValue();
			}
			if (fieldName.equals(smsProviderTokenIdName)) {
				this.smsProviderToken = conf.getFieldValue();
			}
			if (fieldName.equals(phoneNumberName)) {
				this.phoneNo = conf.getFieldValue();
			}
		}
	}

	public String getUrl() {
		return url;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}

	public String getPhoneNumber() {
		return phoneNo;
	}

	public String getSmsProvider() {
		return smsProvider;
	}

	public String getSmsProviderAccountId() {
		return smsProviderAccountId;
	}

	public String getSmsProviderTokenId() {
		return smsProviderToken;
	}

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public String getMifosToken() {
		return mifosToken;
	}

	public void setMifosToken(String mifosToken) {
		this.mifosToken = mifosToken;
	}

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}
}

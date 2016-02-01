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
package org.apache.fineract.infrastructure.security.vote;

import java.util.Collection;

import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.FilterInvocation;

public class SelfServiceUserAccessVote implements AccessDecisionVoter<FilterInvocation> {

	@Override
	public boolean supports(@SuppressWarnings("unused") ConfigAttribute attribute) {
		// This implementation supports any attribute, because it does not rely on it.
		return true;
	}

	@Override
	public boolean supports(Class<?> clazz) {
		return FilterInvocation.class.isAssignableFrom(clazz);
	}

	@Override
	public int vote(final Authentication authentication, final FilterInvocation fi,
			@SuppressWarnings("unused") final Collection<ConfigAttribute> attributes) {
		if(!"OPTIONS".equalsIgnoreCase(fi.getHttpRequest().getMethod())){
			AppUser user = (AppUser) authentication.getPrincipal();
			
			String pathURL = fi.getRequestUrl();
			boolean isSelfServiceRequest = (pathURL != null && pathURL.contains("/self/"));

			boolean notAllowed = ((isSelfServiceRequest && !user.isSelfServiceUser())
					||(!isSelfServiceRequest && user.isSelfServiceUser()));
			
			if(notAllowed){
				return ACCESS_DENIED;
			}
		}
		return ACCESS_GRANTED;
	}

}

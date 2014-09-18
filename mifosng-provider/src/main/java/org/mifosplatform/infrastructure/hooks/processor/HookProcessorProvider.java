/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.hooks.processor;

import static org.mifosplatform.infrastructure.hooks.api.HookApiConstants.smsTemplateName;
import static org.mifosplatform.infrastructure.hooks.api.HookApiConstants.webTemplateName;

import org.mifosplatform.infrastructure.hooks.domain.Hook;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

@Service
public class HookProcessorProvider implements ApplicationContextAware {

	private ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(
			final ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	public HookProcessor getProcessor(final Hook hook) {
		HookProcessor processor;
		final String templateName = hook.getHookTemplate().getName();
		if (templateName.equalsIgnoreCase(smsTemplateName)) {
			processor = this.applicationContext.getBean("twilioHookProcessor",
					TwilioHookProcessor.class);
		} else if (templateName.equals(webTemplateName)) {
			processor = this.applicationContext.getBean("webHookProcessor",
					WebHookProcessor.class);
		} else {
			processor = null;
		}
		return processor;
	}

}

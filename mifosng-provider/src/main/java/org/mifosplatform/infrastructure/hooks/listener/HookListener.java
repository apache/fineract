/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.hooks.listener;

import org.mifosplatform.infrastructure.hooks.event.HookEvent;
import org.springframework.context.ApplicationListener;

public interface HookListener extends ApplicationListener<HookEvent> {

}

/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.configuration.service;

import java.util.Collection;

import org.mifosplatform.infrastructure.configuration.data.ExternalServicesPropertiesData;
import org.mifosplatform.infrastructure.configuration.data.S3CredentialsData;
import org.mifosplatform.infrastructure.configuration.data.SMTPCredentialsData;

public interface ExternalServicesPropertiesReadPlatformService {

    S3CredentialsData getS3Credentials();

    SMTPCredentialsData getSMTPCredentials();

    Collection<ExternalServicesPropertiesData> retrieveOne(String serviceName);

}
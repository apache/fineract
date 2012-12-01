package org.mifosplatform.infrastructure.core.service;

import org.mifosplatform.infrastructure.core.domain.EmailDetail;


public interface PlatformEmailService {

    void sendToUserAccount(EmailDetail emailDetail, String unencodedPassword);

}

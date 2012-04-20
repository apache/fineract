package org.mifosng.platform.infrastructure;


public interface PlatformEmailService {

    void sendToUserAccount(EmailDetail emailDetail, String unencodedPassword);

}

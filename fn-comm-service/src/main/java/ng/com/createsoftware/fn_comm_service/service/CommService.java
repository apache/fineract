package ng.com.createsoftware.fn_comm_service.service;

import ng.com.createsoftware.fn_comm_service.dto.request.MessageRequest;

public interface CommService {

    void sendMessage(MessageRequest request);

}

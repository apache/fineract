package ng.com.createsoftware.fn_accounting_service.dto.request;

import lombok.Data;

@Data
public class OutboxRequest {
    private  String aggregateType;
    private String aggregateId;
    private String eventType;
    private Object payloadObj;
}

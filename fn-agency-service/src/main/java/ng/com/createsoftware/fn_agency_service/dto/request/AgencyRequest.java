package ng.com.createsoftware.fn_agency_service.dto.request;

import lombok.Data;

@Data
public class AgencyRequest {

    private String channel;
    private String agentId;
    private Long clientId;
    private Double amount;
    private String action;
}

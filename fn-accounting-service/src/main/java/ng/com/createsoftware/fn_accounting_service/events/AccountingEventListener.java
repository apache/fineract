package ng.com.createsoftware.fn_accounting_service.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import ng.com.createsoftware.fn_accounting_service.dto.request.GLTransactionRequest;
import ng.com.createsoftware.fn_accounting_service.dto.response.GLTransactionResponse;
import ng.com.createsoftware.fn_accounting_service.model.TransactionType;
import ng.com.createsoftware.fn_accounting_service.service.GLTransactionService;
import ng.com.createsoftware.fn_accounting_service.service.GeneralLedgerService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.JsonNode;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class AccountingEventListener {
    private final GLTransactionService transactionService;
    private final GeneralLedgerService glService;
    private final ObjectMapper mapper = new ObjectMapper();

    @RabbitListener(queues = "${accounting.rabbit.queues.ledgerFund}")
    public void onLedgerFund(String payload){
             try {
                     JsonNode node = mapper.readTree(payload);
                     //Expected fields: ledgerCode, amount, type(DEBIT/CREDIT), reference, narration
                 String code = node.get("ledgerCode").asText();
                 BigDecimal amount = BigDecimal.valueOf(node.get("amount").asDouble());
                 String type = node.get("type").asText("CREDIT");
                 String reference = node.has("reference") ? node.get("reference").asText() : null;
                 String narration = node.has("narration") ? node.get("narration").asText() : null;

                 //find GL by code
                 var glOpt = glService.getGeneralLedgers().stream().filter(g-> code.equals(g.getCode())).findFirst();
                 if(glOpt.isPresent()){
                     Long gLtd = glOpt.get().getId();
                     GLTransactionRequest res = GLTransactionRequest.builder()
                             .ledgerId(gLtd)
                             .amount(amount)
                             .type(TransactionType.valueOf(type))
                             .reference(reference)
                             .narration(narration)
                             .build();
                     transactionService.addGLTransaction(res);
                 }
                  }catch(Exception ex){
                     System.out.println(ex.getMessage());
                  }
    }

    @RabbitListener(queues = "${accounting.rabbit.queues.walletEvents}")
    public void onWalletEvent(String payload){
        //adapt wallet events into GL transactions as needed
        onLedgerFund(payload);
    }
}

package ng.com.createsoftware.fn_loaning_service.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    @Value("${loan.events.exchange: 'domain.exchange'}")
    private String domainExchange;

    @Value("${loan.events.routing.created: 'loan.created'}")
    public String createdRouting;

    @Value("${loan.events.routing.approved: 'loan.approved'}")
    private String approvedRouting;

    @Value("${loan.events.routing.disbursed: 'loan.disbursed'}")
    private String disbursedRouting;

    @Value("${loan.events.routing.repayment: 'loan.repayment'}")
    private String repaymentRouting;

    public static final String LOAN_CREATED_QUEUE = "loan.created.queue";
    public static final String LOAN_APPROVED_QUEUE = "loan.approved.queue";
    public static final String LOAN_DISBURSED_QUEUE = "loan.disbursed.queue";
    public static final String LOAN_REPAYMENT_QUEUE = "loan.repayment.queue";

    @Bean
    public TopicExchange domainExchange(){
        return new TopicExchange(domainExchange, true, false);
    }

    @Bean
    public Queue loanCreatedQueue(){
        return QueueBuilder.durable(LOAN_CREATED_QUEUE).build();
    }


    @Bean
    public Queue loanApprovedQueue(){
        return QueueBuilder.durable(LOAN_APPROVED_QUEUE).build();
    }

    @Bean
    public Queue loanDisbursedQueue(){
        return QueueBuilder.durable(LOAN_DISBURSED_QUEUE).build();
    }

    @Bean
    public Queue loanRepaymentQueue(){
        return QueueBuilder.durable(LOAN_REPAYMENT_QUEUE).build();
    }

    @Bean
    public Binding bindLoanCreated(Queue loanCreatedQueue, TopicExchange domainExchange){
        return BindingBuilder.bind(loanCreatedQueue).to(domainExchange).with(createdRouting);
    }

    @Bean
    public Binding bindLoanApproved(Queue loanApprovedQueue, TopicExchange domainExchange){
        return BindingBuilder.bind(loanApprovedQueue).to(domainExchange).with(approvedRouting);
    }

    @Bean
    public Binding bindLoanDisbursed(Queue loanDisbursedQueue, TopicExchange domainExchange){
        return BindingBuilder.bind(loanDisbursedQueue).to(domainExchange).with(disbursedRouting);
    }

    @Bean
    public Binding bindLoanRepayment(Queue loanRepaymentQueue, TopicExchange domainExchange){
        return BindingBuilder.bind(loanRepaymentQueue).to(domainExchange).with(repaymentRouting);
    }

}

/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.fineract.infrastructure.campaigns.sms.service;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.fineract.infrastructure.campaigns.sms.constants.SmsCampaignTriggerType;
import org.apache.fineract.infrastructure.campaigns.sms.domain.SmsCampaign;
import org.apache.fineract.infrastructure.campaigns.sms.domain.SmsCampaignRepository;
import org.apache.fineract.infrastructure.campaigns.sms.exception.SmsRuntimeException;
import org.apache.fineract.infrastructure.sms.domain.SmsMessage;
import org.apache.fineract.infrastructure.sms.domain.SmsMessageRepository;
import org.apache.fineract.infrastructure.sms.scheduler.SmsMessageScheduledJobService;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.organisation.office.domain.OfficeRepository;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.common.BusinessEventNotificationConstants;
import org.apache.fineract.portfolio.common.BusinessEventNotificationConstants.BUSINESS_ENTITY;
import org.apache.fineract.portfolio.common.BusinessEventNotificationConstants.BUSINESS_EVENTS;
import org.apache.fineract.portfolio.common.service.BusinessEventListner;
import org.apache.fineract.portfolio.common.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.group.domain.Group;
import org.apache.fineract.portfolio.group.domain.GroupRepository;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.exception.InvalidLoanTypeException;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransaction;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SmsCampaignDomainServiceImpl implements SmsCampaignDomainService {

	private static final Logger logger = LoggerFactory.getLogger(SmsCampaignDomainServiceImpl.class);
	
	//private final static int POOL_SIZE = 5 ;
	
    private final SmsCampaignRepository smsCampaignRepository;
    private final SmsMessageRepository smsMessageRepository;
    private final OfficeRepository officeRepository;
    private final BusinessEventNotifierService businessEventNotifierService;
    private final SmsCampaignWritePlatformService smsCampaignWritePlatformCommandHandler;
    private final GroupRepository groupRepository;

    private final SmsMessageScheduledJobService smsMessageScheduledJobService; 
    
    @Autowired
    public SmsCampaignDomainServiceImpl(final SmsCampaignRepository smsCampaignRepository, final SmsMessageRepository smsMessageRepository,
                                        final BusinessEventNotifierService businessEventNotifierService, final OfficeRepository officeRepository,
                                        final SmsCampaignWritePlatformService smsCampaignWritePlatformCommandHandler,
                                        final GroupRepository groupRepository,
                                        final SmsMessageScheduledJobService smsMessageScheduledJobService){
        this.smsCampaignRepository = smsCampaignRepository;
        this.smsMessageRepository = smsMessageRepository;
        this.businessEventNotifierService = businessEventNotifierService;
        this.officeRepository = officeRepository;
        this.smsCampaignWritePlatformCommandHandler = smsCampaignWritePlatformCommandHandler;
        this.groupRepository = groupRepository;
        this.smsMessageScheduledJobService = smsMessageScheduledJobService ;
    }

    @PostConstruct
    public void addListners() {
        this.businessEventNotifierService.addBusinessEventPostListners(BUSINESS_EVENTS.LOAN_APPROVED, new SendSmsOnLoanApproved());
        this.businessEventNotifierService.addBusinessEventPostListners(BUSINESS_EVENTS.LOAN_REJECTED, new SendSmsOnLoanRejected());
        this.businessEventNotifierService.addBusinessEventPostListners(BUSINESS_EVENTS.LOAN_MAKE_REPAYMENT, new SendSmsOnLoanRepayment());
        this.businessEventNotifierService.addBusinessEventPostListners(BUSINESS_EVENTS.CLIENTS_ACTIVATE, new ClientActivatedListener());
        this.businessEventNotifierService.addBusinessEventPostListners(BUSINESS_EVENTS.CLIENTS_REJECT, new ClientRejectedListener());
        this.businessEventNotifierService.addBusinessEventPostListners(BUSINESS_EVENTS.SAVINGS_ACTIVATE, new SavingsAccountActivatedListener());
        this.businessEventNotifierService.addBusinessEventPostListners(BUSINESS_EVENTS.SAVINGS_REJECT, new SavingsAccountRejectedListener());
        this.businessEventNotifierService.addBusinessEventPostListners(BUSINESS_EVENTS.SAVINGS_DEPOSIT, new SavingsAccountTransactionListener(true));
        this.businessEventNotifierService.addBusinessEventPostListners(BUSINESS_EVENTS.SAVINGS_WITHDRAWAL, new SavingsAccountTransactionListener(false));
    }

	private void notifyRejectedLoanOwner(Loan loan) {
		List<SmsCampaign> smsCampaigns = retrieveSmsCampaigns("Loan Rejected");
		if (smsCampaigns.size() > 0) {
			for (SmsCampaign campaign : smsCampaigns) {
				if (campaign.isActive()) {
					SmsCampaignDomainServiceImpl.this.smsCampaignWritePlatformCommandHandler
							.insertDirectCampaignIntoSmsOutboundTable(loan, campaign);
				}
			}
		}
	}

    private void notifyAcceptedLoanOwner(Loan loan) {
        List<SmsCampaign> smsCampaigns = retrieveSmsCampaigns("Loan Approved");
        if(smsCampaigns.size()>0){
            for (SmsCampaign campaign:smsCampaigns){
            	this.smsCampaignWritePlatformCommandHandler.insertDirectCampaignIntoSmsOutboundTable(loan, campaign);
            }
        }
    }

    private void notifyClientActivated(final Client client) {
    	 List<SmsCampaign> smsCampaigns = retrieveSmsCampaigns("Client Activated");
    	 if(smsCampaigns.size()>0){
             for (SmsCampaign campaign:smsCampaigns){
            	 this.smsCampaignWritePlatformCommandHandler.insertDirectCampaignIntoSmsOutboundTable(client, campaign);
             }
         }
    	 
    }
    
    private void notifyClientRejected(final Client client) {
   	 List<SmsCampaign> smsCampaigns = retrieveSmsCampaigns("Client Rejected");
   	 if(smsCampaigns.size()>0){
            for (SmsCampaign campaign:smsCampaigns){
            	this.smsCampaignWritePlatformCommandHandler.insertDirectCampaignIntoSmsOutboundTable(client, campaign);
            }
        }
   	 
   }
    
	private void notifySavingsAccountActivated(final SavingsAccount savingsAccount) {
		List<SmsCampaign> smsCampaigns = retrieveSmsCampaigns("Savings Activated");
		if (smsCampaigns.size() > 0) {
			for (SmsCampaign campaign : smsCampaigns) {
				this.smsCampaignWritePlatformCommandHandler.insertDirectCampaignIntoSmsOutboundTable(savingsAccount,
							campaign);
			}
		}

	}

	private void notifySavingsAccountRejected(final SavingsAccount savingsAccount) {
		List<SmsCampaign> smsCampaigns = retrieveSmsCampaigns("Savings Rejected");
		if (smsCampaigns.size() > 0) {
			for (SmsCampaign campaign : smsCampaigns) {
				this.smsCampaignWritePlatformCommandHandler.insertDirectCampaignIntoSmsOutboundTable(savingsAccount,
							campaign);
			}
		}

	}
	
	private void sendSmsForLoanRepayment(LoanTransaction loanTransaction) {
		List<SmsCampaign> smsCampaigns = retrieveSmsCampaigns("Loan Repayment");
		if (smsCampaigns.size() > 0) {
			for (SmsCampaign smsCampaign : smsCampaigns) {
				try {
					Loan loan = loanTransaction.getLoan();
					final Set<Client> groupClients = new HashSet<>();
					if (loan.hasInvalidLoanType()) {
						throw new InvalidLoanTypeException(
								"Loan Type cannot be Invalid for the Triggered Sms Campaign");
					}
					if (loan.isGroupLoan()) {
						Group group = this.groupRepository.findOne(loan.getGroupId());
						groupClients.addAll(group.getClientMembers());
					} else {
						groupClients.add(loan.client());
					}
					HashMap<String, String> campaignParams = new ObjectMapper().readValue(smsCampaign.getParamValue(),
							new TypeReference<HashMap<String, String>>() {
							});

					if (groupClients.size() > 0) {
						for (Client client : groupClients) {
							HashMap<String, Object> smsParams = processRepaymentDataForSms(loanTransaction, client);
							for (String key : campaignParams.keySet()) {
								String value = campaignParams.get(key);
								String spvalue = null;
								boolean spkeycheck = smsParams.containsKey(key);
								if (spkeycheck) {
									spvalue = smsParams.get(key).toString();
								}
								if (spkeycheck && !(value.equals("-1") || spvalue.equals(value))) {
									if (key.equals("officeId")) {
										Office campaignOffice = this.officeRepository.findOne(Long.valueOf(value));
										if (campaignOffice
												.doesNotHaveAnOfficeInHierarchyWithId(client.getOffice().getId())) {
											throw new SmsRuntimeException("error.msg.no.office",
													"Office not found for the id");
										}
									} else {
										throw new SmsRuntimeException("error.msg.no.id.attribute",
												"Office Id attribute is notfound");
									}
								}
							}
							String message = this.smsCampaignWritePlatformCommandHandler.compileSmsTemplate(
									smsCampaign.getMessage(), smsCampaign.getCampaignName(), smsParams);
							Object mobileNo = smsParams.get("mobileNo");
							if (mobileNo != null) {
								SmsMessage smsMessage = SmsMessage.pendingSms(null, null, client, null, message,
										mobileNo.toString(), smsCampaign);
								this.smsMessageRepository.save(smsMessage);
								Collection<SmsMessage> messages = new ArrayList<>();
								messages.add(smsMessage);
								Map<SmsCampaign, Collection<SmsMessage>> smsDataMap = new HashMap<>();
								smsDataMap.put(smsCampaign, messages);
								this.smsMessageScheduledJobService.sendTriggeredMessages(smsDataMap);
							}
						}
					}
				} catch (final IOException e) {
					logger.error("smsParams does not contain the key: " + e.getMessage());
				} catch (final RuntimeException e) {
					logger.debug("Client Office Id and SMS Campaign Office id doesn't match");
				}
			}
		}
	}

	private void sendSmsForSavingsTransaction(final SavingsAccountTransaction savingsTransaction, boolean isDeposit) {
		String campaignName = isDeposit ? "Savings Deposit" : "Savings Withdrawal";
		List<SmsCampaign> smsCampaigns = retrieveSmsCampaigns(campaignName);
		if (smsCampaigns.size() > 0) {
			for (SmsCampaign smsCampaign : smsCampaigns) {
				try {
					final SavingsAccount savingsAccount = savingsTransaction.getSavingsAccount();
					final Client client = savingsAccount.getClient();
					HashMap<String, String> campaignParams = new ObjectMapper().readValue(smsCampaign.getParamValue(),
							new TypeReference<HashMap<String, String>>() {
							});
					HashMap<String, Object> smsParams = processSavingsTransactionDataForSms(savingsTransaction, client);
					for (String key : campaignParams.keySet()) {
						String value = campaignParams.get(key);
						String spvalue = null;
						boolean spkeycheck = smsParams.containsKey(key);
						if (spkeycheck) {
							spvalue = smsParams.get(key).toString();
						}
						if (spkeycheck && !(value.equals("-1") || spvalue.equals(value))) {
							if (key.equals("officeId")) {
								Office campaignOffice = this.officeRepository.findOne(Long.valueOf(value));
								if (campaignOffice.doesNotHaveAnOfficeInHierarchyWithId(client.getOffice().getId())) {
									throw new SmsRuntimeException("error.msg.no.office",
											"Office not found for the id");
								}
							} else {
								throw new SmsRuntimeException("error.msg.no.id.attribute",
										"Office Id attribute is notfound");
							}
						}
					}
					String message = this.smsCampaignWritePlatformCommandHandler
							.compileSmsTemplate(smsCampaign.getMessage(), smsCampaign.getCampaignName(), smsParams);
					Object mobileNo = smsParams.get("mobileNo");
					if (mobileNo != null) {
						SmsMessage smsMessage = SmsMessage.pendingSms(null, null, client, null, message,
								mobileNo.toString(), smsCampaign);
						this.smsMessageRepository.save(smsMessage);
						Collection<SmsMessage> messages = new ArrayList<>();
						messages.add(smsMessage);
						Map<SmsCampaign, Collection<SmsMessage>> smsDataMap = new HashMap<>();
						smsDataMap.put(smsCampaign, messages);
						this.smsMessageScheduledJobService.sendTriggeredMessages(smsDataMap);
					}
				} catch (final IOException e) {
					logger.error("smsParams does not contain the key: " + e.getMessage());
				} catch (final RuntimeException e) {
					logger.debug("Client Office Id and SMS Campaign Office id doesn't match");
				}
			}
		}
	}
    
    private List<SmsCampaign> retrieveSmsCampaigns(String paramValue){
        List<SmsCampaign> smsCampaigns = smsCampaignRepository.findActiveSmsCampaigns("%"+paramValue+"%", SmsCampaignTriggerType.TRIGGERED.getValue());
        return smsCampaigns;
    }

    private HashMap<String, Object> processRepaymentDataForSms(final LoanTransaction loanTransaction, Client groupClient){

        HashMap<String, Object> smsParams = new HashMap<String, Object>();
        Loan loan = loanTransaction.getLoan();
        final Client client;
        if(loan.isGroupLoan() && groupClient != null){
            client = groupClient;
        }else if(loan.isIndividualLoan()){
            client = loan.getClient();
        }else{
            throw new InvalidParameterException("");
        }

        DateTimeFormatter timeFormatter = DateTimeFormat.forPattern("HH:mm");
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("MMM:d:yyyy");

        smsParams.put("id",loanTransaction.getLoan().getClientId());
        smsParams.put("firstname",client.getFirstname());
        smsParams.put("middlename",client.getMiddlename());
        smsParams.put("lastname",client.getLastname());
        smsParams.put("FullName",client.getDisplayName());
        smsParams.put("mobileNo",client.mobileNo());
        smsParams.put("LoanAmount",loan.getPrincpal());
        smsParams.put("LoanOutstanding",loanTransaction.getOutstandingLoanBalance());
        smsParams.put("loanId",loan.getId());
        smsParams.put("LoanAccountId", loan.getAccountNumber());
        smsParams.put("officeId", client.getOffice().getId());
        
        if(client.getStaff() != null) {
        	smsParams.put("loanOfficerId", client.getStaff().getId());
        }else {
        	  smsParams.put("loanOfficerId", -1);
        }
        
        smsParams.put("repaymentAmount", loanTransaction.getAmount(loan.getCurrency()));
        smsParams.put("RepaymentDate", loanTransaction.getCreatedDateTime().toLocalDate().toString(dateFormatter));
        smsParams.put("RepaymentTime", loanTransaction.getCreatedDateTime().toLocalTime().toString(timeFormatter));
        
        if(loanTransaction.getPaymentDetail() != null) {
        	smsParams.put("receiptNumber", loanTransaction.getPaymentDetail().getReceiptNumber());
        }else {
        	smsParams.put("receiptNumber", -1);	
        }
        return smsParams;
    }

    private HashMap<String, Object> processSavingsTransactionDataForSms(final SavingsAccountTransaction savingsAccountTransaction, Client client){

    	// {{savingsId}} {{id}} {{firstname}} {{middlename}} {{lastname}} {{FullName}} {{mobileNo}} {{savingsAccountId}} {{depositAmount}} {{balance}}
    	
    	//transactionDate
        HashMap<String, Object> smsParams = new HashMap<String, Object>();
        SavingsAccount savingsAccount = savingsAccountTransaction.getSavingsAccount() ;
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("MMM:d:yyyy");
        smsParams.put("clientId",client.getId());
        smsParams.put("firstname",client.getFirstname());
        smsParams.put("middlename",client.getMiddlename());
        smsParams.put("lastname",client.getLastname());
        smsParams.put("FullName",client.getDisplayName());
        smsParams.put("mobileNo",client.mobileNo());
        smsParams.put("savingsId", savingsAccount.getId()) ;
        smsParams.put("savingsAccountNo",savingsAccount.getAccountNumber());
        smsParams.put("withdrawAmount",savingsAccountTransaction.getAmount(savingsAccount.getCurrency()));
        smsParams.put("depositAmount",savingsAccountTransaction.getAmount(savingsAccount.getCurrency()));
        smsParams.put("balance",savingsAccount.getWithdrawableBalance());
        smsParams.put("officeId", client.getOffice().getId());
        smsParams.put("transactionDate", savingsAccountTransaction.getTransactionLocalDate().toString(dateFormatter)) ;
        smsParams.put("savingsTransactionId", savingsAccountTransaction.getId()) ;
        
        if(client.getStaff() != null) {
        	smsParams.put("loanOfficerId", client.getStaff().getId());
        }else {
        	  smsParams.put("loanOfficerId", -1);
        }
        
        if(savingsAccountTransaction.getPaymentDetail() != null) {
        	smsParams.put("receiptNumber", savingsAccountTransaction.getPaymentDetail().getReceiptNumber());
        }else {
        	smsParams.put("receiptNumber", -1);	
        }
        return smsParams;
    }
    
    private abstract class SmsBusinessEventAdapter implements BusinessEventListner {

		@Override
		public void businessEventToBeExecuted(Map<BUSINESS_ENTITY, Object> businessEventEntity) {
			//Nothing to do
		}
    }
    
    private class SendSmsOnLoanApproved extends SmsBusinessEventAdapter{

        @Override
        public void businessEventWasExecuted(Map<BusinessEventNotificationConstants.BUSINESS_ENTITY, Object> businessEventEntity) {
            Object entity = businessEventEntity.get(BusinessEventNotificationConstants.BUSINESS_ENTITY.LOAN);
            if (entity instanceof Loan) {
                Loan loan = (Loan) entity;
                notifyAcceptedLoanOwner(loan);
            }
        }
    }

    private class SendSmsOnLoanRejected extends SmsBusinessEventAdapter{

        @Override
        public void businessEventWasExecuted(Map<BusinessEventNotificationConstants.BUSINESS_ENTITY, Object> businessEventEntity) {
            Object entity = businessEventEntity.get(BusinessEventNotificationConstants.BUSINESS_ENTITY.LOAN);
            if (entity instanceof Loan) {
                Loan loan = (Loan) entity;
                notifyRejectedLoanOwner(loan);
            }
        }
    }

    private class SendSmsOnLoanRepayment extends SmsBusinessEventAdapter{

        @Override
        public void businessEventWasExecuted(Map<BusinessEventNotificationConstants.BUSINESS_ENTITY, Object> businessEventEntity) {
            Object entity = businessEventEntity.get(BusinessEventNotificationConstants.BUSINESS_ENTITY.LOAN_TRANSACTION);
            if (entity instanceof LoanTransaction) {
                LoanTransaction loanTransaction = (LoanTransaction) entity;
                sendSmsForLoanRepayment(loanTransaction);
            }
        }
    }
    
    private class ClientActivatedListener extends SmsBusinessEventAdapter {

		@Override
		public void businessEventWasExecuted(Map<BUSINESS_ENTITY, Object> businessEventEntity) {
			Object entity = businessEventEntity.get(BusinessEventNotificationConstants.BUSINESS_ENTITY.CLIENT);
			if(entity instanceof Client) {
				notifyClientActivated((Client)entity);	
			}
		}
    }
    
    private class ClientRejectedListener extends SmsBusinessEventAdapter {

		@Override
		public void businessEventWasExecuted(Map<BUSINESS_ENTITY, Object> businessEventEntity) {
			Object entity = businessEventEntity.get(BusinessEventNotificationConstants.BUSINESS_ENTITY.CLIENT);
			if(entity instanceof Client) {
				notifyClientRejected((Client)entity);
			}
			
		}
    }
    
    private class SavingsAccountActivatedListener extends SmsBusinessEventAdapter{

		@Override
		public void businessEventWasExecuted(Map<BUSINESS_ENTITY, Object> businessEventEntity) {
			Object entity = businessEventEntity.get(BusinessEventNotificationConstants.BUSINESS_ENTITY.SAVING);
			if(entity instanceof SavingsAccount) {
				notifySavingsAccountActivated((SavingsAccount)entity);
			}
			
		}
    }
    
    private class SavingsAccountRejectedListener extends SmsBusinessEventAdapter {

		@Override
		public void businessEventWasExecuted(Map<BUSINESS_ENTITY, Object> businessEventEntity) {
			Object entity = businessEventEntity.get(BusinessEventNotificationConstants.BUSINESS_ENTITY.SAVING);
			if(entity instanceof SavingsAccount) {
				notifySavingsAccountRejected((SavingsAccount)entity);
			}
		}
    }
    
    private class SavingsAccountTransactionListener extends SmsBusinessEventAdapter {

    	final boolean isDeposit ;
    	
    	public SavingsAccountTransactionListener(final boolean isDeposit) {
			this.isDeposit = isDeposit ;
		}

		@Override
		public void businessEventWasExecuted(Map<BUSINESS_ENTITY, Object> businessEventEntity) {
			Object entity = businessEventEntity.get(BusinessEventNotificationConstants.BUSINESS_ENTITY.SAVINGS_TRANSACTION);
			if(entity instanceof SavingsAccountTransaction) {
				sendSmsForSavingsTransaction((SavingsAccountTransaction)entity, this.isDeposit);
			}
		}
    }
    
    /*private abstract class Task implements Runnable {
    	
    	protected final FineractPlatformTenant tenant;
    	
    	protected final String reportName ;
    	
    	private final Object entity ;
    	
    	public Task(final FineractPlatformTenant tenant, final String reportName, final Object entity) {
            this.tenant = tenant;
            this.reportName = reportName ;
            this.entity = entity ;
    	}
    }*/
}

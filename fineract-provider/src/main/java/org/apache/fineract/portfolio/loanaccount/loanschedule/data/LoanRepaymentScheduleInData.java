package org.apache.fineract.portfolio.loanaccount.loanschedule.data;

import lombok.Data;

import java.math.BigDecimal;
@Data
public class LoanRepaymentScheduleInData {
    private final Integer Id;
    private final  String fromDate;
    private final  String  dueDate;
    private final Integer installmentNo;
    private final BigDecimal principalAmount;
    private final BigDecimal principalPaid;
    private final BigDecimal  principalWrittenOff;

    private final BigDecimal interestAmount;
    private final BigDecimal interestPaid;
    private final BigDecimal interestWrittenOff;
    private final BigDecimal interestWaived;

    private final BigDecimal feeChargesAmount;
    private final BigDecimal feePaid;
    private final BigDecimal feeChargesWrittenOff;
    private final BigDecimal feeChargeWaived;

    private final BigDecimal penaltyChargesAmount;
    private final BigDecimal penaltyChargePaid;
    private final BigDecimal penaltyChargesWrittenOff;
    private final BigDecimal penaltyChargesWaived;
    private final Boolean completedDerived;
    private final String obligationMetOnDate;

    public LoanRepaymentScheduleInData(Integer id, String fromDate, String dueDate, Integer installmentNo,
                                       BigDecimal principalAmount, BigDecimal principalPaid,
                                       BigDecimal principalWrittenOff, BigDecimal interestAmount,
                                       BigDecimal interestPaid, BigDecimal interestWrittenOff,
                                       BigDecimal interestWaived, BigDecimal feeChargesAmount,
                                       BigDecimal feePaid, BigDecimal feeChargesWrittenOff,
                                       BigDecimal feeChargeWaived, BigDecimal penaltyChargesAmount,
                                       BigDecimal penaltyChargePaid, BigDecimal penaltyChargesWrittenOff,
                                       BigDecimal penaltyChargesWaived, Boolean completedDerived, String obligationMetOnDate) {
        this.Id = id;
        this.fromDate = fromDate;
        this.dueDate = dueDate;
        this.installmentNo = installmentNo;
        this.principalAmount = principalAmount;
        this.principalPaid = principalPaid;
        this.principalWrittenOff = principalWrittenOff;
        this.interestAmount = interestAmount;
        this.interestPaid = interestPaid;
        this.interestWrittenOff = interestWrittenOff;
        this.interestWaived = interestWaived;
        this.feeChargesAmount = feeChargesAmount;
        this.feePaid = feePaid;
        this.feeChargesWrittenOff = feeChargesWrittenOff;
        this.feeChargeWaived = feeChargeWaived;
        this.penaltyChargesAmount = penaltyChargesAmount;
        this.penaltyChargePaid = penaltyChargePaid;
        this.penaltyChargesWrittenOff = penaltyChargesWrittenOff;
        this.penaltyChargesWaived = penaltyChargesWaived;
        this.completedDerived = completedDerived;
        this.obligationMetOnDate = obligationMetOnDate;
    }
}

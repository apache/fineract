package org.apache.fineract.portfolio.account.validator;

import static org.apache.fineract.portfolio.account.AccountDetailConstants.dateFormatParamName;
import static org.apache.fineract.portfolio.account.AccountDetailConstants.fromAccountIdParamName;
import static org.apache.fineract.portfolio.account.AccountDetailConstants.fromAccountTypeParamName;
import static org.apache.fineract.portfolio.account.AccountDetailConstants.fromClientIdParamName;
import static org.apache.fineract.portfolio.account.AccountDetailConstants.fromOfficeIdParamName;
import static org.apache.fineract.portfolio.account.AccountDetailConstants.localeParamName;
import static org.apache.fineract.portfolio.account.AccountDetailConstants.toAccountIdParamName;
import static org.apache.fineract.portfolio.account.AccountDetailConstants.toAccountTypeParamName;
import static org.apache.fineract.portfolio.account.AccountDetailConstants.toClientIdParamName;
import static org.apache.fineract.portfolio.account.AccountDetailConstants.toOfficeIdParamName;
import static org.apache.fineract.portfolio.account.AccountDetailConstants.transferTypeParamName;
import static org.apache.fineract.portfolio.account.api.StandingInstructionApiConstants.amountParamName;
import static org.apache.fineract.portfolio.account.api.StandingInstructionApiConstants.instructionTypeParamName;
import static org.apache.fineract.portfolio.account.api.StandingInstructionApiConstants.monthDayFormatParamName;
import static org.apache.fineract.portfolio.account.api.StandingInstructionApiConstants.nameParamName;
import static org.apache.fineract.portfolio.account.api.StandingInstructionApiConstants.priorityParamName;
import static org.apache.fineract.portfolio.account.api.StandingInstructionApiConstants.recurrenceFrequencyParamName;
import static org.apache.fineract.portfolio.account.api.StandingInstructionApiConstants.recurrenceIntervalParamName;
import static org.apache.fineract.portfolio.account.api.StandingInstructionApiConstants.recurrenceOnMonthDayParamName;
import static org.apache.fineract.portfolio.account.api.StandingInstructionApiConstants.recurrenceTypeParamName;
import static org.apache.fineract.portfolio.account.api.StandingInstructionApiConstants.statusParamName;
import static org.apache.fineract.portfolio.account.api.StandingInstructionApiConstants.validFromParamName;
import static org.apache.fineract.portfolio.account.api.StandingInstructionApiConstants.validTillParamName;

import java.util.function.Function;
import java.util.Optional;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.portfolio.account.data.AccountTransferDetails;
import org.apache.fineract.portfolio.account.data.StandingInstruction;
import org.apache.fineract.portfolio.account.domain.AccountTransferRecurrenceType;
import org.apache.fineract.portfolio.account.domain.AccountTransferType;
import org.apache.fineract.portfolio.account.domain.StandingInstructionType;
import org.apache.fineract.portfolio.account.PortfolioAccountType;
import org.springframework.stereotype.Component;

@Component
public class StandingInstructionHelper {

    public StandingInstruction extractStandingInstruction(final JsonCommand command) {
        final AccountTransferDetails details = new AccountTransferDetails();
        details.setFromOfficeId(command.longValueOfParameterNamed(fromOfficeIdParamName));
        details.setFromClientId(command.longValueOfParameterNamed(fromClientIdParamName));
        details.setFromAccountId(command.longValueOfParameterNamed(fromAccountIdParamName));
        details.setFromAccountType(command.integerValueSansLocaleOfParameterNamed(fromAccountTypeParamName));
        details.setToOfficeId(command.longValueOfParameterNamed(toOfficeIdParamName));
        details.setToClientId(command.longValueOfParameterNamed(toClientIdParamName));
        details.setToAccountId(command.longValueOfParameterNamed(toAccountIdParamName));
        details.setToAccountType(command.integerValueSansLocaleOfParameterNamed(toAccountTypeParamName));

        final StandingInstruction instruction = new StandingInstruction();
        instruction.setAccountTransferDetails(details);
        instruction.setLocale(command.stringValueOfParameterNamed(localeParamName));
        instruction.setDateFormat(command.stringValueOfParameterNamed(dateFormatParamName));
        instruction.setTransferType(command.integerValueSansLocaleOfParameterNamed(transferTypeParamName));
        instruction.setName(command.stringValueOfParameterNamed(nameParamName));
        instruction.setPriority(command.integerValueSansLocaleOfParameterNamed(priorityParamName));
        instruction.setStatus(command.integerValueSansLocaleOfParameterNamed(statusParamName));
        instruction.setInstructionType(command.integerValueSansLocaleOfParameterNamed(instructionTypeParamName));
        instruction.setValidFrom(command.localDateValueOfParameterNamed(validFromParamName));
        instruction.setValidTill(command.localDateValueOfParameterNamed(validTillParamName));
        instruction.setAmount(command.bigDecimalValueOfParameterNamed(amountParamName));
        instruction.setRecurrenceType(command.integerValueSansLocaleOfParameterNamed(recurrenceTypeParamName));
        instruction.setRecurrenceFrequency(command.integerValueSansLocaleOfParameterNamed(recurrenceFrequencyParamName));
        instruction.setRecurrenceInterval(command.integerValueSansLocaleOfParameterNamed(recurrenceIntervalParamName));
        instruction.setMonthDayStr(command.stringValueOfParameterNamed(recurrenceOnMonthDayParamName));
        instruction.setMonthDayFormat(command.stringValueOfParameterNamed(monthDayFormatParamName));

        return instruction;
    }

    protected boolean isAccountTransfer(final Integer transferType) {
        return isMatchingType(transferType, AccountTransferType::fromInt, AccountTransferType::isAccountTransfer);
    }

    protected boolean isLoanRepayment(final Integer transferType) {
        return isMatchingType(transferType, AccountTransferType::fromInt, AccountTransferType::isLoanRepayment);
    }

    protected boolean isFixedInstruction(final Integer instructionType) {
        return isMatchingType(instructionType, StandingInstructionType::fromInt, StandingInstructionType::isFixedAmoutTransfer);
    }

    protected boolean isDuesInstruction(final Integer instructionType) {
        return isMatchingType(instructionType, StandingInstructionType::fromInt, StandingInstructionType::isDuesAmoutTransfer);
    }

    public boolean isPeriodicRecurrence(final Integer recurrenceType) {
        return isMatchingType(recurrenceType, AccountTransferRecurrenceType::fromInt, AccountTransferRecurrenceType::isPeriodicRecurrence);
    }

    protected boolean isAsPerDuesRecurrence(final Integer recurrenceType) {
        return isMatchingType(recurrenceType, AccountTransferRecurrenceType::fromInt, AccountTransferRecurrenceType::isDuesRecurrence);
    }

    protected boolean isLoanAccount(final Integer accountType) {
        return isMatchingType(accountType, PortfolioAccountType::fromInt, PortfolioAccountType.LOAN::equals);
    }

    protected boolean isSavingsAccount(final Integer accountType) {
        return isMatchingType(accountType, PortfolioAccountType::fromInt, PortfolioAccountType.SAVINGS::equals);
    }

    private <T> boolean isMatchingType(final Integer codeType, final Function<Integer, T> resolver, final Function<T, Boolean> predicate) {
        return Optional.ofNullable(codeType)
                       .map(resolver)
                       .map(predicate)
                       .orElse(false);
    }
}
@ManualJournalEntryAccountingRule
Feature: Manual journal entries with a fixed account and account tags

  Background:
    When Admin sets the business date to "01 January 2026"

  Scenario Outline: Save a manual journal entry when both accounts match the rule with a fixed <fixed side> account
    Given an accounting rule with a fixed "<fixed side>" account and tags on the other side
    Then a manual journal entry debiting the "<debit>" account and crediting the "<credit>" account is saved

    Examples:
      | fixed side | debit  | credit |
      | debit      | fixed  | tagged |
      | credit     | tagged | fixed  |

  Scenario Outline: Reject a manual journal entry with <mismatch> for a rule with a fixed <fixed side> account
    Given an accounting rule with a fixed "<fixed side>" account and tags on the other side
    Then a manual journal entry with debit "<debit>" and credit "<credit>" is rejected with "<error code>" and leaves no lines

    Examples:
      | fixed side | mismatch                | debit  | credit | error code                                                |
      | debit      | the wrong fixed account | other  | tagged | error.msg.glJournalEntry.invalid.debit.or.credit.accounts |
      | debit      | the wrong credit tag    | fixed  | other  | error.msg.glJournalEntry.invalid.credits                  |
      | credit     | the wrong fixed account | tagged | other  | error.msg.glJournalEntry.invalid.debit.or.credit.accounts |
      | credit     | the wrong debit tag     | other  | fixed  | error.msg.glJournalEntry.invalid.debits                   |

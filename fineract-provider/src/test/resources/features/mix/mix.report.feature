Feature: MIX XBRL

  @mix
  Scenario Outline: Verify that GL codes resolve
    Given A XBRL template <template>
    When The user resolves GL codes
    Then The result should contain <values>

    Examples:
      | template        | values      |
      | {12000}+{11000} | 12000,11000 |

  @mix
  Scenario Outline: Verify XBRL builder is working
    Given The XBRL input parameters start date <start>, end date <end>, currency <currency>, taxonomy <taxonomy> and sample <sample>
    When The user builds the XBRL report
    Then The XBRL result should match <result>

    Examples:
      | start      | end        | currency | taxonomy | sample | result   |
      | 2005-11-11 | 2013-07-17 | USD      | Assets   | 10000  | xbrl.xml |

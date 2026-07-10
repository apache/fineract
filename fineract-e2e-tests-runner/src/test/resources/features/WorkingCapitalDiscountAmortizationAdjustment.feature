@WorkingCapitalDiscountFeeAmortizationAdjustmentFeature
Feature: Working Capital Discount Fee Amortization Adjustment

  @TestRailId:C83076
  Scenario: Discount fee adjustment after COB amortization creates discount fee amortization adjustment on next COB - UC1
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin adds Discount fee with "1000" amount on Working Capital loan account for last disbursement
    When Admin sets the business date to "05 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Customer makes repayment on "05 January 2026" with 50 transaction amount on Working Capital loan
    When Admin sets the business date to "08 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Discount Fee Amortization | 9.61              |                  |                   |                       | false    |
    Then Working Capital Loan Transactions tab has a "DISCOUNT_FEE_AMORTIZATION" transaction with date "05 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name                 | Debit  | Credit |
      | INCOME    | 404000       | Interest Income              |        | 9.61   |
      | LIABILITY | 240005       | Deferred Interest Revenue    | 9.61   |        |
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 10000.0   | 50.0               | 9.61           | 990.39           | 0.0               |
    And Admin adds Discount fee adjustment with "500" amount on transaction date "08 January 2026" on Working Capital loan account for last discount
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9500.0    | 50.0               | 9.61           | 490.39           | 0.0               |
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Discount Fee Amortization | 9.61              |                  |                   |                       | false    |
      | 08 January 2026 | Discount Fee Adjustment   | 500.0             | 500.0            | 0.0               | 0.0                   | false    |
    When Admin sets the business date to "09 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    Then Working Capital loan amortization schedule has 194 periods, with the following data for periods:
      | paymentNo | paymentDate      | expectedPaymentAmount | actualPaymentAmount | expectedBalance | actualBalance | expectedAmortizationAmount | actualAmortizationAmount | expectedDiscountFeeBalance |
      | 0         | 01 January 2026  | -9000.00              |                     | 9000.00         | 9000.00       |                            |                          | 500.00                     |
      | 1         | 02 January 2026  | 50.00                 | 0.00                | 8955.14         | 9000.00       | 5.14                       | 0.00                     | 494.86                     |
      | 2         | 03 January 2026  | 50.00                 | 0.00                | 8910.26         | 9000.00       | 5.12                       | 0.00                     | 489.74                     |
      | 3         | 04 January 2026  | 50.00                 | 0.00                | 8865.35         | 9000.00       | 5.09                       | 0.00                     | 484.65                     |
      | 4         | 05 January 2026  | 50.00                 | 50.00               | 8820.42         | 8955.14       | 5.07                       | 5.14                     | 479.58                     |
      | 5         | 06 January 2026  | 50.00                 |                     | 8775.46         |               | 5.04                       |                          | 474.54                     |
      | 6         | 07 January 2026  | 50.00                 |                     | 8730.47         |               | 5.01                       |                          | 469.53                     |
      | 7         | 08 January 2026  | 50.00                 |                     | 8685.46         |               | 4.99                       |                          | 464.54                     |
      | 8         | 09 January 2026  | 50.00                 |                     | 8640.43         |               | 4.96                       |                          | 459.58                     |
      | 9         | 10 January 2026  | 50.00                 |                     | 8595.36         |               | 4.94                       |                          | 454.64                     |
      | 10        | 11 January 2026  | 50.00                 |                     | 8550.28         |               | 4.91                       |                          | 449.73                     |
      | 11        | 12 January 2026  | 50.00                 |                     | 8505.16         |               | 4.89                       |                          | 444.84                     |
      | 12        | 13 January 2026  | 50.00                 |                     | 8460.02         |               | 4.86                       |                          | 439.98                     |
      | 13        | 14 January 2026  | 50.00                 |                     | 8414.86         |               | 4.83                       |                          | 435.15                     |
      | 14        | 15 January 2026  | 50.00                 |                     | 8369.66         |               | 4.81                       |                          | 430.34                     |
      | 15        | 16 January 2026  | 50.00                 |                     | 8324.45         |               | 4.78                       |                          | 425.56                     |
      | 16        | 17 January 2026  | 50.00                 |                     | 8279.20         |               | 4.76                       |                          | 420.80                     |
      | 17        | 18 January 2026  | 50.00                 |                     | 8233.94         |               | 4.73                       |                          | 416.07                     |
      | 18        | 19 January 2026  | 50.00                 |                     | 8188.64         |               | 4.71                       |                          | 411.36                     |
      | 19        | 20 January 2026  | 50.00                 |                     | 8143.32         |               | 4.68                       |                          | 406.68                     |
      | 20        | 21 January 2026  | 50.00                 |                     | 8097.97         |               | 4.65                       |                          | 402.03                     |
      | 21        | 22 January 2026  | 50.00                 |                     | 8052.60         |               | 4.63                       |                          | 397.4                      |
      | 22        | 23 January 2026  | 50.00                 |                     | 8007.20         |               | 4.60                       |                          | 392.8                      |
      | 23        | 24 January 2026  | 50.00                 |                     | 7961.78         |               | 4.58                       |                          | 388.22                     |
      | 24        | 25 January 2026  | 50.00                 |                     | 7916.33         |               | 4.55                       |                          | 383.67                     |
      | 25        | 26 January 2026  | 50.00                 |                     | 7870.85         |               | 4.52                       |                          | 379.15                     |
      | 26        | 27 January 2026  | 50.00                 |                     | 7825.35         |               | 4.50                       |                          | 374.65                     |
      | 27        | 28 January 2026  | 50.00                 |                     | 7779.82         |               | 4.47                       |                          | 370.18                     |
      | 28        | 29 January 2026  | 50.00                 |                     | 7734.27         |               | 4.45                       |                          | 365.73                     |
      | 29        | 30 January 2026  | 50.00                 |                     | 7688.69         |               | 4.42                       |                          | 361.31                     |
      | 30        | 31 January 2026  | 50.00                 |                     | 7643.08         |               | 4.39                       |                          | 356.92                     |
      | 31        | 01 February 2026 | 50.00                 |                     | 7597.45         |               | 4.37                       |                          | 352.55                     |
      | 32        | 02 February 2026 | 50.00                 |                     | 7551.79         |               | 4.34                       |                          | 348.21                     |
      | 33        | 03 February 2026 | 50.00                 |                     | 7506.11         |               | 4.32                       |                          | 343.89                     |
      | 34        | 04 February 2026 | 50.00                 |                     | 7460.40         |               | 4.29                       |                          | 339.6                      |
      | 35        | 05 February 2026 | 50.00                 |                     | 7414.66         |               | 4.26                       |                          | 335.34                     |
      | 36        | 06 February 2026 | 50.00                 |                     | 7368.90         |               | 4.24                       |                          | 331.1                      |
      | 37        | 07 February 2026 | 50.00                 |                     | 7323.11         |               | 4.21                       |                          | 326.89                     |
      | 38        | 08 February 2026 | 50.00                 |                     | 7277.29         |               | 4.18                       |                          | 322.71                     |
      | 39        | 09 February 2026 | 50.00                 |                     | 7231.45         |               | 4.16                       |                          | 318.55                     |
      | 40        | 10 February 2026 | 50.00                 |                     | 7185.58         |               | 4.13                       |                          | 314.42                     |
      | 41        | 11 February 2026 | 50.00                 |                     | 7139.69         |               | 4.11                       |                          | 310.31                     |
      | 42        | 12 February 2026 | 50.00                 |                     | 7093.77         |               | 4.08                       |                          | 306.23                     |
      | 43        | 13 February 2026 | 50.00                 |                     | 7047.82         |               | 4.05                       |                          | 302.18                     |
      | 44        | 14 February 2026 | 50.00                 |                     | 7001.85         |               | 4.03                       |                          | 298.15                     |
      | 45        | 15 February 2026 | 50.00                 |                     | 6955.85         |               | 4.00                       |                          | 294.15                     |
      | 46        | 16 February 2026 | 50.00                 |                     | 6909.83         |               | 3.97                       |                          | 290.18                     |
      | 47        | 17 February 2026 | 50.00                 |                     | 6863.78         |               | 3.95                       |                          | 286.23                     |
      | 48        | 18 February 2026 | 50.00                 |                     | 6817.70         |               | 3.92                       |                          | 282.31                     |
      | 49        | 19 February 2026 | 50.00                 |                     | 6771.59         |               | 3.90                       |                          | 278.41                     |
      | 50        | 20 February 2026 | 50.00                 |                     | 6725.46         |               | 3.87                       |                          | 274.54                     |
      | 51        | 21 February 2026 | 50.00                 |                     | 6679.31         |               | 3.84                       |                          | 270.7                      |
      | 52        | 22 February 2026 | 50.00                 |                     | 6633.12         |               | 3.82                       |                          | 266.88                     |
      | 53        | 23 February 2026 | 50.00                 |                     | 6586.91         |               | 3.79                       |                          | 263.09                     |
      | 54        | 24 February 2026 | 50.00                 |                     | 6540.68         |               | 3.76                       |                          | 259.33                     |
      | 55        | 25 February 2026 | 50.00                 |                     | 6494.42         |               | 3.74                       |                          | 255.59                     |
      | 56        | 26 February 2026 | 50.00                 |                     | 6448.13         |               | 3.71                       |                          | 251.88                     |
      | 57        | 27 February 2026 | 50.00                 |                     | 6401.81         |               | 3.68                       |                          | 248.2                      |
      | 58        | 28 February 2026 | 50.00                 |                     | 6355.47         |               | 3.66                       |                          | 244.54                     |
      | 59        | 01 March 2026    | 50.00                 |                     | 6309.10         |               | 3.63                       |                          | 240.91                     |
      | 60        | 02 March 2026    | 50.00                 |                     | 6262.71         |               | 3.61                       |                          | 237.3                      |
      | 61        | 03 March 2026    | 50.00                 |                     | 6216.29         |               | 3.58                       |                          | 233.72                     |
      | 62        | 04 March 2026    | 50.00                 |                     | 6169.84         |               | 3.55                       |                          | 230.17                     |
      | 63        | 05 March 2026    | 50.00                 |                     | 6123.36         |               | 3.53                       |                          | 226.64                     |
      | 64        | 06 March 2026    | 50.00                 |                     | 6076.86         |               | 3.50                       |                          | 223.14                     |
      | 65        | 07 March 2026    | 50.00                 |                     | 6030.34         |               | 3.47                       |                          | 219.67                     |
      | 66        | 08 March 2026    | 50.00                 |                     | 5983.78         |               | 3.45                       |                          | 216.22                     |
      | 67        | 09 March 2026    | 50.00                 |                     | 5937.20         |               | 3.42                       |                          | 212.8                      |
      | 68        | 10 March 2026    | 50.00                 |                     | 5890.59         |               | 3.39                       |                          | 209.41                     |
      | 69        | 11 March 2026    | 50.00                 |                     | 5843.96         |               | 3.37                       |                          | 206.04                     |
      | 70        | 12 March 2026    | 50.00                 |                     | 5797.30         |               | 3.34                       |                          | 202.7                      |
      | 71        | 13 March 2026    | 50.00                 |                     | 5750.61         |               | 3.31                       |                          | 199.39                     |
      | 72        | 14 March 2026    | 50.00                 |                     | 5703.90         |               | 3.29                       |                          | 196.1                      |
      | 73        | 15 March 2026    | 50.00                 |                     | 5657.16         |               | 3.26                       |                          | 192.84                     |
      | 74        | 16 March 2026    | 50.00                 |                     | 5610.39         |               | 3.23                       |                          | 189.61                     |
      | 75        | 17 March 2026    | 50.00                 |                     | 5563.60         |               | 3.21                       |                          | 186.4                      |
      | 76        | 18 March 2026    | 50.00                 |                     | 5516.78         |               | 3.18                       |                          | 183.22                     |
      | 77        | 19 March 2026    | 50.00                 |                     | 5469.93         |               | 3.15                       |                          | 180.07                     |
      | 78        | 20 March 2026    | 50.00                 |                     | 5423.06         |               | 3.13                       |                          | 176.94                     |
      | 79        | 21 March 2026    | 50.00                 |                     | 5376.15         |               | 3.10                       |                          | 173.84                     |
      | 80        | 22 March 2026    | 50.00                 |                     | 5329.23         |               | 3.07                       |                          | 170.77                     |
      | 81        | 23 March 2026    | 50.00                 |                     | 5282.27         |               | 3.05                       |                          | 167.72                     |
      | 82        | 24 March 2026    | 50.00                 |                     | 5235.29         |               | 3.02                       |                          | 164.7                      |
      | 83        | 25 March 2026    | 50.00                 |                     | 5188.28         |               | 2.99                       |                          | 161.71                     |
      | 84        | 26 March 2026    | 50.00                 |                     | 5141.25         |               | 2.96                       |                          | 158.75                     |
      | 85        | 27 March 2026    | 50.00                 |                     | 5094.18         |               | 2.94                       |                          | 155.81                     |
      | 86        | 28 March 2026    | 50.00                 |                     | 5047.10         |               | 2.91                       |                          | 152.9                      |
      | 87        | 29 March 2026    | 50.00                 |                     | 4999.98         |               | 2.88                       |                          | 150.02                     |
      | 88        | 30 March 2026    | 50.00                 |                     | 4952.84         |               | 2.86                       |                          | 147.16                     |
      | 89        | 31 March 2026    | 50.00                 |                     | 4905.67         |               | 2.83                       |                          | 144.33                     |
      | 90        | 01 April 2026    | 50.00                 |                     | 4858.47         |               | 2.80                       |                          | 141.53                     |
      | 91        | 02 April 2026    | 50.00                 |                     | 4811.25         |               | 2.78                       |                          | 138.75                     |
      | 92        | 03 April 2026    | 50.00                 |                     | 4764.00         |               | 2.75                       |                          | 136.0                      |
      | 93        | 04 April 2026    | 50.00                 |                     | 4716.72         |               | 2.72                       |                          | 133.28                     |
      | 94        | 05 April 2026    | 50.00                 |                     | 4669.41         |               | 2.70                       |                          | 130.58                     |
      | 95        | 06 April 2026    | 50.00                 |                     | 4622.08         |               | 2.67                       |                          | 127.91                     |
      | 96        | 07 April 2026    | 50.00                 |                     | 4574.72         |               | 2.64                       |                          | 125.27                     |
      | 97        | 08 April 2026    | 50.00                 |                     | 4527.34         |               | 2.61                       |                          | 122.66                     |
      | 98        | 09 April 2026    | 50.00                 |                     | 4479.93         |               | 2.59                       |                          | 120.07                     |
      | 99        | 10 April 2026    | 50.00                 |                     | 4432.49         |               | 2.56                       |                          | 117.51                     |
      | 100       | 11 April 2026    | 50.00                 |                     | 4385.02         |               | 2.53                       |                          | 114.98                     |
      | 101       | 12 April 2026    | 50.00                 |                     | 4337.52         |               | 2.51                       |                          | 112.47                     |
      | 102       | 13 April 2026    | 50.00                 |                     | 4290.00         |               | 2.48                       |                          | 109.99                     |
      | 103       | 14 April 2026    | 50.00                 |                     | 4242.45         |               | 2.45                       |                          | 107.54                     |
      | 104       | 15 April 2026    | 50.00                 |                     | 4194.88         |               | 2.42                       |                          | 105.12                     |
      | 105       | 16 April 2026    | 50.00                 |                     | 4147.28         |               | 2.40                       |                          | 102.72                     |
      | 106       | 17 April 2026    | 50.00                 |                     | 4099.65         |               | 2.37                       |                          | 100.35                     |
      | 107       | 18 April 2026    | 50.00                 |                     | 4051.99         |               | 2.34                       |                          |  98.01                     |
      | 108       | 19 April 2026    | 50.00                 |                     | 4004.30         |               | 2.32                       |                          |  95.69                     |
      | 109       | 20 April 2026    | 50.00                 |                     | 3956.59         |               | 2.29                       |                          |  93.4                      |
      | 110       | 21 April 2026    | 50.00                 |                     | 3908.85         |               | 2.26                       |                          |  91.14                     |
      | 111       | 22 April 2026    | 50.00                 |                     | 3861.09         |               | 2.23                       |                          |  88.91                     |
      | 112       | 23 April 2026    | 50.00                 |                     | 3813.29         |               | 2.21                       |                          |  86.7                      |
      | 113       | 24 April 2026    | 50.00                 |                     | 3765.47         |               | 2.18                       |                          |  84.52                     |
      | 114       | 25 April 2026    | 50.00                 |                     | 3717.62         |               | 2.15                       |                          |  82.37                     |
      | 115       | 26 April 2026    | 50.00                 |                     | 3669.75         |               | 2.12                       |                          |  80.25                     |
      | 116       | 27 April 2026    | 50.00                 |                     | 3621.85         |               | 2.10                       |                          |  78.15                     |
      | 117       | 28 April 2026    | 50.00                 |                     | 3573.92         |               | 2.07                       |                          |  76.08                     |
      | 118       | 29 April 2026    | 50.00                 |                     | 3525.96         |               | 2.04                       |                          |  74.04                     |
      | 119       | 30 April 2026    | 50.00                 |                     | 3477.97         |               | 2.01                       |                          |  72.03                     |
      | 120       | 01 May 2026      | 50.00                 |                     | 3429.96         |               | 1.99                       |                          |  70.04                     |
      | 121       | 02 May 2026      | 50.00                 |                     | 3381.92         |               | 1.96                       |                          |  68.08                     |
      | 122       | 03 May 2026      | 50.00                 |                     | 3333.85         |               | 1.93                       |                          |  66.15                     |
      | 123       | 04 May 2026      | 50.00                 |                     | 3285.76         |               | 1.91                       |                          |  64.24                     |
      | 124       | 05 May 2026      | 50.00                 |                     | 3237.64         |               | 1.88                       |                          |  62.36                     |
      | 125       | 06 May 2026      | 50.00                 |                     | 3189.49         |               | 1.85                       |                          |  60.51                     |
      | 126       | 07 May 2026      | 50.00                 |                     | 3141.31         |               | 1.82                       |                          |  58.69                     |
      | 127       | 08 May 2026      | 50.00                 |                     | 3093.10         |               | 1.80                       |                          |  56.89                     |
      | 128       | 09 May 2026      | 50.00                 |                     | 3044.87         |               | 1.77                       |                          |  55.12                     |
      | 129       | 10 May 2026      | 50.00                 |                     | 2996.61         |               | 1.74                       |                          |  53.38                     |
      | 130       | 11 May 2026      | 50.00                 |                     | 2948.32         |               | 1.71                       |                          |  51.67                     |
      | 131       | 12 May 2026      | 50.00                 |                     | 2900.01         |               | 1.68                       |                          |  49.99                     |
      | 132       | 13 May 2026      | 50.00                 |                     | 2851.67         |               | 1.66                       |                          |  48.33                     |
      | 133       | 14 May 2026      | 50.00                 |                     | 2803.30         |               | 1.63                       |                          |  46.7                      |
      | 134       | 15 May 2026      | 50.00                 |                     | 2754.90         |               | 1.60                       |                          |  45.1                      |
      | 135       | 16 May 2026      | 50.00                 |                     | 2706.47         |               | 1.57                       |                          |  43.53                     |
      | 136       | 17 May 2026      | 50.00                 |                     | 2658.02         |               | 1.55                       |                          |  41.98                     |
      | 137       | 18 May 2026      | 50.00                 |                     | 2609.54         |               | 1.52                       |                          |  40.46                     |
      | 138       | 19 May 2026      | 50.00                 |                     | 2561.03         |               | 1.49                       |                          |  38.97                     |
      | 139       | 20 May 2026      | 50.00                 |                     | 2512.49         |               | 1.46                       |                          |  37.51                     |
      | 140       | 21 May 2026      | 50.00                 |                     | 2463.93         |               | 1.44                       |                          |  36.07                     |
      | 141       | 22 May 2026      | 50.00                 |                     | 2415.34         |               | 1.41                       |                          |  34.66                     |
      | 142       | 23 May 2026      | 50.00                 |                     | 2366.72         |               | 1.38                       |                          |  33.28                     |
      | 143       | 24 May 2026      | 50.00                 |                     | 2318.07         |               | 1.35                       |                          |  31.93                     |
      | 144       | 25 May 2026      | 50.00                 |                     | 2269.39         |               | 1.32                       |                          |  30.61                     |
      | 145       | 26 May 2026      | 50.00                 |                     | 2220.69         |               | 1.30                       |                          |  29.31                     |
      | 146       | 27 May 2026      | 50.00                 |                     | 2171.96         |               | 1.27                       |                          |  28.04                     |
      | 147       | 28 May 2026      | 50.00                 |                     | 2123.20         |               | 1.24                       |                          |  26.8                      |
      | 148       | 29 May 2026      | 50.00                 |                     | 2074.41         |               | 1.21                       |                          |  25.59                     |
      | 149       | 30 May 2026      | 50.00                 |                     | 2025.60         |               | 1.19                       |                          |  24.4                      |
      | 150       | 31 May 2026      | 50.00                 |                     | 1976.76         |               | 1.16                       |                          |  23.24                     |
      | 151       | 01 June 2026     | 50.00                 |                     | 1927.89         |               | 1.13                       |                          |  22.11                     |
      | 152       | 02 June 2026     | 50.00                 |                     | 1878.99         |               | 1.10                       |                          |  21.01                     |
      | 153       | 03 June 2026     | 50.00                 |                     | 1830.06         |               | 1.07                       |                          |  19.94                     |
      | 154       | 04 June 2026     | 50.00                 |                     | 1781.11         |               | 1.05                       |                          |  18.89                     |
      | 155       | 05 June 2026     | 50.00                 |                     | 1732.13         |               | 1.02                       |                          |  17.87                     |
      | 156       | 06 June 2026     | 50.00                 |                     | 1683.12         |               | 0.99                       |                          |  16.88                     |
      | 157       | 07 June 2026     | 50.00                 |                     | 1634.08         |               | 0.96                       |                          |  15.92                     |
      | 158       | 08 June 2026     | 50.00                 |                     | 1585.01         |               | 0.93                       |                          |  14.99                     |
      | 159       | 09 June 2026     | 50.00                 |                     | 1535.92         |               | 0.91                       |                          |  14.08                     |
      | 160       | 10 June 2026     | 50.00                 |                     | 1486.79         |               | 0.88                       |                          |  13.2                      |
      | 161       | 11 June 2026     | 50.00                 |                     | 1437.64         |               | 0.85                       |                          |  12.35                     |
      | 162       | 12 June 2026     | 50.00                 |                     | 1388.47         |               | 0.82                       |                          |  11.53                     |
      | 163       | 13 June 2026     | 50.00                 |                     | 1339.26         |               | 0.79                       |                          |  10.74                     |
      | 164       | 14 June 2026     | 50.00                 |                     | 1290.02         |               | 0.77                       |                          |   9.97                     |
      | 165       | 15 June 2026     | 50.00                 |                     | 1240.76         |               | 0.74                       |                          |   9.23                     |
      | 166       | 16 June 2026     | 50.00                 |                     | 1191.47         |               | 0.71                       |                          |   8.52                     |
      | 167       | 17 June 2026     | 50.00                 |                     | 1142.15         |               | 0.68                       |                          |   7.84                     |
      | 168       | 18 June 2026     | 50.00                 |                     | 1092.80         |               | 0.65                       |                          |   7.19                     |
      | 169       | 19 June 2026     | 50.00                 |                     | 1043.43         |               | 0.62                       |                          |   6.57                     |
      | 170       | 20 June 2026     | 50.00                 |                     | 994.02          |               | 0.60                       |                          |   5.97                     |
      | 171       | 21 June 2026     | 50.00                 |                     | 944.59          |               | 0.57                       |                          |   5.4                      |
      | 172       | 22 June 2026     | 50.00                 |                     | 895.13          |               | 0.54                       |                          |   4.86                     |
      | 173       | 23 June 2026     | 50.00                 |                     | 845.64          |               | 0.51                       |                          |   4.35                     |
      | 174       | 24 June 2026     | 50.00                 |                     | 796.13          |               | 0.48                       |                          |   3.87                     |
      | 175       | 25 June 2026     | 50.00                 |                     | 746.58          |               | 0.45                       |                          |   3.42                     |
      | 176       | 26 June 2026     | 50.00                 |                     | 697.01          |               | 0.43                       |                          |   2.99                     |
      | 177       | 27 June 2026     | 50.00                 |                     | 647.41          |               | 0.40                       |                          |   2.59                     |
      | 178       | 28 June 2026     | 50.00                 |                     | 597.78          |               | 0.37                       |                          |   2.22                     |
      | 179       | 29 June 2026     | 50.00                 |                     | 548.12          |               | 0.34                       |                          |   1.88                     |
      | 180       | 30 June 2026     | 50.00                 |                     | 498.43          |               | 0.31                       |                          |   1.57                     |
      | 181       | 01 July 2026     | 50.00                 |                     | 448.72          |               | 0.28                       |                          |   1.29                     |
      | 182       | 02 July 2026     | 50.00                 |                     | 398.97          |               | 0.26                       |                          |   1.03                     |
      | 183       | 03 July 2026     | 50.00                 |                     | 349.20          |               | 0.23                       |                          |   0.8                      |
      | 184       | 04 July 2026     | 50.00                 |                     | 299.40          |               | 0.20                       |                          |   0.6                      |
      | 185       | 05 July 2026     | 50.00                 |                     | 249.57          |               | 0.17                       |                          |   0.43                     |
      | 186       | 06 July 2026     | 50.00                 |                     | 199.71          |               | 0.14                       |                          |   0.29                     |
      | 187       | 07 July 2026     | 50.00                 |                     | 149.83          |               | 0.11                       |                          |   0.18                     |
      | 188       | 08 July 2026     | 50.00                 |                     | 99.91           |               | 0.09                       |                          |   0.09                     |
      | 189       | 09 July 2026     | 50.00                 |                     | 49.97           |               | 0.06                       |                          |   0.03                     |
      | 190       | 10 July 2026     | 50.00                 |                     | 0.00            |               | 0.03                       |                          |   0.0                      |
      | 191       | 11 July 2026     | 50.00                 |                     | 0.00            |               | 0.00                       |                          |   0.0                      |
      | 192       | 12 July 2026     | 50.00                 |                     | 0.00            |               | 0.00                       |                          |   0.0                      |
      | 193       | 13 July 2026     | 50.00                 |                     | 0.00            |               | 0.00                       |                          |   0.0                      |
    And Working Capital Loan has transactions:
      | transactionDate | type                                 | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement                         | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee                         | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Repayment                            | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Discount Fee Amortization            | 9.61              |                  |                   |                       | false    |
      | 08 January 2026 | Discount Fee Adjustment              | 500.0             | 500.0            | 0.0               | 0.0                   | false    |
      | 08 January 2026 | Discount Fee Amortization Adjustment | 4.47              |                  |                   |                       | false    |
    Then Working Capital Loan Transactions tab has a "DISCOUNT_FEE_AMORTIZATION_ADJUSTMENT" transaction with date "08 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name                 | Debit  | Credit |
      | INCOME    | 404000       | Interest Income              | 4.47   |        |
      | LIABILITY | 240005       | Deferred Interest Revenue    |        | 4.47   |
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9500.0    | 50.0               | 5.14           | 494.86           | 0.0               |

  @TestRailId:C83077
  Scenario: Second COB without changes does not duplicate discount fee amortization adjustment - UC2
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin adds Discount fee with "1000" amount on Working Capital loan account for last disbursement
    When Admin sets the business date to "05 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Customer makes repayment on "05 January 2026" with 50 transaction amount on Working Capital loan
    When Admin sets the business date to "08 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Discount Fee Amortization | 9.61              |                  |                   |                       | false    |
    Then Working Capital Loan Transactions tab has a "DISCOUNT_FEE_AMORTIZATION" transaction with date "05 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name                 | Debit  | Credit |
      | INCOME    | 404000       | Interest Income              |        | 9.61   |
      | LIABILITY | 240005       | Deferred Interest Revenue    | 9.61   |        |
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 10000.0   | 50.0               | 9.61           | 990.39           | 0.0               |
    And Admin adds Discount fee adjustment with "500" amount on transaction date "08 January 2026" on Working Capital loan account for last discount
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9500.0    | 50.0               | 9.61           | 490.39           | 0.0               |
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Discount Fee Amortization | 9.61              |                  |                   |                       | false    |
      | 08 January 2026 | Discount Fee Adjustment   | 500.0             | 500.0            | 0.0               | 0.0                   | false    |
    When Admin sets the business date to "09 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    Then Working Capital loan amortization schedule has 194 periods, with the following data for periods:
      | paymentNo | paymentDate      | expectedPaymentAmount | actualPaymentAmount | expectedBalance | actualBalance | expectedAmortizationAmount | actualAmortizationAmount | expectedDiscountFeeBalance |
      | 0         | 01 January 2026  | -9000.00              |                     | 9000.00         | 9000.00       |                            |                          | 500.00                     |
      | 1         | 02 January 2026  | 50.00                 | 0.00                | 8955.14         | 9000.00       | 5.14                       | 0.00                     | 494.86                     |
      | 2         | 03 January 2026  | 50.00                 | 0.00                | 8910.26         | 9000.00       | 5.12                       | 0.00                     | 489.74                     |
      | 3         | 04 January 2026  | 50.00                 | 0.00                | 8865.35         | 9000.00       | 5.09                       | 0.00                     | 484.65                     |
      | 4         | 05 January 2026  | 50.00                 | 50.00               | 8820.42         | 8955.14       | 5.07                       | 5.14                     | 479.58                     |
      | 5         | 06 January 2026  | 50.00                 |                     | 8775.46         |               | 5.04                       |                          | 474.54                     |
      | 6         | 07 January 2026  | 50.00                 |                     | 8730.47         |               | 5.01                       |                          | 469.53                     |
      | 7         | 08 January 2026  | 50.00                 |                     | 8685.46         |               | 4.99                       |                          | 464.54                     |
      | 8         | 09 January 2026  | 50.00                 |                     | 8640.43         |               | 4.96                       |                          | 459.58                     |
      | 9         | 10 January 2026  | 50.00                 |                     | 8595.36         |               | 4.94                       |                          | 454.64                     |
      | 10        | 11 January 2026  | 50.00                 |                     | 8550.28         |               | 4.91                       |                          | 449.73                     |
      | 11        | 12 January 2026  | 50.00                 |                     | 8505.16         |               | 4.89                       |                          | 444.84                     |
      | 12        | 13 January 2026  | 50.00                 |                     | 8460.02         |               | 4.86                       |                          | 439.98                     |
      | 13        | 14 January 2026  | 50.00                 |                     | 8414.86         |               | 4.83                       |                          | 435.15                     |
      | 14        | 15 January 2026  | 50.00                 |                     | 8369.66         |               | 4.81                       |                          | 430.34                     |
      | 15        | 16 January 2026  | 50.00                 |                     | 8324.45         |               | 4.78                       |                          | 425.56                     |
      | 16        | 17 January 2026  | 50.00                 |                     | 8279.20         |               | 4.76                       |                          | 420.80                     |
      | 17        | 18 January 2026  | 50.00                 |                     | 8233.94         |               | 4.73                       |                          | 416.07                     |
      | 18        | 19 January 2026  | 50.00                 |                     | 8188.64         |               | 4.71                       |                          | 411.36                     |
      | 19        | 20 January 2026  | 50.00                 |                     | 8143.32         |               | 4.68                       |                          | 406.68                     |
      | 20        | 21 January 2026  | 50.00                 |                     | 8097.97         |               | 4.65                       |                          | 402.03                     |
      | 21        | 22 January 2026  | 50.00                 |                     | 8052.60         |               | 4.63                       |                          | 397.4                      |
      | 22        | 23 January 2026  | 50.00                 |                     | 8007.20         |               | 4.60                       |                          | 392.8                      |
      | 23        | 24 January 2026  | 50.00                 |                     | 7961.78         |               | 4.58                       |                          | 388.22                     |
      | 24        | 25 January 2026  | 50.00                 |                     | 7916.33         |               | 4.55                       |                          | 383.67                     |
      | 25        | 26 January 2026  | 50.00                 |                     | 7870.85         |               | 4.52                       |                          | 379.15                     |
      | 26        | 27 January 2026  | 50.00                 |                     | 7825.35         |               | 4.50                       |                          | 374.65                     |
      | 27        | 28 January 2026  | 50.00                 |                     | 7779.82         |               | 4.47                       |                          | 370.18                     |
      | 28        | 29 January 2026  | 50.00                 |                     | 7734.27         |               | 4.45                       |                          | 365.73                     |
      | 29        | 30 January 2026  | 50.00                 |                     | 7688.69         |               | 4.42                       |                          | 361.31                     |
      | 30        | 31 January 2026  | 50.00                 |                     | 7643.08         |               | 4.39                       |                          | 356.92                     |
      | 31        | 01 February 2026 | 50.00                 |                     | 7597.45         |               | 4.37                       |                          | 352.55                     |
      | 32        | 02 February 2026 | 50.00                 |                     | 7551.79         |               | 4.34                       |                          | 348.21                     |
      | 33        | 03 February 2026 | 50.00                 |                     | 7506.11         |               | 4.32                       |                          | 343.89                     |
      | 34        | 04 February 2026 | 50.00                 |                     | 7460.40         |               | 4.29                       |                          | 339.6                      |
      | 35        | 05 February 2026 | 50.00                 |                     | 7414.66         |               | 4.26                       |                          | 335.34                     |
      | 36        | 06 February 2026 | 50.00                 |                     | 7368.90         |               | 4.24                       |                          | 331.1                      |
      | 37        | 07 February 2026 | 50.00                 |                     | 7323.11         |               | 4.21                       |                          | 326.89                     |
      | 38        | 08 February 2026 | 50.00                 |                     | 7277.29         |               | 4.18                       |                          | 322.71                     |
      | 39        | 09 February 2026 | 50.00                 |                     | 7231.45         |               | 4.16                       |                          | 318.55                     |
      | 40        | 10 February 2026 | 50.00                 |                     | 7185.58         |               | 4.13                       |                          | 314.42                     |
      | 41        | 11 February 2026 | 50.00                 |                     | 7139.69         |               | 4.11                       |                          | 310.31                     |
      | 42        | 12 February 2026 | 50.00                 |                     | 7093.77         |               | 4.08                       |                          | 306.23                     |
      | 43        | 13 February 2026 | 50.00                 |                     | 7047.82         |               | 4.05                       |                          | 302.18                     |
      | 44        | 14 February 2026 | 50.00                 |                     | 7001.85         |               | 4.03                       |                          | 298.15                     |
      | 45        | 15 February 2026 | 50.00                 |                     | 6955.85         |               | 4.00                       |                          | 294.15                     |
      | 46        | 16 February 2026 | 50.00                 |                     | 6909.83         |               | 3.97                       |                          | 290.18                     |
      | 47        | 17 February 2026 | 50.00                 |                     | 6863.78         |               | 3.95                       |                          | 286.23                     |
      | 48        | 18 February 2026 | 50.00                 |                     | 6817.70         |               | 3.92                       |                          | 282.31                     |
      | 49        | 19 February 2026 | 50.00                 |                     | 6771.59         |               | 3.90                       |                          | 278.41                     |
      | 50        | 20 February 2026 | 50.00                 |                     | 6725.46         |               | 3.87                       |                          | 274.54                     |
      | 51        | 21 February 2026 | 50.00                 |                     | 6679.31         |               | 3.84                       |                          | 270.7                      |
      | 52        | 22 February 2026 | 50.00                 |                     | 6633.12         |               | 3.82                       |                          | 266.88                     |
      | 53        | 23 February 2026 | 50.00                 |                     | 6586.91         |               | 3.79                       |                          | 263.09                     |
      | 54        | 24 February 2026 | 50.00                 |                     | 6540.68         |               | 3.76                       |                          | 259.33                     |
      | 55        | 25 February 2026 | 50.00                 |                     | 6494.42         |               | 3.74                       |                          | 255.59                     |
      | 56        | 26 February 2026 | 50.00                 |                     | 6448.13         |               | 3.71                       |                          | 251.88                     |
      | 57        | 27 February 2026 | 50.00                 |                     | 6401.81         |               | 3.68                       |                          | 248.2                      |
      | 58        | 28 February 2026 | 50.00                 |                     | 6355.47         |               | 3.66                       |                          | 244.54                     |
      | 59        | 01 March 2026    | 50.00                 |                     | 6309.10         |               | 3.63                       |                          | 240.91                     |
      | 60        | 02 March 2026    | 50.00                 |                     | 6262.71         |               | 3.61                       |                          | 237.3                      |
      | 61        | 03 March 2026    | 50.00                 |                     | 6216.29         |               | 3.58                       |                          | 233.72                     |
      | 62        | 04 March 2026    | 50.00                 |                     | 6169.84         |               | 3.55                       |                          | 230.17                     |
      | 63        | 05 March 2026    | 50.00                 |                     | 6123.36         |               | 3.53                       |                          | 226.64                     |
      | 64        | 06 March 2026    | 50.00                 |                     | 6076.86         |               | 3.50                       |                          | 223.14                     |
      | 65        | 07 March 2026    | 50.00                 |                     | 6030.34         |               | 3.47                       |                          | 219.67                     |
      | 66        | 08 March 2026    | 50.00                 |                     | 5983.78         |               | 3.45                       |                          | 216.22                     |
      | 67        | 09 March 2026    | 50.00                 |                     | 5937.20         |               | 3.42                       |                          | 212.8                      |
      | 68        | 10 March 2026    | 50.00                 |                     | 5890.59         |               | 3.39                       |                          | 209.41                     |
      | 69        | 11 March 2026    | 50.00                 |                     | 5843.96         |               | 3.37                       |                          | 206.04                     |
      | 70        | 12 March 2026    | 50.00                 |                     | 5797.30         |               | 3.34                       |                          | 202.7                      |
      | 71        | 13 March 2026    | 50.00                 |                     | 5750.61         |               | 3.31                       |                          | 199.39                     |
      | 72        | 14 March 2026    | 50.00                 |                     | 5703.90         |               | 3.29                       |                          | 196.1                      |
      | 73        | 15 March 2026    | 50.00                 |                     | 5657.16         |               | 3.26                       |                          | 192.84                     |
      | 74        | 16 March 2026    | 50.00                 |                     | 5610.39         |               | 3.23                       |                          | 189.61                     |
      | 75        | 17 March 2026    | 50.00                 |                     | 5563.60         |               | 3.21                       |                          | 186.4                      |
      | 76        | 18 March 2026    | 50.00                 |                     | 5516.78         |               | 3.18                       |                          | 183.22                     |
      | 77        | 19 March 2026    | 50.00                 |                     | 5469.93         |               | 3.15                       |                          | 180.07                     |
      | 78        | 20 March 2026    | 50.00                 |                     | 5423.06         |               | 3.13                       |                          | 176.94                     |
      | 79        | 21 March 2026    | 50.00                 |                     | 5376.15         |               | 3.10                       |                          | 173.84                     |
      | 80        | 22 March 2026    | 50.00                 |                     | 5329.23         |               | 3.07                       |                          | 170.77                     |
      | 81        | 23 March 2026    | 50.00                 |                     | 5282.27         |               | 3.05                       |                          | 167.72                     |
      | 82        | 24 March 2026    | 50.00                 |                     | 5235.29         |               | 3.02                       |                          | 164.7                      |
      | 83        | 25 March 2026    | 50.00                 |                     | 5188.28         |               | 2.99                       |                          | 161.71                     |
      | 84        | 26 March 2026    | 50.00                 |                     | 5141.25         |               | 2.96                       |                          | 158.75                     |
      | 85        | 27 March 2026    | 50.00                 |                     | 5094.18         |               | 2.94                       |                          | 155.81                     |
      | 86        | 28 March 2026    | 50.00                 |                     | 5047.10         |               | 2.91                       |                          | 152.9                      |
      | 87        | 29 March 2026    | 50.00                 |                     | 4999.98         |               | 2.88                       |                          | 150.02                     |
      | 88        | 30 March 2026    | 50.00                 |                     | 4952.84         |               | 2.86                       |                          | 147.16                     |
      | 89        | 31 March 2026    | 50.00                 |                     | 4905.67         |               | 2.83                       |                          | 144.33                     |
      | 90        | 01 April 2026    | 50.00                 |                     | 4858.47         |               | 2.80                       |                          | 141.53                     |
      | 91        | 02 April 2026    | 50.00                 |                     | 4811.25         |               | 2.78                       |                          | 138.75                     |
      | 92        | 03 April 2026    | 50.00                 |                     | 4764.00         |               | 2.75                       |                          | 136.0                      |
      | 93        | 04 April 2026    | 50.00                 |                     | 4716.72         |               | 2.72                       |                          | 133.28                     |
      | 94        | 05 April 2026    | 50.00                 |                     | 4669.41         |               | 2.70                       |                          | 130.58                     |
      | 95        | 06 April 2026    | 50.00                 |                     | 4622.08         |               | 2.67                       |                          | 127.91                     |
      | 96        | 07 April 2026    | 50.00                 |                     | 4574.72         |               | 2.64                       |                          | 125.27                     |
      | 97        | 08 April 2026    | 50.00                 |                     | 4527.34         |               | 2.61                       |                          | 122.66                     |
      | 98        | 09 April 2026    | 50.00                 |                     | 4479.93         |               | 2.59                       |                          | 120.07                     |
      | 99        | 10 April 2026    | 50.00                 |                     | 4432.49         |               | 2.56                       |                          | 117.51                     |
      | 100       | 11 April 2026    | 50.00                 |                     | 4385.02         |               | 2.53                       |                          | 114.98                     |
      | 101       | 12 April 2026    | 50.00                 |                     | 4337.52         |               | 2.51                       |                          | 112.47                     |
      | 102       | 13 April 2026    | 50.00                 |                     | 4290.00         |               | 2.48                       |                          | 109.99                     |
      | 103       | 14 April 2026    | 50.00                 |                     | 4242.45         |               | 2.45                       |                          | 107.54                     |
      | 104       | 15 April 2026    | 50.00                 |                     | 4194.88         |               | 2.42                       |                          | 105.12                     |
      | 105       | 16 April 2026    | 50.00                 |                     | 4147.28         |               | 2.40                       |                          | 102.72                     |
      | 106       | 17 April 2026    | 50.00                 |                     | 4099.65         |               | 2.37                       |                          | 100.35                     |
      | 107       | 18 April 2026    | 50.00                 |                     | 4051.99         |               | 2.34                       |                          |  98.01                     |
      | 108       | 19 April 2026    | 50.00                 |                     | 4004.30         |               | 2.32                       |                          |  95.69                     |
      | 109       | 20 April 2026    | 50.00                 |                     | 3956.59         |               | 2.29                       |                          |  93.4                      |
      | 110       | 21 April 2026    | 50.00                 |                     | 3908.85         |               | 2.26                       |                          |  91.14                     |
      | 111       | 22 April 2026    | 50.00                 |                     | 3861.09         |               | 2.23                       |                          |  88.91                     |
      | 112       | 23 April 2026    | 50.00                 |                     | 3813.29         |               | 2.21                       |                          |  86.7                      |
      | 113       | 24 April 2026    | 50.00                 |                     | 3765.47         |               | 2.18                       |                          |  84.52                     |
      | 114       | 25 April 2026    | 50.00                 |                     | 3717.62         |               | 2.15                       |                          |  82.37                     |
      | 115       | 26 April 2026    | 50.00                 |                     | 3669.75         |               | 2.12                       |                          |  80.25                     |
      | 116       | 27 April 2026    | 50.00                 |                     | 3621.85         |               | 2.10                       |                          |  78.15                     |
      | 117       | 28 April 2026    | 50.00                 |                     | 3573.92         |               | 2.07                       |                          |  76.08                     |
      | 118       | 29 April 2026    | 50.00                 |                     | 3525.96         |               | 2.04                       |                          |  74.04                     |
      | 119       | 30 April 2026    | 50.00                 |                     | 3477.97         |               | 2.01                       |                          |  72.03                     |
      | 120       | 01 May 2026      | 50.00                 |                     | 3429.96         |               | 1.99                       |                          |  70.04                     |
      | 121       | 02 May 2026      | 50.00                 |                     | 3381.92         |               | 1.96                       |                          |  68.08                     |
      | 122       | 03 May 2026      | 50.00                 |                     | 3333.85         |               | 1.93                       |                          |  66.15                     |
      | 123       | 04 May 2026      | 50.00                 |                     | 3285.76         |               | 1.91                       |                          |  64.24                     |
      | 124       | 05 May 2026      | 50.00                 |                     | 3237.64         |               | 1.88                       |                          |  62.36                     |
      | 125       | 06 May 2026      | 50.00                 |                     | 3189.49         |               | 1.85                       |                          |  60.51                     |
      | 126       | 07 May 2026      | 50.00                 |                     | 3141.31         |               | 1.82                       |                          |  58.69                     |
      | 127       | 08 May 2026      | 50.00                 |                     | 3093.10         |               | 1.80                       |                          |  56.89                     |
      | 128       | 09 May 2026      | 50.00                 |                     | 3044.87         |               | 1.77                       |                          |  55.12                     |
      | 129       | 10 May 2026      | 50.00                 |                     | 2996.61         |               | 1.74                       |                          |  53.38                     |
      | 130       | 11 May 2026      | 50.00                 |                     | 2948.32         |               | 1.71                       |                          |  51.67                     |
      | 131       | 12 May 2026      | 50.00                 |                     | 2900.01         |               | 1.68                       |                          |  49.99                     |
      | 132       | 13 May 2026      | 50.00                 |                     | 2851.67         |               | 1.66                       |                          |  48.33                     |
      | 133       | 14 May 2026      | 50.00                 |                     | 2803.30         |               | 1.63                       |                          |  46.7                      |
      | 134       | 15 May 2026      | 50.00                 |                     | 2754.90         |               | 1.60                       |                          |  45.1                      |
      | 135       | 16 May 2026      | 50.00                 |                     | 2706.47         |               | 1.57                       |                          |  43.53                     |
      | 136       | 17 May 2026      | 50.00                 |                     | 2658.02         |               | 1.55                       |                          |  41.98                     |
      | 137       | 18 May 2026      | 50.00                 |                     | 2609.54         |               | 1.52                       |                          |  40.46                     |
      | 138       | 19 May 2026      | 50.00                 |                     | 2561.03         |               | 1.49                       |                          |  38.97                     |
      | 139       | 20 May 2026      | 50.00                 |                     | 2512.49         |               | 1.46                       |                          |  37.51                     |
      | 140       | 21 May 2026      | 50.00                 |                     | 2463.93         |               | 1.44                       |                          |  36.07                     |
      | 141       | 22 May 2026      | 50.00                 |                     | 2415.34         |               | 1.41                       |                          |  34.66                     |
      | 142       | 23 May 2026      | 50.00                 |                     | 2366.72         |               | 1.38                       |                          |  33.28                     |
      | 143       | 24 May 2026      | 50.00                 |                     | 2318.07         |               | 1.35                       |                          |  31.93                     |
      | 144       | 25 May 2026      | 50.00                 |                     | 2269.39         |               | 1.32                       |                          |  30.61                     |
      | 145       | 26 May 2026      | 50.00                 |                     | 2220.69         |               | 1.30                       |                          |  29.31                     |
      | 146       | 27 May 2026      | 50.00                 |                     | 2171.96         |               | 1.27                       |                          |  28.04                     |
      | 147       | 28 May 2026      | 50.00                 |                     | 2123.20         |               | 1.24                       |                          |  26.8                      |
      | 148       | 29 May 2026      | 50.00                 |                     | 2074.41         |               | 1.21                       |                          |  25.59                     |
      | 149       | 30 May 2026      | 50.00                 |                     | 2025.60         |               | 1.19                       |                          |  24.4                      |
      | 150       | 31 May 2026      | 50.00                 |                     | 1976.76         |               | 1.16                       |                          |  23.24                     |
      | 151       | 01 June 2026     | 50.00                 |                     | 1927.89         |               | 1.13                       |                          |  22.11                     |
      | 152       | 02 June 2026     | 50.00                 |                     | 1878.99         |               | 1.10                       |                          |  21.01                     |
      | 153       | 03 June 2026     | 50.00                 |                     | 1830.06         |               | 1.07                       |                          |  19.94                     |
      | 154       | 04 June 2026     | 50.00                 |                     | 1781.11         |               | 1.05                       |                          |  18.89                     |
      | 155       | 05 June 2026     | 50.00                 |                     | 1732.13         |               | 1.02                       |                          |  17.87                     |
      | 156       | 06 June 2026     | 50.00                 |                     | 1683.12         |               | 0.99                       |                          |  16.88                     |
      | 157       | 07 June 2026     | 50.00                 |                     | 1634.08         |               | 0.96                       |                          |  15.92                     |
      | 158       | 08 June 2026     | 50.00                 |                     | 1585.01         |               | 0.93                       |                          |  14.99                     |
      | 159       | 09 June 2026     | 50.00                 |                     | 1535.92         |               | 0.91                       |                          |  14.08                     |
      | 160       | 10 June 2026     | 50.00                 |                     | 1486.79         |               | 0.88                       |                          |  13.2                      |
      | 161       | 11 June 2026     | 50.00                 |                     | 1437.64         |               | 0.85                       |                          |  12.35                     |
      | 162       | 12 June 2026     | 50.00                 |                     | 1388.47         |               | 0.82                       |                          |  11.53                     |
      | 163       | 13 June 2026     | 50.00                 |                     | 1339.26         |               | 0.79                       |                          |  10.74                     |
      | 164       | 14 June 2026     | 50.00                 |                     | 1290.02         |               | 0.77                       |                          |   9.97                     |
      | 165       | 15 June 2026     | 50.00                 |                     | 1240.76         |               | 0.74                       |                          |   9.23                     |
      | 166       | 16 June 2026     | 50.00                 |                     | 1191.47         |               | 0.71                       |                          |   8.52                     |
      | 167       | 17 June 2026     | 50.00                 |                     | 1142.15         |               | 0.68                       |                          |   7.84                     |
      | 168       | 18 June 2026     | 50.00                 |                     | 1092.80         |               | 0.65                       |                          |   7.19                     |
      | 169       | 19 June 2026     | 50.00                 |                     | 1043.43         |               | 0.62                       |                          |   6.57                     |
      | 170       | 20 June 2026     | 50.00                 |                     | 994.02          |               | 0.60                       |                          |   5.97                     |
      | 171       | 21 June 2026     | 50.00                 |                     | 944.59          |               | 0.57                       |                          |   5.4                      |
      | 172       | 22 June 2026     | 50.00                 |                     | 895.13          |               | 0.54                       |                          |   4.86                     |
      | 173       | 23 June 2026     | 50.00                 |                     | 845.64          |               | 0.51                       |                          |   4.35                     |
      | 174       | 24 June 2026     | 50.00                 |                     | 796.13          |               | 0.48                       |                          |   3.87                     |
      | 175       | 25 June 2026     | 50.00                 |                     | 746.58          |               | 0.45                       |                          |   3.42                     |
      | 176       | 26 June 2026     | 50.00                 |                     | 697.01          |               | 0.43                       |                          |   2.99                     |
      | 177       | 27 June 2026     | 50.00                 |                     | 647.41          |               | 0.40                       |                          |   2.59                     |
      | 178       | 28 June 2026     | 50.00                 |                     | 597.78          |               | 0.37                       |                          |   2.22                     |
      | 179       | 29 June 2026     | 50.00                 |                     | 548.12          |               | 0.34                       |                          |   1.88                     |
      | 180       | 30 June 2026     | 50.00                 |                     | 498.43          |               | 0.31                       |                          |   1.57                     |
      | 181       | 01 July 2026     | 50.00                 |                     | 448.72          |               | 0.28                       |                          |   1.29                     |
      | 182       | 02 July 2026     | 50.00                 |                     | 398.97          |               | 0.26                       |                          |   1.03                     |
      | 183       | 03 July 2026     | 50.00                 |                     | 349.20          |               | 0.23                       |                          |   0.8                      |
      | 184       | 04 July 2026     | 50.00                 |                     | 299.40          |               | 0.20                       |                          |   0.6                      |
      | 185       | 05 July 2026     | 50.00                 |                     | 249.57          |               | 0.17                       |                          |   0.43                     |
      | 186       | 06 July 2026     | 50.00                 |                     | 199.71          |               | 0.14                       |                          |   0.29                     |
      | 187       | 07 July 2026     | 50.00                 |                     | 149.83          |               | 0.11                       |                          |   0.18                     |
      | 188       | 08 July 2026     | 50.00                 |                     | 99.91           |               | 0.09                       |                          |   0.09                     |
      | 189       | 09 July 2026     | 50.00                 |                     | 49.97           |               | 0.06                       |                          |   0.03                     |
      | 190       | 10 July 2026     | 50.00                 |                     | 0.00            |               | 0.03                       |                          |   0.0                      |
      | 191       | 11 July 2026     | 50.00                 |                     | 0.00            |               | 0.00                       |                          |   0.0                      |
      | 192       | 12 July 2026     | 50.00                 |                     | 0.00            |               | 0.00                       |                          |   0.0                      |
      | 193       | 13 July 2026     | 50.00                 |                     | 0.00            |               | 0.00                       |                          |   0.0                      |
    And Working Capital Loan has transactions:
      | transactionDate | type                                 | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement                         | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee                         | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Repayment                            | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Discount Fee Amortization            | 9.61              |                  |                   |                       | false    |
      | 08 January 2026 | Discount Fee Adjustment              | 500.0             | 500.0            | 0.0               | 0.0                   | false    |
      | 08 January 2026 | Discount Fee Amortization Adjustment | 4.47              |                  |                   |                       | false    |
    Then Working Capital Loan Transactions tab has a "DISCOUNT_FEE_AMORTIZATION_ADJUSTMENT" transaction with date "08 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name                 | Debit  | Credit |
      | INCOME    | 404000       | Interest Income              | 4.47   |        |
      | LIABILITY | 240005       | Deferred Interest Revenue    |        | 4.47   |
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9500.0    | 50.0               | 5.14           | 494.86           | 0.0               |
    When Admin sets the business date to "10 February 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Working Capital Loan has transactions:
      | transactionDate | type                                 | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement                         | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee                         | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Repayment                            | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Discount Fee Amortization            | 9.61              |                  |                   |                       | false    |
      | 08 January 2026 | Discount Fee Adjustment              | 500.0             | 500.0            | 0.0               | 0.0                   | false    |
      | 08 January 2026 | Discount Fee Amortization Adjustment | 4.47              |                  |                   |                       | false    |
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9500.0    | 50.0               | 5.14           | 494.86           | 0.0               |

  @TestRailId:C83078
  Scenario: Discount fee amortization adjustment created after multiple repayment transactions and discount fee adjustment - UC3
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    And Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    And Admin adds Discount fee with "1000" amount on Working Capital loan account for last disbursement
    When Admin sets the business date to "02 January 2026"
    And Customer makes repayment on "02 January 2026" with 50 transaction amount on Working Capital loan
    When Admin sets the business date to "03 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    Then Working Capital Loan Transactions tab has a "DISCOUNT_FEE_AMORTIZATION" transaction with date "02 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name                 | Debit  | Credit |
      | INCOME    | 404000       | Interest Income              |        | 9.61   |
      | LIABILITY | 240005       | Deferred Interest Revenue    | 9.61   |        |
    And Customer makes repayment on "03 January 2026" with 50 transaction amount on Working Capital loan
    When Admin sets the business date to "04 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    Then Working Capital Loan Transactions tab has a "DISCOUNT_FEE_AMORTIZATION" transaction with date "03 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name                 | Debit  | Credit |
      | INCOME    | 404000       | Interest Income              |        | 9.57   |
      | LIABILITY | 240005       | Deferred Interest Revenue    | 9.57   |        |
    And Customer makes repayment on "04 January 2026" with 50 transaction amount on Working Capital loan
    When Admin sets the business date to "05 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    Then Working Capital Loan Transactions tab has a "DISCOUNT_FEE_AMORTIZATION" transaction with date "04 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name                 | Debit  | Credit |
      | INCOME    | 404000       | Interest Income              |        | 9.52   |
      | LIABILITY | 240005       | Deferred Interest Revenue    | 9.52   |        |
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Amortization | 9.61              |                  |                   |                       | false    |
      | 03 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 03 January 2026 | Discount Fee Amortization | 9.57              |                  |                   |                       | false    |
      | 04 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 04 January 2026 | Discount Fee Amortization | 9.52              |                  |                   |                       | false    |
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 10000.0   | 150.0              | 28.7           | 971.3            | 0.0               |
    And Admin adds Discount fee adjustment with "500" amount on transaction date "05 January 2026" on Working Capital loan account for last discount
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9500.0    | 150.0              | 28.7           | 471.3            | 0.0               |
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Amortization | 9.61              |                  |                   |                       | false    |
      | 03 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 03 January 2026 | Discount Fee Amortization | 9.57              |                  |                   |                       | false    |
      | 04 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 04 January 2026 | Discount Fee Amortization | 9.52              |                  |                   |                       | false    |
      | 05 January 2026 | Discount Fee Adjustment   | 500.0             | 500.0            | 0.0               | 0.0                   | false    |
    When Admin sets the business date to "09 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    Then Working Capital loan amortization schedule has 191 periods, with the following data for periods:
      | paymentNo | paymentDate      | expectedPaymentAmount | actualPaymentAmount | expectedBalance | expectedAmortizationAmount | actualAmortizationAmount | expectedDiscountFeeBalance |
      | 0         | 01 January 2026  | -9000.00              |                     | 9000.00         |                            |                          | 500.00                     |
      | 1         | 02 January 2026  | 50.00                 | 50.00               | 8955.14         | 5.14                       | 5.14                     | 494.86                     |
      | 2         | 03 January 2026  | 50.00                 | 50.00               | 8910.26         | 5.12                       | 5.12                     | 489.74                     |
      | 3         | 04 January 2026  | 50.00                 | 50.00               | 8865.35         | 5.09                       | 5.09                     | 484.65                     |
      | 4         | 05 January 2026  | 50.00                 |                     | 8820.42         | 5.07                       |                          | 479.58                     |
      | 5         | 06 January 2026  | 50.00                 |                     | 8775.46         | 5.04                       |                          | 474.54                     |
      | 6         | 07 January 2026  | 50.00                 |                     | 8730.47         | 5.01                       |                          | 469.53                     |
      | 7         | 08 January 2026  | 50.00                 |                     | 8685.46         | 4.99                       |                          | 464.54                     |
      | 8         | 09 January 2026  | 50.00                 |                     | 8640.43         | 4.96                       |                          | 459.58                     |
      | 9         | 10 January 2026  | 50.00                 |                     | 8595.36         | 4.94                       |                          | 454.64                     |
      | 10        | 11 January 2026  | 50.00                 |                     | 8550.28         | 4.91                       |                          | 449.73                     |
      | 11        | 12 January 2026  | 50.00                 |                     | 8505.16         | 4.89                       |                          | 444.84                     |
      | 12        | 13 January 2026  | 50.00                 |                     | 8460.02         | 4.86                       |                          | 439.98                     |
      | 13        | 14 January 2026  | 50.00                 |                     | 8414.86         | 4.83                       |                          | 435.15                     |
      | 14        | 15 January 2026  | 50.00                 |                     | 8369.66         | 4.81                       |                          | 430.34                     |
      | 15        | 16 January 2026  | 50.00                 |                     | 8324.45         | 4.78                       |                          | 425.56                     |
      | 16        | 17 January 2026  | 50.00                 |                     | 8279.20         | 4.76                       |                          | 420.80                     |
      | 17        | 18 January 2026  | 50.00                 |                     | 8233.94         | 4.73                       |                          | 416.07                     |
      | 18        | 19 January 2026  | 50.00                 |                     | 8188.64         | 4.71                       |                          | 411.36                     |
      | 19        | 20 January 2026  | 50.00                 |                     | 8143.32         | 4.68                       |                          | 406.68                     |
      | 20        | 21 January 2026  | 50.00                 |                     | 8097.97         | 4.65                       |                          | 402.03                     |
      | 21        | 22 January 2026  | 50.00                 |                     | 8052.60         | 4.63                       |                          | 397.4                      |
      | 22        | 23 January 2026  | 50.00                 |                     | 8007.20         | 4.60                       |                          | 392.8                      |
      | 23        | 24 January 2026  | 50.00                 |                     | 7961.78         | 4.58                       |                          | 388.22                     |
      | 24        | 25 January 2026  | 50.00                 |                     | 7916.33         | 4.55                       |                          | 383.67                     |
      | 25        | 26 January 2026  | 50.00                 |                     | 7870.85         | 4.52                       |                          | 379.15                     |
      | 26        | 27 January 2026  | 50.00                 |                     | 7825.35         | 4.50                       |                          | 374.65                     |
      | 27        | 28 January 2026  | 50.00                 |                     | 7779.82         | 4.47                       |                          | 370.18                     |
      | 28        | 29 January 2026  | 50.00                 |                     | 7734.27         | 4.45                       |                          | 365.73                     |
      | 29        | 30 January 2026  | 50.00                 |                     | 7688.69         | 4.42                       |                          | 361.31                     |
      | 30        | 31 January 2026  | 50.00                 |                     | 7643.08         | 4.39                       |                          | 356.92                     |
      | 31        | 01 February 2026 | 50.00                 |                     | 7597.45         | 4.37                       |                          | 352.55                     |
      | 32        | 02 February 2026 | 50.00                 |                     | 7551.79         | 4.34                       |                          | 348.21                     |
      | 33        | 03 February 2026 | 50.00                 |                     | 7506.11         | 4.32                       |                          | 343.89                     |
      | 34        | 04 February 2026 | 50.00                 |                     | 7460.40         | 4.29                       |                          | 339.6                      |
      | 35        | 05 February 2026 | 50.00                 |                     | 7414.66         | 4.26                       |                          | 335.34                     |
      | 36        | 06 February 2026 | 50.00                 |                     | 7368.90         | 4.24                       |                          | 331.1                      |
      | 37        | 07 February 2026 | 50.00                 |                     | 7323.11         | 4.21                       |                          | 326.89                     |
      | 38        | 08 February 2026 | 50.00                 |                     | 7277.29         | 4.18                       |                          | 322.71                     |
      | 39        | 09 February 2026 | 50.00                 |                     | 7231.45         | 4.16                       |                          | 318.55                     |
      | 40        | 10 February 2026 | 50.00                 |                     | 7185.58         | 4.13                       |                          | 314.42                     |
      | 41        | 11 February 2026 | 50.00                 |                     | 7139.69         | 4.11                       |                          | 310.31                     |
      | 42        | 12 February 2026 | 50.00                 |                     | 7093.77         | 4.08                       |                          | 306.23                     |
      | 43        | 13 February 2026 | 50.00                 |                     | 7047.82         | 4.05                       |                          | 302.18                     |
      | 44        | 14 February 2026 | 50.00                 |                     | 7001.85         | 4.03                       |                          | 298.15                     |
      | 45        | 15 February 2026 | 50.00                 |                     | 6955.85         | 4.00                       |                          | 294.15                     |
      | 46        | 16 February 2026 | 50.00                 |                     | 6909.83         | 3.97                       |                          | 290.18                     |
      | 47        | 17 February 2026 | 50.00                 |                     | 6863.78         | 3.95                       |                          | 286.23                     |
      | 48        | 18 February 2026 | 50.00                 |                     | 6817.70         | 3.92                       |                          | 282.31                     |
      | 49        | 19 February 2026 | 50.00                 |                     | 6771.59         | 3.90                       |                          | 278.41                     |
      | 50        | 20 February 2026 | 50.00                 |                     | 6725.46         | 3.87                       |                          | 274.54                     |
      | 51        | 21 February 2026 | 50.00                 |                     | 6679.31         | 3.84                       |                          | 270.7                      |
      | 52        | 22 February 2026 | 50.00                 |                     | 6633.12         | 3.82                       |                          | 266.88                     |
      | 53        | 23 February 2026 | 50.00                 |                     | 6586.91         | 3.79                       |                          | 263.09                     |
      | 54        | 24 February 2026 | 50.00                 |                     | 6540.68         | 3.76                       |                          | 259.33                     |
      | 55        | 25 February 2026 | 50.00                 |                     | 6494.42         | 3.74                       |                          | 255.59                     |
      | 56        | 26 February 2026 | 50.00                 |                     | 6448.13         | 3.71                       |                          | 251.88                     |
      | 57        | 27 February 2026 | 50.00                 |                     | 6401.81         | 3.68                       |                          | 248.2                      |
      | 58        | 28 February 2026 | 50.00                 |                     | 6355.47         | 3.66                       |                          | 244.54                     |
      | 59        | 01 March 2026    | 50.00                 |                     | 6309.10         | 3.63                       |                          | 240.91                     |
      | 60        | 02 March 2026    | 50.00                 |                     | 6262.71         | 3.61                       |                          | 237.3                      |
      | 61        | 03 March 2026    | 50.00                 |                     | 6216.29         | 3.58                       |                          | 233.72                     |
      | 62        | 04 March 2026    | 50.00                 |                     | 6169.84         | 3.55                       |                          | 230.17                     |
      | 63        | 05 March 2026    | 50.00                 |                     | 6123.36         | 3.53                       |                          | 226.64                     |
      | 64        | 06 March 2026    | 50.00                 |                     | 6076.86         | 3.50                       |                          | 223.14                     |
      | 65        | 07 March 2026    | 50.00                 |                     | 6030.34         | 3.47                       |                          | 219.67                     |
      | 66        | 08 March 2026    | 50.00                 |                     | 5983.78         | 3.45                       |                          | 216.22                     |
      | 67        | 09 March 2026    | 50.00                 |                     | 5937.20         | 3.42                       |                          | 212.8                      |
      | 68        | 10 March 2026    | 50.00                 |                     | 5890.59         | 3.39                       |                          | 209.41                     |
      | 69        | 11 March 2026    | 50.00                 |                     | 5843.96         | 3.37                       |                          | 206.04                     |
      | 70        | 12 March 2026    | 50.00                 |                     | 5797.30         | 3.34                       |                          | 202.7                      |
      | 71        | 13 March 2026    | 50.00                 |                     | 5750.61         | 3.31                       |                          | 199.39                     |
      | 72        | 14 March 2026    | 50.00                 |                     | 5703.90         | 3.29                       |                          | 196.1                      |
      | 73        | 15 March 2026    | 50.00                 |                     | 5657.16         | 3.26                       |                          | 192.84                     |
      | 74        | 16 March 2026    | 50.00                 |                     | 5610.39         | 3.23                       |                          | 189.61                     |
      | 75        | 17 March 2026    | 50.00                 |                     | 5563.60         | 3.21                       |                          | 186.4                      |
      | 76        | 18 March 2026    | 50.00                 |                     | 5516.78         | 3.18                       |                          | 183.22                     |
      | 77        | 19 March 2026    | 50.00                 |                     | 5469.93         | 3.15                       |                          | 180.07                     |
      | 78        | 20 March 2026    | 50.00                 |                     | 5423.06         | 3.13                       |                          | 176.94                     |
      | 79        | 21 March 2026    | 50.00                 |                     | 5376.15         | 3.10                       |                          | 173.84                     |
      | 80        | 22 March 2026    | 50.00                 |                     | 5329.23         | 3.07                       |                          | 170.77                     |
      | 81        | 23 March 2026    | 50.00                 |                     | 5282.27         | 3.05                       |                          | 167.72                     |
      | 82        | 24 March 2026    | 50.00                 |                     | 5235.29         | 3.02                       |                          | 164.7                      |
      | 83        | 25 March 2026    | 50.00                 |                     | 5188.28         | 2.99                       |                          | 161.71                     |
      | 84        | 26 March 2026    | 50.00                 |                     | 5141.25         | 2.96                       |                          | 158.75                     |
      | 85        | 27 March 2026    | 50.00                 |                     | 5094.18         | 2.94                       |                          | 155.81                     |
      | 86        | 28 March 2026    | 50.00                 |                     | 5047.10         | 2.91                       |                          | 152.9                      |
      | 87        | 29 March 2026    | 50.00                 |                     | 4999.98         | 2.88                       |                          | 150.02                     |
      | 88        | 30 March 2026    | 50.00                 |                     | 4952.84         | 2.86                       |                          | 147.16                     |
      | 89        | 31 March 2026    | 50.00                 |                     | 4905.67         | 2.83                       |                          | 144.33                     |
      | 90        | 01 April 2026    | 50.00                 |                     | 4858.47         | 2.80                       |                          | 141.53                     |
      | 91        | 02 April 2026    | 50.00                 |                     | 4811.25         | 2.78                       |                          | 138.75                     |
      | 92        | 03 April 2026    | 50.00                 |                     | 4764.00         | 2.75                       |                          | 136.0                      |
      | 93        | 04 April 2026    | 50.00                 |                     | 4716.72         | 2.72                       |                          | 133.28                     |
      | 94        | 05 April 2026    | 50.00                 |                     | 4669.41         | 2.70                       |                          | 130.58                     |
      | 95        | 06 April 2026    | 50.00                 |                     | 4622.08         | 2.67                       |                          | 127.91                     |
      | 96        | 07 April 2026    | 50.00                 |                     | 4574.72         | 2.64                       |                          | 125.27                     |
      | 97        | 08 April 2026    | 50.00                 |                     | 4527.34         | 2.61                       |                          | 122.66                     |
      | 98        | 09 April 2026    | 50.00                 |                     | 4479.93         | 2.59                       |                          | 120.07                     |
      | 99        | 10 April 2026    | 50.00                 |                     | 4432.49         | 2.56                       |                          | 117.51                     |
      | 100       | 11 April 2026    | 50.00                 |                     | 4385.02         | 2.53                       |                          | 114.98                     |
      | 101       | 12 April 2026    | 50.00                 |                     | 4337.52         | 2.51                       |                          | 112.47                     |
      | 102       | 13 April 2026    | 50.00                 |                     | 4290.00         | 2.48                       |                          | 109.99                     |
      | 103       | 14 April 2026    | 50.00                 |                     | 4242.45         | 2.45                       |                          | 107.54                     |
      | 104       | 15 April 2026    | 50.00                 |                     | 4194.88         | 2.42                       |                          | 105.12                     |
      | 105       | 16 April 2026    | 50.00                 |                     | 4147.28         | 2.40                       |                          | 102.72                     |
      | 106       | 17 April 2026    | 50.00                 |                     | 4099.65         | 2.37                       |                          | 100.35                     |
      | 107       | 18 April 2026    | 50.00                 |                     | 4051.99         | 2.34                       |                          |  98.01                     |
      | 108       | 19 April 2026    | 50.00                 |                     | 4004.30         | 2.32                       |                          |  95.69                     |
      | 109       | 20 April 2026    | 50.00                 |                     | 3956.59         | 2.29                       |                          |  93.4                      |
      | 110       | 21 April 2026    | 50.00                 |                     | 3908.85         | 2.26                       |                          |  91.14                     |
      | 111       | 22 April 2026    | 50.00                 |                     | 3861.09         | 2.23                       |                          |  88.91                     |
      | 112       | 23 April 2026    | 50.00                 |                     | 3813.29         | 2.21                       |                          |  86.7                      |
      | 113       | 24 April 2026    | 50.00                 |                     | 3765.47         | 2.18                       |                          |  84.52                     |
      | 114       | 25 April 2026    | 50.00                 |                     | 3717.62         | 2.15                       |                          |  82.37                     |
      | 115       | 26 April 2026    | 50.00                 |                     | 3669.75         | 2.12                       |                          |  80.25                     |
      | 116       | 27 April 2026    | 50.00                 |                     | 3621.85         | 2.10                       |                          |  78.15                     |
      | 117       | 28 April 2026    | 50.00                 |                     | 3573.92         | 2.07                       |                          |  76.08                     |
      | 118       | 29 April 2026    | 50.00                 |                     | 3525.96         | 2.04                       |                          |  74.04                     |
      | 119       | 30 April 2026    | 50.00                 |                     | 3477.97         | 2.01                       |                          |  72.03                     |
      | 120       | 01 May 2026      | 50.00                 |                     | 3429.96         | 1.99                       |                          |  70.04                     |
      | 121       | 02 May 2026      | 50.00                 |                     | 3381.92         | 1.96                       |                          |  68.08                     |
      | 122       | 03 May 2026      | 50.00                 |                     | 3333.85         | 1.93                       |                          |  66.15                     |
      | 123       | 04 May 2026      | 50.00                 |                     | 3285.76         | 1.91                       |                          |  64.24                     |
      | 124       | 05 May 2026      | 50.00                 |                     | 3237.64         | 1.88                       |                          |  62.36                     |
      | 125       | 06 May 2026      | 50.00                 |                     | 3189.49         | 1.85                       |                          |  60.51                     |
      | 126       | 07 May 2026      | 50.00                 |                     | 3141.31         | 1.82                       |                          |  58.69                     |
      | 127       | 08 May 2026      | 50.00                 |                     | 3093.10         | 1.80                       |                          |  56.89                     |
      | 128       | 09 May 2026      | 50.00                 |                     | 3044.87         | 1.77                       |                          |  55.12                     |
      | 129       | 10 May 2026      | 50.00                 |                     | 2996.61         | 1.74                       |                          |  53.38                     |
      | 130       | 11 May 2026      | 50.00                 |                     | 2948.32         | 1.71                       |                          |  51.67                     |
      | 131       | 12 May 2026      | 50.00                 |                     | 2900.01         | 1.68                       |                          |  49.99                     |
      | 132       | 13 May 2026      | 50.00                 |                     | 2851.67         | 1.66                       |                          |  48.33                     |
      | 133       | 14 May 2026      | 50.00                 |                     | 2803.30         | 1.63                       |                          |  46.7                      |
      | 134       | 15 May 2026      | 50.00                 |                     | 2754.90         | 1.60                       |                          |  45.1                      |
      | 135       | 16 May 2026      | 50.00                 |                     | 2706.47         | 1.57                       |                          |  43.53                     |
      | 136       | 17 May 2026      | 50.00                 |                     | 2658.02         | 1.55                       |                          |  41.98                     |
      | 137       | 18 May 2026      | 50.00                 |                     | 2609.54         | 1.52                       |                          |  40.46                     |
      | 138       | 19 May 2026      | 50.00                 |                     | 2561.03         | 1.49                       |                          |  38.97                     |
      | 139       | 20 May 2026      | 50.00                 |                     | 2512.49         | 1.46                       |                          |  37.51                     |
      | 140       | 21 May 2026      | 50.00                 |                     | 2463.93         | 1.44                       |                          |  36.07                     |
      | 141       | 22 May 2026      | 50.00                 |                     | 2415.34         | 1.41                       |                          |  34.66                     |
      | 142       | 23 May 2026      | 50.00                 |                     | 2366.72         | 1.38                       |                          |  33.28                     |
      | 143       | 24 May 2026      | 50.00                 |                     | 2318.07         | 1.35                       |                          |  31.93                     |
      | 144       | 25 May 2026      | 50.00                 |                     | 2269.39         | 1.32                       |                          |  30.61                     |
      | 145       | 26 May 2026      | 50.00                 |                     | 2220.69         | 1.30                       |                          |  29.31                     |
      | 146       | 27 May 2026      | 50.00                 |                     | 2171.96         | 1.27                       |                          |  28.04                     |
      | 147       | 28 May 2026      | 50.00                 |                     | 2123.20         | 1.24                       |                          |  26.8                      |
      | 148       | 29 May 2026      | 50.00                 |                     | 2074.41         | 1.21                       |                          |  25.59                     |
      | 149       | 30 May 2026      | 50.00                 |                     | 2025.60         | 1.19                       |                          |  24.4                      |
      | 150       | 31 May 2026      | 50.00                 |                     | 1976.76         | 1.16                       |                          |  23.24                     |
      | 151       | 01 June 2026     | 50.00                 |                     | 1927.89         | 1.13                       |                          |  22.11                     |
      | 152       | 02 June 2026     | 50.00                 |                     | 1878.99         | 1.10                       |                          |  21.01                     |
      | 153       | 03 June 2026     | 50.00                 |                     | 1830.06         | 1.07                       |                          |  19.94                     |
      | 154       | 04 June 2026     | 50.00                 |                     | 1781.11         | 1.05                       |                          |  18.89                     |
      | 155       | 05 June 2026     | 50.00                 |                     | 1732.13         | 1.02                       |                          |  17.87                     |
      | 156       | 06 June 2026     | 50.00                 |                     | 1683.12         | 0.99                       |                          |  16.88                     |
      | 157       | 07 June 2026     | 50.00                 |                     | 1634.08         | 0.96                       |                          |  15.92                     |
      | 158       | 08 June 2026     | 50.00                 |                     | 1585.01         | 0.93                       |                          |  14.99                     |
      | 159       | 09 June 2026     | 50.00                 |                     | 1535.92         | 0.91                       |                          |  14.08                     |
      | 160       | 10 June 2026     | 50.00                 |                     | 1486.79         | 0.88                       |                          |  13.2                      |
      | 161       | 11 June 2026     | 50.00                 |                     | 1437.64         | 0.85                       |                          |  12.35                     |
      | 162       | 12 June 2026     | 50.00                 |                     | 1388.47         | 0.82                       |                          |  11.53                     |
      | 163       | 13 June 2026     | 50.00                 |                     | 1339.26         | 0.79                       |                          |  10.74                     |
      | 164       | 14 June 2026     | 50.00                 |                     | 1290.02         | 0.77                       |                          |   9.97                     |
      | 165       | 15 June 2026     | 50.00                 |                     | 1240.76         | 0.74                       |                          |   9.23                     |
      | 166       | 16 June 2026     | 50.00                 |                     | 1191.47         | 0.71                       |                          |   8.52                     |
      | 167       | 17 June 2026     | 50.00                 |                     | 1142.15         | 0.68                       |                          |   7.84                     |
      | 168       | 18 June 2026     | 50.00                 |                     | 1092.80         | 0.65                       |                          |   7.19                     |
      | 169       | 19 June 2026     | 50.00                 |                     | 1043.43         | 0.62                       |                          |   6.57                     |
      | 170       | 20 June 2026     | 50.00                 |                     | 994.02          | 0.60                       |                          |   5.97                     |
      | 171       | 21 June 2026     | 50.00                 |                     | 944.59          | 0.57                       |                          |   5.4                      |
      | 172       | 22 June 2026     | 50.00                 |                     | 895.13          | 0.54                       |                          |   4.86                     |
      | 173       | 23 June 2026     | 50.00                 |                     | 845.64          | 0.51                       |                          |   4.35                     |
      | 174       | 24 June 2026     | 50.00                 |                     | 796.13          | 0.48                       |                          |   3.87                     |
      | 175       | 25 June 2026     | 50.00                 |                     | 746.58          | 0.45                       |                          |   3.42                     |
      | 176       | 26 June 2026     | 50.00                 |                     | 697.01          | 0.43                       |                          |   2.99                     |
      | 177       | 27 June 2026     | 50.00                 |                     | 647.41          | 0.40                       |                          |   2.59                     |
      | 178       | 28 June 2026     | 50.00                 |                     | 597.78          | 0.37                       |                          |   2.22                     |
      | 179       | 29 June 2026     | 50.00                 |                     | 548.12          | 0.34                       |                          |   1.88                     |
      | 180       | 30 June 2026     | 50.00                 |                     | 498.43          | 0.31                       |                          |   1.57                     |
      | 181       | 01 July 2026     | 50.00                 |                     | 448.72          | 0.28                       |                          |   1.29                     |
      | 182       | 02 July 2026     | 50.00                 |                     | 398.97          | 0.26                       |                          |   1.03                     |
      | 183       | 03 July 2026     | 50.00                 |                     | 349.20          | 0.23                       |                          |   0.8                      |
      | 184       | 04 July 2026     | 50.00                 |                     | 299.40          | 0.20                       |                          |   0.6                      |
      | 185       | 05 July 2026     | 50.00                 |                     | 249.57          | 0.17                       |                          |   0.43                     |
      | 186       | 06 July 2026     | 50.00                 |                     | 199.71          | 0.14                       |                          |   0.29                     |
      | 187       | 07 July 2026     | 50.00                 |                     | 149.83          | 0.11                       |                          |   0.18                     |
      | 188       | 08 July 2026     | 50.00                 |                     | 99.91           | 0.09                       |                          |   0.09                     |
      | 189       | 09 July 2026     | 50.00                 |                     | 49.97           | 0.06                       |                          |   0.03                     |
      | 190       | 10 July 2026     | 50.00                 |                     | 0.00            | 0.03                       |                          |   0.0                      |
    And Working Capital Loan has transactions:
      | transactionDate | type                                 | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement                         | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee                         | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment                            | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Amortization            | 9.61              |                  |                   |                       | false    |
      | 03 January 2026 | Repayment                            | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 03 January 2026 | Discount Fee Amortization            | 9.57              |                  |                   |                       | false    |
      | 04 January 2026 | Repayment                            | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 04 January 2026 | Discount Fee Amortization            | 9.52              |                  |                   |                       | false    |
      | 05 January 2026 | Discount Fee Adjustment              | 500.0             | 500.0            | 0.0               | 0.0                   | false    |
      | 05 January 2026 | Discount Fee Amortization Adjustment | 13.35             |                  |                   |                       | false    |
    Then Working Capital Loan Transactions tab has a "DISCOUNT_FEE_AMORTIZATION_ADJUSTMENT" transaction with date "05 January 2026" which has the following Journal entries:
      | Type      | Account code | Account name                 | Debit  | Credit |
      | INCOME    | 404000       | Interest Income              | 13.35  |        |
      | LIABILITY | 240005       | Deferred Interest Revenue    |        | 13.35  |
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9500.0    | 150.0              | 15.35          | 484.65           | 0.0               |

  @TestRailId:C83079
  Scenario: Discount fee adjustment after COB amortization does NOT create discount fee amortization adjustment on next COB if added on the same as discount fee date - UC4
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountApproved |
      | WCLP_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Approved | 0.0       | 9000.0            | 100000.0           | 18.0              | null             |
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Verify Working Capital loan disbursement was successful
    And Admin adds Discount fee with "1000" amount on Working Capital loan account for last disbursement
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Active | 10000.0   | 9000.0            | 100000.0           | 18.0              | 1000.0   |
    When Admin sets the business date to "02 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "02 January 2026" with 150.0 transaction amount on Working Capital loan
    And Admin adds Discount fee adjustment with "300" amount on transaction date "02 January 2026" on Working Capital loan account for last discount
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9700.0    | 150.0              | 0.0            | 700.0            | 0.0               |
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment                 | 150.0             | 150.0            | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Adjustment   | 300.0             | 300.0            | 0.0               | 0.0                   | false    |
    When Admin sets the business date to "03 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment                 | 150.0             | 150.0            | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Adjustment   | 300.0             | 300.0            | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Amortization | 20.91             |                  |                   |                       | false    |

  @TestRailId:C83080
  Scenario: Discount fee adjustment after COB amortization creates discount fee amortization adjustment on next COB after exceed expected amount repayment - UC5
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountApproved |
      | WCLP_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Approved | 0.0       | 9000.0            | 100000.0           | 18.0              | null             |
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Verify Working Capital loan disbursement was successful
    And Admin adds Discount fee with "1000" amount on Working Capital loan account for last disbursement
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Active | 10000.0   | 9000.0            | 100000.0           | 18.0              | 1000.0   |
    When Admin sets the business date to "02 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "02 January 2026" with 150.0 transaction amount on Working Capital loan
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 10000.0   | 150.0              | 0.0            | 1000.0           | 0.0               |
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment                 | 150.0             | 150.0            | 0.0               | 0.0                   | false    |
    When Admin sets the business date to "03 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 10000.0   | 150.0              | 28.7           | 971.3           | 0.0               |
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment                 | 150.0             | 150.0            | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Amortization | 28.7              |                  |                   |                       | false    |
    And Admin adds Discount fee adjustment with "400" amount on transaction date "03 January 2026" on Working Capital loan account for last discount
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9600.0    | 150.0              | 28.7           | 571.3          | 0.0               |
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment                 | 150.0             | 150.0            | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Amortization | 28.7              |                  |                   |                       | false    |
      | 03 January 2026 | Discount Fee Adjustment   | 400.0             | 400.0            | 0.0               | 0.0                   | false    |
    When Admin sets the business date to "04 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Working Capital Loan has transactions:
      | transactionDate | type                                 | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement                         | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee                         | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment                            | 150.0             | 150.0            | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Amortization            | 28.7              |                  |                   |                       | false    |
      | 03 January 2026 | Discount Fee Adjustment              | 400.0             | 400.0            | 0.0               | 0.0                   | false    |
      | 03 January 2026 | Discount Fee Amortization Adjustment | 10.52             |                  |                   |                       | false    |

  @TestRailId:C83081
  Scenario: Discount fee adjustment after COB amortization creates discount fee amortization adjustment on next COB after repayment with amount less then expected - UC6
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountApproved |
      | WCLP_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Approved | 0.0       | 9000.0            | 100000.0           | 18.0              | null             |
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Verify Working Capital loan disbursement was successful
    And Admin adds Discount fee with "1000" amount on Working Capital loan account for last disbursement
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Active | 10000.0   | 9000.0            | 100000.0           | 18.0              | 1000.0     |
    When Admin sets the business date to "02 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "02 January 2026" with 30.0 transaction amount on Working Capital loan
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 10000.0   | 30.0               | 0.0            | 1000.0         | 0.0               |
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment                 | 30.0              | 30.0             | 0.0               | 0.0                   | false    |
    When Admin sets the business date to "03 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 10000.0   | 30.0               | 5.77           | 994.23          | 0.0               |
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment                 | 30.0              | 30.0             | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Amortization | 5.77              |                  |                   |                       | false    |
    When Admin sets the business date to "04 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds Discount fee adjustment with "600" amount on transaction date "04 January 2026" on Working Capital loan account for last discount
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9400.0    | 30.0               | 5.77           | 394.23           | 0.0               |
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment                 | 30.0              | 30.0             | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Amortization | 5.77              |                  |                   |                       | false    |
      | 04 January 2026 | Discount Fee Adjustment   | 600.0             | 600.0            | 0.0               | 0.0                   | false    |
    When Admin sets the business date to "05 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Working Capital Loan has transactions:
      | transactionDate | type                                 | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement                         | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee                         | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment                            | 30.0              | 30.0             | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Amortization            | 5.77              |                  |                   |                       | false    |
      | 04 January 2026 | Discount Fee Adjustment              | 600.0             | 600.0            | 0.0               | 0.0                   | false    |
      | 04 January 2026 | Discount Fee Amortization Adjustment | 3.27              |                  |                   |                       | false    |

  @TestRailId:C83082
  Scenario: Discount fee adjustment after COB amortization creates discount fee amortization adjustment on next COB for each discount adjustment transaction - UC7
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountApproved |
      | WCLP_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Approved | 0.0       | 9000.0            | 100000.0           | 18.0              | null             |
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Verify Working Capital loan disbursement was successful
    And Admin adds Discount fee with "1000" amount on Working Capital loan account for last disbursement
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Active | 10000.0   | 9000.0            | 100000.0           | 18.0              | 1000.0     |
    When Admin sets the business date to "02 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "02 January 2026" with 50.0 transaction amount on Working Capital loan
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 10000.0   | 50.0               | 0.0            | 1000.0         | 0.0               |
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
    When Admin sets the business date to "03 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 10000.0   | 50.0               | 9.61           | 990.39           | 0.0               |
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Amortization | 9.61              |                  |                   |                       | false    |
    And Admin adds Discount fee adjustment with "200" amount on transaction date "03 January 2026" on Working Capital loan account for last discount
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9800.0    | 50.0               | 9.61           | 790.39           | 0.0               |
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Amortization | 9.61              |                  |                   |                       | false    |
      | 03 January 2026 | Discount Fee Adjustment   | 200.0             | 200.0            | 0.0               | 0.0                   | false    |
    When Admin sets the business date to "04 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Admin adds Discount fee adjustment with "300" amount on transaction date "04 January 2026" on Working Capital loan account for last discount
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9500.0    | 50.0               | 7.9            | 492.1            | 0.0               |
    And Working Capital Loan has transactions:
      | transactionDate | type                                 | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement                         | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee                         | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment                            | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Amortization            | 9.61              |                  |                   |                       | false    |
      | 03 January 2026 | Discount Fee Adjustment              | 200.0             | 200.0            | 0.0               | 0.0                   | false    |
      | 03 January 2026 | Discount Fee Amortization Adjustment | 1.71              |                  |                   |                       | false    |
      | 04 January 2026 | Discount Fee Adjustment              | 300.0             | 300.0            | 0.0               | 0.0                   | false    |
    When Admin sets the business date to "05 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Working Capital Loan has transactions:
      | transactionDate | type                                  | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement                          | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee                          | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment                             | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Amortization             | 9.61              |                  |                   |                       | false    |
      | 03 January 2026 | Discount Fee Adjustment               | 200.0             | 200.0            | 0.0               | 0.0                   | false    |
      | 03 January 2026 | Discount Fee Amortization Adjustment  | 1.71              |                  |                   |                       | false    |
      | 04 January 2026 | Discount Fee Adjustment               | 300.0             | 300.0            | 0.0               | 0.0                   | false    |
      | 04 January 2026 | Discount Fee Amortization Adjustment  | 2.76              |                  |                   |                       | false    |

  @TestRailId:C83083
  Scenario: Verify Discount fee adjustment with amount that equals to discount fee creates discount fee amortization adjustment equals to discount fee amortization - UC8
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountApproved |
      | WCLP_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Approved | 0.0       | 9000.0            | 100000.0           | 18.0              | null             |
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Verify Working Capital loan disbursement was successful
    And Admin adds Discount fee with "1000" amount on Working Capital loan account for last disbursement
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Active | 10000.0   | 9000.0            | 100000.0           | 18.0              | 1000.0     |
    When Admin sets the business date to "02 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "02 January 2026" with 50.0 transaction amount on Working Capital loan
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 10000.0   | 50.0               | 0.0            | 1000.0           | 0.0               |
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
    When Admin sets the business date to "03 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 10000.0   | 50.0               | 9.61           | 990.39           | 0.0               |
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Amortization | 9.61              |                  |                   |                       | false    |
    And Admin adds Discount fee adjustment with "1000" amount on transaction date "03 January 2026" on Working Capital loan account for last discount
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9000.0    | 50.0               | 9.61           | 0.0              | 0.0               |
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment                 | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Amortization | 9.61              |                  |                   |                       | false    |
      | 03 January 2026 | Discount Fee Adjustment   | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
    When Admin sets the business date to "04 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Working Capital Loan has transactions:
      | transactionDate | type                                  | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement                          | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee                          | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment                             | 50.0              | 50.0             | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Amortization             | 9.61              |                  |                   |                       | false    |
      | 03 January 2026 | Discount Fee Adjustment               | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 03 January 2026 | Discount Fee Amortization Adjustment  | 9.61              |                  |                   |                       | false    |
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9000.0    | 50.0               | 0.0            | 0.0              | 0.0               |

  @TestRailId:C83084
  Scenario: Verify discount fee amortization adjustment transaction on working capital after discount fee adjustment overpays loan - UC9
    When Admin sets the business date to "01 January 2026"
    And Admin creates a client with random data
    And Admin creates a working capital loan with the following data:
      | LoanProduct              | submittedOnDate | expectedDisbursementDate | principalAmount | totalPayment | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 01 January 2026 | 01 January 2026          | 9000            | 100000       | 18                | 0        |
    And Admin successfully approves the working capital loan on "01 January 2026" with "9000" amount and expected disbursement date on "01 January 2026"
    Then Working capital loan approval was successful
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status   | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discountApproved |
      | WCLP_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Approved | 0.0       | 9000.0            | 100000.0           | 18.0              | null             |
    When Admin successfully disburse the Working Capital loan on "01 January 2026" with "9000" EUR transaction amount
    Then Working Capital loan status will be "ACTIVE"
    And Verify Working Capital loan disbursement was successful
    And Admin adds Discount fee with "1000" amount on Working Capital loan account for last disbursement
    And Working capital loan account has the correct data:
      | product.name             | submittedOnDate | expectedDisbursementDate | status | principal | approvedPrincipal | totalPaymentVolume | periodPaymentRate | discount |
      | WCLP_ADVANCED_ACCOUNTING | 2026-01-01      | 2026-01-01               | Active | 10000.0   | 9000.0            | 100000.0           | 18.0              | 1000.0     |
    When Admin sets the business date to "02 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Customer makes repayment on "02 January 2026" with 9900.0 transaction amount on Working Capital loan
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 10000.0   | 9900.0             | 0.0            | 1000.0           | 0.0               |
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment                 | 9900.0            | 9900.0           | 0.0               | 0.0                   | false    |
    When Admin sets the business date to "03 January 2026"
    When Admin runs inline COB job for Working Capital Loan
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 10000.0   | 9900.0             | 999.83         | 0.17             | 0.0               |
    And Working Capital Loan has transactions:
      | transactionDate | type                      | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement              | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee              | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment                 | 9900.0            | 9900.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Amortization | 999.83            |                  |                   |                       | false    |
    And Admin adds Discount fee adjustment with "500" amount on transaction date "03 January 2026" on Working Capital loan account for last discount
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9500.0    | 9500.0             | 500.0          | 0.0              | 400.0             |
    And Working Capital Loan has transactions:
      | transactionDate | type                                 | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement                         | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee                         | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment                            | 9900.0            | 9900.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Amortization            | 999.83            |                  |                   |                       | false    |
      | 03 January 2026 | Discount Fee Adjustment              | 500.0             | 500.0            | 0.0               | 0.0                   | false    |
      | 03 January 2026 | Discount Fee Amortization Adjustment | 499.83            |                  |                   |                       | false    |
    When Admin sets the business date to "04 January 2026"
    And Admin runs inline COB job for Working Capital Loan by loanId
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9500.0    | 9500.0             | 500.0          | 0.0              | 400.0             |
    And Working Capital Loan has transactions:
      | transactionDate | type                                 | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement                         | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee                         | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment                            | 9900.0            | 9900.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Amortization            | 999.83            |                  |                   |                       | false    |
      | 03 January 2026 | Discount Fee Adjustment              | 500.0             | 500.0            | 0.0               | 0.0                   | false    |
      | 03 January 2026 | Discount Fee Amortization Adjustment | 499.83            |                  |                   |                       | false    |
    Then Working Capital loan status will be "OVERPAID"
    And Customer makes credit balance refund on "04 January 2026" with 400.0 transaction amount on Working Capital loan
    Then Working Capital loan status will be "CLOSED_OBLIGATIONS_MET"
    And Working capital loan account has the correct data:
      | principal | totalPaidPrincipal | realizedIncome | unrealizedIncome | overpaymentAmount |
      | 9500.0    | 9500.0             | 500.0          | 0.0              | 0.0               |
    And Working Capital Loan has transactions:
      | transactionDate | type                                 | transactionAmount | principalPortion | feeChargesPortion | penaltyChargesPortion | reversed |
      | 01 January 2026 | Disbursement                         | 9000.0            | 9000.0           | 0.0               | 0.0                   | false    |
      | 01 January 2026 | Discount Fee                         | 1000.0            | 1000.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Repayment                            | 9900.0            | 9900.0           | 0.0               | 0.0                   | false    |
      | 02 January 2026 | Discount Fee Amortization            | 999.83            |                  |                   |                       | false    |
      | 03 January 2026 | Discount Fee Adjustment              | 500.0             | 500.0            | 0.0               | 0.0                   | false    |
      | 03 January 2026 | Discount Fee Amortization Adjustment | 499.83            |                  |                   |                       | false    |
      | 04 January 2026 | Credit Balance Refund                | 400.0             | 0.0              | 0.0               | 0.0                   | false    |

# Fineract Progressive Loan Embeddable Schedule Generator

## Build

- Generate Embeddable Progressive Schedule Generator Jar

    ```shell
        ./gradlew :fineract-progressive-loan-embeddable-schedule-generator:shadowJar
    ```

- Copy Jar from `fineract-progressive-loan-embeddable-schedule-generator/build/libs/fineract-progressive-loan-embeddable-schedule-generator-*-SNAPSHOT-all.jar` to Your class path.

## Dependencies

There is no extra dependency.


## Sample Application

Copy a [Main.java](misc/Main.java) file from the `misc` directory into the working directory:

The project directory structure:

  - `Main.java`
  - `fineract-embeddable-calculator-1.11.0-SNAPSHOT-all.jar`


- Check Java minimum version

  ```shell
  java -version # openjdk version "17.0.3" 2022-04-19 LTS
  ```

- Compile the source

  ```shell
  javac -cp fineract-progressive-loan-embeddable-schedule-generator-1.11.0-SNAPSHOT-all.jar Main.java
  ```

- Run with dependencies:

  ```shell
  java -cp fineract-progressive-loan-embeddable-schedule-generator-1.11.0-SNAPSHOT-all.jar:. Main
  ```

This code has the following output:

```
#------ Loan Schedule -----------------#
  Number of Periods: 6
  Loan Term in Days: 182
  Total Disbursed Amount: 100.00
  Total Interest Amount: 2.05
  Total Repayment Amount: 102.05
#------ Repayment Schedule ------------#
  Disbursement - Date: 2024-01-01, Amount: 100.00
  Repayment Period: #1, Due Date: 2024-02-01, Balance: 83.57, Principal: 16.43, Interest: 0.58, Total: 17.01
  Repayment Period: #2, Due Date: 2024-03-01, Balance: 67.05, Principal: 16.52, Interest: 0.49, Total: 17.01
  Repayment Period: #3, Due Date: 2024-04-01, Balance: 50.43, Principal: 16.62, Interest: 0.39, Total: 17.01
  Repayment Period: #4, Due Date: 2024-05-01, Balance: 33.71, Principal: 16.72, Interest: 0.29, Total: 17.01
  Repayment Period: #5, Due Date: 2024-06-01, Balance: 16.90, Principal: 16.81, Interest: 0.20, Total: 17.01
  Repayment Period: #6, Due Date: 2024-07-01, Balance: 0.00, Principal: 16.90, Interest: 0.10, Total: 17.00
```

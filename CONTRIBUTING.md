# Contributing

Hello there!

First of all, **Thank You** for contributing to Apache Fineract! We are grateful for your interest in this project.

Please join the [developer mailing list](https://fineract.apache.org/#contribute), if you have not already done so, as this is where discussions about this project take place - say _Hi_ there! Please also have a quick look at the [Code of Conduct](CODE_OF_CONDUCT.md).

The [JIRA Dashboard](https://issues.apache.org/jira/secure/Dashboard.jspa?selectPageId=12335824) shows what's going on. Create a login - it can take a day or two. If you face difficulties, ask on the mailing list.

You don't need to be a committer to provide pull requests, but [Becoming a Committer](https://cwiki.apache.org/confluence/display/FINERACT/Becoming+a+Committer) explains the process of becoming one - just in case...


## Developer How To's

### How to run tests

#### Unit tests

Here's how to run the set of relatively fast and independent Fineract tests:

```bash
./gradlew test -x :twofactor-tests:test -x :oauth2-tests:test -x :integration-tests:test
```

This runs nearly 1,000 tests and completes in a few minutes on decent hardware.
They shouldn't need any special servers/services running.

#### Integration tests

Running tests with external dependencies is a multi-step process with many moving parts.
Sometimes there are arbitrary failures and the prerequisite setup can be daunting.
A full local integration test run (on a developer workstation) covering every possible test using every external service and every supported relational database engine could take an entire day - and that's assuming everything is properly configured and runs as expected.

Right now we depend on GitHub to know if "the build" is passing (it's actually multiple builds).
The authoritative source of truth for what commands/services/tests to run, how, and when are the files in `.github/workflows/`.
Output from runs based on those configuration files appears at <https://github.com/apache/fineract/actions>.

Incorrect default Java-related executables may cause test failures.
To fix this on Debian and Ubuntu systems, run the following:

```bash
export JAVA_HOME=/usr/lib/jvm/zulu21
sudo update-alternatives --set java $JAVA_HOME/bin/java
sudo update-alternatives --set javac $JAVA_HOME/bin/javac
sudo update-alternatives --set javadoc $JAVA_HOME/bin/javadoc
```

This would correct, for example, a [class file version error](https://en.wikipedia.org/wiki/Java_class_file#General_layout).
You might see something like this if a Java 11 executable (class file format version 56) was the system default, but the integration tests were using Java 21 (class file format version 65):

> UnsupportedClassVersionError: com.example.package/ClassName has been compiled by a more recent version of the Java Runtime (class file version 65.0), this version of the Java Runtime only recognizes class file versions up to 55.0

The GitHub builds are run in [short-lived virtual machines](https://docs.github.com/en/actions/using-github-hosted-runners/using-github-hosted-runners), so locally reproducing the same may require additional effort, such as these extra clean-up procedures:

```bash
# Might fix `error: cannot find symbol` or other intermittent failures.
# `doc` here is a placeholder for any task(s) you are trying to run.
# 💚 This is generally very safe to run between builds.
./gradlew --refresh-dependencies doc

# Destroy anything untracked by git.
# ⚠️ This may delete something important, e.g. a finely-tuned IDE configuration.
git clean --force -dx

# Destroy various caches and configs.
# ⚠️ This may delete gibibytes of cached data, making the next build very slow.
rm -rf ~/.gradle ~/.m2 /tmp/cargo*

# Destroy any Java containers left running.
# 💚 This is generally very safe to run between builds.
ps auxwww | grep [c]argo | awk '{ print $2 }' | xargs -r kill
```

Integration test runs such as
```bash
./gradlew --no-daemon --console=plain test -x :twofactor-tests:test \
  -x :oauth2-tests:test :fineract-e2e-tests-runner:test -PdbType=postgresql
```
in `.github/workflows/build-postgresql.yml` often take an hour or longer to complete.
If you notice the `:integration-tests:test` task taking significantly less time, say, one minute, gradle may be skipping it.
Look for something like this in the test output:

> Task :integration-tests:test UP-TO-DATE 👀
Custom actions are attached to task ':integration-tests:test'.
Build cache key for task ':integration-tests:test' is 6aeeec3f58bf9703d4c100fbaa657f5c
Skipping task ':integration-tests:test' as it is up-to-date.
Resolve mutations for :integration-tests:cargoStopLocal (Thread[Execution worker Thread 11,5,main]) started.
:integration-tests:cargoStopLocal (Thread[Execution worker Thread 11,5,main]) started.


(This is with the `--info` gradle argument with eyeballs added for emphasis.)
The `--rerun-tasks` gradle argument may help, or you can try destroying `~/.gradle` and other clean-up procedures as indicated above, then re-running tests.
This is useful for repeated test runs (say, for timing) when gradle would otherwise assume a task is "up-to-date" and not re-run it.

See the next section for testing in Eclipse and [here](https://fineract-academy.com) for testing in IntelliJ.

### How to run and debug in Eclipse IDE

It is possible to run Fineract in Eclipse IDE and also to debug Fineract using Eclipse's debugging facilities.
To do this, you need to create the Eclipse project files and import the project into an Eclipse workspace:

1. Create Eclipse project files into the Fineract project by running `./gradlew cleanEclipse eclipse`
2. Import the fineract-provider project into your Eclipse workspace (File->Import->General->Existing Projects into Workspace, choose root directory fineract/fineract-provider)
3. Do a clean build of the project in Eclipse (Project->Clean...)
3. Run / debug Fineract by right clicking on org.apache.fineract.ServerApplication class and choosing Run As / Debug As -> Java Application. All normal Eclipse debugging features (breakpoints, watchpoints etc) should work as expected.

If you change the project settings (dependencies etc) in Gradle, you should redo step 1 and refresh the project in Eclipse.

You can also use Eclipse JUnit support to run tests in Eclipse (Run As->JUnit Test)

Finally, modifying source code in Eclipse automatically triggers hot code replace to a running instance, allowing you to immediately test your changes

How to download Gradle wrapper
---
The file gradle/wrapper/gradle-wrapper.jar binary is checked into this projects Git source repository,
but won't exist in your copy of the Fineract codebase if you downloaded a released source archive from apache.org.
In that case, you need to download it using the commands below:
```bash
wget -P gradle/wrapper https://github.com/apache/fineract/raw/develop/gradle/wrapper/gradle-wrapper.jar
```
or
```bash
curl -L https://github.com/apache/fineract/raw/develop/gradle/wrapper/gradle-wrapper.jar > \
    gradle/wrapper/gradle-wrapper.jar
```

### How to run Apache RAT (Release Audit Tool)

1. Extract the archive file to your local directory.
2. Run `./gradlew rat`. A report will be generated under build/reports/rat/rat-report.txt


### How to build documentation

Run the following command:

```bash
./gradlew doc
```

Some dependencies are required (e.g. Ghostscript, Graphviz), see [.github/workflows/build-documentation.yml](https://github.com/apache/fineract/tree/develop/.github/workflows/build-documentation.yml) for hints.

IDEs such as IntelliJ are useful for editing the AsciiDoc source files while providing a live rendered preview.

HTML rendered from the AsciiDoc source files is also available online at [https://fineract.apache.org/docs/current/](https://fineract.apache.org/docs/current/).


## How We Code

### Checkstyle and Spotless

This project enforces its code conventions using [checkstyle.xml](config/checkstyle/checkstyle.xml) through Checkstyle and [fineractdev-formatter.xml](config/fineractdev-formatter.xml) through Spotless. They are configured to run automatically during the normal Gradle build, and fail if there are any violations detected. You can run the following command to automatically fix spotless violations:
```bash
./gradlew spotlessApply
```
Since some checks are present in both Checkstyle and Spotless, the same command can help you fix some of the Checkstyle violations too.

You can also check solely for Spotless violations, but normally don't have to, because regular builds already include this:
```bash
./gradlew spotlessCheck
```

We recommend that you configure your favourite Java IDE to match those conventions. For Eclipse, you can go to
Window > Java > Code Style and import the aforementioned [config/fineractdev-formatter.xml](config/fineractdev-formatter.xml) under formatter section and [config/fineractdev-cleanup.xml](config/fineractdev-cleanup.xml) under cleanup section.

You could also use Checkstyle directly in your IDE, but you don't have to: it may just be more convenient for you.  For Eclipse, use https://checkstyle.org/eclipse-cs/ and load our checkstyle.xml into it. For IntelliJ you can use [CheckStyle-IDEA](https://plugins.jetbrains.com/plugin/1065-checkstyle-idea).


### Code Coverage

Changed or added code should ideally have test coverage.

The project uses Jacoco to measure unit tests code coverage. To generate a report run the following command:
```bash
./gradlew clean build jacocoTestReport
```
Generated reports can be found in the build/code-coverage directory.


### Error Handling

* When catching exceptions, either rethrow them, or log them.  Either way, include the root cause by using `catch (SomeException e)` and then either `throw AnotherException("..details..", e)` or `LOG.error("...context...", e)`.
* Completely empty catch blocks are VERY suspicious.  Are you sure that you want to just "swallow" an exception?  Really, 100% totally absolutely sure?? ;-) Such "normal exceptions which just happen sometimes but are actually not really errors" are almost always a bad idea, can be a performance issue, and typically are an indication of another problem - e.g. the use of a wrong API which throws an Exception for an expected condition, when really you would want to use another API that instead returns something empty or optional.
* In tests, you'll typically never catch exceptions, but just propagate them, with `@Test void testXYZ() throws SomeException, AnotherException`..., so that the test fails if the exception happens.  Unless you actually really want to test for the occurrence of a problem - in that case, use [JUnit's Assert.assertThrows()](https://github.com/junit-team/junit4/wiki/Exception-testing) (but not `@Test(expected = SomeException.class)`).
* Never catch `NullPointerException` & Co.

### Logging

* We use [SLF4J](http://www.slf4j.org) as our logging API.
* Never, ever, use `System.out` and `System.err` or `printStackTrace()` anywhere, but always `LOG.info()` or `LOG.error()` instead.
* Use placeholder (`LOG.error("Could not... details: {}", something, exception)`) and never String concatenation (`LOG.error("Could not... details: " + something, exception)`)
* Which Log Level is appropriate?
    * `LOG.error()` should be used to inform an "operator" running Fineract who supervises error logs of an unexpected condition.  This includes technical problems with an external "environment" (e.g. can't reach a database), and situations which are likely bugs which need to be fixed in the code.  They do NOT include e.g. validation errors for incoming API requests - that is signaled through the API response - and does (should) not be logged as an error.  (Note that there is no _FATAL_ level in SLF4J; a "FATAL" event should just be logged as an _ERROR_.)
    * `LOG.warn()` should be using sparingly.  Make up your mind if it's an error (above) - or not!
    * `LOG.info()` can be used notably for one-time actions taken during start-up.  It should typically NOT be used to print out "regular" application usage information.  The default logging configuration always outputs the application INFO logs, and in production under load, there's really no point to constantly spew out lots of information from frequently traversed paths in the code about what's going on.  (Metrics are a better way.)  `LOG.info()` *can* be used freely in tests though.
    * `LOG.debug()` can be used anywhere in the code to log things that may be useful during investigations of specific problems.  They are not shown in the default logging configuration, but can be enabled for troubleshooting.  Developers should typically "turn down" most `LOG.info()` which they used while writing a new feature to "follow along what happens during local testing" to `LOG.debug()` for production before we merge their PRs.
    * `LOG.trace()` is not used in Fineract.

## Change Process

### Dependency Upgrades

This project uses a number of 3rd-party libraries. We have set up [Renovate's bot](https://github.com/renovatebot/github-action) to automatically raise Pull Requests for our review when new dependencies are available.

Our `ClasspathHellDuplicatesCheckRuleTest` detects classes that appear in more than 1 JAR.  If a version bump in [build.gradle](https://github.com/apache/fineract/blob/develop/build.gradle) causes changes in transitives dependencies, then you may have to add related `exclude` to our [dependencies.gradle](https://github.com/apache/fineract/search?q=dependencies.gradle).  Running `./gradlew dependencies` helps to understand what is required.

### Pull Requests

We request that your commit message includes a FINERACT JIRA issue and a one-liner that describes the changes.
Start with an upper case imperative verb (not past form), and a short but concise clear description. (E.g. "FINERACT-821: Add enforced HideUtilityClassConstructor checkstyle").

If your PR is failing to pass our CI build due to a test failure, then:

1. Understand if the failure is due to your PR or an unrelated unstable test.
1. If you suspect it is because of a "flaky" test, and not due to a change in your PR, then please do not simply wait for an active maintainer to come and help you, but instead be a proactive contributor to the project - see next steps.  Do understand that we may not review PRs that are not green - it is the contributor's (that's you!) responsibility to get a proposed PR to pass the build, not primarily the maintainers.
1. Search for the name of the failed test on https://issues.apache.org/jira/, e.g. for `AccountingScenarioIntegrationTest` you would find [FINERACT-899](https://issues.apache.org/jira/browse/FINERACT-899).
1. If you happen to read in such bug reports that tests were just recently fixed, or ignored, then rebase your PR to pick up that change.
1. If you find previous comments "proving" that the same test has arbitrarily failed in at least 3 past PRs, then please do yourself raise a small separate new PR proposing to add an `@Disabled // TODO FINERACT-123` to the respective unstable test (e.g. [#774](https://github.com/apache/fineract/pull/774)) with the commit message mentioning said JIRA, as always.  (Please do NOT just `@Disabled` any existing tests mixed in as part of your larger PR.)
1. If there is no existing JIRA for the test, then first please evaluate whether the failure couldn't be a (perhaps strange) impact of the change you are proposing after all.  If it's not, then please raise a new JIRA to document the suspected Flaky Test, and link it to [FINERACT-850](https://issues.apache.org/jira/browse/FINERACT-850).  This will allow the next person coming along hitting the same test failure to easily find it, and eventually propose to ignore the unstable test.
1. Then (only) Close and Reopen your PR, which will cause a new build, to see if it passes.
1. Of course, we very much appreciate you then jumping onto any such bugs and helping us figure out how to fix all ignored tests!

[Pull Request Size Limit](https://cwiki.apache.org/confluence/display/FINERACT/Pull+Request+Size+Limit)
documents that we cannot accept huge "code dump" Pull Requests, with some related suggestions.

Guideline for new Feature commits involving Refactoring: If you are submitting a PR for a new feature,
and it involves refactoring, try to differentiate "new feature code" from "refactored" by placing
them in different commits. This helps to review your code faster.

We have an automated bot which marks pull requests as "stale" after a while, and ultimately automatically closes them.


### Merge Strategy

This project's committers typically prefer to bring your pull requests in through _Rebase and Merge_ instead of _Create a Merge Commit_. (If you are unfamiliar with GitHub's UI regarding this, note the somewhat hidden little triangle drop-down at the bottom of the PR, visible only to committers, not contributors.)  This avoids the "merge commits" which we consider to be somewhat "polluting" the project's commit log history view.  We understand this doesn't give an easy automatic reference to the original PR (which GitHub automatically adds to the merge commit message it generates), but we consider this an only very minor inconvenience; it's typically relatively easy to find the original PR even just from the commit message, and JIRA.

We expect most proposed PRs to typically consist of a single commit. Committers may use _Squash and merge_ to combine your commits at merge time, and if they do so, will rewrite your commit message as they see fit.

Neither of these two are hard absolute rules, but mere conventions. Multiple commits in single PRs make sense in certain cases (e.g. branch backports).

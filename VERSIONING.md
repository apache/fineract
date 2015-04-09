Versioning
=====
For releases, we follow a simplified date versioning scheme which ties in nicely with our time based release cycle (with stables releases being promoted on a three month cycle)

Prior to 15.03.RELEASE, we followed a semantic versioning approach. (http://semver.org/)

Date Versioning
===================

Our date versioning scheme is built around the YY.MM.{PATCH}.{release_type} concept (for example 15.06.RELEASE or 15.06.1.BUILD-SNAPSHOT)  where

    YY represents the year of the release
    MM represents the month of the major release 
    PATCH (optional) gets incremented whenever a new release is made from an existing major release 
    release_type differentiates different builds off the same branch. It also differentiates between development, test and production builds.
    
Version structure

{number}.{release_type}

where {number} is further broken down as YY.MM.{PATCH}.

Examples:

    15.03.BUILD-SNAPSHOT
    15.03.M1
    15.03.RC1
    15.03.RELEASE
    15.03.1.RC1
    15.03.1.RELEASE

Number semantics

In general:

    a major version (Ex:15.03) does not have a patch number and suggests many significant new features, an associated marketing campaign, and possible breaking of backward compatibility. You can expect major releases every 3 months
    a minor version (Ex:15.03.1)  may also contain serious new features, but typically contains many bug fixes and improvements and does not break compatibility. You can expect minor releases between the quarterly major release cycle.

    
What You Should Install Where

As a rule of thumb …

    Snapshot or Milestone versions should only be installed on dev boxes and integration environments. They shouldn’t be deployed to any of the dedicated test environments.
    Release candidates should be installed in the dedicated test environments. In an emergency, a release candidate can be installed into the production environment.
    Production releases can be installed anywhere - and they are the only kind of build that should be installed into the production environment.

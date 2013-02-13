Versioning
=====

For releases we will follow semantic versioning approach. (http://semver.org/)

Semantic Versioning
===================

Semantic versioning is a very simple scheme built around the X.Y.Z-buildNo concept (for example 2.6.0-x or 2.6.0-BUILD-SNAPSHOT):

    Increment Z when you fix something
    Increment Y when you add a new feature
    Increment X when you break backwards-compatibility or add major features
    Use the buildNo to differentiate different builds off the same branch, and to differentiate between development, test and production builds.
    
Version structure

{number}.{release_type}

where {number} is further broken down as {major}.{minor}.{micro}.

Examples:

    1.0.0.BUILD-SNAPSHOT
    1.0.0.M1
    1.0.0.RC1
    1.0.0.RELEASE

Number semantics

major/minor/micro semantics may have some variance across projects but in general this is what we mean.

In general:

    a major version suggests many significant new features, an associated marketing campaign, and possible breaking of backward compatibility.
    a minor version may also contain serious new features, but should not break compatibility. You can expect minor releases every 1-3 months.
    a micro version typically contains many bug fixes and improvements and perhaps several smaller new features. You can expect micro version approximately every 5-20 days (depending on bugs or suggested improvements).

Order matters

Notice that the following sequence of versions is in ordered ascending both alphabetically and toward GA:

    1.0.0.BUILD-SNAPSHOT
    1.0.0.M1
    1.0.0.RC1
    1.0.0.RELEASE
    
What You Should Install Where

As a rule of thumb …

    Snapshot or Milestone versions should only be installed on dev boxes and integration environments. They shouldn’t be deployed to any of the dedicated test environments.
    Release candidates should be installed in the dedicated test environments. In an emergency, a release candidate can be installed into the production environment.
    Production releases can be installed anywhere - and they are the only kind of build that should be installed into the production environment.
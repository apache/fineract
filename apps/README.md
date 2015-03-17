Mifos Platform Apps
======

Community created apps built on top of mifosplatform are stored in source control repository @ https://github.com/openMF/

To get the Git sub modules from here, use "--recursive" in your original git clone, or, if already cloned, do:

    git submodule update --init --recursive; git submodule status

Later to update both this main repo but also get the latest from the submodule(s), do:

    git pull; git submodule update --remote

Here is how a new Git submodule can (was originally) added here:

    git submodule add -b develop https://github.com/openMF/community-app.git
    git submodule init


The Community App (AngularJS-based)
===============
https://github.com/openMF/community-app

The Default app shipped which is a core part of every community release.

The original Reference App (Now Deprecated)
===============

https://github.com/openMF/mifosx-community-apps/tree/master/IndividualLendingGeneralJavaScript

The reference app was used to showcase platform API capabilities and is also used in production by MFIs.

[33mcommit 034ba0d394fc1a2b455ee1c08a2fe7e64d9936ed[m
Author: jinjurajan <jinju.rajan@confluxtechnologies.com>
Date:   Fri Apr 29 18:40:26 2016 +0530

    Changing dependency of m_entity_to_entity_access to m_entity_mapping and adding validation for new loan application

[33mcommit 4d8ad87d93d19c52da0398ebcfb2989f094a98f0[m
Merge: 0480e08 a837478
Author: Nazeer Hussain Shaik <nazeer.shaik@confluxtechnologies.com>
Date:   Wed Apr 27 14:15:50 2016 +0530

    Merge branch 'FINERACT-144' into develop

[33mcommit 0480e085468eb6f72ed14f27dbc03c873d730b37[m
Merge: c8159d8 f755564
Author: Nazeer Hussain Shaik <nazeer.shaik@confluxtechnologies.com>
Date:   Tue Apr 26 20:27:07 2016 +0530

    Merge branch 'pullrequest83' into develop

[33mcommit c8159d83f3bdefe263c818bcb0e0b58e3fd5a268[m
Merge: 2ac38ff 7eed1db
Author: Nazeer Hussain Shaik <nazeer.shaik@confluxtechnologies.com>
Date:   Tue Apr 26 20:20:03 2016 +0530

    Merge branch 'pullrequest82' into develop

[33mcommit f755564599cd290f742939fe08114950fb8cf3ee[m
Author: Nazeer Hussain Shaik <nazeer.shaik@confluxtechnologies.com>
Date:   Tue Apr 26 19:54:42 2016 +0530

    removing non-apache licensed imports which are not used

[33mcommit 7eed1dbd2212e19dcf24b12449dc16407703a3ee[m
Author: Nazeer Hussain Shaik <nazeer.shaik@confluxtechnologies.com>
Date:   Tue Apr 26 19:23:24 2016 +0530

    removing javax.transaction.Transactional imports

[33mcommit 2ac38ffc584a6932f11bc807d65a6976e1c12468[m
Author: Nazeer Hussain Shaik <nazeer.shaik@confluxtechnologies.com>
Date:   Tue Apr 26 18:58:07 2016 +0530

    removing third party licensed files import as they are not used

[33mcommit 90d238167c7bb28e6892601cb900a619ba15b0b1[m
Merge: df690ec 6c84834
Author: Nazeer Hussain Shaik <nazeer.shaik@confluxtechnologies.com>
Date:   Tue Apr 26 12:11:11 2016 +0530

    Merge branch 'PULLREQUEST80' into develop

[33mcommit df690ec894eb7f3c2cafe0dd8cfb976481c1c5d8[m
Author: Nazeer Hussain Shaik <nazeer.shaik@confluxtechnologies.com>
Date:   Tue Apr 26 12:06:24 2016 +0530

    Moving actualDisbursementDate outside in LoanWritePlatformServiceJpaRepositoryImpl

[33mcommit 19329c4a12410ceaef028a741dd2cc9d04774c77[m
Merge: c0c6f1d 67be38e
Author: Nazeer Hussain Shaik <nazeer.shaik@confluxtechnologies.com>
Date:   Tue Apr 26 11:53:19 2016 +0530

    Merge branch 'PULLREQUEST79' into develop

[33mcommit c0c6f1d83cb6ea46a457566203b800ac062e6d54[m
Merge: baad7ac aa20ba3
Author: Nazeer Hussain Shaik <nazeer.shaik@confluxtechnologies.com>
Date:   Tue Apr 26 11:51:44 2016 +0530

    Merge branch 'PULLREQUEST78' into develop

[33mcommit baad7ac019f3fe11aaf92efc1653cfd479efded4[m
Merge: 218663a c9d4b63
Author: Nazeer Hussain Shaik <nazeer.shaik@confluxtechnologies.com>
Date:   Tue Apr 26 11:50:18 2016 +0530

    Merge branch 'PULLREQUEST77' into develop

[33mcommit 218663ac319d625f037d883944cf5227fb767f11[m
Merge: ded0c7b 2dae8f8
Author: Nazeer Hussain Shaik <nazeer.shaik@confluxtechnologies.com>
Date:   Tue Apr 26 11:45:49 2016 +0530

    Merge branch 'PullREQUEST73' into develop

[33mcommit ded0c7b8459409fa2e5c930502d0a4ce480d580a[m
Merge: f974671 1cd226d
Author: Nazeer Hussain Shaik <nazeer.shaik@confluxtechnologies.com>
Date:   Tue Apr 26 11:43:02 2016 +0530

    Merge branch 'fixschedulegeneratorforwaivers' into develop

[33mcommit f9746718fb1c2fb19f11ac260b56d92ab512b1cb[m
Author: Nazeer Hussain Shaik <nazeer.shaik@confluxtechnologies.com>
Date:   Tue Apr 26 11:35:46 2016 +0530

    Changing sql script number

[33mcommit 93e615badd5546431217fc1ea21a46f251ad951b[m
Merge: e083d79 d6668d2
Author: Nazeer Hussain Shaik <nazeer.shaik@confluxtechnologies.com>
Date:   Tue Apr 26 11:34:44 2016 +0530

    Merge branch 'PULLREQUEST68' into develop

[33mcommit e083d7904794585c0e555510c0bd1be2d352a480[m
Author: Nazeer Hussain Shaik <nazeer.shaik@confluxtechnologies.com>
Date:   Tue Apr 26 11:12:26 2016 +0530

    changing name of sql script

[33mcommit fdf008d73e4a9eccdf90f2d2b0a0363ef75c92ba[m
Merge: 3970b2e 437de26
Author: Nazeer Hussain Shaik <nazeer.shaik@confluxtechnologies.com>
Date:   Tue Apr 26 11:11:00 2016 +0530

    Merge branch 'FINERACT-94' into develop

[33mcommit 6c84834459c5a86044bef453d75c541e416c2864[m
Author: sachinkulkarni12 <sachin.kulkarni@confluxtechnologies.com>
Date:   Tue Apr 26 10:54:32 2016 +0530

    254:In tranche loans, if repayment date is same as tranche disbursement date then allow to change the emi amount

[33mcommit 67be38e71ab26836f341b62cd80277b2abbedfad[m
Author: sachinkulkarni12 <sachin.kulkarni@confluxtechnologies.com>
Date:   Thu Apr 21 16:47:00 2016 +0530

    136 : accrual exception while doing repayment

[33mcommit aa20ba36a514aebe41f68436fe2ed11f3fa30e06[m
Author: sachinkulkarni12 <sachin.kulkarni@confluxtechnologies.com>
Date:   Mon Apr 11 11:19:26 2016 +0530

    112:Edit disbursement causes the full amount to be shown - in repayment schedule

[33mcommit c9d4b631220560246f817b1fce6b6834177d6a5c[m
Author: sachinkulkarni12 <sachin.kulkarni@confluxtechnologies.com>
Date:   Fri Apr 22 10:35:18 2016 +0530

    152:Repayment schedule not regenerating when frp is changed at the time of disbursal

[33mcommit 3970b2ede1658ab9730b1ffec04a62fc7e0d4b3a[m
Author: Nazeer Hussain Shaik <nazeer.shaik@confluxtechnologies.com>
Date:   Thu Apr 21 18:29:07 2016 +0530

    Issues Found during QA

[33mcommit 60588a78e0c23cf492371e38c6c4d15ced89d628[m
Author: Nazeer Hussain Shaik <nazeer.shaik@confluxtechnologies.com>
Date:   Tue Apr 19 17:55:23 2016 +0530

    Issue fixes on shares and dividends reported by QA

[33mcommit 2dae8f8a0edbe1baee95838babd17247231dd959[m
Author: pramod <pramod@confluxtechnologies.com>
Date:   Mon Apr 18 16:10:35 2016 +0530

    MIFOSX-2505 : included due date charge for interest only recalcualtion installments

[33mcommit 1cd226d4c634db5bbad9bf7ff5e021ff3b0858e4[m
Author: pramod <pramod@confluxtechnologies.com>
Date:   Mon Apr 18 13:23:43 2016 +0530

    MIFOSX-2497 : fixed schedule generator for waivers

[33mcommit fe6e1e88fe503a5c83e4b1f45a1489ea8e0dd1a6[m
Author: Nazeer Hussain Shaik <nazeer.shaik@confluxtechnologies.com>
Date:   Thu Apr 14 13:45:14 2016 +0530

    Edit share account is not working because of merge

[33mcommit 08c553f9fcb47137464d7fb66a644aa8bf3ccdac[m
Author: Nazeer Hussain Shaik <nazeer.shaik@confluxtechnologies.com>
Date:   Wed Apr 13 16:34:38 2016 +0530

    Shares And Dividends Implementation

[33mcommit 9fedac2ba14d23b5b9fd21752912c3574a4061dc[m
Author: pramod <pramod@confluxtechnologies.com>
Date:   Tue Apr 12 14:37:35 2016 +0530

    added api doc

[33mcommit 3015747f3d0ddce9287035ccba6609f497865eaf[m
Author: pramod <pramod@confluxtechnologies.com>
Date:   Mon Apr 11 14:44:59 2016 +0530

    FINERACT-33 : savings withhold tax changes

[33mcommit d6668d2e896061ad05cb1ef603af7f5295d3019e[m
Author: guptas9 <guptas9@mail.uc.edu>
Date:   Mon Apr 11 04:13:44 2016 -0400

    MIFOSX-2405 : modified rest calls to save and fetch status of client identifiers. Added ability to mark Client Identifier as Active or Inactive

[33mcommit abc529056372039aa264ba11dc65411323c9227c[m
Author: guptas9 <guptas9@mail.uc.edu>
Date:   Mon Apr 11 04:12:52 2016 -0400

    added licence headers

[33mcommit a83747822beb691b4f92f4ba6d7af044a25e4bb7[m
Author: sachinkulkarni12 <sachin.kulkarni@confluxtechnologies.com>
Date:   Fri Apr 8 12:00:59 2016 +0530

    changes for fees

[33mcommit 437de26c33a800004af1918c48520713932e7e43[m
Author: binnygopinath <binny.gopinath@gmail.com>
Date:   Thu Apr 7 10:44:02 2016 +0530

    FINERACT-94: Flexible morotorium(only principal portion)

[33mcommit 446ff4403fe4b8913629513278506ee6eccd8166[m
Author: Nazeer Hussain Shaik <nazeer.shaik@confluxtechnologies.com>
Date:   Sat Apr 2 15:57:28 2016 +0530

    migration file change license error

[33mcommit 5178d3a8bfa3d52ca27f6e3e43d361bf9a81a03e[m
Author: Koustav Muhuri <koustav.muhuri@confluxtechnologies.com>
Date:   Thu Mar 24 17:44:01 2016 +0530

    Capturing Time

[33mcommit 8dfec9a3755436b8370d99d9f8a7085821bad9b1[m
Merge: ae5d01a 3c69d35
Author: Adi Narayana Raju <adi.raju@confluxtechnologies.com>
Date:   Mon Mar 28 15:39:55 2016 +0530

    Merge branch '53' into develop

[33mcommit ae5d01a912fad7ff3f13e359584cce48a2e77c74[m
Merge: bba8ca4 b533916
Author: Adi Narayana Raju <adi.raju@confluxtechnologies.com>
Date:   Mon Mar 28 15:39:40 2016 +0530

    Merge branch '51' into develop

[33mcommit 3c69d35da78d30902002c2c1cba5c54438584402[m
Author: sachinkulkarni12 <sachin.kulkarni@confluxtechnologies.com>
Date:   Fri Mar 25 11:01:55 2016 +0530

    FINERACT-98:fix for deleting a tranche

[33mcommit b533916ab68fabab9444505bb5a8b9e0b1d39451[m
Author: jinjurajan <jinju.rajan@confluxtechnologies.com>
Date:   Thu Mar 24 16:14:15 2016 +0530

    Populate Display Name as Full Name when client is a Person

[33mcommit bba8ca47be73d7663f8675a1fb061c434ef96797[m
Author: jinjurajan <jinju.rajan@confluxtechnologies.com>
Date:   Wed Mar 23 14:50:47 2016 +0530

    Skip Repayment Date Falling On First Day of Month

[33mcommit 9c38078de359ef4c2e75e4d71a7eea5e3e364b71[m
Merge: c1af84d 25b66ad
Author: Nazeer Hussain Shaik <nazeer.shaik@confluxtechnologies.com>
Date:   Tue Mar 22 15:47:25 2016 +0530

    Merge branch 'FINERACT86' into develop

[33mcommit 25b66ad1a1865ab4970d9e31fb0d1e70b76dc713[m
Author: sachinkulkarni12 <sachin.kulkarni@confluxtechnologies.com>
Date:   Tue Mar 22 15:29:57 2016 +0530

    center reschedule fix

[33mcommit c1af84dd4b951c8fed3b365ae906c06d9c0de4e7[m
Merge: dd73685 db36721
Author: Nazeer Hussain Shaik <nazeer.shaik@confluxtechnologies.com>
Date:   Tue Mar 22 14:59:41 2016 +0530

    Merge branch 'FINERACT64' into develop

[33mcommit dd736852f821da0841e550c11adb43bb3a53141c[m
Merge: 2ddea0a aad266d
Author: Nazeer Hussain Shaik <nazeer.shaik@confluxtechnologies.com>
Date:   Tue Mar 22 14:13:15 2016 +0530

    Merge branch 'FINERACT90' into develop

[33mcommit db367216a9f5fa54938362d5e2edfbf40ea59660[m
Author: venkatconflux <venkata.conflux@confluxtechnologies.com>
Date:   Mon Mar 21 18:19:05 2016 +0530

    FINERACT-64 issues resolved

[33mcommit 2ddea0aaee8385591343026cef2f276765f72a79[m
Author: Adi Narayana Raju <adi.raju@confluxtechnologies.com>
Date:   Mon Mar 21 17:32:19 2016 +0530

    Self Service - Loan Application

[33mcommit aad266def843692c353592e3d56d0241bdb36f13[m
Author: sachinkulkarni12 <sachin.kulkarni@confluxtechnologies.com>
Date:   Mon Mar 21 16:18:52 2016 +0530

    FINERACT-90:addind a new tranche to an active loan is changing the disbursement amount

[33mcommit 04fe104dde51de3696dc05f545a1bbbf3ef7d83a[m
Author: sachinkulkarni12 <sachin.kulkarni@confluxtechnologies.com>
Date:   Tue Mar 15 18:06:24 2016 +0530

    adding global config for interest charged from date same as disbursement date

[33mcommit bf6d0eaa5b0c44edccbda619433962f0b5de11ee[m
Merge: 4d6488e 8331deb
Author: Nazeer Hussain Shaik <nazeer.shaik@confluxtechnologies.com>
Date:   Mon Mar 14 16:16:22 2016 +0530

    Merge branch 'FINERACT68' into develop

[33mcommit 8331deb9bec67e1b72b639b8426242e6ac8a8b24[m
Author: sachinkulkarni12 <sachin.kulkarni@confluxtechnologies.com>
Date:   Mon Mar 14 14:54:20 2016 +0530

    FINERACT-68:Validating for first repayment date while center rescheduling

[33mcommit 4d6488eb00c7074ee6af103879adb2e2f40f392b[m
Merge: e3f0eca 746d2a7
Author: Nazeer Hussain Shaik <nazeer.shaik@confluxtechnologies.com>
Date:   Mon Mar 14 14:49:42 2016 +0530

    Merge branch 'FINERACT67' into develop

[33mcommit 746d2a79f0166b2c6cf7a4c26d4f2a9fda6de8b4[m
Author: sachinkulkarni12 <sachin.kulkarni@confluxtechnologies.com>
Date:   Mon Mar 14 11:34:53 2016 +0530

    fix for undo last tranche if we change emi amount for second tranche

[33mcommit e3f0ecafc38b3ae3d7bc7e35943fb419f5d2b728[m
Author: venkatconflux <venkata.conflux@confluxtechnologies.com>
Date:   Fri Mar 11 17:59:08 2016 +0530

    MIFOSX-2626 Resolved

[33mcommit 7e9a7596c4e199b35570d61cfffd45d36c88a190[m
Author: Nazeer Hussain Shaik <nazeer.shaik@confluxtechnologies.com>
Date:   Wed Mar 9 17:25:43 2016 +0530

    Chanrging the version

[33mcommit 6ecbd28121f5fb20781c4a7f1135223d94364eab[m
Author: venkatconflux <venkata.conflux@confluxtechnologies.com>
Date:   Wed Mar 9 12:42:43 2016 +0530

    payment type applicable for disbursement charge configuration added

[33mcommit b786d459bc64a16fc907ea181461c1bc5ba3781a[m
Merge: a6c9d3d 859c0af
Author: Nazeer Hussain Shaik <nazeer.shaik@confluxtechnologies.com>
Date:   Wed Mar 9 12:24:09 2016 +0530

    Merge branch 'FINERACT-59' into develop

[33mcommit a6c9d3d3e2779a644c54b2306b15d1ebd96950f0[m
Merge: 37dfe1a 6e521f8
Author: Nazeer Hussain Shaik <nazeer.shaik@confluxtechnologies.com>
Date:   Wed Mar 9 12:12:21 2016 +0530

    Merge branch 'organizationstart' into develop

[33mcommit 859c0affefe95d61c1cae8a7f158288591ba5ad0[m
Author: pramod <pramod@confluxtechnologies.com>
Date:   Mon Mar 7 17:15:56 2016 +0530

    FINERACT-59 : corrected interest calculation for flat loans

[33mcommit 6e521f81ac68e059b89351a4579038fae1381e1a[m
Author: sachinkulkarni12 <sachin.kulkarni@confluxtechnologies.com>
Date:   Mon Mar 7 16:50:43 2016 +0530

    fix for Junit test cases

[33mcommit 37dfe1a0bf1fc65d829cc48b57a69fea616cc093[m
Author: pramod <pramod@confluxtechnologies.com>
Date:   Fri Mar 4 15:57:27 2016 +0530

    MIFOSX-2426 : added support for Interest Rate Charts based on Amount

[33mcommit 14499a26fb521f0f5b689337296fa242b1169fbf[m
Merge: c1125cf fa02a3a
Author: Adi Narayana Raju <adi.raju@confluxtechnologies.com>
Date:   Fri Mar 4 11:14:54 2016 +0530

    Merge branch 'repay' into develop

[33mcommit c1125cfdf3c53df2749133451481f97a9c849b6a[m
Merge: 1485dc9 0e0673e
Author: Adi Narayana Raju <adi.raju@confluxtechnologies.com>
Date:   Fri Mar 4 11:11:11 2016 +0530

    Merge branch 'sql' into develop

[33mcommit fa02a3a712c4a798de9cf780fe2a0a959256aa83[m
Author: sachinkulkarni12 <sachin.kulkarni@confluxtechnologies.com>
Date:   Thu Mar 3 16:45:24 2016 +0530

    fix for repayment info

[33mcommit 1485dc9a87aad88367c44c9489bacff9ad6601a5[m
Merge: 00e0ea6 a01c1e3
Author: Nazeer Hussain Shaik <nazeer.shaik@confluxtechnologies.com>
Date:   Wed Mar 2 17:59:18 2016 +0530

    Merge branch 'reschedule_center' into develop

[33mcommit a01c1e361ff2cf91eafe8888102b492f67030476[m
Author: sachinkulkarni12 <sachin.kulkarni@confluxtechnologies.com>
Date:   Wed Mar 2 17:12:31 2016 +0530

    Center Rescheduling

[33mcommit 0e0673e3aca9645c7ec8f4d101c7c2affaccc343[m
Author: sachinkulkarni12 <sachin.kulkarni@confluxtechnologies.com>
Date:   Wed Mar 2 15:45:20 2016 +0530

    insert script for organisation start date

[33mcommit 00e0ea6730d09112990b14f2eb656e7b3d9106c6[m
Author: Adi Narayana Raju <adi.raju@confluxtechnologies.com>
Date:   Wed Mar 2 14:27:14 2016 +0530

    Correction in interest recalculation scheduler job to avoid infinite loop

[33mcommit 79fe66e30dbd966aa6f0e75a3f1e2412b944bd52[m
Author: sachinkulkarni12 <sachin.kulkarni@confluxtechnologies.com>
Date:   Mon Feb 29 12:37:18 2016 +0530

    adding organisation start in global configuartion

[33mcommit 8d160d3e7deaf1059c45c7859d801b4e5465d54c[m
Author: Nazeer Hussain Shaik <nazeer.shaik@confluxtechnologies.com>
Date:   Wed Feb 24 12:06:01 2016 +0530

    Updating release notes

[33mcommit 331e8e4dec73ed68b3b002cfebd0647b99d652ad[m
Author: pramod <pramod@confluxtechnologies.com>
Date:   Tue Feb 23 14:29:02 2016 +0530

    MIFOSX-2516: fix for  Floating rates with multi tranche loan

[33mcommit 1be4d026cff05e2cd802e8260eebfbadd1ab9836[m
Author: unknown <nazeer.shaik@confluxtechnologies.com>
Date:   Wed Feb 17 19:18:24 2016 +0530

    Adding Notice.txt file

[33mcommit 1c0e5502e0832f56c09c34f1ad3e6d8616e825fd[m
Author: unknown <nazeer.shaik@confluxtechnologies.com>
Date:   Tue Feb 16 18:41:16 2016 +0530

    moving driver info to a property file

[33mcommit 9c27413cf1842170583a5d8eaa7c9832e337677b[m
Author: unknown <nazeer.shaik@confluxtechnologies.com>
Date:   Tue Feb 16 18:16:47 2016 +0530

    License text missing error in application.properties

[33mcommit b5dc6b2b5bec2bb4f9681b66a862939f6cdfbadd[m
Author: pramod <pramod@confluxtechnologies.com>
Date:   Tue Feb 16 12:21:30 2016 +0530

    corrected type while fethcing data from metadata

[33mcommit a13a80794fabe8bba6dc6e359006d7ab594f649d[m
Author: pramod <pramod@confluxtechnologies.com>
Date:   Mon Feb 15 16:01:32 2016 +0530

    MIFOSX-2400 : fix for overdue charges on payment time

[33mcommit bb4799f583197d2ef21843b9389ba4061916aa0c[m
Author: unknown <nazeer.shaik@confluxtechnologies.com>
Date:   Tue Feb 9 18:17:56 2016 +0530

    mysql data type changes and provisiong update validation changes

[33mcommit 06fd26b8969ed4cbd84ed08069a8435442addefa[m
Author: unknown <nazeer.shaik@confluxtechnologies.com>
Date:   Mon Feb 8 14:11:19 2016 +0530

    new date types newdecimal and longlong handling with drizzle connector

[33mcommit 2a45fe1390abc166a4b9e53c83051defdf40f551[m
Author: unknown <nazeer.shaik@confluxtechnologies.com>
Date:   Mon Feb 8 11:27:46 2016 +0530

    Connector changes from mysql to drizzle

[33mcommit 86ca06215092a00d400c1d151bea585267b9ff84[m
Author: Adi Narayana Raju <adi.raju@confluxtechnologies.com>
Date:   Tue Feb 2 12:15:24 2016 +0530

    Migrate to Java 8

[33mcommit 5d7520d3e08eb3b78331703ba336343b556c2589[m
Merge: c9afea9 ee2819b
Author: Adi Narayana Raju <adi.raju@confluxtechnologies.com>
Date:   Tue Feb 2 08:48:33 2016 +0530

    Merge branch 'adiLicense' into develop

[33mcommit ee2819b793ca18044bf8c4e525fabea6ba387086[m
Author: Adi Narayana Raju <adi.raju@confluxtechnologies.com>
Date:   Mon Feb 1 16:48:06 2016 +0530

    License text spill overs

[33mcommit c9afea91fe55c7ed2c44a25577b5966ad68f6154[m
Author: pramod <pramod@confluxtechnologies.com>
Date:   Mon Feb 1 16:19:46 2016 +0530

    removed Pentaho from validation and template

[33mcommit 5903a851f4851ed9bf87cdec7c3e52394304141a[m
Author: mage <mage@apache.org>
Date:   Mon Feb 1 10:50:23 2016 +0100

    small edit to trigger mirror

[33mcommit 378330084c30da4d4ce31a9de77cc860f5ae9586[m
Merge: 66cee78 e409a4f
Author: mage <mage@apache.org>
Date:   Mon Feb 1 10:41:47 2016 +0100

    Merge branch 'master' of https://git-wip-us.apache.org/repos/asf/incubator-fineract

[33mcommit 66cee7856b5db8c027598e81ec190ef4b5bedf6a[m
Author: mage <mage@apache.org>
Date:   Mon Feb 1 10:23:53 2016 +0100

    deleted initial code

[33mcommit e409a4f17cc183f79d38b7e5d4d6475408d70641[m
Merge: 97587d9 7098d7f
Author: Shaik Nazeer Hussain <nazeer.shaik@confluxtechnologies.com>
Date:   Mon Feb 1 13:42:16 2016 +0530

    Merge pull request #1616 from rajuan/fineractwithhistory
    
    Changes for Apache Fineract

[33mcommit 7098d7f83d9da84622d22b5c564e7e0749fe62a6[m
Author: Adi Narayana Raju <adi.raju@confluxtechnologies.com>
Date:   Mon Feb 1 12:05:28 2016 +0530

    Modify file contents

[33mcommit a3b7a5c77245cc9daf1ba83ade7526f6e49d60c7[m
Author: Adi Narayana Raju <adi.raju@confluxtechnologies.com>
Date:   Mon Feb 1 12:00:59 2016 +0530

    Delete unwanted files

[33mcommit 881cc41480ef15eb3d6efee3a32f6308d792859a[m
Author: Adi Narayana Raju <adi.raju@confluxtechnologies.com>
Date:   Mon Feb 1 11:56:25 2016 +0530

    Rename filesnames containing mifos

[33mcommit 494ba87c4c5ec9107a47f77adf9c470a5881ec88[m
Author: Adi Narayana Raju <adi.raju@confluxtechnologies.com>
Date:   Mon Feb 1 11:34:52 2016 +0530

    Rename java root packages

[33mcommit 139e066d3d0e6b14d943d93415406be0f19b6e14[m
Author: Adi Narayana Raju <adi.raju@confluxtechnologies.com>
Date:   Mon Feb 1 11:00:15 2016 +0530

    Rename root folders

[33mcommit 4b1ec9ef542a2e5e0e0a23a26bfbe602ab48f4cc[m
Author: mage <mage@apache.org>
Date:   Thu Jan 28 06:45:31 2016 +0100

    initial code push

[33mcommit c5b56dae8fc93986628301bf1b2c7f4a96fa880b[m
Author: pramod <pramod@confluxtechnologies.com>
Date:   Mon Jan 25 17:22:30 2016 +0530

    Report related changes

[33mcommit 97587d90198e8a32c7a3d7f11d4ffa9423971392[m
Author: Adi Narayana Raju <adi.raju@confluxtechnologies.com>
Date:   Wed Jan 20 18:28:14 2016 +0530

    Updating release notes for 16.01.2.RELEASE

[33mcommit 104aa3a14f1b0069fa725e6aedc6fe10ce189249[m
Merge: 02a1681 2ae4f02
Author: pramod <pramod@confluxtechnologies.com>
Date:   Wed Jan 20 16:52:17 2016 +0530

    Merge pull request #1614 from venkatconflux/MIFOSX-2481
    
    MIFOSX-2481 : Adding Tranche - Branch Closure Issue Resolved

[33mcommit 2ae4f02414dfa7800d3f8d52530dc83ab2252173[m
Author: venkatconflux <venkata.conflux@confluxtechnologies.com>
Date:   Wed Jan 20 16:41:54 2016 +0530

    MIFOSX-2481 adding tranche - branch closure issue resolved

[33mcommit 02a16817844c2913ae66723ed6e1593f72becf1f[m
Merge: f6f5af5 10fd3f8
Author: Adi Narayana Raju <adi.raju@confluxtechnologies.com>
Date:   Wed Jan 20 16:27:24 2016 +0530

    Merge pull request #1612 from pramodn02/MIFOSX-2390
    
    MIFOSX-2390 : fixed prepay loan with installment charges & junit fixes

[33mcommit 10fd3f8978996175fcaf6dd7660fca5c15b3118a[m
Author: pramod <pramod@confluxtechnologies.com>
Date:   Wed Jan 20 16:04:13 2016 +0530

    junit test case fixes

[33mcommit 4caeecfeb988b27ba8dec3dfced233a2dec32806[m
Author: pramod <pramod@confluxtechnologies.com>
Date:   Wed Jan 20 11:24:26 2016 +0530

    MIFOSX-2390 : fixed prepay loan with installment charges

[33mcommit f6f5af5c070955cf17a80cedff4a85962f2a619e[m
Merge: 7e6fced b57db81
Author: Adi Narayana Raju <adi.raju@confluxtechnologies.com>
Date:   Wed Jan 20 16:01:01 2016 +0530

    Merge pull request #1611 from nazeer1100126/SharesAndDividends
    
    Shares and dividends

[33mcommit b57db81a66dfcb8a9670da049b9da318115aecde[m
Author: unknown <nazeer.shaik@confluxtechnologies.com>
Date:   Wed Jan 20 15:54:36 2016 +0530

    Shares and Dividends Mockup API implementation

[33mcommit b4d9c88ba49a14383ddbd1e1ef29eab0b5903601[m
Author: unknown <nazeer.shaik@confluxtechnologies.com>
Date:   Fri Jan 8 10:39:43 2016 +0530

    Share and dividends implementation

[33mcommit 7e6fcede8552d5da26778a545ef096733f957f5b[m
Merge: 3cf5c5d 7c1d9e5
Author: Adi Narayana Raju <adi.raju@confluxtechnologies.com>
Date:   Wed Jan 20 14:42:09 2016 +0530

    Merge pull request #1610 from sangameshn/patch-11
    
    Update INSTALL.md

[33mcommit 7c1d9e5a7a3f98e445f4554bf7193eddb72f4f81[m
Author: sangameshn <sangamesh@confluxtechnologies.com>
Date:   Wed Jan 20 14:36:23 2016 +0530

    Update INSTALL.md

[33mcommit 3cf5c5dffaf5b9eba34ca63d5c6126afd35d6d45[m
Merge: fcc597a 843b5ec
Author: Adi Narayana Raju <adi.raju@confluxtechnologies.com>
Date:   Wed Jan 20 14:22:21 2016 +0530

    Merge pull request #1608 from deepak7conflux/clientNonPersonFeature
    
    Added Client as Entity feature

[33mcommit 843b5ec04b2e8bd1083f3197d8bb1f8c2525256b[m
Author: deepak7conflux <deepak.kumar@confluxtechnologies.com>
Date:   Wed Jan 20 10:15:42 2016 +0530

    Added Client as Entity feature

[33mcommit fcc597a2bfa44acd284490af253060b99d211798[m
Merge: 7ef1de1 4aa8cc9
Author: Adi Narayana Raju <adi.raju@confluxtechnologies.com>
Date:   Wed Jan 20 10:02:58 2016 +0530

    Merge pull request #1605 from venkatconflux/OverdraftInterest
    
    Added Overdraft Interest Feature

[33mcommit 7ef1de1431908d8279f67d2f4d1d4607fcedca0d[m
Merge: dc949d5 c7369fd
Author: Adi Narayana Raju <adi.raju@confluxtechnologies.com>
Date:   Wed Jan 20 09:59:56 2016 +0530

    Merge pull request #1606 from pramodn02/MIFOSX-2441
    
    MIFOSX-2441: fix for prepay loan on the same day

[33mcommit c7369fda96a89cf06f8d976956ba2b50ec72f417[m
Author: pramod <pramod@confluxtechnologies.com>
Date:   Wed Jan 20 09:28:21 2016 +0530

    MIFOSX-2441: fix for prepay loan on the same day

[33mcommit dc949d5386f2853325e95c5ddc255b8fe5ebd7e3[m
Merge: 866cd25 5134153
Author: pramod <pramod@confluxtechnologies.com>
Date:   Wed Jan 20 09:45:49 2016 +0530

    Merge pull request #1603 from rajuan/MIFOSX-2435
    
    MIFOSX-2435 : Differentiate between charges and penalty

[33mcommit 4aa8cc98923cfae44630b602dc3c3da4b29d896d[m
Author: venkatconflux <venkata.conflux@confluxtechnologies.com>
Date:   Tue Jan 19 20:40:47 2016 +0530

    Added Overdraft Interest Feature

[33mcommit 51341532e11c0f08f01c1d17362f6ee0c8cf0432[m
Author: Adi Narayana Raju <adi.raju@confluxtechnologies.com>
Date:   Tue Jan 19 16:18:24 2016 +0530

    MIFOSX-2435 : Differentiate between charges and penalty

[33mcommit 866cd25d9a2ae7b45defaa6d3d4af4f3abd90c31[m
Merge: 2eafc13 72bd702
Author: pramod <pramod@confluxtechnologies.com>
Date:   Mon Jan 18 12:08:33 2016 +0530

    Merge pull request #1600 from sachinkulkarni12/CC-36-New
    
    Loan Repayment Rescheduling while disbursement

[33mcommit 2eafc13604c06b76f6165303c06e3c0b8be74691[m
Merge: 787bad8 2c98046
Author: pramod <pramod@confluxtechnologies.com>
Date:   Mon Jan 18 12:06:59 2016 +0530

    Merge pull request #1599 from sachinkulkarni12/CC-57
    
    Interest recalculation scheduler job enhancement

[33mcommit 72bd70243ada218326e5195047094c3a6625fcd7[m
Author: sachinkulkarni12 <sachin.kulkarni@confluxtechnologies.com>
Date:   Thu Jan 14 14:10:55 2016 +0530

    Loan Repayment Rescheduling while disbursement

[33mcommit 2c98046961675451c453ae9500adea3ccc2fd1dd[m
Author: sachinkulkarni12 <sachin.kulkarni@confluxtechnologies.com>
Date:   Thu Jan 14 17:09:16 2016 +0530

    Interest recalculation scheduler job enhancement

[33mcommit 787bad8179cd89bcab41aaab9f6001bc094d9dcb[m
Merge: 34fe42c b5c355c
Author: Markus Geiß <mgeiss@mifos.org>
Date:   Thu Jan 14 12:03:01 2016 +0100

    Merge pull request #1597 from mgeiss/MIFSX-SPM-5
    
    Mifsx spm 5

[33mcommit b5c355c3c3bc68f12a238ec5dc9623d869fb69b0[m
Author: mage <mage@apache.org>
Date:   Thu Jan 14 11:59:56 2016 +0100

    added additional files

[33mcommit 265a6c4b42676c6a3def329b2d1e75791d738f91[m
Author: mage <mage@apache.org>
Date:   Thu Jan 14 11:55:09 2016 +0100

    changed scorecard from staff to appuser

[33mcommit 34fe42cd1632699fc9bfa5d76e9ade4e085bf59c[m
Merge: 6fd2141 0b19ddf
Author: Shaik Nazeer Hussain <nazeer.shaik@confluxtechnologies.com>
Date:   Thu Jan 14 16:07:04 2016 +0530

    Merge pull request #1596 from pramodn02/variableInstallment
    
    Added installment amount for period in schedule data

[33mcommit 0b19ddff32fdca23478b6fdc12d3c9be97578f26[m
Author: pramod <pramod@confluxtechnologies.com>
Date:   Thu Jan 14 15:35:47 2016 +0530

    Added installment amount for period in schedule data

[33mcommit 6fd21419450f6e25e55b29335aaeb5ea39a8000b[m
Merge: 47974cf 1b04614
Author: Shaik Nazeer Hussain <nazeer.shaik@confluxtechnologies.com>
Date:   Thu Jan 14 15:11:42 2016 +0530

    Merge pull request #1595 from rajuan/FloatingRatesOrdering
    
    Determine base lending rate correctly based on the order of rate periods

[33mcommit 1b04614f4221ce7c4cd4bf01f04e84120ed23b93[m
Author: Adi Narayana Raju <adi.raju@confluxtechnologies.com>
Date:   Thu Jan 14 15:05:27 2016 +0530

    Determine base lending rate correctly based on the order of rate periods

[33mcommit 47974cfa433b59b61899d0a8234cbb1c8ee81f76[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Jan 13 18:52:30 2016 -0800

    Update INSTALL.md
    
    One click installer does not work

[33mcommit 3ace65d4938e637af22c76edd125ee63a74af912[m
Merge: b4f61c5 0d3338a
Author: Shaik Nazeer Hussain <nazeer.shaik@confluxtechnologies.com>
Date:   Wed Jan 13 17:35:30 2016 +0530

    Merge pull request #1592 from rajuan/FixFloatingRatesPeriodsOrder
    
    Fixing ordering of Rate periods while retrieving floating rate data

[33mcommit 0d3338ab164d182b4aab33c7bf32dc757b748105[m
Author: Adi Narayana Raju <adi.raju@confluxtechnologies.com>
Date:   Wed Jan 13 17:31:42 2016 +0530

    Fixing ordering of Rate periods while retrieving floating rate data

[33mcommit b4f61c5ed0b9f0b1cdcf0ff0c5c597b74b64764f[m
Merge: e965ea7 7c45056
Author: Adi Narayana Raju <adi.raju@confluxtechnologies.com>
Date:   Wed Jan 13 15:49:40 2016 +0530

    Merge pull request #1591 from pramodn02/preclosefix
    
    MIFOSX-2437: fix for prepay of loan

[33mcommit 7c45056f88ebf2367ea86f03c17f59f985726057[m
Author: pramod <pramod@confluxtechnologies.com>
Date:   Wed Jan 13 15:27:50 2016 +0530

    MIFOSX-2437: fix for prepay of loan

[33mcommit e965ea78d0b3973bbfe9c4b7477d7f24faabac51[m
Merge: ecb2c93 548eb1a
Author: Markus Geiß <mgeiss@mifos.org>
Date:   Wed Jan 13 08:35:06 2016 +0100

    Merge pull request #1589 from mgeiss/MIFOSX-SPM-4
    
    eased survey handling for scorecard

[33mcommit 548eb1a110448b39a9c0c8ea687db226f6af2a87[m
Author: mage <mage@apache.org>
Date:   Wed Jan 13 08:33:42 2016 +0100

    eased survey handling for scorecard

[33mcommit ecb2c9333490aa0d35d6dffd0b7b4f43051d6d2f[m
Author: unknown <nazeer.shaik@confluxtechnologies.com>
Date:   Tue Jan 12 18:26:42 2016 +0530

    Updating release notes for 16.01.1.RELEASE

[33mcommit 1bfeb384dfbce5de5b8e119228a1df68a293396b[m
Merge: 73f3df5 bb6e3b4
Author: Shaik Nazeer Hussain <nazeer.shaik@confluxtechnologies.com>
Date:   Mon Jan 11 16:17:28 2016 +0530

    Merge pull request #1585 from pramodn02/variableinstalmentchanges
    
    Partial periods interest calculation & variable installment junits

[33mcommit bb6e3b4537e51a61400a027fee2a48b10870b162[m
Author: pramod <pramod@confluxtechnologies.com>
Date:   Mon Jan 11 16:13:17 2016 +0530

     MIFOSX-2434 : junit test cases for variable instalments

[33mcommit 96ea36b1017b5a3e5b916d688ed42825d0ca17d4[m
Author: pramod <pramod@confluxtechnologies.com>
Date:   Mon Jan 11 14:40:43 2016 +0530

     MIFOSX-2419 : added flag to consider partial periods while interest calculation

[33mcommit 73f3df522978333ef37a426d33b23ea31dd3ebbb[m
Merge: b19a576 c7cc9b2
Author: Shaik Nazeer Hussain <nazeer.shaik@confluxtechnologies.com>
Date:   Thu Jan 7 23:02:27 2016 +0530

    Merge pull request #1582 from mgeiss/MIFOSX-SPM-3
    
    retrofitted SPM API to ease the usage

[33mcommit b19a5764b9807aa855317ef0fd6bb7d01a30006b[m
Merge: 0492b2a 63751ce
Author: pramod <pramod@confluxtechnologies.com>
Date:   Wed Jan 6 16:44:38 2016 +0530

    Merge pull request #1583 from sachinkulkarni12/CC-32New
    
    CC-32: undo last tranche disbursal feature

[33mcommit 63751ceb8c3a345064459b0f057949519d73e69b[m
Author: sachinkulkarni12 <sachin.kulkarni@confluxtechnologies.com>
Date:   Tue Jan 5 12:06:42 2016 +0530

    CC-32: undo last tranche disbursal feature

[33mcommit c7cc9b205fff6c0a029b8dde33cdb408d947e1b8[m
Author: mage <mage@apache.org>
Date:   Mon Jan 4 16:31:51 2016 +0100

    retrofitted SPM API to ease the usage

[33mcommit 0492b2a10b01d075547142e9d8458303a44cc09e[m
Merge: 0e2fba6 9901fc0
Author: Adi Narayana Raju <adi.raju@confluxtechnologies.com>
Date:   Tue Dec 29 14:06:58 2015 +0530

    Merge pull request #1580 from pramodn02/variableinstalmentchanges
    
    MIFOSX-2358:fix for last installment overdue charge

[33mcommit 9901fc0d3f4f8457b9d0d209f0afb26286e056b4[m
Author: pramod <pramod@confluxtechnologies.com>
Date:   Tue Dec 29 10:21:32 2015 +0530

    added support to accept interest charged from before disburse date

[33mcommit 453d50a4df3753215c4f7babb8507b5187fe1b07[m
Author: Roman Shaposhnik <rvs@apache.org>
Date:   Fri Dec 25 12:13:16 2015 -0800

    Repo init

[33mcommit 09df00c078e23524c77fd0a77b2d0f21983130d7[m
Author: pramod <pramod@confluxtechnologies.com>
Date:   Thu Dec 24 12:41:26 2015 +0530

    MIFOSX-2358:fix for last installment overdue charge

[33mcommit 0e2fba642767417c8dff9235c227dacba27dcb95[m
Merge: a425a96 2f29a77
Author: pramod <pramod@confluxtechnologies.com>
Date:   Thu Dec 24 17:32:58 2015 +0530

    Merge pull request #1579 from sachinkulkarni12/CC-38_new
    
    CC-38:adding client account no in client data

[33mcommit 2f29a77f02e1e3f1456d000dcc968671a141dcf0[m
Author: sachinkulkarni12 <sachin.kulkarni@confluxtechnologies.com>
Date:   Thu Dec 24 17:22:49 2015 +0530

    CC-38:adding client account no in client data

[33mcommit a425a9605fc8872c6b979fc973ddaf49ecb7a7f0[m
Merge: 21aca28 877ef13
Author: pramod <pramod@confluxtechnologies.com>
Date:   Thu Dec 24 17:00:58 2015 +0530

    Merge pull request #1567 from sachinkulkarni12/CC-42
    
    CC-42:adding new transaction date in collection sheet

[33mcommit 21aca2863b6a307792cf794842f85c9fb1b6cf57[m
Merge: c32ad6d 6a78b6e
Author: pramod <pramod@confluxtechnologies.com>
Date:   Thu Dec 24 16:59:41 2015 +0530

    Merge pull request #1570 from sachinkulkarni12/CC-61
    
    CC-61:validate holiday only once while doing repayment through collection sheet.

[33mcommit c32ad6d07d89352d9d2d08d48bd8cc67091c2b70[m
Merge: 5a356f9 23ff03b
Author: pramod <pramod@confluxtechnologies.com>
Date:   Thu Dec 24 16:59:04 2015 +0530

    Merge pull request #1578 from sachinkulkarni12/CC-48
    
    CC-48:enabling code values based on active or inactive status

[33mcommit 23ff03b0c2a167375f331a5eb2c21b8718d638fe[m
Author: sachinkulkarni12 <sachin.kulkarni@confluxtechnologies.com>
Date:   Thu Dec 24 15:28:54 2015 +0530

    CC-48:enabling code values based on active or inactive status

[33mcommit 5a356f9d3fa419762f6112b4c63927ee9bbfd0c9[m
Merge: a17d5ab 795bd49
Author: pramod <pramod@confluxtechnologies.com>
Date:   Thu Dec 24 14:00:36 2015 +0530

    Merge pull request #1577 from nazeer1100126/provisioningintegrationtests
    
    Provisioning Integration Tests

[33mcommit 795bd4932c09b6243cb275e72368a8e13a24104d[m
Author: unknown <nazeer.shaik@confluxtechnologies.com>
Date:   Thu Dec 24 13:29:57 2015 +0530

    Provisioning Integration Tests

[33mcommit a17d5ab9dfe3d9e34289fddc0d7e1bd9932041c7[m
Merge: 782160c 1d56e80
Author: Adi Narayana Raju <adi.raju@confluxtechnologies.com>
Date:   Wed Dec 23 09:19:40 2015 +0530

    Merge pull request #1574 from pramodn02/variableinstalmentchanges
    
    interest calculation fix for months with partial period

[33mcommit 1d56e8062b2b5f05afaddc9984e78202aea88a47[m
Author: pramod <pramod@confluxtechnologies.com>
Date:   Wed Dec 23 09:14:29 2015 +0530

    interest calculation fix for months with partial period

[33mcommit 782160c912b404d54f7b12944cccbfc278dbcbed[m
Merge: a5e915d c2744ea
Author: pramod <pramod@confluxtechnologies.com>
Date:   Tue Dec 22 14:05:35 2015 +0530

    Merge pull request #1572 from rajuan/MIFOSX-2207
    
    MIFOSX-2207 : Fixed outstanding balance calculation to exclude transfer transactions

[33mcommit c2744eafea26bf888ced57d704624a7713021493[m
Author: Adi Narayana Raju <adi.raju@confluxtechnologies.com>
Date:   Tue Dec 22 13:07:25 2015 +0530

    MIFOSX-2207 : Fixed outstanding balance calculation to exclude transfer transactions

[33mcommit a5e915d4562d0557f8d34d4e24d72760ede0a1ad[m
Merge: 3f45a90 b46b14a
Author: Adi Narayana Raju <adi.raju@confluxtechnologies.com>
Date:   Mon Dec 21 17:23:06 2015 +0530

    Merge pull request #1571 from pramodn02/variableinstalmentchanges
    
    MIFOSX-579:variable installment feature

[33mcommit b46b14acd3b18ff46793c206995a59faa8a6c023[m
Author: pramod <pramod@confluxtechnologies.com>
Date:   Mon Dec 21 17:18:16 2015 +0530

    MIFOSX-579:variable installment feature

[33mcommit 6a78b6eae1b1afc5ede0419ca9da2ff0a5e7f101[m
Author: sachinkulkarni12 <sachin.kulkarni@confluxtechnologies.com>
Date:   Mon Dec 21 16:41:55 2015 +0530

    CC-61:validate holiday only once while doing repayment through collection sheet

[33mcommit 877ef133a322d968bc2a79d5795bad4bbd3e794e[m
Author: sachinkulkarni12 <sachin.kulkarni@confluxtechnologies.com>
Date:   Mon Dec 21 13:21:47 2015 +0530

    CC-42:adding new transaction date in collection sheet

[33mcommit 3f45a904b130deeeeabc878cdd6bf623134409db[m
Author: Adi Narayana Raju <adi.raju@confluxtechnologies.com>
Date:   Thu Dec 17 10:32:01 2015 +0530

    Update release notes for 15.12.2.RELEASE

[33mcommit b1ac9aef95897e45f365a70ff8ee81fd4fc8d9ac[m
Merge: 70e7aa5 196d19d
Author: pramod <pramod@confluxtechnologies.com>
Date:   Thu Dec 17 09:24:57 2015 +0530

    Merge pull request #1563 from rajuan/CustomerSelfService
    
    Customer Self Service API Phase 1

[33mcommit 70e7aa5847b08ea2cebeb05ae466989b467d2510[m
Merge: 65b767c b790908
Author: pramod <pramod@confluxtechnologies.com>
Date:   Thu Dec 17 09:05:15 2015 +0530

    Merge pull request #1565 from dtsuran/MIFOSX-2388
    
    MIFOSX-2388: Frequency type isn't loaded during modifying loan application

[33mcommit b79090806cdef72cf0e657ec9f9e5f691c34c6dc[m
Author: Tsuran D.V <dtsuran@gmail.com>
Date:   Wed Dec 16 18:26:09 2015 +0200

    MIFOSX-2388: Frequency type isn't loaded during modifying loan application

[33mcommit 196d19d87aec788c8d5ce159fb4002984876c5cd[m
Author: Adi Narayana Raju <adi.raju@confluxtechnologies.com>
Date:   Wed Dec 16 17:38:52 2015 +0530

    Customer Self Service API Phase 1

[33mcommit 65b767c31dd1547441df630d3d1b074dbbcc3bc0[m
Merge: e00f40f 5c4f5fc
Author: pramod <pramod@confluxtechnologies.com>
Date:   Wed Dec 16 10:04:21 2015 +0530

    Merge pull request #1560 from minerals/mifosx-2384
    
    MIFOSX-2384 - Fix incorrect total in repayment schedule

[33mcommit e00f40f2bdbbe0523e5da0d4b24c4b2eef6e492d[m
Merge: 6961d3e 5384067
Author: pramod <pramod@confluxtechnologies.com>
Date:   Wed Dec 16 10:03:11 2015 +0530

    Merge pull request #1558 from Musoni/MIFOSX-2371
    
    MIFOSX-2371 fixed days in year issue

[33mcommit 6961d3e97c3157542558278295a3d10c5d46a73b[m
Merge: 15c204e 4444a8a
Author: Adi Narayana Raju <adi.raju@confluxtechnologies.com>
Date:   Wed Dec 16 09:20:47 2015 +0530

    Merge pull request #1562 from mgeiss/MIFOSX-SPM-2
    
    changed fetch type, edited API docs

[33mcommit 15c204e4db787406e8f595f77f76aad963b717aa[m
Merge: c50a45b be252fb
Author: pramod <pramod@confluxtechnologies.com>
Date:   Wed Dec 16 08:47:52 2015 +0530

    Merge pull request #1561 from dtsuran/MIFOSX-2385
    
    MIFOSX-2385: When request data of floating rate, server side send JSON data with locale specific format of date

[33mcommit 4444a8a0e9478c51028e016b13704e7c98e628f9[m
Author: Markus Geiss <mgeiss@mage.xip.io>
Date:   Mon Dec 14 10:22:59 2015 +0100

    changed fetch type, edited API docs

[33mcommit be252fb91c7a22946a78066416569182a392125e[m
Author: Tsuran D.V <dtsuran@gmail.com>
Date:   Sat Dec 12 14:43:45 2015 +0200

    MIFOSX-2385: When request data of floating rate, server side send JSON data with locale specific format of date

[33mcommit 5c4f5fcbd767a995ebd1f9404eb412b1792d88bb[m
Author: Arvind Sujeeth <arvind@mines.io>
Date:   Wed Dec 9 17:47:37 2015 -0800

    MIFOSX-2384 - Fix incorrect total in repayment schedule
    
    Issue occurs when a non-disbursement charge is used.

[33mcommit 5384067737e3c5cfc2326ae8e4ab6aa61a0c503c[m
Author: andrew-dzak <andrewdzakpasu@musoni.eu>
Date:   Fri Dec 4 21:32:31 2015 +0100

    MIFOSX-2371 fixed days in year issue

[33mcommit c50a45b2c8e4eb6b47e74fd964a6581f5aca8a65[m
Merge: 2c0e98a e13d0e7
Author: pramod <pramod@confluxtechnologies.com>
Date:   Fri Dec 4 14:50:38 2015 +0530

    Merge pull request #1556 from minerals/mifosx-2372
    
    MIFOSX-2372 fix incorrect charge in ScheduledJobRunner

[33mcommit e13d0e7339252aafb7faa76c2b1de2832106bdc2[m
Author: Arvind Sujeeth <asujeeth@stanford.edu>
Date:   Thu Dec 3 11:46:24 2015 -0800

    MIFOSX-2372 fix incorrect charge in ScheduledJobRunner

[33mcommit 2c0e98ac750eb246f8c454d35b098621fa2c6264[m
Merge: 8e67159 59dbdad
Author: Adi Narayana Raju <adi.raju@confluxtechnologies.com>
Date:   Wed Dec 2 16:24:53 2015 +0530

    Merge branch 'develop'

[33mcommit 59dbdad628ac3ab5c85ca00ed73376bae603bfe4[m
Author: Adi Narayana Raju <adi.raju@confluxtechnologies.com>
Date:   Wed Dec 2 16:23:26 2015 +0530

    Update release notes for 15.12.1.RELEASE

[33mcommit b9812330150ffeafbcc63a4ae18659a577dc1758[m
Author: Adi Narayana Raju <adi.raju@confluxtechnologies.com>
Date:   Wed Dec 2 12:12:11 2015 +0530

    Correcting sql file version

[33mcommit 954f513ca3f062d9a7498f97038434ea4b46276e[m
Merge: 5cff3a9 883ca09
Author: pramod <pramod@confluxtechnologies.com>
Date:   Wed Dec 2 11:32:15 2015 +0530

    Merge pull request #1552 from Musoni/MIFOSX-2184
    
    MIFOSX-2184 - Add ability to disable backdating of penalties

[33mcommit 5cff3a9feac825e5fc7b04cfaf1a33ef4ba64da3[m
Merge: 64ab498 f98b4e3
Author: Adi Narayana Raju <adi.raju@confluxtechnologies.com>
Date:   Wed Dec 2 11:28:44 2015 +0530

    Merge pull request #1553 from mgeiss/MIFOSX-SPM
    
    initial push for spm framework

[33mcommit 64ab498c9f459ab2ae6c710318ae04447ca158e5[m
Merge: 2d214fd 95c2921
Author: pramod <pramod@confluxtechnologies.com>
Date:   Wed Dec 2 11:23:54 2015 +0530

    Merge pull request #1546 from theupscale/template
    
    Reverted Back to basicAuth MIFOSX-2322

[33mcommit 2d214fd6d07b3f7b122c44ce8fc6a98d19b0012c[m
Merge: 3cdf64f c241c29
Author: Adi Narayana Raju <adi.raju@confluxtechnologies.com>
Date:   Mon Nov 30 15:22:52 2015 +0530

    Merge pull request #1554 from nazeer1100126/MIFOSX-2368
    
    Issue MIFOSX-2368 for last installment amount for equal principal pay…

[33mcommit c241c295ccb6110266c91516df62e0b6ab74798b[m
Author: unknown <nazeer.shaik@confluxtechnologies.com>
Date:   Mon Nov 30 15:14:40 2015 +0530

    Issue MIFOSX-2368 for last installment amount for equal principal payment Loan

[33mcommit f98b4e3fa928269f456e05b77fdd0aaeec5b7e8f[m
Author: Markus Geiss <mgeiss@mage.xip.io>
Date:   Fri Nov 27 10:10:04 2015 +0100

    initial push for spm framework

[33mcommit 883ca093465bd8a6199583c75dd7238eddb4b61d[m
Author: Sander van der Heijden <sandervanderheyden@musoni.eu>
Date:   Thu Nov 26 16:12:09 2015 +0100

    MIFOSX-2184 - Add ability to disable backdating of penalties

[33mcommit 3cdf64fa1644f74c0fe9a81f0ad6601e1ce6d61d[m
Merge: feda682 4a99634
Author: Adi Narayana Raju <adi.raju@confluxtechnologies.com>
Date:   Tue Nov 24 12:54:21 2015 +0530

    Merge pull request #1550 from pramodn02/MIFOSX-2363
    
    MIFOSX-2363 : isFirstRepayment set to false  for partial updates

[33mcommit 4a99634a25ce33b43b557f58de74ceae04ca5150[m
Author: pramod <pramod@confluxtechnologies.com>
Date:   Tue Nov 24 12:36:00 2015 +0530

    MIFOSX-2363 : isFirstRepayment set to false  for partial updates

[33mcommit feda68209fcb62a5b30c22fd59b5e55510803644[m
Merge: 55958b5 e29e43b
Author: pramod <pramod@confluxtechnologies.com>
Date:   Thu Nov 19 15:50:37 2015 +0530

    Merge pull request #1549 from nazeer1100126/MIFOSX-2313_Provisioning
    
    MIFOSX-2313 Loan provisioning entries not getting created for multi t…

[33mcommit e29e43b80bb8d81d1512eb81cb4caa05f0dbb940[m
Author: unknown <nazeer.shaik@confluxtechnologies.com>
Date:   Thu Nov 19 15:43:05 2015 +0530

    MIFOSX-2313 Loan provisioning entries not getting created for multi tranche loans

[33mcommit 55958b50d22bc76ccc2217f4b376bb03204baf8e[m
Merge: 2686988 9e4cb99
Author: pramod <pramod@confluxtechnologies.com>
Date:   Thu Nov 19 12:55:33 2015 +0530

    Merge pull request #1545 from Musoni/MIFOSX-2348
    
     MIFOSX-2348 fixed Insufficient account balance due to guarantors fun…

[33mcommit 8e6715992f3d7ac4c239d43792bca1e4114f30be[m
Merge: 2686988 0075961
Author: Adi Narayana Raju <adi.raju@confluxtechnologies.com>
Date:   Thu Nov 19 09:21:57 2015 +0530

    Merge pull request #1548 from sangameshn/patch-5
    
    Update INSTALL.md

[33mcommit 00759619f511933b34e0f0b82f9ba6e2aceef1b6[m
Author: sangameshn <sangamesh@confluxtechnologies.com>
Date:   Wed Nov 18 18:40:11 2015 +0530

    Update INSTALL.md

[33mcommit 2686988954b6bf72f5ce1dc1f88f348ade489886[m
Author: Adi Narayana Raju <adi.raju@confluxtechnologies.com>
Date:   Wed Nov 18 16:27:04 2015 +0530

    Update release notes for 15.11.2.RELEASE

[33mcommit f58bc239ab5c1cb4267bc03c0fe3b87dbb2ea103[m
Merge: 7ba9545 a8cf131
Author: Shaik Nazeer Hussain <nazeer.shaik@confluxtechnologies.com>
Date:   Wed Nov 18 14:47:04 2015 +0530

    Merge pull request #1547 from rajuan/FRBug
    
    Consider disbursement date during floating rate read for a loan

[33mcommit a8cf131ccf6445612c38be0489d85dc51aac02ea[m
Author: Adi Narayana Raju <adi.raju@confluxtechnologies.com>
Date:   Wed Nov 18 14:41:25 2015 +0530

    Consider disbursement date during floating rate read for a loan

[33mcommit 95c2921ff0dff9bb819cbee8161c28aeadeed94e[m
Author: Maek Twain <saransh@theupscale.in>
Date:   Tue Nov 17 15:33:07 2015 +0530

    Reverted Back to basicauth

[33mcommit 9e4cb99853cb95545213778cd275efbc7658b56b[m
Author: andrew-dzak <andrewdzakpasu@musoni.eu>
Date:   Tue Nov 17 09:35:58 2015 +0100

     MIFOSX-2348 fixed Insufficient account balance due to guarantors funds onhold

[33mcommit b52e3bc23341deb5fbd31ec029ab0e215588c42a[m
Author: Maek Twain <saransh@theupscale.in>
Date:   Mon Nov 16 20:56:11 2015 +0530

    Template ERROR bug

[33mcommit 7ba9545dd173777edebc52d7e6ddd866d6e26776[m
Merge: 82242a0 a9912c5
Author: Shaik Nazeer Hussain <nazeer.shaik@confluxtechnologies.com>
Date:   Mon Nov 16 13:27:22 2015 +0530

    Merge pull request #1543 from rajuan/MIFOSX-2350
    
    MIFOSX-2350 : Added additional validations

[33mcommit a9912c5cb8c40b09217ba3e1c1648ea92f5c6a92[m
Author: Adi Narayana Raju <adi.raju@confluxtechnologies.com>
Date:   Mon Nov 16 13:20:40 2015 +0530

    MIFOSX-2350 : Added additional validations

[33mcommit 82242a0fb02cf181b4d8edc6d20aae716e6604a8[m
Merge: 5567319 c60f148
Author: pramod <pramod@confluxtechnologies.com>
Date:   Tue Nov 10 16:46:55 2015 +0530

    Merge pull request #1541 from rajuan/FloatingRates
    
    Floating Rates Feature

[33mcommit c60f14885620072d758890fe28e2c31b58f9dc4f[m
Author: Adi Narayana Raju <adi.raju@confluxtechnologies.com>
Date:   Tue Nov 10 15:25:08 2015 +0530

    Floating Rates Feature

[33mcommit 5567319d2f5b7d3f58f2e399a70be7d043fbec80[m
Merge: 55b1c8c 3685c28
Author: Adi Narayana Raju <adi.raju@confluxtechnologies.com>
Date:   Thu Nov 5 16:15:42 2015 +0530

    Merge pull request #1537 from pramodn02/MIFOSX-2328
    
    MIFOSX-2328 : added call to update maturity date on approval

[33mcommit 3685c2868e03c66241a62e74445faa5a364c9b18[m
Author: pramod <pramod@confluxtechnologies.com>
Date:   Thu Nov 5 16:06:57 2015 +0530

    MIFOSX-2328 : added call to update maturity date on approval

[33mcommit 55b1c8c97b893f5815f3b40dd45633637da6f275[m
Author: Adi Narayana Raju <adi.raju@confluxtechnologies.com>
Date:   Wed Nov 4 17:19:06 2015 +0530

    Update Release notes for 15.11.1.RELEASE

[33mcommit 1333e79c30edcc19cb3071e3cd3bd2306787d083[m
Merge: 0b34ee8 e5078f9
Author: pramod <pramod@confluxtechnologies.com>
Date:   Wed Nov 4 15:54:08 2015 +0530

    Merge pull request #1534 from Musoni/MIFOSX-2334
    
    Mifosx-2334 meltdown in advanced accounting...sum of all charges is no…

[33mcommit 0b34ee80686587f012e635fedd8984cb78cf9b36[m
Merge: dcce0e6 5503d5a
Author: pramod <pramod@confluxtechnologies.com>
Date:   Wed Nov 4 15:49:53 2015 +0530

    Merge pull request #1535 from Musoni/MIFOSX-2335
    
    MIFOSX-2335 fix removing a charge on a recalculateLoanSchedule

[33mcommit 5503d5a22f4b949a416e42c7e425040b4c45fb8b[m
Author: andrew-dzak <andrewdzakpasu@musoni.eu>
Date:   Tue Nov 3 11:14:03 2015 +0100

    MIFOSX-2335 fix removing a charge on a recalculateLoanSchedule

[33mcommit e5078f9baca67c3b6d3a010a558e037171a85991[m
Author: andrew-dzak <andrewdzakpasu@musoni.eu>
Date:   Tue Nov 3 10:54:06 2015 +0100

    Mifosx-2334 eltdown in advanced accounting...sum of all charges is not equal to the fee charge for a transaction fix

[33mcommit dcce0e6a929ee67e4ac0a3b4ad862bd397d8c2dc[m
Merge: e61a5fb 6ae5d7d
Author: Adi Narayana Raju <adi.raju@confluxtechnologies.com>
Date:   Mon Nov 2 11:08:16 2015 +0530

    Merge pull request #1530 from nazeer1100126/MIFOSX-2303-Provisioning
    
    MIFOSX-2303 Add Product/Branch/Category filtering to Loan Provisionin…

[33mcommit 6ae5d7d52d0e9f7b54fd663e02a9f07b5abdc749[m
Author: unknown <nazeer.shaik@confluxtechnologies.com>
Date:   Thu Oct 29 18:19:52 2015 +0530

    MIFOSX-2303 Add Product/Branch/Category filtering to Loan Provisioning report

[33mcommit e61a5fba037a042599a5656c88ffe187efd5735d[m
Merge: 51d3ff7 dd472c4
Author: Adi Narayana Raju <adi.raju@confluxtechnologies.com>
Date:   Thu Oct 29 11:28:03 2015 +0530

    Merge pull request #1529 from nazeer1100126/MIFOSX-2045_RepaymentStrategy
    
    MIFOSX-2045 Clarify New Loan Repayment Strategy Wording

[33mcommit dd472c4bbc065df350376661e113800ebcc1cc37[m
Author: unknown <nazeer.shaik@confluxtechnologies.com>
Date:   Thu Oct 29 11:22:22 2015 +0530

    MIFOSX-2045 Clarify New Loan Repayment Strategy Wording

[33mcommit 51d3ff7b8b57800a106a806acf7c7648d020532a[m
Merge: 5a0fa89 88d7d05
Author: Adi Narayana Raju <adi.raju@confluxtechnologies.com>
Date:   Thu Oct 29 10:02:45 2015 +0530

    Merge pull request #1528 from nazeer1100126/provision
    
    MIFOSX-2314 MIFOSX-2313 All Provisioning Critical Issues

[33mcommit 5a0fa8940a1d5349d1f84d6cc9c4aad9eb3e66ff[m
Merge: 2108a2f 8cc65d6
Author: Adi Narayana Raju <adi.raju@confluxtechnologies.com>
Date:   Thu Oct 29 10:02:30 2015 +0530

    Merge pull request #1527 from nazeer1100126/MIFOSX-2045_RepaymentStrategy
    
    MIFOSX-2045 Clarify New Loan Repayment Strategy Wording

[33mcommit 2108a2fb4de13516b8152bc8042700069fa359b3[m
Merge: b37fb4a af089f0
Author: Adi Narayana Raju <adi.raju@confluxtechnologies.com>
Date:   Thu Oct 29 10:02:17 2015 +0530

    Merge pull request #1526 from pramodn02/MIFOSX-2320
    
    MIFOSX-2320 : Moved stop processing check as first statement

[33mcommit b37fb4a2e81ba68811e06e5975b3d2bcce5a3b01[m
Merge: bbc89eb f4fdc64
Author: Adi Narayana Raju <adi.raju@confluxtechnologies.com>
Date:   Thu Oct 29 10:02:06 2015 +0530

    Merge pull request #1520 from msiegel07/patch-1
    
    Update INSTALL.md

[33mcommit 88d7d058939c585b97d471e7a238b5032260b249[m
Author: unknown <nazeer.shaik@confluxtechnologies.com>
Date:   Wed Oct 28 15:25:44 2015 +0530

    MIFOSX-2314 MIFOSX-2313 All Provisioning Critical Issues

[33mcommit 8cc65d676e97f29912c53962f93ec506e2531fb7[m
Author: unknown <nazeer.shaik@confluxtechnologies.com>
Date:   Wed Oct 28 13:58:58 2015 +0530

    MIFOSX-2045 Clarify New Loan Repayment Strategy Wording

[33mcommit af089f06e409728cb51047224809c76ec88b45d0[m
Author: pramod <pramod@confluxtechnologies.com>
Date:   Wed Oct 28 11:01:24 2015 +0530

    MIFOSX-2320 : Moved stop processing check as first statement

[33mcommit bbc89ebbd1d6e5a8d4b2aef8695f12703054845e[m
Author: Adi Narayana Raju <adi.raju@confluxtechnologies.com>
Date:   Wed Oct 21 12:05:39 2015 +0530

    Update release notes for 15.10.2.RELEASE

[33mcommit 74b2c2567ed9178229526d7795d60251c53f86e5[m
Merge: 5bbe8dd e19835c
Author: Adi Narayana Raju <adi.raju@confluxtechnologies.com>
Date:   Wed Oct 21 11:59:01 2015 +0530

    Merge pull request #1525 from nazeer1100126/provision
    
    MIFOSX-2307 Provisioning entries are not generated if tenant have gro…

[33mcommit e19835ce465bd3c9af1db0c0ece22a02e1101e78[m
Author: unknown <nazeer.shaik@confluxtechnologies.com>
Date:   Wed Oct 21 11:53:12 2015 +0530

    MIFOSX-2307 Provisioning entries are not generated if tenant have group loans

[33mcommit 5bbe8dd1927c05ee513c1f39ca73f2181e8601fa[m
Author: Adi Narayana Raju <adi.raju@confluxtechnologies.com>
Date:   Tue Oct 20 20:52:44 2015 +0530

    Fixing 277 SQL

[33mcommit e09a94cdf59689e21b393094ccc737e31fbe915c[m
Author: Adi Narayana Raju <adi.raju@confluxtechnologies.com>
Date:   Tue Oct 20 20:06:21 2015 +0530

    Update release notes for 15.10.1.RELEASE

[33mcommit f5f8fa50a5b253af16a36a54224c375c3695f9eb[m
Merge: 5deba1c 7fabfa5
Author: Adi Narayana Raju <adi.raju@confluxtechnologies.com>
Date:   Tue Oct 20 19:48:56 2015 +0530

    Merge pull request #1524 from nazeer1100126/provision
    
    MIFOSX-2200 Adding Provisioning Implementation

[33mcommit 7fabfa5cb99f595575c7c3b40cc4f1eb3a5bdfff[m
Author: unknown <nazeer.shaik@confluxtechnologies.com>
Date:   Tue Oct 20 19:42:47 2015 +0530

    MIFOSX-2200 Adding Provisioning Implementation

[33mcommit 5deba1c71ad8529b7e6b099a7b9462e2e7dd865b[m
Merge: 1f0ce46 25ddb88
Author: pramod <pramod@confluxtechnologies.com>
Date:   Thu Oct 15 11:27:22 2015 +0530

    Merge pull request #1522 from LisaBennett/develop2
    
    Validation changes when setting staff to inactive.

[33mcommit 25ddb88db9fcb325abc55bb9023844b5e4f3c841[m
Author: LisaBennett <lisabennett13@gmail.com>
Date:   Wed Oct 14 09:41:22 2015 -0400

    Validation changes when setting staff to inactive.

[33mcommit f4fdc64bc2e66f30b7c56705f2e4ad9cdc411e5f[m
Author: msiegel07 <msiegel07@users.noreply.github.com>
Date:   Mon Oct 12 13:08:06 2015 -0400

    Update INSTALL.md
    
    Update database set up for running a database on another host

[33mcommit 1f0ce4670e7f34e289e35fe84c278e3aa078092c[m
Merge: 7517827 7710058
Author: pramod <pramod@confluxtechnologies.com>
Date:   Mon Oct 12 16:00:46 2015 +0530

    Merge pull request #1519 from sachinkulkarni12/MIFOSX-2289
    
    MIFOSX-2289:fix for Journal Entry not created for new transaction when recalculate interest batch job is ran

[33mcommit 7710058690e4997c7fea877e383ae637ba3cd96f[m
Author: sachinkulkarni12 <sachin.kulkarni@confluxtechnologies.com>
Date:   Mon Oct 12 15:10:26 2015 +0530

    MIFOSX-2289:fix for Journal Entry not created for new transaction when recalculate interest batch job is ran

[33mcommit 75178272b9952b76db376d7837634735bc9a4fc2[m
Merge: c35d4b8 0f65d4d
Author: Adi Narayana Raju <adi.raju@confluxtechnologies.com>
Date:   Mon Oct 12 12:27:18 2015 +0530

    Merge pull request #1517 from pramodn02/MIFOSX-2228
    
    MIFOSX-2228 : changed the order of calculating outstanding for multidisburse loan

[33mcommit 0f65d4dd2732f29a197b5185dbcabd2759d4e457[m
Author: pramod <pramod@confluxtechnologies.com>
Date:   Mon Oct 12 11:21:55 2015 +0530

    MIFOSX-2228 : changed the order of calculating outstanding for multi disburse loan

[33mcommit c35d4b8a9a41f1da4ffae553f5005643c0816a24[m
Merge: 49b6582 9626995
Author: pramod <pramod@confluxtechnologies.com>
Date:   Fri Oct 9 15:09:47 2015 +0530

    Merge pull request #1496 from sangameshn/patch-4
    
    Update INSTALL.md

[33mcommit 49b65827ba1547c150c46dbb5fd48afd824312a6[m
Merge: 5b78a2a 1c17e73
Author: Adi Narayana Raju <adi.raju@confluxtechnologies.com>
Date:   Fri Oct 9 15:07:16 2015 +0530

    Merge pull request #1516 from pramodn02/MIFOSX-2228
    
    MIFOSX-2228 : added recalcualte from date flexibility for interest recalculation

[33mcommit 5b78a2ace9e32fcba7509dc59fec8ac92d5cf820[m
Merge: b732fca b9e60c0
Author: pramod <pramod@confluxtechnologies.com>
Date:   Fri Oct 9 11:09:36 2015 +0530

    Merge pull request #1498 from prasadpatill2/jira-cc-18
    
    Implemented Loan purpose display

[33mcommit 1c17e73ec2f20bc8ee48e99e4bdaa29c7e329634[m
Author: pramod <pramod@confluxtechnologies.com>
Date:   Thu Oct 8 15:51:15 2015 +0530

    MIFOSX-2228 : added recalcualte from date flexibility for interest recalculation

[33mcommit b732fca525fd49f58aab903cddc554afd484e773[m
Merge: 2100fc8 093a984
Author: pramod <pramod@confluxtechnologies.com>
Date:   Thu Sep 24 09:57:38 2015 +0530

    Merge pull request #1513 from Musoni/MIFOSX-2271
    
    MIFOSX-2271 done

[33mcommit 093a9844c631d56e347646b89426a1751133cfa6[m
Author: andrew-dzak <andrewdzakpasu@musoni.eu>
Date:   Wed Sep 23 16:14:45 2015 +0200

    MIFOSX-2271 done

[33mcommit 2100fc806faa6fda9499b5ed6212b0de57aff0db[m
Author: Adi Narayana Raju <adi.raju@confluxtechnologies.com>
Date:   Mon Sep 21 20:36:38 2015 +0530

    Updates release versions for 15.09.4.RELEASE

[33mcommit cd17e609cb318d8524e3981d9a493ff9db97efae[m
Merge: d9c6ddb f51ebeb
Author: Adi Narayana Raju <adi.raju@confluxtechnologies.com>
Date:   Mon Sep 21 19:43:59 2015 +0530

    Merge pull request #1511 from nazeer1100126/DisablingReschedule
    
    MIFOSX-2258

[33mcommit f51ebeb967275108cd351511e51b952a39cd4b9a[m
Author: unknown <nazeer.shaik@confluxtechnologies.com>
Date:   Mon Sep 21 19:38:20 2015 +0530

    MIFOSX-2258

[33mcommit d9c6ddbeca334d49a7df17975f1c9dae4c52e86a[m
Merge: 6e2d6c2 c3607f1
Author: pramod <pramod@confluxtechnologies.com>
Date:   Mon Sep 21 18:52:29 2015 +0530

    Merge pull request #1509 from nazeer1100126/develop_chargesIssue
    
    MIFOSX-2240

[33mcommit c3607f19a0c91dec0af07ae53518df0b43658763[m
Author: unknown <nazeer.shaik@confluxtechnologies.com>
Date:   Mon Sep 21 17:40:00 2015 +0530

    MIFOSX-2240

[33mcommit 6e2d6c22423ab36e8d1c987d725eced437f5f06f[m
Merge: 72dcd86 0cb619f
Author: Adi Narayana Raju <adi.raju@confluxtechnologies.com>
Date:   Mon Sep 21 12:46:20 2015 +0530

    Merge pull request #1505 from pramodn02/develop
    
    MIFOSX-2227 : added mapping between Loan transaction and repayment schedule

[33mcommit 72dcd866faf4429099a24db2926856b15f06cadc[m
Merge: 6914b1e d255f7b
Author: pramod <pramod@confluxtechnologies.com>
Date:   Mon Sep 21 10:04:33 2015 +0530

    Merge pull request #1508 from rajuan/MIFOSX-2259
    
    MIFOSX-2259 : Added parameter validation

[33mcommit d255f7b3ef51804c4b4f7269b53887c83bac0e11[m
Author: Adi Narayana Raju <adi.raju@confluxtechnologies.com>
Date:   Mon Sep 21 09:05:25 2015 +0530

    MIFOSX-2259 : Added parameter validation

[33mcommit 0cb619fa80cd122ef83a589a75ff02ce457839e0[m
Author: pramod <pramod@confluxtechnologies.com>
Date:   Fri Sep 18 16:23:52 2015 +0530

    MIFOSX-2227 : added mapping between Loan transaction and repayment schedule

[33mcommit 6914b1ee93c04bb57a3fc91f6ee6de28d03dd9af[m
Author: Adi Raju <rajuan@gmail.com>
Date:   Tue Sep 15 18:13:35 2015 +0530

    Update sample data sql

[33mcommit 2fb9fc057577d45fe7bd178841546062700e051c[m
Author: Adi Raju <rajuan@gmail.com>
Date:   Tue Sep 15 18:02:48 2015 +0530

    Updating release versions

[33mcommit 132e869315a6dfad1de2beb36ab312445d3cd0d9[m
Author: Adi Narayana Raju <adi.raju@confluxtechnologies.com>
Date:   Tue Sep 15 17:33:27 2015 +0530

    Update CHANGELOG.md

[33mcommit 2124d1bcbb632b48aa5885fd082330be3049a7de[m
Merge: 326dca1 97a499b
Author: pramod <pramod@confluxtechnologies.com>
Date:   Tue Sep 15 16:55:01 2015 +0530

    Merge pull request #1504 from nazeer1100126/Mifosx_1523_-_Loan_Rescheduling
    
    Mifosx 1523   loan rescheduling

[33mcommit 97a499b07177c11e2ec53d46b3f6de3e302a31eb[m
Author: unknown <nazeer.shaik@confluxtechnologies.com>
Date:   Tue Sep 15 16:50:32 2015 +0530

    Mifosx 1523 loan rescheduling

[33mcommit e6def2a988702a065c33e54456915f80cdc9fa54[m
Author: unknown <nazeer.shaik@confluxtechnologies.com>
Date:   Tue Sep 15 16:37:37 2015 +0530

    Mifosx 1523 loan rescheduling

[33mcommit 326dca1e41c66cfb8ed1b41bbea5737fe28bb633[m
Merge: 88685e1 f094d13
Author: pramod <pramod@confluxtechnologies.com>
Date:   Tue Sep 15 13:20:36 2015 +0530

    Merge pull request #1502 from nazeer1100126/Mifosx_1523_-_Loan_Rescheduling
    
    Mifosx 1523   loan rescheduling

[33mcommit f094d13058d684548afb8c4990af481fdcfaab83[m
Author: unknown <nazeer.shaik@confluxtechnologies.com>
Date:   Tue Sep 15 12:37:18 2015 +0530

    Mifosx 1523 - Loan Rescheduling modification

[33mcommit 928dbccfc4358f65bd62f1a31de4e589257af0d7[m
Author: unknown <nazeer.shaik@confluxtechnologies.com>
Date:   Tue Sep 15 11:57:52 2015 +0530

    Mifosx 1523 - Loan Rescheduling modification

[33mcommit 75df51452655dcbce5d3885dce15a4eb6980b186[m
Author: unknown <nazeer.shaik@confluxtechnologies.com>
Date:   Tue Sep 15 11:53:44 2015 +0530

    Mifosx 1523 - Loan Rescheduling modification

[33mcommit 9f0020d8de94f3a86ac2788583f10a01d28410ee[m
Author: unknown <nazeer.shaik@confluxtechnologies.com>
Date:   Mon Sep 14 18:02:33 2015 +0530

    MIFOSX-1923 Adding Tranche Disbursement Implementation

[33mcommit dfa51dd7b3053e7a56699274ad8ee46ed56ca6cf[m
Author: unknown <nazeer.shaik@confluxtechnologies.com>
Date:   Mon Sep 14 17:39:42 2015 +0530

    MIFOSX-1523 Loan Rescheduling

[33mcommit 88685e1f0b893db0ef695971c69aa6370102884b[m
Merge: a9a5e73 d112f4a
Author: pramod <pramod@confluxtechnologies.com>
Date:   Mon Sep 14 13:09:16 2015 +0530

    Merge pull request #1497 from pramodn02/develop
    
     MIFOSX-2209 : corrected refund transaction reversals on adjust transaction

[33mcommit b9e60c0acf674bc0dc8a488caba5101974885fae[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Mon Sep 14 13:00:09 2015 +0530

    Implemented Loan purpose display

[33mcommit d112f4a79dc10257946345790b609d61caf4385c[m
Author: pramod <pramod@confluxtechnologies.com>
Date:   Mon Sep 14 10:14:09 2015 +0530

     MIFOSX-2209 : corrected refund transaction reversals on adjust transaction

[33mcommit 9626995b9fa7ab57a2911fa08a78007ed8e409b5[m
Author: sangameshn <sangamesh@confluxtechnologies.com>
Date:   Mon Sep 14 09:21:49 2015 +0530

    Update INSTALL.md

[33mcommit a9a5e735da44973acb97026907bbf769d448cd16[m
Merge: 30f7e70 ac70343
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Sep 11 15:32:15 2015 +0530

    Merge pull request #1494 from pramodn02/develop
    
    MIFOSX-2123 : added fix to change the principal amount

[33mcommit ac70343a04ef0a2373bd1ea9d99206035da13dd0[m
Author: pramod <pramod@confluxtechnologies.com>
Date:   Fri Sep 11 14:50:02 2015 +0530

    MIFOSX-2123 : added fix to change the principal amount

[33mcommit 30f7e7022f0ec3d2f58754977b9b48c22ee0d714[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Sep 9 14:00:36 2015 +0530

    Update CHANGELOG.md
    
    with details of 15.09.2 release

[33mcommit 2348c4d78e165199f732ed58d59c981c23493bf4[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Sep 9 13:48:13 2015 +0530

    updating properties for 15.09.02 release

[33mcommit d33d34a7b583366368d4a465604704e145c34be4[m
Merge: dedae5d c9767e7
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Sep 9 12:58:53 2015 +0530

    Merge branch 'MIFOSX-1746' of https://github.com/rajuan/mifosx into rajuan-MIFOSX-1746

[33mcommit dedae5dd9795ebe5dfa3661341fa18ed000e75bf[m
Merge: dbfbe7d d52809f
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Sep 9 12:54:25 2015 +0530

    Merge pull request #1493 from pramodn02/develop
    
    MIFOSX-2199 : column name corrected in the query

[33mcommit d52809fd1fdb4f81f090de532afab050d1b58327[m
Author: pramod <pramod@confluxtechnologies.com>
Date:   Wed Sep 9 12:48:11 2015 +0530

    MIFOSX-2199 : column name corrected in the query

[33mcommit dbfbe7d01620a325ff8a466141280667a123f680[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Sep 8 18:02:13 2015 +0530

    updating release properties

[33mcommit 923fbba14b27d1565a3fd4483f5eebb7b0f8462b[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Sep 8 17:56:50 2015 +0530

    Update CHANGELOG.md
    
    with details for 15.09.1.release

[33mcommit 33d4a48be5f8b87bbc279e4a107c1bdf427204b2[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Sep 8 17:27:24 2015 +0530

    MIFOSX-2195

[33mcommit c9767e76d79abbad839a19e656d4c38f8c8d34ad[m
Author: Adi Raju <rajuan@gmail.com>
Date:   Tue Sep 8 11:42:34 2015 +0530

    MIFOSX-1746: Add support for Oauth2 and make it build time configurable

[33mcommit 07f8812957cee1257c931a88ade86a839973e730[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Sep 6 21:44:01 2015 +0530

    fixing transaction scope for client charges deletion

[33mcommit b7b6e39c426e33f21ffac02372753f4a2cf367a4[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Sep 6 21:02:10 2015 +0530

    updating release properties and sample data

[33mcommit 8f1eb39d5dda9db67be95a69a203ccd94b2d679e[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Sep 6 00:04:35 2015 +0530

    MIFOSX-2049 and fix failing test cases

[33mcommit 66ce3b48738162b3277fb19ef6b71f1a78c4682e[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sat Sep 5 01:49:11 2015 +0530

    Client fees and MIFOSX-2169

[33mcommit 670b6eef193545e10480543341645ac82f2f28d1[m
Author: VenuShyam <venu.esampally@confluxtechnologies.com>
Date:   Thu Sep 3 19:50:41 2015 +0530

    Review comments addressed

[33mcommit 5a990af553ef2e6172382c8b88f2278b4da4d05e[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Sep 3 17:37:06 2015 +0530

    Update CHANGELOG.md
    
    with release notes

[33mcommit 73cd56c4911eb3a7cca61aabf866641c5202c445[m
Merge: 342c056 57344f9
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Sep 2 17:15:12 2015 +0530

    Merge pull request #1421 from chandrikamohith/MIFOSX-1578
    
    MIFOSX-1578 submitted/approved loan status clients are no more displa…

[33mcommit 342c056ee9ddf7242a2bd10108b09525a8e70256[m
Merge: 63cca6b b359db2
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Sep 2 17:08:37 2015 +0530

    Merge pull request #1427 from chandrikamohith/newMIFOSX-1775
    
    MIFOSX-1775 Adding validation to not allow edit meeting date at group…

[33mcommit 63cca6be423851ddd58aba92673c779f22723e35[m
Merge: c181b67 d226ea5
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Sep 2 17:01:55 2015 +0530

    Merge pull request #1480 from shahrahul1985/MIFOSX-2157
    
    MIFOSX-2157: Fxing the UI Issue

[33mcommit c181b67afa18a06b6789676814db0e61efef527a[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Sep 2 16:57:04 2015 +0530

    adding licence files

[33mcommit d226ea59ade2711b0a3d9d6a0c9e0e06917ea21b[m
Author: shahrahul1985 <Rahul.shah@confuxtechnologies.com>
Date:   Wed Sep 2 16:54:02 2015 +0530

    MIFOSX-2157: Fxing the UI Issue

[33mcommit 99318bdba53ef5cbb748bdec0230e250efb8fa08[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Sep 2 16:53:55 2015 +0530

    merging chandrika's pull request for test cases

[33mcommit b217b95a18587cd7d1ce6eb137e458565cfb9689[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Sep 2 15:49:06 2015 +0530

    MIFOSX-1573

[33mcommit 59d4515c61703405e01fc5b2e00615754d8fd1b0[m
Merge: 5b4ca41 2a7ab8c
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Sep 2 15:40:56 2015 +0530

    Merge pull request #1479 from prasadpatill2/jire-2015and2013
    
    Jira issues 2015 and 2013 are resolved

[33mcommit 2a7ab8cf1846afa8f860805487eb9adaaa043f81[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Wed Sep 2 15:30:12 2015 +0530

    2015 and 2013 jira issues resolved

[33mcommit 5b4ca41eb347bf9db933ace3f8ed8fdca985305d[m
Merge: 5325066 971e23e
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Sep 1 22:25:42 2015 +0530

    merging MIFOSX-1923

[33mcommit 532506608f8c4bb2e6bbb502d4a3b1b3c7515b0b[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Sep 1 06:11:12 2015 +0530

    bug fixes for client charges

[33mcommit 2a7c4919db9a3ed7ccb53d0ad6c97f31e7376b3f[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Sep 1 04:27:19 2015 +0530

    updating release properties

[33mcommit 4bf6eb186cd6c0e0e3c20d09443f07c6c23c4708[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Sep 1 04:25:47 2015 +0530

    MIFOSX-2180

[33mcommit a4cee3b1d0d2e819e9570a88ffbfa01d32d6a7eb[m
Merge: da5663f 56fe138
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Sep 1 04:22:35 2015 +0530

    Merge branch 'client-fees' into develop

[33mcommit 56fe13852d6b7a48e11582d23183efb1c75cea59[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Sep 1 04:19:55 2015 +0530

    MIFOSX-2180

[33mcommit 879f3082ba138060f13012de2c7ab67dc05af2bf[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Aug 25 20:14:07 2015 +0530

    client charges:adding accounting

[33mcommit 971e23e8b74f434466dbaf372c2358e7963a43f0[m
Author: unknown <nazeer.shaik@confluxtechnologies.com>
Date:   Mon Aug 31 19:51:33 2015 +0530

    MIFOSX-1923 Adding Tranche Disbursement Implementation

[33mcommit da5663f15cc4f3dab5f86f4861963f77ef37aa79[m
Author: keshav10 <keshav.mishra@confluxtechnologies.com>
Date:   Mon Jun 8 13:53:24 2015 +0530

    MIFOSX-2039

[33mcommit 87adf49f445e45ccb0adf982429a098fbe744f7f[m
Author: pramod <pramod@confluxtechnologies.com>
Date:   Thu Aug 27 15:04:40 2015 +0530

    MIFOSX-2038 : cleanup

[33mcommit 80f122446aabce9ad3ba0d607845a66abd4bb456[m
Author: keshav10 <keshav.mishra@confluxtechnologies.com>
Date:   Sun Jun 7 17:26:34 2015 +0530

    MIFOSX-2038

[33mcommit 2d9fc6dca406bb41dd717d0c5c1e88e9049d2753[m
Merge: 21cc6e9 b89daf3
Author: pramod <pramod@confluxtechnologies.com>
Date:   Thu Aug 27 11:35:20 2015 +0530

    Merge pull request #1402 from chandrikamohith/MIFOSX-2049
    
    MIFOSX-2049 Able to withdraw from savings account when overdraft limi…

[33mcommit 21cc6e946d9b4f85eaa04817d85144ec62738c0e[m
Merge: f495723 00ba422
Author: pramod <pramod@confluxtechnologies.com>
Date:   Thu Aug 27 10:20:37 2015 +0530

    Merge pull request #1413 from chandrikamohith/MIFOSX-1881Review
    
    MIFOSX-1881 Breadcrumb issue is resolved.Able to navigate to the resp…

[33mcommit f495723c767f06f339da7bee4ccaca22ad70d91b[m
Author: pramod <pramod@confluxtechnologies.com>
Date:   Wed Aug 26 19:36:31 2015 +0530

    MIFOSX-2148 : fixed junit test case

[33mcommit ded3006f7440c93d52560e37245406e4e8be3c8b[m
Author: pramod <pramod@confluxtechnologies.com>
Date:   Wed Aug 26 18:22:24 2015 +0530

    MIFOSX-2148 : fixed junit test case

[33mcommit 14eb1c0b72450c26be71276cf90df6890b84995f[m
Author: pramod <pramod@confluxtechnologies.com>
Date:   Wed Aug 26 16:21:20 2015 +0530

    MIFOSX-2148 : cleanup

[33mcommit fd5ef0936009c8382e7a64ef9a675d3bec2b75a0[m
Author: shahrahul1985 <Rahul.shah@confuxtechnologies.com>
Date:   Wed Aug 19 15:16:00 2015 +0530

    MIFOSX-2148: Make Rounding Mode as configurable throughout Mifos

[33mcommit f0252a174706b2893a18c5070121d47554144d28[m
Merge: 293787f ed68b06
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Aug 26 14:24:55 2015 +0530

    Merge pull request #1472 from pramodn02/performanceissues
    
    MIFOSX-2168 : changed transaction scope

[33mcommit ed68b0676da39c26eecfd15a766c90f9ed7b0118[m
Author: pramod <pramod@confluxtechnologies.com>
Date:   Wed Aug 26 14:19:24 2015 +0530

    MIFOSX-2168 : changed transaction scope

[33mcommit 293787f9139f49c0d08b15b0c8d4a18549372fcc[m
Merge: 3ff52f9 5f7c005
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Aug 25 17:16:36 2015 +0530

    Merge pull request #1471 from pramodn02/performanceissues
    
    MIFOSX-2166: changed transaction scope to loan level instead of repay…

[33mcommit 5f7c005b49e76799eea0a4c5e1e087d448f3f1e3[m
Author: pramod <pramod@confluxtechnologies.com>
Date:   Tue Aug 25 15:02:01 2015 +0530

    MIFOSX-2166: changed transaction scope to loan level instead of repayment schedule level

[33mcommit 3ff52f902acbc9c865562980a2f39463f8482a75[m
Merge: a76b60f 6628b22
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Aug 25 14:20:35 2015 +0530

    Merge pull request #1468 from pramodn02/performanceissues
    
    MIFOSX-2163 : added transaction scope at loan level

[33mcommit a76b60f7c0a46ddde3d33bf94e45cf116c2aeb84[m
Merge: 6716a5a 08e2273
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Aug 25 13:59:23 2015 +0530

    Merge pull request #1470 from pramodn02/MIFOSX-2165
    
    MIFOSX-2165 : Calendar title column length increased to 70

[33mcommit 08e22737ebfaf75b2346b3ae68f5270fc8a90a18[m
Author: pramod <pramod@confluxtechnologies.com>
Date:   Tue Aug 25 13:37:32 2015 +0530

    MIFOSX-2165 : Calendar title column length increased to 70

[33mcommit 6628b227a7f71fd82c7a2893bd48ef065b80f7e8[m
Author: pramod <pramod@confluxtechnologies.com>
Date:   Tue Aug 25 11:41:55 2015 +0530

    MIFOSX-2163 : added transaction scope at loan level

[33mcommit 6716a5a00a41271b58f331b409a924075a3c1a82[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Aug 24 17:48:52 2015 +0530

    bumping up sql patch

[33mcommit 5a1ebfe768f3365ce3deeba8448d5e44e65e1f68[m
Merge: 80bfc5e 840fd4c
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Aug 24 17:48:06 2015 +0530

    Merge branch 'MIFOSX-2117' of https://github.com/shahrahul1985/mifosx into shahrahul1985-MIFOSX-2117

[33mcommit 80bfc5e81151d59ba9641d4798dbb58246a92cb7[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Aug 24 17:46:21 2015 +0530

    trivial updates to sql patch v4

[33mcommit 9cf159e77c5edd2a5413bb3803bae62f35fb24c2[m
Merge: 2587c65 cef018a
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Aug 24 17:40:25 2015 +0530

    Merge pull request #1466 from rajuan/MIFOSX-2153
    
    MIFOSX-2153:Added support to use seperate DB server instance for reports

[33mcommit 2587c65b263c5f34c49eb1fad09ae9d807b4428f[m
Merge: 2273c8b 72d82e3
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Aug 24 17:33:25 2015 +0530

    Merge pull request #1394 from sachinkulkarni12/futureschedule
    
    MIFOSX-2025:Unable to access loan account page/loan page does not load after a multi-disbursal loan is disbursed

[33mcommit 2273c8ba65252b39c2a211017e85feec3c7e521e[m
Merge: 4842f3d ae6877d
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Aug 24 17:30:25 2015 +0530

    Merge pull request #1430 from venkatconflux/MIFOSX-2085
    
    MIFOSX-2085:scond repayment computing more interest resolved

[33mcommit 4842f3d80ba3e6c67a0af2994d25904eebda68b7[m
Merge: f7c0a83 6b643eb
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Aug 24 17:30:03 2015 +0530

    Merge pull request #1441 from venkatconflux/MIFOSX-2118
    
    MIFOSX-2118 : Resolved Second Specific Due Date Charges not collecting in RBI Strategy

[33mcommit f7c0a833cbfdc01bbaaa6b07e0f0603e4514de81[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Aug 21 04:39:49 2015 +0530

    MIFOSX-583 wip

[33mcommit cef018a990b1e3543306b9a0dc853b81c715d38b[m
Author: Adi Raju <rajuan@gmail.com>
Date:   Thu Aug 20 15:19:23 2015 +0530

    MIFOSX-2153:Added support to use seperate DB server instance  for reports

[33mcommit da514b8856d2ada70c10780eefaea760548e1721[m
Merge: 340aa20 69f9538
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Aug 20 14:14:33 2015 +0530

    Merge pull request #1465 from rajuan/MIFOSX-2158
    
    MIFOSX-2158:Changed how CommandType annotations are retrieved.

[33mcommit 69f9538905fe95a0aeffa036ecc9ca1963e188e4[m
Author: Adi Raju <rajuan@gmail.com>
Date:   Thu Aug 20 11:32:37 2015 +0530

    MIFOSX-2158:Changed how CommandType annotations are retrieved. Reverted changes done for issues MIFOSX-2131. Added mozilla license text for missing files.

[33mcommit 340aa20120d08e33eb2f536c3a84aabb317896e6[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Aug 20 01:43:38 2015 +0530

    client fees : adding client transaction api's

[33mcommit b6626b0b832bd095d7a3afd076ee756e465d8e60[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Aug 19 00:49:48 2015 +0530

    wip:changes for client fees

[33mcommit d0fd3e4a6c47b1761a566729a1875bcfd76cfddf[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Aug 17 17:42:29 2015 +0530

    changes for client fees

[33mcommit 840fd4c0a7f3524dd9e25fb1b412461724a8018c[m
Author: shahrahul1985 <Rahul.shah@confuxtechnologies.com>
Date:   Wed Aug 19 16:08:45 2015 +0530

    MIFOSX-2117: Unable to create user - with auto email generate password option

[33mcommit 89aab6b93d48a19ef75fafda7b160b92fa8db886[m
Merge: fdf31ab 5a9f0f9
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Aug 19 15:41:50 2015 +0530

    Merge pull request #1461 from rajuan/MIFOSX2156
    
    Fix import

[33mcommit 5a9f0f9941dafa585828e0a571b7608f0d857a86[m
Author: Adi Raju <rajuan@gmail.com>
Date:   Wed Aug 19 15:34:36 2015 +0530

    Fix import

[33mcommit fdf31abd228924cd0d97bb02509b2127c3c12983[m
Merge: 34e85b2 7347012
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Aug 19 13:49:39 2015 +0530

    Merge pull request #1460 from rajuan/MIFOSX2156
    
    MIFOSX-2156:Fixed integration test failure

[33mcommit 7347012bfa5f918cf6930feb769eb254c6fad136[m
Author: Adi Raju <rajuan@gmail.com>
Date:   Wed Aug 19 13:13:55 2015 +0530

    MIFOSX-2156:Fixed integration test failure

[33mcommit 34e85b2c89a4f3b57c55cb2a21a120f864684ca2[m
Merge: 7ccb57e aa214ae
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Aug 19 13:04:55 2015 +0530

    Merge pull request #1459 from shahrahul1985/MIFOSX-2154
    
    MIFOSX-2154: Fix the integration test for ExternalServicesConfigurati…

[33mcommit aa214aea920644009e64e1b30b4dcb043f6c1f90[m
Author: shahrahul1985 <Rahul.shah@confuxtechnologies.com>
Date:   Wed Aug 19 11:29:37 2015 +0530

    MIFOSX-2154: Fix the integration test for ExternalServicesConfigurationTest

[33mcommit 7ccb57eab9792436818353ab2941e2be30e661b6[m
Merge: b4b7555 8902d2a
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Aug 18 14:57:46 2015 +0530

    Merge pull request #1458 from pramodn02/performanceissues
    
    MIFOSX-2152 : corrected transaction scope

[33mcommit 8902d2a5cb6375aa6eb14e390937b77db59b4cec[m
Author: pramod <pramod@confluxtechnologies.com>
Date:   Tue Aug 18 12:30:35 2015 +0530

    MIFOSX-2152 : corrected transaction scope

[33mcommit b4b7555bc1a06578d8776f97da12f1a4ca0ce904[m
Merge: e2ddedd 68bb26b
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Aug 17 21:03:21 2015 +0530

    Merge pull request #1457 from shahrahul1985/MIFOSX-2135
    
    MIFOSX-2135: Fixing the sql file

[33mcommit 68bb26bd582c915d6098bceac2fe4a9b5a161076[m
Author: shahrahul1985 <Rahul.shah@confuxtechnologies.com>
Date:   Mon Aug 17 20:59:18 2015 +0530

    MIFOSX-2135: Fixing the sql file

[33mcommit e2ddedd5311162b094f805cb18e0fc5ce497f33b[m
Merge: 5536342 72495da
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Aug 17 17:47:23 2015 +0530

    Merge pull request #1452 from shahrahul1985/MIFOSX-2103
    
    MIFOSX-2103:Manage employees display is not working as expected

[33mcommit 5536342093b7fcc68eb1cbd73aa2654c565b6fb8[m
Merge: f8fa861 e4f260b
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Aug 17 17:46:45 2015 +0530

    Merge pull request #1451 from shahrahul1985/MIFOSX-2135
    
    Mifosx 2135

[33mcommit f8fa86190941ed0fc1e3b6134c6154632623cf35[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Aug 17 17:44:21 2015 +0530

    applying licence plugin

[33mcommit fda49b07940fb63643c0ed083c89d322da0186ef[m
Merge: e751e1f 222b943
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Aug 17 17:40:33 2015 +0530

    Merge pull request #1455 from pramodn02/performanceissues
    
    MIFOSX-2151 : corrected transaction scope

[33mcommit 222b943bfdecd79bc0c260002136c68bc050f693[m
Author: pramod <pramod@confluxtechnologies.com>
Date:   Mon Aug 17 16:57:47 2015 +0530

    MIFOSX-2151 : corrected transaction scope

[33mcommit e751e1f6826204806723aed62872bbb76c67267b[m
Author: pramod <pramod@confluxtechnologies.com>
Date:   Mon Aug 17 13:29:55 2015 +0530

    ignoring failing test cases

[33mcommit fce8f73b83756cd508a4715f334f2b9d059c22d1[m
Merge: fb8eb4a 95c4320
Author: pramod <pramod@confluxtechnologies.com>
Date:   Mon Aug 17 13:28:15 2015 +0530

    Merge branch 'peter1' into develop

[33mcommit 95c43200d6413e8d95f63aa53624a8707d14c514[m
Author: pramod <pramod@confluxtechnologies.com>
Date:   Mon Aug 17 13:27:22 2015 +0530

    cleaning up MIFOSX-1765

[33mcommit dc672153acb63e6f78ac6aad7324690cb6ead6b0[m
Author: Peter Naftaliev <horkanus26@gmail.com>
Date:   Fri Aug 14 20:34:19 2015 +0300

    Finished fixing group loan date change and rechduling date issues. Bug: MIFOSX-1765

[33mcommit 72495da5b9b23b25005f63ec7694b01cd940d31d[m
Author: shahrahul1985 <Rahul.shah@confuxtechnologies.com>
Date:   Fri Aug 14 12:44:27 2015 +0530

    MIFOSX-2103:Manage employees display is not working as expected

[33mcommit e4f260bd691aa0c44ed1f9e226bcdfda2d605449[m
Author: shahrahul1985 <Rahul.shah@confuxtechnologies.com>
Date:   Fri Aug 14 12:27:22 2015 +0530

    MIFOSX-2135: Create new endpoints and the User interface for managing credentials to external services.

[33mcommit fb8eb4a977e1b954edcb7a5487470cc02147425e[m
Merge: d05f8d8 da59a50
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Aug 12 06:20:07 2015 -0700

    Merge pull request #1448 from vorburger/UGDTemplateArraySupport_squashed
    
    MIFOSX-2144: TemplateMergeService expandMapArrays + passing @Test arrayUsingLoop

[33mcommit da59a50299a7b2c6a6291458fab26d8fd27eeb81[m
Author: Michael Vorburger <mike@vorburger.ch>
Date:   Sat Aug 1 12:18:37 2015 +0530

    MIFOS-2144 TemplateMergeService expandMapArrays + @Test arrayUsingIndex
    
    TemplateMergeService clean up, incl. proper logging
    TemplateMergeServiceTest fixed up, incl. fix compileLoanSummary() now()
    TemplateMergeServiceTest arrayUsingLoop
    TemplateMergeServiceTest simplified
    Gradle License check exclude **/*.html for src/test/resources/template-expected.html

[33mcommit d05f8d8092b7b334f0f790b93f87349f86a2b9f6[m
Merge: e201f37 9947563
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Aug 6 14:01:53 2015 -0700

    Merge branch 'master' into develop

[33mcommit 9947563078ba3274f34ce2c464964cb5bab5c88d[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Aug 6 13:32:16 2015 -0700

    fix for MIFOSX-2131

[33mcommit e201f37191c4c8d38ba5c9396cdc67da7e8abca7[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Jul 31 19:53:06 2015 -0700

    updating documentation for MIFOSX-2122

[33mcommit 28d359653e3c7041de74ae57bf19e0ed00f8cb66[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Jul 31 19:23:06 2015 -0700

    cleaning up formatting diffs for MIFOSX-2129

[33mcommit 9aae2aad3d2e163a6574727513c3cbd918d901d1[m
Merge: 16d92b8 3757e2b
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Jul 31 19:22:07 2015 -0700

    Merge pull request #1447 from sachinkulkarni12/MIFOSX-2129
    
    MIFOSX-2129:Submitting Collection sheet for zero repayments create an entry in Loan trasaction page

[33mcommit 3757e2b6890ed226a9896541d0baf07983351c27[m
Author: sachinkulkarni12 <sachin.kulkarni@confluxtechnologies.com>
Date:   Wed Jul 29 11:13:58 2015 +0530

    MIFOSX-2129:Submitting Collection sheet for zero repayments create an entry in Loan trasaction page

[33mcommit befd953fcf2c1c7bfd8035f93f9ca625c944dfd1[m
Author: sachinkulkarni12 <sachin.kulkarni@confluxtechnologies.com>
Date:   Sun Aug 30 16:27:09 2015 +0530

    MIFOSX-1788:Waive interest is not working as expected

[33mcommit 16d92b83c4e4f4d6ebd2f4b5d0d85a3d8815c636[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Jul 27 17:30:36 2015 -0700

    Update INSTALL.md

[33mcommit 3f9e411670e779efd69741d0261ce38f77fd174a[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Jul 27 17:28:39 2015 -0700

    Update INSTALL.md

[33mcommit 7e093be216d910f8a75f6aae351fc27dce1cef61[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Jul 27 17:27:49 2015 -0700

    Update INSTALL.md

[33mcommit 39c13268f5eca38aea1bea39941c14d9bf385fc1[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Jul 23 22:30:03 2015 -0700

    clarifying changes made for MIFOSX-1788

[33mcommit a3dcf36e6b50635e8beeb51fa7afb042addda0a9[m
Merge: 3698b4f befd953
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Jul 23 22:02:02 2015 -0700

    Merge branch 'MIFOSX-1788' of https://github.com/sachinkulkarni12/mifosx into sachinkulkarni12-MIFOSX-1788

[33mcommit 3698b4fc988ee47a476a73df70c5f6f92a2d1705[m
Merge: a43a117 9aacbb3
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Jul 21 16:14:53 2015 -0700

    Merge pull request #1438 from emmanuelnnaa/MIFOSX-2102
    
    commit for MIFOSX-2102 (Loan application submittedOnDate property compared with server date instead of tenant date)

[33mcommit a43a117eefe4881bce4a8d107bc98f901273095c[m
Merge: 1e00782 45688cf
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Jul 16 11:29:37 2015 -0700

    Merge pull request #1443 from sughosh88/MIFOSX-2122
    
    Mifosx 2122

[33mcommit 45688cf408cc8f6d34786420d9fe19b479c8dc19[m
Author: sughosh88 <sughosh@confluxtechnologies.com>
Date:   Thu Jul 16 10:41:34 2015 -0700

    Minor Reporting Fix For issues 2012,2013 & 2015

[33mcommit f5b86e52b65e5cb5fee70bfa2807a049c657425e[m
Author: sughosh88 <sughosh@confluxtechnologies.com>
Date:   Thu Jul 16 10:34:17 2015 -0700

    Export pentaho report to Excel 2007 format (.xlsx)

[33mcommit 6b643eb1a050ca69e7c4e03ceb65cc3dbd10fcd9[m
Author: venkatconflux <venkata.conflux@confluxtechnologies.com>
Date:   Thu Jul 16 09:59:51 2015 +0530

    MIFOSX-2118 : Resolved Second Specific Due Date Charges not collecting

[33mcommit 9aacbb3178aa64b5573ad2e571cfaa41df8ae3df[m
Author: Emmanuel Nnaa <emmanuelnnaa@musoni.eu>
Date:   Mon Jul 6 10:40:12 2015 +0200

    commit for MIFOSX-2102 (Loan application submittedOnDate property compared with server date instead of tenant date)

[33mcommit 1e00782c911de4f17f45fca835d7d3c056e42c7b[m
Merge: 4d0fc18 19b624e
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Jun 26 17:16:45 2015 -0700

    Merge pull request #1432 from venkatconflux/develop
    
    corrected misspelled payment type API in  API-Docs

[33mcommit 19b624eb867c0b42e02d9edce8482f022d7e198f[m
Author: venkatconflux <venkata.conflux@confluxtechnologies.com>
Date:   Fri Jun 26 16:32:00 2015 +0530

    corrected misspelled payment type API in  API-Docs

[33mcommit ae6877d4a0edabd86b2cce2305e2ffce9cc60f5d[m
Author: venkatconflux <venkata.conflux@confluxtechnologies.com>
Date:   Wed Jun 24 11:46:22 2015 +0530

    MIFOSX-2085:scond repayment computing more interest resolved

[33mcommit 4d0fc186299fea9def09f1c94d8f1d9c0bd7f0ba[m
Merge: cc58715 f8992fe
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Jun 22 05:00:37 2015 -0700

    Merge pull request #1425 from sughosh88/MIFOSX-2009
    
    MIFOSX-2009 Reports Fix

[33mcommit b359db2ac95233e706be976b789b2e689a120ed4[m
Author: Chandrika <chandrika@confluxtechnologies.com>
Date:   Thu Jun 18 10:21:51 2015 -0400

    MIFOSX-1775 Adding validation to not allow edit meeting date at group level,if meeting is created by center.

[33mcommit f8992fe0f30202591874654d42f8f9d65281a37b[m
Author: sughosh88 <sughosh@confluxtechnologies.com>
Date:   Thu Jun 18 15:01:37 2015 +0530

    MIFOSX-2009 Reports Fix

[33mcommit cc5871557574ffd3b0e94c7baa475dc8ebebfa90[m
Merge: 9ed5631 bdfb61e
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Jun 17 15:53:16 2015 -0700

    Merge pull request #1422 from chandrikamohith/MIFOSX-1592
    
    MIFOSX-1592 Fix for Global search api - group loans/savings search ar…

[33mcommit bdfb61eaa9105c2ec506e32a425309e3ffa295e1[m
Author: Chandrika <chandrika@confluxtechnologies.com>
Date:   Wed Jun 17 17:13:42 2015 -0400

    MIFOSX-1592 Fix for Global search api - group loans/savings search are now working as expected.

[33mcommit 57344f913755066089a981369c26e5c726fa6e4c[m
Author: Chandrika <chandrika@confluxtechnologies.com>
Date:   Wed Jun 17 16:59:01 2015 -0400

    MIFOSX-1578 submitted/approved loan status clients are no more displayed.Able to save the collection sheet now.

[33mcommit 9ed56310339acc6d260e9a29dd365e27742cca14[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Jun 16 11:12:46 2015 -0700

    bumping up sql version

[33mcommit cef55bdde776652f5a8f36012bad8d7d09da2e62[m
Merge: ee9ed47 7d3307a
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Jun 16 11:11:52 2015 -0700

    Merge branch 'MIFOSX-2033' of https://github.com/chandrikamohith/mifosx into chandrikamohith-MIFOSX-2033

[33mcommit ee9ed471634e95291f977214f5fd3e8750d0cc57[m
Merge: 55ec6df 5a4dbe3
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Jun 16 10:44:11 2015 -0700

    Merge pull request #1419 from pramodn02/develop
    
    MIFOSX-2006 : fixed emi amount for tranche loan

[33mcommit 5a4dbe32396c25134eaf31290ee4da376e948c3c[m
Author: pramod <pramod@confluxtechnologies.com>
Date:   Tue Jun 16 22:57:56 2015 +0530

    MIFOSX-2006 : fixed emi amount for tranche loan

[33mcommit 55ec6df1571aeb804455af273aadc84764e44513[m
Merge: f2c1b93 5d73f2b
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Jun 16 07:23:39 2015 -0700

    Merge pull request #1417 from chandrikamohith/MIFOSX-2075
    
    MIFOSX-2075 Adding parameter interestRateFrequencyType to supported p…

[33mcommit f2c1b930a82d3b67a71f3d9f1fa74cb5409de193[m
Merge: 95f8bb8 dbd1889
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Jun 16 07:22:12 2015 -0700

    Merge pull request #1410 from sharmapankaj2512/MIFOSX-1935
    
    MIFOSX-1935: role with no active user attached will get deleted

[33mcommit 95f8bb8bca71877857b9aa74e4bcf607ed9d2405[m
Merge: e088d50 e93072f
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Jun 15 23:40:20 2015 -0700

    Merge pull request #1415 from sughosh88/MIFOSX-2009
    
    Mifosx 2009

[33mcommit 5d73f2bd1d6f80aa8a32d459b6367411d7cb5f2a[m
Author: Chandrika <chandrika@confluxtechnologies.com>
Date:   Mon Jun 15 23:56:03 2015 -0400

    MIFOSX-2075 Adding parameter interestRateFrequencyType to supported parameters.

[33mcommit e088d50f5d11f2d35fd90e8b374be04e06f0eedf[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Jun 15 15:21:11 2015 -0700

    MIFOSX-2074

[33mcommit e93072fca1fbd0c15a808963ff23a3174ba037a9[m
Author: sughosh88 <sughosh@confluxtechnologies.com>
Date:   Wed Jun 10 10:37:50 2015 +0530

    MIFOSX-2009(Epic) MIFOSX-2012,MIFOSX-2013,MIFOSX-2015 & MIFOSX-2016

[33mcommit 00ba422705bf2b7616a65805e4d8a509ebdb5f6b[m
Author: Chandrika <chandrika@confluxtechnologies.com>
Date:   Tue Jun 9 12:07:03 2015 -0400

    MIFOSX-1881 Breadcrumb issue is resolved.Able to navigate to the respective center/group.Group Level has been fetched for this case.

[33mcommit b3735c6270540fa380057e81b20f9f42fa47f629[m
Author: sughosh88 <sughosh@confluxtechnologies.com>
Date:   Mon Jun 8 17:36:57 2015 +0530

    Balance Outstanding,Collection Report,Disbursal Report & Active Loan Summary Per Branch

[33mcommit dbd1889bf1fedbd8076171453d2745e69d26bda9[m
Author: Pankaj Sharma <pankajsharma@INPankajs.local>
Date:   Sat Jun 6 09:47:10 2015 +0530

    MIFOSX-1935: role with no active user attached will get deleted

[33mcommit be9cb891c912b92cdb0fbac615d89bcabfe66f1a[m
Merge: 52b5efd 1947031
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Jun 4 16:40:50 2015 -0700

    Merge pull request #1409 from venkatconflux/MIFOSX-2061
    
    MIFOSX-2061 : charges for savings are specific for branch

[33mcommit 1947031aca456b5ee82ddd896b42eaf10ff332b4[m
Author: venkatconflux <venkata.conflux@confluxtechnologies.com>
Date:   Thu Jun 4 09:47:00 2015 +0530

    MIFOSX-2061 : charges for savings are specific for branch

[33mcommit 7d3307a0ebb61fa2de6cea249fd6b8d87d7ad998[m
Author: Chandrika <chandrika@confluxtechnologies.com>
Date:   Wed Jun 3 20:08:05 2015 -0400

    MIFOSX-2033 Insert permissions for read payment type and Staff Assignment History report.

[33mcommit 52b5efdf895485dcaff1dc9eaf54fdc77a31a2ec[m
Merge: 4060ab3 7485867
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Jun 3 03:23:22 2015 -0700

    Merge branch 'master' into develop

[33mcommit 74858671dbe7fac40b8eb1cfe077088d8a011b80[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Jun 3 03:18:49 2015 -0700

    MIFOSX-2063

[33mcommit 4060ab33a4a91fe84d45eb99878d351e648767b8[m
Merge: 6a11ec2 7ab9f64
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Jun 3 02:38:37 2015 -0700

    Merge branch 'master' into develop

[33mcommit 7ab9f6450f128d644bba6f1cca28b727f6c8662a[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Jun 3 02:36:57 2015 -0700

    Update build.gradle
    
    MIFOSX-2062

[33mcommit 6a11ec28b148473572449cb10295e61614a28b5b[m
Merge: e834b9c af6bd39
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Jun 2 23:28:14 2015 -0700

    Merge pull request #1398 from sharmapankaj2512/MIFOSX-1573
    
    MIFOSX-1573 closedOnDate will be set to null on reactivation

[33mcommit e834b9c92063460af2eac8d3fe8cd0d7868fc8aa[m
Merge: 7926440 eb2d539
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Jun 2 23:26:00 2015 -0700

    Merge pull request #1401 from chandrikamohith/MIFOSX-2054
    
    MIFOSX-2054 Condition added to check if meeting frequency is not atta…

[33mcommit 7926440f3772c3989b651700bf66a8081c4b5355[m
Merge: 944dc53 a3ebe54
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Jun 2 18:46:40 2015 -0700

    Merge pull request #1405 from chandrikamohith/reviewChanges
    
    MIFOSX-1992 Addressed review comments for 1992.

[33mcommit 944dc53b2622439401603c73fe3c03085d3fea3d[m
Merge: e8fa4b3 09d3eb9
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Jun 2 18:44:06 2015 -0700

    Merge pull request #1404 from sughosh88/MIFOSX-1939
    
    MIFOSX-1939 FIX

[33mcommit a3ebe54e308170209ff06f300814bc5770711f4c[m
Author: Chandrika <chandrika@confluxtechnologies.com>
Date:   Tue Jun 2 18:57:04 2015 -0400

    MIFOSX-1992 Addressed review comments for 1992.

[33mcommit 09d3eb9c95be16df6053dbb276fb001f1dbbbae7[m
Author: sughosh88 <sughosh@confluxtechnologies.com>
Date:   Tue Jun 2 16:30:51 2015 +0530

    MIFOSX-1939 FIX

[33mcommit e8fa4b3cb696e6fbc671bcdaab351d23c15466ff[m
Merge: 614df03 2f61752
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Jun 1 17:58:53 2015 -0700

    Merge branch 'chandrikamohith-MIFOSX-1992' into develop

[33mcommit 2f617529d58e4ee567bb5b317540d032800b5004[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Jun 1 17:56:59 2015 -0700

    bumping up account number for groups

[33mcommit 542c81cbba24332105586edb6742e95a1a522168[m
Merge: 8311a5d 9402ab5
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Jun 1 17:56:06 2015 -0700

    Merge branch 'MIFOSX-1992' of https://github.com/chandrikamohith/mifosx into chandrikamohith-MIFOSX-1992

[33mcommit b89daf38a8c49fd6a00d90f662cd1e09c0f2bc9a[m
Author: Chandrika <chandrika@confluxtechnologies.com>
Date:   Mon Jun 1 20:47:22 2015 -0400

    MIFOSX-2049 Able to withdraw from savings account when overdraft limit is not defined in savings product.

[33mcommit 614df03820d1ca4ab62f62aec38eb8901d164308[m
Merge: 8311a5d 1d0892d
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Jun 1 17:36:22 2015 -0700

    Merge pull request #1400 from chandrikamohith/MIFOSX-2053
    
    MIFOSX-2053 Able to add/delete/edit tranches.

[33mcommit eb2d5399370401a98aa3896712b59544c62fda74[m
Author: Chandrika <chandrika@confluxtechnologies.com>
Date:   Mon Jun 1 17:16:01 2015 -0400

    MIFOSX-2054 Condition added to check if meeting frequency is not attached to group/center.

[33mcommit 1d0892d1994c7e4991e72dc236a464a6c3ed1d74[m
Author: Chandrika <chandrika@confluxtechnologies.com>
Date:   Mon Jun 1 14:58:42 2015 -0400

    MIFOSX-2053 Able to add/delete/edit tranches.

[33mcommit 8311a5d1fcc0e0a991b8191a4a64c7354b1614d9[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Jun 1 00:10:20 2015 -0700

    undoing changes made for MIFOSX-2006

[33mcommit 6ccd513236838cb67f5cf3311b56174f71f79a11[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Jun 1 00:04:34 2015 -0700

    undoing changes made for MIFOSX-2006

[33mcommit 1f3f9e0d2c0bc07e4d3cc1a7ab06e82e3f677b9f[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun May 31 18:43:34 2015 -0700

    MIFOSX-2006

[33mcommit af6bd39186e8cfb61eefea97a489978095d60247[m
Author: Pankaj Sharma <pankajsharma@INPankajs.local>
Date:   Fri May 29 21:14:55 2015 +0530

    MIFOSX-1573 closedOnDate will be set to null on reactivation

[33mcommit 30b4d662ed767fd2063168fda2ffedf70fdfc398[m
Merge: 55e3661 8710b79
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri May 29 00:45:18 2015 -0700

    Merge pull request #1392 from sangameshn/patch-3
    
    Update INSTALL.md

[33mcommit 33e72e542d7f72718e30ca974664e8bb344dcc79[m
Merge: a59ebad 66876c0
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri May 29 00:44:16 2015 -0700

    Merge pull request #1396 from sughosh88/MIFOSX-1939-FIX
    
    Modifies reports MIFOSX-1939

[33mcommit 66876c085907ee6f890b27cd631e70796bf8690a[m
Author: sughosh88 <sughosh@confluxtechnologies.com>
Date:   Fri May 29 11:11:31 2015 +0530

    Modifies reports MIFOSX-1939

[33mcommit 9402ab552919a17e07c8972fd4366b0a6e84569b[m
Author: Chandrika <chandrika@confluxtechnologies.com>
Date:   Thu May 28 17:44:50 2015 -0400

    MIFOSX-1992 Introducing account# to groups and centers.Also expand scope to group and center search API.

[33mcommit 72d82e32673c0c69382caafee6e7f38f87b15a66[m
Author: sachinkulkarni12 <sachin.kulkarni@confluxtechnologies.com>
Date:   Thu May 28 14:05:18 2015 +0530

    MIFOSX-2025:Unable to access loan account page

[33mcommit a59ebad2bf8eb0a5090488def25748ade0c33688[m
Merge: ae86a22 9c74145
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed May 27 19:49:44 2015 -0700

    Merge pull request #1393 from sughosh88/MIFOSX-1939
    
    MIFOSX-1939, Modified reports

[33mcommit 9c74145e0a9464722edf9687f137a5e556dedd67[m
Author: sughosh88 <sughosh@confluxtechnologies.com>
Date:   Tue May 26 17:00:42 2015 +0530

    MIFOSX-1939, Modified reports

[33mcommit 8710b7986e1442981cb42cacf484211d5125b3e8[m
Author: sangameshn <sangamesh@confluxtechnologies.com>
Date:   Mon May 25 17:47:04 2015 +0530

    Update INSTALL.md

[33mcommit ae86a229fc7a0e428353292d93f4435f82f10807[m
Merge: a6eb16d 5d713be
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu May 21 23:31:49 2015 -0700

    Merge pull request #1389 from keshav10/MIFOSX-2050
    
    MIFOSX-2050

[33mcommit a6eb16d4a8012951fd617b42b9ac69b554388c33[m
Merge: 67eb567 e89772d
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed May 20 23:12:30 2015 -0700

    Merge pull request #1391 from chandrikamohith/MIFOSX-2047
    
    MIFOSX-2047 Updating Integration test case for hooks.Adding test case…

[33mcommit e89772db7800316fb96f45e0085799abe7bda005[m
Author: Chandrika <chandrika@confluxtechnologies.com>
Date:   Wed May 20 15:55:19 2015 -0400

    MIFOSX-2047 Updating Integration test case for hooks.Adding test case for update and delete hook.

[33mcommit 5d713be0d683c6b22750d00446ff1194b16ae3f3[m
Author: keshav10 <keshav.mishra@confluxtechnologies.com>
Date:   Wed May 20 10:16:57 2015 +0530

    MIFOSX-2050

[33mcommit 67eb567b84d26f5b0d9e32b727ac33ac46ca0961[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue May 19 10:32:11 2015 -0700

    MIFOSX-2047

[33mcommit 55e3661a6f334e34e31acf6c542a411303fde850[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon May 18 17:18:26 2015 -0700

    Update CHANGELOG.md
    
    with details of 15.03.1 Release

[33mcommit 3c483314803e46afd9a49b69d1572c53bd0d8f59[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon May 18 16:27:06 2015 -0700

    updating sample data dump

[33mcommit d7e301c5d4836103242f60e35df36698eb93e43e[m
Merge: acf3c74 298f338
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon May 18 14:55:27 2015 -0700

    Merge pull request #1387 from chandrikamohith/MIFOSX-2037
    
    MIFOSX-2037 Payment type is getting displayed in Individual collectio…

[33mcommit acf3c74f37a781871f5dcfbd1f91298eb5944068[m
Merge: 75f774e abec451
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon May 18 14:53:34 2015 -0700

    Merge pull request #1386 from Musoni/MIFOSX-2046
    
    MIFOSX-2046 fixed overpaid loans not been transferred along when tran…

[33mcommit 298f3384cf1a8717fc16cccb6707d2f2e98f2926[m
Author: Chandrika <chandrika@confluxtechnologies.com>
Date:   Mon May 18 16:26:34 2015 -0400

    MIFOSX-2037 Payment type is getting displayed in Individual collection sheet.

[33mcommit abec451ec2121b14d35a51110661193e422b3ca7[m
Author: andrew-dzak <andrewdzakpasu@musoni.eu>
Date:   Mon May 18 12:25:36 2015 +0200

    MIFOSX-2046 fixed overpaid loans not been transferred along when transfering a client

[33mcommit 75f774e7eefc1bb1716e057e1f429c8521fad848[m
Merge: 26fad11 0567578
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun May 17 22:31:32 2015 -0700

    Merge pull request #1375 from awaleram/MIFOSX-1956
    
    MIFOSX-1956: accounting opening balance with multiple currency

[33mcommit 26fad113d50492163b2ee14898510a57b0cd5710[m
Merge: e692e1b 1230025
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun May 17 22:30:37 2015 -0700

    Merge pull request #1374 from awaleram/MIFOSX-2032
    
    teller-cashier management with multiple  currency

[33mcommit e692e1b58bdb80bb2d347e5c6b206b9a1c58b345[m
Merge: 0061e1d 5c86915
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri May 15 00:07:25 2015 -0700

    Merge pull request #1385 from chandrikamohith/MIFOSX-2043
    
    MIFOSX-2043 Adding new Database column 'key' to password preferences …

[33mcommit 5c8691598e0f08dfb5365ee4fff8715aeb516ea0[m
Author: Chandrika <chandrika@confluxtechnologies.com>
Date:   Thu May 14 15:18:28 2015 -0400

    MIFOSX-2043 Adding new Database column 'key' to password preferences to support localization of description values.

[33mcommit 0061e1dd2e16e46a9e4b714d670ba605ea9a715d[m
Merge: e8cfdcf 4b3e76a
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed May 13 22:37:15 2015 -0700

    Merge pull request #1382 from chandrikamohith/MIFOSX-2022
    
    MIFOSX-2022 To enable force-password reset, the value of no of days s…

[33mcommit 4b3e76afc264535c38644ea380f17c6d9d33b776[m
Author: Chandrika <chandrika@confluxtechnologies.com>
Date:   Wed May 13 20:48:28 2015 -0400

    MIFOSX-2022 To enable force-password reset, the value of no of days should be greater than zero.

[33mcommit e8cfdcf865c78e950ee33b551d68eba4f8840aaa[m
Merge: d7ff069 84b9d5f
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue May 12 13:35:14 2015 -0700

    Merge pull request #1378 from chandrikamohith/MIFOSX-2036
    
    MIFOSX-2036 Refactoring Groups and Centers API - removing clientOptio…

[33mcommit 84b9d5f9f259398fcf9c5e6bd20e5abd8fb8d692[m
Author: Chandrika <chandrika@confluxtechnologies.com>
Date:   Tue May 12 16:31:36 2015 -0400

    MIFOSX-2036 Refactoring Groups and Centers API - removing clientOptions and groupMemberOptions for template true.

[33mcommit d7ff069e1156d81e338a6be221babe947027e0fc[m
Merge: 257d187 efa73ed
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue May 12 10:44:33 2015 -0700

    Merge pull request #1377 from chandrikamohith/ClientApiChanges
    
    MIFOSX-2035 Added new boolean parameter optionsOnly to retrieve all c…

[33mcommit efa73ed009e246b422b2cafc9751a6f4a48b0550[m
Author: Chandrika <chandrika@confluxtechnologies.com>
Date:   Tue May 12 13:40:15 2015 -0400

    MIFOSX-2035 Added new boolean parameter optionsOnly to retrieve all clients.

[33mcommit 05675782f55dc90e9acb8856dadd44d025a473a9[m
Author: awaleram <ram.awale@confuxtechnologies.com>
Date:   Tue May 12 17:36:43 2015 +0530

    MIFOSX-1956: accounting opening balance with multiple currency

[33mcommit 12300255d25f385f5e137f62cfe440e4a9cd80d5[m
Author: awaleram <ram.awale@confuxtechnologies.com>
Date:   Tue May 12 12:50:46 2015 +0530

    teller-cashier management with currency

[33mcommit 257d18798cf20bf46113c7fdf4cb739f89bbdcad[m
Merge: e47cee6 9c32945
Author: Markus Geiß <mgeiss@mifos.org>
Date:   Tue May 12 07:19:40 2015 +0200

    Merge pull request #1373 from mgeiss/command-provider-test
    
    added logging to detect build failure

[33mcommit 9c32945c2c8289e3772a41380575ec1a9689255c[m
Author: Markus Geiss <mgeiss@mifos.org>
Date:   Tue May 12 07:17:17 2015 +0200

    added logging to detect build failure

[33mcommit e47cee6cd96b4a252b5d329bdafd4e6a05c287bb[m
Merge: a4dfa7e 570870b
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon May 11 21:58:21 2015 -0700

    Merge pull request #1372 from chandrikamohith/apiDoc
    
    MIFOSX-2028 Updating API docs for List client/list groups.

[33mcommit 570870bfef1055121ae9b091a77c34ea1200d8df[m
Author: Chandrika <chandrika@confluxtechnologies.com>
Date:   Mon May 11 22:46:12 2015 -0400

    MIFOSX-2028 Updating API docs for List client/list groups.

[33mcommit a4dfa7e8c47328e535a9fe20d4be4f0572bb5825[m
Merge: 37934da a1e58b7
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon May 11 17:11:07 2015 -0700

    Merge pull request #1371 from chandrikamohith/MIFOSX-2028
    
    MIFOSX-2028 Adding query param OrphansOnly to Groups API.

[33mcommit a1e58b7768b3253611ce401e5d9393a8a09c72c6[m
Author: Chandrika <chandrika@confluxtechnologies.com>
Date:   Mon May 11 15:02:21 2015 -0400

    MIFOSX-2028 Adding query param OrphansOnly to Groups API.

[33mcommit 37934dab66216a3fb7385ee0ae07308c6522c5a1[m
Merge: a20bd6f 6beac91
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun May 10 17:29:41 2015 -0700

    Merge branch 'MIFOSX-2004' of https://github.com/venkatconflux/mifosx into venkatconflux-MIFOSX-2004

[33mcommit a20bd6fa7eec2ed085a36559e5fd250bf76173c2[m
Merge: 658fb7e 8b71be5
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu May 7 19:13:13 2015 -0700

    Merge pull request #1369 from chandrikamohith/MIFOSX-2003
    
    MIFOSX-2003 Adding new annotation @commandtype to all command handler…

[33mcommit 8b71be5106c6728d355331b04dca60ffa71d49f8[m
Author: Chandrika <chandrika@confluxtechnologies.com>
Date:   Thu May 7 10:36:26 2015 -0400

    MIFOSX-2003 Adding new annotation @commandtype to all command handlers and refactoring code in SynchronousCommandProcessingService and CommandWrapper.

[33mcommit 658fb7ea82ff8812778a60554ad8ba546fcb2611[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed May 6 02:29:40 2015 -0700

    fixing issues with MIFOSX-2002

[33mcommit 6b8bae57f92d9d7763adf562961682187747ad7a[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue May 5 22:49:29 2015 -0700

    reverting changes breaking the build

[33mcommit 0cf447736e7f843cde40233fbdf64921c76bff18[m
Merge: 0ee38cf f84532d
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue May 5 22:33:28 2015 -0700

    Merge pull request #1367 from keshav10/MIFOSX-2002
    
    MIFOSX-2002: allow search client by mobile_no

[33mcommit 0ee38cfbc82fba241ef439e68125f104e4ddec35[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue May 5 07:12:10 2015 -0700

    removing warnings

[33mcommit e4d231913be8f096a4aa3abe4c8dc427da323890[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sat May 2 12:21:52 2015 -0700

    MIFOSX-2007

[33mcommit f84532d88c598143458492674dbf1e3d67499ef0[m
Author: keshav10 <keshav.mishra@confluxtechnologies.com>
Date:   Wed Apr 29 14:40:24 2015 +0530

    MIFOSX-2002

[33mcommit 6beac91da7a9edddb2dfed70599b738f5c059a42[m
Author: venkatconflux <venkata.conflux@confluxtechnologies.com>
Date:   Wed Apr 29 13:44:52 2015 +0530

    MIFOSX-2004:changed to get the loan id with the type of association

[33mcommit d0f7c8b686fe1e07b618cedc41f1ddb78af00ec1[m
Merge: 212e212 514e174
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Apr 27 08:21:44 2015 -0700

    Merge pull request #1364 from pramodn02/develop
    
    MIFOSX-1831 : fix for accrual accounting overpayment

[33mcommit 514e17494df0226b4f6f395b0f6cf50ca6e4eb58[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Mon Apr 27 17:14:00 2015 +0530

    MIFOSX-1831 : fix for accrual accounting overpayment

[33mcommit 212e2127c551cba24ee74b99cca0f73bd5702459[m
Merge: 86d12b5 06c5700
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Apr 26 19:57:20 2015 -0700

    Merge pull request #1363 from mgeiss/command-provider
    
    Command provider

[33mcommit 06c57000c7fd3408d2974d38f7f61f4f602547ae[m
Author: Markus Geiss <mgeiss@mifos.org>
Date:   Sun Apr 26 17:08:07 2015 +0200

    Added autodiscovery for command handlers.

[33mcommit a9ad69f84431e8f2254cc03726675eb6ce41839c[m
Author: Markus Geiss <mgeiss@mifos.org>
Date:   Sun Apr 26 16:17:44 2015 +0200

    Added provider to register and retrieve command handlers.

[33mcommit 86d12b57a84594db693d0fbaf3616b788eeb5692[m
Merge: d33f762 82d3d69
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Apr 24 11:21:49 2015 -0700

    Merge branch 'MIFOSX-1891' of https://github.com/chandrikamohith/mifosx into chandrikamohith-MIFOSX-1891

[33mcommit d33f762350e45d3137527b360361fac1d7e692bd[m
Merge: 577f59a 356ec7b
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Apr 24 08:26:15 2015 -0700

    Merge pull request #1359 from pramodn02/develop
    
    interest recalculation issues fixes and compounding changes

[33mcommit 356ec7bb5937839bd64e9684eabf1d45a3498658[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Fri Apr 24 14:29:40 2015 +0530

    MIFOSX-1995 : fix for prepay of loan with moratorium

[33mcommit ff84a13c4c71a62be05fe4f325e3c55cce6da180[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Wed Apr 22 20:32:02 2015 +0530

    MIFOSX-1951 : compounding changes and recalculation fixes

[33mcommit 577f59a7a9d8baeebc7bf065594b7c07460526a9[m
Merge: 3583fab ebfe13f
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Apr 23 12:47:23 2015 -0700

    Merge pull request #1358 from chandrikamohith/MIFOSX-1988
    
    MIFOSX-1988 Total Disburse amount is updated based on the tranche amount...

[33mcommit ebfe13f9c2a9becd3416026610f0173b4f427850[m
Author: Chandrika <chandrika@confluxtechnologies.com>
Date:   Thu Apr 23 15:37:27 2015 -0400

    MIFOSX-1988 Total Disburse amount is updated based on the tranche amounts disbursed.

[33mcommit 82d3d692d72753e8ee4c311654bbf1ca3027efca[m
Author: Chandrika <chandrika@confluxtechnologies.com>
Date:   Tue Apr 21 17:01:39 2015 -0400

    MIFOSX-1891 Improvements to extend term for loans following daily repayment schedule.

[33mcommit 3583fabe7a7bb96f7a3b3447fa9e46fc24d1489a[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Apr 17 22:04:13 2015 -0700

    updating release in develop branch

[33mcommit 5b58bf84bce63b5706872cd27846c8e28e466951[m
Merge: ed754fc db2934d
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Apr 16 22:21:11 2015 -0700

    Merge pull request #1353 from sangameshn/patch-2
    
    Update INSTALL.md

[33mcommit db2934d905dbaf8dc8d02906c98e4daaaffe5a4e[m
Author: sangameshn <sangamesh@confluxtechnologies.com>
Date:   Fri Apr 17 10:43:00 2015 +0530

    Update INSTALL.md

[33mcommit ed754fc964c7af941d13ce42557f27d9eda8e107[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Apr 16 18:09:46 2015 -0700

    Update INSTALL.md

[33mcommit 38b9ef2b43a6493084632be0f9ed4ba04b37d28c[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Apr 16 18:08:40 2015 -0700

    Update INSTALL.md
    
    fixing type

[33mcommit 4ae07dd47c281bb955ae5df0a3b14d1f4b68e1a2[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Apr 16 18:08:05 2015 -0700

    Update INSTALL.md
    
    Automatic setup is broken

[33mcommit 511303d9e50b5c52cee60d5b5661645887ab0a78[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Apr 16 04:34:15 2015 -0700

    update instructions to access mifosx from the browser

[33mcommit 7934e54b22fa2b0c6a3f9d28187a60395c831580[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Apr 16 04:16:27 2015 -0700

    updating sample data

[33mcommit 8dd47769d1291d6ef0796c968885a6c09d30dfc6[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Apr 16 04:09:32 2015 -0700

    fixing issues with executable war file

[33mcommit 3b304f8c08204d405082ec11c21a419eb6d07a68[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Apr 16 03:49:14 2015 -0700

    updates for gl report

[33mcommit 158e048e87cd0af7683ecba26c16ab6d36f0afaa[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Apr 16 02:46:05 2015 -0700

    update sample data for 15.03 release

[33mcommit 1e7fc9d8c3772401eca121a748a91f2ea7d65c90[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Apr 16 02:07:00 2015 -0700

    Update CHANGELOG.md
    
    with release details for 15.03 release

[33mcommit 05ae42b20bc679b13a61614aa8fa3d829a525d0d[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Apr 15 22:22:58 2015 -0700

    bumping up release number

[33mcommit 7e2c33d05d5b9f9ec0d4205687be12bf0059e48c[m
Merge: 5f888ca d45b3a5
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Apr 14 14:05:19 2015 -0700

    Merge pull request #1351 from chandrikamohith/tranche
    
    MIFOSX-1842 Adding validation for add/delete tranche based on loan statu...

[33mcommit d45b3a504e997b1fcdb6dbda5d5be761e3fc0594[m
Author: Chandrika <chandrika@confluxtechnologies.com>
Date:   Tue Apr 14 13:44:50 2015 -0400

    MIFOSX-1842 Adding validation for add/delete tranche based on loan status.

[33mcommit 5f888ca2f2061d15affb699c0ff9234c4c6cbc8c[m
Merge: 6da6ec7 19e568e
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Apr 13 15:36:01 2015 -0700

    Merge pull request #1348 from sachinkulkarni12/collection
    
    fix for saving payment details in collection sheet

[33mcommit 6da6ec799cac6690596f4a3ddbe4b4868e1dcf8c[m
Merge: b393b24 445f57f
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Apr 13 14:32:28 2015 -0700

    Merge pull request #1350 from chandrikamohith/exception
    
    MIFOSX-1842 Fix for TransientPropertyValueException during Loan Approval...

[33mcommit 445f57f81df283f3c1440b814bd0480c35efd9d2[m
Author: Chandrika <chandrika@confluxtechnologies.com>
Date:   Mon Apr 13 16:15:03 2015 -0400

    MIFOSX-1842 Fix for TransientPropertyValueException during Loan Approval-when the approved amount was lesser than loan demanded.

[33mcommit b393b24c2d04eaa3d0fbf82f13a9b840e544b2bd[m
Merge: f4ed5f8 006b14d
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Apr 13 12:21:25 2015 -0700

    Merge pull request #1349 from chandrikamohith/1842
    
    MIFOSX-1842 validations added for add/delete tranches.

[33mcommit 006b14db0aa6b08029e3432bbf095edeefd75620[m
Author: Chandrika <chandrika@confluxtechnologies.com>
Date:   Mon Apr 13 14:15:01 2015 -0400

    MIFOSX-1842 validations added for add/delete tranches.

[33mcommit 19e568e8f9c9037ff3f66bc14826c3f503e70100[m
Author: sachinkulkarni12 <sachin.kulkarni@confluxtechnologies.com>
Date:   Mon Apr 13 18:38:49 2015 +0530

    fix for saving payment details in collection sheet

[33mcommit f4ed5f80ddfbad5a84489ef903ecf7c45fb7ea7e[m
Merge: 29de3ed 864b18d
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Apr 13 01:45:33 2015 -0700

    Merge pull request #1347 from rajeshreddy-k/MIFOSX-1958
    
    MIFOSX-1958:Displaying Payment details in Search Journal entry screen

[33mcommit 864b18d1a06a260dae781634bc067df3219dba40[m
Author: rajeshreddy-k <rajesh.reddy@confluxtechnologies.com>
Date:   Mon Apr 13 13:57:52 2015 +0530

    MIFOSX-1958:Displaying Payment details in Search Journal entry screen

[33mcommit 29de3ed7452588ee9317cf3149a3ca00a6cda01d[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Apr 12 00:54:36 2015 -0700

    fixing failing test cases

[33mcommit 2537b63183a69355ca0ef40526875f069604a834[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sat Apr 11 09:20:48 2015 -0700

    some timezone related fixes

[33mcommit 18529f0ea6cdda4a2bceae869547c9ef6f3ad565[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sat Apr 11 01:35:07 2015 -0700

    fix failing test cases

[33mcommit f8831580018fb0495201e94537703a43f05280d3[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Apr 10 23:57:36 2015 -0700

    cleaning up MIFOSX-1947

[33mcommit 72be7a82a5499723aae2fd10b8d00548db8304d0[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Apr 10 17:13:05 2015 -0700

    adding foreign key relationship and bumping up sql patch

[33mcommit edcfce422ab0a5267aba75ecdc59184856dc6d63[m
Merge: 36aea53 e11236d
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Apr 10 17:05:12 2015 -0700

    Merge branch 'MIFOSX-1947' of https://github.com/florinvlad/mifosx into florinvlad-MIFOSX-1947

[33mcommit 36aea53215deef0a7721c065dd0891d69a1211f6[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Apr 10 17:04:11 2015 -0700

    MIFOSX-1938

[33mcommit bab630a24e871e980b968c774ce9ec8d9b6b716d[m
Merge: f75d2a3 3561ff1
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Apr 10 16:59:34 2015 -0700

    Merge branch 'MIFOSX-1938_NEW' of https://github.com/prasadpatill2/mifosx into prasadpatill2-MIFOSX-1938_NEW

[33mcommit e11236dc2b131a4dffdbaf960b382f2e5b493abf[m
Author: Florin Vlad <florin.vlad01@gmail.com>
Date:   Fri Apr 10 12:21:26 2015 +0300

    fix for MIFOSX-1947
    
    change file name
    
    update fix
    
    added sql
    
    added tests for image api
    
    remove comment
    
    api doc
    
    more tests

[33mcommit f75d2a3b8130f3781ff461987549400762258870[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Apr 9 17:06:41 2015 -0700

    adding logs for batch job failures in Integration test cases

[33mcommit aa915a122b627d0500c00c0cfa787b257000e4d4[m
Merge: 8c03f6e 8e7ae0a
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Apr 9 16:07:21 2015 -0700

    Merge pull request #1340 from chandrikamohith/1256
    
    MIFOSX-1256 Charges dropdown to display only matching currency for savin...

[33mcommit 8c03f6e41eb1ee66ab001fd09e12e7d22558b7c3[m
Merge: ce576f1 b0173fa
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Apr 9 15:23:03 2015 -0700

    Merge pull request #1344 from chandrikamohith/MIFOSX-1954
    
    MIFOSX-1954 End date is validated if it is before startDate.Validation f...

[33mcommit b0173fa46a011af177ca0cc65b2ed05711a1d2bb[m
Author: Chandrika <chandrika@confluxtechnologies.com>
Date:   Thu Apr 9 11:30:59 2015 -0400

    MIFOSX-1954 End date is validated if it is before startDate.Validation function merged for Create/Update teller.

[33mcommit ce576f10be7554e62164ae4e4304ac389bf22922[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Apr 8 21:17:22 2015 -0700

    Update VERSIONING.md
    
    with edits around Date versioning scheme

[33mcommit 1225a72ceeae3f918d9186b64545ced4a1c6edc2[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Apr 8 21:10:12 2015 -0700

    Update VERSIONING.md
    
    with details of date versioning scheme

[33mcommit f790b7e695319db6d547d8988aed6d92c0eddb6c[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Apr 8 16:01:54 2015 -0700

    fixing validation for loanproduct data validation

[33mcommit ee71366032ef44ad31cb1e391ef97ca392f50bad[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Apr 8 15:17:36 2015 -0700

    reduce number of server side calls in testLoanProductConfiguration

[33mcommit 3561ff1706645c383ae77218c848b91505b21b27[m
Author: prasadpatill <prasad@confluxtechnologies.com>
Date:   Wed Apr 8 13:50:09 2015 +0530

    update script for General_Ledger_report

[33mcommit 69150fc7ad2fa5c7ea78782b92f44d281ea37a2d[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Apr 7 18:06:10 2015 -0700

    fixes for loan refund test cases

[33mcommit e6e0af78855e09eea0ba57d2c2d1c973e1ef0891[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Apr 7 17:11:46 2015 -0700

    fix for failing test case testSavingsInterestPostingAtPeriodEnd

[33mcommit 7a8702e16561cfd873bb1c062300faa469b75bf0[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Apr 7 16:59:01 2015 -0700

    more fixes for failing integration tests w.r.t Interest recalculation

[33mcommit 6b67e39b726f7acbefcf650a0f75b8d71f6626be[m
Merge: 7cf3e05 741d683
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Apr 7 16:15:21 2015 -0700

    Merge pull request #1337 from emmanuelnnaa/MIFOSX-1941
    
    commit for MIFOSX-1941 (Incorrect GL Account running balance figures)

[33mcommit 7cf3e05fdccaf8381e82d5eaae139a30f73a16a2[m
Merge: d665ff5 e992a2b
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Apr 7 16:12:29 2015 -0700

    Merge pull request #1339 from pramodn02/develop
    
    MIFOSX-1918 : API changes to add option for  fetching pre-close detail b...

[33mcommit d665ff5a0017d76803e7c18dfb3e206f4da08997[m
Merge: 4b110cf 2b17dba
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Apr 7 16:10:48 2015 -0700

    Merge pull request #1341 from chandrikamohith/1877
    
    MIFOSX-1877 NumberFormatException issue fixed for working days

[33mcommit 4b110cfe4b916a25a192e3efbeeb96f255a5e61c[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Apr 7 16:06:31 2015 -0700

    MIFOSX-1948

[33mcommit 2b17dba8cd67d783632a0dc70222a9f46694a5db[m
Author: Chandrika <chandrika@confluxtechnologies.com>
Date:   Tue Apr 7 17:52:44 2015 -0400

    MIFOSX-1877 NumberFormatException issue fixed for working days

[33mcommit 48bf33c930306a2f513d3f6cddf4a4fcf176e0cd[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Apr 7 11:27:54 2015 -0700

    Update CHANGELOG.md
    
    with release description for 15.03

[33mcommit ad8d9e792fd868fb298e8d883b370944770f8869[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Apr 7 10:59:47 2015 -0700

    Update CHANGELOG.md

[33mcommit a5a4d1670d1359d0b1585532d4e06b37bc0d4f88[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Apr 7 10:33:30 2015 -0700

    fixing failing test cases on payment types

[33mcommit 15fe1477b7f5e741d9f39898016b37cd1aeb3996[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Apr 7 09:53:07 2015 -0700

    adding logs to detect failures in test cases

[33mcommit 8e7ae0ac03fc01bf116db7767eb734392964ef05[m
Author: Chandrika <chandrika@confluxtechnologies.com>
Date:   Tue Apr 7 11:25:55 2015 -0400

    MIFOSX-1256 Charges dropdown to display only matching currency for savings/FD/RD.

[33mcommit e992a2b4102e4e5daeec3b148a8570833c2a1f77[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Tue Apr 7 20:20:30 2015 +0530

    MIFOSX-1918 : API changes to add option for  fetching pre-close detail based on date

[33mcommit 741d683af91c4d372e33b19cedbf7deb6a965e06[m
Author: Emmanuel Nnaa <emmanuelnnaa@musoni.eu>
Date:   Tue Apr 7 09:28:44 2015 +0200

    commit for MIFOSX-1941 (Incorrect GL Account running balance figures)

[33mcommit b1a429e3a25ac73865dad9c48b36c2b38376cbf0[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Apr 6 22:50:05 2015 -0700

    Update CHANGELOG.md
    
    for 15.03.RELEASE

[33mcommit 8fc3ace96e00c8ab13c8665b04ad43f71057ecea[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Apr 6 18:59:09 2015 -0700

    MIFOSX-1908

[33mcommit ade5e76ad9183aa4e5b331e7a148e394a4d198ca[m
Merge: 0ab2e72 d213857
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Apr 6 17:11:19 2015 -0700

    Merge branch 'loanConfig' of https://github.com/chandrikamohith/mifosx into chandrikamohith-loanConfig
    
    Conflicts:
    	mifosng-provider/src/main/java/org/mifosplatform/portfolio/loanproduct/domain/LoanProduct.java
    	mifosng-provider/src/main/java/org/mifosplatform/portfolio/loanproduct/serialization/LoanProductDataValidator.java

[33mcommit 0ab2e727ad56bccb7de5c0efd80d852e242c022b[m
Merge: 2ab024e b6f9ec6
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Apr 6 16:23:41 2015 -0700

    Merge pull request #1336 from chandrikamohith/MIFOSX-1742
    
    MIFOSX-1742 Not displaying loan records which are not Active.

[33mcommit b6f9ec6bcbc505c5c6d569116be0b665795af45d[m
Author: Chandrika <chandrika@confluxtechnologies.com>
Date:   Mon Apr 6 19:16:46 2015 -0400

    MIFOSX-1742 Not displaying loan records which are not Active.

[33mcommit 2ab024e6f7df032bfadf510f5f2b61ec86f06239[m
Merge: 2a1fed6 ea8080f
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Apr 6 10:00:15 2015 -0700

    resolving merge issues with api-docs

[33mcommit 2a1fed6fa11b9c638df50ec18cd65be6502fc940[m
Merge: 3abee1c 312446e
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Apr 5 21:15:59 2015 -0700

    Merge pull request #1335 from pramodn02/develop
    
    added extra logs in test cases

[33mcommit 312446e8ed9260f286dad15d5d3afd4af6bd1fc1[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Mon Apr 6 08:02:07 2015 +0530

    added extra logs in test cases

[33mcommit 3abee1cf403e020ece79985cdf011deac30daa40[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Apr 5 14:05:39 2015 -0700

    MIFOSX-1918 API docs typos

[33mcommit 91f21707abac1337e49654a4354407308ae4812d[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Apr 5 13:53:43 2015 -0700

    MIFOSX-1918

[33mcommit 52b82f32d0326477cc653aec2fe08c29fff3b453[m
Merge: 4224fc1 2bfc837
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Apr 5 13:18:32 2015 -0700

    Merge pull request #1334 from pramodn02/develop
    
     MIFOSX-1918 : added Pre-closure interest calculation strategy

[33mcommit 2bfc83722fd8ba394fd66558bd05a28281a8e4fe[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Sun Apr 5 23:27:18 2015 +0530

     MIFOSX-1918 : added Pre-closure interest calculation strategy

[33mcommit 4224fc16cc3a4a9441982d58a5cbd0954ac9fec9[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sat Apr 4 20:35:45 2015 -0700

    remove server startup warnings

[33mcommit dbb1c18dd4960e918ccc3905bffeb653cd21510b[m
Merge: ae36b0a fa5d388
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sat Apr 4 01:37:34 2015 -0700

    Merge pull request #1333 from chandrikamohith/1939
    
    MIFOSX-1939 Correcting the savings statusEnum value for TRANSFER_ON_HOLD...

[33mcommit fa5d3880843d216b4371bef0a4e6562b1f3fcc47[m
Author: Chandrika <chandrika@confluxtechnologies.com>
Date:   Fri Apr 3 17:36:23 2015 -0400

    MIFOSX-1939 Correcting the savings statusEnum value for TRANSFER_ON_HOLD.

[33mcommit ae36b0ad470cae6dfe8c77fee0194aa559609634[m
Merge: c3dc53a 3038169
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Apr 2 07:38:23 2015 -0700

    Merge pull request #1330 from emmanuelnnaa/MIFOSX-1940
    
    commit for MIFOSX-1940 (Accounting running balance job hangs when updating 400,000 or more entries)

[33mcommit 3038169bb80816c89b5160ce36b984686e600f0f[m
Author: Emmanuel Nnaa <emmanuelnnaa@musoni.eu>
Date:   Thu Apr 2 16:10:24 2015 +0200

    commit for MIFOSX-1940 (Accounting running balance job hangs when updating 400,000 or more entries)

[33mcommit c3dc53a4b1ca1384921fbed2bbbfb5014239c160[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Apr 1 19:46:44 2015 -0700

    remove warnings from portfolio packages

[33mcommit d21385792af68e050e9f3ecc66ada881b3352eaa[m
Author: Chandrika <chandrika@confluxtechnologies.com>
Date:   Wed Apr 1 15:27:46 2015 -0400

    MIFOSX-1667 Overriding loan product attributes by individual loan accounts.

[33mcommit b46e9b66c94b709116ec74aa0cdb66b14eae10e5[m
Merge: 377dee1 8416cd1
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Apr 1 08:47:37 2015 -0700

    Merge pull request #1327 from prasadpatill2/generalreport
    
    GeneralLedgerReport

[33mcommit 8416cd1ec7888cd768f35f87c2130c40ae8a6ce5[m
Author: prasadpatill <prasad@confluxtechnologies.com>
Date:   Wed Apr 1 16:38:04 2015 +0530

    GeneralLedgerReport

[33mcommit 377dee1984fec4f3a5278cc7db7f6b5ff1dd783d[m
Author: Vishwas Babu A J <vishwas@confluxtechnologes.com>
Date:   Tue Mar 31 13:52:17 2015 -0700

    removing warnings on portfolio packages

[33mcommit 49dc20d2a36113be672b99d57534fb5782d5e9ea[m
Merge: ecaa78f cf0f544
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Mar 31 09:58:50 2015 -0700

    Merge pull request #1326 from chandrikamohith/passwordPref
    
    MIFOSX-1875 Adding test cases and API docs for Password Preferences.

[33mcommit cf0f5440a9a4ff076092bef296950e5dfe388569[m
Author: Chandrika <chandrika@confluxtechnologies.com>
Date:   Tue Mar 31 12:17:15 2015 -0400

    MIFOSX-1875 Adding test cases and API docs for Password Preferences.

[33mcommit ea8080f32c039c0bd896028ffc2ae1f8940953a9[m
Author: MithunKashyap <mithunkashyap0206@gmail.com>
Date:   Mon Mar 30 10:53:41 2015 +0530

    MIFOSX : Api docs for Teller cash mgmt and paymenttypes

[33mcommit ecaa78f6b52578826aa0ce1730a5bc391aa144ee[m
Merge: a72f105 60d38fb
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sat Mar 28 16:03:38 2015 +0530

    Merge pull request #1323 from prasadpatill2/LoanandSavingsAccountStatement
    
    LoanandSavingsAccountStatementReports

[33mcommit 60d38fbb8fcde097752c02955ce3fd13d780aec8[m
Author: prasadpatill <prasad@confluxtechnologies.com>
Date:   Sat Mar 28 13:21:31 2015 +0530

    LoanandSavingsAccountStatement

[33mcommit a72f1057cd003cbed6b69ef9aa2c9388149c5136[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sat Mar 28 11:27:16 2015 +0530

    bumping up sql patch number

[33mcommit 30e9e3b33040669b3f9d524b09e984b94e8ed4ec[m
Merge: edef7db 3cb9932
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sat Mar 28 11:25:26 2015 +0530

    Merge branch 'MIFOSX-1892' of git://github.com/MithunKashyap/mifosx into MithunKashyap-MIFOSX-1892

[33mcommit edef7dbf93a821df9fa6b13f4842408673cae864[m
Merge: cc2fdbb c73e38f
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sat Mar 28 11:23:09 2015 +0530

    Merge branch 'MIFOSX-1904' of git://github.com/MithunKashyap/mifosx into MithunKashyap-MIFOSX-1904
    
    Conflicts:
    	mifosng-provider/src/main/java/org/mifosplatform/commands/domain/CommandWrapper.java
    	mifosng-provider/src/main/java/org/mifosplatform/commands/service/CommandWrapperBuilder.java
    	mifosng-provider/src/main/java/org/mifosplatform/commands/service/SynchronousCommandProcessingService.java

[33mcommit cc2fdbbacc65af8a3fd0e7d9e33fd19661547449[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sat Mar 28 10:44:36 2015 +0530

    updating licences

[33mcommit 237b96753773ab350d831607ad78084631539083[m
Merge: aaaf47c f9a33c7
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sat Mar 28 10:42:12 2015 +0530

    Merge pull request #1321 from chandrikamohith/MIFOSX-1875
    
    MIFOSX-1875 Fix for NullPointerException,update build attributes before ...

[33mcommit aaaf47c763cb18d61f2d57c27e3295967335ea76[m
Merge: 3043d2a 94af5d0
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sat Mar 28 10:41:30 2015 +0530

    Merge pull request #1322 from chandrikamohith/MIFOSX-1868
    
    MIFOSX-1868 Spelling mistake corrected for the field 'passwordNeverExpir...

[33mcommit 94af5d085378c388b8e7d410ae53bfa387f6f409[m
Author: Chandrika <chandrika@confluxtechnologies.com>
Date:   Fri Mar 27 20:59:14 2015 -0400

    MIFOSX-1868 Spelling mistake corrected for the field 'passwordNeverExpires' during Update.

[33mcommit f9a33c790e9d9a32ef257d93e3e5d6449f9f0133[m
Author: Chandrika <chandrika@confluxtechnologies.com>
Date:   Fri Mar 27 17:43:20 2015 -0400

    MIFOSX-1875 Fix for NullPointerException,update build attributes before building commandRequest.

[33mcommit 3043d2ae992b382e31521953308fce8830af6288[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sat Mar 28 00:09:40 2015 +0530

    fix failing test cases

[33mcommit 064fb4845cdd8b126943064c7df9512edfdebb9b[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sat Mar 28 00:07:20 2015 +0530

    MIFOSX-1875

[33mcommit 3416a8dc8e15a3a4e05b5e8f9d088dfeb9e6174e[m
Merge: cc010e5 d89152c
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Mar 27 01:14:59 2015 +0530

    Merge branch 'MIFOSX-1875' of git://github.com/Musoni/mifosx into Musoni-MIFOSX-1875
    
    Conflicts:
    	mifosng-provider/src/main/java/org/mifosplatform/commands/domain/CommandWrapper.java
    	mifosng-provider/src/main/java/org/mifosplatform/commands/service/CommandWrapperBuilder.java
    	mifosng-provider/src/main/java/org/mifosplatform/commands/service/SynchronousCommandProcessingService.java

[33mcommit cc010e5b37bd28a1fc2ead60bb356d116eaff1f8[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Mar 26 22:38:17 2015 +0530

    cleaning up -MIFOSX-1877

[33mcommit 3b71c9077301b81458363faeb4bfb658ad60db7f[m
Merge: a687c49 fdd8409
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Mar 26 21:34:15 2015 +0530

    Merge branch 'MIFOSX-1877' of git://github.com/Musoni/mifosx into Musoni-MIFOSX-1877

[33mcommit a687c49eb16c5f799adc38a98659de0130134874[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Mar 26 18:29:56 2015 +0530

    cleaning up MIFOSX-1897

[33mcommit 392e94f6bf6163f091cfb3cf3d38c3854a0be380[m
Merge: 1098923 a38d3d5
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Mar 26 18:06:52 2015 +0530

    Merge branch 'MIFOSX-1897' of git://github.com/Musoni/mifosx into Musoni-MIFOSX-1897

[33mcommit 1098923689cb5711920281ed14a1c721d3a88598[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Mar 26 16:57:32 2015 +0530

    cleaning up MIFOSX-1868

[33mcommit 9a55b3ffa84f118db9e33e2f1dd11e393aa5a560[m
Merge: 5624da8 9e6ec11
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Mar 26 15:53:22 2015 +0530

    Merge branch 'MIFOSX-1868' of git://github.com/Musoni/mifosx into Musoni-MIFOSX-1868

[33mcommit 3cb99328eb36fb9f4225d37995ee5120d67502e5[m
Author: MithunKashyap <mithunkashyap0206@gmail.com>
Date:   Wed Mar 25 22:09:24 2015 +0530

    MIFOSX-1892:Bug fix for Teller cash management

[33mcommit c73e38fc8c030ea35574c96891b0535c9dbbc509[m
Author: MithunKashyap <mithunkashyap0206@gmail.com>
Date:   Wed Mar 25 21:43:31 2015 +0530

    MIFOSX-1904 : Moving payment type to their own table

[33mcommit 5624da89ea93bb7b5e139414d08c489584fa5ba0[m
Merge: 0fabff8 1db9345
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Mar 23 17:51:34 2015 +0530

    Merge pull request #1316 from sachinkulkarni12/MIFOSX-1839
    
    MIFOSX-1839:fix for show payment details

[33mcommit 0fabff864cb5a85a9c13115816474d06ed9acc25[m
Merge: 5227b48 2833318
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Mar 23 17:47:21 2015 +0530

    Merge pull request #1313 from MithunKashyap/MIFOSX-1905
    
    MIFOSX-1905: Consistency w.r.t spelling principalThresholdForLastInstalment for loan products

[33mcommit 5227b48e236012cfb5d7d954f92bc4db75614c2a[m
Merge: 03a5bb5 b8930cb
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Mar 23 17:28:10 2015 +0530

    Merge pull request #1315 from sachinkulkarni12/MIFOSX-1885_client_dissasociate
    
    MIFOSX-1885:fix for client dissasociate

[33mcommit 1db93456699567409e9dbe584593fd1fe5dfbaca[m
Author: sachinkulkarni12 <sachin.kulkarni@confluxtechnologies.com>
Date:   Mon Mar 23 15:47:50 2015 +0530

    MIFOSX-1839:fix for show payment details

[33mcommit b8930cb83134049a8bbc12a02ff17482d6607a2f[m
Author: sachinkulkarni12 <sachin.kulkarni@confluxtechnologies.com>
Date:   Mon Mar 23 15:40:17 2015 +0530

    MIFOSX-1885:fix for client dissasociate

[33mcommit 03a5bb57cea5716a640d7f83c19afae8a02b4a8c[m
Merge: d91c6a0 cb15104
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Mar 23 14:55:55 2015 +0530

    Merge pull request #1312 from pramodn02/develop
    
    MIFOSX-1884 : corrected test cases

[33mcommit 2833318fd386b484a49bc9ecb76da25792788054[m
Author: MithunKashyap <mithunkashyap0206@gmail.com>
Date:   Mon Mar 23 13:25:55 2015 +0530

    MIFOSX-1905: Consistency w.r.t spelling principalThresholdForLastInstalment for loan products

[33mcommit cb1510463d247d029c4929bc134ef3e28e1b5c6a[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Mon Mar 23 11:25:56 2015 +0530

    MIFOSX-1884 : corrected test cases

[33mcommit d91c6a0b648305bb0ad12a8eb765bb212a2e1d29[m
Merge: 259a9c4 c923cc2
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Mar 20 19:16:08 2015 +0530

    Merge pull request #1309 from pramodn02/develop
    
    MIFOSX-1884 : corrected principal moratorium

[33mcommit c923cc2723197aacef43ca8627bb29f0665348a3[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Fri Mar 20 12:13:08 2015 +0530

    MIFOSX-1884 : corrected principal moratorium

[33mcommit 259a9c43c4d3fd26d07630c0ac93e38c7cf1f51c[m
Merge: ef43bbc 1a50bd0
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Mar 20 10:13:56 2015 +0530

    Merge branch 'develop' of github.com:openMF/mifosx into develop

[33mcommit ef43bbc5b10745917c497a9a1eccfed4f289cd7f[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Mar 19 21:58:59 2015 +0530

    fixing compilation issues

[33mcommit 1a50bd0d4742d817db8791930a6049cb463ab509[m
Merge: a6e7642 0710ef9
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Mar 19 21:30:30 2015 +0530

    Merge pull request #1308 from venkatconflux/MIFOSX-1911
    
    MIFOSX-1911:Deposit Account On Hold Transactions

[33mcommit 0710ef964ba54a938d86e1e67d9a97734ec9ef69[m
Author: venkatconflux <venkata.conflux@confluxtechnologies.com>
Date:   Thu Mar 19 21:27:45 2015 +0530

    MIFOSX-1911:CodeFormated

[33mcommit a6e764279b704060741e3300caad5d64f9315d85[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Mar 19 21:24:58 2015 +0530

    MIFOSX-1914

[33mcommit c5e4f22826453d201f2e9bee07033eeca9ad6fca[m
Merge: 89377a2 968ddcd
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Mar 19 17:09:56 2015 +0530

    Merge pull request #1296 from terencemo/release-script
    
    Release script to set version and releasedate

[33mcommit 89377a23b4332352546c6192c55c0c216e999cd0[m
Author: sachinkulkarni12 <sachin.kulkarni@confluxtechnologies.com>
Date:   Thu Mar 19 16:01:55 2015 +0530

    MIFOSX-1885
    
    Conflicts:
    	mifosng-provider/src/main/java/org/mifosplatform/portfolio/group/service/GroupingTypesWritePlatformServiceJpaRepositoryImpl.java

[33mcommit 94dff21a973c669b7320bc966e1b7eb37b4e59a2[m
Merge: c8391d1 7f95e4b
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Mar 19 15:46:12 2015 +0530

    Merge pull request #1297 from chandrikamohith/1808
    
    MIFOSX-1808 Group hierarchy updation issue corrected.Center summary is n...

[33mcommit c8391d119fb5adb77f42d6c130ae5c7868fc4699[m
Merge: 44e08dd 9813e09
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Mar 19 13:46:31 2015 +0530

    Merge pull request #1303 from pramodn02/develop
    
    overdue charge lock issue fix

[33mcommit 9813e097c3cf22a8c19350fd363a5b6a98b9cdd8[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Wed Mar 18 14:43:20 2015 +0530

    overdue charge lock issue fix

[33mcommit 44e08dd2e6297f73b22de18da97fc1b5a66a727d[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Mar 19 11:52:27 2015 +0530

    Update Contributing.md

[33mcommit 9f92e5cb7e188210979743679d51207026623404[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Mar 19 11:52:04 2015 +0530

    Update Contributing.md

[33mcommit 4c5d1947660de6cefda42a1b3313392cd2929d32[m
Merge: 48ee0c9 0d1439a
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Mar 18 17:25:13 2015 +0530

    Merge pull request #1299 from pramodn02/develop
    
    schedule correction for payments after maturity date

[33mcommit 0d1439afea61a959dd5b89beb36b8fd834b0f661[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Tue Mar 17 17:14:53 2015 +0530

    schedule correction for payments after maturity date

[33mcommit 48ee0c97392f41b88808e1f58b2d07f4c85151ed[m
Merge: 7fcdb77 ac75cc9
Author: Michael Vorburger <mike@vorburger.ch>
Date:   Tue Mar 17 23:31:25 2015 +0100

    Merge pull request #1291 from vorburger/MIFOSX-1758_apps_git_submodule
    
    MIFOSX-1758 Make dist ZIP include community-app in apps/ via Git submodule

[33mcommit ac75cc9ba6a10c818c90a2e209f3ec9e79c35a39[m
Author: Michael Vorburger <mike@vorburger.ch>
Date:   Tue Mar 10 12:51:13 2015 +0100

    MIFOSX-1758 Make the dist ZIP include the community-app in apps/ via Git submodule
    Root build.sh, for both back-end and front-end
    Added -Penv=dev to incl. MariaDBj4
    Include apps/**/* in ZIP, fixed UI URL in launcher, some doc
    Set submodule Git rev to one that actually exists on remote
    Don't use 'git submodule update --remote' but foreach ..
    build-cloudbees.sh makes npm available
    Better logging, and trying dev and prod dist UI

[33mcommit 7f95e4bf2081211e5d34b02ba7f6e63db9cfb6a9[m
Author: Chandrika <chandrika@confluxtechnologies.com>
Date:   Mon Mar 16 19:16:42 2015 -0400

    MIFOSX-1808 Group hierarchy updation issue corrected.Center summary is now getting updated properly.

[33mcommit 7fcdb77f362f0c8738483e3ece10dac5dd9a51f2[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Mar 16 02:26:49 2015 +0530

    git push origin develop

[33mcommit 968ddcd8de7fd2cb019fca89b73a5ca19d3fb243[m
Author: Terence Denzil Monteiro <terence@sanjosesolutions.in>
Date:   Fri Mar 13 20:25:44 2015 +0400

    Release script to set version and releasedate

[33mcommit 2a800bf367924a3fa6c91ef45837abc71841d3bd[m
Merge: f440f29 cbe6d67
Author: Michael Vorburger <mike@vorburger.ch>
Date:   Fri Mar 13 09:04:40 2015 +0100

    Merge pull request #1295 from channagayan/licenceError
    
    ConcurrencyIntegrationTest licenceIssue fixed

[33mcommit cbe6d672e86ab7e9d305c23308227a29402f826f[m
Author: Channa Gayan <channagayan@gmail.com>
Date:   Fri Mar 13 13:19:07 2015 +0530

    ConcurrencyIntegrationTest licenceIssue fixed

[33mcommit a38d3d565d80987a36ae5198d49076b21f6e16f1[m
Author: andrew-dzak <andrewdzakpasu@musoni.eu>
Date:   Thu Mar 12 16:04:56 2015 +0100

    MIFOSX-1897 ability to see journals entries for specific loans and savings

[33mcommit fdd84097ea4422889d1dc57ebcaa3c3180b8f2fa[m
Author: andrew-dzak <andrewdzakpasu@musoni.eu>
Date:   Thu Mar 12 14:08:41 2015 +0100

    MIFOSX-1877 ability to edit working days by clients and added license text to ConcurrencyIntegrationTest file

[33mcommit f440f294ad26e0daef48c07e7ee36570ed06bfb7[m
Merge: 19021dd 4ad30d9
Author: Michael Vorburger <mike@vorburger.ch>
Date:   Tue Mar 10 12:20:23 2015 +0100

    Merge pull request #1226 from vorburger/MIFOSX-1756_UseAppsInsteadWebAndDocUpdate
    
    MIFOSX-1756 Follow-up, switched to using (existing) /apps/ instead /web/...

[33mcommit 19021dd18b1f3301c611564768ddb27ac1dcd769[m
Merge: eaa32e9 4e7f903
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Mar 9 00:11:25 2015 +0530

    Merge pull request #1289 from pramodn02/develop
    
    interest recalculation performance issue fixes

[33mcommit 4e7f9031bec37643ee08d018edf133cf55634faf[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Sun Mar 8 15:56:10 2015 +0530

    interest recalculation performance issue fixes

[33mcommit eaa32e9465c78b29d36068615c65c3e650772fe5[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Mar 5 20:32:38 2015 +0530

    sorting out concurrency issues with loans -1

[33mcommit 221cb1c17daea9d9b84fc5b22fcbdfd99fa3c4e4[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Mar 4 23:39:03 2015 +0530

    fixing more failing test cases

[33mcommit d00f793f99cbb816b292b0b11aa94188a4ae7803[m
Merge: 942eca1 d393812
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Mar 4 23:30:14 2015 +0530

    Merge branch 'develop' of github.com:openMF/mifosx into develop

[33mcommit 942eca124319b16b81529a04006491f936a7b57c[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Mar 4 23:29:23 2015 +0530

    fix for MifosX-1859

[33mcommit d393812cfe0e21c9bf018c3f57bb8a2606c86b06[m
Merge: 5262bb6 07ac26b
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Mar 4 23:14:52 2015 +0530

    Merge pull request #1272 from chandrikamohith/MIFOSX-1864
    
    MIFOSX-1864 Able to delete/add mapping fees and penalties during Edit

[33mcommit 5262bb6478440dd18474e19e0d985c1b4c02b510[m
Merge: 12746eb 1b2ea2e
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Mar 4 23:11:57 2015 +0530

    Merge pull request #1287 from MithunKashyap/MIFOSX-1737_New
    
    MIFOSX-1737 : Bug fix for center staff assignment history

[33mcommit 12746ebc0f66da24287e4c683f8f7c1942953e69[m
Merge: a16265e 3d7ef79
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Mar 4 22:44:14 2015 +0530

    Merge branch 'develop' of github.com:openMF/mifosx into develop

[33mcommit a16265eb924f029bab4060c217b7ec6967125a8a[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Mar 4 22:41:44 2015 +0530

    fix failing test cases

[33mcommit 1b2ea2ebdd44b8493b80917c1aa93671120e6ed8[m
Author: MithunKashyap <mithunkashyap0206@gmail.com>
Date:   Wed Mar 4 22:15:03 2015 +0530

    MIFOSX-1737 : Bug fix for center staff assignment history

[33mcommit 3d7ef79d3dd5978de0ce7d811966508349d4ee12[m
Merge: d9ce174 71cd946
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Mar 4 21:22:58 2015 +0530

    Merge pull request #1286 from sachinkulkarni12/accountmapping
    
    Financial Account Mapping Changes Updated.

[33mcommit 71cd946d211cf1660c18150f748cfcadef8126fd[m
Author: sachinkulkarni12 <sachin.kulkarni@confluxtechnologies.com>
Date:   Wed Mar 4 11:51:16 2015 +0530

    Financial Account Mapping Changes Updated.

[33mcommit d9ce1745da32ec58fcb30f1ff2999c2637153022[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Mar 3 20:07:17 2015 +0530

    buping sql patch number

[33mcommit 95acad7cf40a1097044354d6b88e0e982825b2cb[m
Merge: ab4e978 4a2a85d
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Mar 3 20:06:35 2015 +0530

    Merge branch 'latest' of git://github.com/pramodn02/mifosx into develop

[33mcommit ab4e97821c6f0a5aa033a4442d518534284925df[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Mar 3 19:14:09 2015 +0530

    fixing issue with patch number 244

[33mcommit 0c75d323eb978ebddcb0fb82a8e5f5d704959929[m
Merge: d47070a 6b8b247
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Mar 3 18:07:53 2015 +0530

    Merge pull request #1283 from karan173/MIFOSX-1847-datatable-delete-fix
    
    MIFOSX-1847. Deleting non empty Datatables now causes an exception

[33mcommit d47070a5c23de46d841d77038a8427b4ffcacf98[m
Merge: e5ccac0 72389cf
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Mar 3 18:04:15 2015 +0530

    Merge pull request #1284 from MithunKashyap/MIFOSX-1737_New
    
    MIFOSX-1737: Center Staff Assignment History

[33mcommit e5ccac0ca2a4223e56c4d5d8a881e23b773bce3b[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Mar 3 17:36:08 2015 +0530

    fixing test case failures due to work on tranche loans improvement

[33mcommit 72389cf933137d8ddaf4070bb471292e04baab24[m
Author: MithunKashyap <mithunkashyap0206@gmail.com>
Date:   Tue Mar 3 10:40:16 2015 +0530

    MIFOSX-1737: Center Staff Assignment History

[33mcommit 445a98d225f36d0bc82926451cd8116bef1145d2[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Mar 3 08:50:54 2015 +0530

    deleting duplicate sql patches and bumping patch numbers

[33mcommit 05fdf849815e7f3838776d26b3b152f102db23d7[m
Merge: 0adc4fa 495126b
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Mar 3 08:43:26 2015 +0530

    Merge branch 'chandrikamohith-TrancheDisbursal' into develop

[33mcommit 495126b75453800b4195273e531b5a690bbf3a48[m
Merge: 0adc4fa f720bd6
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Mar 3 08:43:14 2015 +0530

    Merge branch 'TrancheDisbursal' of git://github.com/chandrikamohith/mifosx into chandrikamohith-TrancheDisbursal

[33mcommit 6b8b247565c5abe26670c9e00563513a0ccb98a6[m
Author: karan173 <karan173@gmail.com>
Date:   Mon Mar 2 03:33:17 2015 +0530

    MIFOSX-1847. Deleting non empty Datatables now causes an exception

[33mcommit 0adc4fae1981aa2da423133f2df2c9636e7f467b[m
Merge: 98e9c3d f179771
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Mar 2 07:06:52 2015 +0530

    Merge pull request #1280 from MithunKashyap/New_MIFOSX-1828
    
    Mifosx 1828: Entity to entity mapping feature

[33mcommit 98e9c3d084c117af0ccc245c4b002a9a3fdb7280[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Mar 2 07:00:31 2015 +0530

    fixing failing test case

[33mcommit f179771b8d5b1d47f327d7676dca8a914c171d61[m
Author: MithunKashyap <mithunkashyap0206@gmail.com>
Date:   Sat Feb 28 15:01:52 2015 +0530

    MIFOSX-1828:Updated Patch script for EntityMapping Feature

[33mcommit f720bd6627f878fd96e4f4c84286e8721c5daab5[m
Author: Chandrika <chandrika@confluxtechnologies.com>
Date:   Tue Feb 24 17:35:39 2015 +0530

    MIFOSX-1842 Changes made for improvements to tranche disbursal loans.

[33mcommit f6f04d7ae310e2c65f5cd891368ffd846e16cb3f[m
Author: Chandrika <chandrika@confluxtechnologies.com>
Date:   Fri Jan 30 11:04:31 2015 -0500

    MIFOSX-1824 API changes and test case for loan approval in case of multi-disburse loans.

[33mcommit 4a2a85d20e0540e9797d6fa77ac54a263a5ba328[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Tue Feb 24 10:45:10 2015 +0530

    MIFOSX-1850 : Recurring deposits without maturity date

[33mcommit 07ac26b152add4727520e3e31199000ca7a4e18a[m
Author: Chandrika <chandrika@confluxtechnologies.com>
Date:   Tue Feb 17 14:39:34 2015 +0530

    MIFOSX-1864 Able to delete/add mapping fees and penalties during Edit

[33mcommit f22cac3febe83c3626dfe914d11d6aae9c2378b9[m
Author: MithunKashyap <mithunkashyap0206@gmail.com>
Date:   Mon Feb 16 09:51:32 2015 +0530

    MIFOSX-1828:EntityMapping Feature

[33mcommit 7ba9a81e530d46d455a28b002b1a83b42c0ebf9b[m
Merge: 1e4b491 2ab84df
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Feb 11 16:06:33 2015 +0530

    Merge pull request #1271 from emmanuelnnaa/MIFOSX-1878
    
    commit for MIFOSX-1878 (Savings account timeline does not include the data of the user that activated the account)

[33mcommit 2ab84dfd2247c511249009de2e323e370d7f0db5[m
Author: Emmanuel Nnaa <emmanuelnnaa@musoni.eu>
Date:   Wed Feb 11 10:58:37 2015 +0100

    commit for MIFOSX-1878 (Savings account timeline does not include the data of the user that activated the account)

[33mcommit d89152ce826a37fbbfc344748882931257e60a97[m
Author: CieyouRaoul <cieyouraoul@musoni.eu>
Date:   Tue Feb 10 15:22:35 2015 +0100

    MIFOSX-1875 password validation policy

[33mcommit 1e4b4911e00a4f9412fb860315cf7111caef7953[m
Merge: 8b2843c ad3a77d
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Feb 10 02:37:49 2015 -0800

    Merge pull request #1268 from bharathc27/test1
    
    guaranterdomainserviceissue

[33mcommit ad3a77d70c04590fa7a7fa2186c30e83c1a78a1d[m
Author: bharathc27 <bharath.c@confluxtechnologies.com>
Date:   Thu Feb 5 09:38:20 2015 +0530

    guaranterdomainserviceissue

[33mcommit 8b2843cabda87df8c0fc7f9a1e9898c6171bd14b[m
Merge: 7d887a4 ca0dcba
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Feb 4 09:39:04 2015 -0800

    Merge pull request #1264 from chandrikamohith/MIFOSX-1860
    
    MIFOSX-1860 Second tranche loan can be disbursed if disbursed on date is...

[33mcommit 7d887a4e0a801fd2af80ce3434403c6e0dda1bb1[m
Merge: c4a21a8 1e3bb03
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Feb 4 09:38:24 2015 -0800

    Merge pull request #1267 from bharathc27/test1
    
    MIFOSX-1464: fixed testcases of loanintegration which was assigned by vi...

[33mcommit 1e3bb039ee744656b145595703ff266d18fc4c7d[m
Author: bharathc27 <bharath.c@confluxtechnologies.com>
Date:   Wed Feb 4 13:00:18 2015 +0530

    MIFOSX-1464: fixed testcases of loanintegration which was assigned by vishwas sir

[33mcommit 9e6ec117d3c470ec4b53d3f9dc06a1024e75f6c5[m
Author: CieyouRaoul <cieyouraoul@musoni.eu>
Date:   Tue Feb 3 15:22:50 2015 +0100

    MIFOSX-1868

[33mcommit c4a21a8f8793c1f0566722f6619c2dc712b1adf8[m
Merge: 88edeb8 57d4c8f
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Feb 3 05:13:58 2015 -0800

    Merge pull request #1265 from chandrikamohith/roundOff
    
    MIFOSX-1464 spelling mistakes corrected

[33mcommit 57d4c8f868f83bf0c03d6ff4b974bd3ea0baec35[m
Author: Chandrika <chandrika@confluxtechnologies.com>
Date:   Tue Feb 3 17:14:53 2015 +0530

    MIFOSX-1464 spelling mistakes corrected

[33mcommit 88edeb8ee3c000668932470ccd2603621570bc04[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Feb 3 00:42:58 2015 -0800

    changing sql patch number for fixed emi changes

[33mcommit 13e3c0c6057ebe299095884a0ce993c403d74134[m
Merge: b4c354b 0182be4
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Feb 3 00:05:00 2015 -0800

    Merge branch 'TestCases' of git://github.com/bharathc27/mifosx into bharathc27-TestCases
    
    Conflicts:
    	api-docs/apiLive.htm
    	mifosng-provider/src/main/java/org/mifosplatform/portfolio/loanaccount/data/LoanAccountData.java
    	mifosng-provider/src/main/java/org/mifosplatform/portfolio/loanaccount/domain/Loan.java
    	mifosng-provider/src/main/java/org/mifosplatform/portfolio/loanaccount/loanschedule/domain/LoanApplicationTerms.java
    	mifosng-provider/src/main/java/org/mifosplatform/portfolio/loanaccount/loanschedule/service/LoanScheduleAssembler.java
    	mifosng-provider/src/main/java/org/mifosplatform/portfolio/loanproduct/LoanProductConstants.java
    	mifosng-provider/src/main/java/org/mifosplatform/portfolio/loanproduct/data/LoanProductData.java
    	mifosng-provider/src/main/java/org/mifosplatform/portfolio/loanproduct/domain/LoanProduct.java
    	mifosng-provider/src/main/java/org/mifosplatform/portfolio/loanproduct/serialization/LoanProductDataValidator.java
    	mifosng-provider/src/main/java/org/mifosplatform/portfolio/loanproduct/service/LoanProductReadPlatformServiceImpl.java

[33mcommit ca0dcba74f577eb485049d362681efffa202d0cd[m
Author: Chandrika <chandrika@confluxtechnologies.com>
Date:   Tue Feb 3 00:39:42 2015 -0500

    MIFOSX-1860 Second tranche loan can be disbursed if disbursed on date is after first repayment date

[33mcommit 0182be4cf92e282b90dac054ff8bcfeccee64f08[m
Author: bharathc27 <bharath.c@confluxtechnologies.com>
Date:   Fri Jan 30 11:11:37 2015 +0530

    MIFOSX: client/group loan testcases fixed

[33mcommit b4c354b22478bb1a9d75e9f7a95def48344e400b[m
Merge: 3f6d90e 4b6d794
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Jan 29 19:24:43 2015 -0800

    Merge branch 'MIFOSX-1613' of git://github.com/pramodn02/mifosx into pramodn02-MIFOSX-1613
    
    Conflicts:
    	api-docs/apiLive.htm
    	mifosng-provider/src/main/java/org/mifosplatform/portfolio/loanproduct/data/LoanProductData.java
    	mifosng-provider/src/main/java/org/mifosplatform/portfolio/loanproduct/domain/LoanProduct.java
    	mifosng-provider/src/main/java/org/mifosplatform/portfolio/loanproduct/serialization/LoanProductDataValidator.java
    	mifosng-provider/src/main/java/org/mifosplatform/portfolio/loanproduct/service/LoanProductReadPlatformServiceImpl.java

[33mcommit 3f6d90e7d4ca4a78b8d25e0263b45b98534f61eb[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Jan 28 15:33:29 2015 -0800

    updating sql patch number

[33mcommit b490a42dcf1d20e3190d30edbdccdf5a8ce72389[m
Merge: 371d38e af43e6c
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Jan 28 15:32:44 2015 -0800

    Merge branch 'MIFOSX-1846' of git://github.com/MithunKashyap/mifosx into MithunKashyap-MIFOSX-1846

[33mcommit 371d38e1c95be89067a6a3e84fad243108bd2ac2[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Jan 28 15:27:24 2015 -0800

    bumping up sql patch number

[33mcommit f3c306ffbb0317322bdc6efb07d4e5c52a66feeb[m
Merge: 1d66c3e 66133d7
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Jan 28 15:24:42 2015 -0800

    Merge branch 'displayNameFix' of git://github.com/MegaAlex/mifosx into MegaAlex-displayNameFix

[33mcommit 1d66c3e897e575808794215ae64baceaa87ecd2e[m
Merge: 19e6c28 b641d92
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Jan 20 22:09:58 2015 -0800

    Merge pull request #1257 from AJLiu/develop
    
    Fixed Failing Test Case

[33mcommit b641d92e7751f4627749c777bcdea0a7ad94f5fc[m
Author: Anthony Liu <tlrnalra@gmail.com>
Date:   Wed Jan 21 00:30:48 2015 -0500

    Fixed Failing Test Case

[33mcommit 19e6c28b5f43901cefd337f5ad94ca2280c0a256[m
Merge: 2ec7d2e d9db7d5
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Jan 20 19:23:11 2015 -0800

    Merge pull request #1255 from pramodn02/MIFOSX-1844
    
    MIFOSX-1844 : added config to manage last transaction

[33mcommit 2ec7d2eb22f87f4be9f243a02ba8a6944c69a897[m
Merge: 69d1edf 852cb45
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Jan 20 05:10:20 2015 -0800

    Merge pull request #1256 from sangameshn/patch-1
    
    Update INSTALL.md

[33mcommit 852cb4557667be5cd0a671eb06bfbe9614ff863b[m
Author: sangameshn <sangamesh@confluxtechnologies.com>
Date:   Tue Jan 20 18:24:51 2015 +0530

    Update INSTALL.md

[33mcommit d9db7d53137b65b59eb355a15b9a75e484eaf00f[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Tue Jan 20 17:03:06 2015 +0530

    MIFOSX-1844 : added config to manage last transaction

[33mcommit af43e6c58fc674166aa39a202d2c482ae260756c[m
Author: MithunKashyap <mithunkashyap0206@gmail.com>
Date:   Sat Jan 17 13:52:31 2015 +0530

    MIFOSX-1846: Report and script files for Loan Transaction Receipt

[33mcommit 69d1edff32bb5f497c7476607b40dded70cccbb9[m
Merge: ea51f9f 2bd1332
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Jan 16 01:56:16 2015 -0800

    Merge pull request #1251 from pramodn02/MIFOSX-1742
    
    MIFOSX-1742 : added collection sheet for individuals

[33mcommit ea51f9fcebaedae5f81dc662d5042491b40bd955[m
Merge: d5f0ae5 f438bc6
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Jan 16 01:52:06 2015 -0800

    Merge pull request #1252 from pramodn02/MIFOSX-1481
    
    MIFOSX-1481 : changed the validation to allow writeoff on last repayment...

[33mcommit f438bc6f17decb0023c32b3e7a0f8a942ec30ef2[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Fri Jan 16 15:11:35 2015 +0530

    MIFOSX-1481 : changed the validation to allow writeoff on last repayment date

[33mcommit d5f0ae53f80877174df13ab4d3538489b52e5296[m
Merge: bc38cf4 a7ddb1a
Author: Markus Geiß <mgeiss257@gmail.com>
Date:   Fri Jan 16 06:25:11 2015 +0100

    Merge pull request #1250 from AJLiu/develop
    
    closed

[33mcommit 2bd1332da763348adf2bbcd0b5b98642397989a6[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Fri Jan 16 10:18:04 2015 +0530

    MIFOSX-1742 : added collection sheet for individuals

[33mcommit a7ddb1a70fd4fa1f4ddcb17fc2e65c8f6c77654b[m
Author: Anthony Liu <tlrnalra@gmail.com>
Date:   Thu Jan 15 01:22:29 2015 -0500

    Office Integration Office Modification

[33mcommit bc38cf4ff9fa6286b147309fb73ed4fdfaab4550[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Jan 14 10:13:07 2015 -0800

    build failing on jenkins

[33mcommit 20f292d329612bf0884417349ced25ffb8147a99[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Jan 14 09:29:42 2015 -0800

    MIFOSX-1841

[33mcommit bd689bc0084e998e2b627f9b91deb8844c66bd69[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Jan 13 15:41:26 2015 -0800

    MIFOSX-1838

[33mcommit bdbd1a85ede722de5dfdd4910e872a76b8a42b2d[m
Merge: 19c7ef1 0c7b8c9
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Jan 13 11:14:33 2015 -0800

    Merge pull request #1248 from chandrikamohith/MIFOSX-1776
    
    MIFOSX-1776 Sync repayment dates with meeting dates for group loan

[33mcommit 19c7ef1f32d713c70a9c8723ed29147017099702[m
Merge: f9d4af7 deb506e
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Jan 13 11:13:56 2015 -0800

    Merge pull request #1249 from MegaAlex/licenseFix
    
    Add license headers

[33mcommit f9d4af7d2fa1c315c0944dcdefe3978c890fcf49[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Jan 13 11:11:09 2015 -0800

    bump version number

[33mcommit 593ba64eacabeebd97faee15d5ab27211d3e4657[m
Merge: fe9b343 d8bcbba
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Jan 13 11:10:16 2015 -0800

    Merge branch 'MIFOSX-1786' of git://github.com/binaryking/mifosx into binaryking-MIFOSX-1786

[33mcommit deb506ece3de4bd98e9df96c4eff76f7ea166947[m
Author: Alex Ivanov <alexivanov97@gmail.com>
Date:   Fri Jan 9 22:47:56 2015 +0200

    Add license headers

[33mcommit 0c7b8c937c9271dbe0ad6af49b8b39d39fd4d9c6[m
Author: Chandrika <chandrika@confluxtechnologies.com>
Date:   Fri Jan 9 12:16:48 2015 -0500

    MIFOSX-1776 Sync repayment dates with meeting dates for group loan

[33mcommit fe9b3437d42d2d7416038f014639925c8cf6d4af[m
Merge: e979bdf 44c9e48
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Jan 8 06:32:58 2015 -0800

    Merge branch 'openingbalances' of git://github.com/binnygopinath/mifosx into binnygopinath-openingbalances
    
    Conflicts:
    	mifosng-provider/src/main/java/org/mifosplatform/accounting/journalentry/service/JournalEntryWritePlatformServiceJpaRepositoryImpl.java

[33mcommit e979bdf6b7e0ba52720cdb8161674d9ba2d4dc0c[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Jan 7 22:57:28 2015 -0800

    resolving version number conflict with sql patch

[33mcommit 7d059acd837ee0df7b5ca4f7e8d15fc88d0c8150[m
Merge: 0acf7c1 e4cf905
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Jan 7 22:56:28 2015 -0800

    Merge branch 'MIFOSX-1809' of git://github.com/chandrikamohith/mifosx into chandrikamohith-MIFOSX-1809

[33mcommit 0acf7c1498a1d307691693e97a76ad4ac2b51cd5[m
Merge: 653c534 4cd7c79
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Jan 7 22:50:45 2015 -0800

    Merge pull request #1237 from chandrikamohith/MIFOSX-1776
    
    MIFOSX-1776 Sync repayment dates with meeting dates for Group Loan appli...

[33mcommit 653c53451438daabf1f54a42f863b9b74e8e91d5[m
Merge: cfcc265 70331f8
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Jan 7 21:39:05 2015 -0800

    Merge pull request #1234 from satish-conflux/MIFOSX-1817
    
    MIFOSX-1870 : Added Currency Type

[33mcommit cfcc2654d43f4773079615441efb08432b65f3f6[m
Merge: d0bb7c7 ebab869
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Jan 7 21:29:25 2015 -0800

    Merge pull request #1238 from MithunKashyap/MIFOSX-1823
    
    MIFOSX-1823: Report file and upgrade script for Savings Transaction Receipt

[33mcommit d0bb7c7ae3213269c59441e3c15f7ff91bfb8222[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Jan 7 17:51:32 2015 -0800

    failing test cases fix

[33mcommit ebab8699fedca7ffc8df517e852e7655863172c8[m
Author: MithunKashyap <mithunkashyap0206@gmail.com>
Date:   Tue Jan 6 15:43:28 2015 +0530

    MIFOSX-1823: Report file and upgrade script for Savings Transaction Receipt

[33mcommit 8b56795821635733014a4e90d07a64eaa45bf78d[m
Merge: f46875f f477a19
Author: Markus Geiß <mgeiss257@gmail.com>
Date:   Mon Jan 5 20:13:46 2015 +0100

    Merge pull request #1236 from AJLiu/develop
    
    closed (y)

[33mcommit 4cd7c79225e94622d534938bc7829776c0b121ea[m
Author: Chandrika <chandrika@confluxtechnologies.com>
Date:   Mon Jan 5 13:54:38 2015 -0500

    MIFOSX-1776 Sync repayment dates with meeting dates for Group Loan application

[33mcommit f46875fcb71bc263162c4bff1fb9c929009451e7[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Jan 4 21:30:27 2015 -0800

    fix compilatin issues

[33mcommit f477a190a787702deddd53be372a4f0d00dd1fd0[m
Author: Anthony Liu <tlrnalra@gmail.com>
Date:   Sun Jan 4 17:15:38 2015 -0500

    Center Integration Update and Deletion

[33mcommit e7db6731d95594520c7377b1b4333a94399b693d[m
Merge: 144860d f082ac4
Author: Markus Geiß <mgeiss257@gmail.com>
Date:   Sun Jan 4 08:43:18 2015 +0100

    Merge pull request #1232 from AJLiu/develop
    
    closed (y)

[33mcommit f082ac40a6daf72a44d2f958ee50bd8a80c69627[m
Author: Anthony Liu <tlrnalra@gmail.com>
Date:   Tue Dec 30 17:00:58 2014 -0500

    Center Integration Retrieval

[33mcommit 144860d48cb8045c1f80a5a26d440fcd7c171bd3[m
Merge: d310eb3 06bb09f
Author: Markus Geiß <mgeiss257@gmail.com>
Date:   Fri Jan 2 11:37:27 2015 +0100

    Merge pull request #1228 from binaryking/MIFOSX-1757
    
    closed (y)

[33mcommit 70331f8447784e49ef250d7ab6bf0bf391afeeb5[m
Author: Satish Sajjan <satish.sajjan@confluxtechnologies.com>
Date:   Wed Dec 31 19:24:29 2014 +0530

    MIFOSX-1870 : Added Currency Type

[33mcommit 4b6d794a47b51dd914d72e4e1dff30541db6ebac[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Wed Dec 31 18:28:43 2014 +0530

    MIFOSX-1613 : changes for overdue status and npa

[33mcommit d310eb308d8e0ba77f18a30b9de8d4e01ca4df50[m
Merge: 253fba3 f5aa3f8
Author: Markus Geiß <mgeiss257@gmail.com>
Date:   Tue Dec 30 22:57:43 2014 +0100

    Merge pull request #1231 from AJLiu/develop
    
    closed

[33mcommit f5aa3f88e988460363f30e41782d82e6fb2443e2[m
Author: Anthony Liu <tlrnalra@gmail.com>
Date:   Tue Dec 30 14:56:51 2014 -0500

    Center Integration Setup

[33mcommit 253fba35c31e5e45eaa344eb759be48a72eab582[m
Merge: bedbe70 ed8809f
Author: Markus Geiß <mgeiss257@gmail.com>
Date:   Tue Dec 30 20:12:11 2014 +0100

    Merge pull request #1229 from MegaAlex/qa-staff3
    
    closed

[33mcommit e4cf90562c1182b9c0235212762a3d41e977756c[m
Author: Chandrika <chandrika@confluxtechnologies.com>
Date:   Tue Dec 30 12:50:04 2014 -0500

    MIFOSX-1809 Closure Reason Code added for Centers-'CenterClosureReason'

[33mcommit ed8809f63ce1bb42e80b367da918534f78f9c0c7[m
Author: Alex Ivanov <alexivanov97@gmail.com>
Date:   Tue Dec 30 17:13:14 2014 +0200

    Add staff update test cases

[33mcommit 06bb09f189fa515dfafc715afee43ceda87b26b0[m
Author: Mohammed Nafees <nafees.technocool@gmail.com>
Date:   Tue Dec 30 12:06:28 2014 +0530

    MIFOSX-1757 Document the new much simpler end-user standalone server set-up end-user experience

[33mcommit bedbe7023f908ecfb71d979b2763ecdeb6f1ae46[m
Merge: 74242ed dcff6fb
Author: Markus Geiß <mgeiss257@gmail.com>
Date:   Tue Dec 30 07:33:07 2014 +0100

    Merge pull request #1224 from MegaAlex/qa-staff2
    
    closed (y)

[33mcommit 4ad30d91d9f707b1059e5bfcf2f7df590d8683e5[m
Author: Michael Vorburger <mike@vorburger.ch>
Date:   Mon Dec 29 18:45:55 2014 +0100

    MIFOSX-1756 Follow-up, switched to using (existing) /apps/ instead /web/, and slightly updated the doc

[33mcommit dcff6fbcd308b92d2d355731bf3882553fc75ac5[m
Author: Alex Ivanov <alexivanov97@gmail.com>
Date:   Sun Dec 28 21:00:00 2014 +0200

    Add tests for staff retrieval

[33mcommit 74242ed036c6915e295845c356d24b57dd993869[m
Merge: 3733326 7ec1f9e
Author: Michael Vorburger <mike@vorburger.ch>
Date:   Mon Dec 29 18:30:16 2014 +0100

    Merge pull request #1201 from binaryking/MIFOSX-1756
    
    MIFOSX-1756 Make it easy for Static Web Content such as the community-app to be served by the new standalone end-user server (not development)

[33mcommit 37333266aa28fb32278fa0844c6e0a3d833f47fa[m
Merge: 6f16786 8971723
Author: Michael Vorburger <mike@vorburger.ch>
Date:   Mon Dec 29 17:13:47 2014 +0100

    Merge pull request #1220 from vorburger/UpgradeMariaDB4jFromv211To213ToFixWindows
    
    MIFOSX-1552 Upgraded MariaDB4j dependency from v2.1.1 to v2.1.3, to fix Windows problem

[33mcommit 66133d7f222a65640cde578bc1b46b9607608c46[m
Author: Alex Ivanov <alexivanov97@gmail.com>
Date:   Sat Dec 27 21:24:28 2014 +0200

    Change maximum display name length for staff

[33mcommit 6f16786aa82685c0556e802a48a638a6eb5472d1[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Dec 28 07:42:48 2014 -0800

    updating migration script v_230 as a aworkaround for a flyway bug

[33mcommit 56a12e3af47aecb910e7bfe4f93b65ab4ec0991c[m
Merge: 8e52dd2 3425d76
Author: Markus Geiß <mgeiss257@gmail.com>
Date:   Sun Dec 28 15:48:29 2014 +0100

    Merge pull request #1221 from MegaAlex/qa-staff1
    
    (y)

[33mcommit 8e52dd2a1e80bb0bd9af980fe4d2661ecc2565fb[m
Merge: a19d345 9b3f56f
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Dec 28 05:36:19 2014 -0800

    Merge pull request #1219 from satish-conflux/MIFOSX-1670-1
    
    Role Functionality with test cases

[33mcommit d8bcbba79cbbb0b114a0344fa8ee2706ab35db0f[m
Author: Mohammed Nafees <nafees.technocool@gmail.com>
Date:   Sun Dec 28 00:18:29 2014 +0530

    MIFOSX-1786 Allow usage of UGD (Templates) for Hooks

[33mcommit 3425d762f042fd8f5ecc34221c8af1a8c2109e9c[m
Author: Alex Ivanov <alexivanov97@gmail.com>
Date:   Sat Dec 27 16:04:39 2014 +0200

    Add Staff creation test cases

[33mcommit 8971723d9664e5b0d8e3d230905e0a030df74596[m
Author: Michael Vorburger <mike@vorburger.ch>
Date:   Sat Dec 27 01:41:06 2014 +0100

    Upgraded MariaDB4j dependency from v2.1.1 to v2.1.3, to fix Windows problem

[33mcommit 9b3f56f6f6ea1aa637d972086adab771c1efbf41[m
Author: Satish Sajjan <satish.sajjan@confluxtechnologies.com>
Date:   Fri Dec 26 17:51:36 2014 +0530

    Role Functionality with test cases

[33mcommit a19d345d88affd8afab5754cc0b3e2c5a20fbd8f[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Dec 23 09:26:02 2014 -0800

    release number bumped up for 1.26 release

[33mcommit dc979da263ca17581aac39b85082244b6559a074[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Dec 23 09:16:45 2014 -0800

    schema update was missed out for 1.26 release

[33mcommit 6f0d24ee988d8292524951d7b6de4da68cde24ea[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Dec 23 08:44:48 2014 -0800

    Update CHANGELOG.md

[33mcommit fe0438a2ac47f33d1b34ced9f3d428aa73bcab0f[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Dec 22 01:12:31 2014 -0800

    Update CHANGELOG.md
    
    fixing typos

[33mcommit 26c8ce6cbec3339e4ee6d057fe8dc039fda09de8[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Dec 22 01:09:57 2014 -0800

    Update CHANGELOG.md
    
    for 1.26 release

[33mcommit da79d200499c2f2423960e5b157b8a42eff052b0[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Dec 22 01:07:50 2014 -0800

    Update CHANGELOG.md
    
    for 1.26 release

[33mcommit 827e6bdbfbe3882463754a8b9fe5bbce5e00d420[m
Merge: b783bc5 38ffaba
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Dec 19 04:28:59 2014 -0800

    Merge pull request #1211 from openMF/revert-1210-develop
    
    Revert "update V197__updated_loan_running_balance_of_transactions.sql wi...

[33mcommit 38ffaba65be4381216593e828682016ad9073b8b[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Dec 19 04:28:47 2014 -0800

    Revert "update V197__updated_loan_running_balance_of_transactions.sql with a stored procedure"

[33mcommit b783bc5fadac32ba353659a3affda7c7e5008cc6[m
Merge: 436bdfd 54a45c5
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Dec 19 01:14:04 2014 -0800

    Merge pull request #1210 from sughosh88/develop
    
    update V197__updated_loan_running_balance_of_transactions.sql with a stored procedure

[33mcommit 54a45c5e41eaaa5b096a0265050c4be675232e81[m
Author: Sughosh <sughosh@confluxtechnologies.com>
Date:   Fri Dec 19 14:06:10 2014 +0530

    update V197__updated_loan_running_balance_of_transactions.sql with a stored procedure

[33mcommit 436bdfd5381eb0ec43b4bf09a0a6a11cd99636e3[m
Merge: 5324256 9d07e9a
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Dec 18 19:05:10 2014 -0800

    Merge pull request #1209 from MegaAlex/MIFOSX-1739
    
    MIFOSX-1739 Add option to show only active loan products

[33mcommit 532425626ed537400e39acf78ea0975d95727679[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Dec 18 19:00:53 2014 -0800

    remove compilation warnings furing gradle build

[33mcommit a665beb1b71dd62472d55953d2726b3aba9fd51b[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Dec 18 18:51:29 2014 -0800

    bump sql patch

[33mcommit 7acc549a018f90054acbed0788b8d59b0e988cc6[m
Merge: 19b2ce6 c0d76e0
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Dec 18 18:46:22 2014 -0800

    Merge branch 'cash_mgmt_revised' of git://github.com/binnygopinath/mifosx into binnygopinath-cash_mgmt_revised

[33mcommit 9d07e9ad7a4cb2b7819cbaabc08ac4ea2f5353be[m
Author: Alex Ivanov <alexivanov97@gmail.com>
Date:   Fri Dec 19 00:32:59 2014 +0200

    MIFOSX-1739 Add option to show only active loan products

[33mcommit 19b2ce6deff8c6d741afc89c3648401a4693f5c4[m
Merge: f72a5ef d7b6e22
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Dec 18 14:28:53 2014 -0800

    Merge pull request #1208 from chandrikamohith/MIFOSX-1768
    
    MIFOSX-1768 Corrected Spelling error

[33mcommit d7b6e2234476f865da71e6d3f912b3522ec68f9c[m
Author: Chandrika <chandrika@confluxtechnologies.com>
Date:   Thu Dec 18 17:12:01 2014 -0500

    MIFOSX-1768 Corrected Spelling error

[33mcommit f72a5efb526ff54db89f7f2841c0a2f28059715a[m
Merge: c4068df a174469
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Dec 17 17:34:22 2014 -0800

    Merge pull request #1198 from pramodn02/MIFOSX-1744
    
    MIFOSX-1744 : added portion of recovery incase guarantee is more than 100%

[33mcommit c4068df9f4e6f24062598b3893a648d1f878b80c[m
Merge: 2cef6cf 114dcb2
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Dec 17 17:23:15 2014 -0800

    Merge pull request #1206 from chandrikamohith/MIFOSX-1766
    
    MIFOSX-1766 Table entry is now being made for savingsOfficer in assignme...

[33mcommit 114dcb21c4bd05d08dd2c39d78cf986aa1a33d70[m
Author: Chandrika <chandrika@confluxtechnologies.com>
Date:   Wed Dec 17 16:55:21 2014 -0500

    MIFOSX-1766 Table entry is now being made for savingsOfficer in assignment history table after Approval

[33mcommit 7ec1f9e80884806d03a17b23fd4129bc2c8d4a7c[m
Author: Mohammed Nafees <nafees.technocool@gmail.com>
Date:   Wed Dec 17 20:07:11 2014 +0530

    MIFOSX-1756 Make it easy for Static Web Content such as the community-app to be served by the new standalone end-user server (not development)

[33mcommit 2cef6cfd9a8130be7b69b09ca110cf966646db1a[m
Merge: 73d0017 7ab8a7b
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Dec 17 07:35:54 2014 -0800

    Merge pull request #1204 from binaryking/develop
    
    Add missing license headers to fix TravisCI build

[33mcommit 7ab8a7bf8e0db4a92ed118bf220377012aec7031[m
Author: Mohammed Nafees <nafees.technocool@gmail.com>
Date:   Wed Dec 17 21:03:30 2014 +0530

    Add missing license headers to fix TravisCI build.

[33mcommit 73d001754053142289ce07236e08902088b815d0[m
Merge: 90ac626 fcff48f
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Dec 17 07:16:23 2014 -0800

    Merge pull request #1203 from chandrikamohith/TestCase-fix
    
    MIFOSX-1662 fix for integration test ClientTest and removing warnings

[33mcommit fcff48ffdce69c26298cf0b2a03fd2ca4cce74b8[m
Author: Chandrika <chandrika@confluxtechnologies.com>
Date:   Wed Dec 17 10:05:45 2014 -0500

    MIFOSX-1662 fix for integration test ClientTest and removing warnings

[33mcommit 90ac626fc1cbf734fc74a4fec61ddef77908d7a8[m
Merge: d8ed1b6 651da79
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Dec 17 06:42:43 2014 -0800

    Merge pull request #1200 from pramodn02/MIFOSX-1760
    
    MIFOSX-1760 : corrected start date calculation for approval

[33mcommit d8ed1b685fcf5837074bc6f3436ec62f88bf017b[m
Merge: 18dba2f 11ba1f5
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Dec 17 06:34:16 2014 -0800

    Merge pull request #1199 from Nayan/MIFOSX-1680
    
    #MIFOSX-1680 Allow to modifiy frequncy and intervals if calendar is NOT synced with any active entity

[33mcommit 651da79697161f167ef138ffb24a99ae4bd7c63a[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Wed Dec 17 15:22:29 2014 +0530

    MIFOSX-1760 : corrected start date calculation for approval

[33mcommit 11ba1f53013f928a3e38cc856b0ef24205d35b18[m
Author: Nayan <nayan.ambali@gmail.com>
Date:   Wed Dec 17 15:01:19 2014 +0530

    Allow to modifiy frequncy and intervals if calendar is NOT synced with any active entity

[33mcommit a174469d19e22427b9b736eafe23087877d5e06f[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Wed Dec 17 12:46:32 2014 +0530

    MIFOSX-1744 : added portion of recovery incase guarantee is more than 100%

[33mcommit 18dba2fe2c05dc340a13b3aa5746731d856ef7fa[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Dec 16 20:45:08 2014 -0800

    bumping up sql patch number

[33mcommit dac4c63632050eb3d05cdfc66246948815e819b9[m
Merge: 299b52c 9d11434
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Dec 16 18:33:14 2014 -0800

    Merge pull request #1169 from binnygopinath/branch_specific_products_and_charges
    
    Branch Specific Products and Charges - initial commit

[33mcommit 299b52ca45105a7138cf8acb54c3c19b262fa11a[m
Merge: 0b9b2bb ed03a30
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Dec 16 18:29:31 2014 -0800

    Merge branch 'develop' of git://github.com/michaeldk-miagstan/mifosx into michaeldk-miagstan-develop
    
    Conflicts:
    	mifosng-provider/src/integrationTest/java/org/mifosplatform/integrationtests/common/loans/LoanTransactionHelper.java
    	mifosng-provider/src/main/java/org/mifosplatform/commands/service/SynchronousCommandProcessingService.java
    	mifosng-provider/src/main/java/org/mifosplatform/portfolio/account/service/PortfolioAccountReadPlatformService.java
    	mifosng-provider/src/main/java/org/mifosplatform/portfolio/loanaccount/data/LoanAccountData.java
    	mifosng-provider/src/main/java/org/mifosplatform/portfolio/loanaccount/domain/Loan.java
    	mifosng-provider/src/main/java/org/mifosplatform/portfolio/loanaccount/domain/LoanAccountDomainServiceJpa.java
    	mifosng-provider/src/main/java/org/mifosplatform/portfolio/loanaccount/service/LoanReadPlatformService.java
    	mifosng-provider/src/main/java/org/mifosplatform/portfolio/loanaccount/service/LoanWritePlatformService.java

[33mcommit 0b9b2bb6f62275a5593d18aa840ea48671495b0a[m
Merge: 66cce83 9160fb5
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Dec 16 11:46:53 2014 -0800

    Merge pull request #1194 from MegaAlex/MIFOSX-1747
    
    MIFOSX-1747 Don't register the basic auth filter before Spring's filter ...

[33mcommit 9160fb5f5713de729555b49886c04acfb1abb630[m
Author: Alex Ivanov <alexivanov97@gmail.com>
Date:   Tue Dec 16 21:06:14 2014 +0200

    MIFOSX-1747 Don't register the basic auth filter before Spring's filter chain

[33mcommit 66cce837b58e2b1036cede65df6b65e6da1bb4cf[m
Merge: 9ec0d7a 88a7ffd
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Dec 16 10:42:51 2014 -0800

    Merge pull request #1193 from chandrikamohith/1662
    
    MIFOSX-1662 Corrected Code Names for Reject and Withdraw.

[33mcommit 9ec0d7acd56288b678da4e3943034dd03016f3ac[m
Author: Michael Vorburger <mike@vorburger.ch>
Date:   Wed Dec 3 22:53:04 2014 +0100

    .travis.yml config to cache Gradle/Maven repos (~/.m2 & .gradle)
    This will lead to faster PR builds, and more importantly more reliable ones, as we should have
    less bad PRs due to spurious build failures due to unreachable http://repository.pentaho.org.
    With Doc + cache APT too (just as an example for later NPM builds)

[33mcommit 88a7ffd75ab6b007bb5e7720600040a055fd750d[m
Author: Chandrika <chandrika@confluxtechnologies.com>
Date:   Tue Dec 16 13:30:58 2014 -0500

    MIFOSX-1662 Corrected Code Names for Reject and Withdraw.

[33mcommit 45790c3a607d49b8fd0222f19fb3eed0e46bf1aa[m
Merge: ba52924 312f016
Author: Michael Vorburger <mike@vorburger.ch>
Date:   Tue Dec 16 19:18:32 2014 +0100

    Merge pull request #1189 from MegaAlex/MIFOSX-1701
    
    MIFOSX-1701 Add option to display only active group members

[33mcommit ba52924e2f7b4bcd386e499faae4e84496fdeec4[m
Author: Michael Vorburger <mike@vorburger.ch>
Date:   Tue Dec 16 19:02:53 2014 +0100

    Added missing license headers which made all the PRs fail on Travis.

[33mcommit 00d00551ad72f518447a4e4e020f657d5f4f136d[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Tue Dec 16 15:17:42 2014 +0530

    MIFOSX-1464 : added loan installment round-off

[33mcommit f98edc8781c74dd87ff23da61ec9d0dfb7eeee6c[m
Merge: a9850c5 d248392
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Dec 15 18:31:29 2014 -0800

    Merge pull request #1191 from chandrikamohith/TestCase
    
    MIFOSX-1717,MIFOSX-1662 Test case for account number preferences and Cod...

[33mcommit d24839267fbe7c1f1177db6c016789d9d2fe415e[m
Author: Chandrika <chandrika@confluxtechnologies.com>
Date:   Mon Dec 15 17:44:22 2014 -0500

    MIFOSX-1717,MIFOSX-1662 Test case for account number preferences and Code Refactoring for Client status.

[33mcommit 312f016a5633238ce0328ed3ed6a4341ac109e04[m
Author: Alex Ivanov <alexivanov97@gmail.com>
Date:   Mon Dec 15 21:59:57 2014 +0200

    MIFOSX-1701 Add option to display only active group members
    
    Complete documentation for the retrieving group call

[33mcommit a9850c50dae3d02ed6bd29d37ed4194260ee6c13[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Dec 14 23:36:10 2014 -0800

    removing merge conflicts introduced in https://github.com/openMF/mifosx/pull/1174

[33mcommit 9e6f8a8e4011353b51a79f6253d200255783af6f[m
Merge: 2c511e8 f48f7ed
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Dec 14 23:26:53 2014 -0800

    Merge pull request #1174 from MithunKashyap/MIFOSX-1663_New
    
    Mifos-1663:added more flexibilty for center/group meeting reschedule

[33mcommit 2c511e8e2800d22b06f6de6585da062c4ecb0d05[m
Merge: c94a59c 8192e55
Author: Michael Vorburger <mike@vorburger.ch>
Date:   Sat Dec 13 19:01:11 2014 +0100

    Merge pull request #1187 from binaryking/MIFOSX-969
    
    MIFOSX-969 Missing API Documentation for Collection Sheets

[33mcommit 8192e55942fce69081fe4f0f32fc845d3fc90b7b[m
Author: Mohammed Nafees <nafees.technocool@gmail.com>
Date:   Sat Dec 13 22:31:31 2014 +0530

    MIFOSX-969 Missing API Documentation for Collection Sheets

[33mcommit c94a59c66f5ddeebb02e7d3166f513e106b89b9f[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sat Dec 13 09:38:43 2014 -0800

    formatting test cases

[33mcommit 5d5d79d46c96d4e1efef9ab27b8176e24abfebcb[m
Merge: 6e07b83 64e7854
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sat Dec 13 09:32:23 2014 -0800

    Merge branch 'chandrikamohith-TestCase' into develop

[33mcommit 64e78540d9bca109a93f910eb906fca283a83d7f[m
Author: Chandrika <chandrika@confluxtechnologies.com>
Date:   Fri Dec 12 14:56:10 2014 -0500

    MIFOSX-1717 Integration test cases for Account Number Preferences.

[33mcommit c0d76e0d23e3d7bbae7156d308d550b23458ef7c[m
Author: Binny G Sreevas <binny.gopinath@gmail.com>
Date:   Fri Dec 12 17:23:26 2014 +0530

    Added accounting entries for allocate and settle transactions

[33mcommit 6e07b83075c67db540495f049e9bd82328dcb8a9[m
Merge: a72ad7e a389088
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Dec 11 11:01:54 2014 -0800

    Merge pull request #1181 from Nagaraj12/clientReject
    
    MIFOSX-1662:Adding new statuses to Client lifecyle

[33mcommit a72ad7eef38c3b1f03a5d0701aac2b6ff8bfe54c[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Dec 11 08:07:30 2014 -0800

    ignore failing test case

[33mcommit 17963105587cca40ad9ecef0c44675a152b58aa8[m
Merge: c1d7f4a 2438932
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Dec 11 06:17:09 2014 -0800

    Merge pull request #1183 from openMF/revert-1182-develop
    
    Revert "Test Cases for Office API. "

[33mcommit 243893240c7f027a778c9022bb47c2a93f755f9a[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Dec 11 06:17:00 2014 -0800

    Revert "Test Cases for Office API. "

[33mcommit c1d7f4a74456bfda1489e0c86e3d18ece987f5c0[m
Merge: 020c11c f16ae30
Author: Markus Geiß <mgeiss257@gmail.com>
Date:   Thu Dec 11 10:50:51 2014 +0100

    Merge pull request #1182 from varunVNnayar/develop
    
    Closed (y)

[33mcommit f16ae302d293d44cafc0c39ae8fe4544f927ab65[m
Author: varunVNnayar <rajeshnair@Rajesh-Nairs-MacBook-Pro.local>
Date:   Thu Dec 11 14:55:03 2014 +0530

    License Header Added.

[33mcommit 7a2dec9987da150b2c2cc7030277531029d1f710[m
Author: varunVNnayar <rajeshnair@Rajesh-Nairs-MacBook-Pro.local>
Date:   Thu Dec 11 13:55:41 2014 +0530

    Renaming to .java

[33mcommit 632fac4d5bdf7b4ce82fb204169dc47bac9cc4b4[m
Author: varunVNnayar <rajeshnair@Rajesh-Nairs-MacBook-Pro.local>
Date:   Thu Dec 11 13:49:29 2014 +0530

    Test Cases for Office API. (Reformatted Code)

[33mcommit fa20a9ccb316465f9ae5f92bac51b0ee725406a7[m
Author: varunVNnayar <rajeshnair@Rajesh-Nairs-MacBook-Pro.local>
Date:   Thu Dec 11 13:38:36 2014 +0530

    Test cases for Office API.

[33mcommit 020c11c89a8da8a7f0cf1b29af0f42ce7a8eb8e3[m
Merge: 50db06a 4caf4ef
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Dec 10 08:24:51 2014 -0800

    Merge pull request #1176 from AJLiu/develop
    
    Added missing @Test annotation and clarified method names

[33mcommit 50db06a5f3c23709b838b4c0295808ece2b21e77[m
Merge: 0d21a6d 23af79c
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Dec 10 08:22:17 2014 -0800

    Merge pull request #1178 from binnygopinath/MIFOSX-1735
    
    MIFOSX-1735 - sub-status of Client to be fetched during read operations

[33mcommit 0d21a6d60d2cfe9f0bc161d9ef3c10d84aa6aeac[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Dec 10 07:47:44 2014 -0800

    MIFOSX-1738 should include unit test cases

[33mcommit ea53d0ca7c2758685aa2fb5eb3f2c79b04dfbeb9[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Dec 10 06:29:06 2014 -0800

    MIFOSX-1738

[33mcommit f9a57a53ba19458522c84c49682786a8fee821a1[m
Author: varunVNnayar <rajeshnair@Rajesh-Nairs-MacBook-Pro.local>
Date:   Wed Dec 10 18:43:40 2014 +0530

    Test case 1 for Office creation.

[33mcommit 95d74ac11bce3c9bc34912c1db77302fb48112c6[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Dec 10 04:42:30 2014 -0800

    commenting out failing client test:

[33mcommit 81973c0da9f4d999f781941900e31d5b09ee00f3[m
Merge: 9e7ce7a a3da63b
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Dec 10 04:39:39 2014 -0800

    Merge pull request #1179 from openMF/revert-1177-develop
    
    Revert "Integration Test Suite for Office API."

[33mcommit a3da63b7606fbff365949359ab725562ea645e5f[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Dec 10 04:39:25 2014 -0800

    Revert "Integration Test Suite for Office API."

[33mcommit 9e7ce7afeb9edf50ea0fada917ee59519f143dd7[m
Merge: 38bb259 6b171c5
Author: Markus Geiß <mgeiss257@gmail.com>
Date:   Wed Dec 10 12:57:09 2014 +0100

    Merge pull request #1177 from varunVNnayar/develop
    
    closed

[33mcommit 6b171c5a3fb50c792a0bd6eebddcf5d96e2b4a94[m
Author: varunVNnayar <rajeshnair@Rajesh-Nairs-MacBook-Pro.local>
Date:   Wed Dec 10 17:09:20 2014 +0530

    OfficeHelper.

[33mcommit 2478851790e39c44a6a851ea8b8256d4c870f692[m
Author: varunVNnayar <rajeshnair@Rajesh-Nairs-MacBook-Pro.local>
Date:   Wed Dec 10 16:46:58 2014 +0530

    Upate Office Helper!

[33mcommit 23af79ce1b7bcb5b0f66cd94e10d868976a46f1a[m
Author: Binny G Sreevas <binny.gopinath@gmail.com>
Date:   Wed Dec 10 10:31:19 2014 +0530

    MIFOSX-1735 - sub-status of Client to be fetched during read operations

[33mcommit 4caf4efb870e24abc70f06cbf5cc19c2e7ca15bb[m
Author: Anthony Liu <tlrnalra@gmail.com>
Date:   Tue Dec 9 23:42:22 2014 -0500

    Added missing @Test annotation and clarified method names

[33mcommit 38bb259f6579041175c206bfb75c0068239548c3[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Dec 9 19:10:09 2014 -0800

    updating install.md

[33mcommit 97ba2321ac43e8b163e9a9b2ebb6c55330af82d1[m
Merge: f63bd6c c1fa4be
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Dec 9 19:05:59 2014 -0800

    Merge pull request #1175 from dragonkoko/patch-1
    
    Updating Mifos Installation Platform

[33mcommit 77fd8ffe53f8c0aee23886509a48e39c1e1731b4[m
Merge: 3d020eb 279660e
Author: varunVNnayar <rajeshnair@Rajesh-Nairs-MacBook-Pro.local>
Date:   Wed Dec 10 00:00:47 2014 +0530

    Merge branch 'develop_Varun' into develop

[33mcommit 279660e21b0c1a2cfa366f00b890c433aae5621c[m
Author: varunVNnayar <rajeshnair@Rajesh-Nairs-MacBook-Pro.local>
Date:   Wed Dec 10 00:00:26 2014 +0530

    Office Suite
    
    Creation of Office Suite, OfficeIntegrationTests.java ,
    OfficeHelper.java, OfficeResourceHandler.java .

[33mcommit c1fa4be54939297684b398401d0708cf85f851a0[m
Author: Kaloyan <dragonkoko@abv.bg>
Date:   Tue Dec 9 15:27:32 2014 +0200

    Updating Mifos Installation Platform
    
    added on line 187 and 188 info about tomcat-jdbc.jar
    added from line 195 to 203 upadated

[33mcommit f63bd6c2cf364c81ab0302464791125da294ce94[m
Merge: 3d399eb 6d79d59
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Dec 9 02:01:47 2014 -0800

    Merge pull request #1173 from pramodn02/develop
    
    MIFOSX-1709 : added test cases and api documentation

[33mcommit f48f7ed0f562f3113335be314d10196080a54cf2[m
Author: MithunKashyap <mithunkashyap0206@gmail.com>
Date:   Tue Dec 9 15:22:08 2014 +0530

    Mifos-1663:added more flexibilty for center/group meeting reschedule

[33mcommit 6d79d59b745c2622dcc7a2d4d335691c818e8f48[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Tue Dec 9 09:46:25 2014 +0530

    MIFOSX-1709 : added test cases and api documentation

[33mcommit 3d399eba1072b9bce51932db83f91e099c3af44d[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Dec 8 17:50:14 2014 -0800

    MIFOSX-1728

[33mcommit 7c64de0a1c70f648c07c20e8c216c5c9e94d36d2[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Dec 8 15:23:50 2014 -0800

    renaming ambiguously named test helper methos

[33mcommit 872cbdf441a23be92aa0397ba4341d2459196552[m
Merge: 3d020eb e6402de
Author: Markus Geiß <mgeiss257@gmail.com>
Date:   Mon Dec 8 15:13:41 2014 +0100

    Merge pull request #1171 from MegaAlex/currencyTestSuite
    
    Looks good (y)

[33mcommit e6402dec73d397e27410e143fe1eed80a066b66f[m
Author: Alex Ivanov <alexivanov97@gmail.com>
Date:   Mon Dec 8 15:19:48 2014 +0200

    Create Currency test suite

[33mcommit 3d020eb6f55d33262502a5bd776990b1d2f5d758[m
Merge: 12dfd63 aeb3f05
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Dec 8 01:05:28 2014 -0800

    Merge pull request #1147 from chandrikamohith/MIFOSX-1639
    
    MIFOSX-1639 Adding field createStandingInstructionAtDisbursement in  Mod...

[33mcommit 12dfd63a1fc380a6e1b025e62fb3c3cd5bfffefe[m
Merge: af6c9e2 e5e9631
Author: Markus Geiß <mgeiss257@gmail.com>
Date:   Mon Dec 8 06:57:17 2014 +0100

    Merge pull request #1170 from AJLiu/develop
    
    Looks good (y)

[33mcommit e5e96319c2d09e3189d0cde5ce889c47eb9f8da8[m
Author: Anthony Liu <tlrnalra@gmail.com>
Date:   Sun Dec 7 04:56:23 2014 -0500

    Integration Test Suite for Currencies API
    
    Includes tests for updates and for retrieves
    
    Formatting

[33mcommit af6c9e2ed6d4b886f94b1469297f571d3d5467b1[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Dec 7 19:47:19 2014 -0800

    MIFOSX-1727

[33mcommit 452d97cfb0cd6f02fe0bfba017b2b2af06e74b25[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Dec 7 13:59:10 2014 -0800

    updates to MIFOSX-1700

[33mcommit 002ef06d514f04cd13d6989aa98323fc7d1a75ef[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sat Dec 6 21:49:20 2014 -0800

    MIFOSX-1700

[33mcommit 990cab63cf71fe5ff02da8df7cd45a180f805dd4[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sat Dec 6 10:37:39 2014 -0800

    remove warnings

[33mcommit cb48a79e249260b6172ab73c6c75d2c0e23b7ada[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sat Dec 6 10:23:22 2014 -0800

    formatting savings classes

[33mcommit 9d11434ee7ee20f97bbfdb7e2ac170976ec01360[m
Author: Binny G Sreevas <binny.gopinath@gmail.com>
Date:   Sat Dec 6 14:16:28 2014 +0530

    Branch Specific Products and Charges - initial commit

[33mcommit c7f3f416adf2d2866396c6e0140b727f742b2788[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Dec 5 22:30:24 2014 -0800

    updates to CalendarInstance Repository

[33mcommit 9f78554972d5f1dd419fb53ea7e185abcab32ef1[m
Merge: 629508d bae2a70
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Dec 5 22:14:50 2014 -0800

    Merge pull request #1164 from Nayan/MIFOSX-1680
    
    #MIFOSX-1680 Option to change center meeting frequency and intervals

[33mcommit 629508dcab411e0859cbb197694178b7b0e46ba1[m
Merge: 7105d68 c6a74a1
Author: Markus Geiß <mgeiss257@gmail.com>
Date:   Fri Dec 5 12:46:27 2014 +0100

    Merge pull request #1167 from MegaAlex/MIFOSX-1630
    
    Merged and Closed (y)

[33mcommit c6a74a1d6488328a5fe997525ec767b783ab9b86[m
Author: Alex Ivanov <alexivanov97@gmail.com>
Date:   Thu Dec 4 22:03:47 2014 +0200

    MIFOSX-1630 Bit and DateTime data type support in DataTable API

[33mcommit a3890887098631a3f90e87ac8d0b91f0f652813c[m
Author: Nagaraj Reddy <nagaraj@confluxtechnologies.com>
Date:   Fri Dec 5 15:59:34 2014 +0530

    MIFOSX-1662:Adding new statuses to Client lifecyle

[33mcommit 7105d688bcf0c3347d01dae11b462e50f8b6027b[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Dec 5 00:45:11 2014 -0800

    sql patch for client statuses

[33mcommit 642b45def85ffc1da81f4b12702eda7bbd5f0c3b[m
Merge: 27b8761 42ff4c1
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Dec 5 00:31:50 2014 -0800

    Merge pull request #1168 from binaryking/MIFOSX-1550
    
    MIFOSX-1550 Check for null principal amount when creating journal entry for loan write-off

[33mcommit 27b876101178f296485ab439c7e2feaaf1cb2488[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Dec 5 00:30:23 2014 -0800

    cleaning up client lifecycle additions

[33mcommit 42ff4c1b1ca01ecfbff2c4688f971de6578574a4[m
Author: Mohammed Nafees <nafees.technocool@gmail.com>
Date:   Fri Dec 5 01:22:44 2014 +0530

    MIFOSX-1550 Check for null principal amount when creating journal entry for loan write-off

[33mcommit 6515d18b73dacbdb58b7f8fc00c724d6c660d025[m
Merge: 2212618 8acb08c
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Dec 4 17:27:42 2014 -0800

    Merge branch 'Nagaraj12-clientReject' into develop

[33mcommit 22126182568000da30ed4b5d7176df4e85ee976b[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Dec 4 17:26:46 2014 -0800

    fix failing integration tests due to enabling optimistic locking on loans

[33mcommit 8acb08c526d0ef16236886a30af34154b6af9191[m
Merge: 34a9ec4 0d85d4b
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Dec 4 16:23:57 2014 -0800

    Merge branch 'clientReject' of git://github.com/Nagaraj12/mifosx into Nagaraj12-clientReject

[33mcommit 09e2d04a6ee5cf77e17feede95dc61fe4b885d29[m
Merge: d69e2de 65d9ca1
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Dec 4 08:35:43 2014 -0800

    Merge pull request #1163 from pramodn02/develop
    
    MIFOSX-1709 : corrected condition to add and delete guarantor

[33mcommit d69e2de0ac503d8d57de126dcec3a78c5eb7a8aa[m
Merge: 34a9ec4 cea6436
Author: Markus Geiß <mgeiss257@gmail.com>
Date:   Thu Dec 4 16:27:08 2014 +0100

    Merge pull request #1165 from binaryking/MIFOSX-1284
    
    Closed

[33mcommit cea64364703b9bca6827c5503c0a44d18eba0ef7[m
Author: Mohammed Nafees <nafees.technocool@gmail.com>
Date:   Thu Dec 4 18:37:31 2014 +0530

    MIFOSX-1284 Add the ability to sort offices

[33mcommit bae2a7088eac7dd0a1a6265e48f121f41a7af579[m
Author: Nayan <nayan.ambali@gmail.com>
Date:   Thu Dec 4 14:56:42 2014 +0530

    Option to change center meeting frequency and intervals

[33mcommit 65d9ca11bf5c2a11f16a84d0dc9120cbe1bcfe7c[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Thu Dec 4 14:43:27 2014 +0530

    MIFOSX-1709 : corrected condition to add and delete guarantor

[33mcommit 34a9ec41c5468f4e3255b9bfdf3b324caf63daec[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Dec 4 01:14:46 2014 -0800

    missing response data parameters for code values

[33mcommit 64c642656fc93d25a8822cf1ad77f0960ecd7d02[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Dec 4 01:12:21 2014 -0800

    enable optimistic locking for m_loan

[33mcommit 8298a8f86ab3db6893b8f48a89e15d4e66f16eca[m
Merge: f197fe5 118f490
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Dec 3 22:52:11 2014 -0800

    Merge pull request #1161 from pramodn02/develop
    
    MIFOSX-1709 : guarantor on hold funds functionality

[33mcommit 118f4904c6d5100cbf9ebec694358bc8c8935474[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Thu Dec 4 11:31:12 2014 +0530

    MIFOSX-1709 : guarantor on hold funds functionality

[33mcommit f197fe572a12fc40c5a2f5fdedacea0886faa7ed[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Dec 3 21:36:46 2014 -0800

    Ignore failing test case on Jenkins

[33mcommit d5ce6f2c98edeee28dda59d1e6f78434e62b1dbe[m
Merge: 2fc3c9c cc48711
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Dec 3 20:54:28 2014 -0800

    Merge pull request #1146 from emmanuelnnaa/MIFOSX-1671
    
    commit for MIFOSX-1671 (User with 'ALL_FUNCTIONS_READ' permission does not have access to /users/{userId} resource)

[33mcommit 2fc3c9c7ee97ebba2831a0570a991b7e411c12d0[m
Merge: 4ec1173 d644fa9
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Dec 3 20:51:34 2014 -0800

    Merge pull request #1155 from MithunKashyap/MIFOSX-1674
    
    MIFOSX-1674:The description field added to m_code_value can be edited/en...

[33mcommit 4ec1173bc3834ca9b42aff16ae1c7b9f4dda3915[m
Merge: 4f67c8b 91fc23e
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Dec 3 20:47:29 2014 -0800

    Merge pull request #1159 from vorburger/MariaDB4jUpgrade
    
    Upgraded MariaDB4j from v2.1.0 to v2.1.1

[33mcommit 4f67c8bf7fc5e48fbb946d683814374c859d3b9b[m
Merge: 8ef61f3 77dbe27
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Dec 3 20:47:11 2014 -0800

    Merge pull request #1160 from vorburger/ReactivateMariaD4jSpringBootServerLoginTest
    
    Un-@Igore SpringBootServerLoginTest

[33mcommit 77dbe27281a092ede1359b752ce5d266f5cd98bb[m
Author: Michael Vorburger <mike@vorburger.ch>
Date:   Thu Dec 4 00:15:57 2014 +0100

    Un-@Igore SpringBootServerLoginTest

[33mcommit 91fc23e92af2618d9a235022c9c87f383b1f8949[m
Author: Michael Vorburger <mike@vorburger.ch>
Date:   Thu Dec 4 00:13:46 2014 +0100

    Upgraded MariaDB4j from v2.1.0 to v2.1.1, see https://github.com/vorburger/MariaDB4j/blob/master/CHANGES.md

[33mcommit 8ef61f3275e0e5a6a7d4c91e98275d86a0012215[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Dec 3 12:31:59 2014 -0800

    MIFOSX-1704

[33mcommit 4c6ea532e9dfcc88231a1af83a8e9c8f4d2c50aa[m
Merge: 466a691 589b1c2
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Dec 3 09:29:40 2014 -0800

    Merge pull request #1157 from binaryking/MIFOSX-1533
    
    MIFOSX-1533: Fix advanced search exception

[33mcommit 589b1c2dbbb23abe4e16fc2d240e73be37297cc2[m
Merge: 2f5dc28 466a691
Author: Mohammed Nafees <nafees.technocool@gmail.com>
Date:   Wed Dec 3 22:21:30 2014 +0530

    Merge branch develop of upstream

[33mcommit 2f5dc2875f97291794809ee58d98b8fba4c91ed5[m
Author: Mohammed Nafees <nafees.technocool@gmail.com>
Date:   Wed Dec 3 22:11:16 2014 +0530

    MIFOSX-1533 Fix advanced search exception

[33mcommit 466a691e5664c1338b5bcfaf2819c546b470c4d6[m
Merge: c802651 9b826ea
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Dec 3 08:37:00 2014 -0800

    Merge pull request #1154 from MegaAlex/bug/MIFOSX-1575
    
    MIFOSX-1575 Add optional 'comments' parameter to Journal Reverse command

[33mcommit c80265124b38b7ab7ec47858c1646891011ab09d[m
Merge: 07052f9 8ad5324
Author: Markus Geiß <mgeiss257@gmail.com>
Date:   Wed Dec 3 14:09:07 2014 +0100

    Merge pull request #1156 from binaryking/FundsIntegrationTest
    
    [GCI Task] Funds Integration Test Suite

[33mcommit 8ad5324fb67a01aa58ea0f50b0e1a37259632b4e[m
Author: Mohammed Nafees <nafees.technocool@gmail.com>
Date:   Wed Dec 3 18:21:22 2014 +0530

    Rename global response spec to be more clear

[33mcommit c4db5b811a7043e84844c40d3240b351d8a3a091[m
Author: Mohammed Nafees <nafees.technocool@gmail.com>
Date:   Wed Dec 3 18:09:30 2014 +0530

    Change method names

[33mcommit 4dcb979b78139a08ff3fb41dddf945b4188c177c[m
Author: Mohammed Nafees <nafees.technocool@gmail.com>
Date:   Wed Dec 3 17:48:51 2014 +0530

    Distribute testing logic in separate methods

[33mcommit aaf7025d795e119298616548cc8b9234ea5f5727[m
Author: Mohammed Nafees <nafees.technocool@gmail.com>
Date:   Wed Dec 3 14:47:47 2014 +0530

    Add test for duplicate creation

[33mcommit eb8c7103ae36a155b0f25a66b5453b7395b039ea[m
Author: Mohammed Nafees <nafees.technocool@gmail.com>
Date:   Wed Dec 3 14:26:43 2014 +0530

    Create test cases to cover funds modifications, covering faulty behaviour

[33mcommit ba04ea535ed26f993b33dbf3bf82b88c3534ff9a[m
Merge: d0f214c 07052f9
Author: Mohammed Nafees <nafees.technocool@gmail.com>
Date:   Wed Dec 3 12:44:21 2014 +0530

    Merge branch develop of upstream

[33mcommit d0f214c834caaeb0719d02dc9343c4c2edb6aff6[m
Author: Mohammed Nafees <nafees.technocool@gmail.com>
Date:   Wed Dec 3 12:21:49 2014 +0530

    Directly return list of funds helpers from retrieveAllFunds()

[33mcommit a51390ae371cd47c96915b7e9dfdb7a12b32038f[m
Author: Mohammed Nafees <nafees.technocool@gmail.com>
Date:   Wed Dec 3 12:07:29 2014 +0530

    Improve testing patterns for funds retrieval

[33mcommit 07052f983ed02501f61a8e93c003836589234a1e[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Dec 2 17:33:19 2014 -0800

    cath database errors related to savings withdrawals

[33mcommit f58fc1c1162d51a166ada5af355ed51e8e2be7f9[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Dec 2 13:24:55 2014 -0800

    add optimistic locking for savings account

[33mcommit 9b826ea98ec07cf7d6e474cc6839ef35fee744a8[m
Author: Alex Ivanov <alexivanov97@gmail.com>
Date:   Tue Dec 2 21:06:53 2014 +0200

    MIFOSX-1575 Add optional 'comments' parameter to Journal Reverse command

[33mcommit 78b6f2a3e658caf4b44b6ce288ad73dd9b933148[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Dec 2 08:35:59 2014 -0800

    updating licences

[33mcommit 637f062ce3d6ce9b4e920c023fd985778b430144[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Dec 2 08:31:50 2014 -0800

    MIFOSX-1511 custom formats for client, loan and savings account numbers

[33mcommit 8f13a96f61a2d89d15ada74314db0cb88fc791f7[m
Author: Mohammed Nafees <nafees.technocool@gmail.com>
Date:   Tue Dec 2 21:26:16 2014 +0530

    Add test cases for funds retrieval

[33mcommit a15ce5ce8b606fc8474c671e3f7770ec388c6571[m
Author: Mohammed Nafees <nafees.technocool@gmail.com>
Date:   Tue Dec 2 13:39:32 2014 +0530

    Create integration test suite for Funds API

[33mcommit d644fa9123a9082874ed6c42c1d7c989eeece99f[m
Author: MithunKashyap <mithunkashyap0206@gmail.com>
Date:   Tue Dec 2 16:18:29 2014 +0530

    MIFOSX-1674:The description field added to m_code_value can be edited/entered

[33mcommit 0d85d4b9de0cc77e7a913c6d37c0ca0e355f3675[m
Author: Nagaraj Reddy <nagaraj@confluxtechnologies.com>
Date:   Fri Nov 28 16:12:25 2014 +0530

    MIFOSX-1662:Client life cycle and adding new statuses

[33mcommit 06dfb9d73295ea5fb3e4df2e58e57059fa669e92[m
Merge: f93bb4a e25ddfd
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Nov 25 19:58:43 2014 +0530

    Merge pull request #1149 from pramodn02/latest
    
    MIFOSX-1676 : changed AppUser to nullable in savings transactions

[33mcommit e25ddfd18af6a6d3b93b21725dffee29344f2948[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Tue Nov 25 18:14:41 2014 +0530

    MIFOSX-1676 : changed AppUser to nullable in savings transactions

[33mcommit f93bb4a7babf6b1881319458f81664c27a97939c[m
Merge: cd76d1d 958fb33
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Nov 24 19:17:56 2014 +0530

    Merge pull request #1148 from pramodn02/develop
    
    MIFOSX-1675 : Added support to capture guarantor funding details for loan account

[33mcommit 958fb33db212eae00e91fa2a3e8ffe649286fbd7[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Mon Nov 24 13:17:07 2014 +0530

    added licences

[33mcommit 6646ab57db30223dcca4472ee5cae205e5533093[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Mon Nov 24 12:13:04 2014 +0530

    MIFOSX-1675 : Added support to capture guarantor funding details for loan account

[33mcommit cd76d1d543880fb4501ee89f59773518927dce5c[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sat Nov 22 03:10:49 2014 +0530

    fixing failing Client integration tests

[33mcommit 1dee8c14ee3a1eabd51de8d74537bf7d44567c16[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sat Nov 22 02:47:53 2014 +0530

    fixing failing Client integration tests

[33mcommit c7a6d93bc4ca11ecc3a780e80dee27588eacab90[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sat Nov 22 00:54:22 2014 +0530

    fixing batch job failures

[33mcommit 44c9e484794e5df666b84815b408afe1c37b4558[m
Author: Binny G Sreevas <binny.gopinath@gmail.com>
Date:   Fri Nov 21 20:41:54 2014 +0530

    Opening Balances: initial commit

[33mcommit aeb3f051aa076ddcd0f98a5fe3651e45663bfbb9[m
Author: Chandrika <chandrika@confluxtechnologies.com>
Date:   Thu Nov 20 15:33:41 2014 -0500

    MIFOSX-1639 Adding field createStandingInstructionAtDisbursement in  Modify Loan

[33mcommit cc487116bb5f89965d2e843c6b072bef6963f9e0[m
Author: Emmanuel Nnaa <emmanuelnnaa@musoni.eu>
Date:   Thu Nov 20 13:13:30 2014 +0100

    commit for MIFOSX-1671 (User with 'ALL_FUNCTIONS_READ' permission does not have access to /users/{userId} resource)

[33mcommit 5afed47767ce98e3ea93adee3a90166e78c4cbd0[m
Author: Binny G Sreevas <binny.gopinath@gmail.com>
Date:   Thu Nov 20 14:35:42 2014 +0530

    Cash Management initial commit

[33mcommit 9dadb669a41129a9a0089afa842545c8509da624[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Nov 20 06:15:48 2014 +0530

    resolving compilation issue and bumping up sql patch number

[33mcommit ff8f116b96e45a9c714809ed665e4e9157876e90[m
Merge: 66c6ddc 555839d
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Nov 20 06:09:27 2014 +0530

    Merge branch 'binnygopinath-MIFOSX-1628' into develop

[33mcommit 555839df31f37227639b0b1a901a0f7d1d3c39e2[m
Merge: 66c6ddc 99dfadf
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Nov 20 06:09:17 2014 +0530

    Merge branch 'MIFOSX-1628' of git://github.com/binnygopinath/mifosx into binnygopinath-MIFOSX-1628

[33mcommit 66c6ddc82e3e1441462ae31b65c04c5a81293525[m
Merge: e420a56 094c205
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Nov 20 06:07:01 2014 +0530

    Merge branch 'MithunKashyap-ClientSubStatus_MIFOSX-1642' into develop

[33mcommit 094c205018b678355d7da13e0bda7375825fc7ca[m
Merge: e420a56 eb2741a
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Nov 20 06:06:20 2014 +0530

    Merge branch 'ClientSubStatus_MIFOSX-1642' of git://github.com/MithunKashyap/mifosx into MithunKashyap-ClientSubStatus_MIFOSX-1642

[33mcommit e420a56986a22d41f9feddce7f3246bb5cfb1ee6[m
Merge: 1c83f18 5a21aa6
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Nov 20 06:00:55 2014 +0530

    Merge branch 'emmanuelnnaa-MIFOSX-1638' into develop

[33mcommit 5a21aa6bfd2ba9b9d5b43dc0a28e39661e3b8495[m
Merge: 180c49f ef3d19c
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Nov 20 06:00:32 2014 +0530

    Merge branch 'MIFOSX-1638' of git://github.com/emmanuelnnaa/mifosx into emmanuelnnaa-MIFOSX-1638
    
    Conflicts:
    	mifosng-provider/src/main/java/org/mifosplatform/portfolio/loanaccount/domain/Loan.java

[33mcommit 1c83f181ab1d2753c9c5129232d6509413490ecf[m
Merge: 180c49f e1313fd
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Nov 20 04:15:33 2014 +0530

    Merge pull request #1140 from Musoni/MIFOSX-1655
    
    fixed MIFOSX-1655 issue with updating a loan with a charge

[33mcommit 99dfadf990cb84b134b4c295315033fc5f9ae938[m
Author: Binny G Sreevas <binny.gopinath@gmail.com>
Date:   Thu Nov 20 02:08:00 2014 +0530

    MIFOSX-1628 - Added DB upgrade script

[33mcommit c11f3af1801b9f4b3d231bbaada5f95b5385311b[m
Author: Binny G Sreevas <binny.gopinath@gmail.com>
Date:   Thu Nov 20 02:03:21 2014 +0530

    MIFOSX-1628 - Added User ID and Datetime to Loan and Savings Transactions

[33mcommit 180c49fce9a33d4a2f2317bc110ee7cce98d8c0f[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Nov 19 03:17:34 2014 +0530

    initial work around MIFOSX-1634

[33mcommit eb2741a0e2be5e78954413ddfab56e9fbc3a006a[m
Author: MithunKashyap <mithunkashyap0206@gmail.com>
Date:   Tue Nov 18 16:59:03 2014 +0530

    MIFOSX-1642: Added sub status to client resource

[33mcommit e1313fd254d0aa6d6295fefbff4f8569910cebc8[m
Author: andrew-dzak <andrewdzakpasu@musoni.eu>
Date:   Sun Nov 16 20:54:55 2014 +0100

    fixed MIFOSX-1655 issue with updating a loan with a charge

[33mcommit 61a096f33c947ca636dd3068b3c2ece2c169da60[m
Merge: 7db8147 131b073
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Nov 13 21:35:54 2014 +0530

    Merge branch 'MIFOSX-1068' of git://github.com/Nayan/mifosx into Nayan-MIFOSX-1068
    
    Conflicts:
    	mifosng-provider/src/main/java/org/mifosplatform/portfolio/loanaccount/api/LoansApiResource.java
    	mifosng-provider/src/main/java/org/mifosplatform/portfolio/loanaccount/data/LoanAccountData.java
    	mifosng-provider/src/main/java/org/mifosplatform/portfolio/loanaccount/domain/Loan.java

[33mcommit 7db8147f9dfe95dca21fe7878a0c2314eb003e81[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Nov 13 21:13:40 2014 +0530

    bump up gurantors sql

[33mcommit 77e08d11c85841e929596e0fad5b90e504827b2b[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Nov 13 21:10:16 2014 +0530

    fixing error messages for min days between loan disbursal and repayment

[33mcommit 95447d21b723174dd08a113251ec24bff9cdf016[m
Merge: 10f22ab c3e66a6
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Nov 13 20:25:48 2014 +0530

    Merge branch 'develop' of git://github.com/pramodn02/mifosx into pramodn02-develop
    
    Conflicts:
    	mifosng-provider/src/main/java/org/mifosplatform/portfolio/savings/domain/SavingsAccount.java

[33mcommit 10f22ab3eba3401ae04fcabe39a351836681952b[m
Merge: 1573b5a 0664aca
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Nov 13 20:20:00 2014 +0530

    Merge branch 'MIFOSX-1639' of git://github.com/emmanuelnnaa/mifosx into emmanuelnnaa-MIFOSX-1639
    
    Conflicts:
    	mifosng-provider/src/main/java/org/mifosplatform/portfolio/loanaccount/data/LoanAccountData.java
    	mifosng-provider/src/main/java/org/mifosplatform/portfolio/loanaccount/domain/Loan.java
    	mifosng-provider/src/main/java/org/mifosplatform/portfolio/loanaccount/service/LoanReadPlatformServiceImpl.java

[33mcommit 1573b5a6b3e11fa8eae2e5b60dadbd5f5c28e5c4[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Nov 13 20:05:45 2014 +0530

    cleaning up API docs for standing instructions

[33mcommit c4ef820ad02303613f601f00aad11f7713a1f6e1[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Nov 13 19:23:04 2014 +0530

    fix failing test cases

[33mcommit 5f5f94d5f27405a3805b03b606d599c0a2c4b857[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Nov 13 17:57:41 2014 +0530

    updates to nth day logic

[33mcommit 580eec5064a56e17a9fabe31c884e64fc7ae6e3e[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Nov 13 17:55:01 2014 +0530

    updates to nth day logic

[33mcommit 03898052a0e13c069e81865db598fdd5e28afd08[m
Merge: 7218ac3 b8b4562
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Nov 13 14:56:20 2014 +0530

    Merge branch 'develop' of github.com:openMF/mifosx into develop

[33mcommit 7218ac354e912a515f097ae9d3f3f6047938e759[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Nov 13 14:55:51 2014 +0530

    updating api docs with detail s of newly added parameter for loan products `minimumDaysBetweenDisbursalAndFirstRepayment`

[33mcommit dc72c8c5f0c764cc967dad7cba1516243b57d26c[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Nov 13 14:27:55 2014 +0530

    fixing test failures due to minimum days between disbursal and first repayment parameter being introduced in integration tests

[33mcommit b8b45620b4306be79bb151aa98caf32af523532c[m
Merge: b27e0a9 8758d6f
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Nov 13 12:15:11 2014 +0530

    Merge pull request #1136 from Nayan/MIFOSX-1650
    
    Fix for client activation within a group and JLG loan 500 error #MIFOSX-1650 #MIFOSX-1647

[33mcommit 8758d6f669b1587086054c5cd06da9fcd32285c1[m
Author: Nayan <nayan.ambali@gmail.com>
Date:   Thu Nov 13 09:50:13 2014 +0530

    Fix for client activation withing a group and JLG loan 500 error

[33mcommit b27e0a9581bca12e1da88a3c87e17a47e48600ce[m
Merge: 99b3bf1 8535b14
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Nov 11 18:53:09 2014 +0530

    Merge branch 'MIFOSX-1461' of git://github.com/cgninan/mifosx into cgninan-MIFOSX-1461
    
    Conflicts:
    	mifosng-provider/src/main/java/org/mifosplatform/portfolio/loanaccount/domain/Loan.java
    	mifosng-provider/src/main/java/org/mifosplatform/portfolio/loanaccount/loanschedule/domain/DefaultScheduledDateGenerator.java
    	mifosng-provider/src/main/java/org/mifosplatform/portfolio/loanaccount/loanschedule/domain/ScheduledDateGenerator.java

[33mcommit 0664aca90146239e883c040ba464dc45b3e3f024[m
Author: Emmanuel Nnaa <emmanuelnnaa@musoni.eu>
Date:   Thu Nov 6 16:05:13 2014 +0100

    commit for MIFOSX-1639 (Auto create standing instruction at loan disbursement)

[33mcommit ef3d19c387c9c25a30bdea143779ef15d6c60f27[m
Author: Emmanuel Nnaa <emmanuelnnaa@musoni.eu>
Date:   Thu Nov 6 14:46:28 2014 +0100

    commit for MIFOSX-1638 (Waiving charges attached to zero balance instalments - Loan Rescheduling)

[33mcommit 131b07318e9d8ecb28940f272f0bd11c9f6ff390[m
Author: Nayan <nayan.ambali@gmail.com>
Date:   Sat Nov 8 19:01:00 2014 +0530

    Option to change approved amount during loan approval

[33mcommit c3e66a6bff89d0e6516022735fbf1a80ec527832[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Fri Nov 7 15:58:38 2014 +0530

    Corrected test cases

[33mcommit 915f187bef81160a1cb75e2d12db4638db6c0cd0[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Fri Nov 7 15:56:33 2014 +0530

    MIFOSX-1643 : modified charge payment to bypass running balance check

[33mcommit 2418c09701750d655af4436cbc91ebcd284b7bd2[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Fri Nov 7 13:15:27 2014 +0530

    MIFOSX-1641: Added capture of guarantee details for loan product and added constrain on savings withdraw

[33mcommit 99b3bf1b4daad3ab9dd5749c9fc0bcfad7b4a984[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Nov 6 00:00:21 2014 +0530

    bumping up sql patch number

[33mcommit f1e9c2800b7bf44ae3c61d374a9a11f0a65aedb4[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Nov 5 23:58:03 2014 +0530

    cleaning up MIFOSX-1584

[33mcommit a8c733100bf5a26a594c93b63d7d15ddb05206a3[m
Merge: 1c49096 1c5364a
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Nov 5 18:53:31 2014 +0530

    Merge branch 'MIFOSX-1584' of git://github.com/Nayan/mifosx into Nayan-MIFOSX-1584

[33mcommit 1c490968815292d0e797e584d83077038f228a5d[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Nov 5 18:09:46 2014 +0530

    updates to accounting section of api-docs

[33mcommit b2e9ab4a9c008d34bb6ae4d568a9c4aba8903ab0[m
Merge: 16d974d 4cb8ca8
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Nov 5 17:33:49 2014 +0530

    Merge branch 'MIFOSX-1175' of git://github.com/Musoni/mifosx into Musoni-MIFOSX-1175
    
    Conflicts:
    	mifosng-provider/src/main/java/org/mifosplatform/portfolio/group/domain/Group.java
    	mifosng-provider/src/main/java/org/mifosplatform/portfolio/group/service/GroupingTypesWritePlatformServiceJpaRepositoryImpl.java

[33mcommit 16d974ded7359638ba08012dda45ceee23ae3293[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Nov 5 10:56:03 2014 +0530

    Update Contributing.md

[33mcommit c5c34138a7b0f1a1734009b8912543ab36396d42[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Nov 4 19:05:03 2014 +0530

    cleaning up MIFOSX-1463

[33mcommit 2b93590c5c07d53dd8e6a111e7f0c9efc0426538[m
Merge: 80dfd75 c8ccc96
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Nov 4 18:01:38 2014 +0530

    Merge pull request #1124 from pramodn02/develop
    
    changed future installments date not to use rest frequency

[33mcommit c8ccc96309193843cedf8287fa4b26833ab905db[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Tue Nov 4 17:02:33 2014 +0530

    changed future installments date not to use rest frequency

[33mcommit 80dfd752d751e66aee212c215f8a77fe01d3512d[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Nov 4 16:03:21 2014 +0530

    issues with latest sql patch name, removing few deprecated statement calls and other warnings

[33mcommit 630694c133535021b2606b21ad3133008efeee77[m
Merge: 34fb6ac db9278d
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Nov 4 14:22:14 2014 +0530

    Merge pull request #1123 from sughosh88/ReportPermissions
    
    Added all reports in permission table

[33mcommit db9278d1b2930dd5924b754ece344b96c2b8f986[m
Author: Sughosh <sughosh@confluxtechnologies.com>
Date:   Tue Nov 4 12:28:56 2014 +0530

    Added all reports in permission table

[33mcommit 34fb6acc44c14b6f4f1ed6eacde263148cb83e31[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Nov 4 12:18:20 2014 +0530

    fixing null pointer exception during client creation

[33mcommit 702ecd480f373384873058e20cd65bd527491319[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Nov 3 21:09:02 2014 +0530

    cleaning up group validations

[33mcommit 7d0ac6bf29f369875585327aabfe69c12f25df25[m
Merge: 6424171 cf085c6
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Nov 3 14:13:49 2014 +0530

    Merge branch 'MIFOSX-1566' of git://github.com/Nayan/mifosx into Nayan-MIFOSX-1566

[33mcommit 642417166cc1c6602cb3ef55a9a04801d95dbdac[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Nov 3 09:48:10 2014 +0530

    moving all config files directly under config folder

[33mcommit 90725569251ab9ddbdc07bc48e27904c7c639628[m
Merge: 9174a94 2134dda
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Nov 3 09:44:23 2014 +0530

    Merge pull request #1116 from Nayan/MIFOSX-1619
    
    #MIFOSX-1619 Added MifosX Eclipse preferences file into source control

[33mcommit 9174a9406c0158e7e0d7c007037c521eac1c3b5d[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Nov 2 19:25:01 2014 +0530

    updating database dump

[33mcommit 2aa18a32da6fb45ba9f39a272ce50e34b60abc17[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Nov 2 17:03:53 2014 +0530

    Update INSTALL.md

[33mcommit f05b3585e1abea85e61c92c8c53a8e332cbb28ce[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Nov 2 16:56:01 2014 +0530

    Update INSTALL.md
    
    adding emphasis

[33mcommit d576c73dbb16a9fed43d598d0b384997d9cb2902[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Nov 2 16:52:41 2014 +0530

    Update INSTALL.md
    
    got updating from a version lower than 1.25

[33mcommit 61db1cee89d65c13b967550f97bd69746fa01068[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Nov 2 16:45:57 2014 +0530

    Update INSTALL.md
    
    with minor cleanups

[33mcommit 48528bc75e6abf516ff9b8fb7626ccdb3035e9d0[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sat Nov 1 19:56:54 2014 +0530

    Update CHANGELOG.md

[33mcommit ec51ca3007053982b24a30164e65368080c48a0a[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sat Nov 1 19:56:14 2014 +0530

    Update CHANGELOG.md
    
    for 1.25.1 release

[33mcommit 77e9549a5426efff81ea6b6aa72f9a0ae421d2f7[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sat Nov 1 18:43:13 2014 +0530

    updating properties for 1.26.1 release

[33mcommit 9a8bb71235bd758feab2048cc5759d381b8757a4[m
Merge: c7cd28c 784c180
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sat Nov 1 11:26:49 2014 +0530

    Merge pull request #1119 from pramodn02/develop
    
    MIFOSX-1631 : added constrains for charges with interest recalculation

[33mcommit ed03a3078ae588d181dbba7431126cafed35dc7f[m
Author: Michael Duku-Kaakyire <michael.duku-kaakyire@miagstan.com>
Date:   Sat Nov 1 00:54:40 2014 +0200

    [MIFOSX-1514] Refund for Active Loans

[33mcommit 784c180154b8f36b0f2c1b95995beeab4c6335e2[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Fri Oct 31 23:43:19 2014 +0530

    MIFOSX-1631 : added constrains for charges with interest recalculation

[33mcommit c7cd28c340fabaacae91f1e1d0b43f6830131335[m
Merge: 8a9c05e 3bba3b9
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Oct 31 00:49:05 2014 -0700

    Merge pull request #1118 from pramodn02/develop
    
    Develop

[33mcommit 3bba3b91926d170319363db7f9c352d97c0e42c0[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Fri Oct 31 12:42:40 2014 +0530

    corrected standing instruction testcase for edge case scenario

[33mcommit 1164bf40a3e917ae4ddac9bc92628b849c6c6956[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Fri Oct 31 11:11:44 2014 +0530

    MIFOSX-1632 : corrected accruals(intriduced with fine tune) for specific due date

[33mcommit 8a9c05e2123928a1b09044fd3584c73ada7d8a40[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Oct 28 02:31:26 2014 -0700

    removing eclipse warnings

[33mcommit 2134dda13f2e20cd92a288fbf8bf90c825a6a492[m
Author: Nayan <nayan.ambali@gmail.com>
Date:   Mon Oct 27 12:17:05 2014 +0530

    Added MifosX Eclipse preferences file into source control

[33mcommit cf085c6a8da2bdc33a4826f37900f0ae370216af[m
Author: Nayan <nayan.ambali@gmail.com>
Date:   Tue Oct 14 09:55:02 2014 +0530

    Added min/max number of clients per group rule for activate/activating
    group

[33mcommit b3424921d593c12cc07db6901606e133726b8e90[m
Merge: 9863f8d 659ffa3
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Oct 26 15:22:47 2014 -0700

    Merge pull request #1114 from pramodn02/MIFOSX-1586
    
    MIFOSX-1586 : corrected overdue charges with interest calculation

[33mcommit 659ffa3e9ad0a21661fd597f910dde18a6de9684[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Sun Oct 26 13:47:48 2014 +0530

    MIFOSX-1586 : corrected overdue charges with interest calculation

[33mcommit 9863f8d6a33d2add9bae4a7b9c818f2f741d0f32[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sat Oct 25 13:00:41 2014 -0700

    fix for MIFOSX-1615

[33mcommit 1c5364ac7f04a8458788d28918a2faade23f32b7[m
Author: Nayan <nayan.ambali@gmail.com>
Date:   Fri Oct 24 16:08:30 2014 +0530

    MIFOSX-1584 Added business logic for minimum number of days between disbursal day and first repayment date

[33mcommit e61abec3de56020f686603f40bcc332551d1389e[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Oct 23 21:51:43 2014 -0700

    Updates to Charge services to filter charges based on currency used in loan/ savings products

[33mcommit 5c76d3d3777240030635bf33bd060078ae2175bb[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Oct 23 19:19:57 2014 -0700

    Cleaning up a few more method signatures on Charge Read Service

[33mcommit ba4154d8df9deebb3a5b2a656bd726d9d048d6bf[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Oct 23 19:05:03 2014 -0700

    updating properties for 1.26 release

[33mcommit 8067a288d0b7661d891a7e8b91a6b63473728bac[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Oct 23 19:02:01 2014 -0700

    cleaning up method signatures in Charge read platform service

[33mcommit a09da301c14d606d7b750463553b63dc1728e44b[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Oct 23 16:27:00 2014 -0700

    removing unnecessary authentication from ChargesRead services

[33mcommit 4cb8ca8c71527fcbd4466e2adcb628d7ed5eb921[m
Author: andrew-dzak <andrewdzakpasu@musoni.eu>
Date:   Wed Oct 22 20:38:21 2014 +0200

    MIFOSX-1175 add extra param to assign staff to group with integration test

[33mcommit 263a32a83acd0512d357fd864bfffcc175b93611[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Oct 21 19:25:54 2014 -0700

    fixing failures for Hook integration test

[33mcommit 546981a51499e83c451f05a1924b9b8ca8f03a98[m
Merge: 21c4cdf c04cebc
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Oct 21 16:34:29 2014 -0700

    Merge pull request #1091 from avikganguly01/MifosX-1466
    
    MifosX-1466 - Flexible Savings Interest Posting

[33mcommit c04cebc5b658a96a3f5d1314b6ec71b9f2123b8d[m
Author: avikganguly01 <avikganguly010@gmail.com>
Date:   Mon Oct 20 13:43:31 2014 +0530

    MifosX-1466-Flexible Savings Interest Posting

[33mcommit 21c4cdf0029d52a97f26956c4da7ef97c317f483[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sat Oct 18 16:19:45 2014 -0700

    possible fix for hooks related integration test failure

[33mcommit 733656685d3022da8e1bd6b2778959dbdb287324[m
Merge: 7c26fd7 1646995
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Oct 17 22:24:24 2014 -0700

    Merge pull request #1109 from Musoni/MIFOSX-1606
    
    fixed null pointer bug when closing savings account

[33mcommit 7c26fd7723a5f5828a261562d37aa45660603c67[m
Merge: e9d50e0 84c321c
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Oct 16 19:16:49 2014 -0700

    Merge pull request #1107 from avikganguly01/develop
    
    Hooks - Validation and Caching

[33mcommit 1646995525c715ae0773bbaa0d296ae6df1158a0[m
Author: andrew-dzak <andrewdzakpasu@musoni.eu>
Date:   Thu Oct 16 16:46:16 2014 +0200

    fixed null pointer bug when closing savings account

[33mcommit 84c321cfe871a5ef178e9d0fc3e8c3dbe21a0c28[m
Author: avikganguly01 <avikganguly010@gmail.com>
Date:   Thu Oct 16 18:18:47 2014 +0530

    Hooks - Validation and Caching

[33mcommit e9d50e09db306402d432d14cefcf8d01155c2b67[m
Merge: d4f8c63 32746c0
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Oct 16 02:43:09 2014 -0700

    Merge pull request #1108 from pramodn02/develop
    
    MIFOSX-1595 : corrected interest posting if run 2 times in a period

[33mcommit 32746c09815e23efcd963933b951722dd5b557cc[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Thu Oct 16 14:17:30 2014 +0530

    MIFOSX-1595 : corrected interest posting if run 2 times in a period

[33mcommit d4f8c632d553e8cc8b15115bfbc686e4e73c11df[m
Merge: 3847186 f9a78d8
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Oct 15 07:48:41 2014 -0700

    Merge pull request #1106 from pramodn02/develop
    
    MIFOSX-1589 : accounting changes for waive interest and waive charge

[33mcommit 3847186f2ba1ab60b17f3efbfc8553a1df4397b1[m
Merge: 76e1c81 a1cbaf7
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Oct 14 16:25:39 2014 -0700

    Merge branch 'master' into develop

[33mcommit a1cbaf792730e72300059c0d9eb0a34b82f9b335[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Oct 14 16:24:46 2014 -0700

    updating sample data for 1.25.0 release

[33mcommit f9a78d8d286a2f3ec6fc4e9c9e1a76f9103d4f81[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Tue Oct 14 21:08:40 2014 +0530

    MIFOSX-1589 : accounting changes for waive interest and waive charge

[33mcommit 76e1c819a4c26113ed50b3c04106afd9c5c115ec[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Oct 13 00:42:03 2014 -0700

    undo temporary bug fixes made for 1.25 release

[33mcommit 07d054cbec137f1a512d4a3f0440bd05c5933dd4[m
Merge: 8b36f6f 59622da
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Oct 12 23:54:42 2014 -0700

    Merge branch 'develop'

[33mcommit 59622daae41542e3c1845528260e7ff50c0e5e2d[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Oct 12 23:53:43 2014 -0700

    Update INSTALL.md

[33mcommit 8f96e18225fddeca1ca08d548862efc1c669d9f0[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Oct 12 23:47:23 2014 -0700

    Update INSTALL.md

[33mcommit d5d53ea84b35002c6ab94cd8782ec825b37e01c1[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Oct 12 23:44:06 2014 -0700

    Update INSTALL.md
    
    to fix typo caused by a github bug

[33mcommit cc712e71a40d399ed2fee4d3c0f4ca515117925b[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Oct 12 23:38:59 2014 -0700

    Update INSTALL.md
    
    with special instructions for 1.25 or higher upgrade

[33mcommit eb6af6a6a326fc1fd85ad3bab4adaf3dbd48b418[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Oct 12 22:36:20 2014 -0700

    updating schema version number for first install of mifosplatform-tenants database

[33mcommit 8b36f6f27e2d696e3da6928035985abb8e041935[m
Merge: f5077ef 511a0d8
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Oct 12 21:46:55 2014 -0700

    Merge branch 'develop'

[33mcommit f5077ef2b9c0e5a03a3e9aac26f41e07f68d5d97[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Oct 12 21:05:55 2014 -0700

    disable charge and interest waivers for periodic accrual accounting

[33mcommit 511a0d89c00c38487794b069e0266f67fcbeeead[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Oct 12 20:37:44 2014 -0700

    Update CHANGELOG.md
    
    removing issues that have not yet passed a round of testing from 1.25 release

[33mcommit 30c73e32abf3c7ca9019be246dde30b7f8d1922f[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Oct 12 20:17:46 2014 -0700

    Update CHANGELOG.md
    
    for 1.25.0 Release

[33mcommit 9f739ee476cdc20d301adc05ebbd758e2d3fa116[m
Merge: 780bdfb 0262d89
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sat Oct 11 08:48:56 2014 -0700

    Merge pull request #1102 from pramodn02/develop
    
    corrected charge payment accouting for accruals

[33mcommit 0262d897273488f32725e958f2c7a279cbb4dae8[m
Author: pramod <pramod@confluxtechnologies.com>
Date:   Sat Oct 11 21:13:59 2014 +0530

    corrected charge payment accouting for accruals

[33mcommit 780bdfb01d74e674bd6a01d42635b56673892be4[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Oct 10 07:12:30 2014 -0700

    updating dump for 1.25

[33mcommit 0bf2557cfa966b4b1b8cf74dad6c660378f2ca2c[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Oct 9 21:59:31 2014 -0700

    fixing failing test case

[33mcommit 7c6485a9680e6f0ba790675f64376f817b2787c5[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Oct 9 20:52:42 2014 -0700

    formatting searchReadPlatformService

[33mcommit e361c913ee48fa8d31f90dd24e01ec94c66a61d1[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Oct 9 20:24:03 2014 -0700

    Update CHANGELOG.md
    
    updating 1.25 release with Batch API and community app fixes

[33mcommit b18a403314a845a029379149401da2c895dbbb40[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Oct 9 05:25:48 2014 -0700

    fix for issues while converting fortuna date to localdate

[33mcommit 7abec8a1c952a6117222caa6be6b6d0bd7133893[m
Merge: d1039c1 0553cda
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Oct 7 21:01:43 2014 -0700

    Merge pull request #1099 from pramodn02/develop
    
    script to insert charges paid by for accruals

[33mcommit d1039c192929947618f58c4e2290827d937609e6[m
Merge: 5841e8f 872843a
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Oct 7 20:14:09 2014 -0700

    Merge pull request #1100 from chandrikamohith/MIFOSX-1480
    
    MIFOSX-1480 Group member changes

[33mcommit 872843a32775a561ff3bcfa64bdba4b06852b78e[m
Author: Chandrika <chandrika@confluxtechnologies.com>
Date:   Tue Oct 7 19:04:32 2014 -0400

    MIFOSX-1480 Group member changes

[33mcommit 0553cda5e31d13392b00ac4ab2ef4bb922b33d8e[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Tue Oct 7 15:37:48 2014 +0530

    script to insert charges paid by for accruals

[33mcommit 5841e8f9572754bbe60f0c1df51d80bb37ffad26[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Oct 6 05:03:41 2014 -0700

    updating release numbers for 1.25 release

[33mcommit 8db27ccb0bea57f74dee17f4d0ffc7f8255b71db[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Oct 6 04:52:14 2014 -0700

    Update CHANGELOG.md
    
    for 1.25 release

[33mcommit ba8710fd51a4ef4a93867e31bd76513d51d49e54[m
Merge: b796cae f30ad4c
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Oct 6 03:25:51 2014 -0700

    Merge pull request #1098 from pramodn02/develop
    
    MIFOSX-1513 : corrected reprocess check for loan repayment schedule

[33mcommit f30ad4ca0b87441373e8c7cc5b6322a37ca157e5[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Mon Oct 6 14:09:59 2014 +0530

    MIFOSX-1513 : corrected reprocess check for loan repayment schedule

[33mcommit b796cae43c71940d667d4e11c4512c6714ccb759[m
Merge: 9a817c0 47afc0e
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Oct 6 00:05:29 2014 -0700

    Merge pull request #1097 from pramodn02/testcase
    
    periodic accounting API changes

[33mcommit 47afc0e8a41b90887178f8c5b47933e4065a239f[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Mon Oct 6 12:25:53 2014 +0530

    periodic accounting API changes

[33mcommit 9a817c0495bfc227845fd34eb8afcfc94a842ccb[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Oct 5 10:40:51 2014 -0700

    clean up of verbiage in API docs (round 1, upto Loan rescheduling)

[33mcommit d7082eebfd4d9e0fc5306467b7d7569a5266e83e[m
Merge: fce91a5 b52ed04
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Oct 3 22:21:59 2014 -0700

    Merge pull request #1096 from vishwasbabu/develop
    
    fixing a few failing test cases due to submitted date not being set

[33mcommit b52ed04fec400cd8c1daa02286ce912ee8de485b[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Oct 3 22:20:45 2014 -0700

    fixing a few failing test cases due to submitted date not being set

[33mcommit fce91a51d3882544dc108db84b6bc21b7b30ff81[m
Merge: e458dd0 cb4a249
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Oct 3 21:50:25 2014 -0700

    Merge pull request #1004 from Musoni/MIFOSX-1400
    
     abillity to modify client submission date

[33mcommit e458dd0a4e95de4e8fd471868a617f9e765e0c2c[m
Merge: 856beec ab14373
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Oct 3 21:41:40 2014 -0700

    Merge pull request #1095 from pramodn02/develop
    
    removed unnecessary save of loan

[33mcommit ab14373f3556dfc1b31b90853d121d3d2cab7a19[m
Author: pramod <pramod@confluxtechnologies.com>
Date:   Sat Oct 4 09:48:14 2014 +0530

    removed unnecessary save of loan

[33mcommit 856beec7602a134fce0036292a82d71727857b7e[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Oct 3 12:51:26 2014 -0700

    bumping up version number for MIFOSX-1397 sql patch

[33mcommit 3a525ac671a4159827115a7f25a4b2e48cdd6ab5[m
Merge: 4ddf994 54ad317
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Oct 3 12:47:42 2014 -0700

    merging pull request for MIFOSX-1397

[33mcommit 4ddf99498e0942510dbaf9addfbabd1d4fcb9296[m
Merge: 4309773 15a9804
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Oct 3 10:41:34 2014 -0700

    merging localization changes for dates in unit test cases

[33mcommit 430977306bc04a96f00d724706c524c3fe60835d[m
Merge: f7be355 0b501d1
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Oct 1 13:22:11 2014 -0700

    Merge pull request #1090 from vorburger/warFinalizedByBootRepackage
    
    MIFOSX-1552 Fix which makes gradlew war produce an executable WAR, just like standard gradlew build does

[33mcommit f7be3555c463e9144a3ec2366656fc77a2ee8e44[m
Merge: 3e433a1 adc99d2
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Oct 1 13:20:56 2014 -0700

    Merge pull request #1092 from pramodn02/develop
    
    MIFOSX-1557 : added check for disbursement fee

[33mcommit 3e433a12c217237ff0b0dbc0fae6dfed2c509682[m
Merge: 00309de 1cac159
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Oct 1 13:20:02 2014 -0700

    Merge pull request #1093 from pramodn02/MIFOSX-1561
    
    MIFOSX-1561 : added check to validate principal collected

[33mcommit 1cac15959c49eae768fcb374a58762797126684b[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Wed Oct 1 18:33:41 2014 +0530

    MIFOSX-1561 : added check to validate principal collected

[33mcommit adc99d2decf404a9a060f439664c94a8c34507a2[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Wed Oct 1 14:26:41 2014 +0530

    MIFOSX-1557 : added check for disbursement fee

[33mcommit 0b501d1dcdc8041bb600ad72bcd8068e54ed6a00[m
Author: Michael Vorburger <mike@vorburger.ch>
Date:   Tue Sep 30 01:01:17 2014 +0200

    MIFOSX-1552 war { ... war.finalizedBy(bootRepackage) } makes gradlew war
    produce an executable WAR, just like standard gradlew build does (this
    resolves the problem mentioned in the PS on
    http://blog2.vorburger.ch/2014/09/mifos-executable-war-with-mariadb4j.html)
    
    Signed-off-by: Michael Vorburger <mike@vorburger.ch>

[33mcommit 00309de7e0cc2be2523764596d96be100abb2392[m
Author: Michael Vorburger <mike@vorburger.ch>
Date:   Mon Sep 29 23:55:44 2014 +0200

    MIFOSX-1552 Upgrading rest-assured from v1.7.2 to v2.3.3 (incl.
    reverting httpcomponents change from
    848797985af367fc2364e8174f4a2c44c47aa67b +
    b9cfcce1f901542ead27cef543e2d48eabf7305c as that wasn't the cause), as
    that fixes the "java.lang.IllegalStateException: Unsupported cookie
    spec: default at
    org.apache.http.cookie.CookieSpecRegistry.getCookieSpec(CookieSpecRegistry.java:110)"
    
    Signed-off-by: Michael Vorburger <mike@vorburger.ch>

[33mcommit ccb5717bd694a8c4f41d33044a55c5ff0b0767b0[m
Merge: a4465fb b760624
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Sep 29 11:52:59 2014 -0700

    Merge pull request #1089 from chandrikamohith/MIFOSX-1252Java
    
    MIFOSX-1252 Changes for Savings Officer API Doc

[33mcommit b760624cd44020c369b961e0cc533a6bfc642208[m
Author: Chandrika <chandrika@confluxtechnologies.com>
Date:   Mon Sep 29 14:47:56 2014 -0400

    MIFOSX-1252 Changes for Savings Officer API Doc

[33mcommit 15a9804427c285ffc896780e9373b4c85fc7df77[m
Author: mplescano <mplescano@gmail.com>
Date:   Mon Sep 29 11:24:28 2014 -0500

    Fixing for locale differents of English

[33mcommit a4465fbc15c105b00e32b4a5653c3753f15d3e0a[m
Merge: 8487979 71517d8
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Sep 26 19:21:26 2014 -0700

    Merge pull request #1086 from pramodn02/develop
    
    MIFOSX-1557 : corrected journal entries for advanced accounting with acc...

[33mcommit 848797985af367fc2364e8174f4a2c44c47aa67b[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Sep 26 18:12:44 2014 -0700

    commenting out apache httpcomponents to see if it makes any difference on the Cloudbees server

[33mcommit b9cfcce1f901542ead27cef543e2d48eabf7305c[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Sep 26 16:01:33 2014 -0700

    adding httpclient 3.1 as a dependency to check if this solves integration test failures on cloudbees

[33mcommit 3a7c8d8ebb816a21cd2247fe1ddcee1343cd8784[m
Merge: e7aadae 192d690
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Sep 26 12:56:37 2014 -0700

    Merge branch 'develop' of github.com:openMF/mifosx into develop

[33mcommit e7aadae9faf6c152b166b8a4939d8102b82add7c[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Sep 26 12:56:11 2014 -0700

    removing dependency on httpclient 4.2.3

[33mcommit 192d6902400ff4604d1b40660c362fde20e4beb6[m
Merge: 276e6b1 7ed6705
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Sep 26 12:27:18 2014 -0700

    Merge pull request #1087 from pramodn02/MIFOSX-1563
    
    MIFOSX-1563 : excluded disburse charge transaction on recalculation

[33mcommit 7ed6705f74b5e35cff73360b3b58c6d3d6f4b56e[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Fri Sep 26 19:50:32 2014 +0530

    MIFOSX-1563 : excluded disburse charge transaction on recalculation

[33mcommit 71517d87c659e6f29fb1f3bca57a85df706a4527[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Fri Sep 26 16:01:26 2014 +0530

    MIFOSX-1557 : corrected journal entries for advanced accounting with accrual

[33mcommit 276e6b186446d3ef519cf8d8c5878f9704c7c044[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Sep 26 01:42:46 2014 -0700

    Ignoring hasMifosPlatformStarted test case and shorter file name for Windows

[33mcommit 68f6ecc60de6b0d967eaebb7989100083e1ad586[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Sep 24 21:23:56 2014 -0700

    moving sql files to new folder

[33mcommit bda10bbf78c5a133d10c36eb7353dce8fa5c2372[m
Merge: a41f926 4b0658a
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Sep 24 21:06:48 2014 -0700

    Merge pull request #1079 from vorburger/SpringBootAndMariaDB4j
    
    MIFOSX-1552 Spring Boot + MariaDB4j

[33mcommit a41f926dbeab6da3c304eb91af27f20a5567b8f0[m
Merge: 114f17e 253c444
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Sep 24 14:49:08 2014 -0700

    Merge pull request #1085 from chandrikamohith/MIFOSX-1252Java
    
    MIFOSX-1252 Squashing all previous commits.Savings Officer changes

[33mcommit 253c444a4537dae71cf1fb1bcf1c7d8c2deafd81[m
Author: Chandrika <chandrika@confluxtechnologies.com>
Date:   Wed Sep 24 17:38:37 2014 -0400

    MIFOSX-1252 Squashing all previous commits.Savings Officer changes

[33mcommit 114f17e30072bc9921fcb3f60d3672f2fa607bb6[m
Merge: 57f014a d19ab1c
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Sep 24 14:32:39 2014 -0700

    Merge pull request #1083 from pramodn02/develop
    
    MIFOSX-1554 : added min balance check only for withdrawal transactions

[33mcommit d19ab1c8e4cb2c85d1da5bc01bdce266d47970ec[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Wed Sep 24 14:57:37 2014 +0530

    MIFOSX-1554 : added min balance check only for withdrawal transactions

[33mcommit 57f014a8b019f3f9897031fb1af3689c7bc09070[m
Merge: ad51032 6d5830e
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Sep 23 14:56:10 2014 -0700

    Merge pull request #1082 from pramodn02/develop
    
    interest recalculation test cases + fixes

[33mcommit 6d5830e232855ad86c5df18bfe0cb01ac247e020[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Tue Sep 23 19:27:48 2014 +0530

    added accrual test case for interest recalculation

[33mcommit 4266e5d7051c9a1bf059e3f02490ad0155be5db1[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Tue Sep 23 16:42:37 2014 +0530

    corrected early payment related issues

[33mcommit ad51032aecd5b21cbd77d97f18117ed3a3e7d248[m
Merge: 4a83cb3 0e2f0eb
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Sep 22 13:43:15 2014 -0700

    Merge pull request #1078 from vorburger/CronMethodParserDoubleSlashProblemWithWAR
    
    Fix for start-up problem if WAR is NOT unpacked

[33mcommit 4a83cb3d17a282b49a284bfa613830cdd3e8cfdc[m
Merge: 0b22529 4eb2cf6
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Sep 22 13:41:58 2014 -0700

    Merge pull request #1077 from vorburger/FlywayCoreInsteadMaven
    
    Fix Flyway Core vs. Maven plug-in mess

[33mcommit 0b22529bb9df9a837cfd242eb2b0524e422ac51d[m
Merge: 0d8bd28 598f0ba
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Sep 22 13:41:34 2014 -0700

    Merge pull request #1080 from pramodn02/develop
    
    recalculation test cases and some corrections

[33mcommit 598f0ba4b1c4317177cbba7d90b71f70ae61c588[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Tue Sep 23 01:37:36 2014 +0530

    recalculation test cases and some corrections

[33mcommit 4b0658a6851514cc9fcac2fce9c6f320154f1d56[m
Author: Michael Vorburger <mike@vorburger.ch>
Date:   Mon Sep 22 04:04:15 2014 +0200

    MIFOSX-1552 Spring Boot + MariaDB4j, read
    http://blog2.vorburger.ch/2014/09/mifos-executable-war-with-mariadb4j.html
    
    Signed-off-by: Michael Vorburger <mike@vorburger.ch>

[33mcommit 0e2f0eb85c9b81b25647e37d10dd4d14a278a869[m
Author: Michael Vorburger <mike@vorburger.ch>
Date:   Mon Sep 22 01:03:36 2014 +0200

    Fix for start-up problem if WAR is NOT unpacked (relevant e.g. for upcoming Spring Boot + MariaDB4j java -jar mifos.war

[33mcommit 4eb2cf6e21f9fba2a6737c7b3a8db2746179fd51[m
Author: Michael Vorburger <mike@vorburger.ch>
Date:   Fri Sep 19 00:31:08 2014 +0200

    Fix Flyway Core vs. Maven plug-in mess
    
    Signed-off-by: Michael Vorburger <mike@vorburger.ch>

[33mcommit 0d8bd285dcf2c17373957682a179a821bb665dc3[m
Merge: 9e13132 7e0742b
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Sep 18 03:46:48 2014 -0700

    Merge pull request #1075 from pramodn02/develop
    
    MIFOSX-1549 : corrected first repayment date

[33mcommit 7e0742b5760e14dbfa7055e580d8f114e8f76087[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Thu Sep 18 15:13:57 2014 +0530

    MIFOSX-1549 : corrected first repayment date

[33mcommit 9e1313226aaf03136cd6990b1273fbfda342a3dc[m
Merge: 5356f5b 2e42036
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Sep 17 22:28:45 2014 -0700

    Merge pull request #1074 from pramodn02/develop
    
    Develop

[33mcommit 2e4203614c3cbfdc4339d2c0a96a90552ef8d3fc[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Thu Sep 18 10:09:26 2014 +0530

    updated API documentation

[33mcommit 5356f5b45fd042e46b79a31618b718be3d0054e4[m
Merge: 7889254 68e0b87
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Sep 17 17:20:21 2014 -0700

    Merge pull request #1063 from avikganguly01/develop
    
    Hooks

[33mcommit 5b0dc5b77ba18f536c97cd9298ef91bc497e19b8[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Wed Sep 17 20:19:59 2014 +0530

    MIFOSX-1541 : corrected prepayment and interest calculation on date change

[33mcommit 68e0b873fe2d9cf30cae95ed715e7e97338d2657[m
Author: avikganguly01 <avikganguly010@gmail.com>
Date:   Wed Sep 17 19:46:01 2014 +0530

    Hooks

[33mcommit 7da550fe7cb5c594d37af396a112ce2ed9eb2967[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Wed Sep 17 13:23:13 2014 +0530

    MIFOSX-1535 : added condition to set the default value

[33mcommit 91ea2f0e2e00433f9b951c562ef31f1d90e37ba7[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Wed Sep 17 12:20:41 2014 +0530

    MIFOSX-1524 : corrected Interest waiver

[33mcommit 8535b146463ca883a18da8b62ab51e2074f51252[m
Author: Christy Ninan <cg.ninan@outlook.com>
Date:   Tue Sep 16 22:04:32 2014 -0500

    Christy - MIFOSX-1461 - Allow a monthly loan to be scheduled on n-th weekday of every month

[33mcommit 7889254e7878e8abd00525482c1f35853a5a5194[m
Merge: e7abf66 18fda5d
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Sep 15 15:03:12 2014 -0700

    Merge pull request #1072 from chandrikamohith/MIFOSX-1252Java
    
    MIFOSX-1252Java API documentation added for Loan officer and Savings Off...

[33mcommit 18fda5d5d746034941a8360148afd09d5b2a7dae[m
Author: Chandrika <chandrika@confluxtechnologies.com>
Date:   Mon Sep 15 17:29:28 2014 -0400

    MIFOSX-1252Java API documentation added for Loan officer and Savings Officer

[33mcommit e7abf6601f9c27a85eaf9cef536561b07c35ec06[m
Merge: d3b8b02 a4fae7e
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Sep 14 20:04:55 2014 -0700

    Merge pull request #1071 from vishwasbabu/develop
    
    updating fix to include update of deposit product , MIFOSX-1483

[33mcommit a4fae7efcadf753233df92e56804f2bb005c688e[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Sep 14 20:04:19 2014 -0700

    updating fix to include update of deposit product , MIFOSX-1483

[33mcommit d3b8b024254fa529c83e283317f31e8141efc07f[m
Merge: 46c51f2 66e9a57
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Sep 14 19:49:49 2014 -0700

    Merge pull request #1070 from vishwasbabu/develop
    
    fix for MIFOSX-1483

[33mcommit 66e9a570ddd31ba42a97dbc9af978866e3047ae2[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Sep 14 19:48:44 2014 -0700

    fix for MIFOSX-1483

[33mcommit 46c51f2ff5e3ecff7010cfd59fbbf27185fd3ab5[m
Merge: 1385588 39c628d
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Sep 14 11:25:47 2014 -0700

    Merge pull request #1069 from vishwasbabu/develop
    
    bumping up number of sql patch

[33mcommit 39c628ddf2afd515eb76b6cbb6b08666281ad0f0[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Sep 14 11:22:56 2014 -0700

    bumping up number of sql patch

[33mcommit 1385588083781fe85a618b5b9f179d142e4e56ed[m
Merge: 3e5db84 b098f53
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Sep 14 10:40:25 2014 -0700

    Merge pull request #1068 from vishwasbabu/develop
    
    Develop

[33mcommit b098f536270dd693c5a88157daa54a0edaa01682[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Sep 14 10:39:14 2014 -0700

    applying licence headers

[33mcommit 4b9f6f304246a19e0183f1fcaefba17c6ad40366[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Sep 14 10:36:06 2014 -0700

    formatting source code

[33mcommit 3e5db84702364e51cc422a767e1f06440d9c73dc[m
Merge: b66cce8 933ede2
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Sep 14 10:23:44 2014 -0700

    Merge pull request #1065 from avikganguly01/startinterest
    
    Start Interest Calculation Date

[33mcommit b66cce870aa48ea59078459e508e65ff48e1911e[m
Merge: f6853f6 cb745a1
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Sep 14 10:11:38 2014 -0700

    Merge pull request #1067 from pramodn02/develop
    
    MIFOSX-1537 : added original schedule to history

[33mcommit cb745a196d78e409fb93ba311b8f791f93c2eb9a[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Sun Sep 14 22:26:04 2014 +0530

    MIFOSX-1537 : added original schedule to history

[33mcommit f6853f681ac45482f39936ed4d3a7d5fd5758848[m
Merge: ba1373a 45cd2e7
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Sep 12 14:13:44 2014 -0700

    Merge pull request #1066 from pramodn02/develop
    
    MIFOSX-1517 : corrected recalculation for tranche loans

[33mcommit 45cd2e7602b0ad97a35182329408e737b8854d1f[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Fri Sep 12 19:24:46 2014 +0530

    MIFOSX-1517 : corrected recalculation for tranche loans

[33mcommit 933ede2c069436041b61554cd7a373e014ab891c[m
Author: avikganguly01 <avikganguly010@gmail.com>
Date:   Fri Sep 12 11:37:46 2014 +0530

    Start Interest Calculation Date

[33mcommit ba1373acae1503e727daf8c90da12c4726d1f5ea[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Sep 11 17:16:56 2014 -0700

    bumping version number of sql patch

[33mcommit ee3adc05ce93eb610f1862a7fe1aa267dcba7113[m
Merge: 5130fba a630d89
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Sep 11 16:48:10 2014 -0700

    Merge pull request #1064 from emmanuelnnaa/MIFOSX-1523
    
    commit for MIFOSX-1523, Loan Rescheduling module

[33mcommit a630d89549218c198b9edde3563ca5a47e54cd9a[m
Author: emmanuel <emmanuelnnaa@musoni.eu>
Date:   Mon Jul 7 12:50:19 2014 +0200

    commit for MIFOSX-1523, Loan Rescheduling module

[33mcommit 5130fba3036747abd2a3e13d7533293d876f2125[m
Merge: 80e3a24 74c36e8
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Sep 10 12:47:14 2014 -0700

    Merge pull request #1062 from vishwasbabu/develop
    
    handle eclipse warning in portfolio packages

[33mcommit 74c36e810d60e05a58fa0f566885e470df1def34[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Sep 10 12:45:43 2014 -0700

    handle eclipse warning in portfolio packages

[33mcommit 80e3a24742a4171e320df2f6b6025fd58c397166[m
Merge: 58a15a0 efdccfb
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Sep 10 12:37:31 2014 -0700

    Merge pull request #1060 from chandrikamohith/MIFOSX-1252Java
    
    MIFOSX-1252 Assign/Unassign staff for Savings account-java srcs.MIFOSX-1...

[33mcommit 58a15a0382f37280d89b2aefeff9c371463b333c[m
Merge: e276135 84ec7cd
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Sep 10 00:08:17 2014 -0700

    Merge pull request #1061 from vishwasbabu/develop
    
    clean up for MIFOSX-1145

[33mcommit 84ec7cd7b90d179c27b653658d03ce393cc1dcce[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Sep 10 00:06:25 2014 -0700

    clean up for MIFOSX-1145

[33mcommit e27613550f326147eab948c46582b732437803c2[m
Merge: 9c3b40b c96980c
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Sep 9 23:49:30 2014 -0700

    Merge pull request #1056 from emmanuelnnaa/MIFOSX-1145
    
    commit for 'MIFOSX-1145': updating datatable throws 'Data truncation: Invalid use of NULL value' SQL error

[33mcommit efdccfbb81d958ac9fc8c3be123b83505d263dc0[m
Author: Chandrika <chandrika@confluxtechnologies.com>
Date:   Tue Sep 9 11:20:31 2014 -0400

    MIFOSX-1252 Assign/Unassign staff for Savings account-java srcs.MIFOSX-1500 populating loan fields in client page-java srcs

[33mcommit 9c3b40b00a2b4981e23e20fd4984560153e46b1d[m
Merge: 527a0be 6247385
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Sep 9 07:30:34 2014 -0700

    Merge pull request #1059 from pramodn02/develop
    
    MIFOSX-1526 : added future instalments calculation

[33mcommit 6247385227a78d06a40728b3bff27054a743e4d0[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Tue Sep 9 19:56:19 2014 +0530

    MIFOSX-1526 : added future instalments calculation

[33mcommit 527a0bea5f72d2f2b55e37f309d54ebb3ba4cc65[m
Merge: 821a879 d846886
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sat Sep 6 16:29:23 2014 -0700

    Merge pull request #1057 from pramodn02/develop
    
    MIFOSX-1522 : added outstanding running balance for transactions

[33mcommit d8468865085a7e922656ff866008026d68beacff[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Thu Sep 4 13:41:03 2014 +0530

    MIFOSX-1522 : added outstanding running balance for transactions

[33mcommit 821a87961088e9c44bcdd2c068eaeb5e62417d56[m
Merge: d55c3d4 5fc958f
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Sep 3 06:58:27 2014 -0700

    Merge pull request #1055 from pramodn02/develop
    
    MIFOSX-1419 : moved capturing of rest frequency to product

[33mcommit 5fc958f2bc084f7073bae1837ddb92300f650727[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Wed Sep 3 17:03:40 2014 +0530

    corrected junits

[33mcommit f324f9ee8af23cfd932d7c4e98fe581a0152700c[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Wed Sep 3 16:12:07 2014 +0530

    MIFOSX-1419 : moved capturing of rest frequency to product

[33mcommit c96980c457d4f45e631d201f17d3d6f39a3dee48[m
Author: emmanuel <emmanuelnnaa@musoni.eu>
Date:   Wed Sep 3 11:39:28 2014 +0200

    commit for 'MIFOSX-1145': updating datatable throws 'Data truncation: Invalid use of NULL value' SQL error

[33mcommit d55c3d4521c0d5c56af92ac41ab8d4ff63019ce5[m
Merge: 8aceac3 e644d6f
Author: Michael Vorburger <mike@vorburger.ch>
Date:   Sun Aug 31 15:27:24 2014 +0200

    Merge pull request #1053 from vorburger/TravisLicenseTest
    
    Travis will now test presence of MPL license headers for each pull request it builds

[33mcommit 8aceac38602ab48a5e9d06fdaed9d5c99308b622[m
Author: Michael Vorburger <mike@vorburger.ch>
Date:   Sun Aug 31 15:22:15 2014 +0200

    Updated Contributing.md with clarification that license header presence is verified on pull requests

[33mcommit e644d6f53dbb70e6e8915fd8366d98189a678af8[m
Author: Michael Vorburger <mike@vorburger.ch>
Date:   Sun Aug 31 14:10:34 2014 +0200

    license-gradle-plugin updated + license header presence verified for each pull request via travis_build.sh now

[33mcommit 9730f9cc65fd101340c4957d1981d2b282f2d44a[m
Author: Michael Vorburger <mike@vorburger.ch>
Date:   Sun Aug 31 14:05:34 2014 +0200

    added correct / missing license MPL headers
    
    Signed-off-by: Michael Vorburger <mike@vorburger.ch>

[33mcommit 79097ca7b9a49689d4f6088bcea144f813150f07[m
Merge: 76f0645 0caed4a
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Aug 28 23:06:16 2014 -0700

    Merge pull request #1052 from pramodn02/develop
    
    MIFOSX-1490 : added interest schedule for interest first repayment strat...

[33mcommit 0caed4ac23c78d9c28e60e87f3203abcfc882722[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Fri Aug 29 11:23:25 2014 +0530

    MIFOSX-1502 : corrected existing transaction ids

[33mcommit 6c6362bf343cc40d2027b8deb257e7ae4082d359[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Thu Aug 28 22:38:20 2014 +0530

    MIFOSX-1490 : added interest schedule for interest first repayment strategies

[33mcommit 76f064503bce67ccfd11cada56c4d5d9bc2d4df8[m
Merge: e68cba5 8a9a57a
Author: Michael Vorburger <mike@vorburger.ch>
Date:   Tue Aug 26 23:23:55 2014 +0200

    Merge pull request #1026 from vorburger/RemoveTestContextXML
    
    removing (now?) apparently un-used testContext.xml

[33mcommit 8a9a57a0f45bc7e28481feabdcc83f26735e6c2c[m
Author: Michael Vorburger <mike@vorburger.ch>
Date:   Tue Aug 26 23:11:33 2014 +0200

    Removed apparently not (no longer?) used old testContext.xml
    
    Signed-off-by: Michael Vorburger <mike@vorburger.ch>

[33mcommit e68cba592919b3f120b89e6ac6add8aa1ac284ee[m
Merge: 3103f20 cd3e26f
Author: Michael Vorburger <mike@vorburger.ch>
Date:   Tue Aug 26 22:52:43 2014 +0200

    Merge pull request #1024 from vorburger/SpringConfigurationTest
    
    New SpringConfigurationTest; the first @RunWith(SpringJUnit4ClassRunner)
    @vishwasbabu and @mgeiss I'm assuming you have no objection to this one, so I've merged it.

[33mcommit 3103f20fdba6ada5c6846a04aad1eb2da4fdf22d[m
Merge: 350f313 2a8cbfd
Author: Markus Geiß <mgeiss257@gmail.com>
Date:   Sat Aug 23 17:11:52 2014 +0200

    Merge pull request #1050 from khatwaniNikhil/FIX_Server_Clean_Shutdown
    
    MIFOSX-1501 #comment Fixed Issues related to AbandonedCleanupThread and

[33mcommit 2a8cbfdbf96bb25203a34e2bef38bb9191fa6c89[m
Author: Nikhil Khatwani <nkhatwani85@gmail.com>
Date:   Fri Aug 22 12:03:27 2014 +0530

    MIFOSX-1501 #comment Fixed Issues related to AbandonedCleanupThread and
    Quartz scheduler worker Threads not stopping on mifos server shut-down.

[33mcommit 350f313411c5050930c8340219fa7a378bc10e7f[m
Merge: e2645b6 87217fb
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sat Aug 16 02:38:41 2014 -0700

    Merge pull request #1049 from pramodn02/develop
    
    MIFOSX-1418 : added regeneration of accrual entries

[33mcommit 87217fb29e79496978e4e83f3b4b0b32d8c4626f[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Sat Aug 16 14:28:53 2014 +0530

    MIFOSX-1418 : added regeneration of accrual entries

[33mcommit e2645b6461de72f5c13bdb416f68b3e0c6c40ae3[m
Merge: df4a070 c62bd61
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Aug 15 14:45:58 2014 -0700

    Merge pull request #1048 from pramodn02/develop
    
    fixes for MIFOSX-1487,MIFOSX-1486

[33mcommit c62bd61222b166431f8e4bdc6b7f8f7d93624976[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Fri Aug 15 18:18:22 2014 +0530

    MIFOSX-1486 : changed the accured till date

[33mcommit debd298a1377ec32b46e04e9d1142a60bfaf50bd[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Fri Aug 15 18:17:05 2014 +0530

    MIFOSX-1487 : changed logic to fetch accrued till

[33mcommit df4a070123e1f031a87ea0a36f4646213a8d3686[m
Author: Michael Vorburger <mike@vorburger.ch>
Date:   Thu Aug 14 23:05:10 2014 +0200

    Better error handling in case a scheduler job name listed in database
    cannot be found in the code (instead of the NPE currently happenig in
    such cases)
    
    Signed-off-by: Michael Vorburger <mike@vorburger.ch>

[33mcommit 9893563865723a9e40ecac5bda0748dcafacaf0e[m
Merge: f2e9aa7 80e1d67
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Aug 13 16:02:20 2014 -0700

    Merge pull request #1046 from pramodn02/develop
    
    MIFOSX-1421 : fixed interest recalculation with RBI strategy

[33mcommit 80e1d67862da6f8cc95e7a2a1c0c982d86df30a6[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Thu Aug 14 01:15:46 2014 +0530

    MIFOSX-1421 : fixed interest recalculation with RBI strategy

[33mcommit f2e9aa7cb0bcd84194c01e070d7e6de1b1bf9c93[m
Merge: 1d1217a 818e085
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Aug 13 09:02:20 2014 -0700

    Merge pull request #1045 from binnygopinath/savingsexternalID
    
    MIFOSX-1265 - added External ID on UI for Savings Account and added vali...

[33mcommit 818e085d3a66991e95caeeab8d7ba8455f883e6e[m
Author: Binny G Sreevas <binny.gopinath@gmail.com>
Date:   Tue Aug 12 10:47:58 2014 +0530

    MIFOSX-1265 - added External ID on UI for Savings Account and added validation for duplicate External ID

[33mcommit 1d1217accf8dd6cab0c1eedbb39c0d8c7d59991e[m
Merge: 0ef30f4 827c4c5
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Aug 8 16:22:08 2014 -0700

    Merge pull request #1044 from pramodn02/develop
    
    MIFOSX-1421 : corrected remove installment charge and add a new installm...

[33mcommit 827c4c5a0db45bb1cb33729553e8bccde1366804[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Fri Aug 8 22:28:16 2014 +0530

    MIFOSX-1421 : corrected interest for extra repayments

[33mcommit 103a4a0ee1cb4ff01563b5e8c3fe16e2cac13c90[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Fri Aug 8 10:33:51 2014 +0530

    MIFOSX-1421 : corrected remove installment charge and add a new installment charge issues

[33mcommit 0ef30f412444058ee16cd95e6213506ebfc2a0db[m
Merge: 6fc9a99 5db84b4
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Aug 7 17:51:52 2014 -0700

    Merge pull request #1025 from vorburger/ImprovedTravisGradleConsoleLog
    
    Improved the logging of test failures on TravisCI

[33mcommit 6fc9a99945e8b0453e01e60cf50d6ef9b18a1a83[m
Merge: fca9659 15c4668
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Aug 7 15:11:09 2014 -0700

    Merge pull request #1043 from pramodn02/develop
    
    MIFOSX-1421 : corrected interest calculation for different interest comp...

[33mcommit 15c46685837642bd8c25fb7dcc24b50ab404a300[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Fri Aug 8 03:37:19 2014 +0530

    MIFOSX-1421 : corrected interest calculation for different interest compounding types and corrected installment charges

[33mcommit fca9659de7671ab17bf50fbdf46973ac622f5634[m
Merge: a90bfad af86c17
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Aug 6 03:05:32 2014 -0700

    Merge pull request #1010 from Musoni/MIFOSX-1429
    
    #MIFOSX-1429 - Loans incorrectly classified as NPA

[33mcommit a90bfada78ec3f7ffa8ea880c45410d73fa5daab[m
Merge: 21ce356 34324ab
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Aug 6 03:03:13 2014 -0700

    Merge pull request #1042 from rishy/Batch-API
    
    extended API-docs and exceptions in Batch API

[33mcommit 34324ab1d6cc175f52128424c9df53023ff761c8[m
Author: rishy <rishy.s13@gmail.com>
Date:   Wed Aug 6 15:09:43 2014 +0530

    extended API-docs and exceptions in Batch API

[33mcommit 21ce356d887092570c3420b365ed8edbdabd72ac[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Aug 6 02:37:53 2014 -0700

    Merging Binny's code around MIFOSX-1468

[33mcommit 0af62967a7bf6eb22146709874911c1962c26830[m
Merge: 98cc90b 4fd78b9
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Aug 6 02:14:45 2014 -0700

    Merge branch 'binnygopinath-staffjoiningdate' into develop

[33mcommit 4fd78b9f0e33d00f7bcf4bebbd13b1dbaf52939f[m
Merge: 98cc90b 1a747ab
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Aug 6 02:14:11 2014 -0700

    Merge branch 'staffjoiningdate' of git://github.com/binnygopinath/mifosx into binnygopinath-staffjoiningdate

[33mcommit 98cc90b6969af6a22dbbc3509a3432a9e1d9bceb[m
Merge: 80146f9 482b7aa
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Aug 5 22:59:18 2014 -0700

    Merge pull request #1040 from pramodn02/develop
    
    MIFOSX-1421 : corrected loan charges

[33mcommit 482b7aa714f6282700ca0509c5d7e9f9bd571722[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Wed Aug 6 11:19:08 2014 +0530

    MIFOSX-1421 : corrected loan charges

[33mcommit 80146f95652b5074c01528314151918d382b9437[m
Merge: 817794b fd3a401
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Aug 5 17:12:31 2014 -0700

    Merge pull request #1039 from pramodn02/develop
    
    MIFOSX-1421 : loan recalculation changes

[33mcommit fd3a4018214aad92d2ab2d3fdf91fdb557186e3a[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Wed Aug 6 02:10:08 2014 +0530

    MIFOSX-1421 : loan recalculation changes

[33mcommit 817794b26b38165a0a77b65031354ecb0bfa1314[m
Merge: e3e5b0a 18f70db
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Aug 5 00:03:31 2014 -0700

    Merge pull request #1038 from vishwasbabu/develop
    
    updating contributor licence

[33mcommit 18f70dbc2eebe43247e8e812446c23d09c6b4c6c[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Aug 5 00:02:20 2014 -0700

    updating contributor licence

[33mcommit e3e5b0a97b9cbc119ef3aaffc5ce875e6db520fe[m
Merge: 4d73189 54765a9
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Aug 4 23:41:28 2014 -0700

    Merge pull request #1037 from vishwasbabu/develop
    
    applying licence for all files

[33mcommit 54765a915f40996cca6f8ab0f004dbc4c8c7b0ac[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Aug 4 23:33:07 2014 -0700

    applying licence for all files

[33mcommit 1a747ab1c0d029793fec2a2eac8b7dff95dc6740[m
Author: Binny G Sreevas <binny.gopinath@gmail.com>
Date:   Sun Aug 3 14:01:38 2014 +0530

    MIFOSX-1468 - Added field 'Joining Date' for Staff

[33mcommit 4d73189a63afe04de67b65f9f0897f43c2c5c5c7[m
Merge: 4dc7581 b2769ff
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Jul 31 17:34:27 2014 -0700

    Merge pull request #1035 from lingalamadhukar/MIFOSX-1419
    
    MIFOSX-1325 Attach a group to center

[33mcommit b2769ff21b057e713e4e4e7b5c65dd343b116046[m
Author: Madhukar <madhukar@confluxtechnologies.com>
Date:   Tue Jul 22 15:23:05 2014 +0530

    MIFOSX-1325 Attach a group to center

[33mcommit 4dc7581972ad6d75bdb3adc16fbef128b40531da[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Jul 30 23:58:33 2014 -0700

    Update Contributing.md
    
    with verbiage changes

[33mcommit 4d4f96567e1e386bce2fda810154cc7ee3b7637a[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Jul 30 23:52:28 2014 -0700

    Update Contributing.md
    
    with bullets for better readability

[33mcommit ce4ec8d035e9b08f9fa15ebe0ea82a912a9afeb9[m
Merge: c05022f 0ab49e7
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Jul 30 23:49:13 2014 -0700

    Merge pull request #1034 from vishwasbabu/develop
    
    adding Contributor checklist

[33mcommit 0ab49e72e0724bf7d57f5d03c0f779c55041ba03[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Jul 30 23:48:47 2014 -0700

    adding Contributor checklist

[33mcommit c05022f692efd2c599ce9cb17598be81ea817f45[m
Merge: fca0404 41b62af
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Jul 30 17:37:03 2014 -0700

    Merge pull request #1029 from cgninan/MIFOSX-1456
    
    MIFOSX-1456 - Wrong Field Name in saveCollectionSheet Command Response

[33mcommit fca0404019855fdb9b43fb0768f0d989625f9ac4[m
Merge: 4664220 ffbff97
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Jul 30 17:36:25 2014 -0700

    Merge pull request #1031 from rishy/Batch-API
    
    added client activation, loan approval and loan disbursal commands

[33mcommit 4664220769e041651eeae767eb79c39cd04fc3d7[m
Merge: c26381d 5ead31a
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Jul 30 17:35:42 2014 -0700

    Merge pull request #1033 from lingalamadhukar/MIFOSX-1420
    
    MIFOSX-1419,MIFOSX-1420 Interest recalculation crud operations

[33mcommit 54ad31722711e7ced09071eed32f6abe89b4bf7a[m
Author: CieyouRaoul <cieyouraoul@musoni.eu>
Date:   Wed Jul 30 12:49:46 2014 +0200

    added support for summittedon_date in saving & loan transaction

[33mcommit 5ead31ae9bca79a35d90d7943668c6caf0aa9f40[m
Author: Madhukar <madhukar@confluxtechnologies.com>
Date:   Wed Jul 23 21:41:41 2014 +0530

    MIFOSX-1419,MIFOSX-1420 Interest recalculation crud operations
    
    MIFOSX-1419 Update recalculation terms with product update
    
    MIFOSX-1419 Adding calendar for interest recalculation
    
    MIFOSX-1419 API docs and test case updates

[33mcommit ffbff972bea15cc5616420955aac868ec1c3693b[m
Author: rishy <rishy.s13@gmail.com>
Date:   Tue Jul 29 12:55:30 2014 +0530

    added client activation, loan approval and loan disbursal commands

[33mcommit c26381d594a668dcc7230e26b41aa507ceb0adde[m
Merge: b758e88 0cd593d
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Jul 22 12:52:11 2014 -0700

    Merge pull request #1028 from pramodn02/develop
    
    MIFOSX-1422 : added number of days  configuration for loan schedule

[33mcommit 41b62af4db133a6cf193a731a593d555774111a0[m
Author: Christy Ninan <cg.ninan@outlook.com>
Date:   Tue Jul 22 10:59:47 2014 -0500

    MIFOSX-1456 - Wrong Field Name in saveCollectionSheet Command Response

[33mcommit 0cd593d14596fb31009af1aceb43bfad4e430c1d[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Tue Jul 22 12:48:41 2014 +0530

    MIFOSX-1422 : added number of days  configuration for loan schedule

[33mcommit b758e88a4eed160aff02f519c9ee1a7820494e4d[m
Merge: 227dd05 f4547c2
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Jul 21 21:36:53 2014 -0700

    Merge pull request #1020 from okelekemike/develop
    
    MIFOSX-1315 Global search should also search for saving account number a...

[33mcommit 227dd05ff20edd66f046132185bdcdc46f58b6e4[m
Merge: e237a3a 1d3e1fd
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Jul 21 18:27:55 2014 -0700

    Merge pull request #1023 from Musoni/MIFOSX-1453
    
    MIFOSX-1453 done

[33mcommit e237a3a70e6f9e72d3d6c66f73e20799e1b5b556[m
Merge: 72e8b11 41bed0a
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Jul 21 18:24:04 2014 -0700

    Merge pull request #1021 from lingalamadhukar/MIFOSX-1408
    
    MIFOSX-1408 Inactivate savings charges

[33mcommit 72e8b11d57b4975fb58c67a0ee78dbf125a96cd4[m
Merge: a2aea2d 1ff83bd
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Jul 21 18:22:54 2014 -0700

    Merge pull request #1012 from lingalamadhukar/MIFOSX-1368
    
    MIFOSX-1368 RBI strategy excess repayments not repid properly

[33mcommit a2aea2da22e1538b1f7f9036d6ac52e2605ace5a[m
Merge: d45270c b8b5d91
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Jul 21 18:20:11 2014 -0700

    Merge pull request #1022 from lingalamadhukar/MIFOSX-1426
    
    MIFOSX-1446,MIFOSX-1445,MIFOSX-1426

[33mcommit cd3e26f817b96f6b90e1a8b209d9af50905c9768[m
Author: Michael Vorburger <mike@vorburger.ch>
Date:   Tue Jul 22 01:22:06 2014 +0200

    Fixed 'Not an managed type:' for JPA @Entity on CLI Gradle tests (PLEASE REVIEW - ok?)

[33mcommit f4483ab2406fb403f1d82ddab6cfa64411c06499[m
Merge: c30398d 5db84b4
Author: Michael Vorburger <mike@vorburger.ch>
Date:   Tue Jul 22 00:49:10 2014 +0200

    Merge branch 'ImprovedTravisGradleConsoleLog' into SpringConfigurationTest

[33mcommit 5db84b4470383df15a918572217002fabf456e73[m
Author: Michael Vorburger <mike@vorburger.ch>
Date:   Tue Jul 22 00:36:55 2014 +0200

    Improved the logging of test failures on TravisCI

[33mcommit c30398d84ab5378fcc65699c6b8ee4ca04b83c8c[m
Author: Michael Vorburger <mike@vorburger.ch>
Date:   Mon Jul 21 22:39:05 2014 +0200

    New SpringConfigurationTest; the first @RunWith(SpringJUnit4ClassRunner)
    @ContextConfiguration in Mifos X. More of it to come - stay tuned.
    
    Signed-off-by: Michael Vorburger <mike@vorburger.ch>

[33mcommit 1d3e1fd60b0c2a9bbbd33b3bbf4dedc93e06e3db[m
Author: andrew-dzak <andrewdzakpasu@musoni.eu>
Date:   Mon Jul 21 17:52:29 2014 +0300

    MIFOSX-1453 done

[33mcommit b8b5d91bc26d8e526295f88d093c1f669738aef4[m
Author: Madhukar <madhukar@confluxtechnologies.com>
Date:   Sun Jul 20 14:31:23 2014 +0530

    MIFOSX-1446,MIFOSX-1445,MIFOSX-1426

[33mcommit 41bed0aaceef8c0f3f591ec323a392bc52491c04[m
Author: Madhukar <madhukar@confluxtechnologies.com>
Date:   Thu Jul 17 11:48:58 2014 +0530

    MIFOSX-1408 Inactivate savings charges

[33mcommit d45270c413132ed56083f3dc73633f2d17d001de[m
Merge: 3f35a3e 5b90930
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Jul 18 19:12:06 2014 -0700

    Merge pull request #1019 from cgninan/MIFOSX-1395
    
    MIFOSX-1395 - Add Loan/Savings Product Short Name in ClientAccounts Resp...

[33mcommit 5b909305c26af082d31053b626e7a3464de63f62[m
Author: Christy Ninan <cg.ninan@outlook.com>
Date:   Fri Jul 18 11:59:27 2014 -0500

    MIFOSX-1395 - Add Loan/Savings Product Short Name in ClientAccounts Response

[33mcommit f4547c26d8323386e2eec40c54bd6a72877437df[m
Author: okelekemike <okelekemike@gmail.com>
Date:   Fri Jul 18 09:28:01 2014 +0100

    MIFOSX-1315 Global search should also search for saving account number and external_id fields

[33mcommit 3f35a3e4cf4071508a45e8963ad2bf4df31c955c[m
Merge: 241a02b ece576e
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Jul 17 16:41:19 2014 -0700

    Merge pull request #906 from vorburger/SpringMajorJUnitMinorVersionsBump
    
    bumped Spring major version 3.2.5 -> 4.0.4 (+ JUnit 4.10->4.11)

[33mcommit 241a02bf0f564e482a62a9a93bb838929cf9595b[m
Merge: e3d4f08 747305c
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Jul 17 09:12:30 2014 -0700

    Merge pull request #1017 from rishy/Batch-API
    
    bug fixes, added api docs for Batch API and Client activation command

[33mcommit e3d4f08a7feaad9df7f2eea13fd002ab936f85b6[m
Merge: c77b457 456e30b
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Jul 17 09:11:27 2014 -0700

    Merge pull request #1018 from pramodn02/develop
    
    MIFOSX-1417 - corrected accruedTill date internally for calculations

[33mcommit 456e30bf41d222536ef3f26828831e685ca2c697[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Thu Jul 17 19:04:24 2014 +0530

    MIFOSX-1417 - corrected accruedTill date internally for calculations

[33mcommit 747305cfc9e6e43f83a903828c60afae12507844[m
Author: rishy <rishy.s13@gmail.com>
Date:   Thu Jul 17 12:19:45 2014 +0530

    api docs for Batch API, added Client activation command

[33mcommit c77b4573b680bc661e27a050ebc581bdb0e525b0[m
Merge: 95afc02 4051afd
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Jul 16 17:23:00 2014 -0700

    Merge pull request #1015 from pramodn02/develop
    
    MIFOSX-1416,MIFOSX-1417 : Periodic accrual changes

[33mcommit 95afc0238d7440bc49fff3d8136df9ef317ed93b[m
Merge: 65fa4d3 bd6fd35
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Jul 16 15:54:34 2014 -0700

    Merge pull request #1013 from lingalamadhukar/MIFOSX-1411
    
    MIFOSX-1411 Fix for RD Withdrawal template

[33mcommit 65fa4d33df89cae5fd0463ead289d7f19a54c408[m
Merge: 348c850 40665f0
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Jul 16 15:40:40 2014 -0700

    Merge pull request #1016 from chandrikamohith/MIFOSX-1289epic
    
    MIFOSX-1289 MIFOSX-1290 MIFOSX-1291 Spelling mistake and capitalization ...

[33mcommit 348c850087656e410103b5c6dfc8d65cb12f4c8b[m
Merge: 6cdd332 eac581c
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Jul 16 15:39:17 2014 -0700

    Merge pull request #1001 from rishy/Batch-API
    
    added create and collect charges command

[33mcommit 6cdd3322def121043dea253bda4f3becff69bdfb[m
Merge: ec7b7c0 a6c463d
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Jul 16 15:35:43 2014 -0700

    Merge pull request #1007 from lingalamadhukar/MIFOSX-1403
    
    MIFOSX-1424 and api-docs update for close savings account

[33mcommit 40665f0b660a3fe948442006af49d12590f6c254[m
Author: Chandrika <chandrika@confluxtechnologies.com>
Date:   Wed Jul 16 15:04:42 2014 +0530

    MIFOSX-1289 MIFOSX-1290 MIFOSX-1291 Spelling mistake and capitalization done for options under Charge time type dropdown

[33mcommit 4051afd5835bfa93cbe5a2609eff123ece475cb6[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Tue Jul 15 21:43:55 2014 +0530

    MIFOSX-1417 : added api to run periodic accrules till specific date

[33mcommit 40b005cbfa836175a8548b38c60a53b672dbed75[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Mon Jul 14 22:51:12 2014 +0530

    MIFOSX-1416 : added batch job for periodic accrual

[33mcommit bd6fd35a20d586d91808c253c7217900c3c35629[m
Author: Madhukar <madhukar@confluxtechnologies.com>
Date:   Tue Jul 15 14:57:24 2014 +0530

    MIFOSX-1411 Fix for RD Withdrawal template

[33mcommit ec7b7c0fd63aace21b58c0e7765f33ec92daafec[m
Merge: c25805a 3771075
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Jul 15 00:49:29 2014 -0700

    merging Michael's pull requests around MIFOSX-1184

[33mcommit 1ff83bd7e8c02731892fa70ba48923993c62662d[m
Author: Madhukar <madhukar@confluxtechnologies.com>
Date:   Fri Jul 4 08:45:21 2014 +0530

    MIFOSX-1368 RBI strategy excess repayments not repid properly

[33mcommit c25805a80403169222655f18f4e5c30e8fcc5b0b[m
Merge: b5f7cc5 7ebabc1
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sat Jul 12 23:28:45 2014 -0700

    Merge pull request #1005 from pramodn02/develop
    
    MIFOSX-1415 : Modified loan to capture last accrued date

[33mcommit b5f7cc5f22b30b8084b38e989c134df48fa68df5[m
Merge: 6df0c28 d9e1ba8
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sat Jul 12 23:27:26 2014 -0700

    Merge pull request #1008 from lingalamadhukar/develop
    
    Updating AMI for 1.24 release

[33mcommit af86c1727fd4ac253ceffcabc93f4d1e4d322ad1[m
Author: Sander van der Heyden <sander@musoni.eu>
Date:   Fri Jul 11 12:05:09 2014 +0200

    #MIFOSX-1429 - Loans incorrectly classified as NPA

[33mcommit d9e1ba8139b94b4b660b4036b7a0c026ca0bb174[m
Author: Madhukar <madhukar@confluxtechnologies.com>
Date:   Fri Jul 11 11:05:58 2014 +0530

    Updating AMI for 1.24 release

[33mcommit 6df0c28df6cf46e27404acb39a4f155dcf1c01e1[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Jul 10 19:14:16 2014 -0700

    Update CHANGELOG.md

[33mcommit 64b142f47808fac6587edd0c9f05d6c82de62d27[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Jul 10 18:39:09 2014 -0700

    update sample data for 1.24 release

[33mcommit f0f2583959221cbb1f4035089dd7f927771652c9[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Jul 10 14:50:54 2014 -0700

    Update CHANGELOG.md
    
    with a few more issues foxed for 1.24 release

[33mcommit a6c463d5253610ac995dc6ae044479bb59557ada[m
Author: Madhukar <madhukar@confluxtechnologies.com>
Date:   Thu Jul 10 21:50:18 2014 +0530

    MIFOSX-1424 and api-docs update for close savings account

[33mcommit eac581c48bf0b95a00b46aa05e0c7a8603f68d8e[m
Author: rishy <rishy.s13@gmail.com>
Date:   Wed Jul 2 15:44:41 2014 +0530

    added create and collect charges Command and support for nested dependencies

[33mcommit e3658a5a8fd79df600ec70e2f71b4133a84531c3[m
Merge: 8edcaf9 14989c0
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Jul 10 00:50:40 2014 -0700

    Merge pull request #1006 from lingalamadhukar/MIFOSX-1403
    
    MIFOSX-1398 update api-docs and fix jenkins issues

[33mcommit 14989c0ccf2ef240f712c88ff265bccfa1b3ec23[m
Author: Madhukar <madhukar@confluxtechnologies.com>
Date:   Thu Jul 10 12:32:13 2014 +0530

    MIFOSX-1398 update api-docs and fix jenkins issues

[33mcommit 8edcaf934e0b7c13f2f41d8e7c447afa64e33337[m
Merge: 3c8a9c2 9599c11
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Jul 9 20:17:06 2014 -0700

    Merge pull request #1003 from lingalamadhukar/MIFOSX-1403
    
    MIFOSX-1398,MIFOSX-1403

[33mcommit 7ebabc10f6bcb612ce05b08384148a1fc0848b8d[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Wed Jul 9 23:08:57 2014 +0530

    MIFOSX-1415 : Modified loan to capture last accrued date

[33mcommit cb4a24995868fec4c59823c3f1fabe38a176acd6[m
Author: CieyouRaoul <cieyouraoul@musoni.eu>
Date:   Wed Jul 9 15:32:33 2014 +0200

     abillity to modify client submission date

[33mcommit 9599c1197ac2d281dac5f054b281b6a654cd9bcb[m
Author: Madhukar <madhukar@confluxtechnologies.com>
Date:   Wed Jul 9 15:28:08 2014 +0530

    MIFOSX-1398,MIFOSX-1403

[33mcommit 3c8a9c232dc50ff3b1bedcef239ab85b8c64172e[m
Merge: 24fb2d4 ae1cbf3
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Jul 9 02:33:04 2014 -0700

    Merge pull request #1002 from vishwasbabu/develop
    
    updating properties for 1.24 release

[33mcommit ae1cbf371eb9d56e3c6397a771c9444299b343a7[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Jul 9 02:31:52 2014 -0700

    updating properties for 1.24 release

[33mcommit 24fb2d489de0bfd3ce94fcce677416335d281eb7[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Jul 9 01:51:54 2014 -0700

    Update CHANGELOG.md
    
    for 1.24.o Release

[33mcommit 21db526584b1a1e7e0ab5898e362f224ab958bab[m
Merge: 958c27c 0389bde
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Jul 6 21:20:55 2014 -0700

    Merge pull request #1000 from lingalamadhukar/MIFOSX-1358
    
    MIFOSX-1394 overdraft account interest calculation fix

[33mcommit 0389bde94e2bc7746fc1e26b8dfdd05dfd139783[m
Author: Madhukar <madhukar@confluxtechnologies.com>
Date:   Wed Jul 2 09:52:18 2014 +0530

    MIFOSX-1394 overdraft account interest calculation fix

[33mcommit 958c27c443649e0bb0c0e12b422d4b844b531aa6[m
Merge: 6745cf6 5cfc7e8
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Jul 3 15:04:59 2014 -0700

    Merge pull request #998 from lingalamadhukar/MIFOSX-1396
    
    MIFOSX-1396 Delete code displaying dataintegration issue

[33mcommit 6745cf628020347865df9e368de9220c1857249f[m
Merge: 530301c 38481b0
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Jul 3 11:35:08 2014 -0700

    Merge pull request #999 from vishwasbabu/develop
    
    Cleaning up inconsistencies in ClientDataValidator.java

[33mcommit 38481b03cfbc75974feeac439d1562d40046665a[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Jul 3 11:33:24 2014 -0700

    Cleaning up inconsistencies in ClientDataValidator.java

[33mcommit 530301cc5936357dafefbfa9f85e38423697dc77[m
Merge: ef7c7c1 3c97fcc
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Jul 3 11:16:59 2014 -0700

    Merge pull request #990 from Musoni/MIFOSX-1379
    
    allow null value for loanProduct principal

[33mcommit 5cfc7e85c588967f7d7435b1408ccde230f11f0a[m
Author: Madhukar <madhukar@confluxtechnologies.com>
Date:   Thu Jul 3 12:06:12 2014 +0530

    MIFOSX-1396 Delete code displaying dataintegration issue

[33mcommit ef7c7c15a3a57bc9075a8a4836c8f21efc8183e8[m
Merge: a30f46f b385a95
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Jul 2 14:49:22 2014 -0700

    Merge pull request #994 from Musoni/MFISOX-1401
    
    fixed inArrearsTolerance validation , make it allows value 0

[33mcommit a30f46f5d89bc98fecfc8336feec140d2a735906[m
Merge: 78284b8 6a14031
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Jul 2 14:47:14 2014 -0700

    Merge pull request #996 from vishwasbabu/develop
    
    cleaning up MIFOSX-1399

[33mcommit 6a14031ac3b7d48114860d68d119a59b5b3b92eb[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Jul 2 14:46:25 2014 -0700

    cleaning up MIFOSX-1399

[33mcommit 78284b8115dab899cbb8e1b3070ea4aabd1508b4[m
Merge: b4c3e8b 7c76541
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Jul 2 14:38:38 2014 -0700

    Merge pull request #992 from Musoni/MIFOSX-1399
    
    fix adding new guaraton to loan account having existing guarantor

[33mcommit b385a95a3033c61701e9c61f86c48429f6f3c14f[m
Author: CieyouRaoul <cieyouraoul@musoni.eu>
Date:   Wed Apr 23 14:10:32 2014 +0200

    fixed inArrearsTolerance validation , make it allows value 0

[33mcommit 7c76541a2d31031a8d1df38fca82a268b56b5dc4[m
Author: CieyouRaoul <cieyouraoul@musoni.eu>
Date:   Wed Jul 2 12:16:52 2014 +0200

    fix adding new guaraton to loan account having existing guarantor

[33mcommit 3c97fccee80b18eb143be3b42746b4c1f66ca635[m
Author: CieyouRaoul <cieyouraoul@musoni.eu>
Date:   Wed Jul 2 10:39:49 2014 +0200

    allow null value for loanProduct principal

[33mcommit b4c3e8be644a3473587ae2fc91d8062ae9566e71[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Jul 1 19:20:28 2014 -0700

    update api-docs for MIFOSX-1161

[33mcommit 83ae893088a648aecf0e61dff1c88c6cb5b5242a[m
Merge: 1b60601 b0df80e
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Jul 1 19:07:48 2014 -0700

    Merge pull request #987 from vishwasbabu/develop
    
    MIFOSX-1161 clean ups

[33mcommit b0df80e2a2d041559c65147bab885dde97654fb6[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Jul 1 19:06:30 2014 -0700

    MIFOSX-1161 clean ups

[33mcommit 1b60601664b75167b44f547d7fe46ab12677c53e[m
Merge: 342a0ed 4975a59
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Jul 1 13:12:49 2014 -0700

    Merge pull request #986 from lingalamadhukar/MIFOSX-1358
    
    MIFOSX-1275 refactoring endpoint of standinginstruction history

[33mcommit 4975a59b7efd8d506c9edb71f42c4de51530d03b[m
Author: Madhukar <madhukar@confluxtechnologies.com>
Date:   Tue Jul 1 11:06:26 2014 +0530

    MIFOSX-1275 refactoring endpoint of standinginstruction history

[33mcommit 342a0edc24794f466d3e67a4e341d8593e317d07[m
Merge: 85207ae 07c39d4
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Jun 30 18:28:26 2014 -0700

    merging Mardukars work arounf standing instructions

[33mcommit 85207ae2732857ca17777370c93e055a2d0d7a43[m
Merge: b2bf8cb 55ef662
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Jun 30 15:33:14 2014 -0700

    Merge pull request #983 from pramodn02/develop
    
    MIFOSX-1392 : changes for minBalanceForInterestCalculation

[33mcommit 07c39d49af8393f98d78ca3ef081b7bc9f699bda[m
Author: Madhukar <madhukar@confluxtechnologies.com>
Date:   Mon Jun 30 21:06:28 2014 +0530

    MIFOSX-1389, MIFOSX-1275 Adding apidocs

[33mcommit 55ef6628c1de79f60e400fb9f1121264c53b3adb[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Mon Jun 30 23:54:48 2014 +0530

    MIFOSX-1392 : changes for minBalanceForInterestCalculation

[33mcommit b2bf8cbe95d993f13a41be6532614ccd5c6dbe37[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Jun 27 01:08:16 2014 -0700

    fix test failures

[33mcommit ffb5dc3715eaf74ced4ac9d55c2ac31af8435430[m
Merge: 3ac3a0f 7f5ff57
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Jun 26 23:42:34 2014 -0700

    Merging @rainerhessmer  changes around MIFOSX-1161

[33mcommit 3ac3a0ff5f638f8d424dbc1a5a61792adb77af15[m
Merge: 5a06119 44a235d
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Jun 26 23:21:08 2014 -0700

    Merge pull request #982 from vishwasbabu/develop
    
    updating documentation for MIFOSX-1324

[33mcommit 5a061198118f056ca11ecd05e5e8e3b82228debf[m
Merge: b852af1 f51cf6f
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Jun 26 23:02:36 2014 -0700

    Merge pull request #981 from pramodn02/develop
    
    MIFOSX-1275 : added table to maintain execution history of standing inst...

[33mcommit f51cf6f4feae3ed7d7d53c1d555596b86a50152f[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Fri Jun 27 11:16:28 2014 +0530

    MIFOSX-1275 : added table to maintain execution history of standing instructions

[33mcommit 44a235d329cb4208c916aa5fcd8bf324955bece3[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Jun 26 19:50:55 2014 -0700

    updating documentation for MIFOSX-1324

[33mcommit b852af14d350dcd8d96e5fef90094b622509885c[m
Merge: 1e89de1 16fc259
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Jun 26 13:25:35 2014 -0700

    Merge pull request #979 from lingalamadhukar/MIFOSX-1358
    
    Update Install.md

[33mcommit 1e89de1807d9c5515f4e65ee0807c0c731a8d4f9[m
Merge: 45a3729 ef43647
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Jun 26 13:24:30 2014 -0700

    Merge pull request #980 from rishy/Batch-API
    
    added ApplySavings command in Batch-API

[33mcommit ef43647a105ab783e1ca7869fdb23882b3768eae[m
Author: rishy <rishy.s13@gmail.com>
Date:   Wed Jun 25 11:59:59 2014 +0530

    added ApplySavings command

[33mcommit 16fc259540c60c8a557fb67a9a5439b967727bf9[m
Author: Madhukar <madhukar@confluxtechnologies.com>
Date:   Thu Jun 26 15:34:37 2014 +0530

    Update Install.md

[33mcommit 45a372944c45cbb68b32720cadee8723ec7f7838[m
Merge: 1eff0f1 fc2d90d
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Jun 25 20:42:42 2014 -0700

    Merge pull request #978 from lingalamadhukar/MIFOSX-1358
    
    MIFOSX-1358,MIFOSX-1357

[33mcommit fc2d90db7b57d4cb444310969f99be4ec34d7fac[m
Author: Madhukar <madhukar@confluxtechnologies.com>
Date:   Thu Jun 26 06:18:31 2014 +0530

    MIFOSX-1358,MIFOSX-1357

[33mcommit 1eff0f138e139d8ac1a30394e7c0ae347b749327[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Jun 25 16:45:37 2014 -0700

    Update INSTALL.md
    
    with details of right version of JDK

[33mcommit b1cf0b5e59a44cece239fa75579381115b5d7753[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Jun 25 16:42:23 2014 -0700

    removing warnings after upgrade to java 7

[33mcommit 722aceccc7e1f06b9a7363e78de92c2c796a3fea[m
Merge: 06448cc 0e62a7d
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Jun 25 11:53:06 2014 -0700

    Merge pull request #977 from pramodn02/develop
    
    MIFOSX-1157 : updated API doc for RD accounts

[33mcommit 0e62a7de81c48d2b830d54b967216c84b29f316d[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Thu Jun 26 00:12:32 2014 +0530

    MIFOSX-1157 : updated API doc for RD accounts

[33mcommit 06448ccb54de5bcc16ac3e5f30cdd8f493b90b14[m
Merge: cdd724f 5481d20
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Jun 25 08:51:17 2014 -0700

    Merge pull request #976 from lingalamadhukar/MIFOSX-1235
    
    MIFOSX-1369 and update groupnamesbystaff report to core report

[33mcommit 5481d2033a5d3f5edb853fad993620ea7eb27240[m
Author: Madhukar <madhukar@confluxtechnologies.com>
Date:   Wed Jun 25 12:50:27 2014 +0530

    MIFOSX-1369 and update groupnamesbystaff report to core report

[33mcommit cdd724f6016a1577b1993b512c75e3bb16c69f46[m
Merge: 2e9604c c0b92c9
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Jun 25 00:02:28 2014 -0700

    Merge pull request #975 from pramodn02/MIFOSX-1359
    
    MIFOSX-1359 : refactored savings write services

[33mcommit c0b92c97afa99e781805e03cf0e84358233b1c3f[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Wed Jun 25 12:25:01 2014 +0530

    MIFOSX-1359 : refactored savings write services

[33mcommit 2e9604c12687d64a3bcc62a7217bea2a8b4b7165[m
Merge: 81e847a 5474e87
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Jun 24 21:26:56 2014 -0700

    Merge pull request #973 from pramodn02/MIFOSX-1340
    
    MIFOSX-1340 : corrected schedule generator to use calendar instance

[33mcommit 81e847aada2a4fa6385fe654fe0884353ab88c0a[m
Merge: 5664ac3 2d7b1d9
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Jun 24 21:26:02 2014 -0700

    Merge pull request #974 from lingalamadhukar/MIFOSX-1235
    
    MIFOSX-1235,MIFOSX-1365,MIFSOX-1204

[33mcommit 5664ac3f98b4dfc71d02acccdf04c028f1d60ed1[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Jun 24 18:07:27 2014 -0700

    Update INSTALL.md

[33mcommit b5ed7fecf71898566190662ec10dc73ff64a1caf[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Jun 24 18:05:54 2014 -0700

    Update INSTALL.md
    
    with amazon image details

[33mcommit 5474e87227209fe57375607e52c47f0e73b37008[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Tue Jun 24 23:51:10 2014 +0530

    MIFOSX-1340 : corrected schedule generator to use calendar instance

[33mcommit 2d7b1d9cf4c4ce54aa1ceb99c40bcd27e896394d[m
Author: Madhukar <madhukar@confluxtechnologies.com>
Date:   Tue Jun 24 18:27:42 2014 +0530

    MIFOSX-1235,MIFOSX-1365,MIFSOX-1204

[33mcommit 5ae0e0f62c757a3e8b1119515c13e143dc41b4a2[m
Merge: cb52335 fc0bb38
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Jun 24 11:01:33 2014 -0700

    Merge pull request #972 from rishy/Batch-API
    
    fixed integration tests in BatchApiTest

[33mcommit fc0bb3897dad7f45bd33c79a65a54adcf0b741fa[m
Author: rishy <rishy.s13@gmail.com>
Date:   Tue Jun 24 23:27:08 2014 +0530

    fixed integration tests in BatchApiTest

[33mcommit cb52335e234288b0c27be5b3975365718f64d09e[m
Merge: d67bf61 da20683
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Jun 24 10:11:40 2014 -0700

    Merge pull request #971 from pramodn02/MIFOSX-1339
    
    MIFOSX-1339 : removed reinvest option for premature close

[33mcommit d67bf61d1c81f5c23fa3dcef0cee6339db33a3d7[m
Merge: d1575d9 0d2abd3
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Jun 24 10:05:25 2014 -0700

    Merge pull request #970 from pramodn02/MIFOSX-1341
    
    MIFOSX-1341 : added deposit type in savings data

[33mcommit da206837153509336e920e12b1a16452710a7b1b[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Tue Jun 24 22:11:20 2014 +0530

    MIFOSX-1339 : removed reinvest option for premature close

[33mcommit 0d2abd3b6001d3d5f7f6cafd779be5a0514930cd[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Tue Jun 24 21:24:47 2014 +0530

    MIFOSX-1341 : added deposit type in savings data

[33mcommit d1575d9cbb29fd4cb5f1d3fdb8753f678c558c84[m
Merge: 3aa3699 0fd87bf
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Jun 23 15:16:39 2014 -0700

    Merge pull request #968 from rishy/Batch-API
    
    added "UpdateClient" and "ApplyLoan" commands with dependency resolution

[33mcommit 3aa3699eaa34c98e8cc8ebbafc99341abd0d100d[m
Merge: a2ba251 a152cb6
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Jun 23 15:12:22 2014 -0700

    Merge pull request #969 from pramodn02/MIFOSX-1353
    
     MIFOSX-1353 : corrected monthly charge calculation

[33mcommit a152cb66988ecdf97fe0355f2650ff3c5956526e[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Mon Jun 23 23:59:51 2014 +0530

     MIFOSX-1353 : corrected monthly charge calculation

[33mcommit 0fd87bf95123ce4df848508c916c3a0a3e066d37[m
Author: rishy <rishy.s13@gmail.com>
Date:   Mon Jun 23 17:01:29 2014 +0530

    added updateClient and ApplyLoan commands with dependency resolution

[33mcommit a2ba251b839661216de05495d404f4410c815a76[m
Merge: 01c05ab 199dbfd
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Jun 22 15:08:33 2014 -0700

    Merge pull request #967 from vishwasbabu/Nayan-MIFOSX-12352
    
    Nayan mifosx 12352

[33mcommit 199dbfd417ae630f0ae91055eece8f3e9e00dddd[m
Merge: 01c05ab 602f0bd
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Jun 22 15:04:47 2014 -0700

    merge Nayan's work around weekly charges

[33mcommit 01c05abad6044d155aff1b0e7aaa9fca2f8bdad1[m
Merge: f9dfd40 317363d
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sat Jun 21 22:45:35 2014 -0700

    Merge pull request #932 from rishy/Batch-API
    
    Batch-API added with 'Create Client Command' and transaction scope

[33mcommit f9dfd409db67c3ee9276731b3593bc33985750d9[m
Author: blingdahl <ben@lindahl.com>
Date:   Sat Jun 21 22:32:58 2014 -0700

    Also adds an output parameter to allow viewing images directly in the browser (values octet to download as an attachment, and inline_octet to view inline)

[33mcommit c2c2f65c1349fd27086d97344000f4e36ab64e90[m
Merge: e0e4b1d 43a3359
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Jun 20 02:11:51 2014 -0700

    Merge pull request #966 from vishwasbabu/develop
    
    MIFOSX-1364

[33mcommit 43a3359ac2ed23ef187ff201088ecdee6a3477b8[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Jun 20 02:11:05 2014 -0700

    MIFOSX-1364

[33mcommit e0e4b1db43f122ac58eac4773c4326c0fb3a0dff[m
Merge: 60e2af3 f23a0cb
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Jun 19 20:48:25 2014 -0700

    Merge pull request #965 from vishwasbabu/develop
    
    renaming a couple of methods for better readability in SavingsAccountCha...

[33mcommit f23a0cb18b5dec9f2fa1cbb86391456561822fc8[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Jun 19 20:46:51 2014 -0700

    renaming a couple of methods for better readability in SavingsAccountCharge.java

[33mcommit 60e2af384bf80f045d45a675c5bb3813037f876f[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Jun 19 20:03:11 2014 -0700

    Update INSTALL.md
    
    with details of amazon image

[33mcommit 722e657ea9ffdf6a90aa4ba93fdc6511270e714b[m
Merge: 4e84934 c26bac9
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Jun 18 20:44:06 2014 -0700

    Merge pull request #964 from pramodn02/MIFOSX-1200
    
    added Override method for getEffectiveInterestRateAsFraction

[33mcommit 4e84934110f6cd8178f20a0de68105b4c4164197[m
Merge: f7204f3 8a4fa86
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Jun 18 20:43:33 2014 -0700

    Merge pull request #963 from lingalamadhukar/MIFOSX-1349
    
    MIFOSX-1351,MIFOSX-1349

[33mcommit f7204f3905a8eb3bb1867ff78003edcf708b8886[m
Merge: 9aa9473 9c9c91b
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Jun 18 14:07:42 2014 -0700

    Merge pull request #962 from pramodn02/MIFOSX-1272
    
    MIFOSX-1272 : added recalculation of charges for diff disburse amount

[33mcommit c26bac92d8778f012d91d02478660816746f79f9[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Wed Jun 18 17:16:25 2014 +0530

    added Override method for getEffectiveInterestRateAsFraction

[33mcommit 8a4fa86f038decd498dc53796f72fe45558cc61f[m
Author: Madhukar <madhukar@confluxtechnologies.com>
Date:   Wed Jun 18 15:41:13 2014 +0530

    MIFOSX-1351,MIFOSX-1349

[33mcommit 9c9c91b44ab1c2fbb5d5c35a8f2a4b83a450daf7[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Wed Jun 18 11:50:51 2014 +0530

    MIFOSX-1272 : added recalculation of charges for diff disburse amount

[33mcommit 9aa9473c8cea2c23e4f6689a3e123c6136ca2371[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Jun 18 00:17:24 2014 -0700

    Update CHANGELOG.md
    
    for 1.23.1. Release

[33mcommit 9126e40ddc1d6faed2b9a8c4c225a0cc0f64275a[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Jun 17 23:17:51 2014 -0700

    updates for 1.23.1 release

[33mcommit 48baa127d4a47a5525517397f6afecedac1a31d7[m
Merge: fcaad33 1f07da1
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Jun 17 17:52:21 2014 -0700

    Merge pull request #961 from pramodn02/MIFOSX-1336
    
    Mifosx 1336

[33mcommit 1f07da18d4c351e47c5553b3fd883d4da14e87fb[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Tue Jun 17 23:27:37 2014 +0530

     added junit test cases

[33mcommit b66e289df85eff14dbd11ecca78a08990944539e[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Tue Jun 17 16:01:25 2014 +0530

     MIFOSX-1336 - updated scale values for money with multiples of roundoff

[33mcommit fcaad335f070796904ebfa9777df8d2557921a69[m
Merge: 7ad0a6c 58d4458
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Jun 17 01:22:07 2014 -0700

    Merge pull request #960 from openMF/develop
    
    Update CHANGELOG.md

[33mcommit 58d44585154e4bbd47982ea661063287969b264f[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Jun 17 01:21:06 2014 -0700

    Update CHANGELOG.md
    
    and fix typos

[33mcommit 7ad0a6c71bfc69879be24330cf9d24fcb2611e47[m
Merge: a01d243 17d96ae
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Jun 17 00:46:15 2014 -0700

    Merge pull request #959 from lingalamadhukar/MIFOSX-1247
    
    MIFOSX-1317 test case fix

[33mcommit 17d96ae8a61afc0f9393a5d56d9d98396d74bfa2[m
Author: Madhukar <madhukar@confluxtechnologies.com>
Date:   Tue Jun 17 13:09:15 2014 +0530

    MIFOSX-1317 test case fix

[33mcommit a01d243078fb724f76ddc5203a5de22ad42e5afe[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Jun 16 22:12:28 2014 -0700

    Update CHANGELOG.md
    
    with details of fixes for 1.23 release

[33mcommit 46b653e387c5339c4e3e95cdf6b556f0051820b3[m
Merge: e0fd3d0 63173c0
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Jun 16 21:52:23 2014 -0700

    merging pramods changes around rd and fd

[33mcommit e0fd3d0fe73a9aaecca8a159e772e8381b0cd423[m
Merge: f7254fc ccb98ba
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Jun 16 21:37:59 2014 -0700

    Merge pull request #958 from vishwasbabu/develop
    
    more fixes to account transfers test cases

[33mcommit ccb98baf5f5c1f336c3c91dbdcf6a523f681c46b[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Jun 16 21:36:40 2014 -0700

    more fixes to account transfers test cases

[33mcommit f7254fc6608beaf3ca800c5be25fb3b8b6168f43[m
Merge: 632855d e8de3cb
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Jun 16 18:40:20 2014 -0700

    Merge pull request #957 from vishwasbabu/develop
    
    fix validations for financial activity accounts test

[33mcommit e8de3cbeccc54f7b433552fe51b7116863ff4524[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Jun 16 18:39:34 2014 -0700

    fix validations for financial activity accounts test

[33mcommit 632855d89663baa6ddcf9fb02f2bb3fd58100162[m
Merge: 97a88e1 fb5398e
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Jun 16 18:20:05 2014 -0700

    Merge pull request #956 from vishwasbabu/develop
    
    more fixes to account transfer test cases

[33mcommit fb5398e83b40bdf69207be88008fe93fd8ac148e[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Jun 16 18:18:15 2014 -0700

    more fixes to account transfer test cases

[33mcommit 97a88e1d1e1b837c77295d3f0f9cc07ce9ef0860[m
Merge: c8fccc5 2a60b05
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Jun 16 18:04:11 2014 -0700

    Merge pull request #955 from vishwasbabu/develop
    
    modifying test cases for account transfers

[33mcommit 2a60b05bf6271ceea95c6354da38ab517338febe[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Jun 16 18:02:26 2014 -0700

    modifying test cases for account transfers

[33mcommit c8fccc51ad08b3d477714c3daf1dee6369e661aa[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Jun 16 17:34:14 2014 -0700

    Update CHANGELOG.md

[33mcommit 67a757041254cbcf36003c2706fdda897dd13a30[m
Merge: 3eb0b46 f4e47cc
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Jun 16 17:16:09 2014 -0700

    Merge pull request #954 from vishwasbabu/develop
    
    Develop

[33mcommit f4e47cc01334ee3bc3bab37d4e19dd93f667418f[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Jun 16 17:14:31 2014 -0700

    fixing failing test cases and updating documentation for account transfers

[33mcommit 3eb0b46a89be3934bccd4a65d9fcf452cc49e0f7[m
Merge: a031463 c6d39fa
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Jun 16 16:34:36 2014 -0700

    Merge pull request #953 from vishwasbabu/develop
    
    updating test cases for financial activity accounts and accounts transfe...

[33mcommit c6d39fa737e89721e132c8d929073943805ab165[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Jun 16 16:33:07 2014 -0700

    updating test cases for financial activity accounts and accounts transfers

[33mcommit a0314639305be9ce96a688e22f97911e97354425[m
Merge: 469f871 5ef9406
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Jun 16 13:09:48 2014 -0700

    Merge pull request #952 from vishwasbabu/develop
    
    update default account for tracking inter branch transfers

[33mcommit 5ef940627c01d68f9ee313f6cc0ba020204949ee[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Jun 16 13:08:37 2014 -0700

    update default account for tracking inter branch transfers

[33mcommit 469f8717566fc8f109d447201203a2bcf77ab664[m
Merge: d7399cc d04f275
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Jun 16 12:14:02 2014 -0700

    Merge pull request #951 from lingalamadhukar/MIFOSX-1247
    
    MIFOSX-1343, format api-docs of standing instructions, account transfers

[33mcommit d04f2757e2fcba34ece7b458ac4edda37025c825[m
Author: Madhukar <madhukar@confluxtechnologies.com>
Date:   Tue Jun 17 00:34:53 2014 +0530

    MIFOSX-1343, format api-docs of standing instructions, account transfers

[33mcommit 602f0bdb074ec2b3d2dc1a227f702582630b6a66[m
Author: goutham-M <nayan.ambali@gmail.com>
Date:   Mon Jun 16 19:02:07 2014 +0530

    Weekly saving charges

[33mcommit d7399cc6e57ecba8d95d7ee9404139dfef91aa47[m
Merge: e820d4a 80439d6
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Jun 16 01:31:01 2014 -0700

    Merge pull request #949 from vishwasbabu/develop
    
    Rewriting Financial Activity Account Mapping

[33mcommit 80439d6e6d9c31d391af926bdf1a623f3bf85950[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Jun 16 01:28:04 2014 -0700

    Rewriting Financial Activity Account Mapping

[33mcommit 7f5ff5780ff400313ae5d033ecde2a304ab22d61[m
Author: rainerh <rainerh@hgoogle.com>
Date:   Sat Jun 14 12:06:24 2014 -0700

    MIFOSX-1161: Enforce minRequiredBalance

[33mcommit 2aa8b89d657f0355c9df3bc0c9bc0574a66544ad[m
Author: rainerh <rainerh@hgoogle.com>
Date:   Fri Jun 13 17:15:16 2014 -0700

    MIFOSX-1161: Adding new properties
    
    Adding fields minRequiredBalance and allowOverdraftMinBalance to
    SavingsProduct and SavingsAccount. No enforcement yet.

[33mcommit 317363de11c616e3b59b9b3e12d68b1cc398147c[m
Author: rishy <rishy.s13@gmail.com>
Date:   Tue Jun 10 16:58:22 2014 +0530

    Batch-API added with CreateClientCommand and transaction scope

[33mcommit e820d4ae00a7224a0657815bec3f5b4242f2d0a6[m
Merge: baeab1f 9035063
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Jun 13 10:50:38 2014 -0700

    Merge pull request #946 from lingalamadhukar/MIFOSX-1267
    
    MIFOSX-1317 Premature Interest Calculation Fix

[33mcommit 63173c0de010143529661d69dddf16e4ddc2aebc[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Fri Jun 13 23:10:09 2014 +0530

    MIFOSX-1335,MIFOSX-1300 - corrected FD and RD related issues

[33mcommit 9035063dc2984619350fbf628d579d821cadcf2e[m
Author: Madhukar <madhukar@confluxtechnologies.com>
Date:   Thu Jun 12 13:23:21 2014 +0530

    Interest calculation bug fix

[33mcommit baeab1f7c8668c2b43a3dd2dc9a3b6caffdf4cf0[m
Merge: 06efcb7 71505ed
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Jun 12 04:58:18 2014 -0700

    Merge pull request #945 from pramodn02/latest
    
    MIFOSX-1313 : client incentive code cleanup

[33mcommit 71505edbc51a6e856e2cf782646cda024141204d[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Thu Jun 12 14:22:51 2014 +0530

    MIFOSX-1313 : client incentive code cleanup

[33mcommit 06efcb73c9aadf40b1a4f861cbcd6597bccd4d47[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Jun 11 13:36:03 2014 -0700

    updates to finanacial activity to account mapping api's

[33mcommit c2d94f2976964ddccd6b77dc08d52f09748faece[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Jun 11 10:21:41 2014 -0700

    update sample data for 1.23 release

[33mcommit dbde081057e2467e0d08c61d7d50cb8adaf42f72[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Jun 11 09:38:00 2014 -0700

    Update CHANGELOG.md
    
    for 1.23 release

[33mcommit caad86ac6568580255e2b670c45afe227beba122[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Jun 11 09:01:09 2014 -0700

    updating details of 1.23 release

[33mcommit e1a8c8a37114bb73ad230c69ccba9e064e668cfd[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Jun 11 08:44:45 2014 -0700

    Update CHANGELOG.md
    
    for 1.23 release

[33mcommit 2885cc08645ffd530779884b2bc817c428742696[m
Merge: c89699c e09162f
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Jun 11 07:08:51 2014 -0700

    Merge pull request #944 from lingalamadhukar/MIFOSX-1267
    
    MIFOSX-1318,MIFOSX-1314,MIFOSX-1300,MIFOSX-1242,MIFOSX-1199

[33mcommit e09162f5e2e4ae663f7fd12684e50d7fee3af9f1[m
Author: Madhukar <madhukar@confluxtechnologies.com>
Date:   Wed Jun 11 17:46:11 2014 +0530

    MIFOSX-1318,MIFOSX-1314,MIFOSX-1300,MIFOSX-1242,MIFOSX-1199

[33mcommit c89699c9df11866a55d4b41fed2570228d0bbe8e[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Jun 10 23:16:10 2014 -0700

    minor cleanups

[33mcommit 6cc6d03785e3c26c7f2393d68990cc74f8644f1e[m
Merge: aa96434 54b60fd
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Jun 10 16:57:01 2014 -0700

    Merge pull request #940 from juniormonkey/develop
    
    MIFOSX-1302: A new exception when a currency that is still in use is set to be deleted.

[33mcommit aa9643429b4419670c0cbe7d07baf9575fb4a091[m
Merge: 1f86299 21de2bd
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Jun 10 16:50:32 2014 -0700

    Merge pull request #938 from pramodn02/MIFOSX-1274
    
    MIFOSX-1274,MIFOSX-1268 : added check for negative account balance while...

[33mcommit 54b60fdcd0607308ecc1c6bf7dfbc42e18f0b65e[m
Author: Martin Strauss <martin@ockle.org>
Date:   Tue Jun 10 15:37:42 2014 -0700

    Fix MIFOSX-1302: Throw a CurrencyInUseException whenever a currency is requested for deletion that is still in use by a charge, a loan or a savings product.

[33mcommit a450829b94483208f1411e55c4ab83adc235a597[m
Author: Martin Strauss <martin@ockle.org>
Date:   Tue Jun 10 15:36:50 2014 -0700

    Throw a CurrencyInUseException when a currency is being deleted that is still in use.

[33mcommit 21de2bd5ab09ffea2fa4040cb16eb93925e4a6ee[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Wed Jun 11 03:44:16 2014 +0530

    MIFOSX-1274,MIFOSX-1268 : added check for negative account balance while creating active savings account

[33mcommit 1f86299cf16dd030fee48e9f6e2f04d25d19c817[m
Merge: 123a59f 38c6702
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Jun 10 14:50:48 2014 -0700

    Merge pull request #937 from pramodn02/MIFOSX-1270
    
    MIFOSX-1270 : corrected accounting for  activation charge

[33mcommit 38c6702cae452331717b0d5d8b9d7eff732ebab0[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Wed Jun 11 02:47:28 2014 +0530

    MIFOSX-1270 : corrected accounting for  activation charge

[33mcommit 123a59f2e7c8156108b1910ecfdc2b1e2809b84d[m
Merge: 6df2f48 84f0aaf
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Jun 10 12:22:38 2014 -0700

    Merge pull request #935 from pramodn02/MIFOSX-1306
    
    MIFOSX-1306 : added check to identify account transfer

[33mcommit 84f0aaf32a2bd60b88dcfaa02bacc10e6d4cc964[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Wed Jun 11 00:09:23 2014 +0530

    MIFOSX-1306 : added check to identify account transfer

[33mcommit 6df2f48bc93671beed1e08cb2188dc6319470862[m
Merge: 45e12ac d743c3e
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Jun 10 07:55:38 2014 -0700

    Merge pull request #933 from lingalamadhukar/MIFOSX-1267
    
    MIFOSX-1260,MIFOSX-1287,MIFOSX-1311

[33mcommit d743c3e5f9d3786cfc7e0d4bafa967661e045b13[m
Author: Madhukar <madhukar@confluxtechnologies.com>
Date:   Mon Jun 9 23:14:12 2014 +0530

    MIFOSX-1260,MIFOSX-1311
    
    MIFOSX-1260,MIFOSX-1287

[33mcommit 45e12ac91e6426c5478fa7f839d5541d4ccf6bb5[m
Merge: 8bd0e29 77e23c8
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Jun 9 22:06:20 2014 -0700

    Merge pull request #931 from pramodn02/latest
    
    MIFOSX-1313 :  changes for interest  incentives

[33mcommit 77e23c8e9d7321b5e7775af0abcacbaeea6a5f9f[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Tue Jun 10 03:01:28 2014 +0530

    MIFOSX-1313 :  changes for interest  incentives

[33mcommit 8bd0e29ba57c07fd29ba76833571d178911f49e5[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Jun 8 03:24:18 2014 -0700

    MIFOSX-1183 and adding upfront accrual transaction back for cash based and no accounting rules

[33mcommit 59c86715225bf64c42aa068a8a886d5b2c979e1b[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sat Jun 7 10:56:43 2014 -0700

    removed addition of main compile classpath to test compile classpath

[33mcommit 2d7c828eee3b9981e37d85b8206026438d701c22[m
Merge: 6c52c91 f939772
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sat Jun 7 10:54:40 2014 -0700

    Merge pull request #929 from vishwasbabu/develop
    
    augument runtime and compile classpath for integration tests to include ...

[33mcommit f9397725cdc180f82a8e2540ba3824595c22dad4[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sat Jun 7 10:50:21 2014 -0700

    augument runtime and compile classpath for integration tests to include main and test

[33mcommit 6c52c91dbe38087c8a8d8f7303de030a866027c9[m
Merge: 8e61abe 325c87b
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sat Jun 7 00:35:49 2014 -0700

    Merge pull request #928 from lingalamadhukar/MIFOSX-1267
    
    Bug Fixes for 1.23 release

[33mcommit 325c87b1219cda1cbbe2392f478e49a5539363ea[m
Author: Madhukar <madhukar@confluxtechnologies.com>
Date:   Sat Jun 7 01:42:31 2014 +0530

    Bug Fixes for 1.23 release

[33mcommit 8e61abe4ed3454fb86e7d61a1d2b7c2574490bdf[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Jun 6 11:23:50 2014 -0700

    removing most warnings in ppi/survey related code

[33mcommit b63364962dfabb64a97f718fd12614b446e8a7d2[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Jun 5 21:27:12 2014 -0700

    resolving compilation errors in survey integration test

[33mcommit 10fd3be477f1a968254afed5725aa7bdd9a965af[m
Merge: 941d732 1ca510b
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Jun 5 21:06:30 2014 -0700

    MIFOSX-merging musonis work around ppi

[33mcommit 941d732cfbb1d07572d630af3609d6273404fff5[m
Merge: 4e53e3b 0426325
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Jun 5 18:49:13 2014 -0700

    Merge branch 'MIFOSX-1023' of git://github.com/Musoni/mifosx into Musoni-MIFOSX-1023

[33mcommit 4e53e3b94d2d397a22fde1c1dfcd7168c2950de2[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Jun 5 14:39:25 2014 -0700

    changing visibility of lockinPeriodFrequencyTypeValue variable in DepositAccountAssembler

[33mcommit d41cea8d46597036ea630e13b2a7206ca9dacffb[m
Merge: 7dcb991 43225c3
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Jun 5 14:37:04 2014 -0700

    Merge pull request #927 from lingalamadhukar/MIFOSX-1267
    
    MIFOSX-1288,MIFOSX-1280,build#699 testcase fixes

[33mcommit 43225c38f6f6a68ef8d4c5061041576c9c28fed0[m
Author: Madhukar <madhukar@confluxtechnologies.com>
Date:   Thu Jun 5 14:59:41 2014 +0530

    MIFOSX-1288,MIFOSX-1280,build#699 testcase fixes

[33mcommit 7dcb9913a4e677e6c9bcd30a11d98913f8a4124c[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Jun 4 18:09:56 2014 -0700

    bumping up version number for sql patch related to accounting cleanup for transfers

[33mcommit 03028fc4eefd4a6b54da39c7616edfe30715c90f[m
Merge: 19833af c7e7a00
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Jun 4 18:07:24 2014 -0700

    merging pramods pr around account transfers accounting clean up

[33mcommit 19833af1a9084d46d798adabb4a045e38d741c91[m
Merge: 75ac5c2 4c9ccd2
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Jun 4 09:00:30 2014 -0700

    Merge pull request #926 from lingalamadhukar/MIFOSX-1267
    
    MIFOSX-1267 and fix for view loan audit entry

[33mcommit 4c9ccd2d53e18f90ba7f158b2abe3af8d07e741e[m
Author: Madhukar <madhukar@confluxtechnologies.com>
Date:   Wed Jun 4 09:23:27 2014 +0530

    MIFOSX-1267 and fix for view loan audit entry

[33mcommit 75ac5c2d68450fda8ad2c462fc7fb4ae14acb18e[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Jun 3 17:56:07 2014 -0700

    renaming get_url to url in Audit Data response

[33mcommit adc42ddaf1d5e52c81d81a53cbd945c44f036152[m
Merge: 528423d a88bad2
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Jun 3 17:43:00 2014 -0700

    Merge pull request #917 from ashok-conflux/NBLMS-9
    
    added api_get_url value to audit api response data

[33mcommit 528423ddc2fa545e48c0653320993af571cb1d3e[m
Merge: a3a414f d80e574
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Jun 3 06:17:22 2014 -0700

    Merge pull request #925 from lingalamadhukar/MIFOSX-1166
    
    MIFOSX-1216 and loan integration tests with accounting

[33mcommit d80e574b3be12ec13ab1e3003709124a7eb7811b[m
Author: Madhukar <madhukar@confluxtechnologies.com>
Date:   Tue Jun 3 10:30:43 2014 +0530

    MIFOSX-1216 and loan integration tests with accounting

[33mcommit a3a414ff0a44cd09d6faf61991b04a62c8a86d65[m
Merge: 761c3b1 95e7869
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Jun 2 16:15:48 2014 -0700

    Merge pull request #924 from lingalamadhukar/MIFOSX-1242
    
    MIFOSX-1245,MIFOSX-1244,MIFOSX-1243,MIFOSX-1242,MIFOSX-1241,MIFOSX-1240

[33mcommit 95e78699d6be998172a32324ffa11494dfdb522e[m
Author: Madhukar <madhukar@confluxtechnologies.com>
Date:   Mon Jun 2 11:05:55 2014 +0530

    MIFOSX-1245,MIFOSX-1244,MIFOSX-1243,MIFOSX-1242,MIFOSX-1241,MIFOSX-1240

[33mcommit 761c3b1e8d73023a7976c7962d17e974bdce22f4[m
Merge: d59683c bd8aa4e
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed May 28 11:47:05 2014 +0530

    Merge pull request #918 from Nayan/MIFOSX-1228
    
    #MIFOSX-1228 Templates should not show inactive staff under staff drop down

[33mcommit d59683c9f8c7f44b40cd39afbd749ebb544d14f4[m
Merge: 00a10a3 2f82783
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed May 28 11:45:07 2014 +0530

    Merge pull request #920 from ashok-conflux/CONFLUX-39-CR
    
    Added support to mandatory savings

[33mcommit c7e7a00071acaca343d6bbb66f0c447d1279d553[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Wed May 28 00:17:58 2014 +0530

    API doc changes

[33mcommit 6fc0c1b4a599284b8150f559c78b2aca090e091c[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Tue May 27 18:55:30 2014 +0530

     MIFOSX-695 - accounting  changes for account transfers

[33mcommit 00a10a31accf0918d10d572f96a63dc9cc4d6389[m
Merge: b2131b5 45d4eb4
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon May 26 19:13:10 2014 +0530

    Merge pull request #922 from lingalamadhukar/MIFOSX-1237
    
    FIXED MIFSOX-1237,MIFOSX-1238

[33mcommit 45d4eb43d5e92e4a0bdebd742e1714262e18f722[m
Author: Madhukar <madhukar@confluxtechnologies.com>
Date:   Mon May 26 12:50:21 2014 +0530

    FIXED MIFSOX-1237,MIFOSX-1238

[33mcommit 2f8278331e82862d7d4b9fcee0f3af47baec1287[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Wed Apr 23 10:46:13 2014 +0530

    Added support to mandatory savings

[33mcommit bd8aa4e7ca906ed517eb859ddd233b45ec0489f3[m
Author: goutham-M <nayan.ambali@gmail.com>
Date:   Fri May 23 17:39:52 2014 +0530

    Templates should not show inactive staff under staff drop down

[33mcommit a88bad2a7f366a7b3a13d6405e7a690cab10e9cf[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Fri May 23 11:57:43 2014 +0530

    added api_get_url value to audit api response data

[33mcommit b2131b52a72de74aa1df7ab7b8dd81fcf39ae8b9[m
Merge: f67b82d 1dddcae
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed May 21 12:18:02 2014 +0530

    Merge pull request #915 from Vishwa1311/MIFOSX-1208
    
    [MIFOSX-1208] Issue with retrieving datatables for offices has been fixed

[33mcommit 1dddcae84a4c7594e4661ad9bcabe09491c25a64[m
Author: Vishwa1311 <vishwanath@confluxtechnologies.com>
Date:   Tue May 20 15:02:12 2014 +0530

    [MIFOSX-1208] Issue with retrieving datatables for offices has been fixed

[33mcommit 1ca510b0ca6bde3705811e03756fe23f56b00042[m
Author: CieyouRaoul <cieyouraoul@musoni.eu>
Date:   Tue May 20 10:08:54 2014 +0200

    work in porgress integration test

[33mcommit f67b82d0b94ca59660668cf4eb939ecf461121bb[m
Merge: 64c8121 de7c256
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon May 19 15:36:00 2014 +0530

    Merge pull request #914 from lingalamadhukar/dailycheck
    
    MIFOSX-1203 and test case fix for build history 692

[33mcommit de7c25635cba9eb53d41b0a978c73711535a7bd8[m
Author: Madhukar <madhukar@confluxtechnologies.com>
Date:   Mon May 19 12:06:26 2014 +0530

    MIFOSX-1203 and test case fix for build history 692

[33mcommit 64c812136270de76c034a4bc5adfcd1783174eec[m
Merge: 9c9c240 ae035ff
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon May 19 09:55:11 2014 +0530

    Merge pull request #911 from pramodn02/MIFOSX-1200
    
    MIFOSX-1200 : corrected openning balance while calculations

[33mcommit 9c9c24060f49f9a5311b7dec5b2d363478a4e2f7[m
Merge: c3cd9aa 38b5047
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon May 19 09:53:42 2014 +0530

    Merge pull request #910 from lingalamadhukar/MIFOSX-1166
    
    Integration tests for loan accounting flow

[33mcommit c3cd9aacbfb97d1a77b30cbc58750977b5a66222[m
Merge: a86ef1d 99025b4
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon May 19 09:52:13 2014 +0530

    Merge pull request #912 from pramodn02/MIFOSX-1196
    
     MIFOSX-1196 - corrected the validation for update

[33mcommit a86ef1da2dab3d616a88b0c438de99afaa27f2fa[m
Merge: 0a3e039 99822a3
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon May 19 09:48:05 2014 +0530

    Merge pull request #913 from pramodn02/MIFOSX-1187
    
    MIFOSX-1187 - added null checks before compare with min and max values

[33mcommit 99822a32e298f4f6c0a8f1d221b96f7c192ae355[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Thu May 15 22:20:25 2014 +0530

    MIFOSX-1187 - added null checks before compare with min and max values

[33mcommit 712b5e000cb876e574cd88fe9b7c4993cd98db23[m
Author: CieyouRaoul <cieyouraoul@musoni.eu>
Date:   Thu May 15 13:32:24 2014 +0200

     implement jpa for likelihood write operation, set default value for registered table category, add likelihood table sql

[33mcommit 0a3e039122229404354dae522a22533ec02bccbf[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu May 15 13:01:34 2014 +0530

    cleaning up PR for MIFOSX-709

[33mcommit b2771704f69b7dcd0b620b6008473c8f51edf0e1[m
Merge: 363ca7d ab23a26
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu May 15 12:33:17 2014 +0530

    Merge pull request #909 from erikbarbara/MIFOSX-709
    
    MIFOSX-709: adding functionality to prevent closing of client with overd...

[33mcommit ae035ff2c972214f384fa450d1bd2bd29812e43d[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Wed May 14 23:00:49 2014 +0530

    MIFOSX-1200 : corrected openning balance while calculations

[33mcommit 99025b4eb3e2bebf3a06d75f79a54b73407526e6[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Wed May 14 21:43:11 2014 +0530

     MIFOSX-1196 - corrected the validation for update

[33mcommit 38b5047bfe8aafb377614e19f64198b063f78ded[m
Author: Madhukar <madhukar@confluxtechnologies.com>
Date:   Wed May 14 09:42:13 2014 +0530

    Integration tests for loan accounting flow

[33mcommit ab23a26ab24d466353ba9d2f1d68d947e9b3cc94[m
Author: Erik Barbara <erik.barbara@lifetech.com>
Date:   Tue May 13 20:29:40 2014 -0400

    MIFOSX-709: adding functionality to prevent closing of client with overdue loan

[33mcommit ece576e27505acbe8c6ee6778a1accfb1c337818[m
Author: Michael Vorburger <mike@vorburger.ch>
Date:   Mon May 12 03:17:36 2014 +0200

    Fixed startup error following upgrade (spring-security-3.2.xsd)

[33mcommit 1db61882a8ab51ad27cb63691de0dc170bbf3faa[m
Author: Michael Vorburger <mike@vorburger.ch>
Date:   Mon May 12 00:17:51 2014 +0200

    bumped Spring major version 3.2.5 -> 4.0.4 (+ JUnit 4.10->4.11)

[33mcommit 37710750eba5638332eb2a635eff1b1db11e7b17[m
Author: Michael Vorburger <mike@vorburger.ch>
Date:   Sun May 11 02:10:00 2014 +0200

    ClassMethodNamesPair for CronMethodParser instead of CLASS_INDEX /
    METHOD_INDEX

[33mcommit f194867c1571cd7c48b9c648119ce71d4965d2a4[m
Author: Michael Vorburger <mike@vorburger.ch>
Date:   Sun May 11 02:03:47 2014 +0200

    SchedulerTriggerListener really should just be normally injected, like
    everything else; it's an anti pattern to directly use
    applicationContext, unless there is a specific good reason for it.

[33mcommit 226439bef1399eb99d2b7c5ee3b1ca3741acdfec[m
Author: Michael Vorburger <mike@vorburger.ch>
Date:   Sun May 11 01:48:49 2014 +0200

    MIFOSX-1184 FIXED JobRegisterServiceImpl <-> SchedulerStopListener
    circular bean dependency

[33mcommit d33c16e5944a141fadba5aaed44f393572181605[m
Author: Michael Vorburger <mike@vorburger.ch>
Date:   Sun May 11 01:16:45 2014 +0200

    Just some clean-up to see more clearly - this should not change
    anything. PLEASE CAREFULLY CODE REVIEW NEVERTHELESS.

[33mcommit e6ac1213f743e0da3077b193633429956a91be8d[m
Author: Michael Vorburger <mike@vorburger.ch>
Date:   Sun May 11 00:37:58 2014 +0200

    Correct space-instead-tab and one-line formatting for new log.

[33mcommit 363ca7dc0eab2326121b9311ed53a9939af210ae[m
Merge: 9e8ead2 b0132db
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun May 11 03:39:01 2014 +0530

    Merge pull request #902 from vorburger/JobLogImprovementIllustratingBug
    
    JobRegisterServiceImpl error logging improvement

[33mcommit b0132db8155786250838e77ed6747d956d26c757[m
Author: Michael Vorburger <mike@vorburger.ch>
Date:   Sun May 11 00:01:07 2014 +0200

    Logging 2x, for otherwise silent failures in a) initial loadAllJobs(),
    and b) subsequent manual executeJob()

[33mcommit 9e8ead20452142e691c7d2d9fcffc134c44d40fb[m
Merge: 7ff989d 0a9de45
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun May 11 01:13:34 2014 +0530

    Merge pull request #901 from lingalamadhukar/MIFOSX-1166
    
    Update users api docs with staff details

[33mcommit 0a9de45cede95d7ab3f140a5c81ff1dfcc9fb430[m
Author: Madhukar <madhukar@confluxtechnologies.com>
Date:   Fri May 9 20:22:50 2014 +0530

    Update users api docs with staff details

[33mcommit 7ff989dbe0f4f9466b793eac2c4b4a91e1e2999d[m
Merge: 7f25cc3 490512e
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sat May 10 12:10:29 2014 +0530

    Merge pull request #900 from vorburger/newKeystoreToReplacedExpiredCert
    
    keystore.jks with new self-signed cert

[33mcommit 490512eb3586870d0da7eaf0dc6f1adc48332fb8[m
Author: Michael Vorburger <mike@vorburger.ch>
Date:   Sat May 10 00:42:13 2014 +0200

    keystore.jks with new self-signed cert, as the old one expired (http://blog2.vorburger.ch/2014/05/how-to-renew-expired-self-signed-ssl.html)

[33mcommit 7f25cc3a91a4933f503707a836cb93f5f9a37401[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri May 9 20:12:19 2014 +0530

    fixing compilation issues

[33mcommit a37edf45d8de6e8fe3903db414565d06380fb195[m
Merge: c0a6ed8 4d2913b
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri May 9 19:26:48 2014 +0530

    resoving merge conflicts with MIFOSX-1182

[33mcommit 4d2913be8f003dc192854fdfa24ebc7b6bfa1f2e[m
Merge: c7e21fc 00f91c8
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri May 9 19:16:06 2014 +0530

    Merge branch 'develop' of git://github.com/pramodn02/mifosx into pramodn02-develop

[33mcommit c0a6ed8081c5cd6cd8ea3fd3d694a6692797980f[m
Merge: c7e21fc 7dd49de
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri May 9 18:16:52 2014 +0530

    Merge pull request #898 from Vishwa1311/NEW-ONE-MIFOSX-1117
    
    [MIFOSX-1117] JUnit Test Cases for FD and RD accounts

[33mcommit c7e21fc39d2530422e49ee7c429f37197c25a923[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri May 9 18:07:50 2014 +0530

    cleaning up MIFOSX-1024

[33mcommit e2a9d8afbc268d777d482a71abe96c4f3130982c[m
Author: andrew-dzak <andrewdzakpasu@musoni.eu>
Date:   Mon Apr 14 16:03:00 2014 +0200

    fixing merge issues with MIFOSX-1024

[33mcommit 7dd49de5af969d8c971dabea5c323b529ba95ad7[m
Author: Vishwa1311 <vishwanath@confluxtechnologies.com>
Date:   Fri May 9 17:24:41 2014 +0530

    [MIFOSX-1117] FD RD Test Cases

[33mcommit 00f91c8c6e256e7b53a6eb48b0c8c93b22cf5ef3[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Fri May 9 17:19:56 2014 +0530

    MIFOSX-1182 - added support for interest transfer on fixed deposit account

[33mcommit 9472ec236c87264e6ff9d2b8c261b1ccf949e5be[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri May 9 14:46:43 2014 +0530

    cleaning up commit for MIFOSX-982

[33mcommit cf4bf988a46d1299a6006327b6d599c66034433a[m
Author: Ishan Khanna <boilingstocks@gmail.com>
Date:   Thu Mar 27 16:56:51 2014 +0530

    MIFOSX-982 Issue Fix
    
    Updated Fix

[33mcommit 67ec9163bd082f9b846d2447ea5c3e556b1ce97b[m
Merge: 37e8478 466f3f8
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue May 6 18:25:26 2014 +0530

    Merge pull request #896 from lingalamadhukar/MIFOSX-1166
    
    MIFOSX-281 Add template for recovery payments

[33mcommit 466f3f8b959a61e0b480ed6c7312723d3ed20ecb[m
Author: Madhukar <madhukar@confluxtechnologies.com>
Date:   Tue May 6 18:19:52 2014 +0530

    MIFOSX-281 Add template for recovery payments

[33mcommit 37e84789c25bc9f7dd9372bce5a70455b8a2a544[m
Merge: 6b33966 6ddacc7
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue May 6 18:16:34 2014 +0530

    Merge pull request #895 from vishwasbabu/develop
    
    MIFOSX-281 adding accrual accounting support

[33mcommit 6ddacc7cdb1199c7804a3f94c17ee5f0c83d5e6b[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue May 6 18:15:22 2014 +0530

    MIFOSX-281 adding accrual accounting support

[33mcommit 6b33966b545b3af184d0df28f44938185d109dec[m
Merge: 285cce1 31a6e07
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue May 6 15:53:01 2014 +0530

    Merging Michael's changes around FD and RD

[33mcommit 285cce1bf83e95dc188426157bbb82364dc3616b[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon May 5 21:17:00 2014 +0530

    fix for MIFOSX-1019

[33mcommit a0c26c65d965228f744e9b835defa9e86d189a7e[m
Merge: 62e4b6f 9c7c81d
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon May 5 17:51:20 2014 +0530

    Merge pull request #893 from lingalamadhukar/MIFOSX-1166
    
    MIFOSX-1166 Rename sql schema name

[33mcommit 62e4b6f6e3387dea3aac2ae280160ec9fb929f1c[m
Merge: 0e93b4a 9327f1e
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon May 5 17:50:40 2014 +0530

    Merge pull request #892 from lingalamadhukar/Docs
    
    Update API docs

[33mcommit 9c7c81d93be706812671a743c4a0af6e77dafa3b[m
Author: Madhukar <madhukar@confluxtechnologies.com>
Date:   Mon May 5 12:29:47 2014 +0530

    MIFOSX-1166 Rename sql schema name

[33mcommit 9327f1eef0e54539d85ed994d88b4b712e01df0b[m
Author: Madhukar <madhukar@confluxtechnologies.com>
Date:   Wed Apr 30 17:09:35 2014 +0530

    Update API docs
    
    Update collection sheet api-doc

[33mcommit 0e93b4a4df037139514f2cc9f423710a4deb2c02[m
Merge: 52d140b ece7c9a
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun May 4 18:02:59 2014 +0530

    cleaning up recovery payments functionality

[33mcommit 52d140bb1750494fa1bbf46fbed3b41ff0fa0e51[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun May 4 14:27:09 2014 +0530

    correcting error message for MIFOSX-1163

[33mcommit 5343b3c676b4fb680853b45ac4d4e5358b3fcccc[m
Merge: d57d1c7 68216e5
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun May 4 14:21:28 2014 +0530

    Merge pull request #890 from pramodn02/develop
    
    MIFOSX-1163 : added functionality to transfer fixed deposit amount from ...

[33mcommit d57d1c7d88c4456c694647fe4278dd3220e7bec2[m
Merge: 78c0761 29478ca
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun May 4 14:01:29 2014 +0530

    Merge pull request #891 from pramodn02/MIFOSX-1162
    
    MIFOSX-1162 : removed overdue loan charge on undo disburse

[33mcommit 68216e565291d0b22854a3515a29d629bd5395ab[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Sat May 3 02:04:42 2014 +0530

    MIFOSX-1163 - corrected accounting entries

[33mcommit 29478ca023c311e92c6df28d6c4bbe307a23544a[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Sat May 3 01:07:01 2014 +0530

    MIFOSX-1162 : removed overdue loan charge on undo disburse

[33mcommit 3677d4eb53d55460e8cd6ab7d5e89b87a522f11f[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Fri May 2 11:26:48 2014 +0530

    MIFOSX-1163 : added functionality to transfer fixed deposit amount from savings account

[33mcommit 78c0761ae5921f52e63c827cee32818832d9c1d1[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu May 1 16:44:56 2014 +0530

    cleaning up sample data for 1.22.0 release

[33mcommit 742c357dfbcb88aa45732e8723a4c012139b96fb[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu May 1 16:36:33 2014 +0530

    Update CHANGELOG.md
    
    with details of dropped tables in 1.22.0 release

[33mcommit 31a6e07e87e6e99b65045c027ce1a8d337887953[m
Author: Michael Duku-Kaakyire <michael.duku-kaakyire@miagstan.com>
Date:   Wed Apr 30 21:42:56 2014 +0000

    Advanced deposit products and accounts (FD & RD) with interest rate
    extensions bug fixes

[33mcommit a109a1cee91819c98a118a2bad22f9f994ced4a1[m
Merge: 1986047 697a322
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Apr 30 21:16:05 2014 +0530

    Merge pull request #887 from lingalamadhukar/FDProduct
    
    Added deposit amount fields in Read FD Product

[33mcommit 697a32221010385b152d0189c4f82b287ec072b7[m
Author: Madhukar <madhukar@confluxtechnologies.com>
Date:   Wed Apr 30 19:58:59 2014 +0530

    Added deposit amount fields in Read deposit

[33mcommit 1986047ae1941032f2863fb51913b65d9fc7428b[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Apr 30 18:59:30 2014 +0530

    commenting out unused rd validation

[33mcommit 1b4e03aa6eac7257fa7fb6eed2b270125926e517[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Apr 30 17:48:53 2014 +0530

    fix warnings in savings code

[33mcommit b69b071ffe93abffdfbf1887963970b5ae930de5[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Apr 30 17:46:18 2014 +0530

    null checks for fixing failing test cases in RD

[33mcommit 508fe4b60d0a605b3a2ebd6d3fc8b96793d9b717[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Apr 30 17:40:33 2014 +0530

    adding null check for fixed deposit suggested amount for failing test cases

[33mcommit 656fc88aae69527b1abaad08a4a2c463cb650b23[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Apr 30 17:04:27 2014 +0530

    updating release number to 1.22.0

[33mcommit e49f44b8110578e9db8858e04443f32a826bf9a6[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Apr 30 15:50:58 2014 +0530

    Update CHANGELOG.md
    
    for 1.22.0 release

[33mcommit 4f3b7f91a18e566e893175b28491ebaf6746c09c[m
Merge: d033ea2 8191ef0
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Apr 30 15:19:54 2014 +0530

    Merge pull request #886 from Vishwa1311/MIFOSX-1117
    
    [MIFOSX-1117] Fixed test case failure issue

[33mcommit 8191ef02113e204e9d5663c14b739d8ae228afb4[m
Author: Vishwa1311 <vishwanath@confluxtechnologies.com>
Date:   Wed Apr 30 15:14:43 2014 +0530

    [MIFOSX-1117] Fixed test case failure issue

[33mcommit d033ea22cac3140863aebee72a99ff114315d8e8[m
Merge: 5cf6b9a 166a381
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Apr 29 21:23:30 2014 +0530

    Merge pull request #885 from vishwasbabu/develop
    
    fixing test failure in fd and rd

[33mcommit 166a381556a7384526bf654db1c84183241add01[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Apr 29 21:17:49 2014 +0530

    fixing test failure in fd and rd

[33mcommit 5cf6b9a34e9b0fddb3cb5be36ebc19e3a1f049c1[m
Merge: 1af5b50 f5e9342
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Apr 29 20:44:07 2014 +0530

    Merge pull request #883 from lingalamadhukar/MIFOSX-885
    
    Update Api docs

[33mcommit f5e9342446c909561aa76c4094d57fd6a4e0e0e0[m
Author: Madhukar <madhukar@confluxtechnologies.com>
Date:   Tue Apr 29 16:57:14 2014 +0530

    Update Api docs

[33mcommit 1af5b5043d31c21fbf30bc406d28f0ebe30c0f40[m
Merge: bec0a36 e9b8294
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Apr 29 10:24:17 2014 +0530

    Merge pull request #880 from michaeldk-miagstan/michael-miagstan-github
    
    Advanced deposit products and accounts (FD & RD) with interest rate extensions

[33mcommit bec0a366443a81c00e4f7714b90bc7abee349ef0[m
Merge: f0bfec3 d34dcb1
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Apr 29 09:42:12 2014 +0530

    Merge pull request #882 from pramodn02/develop
    
    MIFOSX-1146: added support for disbursing loan amount to savings account

[33mcommit d34dcb10378a894ca95a3fbec6921dbd885c6a16[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Mon Apr 28 21:24:42 2014 +0530

    MIFOSX-1146: added support for disbursing loan amount to savings account

[33mcommit f0bfec345907f1a87004eadad80ca6c6dcbb1dff[m
Merge: 3cc95e3 dcee94e
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Apr 28 18:16:05 2014 +0530

    Merge pull request #881 from vishwasbabu/develop-new
    
    MIFOSX-865 bug fixes for accruals batch job

[33mcommit dcee94e732761f93e38b62b83b5ac85c24dfc0df[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Apr 28 18:01:36 2014 +0530

    MIFOSX-865 bug fixes for accruals batch job

[33mcommit e9b8294f8045ddf7fcf3c17a8839757c35afdf52[m
Author: Michael Duku-Kaakyire <michael.duku-kaakyire@miagstan.com>
Date:   Mon Apr 28 08:22:25 2014 +0000

    Advanced deposit products and accounts (FD & RD) with interest rate
    slabs extensions

[33mcommit 3cc95e3cf17c9cdc70266a4f7328949f2c926b1d[m
Merge: dca2835 f8416fe
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sat Apr 26 23:17:30 2014 -0700

    Merge pull request #876 from pramodn02/MIFOSX-1094
    
    MIFOSX-1094 - changes for passing DB url as param

[33mcommit f8416fe714cec9f3b5c647464be19e4026f2fe3c[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Fri Apr 25 17:53:58 2014 +0530

    MIFOSX-1094 - changes for passing DB url as param

[33mcommit dca28356ba527fdf858ff239610a38db6d18892d[m
Merge: b81a3ed 09cbbcd
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Apr 23 01:06:54 2014 -0700

    Merge pull request #870 from ashok-conflux/CONFLUX-39-api-1
    
    API documentation updated for FD and RD

[33mcommit b81a3ed502ffc6d3a2f581f8a5b84d65695f4d3d[m
Merge: a7cee7e c452340
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Apr 22 23:45:10 2014 -0700

    Merge pull request #875 from ashok-conflux/CONFLUX-39-testcase-fix
    
    fixed failing Post Interest Test case

[33mcommit c452340e2e3038a61799908254fce2b1228298a1[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Wed Apr 23 12:12:52 2014 +0530

    fixed failing Post Interest Test case

[33mcommit a7cee7ee8eee9c4a7e79f079de34b84e2c5adeac[m
Merge: 6069379 a032ad2
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Apr 22 18:19:38 2014 -0700

    Merge pull request #874 from pramodn02/MIFOSX-1056
    
    MIFOSX-1056 - changed to use create validation from preview

[33mcommit 6069379af9dd9672c326fa6d3066fbc38cf41bf0[m
Merge: bf6ffb2 40c6223
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Apr 22 18:18:30 2014 -0700

    Merge pull request #872 from pramodn02/MIFOSX-1076
    
    added loan charge junits

[33mcommit a032ad2133e54eb43e8704b9d6043da4e2653433[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Tue Apr 22 22:31:36 2014 +0530

    MIFOSX-1056 - changed to use create validation from preview

[33mcommit ece7c9a60095af1c6389f229f037029ebaf2d64d[m
Author: andrew-dzak <andrewdzakpasu@musoni.eu>
Date:   Tue Apr 22 11:55:49 2014 +0200

    mifosx-281 done

[33mcommit 40c6223d8c6b9ae474fcf2fc6bd8198e1858c09e[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Mon Apr 21 22:07:13 2014 +0530

    added loan charge junits

[33mcommit bf6ffb27eb172ee36780fcdc3657e4676fda81c1[m
Merge: 9399314 d4c897a
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Apr 20 09:09:07 2014 -0700

    Merge pull request #869 from pramodn02/MIFOSX-1076
    
    MIFOSX-1076 - Added recalculation of charges on updating amount

[33mcommit 9399314ed4537960594401d5acef34a11f5c7521[m
Merge: 84f2ccf 3bca8e3
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Apr 20 08:50:42 2014 -0700

    Merge pull request #871 from Vishwa1311/MIFOSX-1117
    
    [MIFISX-1117] JUnit Test Cases for Fixed and Recurring deposits

[33mcommit 3bca8e3215668d802f2a0b05a2f9676a89b9f4bb[m
Author: Vishwanath R <vishwanath@confluxtechnologies.com>
Date:   Sun Apr 20 13:35:22 2014 +0530

    [MIFISX-1117] JUnit Test Cases for Fixed and Recurring deposits

[33mcommit 09cbbcd70d5ab574894b2b306cbd8eafd1013906[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Sat Apr 19 18:10:47 2014 +0530

    API documentation updated for FD and RD

[33mcommit d4c897af8b5f7d31dedb4fcc260a0fa8c8a8669e[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Fri Apr 18 23:42:10 2014 +0530

    MIFOSX-1076 - Added recalculation of charges on updating amount

[33mcommit 84f2ccfb85ad79b20eab140f90c840e8d2ae7f56[m
Merge: fdf04df 23751e7
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Apr 17 13:19:38 2014 -0700

    Merge pull request #865 from ashok-conflux/CONFLUX-39-JobChanges
    
    Removed duplicate Job POST_INTEREST_FOR_SAVINGS

[33mcommit fdf04dfe0afea8aa545f2f5bd79c864a3ca3fdfb[m
Merge: 7069328 9279446
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Apr 17 13:18:59 2014 -0700

    Merge pull request #866 from pramodn02/MIFOSX-1077
    
    MIFOSX-1077- added installment charge check to aviod wrong calculation

[33mcommit 927944683390c19eb4320c68534de2c94df325bc[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Fri Apr 18 00:46:23 2014 +0530

    MIFOSX-1077- added installment charge check to aviod wrong calculation

[33mcommit 23751e7017f2931bee4fe3b29e50c49362c44b1b[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Thu Apr 17 20:25:24 2014 +0530

    Removed duplicate Job POST_INTEREST_FOR_SAVINGS

[33mcommit 70693283d59e39a5b09ae34f7fa410829e30cff4[m
Merge: a0749d2 4e32896
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Apr 16 12:17:47 2014 -0700

    Merge pull request #862 from pramodn02/MIFOSX-1090
    
    MIFOSX-1090 - moved saving of loan out of upfront accrual check

[33mcommit a0749d26d1c8e1fafc73cce6e4d873b9c7ba4516[m
Merge: e60c4ec 7c399ce
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Apr 16 12:16:31 2014 -0700

    Merge pull request #863 from pramodn02/MIFOSX-1093
    
    MIFOSX-1093- modified scheduler jobs for performence issues

[33mcommit 7c399ce65351390f3461000521bbccf43abdcb17[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Wed Apr 16 23:04:20 2014 +0530

    MIFOSX-1093- modified scheduler jobs for performence issues

[33mcommit 4e3289621c7954927db7271a3944f52240ed1bf9[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Wed Apr 16 18:39:44 2014 +0530

    MIFOSX-1090 - moved saving of loan out of upfront accrual check

[33mcommit e60c4ec40dc168f8dfd374c1d9d90fc7f9475eab[m
Merge: d1e74d7 75635b3
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Apr 15 18:58:00 2014 -0700

    Merge pull request #861 from ashok-conflux/CONFLUX-39-1
    
    Advanced deposit products and accounts (FD & RD) with interest rate slabs

[33mcommit 75635b3b6f80dfcdf435853a46b4021d59a79165[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Mon Dec 30 10:53:04 2013 +0530

    Advanced deposit products and accounts (FD & RD) with interest rate slabs

[33mcommit d1e74d72d6b7524acc88af4d00ff3c703b45148a[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Apr 15 05:45:24 2014 -0700

    adding details of new optional field overdueDaysForNPA to loan product definition

[33mcommit 131c87c163bf759087414af0afa3145794cb6534[m
Merge: 9a26ba5 d10c27b
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Apr 15 03:13:32 2014 -0700

    Merge pull request #860 from vishwasbabu/develop
    
    update loan product api docs to reflect upfront and periodic accrual acc...

[33mcommit d10c27bd16ffc84c65747a3096659e06bedfd666[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Apr 15 03:12:02 2014 -0700

    update loan product api docs to reflect upfront and periodic accrual accounting

[33mcommit 9a26ba50ff9ed31d64ecc34c7c8e1b57d23515de[m
Merge: b85c47a 025e727
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Apr 14 13:32:39 2014 -0700

    Merge pull request #859 from pramodn02/MIFOSX-1081
    
     MIFOSX-1081 - Added Non Performing assets to Loan

[33mcommit 025e727757eef8849b4f8834de8ffd2436829e5c[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Tue Apr 15 00:16:14 2014 +0530

     MIFOSX-1081 - Added Non Performing assets to Loan

[33mcommit b85c47ad7538151645d2c365842641dc30677347[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Apr 13 11:56:52 2014 -0700

    MIFOSX-865 update validation to allow two flavours of accrual accounting

[33mcommit e82bffa94447eeeffbce681b2f3c2e48856a6e2f[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Apr 13 10:59:01 2014 -0700

    MIFOSX-865 update test case to use UPFRONT_ACCRUALS

[33mcommit b30aa2839e497c89d578bee9d2f097ee7231ce8c[m
Merge: 33ae3b6 739ca78
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Apr 13 01:11:07 2014 -0700

    Merge pull request #856 from vishwasbabu/develop
    
    MIFOSX-865 introduce new transaction type for accrual and deprecate inte...

[33mcommit 739ca7897a98363341cc91627e7f0ab37cfddc05[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Apr 13 01:08:27 2014 -0700

    MIFOSX-865 introduce new transaction type for accrual and deprecate interest applied and charge applied transaction types

[33mcommit 33ae3b6365196ff517a7da30fff95d42d7863aaf[m
Merge: bf04f97 ea6842e
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sat Apr 12 17:04:16 2014 -0700

    Merge pull request #855 from Vishwa1311/MIFOSX-1047
    
    [MIFOSX-1047] Added test cases for Apply Penalty to overdue loans Job

[33mcommit ea6842eced060cca522258cd12b1e84c947804c7[m
Author: Vishwa1311 <vishwanath@confluxtechnologies.com>
Date:   Sat Apr 12 19:42:16 2014 +0530

    [MIFOSX-1047] Added test cases for Apply Penalty to overdue loans Job

[33mcommit bf04f973334457c8107bebb7c2ff5ad4ba1500d4[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Apr 11 17:36:38 2014 -0700

    MIFOSX-865: fixing failing testing cases for accrual accounting

[33mcommit 4a0eae6fa1f125d33473635c6e2bd7894c1d0680[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Apr 11 14:37:54 2014 -0700

    MIFOSX-865: compilation issues with batch job for accruals for periodic posting

[33mcommit 304fde46bd8c4796af0cc1f30c00a813a4f4dd61[m
Merge: 9ddec84 fbc55d5
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Apr 11 14:28:40 2014 -0700

    Merge pull request #854 from vishwasbabu/develop
    
    MIFOSX-865: initial work around adding support for accrual accounting

[33mcommit fbc55d5dd18fe93d971b1af8756f0452add2363f[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Apr 11 14:25:29 2014 -0700

    MIFOSX-865: initial work around adding support for accrual accounting

[33mcommit 9ddec847403b28a8b251109affde14cbefbe8af4[m
Merge: 64d2795 20b0251
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Apr 11 12:42:14 2014 -0700

    Merge pull request #850 from smhom/MIFOSX-1046
    
    MIFOSX-1046

[33mcommit 64d27951a36c3791fc363bb76a841459adab4e6e[m
Merge: ccde866 506721a
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Apr 11 12:39:28 2014 -0700

    Merge pull request #853 from pramodn02/MIFOSX-1075
    
     MIFOSX-1075- added overdue penalty batch job in DB

[33mcommit ccde8666e06248ae6e4a24db2fc1ac68b1a0e275[m
Merge: 098060a a9eb4e4
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Apr 11 12:39:22 2014 -0700

    Merge pull request #851 from pramodn02/MIFOSX-1074
    
    MIFOSX-1074 - Added batch job for accrual accounting journal entry

[33mcommit 098060a44b7ab6d644e36a23b283eef076254298[m
Merge: 8dd9bfc f8f3ddc
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Apr 11 12:39:08 2014 -0700

    Merge pull request #852 from Vishwa1311/MIFOSX-1047
    
    [MIFOSX-1047] Updated and Completed JUnit Test Cases for Scheduler Jobs

[33mcommit 506721a5d8f474bf300cb81d90a48726270f5849[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Fri Apr 11 19:56:33 2014 +0530

     MIFOSX-1075- added overdue penalty batch job in DB

[33mcommit a9eb4e4f4dcc3931cc3a52ed4a3b630aa9a075bd[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Fri Apr 11 19:06:31 2014 +0530

    MIFOSX-1074 - Added batch job for accrual accounting journal entry

[33mcommit f8f3ddc06967a17e539076d4dcb993a9b89f2b7f[m
Author: Vishwa1311 <vishwanath@confluxtechnologies.com>
Date:   Fri Apr 11 18:48:16 2014 +0530

    [MIFOSX-1047] Updated and Completed JUnit Test Cases for Scheduler Jobs

[33mcommit 20b0251a9e583596281428d0444d75b87c6331f1[m
Author: Sherry Hom <sherryhom@gmail.com>
Date:   Thu Apr 10 23:47:19 2014 -0700

    MIFOSX-1046
    JUnit test cases for code and code values API's

[33mcommit 8dd9bfc0872dae4b480de5ad5cc0b743bf88f321[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Apr 10 13:53:57 2014 -0700

    cleaning up warnings in account transfers

[33mcommit c1acf1baae598e7590dc3fa4b6da44c326c4f7bb[m
Author: CieyouRaoul <cieyouraoul@musoni.eu>
Date:   Thu Apr 10 12:22:40 2014 +0200

    modified the ppi call to retrieve ppi entry. instead of returning the entry of the active ppi now return all the entries of a user

[33mcommit a4ec52085368691f720c226ba4275c9060f80ec7[m
Merge: 179e1e0 145318c
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Apr 9 21:58:50 2014 -0700

    Merge pull request #848 from lingalamadhukar/MIFOSX-1049
    
    MIFOSX-1027 Adding missed subresources

[33mcommit 145318c2600a83a73787a4caf0fa25ea491970e5[m
Author: Madhukar <madhukar@confluxtechnologies.com>
Date:   Thu Apr 10 09:57:37 2014 +0530

    MIFOSX-1027 Adding missed subresources

[33mcommit 179e1e087fd1676482a3bc0224396a506cfaff9e[m
Merge: 594d54f 2489997
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Apr 9 10:00:48 2014 -0700

    Merge pull request #847 from Vishwa1311/MIFOSX-1047
    
    [MIFOSX-1047] Updated Test Cases for Scheduler Jobs

[33mcommit 2489997e8bfd532b320b6141e7c68356e8573c19[m
Author: Vishwa1311 <vishwanath@confluxtechnologies.com>
Date:   Wed Apr 9 19:48:13 2014 +0530

    [MIFOSX-1047] Updated Test Cases for Scheduler Jobs

[33mcommit 594d54f099e749101ee4072f97fd07fcccdd8251[m
Merge: 6638bf8 209dd48
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Apr 8 13:25:37 2014 -0700

    Merge pull request #846 from pramodn02/MIFOSX-1025
    
    MIFOSX-1055 - added standing instructions

[33mcommit 6638bf87e4446c3397493eede5ad64f10601db12[m
Merge: b9bfced b59bcbf
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Apr 8 13:22:01 2014 -0700

    Merge pull request #845 from Vishwa1311/MIFOSX-1047
    
    [MIFOSX-1047] Fixed failure Test Cases and updated Scheduler Job Test Cases

[33mcommit 209dd4803134d2483a4c326d54cc1cf0be7c5a6b[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Tue Apr 8 22:59:16 2014 +0530

    MIFOSX-1055 - added standing instructions

[33mcommit b59bcbff0986b9f95f51961043335bdbab4318db[m
Author: Vishwa1311 <vishwanath@confluxtechnologies.com>
Date:   Tue Apr 8 20:16:14 2014 +0530

    [MIFOSX-1047] Fixed failure Test Cases and updated Scheduler Job Test Cases

[33mcommit b9bfced9c5f3d35cf51340d79bb7e34fd5035ed1[m
Merge: e385185 23759af
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Apr 7 17:59:28 2014 -0700

    Merge pull request #843 from lingalamadhukar/MIFOSX-1049
    
    MIFOSX-1053 Fix PAY_DUE_SAVINGS_CHARGES job

[33mcommit e38518589261f92d0d6f84a6872f34b6eb0149cf[m
Merge: bf67700 d842dee
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Apr 7 17:58:30 2014 -0700

    Merge pull request #844 from Vishwa1311/MIFOSX-1047
    
    [MIFOSX-1047] New udated Test Cases for Scheduler Jobs

[33mcommit d842dee5dd52651524e989aacd89e5a11b013d04[m
Author: Vishwa1311 <vishwanath@confluxtechnologies.com>
Date:   Mon Apr 7 20:05:09 2014 +0530

    [MIFOSX-1047] New udated Test Cases for Scheduler Jobs

[33mcommit 23759af6e0adcfc2cf866a4fba0421ce24f774cf[m
Author: Madhukar <madhukar@confluxtechnologies.com>
Date:   Mon Apr 7 12:46:35 2014 +0530

    MIFOSX-1053 Fix PAY_DUE_SAVINGS_CHARGES job

[33mcommit bf67700e6740ef8897e2863e20e57463d183ccf3[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Apr 4 22:45:15 2014 -0700

    Update INSTALL.md
    
    and fix typos

[33mcommit 1d0b20280641f9d6c7ce25552f9f459126185790[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Apr 4 22:43:55 2014 -0700

    Update INSTALL.md
    
    with reference to sample data

[33mcommit 1fdbc59e86c8d5fe9e54b2a5330bc3348e1b30ab[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Apr 4 22:42:03 2014 -0700

    Update INSTALL.md
    
    with details of loading sample data

[33mcommit 40efe1c1006a81f180a2148ceb4330da744be74f[m
Merge: 1bd685e 9c4ef17
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Apr 4 18:01:37 2014 -0700

    Merge pull request #842 from Vishwa1311/MIFOSX-1047
    
    [MIFOSX-1047] Updated JUnit Test Cases for Scheduler Jobs

[33mcommit 1bd685e98084c6d38672abaa8e41a2651d0db499[m
Merge: 25c35b6 c6f4a08
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Apr 4 18:00:16 2014 -0700

    Merge pull request #841 from goutham-M/mifosx-collectionsheer-1041
    
    mifosx-1041 collection sheet issue fix

[33mcommit 9c4ef176ec991c6d4a43f9bf8396932ff9ef94bc[m
Author: Vishwa1311 <vishwanath@confluxtechnologies.com>
Date:   Fri Apr 4 19:42:28 2014 +0530

    [MIFOSX-1047] Updated JUnit Test Cases for Scheduler Jobs

[33mcommit c6f4a085db62a32d28ee931fac3aec4ce8aaa890[m
Author: goutham-M <goutham@confluxtechnologies.com>
Date:   Fri Apr 4 17:44:01 2014 +0530

    mifosx-1041 collection sheet issue fix

[33mcommit 25c35b60d516a0802fdb1ff8fa932474ee442143[m
Merge: 872074f b8668af
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Apr 3 21:56:46 2014 -0700

    Merge pull request #838 from Vishwa1311/MIFOSX-1047
    
    [MIFOSX-1047] Latest updated Scheduler Job test cases

[33mcommit b8668af49cc77c4ec09a891076b4ab5417296f12[m
Author: Vishwa1311 <vishwanath@confluxtechnologies.com>
Date:   Thu Apr 3 19:59:26 2014 +0530

    [MIFOSX-1047] Latest updated Scheduler Job test cases

[33mcommit 0426325e03cea0367e4d6030df9ef5655f2abe8e[m
Author: andrew-dzak <andrewdzakpasu@musoni.eu>
Date:   Thu Apr 3 15:27:05 2014 +0200

    MIFOSX-1023

[33mcommit 872074fe8ee2910bc26f33da277c88096867d8e3[m
Merge: 235d4f0 e5316d3
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Apr 3 00:32:01 2014 -0700

    Merge pull request #836 from lingalamadhukar/MIFOSX-1049
    
    MIFOSX-1008 post journalentries if earned interest is > 0

[33mcommit e5316d35590fccd9d7e463c1af015819cdcaf03c[m
Author: Madhukar <madhukar@confluxtechnologies.com>
Date:   Thu Apr 3 12:08:44 2014 +0530

    MIFOSX-1008 post journalentries if earned interest is > 0

[33mcommit 235d4f010c526cc443bcdc14190da23d3ba76dfd[m
Merge: 155cfce 1c49268
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Apr 2 09:40:47 2014 -0700

    Merge pull request #833 from lingalamadhukar/MIFOSX-1027
    
    MIFOSX-1027 Fixing issues with maker checker

[33mcommit 155cfce9b23fda9ac116f201996215d5780b9435[m
Merge: 693d75a 77082ab
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Apr 2 09:35:25 2014 -0700

    Merge pull request #834 from Vishwa1311/MIFOSX-1047
    
    [MIFOSX-1047] Updated Test Cases for Scheduler Jobs

[33mcommit 693d75a67c34f513eb43adbc9c172b606f07342f[m
Merge: 1091eff a64c31f
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Apr 2 09:33:22 2014 -0700

    Merge pull request #835 from lingalamadhukar/MIFOSX-1049
    
    MIFOSX-1049 mifosx_sample_data

[33mcommit a64c31fd76cb6126efcf0859a15edf7b84dc12bf[m
Author: Madhukar <madhukar@confluxtechnologies.com>
Date:   Wed Apr 2 21:05:25 2014 +0530

    MIFOSX-1049 mifosx_sample_data

[33mcommit 77082ab6a0dd6c01faecb0866e54d1a4689dd294[m
Author: Vishwa1311 <vishwanath@confluxtechnologies.com>
Date:   Wed Apr 2 19:35:41 2014 +0530

    [MIFOSX-1047] Updated Test Cases for Scheduler Jobs

[33mcommit 1c49268e77638863475b4a209c87c9e098499322[m
Author: Madhukar <madhukar@confluxtechnologies.com>
Date:   Wed Apr 2 12:49:25 2014 +0530

    MIFOSX-1027 Fixing issues with maker checker

[33mcommit 1091eff2bf06f7c523a57fc1ee965a43391f04de[m
Merge: 591080b 646956a
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Apr 1 20:44:53 2014 -0700

    Merge pull request #829 from lingalamadhukar/MIFOSX-1008
    
    MIFOSX-998,MIFOSX-1008 Interest posting issue

[33mcommit 591080b843297c868cd62266988f17aee27c1b58[m
Merge: bf5b6f6 9cd7a9f
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Apr 1 11:17:32 2014 -0700

    Merge pull request #832 from Vishwa1311/MIFOSX-1047
    
    [MIFOSX-1047] JUnit test cases for Scheduler Jobs

[33mcommit bf5b6f695ea12dd203d17ecdc2ab1313b903711f[m
Merge: c007785 6d6c7af
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Apr 1 11:15:56 2014 -0700

    Merge pull request #830 from Vishwa1311/MIFOSX-1035
    
    [MIFOSX-1035] Updated Test Cases for Charges

[33mcommit 9cd7a9fe0d22e4a45897a15b1ff32a63d82225fe[m
Author: Vishwa1311 <vishwanath@confluxtechnologies.com>
Date:   Tue Apr 1 19:23:57 2014 +0530

    [MIFOSX-1047] JUnit test cases for Scheduler Jobs

[33mcommit c007785fbab96a16dffe27e8dc4b71a0042f5a03[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Mar 31 15:33:06 2014 -0700

    Update README.md
    
    with minor edits

[33mcommit bed30fcd47098f1e58feacb6b8a58eb909bace3d[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Mar 31 15:28:34 2014 -0700

    fixing typos in install.md

[33mcommit 2de2bb95c44a85332ed70d95e61b856bf4ed6b2c[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Mar 31 15:20:24 2014 -0700

    update the readme for community apps

[33mcommit f1c2c4a519dbad27d567fb5eed4e48a5b463bc01[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Mar 31 15:13:15 2014 -0700

    fix insert script for mifosplatform-tenants database

[33mcommit 954d65c83a56375bd7f40d9dec6fa4485248ba3c[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Mar 31 15:02:01 2014 -0700

    Update INSTALL.md
    
    move reference to database updates to section  2.2.2

[33mcommit b8042daa67e0ba2dda80514aa5e4b2422d0f9568[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Mar 31 14:44:20 2014 -0700

    Update INSTALL.md
    
    with instructions for those upgrading an existing Mifosx installation to version 1.21.* or higher

[33mcommit a997e0ac5d4bf9a1182d14b95ffb6be039aacd06[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Mar 31 14:32:27 2014 -0700

    Update README.md
    
    with links to API docs

[33mcommit e323fe8fc40e356c45c7c2219c2c27f58f1af153[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Mar 31 14:19:07 2014 -0700

    bump up the release number to 1.21.0

[33mcommit 7631a00398e3cf9cfa6d7b0c4047d4342a848954[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Mar 31 14:03:02 2014 -0700

    Update CHANGELOG.md
    
    for 1.21.0 RELEASE

[33mcommit efb83a8b826a92f237ce4498e0ceda7ce0349858[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Mar 31 13:42:13 2014 -0700

    Update README.md
    
    and strike out the reference app link

[33mcommit a2bc6b61ade31ecf4b95bc9d713ecdad822d6512[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Mar 31 13:40:36 2014 -0700

    Update README.md
    
    and update links to community and demo app

[33mcommit 4ce27ba0f3fc9154b723a4dfe2c7eda0bf4bf2d4[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Mar 31 13:37:38 2014 -0700

    updating insert script for tenants table

[33mcommit 6d6c7af5fdcc05645f9092381822645836e74587[m
Author: Vishwa1311 <vishwanath@confluxtechnologies.com>
Date:   Sat Mar 29 19:15:32 2014 +0530

    [MIFOSX-1035] Updated Test Cases for Charges

[33mcommit 646956ac99cdfeb02b44e9166c30532789b98a5c[m
Author: Madhukar <madhukar@confluxtechnologies.com>
Date:   Thu Mar 20 19:41:33 2014 +0530

    MIFOSX-998,MIFOSX-1008 Interest posting issue

[33mcommit dccbec3701092eb9053437dc53531d7de8604c66[m
Merge: 10b0baf 67b4442
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Mar 28 22:39:02 2014 -0700

    Merge pull request #828 from Vishwa1311/MIFOSX-1026-1
    
    [MIFOSX-1026] Failure test cases has been fixed

[33mcommit 10b0baf349f573732d3ebf5e672e41c669bfa860[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Mar 28 22:33:44 2014 -0700

    Update INSTALL.md
    
    and remove references to deprecated Reference App

[33mcommit 67b4442c03ab438579909ea6e90dbaff23b57791[m
Author: Vishwa1311 <vishwanath@confluxtechnologies.com>
Date:   Sat Mar 29 11:01:00 2014 +0530

    [MIFOSX-1026] Failure test cases has been fixed

[33mcommit fff47689f7b722f73fa357866c532a907db8437e[m
Merge: d228968 7c3d011
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Mar 28 19:32:59 2014 -0700

    Merge pull request #827 from Vishwa1311/MIFOSX-1035
    
    [MIFOSX-1035] JUnit Test Cases for Charges

[33mcommit d2289689c4fd0112b8b37bdfa6099be5e97afdc7[m
Merge: c9be118 d926822
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Mar 28 19:31:58 2014 -0700

    Merge pull request #826 from Vishwa1311/MIFOSX-1026_1
    
    [MIFOSX-1026] JUnit Test Cases for Global Configuration

[33mcommit 7c3d011db04492643065ff870ea8735bceec99b1[m
Author: Vishwa1311 <vishwanath@confluxtechnologies.com>
Date:   Thu Mar 27 14:42:39 2014 +0530

    [MIFOSX-1035] JUnit Test Cases for Charges
    
    [MIFOSX-1035] JUnit Test Cases for Charges

[33mcommit d926822573c6b3da6132fb6ed17df669501c66b6[m
Author: Vishwa1311 <vishwanath@confluxtechnologies.com>
Date:   Wed Mar 26 19:47:59 2014 +0530

    [MIFOSX-1026] JUnit Test Cases for Global Configuration
    
    [MIFOSX-1026] JUnit Test Cases for Global Configuration

[33mcommit c9be118c324b26d94c98f4de86499922df45397b[m
Merge: 66178b2 59b7ace
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Mar 28 00:33:25 2014 -0700

    Merge pull request #825 from Vishwa1311/MIFOSX-1003
    
    [MIFOSX-1003] All the failing test cases has been fixed

[33mcommit 59b7acee94ed1fcdd46c7dff0b88815d1e21d705[m
Author: Vishwa1311 <vishwanath@confluxtechnologies.com>
Date:   Fri Mar 28 12:03:04 2014 +0530

    [MIFOSX-1003] Failing test cases has been fixed

[33mcommit 66178b27a4cb48c4a427ad2c04632fdd52898d7a[m
Merge: 7319c21 d8528aa
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Mar 27 04:02:09 2014 -0700

    Merge pull request #823 from lingalamadhukar/MIFOSX-998
    
    Activation permission check for group and center

[33mcommit d8528aa96c883e0e92174a552432833b032527fd[m
Author: Madhukar <madhukar@confluxtechnologies.com>
Date:   Thu Mar 27 15:49:33 2014 +0530

    Activation permission check for group and center

[33mcommit 7319c21890baaeec49ed7355a2989c4aa1e12399[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Mar 27 02:51:41 2014 -0700

    minor refactor for LoanWritePlatformServiceJpaRepositoryImpl

[33mcommit 1fdee8aa70e04383c2b11ffc8903edd7094f80bf[m
Merge: 2588190 e2ee6d1
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Mar 27 02:39:09 2014 -0700

    Merge pull request #807 from lingalamadhukar/MIFOSX-909
    
    #MIFOSX-886,#MIFOSX-835,#MIFOSX-996,#MIFOSX-990,#MIFOSX-957

[33mcommit 25881904f15713528ea6f7c3f8d9918913809c70[m
Merge: 4f231f2 71a6399
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Mar 27 00:11:23 2014 -0700

    Merge pull request #808 from Vishwa1311/MIFOSX-1003
    
    [MIFOSX-1003] JUnit Test Cases for Accounting with Savings

[33mcommit 4f231f2888760a04fc0d5ccd142f929d06e7d3e4[m
Merge: c870e0d d26b14b
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Mar 27 00:11:13 2014 -0700

    Merge pull request #816 from Vishwa1311/MIFOSX-1012
    
    [MIFOSX-1012] JUnit Test Cases for Savings Account Transfer

[33mcommit 59429326d12fe853a5e15c506bb97d0a95c9b8d8[m
Author: CieyouRaoul <cieyouraoul@musoni.eu>
Date:   Fri Mar 7 13:13:49 2014 +0100

    PPI implementation
    
    ppi step 2 work in progress
    
    wip
    
    work in progress
    
    wip
    
    ppi permission
    
     done

[33mcommit c870e0d26f58edc96786bcce50a2b05d6634c2f4[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Mar 26 02:55:28 2014 -0700

    changing table collation for patch v_157

[33mcommit d940c72d6101b0c1f164fee919ddffd02c7be6c4[m
Merge: e3637bb e1917f9
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Mar 26 02:53:18 2014 -0700

    Merge pull request #818 from pramodn02/MIFOSX-1025
    
    MIFOSX-1025 - Added Overdue Penalty recurrence

[33mcommit e3637bb2af21fad6646f2706233397078762c69d[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Mar 26 02:50:37 2014 -0700

    cleaning up MIFOSX-1013

[33mcommit e1917f99a21820b9a1319c070d7d0762876850ef[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Wed Mar 26 15:14:22 2014 +0530

    MIFOSX-1025 - Added Overdue Penalty recurrence

[33mcommit d3ebc2b4dd7799d421bc0d55f2413f84a701b1c4[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Mar 26 01:51:08 2014 -0700

    bumping up sql patch revision number for mifosx-1015

[33mcommit da2365caa06b3d8b9ac55cf4b95bcb229d70f749[m
Merge: f28e26c 0678533
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Mar 26 01:49:52 2014 -0700

    Merge pull request #817 from goutham-M/mifosx-savingloan-a/c-reports
    
    loan txn, repayment. saving txns mifosx-1015

[33mcommit f28e26c1e4728171c83162d22860614043f2a4d1[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Mar 26 01:21:31 2014 -0700

    cleaning up MIFOSX-1014

[33mcommit 8b86bde18b4821cd9228069a63b1e5c70e76f136[m
Merge: 09f26f1 b25e121
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Mar 26 01:09:45 2014 -0700

    Merge pull request #814 from Musoni/MIFOSX-1014
    
    MIFOSX-1014 - Fixed MakerChecker for Loans,Savings and possibly many mor...

[33mcommit 09f26f12133e264bc3fb153c7a104e35dcaa461c[m
Author: andrew-dzak <andrewdzakpasu@musoni.eu>
Date:   Wed Mar 26 00:15:10 2014 -0700

    mifosx-986 fixes from andrew

[33mcommit 025a18b54a0d3c3f5ed70362fc0ba3cb64989c60[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Mar 25 23:15:59 2014 -0700

    cleaning up MIFOSX-1013

[33mcommit 598091b1b2c1feb44154625d280e91d21dfecd67[m
Author: andrew-dzak <andrewdzakpasu@musoni.eu>
Date:   Fri Mar 21 16:03:11 2014 +0100

    Mifosx-1013 done

[33mcommit 33aff878f581a2851b03a98a56a4b24040775cff[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Mar 25 22:25:54 2014 -0700

    Delete report-designer.properties
    
    as it seems to be checked in by accident....

[33mcommit d3cc54b738b2d94b2e73602718de1512abb29365[m
Merge: a9cff14 f58dc4b
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Mar 25 22:24:41 2014 -0700

    Merge pull request #815 from awesh/mifosx-1010
    
    Converted Stretchy Report Into Pentaho Pentaho mifosx-1010

[33mcommit a9cff14e9102cce3d6233cb38bceaf74e3898d53[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Mar 25 18:20:34 2014 -0700

    Update CHANGELOG.md
    
    with new link to sql patch file

[33mcommit d8156dc8a04b7341ffbcb5ad7dacbc54803df007[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Mar 25 18:17:22 2014 -0700

    deleting duplicate update scripts for tenant schema

[33mcommit dc377c1d5c0b1294ce8544e9567cca68d0462a5a[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Mar 25 17:41:24 2014 -0700

    adding tenant schema migration patch for flyway

[33mcommit 3681e3af33f63b0c51c9578cac5f74c40bc90b79[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Mar 25 17:15:28 2014 -0700

    Update CHANGELOG.md
    
    to add a note that for users updating to latest version on dev branch, an sql patch needs to be run on mifosplatform-tenants schema

[33mcommit 0ca68e98d5a04806529f2a4335d44019edda4ffc[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Mar 25 17:10:17 2014 -0700

    MIFOSX-949 cleaning up db upgrades and externalizing remaining properties

[33mcommit d26b14b85ab54665aae12189c1d7f38b6edc31fa[m
Author: Vishwa1311 <vishwanath@confluxtechnologies.com>
Date:   Tue Mar 25 16:48:43 2014 +0530

    [MIFOSX-1012] JUnit Test Cases for Account Transfer

[33mcommit f89a5737a7aab2578054360324df8c8fe4fd718c[m
Author: Vishwa1311 <vishwanath@confluxtechnologies.com>
Date:   Fri Mar 21 19:07:17 2014 +0530

    [MIFOSX-1012] JUnit Test Cases for Savings Account Transfer
    
    JUnit Test Cases for Savings Account Transfer

[33mcommit 06785331e1dc7d291f3d64755ecbd08535813250[m
Author: goutham-M <goutham@confluxtechnologies.com>
Date:   Mon Mar 24 18:42:28 2014 +0530

    loan txn, repayment. saving txns mifosx-1015

[33mcommit f58dc4b737d9659bf10810870e3386939bdccb39[m
Author: Awesh <awesh@confluxtechnologies.com>
Date:   Mon Mar 24 13:43:43 2014 +0530

    Converted Stretchy Report Into Pentaho Pentaho mifosx-1010

[33mcommit b25e121f571d6cdb4bd8b9966b9cb2781a250e1a[m
Author: Sander van der Heyden <sander@musoni.eu>
Date:   Fri Mar 21 18:51:29 2014 +0100

    MIFOSX-1014 - Fixed MakerChecker for Loans,Savings and possibly many more

[33mcommit 71a6399d3d5a77028e817bc95c05c8bf78d19ea6[m
Author: Vishwa1311 <vishwanath@confluxtechnologies.com>
Date:   Fri Mar 21 17:07:13 2014 +0530

    JUnit Test Cases for Accounting with Savings

[33mcommit b7e30f1aa4ded3b5f6bedd05aa553fcb939841ff[m
Author: Vishwa1311 <vishwanath@confluxtechnologies.com>
Date:   Wed Mar 19 18:47:17 2014 +0530

    [MIFOSX-1003] JUnit Test Cases for Accounting with Savings
    
    [MIFOSX-1003] JUnit Test Cases for Accounting with Savings Updated

[33mcommit e2ee6d1f5ef9afbc76fdb2d9d528d9e72980461f[m
Author: Madhukar <madhukar@confluxtechnologies.com>
Date:   Wed Mar 19 21:31:41 2014 +0530

    #MIFOSX-886,#MIFOSX-835,#MIFOSX-996,#MIFOSX-990,#MIFOSX-957

[33mcommit d079442e0ec322fe01b08906da2521475380b9bb[m
Author: Channa Gayan <channagayan@gmail.com>
Date:   Sat Mar 15 22:42:21 2014 +0530

    Changes to issue: MIFOSX-949

[33mcommit e496ccf02af6b2d2adc21c13b2cdfe2bc7fe8ecd[m
Merge: 1c0bf0e d05dd0e
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Mar 18 12:47:56 2014 -0700

    Merge pull request #805 from Vishwa1311/MIFOSX-993
    
    [MIFOSX-993] JUnit Integration Test Cases for GroupSavings.

[33mcommit 1c0bf0ef3c9d8eaf95f97f591c88ada227a8efa7[m
Merge: b9b5dd1 5e9e43e
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Mar 18 01:33:17 2014 -0700

    Merge branch 'master' into develop

[33mcommit 5e9e43e1f470a58a06b9727f46be5e6e0c7acf58[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Mar 18 01:30:32 2014 -0700

    Updates for 1.20.1 Release

[33mcommit 074c0a9fb5f82d5f84b9019a671101e7c8b03118[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Mar 18 01:28:23 2014 -0700

    Update CHANGELOG.md
    
    for 1.20.1 Release

[33mcommit b9b5dd1da51e7fe4b435d7af811264901408a49e[m
Merge: 412ab77 7150878
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Mar 17 20:30:33 2014 -0700

    Merge pull request #802 from pramodn02/MIFOSX-994
    
     MIFOSX-994 - removed the constrain for overdraft account

[33mcommit 412ab773f87abd9d727dfa99a7050fb542c42f62[m
Merge: aede199 783bacb
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Mar 17 20:13:07 2014 -0700

    Merge pull request #803 from lingalamadhukar/MIFOSX-909
    
    #MIFOSX-941,#MIFOSX-942,#MIFOSX-909

[33mcommit 783bacbcb561e0ea8209eda10d645d5561407728[m
Author: Madhukar <madhukar@confluxtechnologies.com>
Date:   Mon Mar 17 20:18:39 2014 +0530

    #MIFOSX-941,#MIFOSX-942,#MIFOSX-909

[33mcommit 6c1db9f4fbb3f1747b7de05d4f7b1ac0e4da7892[m
Author: Sander van der Heyden <sander@musoni.eu>
Date:   Mon Mar 17 12:05:45 2014 +0100

    MIFOSX-995 - Fixed Grace periods

[33mcommit 71508780a0ef8b8e06cf4e921d9ec873415fd24a[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Mon Mar 17 17:13:21 2014 +0530

     MIFOSX-994 - removed the constrain for overdraft account

[33mcommit d05dd0e9b6ba175474733a8eb87a352e4d5ee1a2[m
Author: Vishwa1311 <vishwanath@confluxtechnologies.com>
Date:   Mon Mar 17 11:41:01 2014 +0530

    [MIFOSX-993] JUnit Integration Test Cases for GroupSavings

[33mcommit aede1995c6a1c491045e0ede2d95331d05a52d54[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Mar 16 21:00:12 2014 -0700

    gradle property updates for 1.20.0.Release

[33mcommit ac33892bb689d26c0f104317f482615a50f404e2[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Mar 16 19:56:04 2014 -0700

    Update CHANGELOG.md

[33mcommit e12bd4de3252a6a7ac9d4f8f3caa9a803faa5671[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Mar 16 19:55:45 2014 -0700

    Update CHANGELOG.md
    
    for 1.20.0.RELEASE

[33mcommit f2766a7e66ed7e4e1bfc1a9da6112aaa8efe3c13[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Mar 16 14:10:14 2014 -0700

    minor updates to ageing details query

[33mcommit 45fbdadff2b12024d9bfb6a15d115e2f15331875[m
Author: Awesh <awesh@confluxtechnologies.com>
Date:   Tue Mar 4 19:54:11 2014 +0530

    MIFOSX-955 strechy reporty issue(fixed)

[33mcommit 85aa76eaa18a46748001a9eea17eb4230ea4b86c[m
Merge: 76c1f83 d92d2f7
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Mar 13 18:28:40 2014 -0700

    Merge pull request #798 from lingalamadhukar/MIFOSX-985
    
    #MIFOSX-985, Added missed permissions

[33mcommit 76c1f83502d76e3e85f0cbca9a7e60853fdbd94e[m
Merge: 0862c4b 6b057e5
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Mar 13 18:26:28 2014 -0700

    Merge pull request #799 from pramodn02/MIFOSX-975
    
    MIFOSX-976 - added editing values for global configurations

[33mcommit 6b057e54924483e021659df1c81747d509cfc915[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Thu Mar 13 13:56:50 2014 +0530

    MIFOSX-976 - added editing values for global configurations

[33mcommit d92d2f7d8c8b9ec9f19997752cfe9cfb3c7ac2b2[m
Author: Madhukar <madhukar@confluxtechnologies.com>
Date:   Thu Mar 13 13:16:45 2014 +0530

    #MIFOSX-985, Added missed permissions

[33mcommit 0862c4b7c611f3d81eade9b601801a43d6df35eb[m
Merge: 41e6154 2dd6dea
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Mar 12 18:50:37 2014 -0700

    resolvings conflicts for MIFOSX-216

[33mcommit 41e615462287bb89ab7516b3f0f4aefe7b02fa82[m
Merge: 9746555 e81e8ed
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Mar 12 16:06:07 2014 -0700

    Merge pull request #795 from pramodn02/MIFOSX-945
    
    MIFOSX-945 - added null check for id while sorting the transactions

[33mcommit 9746555cebdbcb7a45228e27f0368b5bc89f1c53[m
Merge: 0e9c0f7 f7ab111
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Mar 12 16:05:14 2014 -0700

    Merge pull request #794 from pramodn02/MIFOSX-980
    
     MIFOSX-980 - added boolean flag for first repayment date caculation

[33mcommit 0e9c0f7c8fd362cecd34d00574738f86c88df577[m
Merge: c67921a a76e710
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Mar 12 16:01:11 2014 -0700

    Merge pull request #797 from pramodn02/MIFOSX-975
    
    MIFOSX-975 - added Arrears status

[33mcommit a76e7107b38d2976d6a937fa361edc21877b5031[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Wed Mar 12 21:25:26 2014 +0530

    MIFOSX-975 - added Arrears status

[33mcommit 2dd6dea1a22bd1ae43b493b08491395f27b035d0[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Wed Mar 12 12:01:07 2014 +0530

    MIFOSX-216 - added grace before In Arrears

[33mcommit e81e8edafa5857d2ec0f2a1f17bf1e29ffe69914[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Tue Mar 11 17:33:48 2014 +0530

    MIFOSX-945 - added null check for id while sorting the transactions

[33mcommit f7ab111190803ded693e11482309c22b5eefa076[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Tue Mar 11 15:03:58 2014 +0530

     MIFOSX-980 - added boolean flag for first repayment date caculation

[33mcommit c67921a0141423f9d559798b9d4b210e2c2210cb[m
Merge: e73b5cf 6d11356
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Mar 11 00:13:07 2014 -0700

    Merge pull request #793 from pramodn02/savings_test
    
    test case corrections

[33mcommit 6d11356204105ff7cf2ce006fcec27fc6dfef2fa[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Tue Mar 11 12:35:40 2014 +0530

    test case corrections

[33mcommit e73b5cf365dbfb6a9b87e9f32364ed457525e4a1[m
Merge: 3531601 286aaf9
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sat Mar 8 11:28:59 2014 -0800

    Merge pull request #791 from pramodn02/savings_test
    
    added savings test cases

[33mcommit 286aaf9ea99aa8f3cde2b942467e98640ddaa775[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Sat Mar 8 22:42:02 2014 +0530

    added savings test cases

[33mcommit 3531601ed6605d96b5cc3252ada2e807d80c6442[m
Merge: 34d0d83 123ec12
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Mar 6 01:00:55 2014 -0800

    Merge pull request #788 from pramodn02/MIFOSX-954
    
    MIFOSX-954 - added update of default savings

[33mcommit 34d0d83c7b0b0bd175fed033fb51466d6a5c9fd0[m
Merge: 119a496 363e448
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Mar 6 00:59:36 2014 -0800

    Merge pull request #790 from pawandubey/api-doc
    
    MIFOSX-948 Added UGD links to API DOCS

[33mcommit 363e448521ac057d3be1457f78e1c9d31b1db95a[m
Author: Pawan Dubey <pawanthegunner@gmail.com>
Date:   Wed Mar 5 22:41:49 2014 +0530

    MIFOSX-948 Added UGD links to API DOCS

[33mcommit 119a49602c436b1c341fd5463d5699b375c26679[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Mar 5 05:38:16 2014 -0800

    updates for 1.19.0 release

[33mcommit 52a564c74a3bad2c381c933315da57f22fb83f0d[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Mar 5 05:25:56 2014 -0800

    adding missing property to clientSavingsSummar

[33mcommit 123ec128c6b8f452452c32264646d4a5b15e98ec[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Wed Mar 5 10:20:44 2014 +0530

    MIFOSX-954 - added update of default savings

[33mcommit 1d187f7ddf496eeea21e6afeb8dc6d86e8f83880[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Mar 4 18:26:59 2014 -0800

    adding category for newly added savings reports

[33mcommit cbd09dffccda40e14a3feaf372358c0c6bd9e396[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Mar 4 08:27:03 2014 -0800

    Update CHANGELOG.md

[33mcommit e72734bfd668aaeb4bb5c7f016ac2a206dc9226e[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Mar 4 08:26:10 2014 -0800

    Update CHANGELOG.md
    
    for 1.19.0 release

[33mcommit 75dc87cccf1cb9a1ef7ea79bfa69adf6f265a4e6[m
Merge: fb1c158 df88e7d
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Mar 3 22:32:53 2014 -0800

    Merge pull request #785 from pramodn02/MIFOSX-932
    
    MIFOSX-932 - chaged the query to fetch hierarchy

[33mcommit df88e7d17a7e32e6f57cac93494a9eacde7131b7[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Tue Mar 4 12:00:15 2014 +0530

    MIFOSX-932 - chaged the query to fetch hierarchy

[33mcommit fb1c158e9555b752bb29c54ef7e2c8dc7a247bfb[m
Merge: 1e63957 cf9a900
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Mar 3 21:31:35 2014 -0800

    Merge pull request #784 from pramodn02/MIFOSX-931
    
    MIFOSX-931 - added API doc for overdraft account

[33mcommit cf9a90042a0a028fd39eae1b8dd659ab3a4a34fd[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Tue Mar 4 10:43:49 2014 +0530

    MIFOSX-931 - added API doc for overdraft account

[33mcommit 1e63957dc927fc284317ac08c4b75a277907b27d[m
Merge: b02a357 ea23257
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Mar 3 15:21:18 2014 -0800

    Merge pull request #783 from pramodn02/MIFOSX-932
    
    MIFOSX-932 - Basic Savings reports

[33mcommit ea232576ba13bd4441e8b17eccbe4671ba358711[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Tue Mar 4 00:27:47 2014 +0530

    MIFOSX-932 - Basic Savings reports

[33mcommit b02a357581be8cec52642bfd90fb1a0f052107c1[m
Merge: c404dfd 1c1e83e
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sat Mar 1 04:54:10 2014 -0800

    Merge pull request #781 from vishwasbabu/develop-new
    
    applying format across the project and fixing all warnings

[33mcommit 1c1e83eb5e5a77372bba79217dbdae34fcb14aa2[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sat Mar 1 04:40:57 2014 -0800

    applying format across the project and fixing all warnings
    
    Conflicts:
    	mifosng-provider/src/main/java/org/mifosplatform/portfolio/client/service/ClientWritePlatformServiceJpaRepositoryImpl.java
    	mifosng-provider/src/main/java/org/mifosplatform/portfolio/loanaccount/service/LoanAssembler.java
    	mifosng-provider/src/main/java/org/mifosplatform/portfolio/savings/service/SavingsApplicationProcessWritePlatformService.java
    	mifosng-provider/src/main/java/org/mifosplatform/portfolio/savings/service/SavingsApplicationProcessWritePlatformServiceJpaRepositoryImpl.java

[33mcommit c404dfdeb15a160cee5984260ca1e005a345c054[m
Merge: 975ae07 4fef1d0
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sat Mar 1 02:47:22 2014 -0800

    Merge pull request #779 from lingalamadhukar/MIFOSX-946
    
    MIFOSX-946 capture loanpurpose for group and jlg loans

[33mcommit 975ae07cf79d16c0f1ad6dbc9ddad6b16993f3c0[m
Merge: 4ae3e99 fb6c544
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sat Mar 1 02:40:11 2014 -0800

    Merge pull request #780 from vishwasbabu/develop
    
    fix for MIFOSX-884

[33mcommit fb6c5440dc1e2b1fd028cb1e8a835185ded97f94[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sat Mar 1 02:37:12 2014 -0800

    fix for MIFOSX-884

[33mcommit 4fef1d0b48e8d3347dc5c09e679dbdb9d046e11a[m
Author: Madhukar <madhukar@confluxtechnologies.com>
Date:   Sat Mar 1 14:09:09 2014 +0530

    MIFOSX-946 capture loanpurpose for group and jlg loans

[33mcommit 4ae3e99c187fbd79ef88656185fdb6d2db48cd07[m
Merge: 8519388 ca24c40
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Feb 28 11:09:58 2014 -0800

    Merge pull request #774 from pramodn02/MIFOSX-929
    
    MIFOSX-929 - added check for multiple permission for client

[33mcommit 85193881b5ea7b963b7f79394d180bf1ecd84c27[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Feb 28 10:56:34 2014 -0800

    clean up to created Date field in SavingsAccountTransactionDTO

[33mcommit 9a668e6cacb0f6885c8d654bd19e519f3342aed0[m
Merge: e6b2a35 b9fa92f
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Feb 28 10:45:59 2014 -0800

    Merge pull request #778 from pramodn02/savingstransaction
    
    MIFOSX-939 - corrected order of saving transactions

[33mcommit b9fa92f4e6563d3f32b1a74a913853b2e736e181[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Fri Feb 28 22:52:36 2014 +0530

    removed commented code

[33mcommit 59b7bcc499fd60db3657ea06f2dbee9c2d8a01e9[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Fri Feb 28 22:47:58 2014 +0530

    MIFOSX-939 - corrected order of saving transactions

[33mcommit e6b2a35f41e55d44cc64d2d882bb8cb3b4e63659[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Feb 27 22:28:19 2014 -0800

    updates to MIFOSX-888

[33mcommit ca24c402f96de64a2122fc3fd97f9f56a04d37e7[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Thu Feb 27 09:33:27 2014 +0530

    removed boolean value from result

[33mcommit 85297aaa65c35fc7db58e7063ed619e7da6f4f3b[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Wed Feb 26 20:09:03 2014 +0530

    MIFOSX-929 - added check for multiple permission for client

[33mcommit fd32d997e7404ba563d66462fe5d9df0633af26f[m
Merge: 7ad240b 69861ba
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Feb 25 01:44:38 2014 -0800

    Merge pull request #773 from pramodn02/savings
    
    MIFOSX-931 - added current account

[33mcommit 69861ba62444d7f19f0b9109803c3d9a039ae330[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Tue Feb 25 13:50:04 2014 +0530

    MIFOSX-931 - added current account

[33mcommit 7ad240b1af4be9e7f1f5da77d5543e391611798c[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Feb 23 16:52:19 2014 -0800

    updates for 1.18.0 release

[33mcommit cf51a85cdcd3c0a941877cd85280ba39f00c3e2d[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Feb 23 16:46:24 2014 -0800

    Update CHANGELOG.md
    
    for 1.18.0 release

[33mcommit 6653dae41b2a1a94db5bb08c462a52d76a72723a[m
Merge: 6ef342a 219bd33
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Feb 23 16:40:15 2014 -0800

    merging 1.17.1 into develop

[33mcommit 219bd33adf99a12319f0e01a4044df742c79d5c9[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Feb 23 16:35:24 2014 -0800

    updating gradle properties for 1.17.1 release

[33mcommit 61236ae24923a0b4731e3711419078fff5c39c9a[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Feb 23 16:29:59 2014 -0800

    Update CHANGELOG.md
    
    for 1.17.1 release

[33mcommit 6ef342a4866356d803dc7c50da4e21c799ff0b96[m
Merge: a3a6432 b120108
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Feb 23 15:34:09 2014 -0800

    Merge pull request #772 from emmanuelnnaa/MIFOSX-923
    
    MIFOSX-923 - entity status property added to the search resultset

[33mcommit b1201087c2262e773128809a826036c6ca694d26[m
Author: Emmanuel Nnaa <emmanuelnnaa@musoni.eu>
Date:   Fri Feb 21 16:56:01 2014 +0100

    MIFOSX-923 - entity status property added to the search resultset

[33mcommit a3a643258a817048bc7cdbab0de3be89f56e7de7[m
Merge: 4865e2a bd816e2
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Feb 19 21:25:30 2014 -0800

    Merge pull request #771 from pramodn02/MIFOSX-920
    
    MIFOSX-920 - corrected parser to return number if it is bigdecimal

[33mcommit bd816e27bd9e5b762f08623473778f21e45c2479[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Thu Feb 20 10:41:21 2014 +0530

    MIFOSX-920 - corrected parser to return number if it is bigdecimal

[33mcommit 4865e2a1563e4519bb5a57cfde4ad72aa1153eec[m
Merge: 3277f84 cd42134
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Feb 18 20:45:12 2014 -0800

    Merge pull request #750 from lingalamadhukar/MIFOSX-880
    
    MIFOSX-880 calculate interest from interestChargedFromDate

[33mcommit 3277f848115a79b394de6190d63b7471f33709ce[m
Merge: 89e3016 d233356
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Feb 18 20:09:20 2014 -0800

    Merge pull request #753 from goutham-M/mifosx-collectionsheet-enhancement
    
    MIFOSX-902 productive collection sheet feature is added

[33mcommit 89e3016fc906e71c58f1fcaec068ef4584b8f3cd[m
Merge: 5e82d5c 6361502
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Feb 18 19:55:24 2014 -0800

    Merge pull request #770 from pramodn02/MIFOSX-847_1
    
    MIFOSX-847 - added variations in loan data for clients

[33mcommit 63615022df42b766c0be0bbe600fb21db25daf5f[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Wed Feb 19 00:03:35 2014 +0530

    MIFOSX-847 - added variations in loan data for clients

[33mcommit 2a21eda984d31b1e7bd264c2c3a799ca7b96d57c[m
Merge: 02191d6 a7b4f77
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Feb 18 18:46:05 2014 -0800

    Merge pull request #761 from lingalamadhukar/MIFOSX-911
    
    MIFOSX-911 Update Api doc issues

[33mcommit 5e82d5c3201ffc3774171e0b6faaf02d81440432[m
Merge: 05fa39a 2d44a28
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Feb 18 18:43:18 2014 -0800

    Merge pull request #769 from lingalamadhukar/MIFOSX-864
    
    MIFOSX-864 Add timeline for list client loans

[33mcommit 05fa39ac1e6ea657806d445db7a55feed803b89d[m
Merge: 65eab01 289e530
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Feb 18 18:30:29 2014 -0800

    Merge pull request #754 from Musoni/MIFOSX-900
    
    MIFOSX-900 - Refuse deactivating charges that are currently in use

[33mcommit 65eab01daac957c60648eb1bb51a2e3386dde4eb[m
Merge: c548f50 ade1bd3
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Feb 18 16:52:18 2014 -0800

    Merge pull request #768 from pramodn02/MIFOSX-92_2
    
    MIFOSX-92 - modified column names

[33mcommit 2d44a2860768bf171a65de91c93e4b513db416dc[m
Author: Madhukar <madhukar@confluxtechnologies.com>
Date:   Tue Feb 18 14:31:47 2014 +0530

    MIFOSX-864 Add timeline for list client loans

[33mcommit ade1bd38cffda9f2103a0971756c6c14809b9a59[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Tue Feb 18 11:25:52 2014 +0530

    MIFOSX-92 - modified column names

[33mcommit c548f501dedf3e65bc04bc17caba45f81d689a05[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Mon Feb 17 09:20:47 2014 -0800

    MIFOSX-92 Tranche loan functionality

[33mcommit 0bb6e0d9f4f4468c7aee5e9d27a234f4198b8b4b[m
Merge: 3460093 4a2ffe5
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Feb 16 19:11:19 2014 -0800

    Merge pull request #755 from pramodn02/MIFOSX-906
    
    MIFOSX-906 - added creation of transaction id before creating Journal En...

[33mcommit 34600936aad2c7947ee59a3653c610aa13208338[m
Merge: fd6551b 908de1c
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Feb 16 19:07:11 2014 -0800

    Merge pull request #764 from vorburger/readMeDemoBeta
    
    add link to demo.openmf.org/beta/ Community App to README

[33mcommit fd6551bcbad1b9745d1f65c8f66b212c29a79a75[m
Merge: 0651468 07b026a
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Feb 16 19:06:01 2014 -0800

    Merge pull request #766 from pramodn02/MIFOSX-895
    
    MIFOSX-895 : corrected validations

[33mcommit 07b026aee05023b3ee66870082af52419a558bb8[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Sat Feb 15 14:14:24 2014 +0530

    MIFOSX-895 : corrected validations

[33mcommit 908de1c8497564b395f65f795c5fcdd149568321[m
Author: Michael Vorburger <mike@vorburger.ch>
Date:   Sat Feb 15 01:46:33 2014 +0100

    add link to demo.openmf.org/beta/ Community App to README

[33mcommit 0651468bebd8c2b9bc68bfba3d6dca3ec4da35ee[m
Merge: 25a32ea a889dbd
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Feb 13 23:05:23 2014 -0800

    Merge pull request #756 from Musoni/MIFOSX-867
    
    MIFOSX-867 - Fixed disbursement or submit date after repayment startdate...

[33mcommit 25a32ea92518dc259e2b60d46aff6b6abb213d4e[m
Merge: 13f8c6c 4b36019
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Feb 13 23:03:15 2014 -0800

    Merge pull request #760 from goutham-M/mifosx-journalentryerrmsgfix
    
    MIFOSX-910 for journal entry posting not proper err msg displaying fixed

[33mcommit 13f8c6cac8b8b8db168342991ccfe0a62649917d[m
Merge: ac93297 57078ec
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Feb 13 17:48:51 2014 -0800

    Merge pull request #751 from emmanuelnnaa/MIFOSX-901
    
    MIFOSX-901 - "BIT" datatype/columntype missing displaytype, throws "error.msg.invalid.lookup.type" error message

[33mcommit a7b4f774985bb703714ad15681195f0ec1a6391b[m
Author: Madhukar <madhukar@confluxtechnologies.com>
Date:   Wed Feb 12 13:25:14 2014 +0530

    MIFOSX-911 Update Api doc issues

[33mcommit a889dbdd2fb5512be00c2c59fa3d665fc79430ea[m
Author: Sander van der Heyden <sander@musoni.eu>
Date:   Tue Feb 11 11:00:30 2014 +0100

    MIFOSX-867 - Fixed disbursement or submit date after repayment startdate by adding validations

[33mcommit 4a2ffe5e48dfe64cad9fd72575fab37cee525b20[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Tue Feb 11 15:18:37 2014 +0530

    MIFOSX-906 - added creation of transaction id before creating Journal Entries

[33mcommit 289e5309fcec36caefa10e770d5af048d24418ed[m
Author: Sander van der Heyden <sander@musoni.eu>
Date:   Tue Feb 11 10:11:50 2014 +0100

    MIFOSX-900 - Refuse deactivating charges that are currently in use

[33mcommit d23335603fabfb87971f1be6134091ddfaf6d9b6[m
Author: goutham-M <goutham@confluxtechnologies.com>
Date:   Tue Feb 11 12:51:42 2014 +0530

    MIFOSX-902 productive collection sheet feature is added

[33mcommit 57078ecdc49d882b8533081299ab263880fb8e19[m
Author: Emmanuel Nnaa <emmanuelnnaa@musoni.eu>
Date:   Mon Feb 10 15:03:55 2014 +0100

    added columntype 'BIT' to return as displaytype 'INTEGER'

[33mcommit cd42134257fdf6165a0309653d2c6eda12336707[m
Author: Madhukar <madhukar@confluxtechnologies.com>
Date:   Mon Feb 10 19:25:38 2014 +0530

    MIFOSX-880 calculate interest from interestChargedFromDate

[33mcommit ac932974f32e3597dee6d11b5d83a71b455e22df[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Feb 10 09:07:10 2014 +0530

    MIFOSX-751 updates

[33mcommit fe510e8e1aa9de1f8efbee80980a2e39a947643e[m
Author: CieyouRaoul <cieyouraoul@musoni.eu>
Date:   Mon Feb 10 04:04:16 2014 +0530

    MIFOSX-751 password reset functionality added by Musoni

[33mcommit 36fcb9c0699945e803fb96e73da02721582877e6[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Feb 9 02:39:13 2014 +0530

    correcting sql patch numbering

[33mcommit f0eea29fb2eb41a99a22dabea8bdf3552af496b4[m
Merge: 9edb9ae f09cf9a
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Feb 9 01:09:50 2014 +0530

    Merge pull request #743 from Musoni/MIFOSX-888
    
    MIFOSX-888 - Early Repayments Strategy

[33mcommit 9edb9ae2197bed102a7d1ae31e4ad6193563fbad[m
Merge: 5004c14 2b27f5b
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Feb 9 00:58:53 2014 +0530

    Merge pull request #735 from lingalamadhukar/mifosx-871
    
    MIFOSX-871 Advanced search query with search operators

[33mcommit 5004c14752365b1255d9c6f80b7f147d85121c0b[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Tue Jan 28 23:17:43 2014 +0530

    added disbursement charges to update summary job

[33mcommit 02191d6280b60338d8c6f9d58ae65ae3896c6d49[m
Merge: 6fccaa8 d46663c
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Feb 9 00:56:05 2014 +0530

    Merge pull request #736 from pramodn02/MIFOSX-870
    
    MIFOSX-870 - added disbursement charges to update summary job

[33mcommit 103beaf56c672065441f6f720e610e9c5129d4cd[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Feb 9 00:53:39 2014 +0530

    correcting names for SQL patch files for MIFOSX-897 and MIFOSX-896

[33mcommit dc3ec32a4325901f8b53b2c8cabb6cb4ed3d2259[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Wed Jan 29 14:23:38 2014 +0530

    MIFOSX-869 - Correct spelling mistakes

[33mcommit 6fed3899c516caa4f7d430c0d16137be7e548964[m
Merge: 84c967b 064ac6d
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Feb 9 00:44:03 2014 +0530

    Merge pull request #747 from emmanuelnnaa/MIFOS-6119
    
    MIFOSX-896 - added permission for 'savingsaccountcharge' endpoint - simple SQL insert

[33mcommit 84c967b6745ac718b79c7dd4dd4c5931d2f153db[m
Merge: 84087a7 29de32a
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sat Feb 8 19:16:59 2014 +0530

    Merge pull request #748 from emmanuelnnaa/MIFOSX-897
    
    MIFOSX-897 - SQL for create journalentry checker permission. Permission missing in m_permission table.

[33mcommit 84087a71b8fb632146dfb426dd828caf182e3776[m
Author: sumit-gupta-sgt <sumit.gupta.sgt@gmail.com>
Date:   Wed Jan 29 16:20:46 2014 +0530

    Fix for Mifosx-814
    
    Mifos-814 Add User for sendPasswordToEmail false

[33mcommit 6fccaa82dadd58db9620c3f8364eb297dc0a1e5a[m
Author: sumit-gupta-sgt <sumit.gupta.sgt@gmail.com>
Date:   Wed Jan 29 16:20:46 2014 +0530

    Fix for Mifosx-814
    
    Mifos-814 Add User for sendPasswordToEmail false

[33mcommit 062ae8905ecae8af56eb90fd7e60d664a2568ae9[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sat Feb 8 16:54:52 2014 +0530

    MifosX-872 optionally capture payment type for a manual journal entry

[33mcommit 133e4e4d9084e41ff9d0eb72d35000768ea6f075[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sat Feb 8 16:54:52 2014 +0530

    MifosX-872 optionally capture payment type for a manual journal entry

[33mcommit 29de32a725dcb173419602fd09aaa18177e92467[m
Author: Emmanuel Nnaa <emmanuelnnaa@musoni.eu>
Date:   Thu Feb 6 12:44:11 2014 +0100

    SQL for create journalentry checker permission. Permission should have been added to m_permission table.

[33mcommit 064ac6dd39151cef9aed8b599d139698c8c83648[m
Author: Emmanuel Nnaa <emmanuelnnaa@musoni.eu>
Date:   Thu Feb 6 09:46:47 2014 +0100

    added permission for 'savingsaccountcharge' endpoint - simple SQL insert

[33mcommit f09cf9a433e3445ea23df909b07e91181575f99d[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Jan 29 03:53:58 2014 +0530

    MIFOSX-888 - Early Repayments Strategy

[33mcommit b78dd9e127ac3c16dca5140689763d89358fe15c[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Jan 29 03:53:58 2014 +0530

    MIFOSX-879 correcting documentation issues

[33mcommit d58d2482ce6dd5019f930b488314316b6c77219f[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Jan 29 03:53:58 2014 +0530

    MIFOSX-879 correcting documentation issues

[33mcommit d46663c630828f4e65a6d8215d2d2261225d4579[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Tue Jan 28 23:17:43 2014 +0530

    added disbursement charges to update summary job

[33mcommit 2b27f5b574b91dd5b7f1b286f42dff0c81e6e6cb[m
Author: Madhukar <madhukar@confluxtechnologies.com>
Date:   Tue Jan 28 20:00:31 2014 +0530

    MIFOSX-871 Advanced search query with search operators

[33mcommit 2e5c9b6eaeef992b313e0ebb03fa23c226a9323b[m
Author: Vishwanath R <vishwanath@confluxtechnologies.com>
Date:   Mon Jan 27 18:01:04 2014 +0530

    [MIFOSX-876] AND [MIFOSX-877] has been fixed and resolved

[33mcommit cb42ff118bb3b835787e2aaffa0cdb245a4d2c0b[m
Author: Vishwanath R <vishwanath@confluxtechnologies.com>
Date:   Mon Jan 27 13:36:14 2014 +0530

    [MIFOSX-875] Unknown Data Integrity Issue has been fixed

[33mcommit 9f08539e595ec13fa1868bbc1b87d5f0f6ad7c2d[m
Author: Vishwanath R <vishwanath@confluxtechnologies.com>
Date:   Mon Jan 27 18:01:04 2014 +0530

    [MIFOSX-876] AND [MIFOSX-877] has been fixed and resolved

[33mcommit f523339ca0c08e07b5ada8dde6b3757737959548[m
Author: Vishwanath R <vishwanath@confluxtechnologies.com>
Date:   Mon Jan 27 13:36:14 2014 +0530

    [MIFOSX-875] Unknown Data Integrity Issue has been fixed

[33mcommit dfab937527ae4029b0ede338386a9217dd03dd71[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Fri Jan 24 23:47:12 2014 +0530

    MIFOSX-827 corrected update loan charge functionality from modify application

[33mcommit 097189621f709ad15f23f14e01aa78022f5e306e[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Fri Jan 24 23:47:12 2014 +0530

    MIFOSX-827 corrected update loan charge functionality from modify application

[33mcommit d365545ec1960e687e9a4a241f8a2a1fc7946e49[m
Author: sumit-gupta-sgt <sumit.gupta.sgt@gmail.com>
Date:   Wed Jan 22 12:36:27 2014 +0530

    Fix for Mifosx-873

[33mcommit 645f2c24aa7537bd8d86d3a17212bff5fd834465[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Mon Jan 20 17:31:39 2014 +0530

    MIFOSX-827 used money in loan charge

[33mcommit 9f9746d4042936681abeeb602f314b30d317e9b9[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Mon Jan 20 02:15:14 2014 +0530

    MIFOSX-868:corrected add loan charge schedule regenerate

[33mcommit 5ae9e5287fdecd004952d5fc6af9a35dd7f8185e[m
Merge: b8b488c a32739c
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Jan 17 06:36:40 2014 -0800

    Merge pull request #722 from pramodn02/MIFOSX-720_243
    
    MIFOSX-720 adding whitespace to query

[33mcommit a32739c1a644691831d38224893a0230434f26dc[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Fri Jan 17 20:01:54 2014 +0530

    MIFOSX-720 adding whitespace to query

[33mcommit b8b488c1bed0ee56b39a2e8b557babf7796fabba[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Jan 17 13:28:50 2014 +0530

    updating release details for 1.7.0 Release

[33mcommit bdbe2d2c51ba0955858b67c2d2b667c3e36caa87[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Jan 16 19:21:32 2014 +0530

    Update CHANGELOG.md
    
    for 1.7.0 Release

[33mcommit 74a058a6b142509a4c11fd524d180e78456c0e93[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Jan 16 12:22:27 2014 +0530

    MIFOSX-866 changes for returing transaction type associated with a journal entry

[33mcommit 5441f29b84cfaa02f55cc6a0b7599144e9bffd77[m
Author: CieyouRaoul <cieyouraoul@musoni.eu>
Date:   Mon Jan 13 17:04:26 2014 +0100

    Added transaction type to journal Entry

[33mcommit 5757cba348b641bd1a57a3989d6dff273471efc0[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Jan 16 10:03:25 2014 +0530

    mifosx-720 update sql patch number

[33mcommit 8b5eab1d87205058659db0e9d2e11950ace6dadd[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Thu Jan 2 13:14:54 2014 +0530

    added status for charges

[33mcommit 45785dff422ee3a842c60b47ea4843385b429f6c[m
Author: Vishwanath R <vishwanath@confluxtechnologies.com>
Date:   Fri Jan 10 23:38:32 2014 +0530

    MIFOSX-861 #comment fixed issues with group/client creation

[33mcommit 4b36019b98503b3ab4fc5617f252932a2471860f[m
Author: goutham-M <goutham@confluxtechnologies.com>
Date:   Wed Jan 1 13:11:23 2014 +0530

    MIFOSX-910 not proper err msg displaying fixed

[33mcommit 31a8a33ce6127ba2d70ffa46202d5ce824e99e88[m
Merge: a08f0db 99c7757
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Dec 31 04:23:51 2013 -0800

    Merge pull request #711 from ashok-conflux/MIFOSX-831-01
    
    Added group by clause for generate collection sheet query

[33mcommit 99c775790a480d966f4c8596acc809f7aaa91ab1[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Tue Dec 31 15:26:50 2013 +0530

    Added group by clause for generate collection sheet query

[33mcommit a08f0db277c1175cc989b457ee4e752f18761524[m
Merge: 0da887d 2276854
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Dec 29 22:52:55 2013 -0800

    Merge pull request #708 from ashok-conflux/IntegrationTestFix
    
    added product shortName to create Loan product test case

[33mcommit 227685402a1143e9c23f81b13d2bef0e27dac066[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Mon Dec 30 11:37:07 2013 +0530

    added product shortName to create Loan product test case

[33mcommit 0da887d11be233ffff4ad4d9b32a988280643b3f[m
Author: Emmanuel Nnaa <emmanuelnnaa@musoni.eu>
Date:   Fri Dec 27 12:54:18 2013 +0100

    added the group external_id scoped field in the search criteria

[33mcommit c8e007478d350b48adab833179d763f7a8358daf[m
Merge: 5e7a259 027b818
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Dec 29 22:47:05 2013 +0530

    merge ashoks changes for collection sheet improvements

[33mcommit 5e7a259052620c15ac03b3cc70ef2a889694b009[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Dec 29 22:37:53 2013 +0530

    renaming sql patch for loan product short name

[33mcommit a86bbc5ebe39a583e6e4c4a242116df0e2b57d15[m
Merge: 341aefa bc55f70
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Dec 29 09:05:36 2013 -0800

    Merge pull request #707 from Vishwa1311/MIFOSX845
    
    SQL patch named "V137__added_is_active_column_in_m_staff.sql" has been added and also I have updated the API-docs for Staff.

[33mcommit 341aefa0807141f494b3aca005b4d903debb42c2[m
Merge: a5e9a32 f32225e
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Dec 29 09:04:59 2013 -0800

    Merge pull request #703 from lingalamadhukar/mifosx-832
    
    Adding short names for loan and saving products

[33mcommit a5e9a32c265d609759d5f36b4a41edc18710d94c[m
Merge: 570296d f32ca69
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Dec 29 22:06:21 2013 +0530

    Merge branch 'master' into develop

[33mcommit f32ca69a377af855be51b3bb58984167959ea7e9[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Dec 29 21:44:35 2013 +0530

    update gradle.properties for 1.16.1 release

[33mcommit d0e5677b4d6def455b7db22be62ec962b24c83fa[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Dec 29 21:43:29 2013 +0530

    Update CHANGELOG.md
    
    for 1.16.1 release

[33mcommit bc55f7030141b944ae0cecee92eea11db48b667e[m
Author: Vishwanath R <vishwanath@confluxtechnologies.com>
Date:   Sat Dec 28 18:27:13 2013 +0530

    initial commit

[33mcommit 37a039454a8e18544d0b67407532c3580d35d510[m
Author: Vishwanath R <vishwanath@confluxtechnologies.com>
Date:   Fri Dec 27 18:44:52 2013 +0530

    Added new column named is_active

[33mcommit 027b818f27f46dc04b0a1d1f5e8c65c58569dba2[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Mon Dec 23 18:39:59 2013 +0530

    Updated collection sheet changes raised in GK pilot

[33mcommit f32225e2758197c48ffc2242fbdab90c6c1268f9[m
Author: Madhukar <madhukar@confluxtechnologies.com>
Date:   Tue Dec 24 17:38:24 2013 +0530

    Adding short names for loan and saving products

[33mcommit 570296d10a92dfe306ed79703f2d6378a30ba5d9[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Dec 22 02:12:18 2013 +0530

    renaming sql patch

[33mcommit d885e9b28a69892493f5ff6bc6a0b09038853d67[m
Author: Madhukar <madhukar@confluxtechnologies.com>
Date:   Fri Dec 20 13:14:24 2013 +0530

    mifosx-834 update report parameter query

[33mcommit 6cb756bab194a7828961ffac7e768e7fe0021b1f[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Dec 22 02:12:18 2013 +0530

    renaming sql patch

[33mcommit 99340eca7475fa16421edfe8f466980813968d4d[m
Author: Madhukar <madhukar@confluxtechnologies.com>
Date:   Fri Dec 20 13:14:24 2013 +0530

    mifosx-834 update report parameter query

[33mcommit fe3632818498dd2b13e4782da0701aeb60bcdbfd[m
Merge: e91ee2d 9d47f49
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Dec 17 06:54:09 2013 -0800

    Merge pull request #698 from ashok-conflux/MIFOSX-812
    
    add currency details to journal entries api

[33mcommit e91ee2d1d13189c22a61476a0759423406643943[m
Merge: a354b3e 14a7e6a
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Dec 17 06:53:34 2013 -0800

    Merge pull request #699 from ashok-conflux/MIFOSX-830
    
    returning valid commandId for api actions when maker checker is enabled

[33mcommit 14a7e6a067bad38d7068c85072fc43da9e1e84eb[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Tue Dec 17 13:35:34 2013 +0530

    returning valid commandId for api actions when maker checker is enabled

[33mcommit 9d47f49e116d41c966d3ca59986edadf5c46e186[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Tue Dec 17 11:03:48 2013 +0530

    add currency details to journal entries api

[33mcommit a354b3e66487fd0550a8d6f34548bbff1a9bd292[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Fri Dec 13 18:34:49 2013 +0530

    added validation around Loan life cycle events like, repayment, disbursement etc..

[33mcommit 49a2f30e2cefe8bf6292b6e5ca1d486f26e37de9[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Mon Dec 16 16:39:17 2013 +0530

    added timeline details to Center

[33mcommit fb45948c0dc49ed5e6fac044a1badcd1d53c2aad[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Dec 16 02:45:32 2013 +0530

    Update CHANGELOG.md
    
    for 1.16 release

[33mcommit b2d8c517e3d8f9ce8e76617f24ceb9c4e5d4474c[m
Merge: fa293d1 7e9e3f4
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Dec 16 02:35:36 2013 +0530

    merge master into develop

[33mcommit 7e9e3f48575976af96e8820214582479b30445ed[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Dec 16 02:18:47 2013 +0530

    updates for 1.15.2 release

[33mcommit fa293d19ce67a49d7f4c8420f63f1d4d453b005e[m
Merge: a9f0147 5373263
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Dec 12 22:58:37 2013 -0800

    Merge pull request #694 from ashok-conflux/MIFOSX-824
    
    Added pagination support to audit API

[33mcommit 5373263f6ceef17d0130efa8093b1a70e3defe54[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Thu Dec 12 18:28:36 2013 +0530

    Add pagination support to audit API

[33mcommit 66f974720f4df32acc221378d2a3d23336986e5b[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Dec 12 15:31:12 2013 +0530

    adding instructions for deploying the community app

[33mcommit 3789ac1f0722159c922b8a65518c4115c63bc913[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Dec 12 15:11:47 2013 +0530

    Update CHANGELOG.md

[33mcommit a9f01477e0863f230a53004cc0d94d8cf4f84839[m
Merge: e7f0e60 1371a3f
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Dec 12 01:05:15 2013 -0800

    Merge pull request #693 from ashok-conflux/MIFOSX-826
    
    added holiday status to response API data

[33mcommit 1371a3f7755d05bc042c1b50d4d34c0a760b908e[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Wed Dec 11 16:59:05 2013 +0530

    added holiday status to response

[33mcommit e7f0e60555f16177017f3f9b0c704e1b83f0a79c[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Dec 11 06:42:46 2013 +0530

    fix for MIFOSX-822

[33mcommit 5c30edc7105698e16008f51edbdf57141419f505[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Dec 11 06:42:46 2013 +0530

    fix for MIFOSX-822

[33mcommit 35b489b79329cb37155e0824d2e0d2540704158a[m
Merge: 68089e0 8e6ca1f
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Dec 9 03:21:02 2013 -0800

    Merge pull request #692 from gsluthra/gitignore
    
    Added .gradletasknamecache to gitignore list

[33mcommit 8e6ca1f6b5557234da1d7bc18bacd18ba42c4d20[m
Author: Gurpreet Luthra <gsluthra@gmail.com>
Date:   Mon Dec 9 16:46:47 2013 +0530

    Added .gradletasknamecache to gitignore list

[33mcommit 68089e036057a6cbd0e011910dedced7f88e5222[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Dec 6 18:41:59 2013 +0530

    fix for MIFOSX-821

[33mcommit cebdc5cfc1463b1f65cf014842e3595c75458bfe[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Dec 6 18:41:59 2013 +0530

    fix for MIFOSX-821

[33mcommit 74606b424d0f043a4bb533fb4f85a206a4329ea7[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Dec 6 18:03:55 2013 +0530

    fix for MIFOSX-818

[33mcommit 5aa6fecd5cacbcb42b22985ca356d5655dc6540d[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Dec 6 18:03:55 2013 +0530

    fix for MIFOSX-818

[33mcommit 584f0fd99fb03cf902adb1f91c06903a9c65dada[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Dec 5 21:04:23 2013 +0530

    fix for MIFOSX-820

[33mcommit 3fa2c26897e2e2b21ab6f1d28bbc99c8adee219c[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Dec 5 21:04:23 2013 +0530

    fix for MIFOSX-820

[33mcommit 8ffd7c831b2633001be64e8839ba07c5e9087e7b[m
Merge: 865ce43 f991075
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Dec 5 02:05:50 2013 -0800

    Merge pull request #690 from pramodn02/MIFOSX-816
    
    mifosx-816 : corrected loan variations to pick based on loan product cou...

[33mcommit 5fa4423ba54ab9da0498608f80ca965082ca6c1e[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Dec 4 18:02:33 2013 +0000

    update changelog for 1.15.1 release

[33mcommit f991075cb7db556c33d5e1f23210bca83c66d8e5[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Wed Dec 4 23:39:25 2013 +0530

    mifosx-816 : corrected loan variations to pick based on loan product count

[33mcommit 865ce43de08955f804bc3f95bff693ac0f7aea7f[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Dec 4 18:02:33 2013 +0000

    update changelog for 1.15.1 release

[33mcommit 93a39abf75947e671bc92140e1d1f2d10c6e8ed5[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Dec 3 15:07:56 2013 +0000

    MIFOSX-811: ensure undo disbursement regenerated schedule when expected and actual disbursement dates are different.

[33mcommit 046b9aaf2dc83e8fd297ede8098cd4e3b888abbb[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Dec 3 15:07:56 2013 +0000

    MIFOSX-811: ensure undo disbursement regenerated schedule when expected and actual disbursement dates are different.

[33mcommit fa7a917f269ae13d1e9cdc2ea1b4bd69f24b31bf[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Tue Dec 3 19:01:26 2013 +0530

    mifosx-801:submitted date updation for clients

[33mcommit 2c74e0cdfe86ef1b3641e2156d779a54b95b3805[m
Merge: 8ffba8a c11e8d5
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Dec 3 13:41:15 2013 +0000

    Merge remote-tracking branch 'pramod/MIFOSX-801_1' into mifosplatform-1.15.1

[33mcommit c11e8d562737c25c76435e42e2edde34d33df38b[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Tue Dec 3 19:01:26 2013 +0530

    mifosx-801:submitted date updation for clients

[33mcommit 9839258135c90158ab88a6fcc61676bb2feeaa57[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Dec 3 13:25:13 2013 +0000

    MIFSOX-810: exception message returned rather than clean error message when attempt to add user that already exists.

[33mcommit 8ffba8a732b4bcac7b2ce1b7845603e92c6a822f[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Dec 3 13:25:13 2013 +0000

    MIFSOX-810: exception message returned rather than clean error message when attempt to add user that already exists.

[33mcommit d86dfe4e667259926f4c335dc5e87cf2043eddb0[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Tue Dec 3 15:04:32 2013 +0530

    MIFOS-805:Corrected update of loan product for term variation for loan cycle

[33mcommit f854e580502ca90739738711d36d9eea48505c1a[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Tue Dec 3 15:04:32 2013 +0530

    MIFOS-805:Corrected update of loan product for term variation for loan cycle

[33mcommit 96cefdddc55c7430a74ff4e959644b1ba0591b8e[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Mon Dec 2 22:08:15 2013 +0530

    added running balance as optional to GLAccount

[33mcommit ab1254ab680bb2e88ed876f098f385999778d425[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Dec 3 01:07:13 2013 +0000

    update properties for release

[33mcommit ab718150aba71fcebe9300e98783455432f233cb[m
Merge: 71803b2 fb3d5c9
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Dec 2 16:55:53 2013 -0800

    Merge pull request #686 from pramodn02/MIFOSX-744
    
    mifosx-744:added running balance as optional to GLAccount

[33mcommit fb3d5c9a0c43bc1db2d4c554ed80568ee27a554d[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Mon Dec 2 22:08:15 2013 +0530

    added running balance as optional to GLAccount

[33mcommit 71803b20f52b3001075c2368ef5a8e38f21934a4[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Mon Dec 2 18:10:34 2013 +0530

    added API changes

[33mcommit eb75708b669a4e0940df31b415879f3c68bc774f[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Mon Dec 2 17:58:40 2013 +0530

    added undo writeoff transaction

[33mcommit 40ec10ef11a4bc1ce0aa0c856ffbf6d0049addc1[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sun Dec 1 14:01:16 2013 +0000

    update release properties for develop branch.

[33mcommit 3b660c197970264a5986dd7478ced3f2b4dad73b[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sun Dec 1 13:54:59 2013 +0000

    update changelog for 1.15.0 release

[33mcommit 5149668ab3af3e4b7ef8326420b4edd23203540e[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sun Dec 1 13:48:36 2013 +0000

    update release properties for 1.15.0

[33mcommit 8bbd028c6a3effb235b9f0003bf101429a85e477[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Nov 29 13:59:25 2013 +0000

    MIFOSX-792: slight change to where call for configurable property happens.

[33mcommit bddf12087822d6457cfa42da578f015afe83c17a[m
Merge: 6df82f8 7f5db0f
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Nov 29 13:46:15 2013 +0000

    Merge remote-tracking branch 'musoni/MIFOSX-792' into develop

[33mcommit 6df82f820a2f914d31aa6b135c5a24d22f6c8044[m
Merge: ef628d0 9ffbef4
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Nov 29 05:30:13 2013 -0800

    Merge pull request #683 from pramodn02/MIFOSX-569
    
    MIFOSX-569 - Corrected validation for borrower cycle

[33mcommit 9ffbef4994d55fad3c4d3adbe3292e5b069dc803[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Fri Nov 29 18:49:39 2013 +0530

    added validation for borrower cycle to start with equal

[33mcommit fe0a519247b0047766050ada0e65e77a22fb4ff7[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Fri Nov 29 18:32:35 2013 +0530

    Corrected validation for borrower cycle

[33mcommit 7f5db0ff00450ad3ecae5b46c7d46874d7bc4239[m
Author: andrew-dzak <andrewdzakpasu@musoni.eu>
Date:   Fri Nov 29 12:35:46 2013 +0100

    added configuration value for wait penalty period of 2 days

[33mcommit ef628d09977998c7360c5c9637088d882f875928[m
Merge: 797c44e 1099e73
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Nov 29 01:49:59 2013 -0800

    Merge pull request #682 from pramodn02/MIFOSX-800
    
    MIFOSX-800 - added empty data check

[33mcommit 797c44e48cde524a655dd534bd0175a7af0dad23[m
Merge: 9bcfea0 c2b42df
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Nov 29 01:49:42 2013 -0800

    Merge pull request #681 from pramodn02/MIFOSX-795
    
    MIFOSX-795 : Products accounting API clean up

[33mcommit 1099e730396ee2e2dcbf33c7bc7e8d9d4ae58ee6[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Fri Nov 29 15:11:34 2013 +0530

    added empty data check

[33mcommit c2b42df3ab74a3f865813f6893b55d51674a8fc0[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Fri Nov 29 13:33:37 2013 +0530

    savings product account detail API clean up

[33mcommit f587d8372275606baae867c8cda537cd4b0699bf[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Thu Nov 28 23:09:51 2013 +0530

    Products accounting API clean up

[33mcommit 9bcfea08d0e188a06412732d7e85d7326fb0b0bb[m
Merge: bd7bdb2 499124b
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Nov 27 02:47:53 2013 -0800

    Merge pull request #680 from pramodn02/MIFOSX-790
    
    linking payment details to journal entries

[33mcommit 499124b2732da8349271c4b6673c566339972038[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Wed Nov 27 15:17:57 2013 +0530

    linking payment details to journal entries

[33mcommit bd7bdb2e576e17b3cc2b00c23a96c2f56e317016[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Nov 27 09:47:49 2013 +0000

    remove test for group delete in GroupTest file as it consistently provides false positive.

[33mcommit 7d4284261c93347c60da9eb19b5d8ca2ae890e5c[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Nov 26 01:30:20 2013 +0000

    update to latest bug release of spring framework.

[33mcommit 100e096f643955ae1f91ca14e543e9a96bed1697[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Nov 25 14:37:42 2013 +0000

    remove eclipse warnings.

[33mcommit 37c04828ddf36b5925aef9deaab4cb553e197ba0[m
Merge: baec784 3832c8c
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Nov 24 22:06:43 2013 -0800

    Merge pull request #679 from pramodn02/MIFOSX-569
    
    added borrower cycle to loan products

[33mcommit baec784db46235d7b1c5317456ad7dec57e7d90e[m
Merge: 35652b1 32a7323
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Nov 24 22:05:17 2013 -0800

    Merge pull request #677 from ashok-conflux/holidays-api-update
    
    updated holidays api doc

[33mcommit 3832c8c2c4d128e37e803de51c3f497e83caaf33[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Mon Nov 25 11:33:46 2013 +0530

    added check for blank feilds

[33mcommit f8da9b4072870451d40368f810e19d9873a2a283[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Mon Nov 25 11:28:33 2013 +0530

    added check for blank feilds

[33mcommit a7632d4fae4d5479cf39a38020a056be9f351805[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Sun Nov 24 12:34:20 2013 +0530

    added borrower cycle to loan products

[33mcommit 35652b1abd80768eecfc31c15a223f18ac040c64[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Nov 22 15:39:10 2013 +0000

    MIFOSX-794: some changes to terms - userRoles not clientRoles.

[33mcommit e64b502b17daf9c9c47c5f70b5d8d06914480d46[m
Author: andrew-dzak <andrewdzakpasu@musoni.eu>
Date:   Fri Nov 22 15:00:54 2013 +0100

    add roles to list users

[33mcommit 23b250bd4045af4754571bf2cf753a559baebeb0[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Nov 22 11:45:15 2013 +0000

    remove unused warnings.

[33mcommit 3fe1f7f058cec9b88a21d2079deb20b2bf127506[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Nov 22 11:30:55 2013 +0000

    update release properties for develop branch.

[33mcommit 3b9be1fa1c22ab356997d023cd4b7418ddc907da[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Nov 22 11:21:00 2013 +0000

    MIFOSX-790: remove warnings.

[33mcommit e87daef59a6b4c8565502e6df13150a22ffc93b4[m
Merge: f4cb810 c337e34
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Nov 22 11:06:14 2013 +0000

    Merge remote-tracking branch 'musoni/MIFOSX-790' into develop

[33mcommit 32a7323cbb222335e323a1c06828f4310b59cbbd[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Fri Nov 22 15:12:21 2013 +0530

    updated holidays api doc

[33mcommit c337e34e4ce3270780d6a0ad3ac2a7f59fc46f21[m
Author: CieyouRaoul <cieyouraoul@musoni.eu>
Date:   Fri Nov 22 09:08:17 2013 +0100

    Added payment details to journalEntry

[33mcommit f4cb8101e89221e7d32812a3276da28872145c1b[m
Merge: 8539541 7bd6ef0
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Nov 21 23:53:13 2013 +0530

    merge 1.14.0 release into master

[33mcommit 7bd6ef0b03c7e7f2a24a7ad39b387427e83e6271[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Nov 21 23:27:51 2013 +0530

    Update CHANGELOG.md
    
    for 1.10.0 RELEASE

[33mcommit 003c21bac06ed2a67bdd643d3b7ff387831b45a2[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Nov 21 23:22:10 2013 +0530

    update to 1.14.0 RELEASE

[33mcommit 779f40e5677c7d3f25d43454c84562b4151c57bd[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Nov 21 10:16:55 2013 +0000

    MIFOSX-794: remove call to service that fetches roles for now.

[33mcommit 9976a0d1488d4b3f6bed6339f4c24237e0d62e16[m
Merge: 7a12a73 21733c1
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Nov 21 02:04:45 2013 -0800

    Merge pull request #675 from Musoni/MIFOSX-794
    
    added roles in the api call for users

[33mcommit 21733c1472d1f0c912149aa209a1fcd2514bc337[m
Author: andrew-dzak <andrewdzakpasu@musoni.eu>
Date:   Wed Nov 20 15:27:13 2013 +0100

    added roles in the api call for users

[33mcommit 7a12a73f788abd47623af9e8d8037dac6f0263da[m
Merge: 9ff10ed f5c91ee
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Nov 19 01:24:44 2013 -0800

    Merge pull request #672 from pramodn02/MIFOSX-787
    
    enabled percentage of interrest for loans

[33mcommit f5c91ee28ec49f97c3402bbfe01d07683a7e22cb[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Tue Nov 19 01:20:02 2013 +0530

    enabled percentage of interrest for loans

[33mcommit 9ff10edc80a102305815f33b701ac3f9480579d5[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Nov 18 17:40:58 2013 +0000

    first draft of system architecture documentation.

[33mcommit ada127fbdf3074741803ef1708ea0bb7e4d74dc8[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Nov 18 08:25:46 2013 +0000

    fix failing integrated test around groups.

[33mcommit 4975c1c18f6c743a791d5bf16ab15e158564340a[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Nov 15 15:15:57 2013 +0000

    Fix group test that uses hard coded date for activation date when no date is passed. submitted on date now also must be catered for.

[33mcommit 291fcd0431163e018380c884c17e3a5e3dd9f30a[m
Merge: 6232e86 e9aaab3
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Nov 15 12:25:38 2013 +0000

    MIFOSX-765: update validation for timeline fields on client and group.

[33mcommit 6232e863ed5f62de0420e1dca32ecd9ff72a46b1[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Nov 15 11:42:52 2013 +0000

    MIFOSX-746: musoni specific approach to auto penalties on late installments.

[33mcommit b43a9472e3b1ccf621f68c89307f1d4ee68daa99[m
Author: andrew-dzak <andrewdzakpasu@musoni.eu>
Date:   Wed Nov 13 17:04:07 2013 +0100

    mifosx-746 fixes

[33mcommit 25bc2f4553d2bf9b7927fdada814412156ec0f92[m
Merge: 244ebfb 1f7c162
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Nov 15 03:06:36 2013 -0800

    Merge pull request #671 from ashok-conflux/MIFOSX-693
    
    added support to edit/delete holiday

[33mcommit 1f7c162244bda24243f7ea6981c6b40d8fd197fe[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Fri Nov 8 11:48:46 2013 +0530

    added support to edit/delete holiday

[33mcommit 244ebfb82298e4ac10c3c4b8707c277b37d7b7b4[m
Merge: cc19a06 8621542
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Nov 14 09:57:59 2013 -0800

    Merge pull request #663 from ashok-conflux/MIFOSX-637
    
    added filter to display only active clients in collection sheet

[33mcommit cc19a06d4d83696337277900684d60547bd23db8[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Nov 14 17:56:18 2013 +0000

    update version number of sql patch for calendar history.

[33mcommit 0fcb7ca8d42d03fbeee146b8932be0d66019d20a[m
Merge: dcbc64e 2d0f714
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Nov 14 09:54:28 2013 -0800

    Merge pull request #661 from ashok-conflux/MIFOSX-649
    
    added support for capturing calendar history data and generate meeting dates accordingly

[33mcommit e9aaab3f56bc189eb96e5bdd14d33624fa4988dc[m
Author: CieyouRaoul <cieyouraoul@musoni.eu>
Date:   Thu Nov 14 17:55:23 2013 +0100

    #MIFOSX-765 fixes validation submittedOnDate

[33mcommit dcbc64e30e26a4b01cc7b952beb819c54847f984[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Nov 14 13:28:20 2013 +0000

    MIFOSX-784: ensure validation is called on create and activate scenarios.

[33mcommit 61bb5c130239bfd0791928485fd33b1d1e15448d[m
Merge: 4fccb6b 4f5db16
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Nov 14 04:43:08 2013 -0800

    Merge pull request #665 from ashok-conflux/MIFOSX-783
    
    display parent offices staff for creating a center

[33mcommit 4fccb6b3adea125fb3e5add282e2de75e7f20b6f[m
Merge: 0451a99 b31b646
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Nov 14 04:30:55 2013 -0800

    Merge pull request #664 from ashok-conflux/MIFOSX-784
    
    at the time of creating client do not allow activate with future date

[33mcommit 0451a99c1916cbe8657ca3c49df90f2483c68f18[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Nov 14 12:22:04 2013 +0000

    MIFOSX-765: ensure activated by is captured at creation if also activated at same time.

[33mcommit 83fc32b0af7ccca268b9ca433fedf8eb5c68935e[m
Merge: 6fd4ee8 5da1221
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Nov 14 03:51:50 2013 -0800

    Merge pull request #666 from Musoni/MIFOSX-765
    
    MIFOSX-765 - Client and Group timeline Implementation

[33mcommit 6fd4ee86343ca93a27bc78aed5a7164081ce08e6[m
Merge: 40882a0 9bfd46f
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Nov 14 03:21:27 2013 -0800

    Merge pull request #667 from pramodn02/MIFOSX-730
    
    handled null pointer exceptoin when there is no charges

[33mcommit 9bfd46ffb4fb1dc0c32b05b8032234c04feb3ea5[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Thu Nov 14 10:36:00 2013 +0530

    handled null pointer exceptoin when there is no charges

[33mcommit 5da12211593b7978b42ce10eeeb92340fa8722b5[m
Author: CieyouRaoul <cieyouraoul@musoni.eu>
Date:   Wed Nov 13 17:04:11 2013 +0100

    Client and Group timeline Implementation

[33mcommit 40882a002ba7739ea943e79b601ec1d4fca83933[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Nov 13 10:34:10 2013 +0000

    remove unused warnings, replace toDateMidnight with toDateTimeAtStartOfDay method as recommended by joda time lib.

[33mcommit 4f5db16722483417e787879afc2b2b7c5afd7537[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Wed Nov 13 15:56:54 2013 +0530

    display parent offices staff for creating a center

[33mcommit b31b646d59b6b6f8104681dedf6dfa391542646b[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Wed Nov 13 15:23:52 2013 +0530

    at the time of creating client do not allow activate with future date

[33mcommit 8621542e08459dc0c1c15ec01a88c8cc55e4ce41[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Wed Nov 13 14:30:57 2013 +0530

    added filter to display only active clients in collection sheet

[33mcommit aa0df6a0c318120d068e987bc605e8d65e21439b[m
Merge: 3e1c3eb d15b40b
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Nov 12 23:35:00 2013 -0800

    Merge pull request #662 from pramodn02/MIFOSX-730
    
    Mifosx 730

[33mcommit d15b40bf331f58ea9d14710020446fcae819fec8[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Wed Nov 13 11:30:20 2013 +0530

    waive and payment of installment charge corrections

[33mcommit 51d8083dc5de4ed30eaba43a0c7203818c158b12[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Tue Nov 12 11:55:40 2013 +0530

    added waive charge for installment charge

[33mcommit 2d0f71447bc8fe5ab39c276ebb8a5072547d9b93[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Sat Nov 9 12:11:49 2013 +0530

    added support for capturing calendar history data and generate meeting dates accordingly

[33mcommit 3e1c3eb4c67a763bfd4bd67bbc6ccdad63e856e9[m
Merge: 7c41a42 58ee4c4
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Nov 12 05:56:25 2013 -0800

    Merge pull request #658 from vorburger/apps_new_community_app
    
    apps/README updated to point to community-app

[33mcommit 58ee4c4ff815be3b71fc97ee5c48e5fc2aa5b61d[m
Author: Michael Vorburger <mike@vorburger.ch>
Date:   Tue Nov 12 02:42:48 2013 +0100

    apps/README updated to point to community-app

[33mcommit 7c41a429e2b9ecabfd4af62bc8bb7706075ebe63[m
Merge: f15a5ae 6fc6568
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Nov 8 01:56:16 2013 -0800

    Merge pull request #657 from ashok-conflux/MIFOSX-769
    
    added min and max constraint validation during preview of repayment schedule in new/edit loan application

[33mcommit 6fc656894dd80f7988ed5e84a1530ccf57e4232f[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Fri Nov 8 12:40:53 2013 +0530

    added min and max constraint validation during preview of repayment schedule in new/edit loan application

[33mcommit f15a5ae2455ee28e87e3aca1469c233a7476d031[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Nov 7 14:11:33 2013 +0000

    MIFOSX-754: add mobile no field to client and staff tables which can be used to by mobile money or SMS functions.

[33mcommit 8c38fcb0b559ee61f6185f5d9bc3fb8db39dfca1[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Nov 7 13:26:22 2013 +0000

    MIFOSX-781: fix issue with repayment schedule not picking up on latest charge been added.

[33mcommit 87554fe1a1e07dac216f14bccfdb314acc3aeec7[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Nov 6 18:09:12 2013 +0000

    MIFOSX-119: basic CRUD outline for sms outbound messages for platform. no specific attempt at using third party lib or service to actually send an SMS included here.

[33mcommit 43343e28b3fc0adc6953e6e505797d2370c99d1b[m
Merge: 7535371 3b2e1bc
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Nov 6 03:23:51 2013 -0800

    Merge pull request #656 from pramodn02/MIFOSX-730
    
    Mifosx 730

[33mcommit 3b2e1bcf91e35218f916817e7ddd39175499de39[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Wed Nov 6 15:50:59 2013 +0530

    corrected amountOrPercentage value for new loans in loan charge data

[33mcommit 906beef7df85cfa49ef2f7aa60d09fd64ec9c4c7[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Wed Nov 6 15:33:11 2013 +0530

    added loan charge update for installment fees

[33mcommit 75353716a9034bbfa6fd7f9f17eb03b9a68c6542[m
Merge: d24e076 2384f3a
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Nov 5 02:09:48 2013 -0800

    Merge pull request #655 from pramodn02/MIFOSX-730
    
     fix for percentage based approach

[33mcommit 2384f3aef89a3a5a7734b333989bcdc13ca026e0[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Tue Nov 5 13:57:13 2013 +0530

    added validation for installment charge update

[33mcommit a54931bfcd06c5124cfaf1f9721929f548dee767[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Mon Nov 4 22:08:36 2013 +0530

     fix for percentage based approach

[33mcommit d24e076f051ff7af040b2a5d8397b60e431535d6[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Nov 4 00:24:33 2013 +0000

    add installment fee type to list of valid types for loans but not savings.

[33mcommit bd512066fc5d80e48b28b275ce65d5e27db67b36[m
Merge: 21ed1c8 6f2e5c8
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sun Nov 3 16:04:38 2013 -0800

    Merge pull request #653 from pramodn02/MIFOSX-730
    
    Mifosx 730

[33mcommit 21ed1c89a049df902e972b099d095aef74e89eb5[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sun Nov 3 02:45:50 2013 +0000

    update changelog for 1.13.4 bug release

[33mcommit 85395416b327c0db70c6b3f1e263082ff541ab9c[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sun Nov 3 02:45:50 2013 +0000

    update changelog for 1.13.4 bug release

[33mcommit 6dc59fac76c5de2d0028fe94a2494f440eaabad6[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Thu Oct 31 22:37:56 2013 +0530

    added reprocess of schedule for add and remove of loan charge

[33mcommit 38caeae650a057ca44291669f2b753ae08c368e9[m
Merge: 18da8ad 1529370
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sat Nov 2 19:16:10 2013 -0700

    Merge pull request #654 from pramodn02/MIFOSX-766
    
    added reprocess of schedule for add and remove of loan charge

[33mcommit 152937042aeeaf287b61b3bb8aad80bbdb945ffd[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Thu Oct 31 22:37:56 2013 +0530

    added reprocess of schedule for add and remove of loan charge

[33mcommit 6f2e5c8e02d2c5cd24cd396fbc4b4e3b07d15d39[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Thu Oct 31 21:48:41 2013 +0530

    added installment fee for add charges

[33mcommit f2595b2043e729944fea15e467ac0f88954b3d7e[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Fri Oct 18 15:07:41 2013 +0530

    added installment fee for loans version 1

[33mcommit a8752cb70abfabdb020cbfedec8ad2abba7ae46c[m
Author: Sander van der Heyden <sander@musoni.eu>
Date:   Thu Oct 31 16:55:23 2013 +0100

    MIFOSX-764 - Fixed default ordering of journal entries to make sure running balances add up

[33mcommit 18da8ade84e79f651662741b743926e069a9b07e[m
Merge: 1ce6514 ff2aae4
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Oct 31 09:03:46 2013 -0700

    Merge pull request #652 from Musoni/MIFOSX-764
    
    MIFOSX-764

[33mcommit ff2aae4a161ff3174364862452aa40945030d0e8[m
Author: Sander van der Heyden <sander@musoni.eu>
Date:   Thu Oct 31 16:55:23 2013 +0100

    MIFOSX-764 - Fixed default ordering of journal entries to make sure running balances add up

[33mcommit 1ce65144dae05e2b8d488feef3ce98fab7575014[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Oct 31 11:48:47 2013 +0000

    move flyway version back to see if it is issue with flyway on cloudbees server.

[33mcommit 983378ac5977d64adb7e05ea9ab7573a338bce66[m
Author: Sander van der Heyden <sander@musoni.eu>
Date:   Thu Oct 31 12:15:07 2013 +0100

    MIFOSX-763 - Fixed issue with running balances being 0 and inability to retrieve GLs without transactions

[33mcommit 7d12e5ec05d029e388c177498d2e6d1dbfcb854e[m
Merge: 2a8f91f 95100a8
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Oct 31 04:21:06 2013 -0700

    Merge pull request #651 from Musoni/MIFOSX-763
    
    MIFOSX-763

[33mcommit 95100a88125122889dbbd302f9a3b34d676a705f[m
Author: Sander van der Heyden <sander@musoni.eu>
Date:   Thu Oct 31 12:15:07 2013 +0100

    MIFOSX-763 - Fixed issue with running balances being 0 and inability to retrieve GLs without transactions

[33mcommit b1fd110c10f9cafa2ad170b9af9afdad889b5323[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Oct 31 10:49:55 2013 +0000

    update release properties

[33mcommit 2a8f91f05228090a785a6dc1df06ee578d55f766[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Oct 30 23:46:39 2013 +0000

    update changelog for 1.13.3 bug release

[33mcommit ffd332e975f298c3d66890b6a280be8804b1ccc7[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Oct 30 23:46:39 2013 +0000

    update changelog for 1.13.3 bug release

[33mcommit 4682b3f877288d04c92113a2a24cd90b41634864[m
Merge: 86f8a16 806ef04
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Oct 30 06:43:55 2013 -0700

    Merge pull request #650 from ashok-conflux/MIFOSX-682
    
    Added validation for meeting frequency, repeats on day and start date for Calendar

[33mcommit 806ef048fea2588db6fbde023c58f681a9c96d54[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Wed Oct 30 18:45:42 2013 +0530

    Added validation for meeting frequency, weekdays and start date for Calendar

[33mcommit 84674a2b263b2e563c44877f367a80d6cb0735a6[m
Author: Sander van der Heyden <sander@musoni.eu>
Date:   Wed Oct 30 11:00:01 2013 +0100

    MIFOS-762 - Fixed SubmittedBy userID for creating new savings

[33mcommit 86f8a16ed1fd5548696beeaee4935cd5adeb93e4[m
Merge: 6b4687e 4733530
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Oct 30 03:20:42 2013 -0700

    Merge pull request #649 from Musoni/MIFOSX-762
    
    MIFOS-762 - Fixed SubmittedBy userID for creating new savings

[33mcommit 4733530f653ad82047dd700b290140f5e14114be[m
Author: Sander van der Heyden <sander@musoni.eu>
Date:   Wed Oct 30 11:00:01 2013 +0100

    MIFOS-762 - Fixed SubmittedBy userID for creating new savings

[33mcommit 6b4687e136d3cd2e6d38c23a18a9644d3b1ff2b8[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Oct 29 16:52:55 2013 +0000

    update platform dependency versions.

[33mcommit 46a4ed7b88eac683af82283d1b9d840a00970a16[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Oct 29 15:39:22 2013 +0000

    MIFOSX-760: ensure charges cache is evicted on update.

[33mcommit ec3b97349da22949ac4e627de2dbd8da62a10a50[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Oct 29 16:17:30 2013 +0000

    switch back to 1.6 compile settings for time being.

[33mcommit ddc77a743c238f74743c624c5d4da67e7a982c00[m
Merge: 3a381ac b766297
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Oct 29 09:15:01 2013 -0700

    Merge pull request #642 from ugupta/develop
    
    Updating to use JDK 7 and created gradle wrapper

[33mcommit d7bcf02b248783c9d89204a0a57c8d2849780abf[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Oct 29 15:39:22 2013 +0000

    MIFOSX-760: ensure charges cache is evicted on update.

[33mcommit b885296a805c50a1e5905248725beff244ce6831[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Tue Oct 29 17:59:08 2013 +0530

    allow to pay all pending arrears recurring charges through scheduled job

[33mcommit 3a381acdf9d64138a16d8140f9f4d2e4faa6fce2[m
Merge: 712690f c1353e3
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Oct 29 07:24:05 2013 -0700

    Merge pull request #648 from ashok-conflux/MIFOSX-741
    
    allow to pay all pending arrears recurring charges through scheduled job

[33mcommit c1353e3001748d53263b16f9199e49d47471719c[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Tue Oct 29 17:59:08 2013 +0530

    allow to pay all pending arrears recurring charges through scheduled job

[33mcommit 4724fa12f798a6482569cf903495daf4d2d20d89[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Tue Oct 29 17:07:59 2013 +0530

    updated montly recurring charge start dateto be after savings account activagtion date

[33mcommit 712690f2f5ab821fa07c931770f882b93b2da64e[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Oct 29 12:16:42 2013 +0000

    MIFOSX-757: fix issue with vaidation on chargeTimeType

[33mcommit e1c198fdb9ed014e8fee750e177b3acef34993f3[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Oct 29 12:16:42 2013 +0000

    MIFOSX-757: fix issue with vaidation on chargeTimeType

[33mcommit 0b5d908b2cd2b93b6d18e15690d524455c7a929a[m
Merge: 150fb57 2d9ee2e
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Oct 29 05:15:25 2013 -0700

    Merge pull request #647 from ashok-conflux/MIFOSX-740
    
    updated montly recurring charge start date to be after savings account activation date.

[33mcommit 2d9ee2eadfcfc4fd3f54b334e10c09380ad9af26[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Tue Oct 29 17:07:59 2013 +0530

    updated montly recurring charge start dateto be after savings account activagtion date

[33mcommit 69780c006135756a2b3738e02dea6f489d7f82f8[m
Author: Sander van der Heyden <sander@musoni.eu>
Date:   Tue Oct 29 11:35:25 2013 +0100

    MIFOSX-759 - Hotfix for non-unique transaction IDs

[33mcommit 150fb578c3df0f5df1e45bdbca3436c3966c1d95[m
Merge: 58b69c3 22cd8ef
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Oct 29 03:50:15 2013 -0700

    Merge pull request #646 from Musoni/MIFOSX-759
    
    MIFOSX-759 - Hotfix for non-unique transaction IDs

[33mcommit 22cd8ef123aae6ecac199ad043456d7343de7ce8[m
Author: Sander van der Heyden <sander@musoni.eu>
Date:   Tue Oct 29 11:35:25 2013 +0100

    MIFOSX-759 - Hotfix for non-unique transaction IDs

[33mcommit 99371b63144a6311d5c49b88d423597a06256013[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Oct 29 10:02:02 2013 +0000

    update to 1.13.3 release properties.

[33mcommit 58b69c31b5afebc566f44ec9de0f5ea59424fec4[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Oct 28 12:26:05 2013 +0000

    update changelog for 1.13.2 bug release

[33mcommit 888803baa0e8f9166ee02470aadc0e24a3022661[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Oct 28 12:26:05 2013 +0000

    update changelog for 1.13.2 bug release

[33mcommit 3729570dbcf8341c7c3755efe5574c112da436f0[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sun Oct 27 22:56:11 2013 +0000

    MIFOSX-722: approximate EMI for daily interest method

[33mcommit 98baa389955b7c0b93dd80e717dc8f79557cea77[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sun Oct 27 22:56:11 2013 +0000

    MIFOSX-722: approximate EMI for daily interest method

[33mcommit 2641d03c8f34bc0ae65aa339dcd527d5edada6a4[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sun Oct 27 18:14:14 2013 +0000

    MIFOSX-729: For flat loans, daily interest calculation not using daily periods when calulating interest, it was still using the installment period and as a result the interest calculated was not correct.

[33mcommit 64b11f8de3d0037b92e9e7e16850a05e08535bae[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sun Oct 27 18:14:14 2013 +0000

    MIFOSX-729: For flat loans, daily interest calculation not using daily periods when calulating interest, it was still using the installment period and as a result the interest calculated was not correct.

[33mcommit 8e6370f8c38390a12c868c46c0e2470fdcea5f36[m
Merge: bc3e107 b1fa667
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sun Oct 27 09:55:54 2013 -0700

    Merge pull request #643 from ugupta/patch-1
    
    Updating INSTALL.md for JDK 7

[33mcommit bc3e107bfd437a4fde6811f94a48cf323366be85[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Oct 25 13:11:47 2013 +0100

    MIFOSX-721: fix issue with daily interest calculation when using interest charged from date feature.

[33mcommit 392be623c2db9b63e2b32032a9bc17dab8e486ec[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Oct 25 13:11:47 2013 +0100

    MIFOSX-721: fix issue with daily interest calculation when using interest charged from date feature.

[33mcommit 1d61eef36932e039c63d8fe7bf42cc0c203d7b9b[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Oct 25 11:28:17 2013 +0100

    MIFOSX-750: earlier fix was incorrect and messed up paging results.

[33mcommit 5e09dce398b1a2a908dc2944b90dd06e414101c4[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Oct 25 11:28:17 2013 +0100

    MIFOSX-750: earlier fix was incorrect and messed up paging results.

[33mcommit b1fa6673b2adc99d351a169950fc6e89b3073197[m
Author: Udai Gupta <mailtoud@gmail.com>
Date:   Wed Oct 23 23:26:39 2013 +0530

    Updating INSTALL.md for JDK 7

[33mcommit b76629792139fefa3301140fd019e2f0e7630fc5[m
Author: Udai Gupta <udgupta@expedia.com>
Date:   Tue Oct 22 19:49:27 2013 +0530

    Updating to use JDK 7 and created gradle wrapper

[33mcommit 5d8738c715ff836eefc1d283b0df822bc84934cc[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Oct 22 14:38:44 2013 +0100

    MIFOSX-749, MIFSOX-750: client search using search term and office hierarchy scoping were no longer working.

[33mcommit 35fa37818fbef09ffa57bd94e9e1d2e805eebdf2[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Oct 22 14:38:44 2013 +0100

    MIFOSX-749, MIFSOX-750: client search using search term and office hierarchy scoping were no longer working.

[33mcommit 18799ed4a7f7f4fc24c3d595d001d43e950a79e2[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Oct 22 13:34:55 2013 +0100

    update release properties.

[33mcommit 0b4d7a5ee5b7fda1fdd81a8d70b269f0d10f667c[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Oct 22 01:20:16 2013 +0100

    udpate changelog for 1.13.1 release

[33mcommit b188d46fde0a9b24a6c7b98d65e7c47c2ee04610[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Oct 22 01:20:16 2013 +0100

    udpate changelog for 1.13.1 release

[33mcommit 15cac255955e5bcbb76af8c0e83bbedd795c90c3[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Oct 22 01:16:12 2013 +0100

    MIFOSX-715: if the image resource is missing for whatever reason (backup didnt copy filestructure, someone removed file) pass back approp error message from API.

[33mcommit 85cd5ff5c06a95d21f6408d747d22a28188bfac8[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Oct 22 01:16:12 2013 +0100

    MIFOSX-715: if the image resource is missing for whatever reason (backup didnt copy filestructure, someone removed file) pass back approp error message from API.

[33mcommit b83075962268b915dade2ab5fc4132866c69e77b[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Oct 22 00:56:53 2013 +0100

    MIFOSX-733: undo of disbural when sending no note parameter was causing null pointer. Fix ensures null pointer exception doesnt occur when checking for existence of a parameter when no request body exists.

[33mcommit 3bbf7538b2b5f13434b9796e49aa1f5670076013[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Oct 22 00:56:53 2013 +0100

    MIFOSX-733: undo of disbural when sending no note parameter was causing null pointer. Fix ensures null pointer exception doesnt occur when checking for existence of a parameter when no request body exists.

[33mcommit d2079b8670969ccea98740f9b078c609c5901a22[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Oct 22 00:15:15 2013 +0100

    MIFOSX-734: add further information on transaction processing strategy feature on platform.

[33mcommit ce8f38694a18fa06834051e0b7a8160ee7138124[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Oct 22 00:15:15 2013 +0100

    MIFOSX-734: add further information on transaction processing strategy feature on platform.

[33mcommit 4e77b115c366508da1f09f52e7713f0d8d257fe8[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sun Oct 20 17:58:52 2013 +0100

    update release version for bug release.

[33mcommit 7cae26432553351fd390e60f81776c0d484c323d[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sat Oct 19 23:02:23 2013 +0100

    update release properties for develop branch.

[33mcommit 28a518cb6b86e7e198737ce1bfad97ae183f4ccc[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sat Oct 19 22:58:39 2013 +0100

    Update CHANGELOG.md

[33mcommit 5e55710de9c352b24a50b545b30d8bb3c24e5ca5[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sat Oct 19 22:54:37 2013 +0100

    Update INSTALL.md

[33mcommit 6cd8e6c5f9841269510832d1dc3cd9ccb67b349a[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sat Oct 19 22:52:21 2013 +0100

    update release properties.

[33mcommit 891dcd1b554f1f63ec008f073c615c7ce5f651db[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sat Oct 19 22:22:14 2013 +0100

    Update INSTALL.md

[33mcommit f8379326b6df72c64f87f6e037ee7ba2a1fdf178[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sat Oct 19 22:20:52 2013 +0100

    update AMI instructions

[33mcommit 0f2685c63cb5e3911f3e3d1bb924c5548e030fe4[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sat Oct 19 21:48:27 2013 +0100

    MIFOSX-748: update api documentation, resolve null pointers when creating charge with empty request.

[33mcommit 961e581f63fd77302f01b7c9e0d13c009e306874[m
Author: andrew-dzak <andrewdzakpasu@musoni.eu>
Date:   Fri Oct 18 13:19:17 2013 +0200

    fix minor issues in Mifosx-437

[33mcommit 7d9e4ca9c197930e1ed33fe0c945da8d2082e2a1[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Oct 18 01:19:53 2013 +0100

    update release props to develop.

[33mcommit 5cba17bba076afc4d56d571296b7adc438a29ce3[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Oct 18 01:18:33 2013 +0100

    supress warnings on data fields.

[33mcommit e1c2d36d108598531860f2ea715ad877261d6e2c[m
Author: andrew-dzak <andrewdzakpasu@musoni.eu>
Date:   Thu Oct 17 12:15:44 2013 +0200

    fix for Mifosx-437

[33mcommit f898c5fcff20cb2af7bb51182d5a7db93f1aa12a[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Oct 16 04:55:24 2013 +0530

    Update CHANGELOG.md
    
    for 1.12.0.release

[33mcommit 768ee1ddefb522bc9c8a9bbc084e24bcf22cab82[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Oct 16 04:51:06 2013 +0530

    prep for 1.12 Release

[33mcommit 7d31547a3722749e3af7ae92cd428a5ad7a58852[m
Merge: d5e798a 264632b
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Oct 15 12:24:52 2013 -0700

    Merge pull request #639 from vishwasbabu/develop
    
    temporary updates to MIFOSX-735 to fix MIFOSX-742

[33mcommit 264632b15a47f94156649842733489cb48c01ec4[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Oct 16 00:52:50 2013 +0530

    temporary updates to MIFOSX-735 to fix MIFOSX-742

[33mcommit d5e798ae6c960638cf6d73bad424809789bfd941[m
Merge: 2847c3a 838ad98
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Oct 15 02:02:07 2013 -0700

    Merge pull request #638 from Musoni/develop
    
    Mifosx-735 removed office runningBalance from glaccount api and updated ...

[33mcommit 838ad986c1326c6779c5679bf002542ffe4352f4[m
Author: andrew-dzak <andrewdzakpasu@musoni.eu>
Date:   Tue Oct 15 09:48:41 2013 +0200

    Mifosx-735 removed office runningBalance from glaccount api and updated api docs

[33mcommit 2847c3ae99a5b22e1ab69bcf5cb816f80d7bd342[m
Author: andrew-dzak <andrewdzakpasu@musoni.eu>
Date:   Mon Oct 14 16:37:18 2013 +0200

    Mifosx-735 api docs update

[33mcommit 3f70867fe85255858f9b8eaa213b91eecc45fa82[m
Author: U-andrew-PC\andrew <andrew.dzak@gmail.com>
Date:   Mon Oct 14 13:53:06 2013 +0200

    Mifosx-735 fixes

[33mcommit cbd08003c1c289d71fba69c716d8eb34539dd109[m
Merge: 13252dc a2766ca
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Oct 13 04:47:55 2013 +0530

    Merge branch 'mifosplatform-1.11.2' into develop

[33mcommit 13252dc7b773798f22d642c3cd9f1257e24d78fc[m
Merge: 9d5071c bebcea8
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sat Oct 12 16:13:41 2013 -0700

    Merge pull request #631 from vorburger/ugd_develop_vorburger
    
    GSoC UGD API Doc proposal (for discussion & review)

[33mcommit 9d5071c320e5327b76e000d84710b50714a23851[m
Merge: f7d89d8 d209663
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sat Oct 12 07:54:48 2013 -0700

    Merge pull request #632 from ashok-conflux/MIFOSX-732
    
    Remove payment mode option from savings charges

[33mcommit d2096630d1463adf1acf0999102ff93b065e5185[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Sat Oct 12 18:54:10 2013 +0530

    Remove payment mode option from savings charges

[33mcommit bebcea8c4220fdc17efc4627cb67080254043ac9[m
Author: Michael Vorburger <mike@vorburger.ch>
Date:   Sat Oct 12 15:14:10 2013 +0200

    GSoC UGD API Doc: Some more intro. blurb, plus minor changes to better illustrate the idea of placeholders in Moustache templates by using concrete loan.* instead abstract {{moustache.tag}}

[33mcommit 5510a408b788a874300714c4f9cc18672e7f9397[m
Author: Andreas Weigel <weigel1986@gmail.com>
Date:   Thu Sep 26 16:05:33 2013 +0200

    added api documentation

[33mcommit f7d89d8b713a3726141abed887e499eed9fb6377[m
Merge: 7f8c7d3 4f4da1d
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sat Oct 12 04:07:05 2013 -0700

    Merge pull request #629 from ashok-conflux/MIFOSX-728
    
    Capture annual fee due date at charge creation time

[33mcommit 7f8c7d3af3bcf61e8a7ca2bdacbe31a146a969ad[m
Merge: 00bbc70 ad127d4
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Oct 10 05:25:45 2013 -0700

    Merge pull request #630 from Musoni/develop
    
    Corrected documentation for undoDisbursal command

[33mcommit ad127d4228a8768c5901029fbdf23de61eaff4ed[m
Author: Sander van der Heyden <sander@musoni.eu>
Date:   Thu Oct 10 14:13:08 2013 +0200

    Corrected documentation for undoDisbursal command

[33mcommit 4f4da1d5079a1c9fdfc0d6b4c0904055f42097f5[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Thu Oct 10 14:54:50 2013 +0530

    Capture annual fee due date at charge creation time

[33mcommit 00bbc70623c3638a6d722ae53525325f79b959b1[m
Merge: c676e01 5838508
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Oct 9 06:27:43 2013 -0700

    Merge pull request #628 from ashok-conflux/MIFOSX-728
    
    Add recurring fee support for savings

[33mcommit 58385081a9a8c8965926ab086bcd089dd6805920[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Wed Oct 9 16:52:42 2013 +0530

    Add support for savings recurring fee

[33mcommit a2766caef226176bebc44408692bcd4e633ffec1[m
Merge: 9e53a08 af0e17f
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Oct 8 22:51:22 2013 -0700

    Merge pull request #627 from pramodn02/MIFOSX-639
    
    handled external id for loan transaction

[33mcommit af0e17fbc900dd8926a2bc83d3b506445020818c[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Wed Oct 9 10:35:08 2013 +0530

    handled external id for loan transaction

[33mcommit 9e53a08fbfe88770d2e1c2da44a7368c7313a76c[m
Merge: c676e01 c79b876
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Oct 8 01:17:52 2013 -0700

    Merge pull request #626 from ashok-conflux/MIFOSX-723
    
    Allow adjustment of deposited amount if there are any charges/withdrawan transactions on same day

[33mcommit c79b876193709117daf8b32edd2f91bc1ac6b63e[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Tue Oct 8 13:42:24 2013 +0530

    Allow adjustment of deposited amount if there are any charges/withdrawan transactions on same day

[33mcommit c676e018722efec8bc9a971785b2b2429da3b38b[m
Merge: 021295f 07ba0c4
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Oct 7 09:21:53 2013 -0700

    Merge pull request #624 from ashok-conflux/savings_charge_apidoc
    
    Updated api doc with savings charges details

[33mcommit 021295f4f3a289e73c9c5e443246795dc29c66d5[m
Merge: f2f5bb4 fd82494
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Oct 7 09:20:49 2013 -0700

    Merge pull request #625 from vishwasbabu/mifosplatform-1.11.1
    
    Mifosplatform 1.11.1

[33mcommit fd82494a07ef58cee972818b8c7be8cac636ad7d[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Oct 7 21:48:29 2013 +0530

    prep for mifosplatform-1.11.1 release

[33mcommit f2f5bb4e9dad1af9e7135b822ce454b98b285f5b[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Oct 7 21:46:32 2013 +0530

    Update CHANGELOG.md
    
    for 1.11.1.RELEASE

[33mcommit dfc3078e136106594e052b8197e2fd0588bf6343[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Oct 7 21:41:42 2013 +0530

    MIFOSX-727: Loan Write off account not being persisted on creating a new Loan Product

[33mcommit 07ba0c4a304739502bbeccd8c79f23c0eb54da71[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Mon Oct 7 20:31:41 2013 +0530

    Updated api doc with savings charges details

[33mcommit 6b8f60539c077fd94c77e5338207b58627d9ff9a[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Oct 6 02:13:08 2013 +0530

    Update CHANGELOG.md
    
    for 1.11.0.RELEASE

[33mcommit 15d905a7d955c0933b747bb9242b6f903b592334[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Oct 6 01:45:32 2013 +0530

    prep for 1.11.0 RELEASE

[33mcommit de481e2fecbaa3f810da87122602f252e706f7d3[m
Merge: b0b5995 c9eb392
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sat Oct 5 12:24:46 2013 -0700

    Merge pull request #623 from vishwasbabu/develop
    
    fixing failing unit tests after accounting changes

[33mcommit c9eb3920d9b2f1e781aa6fa49c1c8ef2b4ae74a8[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Oct 6 00:46:38 2013 +0530

    fixing failing unit tests after accounting changes

[33mcommit b0b59951cd169daf19b7ed1e052cf897d0726e5b[m
Merge: b47d4c3 45e133a
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sat Oct 5 10:19:30 2013 -0700

    Merge pull request #622 from vishwasbabu/develop
    
    edits to temp liability account for overpayments

[33mcommit 45e133ab23e439f12985bde3932d21f2dd63755b[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sat Oct 5 22:43:36 2013 +0530

    edits to temp liability account for overpayments

[33mcommit b47d4c390793b2c81dcbe60ee6cdd297af43530d[m
Merge: f9da94b 3181136
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sat Oct 5 08:53:23 2013 -0700

    Merge pull request #621 from ashok-conflux/MIFOSX-697-patch
    
    Fixed annual fee batch job and savings charges related issues

[33mcommit f9da94b89fa080bf570ebaa235203452643a7633[m
Merge: 3e3b0d6 db37929
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sat Oct 5 08:39:41 2013 -0700

    Merge pull request #620 from vishwasbabu/develop
    
    api updates for accounting changes to loans and savings

[33mcommit 3181136977055d0b28905c3872f07b8a0360b1e1[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Sat Oct 5 12:48:59 2013 +0530

    Fixed annual fee batch job and savings charges related issues

[33mcommit db379295faa246460ce79d31f3dd9b32c2ecd87b[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sat Oct 5 21:07:38 2013 +0530

    api updates for accounting changes to loans and savings

[33mcommit 3e3b0d65a3f78c66d901fcf20b03565e970991a2[m
Merge: 3294c88 73adbcf
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sat Oct 5 05:02:35 2013 +0530

    Merge branch 'master' into develop

[33mcommit 73adbcf99f755daf1ea8326aaf39581ae88f77c3[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sat Oct 5 04:50:19 2013 +0530

    Update CHANGELOG.md
    
    for 1.10.3.RELEASE

[33mcommit d9fca64f415fd05b8393b729f130cd1b7e18a28b[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Mon Sep 23 13:09:43 2013 +0530

    Added missing Braces for create holidays in API doc

[33mcommit 61294724050fa9da60b1790303fc8416d61f63c1[m
Author: mifoscontributer <mifos.contributer@gmail.com>
Date:   Thu Oct 3 14:48:10 2013 +0530

    mifosx-717 fix register/deregister datatable for constraint based approach

[33mcommit 622a4307ecba8bc3bd3dca03b2aff8e5a3ad7ccf[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Sep 26 20:47:39 2013 +0530

    fix for daily interest calculation

[33mcommit 3294c8877db700884d58374204c60857ae51a75b[m
Merge: d374406 7964422
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sat Oct 5 03:41:39 2013 +0530

    moving all bugf fixes and improvemnets from 1.10.3 to develop branch

[33mcommit d37440687b6ce31f43dd4b715c8271f4f7b4a34a[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Oct 4 12:03:05 2013 +0100

    MIFOSX-716: evict user caches when roles and permissions change to stop stale data being returned.

[33mcommit 97846977626feb6061bd59304f5c236a40a289a1[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Oct 4 12:03:05 2013 +0100

    MIFOSX-716: evict user caches when roles and permissions change to stop stale data being returned.

[33mcommit 32a8f921dd0988be23351da2afc61e4c4c1ffc51[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Sep 26 23:52:46 2013 +0100

    MIFOSX-694: add missing users cache to ehcache xml file.

[33mcommit 69bd598456c437289860d61dfa8791b0f3486de1[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Oct 4 11:54:52 2013 +0100

    update version for release

[33mcommit 7964422d7d4e5d5b2b6ea70c88632210e1e70cfb[m
Merge: bd3c0b5 c02fba1
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Oct 4 01:05:49 2013 -0700

    Merge pull request #619 from vishwasbabu/mifosplatform-1.10.3
    
    handle overpayments and refunds in accounting

[33mcommit bd3c0b547f4c88d791171431c78d0edbdfd2a7c3[m
Merge: 5f20909 be24d60
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Oct 4 01:04:09 2013 -0700

    Merge pull request #618 from mifoscontributer/datatables
    
    mifosx-717 fix register/deregister datatable for constraint based approach

[33mcommit c02fba1efef51617aa8e250e74e189343b78cfdd[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Oct 3 15:57:13 2013 +0530

    handle overpayments and refunds in accounting

[33mcommit be24d60b9ee079d95b635f70b7be5bc413f66819[m
Author: mifoscontributer <mifos.contributer@gmail.com>
Date:   Thu Oct 3 14:48:10 2013 +0530

    mifosx-717 fix register/deregister datatable for constraint based approach

[33mcommit 5f209097e1500658615bb6f559bb246c92c569c9[m
Merge: 46146cf 0694dde
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Oct 2 14:50:15 2013 -0700

    Merge pull request #617 from ashok-conflux/MIFOSX-697
    
    Move withdrawal and annual fee charges to charges workflow

[33mcommit 2bd812899aa1a561f41fb70935fed5ffa787f993[m
Merge: 7c80167 c924b33
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Oct 2 14:49:53 2013 -0700

    Merge pull request #615 from pramodn02/MIFOSX-435_2
    
    Mifosx 435 2

[33mcommit 0694dde72fd926c365bf643fed729b0045436861[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Fri Sep 27 17:19:46 2013 +0530

    Move withdrawal and annual fee charges to charges workflow

[33mcommit c924b331896df13268b1e252ed26c9150b75be0a[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Tue Oct 1 22:37:12 2013 +0530

    added DB changes

[33mcommit ba3314f1ee5c8c7771f45be5634f4e455481171d[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Tue Oct 1 22:25:52 2013 +0530

    running balance updation for journal entries

[33mcommit 7c8016726a02b24b85b720b76a06f4aad5119da3[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Sep 27 20:19:16 2013 +0100

    remove unneeded eclipse suppressions.

[33mcommit 46146cf638f89b8a04c0ca337781b90b15cb3e10[m
Merge: c3f127e 2e5a5e8
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Sep 27 04:52:24 2013 -0700

    Merge pull request #614 from pramodn02/MIFOSX-699
    
    Mifosx 699

[33mcommit 2fa22e4a16ffca6489afbe0198766c6c3e24ab00[m
Merge: c4e92ed ca6bbcb
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Sep 27 04:51:52 2013 -0700

    Merge pull request #613 from pramodn02/MIFOSX-435
    
    Mifosx 435

[33mcommit 2e5a5e8dc41a099d6d18c78b6026bc4f63375b70[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Fri Sep 27 13:13:14 2013 +0530

    Added existing transaction id’s for accounting purpose

[33mcommit ca6bbcb550699431b1db8824d446e27f8301ad89[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Fri Sep 27 10:12:06 2013 +0530

    added running balance for journal entries

[33mcommit c4e92edf9914d02d38290f58c1f33005c33532db[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Sep 26 23:52:46 2013 +0100

    MIFOSX-694: add missing users cache to ehcache xml file.

[33mcommit c3f127e1e9e841caf3ca6e8553e14c4911e7f9bc[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Sep 26 23:52:46 2013 +0100

    MIFOSX-694: add missing users cache to ehcache xml file.

[33mcommit e89982223cb0196c57b9e50e5fc77619d9cf440f[m
Merge: a650058 9470e3a
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Sep 26 08:30:46 2013 -0700

    Merge pull request #612 from vishwasbabu/mifosplatform-1.10.3
    
    fix for daily interest calculation with declining balance

[33mcommit 9470e3a858b898d6aa6fd86a69b3a59dc3d42966[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Sep 26 20:47:39 2013 +0530

    fix for daily interest calculation

[33mcommit a650058159b73e52ed44f96050356cffc357c159[m
Merge: 3793b08 4ee40b7
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Sep 26 06:56:56 2013 -0700

    Merge pull request #611 from vishwasbabu/mifosplatform-1.10.3
    
    cleaning up accounting integration for savings

[33mcommit 4ee40b7f5794a312254957700cba7f7741deaf18[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Sep 26 19:23:02 2013 +0530

    cleaning up accounting integration for savings

[33mcommit 3793b0872a6e26314787446200bee8f743e11716[m
Merge: 4320fc2 5d2c331
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Sep 26 02:45:57 2013 -0700

    Merge pull request #610 from ashok-conflux/MIFOSX-692
    
    Mifosx 692

[33mcommit 5d2c331fe154afdf84053b034b5bd9d708377ac1[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Thu Sep 26 12:27:44 2013 +0530

    savings charge currency must match savings account currency

[33mcommit 4320fc25a26fe70250110d102d381f9ff9a9efe0[m
Merge: 617662f aabe954
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Sep 25 07:34:42 2013 -0700

    Merge pull request #609 from ashok-conflux/MIFOSX-691-Patch
    
    Mifosx 691 patch

[33mcommit aabe9549e0e76c76cbe7dd8bfd8780cce56c4dca[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Wed Sep 25 16:34:22 2013 +0530

    added support for undo savings fees

[33mcommit 617662f0dfe7c504fae3a8440f59cd0129134570[m
Merge: 72b0889 5776ee3
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Sep 24 14:24:28 2013 -0700

    Merge pull request #608 from vishwasbabu/temp
    
    Temp

[33mcommit 5776ee3b534d028ba104025cbf769ca6f21b7a90[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Sep 24 23:49:10 2013 +0530

    handle penalties/fees separately for savings and add ability to map penalties/fees to separate income accounts

[33mcommit 72b0889bbfbeb6a4f5b3546fcb4f8aff40231bf6[m
Merge: 7e606a2 7330ed0
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Sep 24 11:17:22 2013 -0700

    Merge pull request #607 from ashok-conflux/MIFOSX-691
    
    Savings fee code cleanup and added shcedule for paying charges

[33mcommit 7330ed08ea8c1905c4532996cd6d1f88d7cffabc[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Tue Sep 24 18:15:12 2013 +0530

    Savings fee code cleanup and added shcedule for paying charges

[33mcommit 7e606a2d13210710c8e70a8cf2fdbb0f44e420c7[m
Merge: ff5779c d95172a
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Sep 23 08:36:24 2013 -0700

    Merge pull request #605 from pramodn02/MIFOSX-688
    
    Mifosx 688

[33mcommit d95172ae814dbc552a7453886c1da215d6a54b9c[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Mon Sep 23 20:59:36 2013 +0530

    removed unused imports

[33mcommit b0500f6ee9ecc74fc343ef00cbd87e23f0a396c1[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Mon Sep 23 20:57:18 2013 +0530

    added unique number generation for account transaction id

[33mcommit 1ca11846b473e640fe6efd041ec4c9fd991e9826[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Sep 23 16:16:18 2013 +0100

    some comments around template implementation.

[33mcommit ff5779c23879718cb38a70a845d683da3935807a[m
Merge: ae7c91c ac31249
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Sep 23 00:46:07 2013 -0700

    Merge pull request #604 from ashok-conflux/MIFOSX-679
    
    Added missing Braces for create holidays in API doc

[33mcommit ac31249a71fc4d94acde246e84268f3943e0ce50[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Mon Sep 23 13:09:43 2013 +0530

    Added missing Braces for create holidays in API doc

[33mcommit ae7c91c6a41e018e60b946cbbbed102e726ea346[m
Merge: 773883e e8c9381
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Sep 23 00:24:36 2013 -0700

    Merge pull request #603 from pramodn02/MIFOSX-689_2
    
    update account transfers with new loan transaction id

[33mcommit e8c9381818cc9faa3b5e1a01276ace34ae3d0916[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Mon Sep 23 12:25:51 2013 +0530

    update account transfers with new loan transaction id

[33mcommit 72a0a547aa53c0d9b4a5de422f8a31319156562d[m
Merge: 090a9a4 773883e
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Sep 23 11:33:08 2013 +0530

    Merge branch 'master' into develop

[33mcommit 773883eff7e395916858ef5c3f502738fc1b5592[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Sep 23 11:27:42 2013 +0530

    Update CHANGELOG.md
    
    for release 1.10.2

[33mcommit 090a9a45364dea90491770df3b3e925b6cbaa9ca[m
Merge: 5e5af8c 878e52c
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Sep 23 11:23:47 2013 +0530

    Merge branch 'master' into develop

[33mcommit 16c864abc28506eae232ec28503f8a74d314d385[m
Merge: 01368e5 878e52c
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Sep 22 22:37:27 2013 -0700

    Merge pull request #602 from vishwasbabu/mifosplatform-1.10.2
    
    bug fixes for 1.10.2 release

[33mcommit 878e52c9865b2537d444e01fe7cdcce86dd4df56[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Sep 23 01:16:37 2013 +0530

    bug fixes for 1.10.2 release

[33mcommit 5e5af8cb1360721fee103238c60351bab289ecab[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sun Sep 22 01:02:37 2013 +0100

    ignore one more template related test for now.

[33mcommit 868533e5ec9daab900eae21948edb92c3e4239b1[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sat Sep 21 21:05:13 2013 +0100

    ignore failing tests from GSOC project integration.

[33mcommit c607c9bba515ef808eb88fbf48352a487a742324[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sat Sep 21 13:28:59 2013 +0100

    MIFOSX-117: integrate andreas work on UGD.

[33mcommit 01368e59b23f1629ef2b95533ba4d6617cc7cd36[m
Merge: b13156b 3531e95
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Sep 20 23:53:10 2013 +0530

    merge release 1.10.1 to develop branch

[33mcommit 3531e956fbec9b2c142ace7430310da6a437dc55[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Sep 20 23:30:40 2013 +0530

    Update CHANGELOG.md
    
    for 1.10.1 Release

[33mcommit 65ca796fce0c2c1c8c460d252657aa01de022745[m
Merge: 26baa69 086fbcd
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Sep 20 10:56:55 2013 -0700

    Merge pull request #601 from vishwasbabu/mifosplatform-1.10.1
    
    Mifosplatform 1.10.1

[33mcommit 086fbcd0c99a757c2aa7dd63398692b6d3b3bf3c[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Sep 20 23:12:47 2013 +0530

    resolve compilation issues generated by cleanup

[33mcommit 749565dd1ca7d0656129111bac59fab051ae1b43[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Sep 20 23:11:38 2013 +0530

    formatting and cleaning up the entire project

[33mcommit 26baa69d2e2f563b78a66db7c7ff5d78ac45ad00[m
Merge: 59e8fe8 07d3669
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Sep 20 10:20:07 2013 -0700

    Merge pull request #600 from vishwasbabu/mifosplatform-1.10.1
    
    fix for multiple product mappings

[33mcommit 07d36695496141f31282dfa62d58f76cdaf45209[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Sep 20 22:46:08 2013 +0530

    fix for multiple product mappings

[33mcommit b13156b33f880c3de69b4e516d890e09e90eaaa3[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Sep 20 10:17:08 2013 +0100

    make patch follow _x naming convention for bug release.

[33mcommit 3fa614f4815e448622baf52a4c0b79f5b6a72530[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Fri Sep 20 14:30:09 2013 +0530

    Added missing charges columns to Savings account table

[33mcommit 5192afaf4e862cbedc8e556bd670cd08c9bae395[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Sep 20 10:10:35 2013 +0100

    fix eclipse warnings around unused imports and fields in Data objects.

[33mcommit 59e8fe838ed4f7d488f40d9615fd39f52892d1f8[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Sep 20 10:17:08 2013 +0100

    make patch follow _x naming convention for bug release.

[33mcommit 81078a449c09c4c5954b1e8c6d123fa79e9ff5b7[m
Merge: f991ba6 5463e27
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Sep 20 10:13:23 2013 +0100

    Merge remote-tracking branch 'ashok/MIFOSX-644-patch' into mifosplatform-1.10.1

[33mcommit f991ba64da4d601c39b829022a862eaf114be83f[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Sep 20 10:10:35 2013 +0100

    fix eclipse warnings around unused imports and fields in Data objects.

[33mcommit 5463e27b2c79c63b1a43eb7dd8a2c00dab9a8524[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Fri Sep 20 14:30:09 2013 +0530

    Added missing charges columns to Savings account table

[33mcommit 1474ea9179c7d0ec72d84598c421502153ce3385[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Sep 20 09:41:38 2013 +0100

    update version numbers for release.

[33mcommit 6e252b8a581a989a3445864f754dc4eb03cb0f1f[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Sep 20 01:40:52 2013 +0530

    Update CHANGELOG.md
    
    for 1.10 release

[33mcommit 3fe60ff08fe732969038ab4c195b23360576ef4b[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Sep 20 01:29:51 2013 +0530

    prep for 1.10 release

[33mcommit bf209819f6b1de5a94cc343fef3c9319109d1299[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Sep 20 01:08:08 2013 +0530

    add validation for charges

[33mcommit 15127b747f4ba8988e67ad66240e854f8a0e4d61[m
Merge: 421724d c46eab4
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Sep 19 12:05:51 2013 -0700

    Merge pull request #596 from vishwasbabu/develop-new
    
    Develop new

[33mcommit c46eab44532331786bb08ad07762b4928ff9a0d9[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Sep 20 00:11:46 2013 +0530

    added some validations around transfers of savings accounts

[33mcommit 421724d24c504a497c1fa8d1e6f4c7437a952d0c[m
Merge: 4a8a0d4 be1f90e
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Sep 19 11:45:16 2013 -0700

    Merge pull request #595 from ashok-conflux/MIFOSX-644
    
    Mifosx 644

[33mcommit be1f90ec5ce700432a3fa62786ce23e939767625[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Fri Sep 13 10:14:28 2013 +0530

    support fee configuration for savings

[33mcommit 4a8a0d42b7bd5e9f6a44c5fa989786493464c85f[m
Merge: 16ccb51 9f22479
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Sep 19 08:25:21 2013 -0700

    Merge pull request #594 from pramodn02/MIFOSX-642_2
    
    Mifosx 642 2

[33mcommit 9f2247953e699378b548efa32ec93b4d70aba755[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Thu Sep 19 20:41:44 2013 +0530

    resolved concurrent exception

[33mcommit 16ccb51a3ac51e601b86b076873718343d0126ae[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Sep 19 15:11:49 2013 +0100

    MIFOSX-259: ensure cache type persisted in database is used when platform starts up.

[33mcommit 5cbcab9c1e1f261638434f54731281b3b5816bf3[m
Merge: 73105c8 ae4a665
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Sep 19 06:49:45 2013 -0700

    Merge pull request #593 from vishwasbabu/develop
    
    fixed issues with combination of regular and account transfer fees at di...

[33mcommit ae4a665015068e0cfcc2a02019c410750f397f81[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Sep 19 19:18:52 2013 +0530

    fixed issues with combination of regular and account transfer fees at disbursal

[33mcommit 73105c8485c3562e12eaae75f9056e115045be6b[m
Merge: 076c8c9 34b031a
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Sep 19 06:10:25 2013 -0700

    Merge pull request #592 from pramodn02/MIFOSX-642_2
    
    Mifosx 642 2

[33mcommit 34b031a05b5cb3a33c677f656aeb6216c5cd6910[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Thu Sep 19 18:36:32 2013 +0530

    API doc changes

[33mcommit 076c8c9fccefc9360b8cf8a4b420212883895021[m
Merge: 99d316f 344ca59
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Sep 19 03:40:05 2013 -0700

    Merge pull request #589 from pramodn02/MIFOSX-642_2
    
    Mifosx 642 2

[33mcommit 344ca591c29c5570c2a88b2dd2d52001504e4244[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Thu Sep 19 16:03:49 2013 +0530

    corrections to stop duplicate entries for loan charge paid by

[33mcommit 77913d2eee4c930d0109e98884f4438372ec6ecd[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Thu Sep 19 11:47:45 2013 +0530

    removed unused imports

[33mcommit a09127cc60a386542a4ad66a299cf2b65e31916d[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Thu Sep 19 11:42:40 2013 +0530

    added validation for add charge

[33mcommit 99d316f406557e8ac809bdd320a7cb3409d74381[m
Merge: 0e18e29 aaaba18
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Sep 19 09:37:36 2013 +0530

    merging 1.9.2 release into develop branch

[33mcommit aaaba187b3da56f67e52d9f81f70818bc804ca44[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Sep 19 09:15:39 2013 +0530

    Update CHANGELOG.md
    
    fix typos

[33mcommit 948da215f599662623c90a5aad145bee8e9cf79a[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Sep 19 09:11:24 2013 +0530

    Update CHANGELOG.md
    
    for 1.9.2 release

[33mcommit 669a7eef3511d5de803f1564e0a776c1c6902ab3[m
Merge: 08450c4 244895e
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Sep 18 20:33:27 2013 -0700

    Merge pull request #587 from vishwasbabu/mifosplatform-1.9.2
    
    minor fixes while prepping for 1.9.2 release

[33mcommit 244895e07d5570862956ad7ad28ef0f2e671a31e[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Sep 19 09:00:54 2013 +0530

    minor fixes while prepping for 1.9.2 release

[33mcommit 0e18e2951b33edea7436748243ba3fe8fe64b0d8[m
Merge: f4f8df0 d57a8fa
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Sep 18 05:30:36 2013 -0700

    Merge pull request #586 from pramodn02/MIFOSX-642
    
    Mifosx 642

[33mcommit f4f8df0b21030958de2c725c26d2d6e863199695[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Sep 18 12:37:20 2013 +0100

    MIFOSX-633: fix typos

[33mcommit cad6a46ea6e82ec62dcbca75fac1cb9e541c300e[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Fri Sep 13 12:41:45 2013 +0530

    Added Holidays and working days details to api doc

[33mcommit c09cf8c5b0a54e77832479f59b3d2b09e90fda3d[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Sep 18 12:30:32 2013 +0100

    fix conflicts on cherry pick

[33mcommit 1f7ab21152e81ebe3df04b2601d358b382727702[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Sep 18 12:15:30 2013 +0100

    MIFOSX-672: fix issue with FLAT interest calculation not correctly calculation prinicpal balance for final installments.

[33mcommit 08450c4a8786e22df88c36fcf1b9b664686517a8[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Sep 18 12:37:20 2013 +0100

    MIFOSX-633: fix typos

[33mcommit edb03763537fe7a43d82cdd9422a6ea26ae9e8d8[m
Merge: 7aa8332 bc0a120
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Sep 18 12:31:52 2013 +0100

    Merge remote-tracking branch 'ashok/MIFOSX663' into mifosplatform-1.9.2

[33mcommit 7aa8332b301fc327e985edd5622bcc1128338657[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Sep 18 12:30:32 2013 +0100

    MIFOSX-671: ensure summary fields like balance on transactions are updated in the case where account is activated and deposit automatically made.

[33mcommit b2168bff2ce0728e859377589739b20650147fa2[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Sep 18 12:15:30 2013 +0100

    MIFOSX-672: fix issue with FLAT interest calculation not correctly calculation prinicpal balance for final installments.

[33mcommit d57a8faf30c436378a840ff544062a84a65b4e6a[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Wed Sep 18 15:49:05 2013 +0530

    payment of loan charges from savings

[33mcommit b6324630ff2691e51bd638a42850c2fc617ce3c2[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Sep 18 09:46:19 2013 +0100

    bump up version numbers for release.

[33mcommit adc915d9efc8b2ead8f753affb25a058339a58f8[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Sep 17 21:19:58 2013 +0530

    fix issues with sql patch number in previous pull request

[33mcommit e731f50f38d65548a43fab8d990b4e6d04c01eb3[m
Merge: 347b618 2b8da96
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Sep 17 08:47:08 2013 -0700

    Merge pull request #585 from vishwasbabu/develop
    
    track currency with accounting

[33mcommit 2b8da96474f75dd27b1f4d64e62a01e87db4e437[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Sep 17 21:13:45 2013 +0530

    track currency with accounting

[33mcommit 347b618be1c44b7b85b8e3846ccd0969df2beba7[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Sep 16 17:29:36 2013 +0100

    MIFOSX-259: support runtime switching of cache through API. By default no-cache implementation is used.

[33mcommit bc0a120f617de694818a5b25c8a321f491285330[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Fri Sep 13 12:41:45 2013 +0530

    Added Holidays and working days details to api doc

[33mcommit 658d38a3fa4cf712d938bcaff29d7b0c7837f150[m
Merge: 5d94776 cbf4a43
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Sep 12 18:27:06 2013 -0700

    Merge pull request #583 from vishwasbabu/develop
    
    minor issues in savings transfer

[33mcommit cbf4a43cb6a084da1641e134b9c12f1b041078d7[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Sep 13 06:54:24 2013 +0530

    minor issues in savings transfer

[33mcommit 5d94776c56ade27364c2f290419aa0b7a6bbee33[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Sep 11 20:51:43 2013 +0100

    MIFOSX-259: make so loanTenantById gets cached correctly - also introduce delegatingCacheManager to allow us to introduce api and configuration to support switching cache solution runtime. At present caching is disabled through use of NoOpCacheManager.

[33mcommit 822eea631f7b09d7a377c6d6b6a966ab3b32f348[m
Author: Anuruddha Premalal <anuruddhapremalal@gmail.com>
Date:   Mon Sep 9 22:43:43 2013 +0530

    Update cache zone namings, Remove roles from being cached, added caching to loadOfficeById,  added caching to loadTenantById(not cached though), made  basicAuthenticationProcessingFilter bean to be depend on cachemanager bean

[33mcommit 802df9b34ae21cc6af1670d9d623c803cef126bb[m
Author: Anuruddha Premalal <anuruddhapremalal@gmail.com>
Date:   Sat Aug 31 07:14:28 2013 +0530

    changed to ehcache inmemory store configuratinos

[33mcommit 72ceb5340f4dc541b5da6d6c150d6a4235a570a2[m
Author: Anuruddha Premalal <anuruddhapremalal@gmail.com>
Date:   Sun Aug 25 18:51:52 2013 +0530

    introduce cache zone identifier to keys

[33mcommit 0410ab14ea33590c9ebc9543a85aa377fbd576c0[m
Author: Anuruddha Premalal <anuruddhapremalal@gmail.com>
Date:   Sun Aug 25 13:50:32 2013 +0530

    initial memcached integration

[33mcommit e67ab7cf88cccc6a260354aedfe27a49f91e6832[m
Merge: 190b9a5 5af987f
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Sep 10 20:32:33 2013 +0530

    merging 1.9.1 release into develop

[33mcommit 5af987ffb81ff663df8063eb6fb196802914b4f7[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Sep 10 20:21:55 2013 +0530

    Update CHANGELOG.md

[33mcommit d5e50a8913576e889802619fa8df572da33d15d4[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Sep 10 20:21:27 2013 +0530

    Update CHANGELOG.md
    
    for 1.9.1 Release

[33mcommit 739737dbcab9257f9f7bcfed3abeef0365d70d95[m
Merge: 1faf75b d1fba8c
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Sep 10 07:42:29 2013 -0700

    Merge pull request #581 from pramodn02/MIFOSX-647
    
    Mifosx 647

[33mcommit d1fba8ca8f34cfb83de56bce0cb3c59e1aa12dcc[m
Merge: 18cc921 1faf75b
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Tue Sep 10 20:02:45 2013 +0530

    Merge branch 'mifosplatform-1.9.1' of github.com:openMF/mifosx into MIFOSX-647

[33mcommit 18cc921d62d23e466ca8e5d7e766694fd5824708[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Tue Sep 10 20:01:47 2013 +0530

    added validation for account trasfer transaction while updating

[33mcommit 1faf75b174ba4ce171ed8d2f5d59aa6f9e12badf[m
Merge: 6d533b7 bba521a
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Sep 10 07:16:48 2013 -0700

    Merge pull request #580 from vishwasbabu/mifosplatform-1.9.1
    
    minor issues with loan transfers

[33mcommit bba521a8c38ed968b6bc94ca4d8b9ab1a180ca55[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Sep 10 19:43:52 2013 +0530

    minor issues with loan transfers

[33mcommit 190b9a58b3057c4f65ec2b995807288017f056b9[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Sep 10 17:13:53 2013 +0530

    wip:enabling transfer of saving accounts

[33mcommit b9f929aec217345e5711a6bcd03766904cd63ce3[m
Merge: da3a3b4 ce66ca4
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Sep 9 23:30:13 2013 -0700

    Merge pull request #579 from vishwasbabu/develop
    
    accounting for savings now picks up office from transaction

[33mcommit ce66ca471886b1b661737eb58cfc1ddea4c954cf[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Sep 10 11:58:30 2013 +0530

    accounting for savings now picks us office from transaction

[33mcommit da3a3b4c672fd381c6dabeb7904dd190bc2e47f3[m
Merge: 1b1c800 546e480
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Sep 9 20:32:50 2013 -0700

    Merge pull request #578 from vishwasbabu/develop
    
    map savings account transactions to an Office in preparation for savings...

[33mcommit 546e480496bbe15afbc79b8bbc93c07f61a6b10f[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Sep 10 09:01:39 2013 +0530

    db changes for mapping savings transactions to an office

[33mcommit dcbcfee1482a6c40f82362d144fb210be12c994f[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Sep 10 08:58:33 2013 +0530

    map savings account transactions to an Office in preparation for savings account transfers

[33mcommit 1b1c800616cb5dc2b8bc8bb4bba634e71c85f04e[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Sep 9 13:24:53 2013 +0100

    ensure repository is picked up from right package after refactor.

[33mcommit d482308d0f988cd2e0cd30fd1a966a170ea7da28[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Sep 9 12:53:42 2013 +0100

    removed empty packages, update appContext.xml to look for components in mix package.

[33mcommit 3d20a6cbbbc58eb6b313cd67bf6b978f5641dfb4[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Sep 9 12:50:06 2013 +0100

    review changes to yanna pull request for mix-xbrl GSOC project.

[33mcommit 3289de9fe3d0972d938bd3bb2da8be26d81db89d[m
Merge: 41aeda4 3a217c7
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Sep 9 10:01:07 2013 +0100

    Merge branch 'yanna' into develop

[33mcommit 41aeda4f0704f885b023638ae46a3033c37e9f1c[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Sep 9 09:54:42 2013 +0100

    MIFOSX-648: allow user to fetch their own account data no matter what permissions they hold.

[33mcommit 6d533b7d5cf7f15b7ceb234caa51fb64c75a93f6[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Sep 9 09:56:21 2013 +0100

    update release version details.

[33mcommit 0de4adf6e5018c1d5b24ff8d063d0160a54d3ebb[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Sep 9 09:54:42 2013 +0100

    MIFOSX-648: allow user to fetch their own account data no matter what permissions they hold.

[33mcommit 3a217c732492b7fa2c8084cd9c3e85ae9477841d[m
Author: Yanna Wu <wuyanna2009@gmail.com>
Date:   Mon Sep 9 03:36:59 2013 -0400

    xbrl reporting integration:
    add four apis:
    /mixtaxonomy get
    /mixmapping get
    /mixmapping put
    /mixreport ger

[33mcommit 4762a7786f351dbe5798b1406fbecdb91e2b16fc[m
Merge: a321910 bdb2162
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Sep 9 07:39:16 2013 +0530

    Merge branch 'master' into develop

[33mcommit bdb2162ab7a4d56864acd41f5eadcb4af24beb0d[m
Merge: 8ddf2dd b297f75
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Sep 8 19:06:41 2013 -0700

    Merge pull request #574 from openMF/master
    
    disable constraint based approach for datatables by default

[33mcommit b297f75c4c847542f5e955a999403a01deb4f41a[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Sep 9 07:34:04 2013 +0530

    disable constraint based approach for datatables by default

[33mcommit a3219104fe7e7b73c9db0eed56d871cbd1bf326c[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Sep 9 07:04:17 2013 +0530

    track 1.10 release

[33mcommit 8ddf2dd85843dda48babba562db0ac4839f54b2b[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Sep 9 06:36:16 2013 +0530

    Update CHANGELOG.md
    
    for 1.9.0 Release

[33mcommit 0297757f50675d019f13992a5216698accbb59e1[m
Merge: 74b0b82 fcacf15
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Sep 8 17:48:03 2013 -0700

    Merge pull request #573 from vishwasbabu/develop
    
    Develop

[33mcommit fcacf1582d473282bd0002933b50d60ac6a90fc1[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Sep 9 06:10:07 2013 +0530

    prep for 1.9.0 Release

[33mcommit 17b87c18a311267cd2f1d8c8fd32bb3f578b0edb[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Sep 9 06:06:40 2013 +0530

    minor fixes to transfer functionality

[33mcommit 74b0b82d1f7013756ff52e525501519bc43b3a49[m
Merge: 445a341 fcee0a3
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Sep 8 16:27:40 2013 -0700

    Merge pull request #572 from vishwasbabu/develop
    
    cleaning up transfers functionality

[33mcommit fcee0a333ed1bcef8387f756d831fd3c127f9f1a[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Sep 9 04:56:35 2013 +0530

    cleaning up transfers functionality

[33mcommit 445a341997db903ea35087c3fafe77de576349ca[m
Merge: 26565d6 3ddaa07
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Sep 8 09:59:33 2013 +0530

    Merge branch 'mifosx-436' into develop

[33mcommit 3ddaa07548dc2b572bdfb836af1e68302eb4e2d1[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Sep 8 09:58:28 2013 +0530

    adding some validations for loan transfers

[33mcommit 26565d6be9b3e94d7f0819979372d90f84990836[m
Merge: e36255c 44a6368
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Sep 6 06:40:24 2013 -0700

    Merge pull request #571 from pramodn02/MIFOSX-634_2
    
    Mifosx 634 2 - added status validation for undo transaction

[33mcommit 44a63683c551660520d7cb9bd6baaeaa9016151a[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Fri Sep 6 18:35:22 2013 +0530

    added status validation for undo transaction

[33mcommit e36255c03033009dd1833c94bb6fd658ae2b6dbf[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Sep 6 18:04:05 2013 +0530

    fixing build issues due to bas merge

[33mcommit 48bd0b02fe9044845cd1e8848055c8cab681b7fa[m
Merge: ae75b07 6dad5ae
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Sep 6 17:27:08 2013 +0530

    merging ashoks changes for group closure

[33mcommit 6dad5aee4ae94835271355f51a229f9757c4c22f[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Fri Aug 30 18:29:17 2013 +0530

    MIFOSX-625 - support ability to close group

[33mcommit ae75b07fb5e1dd66879bbd82046272822494e72d[m
Merge: 34f514d d816baf
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Sep 6 03:39:08 2013 -0700

    Merge pull request #569 from pramodn02/MIFOSX-634
    
    Mifosx 634-Added support to activate closed account by adjusting the transaction

[33mcommit d816baf088ff9aed0f4b9c040ec6bbcd07f9e18e[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Fri Sep 6 16:07:15 2013 +0530

    Formatted the classes

[33mcommit 0200ca84dc605e3a02ca1851ebd971644c534ec2[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Fri Sep 6 15:58:36 2013 +0530

    Added support to activate closed account by adjusting the transaction

[33mcommit 34f514da4e99769e9037205c6a4de29f585e20da[m
Merge: ebba52b 1b7e2df
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Sep 5 07:49:29 2013 -0700

    Merge pull request #565 from pramodn02/MIFOSX-622
    
    Mifosx 622 - added transfer withdrawal fee configuration

[33mcommit 1b7e2df3eee4d91d3d6961025c1169548d7f5cfb[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Thu Sep 5 18:39:17 2013 +0530

    api doc changes added for transfer withdrawal fee configuration

[33mcommit 6769fd18438cfad003c9a62900205a4dc6f5f48e[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Thu Sep 5 17:53:01 2013 +0530

    added transfer withdrawal fee configuration

[33mcommit ebba52bb9ca6bc9d2174404d019e6d73d54f1c8a[m
Merge: aab79b3 c3c37e7
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Sep 4 03:29:27 2013 -0700

    Merge pull request #564 from pramodn02/MIFOSX-624
    
    Mifosx 624-added validation for applying annual fee negative balance

[33mcommit c3c37e7e647698e31e6a22f55f805451f37d69f7[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Wed Sep 4 15:36:41 2013 +0530

    added validation for applying annual fee negative balance

[33mcommit aab79b35613f3100f26f34d404fb4e2c387d6e54[m
Merge: b3d94aa 0e1c55f
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Sep 4 01:16:37 2013 -0700

    Merge pull request #563 from vishwasbabu/mifosx-436
    
    Mifosx 436

[33mcommit 0e1c55f349c6725d89dcd26afbbb0ef3c66f32d0[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Sep 4 13:37:55 2013 +0530

    fix for repayment intervals recalculation

[33mcommit b3d94aa8c4db280c48144ff15464e07623dabaa2[m
Merge: 6650946 d93f630
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Sep 3 19:56:02 2013 -0700

    Merge pull request #561 from vishwasbabu/mifosx-436
    
    Mifosx 436

[33mcommit d93f630c9bd357c3cef23de52ad1488297a9c3ec[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Sep 4 08:20:22 2013 +0530

    cleaning up transfers functionality and prepping for 1.9 release

[33mcommit 6650946b1e4f7b35f9efeb806b8ef4aa7f893e2a[m
Merge: 93d303b 482c1fe
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Sep 3 07:18:56 2013 -0700

    Merge pull request #560 from pramodn02/MIFOSX-628
    
    Mifosx 628

[33mcommit 93d303b28666846a8c40c4fe2a67aeac107d8d95[m
Merge: 2a19622 ddc5757
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Sep 3 07:18:11 2013 -0700

    Merge pull request #559 from pramodn02/MIFOSX-635
    
    Mifosx 635

[33mcommit 2a196228d21dd442115fcc5f02813caccf570c38[m
Merge: 5d4b6f8 d1e442a
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Sep 3 07:17:40 2013 -0700

    Merge pull request #557 from pramodn02/MIFOSX-517_2
    
    datatable mappings with code and code values implementation

[33mcommit 482c1fe6778f98178fc86e285c79e26b10d73ecb[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Tue Sep 3 10:49:34 2013 +0530

    added condition to change the year

[33mcommit ddc5757c6d5d09dba12648b50e742b0ee476ee73[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Tue Sep 3 10:01:09 2013 +0530

    Changed validation API for accepting formatted Strings

[33mcommit 5d4b6f8f7228392831fef847f22e320365ddc38e[m
Merge: 3c56795 3544f5c
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sat Aug 31 21:45:13 2013 +0100

    Merge branch 'mifosplatform-1.8.2' into develop

[33mcommit 3544f5cd5c06b37f8c42e8218d7d3f5a2b849b7f[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sat Aug 31 21:41:39 2013 +0100

    MIFOSX-627: closing balance is calculated wrongly for same day transactions which can results in negative interest been calculated.

[33mcommit d1e442aa14f6ec4286d404a3a53232dbb5fe8d65[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Sat Aug 31 14:22:56 2013 +0530

    datatable and code value mappings

[33mcommit 3c5679544bf8d65f467dbee8fc656e32d5d4b4fe[m
Merge: 3dc5126 45b4a06
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sat Aug 31 00:08:29 2013 -0700

    Merge pull request #555 from ashok-conflux/MIFOSX-607
    
    Mifosx 607

[33mcommit 45b4a066d9e8f08326c5d581850c65a95ff93517[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Fri Aug 30 19:39:26 2013 +0530

    MIFOSX-607- fixed collection sheet save issue with group which in a center

[33mcommit 3dc5126bb98e5f95d247da55c78b1e5955a3281d[m
Merge: f131506 f218bce
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Aug 29 09:45:19 2013 -0700

    Merge pull request #554 from vishwasbabu/mifosx-436
    
    Mifosx 436

[33mcommit f218bce80f952bf43e44dee684d93424fd393503[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Aug 29 22:13:22 2013 +0530

    clean up for close client

[33mcommit f131506916a1aedc562ccdbfc4e55708a1e3785d[m
Merge: 235c258 22fc47a
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Aug 29 09:20:43 2013 -0700

    Merge pull request #553 from vishwasbabu/mifosx-436
    
    updating loan transacation api to retreive office data

[33mcommit 22fc47a938b9f0d324cbff237ebd369c6384856c[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Aug 29 21:47:49 2013 +0530

    updating loan transacation api to retreive office data

[33mcommit 235c258be78120bd118cfa9247494e1cf1e7220a[m
Merge: c842339 a99997f
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Aug 29 05:30:32 2013 -0700

    Merge pull request #551 from ashok-conflux/MIFOSX-613
    
    Mifosx 613

[33mcommit c842339bde036f149959fe7d9a54c5fc2386ab24[m
Merge: 8286801 82821ce
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Aug 29 05:29:51 2013 -0700

    Merge pull request #550 from pramodn02/MIFOSX-596
    
     changes to transfer funds from overpaid account

[33mcommit a99997f5dec9b2e8eba5215ddd934b40171be40b[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Thu Aug 29 16:47:30 2013 +0530

    fixed MIFOSX-613 loan office assignment issue

[33mcommit 82821cedecf05814c57c511fcedc6c4e0420382e[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Thu Aug 29 15:53:16 2013 +0530

     changes to transfer funds from overpaid account

[33mcommit 8286801eac936212f0a76e750ac22e46f6d78ed2[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Aug 28 12:52:09 2013 +0100

    add suppress warning to Data object.

[33mcommit 4ad25ce2ea5dc2ac557f0526611242dc0b6af2e8[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Aug 28 12:46:29 2013 +0100

    remove invalid suppressings and issue found by ashok in savings.

[33mcommit b2644bee9d35496a135cfb329535d1543f5859ee[m
Merge: 2ac1dad 2c8db68
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Aug 28 00:34:34 2013 -0700

    Merge pull request #548 from ashok-conflux/MIFOSX-607
    
    MIFOSX-607 - capture client atendance in collection sheet

[33mcommit 2ac1dad66a6a0a9c85867ee7f16cf234173a4a55[m
Merge: 1f7a93b e15fa79
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Aug 28 00:31:44 2013 -0700

    Merge pull request #547 from mifoscontributer/mifox-predefinedAR
    
    Changing predefined posting json for consistenxy

[33mcommit 2c8db68a4068b53efe2758b78dc4cbcba322b231[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Sat Aug 17 10:37:38 2013 +0530

    MIFOSX-607 - capture client atendance in collection sheet

[33mcommit e15fa7967fd4a3b6a343a59d34aec332419bdb67[m
Author: mifoscontributer <mifos.contributer@gmail.com>
Date:   Wed Aug 28 10:58:21 2013 +0530

    Changing predefined posting json for consistenxy

[33mcommit 1f7a93b7aa650802b8675ae1bef5d74f78376a4b[m
Merge: 4bc7b64 75bcfbf
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Aug 27 10:01:06 2013 -0700

    Merge pull request #546 from vishwasbabu/mifosx-436
    
    update test cases to handle new parameters required for accrual/cash acc...

[33mcommit 75bcfbff5771756997ee6b648e99585502a23efb[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Aug 27 22:28:20 2013 +0530

    update test cases to handle new parameters required for accrual/cash accounting

[33mcommit 4bc7b64270da1d2b025b1b00837c845bd98c136c[m
Merge: 5a8c36d 8afe033
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Aug 27 07:30:05 2013 -0700

    Merge pull request #534 from mifoscontributer/mifosx-440
    
    Meeting calendar cleanup

[33mcommit 5a8c36d0af0241d15b8e04366f581f750bdf60d4[m
Merge: e10c4c2 52be32f
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Aug 27 07:28:48 2013 -0700

    Merge pull request #541 from pramodn02/MIFOSX-619
    
    Mifosx 619

[33mcommit 52be32fbdf65a50dc6972965556021269055437b[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Tue Aug 27 15:51:10 2013 +0530

    removed postJournalEntries call for close

[33mcommit e10c4c25e7bcedf61b37f720999d033c77e77c13[m
Merge: faaf180 7da8cbc
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Aug 27 02:01:00 2013 -0700

    Merge pull request #544 from mifoscontributer/mifosx-547_c
    
    Transfer clients between groups

[33mcommit 7da8cbc3d9675090b67fd39aa55155ca09a047c2[m
Author: mifoscontributer <mifos.contributer@gmail.com>
Date:   Tue Aug 27 10:46:41 2013 +0530

    Transfer clients between groups

[33mcommit faaf180ec4108bacb1aba5eedc9b596dda319f9d[m
Merge: 90de8d1 22fee03
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Aug 26 10:57:31 2013 -0700

    Merge pull request #542 from vishwasbabu/mifosx-436
    
    Mifosx 436

[33mcommit 22fee03e0ae7097cece1a0b8c471f85b48bfa3a2[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Aug 26 23:25:33 2013 +0530

    adding an api workflow for transfers

[33mcommit 99bddc9f27972820d3c74e347b1c4819255ee69d[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Mon Aug 26 23:15:53 2013 +0530

    Added functionality to close a savings account

[33mcommit 90de8d1cd78a5d50d75cbafb59d064a1bd147855[m
Merge: 8dcb251 a89b2dd
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Aug 23 00:03:48 2013 -0700

    Merge pull request #539 from mifoscontributer/mifosx-547
    
    Change Api call for client transfers

[33mcommit a89b2dddd8d8d2a7f750fb4a693e67cf80d32c38[m
Author: mifoscontributer <mifos.contributer@gmail.com>
Date:   Wed Aug 21 17:02:06 2013 +0530

    Change Api call for client transfers

[33mcommit 8dcb251826ffaaa8955e7b92f27bbc5561aad9ed[m
Merge: 6d0afd2 f59f18e
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Aug 22 07:33:37 2013 -0700

    Merge pull request #538 from mifoscontributer/mifosx-613
    
    Return valid(not null) officeId for a loan

[33mcommit f59f18eaeb7d808abb7afa5364c81936cd44a9b2[m
Author: mifoscontributer <mifos.contributer@gmail.com>
Date:   Thu Aug 22 19:44:01 2013 +0530

    Fix for Retrieve group loanofficers for ASSIGN LOANOFFICER template

[33mcommit 6d0afd2b1e64e94ec2b09e95e62b79a5e8047c81[m
Merge: 81bde6d b1d2b42
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Aug 21 09:47:24 2013 -0700

    Merge pull request #535 from vishwasbabu/mifosx-436
    
    linking loan transactions to branches in preparation for inter-branch ac...

[33mcommit b1d2b42da10a29e3fecaf57aee6ae6f234dd243e[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Aug 21 21:52:43 2013 +0530

    linking loan transactions to branches in preparation for inter-branch account transfers

[33mcommit 81bde6ddad99d6014ba3e054025190d973c1660d[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Aug 21 18:45:14 2013 +0530

    Update CHANGELOG.md
    
    for 1.8.1 RELEASE

[33mcommit ba55add48897715de743132fe9ae1ba6a5573ed5[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Aug 21 18:42:23 2013 +0530

    prepping for 1.8.1 release

[33mcommit e62a279b3cb8edae7b790955d9361a9d7d7fbda7[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Aug 21 17:26:28 2013 +0530

    fix for mifosx-617

[33mcommit 8afe033f70afe8a3e1f35677d60b8fa1fcd75a1f[m
Author: mifoscontributer <mifos.contributer@gmail.com>
Date:   Wed Aug 21 12:37:27 2013 +0530

    Meeting calendar cleanup

[33mcommit bb1aa34f626c8519582d5eb13c22db6645646361[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Aug 20 20:27:35 2013 +0530

    preparing dev branch for 1.9.0 release

[33mcommit 1e4b3eb01f37af945c6026a20645b7ffe6206acb[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Aug 20 20:21:49 2013 +0530

    Update CHANGELOG.md
    
    for 1.8.0 Release

[33mcommit ffdf616bf04dc6fb5a28b1617991a5781c8a3102[m
Merge: 18c1c07 89865a0
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Aug 20 20:15:05 2013 +0530

    merging conflicts for 1.8.0 RELEASE

[33mcommit 89865a08007c5d3020a8dc99ee7817d736e647e4[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Aug 20 20:01:16 2013 +0530

    prep for 1.8.0 RELEASE

[33mcommit 4a5133fbc04f7dfd1c7a574933c6ed5c095a333f[m
Merge: 47be0d2 d563b87
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Aug 20 07:23:05 2013 -0700

    Merge pull request #532 from mifoscontributer/mifosx-547
    
    Transfer clients between groups

[33mcommit 47be0d2ed52ae5ebe65951b892196c54b18f4be5[m
Merge: 94e4d22 2727dee
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Aug 20 07:21:31 2013 -0700

    Merge pull request #533 from vishwasbabu/mifosx-436
    
    initial stub for client transfer (between branches)

[33mcommit 2727dee436c5b8e5e4b0f7004fcc374344e992ca[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Aug 20 19:47:56 2013 +0530

    initial stub for client transfer (between branches)

[33mcommit d563b87468f177a39f97211f7e06e85d238117bb[m
Author: mifoscontributer <mifos.contributer@gmail.com>
Date:   Tue Aug 20 19:40:36 2013 +0530

    Transfer clients between groups

[33mcommit 94e4d22e39ceef4dc7d9211a622c924b83713e31[m
Merge: 8c4a142 f9e001f
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Aug 20 06:29:51 2013 -0700

    Merge pull request #531 from vishwasbabu/mifosx-436
    
    Mifosx 436

[33mcommit f9e001f16b32d6950662c18bc80e120446df9156[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Aug 20 18:57:28 2013 +0530

    initial documentation stub for client transfers between groups

[33mcommit 8c4a142934d57fb86663aee9da158555d307f069[m
Merge: fd2cdef 40bbf46
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Aug 20 04:51:14 2013 -0700

    Merge pull request #530 from vishwasbabu/mifosx-436
    
    Mifosx 436

[33mcommit 40bbf46e0507368fd9c2f08d2bc5175fa9dab6b1[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Aug 20 17:00:56 2013 +0530

    initial work on transfers

[33mcommit fd2cdefb95338f672f8d70c07e002dff0f605fc8[m
Merge: 501478c 7c18496
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Aug 19 08:57:14 2013 -0700

    Merge pull request #528 from mifoscontributer/mifosx-611
    
    Add data validations for bulkloanreassignment

[33mcommit 501478c94cd14fd8ea3b27389cb69eb77e9aaf9a[m
Merge: 01a2eeb e1a4ebd
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Aug 19 08:55:50 2013 -0700

    Merge pull request #529 from pramodn02/MIFOSX-615
    
    Mifosx 615

[33mcommit e1a4ebdf9226033eac28e1d1e26621f9309a3f1c[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Mon Aug 19 18:13:17 2013 +0530

    Corrected postJournalEntries to call createJournalEntriesForLoan API

[33mcommit 7c184966203c6999569674f08cab3793ebda865f[m
Author: mifoscontributer <mifos.contributer@gmail.com>
Date:   Mon Aug 19 18:11:07 2013 +0530

    Add data validations for bulkloanreassignment

[33mcommit 01a2eeb46a1e51c61658d61676f4c4649b26c6a6[m
Merge: 500b563 8d0fbe4
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Aug 18 23:50:36 2013 -0700

    Merge pull request #527 from pramodn02/MIFOSX-605_2
    
    Mifosx 605 2

[33mcommit 8d0fbe4197ab19c77c7ccfb39b8142fd30d3277e[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Mon Aug 19 06:50:02 2013 +0530

    added cluster support for Scheduler

[33mcommit 500b56337515d26ca14bf7c8e3e6f8c207b950ac[m
Merge: 50823d0 28bbd36
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Aug 16 11:32:33 2013 +0530

    incrementing version number for client attendance patch

[33mcommit 50823d061b2dcb70e7755364160ee8ffb6189337[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Aug 15 11:05:27 2013 +0100

    MIFOSX-595: support savings account to loan account transfers for purposes of making loan repayment.

[33mcommit 28bbd361c209abe0bd73b7384a1d8cb6d65f45ad[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Wed Jul 24 10:27:27 2013 +0530

    MIFOSX-496: Capture client attendance details

[33mcommit 18c1c078170d897617e8da70796b4db00196d8f8[m
Merge: 79aba4b a85cee8
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Aug 14 03:28:34 2013 -0700

    Merge pull request #525 from openMF/mifosplatform-1.7.2
    
    updating release in gradle.properties

[33mcommit a85cee87648907e9ada1493b4ad5cf46a09e348d[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Aug 14 15:14:18 2013 +0530

    updating release in gradle.properties

[33mcommit 79aba4b49583405452ac3df541ab65c94cbb1301[m
Merge: 70463fc 86c5746
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Aug 13 23:29:59 2013 -0700

    Merge pull request #523 from openMF/mifosplatform-1.7.2
    
    Mifosplatform 1.7.2

[33mcommit 86c57464d6663c8e9f1745c5a86e19ecea68c742[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Aug 14 11:58:56 2013 +0530

    Update CHANGELOG.md
    
    for 1.7.2 release

[33mcommit 8ca7c66ec4cecff7714310b160c91649749d19ad[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Aug 14 11:50:52 2013 +0530

    only interested in picking up the fix for http://tinyurl.com/oazjwq7 from 662d26771ed4f020276163574932a57cf9087829

[33mcommit 1dc9bc50099230da541d58202778894d2de91498[m
Author: mifoscontributer <mifos.contributer@gmail.com>
Date:   Mon Aug 12 17:48:20 2013 +0530

    Add basic saving summary reports for groups

[33mcommit eee431d3b00330bb8d664da3b231fbfff7b8fb1b[m
Merge: f01ebf3 662d267
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Aug 13 06:22:31 2013 -0700

    Merge pull request #520 from mifoscontributer/mifosx-599
    
    Add basic saving summary reports for groups

[33mcommit f01ebf3cb2f2b3b787152e58901971a94bd4a3d8[m
Merge: f4e0d10 9956e38
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Aug 13 03:34:16 2013 -0700

    Merge pull request #522 from pramodn02/MIFOSX-433_3
    
    Mifosx 433 3

[33mcommit 9956e387ce877723b6c81330f040c404bafa95bc[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Tue Aug 13 15:52:05 2013 +0530

    corrected typo errors

[33mcommit f4e0d104a63704f3de470fc52e3e6742c1fbd205[m
Merge: a0b2feb a62b328
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Aug 13 02:34:17 2013 -0700

    Merge pull request #521 from pramodn02/MIFOSX-433_2
    
    Mifosx 433 2

[33mcommit a62b32833c6ba4451dda12b3671eb517530248e4[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Tue Aug 13 14:59:39 2013 +0530

    changed the rounding value while adjusting amounts

[33mcommit a0b2feb8729b85861cb8f865f012b4df0766fb3c[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Aug 12 22:42:32 2013 +0530

    dev branch prepped for 1.8.0 release

[33mcommit a2764237a2802fe859940d03695cd5d53091e7f7[m
Merge: 5df9b37 70463fc
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Aug 12 22:40:43 2013 +0530

    Merge remote-tracking branch 'upstream/master' into develop

[33mcommit 70463fc52b5bea9459f17d3d5805cbf60447a29f[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Aug 12 22:39:05 2013 +0530

    Update CHANGELOG.md
    
    updates for 1.7.1 release

[33mcommit 5df9b37c69f490b8f23a94679319aab5ad50bed7[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Aug 12 22:26:04 2013 +0530

    cleaning up sql file numbers

[33mcommit 1ff206b409c6be1cb7096aac416a16c008811ec1[m
Merge: 5d8cd54 b2e2fdf
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Aug 12 22:25:24 2013 +0530

    merging 1.7.1 release into develop branch

[33mcommit b2e2fdf8f1457eb05ac207c826c40ab449a7483d[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Aug 12 21:51:39 2013 +0530

    updating to 1.7.1 Release

[33mcommit 32ba28031e58effb333f469d31dce2da993180b3[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Aug 12 21:24:25 2013 +0530

    flyway enable out of order migrations

[33mcommit 5d8cd54dfbc57b9883f0184ed5f861be6b35bb4c[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Aug 12 15:23:36 2013 +0100

    MIFOSX-601: fix issue with negative interest been calculated due to incorrect calculation of end of day balance.

[33mcommit 662d26771ed4f020276163574932a57cf9087829[m
Author: mifoscontributer <mifos.contributer@gmail.com>
Date:   Mon Aug 12 17:48:20 2013 +0530

    Add basic saving summary reports for groups

[33mcommit ef5c040a9e2b021c87f1901f72befed7e9cd0260[m
Merge: 5ffafde 2482978
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Aug 12 00:12:48 2013 -0700

    Merge pull request #518 from pramodn02/MIFOSX-433
    
    Mifosx 433

[33mcommit fa8de862f4e84717b8d927a42594a41c29f61bd9[m
Merge: f1c2c2c 57eecb4
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Aug 11 23:36:45 2013 -0700

    Merge pull request #516 from mifoscontributer/mifosx-597
    
    Validation check for loans and savings for client or group status

[33mcommit 24829781513b431b091abdbe8381c944578bc70f[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Fri Aug 9 17:17:49 2013 +0530

    added rounding to multiples of a decimal
    
    modifications as per the comments

[33mcommit 57eecb4b1b763cfa21d59e58c80f3af52bfca446[m
Author: mifoscontributer <mifos.contributer@gmail.com>
Date:   Fri Aug 9 13:39:27 2013 +0530

    Validation check for loans and savings for client or group status

[33mcommit 5ffafdef08046b746b106d430bcf774ad7d000cd[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Aug 8 16:26:15 2013 +0530

    fixing issues with query for retrieveAccountsWithAnnualFeeDue

[33mcommit ef8600b8be6ed1f5f0d0c65d361cd23a6cf21014[m
Merge: f260591 9e6108c
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Aug 8 03:01:25 2013 -0700

    Merge pull request #514 from pramodn02/MIFOSX-586
    
    added savings transaction adjustment API

[33mcommit 9e6108c11edbb4cafdc5867170758cc4893e285d[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Thu Aug 8 11:56:14 2013 +0530

    added savings transaction adjustment API
    
    added Interest posting for undo transactions
    
    rebase changes

[33mcommit f260591ea955700c82241619d92185377699ec0a[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Aug 8 00:17:54 2013 +0100

    remove invalid suppression of warnings which were shown as warnings in eclipse indigo but fixed in eclipse juno and kepler.

[33mcommit cc684e02c377571afbf53b51683096e19b0a836b[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Aug 7 23:58:30 2013 +0100

    MIFOSX-432: ability to transfer from savings account to savings account.

[33mcommit f1c2c2c342d4f768b5a0f6179c609b00a487dd20[m
Merge: 2b13948 94e7118
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Aug 6 23:14:16 2013 -0700

    Merge pull request #513 from mifoscontributer/mifosx-554
    
    MIFOSX-554 Adjust repaymentdate for frequent meeting changes

[33mcommit 94e711873861d4149ca102d973d19e7c3b6b1362[m
Author: mifoscontributer <mifos.contributer@gmail.com>
Date:   Wed Aug 7 10:39:52 2013 +0530

    MIFOSX-554 Adjust repaymentdate for frequent meeting changes

[33mcommit d0cbf417cf39035b04554f94ba66e1188e935ba5[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Aug 6 19:26:03 2013 +0100

    removed unused warnings.

[33mcommit af3ea46a38de795920fc7ba4e9ddf2734c9122dd[m
Merge: 6d46b4a 610efe1
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Aug 6 10:10:56 2013 -0700

    Merge pull request #512 from pramodn02/MIFOSX-159
    
    Mifosx 159

[33mcommit 6d46b4a3835c768288e96228caee9feb531168c7[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Aug 6 22:37:00 2013 +0530

    fixing issue with query for savings account annual fee mapper

[33mcommit 610efe12d3ade2a28cf5c8506ad9fae78f181c31[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Tue Aug 6 21:57:24 2013 +0530

    batch job for post Interest
    
    rebase corrections

[33mcommit 62ef2338f0e0f8593e9aba5e6fd2b04b7cacaa9e[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Aug 6 21:08:11 2013 +0530

    incrementing version number of sql fix pulled in from bug fix branch to develop branch

[33mcommit dc38cb7e0b480244b2aecb2ee61b647cbe1d6ad8[m
Author: mifoscontributer <mifos.contributer@gmail.com>
Date:   Tue Aug 6 12:38:41 2013 +0530

    merging 327880473293c07fb82be9363f55d29547cdf9f9 into develop branch

[33mcommit 63610b3c50750b5a6ffdb2618246eb3f4da2623a[m
Author: Raman Kansal <ramankan@thoughtworks.com>
Date:   Tue Aug 6 09:33:12 2013 +0530

    Raman - Added Group Loan test

[33mcommit 2b13948cfecccd395f66f8695ab334b49c027cc3[m
Merge: f01a46a 3278804
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Aug 6 01:01:24 2013 -0700

    Merge pull request #511 from mifoscontributer/mifosx-589
    
    Add configuration settings for holidays and non_workingdays

[33mcommit 4223ad80b5d8b31576c866eceb5a329add450f56[m
Merge: 1320ef5 1fdf726
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Aug 6 01:00:10 2013 -0700

    Merge pull request #510 from pramodn02/MIFOSX-575
    
    Mifosx 575

[33mcommit 327880473293c07fb82be9363f55d29547cdf9f9[m
Author: mifoscontributer <mifos.contributer@gmail.com>
Date:   Tue Aug 6 12:38:41 2013 +0530

    Add configuration settings for holidays and non_workingdays

[33mcommit 1fdf72679a11bb47501d321af3c4f91141fb7268[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Tue Aug 6 12:13:52 2013 +0530

    added modifications for creating group and JLG savings account

[33mcommit 1320ef5080ab979ceb85b3397d77eceefee97748[m
Author: mifoscontributer <mifos.contributer@gmail.com>
Date:   Mon Aug 5 15:37:30 2013 +0530

    MIFSOX-428 Api fix for edit user

[33mcommit 49c968da4affdc5775fd7aff865ac2fef2c40ee0[m
Author: mifoscontributer <mifos.contributer@gmail.com>
Date:   Mon Aug 5 13:03:14 2013 +0530

    MIFOSX-589 check edit transaction transactiondate for holiday or nonworking day

[33mcommit f01a46a93075680f815d82aff71fff51d1c661f8[m
Merge: 92ec82d 6f5e79b
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Aug 5 04:09:56 2013 -0700

    Merge pull request #506 from mifoscontributer/mifosx-589
    
    MIFOSX-589 check edit transaction transactiondate for holiday

[33mcommit a808b897f2b6075be8dd20f6db9cd13ece6f7741[m
Merge: 0ca3928 ea36625
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon Aug 5 03:40:43 2013 -0700

    Merge pull request #508 from johnw65/externalIds
    
    mifosx-567 fix externalId defaulting to blank string if not passed as a ...

[33mcommit ea3662519b73a61342813df856bd1367ddf5ca4c[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon Aug 5 11:37:09 2013 +0100

    mifosx-567 fix externalId defaulting to blank string if not passed as a parameter

[33mcommit 92ec82df74f2fd8931daf07b1509f2f5174c60b9[m
Merge: dfe418b 13c5c5c
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Aug 5 03:25:28 2013 -0700

    Merge pull request #507 from mifoscontributer/mifosx-428_1
    
    MIFSOX-428 Api fix for edit user

[33mcommit 13c5c5c050bf155c6b401a9843ec30990052baf5[m
Author: mifoscontributer <mifos.contributer@gmail.com>
Date:   Mon Aug 5 15:37:30 2013 +0530

    MIFSOX-428 Api fix for edit user

[33mcommit 6f5e79b387ffacde90c68112ba88f98e4918ae20[m
Author: mifoscontributer <mifos.contributer@gmail.com>
Date:   Mon Aug 5 13:03:14 2013 +0530

    MIFOSX-589 check edit transaction transactiondate for holiday or nonworking day

[33mcommit 0ca3928728cf5138714a029fbb169f898ac31e74[m
Merge: 1a50364 d193fcf
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Aug 5 00:27:13 2013 -0700

    Merge pull request #505 from pramodn02/MIFOSX-591
    
    Mifosx 591

[33mcommit d193fcf35f9c36d4ec82c0380102a07bfcb3f4f4[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Mon Aug 5 11:24:59 2013 +0530

    group retrive accountdetails API changes

[33mcommit 1a50364ebc51a2a57cbaaebe682d4264ae939cb8[m
Merge: e2a76bb 9d69ed1
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sat Aug 3 06:57:19 2013 -0700

    Merge pull request #504 from mifoscontributer/MIFOSX-310
    
    Null checks for collection sheet get data

[33mcommit 9d69ed11a9cbd939bae00f33c7a5b6a26af42ad0[m
Author: mifoscontributer <mifos.contributer@gmail.com>
Date:   Sat Aug 3 19:21:41 2013 +0530

    Null checks for collection sheet get data

[33mcommit e2a76bbd0e3ba673ab6229f66df7bca96c01771f[m
Author: Raman Kansal <ramankan@thoughtworks.com>
Date:   Fri Aug 2 23:18:57 2013 +0530

    Raman - Fixed defect for java 1.6.

[33mcommit cdb4e18be8eaf4e856b95921d7667a694e1c1b9c[m
Merge: e6a30e2 b3f1123
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri Aug 2 12:09:51 2013 -0700

    Merge pull request #503 from johnw65/externalIds
    
    mifosx-567 add support for externalId for loan transactions

[33mcommit b3f11233cd2245d61f8e051dd6f80efede4dc77a[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri Aug 2 20:03:48 2013 +0100

    mifosx-567 add support for externalId for loan transactions

[33mcommit e6a30e2b58185f14d9f0a407dfc43502f82e5e54[m
Merge: c1aa16c b6a164c
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Aug 1 11:18:15 2013 -0700

    Merge pull request #502 from johnw65/externalIds
    
    mifosx-567  allow post and put for externalId for loanproduct

[33mcommit b6a164c8c2287c767ba0d73fc08dbdbefb8936f9[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Aug 1 19:15:28 2013 +0100

    mifosx-567  allow post and put for externalId for loanproduct

[33mcommit c1aa16c7546f2e7311333c74080981d84c034232[m
Merge: 627a754 0da7dab
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Aug 1 06:41:46 2013 -0700

    Merge pull request #501 from johnw65/externalIds
    
    mifosx-567 ensure duplicate externalId for staff and loan generate valid ...

[33mcommit 0da7dabf0cb8865d1e38c6b543752b034c472992[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Aug 1 14:33:37 2013 +0100

    mifosx-567 ensure duplicate externalId for staff and loan general valid json error response

[33mcommit 627a75426215395530124232178653a6c2fe7570[m
Merge: ce93ec6 425f5f3
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Aug 1 05:50:54 2013 -0700

    Merge pull request #500 from goutham-M/mifosx-357
    
     assign staff feature added  for group/center

[33mcommit 425f5f382269130b875e4b9ca865fa9286f1026e[m
Author: goutham-M <goutham@confluxtechnologies.com>
Date:   Thu Aug 1 17:51:39 2013 +0530

    assign staff for group/center(improvement)

[33mcommit ce93ec677e3868386a933f2f4796c632ce6ccc88[m
Author: mifoscontributer <mifos.contributer@gmail.com>
Date:   Tue Jul 30 16:13:31 2013 +0530

    Validating Expected disbursal date for holiday or nonworking day

[33mcommit 55af36dec0a0fc0bd0a25b87e0b70f80897e6f2b[m
Author: mifoscontributer <mifos.contributer@gmail.com>
Date:   Thu Aug 1 12:23:00 2013 +0530

    mifosx-538 global search functionality query fix

[33mcommit 194b00665f7890c49688ade8dad9b537535a18b3[m
Author: mifoscontributer <mifos.contributer@gmail.com>
Date:   Wed Jul 31 18:33:31 2013 +0530

    Retrieve writeoff date for writtenoff loan

[33mcommit c0f780b398eec3d6959822db491b72e3431f3cfe[m
Author: Raman Kansal <ramankan@thoughtworks.com>
Date:   Wed Jul 31 10:58:32 2013 +0530

    Added Group Tests for Creation, Activation, Client Association, Updating & Deletion.

[33mcommit fff9cba28f1b88a10b0a274f996b26d050b0c9ce[m
Author: Raman Kansal <ramankan@thoughtworks.com>
Date:   Mon Jul 29 23:08:56 2013 +0530

    Added Group Tests for Creation, Activation, Client Association, Updating & Deletion.

[33mcommit dfe418b495b00992f96911f914290753be2929f5[m
Merge: c7be072 3334537
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Aug 1 04:46:34 2013 -0700

    Merge pull request #497 from mifoscontributer/MIFOSX-555
    
    Retrieve writeoff date for writtenoff loan

[33mcommit c7be07271c46dd22a9f868acd2c4d5e233ec2212[m
Merge: e8975d9 538039e
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Aug 1 04:18:03 2013 -0700

    Merge pull request #499 from mifoscontributer/MIFOSX-538
    
    mifosx-538 global search functionality query fix

[33mcommit 538039e9bdf05e4a321aba965ef440ab278e2c1a[m
Author: mifoscontributer <mifos.contributer@gmail.com>
Date:   Thu Aug 1 12:23:00 2013 +0530

    mifosx-538 global search functionality query fix

[33mcommit 5669ccfc4ba71632a83ebfd11b11e2a45da4b2bc[m
Merge: 111175d 872d13a
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed Jul 31 09:38:04 2013 -0700

    Merge pull request #498 from johnw65/externalIds
    
    mifosx-567 externalId updates (this for staff)

[33mcommit 872d13a6ab85acdd7cb49760307be3cc49fb9e7e[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed Jul 31 17:33:40 2013 +0100

    mifosx-567 externalId updates (this for staff)

[33mcommit 33345378d4d00834f6d76fd14213615268653c62[m
Author: mifoscontributer <mifos.contributer@gmail.com>
Date:   Wed Jul 31 18:33:31 2013 +0530

    Retrieve writeoff date for writtenoff loan

[33mcommit e8975d93f4970093f932bf6d36c93e03866b2834[m
Merge: 6a5b2b1 328f225
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Jul 30 06:51:13 2013 -0700

    Merge pull request #495 from mifoscontributer/MIFOSX-543_3
    
    Validating Expected disbursal date for holiday or nonworking day

[33mcommit 328f225072b663aba3a336a6beeffb7d381438a2[m
Author: mifoscontributer <mifos.contributer@gmail.com>
Date:   Tue Jul 30 16:13:31 2013 +0530

    Validating Expected disbursal date for holiday or nonworking day

[33mcommit 111175df91846db89b41c798630957593d863a59[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Mon Jul 29 11:22:29 2013 +0530

    added condition to call scheduler's standby

[33mcommit 6a5b2b18b9f410cb9b852112f3c6361ee4c440fe[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Mon Jul 29 11:22:29 2013 +0530

    added condition to call scheduler's standby

[33mcommit f34b54eddb74abada7bf181cc4fa39213832c6d5[m
Merge: c5944b3 f6ba4cc
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Jul 29 03:07:25 2013 -0700

    Merge pull request #493 from Nayan/MIFOSX-428
    
    Minor fix for MIFOSX-428

[33mcommit f6ba4cc93055c05e6e9803e2504099cffed26d0f[m
Author: Nayan Ambali <nayan.ambali@gmail.com>
Date:   Mon Jul 29 14:08:34 2013 +0530

    minor change

[33mcommit c5944b34349d405d716a3c968ec0f69cee15cdac[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sun Jul 28 20:22:13 2013 +0100

    update properties to dev branch properties.

[33mcommit 91b3f25d047a16724b82107e1a2e50cc42124a6c[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Jul 28 22:05:26 2013 +0530

    Update CHANGELOG.md
    
    for 1.7.0 RELEASE

[33mcommit bd308d00bf0e362c0755fccb363669d480c1db35[m
Merge: e0bb9e5 9b5c63e
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Jul 28 22:00:18 2013 +0530

    mergeing 1.6.1 release into develop branch

[33mcommit 9b5c63e244ded7065a20415aecfcfeaea71d04e4[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Jul 28 21:55:09 2013 +0530

    updating to 1.6.1 release

[33mcommit d28d8df6f67728864310f8cbaa0af44abd860c4c[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Jul 28 21:45:01 2013 +0530

    moving up to 1.6.1 release

[33mcommit 72de6ea83cc3cc4e6af88725fc002562a5d83ed8[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Jul 28 21:43:16 2013 +0530

    Update CHANGELOG.md
    
    for 1.6.1 RELEASE

[33mcommit e0bb9e58681a64ccc1090932064a69dc406744cc[m
Merge: ed7ac1b 19428d6
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Jul 28 08:33:37 2013 -0700

    Merge pull request #491 from Nayan/MIFOSX-428CP
    
    Update for 'set new password' during user creation

[33mcommit b8d6c05d8968bfe22dd9a541ad127b4babeaf0a1[m
Merge: 0a312b6 0943830
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Jul 28 08:32:10 2013 -0700

    Merge pull request #489 from goutham-M/mifosx-558
    
    validation on submit date

[33mcommit 19428d63a6bda63904dea104ad99b07a31bf5ab2[m
Author: Nayan Ambali <nayan.ambali@gmail.com>
Date:   Sun Jul 28 16:23:36 2013 +0530

    update for options to set new password

[33mcommit ed7ac1b6aee0388fa6173037a3a8ab6b1bb66fa0[m
Merge: 7eea893 a2c093e
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sat Jul 27 00:59:13 2013 -0700

    Merge pull request #490 from goutham-M/scheduler-api-updation
    
    Scheduler api updation

[33mcommit a2c093eb4c8fc29cc7c75307a41d670c013c61a3[m
Author: goutham-M <goutham@confluxtechnologies.com>
Date:   Fri Jul 26 21:02:07 2013 +0530

    for scheduler jobs apiLive.htm updation

[33mcommit 0943830da5c8ca97c9691c3c066ee7d4d363eb52[m
Author: goutham-M <goutham@confluxtechnologies.com>
Date:   Fri Jul 26 17:14:04 2013 +0530

    validation on submit date

[33mcommit 7eea89393dbbdfdebb21153b3ea71818ffb4bd9d[m
Merge: 5b21395 272ff9e
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Jul 25 22:45:10 2013 -0700

    Merge pull request #487 from pramodn02/MIFOSX-506_77
    
     Added cron expression in response object of jobs

[33mcommit 272ff9eba9d0df9a0ffa8df6839e72b3b6c43d53[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Fri Jul 26 10:55:20 2013 +0530

     Added cron expression in response object of jobs

[33mcommit 5b21395bc2c0d239315a6dfb715e6f401d4c2899[m
Merge: cb57df8 1cb64e0
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Jul 25 07:05:44 2013 -0700

    Merge pull request #486 from pramodn02/MIFOSX-506_7
    
    added scheduler suspend  API

[33mcommit 1cb64e0cde6a3659bccdf09db1b3ac71b8a57aa1[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Thu Jul 25 19:00:44 2013 +0530

    changes to run jobs from application

[33mcommit ce95c68aa27e9189ebdccd704716bb14afaa22ae[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Thu Jul 25 15:36:29 2013 +0530

    added scheduler stop API

[33mcommit cb57df866cbaf059cbf83ce5c9c3b0d500715bd3[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Jul 25 14:27:23 2013 +0100

    MIFOSX-565: written off loans reverse their write off transaction when undoing or adjusting transaction on closed-written off loan, closed-rescheduled loans switch to active when transactions is undone.

[33mcommit b32d51033bc626ed89c348d87c399881a51448d8[m
Merge: d0c7718 b57e030
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Jul 25 04:37:00 2013 -0700

    Merge pull request #483 from mifoscontributer/MIFOSX-506
    
    SQL update for fetch job history details

[33mcommit b57e030462b7ef8f0761b6ffe5394a8c42c698ea[m
Author: mifoscontributer <mifos.contributer@gmail.com>
Date:   Thu Jul 25 16:54:11 2013 +0530

    SQL update for fetch job history details

[33mcommit d0c7718eae3025fc8612b1867b2ac9fbd3a6a1a4[m
Author: goutham-M <goutham@confluxtechnologies.com>
Date:   Thu Jul 25 13:13:36 2013 +0530

    data table not able to create when column/roz size is large

[33mcommit 54e39c939e2e06315faa50922babb91b9bd10998[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Jul 25 11:43:03 2013 +0100

    MIFOSX-566: ensure all types of debits and credits are taken into account when validating if transaction causes account balance to go negative.

[33mcommit 0a312b62864db3384560743c7ad69cbd06fa4e42[m
Merge: f2fac3a 3e43f39
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Jul 25 02:49:28 2013 -0700

    Merge pull request #482 from goutham-M/mifosx-564
    
    data table not able to create when column/row size is large

[33mcommit 3e43f398952a328a4ee474d997f7bed7d28a2e66[m
Author: goutham-M <goutham@confluxtechnologies.com>
Date:   Thu Jul 25 13:13:36 2013 +0530

    data table not able to create when column/roz size is large

[33mcommit 121f945af56acf64f3030a65b61fcb417371f602[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Jul 25 01:40:33 2013 +0100

    MIFOSX-565: allow transactions on any type of closed loan to be adjusted or undone resulting in loan becoming active again and allowing transaction to be made against it.

[33mcommit b48bc6269178fe8936808c19fc92fe7613edf550[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Jul 25 00:57:25 2013 +0100

    MIFOSX-397: support apply of annual fees on savings account through manual and scheduled job approach.

[33mcommit 4b392a830c34a7f9f7afb0663f044175906b87fa[m
Merge: 9fe16ec 4452b45
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Jul 24 06:15:13 2013 -0700

    Merge pull request #480 from Nayan/MIFOSX-563
    
    Added repayment strategy to "get loan api"

[33mcommit 9fe16ecb11d6b4e2e5657ebbb61518a14c40d415[m
Merge: eb6e85e b12d755
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Jul 24 05:56:08 2013 -0700

    Merge pull request #476 from goutham-M/mifosx-470
    
    Avoiding Deletion of active Charge which was already assigned to a Loanproduct.

[33mcommit eb6e85eb99d3fdc1ee4effc567e01ba903513d5a[m
Author: goutham-M <goutham@confluxtechnologies.com>
Date:   Wed Jul 24 17:38:40 2013 +0530

    calendarId will be passed only when syn repay/disbur checkbox selected

[33mcommit f2fac3ad12746f953468a3b1b3a111042ae94cc2[m
Author: goutham-M <goutham@confluxtechnologies.com>
Date:   Wed Jul 24 17:38:40 2013 +0530

    calendarId will be passed only when syn repay/disbur checkbox selected

[33mcommit 4452b4542554d176c2b0c41e0d984d32b3bb7be7[m
Author: Nayan Ambali <nayan.ambali@gmail.com>
Date:   Wed Jul 24 18:03:29 2013 +0530

    added repayment strategy to get loan api

[33mcommit 7f4c1b6ea58a5e4d8de6b768d80e6c832e304f22[m
Merge: 2f04750 c2a9da9
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Jul 24 05:09:47 2013 -0700

    Merge pull request #478 from mifoscontributer/MIFOSX-495
    
    Core reports required for client summary

[33mcommit c2a9da951a377cea5a92347d1ef4431caf78113a[m
Author: mifoscontributer <mifos.contributer@gmail.com>
Date:   Wed Jul 24 17:27:17 2013 +0530

    Core reports required for client summary

[33mcommit 2f0475065084285da3ac67238645bcf4893dd727[m
Merge: 8fcdd34 62b78c1
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Jul 24 01:35:26 2013 -0700

    Merge pull request #477 from pramodn02/MIFOSX-506_7
    
    changed exception type to service unavilable

[33mcommit 62b78c122cd5d12f2da29a6e0b80b1acbd21b921[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Wed Jul 24 12:42:57 2013 +0530

    changed exception type to service unavilable

[33mcommit b12d7551e5eed9338f00df59ee838119e03afd23[m
Author: goutham-M <goutham@confluxtechnologies.com>
Date:   Tue Jul 23 15:29:17 2013 +0530

    Avoiding Deletion of active Charge which was already assigned to a Loan product.

[33mcommit 8fcdd3443f46fed5deccd365f7a68029d246a94a[m
Merge: b9efbb5 43fe726
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Jul 23 21:56:53 2013 -0700

    Merge pull request #470 from goutham-M/mifosx-548
    
    A holiday's Reschedule Repayment to date should not be a non-working day

[33mcommit b9efbb55809dface775b248433b8f4aee2f2c98f[m
Merge: 8d58cb8 5fd8372
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Jul 23 21:22:11 2013 -0700

    Merge pull request #475 from pramodn02/MIFOSX-506_77
    
    added scheduler groups for dependent jobs

[33mcommit 5fd83722ed866acfffc1d91ff4be4b1b3e809a29[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Wed Jul 24 09:19:59 2013 +0530

    added scheduler groups for dependent jobs

[33mcommit 8d58cb8a8452e6fe1d0178fff324ad7ff3a514e9[m
Merge: 3324e23 e782a8b
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Jul 23 04:57:40 2013 -0700

    Merge pull request #469 from mifoscontributer/MIFOSX-478
    
    Applying Productmix check for loans

[33mcommit 3324e2318c3fdc51d8f11927c216f71ce0fcc863[m
Merge: 7ee6a22 e3c6c84
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Jul 23 04:35:42 2013 -0700

    Merge pull request #471 from pramodn02/MIFOSX-506_7
    
    added update constrain while job execution in propcess

[33mcommit e3c6c84c3e77191f107bef8eb8d77322d30b6a2d[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Mon Jul 22 16:54:50 2013 +0530

    added update constrain while job execution in propcess
    
    added column for update constrain in jobs table

[33mcommit 43fe7265f5c0b6d009b0d70ede5d9639201b4139[m
Author: goutham-M <goutham@confluxtechnologies.com>
Date:   Mon Jul 22 15:43:01 2013 +0530

    A holiday's Reschedule Repayment to date should not be a non-working day

[33mcommit e782a8b416e467119fa98ed96a31c158e1de876a[m
Author: mifoscontributer <mifos.contributer@gmail.com>
Date:   Mon Jul 22 14:01:05 2013 +0530

    Applying Productmix check for loans

[33mcommit 7ee6a22df56a6bb150f26f472d80e5c0ca40713e[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sat Jul 20 21:19:36 2013 +0530

    develop branch updated to 1.7.0.DEVELOP

[33mcommit fca10c9c5b7788c4134b11a00e8496420e854905[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sat Jul 20 21:14:57 2013 +0530

    Update CHANGELOG.md
    
    for 1.6.0 RELEASE

[33mcommit 221e3439ad9f7a103072ace9c499edc8ae795554[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sat Jul 20 21:03:38 2013 +0530

    updating to 1.6.0 RELEASE

[33mcommit 583fbf8459218fcb25de1f91d8606eb7d44fa8d4[m
Merge: 20908a8 874d684
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sat Jul 20 03:19:59 2013 -0700

    Merge pull request #468 from goutham-M/mifosx-558
    
    modify loan application submit on date after the product close date is getting created(fixed)

[33mcommit 874d6845d0c126519254d5587a232da64141ec40[m
Author: goutham-M <goutham@confluxtechnologies.com>
Date:   Sat Jul 20 12:56:43 2013 +0530

    loan application submit on date after the product close date is getting created

[33mcommit 20908a80ac9a961fb8c57fd0755812ba783d5665[m
Merge: ef57e71 bc0323b
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Jul 19 08:09:33 2013 -0700

    Merge pull request #467 from vishwasbabu/pramodn02-MIFOSX-506_77
    
    Pramodn02 mifosx 506 77

[33mcommit bc0323be913fff1a645a559e128143fa30e59cab[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Jul 19 20:34:22 2013 +0530

    refactoring scheduler api names and adding TODO's to be consistent with the rest of the application

[33mcommit ef57e7167b1891e99328db9ae72516ffa6278b46[m
Merge: 722f113 c17364a
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Jul 19 06:49:32 2013 -0700

    Merge pull request #466 from goutham-M/mifosx-550
    
    view holidays(improvement)

[33mcommit c17364a897521f2e007f81c9ebcd98453577f4c7[m
Author: goutham-M <goutham@confluxtechnologies.com>
Date:   Fri Jul 19 19:10:35 2013 +0530

    view holidays(improvement)

[33mcommit f1a74827bfd830311acd29402afd7399e35cba38[m
Merge: 722f113 fffa2fa
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Jul 19 19:00:34 2013 +0530

    merging in pramods changes for scheduler update functionality...

[33mcommit 722f11354f9c54cbe916b5d547955aa9bff4d436[m
Merge: 693172d c9e3e06
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Jul 19 01:36:33 2013 -0700

    Merge pull request #463 from ashok-conflux/MIFOSX-489
    
    do not display inactive clients for creating new JLG loans

[33mcommit 693172d07f6b3eabf7ce3577f781e3d669f7c869[m
Merge: a46ebaa 546400c
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Jul 19 01:35:56 2013 -0700

    Merge pull request #462 from ashok-conflux/MIFOSX-551
    
    Fixed MIFOSX-551 issue

[33mcommit a46ebaa001cbaba66584a536a302f432aa3a35c8[m
Merge: 4b01f3f aae2e50
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Jul 19 01:35:10 2013 -0700

    Merge pull request #465 from vishwasbabu/ashok-conflux-MIFOSX-543-TestCase
    
    Ashok conflux mifosx 543 test case

[33mcommit aae2e50acda8e92b9f1d52f9b77578e4e461c1e3[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Jul 19 14:03:27 2013 +0530

    incrementing seq number for updating working days patch

[33mcommit d5498b92ae3609c83e7829e6efb4dadcf502510f[m
Merge: 4b01f3f a8fe964
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Jul 19 14:01:45 2013 +0530

    Merge branch 'MIFOSX-543-TestCase' of git://github.com/ashok-conflux/mifosx into ashok-conflux-MIFOSX-543-TestCase

[33mcommit 4b01f3f6699bd758908c53bf978189d45b11eb4b[m
Merge: 666362f 6f63a33
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Jul 19 01:28:26 2013 -0700

    Merge pull request #464 from vishwasbabu/mifoscontributer-MIFOSX-478
    
    Mifoscontributer mifosx 478

[33mcommit 6f63a33ef406a0a034c9874030e35cb5c15cc92b[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Jul 19 13:54:15 2013 +0530

    incrementing version number of product mix db patch

[33mcommit ce971b246e7bbacd1568d147deb063d8531e4e5f[m
Merge: 666362f 051e037
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Jul 19 13:52:16 2013 +0530

    Merge branch 'MIFOSX-478' of git://github.com/mifoscontributer/mifosx into mifoscontributer-MIFOSX-478

[33mcommit c9e3e06e17d4d5d1fab1c792e3452cae7d6d0ef4[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Fri Jul 19 13:40:56 2013 +0530

    do not display inactive clients for creating new JLG loans

[33mcommit 546400c387b91ae7e143674785b492272b654931[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Fri Jul 19 11:01:07 2013 +0530

    Fixed MIFOSX-551 issue

[33mcommit 666362f1591b816c3e369573faa871a8963b64d6[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Jul 18 19:57:14 2013 +0100

    update api docs.

[33mcommit 26bed02096e81e0b1494917856c2f8a9d53d376c[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Jul 18 19:49:47 2013 +0100

    refactor client accounts to be returned as list of accounts broken up into loans and savings rather than in collections by status.

[33mcommit a3dc8ff378d34fea810592c426460e9160fb2f6d[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Jul 18 16:53:30 2013 +0100

    support ability to undo transactions on savings accounts.

[33mcommit 051e0372f55659b18fcb3e245416feb20a34b98c[m
Author: mifoscontributer <mifos.contributer@gmail.com>
Date:   Tue Jul 16 19:17:56 2013 +0530

    Product Mix Functionality
    
    Updating Api Docs

[33mcommit fffa2fa95228f3e502a233a4b8cb0a3c2b9edd79[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Thu Jul 18 10:57:09 2013 +0530

    Scheduler changes is a combination of 2 commits.
    
    update API changes for scheduler jobs
    
    added scheduler job update API
    
    added pagination for job history

[33mcommit a8fe964fad96c7aeef8e9998252a5f3408d773b3[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Thu Jul 18 13:35:06 2013 +0530

    update all week days as working days

[33mcommit 12abc17a8807e658a8b29e94d62d3599fd4527e9[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Jul 17 14:11:52 2013 +0100

    tidy up validation of savings accounts.

[33mcommit 28f0c80b848a27108d9080a72ceae29737cd4d0c[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Jul 17 12:45:09 2013 +0100

    MIFOSX-406: fix application modification and update api docs for application process areas.

[33mcommit 5b1b917d0405ee084bb37470ce2c6d18233670ea[m
Merge: 2c42d92 dcad395
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Jul 17 04:02:53 2013 -0700

    Merge pull request #457 from ashok-conflux/MIFOSX-543
    
    MIFOSX-543: Implement working days for Loans

[33mcommit dcad3955e19acc4760829367ce48e7b885f1c762[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Thu Jul 11 18:07:25 2013 +0530

    MIFOSX-543: Implement working days for Loans

[33mcommit 2c42d926cc5d1915b6ea4ff6f77f1921227b1321[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Jul 16 17:59:22 2013 +0100

    harden up validation around savings application process.

[33mcommit 49ac64020a61ddf77da8840e270f8243d8198df8[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Jul 16 11:58:23 2013 +0100

    retrieve field officers in template for retrieve one.

[33mcommit 6f559533849bfb191ee8724c35a7cdd552b5dd17[m
Merge: ad51a6b e4c6040
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Jul 16 03:31:11 2013 -0700

    Merge pull request #455 from pramodn02/MIFOSX-506_7
    
    scheduler changes for API

[33mcommit e4c604004cde8cb853507d1980561d860f7bf2bb[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Tue Jul 16 14:54:46 2013 +0530

    scheduler changes for API

[33mcommit ad51a6b62fefe9025a8a7055b46c9b74c96bc9dc[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Jul 16 00:43:34 2013 +0100

    MIFOSX-541: add application process around savings accounts.

[33mcommit ee223dab61ad4f09ffce95cf3134696146eac606[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sun Jul 14 22:47:12 2013 +0100

    move api versions to develop versions for next release. suppress unused warnings.

[33mcommit c6fc2aa1cef00aef67525830d2be4d6b952b7600[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Jul 14 20:08:11 2013 +0530

    Update CHANGELOG.md
    
    for 1.5.0.RELEASE

[33mcommit d17e7836174a6d2cc88c135dd5e416c0a389c530[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Jul 14 19:36:13 2013 +0530

    update release to 1.5.0 in gradle.properties

[33mcommit 5237061ce0d82c7c1126629da71e1a873f80c80c[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Jul 14 19:05:27 2013 +0530

    updating release to 1.5.0 in api docs

[33mcommit 4387d25559eb8d3a5fd24eb4d8aca713f81d17b9[m
Merge: f37b2ea 313f8c5
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Jul 14 05:40:29 2013 -0700

    Merge pull request #453 from vishwasbabu/develop
    
    updating inetgration tests for changes in journal entries api

[33mcommit 313f8c51a14ddd05d2c12f49f6109851492c3e9b[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Jul 14 18:08:43 2013 +0530

    updating inetgration tests for changes in journal entries api

[33mcommit f37b2ea2aeb66313f741900aa793ba2c09011a83[m
Merge: bb12ed1 aa8a687
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sat Jul 13 07:35:14 2013 -0700

    Merge pull request #452 from vishwasbabu/develop
    
    minor changes to batch jobs code

[33mcommit aa8a6873e034b05317af128c26f62c8ec2d0b3c8[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sat Jul 13 20:03:29 2013 +0530

    minor changes to batch jobs code

[33mcommit bb12ed1c4522d60bb4f50abf1d72bd51d657afc3[m
Merge: 0ea0ea6 ae9b812
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Jul 12 06:20:05 2013 -0700

    Merge pull request #451 from vishwasbabu/mifosx-500
    
    bug fixes for mifosx-519, 516, 361 and 362

[33mcommit ae9b812d8c9b8b9a4a22a1a748acec9a9e9a0419[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Jul 12 18:32:18 2013 +0530

    bug fixes for mifosx-519, 516, 361 and 362

[33mcommit 0ea0ea662a485e819883167821bcc1c10ee929ea[m
Merge: a8d31aa 385c1e7
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Jul 12 03:44:01 2013 -0700

    Merge pull request #450 from pramodn02/MIFOSX-506_2
    
    batch job changes for load and save

[33mcommit 385c1e7fe55a40a7a01fb7a5cddc4bec25dc87c8[m
Author: Pramod Nuthakki <pramod@confluxtechnologies.com>
Date:   Fri Jul 12 13:34:00 2013 +0530

    batch job changes for load and save

[33mcommit a8d31aacc94ca0f3b9c392003a1482db963ec52d[m
Merge: 8d61042 9ee36e4
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Jul 11 23:19:42 2013 -0700

    Merge pull request #449 from mifoscontributer/MIFOSX-526
    
    Restricting existing guarantor as new guarantor for same loan

[33mcommit 9ee36e42f584635c99787bdeca15d8a4c6ec8581[m
Author: mifoscontributer <mifos.contributer@gmail.com>
Date:   Fri Jul 12 11:42:13 2013 +0530

    Restricting existing guarantor as new guarantor for same loan

[33mcommit 8d61042005f0e097b6c995dbeca0abf23a1957e4[m
Merge: 6d04dad 436e9d7
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Jul 11 10:03:18 2013 -0700

    Merge pull request #447 from johnw65/perms
    
    Perms

[33mcommit 436e9d79521a9c0defc74970091628485f60d035[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Jul 11 17:57:20 2013 +0100

    mifosx-476 make portfolio permissions not grotesque (amendment)

[33mcommit 297df3b75038a4ca124fe2a37789e97757b29ecb[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Jul 11 17:53:27 2013 +0100

    mifosx-476 make portfolio permissions not grotesque

[33mcommit 6d04dadd6b3054c72d334d115f80c05bc8be48e2[m
Merge: b29d942 1873e73
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Jul 11 07:33:08 2013 -0700

    Merge pull request #446 from goutham-M/mifosx-499
    
    reactivate feature is added

[33mcommit 1873e73aa85117a5d7942c573b79dfad4443e4e9[m
Author: goutham-M <goutham@confluxtechnologies.com>
Date:   Thu Jul 11 19:47:18 2013 +0530

    reactivate feature is added

[33mcommit b29d9423dd9b4444e656335f1f4a10078ba3284c[m
Merge: c849ac0 d9368ee
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Jul 11 05:39:18 2013 -0700

    Merge pull request #438 from mifoscontributer/MIFOSX-526
    
    Edit or Update loan guarantor

[33mcommit c849ac04fdfab66619c229eda065e78275dab213[m
Merge: 4372213 fcbbeee
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Jul 11 03:39:02 2013 -0700

    Merge pull request #443 from johnw65/perms
    
    rename permission grouping organistion to accounting

[33mcommit fcbbeee3fb61794419212e7b888af28761701569[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Jul 11 11:31:59 2013 +0100

    rename permission grouping organistion to accounting

[33mcommit 43722131b8a89598916b9fab4968e6b3659acb05[m
Merge: 69c2885 3e04aa5
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Jul 8 23:23:35 2013 -0700

    Merge pull request #440 from mifoscontributer/MIFOSX-498
    
    MIFOSX-498 Searched Center is behaving as Group(FIXED)

[33mcommit 3e04aa5dff22354b39bd70aafe9870b31bd36fcf[m
Author: mifoscontributer <mifos.contributer@gmail.com>
Date:   Tue Jul 9 11:29:00 2013 +0530

    MIFOSX-498 Searched Center is behaving as Group(FIXED)

[33mcommit 69c288517534f5696172faa8614251e39bafb9df[m
Merge: 4e8177f b9de845
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Jul 8 07:21:48 2013 -0700

    Merge pull request #437 from goutham-M/mifosx-537
    
    mifosx-537 differentiate b/w client/loan related notes

[33mcommit d9368ee3b03dca865f2b866258d190a2cc94d99d[m
Author: mifoscontributer <mifos.contributer@gmail.com>
Date:   Mon Jul 8 18:28:50 2013 +0530

    Edit or Update loan guarantor

[33mcommit b9de8455e38ca6eb5e0d7b9cc21f7a9da545f094[m
Author: goutham-M <goutham@confluxtechnologies.com>
Date:   Mon Jul 8 17:59:04 2013 +0530

    mifosx-537 differentiate b/w client/loan related notes

[33mcommit 4e8177ff867ae62b2ef92ca2cf6796c4a1ec4ea6[m
Merge: 4c5cbb3 b33bb55
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Jul 8 04:10:16 2013 -0700

    Merge pull request #436 from ashok-conflux/MIFOSX-76
    
    MIFOSX-76: Apply holidays to loan repayment schedule

[33mcommit 4c5cbb38bb64cc1b2147ba7c93603720a5ad3e73[m
Merge: 1bc0963 e5b42aa
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Jul 8 04:05:41 2013 -0700

    Merge pull request #435 from vishwasbabu/develop
    
    quick fix for enabling mifosx-443 enable separate datatables for centers

[33mcommit b33bb5583e1002d0c4483b9eab383404707c9c72[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Fri Jun 28 13:02:21 2013 +0530

    MIFOSX-76: Apply holidays to loan repayment schedule

[33mcommit e5b42aa296cf619800308e378450909008d1c5ed[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Jul 8 15:21:59 2013 +0530

    quick fix for enabling mifosx-443 enable separate datatables for centers

[33mcommit 1bc0963be9558a6959d3970b1e6965431c004bb9[m
Merge: 99ecf67 20705e5
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sun Jul 7 13:26:35 2013 -0700

    Merge pull request #434 from mifoscontributer/MIFOSX-493
    
    MIFSOX-493 Update LoanTerms onChange product

[33mcommit 20705e5e63f58f717ecc0c92a3dfb6a9281767b2[m
Author: mifoscontributer <mifos.contributer@gmail.com>
Date:   Sat Jul 6 16:41:28 2013 +0530

    MIFSOX-493 Update LoanTerms onChange product

[33mcommit 99ecf679938209f5b62947f97c97ff13a6a21da7[m
Merge: d95d576 e32bebf
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Jul 5 13:24:19 2013 -0700

    Merge pull request #433 from mifoscontributer/MIFOSX-527
    
    MIFOSX-527-Display loan collaterals options in dropdown

[33mcommit d95d5764e722ee546a7b2859a6f2b193decdd984[m
Merge: 2dd0087 a6b94bc
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Jul 5 13:23:50 2013 -0700

    Merge pull request #432 from mifoscontributer/MIFOSX-536
    
    MIFOSX-536-Client search By Display Name(Fixed)

[33mcommit e32bebf7d65819e6c522953c24fe38a4ab063b24[m
Author: mifoscontributer <mifos.contributer@gmail.com>
Date:   Fri Jul 5 17:37:04 2013 +0530

    MIFOSX-527-Display loan collaterals options in dropdown

[33mcommit a6b94bcfb8cfe8b57ef24ab8b140fd5bae31d53e[m
Author: mifoscontributer <mifos.contributer@gmail.com>
Date:   Fri Jul 5 15:38:54 2013 +0530

    MIFOSX-536-Client search By Display Name(Fixed)

[33mcommit 2dd0087d87827a0d6ec867b4aef830dfbd47b1b8[m
Merge: eb0651e bb27885
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Jul 5 02:47:31 2013 -0700

    Merge pull request #428 from mifoscontributer/MIFOSX-534
    
    MIFSOX-534 In Client's page not able to assign staff from parent office

[33mcommit eb0651e838156b7ad615bd2bcaab0a14085b3121[m
Merge: abbff93 5da24f6
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Jul 5 02:44:53 2013 -0700

    Merge pull request #431 from mifoscontributer/MIFOSX-212-JLG
    
    Update DB Script for loan counter changes

[33mcommit 5da24f6281d933d8970797e5ae98a6a4a042f103[m
Author: mifoscontributer <mifos.contributer@gmail.com>
Date:   Fri Jul 5 10:59:01 2013 +0530

    Update DB Script for loan counter changes

[33mcommit abbff93950e7635efbf4deac813d2be1a848bb03[m
Merge: c902fa5 c3a9132
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Jul 4 07:07:04 2013 -0700

    Merge pull request #430 from goutham-M/mifosx533
    
    mifosx533 able to associate for group even closed client also(fixed)

[33mcommit c3a9132f16a0d94e29df7ec72a74623020aae9f3[m
Author: goutham-M <goutham@confluxtechnologies.com>
Date:   Thu Jul 4 13:07:35 2013 +0530

    mifosx533 able to associate for group even closed client also(fixed)

[33mcommit bb278855cf1290f92e236c922c12ee574e3b672f[m
Author: mifoscontributer <mifos.contributer@gmail.com>
Date:   Thu Jul 4 18:30:59 2013 +0530

    MIFSOX-534 In Client's page not able to assign staff from parent office

[33mcommit c902fa58084dbff8ea82a7d9b5d494ddb5a7424d[m
Merge: f839f1f 9010ae6
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Jul 4 05:13:25 2013 -0700

    Merge pull request #423 from mifoscontributer/MIFOSX-212-JLG
    
    Add Loan counter for Group and JLG Loan

[33mcommit f839f1f38b94679ede7f3248d951d51f736751be[m
Merge: 2ef53a6 0aaf268
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Jul 4 13:09:50 2013 +0100

    Merge remote-tracking branch 'goutham/mifosx-530-B' into develop

[33mcommit 2ef53a6589fa07dfbfafc93b13e3a90b5a892b0e[m
Merge: 94e11c2 c869abc
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Jul 3 21:18:08 2013 +0100

    Merge remote-tracking branch 'sander/master' into develop

[33mcommit c869abcaab3e3c81a11868f0d762630170365718[m
Author: Sander van der Heyden <sander@musoni.eu>
Date:   Wed Jul 3 21:54:41 2013 +0200

    Enable datatable functionality for savings products and loan products

[33mcommit 0aaf2681f8dc93162ca83b4a21244cdf8b407e9a[m
Author: goutham-M <goutham@confluxtechnologies.com>
Date:   Wed Jul 3 19:32:37 2013 +0530

    mifosx-530 fixing no connection error

[33mcommit 9010ae6a80bf2a7ca09c397db985f30f89f930d0[m
Author: mifoscontributer <mifos.contributer@gmail.com>
Date:   Wed Jul 3 18:15:37 2013 +0530

    Add Loan counter for Group and JLG Loan

[33mcommit 94e11c2dea0950e3a5e40384edcc60aefda32785[m
Merge: bf7c531 a941bbd
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Jul 3 02:40:30 2013 -0700

    Merge pull request #422 from goutham-M/mifosx-531
    
    Loan is getting approved for a Closed client(fixed)

[33mcommit bf7c5318511ead497b06143879ea4bce617c21b6[m
Merge: f00d212 be4a9a9
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Jul 3 02:39:49 2013 -0700

    Merge pull request #421 from goutham-M/mifosx-528
    
    For same loan same guarantor can be associate more than once(fixed)

[33mcommit a941bbdded1e7d4eb2371f102a04cc962d77ea07[m
Author: goutham-M <goutham@confluxtechnologies.com>
Date:   Wed Jul 3 11:10:39 2013 +0530

    Loan is getting approved for a Closed client(fixed)

[33mcommit be4a9a9f692ed457aca32792ceaa33b9d2c4742d[m
Author: goutham-M <goutham@confluxtechnologies.com>
Date:   Tue Jul 2 20:37:47 2013 +0530

    For same loan same guarantor can be associate more than once(fixed)

[33mcommit 2e752407842f43c9bb874e738df7297dda835d1c[m
Author: Sander van der Heyden <sander@musoni.eu>
Date:   Tue Jul 2 20:51:06 2013 +0200

    Add datatables functionality to loan product and savings product entities

[33mcommit f00d212e8f0bb3fb0976802ff3eeac5fa3066257[m
Merge: 8fe4396 1b1b628
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Jul 2 09:55:06 2013 -0700

    Merge pull request #414 from mifoscontributer/MIFOSX-212-Test
    
    Removing mandatory check for includeInBorrowerCycle for loan product

[33mcommit 8fe439698ca66704ef2d050bcd2471a90b493c48[m
Merge: fa59a1e 8fc9651
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Jul 2 09:53:04 2013 -0700

    Merge pull request #416 from goutham-M/mifosx522
    
    In add new group form if activation date field is empty it showing connection fail(fixed)

[33mcommit 8fc9651410ec12c0d419eaf4fc612de9a5319d63[m
Author: goutham-M <goutham@confluxtechnologies.com>
Date:   Tue Jul 2 12:46:41 2013 +0530

    In add new group activation date field is empty it showing connection fail(fixed)

[33mcommit 1b1b62869046e37f4d3d28d05dbb7e83d948cc51[m
Author: mifoscontributer <mifos.contributer@gmail.com>
Date:   Sat Jun 29 12:03:57 2013 +0530

    Removing mandatory check for includeInBorrowerCycle for loan product
    
    Refactor method name for validate boolean method

[33mcommit fa59a1eddc897c90ad2156298b6ee206c156421f[m
Merge: cf20cae 7e9fc03
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Jul 1 06:54:12 2013 -0700

    Merge pull request #410 from ashok-conflux/MIFOSX-518
    
    MIFOSX-518: capture alternative working date

[33mcommit 7e9fc03dc59536f89b9372e1f0fdcb36dbb5e108[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Fri Jun 28 18:08:42 2013 +0530

    MIFOSX-518: capture alternative working date

[33mcommit cf20cae7d7a6b567b688ac4337687ea201631b09[m
Merge: e42b3bb 628a6d7
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Jun 27 23:42:06 2013 -0700

    Merge pull request #406 from mifoscontributer/MIFOSX-212-B
    
    Add Loan cycle counter for client

[33mcommit 628a6d778bb46b89e3b979f738221b6200ad5d0e[m
Author: mifoscontributer <mifos.contributer@gmail.com>
Date:   Fri Jun 28 11:38:44 2013 +0530

    Add Loan cycle counter for client
    
    Update the sql version

[33mcommit e42b3bb5242fa0af01d291ad04e1eff440a590c8[m
Merge: b764082 b72c007
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Jun 27 06:00:13 2013 -0700

    Merge pull request #404 from goutham-M/mifosx--471
    
    New group activation date is accepting before the office opening date(fixed)"

[33mcommit b76408271750700e6f402be89688ba89e2a6d5bf[m
Merge: d2eac27 1d9d4a5
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Jun 27 03:53:06 2013 -0700

    Merge pull request #405 from vishwasbabu/develop
    
    added max length validation for datatables

[33mcommit 1d9d4a51185ff35049ca1ff271084b1f3c65d931[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Thu Jun 27 16:21:44 2013 +0530

    added max length validation for datatables

[33mcommit b72c0074e64caafcc9fa4f61718b561af1538604[m
Author: goutham-M <goutham@confluxtechnologies.com>
Date:   Thu Jun 27 14:48:22 2013 +0530

    New group activation date is accepting before the office opening date(fixed)

[33mcommit d2eac27f5f62841fdbfa288b8cb7f6bc5ad9f951[m
Merge: b898a65 1a69409
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Jun 27 09:26:36 2013 +0100

    Merge remote-tracking branch 'ashok/MIFOSX-475' into develop

[33mcommit b898a657a4177bb36c9032cfc8c271306b6278aa[m
Merge: 5fa5503 178952c
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Jun 26 21:21:48 2013 -0700

    Merge pull request #400 from goutham-M/mifosx---438
    
    for close client functionality apiLive.htm is updated

[33mcommit 1a694096af4aa90c6c64f1c485173ebcecdf218e[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Wed Jun 26 18:39:44 2013 +0530

    based on meeting start date change reschedule future repayments of all attached loans

[33mcommit 5fa55031c16a3d6e09a140b5e34852e070996d16[m
Merge: b2c225e 23b0cb7
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue Jun 25 15:21:42 2013 -0700

    Merge pull request #401 from johnw65/pgmdetails
    
    fix for programdetails query (quipo)

[33mcommit 23b0cb75e2bd6f68eba6f401ddf8a3a8588ab3a5[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue Jun 25 23:20:14 2013 +0100

    fix for programdetails query (quipo)

[33mcommit 178952cc8b3726ed50baa28ef76e823f5caf0904[m
Author: goutham-M <goutham@confluxtechnologies.com>
Date:   Tue Jun 25 20:46:01 2013 +0530

    for close client functionality apiLive.htm is updated

[33mcommit b2c225efe78a59c57efe64d129e51bbdd4a24a72[m
Merge: f283aca 5db999b
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Jun 25 14:31:00 2013 +0100

    fix merge conflicts from 1.4.1

[33mcommit 5db999b5ba2b4e5c3157b9b50fab8c2a9aaa006e[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Jun 25 14:27:17 2013 +0100

    update release to 1.4.1

[33mcommit 678728c2b7ad15ebd4caa4a45b0dec0dfb675a43[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Jun 25 15:25:43 2013 +0200

    udpate changelog 1.4.1 release

[33mcommit 4f83102834c90ee9ab5f8d536ee8a11d03f55ec9[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Jun 25 14:58:44 2013 +0200

    update latest version info on readme so doesnt need to change each release

[33mcommit fda126474cb18b8ed0da7213a4d4c790261dc59d[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Tue Jun 25 15:23:45 2013 +0530

    datatable delete apis should only delete registered datatables

[33mcommit f283acad5ae17d376f929fd8e50a77a138d76902[m
Merge: 7ec60c9 ced8811
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Jun 25 02:55:18 2013 -0700

    Merge pull request #397 from vishwasbabu/develop
    
    datatable delete apis should only delete registered datatables

[33mcommit ced88110117feb69531e88839881820d395ce577[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Tue Jun 25 15:23:45 2013 +0530

    datatable delete apis should only delete registered datatables

[33mcommit 7ec60c9eba0f3a39d967dadbcca6e18beea94627[m
Merge: 8f3a607 db5c91f
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon Jun 24 13:46:33 2013 -0700

    Merge pull request #396 from johnw65/tevieile
    
    initialise loans_paid_in_advance table (batch job updates daily afterwar...

[33mcommit db5c91fea805679f67e4a2ca80d004c744d09814[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon Jun 24 21:42:22 2013 +0100

    initialise loans_paid_in_advance table (batch job updates daily afterwards)

[33mcommit 58eda883270af1541def3cb1affcf870a7cc0747[m
Author: mifoscontributer <mifos.contributer@gmail.com>
Date:   Mon Jun 24 18:48:17 2013 +0530

    Allow assign parent office staff to group
    
    Fixing missed fun

[33mcommit 8f3a607067cf7b15db47bf2dacd26178cac05115[m
Merge: 7b2c769 4823329
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Jun 24 07:26:01 2013 -0700

    Merge pull request #395 from vishwasbabu/develop-old
    
    updating client apis to allow linking with staff from parent offices

[33mcommit 48233290e4b3f7563380bfca6ce27f4df42ad715[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Mon Jun 24 19:54:02 2013 +0530

    updating client apis to allow linking with staff from parent offices

[33mcommit 7b2c769b634158bebcdf63f46af79e85f226e5cd[m
Merge: 95713f2 d4c75a7
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Jun 24 06:48:07 2013 -0700

    Merge pull request #394 from mifoscontributer/MIFOSX-480
    
    Allow assign parent office staff to group

[33mcommit d4c75a76677a4a757a325a4e0d84cf7eb12bcde4[m
Author: mifoscontributer <mifos.contributer@gmail.com>
Date:   Mon Jun 24 18:48:17 2013 +0530

    Allow assign parent office staff to group
    
    Fixing missed fun

[33mcommit 95713f2777ddfa78902d26aa4a7475a298cae0c6[m
Merge: 7f2b5ee 876bca0
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon Jun 24 02:09:36 2013 -0700

    Merge pull request #393 from johnw65/batchjob
    
    batch job changes

[33mcommit 876bca05d2f656372888debc96daa0a4b9e399e3[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon Jun 24 10:06:50 2013 +0100

    batch job changes

[33mcommit 7f2b5ee8100ec6832bc40249afbda10cba3eca84[m
Merge: f56f748 a4f8b5c
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Jun 23 21:38:55 2013 -0700

    Merge pull request #392 from vishwasbabu/develop
    
    adding locale based am/pm to time in accountig pentaho reports

[33mcommit c28ea2b4fc23b63b8b6ce51ce3eebe7c58cbf3a9[m
Merge: a71dea0 bde74b8
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Jun 23 21:37:49 2013 -0700

    Merge pull request #391 from vishwasbabu/mifosplatform-1.4.1
    
    adding locale based am/pm to time in accountig pentaho reports

[33mcommit a4f8b5cdfa6ca3efea78f0d95ced605c99e1eeba[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Mon Jun 24 10:00:02 2013 +0530

    adding locale based am/pm to time in accountig pentaho reports

[33mcommit bde74b8e24495743361adcb860e4f7f88bf94154[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Mon Jun 24 10:00:02 2013 +0530

    adding locale based am/pm to time in accountig pentaho reports

[33mcommit f56f748ac0e718b19013acb0e3bcf9aaf5dabaf0[m
Merge: 7884534 69d923d
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sun Jun 23 16:33:46 2013 -0700

    Merge pull request #390 from johnw65/loansinadvance
    
    add support for reporting on loans with payments in advance (quipo need)

[33mcommit 69d923d135d0ef7da5a75beb2e07cf245a95e637[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon Jun 24 00:30:31 2013 +0100

    add support for reporting on loans with payments in advance (quipo need)

[33mcommit 78845348a194c55b9056026038f1877275da8d46[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Sun Jun 23 18:58:44 2013 +0530

    localizing income statement report

[33mcommit a71dea07667c0e501f2141bb8509c94edf6a2389[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Sun Jun 23 18:58:44 2013 +0530

    localizing income statement report

[33mcommit 7982cbeb2b2586c86d849f222e524155858127bc[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Sun Jun 23 18:41:58 2013 +0530

    adding support for pentaho report localization

[33mcommit fb71820c5c2b25bdaf2b85397b233824c7f21f65[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Sun Jun 23 18:41:58 2013 +0530

    adding support for pentaho report localization

[33mcommit c008ed7ff1b32e01dab33a6710407de1fe9090b1[m
Author: mifoscontributer <mifos.contributer@gmail.com>
Date:   Sat Jun 22 13:49:08 2013 +0530

    Use Login user hierarchy for view accounting rules

[33mcommit c3224c5e113380d93791cc7a50e1e840e09d7e1a[m
Merge: 45be083 62ff19e
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sat Jun 22 03:23:21 2013 -0700

    Merge pull request #389 from mifoscontributer/MIFOSX-473
    
    Use Login user hierarchy for view accounting rules

[33mcommit 62ff19e0d22166cc112bff703843fec579e2b2ec[m
Author: mifoscontributer <mifos.contributer@gmail.com>
Date:   Sat Jun 22 13:49:08 2013 +0530

    Use Login user hierarchy for view accounting rules

[33mcommit 45be083ddb039a23839a55def0441f79299d993b[m
Merge: 8d70270 ee6df1f
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Jun 21 21:48:05 2013 -0700

    Merge pull request #388 from vishwasbabu/develop
    
    fixing issues with database migration

[33mcommit ee6df1fbc3ea4b626bcf3892d729994204410056[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Sat Jun 22 10:16:13 2013 +0530

    fixing issues with database migration

[33mcommit 8d702705b7b6e37401e1b67fe38d9e38ce894649[m
Merge: 8189d5a 86bb794
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Jun 21 09:49:31 2013 -0700

    Merge pull request #387 from goutham-M/mifosx438
    
    close client functionality

[33mcommit 86bb794f4122cc6c7b179c1de8e9d01b2229fc19[m
Author: goutham-M <goutham@confluxtechnologies.com>
Date:   Fri Jun 21 22:15:18 2013 +0530

    close client functionality

[33mcommit 8189d5ac50b8c415d5a1f495cfdce2fbb84eab86[m
Merge: 71de484 b602ca0
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Jun 21 08:32:38 2013 -0700

    Merge pull request #385 from vishwasbabu/bug-fix
    
    fix for rupee symbol on unicode unsafe connections

[33mcommit b602ca0b001047df1bbff80868d881af8635308b[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Fri Jun 21 21:01:31 2013 +0530

    fix for rupee symbol on unicode unsafe connections

[33mcommit 71de48414f77a33271f2c93bd096e6ffbaca046d[m
Merge: 4e934ae 50cf269
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Jun 21 08:11:27 2013 -0700

    Merge pull request #383 from mifoscontributer/MIFOSX-429
    
    Allow AssignStaff from command param and refactor method names

[33mcommit 50cf269bac4608d2e612f250120a4f75d3768ab3[m
Author: mifoscontributer <mifos.contributer@gmail.com>
Date:   Fri Jun 21 19:02:45 2013 +0530

    Allow AssignStaff from command param and refactor method names
    
    Permission script for Assign staff

[33mcommit 4e934ae0a0853627c9cb3fe6efc05ea6f0e62f8d[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Jun 20 12:53:58 2013 +0100

    MIFOSX-479: run scheduled jobs for all tenants.

[33mcommit 97f681244a0ffbe6f4be2e0c3fd5f8ef769e1913[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Jun 20 12:53:58 2013 +0100

    MIFOSX-479: run scheduled jobs for all tenants.

[33mcommit 14589590a6a95d3aca9e76c0cc1a9e241c9aee1f[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Jun 20 12:04:53 2013 +0100

    update release for 1.4.1 candidate.

[33mcommit 1aa309c46526027653288248a9f8f76b73c39977[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Jun 20 12:01:49 2013 +0100

    update to next release candidate.

[33mcommit 122e63254c4e09db3608cfa59ea57566ce9c1ce0[m
Merge: c6048f9 4b88501
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Jun 20 11:54:57 2013 +0100

    Merge branch 'develop'

[33mcommit 4b885019b3062977a075159c557dfe2ac8f768ce[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Jun 20 12:54:14 2013 +0200

    update AMI info for 1.4.0

[33mcommit c6048f928cfb25f5bdaea3f1756bd8e3eb468eb9[m
Merge: 8b20c56 a999cdf
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Jun 20 11:18:22 2013 +0100

    Merge branch 'develop'

[33mcommit a999cdf8af46c763f425f3016f7b71447f100ad9[m
Merge: b371526 c2a5f8d
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Jun 20 03:10:12 2013 -0700

    Merge pull request #382 from ashok-conflux/MIFOSX-449
    
    MIFOSX-449 - Sync repayment dates with meeting dates for group and JLG loans

[33mcommit 8b20c562bf0e9951d05359a2ea391f675d2d4899[m
Merge: dd29b41 b371526
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Jun 20 10:40:49 2013 +0100

    Merge branch 'develop'

[33mcommit dd29b41aab20a1f4403a563b8098d74b6398823a[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Jun 20 10:39:55 2013 +0100

    Revert "update for 1.4.0 release"
    
    This reverts commit 5e3a10bf2b2e0ebfed7a642c376cdf9da02517a2.

[33mcommit b371526214a1cd48ed73b7489dd45066fb0b80f6[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Jun 20 11:31:28 2013 +0200

    update for 1.4.0 release

[33mcommit cb698466f645c95eecbb0a73feafae5c95ac6288[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Jun 20 11:21:36 2013 +0200

    update for 1.4.0 release

[33mcommit 5e3a10bf2b2e0ebfed7a642c376cdf9da02517a2[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Jun 20 11:20:30 2013 +0200

    update for 1.4.0 release

[33mcommit fc829a48f113518f6654c79a10f0f8f55f8f2e3b[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Jun 20 10:17:49 2013 +0100

    update release properties.

[33mcommit c2a5f8de617ce21eb417bc98b014407a57435e15[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Wed Jun 19 11:40:44 2013 +0530

    MIFOSX-449 - Sync repayment dates with meeting dates for group and JLG loans

[33mcommit 348b469652dc6340587abbec5489126b853e861e[m
Merge: f915222 83c6442
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Jun 19 12:01:03 2013 -0700

    Merge pull request #380 from vishwasbabu/bug-fix
    
    fix for MIFOSX-464

[33mcommit 83c6442c4bde84ac340abf458c1735151e835378[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Wed Jun 19 23:31:20 2013 +0530

    fix for MIFOSX-464

[33mcommit f915222c231e8a37ff7c1d794a0cc4feabb1ebf2[m
Merge: 3019f28 d414e67
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Jun 19 07:29:07 2013 -0700

    Merge pull request #379 from vishwasbabu/bug-fix
    
    fix for MIFOSX-474

[33mcommit d414e677d68df1ec088f8a4c4b09effd24cb6622[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Wed Jun 19 19:43:59 2013 +0530

    fix for MIFOSX-474

[33mcommit 3019f283712d8138eb89bc9a681882c03cd22947[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Wed Jun 19 16:20:31 2013 +0530

    MIFOSX-469 - fixed group activating issue

[33mcommit 5094f8b89e9828f14add9946a62f297da79fbdbb[m
Merge: 95cc4c6 3c9184b
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Jun 18 23:29:39 2013 -0700

    Merge pull request #376 from vishwasbabu/bug-fix
    
    make staff Id optional for client creation/updation

[33mcommit 95cc4c6365704800472b4e483279126382476e18[m
Merge: 10ad444 7c91a3a
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Jun 18 23:27:54 2013 -0700

    Merge pull request #375 from goutham-M/mifosx76
    
    officeId attribute is added in HolidayApiConstants class

[33mcommit 3c9184bf1c21690ceef8a7573d139e1afb53f441[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Wed Jun 19 11:55:08 2013 +0530

    make staff Id optional for client creation/updation

[33mcommit 7c91a3a83bbfbd69ddfe899dca72580335999f12[m
Author: goutham-M <goutham@confluxtechnologies.com>
Date:   Wed Jun 19 11:43:12 2013 +0530

    officeId attribute is added in HolidayApiConstants class

[33mcommit 10ad4446683f5fe8793da529cb870e231278d3a1[m
Merge: 6c09148 799dcfb
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Jun 18 21:40:10 2013 -0700

    Merge pull request #374 from goutham-M/MIFOSX--76
    
    data validation error holiday name appearing twice(fixed)

[33mcommit 6c091489d2b504021644ae8fa7d907982c054f95[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Jun 18 16:22:40 2013 +0100

    fix error in query for group and client members due to addition of staff to query.

[33mcommit 799dcfb4e35cbf211742ff0459d2acd0636b1334[m
Author: goutham-M <goutham@confluxtechnologies.com>
Date:   Tue Jun 18 20:24:52 2013 +0530

    data validation error holiday name appearing twice(fixed)

[33mcommit dfd0bed94c0b351a468b146a74e472beb90e2df5[m
Merge: cd1af19 6def2fd
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Jun 17 21:26:24 2013 -0700

    Merge pull request #373 from vishwasbabu/bug-fix
    
    fixed issues observed in api-docs while recording screencasts

[33mcommit 6def2fd978cd422822706a8b97ad61db784beef3[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Tue Jun 18 09:53:20 2013 +0530

    fixed issues observed in api-docs while recording screencasts

[33mcommit cd1af19601f78ed1e7c140416b90fcda70f65bde[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Jun 17 10:47:47 2013 +0100

    FIX naming pattern of database migration scripts as lowercase v causes migration not to be picked up, captial V is required.

[33mcommit c1ce9a5d33099690f56f02b42682836b350a18cd[m
Merge: 5527d59 0ae3422
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Jun 17 00:45:50 2013 -0700

    Merge pull request #372 from vishwasbabu/mifoscontributer-MIFOSX-429
    
    Mifoscontributer mifosx 429

[33mcommit 0ae342211a4ace654100a5eb02faa718d781fbaa[m
Merge: 5527d59 4d50d4b
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Mon Jun 17 13:13:50 2013 +0530

    merging pull request for validating access to client/group resources

[33mcommit 5527d59620a945bd9076e6fe31886603641fa6c1[m
Merge: 92d8825 bf6eb00
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Jun 17 00:27:40 2013 -0700

    Merge pull request #371 from vishwasbabu/mifoscontributer-MIFOSX-434
    
    Mifoscontributer mifosx 434

[33mcommit bf6eb00b6a69ccae4616e93e4e1c36450437dd74[m
Merge: 92d8825 7a05bb6
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Mon Jun 17 12:46:52 2013 +0530

    merging comment for linking staff to client

[33mcommit 4d50d4b583458dadf4fef29162a548e7d60c00d5[m
Author: mifoscontributer <mifos.contributer@gmail.com>
Date:   Sat Jun 15 16:00:34 2013 +0530

    Don't Allow other office user to Update the client
    
    Restrict other office user to update group or client details

[33mcommit 92d882565fd81935ee2e99c41463c8887c1e248f[m
Merge: 41e7f2b ce0e52e
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sun Jun 16 15:15:04 2013 -0700

    Merge pull request #369 from johnw65/rbalrpt
    
    add example running balances report to migration script

[33mcommit ce0e52e387651bdc1fc4a137d53fc7bea2e4f0b1[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sun Jun 16 23:12:54 2013 +0100

    add example running balances report to migration script

[33mcommit 41e7f2b91593ae8689fc8a34f32bcfd2e7118467[m
Merge: 423f8bf e6293c3
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sun Jun 16 12:59:00 2013 -0700

    Merge pull request #368 from johnw65/teviupdate
    
    mifosx-378 updated reports for landing pages

[33mcommit e6293c3651ad04cf42487f86649c2ffac8ac9683[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sun Jun 16 20:56:29 2013 +0100

    mifosx-378 updated reports for landing pages

[33mcommit 423f8bf07a95b46f70b7bc7cfc8ccc43d358decc[m
Merge: 1a52185 371352c
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Jun 14 05:56:32 2013 -0700

    Merge pull request #366 from vishwasbabu/mifoscontributer-MIFOSX-421
    
    updating version number for group roles migration script

[33mcommit 371352cc5f46dd6aa72d2c3d43ba8fd29bc7d01f[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Fri Jun 14 18:25:27 2013 +0530

    updating version number for group roles migration script

[33mcommit 1a521852a0d37651234fd76947764181a2fccd80[m
Merge: 8ed65fc f6b071c
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Jun 14 05:39:47 2013 -0700

    Merge pull request #365 from vishwasbabu/mifoscontributer-MIFOSX-421
    
    Mifoscontributer mifosx 421

[33mcommit f6b071cc17e01f5d85e539d23b434bdcdc53ff7d[m
Merge: 8ed65fc 8b89c9a
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Fri Jun 14 18:07:48 2013 +0530

    merging group role changes and removing holiday id from Commandwrapper and related classes

[33mcommit 7a05bb6b6a7465b5c5852d60f816f085baaff924[m
Author: mifoscontributer <mifos.contributer@gmail.com>
Date:   Thu Jun 13 20:31:18 2013 +0530

    Allow linking of staff to Clients
    
    Changes required for UI to link staff to client

[33mcommit 8ed65fc7346d967f0462705e943c42e847dfa826[m
Merge: 0393ed5 7faaee7
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Jun 13 11:30:32 2013 -0700

    Merge pull request #361 from mifoscontributer/MIFOSX-391
    
    Correct the Input param names for AccountingRule

[33mcommit 0393ed5017dbd4425c00ccb7382aafeb0c55343f[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Jun 13 14:29:21 2013 +0100

    MIFOSX-445: support ability to use staff from branches higher in hierarchy on groups.

[33mcommit 7faaee70b493b39c8cc08635acb0f7a614fdad56[m
Author: mifoscontributer <mifos.contributer@gmail.com>
Date:   Thu Jun 13 15:06:02 2013 +0530

    Correct the Input param names for AccountingRule

[33mcommit 8b89c9a584f890678c7cef2d80186d3582d9bcb6[m
Author: mifoscontributer <mifos.contributer@gmail.com>
Date:   Thu Jun 13 12:14:07 2013 +0530

    Creating Roles for Group

[33mcommit 256cdd3c4725d389ab621ecea20974174fea6554[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Thu Jun 13 11:50:51 2013 +0530

    Move Group API documentation from Beta release to General availibility

[33mcommit fa80413d5e7b4ab3a923df646f531eccb1a084d6[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Jun 13 00:24:01 2013 +0100

    tidyup usage of PlatformApiDataValidationException constructor.

[33mcommit c58d5412799de9cd8d303e853940abd9da2df92d[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Jun 13 00:13:02 2013 +0100

    MIFOSX-445: default the list of loan officer options for new loan application to pick up on loan officers in offices higher in its office hierarchy. add optional parameter to allow people to restrict this list to only the selected branch.

[33mcommit 2d2028bb691ea4d2bbfc69505f92bd72e7f1d12d[m
Merge: 6527e8d 59f1c0c
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Jun 12 07:40:55 2013 -0700

    Merge pull request #356 from goutham-M/MIFOSX---76
    
    holiday functionality to apply holidays for offices

[33mcommit 6527e8d4597a4dec00177b18a4fd4183fc36f04b[m
Merge: b3d7b98 7fc4fd1
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Jun 12 07:37:06 2013 -0700

    Merge pull request #358 from mifoscontributer/MIFOSX-446
    
    Handle Min Max constraint exceptions for crete loan

[33mcommit 7fc4fd12e4a9537dcf7cfebd2546ad107fd347e9[m
Author: mifoscontributer <mifos.contributer@gmail.com>
Date:   Wed Jun 12 19:09:17 2013 +0530

    Handle Min Max constraint exceptions for crete loan

[33mcommit 59f1c0ce8ab2a5c1d6a77dc64b113090e096d94b[m
Author: goutham-M <goutham@confluxtechnologies.com>
Date:   Wed Jun 12 16:31:22 2013 +0530

    holiday functionality to apply holidays for offices

[33mcommit b3d7b98f087b915e5995c948a39cfa6f3a489068[m
Author: Lech Rozanski <lrozanski@soldevelo.com>
Date:   Wed Jun 12 14:44:19 2013 +0200

    MIFOSX-365: Simplify 'Data Tables' functionality

[33mcommit 6dda7180cf526f4d91e2ef5819e227e1f3f2c22f[m
Merge: 8690bbb b64ec8a
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue Jun 11 08:23:45 2013 -0700

    Merge pull request #354 from vishwasbabu/develop
    
    updating api-docs with examples of mapping fees and penalties to income ...

[33mcommit b64ec8addbf32f0566de43617c28920a6a5e944c[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Tue Jun 11 20:46:39 2013 +0530

    updating api-docs with examples of mapping fees and penalties to income heads

[33mcommit 8690bbbc47a985991d4bfe06fce92212ef247ab3[m
Author: Lech Rozanski <lrozanski@soldevelo.com>
Date:   Tue Jun 11 16:24:34 2013 +0200

    MIFOSX-365: Simplify 'Data Tables' functionality

[33mcommit 8c81fe7fc10a3d6725ce6e81ef8771978b58bc98[m
Author: mifoscontributer <mifos.contributer@gmail.com>
Date:   Tue Jun 11 19:26:28 2013 +0530

    Update missing database scripts for MIFOSX-391

[33mcommit f0ee8920508790f27720f81addf727b402fde948[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Jun 11 13:44:40 2013 +0100

    MIFOSX-411: fix interest period determination for annual posting periods.

[33mcommit 106fc2d56d12f269d0548b06974e1f713dfa5a7d[m
Author: Lech Rozanski <lrozanski@soldevelo.com>
Date:   Tue Jun 11 15:00:01 2013 +0200

    MIFOSX-365: Simplify 'Data Tables' functionality

[33mcommit af1f5de611f90c7df1ea3946a74f192af7701530[m
Merge: 232426f 44d864e
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Jun 10 08:47:30 2013 -0700

    Merge pull request #350 from vishwasbabu/develop
    
    minor changes to api docs for accounting rules

[33mcommit 44d864e7aaf55c991ccddac0c3dd5ab12633b162[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Mon Jun 10 21:16:09 2013 +0530

    minor changes to api docs for accounting rules

[33mcommit 232426fd42eb417d120c2d51b76a2f684ee9ffeb[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Jun 10 15:24:57 2013 +0100

    update version of develop branch.

[33mcommit d797a53d1fa9f1e3a217c32151f94ae5a406deda[m
Merge: 30fd2fa 5c49c54
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Jun 10 14:54:47 2013 +0100

    Merge branch 'develop'

[33mcommit 5c49c542067a52d12c3a7691df8e08797e287cff[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Jun 10 15:50:56 2013 +0200

    update for 1.3.0 release

[33mcommit cdcba050408240f72964d1a48fa01b68fec4f638[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Jun 10 15:37:55 2013 +0200

    update for 1.3.0 release

[33mcommit 30fd2faff69dc501d1e2738164ddf45db48e69dc[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Jun 10 15:50:56 2013 +0200

    update for 1.3.0 release

[33mcommit 6a8eb79e97cd658ebec2320180e1b7a15dcf2815[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Jun 10 15:37:55 2013 +0200

    update for 1.3.0 release

[33mcommit 4d7106c28e6ede7b3d3cedb35ecb9e323013f86c[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Jun 10 14:35:42 2013 +0100

    update release number

[33mcommit dd145c7fc1ca41d300ac175fb7bb31af7076aef1[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Jun 10 14:16:16 2013 +0100

    MIFOSX-297: track overpaid amount on loan table.

[33mcommit 989a869eb8d3f26dd7e2a32b4eb3ab16d8916c40[m
Merge: fd8ab3e 5672ce1
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Jun 9 08:37:22 2013 -0700

    Merge pull request #349 from vishwasbabu/ugupta
    
    fix file name : fails on *nix due to case sensitivity

[33mcommit 5672ce172287a698074b47008eefbcdd08b5861c[m
Author: Udai Gupta <mailtoud@gmail.com>
Date:   Sat Jun 8 23:37:11 2013 +0530

    fix file name : fails on *nix due to case sensitivity

[33mcommit 3e1a32cb3c5d7f27b7deea11fc2783c106b1bf92[m
Merge: 792877e 83cf141
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Jun 9 08:25:08 2013 -0700

    Merge pull request #348 from ugupta/master
    
    fix file name : build fails on *nix due to case sensitivity

[33mcommit 83cf141f57e4421ab49832fe88e6398cb0832c58[m
Author: Udai Gupta <mailtoud@gmail.com>
Date:   Sat Jun 8 23:37:11 2013 +0530

    fix file name : fails on *nix due to case sensitivity

[33mcommit fd8ab3e072df7f9eda0a50cf759fca0677279226[m
Merge: 3dc08ed 3edceae
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sat Jun 8 08:39:45 2013 -0700

    Merge pull request #347 from vishwasbabu/mifosx-413
    
    fixes for penalty and charges appropriation in Loan Repayment transactio...

[33mcommit 3edceae00de88e676ce813a6026152da53235567[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Sat Jun 8 20:57:56 2013 +0530

    fixes for penalty and charges appropriation in Loan Repayment transaction processor

[33mcommit 3dc08ed5ba9bf4d37b4a363a6c8bcb23d7c47170[m
Merge: 9a83c50 8054592
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sat Jun 8 08:00:42 2013 -0700

    Merge pull request #346 from mifoscontributer/MIFOSX-391
    
    Validation fixes for Accounting Rule

[33mcommit 80545929908508c7e078956180dcdec6b2bb5ac9[m
Author: mifoscontributer <mifos.contributer@gmail.com>
Date:   Sat Jun 8 20:10:54 2013 +0530

    Validation fixes for Accounting Rule

[33mcommit 9a83c5097e7b9a3f16b7e46a27b6ec14b24bd9f9[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sat Jun 8 12:52:00 2013 +0100

    MIFOSX-426: remove mifostenant-default from sql patch.

[33mcommit f4574ce85833cb8f57451c94665e333d50b0ec34[m
Merge: ac9c6a7 af3d917
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Jun 7 21:28:37 2013 -0700

    Merge pull request #344 from vishwasbabu/mifosx-413
    
    optionally fetch all penalty options as part of loan product creation

[33mcommit af3d917441ae9864905eec9220a203768ae3b598[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Sat Jun 8 09:40:29 2013 +0530

    optionally fetch all penalty options as part of loan product creation

[33mcommit ac9c6a792bff40e53ebc51bb3e25392b7c464f2d[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Jun 7 15:18:24 2013 +0100

    removed unused deprecated code. small experiment with rounding monetary amounts to multiples of a given value like 50.

[33mcommit ed0bbff7eebc6949b912c4e317056695c4c4b2c4[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Jun 7 14:07:33 2013 +0100

    MIFOSX-374: add strategy to process partial repayments in order of interest first, then principal, penalties and fees for TEVI.

[33mcommit 5ac5af1c6ae5b560685811bddf5c39169bf99221[m
Merge: 4e0b70a 2319753
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Jun 7 05:23:43 2013 -0700

    Merge pull request #343 from ashok-conflux/groupcodecleanup
    
    Inherit Center meeting to child group

[33mcommit 23197537fa0a0b3cd4d1aa42b860562c96d52824[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Fri Jun 7 16:52:17 2013 +0530

    Inherit Center meeting to child group

[33mcommit 4e0b70aee2a02cbdf839991ae20a483dbc669059[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Jun 7 11:18:25 2013 +0100

    fix assertion of principal due values for loan scheudle.

[33mcommit 435e0a02593f69da7b2c796d579ec56fe460af74[m
Merge: 8733e10 8d53d85
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Jun 6 21:54:47 2013 -0700

    Merge pull request #342 from vishwasbabu/mifosx-413
    
    validation for advanced accoounting options passed in during loan produc...

[33mcommit 8d53d854e036d780c84ab97c3300b9ebaf72701d[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Fri Jun 7 10:23:11 2013 +0530

    validation for advanced accoounting options passed in during loan product creation

[33mcommit 8733e107297c97dd0437aea12459966f4c6c56df[m
Merge: 97daba6 6ce926f
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Jun 6 21:15:31 2013 -0700

    Merge pull request #341 from vishwasbabu/mifosx-413
    
    Mifosx 413

[33mcommit 6ce926f01f7fb6d4beaecc4757192d83e7511cff[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Fri Jun 7 09:43:19 2013 +0530

    removing warnings in loanproduct data

[33mcommit 4d419c3e45cf57e517117a845aaab9a89842ea72[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Fri Jun 7 09:24:14 2013 +0530

    optionally account different fees and penalties under different heads

[33mcommit 97daba62cbfa827fc931bf1a8317bb5c2807c329[m
Merge: be304ae 792877e
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Jun 6 16:23:03 2013 +0100

    Merge branch 'master' into develop

[33mcommit be304ae4b253542e2bdc7b7b11f6fc5551627189[m
Merge: d7bbdad 18ee622
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Jun 6 16:19:31 2013 +0100

    merge master to develop

[33mcommit 792877ef8ca8e6553b41b1e664c3bb39ff3d4efa[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Jun 6 17:04:45 2013 +0200

    update for 1.2.1 release

[33mcommit 720b1f49af7f699c97112677e2bc4f66dc8014f7[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Jun 6 17:00:44 2013 +0200

    udpate for 1.2.1 release

[33mcommit 18ee622a0347928c4dbba9d676dd25c29ca94dba[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Jun 6 15:59:01 2013 +0100

    bump up release version

[33mcommit d7bbdad07519340fe7e95e77c2cdf68323a45cb9[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Jun 6 15:50:15 2013 +0100

    MIFOSX-376: sum up advance and late payments for each repayment period for totals display.

[33mcommit 066adccc44d24d7cd09845f04e6173772b9988ee[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Jun 6 14:49:36 2013 +0100

    MIFOSX-376: support ability to track advance and late paid amounts in repayment period.

[33mcommit 881ddf9c07ee50350bceafbe612a13908a76636c[m
Merge: d259ca3 96c535a
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Jun 6 14:40:39 2013 +0100

    Merge remote-tracking branch 'ashok/MIFOSX-423' into develop

[33mcommit d259ca34482b33eed5e9caa2b49341ca943eb09f[m
Merge: 06f2da8 5bff72f
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Jun 6 06:38:58 2013 -0700

    Merge pull request #340 from mifoscontributer/MIFOSX-391
    
    Update Accounting Rule

[33mcommit 5bff72fe879efccfc7db8bc47580804e43c1dcba[m
Author: mifoscontributer <mifos.contributer@gmail.com>
Date:   Wed Jun 5 12:42:08 2013 +0530

    Update Accounting Rule
    
    Format Selected Options for UI
    
    Add required database fields for Accounting rule
    
    Cleanup Accounting Rule Update
    
    Format outcoming json for accounting rule

[33mcommit 96c535a8b68d120b51b52496dcf5bb66bd34bd1c[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Thu Jun 6 15:57:40 2013 +0530

    MIFOSX-423: bug fix to view group loans in group context

[33mcommit 06f2da8443ed576f4435a4b3aef801c4859bc457[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Jun 5 20:59:10 2013 +0100

    MIFOSX-376: track when an installment is completed in full.

[33mcommit 379d052a6c512ea8f8b7f904419a6a9f67e0c6ed[m
Merge: b4daa22 7409063
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Jun 5 08:06:01 2013 -0700

    Merge pull request #338 from goutham-M/Back_Up
    
    annual fee amount in creating new savings product not accepting empty value fixed

[33mcommit 740906300b902d47227b599cf6e176ef68005c55[m
Author: goutham-M <goutham@confluxtechnologies.com>
Date:   Wed Jun 5 20:16:26 2013 +0530

    annual fee amount in new savings a/c not accepting empty value fixed

[33mcommit b4daa22510200ed8efefa49a0018796833cefc27[m
Merge: 59580e6 47feb80
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Jun 5 06:30:18 2013 -0700

    Merge pull request #337 from goutham-M/MIFOSX-405
    
    differentiate b/w savings a/c activated/pending(fixed)

[33mcommit 47feb800abf5e416a345d798cd96abf5c45ba9c6[m
Author: goutham-M <goutham@confluxtechnologies.com>
Date:   Wed Jun 5 18:50:54 2013 +0530

    differentiate b/w savings a/c activated/pending(fixed)

[33mcommit efbb1e3b2f6ab5d4b0ad6b6c7e2db84b63f9879c[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Jun 5 12:10:51 2013 +0100

    MIFOSX-295: support principal and interest grace along with ability to have interest-free periods.

[33mcommit 52051865ee3e6e755d8e73941849559401d20263[m
Merge: 997fd4d 8e0427d
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Jun 5 03:46:29 2013 -0700

    Merge pull request #336 from vishwasbabu/mifosx-413
    
    deleting test cases for document upload

[33mcommit 8e0427d63355fd096b97ccf041ed4fd2a7174569[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Wed Jun 5 16:15:18 2013 +0530

    deleting test cases for document upload

[33mcommit 997fd4d3e23c80d6445481306d770a7cd031e7a1[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Wed Jun 5 14:57:37 2013 +0530

    MIFOSX-416 : display loan type for loan in Client's general details

[33mcommit 4059e1900a2d4bc0a0d5fcf2f430b48d70384dd9[m
Merge: 86a9bad c62ee07
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Jun 5 01:59:05 2013 -0700

    Merge pull request #334 from vishwasbabu/mifosx-413
    
    tracking transactions that repay loan charges

[33mcommit c62ee07d455189fdaa5d7313773e56a29d2ff9d8[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Wed Jun 5 14:22:19 2013 +0530

    tracking transactions that repay loan charges

[33mcommit 86a9badb56e61b8a5ec78417d29ed7d0aed2135a[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Jun 4 14:13:03 2013 +0100

    remove unused import warnings and unused variable warnings.

[33mcommit 16996b5e8c0d728dd842e51e69d126a4d01eabf8[m
Author: Lech Rozanski <lrozanski@soldevelo.com>
Date:   Tue Jun 4 14:48:41 2013 +0200

    MIFOSX-365: Simplify 'Data Tables' functionality

[33mcommit 59580e6f3b0b464a632b81a2e3c4e21c87388ddc[m
Merge: 90169da d203644
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Jun 3 23:22:45 2013 -0700

    Merge pull request #331 from goutham-M/MIFOSX-399
    
    balance amount in savings account showing negative balance(fixed)

[33mcommit d2036447b1bf12810328bf791c0415d5cb53a28e[m
Author: goutham-M <goutham@confluxtechnologies.com>
Date:   Mon Jun 3 21:22:11 2013 +0530

    balance amount in savings account showing negative balance(fixed)
    
    withdrawl amount from savings account, showing negative balance(fixed)

[33mcommit e99834bd83bc0d073ffe9a724dce42bf17932c7e[m
Merge: c332e70 5ce21c9
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Jun 3 05:54:54 2013 -0700

    Merge pull request #329 from vishwasbabu/bug-fix
    
    updating image migration stored proc to work with older versions of mysql

[33mcommit 5ce21c9b325ec80d1221f997da5fd0d6085a471b[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Mon Jun 3 18:21:18 2013 +0530

    updating image migration stored proc to work in older versions of mysql

[33mcommit c332e7037fc3dacf4686474bed995f0c5e0417b4[m
Merge: 68c58da d858221
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Jun 3 01:47:19 2013 -0700

    Merge pull request #328 from vishwasbabu/bug-fix
    
    minor issues in groups after s3 refactoring

[33mcommit d858221e3a8bf8b50a8f146d85a1ee178811db3b[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Mon Jun 3 14:13:07 2013 +0530

    minor issues in groups after s3 refactoring

[33mcommit 68c58da786fc6fc73fcc1a00d9e9e8ea3f1fccca[m
Merge: c05a0dc 0e4d0b4
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Jun 3 01:37:08 2013 -0700

    Merge pull request #327 from vishwasbabu/harshac-MIFOSX69
    
    fix for MIFOSX-396

[33mcommit 0e4d0b4cfcb0b8f7df5230b55d939bd1fdb5b345[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Mon Jun 3 14:04:03 2013 +0530

    fix for MIFOSX-396

[33mcommit c05a0dc1d7ccba461072f813dca18bddc7f94d9d[m
Merge: a18197d bd93ce6
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Jun 2 23:33:47 2013 -0700

    Merge pull request #326 from vishwasbabu/harshac-MIFOSX69
    
    adding amazon aws java sdk as dependency

[33mcommit bd93ce67ea544316369f37b3df6c3104beb390ca[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Mon Jun 3 12:01:52 2013 +0530

    adding amazon aws java sdk as dependency

[33mcommit a18197d15b9227f9f5413f4763fd538eb8beffec[m
Merge: 14c8eab 15311f6
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Jun 2 22:20:36 2013 -0700

    Merge pull request #325 from vishwasbabu/harshac-MIFOSX69
    
    Amazon S3 support and refactoring

[33mcommit 15311f6425e2770acb6d8f3ff9700c0183ab991f[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Mon Jun 3 02:05:43 2013 +0530

    Refactoring document and image management

[33mcommit 9c1f9cd912ea58b42f07da23c963c50292c05a06[m
Author: Harshac <harshac@thoughtworks.com>
Date:   Thu Apr 25 18:31:38 2013 +0530

    Adding ability to upload files on s3.
    To enable file upload on S3 you need to enable the configuration setting in c_configuration table for s3.
    The S3 credentials (access_key, secret_key, bucket_name) in a m_external_services table.
    m_client table is normalised into m_client and m_image table. The relation between m_client and m_image is one-to-one for now.
    To persist the storage type(s3 or filesystem) a column had been added to m_document.
    
    MISOFX-69 Adding db migrations for s3 support
    
    MIFOSX-69 Adding method to return boolean value of S3 config
    
    MIFOSX-69 - Priti/Harsha - Refactoring Code, extracted interface for Documents upload
    
    MIFOSX-69 Refactored to add S3 Support for file upload. Modified saveDocs to throw Document Management Exception instead of IO exception
    
    MIFOSX-69 Adding S3 support for file upload
    
    MIFOSX-69 - Adding S3 support for Image upload
    
    MIFOSX-69 Adding factory to get instance of data store as per s3 configuration
    
    MIFOSX-69 Changing table s3_details to generic tabe for external services, adding column to store the storage type in m_documents table
    
    MIFOSX-69 Fetching S3 details from database
    
    MIFOSX-69 Adding document storage type while creating a document
    
    MIFOSX-69 Normalising client table to create an image table
    
    Refactoring ClientImage code to save client image in m_image table
    
    Refactored code to get document as file
    
    MIFOSX-69 Retreiving documents from S3
    
    MIFOSX-69 Refactored ClientData to store ImageData
    
    MIFOSX-69 Retrieving images from S3, Modifying Base64 to encode from byte array instead of file in CLientImagesApiResource
    
    MIFOSX-69 Refactored code to delete client image
    
    MIFOSX-69 Deleting Images from S3
    
    Refactored DocumentStore to return StoreType from string value
    
    MIFOSX-69 Refactored to move document deletion responsibility to storageType
    
    MIFOSX-69 Giving more appropriate name to getInstance methods of DocumentStoreFactory as per the instance creation criteria
    
    MIFOSX-69 Deleting Documents from S3
    
    MIFOSX-69 Removing unnecessary s3_param from migration
    
    MIFOSX-69 Adding ability to update document and update doc store type
    
    MIFOSX-69 Renaming method to follow java conventions
    
    MIFOSX-69 Adding filename to error log for s3 file retrieval
    
    Adding data migrations and changing the version for migration file

[33mcommit 14c8eabdcc8bd8e5d916fe01d801cb79b232f994[m
Merge: 01d0772 14e5504
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sat Jun 1 12:03:25 2013 +0100

    Merge remote-tracking branch 'lech/MIFOSX-365' into develop

[33mcommit 01d0772261e07743ca7f37ca66cca2316bdfd12f[m
Author: goutham-M <goutham@confluxtechnologies.com>
Date:   Sat Jun 1 15:34:23 2013 +0530

    savings ac getting activated before client activation date fixed

[33mcommit 90169da8e44eb79bae0e95e4fe39baadeded6e48[m
Author: goutham-M <goutham@confluxtechnologies.com>
Date:   Sat Jun 1 15:34:23 2013 +0530

    savings ac getting activated before client activation date fixed

[33mcommit 4dbd0cf7fdcef2f55918f8f56b578b517a53f232[m
Author: goutham-M <goutham@confluxtechnologies.com>
Date:   Fri May 31 19:32:30 2013 +0530

    updating withdraw fee in saving product(exception) fixed

[33mcommit 7478e17f3c4805e88eab7aef7bb9d97ea42f88a5[m
Author: goutham-M <goutham@confluxtechnologies.com>
Date:   Fri May 31 17:22:58 2013 +0530

     updating annual fee amount in savings product(exception) fixed

[33mcommit 3a5b95bc25cadbc5cd7b8a9f1da40941ce94b14c[m
Merge: 6683e0e bc41c58
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri May 31 15:48:18 2013 +0100

    Merge remote-tracking branch 'goutham/MIFOSX-403' into mifosplatform-1.2.1

[33mcommit bc41c5894746e1dd6782609394eb6dec8bd85aa0[m
Author: goutham-M <goutham@confluxtechnologies.com>
Date:   Fri May 31 19:32:30 2013 +0530

    updating withdraw fee in saving product(exception) fixed

[33mcommit b373458f5752b3cc5d11a2896242c3ac49b40843[m
Author: mifoscontributer <mifos.contributer@gmail.com>
Date:   Fri May 31 18:50:18 2013 +0530

    Add multiple credit and debit journalentries with accoutnting rule

[33mcommit 6683e0e9ca74795ff9ee6139faa27e1cf0df9b16[m
Author: goutham-M <goutham@confluxtechnologies.com>
Date:   Fri May 31 17:22:58 2013 +0530

     updating annual fee amount in savings product(exception) fixed

[33mcommit 8ef012826c1d69de0f0446694b805878e120e13c[m
Author: goutham-M <goutham@confluxtechnologies.com>
Date:   Thu May 30 18:39:45 2013 +0530

    days period frequency for locking savings product fixed

[33mcommit eb082d5ec500385c33f75f8c7b393afc10ab1c7b[m
Author: goutham-M <goutham@confluxtechnologies.com>
Date:   Thu May 30 18:39:45 2013 +0530

    days period frequency for locking savings product fixed

[33mcommit 4d473ac4dbaf8405d47b3b9b7777f92aa1c84001[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu May 30 13:20:11 2013 +0100

    update savings api docs for withdrawal and annual fees.

[33mcommit c08d1635629c588bb6689d81be2c8c748f5d22e9[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu May 30 12:50:38 2013 +0100

    MIFOSX-395: provide optional staffId and staffDisplayName to auth response.

[33mcommit 14e55045885c66215a5f5609fe1d7fcaa8ab77ae[m
Author: Lech Rozanski <lrozanski@soldevelo.com>
Date:   Wed May 29 16:09:01 2013 +0200

    MIFOSX-365: Simplify 'Data Tables' functionality

[33mcommit 7f062a394bccb0978026ec1443da679c0fb07755[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed May 29 10:13:48 2013 +0100

    some formating

[33mcommit ffa5435765b0a90e404e9af3703b63b8df5fdc35[m
Author: Anuruddha Premalal <anuruddhapremalal@gmail.com>
Date:   Wed May 29 14:27:39 2013 +0530

    Impelement pagination for SavingsAccounts api

[33mcommit 96789be7aded9cd7dcd5a15acabf07e7d0581bc9[m
Author: mifoscontributer <mifos.contributer@gmail.com>
Date:   Wed May 29 12:05:29 2013 +0530

    Add Predefined Journal Entries with optional tag fields

[33mcommit 42a2428774c44556098f185750e03ab46ebe4c9c[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed May 29 00:11:47 2013 +0100

    MIFOSX-395: initial database changes along with authentication api changes to support organisational roles, with optional ability to link application users with staff members to support role based screen views.

[33mcommit e2c4ce9a1336238fb32f40aca190fb370639246a[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue May 28 21:21:43 2013 +0100

    MIFOSX-365: formatting and add license to files without it.

[33mcommit 721a12c2fa72df149979146b6346cce49e413f1c[m
Author: Lech Rozanski <lrozanski@soldevelo.com>
Date:   Tue May 28 17:17:51 2013 +0200

    MIFOSX-365: Simplify 'Data Tables' functionality

[33mcommit 2d4050de12f55d3d1c604ae3cfeb6e97f4d7d97a[m
Author: mifoscontributer <mifos.contributer@gmail.com>
Date:   Tue May 28 10:43:44 2013 +0530

    Defining accounting rule with Tagged Accounts
    
    Formatting the white spaces

[33mcommit 94150b91ad2ff222f4418cdc1b0460db5ad4bd4c[m
Merge: 6933cdf 17454ce
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon May 27 09:20:51 2013 -0700

    Merge pull request #313 from vishwasbabu/wuyanna-master
    
    Apply annual fees for savings accounts

[33mcommit 17454ce21a9c3a9698d35df8eb29af65da11cda9[m
Merge: 6933cdf bf841eb
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Mon May 27 21:28:29 2013 +0530

    minor changes to applyAnnual Saving fees job

[33mcommit 6933cdfafb696900259eac9870ddce3a9db10103[m
Merge: b5d6c16 afaa4ba
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon May 27 04:12:26 2013 -0700

    Merge pull request #311 from vishwasbabu/mifosx-345
    
    fix for issue with apply charges event

[33mcommit afaa4ba3729b93a7cf3bb00d3111b8d485e9f9ff[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Mon May 27 16:39:28 2013 +0530

    fix for issue with apply charges event

[33mcommit b5d6c163c603af2ac8f9c6cf91bb2884d39695f2[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon May 27 12:43:31 2013 +0200

    update readme for 1.2.0

[33mcommit c1b6e59f0a8f2b1c48488227e2cba7302009529d[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon May 27 12:42:40 2013 +0200

    update changelog for 1.2.0

[33mcommit 0ec17a6c1dcd2060fd9cd7f87bbfdcda51ddfd5e[m
Merge: 7db939a 4216233
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon May 27 00:58:32 2013 -0700

    Merge pull request #310 from vishwasbabu/mifosx-345
    
    updated documentation for savings with accounting integration

[33mcommit 421623359ef0760ab8a785bc1fd2801873fc3f2d[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Mon May 27 13:26:35 2013 +0530

    updated documentation for savings with accounting integration

[33mcommit 7db939a971faa1f7fd5950d5e750a2fa7b1147e8[m
Merge: 042df1a 18d65f2
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun May 26 23:56:21 2013 -0700

    Merge pull request #309 from vishwasbabu/mifosx-345
    
    bug fixes for document upload

[33mcommit 18d65f28906973c9e1ff3271af77dbfb0c243189[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Mon May 27 12:21:36 2013 +0530

    bug fixes for dcument upload

[33mcommit 042df1a3b131ddceeb6ba79762f954fa106c30f7[m
Merge: a55f17c da50121
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun May 26 22:30:30 2013 -0700

    Merge pull request #308 from vishwasbabu/mifosx-345
    
    accounting integration for savings and refactoring

[33mcommit da501214377823961944b189e7b8603fdee83fa2[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Mon May 27 09:21:13 2013 +0530

    accounting integration for savings and refactoring

[33mcommit a55f17c7b5a19f6568f96f257ac4d517a9b39837[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon May 27 01:07:32 2013 +0100

    update dependencies versions (except for hibernate which still causes error with rollback of jdbc transactions).

[33mcommit 7680ca8284cd6c56a1b0d62f3e94f3949fd111a6[m
Merge: ebd1de9 07f4f14
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri May 24 14:08:57 2013 +0100

    merge in 1.1.4 changes into develop branch for 1.2 release.

[33mcommit 07f4f1430dbfca495b02461198edcbd01c0f6933[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri May 24 14:36:41 2013 +0200

    udpate for 1.1.4 release

[33mcommit 3d69e7fe559b46e5250e6333ea81ec12dd750855[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri May 24 14:33:06 2013 +0200

    udpate for 1.1.4 release

[33mcommit ebd1de95fb55d0caa55670c629e2a1642af626e4[m
Author: goutham-M <goutham@confluxtechnologies.com>
Date:   Fri May 24 16:15:34 2013 +0530

    finished document updation for latest changes
    
    chaging style

[33mcommit f86ffd81ab278ebdcfb68476e2ac68511afa0f4e[m
Author: goutham-M <goutham@confluxtechnologies.com>
Date:   Fri May 24 15:00:39 2013 +0530

    removing unused fields in journal entry

[33mcommit 6c5b751f10b492eb969b6e7e8f75f06aa3446483[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu May 23 23:30:45 2013 +0100

    formatting

[33mcommit ee43ea9643b2e412a82ad3d6d9174e3f64d0096a[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu May 23 16:46:49 2013 +0100

    MIFOSX-387: currency should not be included in response of list of product options for loan template.

[33mcommit 5f5074a768ca03f7950fce0953086d811e6c01a2[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu May 23 16:49:03 2013 +0100

    bump up release version for bug release.

[33mcommit 27dc2a96503015bc74ab190ab2852ace027a6ae8[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu May 23 16:46:49 2013 +0100

    MIFOSX-387: currency should not be included in response of list of product options for loan template.

[33mcommit 9cff637e16cacb6896056f5c8c6ab300b1822d9a[m
Author: goutham-M <goutham@confluxtechnologies.com>
Date:   Thu May 23 15:53:10 2013 +0530

    update documentation for Accounting changes

[33mcommit 683ba2ad76c6c862abd56f881e189c03b40515b4[m
Merge: 89a28c8 badfc54
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed May 22 11:46:45 2013 +0100

    Merge remote-tracking branch 'madhukar/MIFOSX-286-DEV' into develop

[33mcommit 89a28c890156e639f51f79d18c299a850d067b42[m
Merge: 418790f 90427e7
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed May 22 10:26:05 2013 +0100

    merge in latest stable 1.1.3 version from master

[33mcommit 90427e7c0b5224e1d041763fe7387c13a15ec0c0[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed May 22 11:22:52 2013 +0200

    udpate changelog 1.1.3 release

[33mcommit 81e02c497d13acdfebd5fa19f419e5e2569c2dba[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed May 22 11:20:02 2013 +0200

    update readme for 1.1.3

[33mcommit badfc547a7d15a1bf44cfb8aa7c2eba50000df6e[m
Author: mifoscontributer <mifos.contributer@gmail.com>
Date:   Wed May 22 13:08:38 2013 +0530

    Add missing db scripts for accounting rule

[33mcommit c896e3d1bdfa432cf1834f9d1b5f6c47d775fbfa[m
Author: mifoscontributer <mifos.contributer@gmail.com>
Date:   Mon May 20 19:11:40 2013 +0530

    Making JournalEntries With predefined accounting rule
    
    Fixing issues with constructor for journalEntryCommand

[33mcommit fb8fd9c91f5126e20c434dcdd53ca87cb57a70fc[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue May 21 16:50:13 2013 +0100

    cherry pick of MIFOSX-368: fix error message being returned based on parameters being passed to api.

[33mcommit 418790f59e1ffc7927a0c757c13589af782e8d82[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue May 21 16:50:13 2013 +0100

    MIFOSX-368: fix error message being returned based on parameters being passed to api.

[33mcommit 14251f425bc35bc444550fd983754cd5b2d44b54[m
Merge: c9976e9 7b408b9
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue May 21 13:48:46 2013 +0100

    Merge remote-tracking branch 'goutham/Mifosx-286' into develop

[33mcommit af8a0b4209b5e9a0bc361879b4898cf42266e74e[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue May 21 13:42:56 2013 +0100

    MIFOSX-379: check for invalid currencies when submitting new loan application.

[33mcommit c9976e92fadbc7b66d9465b97d2c2e771bf32a1a[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue May 21 13:42:56 2013 +0100

    MIFOSX-379: check for invalid currencies when submitting new loan application.

[33mcommit 7b408b9bc6133e0e5f444bed817ac9ef0649fa24[m
Author: goutham-M <goutham@confluxtechnologies.com>
Date:   Tue May 21 12:02:37 2013 +0530

    Updating Accounting Rule json format

[33mcommit e3a0aa71a4d1498b69c2a1e64b240746d6df721d[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon May 20 15:29:30 2013 +0100

    MIFOSX-369: validate client activation date on activate api also.

[33mcommit 5b53c7f8ed08785296bedcec0115dd0fdd602f8e[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon May 20 15:29:30 2013 +0100

    MIFOSX-369: validate client activation date on activate api also.

[33mcommit 9705761bdb1e4690d888a2ed738d552d8f9a7152[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon May 20 14:32:53 2013 +0100

    MIFOSX-367: fix retrieve all filtered by loan account no.

[33mcommit 6a6f576a614c10598852430f815a818e13865bff[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon May 20 14:32:53 2013 +0100

    MIFOSX-367: fix retrieve all filtered by loan account no.

[33mcommit 2d0bc0390cd633bf74d8a9eb10f0b834d747722b[m
Merge: 813b829 2ef76e1
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon May 20 05:58:48 2013 -0700

    Merge pull request #300 from johnw65/mifosx-354C
    
    mifosx-354 2 reports

[33mcommit 2ef76e174ff656f4d0ee5d89897172f1aa55bc7f[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon May 20 13:52:50 2013 +0100

    mifosx-354 2 reports

[33mcommit df8a2eb4a2c03dc849fe3760ed0ae4b204211bd4[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon May 20 13:14:40 2013 +0100

    update release numbers.

[33mcommit 813b8292b09ce5235a80a01e778e2f06849cc98b[m
Merge: 1173aa8 c3cc584
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sun May 19 23:21:30 2013 +0100

    Merge remote-tracking branch 'goutham/MIFOSX-286' into develop

[33mcommit 1173aa855d0980d0e471934e8e89565a8ea42eb7[m
Merge: 2582f89 b859ea3
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sun May 19 12:32:28 2013 +0100

    merge in 1.1.2 bug release changes and fix conflicts

[33mcommit b859ea3c3ac1fee6d62e4be0fbff56720beeb845[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sun May 19 12:29:29 2013 +0100

    update api docs to 1.1.2 version.

[33mcommit 65efe649b23b9fa041922ed13830b885ed425e82[m
Merge: 2b5b324 56d7761
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sun May 19 12:27:57 2013 +0100

    Merge branch 'master' into mifosplatform-1.1.2

[33mcommit 56d776155433597e4857d7195976b60080c3212d[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sun May 19 13:23:36 2013 +0200

    udpate changelog for 1.1.1 and 1.1.2 releases

[33mcommit 17c7eb8da4faf8d7d6fed62e218a302af47d7d98[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sun May 19 13:17:28 2013 +0200

    update readme for 1.1.2

[33mcommit 2b5b3249a6500d47539652eee0eb0f3fa4880a59[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sun May 19 12:10:17 2013 +0100

    update release number to 1.1.2

[33mcommit a11161208dbea21f63343a9b8b923f64e96d7155[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sun May 19 12:07:17 2013 +0100

    MIFOSX-364: ensure valiation check on loanProductId happens first.

[33mcommit 14968ba2208a1689a475e6ee14b12236aa983db0[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue May 14 03:30:08 2013 +0100

    mifosx-353 datetime column type not andled by runreports api

[33mcommit c3cc584c02fabefcc576a776cb994b8b0b6c4a78[m
Author: goutham-M <goutham@confluxtechnologies.com>
Date:   Wed May 15 15:24:21 2013 +0530

    Defining accounting rules for organisation
    
    Defining Accounting Rules
    
    Permissions for accounting rules

[33mcommit 2582f8925cf4fb4be4ea2ba6b8bccde5edd40802[m
Merge: f91bbc9 7343b54
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri May 17 04:44:24 2013 -0700

    Merge pull request #297 from michalwisniewski/MIFOSX-328
    
    MIFOSX-328 test FIX

[33mcommit 7343b54bd8cf62b064220f398b7ddcddec246d7e[m
Author: Michal Wisniewski <mwisniewski@soldevelo.com>
Date:   Fri May 17 12:03:03 2013 +0200

    MIFOSX-328 test FIX

[33mcommit f91bbc9e5d37201d841fa45457b830e9b2a438e6[m
Merge: 2725ff1 4d68203
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu May 16 07:33:02 2013 -0700

    Merge pull request #296 from michalwisniewski/MIFOSX-328
    
    MIFOSX-328 Loan approval date should not be earlier to the Client activa...

[33mcommit 4d6820333f48403f07f4ac3b26b517171d7db65a[m
Author: Michal Wisniewski <mwisniewski@soldevelo.com>
Date:   Thu May 16 15:34:43 2013 +0200

    MIFOSX-328 Loan approval date should not be earlier to the Client activation date

[33mcommit 2725ff1c4c0dfe0372e3a1b57c9002bc13e837d2[m
Merge: fe8992e a445854
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu May 16 03:50:38 2013 -0700

    Merge pull request #294 from ashok-conflux/MIFOSX-356
    
    MIFOSX-356: modify api call to unassignStaff from a group

[33mcommit fe8992ee1963ac0d226a9479c968b4aece4f4f5f[m
Merge: 5d0feae 8f13475
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu May 16 03:48:22 2013 -0700

    Merge pull request #295 from vishwasbabu/mifosx-223
    
    updating api docs with examples of payment channels usage during disburs...

[33mcommit 8f134750763bff29c85914e1abc691e37c8b872e[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Thu May 16 16:15:19 2013 +0530

    updating api docs with examples of payment channels usage during disbursements and repayments

[33mcommit a44585454ea28e730faf2dce695f72b7faf43670[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Thu May 16 14:38:18 2013 +0530

    MIFOSX-356: modify api call to unassignStaff from a group

[33mcommit 5d0feaece1fa157ddd5e2fa64beae55c177d66a2[m
Merge: f5b2a27 a60b2f2
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed May 15 03:37:57 2013 -0700

    Merge pull request #293 from vishwasbabu/mifosx-223
    
    ensure payment details are copied over while duplicating transactions

[33mcommit a60b2f221d65e0bc82b9376c33ebc6ac9c1f3f49[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Wed May 15 15:58:15 2013 +0530

    ensure payment details are copied over while duplicating transactions

[33mcommit f5b2a2762878e961291873699df24bc666d1d428[m
Merge: fd63749 5596efc
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed May 15 02:49:38 2013 -0700

    Merge pull request #292 from vishwasbabu/mifosx-223
    
    bug fixes for mapping payment channel to ledger accounts

[33mcommit 5596efcf552214a9029b87e427d7d6ac03328a5c[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Wed May 15 15:10:03 2013 +0530

    bug fixes for mapping paymet channel to ledger accounts

[33mcommit fd6374908506c1bcf5a9df2352949d422278a325[m
Merge: 4de397c bee72af
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Tue May 14 04:46:33 2013 -0700

    Merge pull request #290 from vishwasbabu/mifosx-223
    
    fixes for accepting user generated payment types

[33mcommit bee72af0967c53b4c6285b055ee5e3723f420634[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Tue May 14 16:58:09 2013 +0530

    fixes for accepting user generated payment types

[33mcommit 4de397cf1c637ea9d4cde5ef669b188799232648[m
Merge: 133590c dbde325
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon May 13 23:43:12 2013 -0700

    Merge pull request #286 from goutham-M/MIFOSX-338-FIXES
    
    Api Documentation Updation for GLAccount recent changes

[33mcommit 133590c54a12a180af48c6d13bc77512f1a65fee[m
Merge: 58900e4 dd5a1cf
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon May 13 23:42:04 2013 -0700

    Merge pull request #289 from vishwasbabu/mifosx-223
    
    user configurable payment types and related accounting changes

[33mcommit dd5a1cf9950969ced347d58a015efa004f24ed82[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Tue May 14 12:08:17 2013 +0530

    user configurable payment types and related accounting changes

[33mcommit dbde325c27d2d5b11806dca8f1e0e7acbdf1244b[m
Author: goutham-M <goutham@confluxtechnologies.com>
Date:   Tue May 14 10:33:23 2013 +0530

    Formatting apiLive.htm

[33mcommit 58900e43759c929d408dab1360b8aa826d66e224[m
Merge: 2570926 63a1310
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon May 13 19:32:38 2013 -0700

    Merge pull request #288 from johnw65/mifosx-353
    
    mifosx-353 datetime column type not andled by runreports api

[33mcommit 63a1310d4423b6a1f6d79978c1e45f0251f02427[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue May 14 03:30:08 2013 +0100

    mifosx-353 datetime column type not andled by runreports api

[33mcommit 25709269b674127d4647a734f73f82722af90009[m
Merge: 86a123b 7987c29
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon May 13 18:33:19 2013 -0700

    Merge pull request #287 from johnw65/mifosx-354
    
    mifosx-354 Group Summary Details (I'll be in big trouble if I've merged this wrongly).

[33mcommit 7987c29960041c57c5c6076a29e7189259b58f02[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue May 14 02:26:43 2013 +0100

    mifosx-354 Group Summary Details

[33mcommit 2634a1a464078852a45dc06e1619aab36b23dad7[m
Author: goutham-M <goutham@confluxtechnologies.com>
Date:   Fri May 10 19:23:11 2013 +0530

    Api Docuentation Updation for GLAccount recent changes

[33mcommit 86a123b702f2bcda3c866197774dfd3146973075[m
Merge: 24bbd13 021a6b3
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sun May 12 14:12:51 2013 +0100

    MIFOSX-348: merge and fix conflict for bug fix.

[33mcommit 021a6b38de1eb4d59f5e88fc6a9da3602088122a[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sun May 12 13:55:18 2013 +0100

    update properties to 1.1.1 release

[33mcommit 3e10423e867003d0a631edd7ad816c67b7287580[m
Author: Anuruddha Premalal <anuruddhapremalal@gmail.com>
Date:   Sun May 12 18:03:36 2013 +0530

    Allow prettyfyiing on paginated data

[33mcommit 24bbd13716fb595d3a231667f6c99b1c23fd9815[m
Merge: 135933c 9598891
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sun May 12 10:35:01 2013 +0100

    MIFOSX-331: merge in Kushas work for 1.2.0 release and fix merge conflict.

[33mcommit 135933c65ee7174a60ed223dad3df2dc4ee65987[m
Merge: 596ff16 dca5d0b
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sun May 12 00:00:09 2013 +0100

    Merge remote-tracking branch 'tw_kaizer/master' into develop

[33mcommit 596ff161b0dc5c103c34e6f8de5ce1994afb733e[m
Merge: 4e489fa 8a86289
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sat May 11 23:53:50 2013 +0100

    Merge remote-tracking branch 'goutham/MIFOSX-338-FIXES' into develop

[33mcommit 4e489fa3e6fb4088e5d388d80181ee919e339bc0[m
Merge: c7f12d9 e14a8bb
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sat May 11 15:24:29 2013 +0100

    Merge remote-tracking branch 'ashok/MIFOSX-310' into develop

[33mcommit bf841ebb16979658cdc00f97cc6b80e43204d6bb[m
Author: wuyanna <yutao@yutaotekiMacBook-Pro.local>
Date:   Fri May 10 22:00:55 2013 +0800

    mifosx 344

[33mcommit c7f12d9eb3576d3ba56ab1a4b7bf63067db7ecc4[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri May 10 13:02:22 2013 +0100

    update release number.

[33mcommit 959889175e8d224ced53a3bc91020dae91c919cf[m
Author: kushagrasingh <kushagresingh@gmail.com>
Date:   Fri May 10 13:11:19 2013 +0530

    Fixed Issue331 errors

[33mcommit dca5d0b36bd646b8ae91fab2187f37071b77a7bf[m
Author: Kaizer Poonawala <kaizersh@gmail.com>
Date:   Fri May 10 12:38:45 2013 +0530

    Fixed incorrect names while logging in Jobs and renamed scheduler bean ids.

[33mcommit 8a86289c9df87b18cb8d9efb9a89efa6f6d2fe28[m
Author: goutham-M <goutham@confluxtechnologies.com>
Date:   Thu May 9 21:12:02 2013 +0530

    Update GLAccount and regenerate hierarchy of GLAccount

[33mcommit 4712a1e3a9e675c5ac6e5d285bb4b917f9922222[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu May 9 16:50:03 2013 +0200

    Update README.md

[33mcommit 38dd9a7e09a8f7442a5341a1df3321cbf23f4d4c[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu May 9 16:48:54 2013 +0200

    Update CHANGELOG.md

[33mcommit 485d6f10e3e864e6260a41de75d31d08106ff6ba[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu May 9 15:29:23 2013 +0100

    fix null pointer when creating a group with clients.

[33mcommit 9755b94a48c2b5b3061447830edf801ea0b8537d[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu May 9 15:01:25 2013 +0100

    MIFOSX-338: fix issue with failing to create an account with empty/null tagId value.

[33mcommit e14a8bbe1fda8ea324bd6095622ffb63dc662773[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Fri May 3 22:49:49 2013 +0530

    Generate center/group collection sheet using meeting calendar dates

[33mcommit f206f20eca720358ed73bc13ecd9c21357d9c1ee[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu May 9 13:50:13 2013 +0100

    MIFOSX-226: add data scoping on loans for retrieveAll and retrieveOne

[33mcommit d097ef966c6e6bed2b9f9b679a49e141f89bf501[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu May 9 12:05:03 2013 +0100

    MIFOSX-339: update api docs to document way of by passing limit on list capability of centers,groups,clients and journal entries api.

[33mcommit 57791b8f37c5cbfbfdc244d875caa15b87513985[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu May 9 11:30:46 2013 +0100

    MIFOSX-339: use common search parameters for loans and journal entries retrieve all as it contains logic and api for dealing with limits.

[33mcommit 2dba94f198d934a2358a7747f47e89b371e56454[m
Merge: 8145887 f11fe9c
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu May 9 10:22:59 2013 +0100

    Merge remote-tracking branch 'soc_anuruddha/mifosx-339' into mifosplatform-1.1.0

[33mcommit f11fe9cdeff7a1abb84d6f434230b0f128001b94[m
Author: apremalal <anuruddhapremalal@gmail.com>
Date:   Thu May 9 14:19:20 2013 +0530

    enabled retrieve all items from paginated end points

[33mcommit 8145887f3182390c965bc27ecbc67901cac29159[m
Merge: 1272e5c 97b8dab
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed May 8 21:10:30 2013 +0100

    MIFOSX-331: merge Khusas work around calendars that was revied by Ashok.2

[33mcommit 1272e5c8523d057593a45fbd206ba02e8179857b[m
Merge: ce22d5d 40126a0
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed May 8 20:51:30 2013 +0100

    MIFOSX-314: merge in aviks work and fix conflicts and format code.

[33mcommit ce22d5dcb5813482753eb6072018b34600bf88b3[m
Merge: ce11cbb 8b4de07
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed May 8 20:24:30 2013 +0100

    MIFOSX-338: merge in gouthams work and fix conflict.

[33mcommit 8b4de075a2cd7a6caf7dcf9e984fdfd7f64864c2[m
Author: goutham-M <goutham@confluxtechnologies.com>
Date:   Mon May 6 18:09:40 2013 +0530

    SQL Retrievals for Hierarchical ChartofAccounts
    
    Fixing Naming Convention for COA options
    
    Tag ID for GLAccounts

[33mcommit ce11cbbeb5c3bb5a6a40fbb8b7ac22515819ed3f[m
Merge: 6c28c68 6c04f5d
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed May 8 15:09:16 2013 +0100

    Merge remote-tracking branch 'soc_anuruddha/mifosx-324-loans' into mifosplatform-1.1.0

[33mcommit 0d18ddc034e3223d7ccea5e3efac21ad2864c472[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed May 8 14:42:57 2013 +0100

    MIFOSX-343: update quick typo

[33mcommit 6c28c684c2357458ed2889902870755193de9b8f[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed May 8 14:37:31 2013 +0100

    MIFOSX-342: bring in work from yanna with some modifications.

[33mcommit 40126a0ae2e20e8531ed6d36bc66f19fa6c1bf65[m
Author: avarice010 <avikganguly010@gmail.com>
Date:   Wed May 8 18:56:42 2013 +0530

    MIFOSX-314 Make adding new accounts of a particular type easier

[33mcommit 4b0307b293bc46778d71fe88c5a7c3bfbb07ac0c[m
Merge: f9ff58b 5332d4b
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed May 8 13:12:17 2013 +0100

    merge in 1.0.1 branch

[33mcommit 5332d4b98a683b4564edb25fc5178b50e9e9b642[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed May 8 14:07:32 2013 +0200

    Update README.md

[33mcommit 28c9a0ba53cbe600eb84d146f569b714f0910184[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed May 8 14:06:16 2013 +0200

    update for 1.0.1 release

[33mcommit 0fb77461e9a0787d2f624f497c023d809605093e[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed May 8 13:00:17 2013 +0100

    MIFOSX-343: Fix issue with retrieve audit entry not handling ids correctly.

[33mcommit 6c04f5d5200639010a719abac4feec6d0072aa60[m
Author: Anuruddha Premalal <anuruddhapremalal@gmail.com>
Date:   Wed May 8 15:49:53 2013 +0530

    implemented pagination and sorting for loans api

[33mcommit 97b8dab831cf16ced8adc7ccb2c6eab28a6a194d[m
Author: kushagrasingh <kushagresingh@gmail.com>
Date:   Wed May 8 12:11:42 2013 +0530

    Issue-331

[33mcommit e55a37fe1adab93ccc1163199f7558ea9aca5d8f[m
Author: kushagrasingh <kushagresingh@gmail.com>
Date:   Wed May 8 12:03:17 2013 +0530

    Issue-331

[33mcommit db9a5b6d16d087a7c623f573dc0847a5333de9aa[m
Author: kushagrasingh <kushagresingh@gmail.com>
Date:   Wed May 8 11:15:49 2013 +0530

    Issue-331

[33mcommit f9ff58b4beaeb8b828505e8db8e086b6cef34e1a[m
Merge: 23a24e8 de818af
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed May 8 00:22:27 2013 +0100

    Merge remote-tracking branch 'ashok/MIFOSX-334' into mifosplatform.1.1.0

[33mcommit 23a24e8607cb24fc2d2af503e0f960c3102c4ecf[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue May 7 23:53:33 2013 +0100

    update gradle.properties

[33mcommit 495c4b71098e4488d5268d5a6f4a7c0ab5ef1be3[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed May 8 00:17:45 2013 +0200

    Update INSTALL.md

[33mcommit 581e00328571484de87eb9a0ac70ac1538e4f551[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed May 8 00:16:49 2013 +0200

    Update INSTALL.md

[33mcommit d1031555ce6ebc058dad2fbbb8be4b751c349111[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue May 7 23:51:21 2013 +0200

    Update INSTALL.md

[33mcommit e63c26194cb90a3c39dda2911228a0d0b448d56c[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue May 7 23:50:07 2013 +0200

    Update INSTALL.md

[33mcommit f8f697cc1799f0b2119f0fda0b41d75064ace8c1[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue May 7 22:44:02 2013 +0100

    remove eclipse warning

[33mcommit 052eb845c933b96a33c34bcb9b9dcf7fbe549c52[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue May 7 22:34:15 2013 +0100

    MIFOSX-329: refactor audit work for replacing ids of offices and clients for better display.

[33mcommit c99d2f6114cd32813a6c4393798cd21a85a30157[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue May 7 19:11:32 2013 +0100

    switch logging to pick up from catalina.base which is /var/lib/tomcat7 on ubuntu.

[33mcommit 49eed78d579d9d33b108358792768093343866b9[m
Merge: c5f7b9c d71d9b6
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue May 7 10:03:44 2013 -0700

    Merge pull request #269 from wuyanna/master
    
    MIFOSX-329 updated

[33mcommit d71d9b6b2e88a843a6de6130a75fa646885d8199[m
Author: wuyanna <yutao@yutaotekiMacBook-Pro.local>
Date:   Wed May 8 00:53:34 2013 +0800

    mifosx329 updated

[33mcommit de818af7b396e74bc64faf4d2b5aa5f1d54f59a1[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Tue May 7 20:41:13 2013 +0530

    updated api doc with commands associateClients and disassociateClients in group API

[33mcommit c5f7b9c325669a60b0aea473ca89eec911ab0570[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue May 7 17:04:52 2013 +0200

    Update INSTALL.md

[33mcommit a52698c8db1e68ef470cef8c3146d9e52ef957df[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue May 7 14:27:26 2013 +0100

    update build and docs.

[33mcommit c594a001636279d36ef97734d884017097e946a1[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue May 7 15:04:28 2013 +0200

    Update INSTALL.md

[33mcommit 6810906feb13fd60cc8479d7d8138a25f12f2378[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue May 7 14:55:25 2013 +0200

    Update INSTALL.md

[33mcommit a76a268560d5372f44cac470c3402dfb47c760dd[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue May 7 14:02:47 2013 +0200

    Update INSTALL.md

[33mcommit 1114a3b5cadbde7b3b8e3853f128ca642e6d0a71[m
Author: wuyanna <yutao@yutaotekiMacBook-Pro.local>
Date:   Tue May 7 12:43:58 2013 +0100

    MIFOSX-329: bring in contribution from gsoc student yanna.

[33mcommit 7b7b2cf1da90909a9d668c979c0bc8ad025ba978[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue May 7 12:40:27 2013 +0100

    update dist task to pick up on sql files needed for first time install or upgrade.

[33mcommit 381b096cac7dce47b3f268b74e18fe665df4be8f[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue May 7 13:21:05 2013 +0200

    Update INSTALL.md

[33mcommit 295eefc57907c00538ffdbc59cc175a9673df680[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue May 7 11:43:01 2013 +0100

    add INSTALL file for installation instructions.

[33mcommit 21b7eeca33a2e83e241fe070b7559af80df3a06f[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue May 7 12:16:03 2013 +0200

    Update CONTRIBUTORS.md

[33mcommit 9bc31cf8ae4002fd36e567c4345cea87eed60769[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue May 7 11:55:59 2013 +0200

    update changelog for 1.0.0 release

[33mcommit a93d307e20a84850558739d83f2dea93eaaed848[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue May 7 11:39:32 2013 +0200

    Update README.md

[33mcommit e8b8cf4f8ae934dba0f0ddf20c804c45d11e494c[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue May 7 10:18:54 2013 +0100

    MIFOSX-195: some formatting and clean up whilst testing this issue.

[33mcommit d8913ab75896acf344a590c886d136e6ba90e822[m
Merge: 3fbd329 aaeec4e
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue May 7 08:20:56 2013 +0100

    Merge remote-tracking branch 'rtuck/MIFOSX-254'

[33mcommit aaeec4e9f3a55ad31383d6bf416ee13fd10bcdd8[m
Author: Robert Tuck <robt@majormajor.me.uk>
Date:   Mon May 6 22:43:39 2013 +0100

    MIFOSX-254 Make JDBC Pool Configuration Setting Work Better
    
    Fix time-of-check-time-of-use bug in
    TomcatJdbcDataSourcePerTenantService.java
    Close database connection in ReadReportingServiceImpl.java

[33mcommit 3fbd32918f6cb8ba1acaddfa2c6b808388658f4f[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon May 6 23:18:29 2013 +0100

    mifosx-195 report maintenance (fixed)

[33mcommit 5a2acd7897350ad149ba741cadcf024a40373ec4[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon May 6 21:13:35 2013 +0100

    update gradle properties and dist task for release artifact.

[33mcommit b93cdaaa82a52df673cfd422ac0a3de86d131540[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon May 6 20:51:57 2013 +0100

    update license on files.

[33mcommit 95647fea1be16250845dcb602e831317c4560cd0[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon May 6 20:26:43 2013 +0100

    MIFOSX-322: Update api docs for centers, groups and journal entries list methods.

[33mcommit ef1d0e2219214e4194a41cf1012f94c2184284e9[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon May 6 19:19:38 2013 +0100

    MIFOSX-336: update api docs request and response examples for loanType parameter.

[33mcommit 4319fdb5121fd9063c36db2d70e05c4a326df711[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon May 6 19:05:57 2013 +0100

    mifosx-195 report maintenance

[33mcommit 176facea13cf21e1bfcb91a22102ced8264bb4b2[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon May 6 14:55:44 2013 +0100

    wip mifosx-195 report maintenance changes

[33mcommit 50628ad9438d388207e6cbd1665706b4283d61aa[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon May 6 13:39:14 2013 +0100

    update loan loan charge functionality to show percentage value correctly if percentage of amount type of charge.

[33mcommit 187c37437e9bde8b23e2ffe78ce91ac08d41e0c9[m
Author: Anuruddha Premalal <anuruddhapremalal@gmail.com>
Date:   Sun May 5 21:52:54 2013 +0530

    Upgraded the journalentry integration test to consume paginated data

[33mcommit d5b790f2027e7953682198525faffc79b3587c05[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sun May 5 17:33:26 2013 +0100

    wip mifosx-195 report maintenance changes

[33mcommit d17aec1eb29ad1ea98ccc18f0f362be5c2f38ee6[m
Merge: 99adb3e 2dc27fa
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sun May 5 13:10:48 2013 +0100

    Merge remote-tracking branch 'soc_anuruddha/mifosx-326'

[33mcommit 99adb3e2fa5bf752062b0ced88006e882e2b39fc[m
Merge: e0119ea d391570
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sun May 5 13:09:17 2013 +0100

    Merge remote-tracking branch 'ashok/MIFOSX-334'

[33mcommit e0119ea2306cc09011818072e7627583f3f47b2e[m
Merge: 857d7a7 56fdd17
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sun May 5 12:56:04 2013 +0100

    Merge remote-tracking branch 'soc_anuruddha/mifosx-322,323'

[33mcommit 2dc27fa4eb7340e1b135a365d0f530aaa2b48e9a[m
Author: Anuruddha Premalal <anuruddhapremalal@gmail.com>
Date:   Sun May 5 13:36:47 2013 +0530

    Implemented severside sorting and pagination for journal entries

[33mcommit d3915703a549ed9677567c0b1d40b9b2207c0f49[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Sat May 4 18:15:59 2013 +0530

    wip:added separate commands for associating/disassociating a client to group

[33mcommit 857d7a70be3ac313aef535c391c8dc07fdd44c52[m
Merge: d5ef103 93db849
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sat May 4 05:10:53 2013 -0700

    Merge pull request #257 from vishwasbabu/mifosx-286
    
    deleting missed out code for accounting autoposting

[33mcommit 93db849d394171f7ab0ba6fbae21875664b7400f[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Sat May 4 17:32:41 2013 +0530

    deleting missed out code for accounting autoposting

[33mcommit 56fdd170ca9e5b85b34bafba197401fab5d458f7[m
Author: Anuruddha Premalal <anuruddhapremalal@gmail.com>
Date:   Sat May 4 11:28:55 2013 +0530

    fixed the search centers and search groups issue,
    'group level was not appended when the sqlQueryCrieteria is not empty

[33mcommit 0caf255a604358ce603a01f43cff0e4a2f85e2b4[m
Author: Anuruddha Premalal <anuruddhapremalal@gmail.com>
Date:   Sat May 4 07:50:13 2013 +0530

    Fixed the clientmapper issue

[33mcommit 17cc35d27a11b9c1afe41cbb5df24ff83b7714f7[m
Author: Anuruddha Premalal <anuruddhapremalal@gmail.com>
Date:   Fri May 3 22:03:18 2013 +0530

    Implemented back-end methods for pagination and sorting
    
    MIFOSX-322, MIFOSX-323

[33mcommit d5ef1033c358f7339adfdae279a6c6d2e8ff08e6[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sat May 4 00:08:46 2013 +0100

    MIFOSX-335: Fix error with adding loan charge to loan application.

[33mcommit 339b95e6152380918dce8fa4ffedc77e8ca3323f[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri May 3 17:39:42 2013 +0100

    MIFOSX-322: update apidocs, remove legacy sql files.

[33mcommit 23b079f4a66a5dfa0774c5db2972524ac283a5b3[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri May 3 16:18:14 2013 +0100

    MIFOSX-322: update apidocs for client pagination and sorting.

[33mcommit 0070dfa611e4ea7e91803c63c9cff80bc59d1b87[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri May 3 14:51:19 2013 +0100

    MIFOSX-322: update api docs.

[33mcommit 950f6a5f44fa1eac84960cb472f387f06a67a009[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri May 3 13:27:39 2013 +0100

    MIFOSX-333: ensure authenticaton only returns roles of the authenticated user.

[33mcommit 92f3c1474e922365daac2873e7d5e7273746f14a[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri May 3 13:19:49 2013 +0100

    MIFOSX-322: update api docs to show API in release 1.0.0 and API still in beta for upcoming releases.

[33mcommit cedd05b0bc8afa12be52e441ac6109d272a6595d[m
Merge: 5d9a93c 61b6bbf
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri May 3 04:45:27 2013 -0700

    Merge pull request #255 from vishwasbabu/mifosx-286
    
    removing autoposting code

[33mcommit 61b6bbf3f9fc66277c64caea11fe010515581997[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Fri May 3 17:11:53 2013 +0530

    removing autoposting code

[33mcommit 5d9a93c7d7503bb83ae808631d57a59b93eda6d0[m
Merge: 74a5caf 574cd2f
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri May 3 00:54:33 2013 -0700

    Merge pull request #253 from vishwasbabu/master
    
    fixing flyway format for referenceNumber sql migration

[33mcommit 574cd2f7d8f33e9569b33b8dbeb7398ce1f9c760[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Fri May 3 13:10:02 2013 +0530

    fixing flyway format for referenceNumber sql migration

[33mcommit 74a5cafc6c32e80faeed5b5f025818e25952ff19[m
Merge: 5573a0f 834ee8f
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri May 3 00:15:31 2013 -0700

    Merge pull request #252 from goutham-M/MIFOSX-307
    
    MIFOSX-307

[33mcommit 5573a0f8c492f27677cf78dca4257aa9e45f6939[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu May 2 23:54:41 2013 +0100

    MIFOSX-217: scheduled job to update m_loan tables summary,derived fields and arrears,ageing details.

[33mcommit 834ee8f76e47d599ae86846ae4cfdeafb728fca1[m
Author: goutham-M <goutham@confluxtechnologies.com>
Date:   Thu May 2 15:52:47 2013 +0530

    refrenceNumber added to journal entry
    
    add referenceNumber field to acc_gl_journal_entry sql script

[33mcommit 4826c181a7251a202f08e404f77d05c226eee30d[m
Author: Arjun K <arjunk@thoughtworks.com>
Date:   Thu May 2 10:10:30 2013 +0100

    MIFOSX-158: squash arjunk/kaizer commits into one on scheduling spike.

[33mcommit 03f4644a613116416b712ce6abc5fadac97e6a2a[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Thu May 2 11:25:17 2013 +0530

    MIFOSX-302 updated api doc with loanType and templateType details for loan

[33mcommit cf3c030db7d2e2c4e4050442816edef54e1bd2a1[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu May 2 02:20:23 2013 +0100

    MIFOSX-246: add support for annual fees on product and savings account. need to introduce overnight process to check and update annual fees.

[33mcommit f107d185e84020cdbc11deccf66f549b2b957b8f[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu May 2 01:56:32 2013 +0100

    MIFOSX-321: tidy up some of backend implementation for client pagination and sorting example.

[33mcommit c73ac954662a90bb507de893d92b942ed0ab08d1[m
Author: Anuruddha Premalal <anuruddhapremalal@gmail.com>
Date:   Wed May 1 16:52:02 2013 +0530

    Changed the client endpoint methods to retrieve clients

[33mcommit 20bcd8bd6c43845a8c49837a04cb1d526a190624[m
Author: Anuruddha Premalal <anuruddhapremalal@gmail.com>
Date:   Tue Apr 30 09:01:41 2013 +0530

    update pagination methods to utilize search and order the results.
    
    optimized data fetching.

[33mcommit 7476d9775b92cb7ee93f5d8357a3e22dd87bbaf8[m
Author: Anuruddha Premalal <anuruddhapremalal@gmail.com>
Date:   Thu Apr 25 17:37:43 2013 +0530

    Updated the method names

[33mcommit b70f2e06b47b77bac80953ce968ede938593fe80[m
Author: Anuruddha Premalal <anuruddhapremalal@gmail.com>
Date:   Thu Apr 25 17:17:33 2013 +0530

    draft implementation of server side pagination for retriveOne

[33mcommit b35d7a62499a75c014b173b244cc556bf4e0a2a7[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Tue Apr 30 20:51:27 2013 +0530

    wip:accounting stubs for autoposting and abstractions

[33mcommit b27b574d7661c1cba81cde1e34ffcd187f028268[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Apr 30 10:10:12 2013 +0100

    bump up release version.

[33mcommit f9f55b8b3a7252439883f2ac9b98b65ddd11362e[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Apr 30 00:48:23 2013 +0100

    MIFOSX-311: cherry pick of fix from 0.11.4 branch.

[33mcommit bc85e81316dbf8e5756d0ce7e4321ec9a448237a[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Mon Apr 29 16:17:44 2013 +0530

    fixed MIFOSX-301 issues and added loan type and template type to loan

[33mcommit 1f7cd4d293edc2bc8d2b83ade3d08caa65fb2895[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon Apr 29 10:22:46 2013 +0100

    update api-docs (mostly reports tempplate)

[33mcommit a7556b17d8b44b8c05a6484ecab3b65852d36cb2[m
Author: Michael Vorburger <mike@vorburger.ch>
Date:   Sun Apr 28 23:01:55 2013 +0200

    Added *~ to .gitignore, for temporary files created e.g. by gedit

[33mcommit 92201291424cba0eddaf53c4dd28c3081bb623b1[m
Author: Michael Vorburger <mike@vorburger.ch>
Date:   Sun Apr 28 22:56:54 2013 +0200

    replaced non UTF-8 apostrophe to avoid 'error: unmappable character for encoding UTF8' on Linux

[33mcommit 09bddb7bf7beaae715294b621c005c3fd039ea43[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sun Apr 28 03:05:14 2013 +0100

    wip mifosx-195 add list of parameters to reports template

[33mcommit a3a40f5a4521b6edbc4a8a1863e56f1bb693e34c[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri Apr 26 17:29:40 2013 +0100

    fixed mifosx-304 deleting loan product charges not working

[33mcommit 44cef96643ceb167dc30ecea3a96d9ccaa592435[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Apr 26 10:13:39 2013 +0100

    MIFOSX-246: support for withdrawal fees on savings accounts.

[33mcommit 51d63b6db743100fd242cde957f5317532cef2ac[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed Apr 24 20:56:45 2013 +0100

    rename migration v25 file properly

[33mcommit c742ca0519167bf91f816332d5b98bece72f8c74[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed Apr 24 19:17:21 2013 +0100

    fix mifosx-303

[33mcommit f93cf1b6206ab0bf0784ffc8eb2a2983561eb1ac[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Wed Apr 24 09:08:03 2013 +0530

    fixed MIFOSX-301 to select a valid disbursement date

[33mcommit 46c5565fb993fecec97ddfa82de72b77e4db4101[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Apr 23 11:52:45 2013 +0100

    MIFOSX-250: interest posting/crediting added.

[33mcommit db35e26d0a3bac5280d28e85112ef07460f72bda[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Tue Apr 23 13:07:37 2013 +0530

    Fixed MIFOSX-301, modified validation conditions for calendar fields and loan disbursement date

[33mcommit a46c9be5b88032a12f3545817ce104df0c225e96[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Apr 22 12:57:49 2013 +0200

    Update README.md

[33mcommit 52ceaf9f9859c1681d072bdf921dcf28c5d349bc[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Apr 22 12:56:58 2013 +0200

    update notes on 0.12.0.beta release

[33mcommit 1ee775998d9217a644b6f7f291df1883f1fcbe67[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Apr 22 11:45:39 2013 +0100

    update release for master branch.

[33mcommit 1068fe58707912c88ea311a4763ac12ed028337b[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Mon Apr 22 12:41:45 2013 +0530

    added foreign key contraint for group and client in loan table

[33mcommit fe5c68d1e432d48e6a8970f654113bd6aab19ceb[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Apr 22 01:26:14 2013 +0200

    Update README.md

[33mcommit 14306f44a7672145189ac4453e555179ebbf0e53[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Apr 22 01:24:53 2013 +0200

    Update for 0.12.0.beta release

[33mcommit ea0ee849673168f21b560e0961ba6a593319aa32[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sun Apr 21 23:57:10 2013 +0100

    MIFOSX-213: center, group and clients api for activating entity to move from pending to active status.

[33mcommit 500767a400ae26cd631ca0a53ee177840bb91de4[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Fri Apr 19 23:36:49 2013 +0530

    wip: added client loan in group context

[33mcommit 4b43b0965153234cc9bcd88d2b9aaf008c2da955[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Apr 19 17:16:53 2013 +0100

    MIFOSX-262: slight changes to anuruddha's commit to use joda date classes over java.util.

[33mcommit 27624633117ac359b86ad685055a516aab4fb0cb[m
Author: Anuruddha Premalal <anuruddhapremalal@gmail.com>
Date:   Fri Apr 19 20:47:24 2013 +0530

    perform client activation date validation with office activation date
    before creation.

[33mcommit dff53b931847507f11adf33f4c8dbf3670db2e7e[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Apr 19 13:27:27 2013 +0100

    remove usage lookup data objects, formating of report java code, update api docs for center, groups.

[33mcommit 604f07f13ece01b917c978704d993940d4d2a2e1[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Apr 19 00:43:13 2013 +0100

    MIFOSX-213: wip statuses on center and group data and remove is_deleted from client.

[33mcommit a6dcba11cff8ffc5f1077ab9108dc08425cb506e[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Apr 18 19:41:33 2013 +0100

    wip mifosx-195 maintenance changes

[33mcommit 0e54ca5fa282f124075e2b912dae365b7200ebff[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Apr 18 15:17:25 2013 +0100

    MIFOSX-213: wip add status to centers and groups, tidy up CRUD on each and update api docs.

[33mcommit 53b5e3c7be214b5155bdb34d1e6aad6d7b7854d9[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Apr 18 14:48:40 2013 +0100

    wip mifosx-195 maintainance and validation fixes

[33mcommit abc6e5a42ab441569e47cde15cba631562ea5475[m
Merge: c3b91d8 fec8d16
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Apr 18 13:11:11 2013 +0100

    Merge branch 'master' of github.com:openMF/mifosx

[33mcommit c3b91d84f0b7eb70b69462ea90bc3a2f8cea1821[m
Author: Gurpreet Luthra <gluthra@thoughtworks.com>
Date:   Thu Apr 18 13:01:05 2013 +0100

    Added Jenkins Cloudbees build status image on github readme. Had to enable Embeddable Build Status plugin in Jenkins.

[33mcommit fec8d16e6d0cba9dfc6124cfb2249b7a749fe128[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Apr 18 13:01:05 2013 +0100

    Added Jenkins Cloudbees build status image on github readme. Had to enable Embeddable Build Status plugin in Jenkins.

[33mcommit a7c72a89b01da0ea45f8b98abe2cb08c9b1b318c[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed Apr 17 17:18:53 2013 +0100

    wip mifosx-195 add report template ability

[33mcommit 9a8476b6cdac5638d5d216bf44464d114133747a[m
Merge: 8f18f84 a7c5c5a
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Apr 17 15:38:54 2013 +0100

    Merge remote-tracking branch 'ashok/MIFOSX-282'

[33mcommit a7c5c5ae67a589d7bf6d844b6fbbd96a49a0be18[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Wed Apr 17 19:44:47 2013 +0530

    updated interestRateFrequencyType in API doc

[33mcommit 8f18f847f5c5cdfa1efe9039b5edadb8748af4b2[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Wed Apr 17 19:16:02 2013 +0530

    wip: fixed issues in Group add/edit and add client functionality

[33mcommit f212b78b992637eff73e114bd2df429b6fe82f97[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Apr 17 13:16:54 2013 +0100

    fix integrationt tests due to api changes to clients api.

[33mcommit 1180200ca33b716513e64a558ba428c922447149[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Apr 17 13:04:14 2013 +0100

    MIFOSX-213: support pending, active, closed status for clients.

[33mcommit 3001cc3582dd674f3c1af77572adfb26cd57a14a[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed Apr 17 12:23:55 2013 +0100

    wip mifosx-195 add/remove READ_{reportName} permissions when add/remove non-core reports

[33mcommit e5b0c31200b8649aa5e1bb06050b620c0ed82821[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Apr 17 09:56:01 2013 +0200

    Update README.md

[33mcommit ae4d669434f2b6d1e98259e2d3f0914b09637e5c[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Apr 17 09:54:57 2013 +0200

    Update README.md

[33mcommit 3f9c47821e27da847cc29f273be120ad91a197d8[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Apr 17 01:27:14 2013 +0200

    Update CHANGELOG.md

[33mcommit b9994713fcc7de90a2a82a24303d1806b3dfff8b[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Apr 17 01:25:15 2013 +0200

    update latest release

[33mcommit cd082fc2af9b9c519b2ec4a1db548f0aad151d6c[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Apr 17 00:20:18 2013 +0100

    MIFOSX-299: correct permissions on document management java services.

[33mcommit 0f6079b8e941906dddd4ac920058280a32fec37c[m
Author: Priti-Biyani <priti.biyani6@gmail.com>
Date:   Tue Apr 16 15:08:15 2013 +0100

    Squash commits around integration tests into one.

[33mcommit 06cd8cd940b02f2f2469a301af71ece9cbbe0426[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue Apr 16 17:59:20 2013 +0100

    wip mifosx-195 reports UI

[33mcommit a6e655761f173ddd0e69d6e855452274d3515e11[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Tue Apr 16 20:26:24 2013 +0530

    wip: group and center API changes

[33mcommit 436f77c3a8cea3e703e8691131ca01ca18a55d3f[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue Apr 16 12:22:25 2013 +0100

    wip mifosx-195 runreports api now used for running reports instead of reports - updated in api-docs along with report maintenance apis

[33mcommit 97d7104c93872c9eecf035f3dec979071223eb42[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue Apr 16 03:22:37 2013 +0100

    WIP mifosx-195 read report api (has conflict

[33mcommit c4ba4c54ca6641d415d638dacd7f48ca86ade7c3[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue Apr 16 02:25:46 2013 +0100

    WIP mifosx-195 create/update validation down to domain level from json checks + put report CRUD into config bucket

[33mcommit cd81798270fad2c9a03e9b9fa8126e2f51e72bce[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon Apr 15 17:35:47 2013 +0100

    WIP mifosx-195 maintenance apis

[33mcommit 38b1d10f62f1084179001222ef57ed4fa3c06f9c[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Apr 15 13:18:25 2013 +0100

    update V17 patch which cause funny foreign key contraint on existing production dbs.

[33mcommit 4b80393d4aa35d412ab5d36f0454c89df9d46337[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Mon Apr 15 11:10:06 2013 +0530

    removed interestRatePeriodType from Json parameters validation

[33mcommit b71e6594209ce09b0f7b5a528bb7b8d4e390ac51[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon Apr 15 13:02:52 2013 +0100

    WIP mifosx-195 more maintenance work plus fix reportId, parameterId in list reports api

[33mcommit b41661cca4c2928dd69725221ef3d415fda71130[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon Apr 15 02:39:02 2013 +0100

    wip mifosx-195 - report class

[33mcommit 4104150bd1762852968becbc659d65cf653f512d[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon Apr 15 02:26:02 2013 +0100

    wip mifosx-195 - updated report related ddl and data

[33mcommit 654b31f75e3180001f5f251a3eac17bdedfcab4f[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sun Apr 14 13:13:02 2013 +0100

    MIFOSX-290: update api docs and refactor READ side of center, group underneath api.

[33mcommit e777491d76154c6fe5a82471443d81bb5de313f9[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sat Apr 13 19:33:16 2013 +0100

    wip mifosx-195 refactor list reports api for maintenance function use and change api-docs

[33mcommit 9cc6abd6c4aaf5f0af8f33a2548d1cfad244501c[m
Merge: 8e99a2e 72bcb78
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sat Apr 13 18:37:33 2013 +0100

    Merge remote-tracking branch 'ashok/MIFOSX-282'

[33mcommit 72bcb78b09babfde4fcc7c6e4000dff224fc6c51[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Sat Apr 13 22:38:02 2013 +0530

    remove interestRateFrequencyType from supported parameters

[33mcommit 8e99a2ef36f789e7511501d9d2d657aa4c0bc649[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Sat Apr 13 20:51:39 2013 +0530

    payment for disbursal

[33mcommit debce5655f0ec31c2455a15c7ce2a2cbab9c448b[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Sat Apr 13 17:57:59 2013 +0530

    MIFOSX-282: remove min and max fields from loan table

[33mcommit 45bf43de5e8c3c4f5121b367a09101177364fa1f[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Fri Apr 12 20:38:58 2013 +0530

    updates for payment details

[33mcommit 850981d4a9cbf354a11830a7bb1aea5385fba403[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Fri Apr 12 17:18:04 2013 +0530

    wip:fixing collection sheet bugs

[33mcommit 6274217e9d7086967a910241397ffe4ccc09642c[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Apr 11 00:47:57 2013 +0200

    Update README.md

[33mcommit 3014fbccd5a50eb67d352a24c11bc3f029d5886d[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Apr 10 23:42:44 2013 +0100

    update release version

[33mcommit 232ccdf715e3f157bc31410d42acdb6aef872eee[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Apr 11 00:38:58 2013 +0200

    Update CHANGELOG.md

[33mcommit 33c7e9f7d5dc7b39b726a768bc569bae168f1c74[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Apr 10 21:25:38 2013 +0100

    fix merge conflict form cherry pick of 974e45c7875f2b08f4d25a35ac79c25b51a0b1ea

[33mcommit b7f72b2943e69cce69810696b0b5f5791f0517e5[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Apr 10 20:48:48 2013 +0100

    MIFOSX-292: ensure correct decimal places value for currency is returned.

[33mcommit dbed6037f39b398fe74db5a0e48d7f8e0d8d22dd[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Apr 10 18:01:25 2013 +0100

    move migration file into coredb folder.

[33mcommit efe0379258de48c7582297fc53ff2254833ad3d3[m
Author: Nayan Ambali <nayan.ambali@gmail.com>
Date:   Wed Apr 10 16:22:46 2013 +0530

    Groups api for centers/groups

[33mcommit 9ee3280b7a30ef5b1e71e50422499100dfa8d9c9[m
Author: Nayan Ambali <nayan.ambali@gmail.com>
Date:   Thu Apr 4 13:00:54 2013 +0530

    WIP: Group center

[33mcommit a8c10facd9767b1105a6798cb8c2ceba5d59835b[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed Apr 10 11:22:11 2013 +0100

    change status_id to status_enum

[33mcommit fdc0767f7f0468d9b8d67b0bda367a01420d23a2[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed Apr 10 10:48:17 2013 +0100

    add client and group status fields

[33mcommit e15a72eab1193221a68c3aef161ea5444a5e46e9[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Tue Apr 9 17:00:25 2013 +0530

    wip: repayment/disbursal apis support optional payment details

[33mcommit c2a462aedef45e87824b2923684612c061e71b4a[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue Apr 9 11:33:23 2013 +0100

    api docs update

[33mcommit e97695c2c9729620a3c491920840448e54f5b77b[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Apr 9 11:23:59 2013 +0100

    file path support for local development that should work on jenkins.

[33mcommit 59dec0097a01c956729d5f6344d9878c511fe18a[m
Author: Priti <priti.biyani6@gmail.com>
Date:   Mon Apr 8 14:56:35 2013 +0530

    Khushal/Sailee: Changed the path of migration folder in flyway related task in build.gradle so that they run on Jenkins

[33mcommit 14aa62073e4b1a69fb6255c713ce1396da7e7e58[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sat Apr 6 11:41:14 2013 +0100

    MIFOSX-250: wip on interest compounding and posting

[33mcommit 8730976f9941d2c0a1b9c15f742fcc9389600010[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sat Apr 6 12:01:51 2013 +0200

    Update README.md

[33mcommit cd744d9f2d638e055bec53bc67decac1c2d66097[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sat Apr 6 11:54:14 2013 +0200

    Update CHANGELOG.md

[33mcommit 7174b82f9d60c646fc901a4f93eb46fb05a44950[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sat Apr 6 11:53:26 2013 +0200

    Update CHANGELOG.md

[33mcommit f2ce84db465a40c4492aae87015fa5fe35446556[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Apr 5 22:42:44 2013 +0100

    update to build snapshot version again.

[33mcommit ec87116ddf76fb80300b89e5123bf8c61244188e[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Apr 5 23:37:55 2013 +0200

    Update CHANGELOG.md

[33mcommit 3d9c949f6a8b23311a6e01a7b6921cf4824b7219[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Apr 5 22:34:52 2013 +0100

    update minor release for bug fixes.

[33mcommit 87ee45a59221b47ff0e430e37fcd8133b6a7f071[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Apr 5 22:31:07 2013 +0100

    remove unused import warnings.

[33mcommit 9011f5568cf08e2066ec751ff8d65e202964ab3a[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Apr 5 22:22:40 2013 +0100

    Due to SPR-10395 - revert back to hibernate 4.1.9 from hibernate 4.2 as causes issues with persistence for datatables.

[33mcommit e0b856ba697220f1594af0985a5d0d5e4b5bd904[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Apr 5 22:16:06 2013 +0100

    MIFOSX-291: fix creation of office. problem with jpa nullable=false being wrong.

[33mcommit 4df690b5016487babc9170815a1dbbc0f691f962[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Apr 5 23:00:21 2013 +0200

    Update CHANGELOG.md

[33mcommit 20f010cbc3fd958ea550bcd3d862a7359b900947[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Apr 5 21:32:58 2013 +0100

    bump up version of database patch.

[33mcommit 2446a0f05e35945f77abec2c6fe479db242e1ef0[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Fri Apr 5 11:35:49 2013 +0530

    MIFOSX-282:added min and max constraints for Interest rate and number of repayments

[33mcommit c28ad6b18c48cddcb743a42225dc434935c4a380[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Apr 5 13:45:23 2013 +0100

    move release property back to build snapshot

[33mcommit cdef41924b5b6381e4140a2a9d0a03a9503910ae[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Apr 5 13:01:01 2013 +0100

    update to release 0.11.0.beta

[33mcommit 7b422c956ce54394a43750854d68324484f4ed0e[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Apr 5 12:45:25 2013 +0100

    update ceda customisations.

[33mcommit 409c48dd78f2b82a96febff54eb9de81175c57cb[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Apr 5 12:25:07 2013 +0100

    remove legacy permissions

[33mcommit 694658daa8bd4e32278a1898df8cae72e5158e04[m
Merge: 7dfcb76 8aacc1b
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Apr 5 11:37:35 2013 +0100

    Merge remote-tracking branch 'tw_priti/API_Documentation'

[33mcommit 7dfcb76e4b250825fbae204fc1b57352013d98fd[m
Merge: d98360d d458f90
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Apr 5 10:57:14 2013 +0100

    Merge remote-tracking branch 'vishwas/mifosx-283'

[33mcommit d458f90c3d48d4237ae0122dbcb47bc6bd394552[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Fri Apr 5 14:49:13 2013 +0530

    upper case in read query was causing errors on Ubuntu demo server (due to mysql case sensitivity issues on unix)

[33mcommit d98360dd39129528cc33f300e9d33c893fe74335[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri Apr 5 02:09:04 2013 +0100

    mifosx-270 api docs audit/makerchecker/permissions + wip mifosx-97 make functionality match api docs

[33mcommit 0ade90b70c414a70b94cb19cd71bffd3d23e0ea7[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Apr 4 18:57:03 2013 +0100

    wip mifosx-97 allow 'id' criteria for audit and makerchecker search + wip mifosx-270 audit/makerchecker api docs

[33mcommit da12288024004d8466f20d59e5b7f7adb10011a1[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Apr 4 12:30:48 2013 +0100

    add sensible defaults to db wrapper tasks.

[33mcommit 8aacc1bb5358d872aa9b0d9ef18445056ff9869a[m
Author: Priti <priti.biyani6@gmail.com>
Date:   Thu Apr 4 16:56:11 2013 +0530

    Full api matrix updated for write off

[33mcommit af79fd07904651279ddb061e4ed357ab665c53cb[m
Merge: 8b135f7 4bda6f3
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Apr 4 12:13:11 2013 +0100

    Merge remote-tracking branch 'khusha/FlywayWrapperTasksForDBMigration'

[33mcommit 8b135f769aefc3a43f79dbdd8a620dcfb292b194[m
Author: Priti <priti.biyani6@gmail.com>
Date:   Thu Apr 4 16:17:14 2013 +0530

    API Doc file updated
    1. WriteOff Loan documentation added
    2. UndoApproval : command correction and request body added

[33mcommit 4bda6f3d439767e581d583825c228dfff7c28822[m
Author: Khushal Oza <khushaloza@gmail.com>
Date:   Thu Apr 4 16:14:20 2013 +0530

    Added seperate gradle tasks for migrating a list DB and a tenant DB. No need to now pass a folder name. Discussed with Vishwas, and modified the war task to pick up the migration files from the appropriate folder... core_db folder.

[33mcommit 3e1ef6dafdf3173c4819c9b96e6ee840796826c4[m
Author: sailee <saileebh@thoughtworks.com>
Date:   Thu Apr 4 14:59:01 2013 +0530

    [Sailee/Priti]:
    Modified LoanProductTestBuilder in Integration tests to support minPrincipal and maxPrincipal fields for LoanProduct

[33mcommit 8956ef35a3fe2489e18e3666648f318a3b8b4f99[m
Author: Khushal Oza <khushaloza@gmail.com>
Date:   Thu Apr 4 12:21:18 2013 +0530

    [Khushal/Priti] Modified build.gradle to have tasks for migrating both types of DB. Added migrateDB and showDBInfo with params as DB name. They wrap over flyway so that one can pass DB name as param. Also seperated the migration folder into list_db and core_db so that migrations for tenant list and core tenent db can be placed in different folders.

[33mcommit 96528448896849cfd1dd7b74299005c91a35ec0a[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Apr 4 01:42:43 2013 +0100

    wip mifosx-270 audit/makerchecker api docs + minor doc changes

[33mcommit 81233724a29fdfe7c112923ebe88cd3644f0a7b3[m
Merge: 98faa19 3719734
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed Apr 3 12:02:50 2013 -0700

    Merge pull request #212 from vishwasbabu/mifosx-11
    
    handling rename of TenantUpgradeService to TenantDatabaseUpgradeService

[33mcommit 371973466dae894d97dccd2e773486a4e6317e78[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Thu Apr 4 00:11:09 2013 +0530

    handling rename of TenantDatabaseUpgradeService

[33mcommit 98faa19b8ebdf8ee7ab32d2f143046ab8f0a6ba6[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed Apr 3 19:39:03 2013 +0100

    wip mifosx-97 only see maker check data that user is allowed to approve

[33mcommit 1666b2b45211394ac7c3a60e1d0d47f4c8f1c548[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Apr 3 18:12:52 2013 +0100

    minor changes to vishwass pull request.2

[33mcommit 2ae603dcc24e48680ddefa5717b01de2c89ffb1e[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Wed Apr 3 21:54:43 2013 +0530

    minor additions to Flyway support...ability to optionally upgrade databases on application startup

[33mcommit af8219de29ee67aa785133d0a419dc60e58caec7[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed Apr 3 15:52:19 2013 +0100

    rename add_min_max sql to have 2 underscores for flyway naming std

[33mcommit 08542b90343b44d1c52d26c3a8c575a5b21c9123[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Apr 3 15:27:28 2013 +0100

    bump up migration script by one version, support clean integrationTest tasks in gradle.

[33mcommit f077733cfbcd179c2dff7213770473706efcc2b8[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Wed Apr 3 19:35:22 2013 +0530

    WIP: add min and max constraint to Principal amount

[33mcommit 979308e41cf4b0f7cef5b6e358faae712fbf528a[m
Author: sailee <saileebh@thoughtworks.com>
Date:   Wed Apr 3 18:41:42 2013 +0530

    [Sailee/Gurpreet]: Added SSL support for Integration Test

[33mcommit 12f799fe6bd9eb5c00556298404606c88dfda4f3[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed Apr 3 03:12:12 2013 +0100

    wip mifosx-97 refactor audit/makerchecker datascoping for non-headoffice user

[33mcommit 8e274b2d4a92e4dbff40ff3d8ea312e56fa7d0e4[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Apr 3 01:59:59 2013 +0100

    recent upgrade of hibernate seems to check JPA annotations more rigidly. fix incorrect nullable=false

[33mcommit 4f4fb982e5f0fe02e4d3ae8dcd34023e00cc5d9b[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Apr 3 01:53:56 2013 +0100

    update junit and hamcrest dependencies, use non-dreprecated junit Assert class, add better error failure messages when tests cannot connect to server or https as not being disabled.

[33mcommit c32ba579cd94ea7fdf158c3fa080d645b164ac8c[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Apr 3 00:30:23 2013 +0100

    update dependences. do gradle clean cleanEclipse eclipse

[33mcommit a9eb946ecd36ad945ddd8dbae9e917adbb826bfd[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue Apr 2 18:49:34 2013 +0100

    mifosx-97 still refactoring audit/makerchecker bits

[33mcommit ec810d9485decac3d480f51bef6b6947a201b07e[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Apr 2 17:12:03 2013 +0100

    MIFOSX-250: interest calculation change for compounding with api docs updates.

[33mcommit 41b31ded07d444a9e74246b3c63902b4914b493d[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Apr 2 15:16:47 2013 +0100

    remove warnings on unchecked types and unused imports. Add IntegrationTest to end of test name.

[33mcommit 1a5ef94def2dd0d6838e8e084264ba573a65365e[m
Merge: 18d0239 7f90f08
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Apr 2 15:01:30 2013 +0100

    Merge remote-tracking branch 'tw_priti/IT_With_Waive_Interest'

[33mcommit 18d02396b6e67033c699c8bd351f293dba48d946[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Tue Apr 2 19:07:29 2013 +0530

    MIFOSX-289 - update Search api doc

[33mcommit 7f90f0861c5c526f9aa44d73092f8d572c729544[m
Merge: 0dab99e e13daf0
Author: Priti <priti.biyani6@gmail.com>
Date:   Tue Apr 2 17:33:18 2013 +0530

    Merge branch 'master' into IT_With_Waive_Interest
    
    Merging branch master into this branch to stay current with the codebase before sending pull request

[33mcommit 0dab99ecdbfb827ce7e52b2917d962b03cf5506a[m
Author: Priti <priti.biyani6@gmail.com>
Date:   Tue Apr 2 17:26:55 2013 +0530

    Renamed File

[33mcommit 571174e10dc8374fd08d071964bf2c497c3fc1b5[m
Author: Priti <priti.biyani6@gmail.com>
Date:   Tue Apr 2 17:12:46 2013 +0530

    'out/' added to gitignore

[33mcommit a9a190827e223e23835f65359f448f9036d1360b[m
Author: Priti <priti.biyani6@gmail.com>
Date:   Tue Apr 2 17:09:30 2013 +0530

    ClientBuilder renamed as ClientHalper

[33mcommit a70712d8fc224ccf60fc8381d5d510ea9b648f28[m
Author: Priti <priti.biyani6@gmail.com>
Date:   Tue Apr 2 17:09:07 2013 +0530

    Refactoring

[33mcommit be32e0b7ebfd31df7d26feabf2dfc905d973fad4[m
Author: Priti <priti.biyani6@gmail.com>
Date:   Tue Apr 2 16:34:00 2013 +0530

    [Priti/Sailee] Refactoring

[33mcommit a66ef5cfbf3199254141ee16ca491f5a20b72f29[m
Author: Priti <priti.biyani6@gmail.com>
Date:   Tue Apr 2 16:31:21 2013 +0530

    1. LoanTranscations Moved out and created seperate Helper file for this
    2. Client related activities are moved to ClientBuilder
    3. Utils Will Have server Post/Get calls
    [Priti/Sailee]

[33mcommit f6fbbbb7d2432d6b4b10eeaf69dae463c86ddd1d[m
Author: Priti <priti.biyani6@gmail.com>
Date:   Tue Apr 2 16:27:59 2013 +0530

    [Priti/Sailee] Created ClientBuilder

[33mcommit e13daf0d29d4010ed25e4c8f28a648629fb6d6ac[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Apr 2 11:08:22 2013 +0100

    update ignoring of .ini files.

[33mcommit 4e4b2138d4fc28566b6c613d24ce8a5c6ed43fb1[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Apr 2 11:07:20 2013 +0100

    delete desktop.ini pentaho file.

[33mcommit 6c46026b5adb52d40b75d4ed7d9eb52af1a1c842[m
Merge: d0576c4 ccedf5b
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Apr 1 21:53:23 2013 -0700

    Merge pull request #205 from vishwasbabu/mifosx-277
    
    updating charges and loan charges documentation

[33mcommit ccedf5bfdc82c31d800c01494e1a612cac5d9ab9[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Tue Apr 2 10:17:09 2013 +0530

    updating charges and loan charges documentation

[33mcommit d0576c44886be424dc9ca6abf5d6b73f41cc2990[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue Apr 2 02:26:29 2013 +0100

    wip mifosx-97 refactor audits and makercheckers under commands package

[33mcommit 596bc82bbe63fb50c7b7fc74adb1af417a4be25c[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue Apr 2 02:09:52 2013 +0100

    wip mifosx-97 refactor endpoints to audits/makercheckers from audit/commands

[33mcommit 7cf6c20b3863139d861f99937f245ad0c2a38c00[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue Apr 2 01:51:29 2013 +0100

    wip mifosx-97 refactor audit/makerchecker functions

[33mcommit f0b526aa53d2ea128ac6eb786f7ec54ee07dbcad[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Apr 2 01:45:48 2013 +0100

    add self signed certificate keystore and point to it from gradle build for local development.

[33mcommit 110d183176b2dfdb4cf7ab52163bdd739d514e8a[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Apr 2 00:21:43 2013 +0100

    update DDL and patches files and move to migrations folder usage now.

[33mcommit c45b41e21abd59646b4a35aec1eae18d97f4d821[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Apr 2 00:08:39 2013 +0100

    update core files to be update to latest patches, gradle flywayclean flywaymigrate will drop and create database to latest schema - need to all agree where we start from when using migrations approach.

[33mcommit 8465664f8974b43641acab3541e4cbc64e162150[m
Merge: 5c73a46 4b53161
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Apr 1 20:54:28 2013 +0100

    Merge remote-tracking branch 'ashok/MIFOSX-278'

[33mcommit 513f6f276467882279f809e6b5cec1cc39a42f87[m
Author: Priti <priti.biyani6@gmail.com>
Date:   Mon Apr 1 19:32:57 2013 +0530

    Refactoring

[33mcommit 3924fafe88e5747b9b281f9c59eb471c9f3c496f[m
Author: Priti <priti.biyani6@gmail.com>
Date:   Mon Apr 1 17:08:24 2013 +0530

    Renaming

[33mcommit 4b53161ac40cea9c43051b8a932012e5c0087245[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Mon Apr 1 17:06:09 2013 +0530

    Fixed MIFOSX-278 and MIFOSX-280

[33mcommit 49eb712a5d431b2b8e5e4bbdb5215463f9c34ed8[m
Merge: f08da67 5d4bbf1
Author: Priti <priti.biyani6@gmail.com>
Date:   Mon Apr 1 16:51:20 2013 +0530

    Conflicts Resolved

[33mcommit f08da673f26ed2d62f9f216dfe1ca7a77f839870[m
Author: Priti <priti.biyani6@gmail.com>
Date:   Mon Apr 1 16:47:24 2013 +0530

    [Priti/Sailee] Renamed package as common

[33mcommit 419f127cd806788d0a6d7f1f43b656e98451a8b9[m
Author: Priti <priti.biyani6@gmail.com>
Date:   Mon Apr 1 16:46:14 2013 +0530

    [Priti/Sailee] Refactoring
    1. Created common package
    2. Moved test files to IntegrationTest
    3. Moved all helpers to Common package

[33mcommit 5c73a46f2ab9c59fc652a2e3760828d6f6c6f046[m
Author: Khushal Oza <khushaloza@gmail.com>
Date:   Mon Apr 1 16:00:25 2013 +0530

    Khushal/Gurpreet: Added task in gradle for creating and droping database- createDatabase and dropDatabase

[33mcommit 23ed73316d18299de58280207797fe08eac35f24[m
Author: Priti <priti.biyani6@gmail.com>
Date:   Sat Mar 30 11:08:16 2013 +0530

    Refactoring

[33mcommit d58fa92f3eb73c5653158da09e847802e2ffd2d4[m
Author: Priti <priti.biyani6@gmail.com>
Date:   Sat Mar 30 11:07:58 2013 +0530

    common jsonCreator moved to Utils

[33mcommit 4a2f7a0597c6a4f6ac7a4995e8affb7d35720957[m
Author: Priti <priti.biyani6@gmail.com>
Date:   Sat Mar 30 11:07:21 2013 +0530

    LoanStatusChecker added which checks the loan status

[33mcommit e555666ac2ac9d32863a6736ded4ea852ec4e88a[m
Author: Priti <priti.biyani6@gmail.com>
Date:   Sat Mar 30 11:06:02 2013 +0530

    Builder for LoanApplication Added

[33mcommit 02a3934e9ff02ecbe1aa79f237b85d406590862f[m
Author: Priti <priti.biyani6@gmail.com>
Date:   Thu Mar 28 21:57:28 2013 +0530

    1.Integration Test : LoanWithWaiveInterest added
    2. LoanProduct Builder created
    3. RandomGenerator methods moved to Util file

[33mcommit 56829c20360a60b6fe7ed9bc116108100ec6b34f[m
Merge: 860caa7 98472b3
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Mar 31 04:30:35 2013 -0700

    Merge pull request #202 from vishwasbabu/mifosx-277
    
    fix for loan officer assignment mifosx-260

[33mcommit 98472b3e59598bfd34dd77b394c982cf477e5ca1[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Sun Mar 31 16:53:43 2013 +0530

    fix for loan officer assignment mifosx-260

[33mcommit 860caa76a057380758efc8d5ff3383eab7c8efe4[m
Merge: c7c0244 d7c2af3
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Mar 31 03:38:54 2013 -0700

    Merge pull request #201 from vishwasbabu/mifosx-277
    
    adding overflow and fixed height for api-docs flybar

[33mcommit d7c2af35388b3c760b60b5ea75152530ff77e4ca[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Sun Mar 31 16:06:30 2013 +0530

    adding overflow and fixed height for api-docs flybar

[33mcommit c7c02448e580ee8fffc6ae33b4c97d0ebacd95ad[m
Merge: 0637fb2 d39047e
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sat Mar 30 12:15:14 2013 -0700

    Merge pull request #200 from vishwasbabu/mifosx-277
    
    api documentation for loan collaterals

[33mcommit d39047e36e33cfd6b079d399b6977410c8c7a655[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Sun Mar 31 00:44:05 2013 +0530

    api documentation for loan collaterals

[33mcommit 5d4bbf191e7ec785fa32e6f3444db641a6946bbe[m
Author: Priti <priti.biyani6@gmail.com>
Date:   Sat Mar 30 11:08:16 2013 +0530

    Refactoring

[33mcommit ed95612b3f437534f24a71b3ebff1021b5ad7c31[m
Author: Priti <priti.biyani6@gmail.com>
Date:   Sat Mar 30 11:07:58 2013 +0530

    common jsonCreator moved to Utils

[33mcommit 3471475c94591adccc5aba32194e82f807859f41[m
Author: Priti <priti.biyani6@gmail.com>
Date:   Sat Mar 30 11:07:21 2013 +0530

    LoanStatusChecker added which checks the loan status

[33mcommit b4edfc4c3405612c3d964080282b574856e1c549[m
Author: Priti <priti.biyani6@gmail.com>
Date:   Sat Mar 30 11:06:02 2013 +0530

    Builder for LoanApplication Added

[33mcommit 0637fb2697d444dc736c887ba5abb220e354165a[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri Mar 29 23:48:12 2013 +0000

    wip mifosx-97 refactor audit/makerchecker for re-use

[33mcommit 4883ce1b63522c1dc3ce4206b11bc0e45716533c[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Sat Mar 30 03:51:17 2013 +0530

    adding relationship for guarantor + adding wrappers

[33mcommit aa6dbd6fd459f182ec0b398d91bbc88a08c84820[m
Merge: 3df312a 239933f
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Mar 29 17:01:16 2013 +0000

    Merge remote-tracking branch 'khusha/flyway_support'

[33mcommit 239933fe917a4b55e041d4557a5255cea74a8313[m
Author: Khushal Oza <khushaloza@gmail.com>
Date:   Fri Mar 29 18:00:19 2013 +0530

    Added support for DB Migration using flybase migration plugin. It reads the schema sql files from mifosng-db/migrations folder. Runs the files in sequence by reading V1, V2, etc so on.

[33mcommit da6ff46f4cfb3e58159db56a338df1c70dfc2d75[m
Author: Khushal Oza <khushaloza@gmail.com>
Date:   Fri Mar 29 17:55:49 2013 +0530

    Removed the DBHelper class. We dont need it now. Will be upgrading the DB from gradle using the flyway plugin

[33mcommit 3b041ab50714b39fab66ecaf53007304a91e9464[m
Author: Khushal Oza <khushaloza@gmail.com>
Date:   Fri Mar 29 16:41:58 2013 +0530

    Removed call to DB helper. We will instead perform migration from Command line or Gradle

[33mcommit f6bcd1fb8a6eece4ccca79ffdf0d7f3ea488d493[m
Merge: ee5ffbc ddc2a6e
Author: Khushal Oza <khushaloza@gmail.com>
Date:   Fri Mar 29 16:39:15 2013 +0530

    Merge branch 'flyway_support' of github.com:khushaloza/mifosx into flyway_support
    
    * 'flyway_support' of github.com:khushaloza/mifosx:
      Khushal/Gurpreet: Added support for Flyway DB migration tool into MifosX. Added a call for it in TomatJDBCDataSourcePerTenantService so that it gets called per tenant once.

[33mcommit ee5ffbc883d1e51a435465a64a0140ff865c6f22[m
Author: Khushal Oza <khushaloza@gmail.com>
Date:   Fri Mar 29 16:37:54 2013 +0530

    removing the call to DB helper, since we want to do migration by command/gradle instead of automatically thru server

[33mcommit 72eb99432ae8e3b7323f2b2ec79153e7be54e635[m
Author: Khushal Oza <khushaloza@gmail.com>
Date:   Fri Mar 29 15:41:39 2013 +0530

    Khushal/Gurpreet: Added support for Flyway DB migration tool into MifosX. Added a call for it in TomatJDBCDataSourcePerTenantService so that it gets called per tenant once.

[33mcommit 3df312a871a22ce7a8f7e3658d3ddbb1eacd2b91[m
Merge: ff9dc97 030f379
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Mar 29 10:49:51 2013 +0000

    Merge remote-tracking branch 'nayan/MIFOSX-269'

[33mcommit ddc2a6e852729f5e20b6eb67ad9828e92b046f10[m
Author: Khushal Oza <khushaloza@gmail.com>
Date:   Fri Mar 29 15:41:39 2013 +0530

    Khushal/Gurpreet: Added support for Flyway DB migration tool into MifosX. Added a call for it in TomatJDBCDataSourcePerTenantService so that it gets called per tenant once.

[33mcommit ff9dc97316707b45ba5bf7e3fa25b0f637829346[m
Merge: 2662b69 3635ed4
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Fri Mar 29 03:05:32 2013 -0700

    Merge pull request #197 from vishwasbabu/mifosx-229
    
    sql errors faced by thoughtworks folks (able to replicate only on window...

[33mcommit 3635ed411a15b04be4e5ab5fda28d183f82b81f7[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Fri Mar 29 15:32:47 2013 +0530

    sql errors faced by thoughtworks folks (able to replicate only on windows box)

[33mcommit 030f379307ceabe2bacc82392683d68121b94c3a[m
Author: Nayan Ambali <nayan.ambali@gmail.com>
Date:   Fri Mar 29 11:22:23 2013 +0530

    Bulk JLG loans

[33mcommit 364941ca760dc6d9452c3b3c36bf445550a88c57[m
Author: Nayan Ambali <nayan.ambali@gmail.com>
Date:   Thu Mar 21 18:51:44 2013 +0530

    support jlg loans

[33mcommit 2662b692bfc6ec669a570741b1f50c66208562a6[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Mar 28 21:52:09 2013 +0000

    add audit 'id' field to api get

[33mcommit c362e0f19a983115a66a3053e43cce401b109a9b[m
Author: Priti <priti.biyani6@gmail.com>
Date:   Thu Mar 28 21:57:28 2013 +0530

    1.Integration Test : LoanWithWaiveInterest added
    2. LoanProduct Builder created
    3. RandomGenerator methods moved to Util file

[33mcommit e37f3cebfa87fcf100808ec1ae801013dd1d98e5[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Mar 28 16:21:43 2013 +0000

    mifosx-196 - added pentaho report to git directory after testing

[33mcommit e44d7e2c2ec3e78024ed596e70ea4f466ef4de54[m
Merge: 310a9e2 06a59b9
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Thu Mar 28 01:49:38 2013 -0700

    Merge pull request #195 from vishwasbabu/mifosx-228
    
    Update loan code to handle Collateral Value

[33mcommit 06a59b95f2b713f412b3f7e62a8fb4a46f646b17[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Thu Mar 28 14:18:02 2013 +0530

    Update loan code to handle Collateral Value

[33mcommit 310a9e248e0a964e8f7fac7e158f5ace69ce3cd8[m
Merge: 3711c5c c0464a5
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Mar 27 21:19:02 2013 -0700

    Merge pull request #194 from vishwasbabu/mifosx-228
    
    cleaning up collateral api's

[33mcommit c0464a571b0c492d5490a93e380a1958d8dee147[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Thu Mar 28 09:44:42 2013 +0530

    cleaning up collateral api's

[33mcommit 3711c5cfd825952647334179a500530f97075ec5[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Mar 28 00:38:28 2013 +0000

    MIFOSX-201: formatting tweaks.

[33mcommit 715bc2cf178c2ede543e7152d66aea299bbcfc53[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Mar 27 23:45:43 2013 +0000

    MIFOSX-250: add support for daily balance and average daily balance methods. support for daily,weekly,monthly interest periods.

[33mcommit ca038a8ebf809babd63aca038ba715114c2ed986[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Wed Mar 27 19:30:04 2013 +0530

    bug fix: MIFOSX-201 and MIFOSX-202

[33mcommit c77f3e15f1a856559cf140729f9a04702d371eb4[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Wed Mar 27 16:22:55 2013 +0530

    Collection sheet UI and bulk update

[33mcommit 69d228a502e8663f9d64faa1993f7885ebb214fb[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Wed Mar 27 03:40:02 2013 +0530

    wip: Loan Collateral

[33mcommit b8e4cf3dc969060b0b8f177e562daa5de979e0d9[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue Mar 26 13:57:01 2013 +0000

    mifosx-275 datatables - support ids on audit for loans/savings that are group related

[33mcommit 2d405a14abad6374ec94916355a28bd6ac07987b[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue Mar 26 11:31:58 2013 +0000

    mifosx-276 Support audit 'ids' when in maker-checker mode

[33mcommit f579054c3334b10f336f56ceefbbc781475f2927[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue Mar 26 00:38:18 2013 +0000

    rename audit savings field to savings_account_id

[33mcommit bf680a2dad5965b90ae364419ea411063d6d4575[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue Mar 26 00:19:26 2013 +0000

    mifosx-274 add account_savings_id to audit table

[33mcommit 82f97875a70f59d6769ec69b91e91993ee2e96e8[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue Mar 26 00:18:50 2013 +0000

    mifosx-274 add id fields for datatables (to audit log)

[33mcommit d4a5b55e71f6d37dcab969b0e68b996ace5fb867[m
Merge: e5a0b8b 17ad098
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Mar 25 20:33:53 2013 +0000

    Merge remote-tracking branch 'vishwas/mifosx-228'

[33mcommit 17ad0984c0a6ced557409ca08dfc90a281db0f09[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Tue Mar 26 01:24:41 2013 +0530

    updating fee_charges_charged and fee_charges_repaid to include charges due at disbursal

[33mcommit e5a0b8b2cf22203090e7a712162b084c81fe3a55[m
Merge: be86537 ef223b3
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Mar 25 18:51:01 2013 +0000

    Merge remote-tracking branch 'vishwas/mifosx-228'

[33mcommit ef223b346c3e05cb941c9c44536dfcc06ce8a4a7[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Tue Mar 26 00:12:42 2013 +0530

    fix for Repayment schedule not showing charges due at disbursement

[33mcommit be86537124fcb9215bab4a11dabe3122d4f4b1b3[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Mar 25 18:18:11 2013 +0000

    update DDL for missing savings_id column and dumps.

[33mcommit a38c1a69a029ec7b54ef6ec7559379825ae35619[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Mar 25 17:48:08 2013 +0000

    udpate DDL and latest schema dumps to latest patch.

[33mcommit 63eba40115da53a198777de4c4b23f3776423e6a[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Mar 25 17:39:01 2013 +0000

    MIFOSX-247: add basic lifecycle state to savings account and support some constrains around deposits and withdrawals.

[33mcommit 3e36a8a3cb86cf429302a9ee1c89e7f867125400[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon Mar 25 16:06:51 2013 +0000

    mifosx-273 savings datatable ability

[33mcommit f2e4df94b8954985dc3c3ca65f0539a1338fa5dc[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sun Mar 24 17:00:43 2013 +0000

    error msg when commandId not found for maker-checker approvals

[33mcommit 0032939e9a430c64c24cc3d3e58f99d65b24d200[m
Merge: e67da73 d0e19c4
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Mar 24 07:29:25 2013 -0700

    Merge pull request #188 from vishwasbabu/master
    
    updating demo server

[33mcommit d0e19c440e8665d4e64f2d8399e92f489f19e23f[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Sun Mar 24 19:57:33 2013 +0530

    updating demo server

[33mcommit e67da73ebcd7af4c66066d07f1d9202cbd17098e[m
Merge: b0fca8c 28d66e3
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Sun Mar 24 06:43:08 2013 -0700

    Merge pull request #187 from vishwasbabu/mifosx-183
    
    bug fixes for accrual based accounting

[33mcommit 28d66e39ec3ca8222ca89fb8c923fc56a3364007[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Sun Mar 24 19:05:32 2013 +0530

    adding Lifecycle event called Interest Applied

[33mcommit 29c071d763958073339371d49cd57ced05f67545[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Sat Mar 23 16:52:18 2013 +0530

    bug fixes for accrual based accounting

[33mcommit b0fca8c44d89566563d8a27388f3e84662053eb7[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri Mar 22 15:07:04 2013 +0000

    fixed mifox-271 locale & dateFormat added to changes list for datatable updates

[33mcommit d34e1a01c8c4f3212379818458128281551f3ec2[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Mar 22 12:38:58 2013 +0000

    update datatables for default demo schema.

[33mcommit ac2f5d60df361bf749bf63bca2a6dcd0d270f6e9[m
Merge: ec6f553 bb5e959
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Mar 22 11:43:39 2013 +0000

    Merge remote-tracking branch 'vishwas/mifosx-255'

[33mcommit ec6f5539b53df31fac6fcf3689534926a10a63e1[m
Merge: 31bb3b3 c9223cd
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Mar 22 11:19:45 2013 +0000

    Merge remote-tracking branch 'tw_saileeb/Integration-Tests'

[33mcommit 31bb3b3e28114ce06750a6896c7c74ee8fd7fdbc[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Mar 22 10:19:02 2013 +0000

    update latest DDL, reference data and schema dump to latest patches.

[33mcommit c9223cd4859e769c38bc1f904f1f2e2c92aa34d9[m
Author: sailee <sailee@SaileeBhekare.local>
Date:   Fri Mar 22 14:58:19 2013 +0530

    First cut Integration Test using REST-Assured. Also modifed .gitignore for Mac.

[33mcommit bb5e9598ea175afe0edd1b681ea6bbcae01a3ccc[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Fri Mar 22 11:53:11 2013 +0530

    bug fixes for accounting

[33mcommit 345d4e2811b3297c2ef5726e4097b9e5c8eec4ad[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Mar 22 01:25:10 2013 +0000

    MIFOSX-247: ensure summary and transactions shown as part of retrieve savings account.

[33mcommit 59ec8a566922339bf9a81c1cad51a78f2c1f1404[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Mar 22 01:14:10 2013 +0000

    MIFOSX-247: update api document for savings account transactions api.

[33mcommit 1b4403e767f922b0acef3256563bd0d7c42f034a[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Mar 22 00:42:01 2013 +0000

    MIFOSX-247: basic savings functions for deposit and withdrawal.

[33mcommit 2374bf9402187cc1909293cdf9411e8806ab7eb4[m
Merge: b94dc4e 58d5acd
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Mar 22 00:28:48 2013 +0000

    Merge remote-tracking branch 'vishwas/mifosx-255'

[33mcommit b94dc4ebe2d59f25c449078f3ef00a8e0e7079b0[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Mar 21 23:48:05 2013 +0000

    remove apptableId and datatableId from audit & maker-checker processing (refactored to not need them)

[33mcommit ea9e24a270953117cf4e273cb16246b7e84e34bd[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Mar 21 22:20:32 2013 +0000

    fixed mifosx-268 one to many datatable update and deletes fail maker checker approve... plus minor other changes

[33mcommit 58d5acda0a363ab241da741de116d8dec19e0534[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Fri Mar 22 01:13:51 2013 +0530

    fix for bug in accounting

[33mcommit d24dc108186d49abcd123e16f62b8d5e9aab8ab4[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Thu Mar 21 20:40:06 2013 +0530

    fixing validation on loan repayment to allow reversal from API

[33mcommit 32b2ed91b4b17c64c8a632cd45e23fce1f334c6e[m
Merge: 577b32f 8300127
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Mar 21 12:29:47 2013 +0000

    Merge remote-tracking branch 'vishwas/mifosx-255'

[33mcommit 830012752266b13cce9f01b7e7da8809b1582915[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Thu Mar 21 17:25:38 2013 +0530

    tracebility for loan transaction updates

[33mcommit 577b32fd227526d0ad0d53877fa57b2f21e086c7[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Thu Mar 21 15:17:47 2013 +0530

    Minor changes in Calendare read queries

[33mcommit d81130484109b77ed6a4f5f8b1b04edd9af415a3[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Thu Mar 21 02:01:35 2013 +0530

    wip:improving tracebililty for loan transaction updates

[33mcommit 043e8dc4fdb02660b5f9a8eb7f6d71142ff8feb4[m
Merge: ceb193a eec727d
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Mar 20 17:28:36 2013 +0000

    Merge remote-tracking branch 'ashok/MIFOSX-256'

[33mcommit ceb193af376b563c352b664366ed052fa6d9a6cc[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed Mar 20 15:59:02 2013 +0000

    fixed mifosx-266 default all permissions to not be maker-checkerable

[33mcommit eec727db5b6cc733bccbf08a7bcbeadd7f54b499[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Wed Mar 20 21:02:21 2013 +0530

    MIFOSX-256: Added calendar meeting date validation for expected disbursment date and first repayment date

[33mcommit 7ae32b346dc015dc39a8d2d8f92584339a9279df[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Mar 20 14:29:54 2013 +0000

    fix datatables on maker checker

[33mcommit 13723f5e801902c5ef2d88311dad2662fe2c4ff2[m
Merge: 587c03a 44aa60d
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Mar 20 14:34:01 2013 +0000

    Merge remote-tracking branch 'sander/MIFOSX-230-2'

[33mcommit 44aa60d2379af2239fd317e63fb2851f510b9c9b[m
Author: Sander van der Heyden <sander@musoni.eu>
Date:   Wed Mar 20 15:27:00 2013 +0100

    MIFOSX-230-2: Extended documents functionality to groups entity and added to documentation

[33mcommit 587c03a277c8ef37206c2494b859206673db161c[m
Author: Nayan Ambali <nayan.ambali@gmail.com>
Date:   Wed Mar 20 19:43:55 2013 +0530

    get parent groups details along with client data

[33mcommit 324ae9acd93d3afda81ce06de8d4d67f859b12a1[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Mar 20 12:00:30 2013 +0000

    update core DDL and default schema dumps to latest patch-3 on savings changes to date.

[33mcommit 3e8d9f1c137f242851842f8ccd329962e6eee33d[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Mar 20 01:13:34 2013 +0000

    MIFOSX-258: return roles of authenticated user in response of authentication api.

[33mcommit 2c8efb4977edf111c4e2a0b2bec622bd78e6fa22[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Mar 20 00:35:56 2013 +0000

    MIFOSX-245: add basic shell for savings products and accounts.

[33mcommit eb7d6168342b54f135d2b31efff933965883dac5[m
Merge: cce49b6 473b579
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Mar 19 17:20:23 2013 +0000

    Merge remote-tracking branch 'ashok/MIFOSX-256'

[33mcommit cce49b634fa34103461d5b4e4a078c37749dc9d3[m
Merge: a5b5249 5145b24
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Mar 19 17:19:41 2013 +0000

    Merge remote-tracking branch 'vishwas/mifosx-251'

[33mcommit 473b579df3c4db77aa7b3cb6c8035907a82dde74[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Tue Mar 19 20:49:29 2013 +0530

    wip:MIFOSX-256 - attaching calendar to loan

[33mcommit 5145b242aee62f89f95be380af25598b84a0840a[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Tue Mar 19 20:01:08 2013 +0530

    documentation stub for entity documents and code values

[33mcommit a5b52492d579e3de70497c44bc44e03e2d652d3f[m
Author: Nayan Ambali <nayan.ambali@gmail.com>
Date:   Tue Mar 19 19:42:38 2013 +0530

    group collection sheet apis

[33mcommit cb8b8e72d186cca8335f78069062ff99013173cb[m
Author: Sander van der Heyden <sander@musoni.eu>
Date:   Tue Mar 19 12:08:48 2013 +0100

    MIFOSX-230-1: Extended datatables functionality to Groups and Offices

[33mcommit d7496593c1582681a76c5b1305130995e3808f35[m
Merge: c85e10c f01e7e8
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Mar 18 21:40:34 2013 -0700

    Merge pull request #172 from vishwasbabu/mifosx-251
    
    restarting numbering sequence for patches

[33mcommit f01e7e80c46e83ad538ffdd32b8b47cb2f6879bc[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Tue Mar 19 10:09:08 2013 +0530

    restarting numbering sequence for patches

[33mcommit c85e10cfab8134f878e63ac0c5c7ba76d8818237[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Tue Mar 19 01:34:58 2013 +0530

    minor changes to client images and updates to documentation

[33mcommit d1896d43b1e82bbc08690c26e8ef0fbaf5a5a4d9[m
Merge: 198ec85 4caa564
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Mar 18 11:03:26 2013 +0000

    Merge remote-tracking branch 'ashok/MIFOSX-244'

[33mcommit 198ec85d1b881240c3a237c5f5ffa2886e906443[m
Merge: c5e40ab b7e1ff3
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Mar 18 10:58:58 2013 +0000

    Merge remote-tracking branch 'vishwas/mifosx-240'

[33mcommit 4caa564c4e3ff45ccc467704bacf4f267d69ab94[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Mon Mar 18 16:08:59 2013 +0530

    MIFOSX-244: Added recurring rule to calendar and added calendar to Groups

[33mcommit c5e40ab74452dddf916b8525de9ed91fdfc9d17c[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sun Mar 17 20:13:08 2013 +0000

    alter default jdbc setting until mifosx-254 addressed

[33mcommit b7e1ff3fa858e6558a8e9d9ebaa6f839968f5e56[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Sun Mar 17 21:23:34 2013 +0530

    wip: refactored accounting and added charge applied event

[33mcommit 759b832360b93b6cdab6e758f96130fa59280335[m
Author: Nayan Ambali <nayan.ambali@gmail.com>
Date:   Sat Mar 16 19:44:59 2013 +0530

    added grouping heirarchy sorted

[33mcommit f111f1666006f828a39432ac9db35e521d1d0261[m
Merge: d1ca092 a65c15a
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sat Mar 16 17:26:13 2013 +0000

    Merge remote-tracking branch 'vishwas/mifosx-240'

[33mcommit d1ca0924053e99c12c47a0737cfd37deb8e92acc[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sat Mar 16 16:58:56 2013 +0000

    only look at active (or before) status for repayments on branch expected cash flow

[33mcommit ea78f7b1260e5708efbe019a244fea20fe5c043e[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sat Mar 16 15:28:58 2013 +0000

    add expect payments by date pentaho rpt to core reports script + other minor changes

[33mcommit a65c15a09b31c82c361dd550a04850898bd1c7d7[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Sat Mar 16 19:52:36 2013 +0530

    minor clean up

[33mcommit ed323c00a334dace9528b817090d3bc4846e2600[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri Mar 15 22:54:31 2013 +0000

    mifosx-209 datatables tie-in with maker-checker process - WIP create data table entry change

[33mcommit e581be40be54049f687617a1481e21802cee02c9[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri Mar 15 21:59:00 2013 +0000

    add read permissions for core reports into core reports script

[33mcommit b9a6e3f410ba370765022af7402e07d7403c4f0e[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri Mar 15 21:32:01 2013 +0000

    use jdbctemplate batchupdate in datatables where > 1 update in a db transaction

[33mcommit 0db309d6927af55615db87b923d22e12ebdc540f[m
Author: Nayan Ambali <nayan.ambali@gmail.com>
Date:   Fri Mar 15 18:41:53 2013 +0530

    Update GroupWritePlatformServiceJpaRepositoryImpl.java

[33mcommit f28e8a0533baa852c90076a3010ba89b6314b189[m
Author: Nayan Ambali <nayan.ambali@gmail.com>
Date:   Fri Mar 15 18:22:52 2013 +0530

    after unassign parent group update heirarchy

[33mcommit 11bd10ddeb73b52f035f91e3f76044e946028519[m
Merge: f5463eb f0bcbc2
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Mar 15 12:39:23 2013 +0000

    Merge remote-tracking branch 'vishwas/mifosx-240'

[33mcommit f0bcbc2fc900733152b7f59ff28e9236e14ffb00[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Fri Mar 15 17:57:29 2013 +0530

    updating demo server

[33mcommit f5463ebb97b102ac80ae9313d04d1ef97fa634c5[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Fri Mar 15 17:47:25 2013 +0530

    wip:MIFOSX-244 Adding Calendar RecurringRule

[33mcommit 26f8fd3a4c06c28cbb4079e768ef2ba483a11ebc[m
Merge: 50b9839 903a958
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Mar 15 10:35:47 2013 +0000

    Merge remote-tracking branch 'nayan/workspace'

[33mcommit 903a958783d49f099fe7233d85623e597e264a38[m
Author: Nayan Ambali <nayan.ambali@gmail.com>
Date:   Fri Mar 15 15:32:02 2013 +0530

    review comments incorporated

[33mcommit 50b9839024b6383c7c4a0a6fb1c10c846041ac20[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Fri Mar 15 13:19:00 2013 +0530

    wip:minor changes to charges

[33mcommit fe426352ff03129cab8fba8ca7e5b9160e3fecc5[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Mar 14 19:44:00 2013 +0000

    fix mifosx-194 (updated core reportswith cascade details and added expected payments screen report from mifosx-196

[33mcommit bfe3b18837521cd9b550db76008d153f8305ed5a[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Mar 14 13:05:56 2013 +0000

    WIP mifosx-194 cascading report UI params

[33mcommit 772fe35a3669214e70c35bcb2da282f68ef88f6d[m
Author: Nayan Ambali <nayan.ambali@gmail.com>
Date:   Thu Mar 14 11:36:15 2013 +0530

    incorp review comments

[33mcommit 5a48f885865c8fe4f0e57f6f77ebd79097477113[m
Author: Nayan Ambali <nayan.ambali@gmail.com>
Date:   Wed Mar 13 22:02:48 2013 +0530

    Incorp review comments

[33mcommit 5ca8d6801dc3b3a63bdde0350e28abb1ed2e18bf[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Mar 13 01:49:28 2013 +0000

    MIFOSX-237: ensure big decimal entries are compared correctly and only transactions with changes are audited.

[33mcommit 509daca905839e3be27a0d42911289244d75dc62[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue Mar 12 22:27:12 2013 +0000

    fixed mifosx-236 make account report support multi-tenancy

[33mcommit 52642045a2925aff0ad1b366892c35b1c3741563[m
Author: Nayan Ambali <nayan.ambali@gmail.com>
Date:   Tue Mar 12 22:43:26 2013 +0530

    GroupUpdate api:in response send list of clients changed, using Guava symmetricDifference

[33mcommit f98b78a180a24a6ce7b7e75d9558e449e687f621[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue Mar 12 16:33:14 2013 +0000

    put pentaho properties in pentahoReports folder and increase jdbc pooled connections temporarily to allow pentaho reports to run without timeouts

[33mcommit 8ad52ca3a4d47ef4dab769d4d5b415e9d67da5af[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Mar 12 13:45:45 2013 +0000

    MIFOSX-239: update dist task to bundle pentaho reports inside pentahoReports directory in zip disbribution.

[33mcommit 3c49134c1779ff91d9daa453ab1e6e75e6d89a3d[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Mar 12 13:19:02 2013 +0000

    update default demo dumps to have accounting pentaho reports enabled by default.

[33mcommit 55f828c0b978dc16ec0227a4609dd27a027c08c3[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Mar 12 12:39:52 2013 +0000

    MIFOSX-239: add in accounting reports and properties from vishwases repository.

[33mcommit 6aebffcfebb89ccabfcf67b6f5fc1c97a2218b10[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Mar 12 12:10:42 2013 +0000

    add deploy pentaho reports task so gradle tomcatrunwar will deploy multi-tenant supported reports into proper directory by default for development.

[33mcommit d8dd0ac13e73e0379ac8230a847b17ddd7b7aaa9[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue Mar 12 11:26:23 2013 +0000

    add pentahoReports folder and report to test gradle putting pentaho reports under mifosx/pentahoReports

[33mcommit 513cfeae77b210eecd33ecb1e7802b7e151bc48b[m
Merge: 5717240 f90bf40
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Mar 11 23:07:59 2013 -0700

    Merge pull request #159 from vishwasbabu/master
    
    Deleting file with wrong case

[33mcommit f90bf403cffe7236c608acea3144be99da0452bc[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Tue Mar 12 11:35:46 2013 +0530

    renaming file

[33mcommit 5717240ccf8e8901ea12493368f709711c7eda71[m
Merge: 84747cb f3e6864
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Mar 11 23:02:08 2013 -0700

    Merge pull request #158 from vishwasbabu/mifosx-240
    
    resolving compilation issues due to git case insensitivity

[33mcommit f3e686406d198be7d1a5f6c09bc6b46b98b473fe[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Tue Mar 12 11:28:44 2013 +0530

    resolving compilation issues due to git case insensitivity

[33mcommit 84747cb109b18cde390f096dd2fb902162ea2852[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Mar 11 19:42:10 2013 +0000

    MIFOSX-237: api doc changes and ensure error handling supported is added back in for datatables api.

[33mcommit b3c77fe04577cc6b03082a3de00091445b75e6f6[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon Mar 11 16:00:10 2013 +0000

    patch contained creocore database name - removed

[33mcommit be011529de55818a47c0d9933a474864644a908b[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Mar 11 13:54:57 2013 +0000

    MIFOSX-237: use of jdbcTemplate and SqlRowSet along with refactoring as a result of these changes.

[33mcommit d310d4df3c580495d060623b0118e1ca6847a95c[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Mon Mar 11 16:52:39 2013 +0530

    MIFOSX-162 - Fixed date validation

[33mcommit cf81fe92b15982266fed8df1ca3cf78b1e2df323[m
Merge: 99ae901 84b4811
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sun Mar 10 22:53:37 2013 +0000

    Merge remote-tracking branch 'nayan/MIFOSX-186'

[33mcommit 99ae901cc21528efd69bc6464ea60ae01ad21ef1[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sun Mar 10 22:20:45 2013 +0000

    add userhierarchy parameter for pentaho reprots and change pentaho directory location

[33mcommit 485182db4565bba2a2636adfeba74a3e4d15ceec[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sun Mar 10 20:38:53 2013 +0000

    fixed mifosx-166 multi-tenant pentaho reports

[33mcommit 84b4811e617a1c11baf7e3648bbccb69db028e07[m
Author: Nayan Ambali <nayan.ambali@gmail.com>
Date:   Sun Mar 10 16:25:30 2013 +0530

    code restructuring

[33mcommit b975da0d45e03f9773ad1b7ca50d62133dc0844e[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sat Mar 9 15:27:42 2013 +0000

    temp pentaho parameter added

[33mcommit b407f282d00d430d137d9d9a4b29db4551a81ddf[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sat Mar 9 13:31:37 2013 +0000

    update dependencies and build script.

[33mcommit 458d2a303cbf410e8fb932df2c5d9c10f3b419e2[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Mar 8 16:51:25 2013 +0000

    update api docs and comments on joda usuage in loan officer history.

[33mcommit 7ad92a03309f8a2bf9401d4ea475aa2942c90c5a[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Mar 8 14:11:48 2013 +0000

    MIFOSX-232: align loan account api data with loan product data.

[33mcommit e3f03f1de3de5a5b0152ec12103d8ce618f96fd0[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Fri Mar 8 18:46:36 2013 +0530

    updated loan officer unassign date validation error message

[33mcommit 47c8daf0dfaa56815df95dc928130222ae4cb477[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Fri Mar 8 16:32:16 2013 +0530

    Modified NoteResourceNotSupportedException class name

[33mcommit 6593faca64f0d3ad3b0c135ac5fe1d3aa5e136da[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Fri Mar 8 16:26:52 2013 +0530

    MIFOSX-162 - Added Loan office assignment date validations

[33mcommit 34e2bb3c4dcac9848633d692858decab142d5b1e[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Thu Mar 7 22:00:07 2013 +0530

    Updated Notes in API doc

[33mcommit 80c3850e007cc32d17fa237b08c5255d08e3e4fa[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Mar 7 14:08:15 2013 +0000

    update latest ddl and reference data for patches 19 and 20.

[33mcommit 76d2dd6209b8a3b2613b9b4fdac1f99dc01e43c5[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Mar 7 13:31:58 2013 +0000

    MIFOSX-232: WIP - split out terms and settings related fields on loan product along with charges and accounting related inputs.

[33mcommit 619ec9e8cca9c90db051e487b7a991ad672ab0e0[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Mar 7 11:42:44 2013 +0000

    all Long data type for pentaho reports to cater for database ids

[33mcommit 856f406e314aa241b488e0811c3b5cb193ccf7b0[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Thu Mar 7 15:35:45 2013 +0530

    Bug fixes for accrual based accounting

[33mcommit 2b784cc0f9e7a8f2a84f4127538110a10e02c3d5[m
Author: Madhukar <madhukar@hugotechnologies.com>
Date:   Thu Mar 7 11:26:13 2013 +0530

    Throwing valid exceptions for deposit account

[33mcommit 66232baa013162628e3e98e2bc2a9ae0bcd89fbd[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Mar 6 17:55:34 2013 +0000

    update patch numbers and remove some unused code(non note related).

[33mcommit a497aeec558472c836792c07c2b24ff1903b6657[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Wed Mar 6 21:22:35 2013 +0530

    Implemented Notes API

[33mcommit 155339342a622ef4b86a66094dda9f793d3d527c[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Wed Mar 6 18:43:35 2013 +0530

    fix for disbursal reversal handling in cash based accounting

[33mcommit 0fdfdd314edf229d94e5c5b75e58ab9932f07723[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed Mar 6 13:26:18 2013 +0000

    core report updated

[33mcommit 63da11993dc9fb286913ceb2bce308dba28097d6[m
Author: Nayan Ambali <nayan.ambali@gmail.com>
Date:   Wed Mar 6 17:30:39 2013 +0530

    Add client within a group, add new group within a parent group

[33mcommit fed9f6dc689579e5cbbbf69de2a574dcf2d5ddda[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Mar 6 11:29:52 2013 +0000

    MIFOSX-175: Fix issue with one-to-one mapping for arrears on loan.

[33mcommit 3850f221402846c809b6b2f65779b473b440e854[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Wed Mar 6 15:11:08 2013 +0530

    wip:initial work for accrual accounting and refactoring

[33mcommit f846286858affb363afe52044e7dad40d991cc46[m
Author: Madhukar <madhukar@hugotechnologies.com>
Date:   Wed Mar 6 12:22:09 2013 +0530

    Throwing account specific exceptions for saving product

[33mcommit da7caddd4692c851b4bbb256644903c66bcec508[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Mar 5 17:13:56 2013 +0000

    Fix up issue with fee charges not been summed up correctly for preview loan schedule.

[33mcommit e2516339608c3ec2906a38d9eab0bb0ef2b12951[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon Mar 4 23:21:19 2013 +0000

    wip core reports update with loan derived fields

[33mcommit c04b9a3444dd6a0bf52261a81ad58f2fea764625[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon Mar 4 22:55:59 2013 +0000

    wip update core reports to use derived loan fields

[33mcommit bf6d06e5ceb7e6ceae2a38e5ce56e867d5b66e22[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Mar 4 17:02:00 2013 +0000

    Update api docs.

[33mcommit 9ec9a0080e2dda831e85a1c815a94b6b38955a56[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Mar 4 16:23:55 2013 +0000

    update DDL and demo dumps based on patch 18.

[33mcommit 2d7bd6f171ccbc9f4cddaf4b51482204207dfed9[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Mar 4 15:35:41 2013 +0000

    MIFOSX-175: make relationship to arrears aging summary table optional.

[33mcommit 0bca626252b2cb59f9136a6ec4eee160a230b410[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Mar 4 14:44:28 2013 +0000

    update patch to change order of min, max, deafult fields on schema.

[33mcommit 0959a22ea8ba86264142db4686f8ebbc0195984d[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Mar 4 14:41:49 2013 +0000

    update patch.

[33mcommit 23ab5f430ddaec19bd00b9d47519c9210af7d68c[m
Merge: 8d4aa69 0e13d49
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Mar 4 14:24:46 2013 +0000

    Merge remote-tracking branch 'madhukar/MIFOSX-222'

[33mcommit 8d4aa69302235a0bef5be8fd6bf9e891f99d1f9f[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Mar 4 13:07:21 2013 +0000

    MIFOSX-175: break out overdue fields into one-to-one table with shared primary key.

[33mcommit e491859cf5b6b25c90a10c750d38757edd66c07d[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon Mar 4 13:39:17 2013 +0000

    rename ddl related to mifosx-175 loan derived fields to have order of running

[33mcommit 6eafd7298fca463103ee020d8817538e8460fd3b[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon Mar 4 13:27:37 2013 +0000

    fix for insert error on base-reference-data

[33mcommit b6c09cab38fc57c8e3c7d2b2ceb2540878d9e7e0[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon Mar 4 12:12:50 2013 +0000

    patches and updates related to mifosx-175 Loan Derived Fields

[33mcommit 0e13d4982e3c9bac8fd26729cd664c2121cf706a[m
Author: Madhukar <madhukar@hugotechnologies.com>
Date:   Thu Feb 28 18:49:09 2013 +0530

    renaming deposit product fields
    
    adding default amount column for deposit product

[33mcommit d81316541d9d450866494a82d9227ebb5e59886e[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sun Mar 3 22:17:09 2013 +0000

    MIFOSX-224: remove unused savings related code and format code whislt reviewing existing savings implementation.

[33mcommit ecd913596554adcd30d4480cc549a69b531ead70[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sun Mar 3 02:27:25 2013 +0000

    update DDL and base reference data with patches 16 and 17.

[33mcommit fa9f40d3a22ec2f8ecc5e8c1ee09c2f70e3c055f[m
Merge: f80c1dd 3636ea7
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sat Mar 2 23:41:20 2013 +0000

    Merge remote-tracking branch 'ashok/globalsearch'

[33mcommit f80c1dd5f214b74d0e36eec37ae5b3a39d47c10f[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sat Mar 2 23:33:11 2013 +0000

    update default charset to UTF-8

[33mcommit 3636ea7fcffb075aaa4249ac9305df8ae498f1ca[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Sat Mar 2 16:54:06 2013 +0530

    Included ClientIdentifiers in global search scope

[33mcommit 194510964744d6c68ea2aff3ff0443e096863220[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Fri Mar 1 21:35:02 2013 +0530

    Implemented basic Calendar services, GET, POST and PUT

[33mcommit d90a07ba6c00f7572d153749ada5c0f9fe657c38[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Feb 28 17:25:17 2013 +0000

    MIFOSX-175: move derived fields into loan summary object (but still part of loan table for now.

[33mcommit e6434aac8bbf964f2ab45b1c2f833ec675f35077[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Feb 27 23:40:43 2013 +0000

    add 2013 roadmap

[33mcommit 57c3b58f95aeb7f53a960835512d3dc7f5382e87[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Feb 27 23:39:29 2013 +0000

    update DDL and demo dumps to patch-15

[33mcommit fd9954a9770e2cf2ce09b0f5f74af1ce2b19b5f5[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Feb 27 15:49:38 2013 +0000

    formatting

[33mcommit 6e60f5baf897a5ac80f9866bb9a3953dbb95f8fe[m
Author: Nayan Ambali <nayan.ambali@gmail.com>
Date:   Wed Feb 27 19:23:02 2013 +0530

    fix: get group summary details

[33mcommit d322e7064968cdaf245347d01e2378a72e0a3bc7[m
Author: Madhukar <madhukar@hugotechnologies.com>
Date:   Wed Feb 27 12:09:23 2013 +0530

    Handle exception for retrieving non existed resource for deposit product

[33mcommit 998c9bb3b242b2b6659f5e999d446d8612d4491d[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Feb 27 01:27:18 2013 +0000

    MIFOSX-175: WIP - adding loan summary fields to loan table and resultant refactor of loan schedule and loan summary retrieval code.

[33mcommit 0bfaf2cbaf2e806bae43f8a2cdb6e8a478babe45[m
Author: Nayan Ambali <nayan.ambali@gmail.com>
Date:   Wed Feb 27 00:48:11 2013 +0530

    adding group summary details

[33mcommit fdfaab10387363f94a7c3404ecf9c0b28a947a06[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Tue Feb 26 17:26:09 2013 +0530

    basic global search implementation

[33mcommit eb09a39a8ea36a8655b22c84e457f7dfa484ce72[m
Author: Madhukar <madhukar@hugotechnologies.com>
Date:   Mon Feb 25 14:10:31 2013 +0530

    FIXING RETRIEVAL OF DELETED DEPOSIT PRODUCT
    
    Remove per row audit for deposit product

[33mcommit e08b0044dafb16a9968c1f428798cebd9df4b89e[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon Feb 25 17:18:28 2013 +0000

    fix prob with linefeeds on csv export & allow reports to have normal json output

[33mcommit 40f991c46787b1020748ce4089d376c8358e0821[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Feb 25 11:04:13 2013 +0000

    update core DDL and demo dumps with latest patch.

[33mcommit 0cb9ab3c56c1fe754fc795dbbccadd6d9f1aba0d[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sun Feb 24 23:47:24 2013 +0000

    MIFOSX-172: finish off update to api-docs around loans, loan transactions and loan charges apis.

[33mcommit bddfa98c4ab94daea6c02d699d7477a7a0c0d3ae[m
Author: Nayan Ambali <nayan.ambali@gmail.com>
Date:   Sun Feb 24 23:48:48 2013 +0530

    added Staff details to GET /groups api

[33mcommit 328f294e1abc4bf101b8d0317cc5eccc2f1d5c17[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Sun Feb 24 14:22:38 2013 +0530

    fixing minor issues detected while testing cash based accounting

[33mcommit 040c33d6089b05df71d147a6ec239ceb132ad081[m
Author: Madhukar <madhukar@hugotechnologies.com>
Date:   Sat Feb 23 12:19:15 2013 +0530

    Fixing validations for update deposit products

[33mcommit 9f9378376b377f5b1b2af8dad269dd0557e29495[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Feb 22 15:18:54 2013 +0000

    formatting.

[33mcommit 634a66cbe9c9a5f7fbec41f0f8e42e4dcb7e7e9d[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Feb 22 14:30:47 2013 +0000

    MIFOSX-172: WIP - update api docs on loan application workflow api.

[33mcommit 29360a8fd6c21acf7fa33d496e80671a61d12bac[m
Author: Madhukar <madhukar@hugotechnologies.com>
Date:   Tue Feb 19 13:53:47 2013 +0530

    Saving product Audit conversion
    
    Saving Account Audit Conversion

[33mcommit 5e69f4a5587e7fb05cbc728a06c9d99010f1e221[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri Feb 22 12:19:56 2013 +0000

    fixed mifosx-192 & altered base reference DDL

[33mcommit 1effc7bbe4915b4aecaea00a461f5a82de300dd4[m
Merge: 726c1ca 03c369b
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Feb 21 13:52:41 2013 +0000

    Merge remote-tracking branch 'nayan/MIFOSX-164'

[33mcommit 726c1caac066bb937240318953441ad7282ed56e[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Feb 21 13:28:22 2013 +0000

    WIP core reports

[33mcommit 03c369b6d5a58676a77c282ed4e626656cd987eb[m
Author: Nayan Ambali <nayan.ambali@gmail.com>
Date:   Thu Feb 21 18:54:27 2013 +0530

    Mifos parity features centers/groups

[33mcommit a94dcb6f50f2750ca239ee2c9f16a2276ee0996d[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Feb 21 01:59:27 2013 +0000

    update DDL and reference data files and also demo dumps with latest schema and core reports work.

[33mcommit fd2515c1ff0905b9ad926fafd144b1b7300f4003[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Feb 20 20:00:33 2013 +0000

    remove warnings.

[33mcommit 3b636fcc2ef1b176f109ec0f287c79e68217327e[m
Author: Nayan Ambali <nayan.ambali@gmail.com>
Date:   Thu Feb 21 00:04:35 2013 +0530

    Updated template and add group apis

[33mcommit 22e1bef324d5ac7120bd814a455cae11dbbe5f39[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Wed Feb 20 20:23:20 2013 +0530

    minor update to journal entries

[33mcommit bb7662b99509ed1b61e689f133e250de9b194f62[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed Feb 20 13:07:35 2013 +0000

    WIP core reports + r_enum_value update

[33mcommit 7743563597d47974d7fc04d1b9efdabb3b27cf60[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Feb 20 12:18:33 2013 +0000

    MIFOSX-172: WIP - update validation and api around loan charges.

[33mcommit 9cc9706df1cb494a9dfa9262872557a8668e2789[m
Author: Nayan Ambali <nayan.ambali@gmail.com>
Date:   Wed Feb 20 15:57:51 2013 +0530

    Update mifosng-db/patches/11-ddl_group_level_table.sql

[33mcommit cad20ff7b51fcdc952e9bd8d2815ad9194c7cc32[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed Feb 20 10:15:16 2013 +0000

    WIP core reports

[33mcommit 2d9a42e04a3be19e6320717de2a5a4c04fc05371[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue Feb 19 23:47:14 2013 +0000

    wip core reports

[33mcommit c13632c47c4a7c424671700ae4a69cce18d3066a[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue Feb 19 16:07:06 2013 +0000

    core reports - remainder of loan_purpose param & do loans awaiting approval / disbursal reports

[33mcommit 77a44f7b1930728b33874722829ac20aac78dfb0[m
Author: Nayan Ambali <nayan.ambali@gmail.com>
Date:   Tue Feb 19 21:04:07 2013 +0530

    DDL for GROUP_LEVEL table and modifications to GROUP table

[33mcommit 8822ecea5041ac1007a3047cb8a5c53addff1f56[m
Author: Nayan Ambali <nayan.ambali@gmail.com>
Date:   Tue Feb 19 20:32:25 2013 +0530

    New api to get contextMenus list for creating groups at different levels

[33mcommit 524206f0ff3469550019233bf64d518d1647e0f3[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Feb 19 12:59:51 2013 +0000

    MIFOSX-172: WIP support loan writeoff, closure scenarios with accounting.

[33mcommit 87eb0a6fe8470cedc94148444305057c2f15cff8[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Feb 19 00:45:33 2013 +0000

    mifosx-172: wip - support adjust transaction for accounting.

[33mcommit bb63d2cb2d14459b1b9070b56bea2e7f7931b90e[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Feb 18 15:32:56 2013 +0000

    MIFOSX-172: wip - support for loan disbursal and undo with reversing transactions.

[33mcommit 40306084428e7ba89e274e9a7af9ba9c8ca4df3f[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Mon Feb 18 20:29:45 2013 +0530

    code cleanup and renaming entry date in journal entries

[33mcommit bf2bdde478db3866311dabbb57177ed1da25eb09[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Feb 18 11:27:35 2013 +0000

    code formatting.

[33mcommit e20869ad9b260ca3391c907f6a44e1620b51546c[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Feb 18 10:55:55 2013 +0000

    remove warnings for unused code, formating in savings area.

[33mcommit cc27eccd79575552a47e017e900386b26d108fc2[m
Merge: 8975d1f 7a2f3d1
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Feb 18 09:57:22 2013 +0000

    Merge remote-tracking branch 'madhukar/MIFOSX-144-Final'

[33mcommit 8975d1ff58974714da4267b04a4daaa7772ae26b[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Mon Feb 18 14:43:10 2013 +0530

    wip: updates to accounting documentation

[33mcommit 7a2f3d10a22f7e72368e3af427b7517760d1f9ce[m
Author: Madhukar <madhukar@hugotechnologies.com>
Date:   Tue Feb 12 17:07:45 2013 +0530

    Change the savings and deposits responses to string
    
    Deposit Product audit conversion
    
    Deposit Account Audit Conversion

[33mcommit abf075a0c0209550b96df5e60cac109cf14e26cc[m
Merge: 8e02d51 7941b0d
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sun Feb 17 12:00:25 2013 +0000

    Merge remote-tracking branch 'vishwasbabu/mifosx-161'

[33mcommit 7941b0dcf36b72d2d03daa00062315b8e4ed539a[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Sat Feb 16 20:20:42 2013 +0530

    wip: adding audit for accounting

[33mcommit 8e02d51b1498847c746aa5dac19eeaa148b6a453[m
Author: Nayan Ambali <nayan.ambali@gmail.com>
Date:   Sat Feb 16 18:01:49 2013 +0530

    Added assign/unassign loanofficerto group and update api docs

[33mcommit 16ae622590a8fc2914168f4e770e7d8094e94119[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Feb 15 17:42:01 2013 +0000

    WIP: change to approach for undo disbursal to just reverse transactions rather than clear them down. Doing this leads to observation that need to change the way we are supported reversing or contra entries in the portfolio area for loan transactions.

[33mcommit 24a50128523eefaeaa401722c7fcce353e2fc0ec[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Feb 15 11:17:41 2013 +0000

    Update README.md

[33mcommit b50f463677e84040894f2776bcb8686fa5709b77[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Feb 15 11:14:43 2013 +0000

    Update README.md

[33mcommit 4f285063b0fe0df4e032fbc062200c4897b98448[m
Merge: ffed686 05ab9dc
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Feb 15 03:07:42 2013 -0800

    Merge pull request #123 from gsluthra/TravisCI-setup
    
    Added travis ci configuration file

[33mcommit 05ab9dc2339a7b7df925179624465b31c589a3cd[m
Author: Gurpreet Luthra <gsluthra@gmail.com>
Date:   Fri Feb 15 16:29:03 2013 +0530

    Added travis ci configuration file, and a shell script that would be executed by travis ci to run the tests

[33mcommit ffed68614422ca63190673d3d2ab3fa4d4ed1139[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri Feb 15 01:00:58 2013 +0000

    WIP core reports

[33mcommit 949fa8b24f3820dfd84b7b3b76457c35fbc092c4[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Feb 15 00:01:52 2013 +0000

    upate default demo ddl.

[33mcommit 8cb35fdb2647f8fc27121fc5993a7255c369788e[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Feb 14 23:47:50 2013 +0000

    update ddl and demo schema files.

[33mcommit 285594e125e7a799aaa56d8b11b0f47cf2691b54[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Feb 14 23:13:38 2013 +0000

    support for tracking what user took action on loan and being able to provide that data as part of api.

[33mcommit 0ec56c32f89730f2ad71a738b2492a30e5baf607[m
Author: Nayan Ambali <nayan.ambali@gmail.com>
Date:   Thu Feb 14 20:10:34 2013 +0530

    Added option for group to have loan officer assigned

[33mcommit d6431bffc1dbdd40b742498589bdaa357b130f6d[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Feb 14 14:37:00 2013 +0000

    save core reports WIP

[33mcommit ea854b8754e6ccefb96b8a520fd1dcce0fa42856[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Feb 13 21:05:32 2013 +0000

    tidy up unneeded pass back of permissions with loan account data.

[33mcommit 20cb05c70823c9d11a8f645c0aa0328db24b9796[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Feb 13 16:44:23 2013 +0000

    Update README.md

[33mcommit e54ca69ce7406181f7082e892e15d5830b0d479a[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Feb 13 16:40:53 2013 +0000

    Update README.md

[33mcommit a1e1491167fe0023225b53ea6715fe9af2147a55[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Feb 13 16:38:40 2013 +0000

    Update README.md

[33mcommit 6b593abd2bbd08286b1bf512fb1dc1f3b8732f7a[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Feb 13 16:38:27 2013 +0000

    Update README.md

[33mcommit 13cf9e98a2532533959744bceedca77c1864eb55[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Feb 13 16:18:59 2013 +0000

    Update CHANGELOG.md

[33mcommit e42453f276db7f43fbce25c0cfdbba3aa736ec54[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Feb 13 16:18:28 2013 +0000

    Update CHANGELOG.md

[33mcommit 37ed43588efe2aef5b5441a4465a586463b6b24a[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Feb 13 16:04:47 2013 +0000

    add content on versioning approach.

[33mcommit 1ac850157a7c839e1ed53a27378ca599a4ce5abf[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed Feb 13 15:46:29 2013 +0000

    Add WIP copy of core reports

[33mcommit 720a8dc83de80dd768bdb0abd36e509d4a879422[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Feb 13 15:44:48 2013 +0000

    add changelog

[33mcommit dd6fcb6309544c02051de7d59421b8483bf3781a[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Feb 13 15:40:31 2013 +0000

    code contributors file.

[33mcommit cee0601edc823fc0118afd32173621f3347028bc[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Feb 13 15:05:22 2013 +0000

    add license to webapp folder which is missed by default setup of gradle license plugin.

[33mcommit a13a313718516cd869cd5b49cc21ba3b4992fd82[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Feb 13 14:59:10 2013 +0000

    update files with MPL v2 using gradle plugin.

[33mcommit 63cddd9e238b4dfc2fe016c276a68cc6a4d235db[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Feb 13 14:53:02 2013 +0000

    add license plugin.

[33mcommit 3c6f5bb526bb578a81bb16b71510bb6d3a179353[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Feb 13 14:28:49 2013 +0000

    add MPL v2 license to test push to new repository.

[33mcommit 28e154847508dd63b0fef46c72ca91714463a8e2[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Feb 13 14:06:55 2013 +0000

    WIP: submit and modify loan application. support for loan purpose, loan collateral, loan fees. Restrict modification of loan application details to be only allowed  in 'submitted and pending approval' state which is first improved required to reach loan and cash based accounting integration.

[33mcommit c66a7515aa6fc0483de92f519bd6b540dc417099[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Wed Feb 13 03:09:34 2013 +0530

    wip: documentation stubs prior to refactoring

[33mcommit 3850cd9fb9ff29be48909de1df502a898ae4b18f[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Feb 11 19:48:45 2013 +0000

    tidy up some bits around deposit products for example going forward.

[33mcommit 835b034926761a89bfda30eb964a1253ba4686a3[m
Author: Madhukar <madhukar@hugotechnologies.com>
Date:   Mon Feb 11 21:35:07 2013 +0530

    Deposit documentation fixes from pull#111

[33mcommit 91a3249c094cd32204c9caa49fae49e6560cd847[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Mon Feb 11 12:52:29 2013 +0530

    MIFOSX-36 - Added validation for Integer  parameters

[33mcommit 061872e4e6e400ea6379dc1fb3cf714d5cdaac54[m
Merge: cb8bc87 86dd2bc
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sun Feb 10 20:34:53 2013 +0000

    Merge remote-tracking branch 'vishwasbabu/mifosx-151'

[33mcommit cb8bc87bf7833cd8da6136e432f8e80a0f8c1af8[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sun Feb 10 20:29:01 2013 +0000

    update patch on tenants

[33mcommit 86dd2bc9c37b1a44132e89c9801d90a4e5e7faf0[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Mon Feb 11 01:44:31 2013 +0530

    wip: Refactoring and Documentation

[33mcommit 9a33ca6fb6fa959414a42ac10daca9ce3e2a4510[m
Merge: 70185e0 e91c821
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sun Feb 10 20:04:30 2013 +0000

    Merge remote-tracking branch 'nayan/unassignlo'

[33mcommit e91c821d1cf8289215c21327b91eaf35816052f8[m
Author: Nayan Ambali <nayan.ambali@gmail.com>
Date:   Sun Feb 10 22:23:36 2013 +0530

    Added business rules validatation for unassign loan officer

[33mcommit 70185e086e1776cb11c4279d03730ff25bb3b107[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Sun Feb 10 16:19:00 2013 +0530

    MIFOSX-36 - Add/Edit Code values

[33mcommit 169c1ce1784d8a77477ca1c63ab1ee32ed243c88[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sat Feb 9 16:52:14 2013 +0000

    another update to cover not properly formatted (utf8) report metadata

[33mcommit 8776d1e13ac5481f8e07706bc232814e61953cbe[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Tue Feb 5 18:49:06 2013 +0530

    This is a combination of 2 commits.
    
    Addressing review comments for Loan-Accounting bridge

[33mcommit b84abcee2ef5af13d10ebb2ac4b01533bc5cc25e[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Feb 7 15:23:24 2013 +0000

    update demo ddls with latest patches for permissions.

[33mcommit 5f7a2f9f3a17a915ed3e58668f3548d47a0f97fd[m
Merge: 9cb3ce5 0ef0478
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Feb 7 13:56:08 2013 +0000

    Merge remote-tracking branch 'ashok/MIFOSX-36'

[33mcommit 9cb3ce56f9d941166345d81704ac7005a9ecd61c[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Feb 7 13:07:27 2013 +0000

    update base reference data with update and remove loan officer permissions and also add in _CHECKER permission

[33mcommit 0ef0478d220ee9262e8e0c7e8488bcca5e7acd53[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Thu Feb 7 17:19:28 2013 +0530

    WIP Code Value MIFOSX-36 (database changes)

[33mcommit 7839ec825aaf7651a5868f10af9a9d0a5c7f005c[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Thu Feb 7 16:56:09 2013 +0530

    WIP CodeValue MIFOSX-36

[33mcommit b7fef480d5b9533bc88c2a827e4df3f5fd37a676[m
Author: Nayan Ambali <nayan.ambali@gmail.com>
Date:   Thu Feb 7 15:24:40 2013 +0530

    Adding unique constraint on Loan Product name

[33mcommit 22698f09dfa3720f5658c83bf609195c975bd429[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Feb 7 01:01:44 2013 +0000

    support fees on specified dates for FLAT interest type loans and loan schedules.

[33mcommit 059c2d4529de37b62fa41183d3a041ed14eee8b1[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Feb 6 20:41:28 2013 +0000

    update ceda schema with changes to datables from feedback with esther.

[33mcommit 5b366b66a47eecfd5348cc0ac85315666ac5ed86[m
Merge: 54535d9 19108aa
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Feb 6 16:39:28 2013 +0000

    Merge remote-tracking branch 'ashok/MIFOSX-36'

[33mcommit 54535d90ac7c5e721add6f31529a548d4e4eefc1[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Feb 6 16:26:03 2013 +0000

    add default coa to demo schemas.

[33mcommit 19108aab04891b40aea268dea6f4f587b501ed73[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Wed Feb 6 21:43:26 2013 +0530

    WIP CodeValue MIFOSX-36

[33mcommit ce3978525c9007ee152cbbb8188e289384ad9239[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Feb 6 14:30:11 2013 +0000

    update latest default and barebones schemas with latest details.

[33mcommit 327dde7fc0a3eafd469d7823cfb703b3a0f79e10[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Feb 6 13:46:08 2013 +0000

    tidy up schema ddl for demos and ceda.

[33mcommit 1c4b09dc3cbfbe77f3112051ed37c005bcf9fec3[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Feb 6 10:48:32 2013 +0000

    fix some spelling/grammar mistakes.

[33mcommit 8b6844a3c18410f5f5783062ef35ec178dce8982[m
Author: Madhukar <madhukar@hugotechnologies.com>
Date:   Tue Feb 5 18:55:32 2013 +0530

    Documentation of FD and Saving Accounts

[33mcommit 36197b4fe3a352a084e1fa3e337c8b469e4684a5[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Feb 6 00:09:27 2013 +0000

    fix up formating on api-docs.

[33mcommit 305ed1d1f3cc05adbfa909c5f941eb629b00e027[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Feb 5 20:19:28 2013 +0000

    fix up formating on api-docs

[33mcommit 0bb4fd282b7c55994f21f89e686dc3caf8a99b35[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Feb 5 20:53:44 2013 +0000

    formatting and rename of patch files.

[33mcommit 58ee1847e34f38538585498de078b3c126052c22[m
Merge: f73dc7e b711436
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Feb 5 20:44:50 2013 +0000

    Merge remote-tracking branch 'nayan/workspace'

[33mcommit f73dc7e742012647a8890e5df51398e3f13903af[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Feb 5 20:40:09 2013 +0000

    formatting changes on file.

[33mcommit 27bb852e3af0c9b83696094c099170ea73771793[m
Author: ashok-conflux <ashok@confluxtechnologies.com>
Date:   Wed Feb 6 01:30:03 2013 +0530

    Added name Unique Constraint to Role

[33mcommit b711436c9ee7dd9583cce08e50a231acea163db8[m
Author: Nayan Ambali <nayan.ambali@gmail.com>
Date:   Wed Feb 6 01:24:51 2013 +0530

    loan prodcut update method changed to saveAndFlush

[33mcommit 22ea9d4dffc98628ebe3a1231e40f483a5e0d2de[m
Author: Nayan Ambali <nayan.ambali@gmail.com>
Date:   Wed Feb 6 00:23:29 2013 +0530

    Adding unique constraint on Loan Product name

[33mcommit e7819f6c25d914aa892f550a67e6fb00e37bca01[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue Feb 5 15:26:16 2013 +0000

    api docs format updates

[33mcommit ba14e1bb01646264107a6fc2d3d1b7b0ad8c055b[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue Feb 5 13:53:00 2013 +0000

    api docs format updates

[33mcommit 82614f3fb42aa29959dcdc3173c176fc5614606e[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue Feb 5 13:46:04 2013 +0000

    api docs format updates

[33mcommit f9f3b22858345857df6906916357eb90dbba286c[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue Feb 5 13:08:01 2013 +0000

    remove bit of merge conflict text

[33mcommit c6b741651b024c5cd6298a9e827b9de7767cf890[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue Feb 5 12:49:45 2013 +0000

    update api docs format

[33mcommit 6bef172c7f225954759edb3bb3e8d75d2da69bf8[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Feb 5 12:38:37 2013 +0000

    update demo base schemas.

[33mcommit 7ab35207384d5d62b655a1ff912427587ad4fb6a[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Feb 5 12:28:46 2013 +0000

    update api docs after running through datatables api.2

[33mcommit 297263f5a00fefd1555791eabf02d27f24709fcc[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Feb 5 11:06:43 2013 +0000

    update api-docs and loan functionality from api.

[33mcommit 330522187008fdab4dff07826efa966edd5aa638[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue Feb 5 09:36:44 2013 +0000

    update core reports config a bit

[33mcommit 986dbfb61f8ac486552aa3cc9486a6cd94c208a2[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue Feb 5 08:43:36 2013 +0000

    base reference data utf-8 encoded

[33mcommit 8321dc7c4d40cf54b6beba69722eb60c547f0e08[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Feb 4 16:47:59 2013 +0000

    update api-docs in prep for demo.

[33mcommit df637aa1039f9f43d7bed346497738d4bcfe3d7f[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Feb 4 10:14:47 2013 +0000

    prepare some demos, reset existing demo to bare bones schema and some small data setup.

[33mcommit a00577a30c9d93f7ba46a98fc90f84ffa5e09920[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sat Feb 2 01:33:05 2013 +0000

    update demo dumps

[33mcommit 80138efb3e58c4aa2e7ce89da8e7df1b910e668f[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Feb 1 23:42:14 2013 +0000

    save reference data as utf-8 encoded for currency symbols, update bare-bones-demo to latest of core DDL and base reference schema.

[33mcommit 8c286349574d5ced4bef79edc5818ab17b1f6d6d[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri Feb 1 17:57:40 2013 +0000

    updated base reference data with core reports definition

[33mcommit 7676172fb918132699c22f378dbb7b85626ebbbe[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Feb 1 16:20:06 2013 +0000

    tidy up validation order of staff.

[33mcommit 614cd93b2aec196a3bdf537609692f238ddda608[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Feb 1 15:31:57 2013 +0000

    fix definition of advanace payment for heavensfamily payment processing.

[33mcommit b5135b0d2dfc84c529c95b865b9998d4ea1d6bd2[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Feb 1 11:49:32 2013 +0000

    update patches based on latest server upgrade.

[33mcommit 65e756374899fd5906b40b22760766aa9b0196d0[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Feb 1 09:37:18 2013 +0000

    tidy up validation around office transactions api.

[33mcommit 4a07e781715a1b719417fdf6003d0f3feac6a253[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Jan 31 15:09:18 2013 +0000

    tidy up validation for currency configuration.

[33mcommit 359b62a550dac95b3a69715e32652ebfaba9cbfc[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Jan 30 20:44:17 2013 +0000

    last update of ddl and base reference data with correct set of permissions.

[33mcommit b5e0e2982f55fe8fa2921d4aad0299c687df6936[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Jan 30 20:11:35 2013 +0000

    update ddl with correct permissions todate.

[33mcommit 07a4d7c4767fa2d77af6bbcb700d11a9c28bcfc3[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Jan 30 16:38:19 2013 +0000

    update loan products api-docs and validation. some refactor at point where loan product and GLAccount mappings meet.

[33mcommit dd9179daddb5e95e7626ec28840a4b33ba8bc68b[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed Jan 30 14:26:20 2013 +0000

    patch to put in r-enum-values used in reporting

[33mcommit 52ff4da56322d9cf49099ec4b25b425f9ea5468a[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Wed Jan 30 06:17:07 2013 +0530

    Package rename and very minor modifications

[33mcommit 08e804d03fe09d2050d2c4a04be0558d92726f14[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Jan 29 20:23:19 2013 +0000

    update loan products api docs

[33mcommit e0036e8d2bdcfad96a1e900d3e5e387e5c316aba[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Jan 29 13:24:35 2013 +0000

    drop audit columns on client and group entities. update client api docs. update ddl and demo schemas.

[33mcommit 5f4e1103c2012b5fedd57a001f1260163885cb8e[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Jan 29 12:28:45 2013 +0000

    remove old schema dump.

[33mcommit 3445ad56ced5203b69adb14266d2695e4b835f25[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Jan 29 11:44:40 2013 +0000

    update ddl, reference data and demo schemas based on patch

[33mcommit 036bb02df49ee0afc03a3f072e11861a4c50e815[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Tue Jan 29 15:27:20 2013 +0530

    wip:modifications to guarantor related services

[33mcommit af7c9b79899717016b1ef8db3c113a0d34bc9cab[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Jan 28 22:29:26 2013 +0000

    update api docs for charges, update validation of charges.

[33mcommit a99da667fb72dcc5503f46a5e78a71dcbdcc50fe[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Jan 28 16:54:37 2013 +0000

    add ability to delete office transactions based on chat with dick.

[33mcommit 0bc06f9215428041a7ba754f952324c457b112a5[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Jan 28 13:21:36 2013 +0000

    update staff api docs and validation.

[33mcommit 17f6e6ba666f5761362cb1408b460ad7bae4d1f4[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Jan 28 12:21:31 2013 +0000

    update docs on funds api

[33mcommit 495ca15fad589fa7fa02e8d1bcc97302b613fe71[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Jan 28 10:16:12 2013 +0000

    update api docs around codes, update validation from JSON API request.

[33mcommit b9ba666bfb91bbd503694cbb228f8959e5372159[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sun Jan 27 22:13:22 2013 +0000

    drop audit columns for office transactions, funds, staff, charges.

[33mcommit abea5e31162acb3db8dc22127b1b186816261f65[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sun Jan 27 00:32:34 2013 +0000

    update DDL of demo database. move portfolio specific serialization classes our of infrastructe packages.

[33mcommit d5c09659ff1ed972cbcbcc86ff40d7fcec82f35d[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sun Jan 27 00:04:01 2013 +0000

    drop audit columns on organisation currency, update api docs on configuration api.

[33mcommit 1ff1e53fb50788640f63f5cd6434b4f91fc1aa88[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sat Jan 26 20:26:16 2013 +0000

    update demo backup.

[33mcommit 071f5556f6136afb363fea087cbf4197a2a694c4[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sat Jan 26 20:23:22 2013 +0000

    drop audit fields for office tables, update api-docs, update DDL and schemas.

[33mcommit 1639fe695ec84ed7544fd7967fd131e4f47e8d20[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sat Jan 26 03:14:21 2013 +0000

    update schemas after patch.

[33mcommit d1d12abd30dbb11dd62b73fdc1e06103065eb287[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sat Jan 26 02:58:34 2013 +0000

    Continue tidy up of DDL and reference data, focusing on user admin area correcting api and implementation details.

[33mcommit 445e84fc79ef546cb0d1a2d5510e6eafac804235[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Jan 25 14:52:58 2013 +0000

    Update mifosng-db/multi-tenant-demo-backups/bare-bones-demo/README.md
    
    update description of whats in the bare-bones-demo

[33mcommit c63db1ce40184e3f5c1e2b2babf0bfe167a9fabd[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Jan 25 14:32:22 2013 +0000

    update core DDL + minimum reference data for deploying mifosx platform.

[33mcommit 216e493840fb43b4abd19a4930b81bb290029621[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Jan 25 11:35:11 2013 +0000

    fix validation on inArrearsTolerance for loan creation, update sql after production and demo server upgrade.

[33mcommit 91d50831f2649713018cc45cb0d9d083da8437a5[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Jan 24 15:19:10 2013 +0000

    allow user with read only permissions to still change their own account details.

[33mcommit 47f8dc9bc7d4d21bb9fcc7e298aa764a20e947a4[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Jan 23 16:50:05 2013 +0000

    update latest mifostenant-default schema for now.

[33mcommit 4c90f3fd6ee25a086001e7f4445f6a0c10201a78[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue Jan 22 17:26:52 2013 +0000

    allow generateJsonFromGenericResultsetData to handle double quotes in strings

[33mcommit 9b85e9b274ce39a04cd60bf70255f98f02ecf104[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Jan 22 10:54:14 2013 +0000

    removed unused warnings and code and format of code.

[33mcommit e8cc870f210c0e6ad9a1dfb6777a67791b886a1a[m
Author: Madhukar <madhukar@hugotechnologies.com>
Date:   Tue Jan 22 12:47:56 2013 +0530

    Add interest posting functionality for saving accounts
    
    rename_patches_after_rebasing
    
    remove existed db script file

[33mcommit 2ab7097b85219a5dbd791493cd5b3b8200a11a8c[m
Author: Madhukar <madhukar@hugotechnologies.com>
Date:   Wed Dec 19 16:20:32 2012 +0530

    Change declarations for enums and spell checks
    
    Adding Saving account & product creation from UI
    
    Add state transitions for Saving accounts
    
    Approve saving account state patch partially done
    
    Approval of saving account
    
    Soft delete of saving account
    
    Add intereset calculation method for saving account
    
    Interest Posting functionality not completed yet
    
    Saving Interest Posting without interest calculation method

[33mcommit 72e712de2412df9d6e91a26070f816816ebe2b14[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Tue Jan 22 03:08:44 2013 +0530

    accounting wip: addressing review comments

[33mcommit 0a30e4e08c9a4216b8e206a768da4cba0a484eb6[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Thu Jan 17 18:20:49 2013 +0530

    adding accounting module specific dto's for loans

[33mcommit bc20ae399ea2cb5e81a96a4063af6de8c6a04509[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Thu Jan 17 15:13:57 2013 +0530

    accounting wip : completed cash based posting includes writeoffs and adjustments

[33mcommit 0b9d76582f19233d6ef4cbc9c0198942e962ee17[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Jan 16 17:01:00 2013 +0000

    fix client update to support data integrity errors on accountNo and externalId.

[33mcommit 11e4a3879698480c9e4e8f46efa1fd9c8776076f[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Jan 16 16:41:17 2013 +0000

    add behaviour for checking if accounting enabled for loan product to loan and product rather than ask for data each time.

[33mcommit 5240d308b8e97060bc51513e7418e564677c73c9[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Wed Jan 16 13:58:06 2013 +0530

    accounting wip:cash basedJournal entries for disbursal and repayments

[33mcommit 38ee2d98aca63c3dce69855e8c37c8c1dc7f1195[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Jan 15 23:42:45 2013 +0000

    use display name for groups.

[33mcommit 9ac6dad1d24b60d55c15dd8c561d0d8fc9f7e210[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Jan 15 13:44:33 2013 +0000

    update patches after server upgrade. change order of client search results to use display name.

[33mcommit 9231d177e01a22d6ada66eed3f2880436a2926d1[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sun Jan 13 13:54:24 2013 +0000

    MIFOSX-37: support ability to update account no for loan accounts, also support ability to update external id (MIFOSX-46).

[33mcommit 0f5c3cb506821f9af39c3554067885f7bb47c13e[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Sun Jan 13 04:09:08 2013 +0530

    adding support for JRebel

[33mcommit 0049d07cacef5712333767b93d58995347c2a5f4[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri Jan 11 13:01:25 2013 +0000

    datatables: handle input containing just space(s)

[33mcommit fe7667b9eca9dfef1feee8d571f48e7d2ba8c2b8[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Jan 10 17:29:32 2013 +0000

    MIFOSX-37: api docs update and support partial response on new client fields.

[33mcommit 4cac334d71402ff09e38fd6a2427b81d5f36d66c[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Jan 10 17:01:09 2013 +0000

    MIFOSX-37: support ability to use different account number generation strategies for client account numbers (programatically for now, from ui,configuration later)

[33mcommit 7db9d409eb6624bcd7076c051b0337be25cb8029[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Jan 10 11:47:37 2013 +0000

    tiny, tiny change to ddl, hardly matters

[33mcommit 1cd21b9f49e9788c20f2d0d7d9ec86d90e53d5c9[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Jan 10 11:44:06 2013 +0000

    MIFOSX-141: move accountingType out of embedded class which is for fields common to both loan product and loan and move back into loan product only.

[33mcommit 4d81b78d5dfef0bea1e8d09c7d9a3683a90d8a15[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Thu Jan 10 00:58:22 2013 +0530

    accounting posting wip (only loan product mapping)

[33mcommit 29af2d5999a91214f5eee5b40373dc6b96e1683c[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed Jan 9 18:15:06 2013 +0000

    ensure datatables update/delete has the url put into the audit table

[33mcommit 029846bda2c53d117c09953ea0d02323eb29e5ae[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed Jan 9 15:19:30 2013 +0000

    remove super_user permission checks as they have been deleted (except for reporting_super_user)

[33mcommit 898aa44db0465e96bf2cd37d997cb92ac84d923b[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed Jan 9 14:34:22 2013 +0000

    update patch to not have id for permission

[33mcommit 986a3c5f91f4e8ae9e40ef53a8a2a22d0e44d9eb[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Jan 9 00:09:45 2013 +0000

    MIFOSX-97: update urls for getting data when viewing changes against existing data for maker-checker.

[33mcommit a0388e34d1a1e880bbde87169da1b4ad93cb81d0[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Jan 8 19:27:14 2013 +0000

    MIFOSX-97: check on read permission for configuration.

[33mcommit 444707b60b1cd7aa05e7f04f2acde349f25b3618[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Jan 8 19:13:44 2013 +0000

    update patch.

[33mcommit a0effe9e7a01ce03f472401cfce8290048caea0e[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Jan 8 18:32:49 2013 +0000

    MIFOSX-97: add configuration option for enabling,disabling maker checker at global level.

[33mcommit 239d60bc02ba1cf23200a88ec68b7ad87f1837cc[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Jan 7 17:18:50 2013 +0000

    MIFOSX-97: initial integration of changes into roles and permissions for maker checker purposes.

[33mcommit ff6eae40d32e95120cf51a7fcd2610ea9833fa81[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Jan 7 16:23:06 2013 +0000

    MIFOSX-97: tidy up of maker checker based on audit work so far.

[33mcommit 32f4bc1d300412d76a029fd1ccbe969f6aa919b0[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Jan 7 15:02:06 2013 +0000

    fix sql issue with checker inbox.2

[33mcommit f2b40dbbe2fdafd99ff45211b400db028221d4ab[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon Jan 7 12:41:21 2013 +0000

    minor patch update

[33mcommit e6de05f690a1f334ca0b58b935d2b96804237b3e[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sun Jan 6 17:45:03 2013 +0000

    remove warnings.

[33mcommit 690f0d4abcb222139a58f5cb1a9ba7d7850a88f8[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sun Jan 6 17:36:05 2013 +0000

    MIFOSX-9: Audit related work

[33mcommit 06f6ad2dff804a8c54e8c156a5dd892f53c9c751[m
Author: Michal Dudzinski <mdudzinski@soldevelo.com>
Date:   Sat Jan 5 16:24:18 2013 +0100

    Extended Groups API to return individual loans created from group.

[33mcommit 556f75fcc1e491be3e786d09d0fe01934ce68a0d[m
Author: Michal Dudzinski <mdudzinski@soldevelo.com>
Date:   Sat Jan 5 16:18:07 2013 +0100

    Workaround for permissions independent of repayment schedule.

[33mcommit 6177e41e70407bd76c82ae4a1ebb5f37e1bed945[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri Jan 4 16:26:25 2013 +0000

    pass only changes for logging datatables updates

[33mcommit b61ab594e405eb9186365429c76c8f713f77aaa4[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Jan 4 14:28:09 2013 +0000

    datatables formatting and first bit of flexible account number generation for clients.

[33mcommit 6d2151f4dbaa680cea1acfd4cee09d70bbda64c3[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Jan 3 18:01:07 2013 +0000

    add get api for single entry of a 'many' datatable

[33mcommit 48c40b5170bcad125972f9f46b397febb283854b[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Jan 3 14:35:54 2013 +0000

    small change to michals create individual loan application from group context work.

[33mcommit dde7c9980df858bcf5ac0e9c45723100367dec2c[m
Author: Michal Dudzinski <mdudzinski@soldevelo.com>
Date:   Thu Jan 3 15:00:22 2013 +0100

    Track group on member loan.

[33mcommit 8b666d3eea350fb5ed822df39c30f1a57e602882[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Jan 2 23:03:00 2013 +0000

    update dependency versions

[33mcommit 127a5efbc34a9d9e4f8722c5fd9302c1b7a94afd[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Jan 2 13:40:16 2013 +0000

    tidy up datatables api and ensure principal is paid off before interest in advance and partial payment cases.

[33mcommit 578bbc29548912bd9f626329f6022ef8a60f4973[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sun Dec 30 18:36:03 2012 +0000

    wip add audit for datatables

[33mcommit fbfcc32f787fd6bd22edf98ba878c5f4e386f59a[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Dec 28 22:14:29 2012 +0000

    Update README.md
    
    update roadmap text.

[33mcommit 8dc8dcad522299bca9d3cae37ce9c9aee6e08025[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Dec 24 13:57:30 2012 +0000

    update api-docs around client and client account summary.

[33mcommit c99053d2a44b75821508fe5fda8ad5c38c9a9db1[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Dec 21 15:28:02 2012 +0000

    update loan creation and retrieval.

[33mcommit e4ee41174d1561d0253d4a73a6a85da40b48f5e3[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri Dec 21 13:15:02 2012 +0000

    audit indexes/indices :)

[33mcommit 46c315815d1bc3fefc7fb0e40285346c6599c8ad[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Dec 21 11:45:37 2012 +0000

    clean up remainder of serialization code for reports, datatables and groups.2

[33mcommit 4e589cfeb809ff2557ca7597ea177a7a89d275d6[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Dec 20 20:00:17 2012 +0000

    break apart loan application concepts from loan contract concepts. ensure submit new loan application returns all validation errors.

[33mcommit a50983d9df0a907f38c00450fc5de421a5f0b77f[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Dec 20 14:32:21 2012 +0000

    MIFOSX-97: initial support of maker checker and audit support around transfer of loan officer and bulk transfer.

[33mcommit 322483698939cb6da2c6272fcfbfc242d89786dd[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Dec 20 12:36:46 2012 +0000

    handle loan creation with no charges after refactoring.

[33mcommit 94459fe2a5838045bc15b80f1a462473e2e5755a[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Dec 20 11:42:42 2012 +0000

    update core DDL and reference data to latest.

[33mcommit 2ee2cf1b1f1e541417d6b7e4091c0125c75b7f78[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Dec 20 11:02:19 2012 +0000

    rename to file as in documentation.

[33mcommit a8f4ff4bb69f63772a652632798dc73a05e68c34[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Dec 20 11:01:35 2012 +0000

    update demo backup.

[33mcommit bd09ecc967c972b47b34ea75207b93ea1ca68500[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Dec 20 10:58:15 2012 +0000

    mark patches folder as applied to server.

[33mcommit a43490102d3ce0ff01392ff5246d6ca437c47fab[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Dec 20 00:28:39 2012 +0000

    MIFOSX-97: inital pass at support for maker-checker and audit around loancharges.

[33mcommit e694355f8c62b3384191d32a713a39c8c4512394[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Dec 19 13:34:42 2012 +0000

    MIFOSX-97: further support for maker-checker and audit around loans.

[33mcommit 281931e19b7caa923f8e8e32807229f21c789261[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Dec 19 01:48:43 2012 +0000

    temp check for null charges

[33mcommit 1fb04811d5128841e1e73b666d9da032622fde31[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Dec 18 20:52:29 2012 +0000

    add validation for client notes.

[33mcommit 013276d9e4a6e2d609f146ad73ce573e32d63b6f[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Dec 18 20:41:12 2012 +0000

    MIFOSX-97: support audit for loan application creation, loan application modification, approval, undo approval, disbursal and undo of disbursal.

[33mcommit 43b1cdd3ef63f1a21a72e9c741f47c744b0a9a20[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue Dec 18 13:44:09 2012 +0000

    remove portfolio, org and user super user special permissions

[33mcommit 49357f13a71a3bcf64779ba47263e015640409d3[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Dec 18 13:13:36 2012 +0000

    remove client specific sql.

[33mcommit 9bb3f4e51f1e9cc57f0cdd9722b9d22c1776e18f[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Dec 18 09:55:07 2012 +0000

    renumber patches, remove getters from data object for savings account data. add fixes for madhukar around spelling mistakes and unused parameters.

[33mcommit 2e050d397f4be6d5707d17348d7bd65c8ece09ee[m
Author: Madhukar <madhukar@hugotechnologies.com>
Date:   Mon Dec 17 11:54:00 2012 +0530

    Adding Saving account & product creation from UI
    
    Adding missing db patch for m_product_savings
    
    repopulating the existed values on view product

[33mcommit 021aa14f905f9035ae09046ef0d5a7a8812f2b79[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sun Dec 16 18:06:31 2012 +0000

    wip audit api docs

[33mcommit e0dc9b22ee4e0d50e7c443db93dbbf43e4834fc4[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sun Dec 16 13:30:39 2012 +0000

    CEDA specific ddl.

[33mcommit 08a8b8cff086c0706854961f409b2903abf21286[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sat Dec 15 22:15:21 2012 +0000

    fix issue with updating root office details.

[33mcommit e52b66d56221082c694298ef3cecb4737c277ba1[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sat Dec 15 20:35:50 2012 +0000

    Add processingResult to Audit

[33mcommit 7b4f078cdaaf5e95b116a7d1cf2af18000fafa8d[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sat Dec 15 00:40:42 2012 +0000

    wip audit services

[33mcommit b2639fea2c0457772b0569c730f8193a8bff9216[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Sat Dec 15 00:14:41 2012 +0530

    minor bug fixes for accounting infrastructure
    
    adding sql files

[33mcommit adca69b0a05747ca2fc152f0d7a44bdc73b330b3[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri Dec 14 17:36:52 2012 +0000

    wip audit api

[33mcommit 7a7654e75f9057435f4b24bc59ae243827154069[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Dec 14 13:46:24 2012 +0000

    MIFOSX-97: add status for processing result of commands.

[33mcommit 8424d5232589743be6c5437874c84569bc7d6290[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Dec 13 00:33:33 2012 +0000

    wip audit view search

[33mcommit e21a8266d112682338b2e835040b005a5c22e021[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed Dec 12 22:32:25 2012 +0000

    minor changes (not functional)

[33mcommit 26fd03ae38430e927b99f2049bf52e2066287e3f[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Thu Dec 13 02:31:54 2012 +0530

    deleting temporary file created during merge

[33mcommit fa9f2129dde722183a09f019a1244bf0261e4ee2[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Thu Dec 13 02:09:48 2012 +0530

    accounting wip

[33mcommit 892afd409d833de3e829bd1c0491799de84f2f5e[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Dec 12 14:00:05 2012 +0000

    MIFOSX-97: move maker checker to cleaner implementatin on handlers with change detection on updates.

[33mcommit 3a73d9aacda48966fde736d00859d736de530d2e[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue Dec 11 22:53:28 2012 +0000

    new audit read permission

[33mcommit 08846d7b1f3f997e61ca1e734bcbf747397fb558[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue Dec 11 22:31:55 2012 +0000

    WIP audit view

[33mcommit 401770c696e6fa918f7b76962070db9348c10adc[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue Dec 11 21:31:22 2012 +0000

    WIP audit views

[33mcommit 6f3fcf31b69be0b88f755e6604b78029967fd609[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon Dec 10 13:12:21 2012 +0000

    WIP Audit views

[33mcommit d95a190efba0604841fae179d081a75ef2aa0b8c[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon Dec 10 12:26:48 2012 +0000

    WIP Audit views

[33mcommit cc0e02696b19a6ae3f2d8baf8a5417124b7c6106[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sun Dec 9 21:27:38 2012 +0000

    WIP Audit services

[33mcommit 3cc91463c031889462da13f34fa386a19f881555[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sun Dec 9 20:57:28 2012 +0000

    WIP Audit services

[33mcommit 0717d02a38fb9c9de3bc9164eb32c6496f152724[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sat Dec 8 01:12:08 2012 +0000

    reenable note against deposit account.

[33mcommit ba24d205f93a8d5de13de1780656c731d0496cb4[m
Merge: 3901a06 2ec422f
Author: Madhukar <madhukar@hugotechnologies.com>
Date:   Sat Dec 8 01:03:49 2012 +0000

    Merge remote-tracking branch 'madhukar/MIFOSX-43-Final'

[33mcommit 3901a06ad8894fdf7df1e86963c2b9f24c33946e[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sat Dec 8 01:02:22 2012 +0000

    MIFOSX-9: ensure @Components are picked up in audit package. fix sql for retrieve of audit data.

[33mcommit 3b27bcc4d1f1ff1741549cf2eb968b2c3e48332b[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri Dec 7 23:29:10 2012 +0000

    WIP Audit

[33mcommit 2ec422f89d35826043248240a6e61488afbdd3b3[m
Author: Madhukar <madhukar@hugotechnologies.com>
Date:   Sat Nov 10 12:03:47 2012 +0530

    Add Interest posting functionality for fd
    
    Basic for rd schedule creation-1
    
    Schedule creation for RD
    
    PrintPdf For FD
    
    Print RRMacs Demo
    
    FD Certificate Generation

[33mcommit 13d3221262c11da071882138709220fe460021df[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Dec 7 20:56:52 2012 +0000

    fix small error.

[33mcommit 68e094dd2fc8d03f95115e608c406f92fb6cafa2[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri Dec 7 16:42:12 2012 +0000

    WIP stub for audit service

[33mcommit fb4dfe799687dfbed91fca0fb413e50fa3a236f1[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Dec 7 13:54:43 2012 +0000

    MIFOSX-97: change detection on user admin areas.

[33mcommit f5c1225dfa09d7d0326fc5640872425237a5389d[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Dec 7 00:10:46 2012 +0000

    MIFOSX-97: detect changes on update of role. split out image part of clients api into its own resource class.

[33mcommit c966892b37453741ff65003a71a4b6bb0d0a8c77[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Dec 6 23:30:36 2012 +0000

    MIFOSX-97: redo permissions for maker-checker and roles. api expects json map of permissions labelled by parameter name 'permissions'

[33mcommit 5ca0604aa278d6e5339299a4e7e3cdaa92bc5df3[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Dec 6 20:10:46 2012 +0000

    update ddl and reference mistakes.

[33mcommit 669503b34b25b9a4e24bbd616ad6d86a6c0bd3c4[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Dec 6 15:31:54 2012 +0000

    MIFOSX-97: update way of handling updates for client and codes.

[33mcommit cb6ea0ae63b01672a7d98fdea2d352c0c417efcf[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Dec 6 14:20:44 2012 +0000

    MIFOSX-97: about turn on implementation of command pattern to support audit.

[33mcommit add6708ab0f9eb5ac0a2bd4ee69458fb6cba724b[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Dec 5 14:28:31 2012 +0000

    MIFOSX-97: breakout client notes subresource into its own class.

[33mcommit b680e50942213ebafe9d94058eb6d6d5781cd3ef[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed Dec 5 12:48:58 2012 +0000

    logging change

[33mcommit 075efe2712b5ce8accb663da4f0169109cf7cce7[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Dec 5 12:16:40 2012 +0000

    restore pretty printing support, fix client update due to detect changes error.

[33mcommit 184ec07487b96ee1027c0f2645689894a0bfba36[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed Dec 5 11:44:31 2012 +0000

    minor changes for reducing console logging

[33mcommit 6a012858d2ea13d5525a1e35896e60fc064c64a4[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Dec 5 10:56:48 2012 +0000

    fix issues with codes and office update, abstract some serialization functions.

[33mcommit 311477a87b92c8995eec2ec8b807d1fa3cb74217[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Dec 5 01:08:59 2012 +0000

    MIFOSX-97: tidy up serialization of commands for clients and client identifiers.

[33mcommit 26470d026dfd275c00072a429d5f76f26674b156[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Dec 4 18:02:31 2012 +0000

    MIFOSX-97: continue tidy up of serialization.

[33mcommit bb6ef42b418b91144f088a5a6efebd7b491ba690[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Dec 4 15:32:09 2012 +0000

    MIFOSX-97: tidy up serialization approach for currencies, offices, staff.

[33mcommit 37ae6dae9e57e39426b8fa8156d49e83c9e42953[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Dec 4 15:29:11 2012 +0000

    formatting changes for savings related code.

[33mcommit d1a1ed60abba749541a89ecf469c337711fbbb48[m
Author: Madhukar <madhukar@hugotechnologies.com>
Date:   Tue Dec 4 15:10:06 2012 +0000

    Madhukars interest posting for fixed deposit and recurrint deposit work.

[33mcommit 2dd5c5a7cd456f64e1ad79d961a3f52079d6874f[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Dec 3 17:11:46 2012 +0000

    MIFOSX-97: iterate for serialization parts of platform infrastructure and for codes resource.

[33mcommit fe77584320447e595f3575992c1d6bfaa7159c44[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Dec 3 13:58:36 2012 +0000

    Fix issue with application not picking up on some api resources components through annotation scanning.

[33mcommit 73c7be839dffdb5c25fc074793ae96a0beb3f3ae[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Dec 3 02:01:11 2012 +0000

    restructure into accounting, portfolio, organisation, user administration and infrastructure related packages.

[33mcommit 388aebd781230d74ca507274b92dea1d67be6323[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Dec 3 00:41:39 2012 +0000

    package tidy up.

[33mcommit f73d42857e288b4fac40f06a1c0749814dfd53a3[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Dec 3 00:20:43 2012 +0000

    MIFOSX-97: repackage remaining savings related code.

[33mcommit 082d77f5b68c5275ddff9b895e680d04be8ca19f[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sun Dec 2 21:10:23 2012 +0000

    MIFOSX-97: organise classes into new package structure.

[33mcommit c395d96517b9084e8f2305074125ca437aabc9b4[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sun Dec 2 11:09:43 2012 +0000

    script to update some permissions settings

[33mcommit 93fef5397480464b2281073e5f5a6fc55768638e[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sun Dec 2 10:30:00 2012 +0000

    another test commit.

[33mcommit 1a5e36f9e1e138d5faf44ac869d6b6c81443c9fe[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sun Dec 2 10:15:08 2012 +0000

    test commit.

[33mcommit 5a492e03db062694daa2f02f917e3774ae28555d[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sat Dec 1 17:09:49 2012 +0000

    MIFOSX-97: move gaurantor under loan account for now.

[33mcommit 6fe8b41ca956163af41a2022a0b78c6ac9c80a23[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sat Dec 1 16:56:55 2012 +0000

    MIFOSX-97: repackage document management code.

[33mcommit 9e4cc15441dfa7968dccda1201672f791ea6560d[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sat Dec 1 15:59:56 2012 +0000

    MIFOSX-97: package restructuring between loan and loan schedule.

[33mcommit 017cc717e59a79c5ac670dcf61d5e505eb94c733[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sat Dec 1 15:38:59 2012 +0000

    MIFOSX-97: move loan packaging.

[33mcommit df283eb53385723dab25e5b1be678a887b7c1214[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sat Dec 1 14:44:14 2012 +0000

    MIFOSX-97: move validation login to where the data exists - the command.

[33mcommit 9f65ff3cbdcd90c63567d97997d1550131726b55[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sat Dec 1 14:08:28 2012 +0000

    MIFOSX-97: move package structure movement and support for command approach.

[33mcommit 38ee5d130dcf7251ba76d0c01fe01e584f27cff9[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sat Dec 1 13:38:23 2012 +0000

    add/update a couple of permissions

[33mcommit 7ac55812d6bdce4c3b6ab80637d8f860c62c8531[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Nov 30 16:35:01 2012 +0000

    MIFOSX-97: move loan products to command handler support.

[33mcommit a3970af6c20d3831ee1ff38051a64efa7dd2cec0[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Nov 30 15:34:05 2012 +0000

    MIFOSX-97: continue move towards command handling structure to support maker-checker style processes.

[33mcommit f58f211664dd6ffb77e34710a535029b6609b6bf[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Nov 30 12:23:05 2012 +0000

    MIFOSX-97: global configuration to enable disable maker checker support on tasks. hardcoded to disabled by default for now.

[33mcommit d2c751f2eb8ac54cbed1b66bc54dd3cfc9c2cddc[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Nov 30 12:58:51 2012 +0000

    format changes

[33mcommit 48b3693944b615b678b4fec0babbc9d86ce231b8[m
Author: kiran <kiran12b0@gmail.com>
Date:   Fri Nov 30 17:08:20 2012 -0800

    Generate Pdf option for reports

[33mcommit 879ffdaa7ee3621020865bb9c3f2f69bf2853b70[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Nov 29 20:41:33 2012 +0000

    MIFOSX-132: tidy up of code and code values implementation so far.

[33mcommit 41536b1beb66f411b810c445d4f4086d317b38a9[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Nov 29 20:11:38 2012 +0000

    MIFOSX-132: tidy up system defined implementation.

[33mcommit b001e3f9997a7b9ace2cacd54be532469338eea4[m
Author: ismail <ismail.gharbi@endarabe.org.tn>
Date:   Thu Nov 29 17:58:15 2012 +0100

    add system defined checks to codes

[33mcommit edae10ed4b3db6addb4d7e83c57f0452a05fde14[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Nov 29 11:54:40 2012 +0000

    MIFOSX-97: restructuring of api resources, data, commands and the services and domain concepts they use. The api is not responsible for logging the command which is then in turn processed. When maker checker is enabled no changes are persisted through until a checker approves them.

[33mcommit bd678bd2b203f10c40a85727ac1ecbe79e08319f[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Nov 27 13:54:53 2012 +0000

    ensure spring data jpa repositories in accounting package are picked up correctly for spring wiring. keep ordering on patches.

[33mcommit 5c38e151e88950ccdfc3d0a9740163e11a26d739[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Tue Nov 27 18:37:47 2012 +0530

    wip accounting service
    
    moving accounting wip code to new package

[33mcommit d997add560539673a89d481f76ee4b8e159ff7ab[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Nov 27 01:27:59 2012 +0000

    move commands and validators into their own package for useradmin area.

[33mcommit 72881c740bd47618bf63a4a5ea1f8b1dbfeef0be[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Nov 27 01:16:41 2012 +0000

    restructure packagin again. move user admin related code under infrastructure.

[33mcommit 71d573cae0b6bf2e171de84e918029b53cc072e7[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Nov 27 00:48:12 2012 +0000

    fix permissions small repackaging on command related classes into future package structure of org.mifosplatform

[33mcommit 65206daf30900557843df23f59240f7c8c930100[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Nov 26 21:38:54 2012 +0000

    code formatting police.

[33mcommit 0549858dea644beb407ec379a517f63b29e32919[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Nov 26 21:32:53 2012 +0000

    MIFOSX-97: move permissions checking to api and cater for _CHECKER permissions needed.

[33mcommit 02617ce7f67cbc73c7ea8c022cd19e94df8a50c1[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon Nov 26 21:06:30 2012 +0000

    add service to update can_maker_checker field on permissions

[33mcommit 19fea3ed00afd22fc26d55dd281f130bb3ab7d61[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Nov 26 18:17:53 2012 +0000

    MIFOSX-97: initial integration of maker-checker capability on permissions with actual api services.

[33mcommit 092eabc0a0c5f0e363746cd4c311ce2e504d59ff[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon Nov 26 14:30:56 2012 +0000

    update permissions work to reflect checker permissions (for maker-checker)

[33mcommit e4e285d9432ac9039c041431fd058dcd23ab8547[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Nov 26 13:09:42 2012 +0000

    resetme

[33mcommit 8f7487340842fc8f1aa103903e61a418691976ce[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Nov 26 11:42:09 2012 +0000

    update backup of demo database after server upgrade.

[33mcommit a5096aad88b32a2cac5e562f0af0e5214ac05d79[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Nov 26 00:33:32 2012 +0000

    fix up some issues with updating client details. needs to go onto upgrade server straightaway.

[33mcommit fbf54d3c64d8cdc316cc2ba03adb3a500fb63119[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sun Nov 25 02:48:55 2012 +0000

    update base DDL and reference data after server upgrade.

[33mcommit 8859c447658d50b74ec7257d2600defccd3267a2[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sat Nov 24 15:40:17 2012 +0000

    fix error in inserting datatable permissions

[33mcommit 1daf8edac59418584a9aaf507a117a14e37c225c[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sat Nov 24 12:20:23 2012 +0000

    tidy up patches.

[33mcommit d0ec32911573862c10235b95e153303bc19d8928[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Nov 23 17:53:06 2012 +0000

    some formatting changes. sorry.

[33mcommit c36370ae83d4905ec2ec54a6c89b5ab7e6337682[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Nov 23 17:37:23 2012 +0000

    update patch

[33mcommit 10c343373c334004360b52a80cab28b6663ba4bc[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri Nov 23 14:04:36 2012 +0000

    remove remaining old permissions code and update api docs for roles and permissions

[33mcommit f7c7bfdd5f7ea77bd344152ed0e18d0cff1fb17b[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Nov 23 13:02:28 2012 +0000

    MIFOSX-97: enable maker checker flow for update of role permissions.

[33mcommit e8676832ee9916a4e51ed61bc6bea5b34aad1a05[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Nov 22 18:08:57 2012 +0000

    remove additional fields functionality from app and api docs

[33mcommit 79ff5a75118c917e080d399ddca7ca9e01adf4c4[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Nov 22 14:51:53 2012 +0000

    MIFOSX-97: process role commands through command_source table.

[33mcommit 85de5e92c25582831231f167f7887764be116e98[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Nov 22 12:22:41 2012 +0000

    MIFOSX-97: some tidy up around users,roles and permissions in prep for command work.

[33mcommit baba0b809e700cd0c6c35028b67785e5062dbc5e[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed Nov 21 21:30:17 2012 +0000

    remove permission maintenance from add/edit role

[33mcommit 43fb5c329464842c3d97e14006dcb8962467037a[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed Nov 21 20:51:16 2012 +0000

    MIFOSX-126 - Objective 1 Complete - new permission regime implemented plus a more intuitive UI

[33mcommit 447b1807901976178a140e9d532846f8d106c18d[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Nov 21 16:34:15 2012 +0000

    example of roles api for john.

[33mcommit 4101cfbcf5216394e0199d919d6db34d3dc26935[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Nov 21 17:06:50 2012 +0000

    MIFOSX-109: some formatting changes and removal of unused items.

[33mcommit e874efc3ac6022ee80649c78d2ed23107e19b2d8[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Wed Nov 21 16:35:59 2012 +0000

    MIFOSX-109: vishwas work on gaurantor squashed into one commit.

[33mcommit 9aa9b870f0975b522a0d4ec401392dc497dda9a5[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Nov 21 14:22:47 2012 +0000

    MIFOSX-97: refactor into command handling logic, prepare for sql based approach for change detection.

[33mcommit b8961d791405cf245e2322af0c7ff821a8323764[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed Nov 21 13:48:44 2012 +0000

    add is_system_defined flag to m_code

[33mcommit dc718d33c9e9679a078a31a3804756902284b52b[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed Nov 21 12:28:04 2012 +0000

    correct authorisation check for UPDATE_CODE (missing quote)

[33mcommit d03705f51c6a0242f506f3b6d4de66735e2699ad[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed Nov 21 11:53:45 2012 +0000

    wip role permissions update api

[33mcommit b085f1872da1d62058283c18539d7e68c4e8639f[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon Nov 19 22:30:20 2012 +0000

    wip permissions - script improvement

[33mcommit b48763a1049d9dd475a740997da89570b1107777[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Nov 19 21:46:04 2012 +0000

    move class into data package.

[33mcommit f79da8144b6cbce3dc970c8961c7b49f60e498e3[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Nov 19 21:13:28 2012 +0000

    MIFOSX-97: wip on supporting maker checker approach in platform starting with clients flow.

[33mcommit 30d4ca7681671e37d90a412a7a444a4192516bd4[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon Nov 19 16:57:19 2012 +0000

    wip permissions

[33mcommit f32618779489dab9f8b872b464cfad1b4fdba9cc[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sat Nov 17 16:01:35 2012 +0000

    update latest permissions work for reference data.

[33mcommit 739dc31f296fb32e1c97797cb819a2ac74bfc51d[m
Merge: f73ef79 7031ed4
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sat Nov 17 15:10:18 2012 +0000

    Merge remote-tracking branch 'mdudzinski/MIFOSX-73'

[33mcommit 7031ed40b4503d6bc744f9c9841bedf486c5bf5a[m
Author: Michal Dudzinski <mdudzinski@soldevelo.com>
Date:   Sat Nov 17 15:55:12 2012 +0100

    MIFOSX-73: track loan officer reassignment history only when loan is approved.

[33mcommit f73ef79add1f2ffb2381a8b15b729bc3036431dc[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sat Nov 17 14:25:22 2012 +0000

    use hasAnyRole instead of hasRole expression when checking against multiple permissions.

[33mcommit 015400b08e8ff07fa71367c5a956c2cd83c4deb7[m
Author: Michal Dudzinski <mdudzinski@soldevelo.com>
Date:   Sat Nov 17 14:18:54 2012 +0100

    MIFOSX-73: new API for loan officer reassignment.

[33mcommit 35a331debd296a5733837c89be1d3120e73083d4[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri Nov 16 22:42:25 2012 +0000

    WIP permissions - add new api to get role permissions

[33mcommit bf847dc352a54d3f5e6b30aa56fd64fcd7eaa708[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri Nov 16 19:37:43 2012 +0000

    WIP permission - with permissions overhaul script

[33mcommit 16c395719c1ef62f7da498e82764483eb8f339b6[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri Nov 16 14:38:03 2012 +0000

    wip permissions

[33mcommit ce188814ecb26e63faf7bd772e918f28a3fe9652[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Nov 16 13:26:25 2012 +0000

    ensure resource idenity is stored with a CREATE command source when command is processed by platform. Allows ability to see all state changing commands accepted to date by platform for a given resource.

[33mcommit e61454792fd851ef0a0e00eaca15de66c2dbec8f[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Nov 16 10:13:06 2012 +0000

    refactor ApplicationConstants usage, refactor usage of code and code values for customer identifiers.

[33mcommit d7ea5d8169f103285faf1b11266da018c1495683[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Nov 15 21:58:19 2012 +0000

    MIFOSX-97: ability to support maker checker on create, update, delete of client.

[33mcommit e046d86e06d06d4da82a2173c675b6f6707a4509[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Nov 15 16:36:49 2012 +0000

    wip permissions

[33mcommit 81a549ae013f3c195db077f2712f249053e6e08d[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Nov 15 14:48:05 2012 +0000

    wip permissions

[33mcommit 74fa0716033708826d7803d5d395aca2797ef949[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Nov 15 13:56:20 2012 +0000

    remove unused imports.

[33mcommit 84db0f979450dd19453448b8287e844bdb8e817a[m
Merge: ac353ec 51f661d
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Nov 15 14:00:59 2012 +0000

    mifosx-45: removed usused imports.

[33mcommit ac353ec28eea5f135ba6a7b62081a82458c5066b[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Nov 15 13:26:36 2012 +0000

    wip permissions

[33mcommit 51f661de17a359be5ef300d66762055322a97dd7[m
Author: ismail <ismail.gharbi@endarabe.org.tn>
Date:   Thu Nov 15 13:47:06 2012 +0100

    changes for crud on code table

[33mcommit 0f2b5fd05d23cf892f787b9fd9d9dcea173b2bf0[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Nov 15 11:59:14 2012 +0000

    WIP permissions

[33mcommit dfb07106747abe132da9d3e25f3ab74bf5d8c405[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed Nov 14 21:37:27 2012 +0000

    wip permissions redo

[33mcommit 211f8b7d460713258b834554d18e19b7fd7243b2[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed Nov 14 21:24:13 2012 +0000

    WIP permissions redo - only use mifos user until finished - also some changes that were pulled but show as changed

[33mcommit 716b1fb11a804ff4d92e52eb409b6c138754d30f[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Nov 13 18:18:49 2012 +0000

    MIFOSX-97: WIP moving towards demo of maker checker of client create functionality.

[33mcommit 3f55301be27f530bc734074db58824fd422d8f4e[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Nov 12 17:30:50 2012 +0000

    MIFOSX-97: WIP on maker checker for clients area.

[33mcommit 3e16891cfb53eadcb5c1e86fbb23118922f4f4d9[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Nov 12 17:16:30 2012 +0000

    MIFOSX-97: some changes around loan officer reassingment capability for loans.

[33mcommit 4cba5ea7197bbc487c11ecd503b49019119500c4[m
Merge: cd2e71e 6e12e9e
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Nov 12 16:11:02 2012 +0000

    Merge remote-tracking branch 'mdudzinski/MIFOSX-73'

[33mcommit 6e12e9e62acfc9d41bf1930e1865c263f756eebc[m
Author: Michal Dudzinski <mdudzinski@soldevelo.com>
Date:   Mon Nov 12 16:53:46 2012 +0100

    support for MIFOSX-73: Loan Officer reassignment History.

[33mcommit cd2e71ed75df0882306d7ca446277e2ce5282ebb[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Nov 12 12:12:42 2012 +0000

    clean up REST API parameters usage around roles and permissions.

[33mcommit 2c92635ced4c4bc027548f2b1e8ec6f15874d7e0[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sat Nov 10 02:30:18 2012 +0000

    wip new permissions api

[33mcommit 0cedba8ae11498b716926f2a46a3b6715d2f20f9[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sat Nov 10 00:46:22 2012 +0000

    remove unused code around permissions from older prototype.

[33mcommit a0f6fd0a32f7365b177eb652b2357a6f46a5d116[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Nov 9 16:23:10 2012 +0000

    remove warnings, fix issue with creating loan with no charges even if loan product has default charges against it.

[33mcommit 0d33fcf118a83d446ee81363c04f226df7618411[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Fri Nov 9 19:23:36 2012 +0530

    Adding new image upload services for Data URLs (Webcam integration)

[33mcommit e0a7231f13010403146dc7cef3100b33e6cf3d71[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Nov 8 15:56:19 2012 +0000

    just give mifosx-126 scripts a running order

[33mcommit 152a70477d4bc35cd3d9bb02ba72ee80eb10f805[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Nov 8 14:23:54 2012 +0000

    MIFOSX-90: support modification of loan charge, fix issue with repayments paying off loan charges in wrong order.

[33mcommit f90802a2925c832a6534f350b26cbb49c8f50dba[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Nov 8 13:00:20 2012 +0000

    WIP mifosx-126 remove group-enum from m_permission and add grouping and order_in_grouping columns

[33mcommit 7af4b12d24bbfa86e08065a7cf7a51f45f32453d[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Nov 8 11:26:51 2012 +0000

    MIFOSX-90: disallow ability to remove loan charge if it is waived (or partially waived).

[33mcommit 027be9df4f100b78d8eab755061e4d7b37efb355[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Nov 8 10:26:08 2012 +0000

    Ability to waive loan charge in full (integrated into repayment schedule) even if loan charge is marked as paid or due for payment in past or future.

[33mcommit b7a951b01a27179fefca92bef2d712826eea95a1[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed Nov 7 21:06:03 2012 +0000

    wip mifosx-126 update reporting parameter query to correctly get reports of users with specific report permissions

[33mcommit d685c36410d8072d914cabdb504986bed61d36c3[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed Nov 7 13:53:51 2012 +0000

    WIP mifosx-126 - rename report and data table permissions and add upgrade script

[33mcommit 89b2a15e4c3fbb6bcb810d9260f5a8bf06b23cb2[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Nov 6 17:31:36 2012 +0000

    MIFOSX-124: get shell of accounting ready for development.

[33mcommit b185bc5f5b56d344fb8f3ddeee70b7f9af1f1009[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Nov 6 15:12:16 2012 +0000

    mifosx-90: validation of loan and loan charges state when adding and removing loan charges and when creating, modifying loan applications.

[33mcommit 81f7b614b44431f8befad03116f3efdc28c5b998[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Nov 6 14:10:24 2012 +0000

    show breakdown of monetary amount applied to portions for each transacton.

[33mcommit d8da3e49b69ebb72f114e560e8c04fee6ff433fe[m
Author: Michal Dudzinski <mdudzinski@soldevelo.com>
Date:   Tue Nov 6 12:12:58 2012 +0100

    Update .gitignore for intelij project files.

[33mcommit db3f0401d9fb84211ac98f2ac17a08df0ffa1735[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Nov 2 15:17:46 2012 +0000

    MIFOSX-90: cater for some validation and edge cases around loan charges and loans.

[33mcommit ee63559abde2df3333b93cbb3a1e9729a25bc3c2[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Nov 1 16:30:57 2012 +0000

    remove unused warnings from savings code.

[33mcommit 75b8a0801d2f52995ae80817f580b35fcb291d3d[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Nov 1 14:53:29 2012 +0000

    rename patch files for savings.

[33mcommit 3b4c1171e892f497a4899b841d5804a54000539c[m
Author: Madhukar <madhukar@hugotechnologies.com>
Date:   Wed Oct 10 16:07:23 2012 +0530

    Display deposit transactions in UI
    
    Adding saving product
    
    SavingProduct Definition
    
    basic saving account creation
    
    patch-2 reccuring deposit account
    
    Saving account creation final patch

[33mcommit 46c82b1f67c8cff8cf99fdbd52ba3704111d26ed[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Nov 1 13:15:05 2012 +0000

    MIFOSX-90: ability to add a defined loan charge (fee or penalty) to a loan and accomodate correctly into the loan schedule given the due date of the charge.

[33mcommit 46970adb2bb67a70b945624fb411d55ae513c417[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Oct 31 17:20:27 2012 +0000

    make write service method transactional along with minor style changes.

[33mcommit 390964f3610b5ae1e5b2dd215f3bd017e4973f80[m
Author: Michal Dudzinski <mdudzinski@soldevelo.com>
Date:   Wed Oct 31 15:45:49 2012 +0100

    MIFOSX-75: Bulk loan reassignment.

[33mcommit a3200a8bc79dc04063834893ad85d388fb0cba25[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Oct 31 11:08:54 2012 +0000

    MIFOSX-90: work in progress on charges (fees and penalties) for loans suitable for gk-maarg. Split out charges on repayment schedule into fee charges and penalty charges. work outstanding on integrating payment processing to auto pay off charges (or auto penalise).

[33mcommit a946e207da7bd75f84b7aae005717b54f337f67c[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon Oct 29 01:05:54 2012 +0000

    recommiting to give clean git status after pull

[33mcommit 2325878e4e0e1a4b0bdec083701c29e6a9801ab2[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Sun Oct 28 14:30:38 2012 +0000

    initials files for client image management.

[33mcommit 3d5c0d88c073c5d5809e9b69a275631927d4d07f[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sun Oct 28 11:29:57 2012 +0000

    Inital support for adding loan charge due on specified date for demo to gk.

[33mcommit f4a9d183d7213453075e67948a7527142b5bfa2e[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sat Oct 27 11:22:47 2012 +0100

    add proposed_loan_amount field to gk risk analysis ddl

[33mcommit ba42641d03083694b662b4e8fac88653b2ee3fdf[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sat Oct 27 11:10:50 2012 +0100

    removed unused code for now.

[33mcommit 82a23d7413e0d62522316af55178d75f5177fcbc[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Fri Oct 26 17:53:07 2012 +0530

    Add new entities for document management validation

[33mcommit eefd55f7af27c8cfa6c756e071e58966a5758f6e[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Oct 26 10:46:08 2012 +0100

    remove unused warnings

[33mcommit e5480dca8ddccb10596cd7bda94b64f0344d7570[m
Author: Natu Lauchande <nlauchande@afrisis.net>
Date:   Thu Oct 25 23:32:21 2012 +0100

    MIFOSX-47: natus commits around code values.

[33mcommit 675cebbbc7fc8e829dfe5df70868ca352253bfbf[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Oct 24 16:20:40 2012 +0100

    MIFOSX-67: update patch to drop recreate client identifier tables with UTF-8 charset and changes to field sizes. duplicate key scenario did not work correctly due to refetch of client data in same session which is closed due to excpetion. Little bit of removal of unused code from earlier solution.

[33mcommit 59858e2bd36cb2550e9ca78827e0ff6644bfd7c5[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Wed Oct 24 18:10:09 2012 +0530

    database patch for enforcing unique identifiers

[33mcommit 4f44cb28d11ba06c647bd44c04b4df5783bd03b3[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Wed Oct 24 17:24:26 2012 +0530

    addres review comments for Client Identifiers

[33mcommit 36e7117e46d7a9934f5a8b71f59c2ee45d226600[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue Oct 23 17:37:58 2012 +0100

    add sql gk risk analysis table

[33mcommit 1e3047c29c17c8c7977f265f24de5af829af2430[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Oct 23 13:38:38 2012 +0100

    MIFOSX-90: tidy up of implementation around add,delete,update loan charges.

[33mcommit ccdda4287bc195c6c44ae770badf58940fa27cf2[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Oct 23 11:52:45 2012 +0100

    MIFOSX-90: detect in active charges on product during loan creation.

[33mcommit 585f59170931a561c5ff97bc37777a23e72eeaa8[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Mon Oct 22 21:19:18 2012 +0530

    Minor fixes to detect empty file updates

[33mcommit bdd2774de969fad0621a0b2450c5bf3b3c700750[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Oct 22 15:38:27 2012 +0100

    MIFOSX-90: in prep for this work, fix some existing issues with charges, product charges and loan charges functionality.

[33mcommit 947f50fc31540651b442fe27e450cd69e43d039a[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Oct 22 13:40:28 2012 +0100

    remove unused import in groups service

[33mcommit 6dfdc458ae1c44c5981387e1cea5b348196d63d2[m
Author: Michal Dudzinski <mdudzinski@soldevelo.com>
Date:   Mon Oct 22 13:53:45 2012 +0200

    MIFOSX-108 minor issues fixes.

[33mcommit dd95e0ad4926a43eda4340c77a44cf608c4b38d9[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sun Oct 21 17:21:04 2012 +0200

    Update README.md
    
    update demo server.

[33mcommit e8bd6ccdbd2b4cc84f1efd5a0c345cb043b749c4[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sun Oct 21 12:02:23 2012 +0100

    update docs to handle demo and production servers as well as localhost dev.

[33mcommit 0b55ef9149566aca23d6fc2005286dafb351a672[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sat Oct 20 14:11:55 2012 +0100

    update tomcat plugin, handle change of disbursement date when modifying loan application for now.

[33mcommit 09165cbb043feadedf68ae54fa806b7d101e6b09[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Oct 18 17:48:26 2012 +0100

    allow undo disbursal if only disbursement transactions. refactor loanTransactionData object.

[33mcommit 3e40a0dac4cefc2bcb0a4b0d92e6e600c3481219[m
Author: Udai Gupta <mailtoud@gmail.com>
Date:   Wed Oct 17 21:25:52 2012 +0530

    fixing test failure

[33mcommit c32a14e5377e16598f90b7c181fe790ced6929e7[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Oct 16 00:47:30 2012 +0100

    upgrade sql files after demo server upgrade.

[33mcommit 3c78d8f64ef971bfbceef35ba57ccfc734131f56[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Oct 12 09:55:22 2012 +0100

    remove deprecate MoneyData class usage forcing tidy up of LoanTransaction usage.

[33mcommit b56ecc83870279d7253fdd33ab67ee9fe6c44251[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Oct 12 00:41:05 2012 +0100

    allow adjusted transactions to be zeroed, ensure inArrearsTolerance is nullable and does not need to be provided.

[33mcommit 3ce1ee7ded48730aff96ab99b792816bc511caf5[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Fri Oct 12 01:50:52 2012 +0530

    address review comments for client search and minor updates to document search api

[33mcommit ab9497c9858fc6a0af0adc06dd39948a3a6aaf7d[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Oct 11 17:44:59 2012 +0100

    mifosx-92: tidy up loan product in prep for tranche loans.

[33mcommit 90f78597d933773d87c642bb16b1e85c79c938e6[m
Author: Michal Dudzinski <mdudzinski@soldevelo.com>
Date:   Thu Oct 11 17:26:51 2012 +0200

    MIFOSX-108: Extend group search functionality.

[33mcommit b79f0495f3a9adb94403c27fd139dbb3c82fa747[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Oct 10 17:00:05 2012 +0100

    remove legacy code and fetaures from earlier prototype version that are no longer used and will be implemented differently in future.

[33mcommit c4578672fabd06dc8f9c2c100bfa2a5ad43361ed[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Oct 10 15:02:41 2012 +0100

    mifosx-61: provide ability to close loans as reschedule or obligations met if loan is within defined tolerance.

[33mcommit e60f1798aa26d88be0ce60bdec236bd11d09acd9[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Wed Oct 10 05:15:42 2012 +0530

    Adding insert script for new table and rolling back changes to cotext.xml

[33mcommit 2e83169b99dc795b7d07a44991c99ce80a3ce35b[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Wed Oct 10 05:11:17 2012 +0530

    Intermediate checking for file upload functionality

[33mcommit 9c75498ebe4c2f1496df079d8caf50d042ee8f3e[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Oct 9 19:40:12 2012 +0100

    remove unused import warnings and unused fields and parameter warnings for now.

[33mcommit 15539185582fe6a13818718c869f5d7893a4151a[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Oct 9 19:29:03 2012 +0100

    bump up patch number.

[33mcommit 79d036d5c9adf97bda7e362f6742f46f7402b036[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Oct 9 19:16:48 2012 +0100

    squash madhukars pull request into one commit.

[33mcommit b97f02ef8ec16de6aba8e9eb10982b728d33b1ec[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Oct 9 14:53:34 2012 +0100

    mifosx-61: support waive of interest along with loan write off capability. refactor loanRepayments as transactions on api.

[33mcommit a6a72541ebf38aea82ad2c3106464667a1b58e6d[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Oct 8 10:16:11 2012 +0100

    remove warnings about unused imports and make CodeValueData immutable.

[33mcommit 27263af35073eb712424b8858edb1dd02f80137f[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sun Oct 7 20:22:48 2012 +0100

    remove collation on sql, defult to utf-8 charset for table.

[33mcommit 721039f637449f10b2428fa9ae52418eb6a8b715[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Sat Oct 6 19:04:43 2012 +0530

    Adding services for client identifiers and some service methods for code and code value

[33mcommit 19f6485a4177c95d08f080b50e813277032beaec[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri Oct 5 14:52:00 2012 +1000

    data tables api docs and remove fiedls option from data tables api

[33mcommit 47ed3e716500039ba709da8f305b93d70fdba0cb[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri Oct 5 12:23:18 2012 +1000

    WIP api docs update data tables

[33mcommit 0a22ab3e419171ba4c23cc82ae8fe0e2e940b809[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Oct 4 16:01:34 2012 +0100

    creocore: change waive functionality to support waive of interest only.

[33mcommit cf865a28d901f0c2c3685d9210fdf08f4cf94578[m
Author: Michal Dudzinski <mdudzinski@soldevelo.com>
Date:   Thu Oct 4 14:58:10 2012 +0200

    MIFOSX-26 group template API returns only clients from specified office.

[33mcommit 82aebe4200891f92aba64b6c3ef869fbc36411c4[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Oct 4 18:26:12 2012 +1000

    apidocs update WIP

[33mcommit 976ea6be21271c56a7c1994def5ee7d969afc2bf[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Oct 3 17:00:04 2012 +0100

    Tidy up programming style bad smells around GETTING data from objects rather than putting behaviour on object that owns the data and using that behaviour.

[33mcommit fd20f72814a39e10b48ba7666b1d8027f2ef35cc[m
Author: Madhukar <madhukar@hugotechnologies.com>
Date:   Wed Apr 17 20:52:39 2013 +0530

    Add edit option for deposit account

[33mcommit f3c63671a0ed997b7b637c725cef9e0eaf05ae7e[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed Oct 3 15:46:50 2012 +1000

    recommiting change after pull that still showed as modified

[33mcommit b86966caf93f250cf567f54da998b35fba8a0a53[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Oct 2 12:24:36 2012 +0100

    remove usused warnings, make group data immutable.

[33mcommit 1bae87e447dcac4b93e07549b53717c0455c0fc6[m
Author: Michal Dudzinski <mdudzinski@soldevelo.com>
Date:   Tue Oct 2 12:31:20 2012 +0200

    MIFOSX-26 added group and client office validation.

[33mcommit 6f8e94b949410e5aba16928d946d6ee157d2af52[m
Author: Madhukar <madhukar@hugotechnologies.com>
Date:   Mon Oct 1 22:05:07 2012 +0530

    actual maturity amount fix

[33mcommit e68db3551e05b7ffab1bc20bd07b3d6a5e1cd43c[m
Author: Madhukar <madhukar@hugotechnologies.com>
Date:   Wed Sep 26 14:28:19 2012 +0530

    validation fix for renewing account
    
    validations for renew Account
    
    populate interest for preclosure deposit account
    
    get available interest for withdrawal for UI
    
    seperated renewing deposit functionality
    
    Renew for FD & validation fix

[33mcommit 98ca88993838120f776e12c16e0f7395c22d2279[m
Author: Michal Dudzinski <mdudzinski@soldevelo.com>
Date:   Mon Oct 1 15:00:48 2012 +0200

    MIFOSX 26 Solidarity group loan

[33mcommit 2855e56e0ffc2719bfa2adacf618edf59616de8d[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Oct 1 10:17:40 2012 +0100

    update files after server upgrade.

[33mcommit 4fc43ced64035b5d2f5463eb92b3bf822cf89f55[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Sep 28 16:53:00 2012 +0100

    mifosx-58: cater for modification of loan charges during loan modify process. ensure existing loan charges are used and updated rather than removed and new ones added each time.

[33mcommit 3860d2af56eda359177da81c49ccf957c6889195[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri Sep 28 16:14:55 2012 +1000

    data tables errors change

[33mcommit 344ed0ebab870661abd4f493c36d4ee7bd9e5af2[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri Sep 28 14:57:21 2012 +1000

    data tables - prevent multiple update attempt

[33mcommit 7d57edd8e8c4dfa3cba5e5d29ceb74aeaa5e3562[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri Sep 28 14:35:03 2012 +1000

    data table query param correction

[33mcommit 7d8ec61e0249c63de037d91a73d744e323393bce[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri Sep 28 12:02:47 2012 +1000

    wip data tables api docs

[33mcommit f737336dc31968ee675e8b8cd8a65298bc171cc5[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Sep 27 11:21:08 2012 +0100

    mifosx-58: handle charges at disbursement for loan creation.

[33mcommit d5f697962c519b13b5fc046d89989df15e030ab3[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Sep 27 16:58:41 2012 +1000

    wip update data tables and docs

[33mcommit 471bfe25668487e92ffb2281269c6c5ac0a3267b[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Sep 27 15:45:01 2012 +1000

    updating data tables api docs WIP

[33mcommit f53e662b6ddd1edb66e9dcaa1327059f61251095[m
Author: Michal Dudzinski <mdudzinski@soldevelo.com>
Date:   Wed Sep 26 15:24:45 2012 +0200

    MIFOSX-26 Added Office to Group.

[33mcommit 3617197f5fd5fffa6ef72f861200d1fa093476af[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Sep 26 10:18:41 2012 +0100

    remove warnings

[33mcommit c08a76dd94aab8ac5b5fc91af48f3304f926bcc1[m
Author: Madhukar <madhukar@hugotechnologies.com>
Date:   Wed Sep 26 12:57:02 2012 +0530

    add account count

[33mcommit 11e7f64cad38a5482ef1bff66b28843f391980e8[m
Author: Madhukar <madhukar@hugotechnologies.com>
Date:   Tue Sep 25 20:56:44 2012 +0530

    getAvailableInterest for withdrawal

[33mcommit 115aede1fa47b27b177daf7c5f9342cddb18fac0[m
Author: Madhukar <madhukar@hugotechnologies.com>
Date:   Tue Sep 25 10:32:07 2012 +0530

    For UI

[33mcommit 4830b85493b596c6abb6faa0b98c564f336943a9[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Sep 25 15:59:45 2012 +0100

    ensure payment of charges due at disbursement are captured on a transaction that is automaticatly entered when disbursement of loan occurs.

[33mcommit 7656bccb21aa1f440385521abe9fffb3876562a6[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue Sep 25 19:00:08 2012 +1000

    wip data tables registration resources

[33mcommit 59e19e005e4a9a32635bf2c8493eeb88e1c6c722[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Sep 25 09:47:38 2012 +0100

    mifosx-58: add interest repayment and total repayment details correctly to new loan schedule been returned.

[33mcommit 1b24a991244bc7d82ed664f0fd495830e16a46c3[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue Sep 25 11:40:46 2012 +1000

    wip data tables registration

[33mcommit afd22ea8265cf7fb5e763bf4e07031292f9c3546[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue Sep 25 02:45:38 2012 +1000

    wip data tables

[33mcommit 30cadb4d0d1867b58e296f12760b23c6368516e5[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon Sep 24 23:24:47 2012 +1000

    wip data tables

[33mcommit 2d2ff222fcd289c41d3b4dc83be2af7893ac1fe8[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sun Sep 23 23:10:24 2012 +1000

    wip data tables

[33mcommit 5dd2bd8859d534a7fa940965b7c1feb2664db3c2[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sun Sep 23 20:50:44 2012 +1000

    wip data tables

[33mcommit 4ef70d0d7863c0456ef5bc156c0236f56794f7ae[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sun Sep 23 17:38:56 2012 +1000

    wip data tables

[33mcommit 1a83d07c1c05aada23d60c74f8c6b7b8b7e5109a[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sun Sep 23 13:39:56 2012 +1000

    break out additional fields functionlity (deprecated) for easier removal later

[33mcommit 4c9450a257491ff9f5f8e1f984a596c53cafc21f[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Sep 21 20:26:48 2012 +0100

    remove warnings, format sections.

[33mcommit 91bcde66bf82c82f70500d655861f44a27b8ba63[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Sep 21 16:37:48 2012 +0100

    keep running tab on overdue amount for each period of repayment schedule, determine arrears from this based on loans arrears tolerance setting, ensure repaymentSchedule from retrieve loan has charges correctly included for disbursement period.

[33mcommit 9e25b070fb6e90eb7d0d9a6fb9673614f011fd1e[m
Author: Michal Dudzinski <mdudzinski@soldevelo.com>
Date:   Fri Sep 21 17:17:15 2012 +0200

    MIFOSX-60 API template method for loan charges

[33mcommit 1f8b325258628533f20bcbc66763e51e692e8b5c[m
Author: Madhukar <madhukar@hugotechnologies.com>
Date:   Fri Sep 21 20:52:04 2012 +0530

    interest payouts patch3

[33mcommit 4ac094fc787b88e5b4b609ea335179a8d927c3e1[m
Author: Madhukar <madhukar@hugotechnologies.com>
Date:   Fri Sep 21 10:25:30 2012 +0530

    interestpayouts patch2

[33mcommit 691e836c68973703fa82164abcba9aa70c1232a3[m
Author: Madhukar <madhukar@hugotechnologies.com>
Date:   Thu Sep 20 12:32:07 2012 +0530

    Interest Payouts

[33mcommit 3a89d10454c11b4439b3db63d327f173f661ebed[m
Author: Michal Dudzinski <mdudzinski@soldevelo.com>
Date:   Fri Sep 21 11:05:29 2012 +0200

    MIFOSX-60 added loan charges api methods.

[33mcommit 4f98144896f561b09caf0b43d6016017fa6a8959[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Sep 20 15:11:48 2012 +0100

    mifosx-58: wip - integrate charges at time of disbursement into loan create and modify flows. suppress non-relevant warnings for unused parameters in overiding classes.

[33mcommit ba8b24ac3bb2569142cc9bc9687040f87a6c3c7e[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Sep 19 17:25:31 2012 +0100

    update file name of loan schedule data class.

[33mcommit a6a74a401b78e7f5bfc79d7bb4efe71dbda6f878[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Sep 19 17:13:26 2012 +0100

    mifosx-58: final piece of aligning loan schedule area in prep for integration with charges. Loan schedule presented during loan creation and modify flows are same as one viewed when click loan link on client view.

[33mcommit 08fc9ff4d6bbfa2fcbe1f7fad38ce5ecaf4f63a1[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed Sep 19 11:17:35 2012 +1000

    wIP data tables

[33mcommit 360ae5a4447a010313d3637f37466c6880b35387[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Sep 18 14:45:37 2012 +0100

    mifosx-58: remove deprecated code and tests around it

[33mcommit 4084c194a0bf29bc19987fd5872a35b9b5c4b6eb[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Sep 18 14:31:17 2012 +0100

    mifosx-58: wip - refactoring api for loan schedule data - introduced new schedule in prep for supporting charges.

[33mcommit c6317acfae5451216ecb0697ee3c6d7e39b61f74[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Sep 17 19:31:30 2012 +0100

    mifosx-58: wip on data to reprsesent loan schedule.

[33mcommit 3e5b47f562a52adcbaea9ff8c26b9411cbc5c088[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Sep 17 11:08:53 2012 +0100

    wip loan charges and loan schedule

[33mcommit 3adfd9c073e682551b92bd4eb2b1f89c29e076e6[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sun Sep 16 19:58:13 2012 +0100

    WIP on support loan charges on loan schedule.

[33mcommit 6bd2d078a3a488cb5c6bcf6788c2ff8f83ed0d94[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sun Sep 16 19:57:59 2012 +0100

    WIP on support loan charges on loan schedule.

[33mcommit 56c8ce43cdafcb358b7cbcc83f37298b4dbac0a6[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Sep 18 15:11:13 2012 +0100

    removed unneeded supression of warning.

[33mcommit 055e6cde70d0fa40bebc48245952d7433fe5e586[m
Author: Michal Dudzinski <mdudzinski@soldevelo.com>
Date:   Tue Sep 18 15:31:40 2012 +0200

    MIFOSX-57 reorganization of loan charges design.

[33mcommit 725011c1ae278b3c315f6542ede0ee02913278c3[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue Sep 18 20:10:56 2012 +1000

    WIP data tables

[33mcommit ad6fb806ff0127e82306784f899589f3f1e23b54[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue Sep 18 13:31:30 2012 +1000

    WIP data tables

[33mcommit c17fa073ec6ad306ca5ba178eab9eb8f62aa58c7[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Sep 17 20:09:31 2012 +0100

    remove unneeded supressings.

[33mcommit 5934ae42adc74cd962e1123a066450603d0f36e7[m
Merge: 82ebfea 9df1c76
Author: Madhukar <madhukar@hugotechnologies.com>
Date:   Mon Sep 17 23:32:26 2012 +0530

    Merge branch 'MIFOSX-43-home' of https://github.com/madhukar547/mifosx into MIFOSX-43-home

[33mcommit 82ebfea45f7e65780e7208d6d3176602ec5e7843[m
Author: Madhukar <madhukar@hugotechnologies.com>
Date:   Mon Sep 17 19:38:57 2012 +0530

    Withdraw for deposit account

[33mcommit 329318327962437254badfdf4c88c901b1d2b5f7[m
Author: Madhukar <madhukar@hugotechnologies.com>
Date:   Mon Sep 17 10:58:26 2012 +0530

    Withdraw of deposit amount

[33mcommit c387c6155a4a3d4ae73fef05ecaaecfd0b0ef763[m
Author: Madhukar <madhukar@hugotechnologies.com>
Date:   Thu Sep 13 19:19:49 2012 +0530

    withdraw_renew_of deposit application on maturity

[33mcommit 899f9a3d465660a85b6cf0979674a54a0f28e6a9[m
Author: Madhukar <madhukar@hugotechnologies.com>
Date:   Thu Sep 13 10:04:46 2012 +0530

    adding maturity for deposit account

[33mcommit 0e66005147360b69365c34d6a278c752d36565f7[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon Sep 17 20:07:53 2012 +1000

    WIP data tables

[33mcommit b5e1ec051fb051a1df0b928985274dca0a45608b[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon Sep 17 17:59:56 2012 +1000

    WIP data tables

[33mcommit 9df1c76500917949ca3b863d4325b4b649694996[m
Merge: 4e35791 41c35af
Author: Madhukar <madhukar@hugotechnologies.com>
Date:   Sat Sep 15 12:14:58 2012 +0530

    applying conflict changes

[33mcommit 4e35791618e559bf7e3736c1bce7a32ae0c9d46d[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Sep 14 17:27:16 2012 +0100

    ensure charges data is returned in json for loan GETs.

[33mcommit 712ba4d69ed6a62432d7c51ee6ee4ef6ba3a0a82[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Sep 14 17:09:25 2012 +0100

    tidy up typical response parameters for loan GET requests.

[33mcommit 63e0f8e64b6ebd23ce1828f6986dbf1767d32866[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Sep 14 16:33:12 2012 +0100

    fix sql grammar error with trailing comma.

[33mcommit a75a8d5d228bfff1cc5c2a7cc899245d20551dba[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Sep 14 16:19:01 2012 +0100

    remove join to get currency data for repayment schedule periods.

[33mcommit e4f8eb6b12ebc1d065be516c57252b7f2266c449[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Sep 14 16:10:16 2012 +0100

    mifosx-58: restructure loan schedule to contain summary plus detail of schedule. As part of this work I removed the use of MoneyData in data objects and instead return currency and decimal for each monetary amount aligning with rest of application.

[33mcommit 47173938166349cb8ac57959c848fe764e9706f5[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Sep 13 22:00:45 2012 +0100

    some tidy up is all.

[33mcommit 2d8ae7ae9e0d25470854f754900f646f8b4a6aae[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Fri Sep 14 01:11:19 2012 +0530

    Return clients based on Office Hierarchy

[33mcommit c1c92fef3ceea4dd2aa526c5696c08da0252d375[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Sep 13 17:43:27 2012 +0100

    fix conflicts with recent commits.

[33mcommit b438747608f6cdb88e31d08e9cec42894b6ff89a[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Sep 13 15:02:37 2012 +0100

    update data parameter repaymentStrategyOptions

[33mcommit 384928670d889d45b48e04117d52bb3897810d5a[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Sep 13 00:46:41 2012 +0100

    mifosx-58: loan charges at time of disbursement

[33mcommit 16da04f08962353b812cd2f0f1195dc147092f74[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Sep 13 17:10:25 2012 +0100

    small bit of tidy up.

[33mcommit 45200343be6c8a40bd9feab9ecdd3866f195f7ca[m
Author: Madhukar <madhukar@hugotechnologies.com>
Date:   Thu Sep 13 19:19:49 2012 +0530

    withdraw_renew_of deposit application on maturity

[33mcommit bacb5bfe6443246415158670c10f76aab5df0cd5[m
Author: Madhukar <madhukar@hugotechnologies.com>
Date:   Thu Sep 13 10:04:46 2012 +0530

    adding maturity for deposit account

[33mcommit 41c35afa87e2f3beeea768c402eeaf78587251f8[m
Author: Madhukar <madhukar@hugotechnologies.com>
Date:   Thu Sep 13 19:19:49 2012 +0530

    withdraw_renew_of deposit application on maturity

[33mcommit 6a7647e1777dddbe0e4a095c768c2d144187d051[m
Author: Michal Dudzinski <mdudzinski@soldevelo.com>
Date:   Thu Sep 13 15:25:06 2012 +0200

    MIFOSX-57 Add possibility to modify loan application charges.

[33mcommit 1957dba4ace4184a068a08646ec72bdf508007cc[m
Author: Madhukar <madhukar@hugotechnologies.com>
Date:   Thu Sep 13 10:04:46 2012 +0530

    adding maturity for deposit account

[33mcommit 1fe9856eb2efd9e6d7752f401ee7fb413eecda9b[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Sep 13 01:05:56 2012 +0100

    some supressing of warnings and retaining loanAccountData as immutable (of sorts).

[33mcommit c90b98c12e6324a11789e59e4d82bcc905466354[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Thu Sep 13 01:07:03 2012 +0530

    Clearing out TODO's and fetching available loan officers for modify loan template

[33mcommit bd80d018213752ee5c0eb5a13b21bc5e3c61a692[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Sep 12 14:17:19 2012 +0100

    dont return loanRepayments if none exist.

[33mcommit 303da8dc61c3138c0e3a01a56f3c1b5b8fc89b92[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed Sep 12 21:51:14 2012 +1000

    redoing last commit

[33mcommit 4c752d7f23bb1a5d3061c234005d6289f9bfd494[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed Sep 12 21:16:54 2012 +1000

    change client query to link to m_office rather than match a  list of offices

[33mcommit 99b53995ea4e1a6a8d06135e0b869160a888cd96[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Sep 12 11:11:19 2012 +0100

    add validation for repayment strategy for loan application flows, add data update patch for heavensfamily data.

[33mcommit ae2279fce0a4f4ea0034ea1f0a803458630ba211[m
Author: Madhukar <madhukar@hugotechnologies.com>
Date:   Wed Sep 12 15:44:07 2012 +0530

    db field add for m_note

[33mcommit 3e7724a8c34a72de2190ef3290b46dddbc44f9d9[m
Author: Madhukar <madhukar@hugotechnologies.com>
Date:   Wed Sep 12 15:25:24 2012 +0530

    add notes for deposit events

[33mcommit 79d2e8bea868b2dbed6eee390b294b00feba2fac[m
Author: Madhukar <madhukar@hugotechnologies.com>
Date:   Tue Sep 11 19:15:25 2012 +0530

    isActive added for depositPermissiondata

[33mcommit da6c2ea818e4f59b76dce3e2542a4f697cb5b105[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Sep 12 02:25:16 2012 +0200

    Update README.md

[33mcommit 3684e7248609e102336fef9df3c7f3c52f63cfb4[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Sep 12 02:23:30 2012 +0200

    Update README.md
    
    Update roadmap of items being worked on

[33mcommit 32e4a2ce64c8792d49802ae47dfca825e83fd4e6[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Sep 11 21:12:16 2012 +0100

    add note about query for transactions data in deposit account cases.

[33mcommit 5154e87b23410ef279fc6089e03d72fc6f2ea54a[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Sep 11 21:08:26 2012 +0100

    Fix deposit account data object, make it immutable again.

[33mcommit 0bfabbe99a90ab5c6299c97abffa4af04a2fd8d7[m
Merge: 14ecbcb dc9c17a
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Sep 11 18:16:36 2012 +0100

    Merge remote-tracking branch 'madhukar/MIFOSX-43-home'

[33mcommit 14ecbcbb7ef0895dbf8fb7cd02e1693f4bbb46e2[m
Author: Michal Dudzinski <mdudzinski@soldevelo.com>
Date:   Tue Sep 11 14:20:32 2012 +0200

    MIFOSX-53 Extended loans template api.

[33mcommit 587cb496b47dacbbfd25f58d7a168a8a5c8cc156[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Sep 11 12:29:20 2012 +0100

    update for recent demo server upgrade.

[33mcommit dc9c17ad08de6522ee5c7c30579af19a5feb58e5[m
Author: Madhukar <madhukar@hugotechnologies.com>
Date:   Mon Sep 10 07:44:02 2012 +0530

    Add permissions for retrieveing

[33mcommit 940a665255c4e81cc956dba60ebf9b8d12509a5e[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sun Sep 9 15:25:24 2012 +0100

    update validation around staff

[33mcommit 1877562ea8e073a7b2f8cd5d59e84d89282f5064[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon Sep 10 00:10:29 2012 +1000

    commit to tidy up staff changes showing as modified

[33mcommit 06ac7a46e2e0a299c4472b1b5454ae6b6d733a34[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sun Sep 9 14:50:50 2012 +0100

    update patch in case table doesnt exist.

[33mcommit fa6370d31a528502cc5f01b4b2f086bf43d6cdc8[m
Author: Vishwas Babu AJ <vishwas@confluxtechnologies.com>
Date:   Sun Sep 9 17:43:09 2012 +0530

    Mifosx 54, associating relations to staff entity

[33mcommit 1585b33f4235d34b6076ebf6fc0f7330212ec2ed[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sun Sep 9 23:20:55 2012 +1000

    WIP update script for additional data table

[33mcommit 1f2bdedcd2512d0e709b04d896a10c5224b9ea28[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sun Sep 9 14:22:12 2012 +1000

    WIP data tables

[33mcommit d85696b7d92b467f69305526ffa966ec3c002747[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sun Sep 9 13:25:27 2012 +1000

    recommit after a push as git status says file is modified

[33mcommit 880c671d03f737837843b4825ebf1c4f12cc56ee[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sun Sep 9 13:23:37 2012 +1000

    WIP data tables

[33mcommit f08bd15b1f63b13cc02fbabfcbcd9be9dbdcac85[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Sep 7 16:14:13 2012 +0100

    add support for charges when create new or modifying loan aplication on the GET of data.

[33mcommit 61b49d0b4ca36db67ead027c3a3d991cbb793deb[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Sep 7 15:32:17 2012 +0100

    fix update loan product transaction strategy field and align new loan application with implementation of modify loan application work from mifosx-32.

[33mcommit c686d0d628b2c7141765d4eec144554d03c3c772[m
Author: Madhukar <madhukar@hugotechnologies.com>
Date:   Fri Sep 7 15:26:35 2012 +0530

    preClosureInterestRate For DepositAccount

[33mcommit e3223536a2664840420270aaf269aa1e06f3d32d[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri Sep 7 16:15:47 2012 +1000

    WIP data tables

[33mcommit 9649f0aba4febd1cba2f18eee7d607827fb2fb7e[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri Sep 7 13:31:57 2012 +1000

    commit item identified as uncommitted after git pull --rebase

[33mcommit 5bc6b5f664212244f06c8b61b5eca51476fff5c3[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Sep 6 23:12:45 2012 +0100

    support change of dates when modify loan application.

[33mcommit a3eed0fdc2ad5a83e551dd217d2d689937a20324[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Sep 6 16:22:37 2012 +0100

    MIFOSX-32: support ability to modify loan application (or loan contract).

[33mcommit 756729ae8e39798ed51b89fbe04c671a64de1b72[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Sep 6 16:50:21 2012 +0100

    fix for change of database column name.

[33mcommit 0306ee866ae4863f8fee8114f58ab5f4b4815c26[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Sep 6 16:39:14 2012 +0100

    rename matured_on column and rename status and types associated with deposit accounts.

[33mcommit e64efb0536dd42e5fd91f82c998d6c13bc82d509[m
Author: Madhukar <madhukar@hugotechnologies.com>
Date:   Thu Sep 6 19:30:53 2012 +0530

    add matured_on field for m_deposit_account

[33mcommit a06ed0d33bd996871bd60857aea79ac5152d42a9[m
Author: Madhukar <madhukar@hugotechnologies.com>
Date:   Thu Sep 6 19:19:39 2012 +0530

    fixed bugs from pull #32

[33mcommit 1e8d4b8c9d266369c43a292e253711656db57a5b[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Sep 6 22:02:33 2012 +1000

    WIP extra data

[33mcommit 492a8547a9ed166f92680739aa8e3277c2d963d6[m
Author: Madhukar <madhukar@hugotechnologies.com>
Date:   Thu Sep 6 07:46:11 2012 +0530

    Get the transactions for DepositAccounts

[33mcommit 04fc9357eba7a1e12559c524ababca8bcafcaf6d[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Sep 6 09:16:19 2012 +1000

    test commit

[33mcommit 29e36a0797eb4c52766bc37d874b71986b7ad42d[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Sep 5 23:19:08 2012 +0100

    MIFOSX-32: continue WIP on abilit to modify loan application.

[33mcommit 6b2ec54a31e79d2ec00a534c3bca0ef8e14356a3[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Sep 5 14:08:50 2012 +0100

    MIFOSX-32: start with modifing loan account data returned from get for use in the submit loan application and modify loan application flows.

[33mcommit 20234a8d6cceaa4a3041d48123421274a19044e7[m
Author: Michal Dudzinski <mdudzinski@soldevelo.com>
Date:   Wed Sep 5 19:19:47 2012 +0200

    MIFOSX-53 updated GET method. Fixed minor problems.

[33mcommit b331a29bf8814b0a2c133428fbdc438157141f7c[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Sep 5 15:46:53 2012 +0100

    fix up some old validation issues on loans.

[33mcommit 99d50b248a1206f0cec5ff17a69b81db7de2851f[m
Author: Michal Dudzinski <mdudzinski@soldevelo.com>
Date:   Wed Sep 5 15:39:12 2012 +0200

    MIFOSX-53 added post and template methods. WIP.

[33mcommit ca3d666578bae5075b8c4427a77c02ae639ccece[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Sep 5 14:41:36 2012 +0100

    remove redundant update case from deposit account for now.

[33mcommit 4a90cb091a2b72ecaf33ac29203f8fb0b98cf774[m
Author: Madhukar <madhukar@hugotechnologies.com>
Date:   Wed Sep 5 18:26:36 2012 +0530

    Transactions for DepositAccounts

[33mcommit 464f146a7ca2c22250d2006019371f580cb8462e[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Sep 4 22:02:46 2012 +0100

    remove annotations suppressing warnings that are no longer needed.

[33mcommit a22d5d10aa2ece06b792e86434bb5213447667fd[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Wed Sep 5 01:18:27 2012 +0530

    updating Client crud service & Db schema for MifosX-35

[33mcommit b9c8f63441666f20120aea228a182975b076a778[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Sep 4 15:05:39 2012 +0200

    Update README.md
    
    udpate public ami

[33mcommit 2529ea8a4f337fb08af6d3f665b86c870c2d42e2[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Sep 3 16:35:26 2012 +0100

    move patch into patches folder.

[33mcommit 48d88de4626dd926af01b9be618bef87ece5f8eb[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Sep 3 20:15:07 2012 +0530

    Staff CRUD Services: adding columns to m_staff table
    
    Adding audit columns to m_staff tables (as all other entities seem to be
    having audit columns and extending AbstractAuditableCustom)

[33mcommit 85df179c9b6985650632a587414423f8e5c267a1[m
Author: Vishwas Babu A J <vishwas@confluxtechnologies.com>
Date:   Mon Sep 3 20:01:41 2012 +0530

    Staff CRUD services : MIFOSX-52

[33mcommit d56c86c9af07f86312bf4575e84f50845edd4715[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon Sep 3 17:58:25 2012 +1000

    add some special permissions (wip)

[33mcommit a337b6574cd59b3537c8402126136d2cb2fe02ea[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon Sep 3 14:27:50 2012 +1000

    update install ddl with changes

[33mcommit 8d0806b723777f39484347a160465689174c10c3[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon Sep 3 10:43:36 2012 +1000

    update demo default backup

[33mcommit b97353975acad60a5e8dd109957275642ffa46a0[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sun Sep 2 22:19:41 2012 +1000

    update default backup and patches after demo server update

[33mcommit db7c96158721262fef3809d90d3f7796dc9bf14d[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sat Sep 1 16:16:33 2012 +1000

    check allowed code values on insert/update

[33mcommit a13d725ec42c21a30746963b611c529eefe76057[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sat Sep 1 13:57:15 2012 +1000

    move additional fields functionality to look at m_code/m_code_value

[33mcommit 9939987a65cd25f5d4f7b88e91ded703d5feacdb[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri Aug 31 18:51:22 2012 +1000

    new table ddl m_loan_charge

[33mcommit a925f5e12df2180984b9b609ba0564d7e012d151[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Aug 31 00:44:28 2012 +0100

    remove unused annotations.

[33mcommit f9d41aa500f16baca2434a612b88218c1d2445db[m
Author: Michal Dudzinski <mdudzinski@soldevelo.com>
Date:   Thu Aug 30 17:12:25 2012 +0200

    MIFOSX-42 Attach charges to loan products.

[33mcommit 57b38e463a01395296acd60c5dbb3f47384bed27[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Aug 30 10:31:26 2012 +0100

    MIFOSX-43: tidy up approval code so exception isnt thrown and no need for new data object to pass details around.

[33mcommit 508a46e537c55b91b2f971a01574d1d8cdeb7e51[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Aug 30 19:05:04 2012 +1000

    refactoring additional fields for data scope

[33mcommit f09d78100567ba483d472f11f8a98694f787dc1d[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Aug 30 10:00:42 2012 +0100

    some formatting changes and cleanup of status for deposit accounts.

[33mcommit 2132a5dccb3d17aff1279f452b8780972d31262c[m
Author: Madhukar <madhukar@hugotechnologies.com>
Date:   Mon Aug 27 13:08:43 2012 +0530

    Mifosx-43 fixed bugs from pull #21

[33mcommit 3cc06530da3415985ef900a6122f78c2acd7fe43[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Aug 30 18:14:30 2012 +1000

    refactoring additional fields for data scope

[33mcommit e0b3edff8a15f041ccd642535d6d7da8490e309f[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Aug 30 17:53:13 2012 +1000

    refactoring additional fields for data scope

[33mcommit b100fd16a17a2baba75041540c61809229455637[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Aug 24 23:00:46 2012 +0100

    return status with deposit account data.

[33mcommit 9da34579a0c6160fb588b486a9129a7779dff34b[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Aug 30 14:16:18 2012 +1000

    refactoring additional fields for data scope

[33mcommit f4dd25b5212ef424a623a3385c992ea1814de4e0[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Aug 30 13:44:11 2012 +1000

    refactoring additional fields for data scope

[33mcommit 2cda93f754cc0bdd27b291707a21db010611fcb4[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Aug 30 13:15:50 2012 +1000

    refactoring additional fields for data scope

[33mcommit 6ae1e98eb134295a0a942c946bb9c5d2942ce92e[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Aug 30 12:06:08 2012 +1000

    refactoring into GenericDataService

[33mcommit ccbf38f44fc11b9e55d0819a794e04a7d40a7431[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed Aug 29 18:30:59 2012 +1000

    used cached resultset for reporting in places (wip)

[33mcommit 7547f93c18956a20bea90563bb7f8f39f20b4f88[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed Aug 29 18:08:21 2012 +1000

    used cached resultset for reporting in places (wip)

[33mcommit 64f55de032697605417fe1d2fcbcc6dc8d20debd[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed Aug 29 17:29:17 2012 +1000

    used cached resultset for reporting in places (wip)

[33mcommit 4e78e7e401c9a335d4fc442f51979c5d4e5e339c[m
Merge: 1e558cd a47586d
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue Aug 28 19:13:58 2012 -0700

    Merge pull request #25 from mdudzinski/ChargeCalculationType_clean_up
    
    Changed ChargeCalculationMethod to ChargeCalculationType.

[33mcommit a47586deed2d90f09d4d0bcb7437787c42e37c9b[m
Author: Michal Dudzinski <mdudzinski@soldevelo.com>
Date:   Tue Aug 28 16:42:37 2012 +0200

    Changed ChargeCalculationMethod to ChargeCalculationType.

[33mcommit 1e558cd5c8fd6adf6b55990fb9f399911189f4c5[m
Merge: c292acf 7042ffc
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue Aug 28 06:50:16 2012 -0700

    Merge pull request #24 from mdudzinski/MIFOSX-41
    
    Charges API template fixes. Charge currency data extended.

[33mcommit c292acff29eb47a91a4e5ba4e290cf811ca5c892[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue Aug 28 23:47:46 2012 +1000

    used cached resultset for additional data in places (wip)

[33mcommit dca9063a07196eb5d52917f0f6f25d5b196d3001[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue Aug 28 23:16:02 2012 +1000

    used cached resultset for additional data in places (wip)

[33mcommit 7042ffcd0df880b136e5921355d19ff0347318ce[m
Author: Michal Dudzinski <mdudzinski@soldevelo.com>
Date:   Tue Aug 28 14:58:49 2012 +0200

    Charges API template fixes. Charge currency data extended.

[33mcommit 47fe49da8ca2b0b46111d71de88260bb748a3559[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue Aug 28 22:52:54 2012 +1000

    used cached resultset for additional data in places (wip)

[33mcommit 74fd29748756b4ce08fda297a727003c778389c1[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue Aug 28 18:50:29 2012 +1000

    used cached resultset for additional data in places (wip)

[33mcommit 0474b5df0c7d71ab17cfcb7db768ddb5366cf058[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue Aug 28 16:06:00 2012 +1000

    refactor noncore reporting and additional data services

[33mcommit 72d854b0cdf1ada8dc682006828151f19e563cf1[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue Aug 28 13:54:45 2012 +1000

    cosmetic office Txn changes to java

[33mcommit c4b5359ef9d64efda4d32b3c63981e33edd4db58[m
Merge: 8310f6c 61ccefb
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon Aug 27 18:24:26 2012 -0700

    Merge pull request #23 from mdudzinski/MIFOSX-41
    
    MIFOSX-41: API fix.

[33mcommit 8310f6cab6b41b0e61001beebca22d76da5e9688[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue Aug 28 01:38:09 2012 +1000

    data scoping for additional fields (WIP)

[33mcommit 61ccefb5d7921461661ac2d4b683e8b502985925[m
Author: Michal Dudzinski <mdudzinski@soldevelo.com>
Date:   Mon Aug 27 16:43:29 2012 +0200

    MIFOSX-41: API fix.

[33mcommit e80119ca71f7bd0fa7080c8dbc9e94d08a9e7f74[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon Aug 27 17:32:04 2012 +1000

    user can only list permitted addit. field data

[33mcommit fbbede83f38963683355fcf43bb4a4aac99ff000[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon Aug 27 17:14:00 2012 +1000

    permissions on additional fields (WIP)

[33mcommit b33b70cb384f3e634e504b1b8d09683fa922c725[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon Aug 27 16:10:10 2012 +1000

    had to put id in office transaction template typical field list to get id in Allowed Offices

[33mcommit cd351ab7233c229fc04502681ff909b3c9a2bd59[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon Aug 27 14:56:31 2012 +1000

    view office transactions api

[33mcommit 0720cee40d5a898c9a5c3dcac296e08ce6b5392d[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon Aug 27 12:37:31 2012 +1000

    only return permitted reports

[33mcommit 03f8a0fcc0f77361e3d8b622e7655fe62934b7bf[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon Aug 27 11:00:04 2012 +1000

    add facility for restricting data scope for reports

[33mcommit 58a6568efcf85cd948f940473e9d4384215c5f93[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sun Aug 26 16:57:58 2012 +1000

    add report permission checking

[33mcommit 345d9f37123697f70a1b79a5bfb33c0561a02cb1[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sat Aug 25 14:51:41 2012 +1000

    put links in new tab (mostly)

[33mcommit 22703500854c82cc50fb2c0642b2cf7de59f8e9d[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sat Aug 25 14:22:45 2012 +1000

    api docs simplify links

[33mcommit 2ee5dedf9c0781ba7fcd19b6c33b470b19c05369[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Aug 24 18:16:27 2012 +0100

    support bringing back pending approval and approved deposit accounts first of with client account summary.

[33mcommit 91890ff204a62683874bea4bd3ab05a2c917d910[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Aug 24 12:31:13 2012 +0100

    add support for soft delete of users and changing users passwords.

[33mcommit 5f602669216361dbc6ebbb625a436a3074bfb1f8[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri Aug 24 21:23:34 2012 +1000

    simplify api docs (WIP)

[33mcommit 39d5884bb95b33100a00873489c5e258ab6966e1[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Aug 23 13:20:40 2012 +0100

    whitespace changes.

[33mcommit dc11ca018ebe97da40719ff9a98354f73415e8df[m
Author: Madhukar <madhukar@hugotechnologies.com>
Date:   Thu Aug 23 16:57:39 2012 +0530

    Mifosx-43-requried_db_changes_patch

[33mcommit bbe525d8a8ac87f3c5dd272a07de7ef6bb7cf53b[m
Author: Madhukar <madhukar@hugotechnologies.com>
Date:   Thu Aug 23 16:51:12 2012 +0530

    Mifosx-43 DepositAccount Approval

[33mcommit 47a215ec4f31ea5546041c0fe12e186712f1481a[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Aug 23 12:01:36 2012 +0100

    MIFOSX-29: allow user to add or edit external id for client.

[33mcommit bf4490feb96cd30dbdaf8982a93d00f6a0d62fe7[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Aug 23 11:18:26 2012 +0100

    add error indicating no authorization for action.

[33mcommit 17ba21d3021a85b472a35877705109444a804834[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Aug 23 10:49:14 2012 +0100

    update default error message for fund data integrity constraints.

[33mcommit 3cd3b487f31400937fe2674e5301c3a66a451da6[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Aug 23 18:00:48 2012 +1000

    add pentaho report examples to api docs (were also put on demo server)

[33mcommit ff5967e377310f9b2cc88fb56da8330908bc896e[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Aug 23 17:21:54 2012 +1000

    update apidocs to default (for live examples) to host it is running on

[33mcommit 030887ad6f09c78768734efd4ed7b6f2901354a2[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Aug 22 18:02:52 2012 +0100

    tidy up enum usuage and api problems around charges during review and testing.

[33mcommit e4f95923eb5d8e4f23ad900dbc28996fab443004[m
Author: Michal Dudzinski <mdudzinski@soldevelo.com>
Date:   Wed Aug 22 17:13:16 2012 +0200

    MIFOSX-41: Update Infrastructure for fees, charges and penalties.

[33mcommit 12a16a07cf9102c16a4a9c9dc24bc5e5a912e111[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Aug 22 16:12:34 2012 +0100

    break out currency information on its own, using use values for loan product amount and inArrearsTolerance.

[33mcommit 9459db944bafcea097b381427adcc7f95ec4eff7[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Aug 22 13:39:54 2012 +0100

    support clientOrBusinessName data parameter for clients.

[33mcommit ed82fdae5207c833c959242256863bcce47290a4[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed Aug 22 22:38:14 2012 +1000

    make apidocs easier to change WIP

[33mcommit 1aafc0b09d09af7726f153e2c5cd9ce574485488[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Aug 22 13:15:51 2012 +0100

    ensure retrieval of head office returns no allowedParents.

[33mcommit 9a3ab88996a020e4450479a371b8f5645aab284b[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Aug 22 12:16:24 2012 +0100

    from MIFOSX-38, tidy up serialization of charges data.

[33mcommit 3b341b29afe28e927732f7be89c885f0347fc49b[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Aug 22 11:15:50 2012 +0100

    from MIFOSX-38, tidy up serialisation around loan accounts and transactions.

[33mcommit b4dc4b16bc38a4a50ca7e52d50cd4cac3edb41bd[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed Aug 22 15:01:46 2012 +1000

    retrieve currencies in name order

[33mcommit 4a11cb3290b4fb9851329838fb483bb262cd160b[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Aug 22 01:09:33 2012 +0100

    from MIFOSX-38, tidy up serialization around notes.

[33mcommit f2ca1746a41b2691822c4c96e29b5475a0ca674f[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Aug 22 00:37:18 2012 +0100

    from MIFOSX-38, tidy up serialization for client and group areas.

[33mcommit b691792de8cab59eb70828ae84bea852fc77b55b[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Aug 22 00:39:02 2012 +0200

    Update README.md
    
    add link to mifos.org wiki

[33mcommit 75ba8097618b50ca93885197ebd789aec4dc382c[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Aug 21 21:47:07 2012 +0100

    from MIFOSX-38, tidy up serialization of deposit accounts.

[33mcommit bd37a66247bb4c434c98c8502eb696cbbfae4b47[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Aug 21 21:20:07 2012 +0100

    from MIFOSX-38, tidy up serialization around financial products.

[33mcommit b44e53c3b7acd9bc1764684695cb2013bfede245[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Aug 21 20:45:44 2012 +0100

    from MIFOSX-38, tidy up serialisation around additional data and reports.2

[33mcommit c7c5198206173d73b6b73cbc3d9a9f7cdc4ea94c[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Aug 21 17:56:35 2012 +0100

    from MIFOSX-38 - tidy up serialisation around funds.

[33mcommit f48e4de3092d07c3fc4993815ae1a7c45dd52816[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Aug 21 16:17:41 2012 +0100

    from MIFOSX-38 - update serialization of offices and office transactions.

[33mcommit 00c4d5399a001dd7f86d485e2a7a51d58f9fe781[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Aug 21 14:37:15 2012 +0100

    MIFOSX-43: add database patch.

[33mcommit 95bdafa4bb3b080a528515a0beefae7669a6d4ba[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Aug 21 13:04:58 2012 +0100

    from MIFOX-38 - need to tidy up serialization of java objects into json across the board - this is for user admin related areas.

[33mcommit 432332c88e39f953bba28d2c9edbd6046a3fbefd[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Aug 21 10:42:04 2012 +0100

    FIXED: MIFOSX-38 - retrieving client notes does not return as an array when only one client note exists.

[33mcommit 28cee2ab076c5b7f6fa4df7a758011612cf6dc20[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue Aug 21 15:20:20 2012 +1000

    add basic m_staff table for Vishwas familiarisation

[33mcommit 592b5378dcb35ae60c131a67001c51d352457645[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon Aug 20 15:28:38 2012 +1000

    update after upgrading demo server

[33mcommit 4bbd878ca5a12ca45d27229510a0ff04a78357a8[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon Aug 20 13:03:08 2012 +1000

    add uptodate backup of demo server data

[33mcommit c1b52199a34ce1e460262499881b88e28408247c[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon Aug 20 12:25:58 2012 +1000

    changes to charges

[33mcommit ccfd8a09e09f35488b5c220352fd18ba1a0b97d4[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sat Aug 18 11:21:18 2012 +0100

    fix issues around loan.

[33mcommit e21e0e9a2f963cf9d434ce1438310e6245eae0ca[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sat Aug 18 15:17:30 2012 +1000

    update new ddl for deposit functionality and base data for default reports (due to core rename tables)

[33mcommit 1e24527487e75d473e991c114f3cb266fb7d8d8a[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sat Aug 18 13:03:26 2012 +1000

    update patches, base data (reports to come later)

[33mcommit 9108cebf674f1ac1d08369f329c72ccf91de0490[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sat Aug 18 02:36:00 2012 +0100

    update java code due to table renames.

[33mcommit bc1b9d89c93884ae922863e5b6ee03687752c862[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sat Aug 18 11:19:53 2012 +1000

    update new schema and demo data ddl with rename to m prefix

[33mcommit 13b09ad4a11c8f0ff88da0dbd1dbc14663e0b2aa[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sat Aug 18 11:01:41 2012 +1000

    rename core tables with prefix m (for mifos)

[33mcommit d7dba9a5159d391cc4b1a8d01a6742d86baaf69d[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Aug 17 16:39:28 2012 +0100

    fixed mifosx-38

[33mcommit 068620c8104c93042090c5183beca75708af00e4[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Aug 17 14:04:38 2012 +0100

    test amortisation for declining balance and support irregular first repayment on flat loans.

[33mcommit af5e2c0e842efe57b500b91ac14d2b6841f34e81[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Aug 17 12:21:02 2012 +0100

    work on fixing interest calculation for declining balance loans.

[33mcommit d855129fea705073b91499171bb626b06456d027[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Aug 17 15:02:36 2012 +0100

    keep DepositAccountData as an immutable object. opt against used selectedProduct approach like in loans. drop currencyOptions as the product should define the currency. add productOptions (options for consistency) when retrieving template.

[33mcommit 88e4c4ec00f2a2affb022dd9beaf1e0a01f62203[m
Author: madhukar <madhukar@hugotechnologies.com>
Date:   Fri Aug 17 18:12:56 2012 +0530

    Changes made for populating client name, available products

[33mcommit 440205fdfad8ff056fe915848f17ecd42ba49bb9[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Aug 17 12:42:36 2012 +0100

    ensure updates allow for changing any one attribute e.g. i want to change name only.

[33mcommit 9ad9070a863386face617338e8bbf11b910ec04b[m
Author: Michal Dudzinski <mdudzinski@soldevelo.com>
Date:   Fri Aug 17 12:57:53 2012 +0200

    MIFOSX-33 Add Infrastructure for fees, charges and penalties.

[33mcommit ac6589e9aa86dead8b96a6f8eaaf0643accf2eec[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Aug 16 19:49:13 2012 +0100

    rename enum to LoanStatus

[33mcommit abb4ec17dc958e27e78053541070ca2a79f21604[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Aug 16 19:40:03 2012 +0100

    drop loan status table in favour of enum for now.

[33mcommit 9fe9a091c9df81ebb2d1db8d7dbcad02ef25ea99[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Aug 16 20:21:11 2012 +1000

    allow field list for datatables (WIP)

[33mcommit 99fbab83b76b87a3ab83a701de46d7b045d7027f[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Aug 16 17:10:35 2012 +1000

    just adding simple search and ordering to datatable api (WIP)

[33mcommit 32668987d0d2616334617b5728d2d88ed0932bee[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Aug 16 15:23:27 2012 +1000

    code and enum tables WIP

[33mcommit 25d1ff3daccd9348828948ceecd479a1e8d7412d[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Aug 16 13:14:54 2012 +1000

    update ddl to remove charge relationship on loan product

[33mcommit 2c6782ca25f14f23a083985684bfb101fb89111e[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Aug 16 13:05:09 2012 +1000

    add delete flag to o_charge

[33mcommit 12b6795188acff8bde2030aacbc1beb44dd118f9[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Aug 16 12:52:15 2012 +1000

    add basic charge table

[33mcommit 392be3745cd2973ca14618036659ff57c0f2e86e[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Aug 15 14:17:00 2012 +0100

    calculate future value of fixed term deposit on opening or creation of deposit account.2

[33mcommit 10c6f9c50977c52f9430790f8a97840266059cda[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Aug 15 12:11:02 2012 +0100

    restructure deposit related files under saving packages.

[33mcommit 349e3f4a4e5c88b77c88e1b1fd4143203eaad7d4[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Aug 15 11:35:25 2012 +0100

    ensure deposit account inherits details of product but allows for user to overriden core details where possible.

[33mcommit 9420d4fd14fd7cd4945f82154ccb804df4d6b3e0[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Aug 15 02:41:12 2012 +0100

    fix up deposit account fields to be consistent with product.

[33mcommit f30e8645a37470cbf1d7871bc151c67392d4d267[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Aug 14 23:54:33 2012 +0100

    add interest compounding frequency to deposit product along with externalId and unique constraints for name and externalId on product.

[33mcommit 6642aacff5e65bfa05f6e34c14a6c4ec578f2b82[m
Author: Michal Dudzinski <mdudzinski@soldevelo.com>
Date:   Tue Aug 14 16:47:05 2012 +0200

    MIFOSX-25: minor api changes.

[33mcommit ee9656c5a6d6f019d993c9dd7b4800b62d59367d[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Aug 14 15:18:34 2012 +0100

    fix error

[33mcommit c2e563bf6f5b012695c5286e19b3ac4311dea23e[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Aug 14 15:12:43 2012 +0100

    make parameter names consistent between product and account for deposits.

[33mcommit 2a1bb14c9fc610258dbca07d44b48970e75a46ac[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Aug 14 13:43:08 2012 +0100

    tidy up deposit product usuage with deposit account.

[33mcommit 53030285de7f72d4afcae3f2e891706307f170b7[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue Aug 14 18:24:33 2012 +1000

    add interest_waived_derived to portfolio_loan_transaction

[33mcommit 42a52cc70aba1f1d22bd6556a88a53c4b3f20655[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Aug 14 00:40:53 2012 +0100

    take care of most of open new deposit account use case - interest calculation is remaining.

[33mcommit 33afb23f122c153cb90d8f08578661feb43096c3[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Aug 13 20:35:20 2012 +0100

    add patch for updating south african rand currency symbol.

[33mcommit 52aa27efea666fe2f323c975d2258de0d1cdd305[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Aug 13 16:43:56 2012 +0100

    add patch for deposit account table.

[33mcommit fc7380f22d868ca21907612bd261d4672cff4fb6[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Aug 13 17:44:05 2012 +0100

    avoid use of Boolean object type. allow ability to update renewal and preClosure settings of product definition.

[33mcommit aa762b8a24bc371049a99df2506e2c3cfec0ac3f[m
Author: madhukar <madhukar@hugotechnologies.com>
Date:   Mon Aug 13 20:26:27 2012 +0530

    Validation Fixes for deposit product

[33mcommit 3d136095a44ba62f30ce0917a2f0fcb98e92c1da[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue Aug 14 00:38:16 2012 +1000

    wip data table api

[33mcommit 93e088a4f8e5739fae6ebdf7e821cebefceae561[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon Aug 13 11:32:31 2012 +1000

    Update README.md

[33mcommit 04eec1f5eabb47e13510b7e8e1af3008260446a6[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon Aug 13 02:12:19 2012 +1000

    files updated after upgrading demo server

[33mcommit 376dd885d8302fd46afa2e306e29b670a301eecb[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sat Aug 11 01:33:19 2012 +0100

    fix sql query for deposit accounts.

[33mcommit 9e264216a6c0dc3688b72ff2efb4f296ea0252f8[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Aug 10 15:32:41 2012 +0100

    allow a user whos not a user administrator update their own password details.2

[33mcommit c60cebcf8ac1fbf8d5acfdfd1c489b00d8cce7b5[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Aug 10 14:24:56 2012 +0100

    update ddl for group schema changes.

[33mcommit 87a29c859690b814432f59fee0667e708e390aa9[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Aug 10 13:50:27 2012 +0100

    return product name with deposit account details.

[33mcommit f277cce8949d555b7fcdb099236c43b1cd2b0aad[m
Author: Michal Dudzinski <mdudzinski@soldevelo.com>
Date:   Fri Aug 10 14:32:51 2012 +0200

    MIFOSX-24: Fixed remaining issues.

[33mcommit 9d916c1630213998b4d06b78383bc32eeb05cb03[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Aug 10 12:55:33 2012 +0100

    update DDL and patch to correct data structure.

[33mcommit 43a40ab14ce6bc8e912e4ea75a54d7dd3449f5c1[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Aug 10 12:43:45 2012 +0100

    integrate deposit account with deposit product concept.

[33mcommit e67df9e3bf7317174b1b1bdcebdf3bf65f1e1c8d[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Aug 10 11:54:45 2012 +0100

    tidy up use of validation on deposit product.

[33mcommit ba2a992e89f8cb3cf38511f5e3359d45e9608ba9[m
Merge: c23b0de 8f70265
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Aug 10 03:08:46 2012 -0700

    Merge pull request #14 from madhukar547/MIFOSX-23-FD
    
    Retrieveing Deposit Product,Basic Validation

[33mcommit c23b0de89e767cc5789e2a6d02d0b33d17a93598[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri Aug 10 20:05:32 2012 +1000

    extra fields for reports

[33mcommit 8f70265337e5f17b97e71d81c868d3e55ac4f9e7[m
Author: madhukar <madhukar@hugotechnologies.com>
Date:   Fri Aug 10 14:40:03 2012 +0530

    Retrieveing Deposit Product,Basic Validation

[33mcommit 32ba284c6d13c25230f21f22eb4872dfd7fca98f[m
Author: Michal Dudzinski <mdudzinski@soldevelo.com>
Date:   Fri Aug 10 10:43:00 2012 +0200

    FIXED MIFOSX-24: General Issues with Groups so far

[33mcommit b689b452c3efe717cc398e08ae85f27bf8cad3d5[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Aug 10 09:03:05 2012 +0100

    fix sql error in GET for depositproducts and reset password again.

[33mcommit c158e6b3182b3de5539f1397dbdb40ff8f499b9b[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Aug 10 08:24:16 2012 +0100

    continue fixed term deposit account basics

[33mcommit ddf33c475a267c2077ef555046f8a5e4716acace[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Aug 8 14:42:27 2012 +0100

    start of work for deposit accounts.

[33mcommit 770e8052421e1193ea9e67b9ae81eaea436115a4[m
Merge: e79df79 dfe6670
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Aug 10 00:23:24 2012 -0700

    Merge pull request #12 from madhukar547/MIFOSX-23-FD
    
    Deposit Product Creation, Retrieving

[33mcommit dfe6670972974b9bd69d9ad8b406fee364f8035c[m
Merge: f40a598 e79df79
Author: madhukar <madhukar@hugotechnologies.com>
Date:   Fri Aug 10 10:33:54 2012 +0530

    Merge remote branch 'upstream/master'

[33mcommit e79df797a32b9ef26cd03360c2b509b053799134[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri Aug 10 10:58:02 2012 +1000

    pentaho reporting - deal with dates, hardcode server location :)

[33mcommit f40a598addfea345f6c7687b64c5936a559946ca[m
Author: madhukar <madhukar@hugotechnologies.com>
Date:   Thu Aug 9 20:51:35 2012 +0530

    Deposit Product Creation, Retrieving

[33mcommit b18d3881b7ec518c2765faa04d3ef253f2012199[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Aug 8 14:47:04 2012 +0100

    reset password back.

[33mcommit c306781a4606ee12416850a338c6151b5f43cb1b[m
Author: madhukar <madhukar@hugotechnologies.com>
Date:   Wed Aug 8 18:13:07 2012 +0530

    MinimumBalance,MaximunBalance fields added for saving product

[33mcommit eff8838f5b0bc0cbd1649d9ded64cdfd351e951a[m
Author: Michal Dudzinski <mdudzinski@soldevelo.com>
Date:   Wed Aug 8 11:02:49 2012 +0200

    FIXED MIFOSX-19: Group formation

[33mcommit c6dd2f55f8fb4fe56509cf692787f5a348308e65[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed Aug 8 18:43:33 2012 +1000

    amend patch to drop correct column name can_pre_close

[33mcommit 96012a97beb083342c5387aec81b0e5889c29b55[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Aug 8 09:07:05 2012 +0100

    ensure json deserialization works for array based items when create a new application user.

[33mcommit d2b522bf6f11356d4343904bf1f99ed7872a10bd[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed Aug 8 17:20:16 2012 +1000

    new ddl for deposit product (slight update)

[33mcommit 1eea30a02711a00c31cdf19c7107f7c3e545b3a9[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed Aug 8 17:05:09 2012 +1000

    new ddl for deposit product

[33mcommit e1c1cb4c4129779db31334b35852d20dadbb0ddc[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Aug 7 23:43:05 2012 +0100

    fix update of client which was blanking out name details in certain scenario.

[33mcommit 762e7b14d9d5729f6640223be9c6cb1c9250e386[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Aug 7 15:56:58 2012 +0100

    reomve patches files. add demo backup that is up to date with patches. update ddl schema for loan terms changes.

[33mcommit 525902cbb2ce7144c5422bd685ab019002b7e995[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Aug 7 14:37:41 2012 +0100

    finish off support for using loan term details of a loan to determine interest due rather than inferring it from the number of repayments and repaid every information. This allows better support for loan schedules where the first repayment is irregular (e.g. not same timescale as repaid every)

[33mcommit 5638a0880c0a33dc5397878259b4ec256aae672d[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Aug 7 10:44:25 2012 +0100

    calculate total interest based on etered loan term.

[33mcommit 41e57ba4bd36d35579fdb1dab0fbf617c264d1fd[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sat Aug 4 01:14:13 2012 +0100

    continue loan term changes..

[33mcommit 20a8d08475c07838ed2e821333e2d4edb618e42a[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Aug 3 14:10:32 2012 +0100

    loan term changes..

[33mcommit c65c185b61b7074b0c4f6fb3e52b85913d7f04c2[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue Aug 7 23:27:56 2012 +1000

    add fixed term fields to savings product

[33mcommit e6d4f1ee662daa205b79df38bec35468e168957c[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Aug 7 11:02:12 2012 +0100

    dont show soft-deleted savings products.

[33mcommit 78d2c18279069f1e049566accb3d05a90494c864[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Aug 6 13:20:33 2012 +0100

    remove unused warnings.

[33mcommit cc30ad79e9bf7718c2c7a4f7db389f2f1d8781d5[m
Author: Michal Dudzinski <mdudzinski@soldevelo.com>
Date:   Mon Aug 6 13:32:23 2012 +0200

    FIXED MIFOSX-16: Support soft delete of group.

[33mcommit f1c52cc757e98d3b41a85f52467d1313913e4fcb[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Aug 6 12:21:56 2012 +0100

    reset password back to mysql as is in docs.

[33mcommit 1134b9ef32ae4359d30f4754ab8fd24689e2acab[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon Aug 6 18:06:13 2012 +1000

    add ddl change for deleted savings product to patches and base ddl

[33mcommit 8bec6c8fe78cb09714314f50e7c3af518e8fa1b5[m
Merge: 9dc9af9 55095f4
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon Aug 6 00:28:00 2012 -0700

    Merge pull request #7 from madhukar547/master
    
    Deleting a Saving Product

[33mcommit 55095f47ba8c70d61fd5ea20f8fabe6f0e9bf4f0[m
Author: madhukar <madhukar@hugotechnologies.com>
Date:   Mon Aug 6 12:21:20 2012 +0530

    delete a saving product

[33mcommit 9dc9af9633080887c31298c04fbcfa0a234f358f[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sat Aug 4 01:21:32 2012 +0100

    ensure currencyOptions is returned for template for savings products.

[33mcommit bf6c1b5606549fa51c8efe93c8b2b4029c02997c[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Aug 3 15:53:33 2012 +0100

    dont show curreny options by default in json, instead only show when using template resource or when template flag is provided.

[33mcommit 0b41f626b2c4861789a1a8854f5bcd80d9df18f6[m
Author: madhukar <madhukar@hugotechnologies.com>
Date:   Fri Aug 3 20:00:16 2012 +0530

    Currency names added in Dropdown box

[33mcommit 237e272119698fd9aeb879ce6b26720e6e4dc495[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Aug 3 10:58:16 2012 +0100

    fix issues where get null pointer if no locale passed along with digitsAfterDecimal or any integer value in general. existed pre madhukars commit.

[33mcommit f088d257d331c87caaa81b731c67c7058decdd1e[m
Author: madhukar <madhukar@hugotechnologies.com>
Date:   Fri Aug 3 13:00:05 2012 +0530

    Currencycode,currencyDigits,interestRate Added for a Saving Prdouct

[33mcommit df13eb60dafe9a8066168002c8baa7cbc1436ea3[m
Author: Michal Dudzinski <mdudzinski@soldevelo.com>
Date:   Fri Aug 3 10:50:51 2012 +0200

    FIXED MIFOSX-13: Create basic API for @GET and @PUT of groups.

[33mcommit 2bda0e8764517fc13d9ac6893d5e996f15b83221[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Aug 3 00:13:27 2012 +0100

    fix loan accounts retrieval, json serialisation was skipping some fields preventing client app from showing what loans existed.

[33mcommit a1b9a9c8f9c7043f4b6e22df604c9e3192827285[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri Aug 3 02:10:09 2012 +1000

    report data added to base data

[33mcommit 47be8388561bf9df0bf4fcbbf33f230fa23a39b4[m
Author: Michal Dudzinski <mdudzinski@soldevelo.com>
Date:   Thu Aug 2 12:12:56 2012 +0200

    FIXED MIFOSX-6: Implement basic api for Creating a Group

[33mcommit bc747a91ed8d17e8c5543e7040f0063a53975c1e[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Aug 1 23:11:53 2012 +0100

    add mifos style repayment processor that pays off interest, then principal from most overdue installments first.

[33mcommit b4e9b5a5661a60fb93cc4c6424e1d4a7aa1de872[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed Aug 1 14:40:10 2012 +1000

    add currency and interest rate to savings

[33mcommit 37a45649b11dd5e5de8423d3c21631bbade1d9a8[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Jul 31 19:27:42 2012 +0100

    tidy up client update

[33mcommit ef02c8be1fd5f01fe70f975605e2c5c6115ab3cd[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed Aug 1 01:27:47 2012 +1000

    and sql file name corrected

[33mcommit b321da9cb6aea79e87357a59355a058344226543[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed Aug 1 01:23:58 2012 +1000

    forgot externalId key on portfolio_group

[33mcommit d9c7d7c548043f394ad9bb8a9c9d58202206f338[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Jul 31 16:24:10 2012 +0100

    tidy up around user api.

[33mcommit b87751913752782b14f9a38ab3909f1d5567ae61[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Jul 31 15:54:44 2012 +0100

    fix up update user functionality

[33mcommit 78aa9eeadde8b39f8639ae8b21fc56ffe3345c11[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed Aug 1 01:12:00 2012 +1000

    ddl for portfolio_group table

[33mcommit d001cf1b69df0ccfeee802121f19dae70006d981[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Jul 31 15:11:49 2012 +0100

    supported parameteres for permissions were wrong which meant the partial response functionality of api through fields=id,name was broken.

[33mcommit 35b76a7d90ee0dce638c4148eb78e1f7235274b7[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue Jul 31 20:19:35 2012 +1000

    remove db patches

[33mcommit 302f2d05712ca1a625c4258438445c17292d2a1f[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Jul 30 19:24:42 2012 +0200

    Update README.md

[33mcommit f6500b76536c83c00ac8ed6e862f03d314cbda34[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Jul 30 18:20:17 2012 +0100

    move default demo backup to documented name.

[33mcommit a500fe553f16e0d0b6528133e5bb11fbd2f04f54[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Jul 30 18:12:04 2012 +0100

    tidy up sql patchs and demo dumps to latest version of platform.

[33mcommit 68ea71a68f98f7337e0d36828af54b442d11eda6[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Jul 30 16:59:55 2012 +0100

    support repayment strategy approach for deciding how loan transactions should be processed against the loan schedule.

[33mcommit 4dc10ae648950bd7ce4f275acb87cb987a25c09f[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Jul 30 13:31:35 2012 +0100

    minor changes after madhukar pull-request, nothing of note.

[33mcommit 62823f65480e5e176ad06c5d42c413477e8c29c5[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Jul 30 13:10:58 2012 +0100

    not able to ignore file that is already tracked and want it to be part of source control so going to live with having to change mysql user password in here for now.

[33mcommit b9963983f6379b8b2a256c14b7237c500655e4ef[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Jul 30 13:05:53 2012 +0100

    add context.xml in test resources to git ignore, meaning if people change this file it wont appear in commit log or be checked in.

[33mcommit f798feab8320af37e38da401782cc211eb7e7402[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Jul 30 13:03:37 2012 +0100

    add context.xml in test resources to git ignore, meaning if people change this file it wont appear in commit log or be checked in.

[33mcommit 665507e75099ec228e3321864eb95a7226fa5100[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Jul 30 13:00:31 2012 +0100

    update patch.

[33mcommit 97bf8e2f93a38871acfbae4ac47ddb2701d246fc[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Jul 30 12:44:06 2012 +0100

    add relationship between loan product and the loan transaction processing strategy. its optional by default for loan product and loans. If no value selected it will default to the the 'Mifos style' repayment strategy which is pay off interest, then principal in all cases.

[33mcommit 864df5eeef0ef8f39e974830eb2460b75c2d3fbb[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Jul 30 12:56:14 2012 +0100

    undo change around context.xml for mysql user, password for now.

[33mcommit 08e9478103329cd8292ac90e2cc15457fd5862ad[m
Merge: 3f3f898 73ca0d7
Author: madhukar <madhukar@hugotechnologies.com>
Date:   Mon Jul 30 16:37:03 2012 +0530

    Merge branch 'master', remote branch 'origin'

[33mcommit 3f3f898be5acd7be0682693590588a48b8381c8c[m
Author: madhukar <madhukar@hugotechnologies.com>
Date:   Mon Jul 30 16:16:03 2012 +0530

    Basic Saving Product Creation,No behaviour added

[33mcommit 755c37deca317ac089bff4736f7fcf2016bf6100[m
Author: madhukar <madhukar@hugotechnologies.com>
Date:   Fri Jul 27 11:46:41 2012 +0530

    nothing in it

[33mcommit 73ca0d71b1139ebfbd52764e900398a5fe903195[m
Merge: 36464e9 c44aed6
Author: madhukar <madhukar@hugotechnologies.com>
Date:   Mon Jul 30 16:20:25 2012 +0530

    Merge branch 'master', remote branch 'origin'

[33mcommit 36464e94f2fe8675853e359d67f05f50dc30b1a4[m
Author: madhukar <madhukar@hugotechnologies.com>
Date:   Mon Jul 30 16:16:03 2012 +0530

    Basic Saving Product Creation,No behaviour added

[33mcommit 55083d3bb91d9682ebeba3a768a3d2bb1b4de022[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sun Jul 29 23:08:25 2012 +0100

    add start of Adhikar example for processing loan transactions.

[33mcommit dfd504cd732449c7a36d8dc22b5978b4d9cc89dd[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sat Jul 28 20:56:46 2012 +0100

    create two types of repayment strategy from abstract template.

[33mcommit 58b3a2cc3ec2401c383e5cbab43c751868550c50[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sat Jul 28 01:47:08 2012 +0100

    fix some issues with late payments and checks for isFullyRepaid or overpaid.

[33mcommit 89230969ead195a804cc6b4fec50d290e434f384[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Jul 27 23:59:39 2012 +0100

    support flexible repayment strategies for loans.

[33mcommit cc9b6eb91bd7785d395c33a9c0189367791a2c27[m
Author: madhukar <madhukar@hugotechnologies.com>
Date:   Fri Jul 27 11:46:41 2012 +0530

    nothing in it

[33mcommit c44aed674e287394070b87f62a5c8aabdf9a71c6[m
Merge: 52c199e 6c39bea
Author: madhukar <madhukar@hugotechnologies.com>
Date:   Fri Jul 27 14:59:34 2012 +0530

    Merge branch 'master', remote branch 'origin'

[33mcommit 52c199eae6b22b6934d07fcf1aa64049f1fbb9fe[m
Author: madhukar <madhukar@hugotechnologies.com>
Date:   Fri Jul 27 11:46:41 2012 +0530

    nothing in it

[33mcommit 6c39bea008b96d8356725eb039f1385068b86cf2[m
Author: madhukar <madhukar@hugotechnologies.com>
Date:   Fri Jul 27 11:46:41 2012 +0530

    nothing in it

[33mcommit ef12c864ab78a5ad4cd853b8f9baf3262bc51a47[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri Jul 27 13:41:48 2012 +1000

    update description field for office transactions

[33mcommit aba29ae88cf8b476f11858c7e0e9713561d5f42b[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Jul 24 22:01:26 2012 +0100

    update demo tenant backups

[33mcommit 912624fd3531d1a93ec9571d8ad4b8cc96dc195f[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Jul 24 18:09:22 2012 +0100

    fix some issues across validation.

[33mcommit b5a4f9a206321b5137c15a938be8e82e57c2b548[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Jul 24 16:12:19 2012 +0100

    support soft delete of clients (includes database upgrade patch)

[33mcommit 25275638415bb8b0ceffde6d176c20e5371bd93a[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Jul 24 14:48:36 2012 +0100

    update javadoc around multi tenant aware infrastructure classes. begin start of delete client support.

[33mcommit 3c7ffb3822bbcd560096ba7a4ff5151ee377700e[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue Jul 24 23:03:17 2012 +1000

    correct patch name

[33mcommit 941a11d593ef3e1c06470ec472505dc1a653d730[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue Jul 24 22:29:26 2012 +1000

    add patch for product_savings table and amend ddl for same

[33mcommit 9dcbdc592952d8038d5161668254001447c1e548[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Jul 23 20:14:45 2012 +0100

    update database patches

[33mcommit 8e7a5467807e989b1b06bea4c876e50d2d608cb1[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Jul 23 19:13:59 2012 +0100

    make displayLabel a field on currency data rather than a getter so json deserialization works correctly.

[33mcommit 9e2d9340f7cd4061b7a542ccb871dc491478fcd4[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Jul 23 18:16:52 2012 +0100

    removed hardcoded usuage of database user and password and allow this to be configured/changed in tenants table for each possible tenant.

[33mcommit de49ed95aefe4ea5b5aef2d8b2b6427c245edd26[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Jul 23 14:44:54 2012 +0100

    add back in jackson

[33mcommit 694c2ade13c35b4922f7660ae5b43f7652d8934f[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Jul 23 01:44:26 2012 +0100

    add project-report plugin to gradle build.

[33mcommit ec18277080ea29758adf41d66dcc1578080242c3[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Jul 23 01:21:09 2012 +0100

    remove ususage of jackson for json serialization or deserialization. Ensure that additional data set info is always passed back in array format.

[33mcommit 31433c2e72a1c96d97292e74d39cb44163a6963a[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Jul 23 00:44:53 2012 +0100

    tidy up json serialization around additional data and reports.

[33mcommit d856b7830277959772e95db6ce107f16e80f5716[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sun Jul 22 23:45:48 2012 +0100

    add back in support for associations parameter as per the documentation.

[33mcommit 36ea860dfc9695af57140134845563ead27cb861[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sun Jul 22 12:48:14 2012 +0100

    tidy up json serialization for loan account areas.

[33mcommit 2a4d7335cb972a7b007ec1b6327ecc7784a1c6c9[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sun Jul 22 11:16:07 2012 +0100

    tidy up json serialization for loan products and fix office transfer serialization.

[33mcommit b3996a4a1cfa3d1614b70e56fa10c4c468774797[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sun Jul 22 00:42:41 2012 +0100

    tidy up return of json for clients api functionality.

[33mcommit 676cb33d37808c752d50f7ed9020f8773da2442c[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Jul 20 23:59:29 2012 +0100

    some fixes around transactions returned for loan account screens.

[33mcommit 3951fdc3830b080a27c8c8f29494eb391b3cbad8[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Jul 20 17:55:52 2012 +0100

    fix office transactions data and adjust loan transactions data.

[33mcommit 3e3d0055a9d7c962eb8dbab0ab1d016af09585d2[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Jul 20 12:29:18 2012 +0100

    update sql for multi-tenant setup.

[33mcommit 6c98381d94af2b81f85fbdd0a3296664df3ecba8[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Jul 20 11:12:08 2012 +0100

    tidy up permissions.

[33mcommit dc10b56388d607eadf811de1298c3930d174c76a[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri Jul 20 19:33:32 2012 +1000

    calc loans arrears for details page

[33mcommit 7630b913ec35481e48c089950cefd56a8f6afba0[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Jul 20 10:03:18 2012 +0100

    fix mistake on parameter names for loan transactions.

[33mcommit dc23dec063d1ab093e59641443b0d27afd3c8629[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri Jul 20 18:47:27 2012 +1000

    loan permissions

[33mcommit 0868960a562533dd4ac11a837236cbe7f67b7416[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Jul 20 09:14:00 2012 +0100

    update base reference data for setting up new tenant database.

[33mcommit d7b06e9809ae2a586204cae9af287d506c40023d[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Jul 20 08:43:26 2012 +0100

    json serialization tidy up for user admin areas.

[33mcommit b1a41f63b7830d229c4c393b11a3eeaf161740ee[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri Jul 20 18:11:40 2012 +1000

    wip loan permissions

[33mcommit 7aeff1f0ac7fb1a59ade2e49ae33ed1ab4ac78cf[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri Jul 20 14:27:50 2012 +1000

    wip retrieve loan schedule/repayment data

[33mcommit 9a92167025d7bc2bfd8c636c5027f049826d19e3[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri Jul 20 13:28:26 2012 +1000

    wip loan schedule data

[33mcommit 42e162762fd3f0422129b264b13fe1896c6399a1[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri Jul 20 12:27:14 2012 +1000

    wip retrieve schedule data

[33mcommit 5d07e70d59aca01423c6fdb6d8db1ee4839cf1ce[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Jul 19 15:05:31 2012 +0100

    tidy up of json deserialization for funds,offices and permissions.

[33mcommit 0bfeecad7bcf084aa725b663d2ded12f898a2929[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Jul 19 11:41:22 2012 +0100

    json returned for funds using gson.

[33mcommit 98e216b6450ff8e342a3a5c918aeaad0db7ef91e[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Jul 19 18:23:56 2012 +1000

    wip - pentaho reports

[33mcommit 55b1392847a29321ae3613125a0e01b8cdf3a173[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Jul 19 10:44:19 2012 +1000

    just some imports removed

[33mcommit 651b9c0ec3ef00181efa37680968926a2efe0332[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Jul 19 09:53:11 2012 +1000

    make full loan retrieve run

[33mcommit 5353a1ed98021bb4b58d62ba069dac84c3999850[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Jul 18 13:47:19 2012 +0100

    update to gradle 1.0 conventions meaning gradle 1.0 is required at minimum to build project.

[33mcommit e71e88a10bb4a4e1c034304cb3adbe68e62d3064[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Jul 18 13:22:25 2012 +0100

    tidy up remaing commands for converstion from json.

[33mcommit 3f201d7ffaed8249993fd5881ea913fc3a1939b2[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed Jul 18 18:40:22 2012 +1000

    WIP get loan data via sql

[33mcommit 1ee488e0ae66061585f377e2898fe82477c176c6[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Jul 18 02:44:32 2012 +0100

    some updates to fix issues from recent changes to updates.

[33mcommit bf659d4fe4f9652ff3e363cfc6967019626b4433[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Jul 18 02:01:41 2012 +0100

    update commands around client and loan functionality to use json conversion service

[33mcommit d492d41800e6d86c2fca8782f8e48c3a7275caa4[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Jul 17 22:48:20 2012 +0100

    modify offices, permissions and roles to support more flexible updates.

[33mcommit ae7d78a16e5f02b5a8878c6d7dc25a5bd2659f5b[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Jul 17 19:17:58 2012 +0100

    use google gson to support translating json request body into a map. This is towards supporting more robust update of attributes wheter they be optional or mandatory.

[33mcommit 5e6e52eebeb7f10c312052b6e0b5c506a47470a4[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue Jul 17 15:36:02 2012 +1000

    include backups of demo tenants

[33mcommit 3ec69d6efbd3f3f8b2793978ca6c153d6c7b5fff[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue Jul 17 11:04:22 2012 +1000

    wip doc update formulti tenant

[33mcommit ffd124ddd4eee7bc8ed172e63244e6b3a5b36724[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue Jul 17 10:22:47 2012 +1000

    add default report parameters to reference data

[33mcommit a3240dcde8727565abe9f021abd9a2338268bd50[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Jul 17 00:05:14 2012 +0100

    tidy up sql files for showcasing latest multi-tenant functionality.

[33mcommit 0a38cf55b3f7f6f682c41876a2945a5cf43683bc[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Jul 16 18:20:18 2012 +0100

    support passing tenant identifer through query string param to ease documentation interaction support.

[33mcommit 9e019aa7dea905b2c839217864bd681fa169a3e0[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Jul 16 17:57:20 2012 +0100

    return back http error code instead of java stacktrace when no tenant identifier is provided in request.

[33mcommit 983a61e939c801af4396300c5ff84b2d6e788363[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Jul 16 17:16:33 2012 +0100

    add check to ensure from and to office are not the same.

[33mcommit cbbeb50f9878c5f7790ed1cc73acfe71a6daf3ad[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Jul 16 17:02:43 2012 +0100

    remove command concept for office transactions to distinguish between external and intra office transactions. allow the provision of a from or to office id to do this solely.

[33mcommit 393ba7f829c2c422261f4b46ca5667d8fd902798[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon Jul 16 05:42:00 2012 +1000

    add officetransactions/template resource

[33mcommit 47b5334ca1f949a6eb17d0f76c6ed302cb5310fb[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sun Jul 15 22:01:34 2012 +1000

    ensure reporting uses tenant database

[33mcommit b84429a7878d8f6fb2e84038ba586fc2d5323945[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sun Jul 15 21:30:05 2012 +1000

    update case sensitive extra table names

[33mcommit f871f47f1d5b6ee2db01662faee0c98635e2d051[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sat Jul 14 23:47:14 2012 +0100

    update demo sql for multi-tenant

[33mcommit 8feb9980f368e04471e3e761ecedcfb67b5d0068[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sat Jul 14 23:38:51 2012 +0100

    remove organisation and anything org_id related

[33mcommit 883d1b88ff48961aeed2f478975094ad73c62789[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sat Jul 14 18:44:47 2012 +0100

    update comments

[33mcommit e7b097698c84bc1046f39a9a5403ab44cbecafc1[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sat Jul 14 18:05:34 2012 +0100

    allow preflight requests through OPTIONS to pass through tenant aware basic auth filter and put back enforcement on channel being HTTPS.

[33mcommit f186531391be29283b537570dfaf48a927cff6d4[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Jul 13 23:11:23 2012 +0100

    add sql for multi-tenant example.

[33mcommit 85eb15b48f35761192a8ffd7441c4242d865bcc0[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Jul 13 18:10:30 2012 +0100

    removed unused validator dependencies.

[33mcommit 15f0fc46774de58e094b68cc57dcb6b81fc2db2a[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Jul 13 18:05:14 2012 +0100

    support schema per tenant multi-tenancy. basic auth security filter is extended to detect tenant-id header on request, verify tenant id is valid and then use provided credentials to authenticate user details against users on the tenant specific schema.

[33mcommit 816d3decf9f5b8aaaadfe5e364f998a8c66caa7f[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sat Jul 14 01:25:37 2012 +1000

    did loanproduct read TODO about joining to currency rather than matching after

[33mcommit f6e99bc8161227dac725cd602e485a1a89be2c22[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri Jul 13 19:58:33 2012 +1000

    use fundId, fundName rather than fund object in loan product view

[33mcommit e26e4aca358728fd541330963df65c4998b13c2b[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri Jul 13 18:25:39 2012 +1000

    put nameDecorated in OfficeLookup view

[33mcommit 1a29bbe97ef5a80ad5aeb2dfd493d52da27ea698[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Jul 12 11:58:05 2012 +0100

    change office transactions capability to support intra office transactions and external transactions (money in or out of office from external entity).

[33mcommit 47c5f33d23670a071b155b5e61426a77afe78c38[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Jul 12 17:17:54 2012 +1000

    add nameDecorated to officeData to present office hierarchy easier

[33mcommit 574b5e025d7fef336d97f2dbd9ea85d910005ae5[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Jul 12 14:57:46 2012 +1000

    move client org_id db changes

[33mcommit 7ede2d31ae89a0692e3f9655d83568d89f99ec56[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Jul 11 17:39:23 2012 +0100

    add basic capability to support transfer of money transactions between offices (micro-banks).

[33mcommit f95feb306743faf6eff415e237e57559d5a0393d[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Jul 11 14:00:21 2012 +0100

    beginings of inter office(micro-bank) transfer functionality.

[33mcommit b653fd52405ffe9dcc65d1b1bdcf65db25174d72[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Jul 11 11:46:37 2012 +0100

    remove organisation association from client to allow use of JPA and respositories to work for clients. support associating fund with loan account.

[33mcommit 7c125b6012d0ab570e0d9f3fb72a64302b2a0ec9[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Jul 11 10:05:26 2012 +0100

    add patch for fund changes.

[33mcommit 855adc27efd58aeb1c548819fc093527402fa500[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Jul 11 09:37:11 2012 +0100

    support basic operations on fund concept and associating as optional with loan product.

[33mcommit 21d5a4cc1a4d9c63fb181be07f3348323fb94391[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Jul 9 08:27:02 2012 +0100

    add basic fund concept to platform.

[33mcommit 40f7627eb238267a9bb4a1ab215429dab7ac2d65[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed Jul 11 17:01:17 2012 +1000

    add try finally for closing db resources

[33mcommit e4d5325f31847e5a6d1e4611f2ec08efba446476[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed Jul 11 15:31:46 2012 +1000

    ensure all exception mappers return JSON (can revert to html/text if resource allows that format

[33mcommit 57556d3a1a25db88f5bf328a855c5d013188869f[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed Jul 11 15:15:18 2012 +1000

    WIP client search and enable Pentaho Reports

[33mcommit 7138a700268a670dda95d05a6ba610ed3c756e87[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon Jul 9 15:45:53 2012 +1000

    test remove

[33mcommit 85551d2412c4779156a7d238761d3998ad9e7924[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon Jul 9 15:44:48 2012 +1000

    test

[33mcommit 9e8088e3ffaf151ef834f658e05ab1ad196a1c03[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon Jul 9 15:30:52 2012 +1000

    remove Individual Lending javascript app

[33mcommit 080e2e32d210dbb06e613bbe9d920e1f1b455fc6[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon Jul 9 14:55:07 2012 +1000

    remove individual lending client app

[33mcommit 547fbbb6b84068e703aa46636d3480fe94d0998e[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sun Jul 8 21:52:11 2012 +1000

    wip javascript

[33mcommit a0dfc3ea192a53c9da73f82e7819e3fb95e970e0[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sun Jul 8 21:27:21 2012 +1000

    wip javascript

[33mcommit 6d03c3ac99f7e93901a83ff9104fb56dd7b05367[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sun Jul 8 20:48:54 2012 +1000

    wip javascript

[33mcommit ba8049820ba60be7e13fe4c2f72a8516c89df8a3[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sun Jul 8 18:20:52 2012 +1000

    wip javascript

[33mcommit e3c0bf3ba75243dc308a009211cea1b37f796a34[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sun Jul 8 16:46:23 2012 +1000

    wip javascript

[33mcommit 0e2265dcad450b35159b8a9aae8290700d197a7f[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sun Jul 8 15:37:12 2012 +1000

    wip javascript

[33mcommit 278c3ec4a16c3ffa457cdae6f83945cac841b44d[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sun Jul 8 01:46:23 2012 +1000

    wip javascript

[33mcommit e4976e53f4f2ef73671eaf3c36dfa7ff5ebe4d84[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sat Jul 7 19:22:08 2012 +1000

    wip javascript

[33mcommit 95650053caaffd7bd29f06d47186435914955e14[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sat Jul 7 19:10:47 2012 +1000

    wip javascript

[33mcommit a9f5efff35d953a2f967b2b139ed62b616788153[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sat Jul 7 19:02:35 2012 +1000

    wip javascript

[33mcommit 088af5843ae9a76eca36ae1bd3afad050ad8589e[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sat Jul 7 18:54:51 2012 +1000

    wip javascript

[33mcommit a7eb015c8f3232a269a3b422c1f23d38b6b60a3b[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sat Jul 7 18:01:31 2012 +1000

    wip javascript

[33mcommit a5590ce31d1cd41ec815d04ec9596e220986020e[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sat Jul 7 17:50:58 2012 +1000

    wip javascript

[33mcommit 54417e2168b413b8f0fc3e563c7136eb68a5dc82[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sat Jul 7 17:20:36 2012 +1000

    wip javascript

[33mcommit a04f42bb29ab5335a1e383098c38430152370c7e[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sat Jul 7 17:02:51 2012 +1000

    wip javascript

[33mcommit 91e9f3cbf72924fe72828ca408dfcc563e530570[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sat Jul 7 16:54:32 2012 +1000

    wip javascript

[33mcommit 3c79adb85523c1a44c757637aebfec0458440781[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sat Jul 7 16:40:52 2012 +1000

    wip javascript

[33mcommit 6bf4d39059e73e0541e49b7f667a749ea972a049[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sat Jul 7 16:34:29 2012 +1000

    wip javascript

[33mcommit 5089a19754fa42e340cc64961635eb2d6e4185cf[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri Jul 6 18:46:43 2012 +1000

    wip javascript

[33mcommit 0e5fa6385d9f2cdfde03b8c8928cf2da7c12ae75[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri Jul 6 18:06:08 2012 +1000

    wip javascript

[33mcommit cf7e00a685e8247eb4eb7cfc366e7ceb1183eb48[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri Jul 6 17:51:40 2012 +1000

    wip javascript

[33mcommit 96deb22a319527748260197d76024baeaf8cc203[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri Jul 6 17:39:57 2012 +1000

    wip javascript

[33mcommit 136136f6b421501c0b77fd07895f62384ed1d841[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri Jul 6 13:25:42 2012 +1000

    wip javascript

[33mcommit decf5006ff4d517b8082df11ae653dfe61a2a45d[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Jul 5 21:28:33 2012 +1000

    wip javascript

[33mcommit dde1688e9c43481296ba71af4c20c30bd9c8e4c5[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Jul 5 21:22:44 2012 +1000

    wip javascript

[33mcommit f1f3fc5873a6358fbb31304eb535042d4f72a928[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Jul 5 19:53:58 2012 +1000

    wip javascript

[33mcommit 425ff4b539a9130c285a64b88b470705a57c857c[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Jul 5 19:16:12 2012 +1000

    wip javascript

[33mcommit 581611d7d2b1725f1537a922b3190411893d4e48[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Jul 5 18:03:45 2012 +1000

    wip javascript

[33mcommit 06a1254abd2755198e3259c20a6e226998ce000c[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Jul 5 16:44:28 2012 +1000

    wip javascript

[33mcommit 63c91e38a9418942929e515b9fbcf80613787a2f[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Jul 5 02:54:25 2012 +1000

    wip javascript

[33mcommit a3c2252b505e904f641293f2b3fdc3feb53d8e06[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed Jul 4 21:33:03 2012 +1000

    wip javascript

[33mcommit fc48c5379ad5f494e9899dab90d0e24d568099ec[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed Jul 4 20:37:15 2012 +1000

    wip javascript

[33mcommit 5e5bb62b81122c11382c1e93f3ca0d75c442e927[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed Jul 4 00:13:11 2012 +1000

    wip javascript

[33mcommit df30e63559d9bb78b020c8d1e074585e0cd263d2[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue Jul 3 21:23:50 2012 +1000

    wip javascript

[33mcommit cd4c29d2a7f8685f0fc6cb3ebdf887a6f209d123[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue Jul 3 16:21:06 2012 +1000

    wip javascript

[33mcommit 21169510187bcb26b84bdf428f83094d65b8ad6b[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue Jul 3 15:53:51 2012 +1000

    wip javascript

[33mcommit 06e64c209b63a787aad63c7c55552233c49b465b[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue Jul 3 14:27:02 2012 +1000

    wip javascript

[33mcommit 3063b5c26f27a53327fe5069aa171423949a5309[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue Jul 3 13:08:31 2012 +1000

    wip javascript

[33mcommit 4aa9fccca6a451f50f030c99a91461d00463620c[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue Jul 3 11:18:14 2012 +1000

    wip javascript

[33mcommit 342ee17b75f5e2acf6e16b6ec7667a2b2cec8ee0[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue Jul 3 09:47:52 2012 +1000

    wip javascript

[33mcommit a6fbde2b21f9ae76e5a9bff54907b1633bfd4acb[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Jul 2 22:59:34 2012 +0200

    update readme with new AMI info

[33mcommit 4c9bd309b49b4ef1428b47b3015c7d29d00764d5[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Jul 2 18:08:04 2012 +0100

    fix add note on individual lending appliction.

[33mcommit 542a969634c6b4f7133e9aeb903ad84d9099b626[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri Jun 29 19:13:09 2012 +1000

     WIP javascript UI

[33mcommit a937d6cdcc6c5c458444a58ee52f50eebbfe6beb[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri Jun 29 17:05:25 2012 +1000

     WIP javascript UI

[33mcommit 379a50e077bcd7b28289fc1be5c165d028ce5909[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri Jun 29 16:35:23 2012 +1000

     WIP javascript UI

[33mcommit 6cf4eb9f3718469b348029e8b121a87e40e8c4f8[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri Jun 29 15:28:10 2012 +1000

     WIP javascript UI

[33mcommit 0eef469fea71faed860950297c7db711347a3b4f[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri Jun 29 15:06:00 2012 +1000

     WIP javascript UI

[33mcommit 607a3d463777e513690f2d19b279771be07a1cde[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri Jun 29 11:26:31 2012 +1000

     WIP javascript UI

[33mcommit 3969ef134da8473768002735df0e21b0d84e4247[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Jun 28 17:01:32 2012 +1000

    WIP javascript app

[33mcommit 0caca699d98e4b5ed38a2a04c047948c87f7de52[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Jun 28 16:35:28 2012 +1000

    WIP javascript app

[33mcommit c9bd332c888f0df96358a6a7564480114528e0e4[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Jun 28 15:26:55 2012 +1000

    WIP javascript app

[33mcommit 170aed05b2b447bde5ee90fb5d5c18a176efb168[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Jun 28 14:37:38 2012 +1000

    WIP javascript app

[33mcommit 17dac4ae039ce60819c93aa7367188f8ae25da8f[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Jun 28 13:40:24 2012 +1000

    WIP javascript app

[33mcommit 3c2d0333e25104322d1b882b366225072ca0781d[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Jun 28 12:38:32 2012 +1000

    WIP javascript app

[33mcommit 1467b27d25efb1e2a657c60b21a5c947a4e85043[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Jun 28 11:56:26 2012 +1000

    WIP javascript app

[33mcommit b120778e8eff57d22f0476ff81692c66866337c3[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue Jun 26 22:38:30 2012 +1000

    WIP javascript client

[33mcommit b91bbf1e01d5f5e4648faa9457a0a4e62dce082c[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue Jun 26 22:31:30 2012 +1000

    WIP javascript client

[33mcommit ca8b7b0abe03b2920c55b78a241cc6779e3bd6df[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue Jun 26 22:04:25 2012 +1000

    WIP javascript client

[33mcommit 3f90eeace05b0e1abe5759060c11212af3849ba1[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Jun 26 10:43:16 2012 +0100

    spelling mistake.

[33mcommit 23047811b447762ce673e89555268c71d507d788[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue Jun 26 14:45:15 2012 +1000

    fix pentaho report check (ignore new temp dirs for now... they are WIP javascript)

[33mcommit bbe25492dd9a379d30680d7be6b2736a21d519c8[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon Jun 25 11:17:41 2012 +0100

    tidy up dependency management of pentaho reporting libraries.

[33mcommit a047d537ab95c13dcde6343bf8b5bfe1cb446d60[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon Jun 25 10:26:50 2012 +1000

    add pentaho reporting (but disabled at the moment)

[33mcommit 1b97a0ac87e02fbd4bd28c04862f1d6bcedd9489[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri Jun 15 21:25:44 2012 +1000

    WIP tidy up pentaho use

[33mcommit 7dd7af2398787a872ff08cd120edd886142e868a[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri Jun 15 12:40:26 2012 +1000

    WIP tidy up pentaho use

[33mcommit 6fd79e3f7dcea91123f58deadf84a05b254a8ab4[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri Jun 15 11:35:13 2012 +1000

    WIP tidy up pentaho use

[33mcommit ac6b0430b8a4d9fc228b8da0a739d45d168f7dca[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sat Jun 9 03:20:57 2012 +0200

    Update master

[33mcommit 97bc9632c47208cd4b01a35e6fe6f5add6dfe107[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu Jun 7 15:21:47 2012 +0100

    fix command being passed to retrieve loan data.

[33mcommit 5dca6fa926477f12b90b260c8e946b84731a7045[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Jun 7 23:46:35 2012 +1000

    api docs update

[33mcommit 1bd23a22d7d9a21ebea9fdae42149072d72c786c[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Jun 7 22:25:34 2012 +1000

    api docs update

[33mcommit 48fe715b52c32409cb8298427c0f963d396830b1[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Jun 7 18:43:06 2012 +1000

    update api docs

[33mcommit 023fe5fd70f4865b1b2dbffa2d375afc4fe0ccba[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Jun 7 18:29:59 2012 +1000

    update api docs

[33mcommit c4a687c5c907e9ce1be6f9f369e77457d9e08059[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Jun 7 17:47:13 2012 +1000

    associations parameter - works but rubbish coding

[33mcommit db27f429cdb21ccec066eb69a044eef0cd79a818[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Jun 7 17:38:52 2012 +1000

    WIP associations parameter

[33mcommit 551cf85416b4725e315e40a10ce0878c64f1a7ae[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Jun 7 17:25:16 2012 +1000

    WIP associations parameter

[33mcommit bfe47b73548d3864f9e7e02ab232c37444155be0[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Jun 7 03:03:39 2012 +1000

    WIP associations parameter

[33mcommit b50195fa4ad1bd42751cbcbf470ccf403ef5380c[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Jun 7 02:46:12 2012 +1000

    WIP associations parameter

[33mcommit fb0c18a14b730a3e643c0296d41a3206dc01066e[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Jun 7 02:43:56 2012 +1000

    WIP associations parameter

[33mcommit 706e017da072b4c39dc1d19c7778c699f8ae5a66[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Jun 6 17:40:24 2012 +0100

    disable ability to pass currency code and digits after decimal properties during loan creation. inherit from loan product.

[33mcommit 28b26d2463cb08fc9be1c1fb68580d826684a4ec[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Jun 6 17:08:24 2012 +0100

    tidy up state transitions and transactions support through api.

[33mcommit 22d20a2273d86180f05e535780d4fdca7ddb28b8[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu Jun 7 01:35:30 2012 +1000

    api docs

[33mcommit db32280c0d50774c77546b113d6f194dfcab0ea6[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Jun 6 15:51:14 2012 +0100

    tidy up response for client data with display name and also loanschedule field of new loan data.

[33mcommit 8ddbb0935d9693c7e49b0e2e724aa19d91f6e8f2[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Jun 6 15:23:28 2012 +0100

    update noteTypeid to be more descriptive in response as per other enum values.

[33mcommit e68f7f75898e00525d40c940479001db8564aae9[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed Jun 6 12:50:50 2012 +0100

    tidy up response for client account summary and retrieval of individual loan information.

[33mcommit 26c57c7ac0814ce3461dd4882367eeca0ab78e84[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed Jun 6 20:09:21 2012 +1000

    api docs update

[33mcommit d4bebbb478bddc7e348a29ecba95a75ebb797d1c[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed Jun 6 19:57:22 2012 +1000

    api docs update

[33mcommit cb4d4544b9bad34bd24e56b533731565d014bd00[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed Jun 6 19:52:01 2012 +1000

    api docs update

[33mcommit a557367215ec74b14144201ba570fe33b06aaf1b[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed Jun 6 19:49:27 2012 +1000

    api docs update

[33mcommit 1d4b0fe6989fe6ccdf826a2abcc1efdb91b965d9[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed Jun 6 17:38:30 2012 +1000

    api docs update

[33mcommit e8b8e362b8abdacdf7da3ad9be2804aecb88df1e[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed Jun 6 16:48:53 2012 +1000

    api docs update

[33mcommit c6d7232fd23cb76a60f322606a08a672605f2ea5[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Jun 5 19:17:01 2012 +0200

    Update master

[33mcommit d5f911f1c951c76e5862dd94cdb580fa9b6b8f60[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Jun 5 18:59:04 2012 +0200

    Update master

[33mcommit 15053d094b4c1561832cfa359d4b18ea3dbc3d5b[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Jun 5 17:52:25 2012 +0100

    move url back to dev url of localhost.

[33mcommit 878c07f441c586232bc52fd98ec3fc3849532fe2[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Jun 5 17:42:09 2012 +0100

    update url to demo url

[33mcommit d0c74bd22aa06c9d382633854758ded9c730625d[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed Jun 6 02:31:08 2012 +1000

    just some docs WIP

[33mcommit 74ddcf7b3c19a476ae1b5ee2a50c09c31e8f0c04[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Jun 5 18:31:19 2012 +0200

    Update master

[33mcommit 78b2a5ef4110d0e2fc93ddb9fec9ed8ae288c402[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Jun 5 18:26:50 2012 +0200

    Update master

[33mcommit dd85988882ceafe2319cf0268c3e34780a091329[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed Jun 6 02:24:16 2012 +1000

    just some docs WIP

[33mcommit 8cec16549d4b6e4bac1cd5b4aa4de264ffb470bd[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Jun 5 18:12:04 2012 +0200

    Update master

[33mcommit 8dc8e863db1fa48b554cac3719f91740c411ca0b[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed Jun 6 02:03:11 2012 +1000

    just some docs WIP

[33mcommit aaa6600a23545c4e9ef652007df1ca4ebc0a216c[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue Jun 5 18:01:58 2012 +0200

    udpate amazon ami details.

[33mcommit 59e425825173442d054deaeb299c30cf5fb97dba[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sun Jun 3 12:20:20 2012 +1000

    update doc

[33mcommit 2c0c9ad6160f22c9d815dd6aae9a9fa871362e1d[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sat Jun 2 01:36:14 2012 +1000

    update api docs

[33mcommit 43dd359daf23740fefbf1aa79988d12ad84f27c2[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sat Jun 2 01:25:29 2012 +1000

    update api docs

[33mcommit 1eb4978eea667e88cd7e582c5942d6c9a2f30910[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Jun 1 01:35:05 2012 +0100

    add check for client id when updating client notes.

[33mcommit fa6af79a3a45faa7b57975cff73c90e518134d4e[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri Jun 1 18:17:23 2012 +1000

    api docs update

[33mcommit 7a2a3157594cf94dbcb701a460bc06337b296071[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri Jun 1 18:13:23 2012 +1000

    api docs update

[33mcommit 2c654019bedb7741aa3333ce1f9c42b8fd19b9eb[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri Jun 1 17:38:08 2012 +1000

    api docs update

[33mcommit bed2136e0d3cdb56abb4a409e3cf234f76754b29[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri Jun 1 15:17:14 2012 +1000

    api docs update

[33mcommit 1674b9f60070c18ef9d9218d3c7ca260f7a8314f[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri Jun 1 13:28:14 2012 +1000

    api docs update

[33mcommit d1947a3845c6c2a5a197b8a50913becd388c834f[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri Jun 1 13:22:47 2012 +1000

    api docs update

[33mcommit 3afd2a6f7499e97bc1fa16b797fb97d4d9dd08e9[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Jun 1 01:17:21 2012 +0100

    ensure actions on loans with dates that violate domain rules throws exception that is handled gracefully.

[33mcommit 4579d66cdd3fa210881f4a616005cbb545442297[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu May 31 22:26:55 2012 +0100

    remove presentation related files from platform.

[33mcommit 713d639c35032867fd9fa639eb7ba8aa1cdbbe67[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu May 31 18:22:56 2012 +0100

    tidy up data files for reports.

[33mcommit 9f7e75b93b622f75cf1434c4e73b88371761b725[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu May 31 17:57:55 2012 +0100

    remove mifosng-data

[33mcommit a4a24a266906780ca4854abd816d8729e48ae040[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu May 31 17:55:50 2012 +0100

    tidy up data classes in prep for deletion of project.

[33mcommit e0beb43f6f4ccebfa1ac9d3d67c0c1fe2f96e706[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu May 31 17:39:44 2012 +0100

    tidy up loan account data being returned breaking up into basic details, permissions, derived loan data and repayments.

[33mcommit bfe75a5c35f17e8698cf1422f9f2de2fa8783796[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri Jun 1 01:17:44 2012 +1000

    update api docs

[33mcommit 9d597e2add615ff098ad2083a56c8074de0012a7[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu May 31 13:39:30 2012 +0100

    tidy up new loan data returned for submit loan flow and ensure locale is posted for forms with numbers and monetary values in them.

[33mcommit 7a62b37210323d800479588f20210679dea4949e[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu May 31 17:43:10 2012 +1000

    api doc and error update

[33mcommit 01f4b83a9c635477db601adc13f12c18645aac01[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu May 31 17:23:39 2012 +1000

    api docs update

[33mcommit 445da9a4e08e4893f1b6f2dbc59bc6fa34641eac[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu May 31 16:53:53 2012 +1000

    api docs update

[33mcommit c37041e6b7cb6a74882cf452a0bad0b77691c169[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu May 31 14:19:12 2012 +1000

    more api doc updates

[33mcommit b491d53f10b40142d5195e6a4918894b7740313f[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu May 31 13:27:36 2012 +1000

    more api doc updates and make available LoanProductLookup to loan template

[33mcommit 803db98668379b5f9e5b3bbec6c0faa9e9b8f3a7[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu May 31 02:10:13 2012 +0100

    make locale mandatory and validate language and country codes given are supported.

[33mcommit bb838509fc806b45c4ca5ec3dafc4e931d6930bf[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed May 30 17:22:02 2012 +0100

    removal of unused resources and tidy up.

[33mcommit bdb549253c21a3b65e505a3b2788bf2913f7e44f[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed May 30 16:50:45 2012 +0100

    tidy up by moving mifosng-data files back into platform project.

[33mcommit 6a0f4b3f6fc1f580e48c2f6b568b3b265bbce87e[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed May 30 16:12:13 2012 +0100

    align loan product data with field names used for create. return enumerations with extra information that describe it better.

[33mcommit cb0cf64a2bec0ad161c88eaebc1b202c563377af[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed May 30 23:45:43 2012 +1000

    remove read for clientid when deleting a loan

[33mcommit b39afa0153b8b717710415c9f68c92a23f5d49f3[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed May 30 23:38:30 2012 +1000

    update api docs plus return loanId after delete

[33mcommit c3dade1b7d8794ef882cae19a71050e104445834[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed May 30 12:23:15 2012 +0100

    handle update of parent office error cases.

[33mcommit 3b212464fe83de66b634d722aa29c5d606deb0e3[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed May 30 11:08:26 2012 +0100

    handle deletion of non existing user case.

[33mcommit 92ea0576155e28fc968daf72a6fc94609c83aa01[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed May 30 18:06:10 2012 +1000

    api docs and notes

[33mcommit 284d05528bcc6abefe8a61608609841904372308[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed May 30 17:40:26 2012 +1000

    updated getLong null check to make adding extra data work again

[33mcommit 3cfd1156cc56dfe867c1d0295d284fdec67b185a[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed May 30 17:16:52 2012 +1000

    update api docs and small additional data code changes

[33mcommit 8ecd64c1665093935926af185008f6c093291441[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed May 30 15:53:24 2012 +1000

    update api docs

[33mcommit 1aaf53bf5253ddb995e3fc9b0334703a997b9e29[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed May 30 11:59:33 2012 +1000

    update docs, remove groupType from display

[33mcommit 7fce2af8fd54d9a454e4679a6b054f74e2177894[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed May 30 09:33:07 2012 +1000

    api docs update

[33mcommit 27eef9c055dc3b8b45ef85dacac72bf32710b078[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue May 29 23:02:00 2012 +0100

    tidy up.

[33mcommit 4b6d4666f0e27affea6e8bd70c853ef418e999c6[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue May 29 22:50:52 2012 +0100

    move adjust transaction to POST http verb.

[33mcommit 92b3ed40301be7fe6da4ccdcbf87fbe8dc7b7bde[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue May 29 22:29:21 2012 +0100

    update state transition and transaction api for loans.

[33mcommit d92a2b1f25bff50ccb8b26588836e88b3eb6f75c[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue May 29 17:36:28 2012 +0100

    tidy up calculate loan schedule validation.

[33mcommit 1450e38f7b2189edf930f809c6e6b14859dbd745[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue May 29 14:45:20 2012 +0100

    support client update properly and tidy up parameter names for client.

[33mcommit d413aadb4a01b7d121eb3de5a4a95733ba3d72aa[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue May 29 13:46:56 2012 +0100

    tidy up resource not found exception handling.

[33mcommit 41493b5c49c4ab4625650c4608a0323ff52cf6d2[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue May 29 13:30:58 2012 +0100

    support loan product validation messages.

[33mcommit 53bf93468fa104d3fd6022963f99fd6b346fd3a0[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue May 29 21:32:28 2012 +1000

    api docs update

[33mcommit e3dbf561d45f37c7161da0b933b68c082d47d15a[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue May 29 19:21:45 2012 +1000

    update api docs

[33mcommit 2254257320045fa3e35f05f846a74785887e2178[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue May 29 17:18:06 2012 +1000

    update api docs

[33mcommit ab3692c64aedfab6e8d1a4c0dc4b303dd25fc2f6[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue May 29 16:57:39 2012 +1000

    update api docs

[33mcommit ba706b3a9fe66e07049312eb1edc3c8b20e48646[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue May 29 16:28:35 2012 +1000

    update api docs

[33mcommit 7bcd4fb9ba4debca663752e42daf875758448a4c[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue May 29 15:35:27 2012 +1000

    update api docs

[33mcommit 4fd921147c939e1696c4c972cc3522cd8a97561a[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue May 29 14:58:05 2012 +1000

    Add dynamic mandatory fields display (for ease of testing - in FF at least;

[33mcommit 25bbd36ee11c44322e53dfe35f5d0d733f73be30[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue May 29 12:05:05 2012 +1000

    fix viewing of additional data after saving

[33mcommit 7282d3fe7d19a59a79ee54628b6b726fe80cf284[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue May 29 10:45:47 2012 +1000

    update api docs

[33mcommit 2e5a68fb4195ed2dbd06c733246b1d1767bb5a22[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue May 29 02:18:49 2012 +0100

    check locale string

[33mcommit 442b9d93d03f03fcf3723f0c516adc12444d542b[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue May 29 02:12:16 2012 +0100

    update loan product resource for create and update

[33mcommit defcce1a312686f4ad7747121afd3a7428d62b2a[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue May 29 00:46:39 2012 +0100

    remove formatted from openingDate parameter for office creation and update.

[33mcommit 95ead2a9fcf00af9cc6833bd1bffc76ea92c9318[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon May 28 23:35:57 2012 +0100

    fix validation message.

[33mcommit 0fb05315ea09ae3f4f745cb35a63934999e973e5[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon May 28 22:15:38 2012 +0100

    add authorization to role and user services.

[33mcommit 682b11f78ea9be322d13a5947389b017bdc6dce3[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon May 28 21:02:29 2012 +0100

    handle resource not found possibilities. move version information to web.xml rather than each resource.

[33mcommit 1cf29aaeadbedee2df2a6f9de691aa523bf7202b[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon May 28 19:49:47 2012 +0100

    catch error cases around users and roles.

[33mcommit 66d3babb06457450911e59008a51a46635e530de[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon May 28 17:20:05 2012 +0100

    remove need for undo resource - align undo of state transitions with state transitions allowing for note to be added through ui.

[33mcommit d127003d6da5ad7a9ccbe3093941fbf97a6dbd19[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue May 29 02:08:03 2012 +1000

    api docs update

[33mcommit 6b3d8fa762a1219fc7396a0ec0a81b2f219103d7[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon May 28 20:28:23 2012 +1000

    minor api docs update

[33mcommit 3e8ea8df6136e5c8ef90ff2a9db5c80b33736de1[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon May 28 11:05:28 2012 +0100

    return id of deleted resource when deleting a user.

[33mcommit beee7823b20a67d1e4493d169e29e9ca95621734[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon May 28 10:56:43 2012 +0100

    fix sql grammar around retrieve all roles.

[33mcommit d348fd18df6c2f072e45321754e62980cbaaf215[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon May 28 17:20:15 2012 +1000

    more api docs update

[33mcommit f2ee4f621938c48e9ce3bc841a16cd41f098c0f9[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon May 28 16:31:05 2012 +1000

    api docs update

[33mcommit d487c7a0fe6cee2554eb6a4c5f9a45f880714f4c[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon May 28 16:27:03 2012 +1000

    api docs update

[33mcommit 59e29f9b6a294cc2f07ea4373df254704860960c[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon May 28 16:09:36 2012 +1000

    api docs update

[33mcommit ff4b011824ac0d53701fc7ec419ea3a5ce484262[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon May 28 15:02:39 2012 +1000

    api docs update

[33mcommit 74a94a09bc62f5d93c1eb95dad696d15718b11e7[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon May 28 14:07:44 2012 +1000

    api docs update and order list users by user_id

[33mcommit c14a46a167e1f0cd3533faccfbc4d81e014aa1c8[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon May 28 12:26:51 2012 +1000

    api docs update

[33mcommit bc4999e254b028a4364ba457e6b1256d049fbd9d[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon May 28 11:58:59 2012 +1000

    api docs update

[33mcommit 4dcafe46db9cda45d7d16b51b1a46fc57466656c[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon May 28 01:53:57 2012 +0100

    add general support for unsupported parameters in modifying requests.

[33mcommit 644d1b9635b132f7b772e4703473844e39d924be[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon May 28 10:50:52 2012 +1000

    delete old doc files

[33mcommit 0acf904c0ed0005ff3f26bbb15791404176bc367[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon May 28 10:43:23 2012 +1000

    api docs update

[33mcommit ca8492e9335acd397ef01821e81103d4e1c23be7[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon May 28 01:27:37 2012 +0100

    remove notSelectedCurrencies from currency configuration resource.

[33mcommit 3318e76150c38576726216f22bb926a6ad96185e[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon May 28 00:17:44 2012 +0100

    data conversion work per locale.

[33mcommit 986cf32fc8af6ddb95d3ae6c89a11b46427fb267[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sun May 27 23:26:46 2012 +0100

    use JdbcSupport throughout backend.

[33mcommit 1465f2429885875aec2bf66332c91fe98535fa45[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sun May 27 22:00:09 2012 +0100

    add JdbcSupport class for retrieving long and integer values with possible nulls.

[33mcommit cc8bd8ba5ece37b9db6219709b1b2d373d5cbf01[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon May 28 02:27:57 2012 +1000

    getLong replacement (temp) plus some api doc updates

[33mcommit 47749b2f457622b0f12bf96340cca6c493d4aaca[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sun May 27 22:23:26 2012 +1000

    updating some api docs and generating questions for keith

[33mcommit 237d28ba10439b12da29efa81dfde39d24677ecc[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sun May 27 21:16:04 2012 +1000

    WIP filtering loans again

[33mcommit 331c8831f93e2c5cf9d31c2880c5f0e9d13617f1[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sun May 27 11:58:58 2012 +0100

    fix notes ui with new json.

[33mcommit 5e3678ed9ff7094d74635c51d4d7ec96a26d162b[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sun May 27 11:30:11 2012 +0100

    support display of client data in tab with ajax fetch of data.

[33mcommit 64849cba7b8975ac88460db946f11a2b144cde5a[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sun May 27 20:38:35 2012 +1000

    WIP filtering loans

[33mcommit 25484f8409c0d7417ec2bd945a49543a26caf824[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sun May 27 14:07:29 2012 +1000

    filtering - start of loans

[33mcommit e79cb4581cc17b21d37e5a575d7f4ec9d5135199[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sun May 27 13:38:19 2012 +1000

    filtering - rest of client gets

[33mcommit 7608ba4b907e721b8b9f703830fa2bb0e919c24e[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sat May 26 12:19:18 2012 +0100

    fix ui areas for new filtered json output.

[33mcommit 3a6923d71e9e8afcad1eb64b5708cfbcaf3d27a4[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sat May 26 17:30:05 2012 +1000

    filtering clients (part of)

[33mcommit b2dfcda8e693c018050c4d4bfaeacbe052a0a7a5[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sat May 26 16:41:07 2012 +1000

    delete some list dtos

[33mcommit c11adc2da8e64a801323b860cd76950f6de64671[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sat May 26 16:32:27 2012 +1000

    filtering users

[33mcommit b96764da56becfc67ba2a3392d57eaedb250174e[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sat May 26 15:36:22 2012 +1000

    filtering configuration (for pretty printing purposes)

[33mcommit 31647034d1ed2e2044b7aa992fa8f0d90303db77[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sat May 26 14:40:39 2012 +1000

    filtering reporting (for pretty printing purposes)

[33mcommit 992379c7025337e22bf3ad94e864cfa7e02b6bdf[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sat May 26 14:15:04 2012 +1000

    filtering permissions/roles - working - but rethinking filters just a little bit - not much!

[33mcommit 5bc12548aaea0f07c1ea63ff4c0c8926bd203cfc[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sat May 26 11:11:27 2012 +1000

    filtering permissions/roles (not working yet)

[33mcommit e48f0247d577c70267bb451eb5184c9aaea76bc4[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sat May 26 09:57:32 2012 +1000

    filtering permissions plus fix to json response builder

[33mcommit 704ad6fe96412e068f5661e84429a64b8309b2bb[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri May 25 23:38:33 2012 +0100

    client update.

[33mcommit fdbb4e62f99ae165e2b0a468db8b51a7e5759373[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri May 25 16:43:41 2012 +0100

    some tidy up around client validation in prep for update.

[33mcommit 63a1f6cd6359a66a0f76e4681ab3511cfe77b86c[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri May 25 14:04:16 2012 +0100

    tidy up ui for loanproducts and offices after json changes.

[33mcommit 5ac62588f6ebd89cf31339c4dc69455d7fd87527[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri May 25 13:12:08 2012 +0100

    tidy up validation for users.

[33mcommit 6c71d027ce26b0f293e37fae6ea0b4639bdf5db5[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri May 25 12:23:34 2012 +0100

    tidy up users resource supporting better updates. useraccounts resources has been removed along with change password template convenience api.

[33mcommit 898632790bf7f2d3fa98df0461f32d9f684c684b[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri May 25 09:06:21 2012 +0100

    some work on role and user validation and updates.

[33mcommit 3bee9582e1bfe5ff6778af8286dd2ca7577577a9[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri May 25 21:10:40 2012 +1000

    LoanProduct filtering changes

[33mcommit a0680ee34bae8d396f1c9357e6b6b2c7db22226a[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri May 25 18:24:32 2012 +1000

    WIP filtering, pretty and templates

[33mcommit 18579bc0b5e2a236a5bc80c64c7c38d41b1f5c3a[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri May 25 18:04:05 2012 +1000

    WIP filtering, pretty and templates

[33mcommit 07b909206c58de166f2da9d616cec4df586b51c4[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri May 25 16:22:39 2012 +1000

    WIP - more generic json response builder

[33mcommit e43f007bd4cb54036e213fec022fc91e5f897df3[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri May 25 14:57:09 2012 +1000

    WIP fields parameter additionfields

[33mcommit 8612aef9e69851128c4db76eca28f6f2ffb24acd[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri May 25 13:09:37 2012 +1000

    update api docs a bit

[33mcommit 10b762f24db2b1070668c7257add7e93b77e8d94[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri May 25 13:06:47 2012 +1000

    WIP fields parameter

[33mcommit 041225c8f5b49900d99fc1b6667a64ae7ab712c2[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu May 24 14:08:48 2012 +0100

    move json formatting code into service class. support pretty printing and use non deprecated api.

[33mcommit 46a18d197e3ac52fcde94bb3e4492d07e4ebbdd8[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu May 24 21:24:08 2012 +1000

    WIP add fields and returnTemplateData parametera to office reads

[33mcommit 182e7a85b68a25f6ebaad1ef681cb6924ea0d23d[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu May 24 12:24:57 2012 +1000

    api docs updates

[33mcommit ce59a3c1cfff4af7b59037f5076eae201353444b[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu May 24 11:55:15 2012 +1000

    json filter work

[33mcommit 9c8edb45a3a86bb4330afc73a999140f687a5d11[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu May 24 01:56:50 2012 +0100

    support update single piece of information for loan product and tidy up validation.

[33mcommit b5d94a8c95aeb52b7086039d009dd36788ffe777[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed May 23 16:54:16 2012 +0100

    support updating single pieces of information about an office.

[33mcommit fac564246954aa4e2145312670b9fb7a31dc1e59[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed May 23 18:20:59 2012 +1000

    WIP api docs - matrix (not bad)

[33mcommit 57b5d28fd43366cc7a08ca6cbf267caa9263a46a[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed May 23 18:16:37 2012 +1000

    WIP api docs - matrix (not bad)

[33mcommit 7bc94e446f392b9facb21abf02119e6c85858bf3[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed May 23 17:41:40 2012 +1000

    WIP api docs - matrix

[33mcommit 44876813896d278d737fe52116d49562443964f1[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed May 23 17:21:32 2012 +1000

    WIP api docs - matrix

[33mcommit 4f5fcd5645c38b4d78c7f3e3d4b7946d76c10a79[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed May 23 16:33:36 2012 +1000

    WIP api docs - matrix

[33mcommit fe03ee35b823f2f4f5db1c990da528e858b7b81a[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed May 23 16:07:45 2012 +1000

    WIP api docs - matrix

[33mcommit ca58016a7337b2e794e7b48eb1d0c8bd13ff3d23[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed May 23 14:44:22 2012 +1000

    WIP api docs - matrix

[33mcommit 393b428a80d21673718b809072d5a33912d263ab[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed May 23 00:18:07 2012 +0100

    doc changes around loan api

[33mcommit 7e6f11d3989dc58977690e2c61b2fa31d6bc4ba8[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue May 22 23:05:58 2012 +0100

    doc changes and related code changes around client update.

[33mcommit 87275e1d47d60881e30375d33b9eeee106a9696c[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue May 22 17:24:30 2012 +0100

    update users and roles

[33mcommit b96247436855f560fc875deb60eb289f0952fc06[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue May 22 16:52:13 2012 +0100

    update to last nights work on offices, loanproducts, users etc.

[33mcommit 6c0880e64c165a3e7cdc4f7f8b9f83fec05bbbcf[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue May 22 16:33:39 2012 +0100

    update to last nights work on offices, loanproducts, users etc.

[33mcommit 30d6e8146bcac462e6b6601a370731eb1c0262c1[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue May 22 16:08:42 2012 +0100

    tidy up mess that is SSL support.

[33mcommit 95fd48e215b6a258d114109b1857b3aadc5b0d3d[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue May 22 14:05:02 2012 +0100

    try to handle SSL stuff.

[33mcommit 25cfaf5de464cce820969a5daa448b5db4d94dac[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue May 22 23:56:14 2012 +1000

    api docs WIP

[33mcommit 4ce2013fac3a8f59cda84f1a441a4b7f2fa4cb05[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue May 22 22:58:38 2012 +1000

    api docs WIP

[33mcommit aa6fac3eebc356bcc0cd8bfbde0c5279076abaf6[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue May 22 22:28:05 2012 +1000

    api docs WIP

[33mcommit d0be1a1d33a4ab8ad04bba521aad4a19062bef39[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue May 22 18:27:11 2012 +1000

    api docs WIP

[33mcommit af1c75cf7b062f15eaf93cf676ff4814d9a9f78a[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue May 22 18:16:09 2012 +1000

    api docs WIP

[33mcommit 8396986766fe3a48357a6003243a86a6112dc390[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue May 22 17:59:30 2012 +1000

    api docs WIP

[33mcommit 9dc603992e3e5fc537a87a445543101a2e4cb772[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue May 22 17:44:55 2012 +1000

    api docs WIP

[33mcommit 77371833172387c79898de54af18f11fc4b9c86d[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue May 22 17:38:42 2012 +1000

    api docs WIP

[33mcommit ecd6ece89bf34eedabb18245d03c90c038832c1d[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue May 22 17:17:58 2012 +1000

    api docs WIP

[33mcommit 8a8922b95b3af63e51ce647202ebcac0e3323c7c[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue May 22 17:02:09 2012 +1000

    api docs WIP

[33mcommit e567a1971bd8129288010d970d4e244c1d5f9539[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue May 22 16:22:56 2012 +1000

    api docs WIP

[33mcommit 54062c8919daebb2f9af4f45e923a1b9c04d8cc6[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue May 22 00:47:32 2012 +0100

    update futher api docs around user accounts.

[33mcommit cf465cb052cb8cbca8798b795a97f4a45fe5c60c[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue May 22 00:26:07 2012 +0100

    update futher api docs around user accounts.

[33mcommit 41b3847a8d3e52b1562cf21641a8d6b06b973561[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon May 21 22:08:02 2012 +0100

    tidy up properties not used at present on commands.

[33mcommit de5b1209ae96aff55aef60e37b375256baf7161f[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon May 21 21:55:44 2012 +0100

    update loan product api request response examples.

[33mcommit cad2722d96eb669120ccdc272987a8d5c40e9eb5[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon May 21 20:54:50 2012 +0100

    update of basic office api info.

[33mcommit 99a3df538f8f6883ab1cbd344bd580f898e33fc3[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon May 21 20:23:11 2012 +0100

    start with update of basic office api info.

[33mcommit 4a8d5a86f6d0a84bcf2af06109a9609aacfe5d24[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon May 21 17:36:13 2012 +0100

    add https support for platform available at localhost:8443 by default when running from command line.

[33mcommit 69722c9409b4470de8eec2e8b2beb83b7d6ae556[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon May 21 21:12:04 2012 +1000

    api docs WIP

[33mcommit 5a72f687e7519345cabac0f2e41ddd70df8445a1[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon May 21 17:46:53 2012 +1000

    api docs WIP

[33mcommit 4019a783a77b73dc6566d78f904b288aec070679[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon May 21 02:08:08 2012 +0100

    begin tidy up of error handling for authentication.

[33mcommit 52de332e5e2475528b588db5c6fdaddef02510bb[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sun May 20 15:48:33 2012 +0100

    remove hardcoded references to localhost or any part of the platform api url from jsp and javascript libs.

[33mcommit 965429e30e76696ed1cb537761317b722b386037[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri May 18 13:57:47 2012 +0100

    remove hard coded reference to localhost:8080 in areas.2

[33mcommit ac8c68a43528c023ce89e95f45ca00616e95ca3e[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri May 18 19:51:00 2012 +1000

    WIP API documentation

[33mcommit 9669f369741c4cfd659a2bf20b5951c142fa64eb[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri May 18 19:32:37 2012 +1000

    WIP API documentation

[33mcommit 428ba5e339c26e80b5e117db5798ba8c58dcf94f[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri May 18 18:40:52 2012 +1000

    WIP API documentation

[33mcommit 9f686e7e808eedbdd0e1fb7b99ef9b054572a48a[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri May 18 14:32:55 2012 +1000

    WIP patching in username/password for basic auth in reporting/additional fields

[33mcommit d6e76390bad4e62f834143cb9bfd8ff8d41243bd[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri May 18 02:46:29 2012 +0100

    support client login and calls to api through jQuery.

[33mcommit 93678bf9cedceb3b944c85661eafb173c15c11fb[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu May 17 15:11:27 2012 +0100

    finish up work on moving backend into stateless server with resource-oriented api using basic auth for securing api calls.

[33mcommit 45965a3f502bb4b93ac0f76c36b1536c6ef0af59[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu May 17 21:09:16 2012 +1000

    API refactoring Additional Fields

[33mcommit 62ec20112fc8caaba7d1c5c7bd9783bbc8f629bc[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu May 17 19:28:30 2012 +1000

    error processing WIP

[33mcommit 3d8439ac98ef3bc2d3a1ee77e20e854599e36e97[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu May 17 17:38:21 2012 +1000

    make sure reports ajax calls dont cache

[33mcommit b71778a67854609017fe63119f7b4f377e4e89f2[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu May 17 17:19:55 2012 +1000

    WIP error handling in reports and additionalfields api

[33mcommit 49b90212ac6cad1c56be3688b40a01908f9edcbd[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu May 17 16:29:13 2012 +1000

    WIP error handling in reports and additionalfields api

[33mcommit 6693b4500351c2fdfe3ddf96b513101118155653[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu May 17 02:45:26 2012 +0100

    tidy of old error handling classes and read and write monolithic platform services.

[33mcommit 7bb8b9359e9c57dcbc6afaf3262efe9afe9849e8[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu May 17 02:07:41 2012 +0100

    add remaining state transitions and loan transactions to api format.

[33mcommit 8bc0b7461f56c193206771c7c22c5f6912fa47c2[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed May 16 22:02:04 2012 +0100

    add state transitions around loan to api.

[33mcommit a006e60681626cc4f1ab70c71bb64136b5d8835b[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed May 16 18:20:59 2012 +0100

    support submit new loan application and delete loan capabilities through api.

[33mcommit bf7783fc9483616325b3d21014ea7e71040268ec[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed May 16 01:42:54 2012 +0100

    move functionality around new loan application to loan resource.

[33mcommit 75054feeeed4a481b04a445dcae4c2b8cfa316d0[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu May 17 01:25:42 2012 +1000

    WIP Additional Fields API

[33mcommit e4e1062f35b3b1c6a5bebec53fd250fdf0d27c75[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu May 17 00:00:24 2012 +1000

    WIP Additional Fields API

[33mcommit 9c49c39448a36381f88f732886a6f79b600cd105[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed May 16 13:36:09 2012 +1000

    WIP Additional Fields api

[33mcommit b0a8b84f6f082f9d6167c36f67937f0ddfe82c46[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue May 15 23:37:02 2012 +0100

    add loan resource to support retrieve of individual loan.

[33mcommit 04d4fbaf85aac62cedbb894f5dd5cc7219023c7f[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue May 15 16:44:51 2012 +0100

    handle client resource and sub resources for notes and loanaccount summaries.

[33mcommit dc1c39c5ec4486e0880990659c9f43bdcf45226b[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue May 15 09:11:49 2012 +0100

    start of client api work.

[33mcommit 29ab498e396d8e049f98d07079c23232e90231de[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue May 15 23:06:13 2012 +1000

    WIP remove orgId references from reporting

[33mcommit 3fe1fe761e1fb11ef12166e5afa8c5d6335e5fac[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue May 15 22:59:17 2012 +1000

    WIP more reports API refactoring plus api docs

[33mcommit 06fa7ff146b7889115d2b1c5e1925c0b214af0e0[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue May 15 18:09:51 2012 +1000

    WIP reports API plus api docs

[33mcommit e96d623a462a4995bc12aea9345c06851132bc10[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue May 15 01:15:25 2012 +0100

    finish off remaining user admin functionality through API, removing client proxy work.

[33mcommit 81e04b66d216fa2e6ad52566df2743db3e08e679[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Tue May 15 10:09:04 2012 +1000

    api reporting stuff

[33mcommit d7b39edaeba49b396ce8e98666085d1b92dd630c[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon May 14 17:05:19 2012 +0100

    udpate change password validator.

[33mcommit 06ac2869b783fecd1db441f1d6b9fde45322845c[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon May 14 17:00:40 2012 +0100

    add useraccounts resource which is responsible for supporting capabilities that allow a user update their account details.

[33mcommit 5ccecb8bc48e3505f0dbe7b95cbb954f57667e40[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon May 14 16:01:24 2012 +0100

    switch provider app to 8080, tidy up users resource and clean up export code for reports.

[33mcommit 7947dda4fec7b7bb6de87e484e1d73e6541145a8[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Mon May 14 00:43:34 2012 +0100

    first steps around moving users to new approach.

[33mcommit 9ddaf4c00399df4f753dd82171cb645b1e6be846[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon May 14 18:00:04 2012 +1000

    WIP refactor api - make rptDB parameter optional

[33mcommit 3ae70b06bc7d84e5af2cc4e577a70967a3b0de24[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon May 14 17:06:03 2012 +1000

    WIP refactor api - export csv refactorings so its just an extra parameter to reports resource (looking very nice)

[33mcommit 934cc0ca4758158edd9ab146f3292069cc8c913f[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon May 14 16:43:52 2012 +1000

    WIP refactor api - export csv refactorings

[33mcommit ad14229e99be732410721674582b3c0d587529a9[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon May 14 14:25:38 2012 +1000

    products, offices and configuraton now use 8080

[33mcommit 1612ff78860c7cccd9a819f8fd8bd4e26b25db38[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon May 14 14:17:40 2012 +1000

    misofng-provider now uses 8080 by default, also reporting resource set to /v1/reports

[33mcommit e085bfa39c0bc4141f8920cf7a89a9d51c58ce68[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon May 14 09:43:24 2012 +1000

    ExportCSVController.java talk

[33mcommit 71f21f05587815be803e618e069044e98619b8bc[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Sat May 12 11:43:09 2012 +1000

    refactored reporting/export code out of core class into own

[33mcommit 6019b6344a1844607bf30a7e74c2866b9f05b735[m
Merge: f82c87d 1bda879
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri May 11 14:26:00 2012 +0100

    Merge branch 'master' into api-work

[33mcommit f82c87de78041a0c2bef4ae4aedbe255c6c4489d[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri May 11 14:23:43 2012 +0100

    finish off integration of organisation admin functions in UI with backend services through cross origin request from jQuery.

[33mcommit 525831db0498e01dfe154dc67bfd45a4fd3e2954[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri May 11 18:30:18 2012 +1000

    WIP reporting refactor

[33mcommit 22faf023768e0117f02e08b2c05e2fd35d688c5b[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri May 11 18:13:01 2012 +1000

    WIP reporting exportcsv refactor

[33mcommit d219f6fa14377d7189cfa1f95ddc744a25736a8b[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri May 11 17:57:09 2012 +1000

    WIP reporting refactor

[33mcommit e729ea901d532613520e87eb257e26f878e7c75c[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri May 11 16:34:53 2012 +1000

    WIP extradata - refactor code, remove from client app

[33mcommit 326fd4f25c9a7344ba80285173346293e4ae548b[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri May 11 12:37:18 2012 +1000

    WIP post extra data and parson json string for edits

[33mcommit 370127a16dd3616c25943c34123a014fb853df2c[m
Merge: 5d9a7c3 f9918ae
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri May 11 10:54:29 2012 +1000

    Merge branch 'api-work' of github.com:keithwoodlock/mifosx into api-work

[33mcommit 5d9a7c3229a10d6eb47b8ff5b7ad1afce37c5555[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri May 11 10:48:04 2012 +1000

    wip api

[33mcommit f9918ae3fdf1d68a5dcbf7e809675a3115ac02d8[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri May 11 01:44:42 2012 +0100

    add example of using get and post for form dialog of products and capture form content and sending as JSON rather than form-url-encoded.

[33mcommit 6b575fd7f4cadf7675c51b47cd05c7a612af14e4[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Fri May 11 10:04:16 2012 +1000

    wip api stuff (with some errors I'll clean up after)

[33mcommit 85ac895bb923a73ba917c576d65990aa8293f32a[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri May 11 00:55:42 2012 +0100

    add CORs filter for backend platform and hook view all offices up to it.

[33mcommit 6f4a47a1ca933bb8255cbda21b8cf2e42a74dad9[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Thu May 10 21:09:37 2012 +0100

    tidy up error handling and services around loan product.

[33mcommit 01adabab4787b84fdf8c4f49dca3b1a1ac2a8485[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu May 10 23:59:12 2012 +1000

    WIP extradata api - hardcoded mifosng-provider url in stretchydata

[33mcommit b016030267a36ac25bb8ef0311a90819287cc48a[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu May 10 13:08:30 2012 +1000

    WIP restructure of extradata api

[33mcommit a6cf21dd42bff95539a32b62e4bc45620df05232[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Thu May 10 13:08:18 2012 +1000

    WIP restructure of extradata api

[33mcommit 008ec86c9ab9b77f141eac733bf7c339f6989737[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed May 9 23:33:33 2012 +0100

    work through cleaner implementation of error handling support for http api for the office resource.

[33mcommit 7ad484bc2e9b1fab4b0e60f6dae68f6b56d4b2d5[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed May 9 14:26:53 2012 +0100

    support offices in resource-oriented way, add better error handling support for unauthenticated user use case.

[33mcommit 226f46663f7e6fc39c93d069021a73281809ecf0[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Wed May 9 19:19:27 2012 +1000

    WIP - list extradata sets

[33mcommit 29a004f74671fc7808bb1d90c197e990fa5c9194[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed May 9 00:12:03 2012 +0100

    update loanproducts resource class with support for create and update through post and put.

[33mcommit 1bda8790a62733cb49cf732a0371e9c8f9eab65d[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue May 8 17:35:00 2012 +0200

    Update README.md

[33mcommit 8cdfb2309f0f53d5010f74fecd00e5e5ae03c4b7[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue May 8 17:31:57 2012 +0200

    Update README.md

[33mcommit f7542e5a8edd36a542248bf65958c4e7bb20a037[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue May 8 16:28:10 2012 +0100

    add support for versioned resource oriented url for loanproducts.

[33mcommit 74621c584ca5a09bb48d3e346a247ec101cba8ef[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Wed May 2 01:31:44 2012 +0200

    Update README.md

[33mcommit ad11415f611098219eb9093cf3e1961ff52c9799[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue May 1 23:55:58 2012 +0100

    add latest dump of updated database for beta 2 from demo.

[33mcommit aa570fb44d9d2128e451e5973fda9cf865a29dbd[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue May 1 21:36:09 2012 +0100

    fix issue with redering multiple loan tabs.

[33mcommit d3d40cabe0dd836143d0c399ee31736ada528a49[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Tue May 1 17:31:32 2012 +0100

    squash changes in interest-calculation-extension branch

[33mcommit ff120f925cd44704e94b01cd7c389ceeabbbe103[m
Author: John Woodlock <john.woodlock@gmail.com>
Date:   Mon Apr 30 11:11:43 2012 +1000

    test commit... also commented out jdepend line on gradle.build cos it gave me build probs (help!)

[33mcommit 5aaf33b09497404cc17a0af3ed897813580ea3f0[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sat Apr 28 02:16:16 2012 +0100

    Revert "add latest dump, add patch for updating currency symbols in reference data. add quick one liner to test daily interest calculation for declining works as per example from shiva of hugo tech."
    
    This reverts commit 5fefc027529b3cefa40a71352d8b56e18da7676d.

[33mcommit 5fefc027529b3cefa40a71352d8b56e18da7676d[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sat Apr 28 02:14:32 2012 +0100

    add latest dump, add patch for updating currency symbols in reference data. add quick one liner to test daily interest calculation for declining works as per example from shiva of hugo tech.

[33mcommit def1c12fb77a4eef14832be8559244daed70b2b1[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Apr 27 23:00:53 2012 +0100

    add currency symbols to reference data.

[33mcommit bb79fda4940837db2d215f97b0d09dde9d13e532[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sun Apr 22 18:29:33 2012 +0100

    remove unneeded resource links from production tomcat context xml file

[33mcommit f9cc4f36860f7fa1654f01945db3d1da4494b86d[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sun Apr 22 18:18:49 2012 +0100

    remove unneeded resource links

[33mcommit 1159e854fc80fd26bfa1f623b1b614a6f1f05590[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sun Apr 22 15:57:45 2012 +0100

    add database structure and latest demo data.

[33mcommit 4d2545aa56a4a209b9b047745177f7f8fbd7183c[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sun Apr 22 16:27:59 2012 +0200

    Update README.md

[33mcommit 39f7d2370ba7eed6678d8a2487aba0a77fda380d[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sun Apr 22 15:02:59 2012 +0200

    Update README.md

[33mcommit f945ad43e894a539fc6daec8940ea786dffec161[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sun Apr 22 14:51:24 2012 +0200

    Update README.md

[33mcommit 34516692c4ee3454c1865a96595480578258fa1b[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Sun Apr 22 12:37:11 2012 +0100

    restructure third party java libraries used in provider and individual lending app.

[33mcommit c1e5474f1e3edf84a511a6b2e1d238f0e15ddda0[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Apr 20 11:50:23 2012 +0100

    add provider project source.

[33mcommit ceb30e80124bf8449675f4abbbbd26db1e52e7e7[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Apr 20 11:49:03 2012 +0100

    add source for individual lending app.

[33mcommit 54189a8d6c43b3517657fa4d9673a9852933d86a[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Apr 20 11:36:32 2012 +0100

    add provider and individual lending apps directoryies and build settings.

[33mcommit a4a703337e21c12bc5af672bd4bf4abe224b8281[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Apr 20 11:24:49 2012 +0100

    add base gradle setting and data project.

[33mcommit cd424598aa3033ed052e147ac2c4df42a192b3ab[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Apr 20 11:21:10 2012 +0100

    update git ignore for gradle java builds.

[33mcommit 32bfd8681e541ffa19abd017c4e30ac8a7fc8bce[m
Author: Keith Woodlock <keithwoodlock@gmail.com>
Date:   Fri Apr 20 02:31:01 2012 -0700

    initial commit

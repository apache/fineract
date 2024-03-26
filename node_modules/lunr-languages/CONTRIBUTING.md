# Contributing to Lunr Languages

## I found a bug

Thanks! Go to the *Issues* section and open a new issue. Please try to provide a detailed example describing the expected and actual outcome.

## I found a bug and have a fix for it

Even better! Open a *Pull request*. Make sure you include everything needed in the pull request (check below)

## I want to add a new language

Wonderful! Here are the changes you need to do, then Open a *Pull request*. Make sure you include everything needed in the pull request (check below)

- [ ] except Japanese, all the `lunr.<language>.js` files are built from `build/lunr.template`
    - [ ] If `lunr.template` fits your needs in terms of code/template, go to `build/build.js` and add your language
        - [ ] You need a stemmer file
        - [ ] You need a stop words file
    - [ ] If `lunr.template` does not fit your needs (for example for non-latin characters), just copy any `lunr.<language>.js` file and start modifying
- [ ] you must also test your new language
    - [ ] go to `test/testdata` and create a new file, then link it to `VersionsAndLanguagesTest.js`
    - [ ] make sure you cover many testcases (wildcard, stemming)
    - [ ] your tests will automatically be run on all the Lunr versions
- [ ] add the new language to `README.md`
- [ ] re-build the files (check section below)
- [ ] open a *Pull request*. Make sure you include everything needed in the pull request (check below)

# Pull request check-list

- [ ] Describe in detail **what you did** and **why you did it**
- [ ] Add tests. The more the better
    - [ ] with at least 1 scenario in case of a bug-fix and 4 scenarios in case of a new language
- [ ] Don't open a Pull request for more unrelated topics. _(eg a bug-fix, a new language, and few changes to stemming for existing languages in a single pull request)_

# Building your own files

The `lunr.<locale>.js` files are the result of a build process that concatenates a stemmer and a stop word list and add functionality to become lunr.js-compatible.
Should you decide to make mass-modifications (add stopwords, change stemming rules, reorganize the code) and build a new set of files, you should do follow these steps:

* `git clone --recursive git://github.com/MihaiValentin/lunr-languages.git` (make sure you use the `--recursive` flag to also clone the repos needed to build `lunr-languages`)
* `cd path/to/lunr-languages`
* `npm install` to install the dependencies needed for building
* change the `build/*.template` files
* run `node build/build.js` to generate the `lunr.<locale>.js` files (and the minified versions as well) and the `lunr.stemmer.support.js` file

var assert = require('assert');

var lunrVersions = [
    {
        version: "0.6.0",
        lunr: "lunr-0.6.0.min"
    }, {
        version: "0.7.0",
        lunr: "lunr-0.7.0.min"
    }, {
        version: "1.0.0",
        lunr: "lunr-1.0.0.min"
    }, {
        version: "2.0.1",
        lunr: "lunr-2.0.1"
    }, {
        version: "2.3.5",
        lunr: "lunr-2.3.5"
    }
    
];

var testDocuments = {
    ar: require('./testdata/ar'),
    de: require('./testdata/de'),
    da: require('./testdata/da'),
    du: require('./testdata/du'),
    es: require('./testdata/es'),
    fi: require('./testdata/fi'),
    fr: require('./testdata/fr'),
    hi: require('./testdata/hi'),
    hu: require('./testdata/hu'),
    it: require('./testdata/it'),
    ja: require('./testdata/ja'),
    jp: require('./testdata/ja'),
    no: require('./testdata/no'),
    pt: require('./testdata/pt'),
    ro: require('./testdata/ro'),
    ru: require('./testdata/ru'),
    sv: require('./testdata/sv'),
    ta: require('./testdata/ta'),
    tr: require('./testdata/tr'),
    th: require('./testdata/th'),
    vi: require('./testdata/vi'),
    zh: require('./testdata/zh')
};

lunrVersions.forEach(function(lunrVersion) {
    describe("Testing Lunr-Languages & Lunr version " + lunrVersion.version, function() {
        describe("should be able to correctly identify words in multi-documents scenarios (eg: en + ru)", function() {
            delete require.cache[require.resolve('./lunr/' + lunrVersion.lunr)]
            var lunr = require('./lunr/' + lunrVersion.lunr);
            require('../lunr.stemmer.support.js')(lunr);
            require('../lunr.ru.js')(lunr);
            require('../lunr.multi.js')(lunr);

            var idxEn = lunr(function () {
                this.field('body');
                this.add({"body": "Этот текст написан на русском.", "id": 1});
                this.add({"body": "This text is written in the English language.", "id": 2});
            });

            var idxRu = lunr(function () {
                this.use(lunr.ru);
                this.field('body');
                this.add({"body": "Этот текст написан на русском.", "id": 1});
                this.add({"body": "This text is written in the English language.", "id": 2});
            });

            var idxMulti = lunr(function () {
                this.use(lunr.multiLanguage('en', 'ru'));
                this.field('body');
                this.add({"body": "Этот текст написан на русском.", "id": 1});
                this.add({"body": "This text is written in the English language.", "id": 2});
            });

            it("should not stem and find 'Русских' in english documents", function() {
                assert.equal(idxEn.search('Русских').length, 0)
            });

            it("should stem and find 'languages' in english documents", function() {
                assert.equal(idxEn.search('languages').length, 1)
            });

            it("should stem and find 'Русских' in russian documents", function() {
                assert.equal(idxRu.search('Русских').length, 1)
            });

            it("should not stem and find 'languages' in russian documents", function() {
                assert.equal(idxRu.search('languages').length, 0)
            });

            it("should stem and find 'Русских' in russian+english documents", function() {
                assert.equal(idxMulti.search('Русских').length, 1)
            });

            it("should stem and find 'languages' in russian+english documents", function() {
                assert.equal(idxMulti.search('languages').length, 1)
            });
        });
        Object.keys(testDocuments).forEach(function(language) {
            describe("should be able to correctly find terms in " + language.toUpperCase() + " correctly", function() {
                // because these tests are asynchronous, we must ensure every load of lunr is fresh
                // so we do not get the previous used languages on it.
                // if we don't do this, when we'll run the test for jp, we'll also have da, de, fr, it languages used
                delete require.cache[require.resolve('./lunr/' + lunrVersion.lunr)];

                var lunr = require('./lunr/' + lunrVersion.lunr);
                require('../lunr.stemmer.support.js')(lunr);
                if (language === 'ja' || language === 'jp') {    // for japanese, we must also load the tinyseg tokenizer
                    require('../tinyseg')(lunr);
                }
                if (language === 'th' || language === 'hi' || language === 'ta') {    // for thai, we must also load the wordcut tokenizer
                    lunr.wordcut = require('../wordcut');
                }
                require('../lunr.' + language + '.js')(lunr);

                var idx = lunr(function () {
                    this.use(lunr[language]);
                    testDocuments[language].fields.forEach(function(field) {
                        this.field(field.name, field.config)
                    }.bind(this));

                    testDocuments[language].documents.forEach(function(doc) {
                        this.add(doc)
                    }.bind(this));
                });

                testDocuments[language].tests.forEach(function(test) {
                    it("should " + test.what.replace('%w', '"' + test.search + '"'), function() {
                        assert.equal(idx.search(test.search).length, test.found)
                    });
                }.bind(this));
            })
        })
    })
});

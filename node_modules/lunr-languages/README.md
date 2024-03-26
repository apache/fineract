Lunr Languages [![npm](https://img.shields.io/npm/v/lunr-languages.svg)](https://www.npmjs.com/package/lunr-languages) [![Bower](https://img.shields.io/bower/v/lunr-languages.svg)]() [![Join the chat at https://gitter.im/lunr-languages/Lobby](https://badges.gitter.im/lunr-languages/Lobby.svg)](https://gitter.im/lunr-languages/Lobby?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge) [![](https://img.shields.io/badge/compatible%20with%20Lunr-0.6.0%20--%3E%202.x-green.svg)](http://lunrjs.com/) [![CircleCI branch](https://img.shields.io/circleci/project/github/MihaiValentin/lunr-languages.svg)](https://circleci.com/gh/MihaiValentin/lunr-languages)
==============

Lunr Languages is a [Lunr](http://lunrjs.com/) addon that helps you search in documents written in the following languages:

* ![](https://raw.githubusercontent.com/madebybowtie/FlagKit/master/Assets/PNG/DE.png) German
* ![](https://raw.githubusercontent.com/madebybowtie/FlagKit/master/Assets/PNG/FR.png) French
* ![](https://raw.githubusercontent.com/madebybowtie/FlagKit/master/Assets/PNG/ES.png) Spanish
* ![](https://raw.githubusercontent.com/madebybowtie/FlagKit/master/Assets/PNG/IT.png) Italian
* ![](https://raw.githubusercontent.com/madebybowtie/FlagKit/master/Assets/PNG/JP.png) Japanese
* ![](https://raw.githubusercontent.com/madebybowtie/FlagKit/master/Assets/PNG/NL.png) Dutch
* ![](https://raw.githubusercontent.com/madebybowtie/FlagKit/master/Assets/PNG/DK.png) Danish
* ![](https://raw.githubusercontent.com/madebybowtie/FlagKit/master/Assets/PNG/PT.png) Portuguese
* ![](https://raw.githubusercontent.com/madebybowtie/FlagKit/master/Assets/PNG/FI.png) Finnish
* ![](https://raw.githubusercontent.com/madebybowtie/FlagKit/master/Assets/PNG/RO.png) Romanian
* ![](https://raw.githubusercontent.com/madebybowtie/FlagKit/master/Assets/PNG/HU.png) Hungarian
* ![](https://raw.githubusercontent.com/madebybowtie/FlagKit/master/Assets/PNG/RU.png) Russian
* ![](https://raw.githubusercontent.com/madebybowtie/FlagKit/master/Assets/PNG/NO.png) Norwegian
* ![](https://raw.githubusercontent.com/madebybowtie/FlagKit/master/Assets/PNG/TH.png) Thai
* ![](https://raw.githubusercontent.com/madebybowtie/FlagKit/master/Assets/PNG/VN.png) Vietnamese
* ![](https://raw.githubusercontent.com/madebybowtie/FlagKit/master/Assets/PNG/IQ.png) Arabic
* ![](https://raw.githubusercontent.com/madebybowtie/FlagKit/master/Assets/PNG/IN.png) Hindi
* ![](https://raw.githubusercontent.com/madebybowtie/FlagKit/master/Assets/PNG/CN.png) Chinese
* ![](https://raw.githubusercontent.com/madebybowtie/FlagKit/master/Assets/PNG/IN.png) Tamil
* [Contribute with a new language](CONTRIBUTING.md)

Lunr Languages is compatible with Lunr version `0.6`, `0.7`, `1.0` and `2.X`.

# How to use

Lunr-languages works well with script loaders (Webpack, requirejs) and can be used in the browser and on the server.

## In a web browser

The following example is for the German language (de).

Add the following JS files to the page:

```html
<script src="lunr.js"></script> <!-- lunr.js library -->
<script src="lunr.stemmer.support.js"></script>
<script src="lunr.de.js"></script> <!-- or any other language you want -->
```

then, use the language in when initializing lunr:

```javascript
var idx = lunr(function () {
  // use the language (de)
  this.use(lunr.de);
  // then, the normal lunr index initialization
  this.field('title', { boost: 10 });
  this.field('body');
  // now you can call this.add(...) to add documents written in German
});
```

That's it. Just add the documents and you're done. When searching, the language stemmer and stopwords list will be the one you used.

## In a web browser, with RequireJS

Add `require.js` to the page:

```html
<script src="lib/require.js"></script>
```

then, use the language in when initializing lunr:

```javascript
require(['lib/lunr.js', '../lunr.stemmer.support.js', '../lunr.de.js'], function(lunr, stemmerSupport, de) {
  // since the stemmerSupport and de add keys on the lunr object, we'll pass it as reference to them
  // in the end, we will only need lunr.
  stemmerSupport(lunr); // adds lunr.stemmerSupport
  de(lunr); // adds lunr.de key

  // at this point, lunr can be used
  var idx = lunr(function () {
  // use the language (de)
  this.use(lunr.de);
  // then, the normal lunr index initialization
  this.field('title', { boost: 10 })
  this.field('body')
  // now you can call this.add(...) to add documents written in German
  });
});
```

# With node.js

```javascript
var lunr = require('./lib/lunr.js');
require('./lunr.stemmer.support.js')(lunr);
require('./lunr.de.js')(lunr); // or any other language you want

var idx = lunr(function () {
  // use the language (de)
  this.use(lunr.de);
  // then, the normal lunr index initialization
  this.field('title', { boost: 10 })
  this.field('body')
  // now you can call this.add(...) to add documents written in German
});
```

# Indexing multi-language content

If your documents are written in more than one language, you can enable multi-language indexing. This ensures every word is properly trimmed and stemmed, every stopword is removed, and no words are lost (indexing in just one language would remove words from every other one.)

```javascript
var lunr = require('./lib/lunr.js');
require('./lunr.stemmer.support.js')(lunr);
require('./lunr.ru.js')(lunr);
require('./lunr.multi.js')(lunr);

var idx = lunr(function () {
  // the reason "en" does not appear above is that "en" is built in into lunr js
  this.use(lunr.multiLanguage('en', 'ru'));
  // then, the normal lunr index initialization
  // ...
});
```

You can combine any number of supported languages this way. The corresponding lunr language scripts must be loaded (English is built in).

If you serialize the index and load it in another script, you'll have to initialize the multi-language support in that script, too, like this:

```javascript
lunr.multiLanguage('en', 'ru');
var idx = lunr.Index.load(serializedIndex);
```

# How to add a new language

Check the [Contributing](CONTRIBUTING.md) section

# How does Lunr Languages work?

Searching inside documents is not as straight forward as using `indexOf()`, since there are many things to consider in order to get quality search results:
* **Tokenization**
    * Given a string like *"Hope you like using Lunr Languages!"*, the tokenizer would split it into individual words, becoming an array like `['Hope', 'you', 'like', 'using', 'Lunr', 'Languages!']`
    * Though it seems a trivial task for Latin characters (just splitting by the space), it gets more complicated for languages like Japanese. Lunr Languages has this included for the Japanese language.
* **Trimming**
    * After tokenization, trimming ensures that the words contain *just* what is needed in them. In our example above, the trimmer would convert `Languages!` into `Languages`
    * So, the trimmer basically removes special characters that do not add value for the search purpose.
* **Stemming**
    * What happens if our text contains the word `consignment` but we want to search for `consigned`? It should find it, since its meaning is the same, only the form is different.
    * A stemmer extracts the root of words that can have many forms and stores it in the index. Then, any search is also stemmed and searched in the index.
    * Lunr Languages does stemming for all the included languages, so you can capture all the forms of words in your documents.
* **Stop words**
    * There's no point in adding or searching words like `the`, `it`, `so`, etc. These words are called *Stop words*
    * Stop words are removed so your index will only contain meaningful words.
    * Lunr Languages includes stop words for all the included languages.

# Technical details & Credits

I've created this project by compiling and wrapping stemmers toghether with stop words from various sources ([including users contributions](https://github.com/MihaiValentin/lunr-languages/pulls?q=is%3Apr)) so they can be directly used with all the current versions of Lunr.

* <https://github.com/fortnightlabs/snowball-js> (the stemmers for all languages, ported from snowball-js)
* <https://github.com/brenes/stopwords-filter> (the stop words list for the other languages)
* <http://chasen.org/~taku/software/TinySegmenter/> (the tinyseg Tiny Segmente Japanese tokenizer)

I am providing code in the repository to you under an [open source license](LICENSE). Because this is my personal repository, the license you receive to my code is from me and not my employer (Facebook)

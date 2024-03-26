'use strict'

const lunr = require('lunr')
const cheerio = require('cheerio')
const { decode } = require('html-entities')

/**
 * Generate a Lunr index.
 *
 * Iterates over the specified pages and creates a Lunr index.
 *
 * @memberof lunr-extension
 *
 * @param {Object} playbook - The configuration object for Antora.
 * @param {Object} contentCatalog - The Antora content catalog, with pages and metadata.
 * @param {Object} [config={}] - Configuration options
 * @param {Boolean} config.indexLatestOnly - If true, only index the latest version of any given page.
 * @param {Array<String>} config.languages - List of index languages
 * @param {Object} config.logger - Logger to use
 * @typedef {Object} SearchIndexData
 * @property {lunr.Index} index - a Lunr index
 * @property {Object} store - the documents store
 * @returns {SearchIndexData} A data object that contains the Lunr index and documents store
 */
function generateIndex (playbook, contentCatalog, { indexLatestOnly = false, languages = ['en'], logger } = {}) {
  if (!logger) logger = process.env.NODE_ENV === 'test' ? { info: () => undefined } : console

  logger.info('Building search index with the language(s): %s', languages.join(', '))

  // Select indexable pages
  const pages = contentCatalog.getPages((page) => {
    if (!page.out || page.asciidoc?.attributes?.noindex != null) return
    if (indexLatestOnly) {
      const component = contentCatalog.getComponent(page.src.component)
      if (contentCatalog.getComponentVersion(component, page.src.version) !== component.latest) return
    }
    return true
  })
  if (!pages.length) return {}

  // Use short numerical identifiers (as ref) to keep the Lunr index as small as possible.
  // Since it's an identifier (and not an index) we start at 1.
  let id = 1
  // Extract document objects from indexable pages
  const documents = pages.reduce((accum, page) => {
    const $ = cheerio.load(page.contents)
    // Only index page if not marked as "noindex" by "robots" meta tag
    if (!$('meta[name=robots][content=noindex]').length) {
      accum.push({ id: id++, ...extractIndexContent(page, $) })
    }
    return accum
  }, [])

  if (languages.length > 1 || !languages.includes('en')) {
    if (languages.length > 1 && !lunr.multiLanguage) {
      // required, otherwise lunr.multiLanguage will be undefined
      require('lunr-languages/lunr.multi')(lunr)
    }
    // required, to load additional languages
    require('lunr-languages/lunr.stemmer.support')(lunr)
    languages.forEach((language) => {
      if (language === 'ja' && !lunr.TinySegmenter) {
        require('lunr-languages/tinyseg')(lunr) // needed for Japanese Support
      }
      if (language === 'th' && !lunr.wordcut) {
        lunr.wordcut = require('lunr-languages/wordcut') // needed for Thai support
      }
      if (language !== 'en' && !lunr[language]) {
        require(`lunr-languages/lunr.${language}`)(lunr)
      }
    })
  }

  // Map of Lunr ref (id) to document
  const store = {
    documents: {},
    components: {},
  }

  // Construct the Lunr index from the extracted content
  const index = lunr(function () {
    if (languages.length > 1) {
      this.use(lunr.multiLanguage(...languages))
    } else if (!languages.includes('en')) {
      this.use(lunr[languages[0]])
    }
    this.ref('id')
    this.field('title', { boost: 10 })
    this.field('name')
    this.field('text')
    this.field('component')
    documents.forEach((doc) => {
      doc.titles.forEach((title) => {
        this.add({ id: `${doc.id}-${title.id}`, title: title.text })
      })
      this.add(doc)
      store.documents[doc.id] = doc
    })
  })

  const componentVersions = {}
  const components = contentCatalog.getComponents()
  for (const component of components) {
    for (const version of component.versions) {
      componentVersions[`${component.name}/${version.version}`] = version
    }
  }
  store.componentVersions = componentVersions

  return { index, store }
}

/**
 * Extract the index content for a given page.
 * @param {Object<Page>} page Full text input to clean irrelevant material from.
 * @param {*} $ Cheerio representation of the page.
 * @returns {Object} Indexable content for a given page.
 */
function extractIndexContent (page, $) {
  // Fetch just the article content, so we don't index the TOC and other on-page text
  // Remove any found headings, to improve search results
  const article = $('article.doc')
  const $h1 = $('h1', article)
  const documentTitle = $h1.first().text()
  $h1.remove()
  const titles = []
  let id = 1
  $('h2,h3,h4,h5,h6', article).each(function () {
    const $title = $(this)
    titles.push({
      text: $title.text(),
      hash: $title.attr('id'),
      id: id++,
    })
    $title.remove()
  })

  // don't index navigation elements for pagination on each page
  // as these are the titles of other pages and it would otherwise pollute the index.
  $('nav.pagination', article).each(function () {
    $(this).remove()
  })

  // Pull the text from the article, and convert entities
  let text = article.text()
  // Decode HTML
  text = decode(text)
  // Strip HTML tags
  text = text
    .replace(/(<([^>]+)>)/gi, '')
    .replace(/\n/g, ' ')
    .replace(/\r/g, ' ')
    .replace(/\s+/g, ' ')
    .trim()

  // Return the indexable content, organized by type
  return {
    text: text,
    title: documentTitle,
    component: page.src.component,
    version: page.src.version,
    name: page.src.stem,
    url: page.pub.url,
    titles: titles, // TODO get title id to be able to use fragment identifier
  }
}

// Helper function allowing Antora to create a site asset containing the index
function createIndexFile (index) {
  return {
    mediaType: 'application/javascript',
    contents: Buffer.from(`antoraSearch.initSearch(lunr, ${JSON.stringify(index)})`),
    src: { stem: 'search-index' },
    out: { path: 'search-index.js' },
    pub: { url: '/search-index.js', rootPath: '' },
  }
}

module.exports = generateIndex
module.exports.createIndexFile = createIndexFile

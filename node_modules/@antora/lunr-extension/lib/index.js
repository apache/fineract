'use strict'

// The name of the package in order to give the Antora logger a useful name
const { name: packageName } = require('../package.json')
const fs = require('fs')
const { promises: fsp } = fs
const generateIndex = require('./generate-index')
const LazyReadable = require('./lazy-readable')
const MultiFileReadStream = require('./multi-file-read-stream')
const ospath = require('path')
const template = require('./template')

/**
 * Lunr integration for an Antora documentation site.
 *
 * @module lunr-extension
 */
function register ({ config: { languages, indexLatestOnly, snippetLength = 100, ...unknownOptions } }) {
  const logger = this.getLogger(packageName)

  if (Object.keys(unknownOptions).length) {
    const keys = Object.keys(unknownOptions)
    throw new Error(`Unrecognized option${keys.length > 1 ? 's' : ''} specified for ${packageName}: ${keys.join(', ')}`)
  }

  this.on('uiLoaded', async ({ playbook, uiCatalog }) => {
    playbook.env.SITE_SEARCH_PROVIDER = 'lunr'
    const uiOutputDir = playbook.ui.outputDir
    vendorJsFile(uiCatalog, logger, uiOutputDir, 'lunr/lunr.min.js', 'lunr.js')
    const otherLanguages = (languages || []).filter((it) => it !== 'en')
    if (otherLanguages.length) {
      playbook.env.SITE_SEARCH_LANGUAGES = otherLanguages.join(',')
      const languageRequires = []
      languageRequires.push('lunr-languages/lunr.stemmer.support.js')
      if (otherLanguages.includes('ja') || otherLanguages.includes('jp')) {
        // needed for Japanese Support
        languageRequires.push('lunr-languages/tinyseg.js')
      }
      if (otherLanguages.includes('th')) {
        // needed for Thai support
        languageRequires.push('lunr-languages/wordcut.js')
      }
      otherLanguages.map((lang) => languageRequires.push(`lunr-languages/lunr.${lang}.js`))
      vendorJsFile(uiCatalog, logger, uiOutputDir, languageRequires, 'lunr-languages.js')
    }
    assetFile(uiCatalog, logger, uiOutputDir, 'css', 'search.css')
    assetFile(uiCatalog, logger, uiOutputDir, 'js', 'search-ui.js')
    const searchScriptsPartialPath = 'partials/search-scripts.hbs'
    if (uiCatalog.findByType('partial').some(({ path }) => path === searchScriptsPartialPath)) return
    const searchScriptsPartialFilepath = ospath.join(__dirname, '../data', searchScriptsPartialPath)
    uiCatalog.addFile({
      contents: Buffer.from(template(await fsp.readFile(searchScriptsPartialFilepath, 'utf8'), { snippetLength })),
      path: searchScriptsPartialPath,
      stem: 'search-scripts',
      type: 'partial',
    })
  })

  this.on('beforePublish', ({ playbook, siteCatalog, contentCatalog }) => {
    delete playbook.env.SITE_SEARCH_PROVIDER
    delete playbook.env.SITE_SEARCH_LANGUAGES
    const index = generateIndex(playbook, contentCatalog, { indexLatestOnly, languages, logger })
    siteCatalog.addFile(generateIndex.createIndexFile(index))
  })
}

function assetFile (
  uiCatalog,
  logger,
  uiOutputDir,
  assetDir,
  basename,
  assetPath = assetDir + '/' + basename,
  createContents = () => new LazyReadable(() => fs.createReadStream(ospath.join(__dirname, '../data', assetPath))),
  overwrite = false
) {
  const outputDir = uiOutputDir + '/' + assetDir
  const file = uiCatalog.findByType('asset').some(({ path }) => path === assetPath)
  if (file) {
    if (overwrite) {
      logger.warn(`Please remove the following file from your UI since it is managed by ${packageName}: ${assetPath}`)
      Object.defineProperty(file, 'contents', { get: () => createContents() })
    } else {
      logger.info(`The following file already exists in your UI: ${assetPath}, skipping`)
    }
  } else {
    const file = uiCatalog.addFile({
      type: 'asset',
      path: assetPath,
      out: { dirname: outputDir, path: outputDir + '/' + basename, basename },
    })
    Object.defineProperty(file, 'contents', { get: () => createContents() })
  }
}

function vendorJsFile (uiCatalog, logger, uiOutputDir, requireRequest, basename = requireRequest.split('/').pop()) {
  let createContents
  if (Array.isArray(requireRequest)) {
    const filepaths = requireRequest.map(require.resolve)
    createContents = () => new LazyReadable(() => new MultiFileReadStream(filepaths))
  } else {
    const filepath = require.resolve(requireRequest)
    createContents = () => new LazyReadable(() => fs.createReadStream(filepath))
  }
  const jsVendorDir = 'js/vendor'
  assetFile(uiCatalog, logger, uiOutputDir, jsVendorDir, basename, jsVendorDir + '/' + basename, createContents)
}

module.exports = { generateIndex, register }

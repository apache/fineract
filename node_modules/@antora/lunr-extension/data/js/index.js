/* global CustomEvent, globalThis */
'use strict'

import { buildHighlightedText, findTermPosition } from './search-result-highlighting.mjs'

const config = document.getElementById('search-ui-script').dataset
const snippetLength = parseInt(config.snippetLength || 100, 10)
const siteRootPath = config.siteRootPath || ''
appendStylesheet(config.stylesheet)
const searchInput = document.getElementById('search-input')
const searchResultContainer = document.createElement('div')
searchResultContainer.classList.add('search-result-dropdown-menu')
searchInput.parentNode.appendChild(searchResultContainer)
const facetFilterInput = document.querySelector('#search-field input[type=checkbox][data-facet-filter]')

function appendStylesheet (href) {
  if (!href) return
  const link = document.createElement('link')
  link.rel = 'stylesheet'
  link.href = href
  document.head.appendChild(link)
}

function highlightPageTitle (title, terms) {
  const positions = getTermPosition(title, terms)
  return buildHighlightedText(title, positions, snippetLength)
}

function highlightSectionTitle (sectionTitle, terms) {
  if (sectionTitle) {
    const text = sectionTitle.text
    const positions = getTermPosition(text, terms)
    return buildHighlightedText(text, positions, snippetLength)
  }
  return []
}

function highlightText (doc, terms) {
  const text = doc.text
  const positions = getTermPosition(text, terms)
  return buildHighlightedText(text, positions, snippetLength)
}

function getTermPosition (text, terms) {
  const positions = terms
    .map((term) => findTermPosition(globalThis.lunr, term, text))
    .filter((position) => position.length > 0)
    .sort((p1, p2) => p1.start - p2.start)

  if (positions.length === 0) {
    return []
  }
  return positions
}

function highlightHit (searchMetadata, sectionTitle, doc) {
  const terms = {}
  for (const term in searchMetadata) {
    const fields = searchMetadata[term]
    for (const field in fields) {
      terms[field] = [...(terms[field] || []), term]
    }
  }
  return {
    pageTitleNodes: highlightPageTitle(doc.title, terms.title || []),
    sectionTitleNodes: highlightSectionTitle(sectionTitle, terms.title || []),
    pageContentNodes: highlightText(doc, terms.text || []),
  }
}

function createSearchResult (result, store, searchResultDataset) {
  let currentComponent
  result.forEach(function (item) {
    const ids = item.ref.split('-')
    const docId = ids[0]
    const doc = store.documents[docId]
    let sectionTitle
    if (ids.length > 1) {
      const titleId = ids[1]
      sectionTitle = doc.titles.filter(function (item) {
        return String(item.id) === titleId
      })[0]
    }
    const metadata = item.matchData.metadata
    const highlightingResult = highlightHit(metadata, sectionTitle, doc)
    const componentVersion = store.componentVersions[`${doc.component}/${doc.version}`]
    if (componentVersion !== undefined && currentComponent !== componentVersion) {
      const searchResultComponentHeader = document.createElement('div')
      searchResultComponentHeader.classList.add('search-result-component-header')
      const { title, displayVersion } = componentVersion
      const componentVersionText = `${title}${doc.version && displayVersion ? ` ${displayVersion}` : ''}`
      searchResultComponentHeader.appendChild(document.createTextNode(componentVersionText))
      searchResultDataset.appendChild(searchResultComponentHeader)
      currentComponent = componentVersion
    }
    searchResultDataset.appendChild(createSearchResultItem(doc, sectionTitle, item, highlightingResult))
  })
}

function createSearchResultItem (doc, sectionTitle, item, highlightingResult) {
  const documentTitle = document.createElement('div')
  documentTitle.classList.add('search-result-document-title')
  highlightingResult.pageTitleNodes.forEach(function (node) {
    let element
    if (node.type === 'text') {
      element = document.createTextNode(node.text)
    } else {
      element = document.createElement('span')
      element.classList.add('search-result-highlight')
      element.innerText = node.text
    }
    documentTitle.appendChild(element)
  })
  const documentHit = document.createElement('div')
  documentHit.classList.add('search-result-document-hit')
  const documentHitLink = document.createElement('a')
  documentHitLink.href = siteRootPath + doc.url + (sectionTitle ? '#' + sectionTitle.hash : '')
  documentHit.appendChild(documentHitLink)
  if (highlightingResult.sectionTitleNodes.length > 0) {
    const documentSectionTitle = document.createElement('div')
    documentSectionTitle.classList.add('search-result-section-title')
    documentHitLink.appendChild(documentSectionTitle)
    highlightingResult.sectionTitleNodes.forEach(function (node) {
      let element
      if (node.type === 'text') {
        element = document.createTextNode(node.text)
      } else {
        element = document.createElement('span')
        element.classList.add('search-result-highlight')
        element.innerText = node.text
      }
      documentSectionTitle.appendChild(element)
    })
  }
  highlightingResult.pageContentNodes.forEach(function (node) {
    let element
    if (node.type === 'text') {
      element = document.createTextNode(node.text)
    } else {
      element = document.createElement('span')
      element.classList.add('search-result-highlight')
      element.innerText = node.text
    }
    documentHitLink.appendChild(element)
  })
  const searchResultItem = document.createElement('div')
  searchResultItem.classList.add('search-result-item')
  searchResultItem.appendChild(documentTitle)
  searchResultItem.appendChild(documentHit)
  searchResultItem.addEventListener('mousedown', function (e) {
    e.preventDefault()
  })
  return searchResultItem
}

function createNoResult (text) {
  const searchResultItem = document.createElement('div')
  searchResultItem.classList.add('search-result-item')
  const documentHit = document.createElement('div')
  documentHit.classList.add('search-result-document-hit')
  const message = document.createElement('strong')
  message.innerText = 'No results found for query "' + text + '"'
  documentHit.appendChild(message)
  searchResultItem.appendChild(documentHit)
  return searchResultItem
}

function clearSearchResults (reset) {
  if (reset === true) searchInput.value = ''
  searchResultContainer.innerHTML = ''
}

function filter (result, documents) {
  const facetFilter = facetFilterInput && facetFilterInput.checked && facetFilterInput.dataset.facetFilter
  if (facetFilter) {
    const [field, value] = facetFilter.split(':')
    return result.filter((item) => {
      const ids = item.ref.split('-')
      const docId = ids[0]
      const doc = documents[docId]
      return field in doc && doc[field] === value
    })
  }
  return result
}

function search (index, documents, queryString) {
  // execute an exact match search
  let query
  let result = filter(
    index.query(function (lunrQuery) {
      const parser = new globalThis.lunr.QueryParser(queryString, lunrQuery)
      parser.parse()
      query = lunrQuery
    }),
    documents
  )
  if (result.length > 0) {
    return result
  }
  // no result, use a begins with search
  result = filter(
    index.query(function (lunrQuery) {
      lunrQuery.clauses = query.clauses.map((clause) => {
        if (clause.presence !== globalThis.lunr.Query.presence.PROHIBITED) {
          clause.term = clause.term + '*'
          clause.wildcard = globalThis.lunr.Query.wildcard.TRAILING
          clause.usePipeline = false
        }
        return clause
      })
    }),
    documents
  )
  if (result.length > 0) {
    return result
  }
  // no result, use a contains search
  result = filter(
    index.query(function (lunrQuery) {
      lunrQuery.clauses = query.clauses.map((clause) => {
        if (clause.presence !== globalThis.lunr.Query.presence.PROHIBITED) {
          clause.term = '*' + clause.term + '*'
          clause.wildcard = globalThis.lunr.Query.wildcard.LEADING | globalThis.lunr.Query.wildcard.TRAILING
          clause.usePipeline = false
        }
        return clause
      })
    }),
    documents
  )
  return result
}

function searchIndex (index, store, text) {
  clearSearchResults(false)
  if (text.trim() === '') {
    return
  }
  const result = search(index, store.documents, text)
  const searchResultDataset = document.createElement('div')
  searchResultDataset.classList.add('search-result-dataset')
  searchResultContainer.appendChild(searchResultDataset)
  if (result.length > 0) {
    createSearchResult(result, store, searchResultDataset)
  } else {
    searchResultDataset.appendChild(createNoResult(text))
  }
}

function confineEvent (e) {
  e.stopPropagation()
}

function debounce (func, wait, immediate) {
  let timeout
  return function () {
    const context = this
    const args = arguments
    const later = function () {
      timeout = null
      if (!immediate) func.apply(context, args)
    }
    const callNow = immediate && !timeout
    clearTimeout(timeout)
    timeout = setTimeout(later, wait)
    if (callNow) func.apply(context, args)
  }
}

function enableSearchInput (enabled) {
  if (facetFilterInput) {
    facetFilterInput.disabled = !enabled
  }
  searchInput.disabled = !enabled
  searchInput.title = enabled ? '' : 'Loading index...'
}

function isClosed () {
  return searchResultContainer.childElementCount === 0
}

function executeSearch (index) {
  const debug = 'URLSearchParams' in globalThis && new URLSearchParams(globalThis.location.search).has('lunr-debug')
  const query = searchInput.value
  try {
    if (!query) return clearSearchResults()
    searchIndex(index.index, index.store, query)
  } catch (err) {
    if (err instanceof globalThis.lunr.QueryParseError) {
      if (debug) {
        console.debug('Invalid search query: ' + query + ' (' + err.message + ')')
      }
    } else {
      console.error('Something went wrong while searching', err)
    }
  }
}

function toggleFilter (e, index) {
  searchInput.focus()
  if (!isClosed()) {
    executeSearch(index)
  }
}

export function initSearch (lunr, data) {
  const start = performance.now()
  const index = { index: lunr.Index.load(data.index), store: data.store }
  enableSearchInput(true)
  searchInput.dispatchEvent(
    new CustomEvent('loadedindex', {
      detail: {
        took: performance.now() - start,
      },
    })
  )
  searchInput.addEventListener(
    'keydown',
    debounce(function (e) {
      if (e.key === 'Escape' || e.key === 'Esc') return clearSearchResults(true)
      executeSearch(index)
    }, 100)
  )
  searchInput.addEventListener('click', confineEvent)
  searchResultContainer.addEventListener('click', confineEvent)
  if (facetFilterInput) {
    facetFilterInput.parentElement.addEventListener('click', confineEvent)
    facetFilterInput.addEventListener('change', (e) => toggleFilter(e, index))
  }
  document.documentElement.addEventListener('click', clearSearchResults)
}

// disable the search input until the index is loaded
enableSearchInput(false)

'use strict'

const { PassThrough } = require('stream')

// adapted from https://github.com/jpommerening/node-lazystream/blob/master/lib/lazystream.js | license: MIT
class LazyReadable extends PassThrough {
  constructor (fn, options) {
    super(options)
    this._read = function () {
      delete this._read // restores original method
      fn.call(this, options).on('error', this.emit.bind(this, 'error')).pipe(this)
      return this._read.apply(this, arguments)
    }
    this.emit('readable')
  }
}

module.exports = LazyReadable

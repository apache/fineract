'use strict'

module.exports = (string, vars) => string.replace(/\${(.+?)}/g, (_, name) => vars[name])

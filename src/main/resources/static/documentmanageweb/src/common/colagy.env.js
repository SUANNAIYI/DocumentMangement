var merge = require('webpack-merge')
var prodEnv = require('./prod.env')

module.exports = merge(prodEnv, {
  NODE_ENV: '"development"',
  BASE_URL: '"/api"',
  // onlyoffice document-server 服务端口
  // 编辑地址
  ONLYOFFICE_DOCUMENT_HOST: '"http://121.5.54.92"',
  // 预览地址
  ONLYOFFICE_DOCUMENT_PREVIEW_HOST: '"http://121.5.54.92"',
  ONLYOFFICE_DOCUMENT_PORT: 8100,
  // onlyoffice前端编辑器地址
  ONLYOFFICE_DOCUMENT_URL: '"/web-apps/apps/api/documents/api.js"'
})

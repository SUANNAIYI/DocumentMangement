module.exports = {
  lintOnSave: false,
  devServer: {
    port: '8081',
    proxy: {
      '/api': {
        target: 'http://192.168.8.108:8080',
        changeOrigin: true
      }
    }
  }
}

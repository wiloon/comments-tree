module.exports = {
  outputDir: './dist',
  devServer: {
    proxy: {
      '/api': {
        target: 'http://localhost:8081',
        ws: true,
        changeOrigin: true,
        pathRewrite: {
          '^/api': ''
        }
      }
    }
  },
  transpileDependencies: [
    'vuetify'
  ],
  productionSourceMap: false
}

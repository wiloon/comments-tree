module.exports = {
  publicPath: '',
  outputDir: './',
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

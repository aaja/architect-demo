1、在项目根目录下，新建一个vue.config.js文件（如果没有的话）
   内容为：
    
    module.exports = {
      // 基本路径
      publicPath: './',
      // 输出文件目录
      outputDir: 'dist',
      configureWebpack: {
        externals: {
        }
      }
    }

    重要就是配置对publicPath,我的项目美哟与部署在根目录下，就
    需要在publicPath:上加一级目录，如下：
    publicPath: process.env.NODE_ENV === "development" ? './' : '/dist',
    
    重新npm run build，如果打开还是空白页，进行第二部操作
2、找到src/router/index.js文件
    
    const router = new VueRouter({
      mode: "hash",
      routes
    })
    
    将mode的history改成hash
    再次build，就OK啦！
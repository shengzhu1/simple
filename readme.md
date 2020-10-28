# 想做的事情
## 简化开发流程
### 网络请求简化
## 编写kt常用代码库 50%
## webview封装
## 组件化
## 头条屏幕适配方案 
```java
attachBaseContext 修改resources.getDisplayMetrics
final float targetDensity = metrics.widthPixels / 360;
final float targetScaledDensity = targetDensity * (metrics.scaledDensity / metrics.density);
final float targetDensityDpi = (int)(160 * targetDensity);
// 部分情况下，修改配置会不生效，需要天添加监听
Application#registerComponentCallbacks 注册下 onConfigurationChanged 

还有webview展示问题
```
## Android版本适配
## 权限适配
## 点击加默认效果
## 添加防止重复点击
## 各种独立工具开发
### 字体缩放
### 免重启切换主题

# 视频去水印

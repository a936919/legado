# 吾爱读书 
尝试了近50款阅读APP，找不到一款完全满意的。完美主义啊啊啊啊！  
fork了大佬kunfei的legado代码。大佬这个软件真的牛！  
整理了一些不太习惯的地方，抽时间调整修改一下。不定期推进进度。   
工作上已经几年不需要写代码了。上次写代码还是给暗黑3的外挂改进优化。开始学习andorid studio，学习gitHub，学习kotlin，整体不是太复杂，发现现在的语言啊、环境啊什么的越来越傻瓜化了，上手很方便。于是顺便买了本kotlin的参考书还在路上。
# 优化计划 
## 阅读界面
#### 阅读界面
* 标题配置为黑体
* （已实现）页眉页脚颜色变浅
* （已实现）时间和电量合并
* （已实现）更改默认封面图片和字体样式
#### 排版
* 冒号不断行
#### 阅读配置上BAR
* 背景色同步阅读背景色
* 字体颜色
* 图标颜色
* 去除章节文字，调整链接文字位置
* 状态栏反色处理
* 更多设置栏背景颜色
* 更多设置栏文字颜色
#### 阅读配置下BAR
* 文字颜色-暂定灰色
* 多个窗口文字颜色
* 配置阅读主题时更新窗口颜色
* 背景色增量-确定颜色含义，确定如何增量的处理
* 配置阅读主题背景色
* （已实现）阅读界面沉浸式设置配色
## 软件界面
#### 书架
* 不同分组布局独立设置
#### 阅读记录
* 阅读记录超过10分钟以上才显示
* 记录每本书的每天阅读记录
* 阅读记录在书籍信息中展示
* 阅读记录窗口展示美化，内容补充
## WEB
* Web支持两端对齐
## EPUB
* 修复卫斯理全集内容错误
* 修复妖艳人生目录解析失败
* 测试其他本地EPUB书籍
* 引用解析
## 其他
#### 默认配置
* 默认的字体目录更改
* 默认访问路径改为Books
## kotlin学习
* 读完参考书
* 基本语法
* 菜单实现
## legado原内容

[![Commitizen friendly](https://img.shields.io/badge/commitizen-friendly-brightgreen.svg)](http://commitizen.github.io/cz-cli/)

### 阅读3.0
* [书源规则](https://alanskycn.gitee.io/teachme/)
* [更新日志](/app/src/main/assets/updateLog.md)
* [帮助文档](/app/src/main/assets/help/appHelp.md)

![image](https://github.com/gedoor/gedoor.github.io/blob/master/images/%E9%98%85%E8%AF%BB%E7%AE%80%E4%BB%8B1.jpg)
![image](https://github.com/gedoor/gedoor.github.io/blob/master/images/%E9%98%85%E8%AF%BB%E7%AE%80%E4%BB%8B2.jpg)
![image](https://github.com/gedoor/gedoor.github.io/blob/master/images/%E9%98%85%E8%AF%BB%E7%AE%80%E4%BB%8B3.jpg)
![image](https://github.com/gedoor/gedoor.github.io/blob/master/images/%E9%98%85%E8%AF%BB%E7%AE%80%E4%BB%8B4.jpg)
![image](https://github.com/gedoor/gedoor.github.io/blob/master/images/%E9%98%85%E8%AF%BB%E7%AE%80%E4%BB%8B5.jpg)
![image](https://github.com/gedoor/gedoor.github.io/blob/master/images/%E9%98%85%E8%AF%BB%E7%AE%80%E4%BB%8B6.jpg)

#### 阅读API
阅读3.0 提供了2种方式的API：`Web方式`和`Content Provider方式`。您可以在[这里](api.md)根据需要自行调用。 

### 免责声明
https://gedoor.github.io/MyBookshelf/disclaimer.html

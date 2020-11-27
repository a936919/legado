# 吾爱读书 
尝试了近50款阅读APP（有比我更多的人吗？），找不到一款完全满意的。完美主义啊啊啊啊！  
实在不行自己来吧，无奈下fork了大佬kunfei的开源阅读代码legado。（大佬这个软件真的牛！）  
工作上近几年不需要写代码了。好像上次写的代码还是给暗黑3外挂改进打怪算法。开始配置andorid studio，熟悉gitHub，学习kotlin。尝试下来整体不太复杂，发现现在的语言、环境什么的越来越傻瓜化了，上手很方便。于是顺便买了本kotlin的参考书还在路上。
根据个人习惯整理了一些待优化的点列在下面，抽时间调整修改，简单的先做，复杂的慢慢做，实在实现不了的就再说。不定期推进。   
# 优化计划 
## 阅读界面
#### 阅读界面
* 阅读标题字体强制为默认黑体
* （已实现）页眉页脚颜色变浅
* （已实现）时间和电量合
#### 排版
* 冒号不断行
* 支持句号逗号实现标点悬挂
#### 阅读配置上BAR
* 背景色同步阅读背景色
* 字体颜色更改
* 图标颜色更改
* 去除章节文字，调整链接文字位置
* 状态栏反色处理
* 更多设置栏背景颜色配置
* 更多设置栏文字颜色配置
#### 阅读配置下BAR
* 文字颜色改色-暂定灰色
* 多个窗口文字颜色同步改色
* 配置阅读主题时更新窗口颜色
* 背景色增量-确定颜色含义，确定如何增量的处理
* 配置阅读主题背景色同步改色
* （已实现）阅读界面沉浸式设置配色，与底色一致或相近，支持配置，朗读，自动翻页，排版配置，更多配置这几个窗口。加大部分阴影
## 软件界面
#### 书架
* 不同分组布局独立设置
* 加速默认封面的字体绘制速度
* （已实现）更改默认封面图片和字体样式
#### 底栏
* 底栏图标支持渐变色
#### 阅读记录
* 阅读记录超过10分钟以上才显示
* 记录每本书的每天阅读记录
* 阅读记录在书籍信息中展示
* 每天、每周、每年阅读时长统计与展示
* 阅读记录窗口展示美化，内容补充
## WEB
* Web支持两端对齐
* Web支持默认封面，与本地默认封面保持一致
## EPUB
* 修复卫斯理全集内容错误
* 修复妖艳人生目录解析失败
* 测试其他本地EPUB书籍
* 引用解析。
* 引用弹出展示
* 增加段落笔记功能
## WEBDAV
* 支持访问坚果云下载本地书
* 支持在坚果云进行文件管理操作
* 支持坚果云文件排序
* 支持本地书第一次阅读自动上传到坚果云
## 阅读记录云同步
* 支持手动上传阅读记录
* 除默认记录外，支持多条阅读记录，记录设备名，上传时间，章节名信息
* 支持在目录访问页获取阅读记录
* 更准确的同步信息上传策略
* 更准确的同步信息下载策略
* WEB端也支持上述功能
## 其他
#### 默认配置
* 默认的字体目录更改
* 默认访问路径改为Books
* 其他配置默认更改
* 去除主题可配置，只支持最佳的主题
## kotlin学习
* 读完参考书
* 熟悉legado代码整体框架
* 熟悉legado各模块代码
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

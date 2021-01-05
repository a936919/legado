# 吾爱读书 
#### 折腾啊折腾
手机常驻近50款阅读APP，找不到一款完全满意的。完美主义啊啊啊啊！

实在不行自己来吧，于是fork了大佬kunfei的开源阅读代码legado，打算改改自用。（大佬这个软件真的牛！）

工作上近几年不需要写代码了。好像上次写的代码还是给暗黑3外挂改进打怪算法。开始配置andorid studio，熟悉gitHub，学习kotlin。尝试下来整体不太复杂，发现现在的语言、环境什么的越来越傻瓜化了，上手很方便。于是顺便买了本kotlin的参考书还在路上。

根据个人习惯整理了一些待优化的点列在下面，抽时间调整修改，简单的先做，复杂的慢慢做，实在实现不了的就再说。不定期推进。

#### 12月11日
试用各个APP期间为了排版的事情纠结来纠结去，终于忍不住自己写了一套排版算法。心想我只看中文书，中文排版其实很好排，首先字符宽度一致，只需要处理好行首行尾的标点就可以了，而且自己天天忍不住就在关注排版的东西经验非常丰富，所以试写了一下，效果非常好。针对常见到的标点做了处理，最多下移一个字符，有必要时才做标点压缩。在看小说这个领域内我感觉超过市面上所有的中文排版了！而且完全自己写的，后面想怎么改就怎么改。
#### 12月14日
没事的时候忍不住总想改改功能，但是发现目前改代码的效率较低。kotlin语法好多还没看，全靠现查，或者换个方法将就，一堆warning还没除。android只了解了下Activity，UI都没看。还是等把一些基础学一遍，再回过头来改比较好。否则就算现在改完了，到时候也大片返工！
#### 1月2日
沉浸式阅读配置的想法基本上完成了主要内容了。从拉代码以后最想改的就是这一块，因为阅读配置是最常用的一个界面。前前后后试了好几次。主要是涉及的控件、BAR、图标和相关的颜色配置太散了，看起来都是图标，结果接口实现不一样。通过各种尝试、查资料、读代码，终于各个击破都改掉了。

整体思路就是协调上下窗口风格设计，精细调整UI、配色、间距、分割等等。第一次搞这个，高度“学习”了新厚墨和微信读书，好多图标都是拿来主义，反正只有自己用，哈哈！
# 优化列表 
## 阅读界面
#### 阅读界面
* （已导入）时间、电量、书籍进度等信息使用内置字体。（使用自定义字体部分字体字距字高显示不美观）
* （已实现）页眉页脚字体颜色变浅
* （已实现）时间和电量合并
* （已实现）增加字重效果，增加配置，以0.1为跨度增加
* 阅读标题字体强制为默认黑体。支持可配置。
* 修改可生成多个Activity的设计。
* 加大选择文字的判定触摸范围，容易点空。
* （已实现）增加了一个漫画模式，当前书源注释里添加comic时，该书源中的书籍启动漫画模式，另外需要自建一个名字为comic的阅读设置（该设置下就可以根据喜好去掉padding，全屏置黑，默认翻滚翻页等），漫画模式下阅读时默认长按无效，且默认采用comic的阅读设置。
* 漫画模式切非漫画模式的书，会重新刷新书目，有时间看看能不能优化。
#### 排版
* （已实现）行首行尾标点溢出处理
* （已实现）冒号等支持在行尾
* （已实现）左对齐情况下也增加平均间隔，增加一致性
* （已解决）句号逗号实现标点悬挂，暂时不需要，不如下移一个字符舒服，还增加逻辑复杂度
* （已实现）总共三种情况进行压缩标点处理
* （已实现）过多的标点连用或者不可压缩标点出现的情况下，常规方法不好处理，统一不考虑显示间隔，前索引到符合要求的字直接断行，这个就是市面上的通用做法。所以最差也跟其他排版一样。
* 一些字体的标点是居中显示的。需要准确截取到起始x。以获得更好的压缩效果。暂时不知道怎么截取，试一下textpaint的几个接口，实在不行先做成按照不同的界面配置设置压缩修正值。
#### 阅读配置
* （已实现）固定颜色的设置界面风格-目前把写死到Color里，已实现沉浸设置的1.0
* （已实现）toolbar的反色处理全部完成，已实现沉浸设置的2.0
* （已实现) 重新整理各功能入口，增加快捷方式页取代原朗读。该页涵盖基本的书籍信息和8个常用快捷方式。OverFlowMenu中的内容清爽很多了，基本上都是不常用功能了。完成菜单美化3.0
* 上Bar支持沉浸设置
  * （已实现）背景色同步阅读背景色
  * （已实现）字体颜色、图标状态栏反色更改
  * （已实现）去除章节文字，调整链接文字位置
  * 更多设置栏背景、文字颜色配置
* 下BAR支持沉浸设置
  * （已实现）多个窗口文字颜色同步改色
  * （已实现）阅读界面沉浸式设置配色，与底色一致或相近，支持配置，朗读，自动翻页，排版配置，更多配置这几个窗口。加大部分阴影
  * （已实现）配置阅读主题时更新窗口颜色
  * （已实现）配置阅读主题背景色同步改色
  * ATH.setTint内部的颜色风格优化。
  * 朗读设置第二个seekBar的按钮颜色没有设置成强调色问题
* （已实现）设置界面整体美化，图标更换。颜值升级MAX。
* 沉浸配置支持界面可设，抽象出三种配置方式，跟随主题，跟随主色调，跟随底色调，跟随阅读背景色。
* （已实现）加了个按名称排序，自己改下主题名字就能自定义排序了-----目前的自定义背景太多了，背景选择框支持按设置频率排序，或支持置顶功能，或者减小多显示几个。
* 文本配置界面增加一个自定义的选项框。将自己要加的自定义选项放进去。在不破坏原有设计的情况下可以小范围改一下UI，如背景设置单独弹出一个UI。翻页动画收起来，因为不常用。下面空出来的地方全部拿来加一些自定义的配置。可以适当美化一下，加一些分隔栏。
## 软件界面
#### 书架
* 长按书籍显示分组管理
* 分组管理中添加书籍详情
* 不同分组布局独立设置
* 美化分组样式
* 加速默认封面的字体绘制速度
* 修复音频书架书籍移动策略问题
* （已实现）更改默认封面图片和字体样式
#### 订阅
* 订阅里加一个常用书签的功能，展示书签名和链接，可以填链接，直接intent到外部浏览器。主要是浏览器的书签栏位太少。跟阅读相关的占了很大一部分。然后阅读本身的访问网页速度又有些慢。这样就把2个软件结合到一起了。再做个跟规则订阅类似的图标。
* 可以在规则订阅里加一个按钮，直接弹出显示MD。
#### 底栏
* 底栏图标支持渐变色
* 更换四大金刚
#### 阅读记录
* （已实现）阅读记录超过5分钟以上才显示，支持取消
* 阅读时间增加监控，如果30S没有点击事件就不要继续累积阅读时间了。
* 记录每本书的每天阅读记录
* 阅读记录在书籍信息中展示
* 每天、每周、每年阅读时长统计与展示
* 阅读记录窗口展示美化，内容补充
* 增加已读书籍记录
* 阅读记录上传WEBDAV
## WEB
* Web支持两端对齐
* （已解决）Web端的首行缩进不对问题
* Web支持默认封面，与本地默认封面保持一致
## 其他
#### 搜索
* （已实现）换源界面只搜索当前分组
* （已实现）切换分组时情况搜索缓存
* **换源界面点击标题和作者可以进入搜索界面**
* （已实现）换源界面支持切换分组选择
* （已实现）换源界面显示当前的书源和分组
* （已实现）换源界面支持书源管理选择
#### EPUB书籍
* 修复卫斯理全集内容错误
* 修复妖艳人生目录解析失败
* 测试其他本地EPUB书籍
* 引用解析。
* 引用弹出展示
* 增加段落笔记功能
#### 导出
* 支持导出epub格式，也支持txt转epub，css支持自定义。（现在手机上就没找到个txt转epub做的好的）
#### WEBDAV
* 支持访问坚果云下载本地书
* 支持在坚果云进行文件管理操作
* 支持坚果云文件排序
* 支持本地书第一次阅读自动上传到坚果云
#### 阅读进度云同步
* （已解决）支持手动上传阅读进度。（目前大佬优化了相关逻辑了，只要Activity挂起就会上传，手动上传可以不做）
* 手动拉取记录时弹出阅读记录选择界面。选择界面显示阅读记录的相关信息：时间、设备名、章节信息。
* 不要自动拉取阅读记录。防止误点。
* 除默认记录外，支持多条阅读进度，记录设备名，上传时间，章节名信息。
* （已导入）更准确的同步信息上传策略
* WEB端也支持上述功能
#### 默认配置
* 默认的字体目录更改
* 默认访问路径改为Books
* 其他配置默认更改
## 自学习
* 读完kotlin参考书，完成关键练习
  * （已完成）Activity
  * **UI**
  * 数据库
* 熟悉legado代码整体框架
* 熟悉legado各模块代码
* （已实现）学了下简单的工具-学习反编译，收集一些好的资源。
* 相关的一些东西也要把简单再学习一下，github，JSON，javaScript，XML，SQL什么的。妈蛋！
* 学习java的语法，方便各种源码阅读。
* 学习python语法，发现calibre是python写的，需要阅读calibre转化格式的源码，为calibre增加自定义css功能。另外如果后面做手机上txt转epub也许还能参考下逻辑。
* 阅读StaticLayout.java源码

# legado原内容
<div>
  <img width="192" height="192" align="left" src="https://github.com/gedoor/legado/raw/master/app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" alt="legado"/>
  <h1>Legado</h1>
  <br>
  <p>阅读3.0, 阅读是一款可以自定义来源阅读网络内容的工具，为广大网络文学爱好者提供一种方便、快捷舒适的试读体验。</p>
</div>

[![Commitizen friendly](https://img.shields.io/badge/commitizen-friendly-brightgreen.svg)](http://commitizen.github.io/cz-cli/)
[![Build Action](https://github.com/gedoor/legado/workflows/Android%20CI/badge.svg)](https://github.com/gedoor/legado/actions)
[![Downloads](https://img.shields.io/github/downloads/gedoor/legado/total.svg)](https://github.com/gedoor/legado/releases/latest)
[![GitHub issues](https://img.shields.io/github/issues/gedoor/legado)](https://github.com/gedoor/legado/issues)
[![GitHub contributors](https://img.shields.io/github/contributors/gedoor/legado)](https://github.com/gedoor/legado/graphs/contributors)

### 阅读3.0
* [书源规则](https://alanskycn.gitee.io/teachme/)
* [更新日志](/app/src/main/assets/updateLog.md)
* [帮助文档](/app/src/main/assets/help/appHelp.md)

## 下载
<a href="https://play.google.com/store/apps/details?id=io.legado.play.release"><img width="200px" alt="Google Play" src="https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png"/></a> or <a href="https://www.coolapk.com/apk/io.legado.app.release"><img width="100px" height="100px" alt="CoolApk" src="https://github.com/Celeter/build/raw/master/.github/scripts/coolapk.png"/></a> or [Releases](https://github.com/gedoor/legado/releases/latest)

## 阅读API
阅读3.0 提供了2种方式的API：`Web方式`和`Content Provider方式`。您可以在[这里](api.md)根据需要自行调用。 

## 感谢
```
org.jsoup:jsoup
cn.wanghaomiao:JsoupXpath
com.jayway.jsonpath:json-path
com.github.gedoor:rhino-android
com.squareup.okhttp3:okhttp
com.ljx.rxhttp:rxhttp
com.github.bumptech.glide:glide
org.nanohttpd:nanohttpd
org.nanohttpd:nanohttpd-websocket
cn.bingoogolapple:bga-qrcode-zxing
com.jaredrummler:colorpicker
org.apache.commons:commons-text
io.noties.markwon:core
io.noties.markwon:image-glide
com.hankcs:hanlp
com.positiondev.epublib:epublib-core
```

## 免责声明
https://gedoor.github.io/MyBookshelf/disclaimer.html

![image](https://github.com/gedoor/gedoor.github.io/blob/master/images/%E9%98%85%E8%AF%BB%E7%AE%80%E4%BB%8B1.jpg)
![image](https://github.com/gedoor/gedoor.github.io/blob/master/images/%E9%98%85%E8%AF%BB%E7%AE%80%E4%BB%8B2.jpg)
![image](https://github.com/gedoor/gedoor.github.io/blob/master/images/%E9%98%85%E8%AF%BB%E7%AE%80%E4%BB%8B3.jpg)
![image](https://github.com/gedoor/gedoor.github.io/blob/master/images/%E9%98%85%E8%AF%BB%E7%AE%80%E4%BB%8B4.jpg)
![image](https://github.com/gedoor/gedoor.github.io/blob/master/images/%E9%98%85%E8%AF%BB%E7%AE%80%E4%BB%8B5.jpg)
![image](https://github.com/gedoor/gedoor.github.io/blob/master/images/%E9%98%85%E8%AF%BB%E7%AE%80%E4%BB%8B6.jpg)

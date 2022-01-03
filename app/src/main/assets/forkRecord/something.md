﻿[待办](/app/src/main/assets/forkRecord/todo.md) | [完成](/app/src/main/assets/forkRecord/finish.md) | [历程](/app/src/main/assets/forkRecord/something.md) | [返回](https://github.com/hoodie13/legado)

## 吾爱读书

> 从最初买各个版本的kindle和国产阅读器，到给电子书重新排版，再到遍历市面上的各种阅读APP，总是没有一个让自己完全满意的，终于走到了自己DIY的最后一步。
>
> 项目fork于大佬kunfei的个人作品legado，从代码里能感受到大佬浓厚的技术热情，哈哈！这个工程真的非常赞，开始学习android、github、kotlin、java等等东西。玩物丧志啊。
>
> 总之能改一个让自己用着合适的APP就满足了。
>
> 遥想当年，2.5寸的mp3电子书不也看一个通宵吗？10块钱一大本的名家全集不也看好多年吗？
>
> 读本闲书，那么多事干嘛！

### 2021年
#### 3月2日
今天读了一下epublib的源码，发现接口开的挺多，于是重写了一部分上层解析代码，把掌上书苑的书籍opf头不对的问题给改掉了。哈哈，已知的epub兼容性的问题全部解决了。现在阅读完全可以当一个epub阅读器用了。因为代码里可以读取到每个epub内部文件，后面如果还遇到其他兼容性问题应该也不难解决的。就是不知道其他专业阅读器是怎么做的，是不是有更通用的方法。

#### 2月21日
修复了epub的兼容性。各种奇怪的epub都给支持了。（只剩一种opf头不对的还在看怎么处理）。增加了去除部分CSS的配置选项。增加缓存加速了打开epub书籍的速度。现在纠结要不要支持CSS。市面有大量多看精排书，想支持和多看一致的效果看下框架应该能实现。但是从完美主义角度考虑，web上读不到本地图片效果难统一，legado的本行网络书的效果也对比下去了。不如做几个内置的精排模板，做成什么书都能套用的更完美。

增加了web上的阅读记录时间统计。增了web上的阅读进度精准同步，精准到了行。增加了web上的书签添加功能。增加了2个按钮。web刚开始接触，需要抽时间学习下html和JavaScript、还有vue。完善了当前的进度同步逻辑，在手机上点同步，可以看到云的进度，网页的进度，本机上次的进度，如果不小心点错可以回到本地上次进度记录，这块春节前反复优化了多次，基本上舒服了。

#### 1月26日
这几天把阅历功能好好完善了一下。把原来的阅读记录拆分成了书籍记录和时间记录2个数据库。书籍记录记录了所有看过书籍的关键信息。删除了的书下次再看仍然能第一时间使用过去的配置和进度。增加了已读和想读记录（想读暂时没放开）。

时间记录细化到每本书每天分别计时，听书和读书分别计时。（听歌的计时暂时没做，发现没什么好的听歌源，暂时不用）这样最小以天为维度可以统计到所有的阅读记录了。另外增加了长时间不翻页不记录。记录会更加准确。

做的时候总想更全面一些，一个配置总要各种地方加。加完以后才觉得，复杂不一定好，还是需要学会做减法。

#### 1月17日
最近加了不少实用的小功能。一个是增加了漫画模式，整体看漫画上要比异次元更舒服了，后面再优化一下下载的详情展示就完美了。优化了一下搜索线程展示逻辑，增加了书源延迟展示。给内部目录增加了一个置顶功能。优化了整理书架的入口和操作逻辑。

整体对代码的熟悉程度提升了不少，对一些层次深的逻辑也能一步一步跟下去。掌控力更强了一些。

#### 1月2日
沉浸式阅读配置的想法基本上完成了主要内容了。从拉代码以后最想改的就是这一块，因为阅读配置是最常用的一个界面。前前后后试了好几次。主要是涉及的控件、BAR、图标和相关的颜色配置太散了，看起来都是图标，结果接口实现不一样。通过各种尝试、查资料、读代码，终于各个击破都改掉了。

整体思路就是协调上下窗口风格设计，精细调整UI、配色、间距、分割等等。第一次搞这个，高度“学习”了新厚墨和微信读书，好多图标都是拿来主义，反正只有自己用，哈哈！

### 2020年

#### 12月14日
没事的时候忍不住总想改改功能，但是发现目前改代码的效率较低。kotlin语法好多还没看，全靠现查，或者换个方法将就，一堆warning还没除。android只了解了下Activity，UI都没看。还是等把一些基础学一遍，再回过头来改比较好。否则就算现在改完了，到时候也大片返工！

#### 12月11日
试用各个APP期间为了排版的事情纠结来纠结去，终于忍不住自己写了一套排版算法。心想我只看中文书，中文排版其实很好排，首先字符宽度一致，只需要处理好行首行尾的标点就可以了，而且自己天天忍不住就在关注排版的东西经验非常丰富，所以试写了一下，效果非常好。针对常见到的标点做了处理，最多下移一个字符，有必要时才做标点压缩。在看小说这个领域内我感觉超过市面上所有的中文排版了！而且完全自己写的，后面想怎么改就怎么改。

#### 11月24日

手机常驻近50款阅读APP，找不到一款完全满意的。完美主义啊啊啊啊！

实在不行自己来吧，于是fork了大佬kunfei的开源阅读代码legado，打算改改自用。（大佬这个软件真的牛！）

工作上近几年不需要写代码了。好像上次写的代码还是给暗黑3外挂改进打怪算法。开始配置android studio，熟悉gitHub，学习kotlin。尝试下来整体不太复杂，发现现在的语言、环境什么的越来越傻瓜化了，上手很方便。于是顺便买了本kotlin的参考书还在路上。

根据个人习惯整理了一些待优化的点列在下面，抽时间调整修改，简单的先做，复杂的慢慢做，实在实现不了的就再说。不定期推进。
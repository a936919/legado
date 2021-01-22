#### code
```kotlin
ReadBookConfig.bgMeanColor
ReadBookConfig.textColor
requireContext().getPrimaryTextColor(isLight)
ColorUtils.withAlpha( Color.parseColor("#292323"),0.95f)
context.getPrimaryTextColor(ColorUtils.isColorLight(bgColor))
val isLight = ColorUtils.isColorLight(bg)
ColorUtils.withAlpha( Color.parseColor("#292323"),0.95f)
ColorUtils.shiftColor(ReadBookConfig.bgMeanColor, 1.02f)
colorControlNormal
val upArrow = ContextCompat.getDrawable(this, R.drawable.abc_ic_ab_back_material)
StringUtils.dateConvert(item.durChapterTime,"yyyy-MM-dd-HH-mm-ss")
```
#### file
```
src\main\java\io\legado\app\utils\MenuExtensions.kt//设置图标颜色
src\main\java\io\legado\app\constant\AppConst.kt//设置全局阴影
src\main\java\io\legado\app\ui\main\bookshelf\books\BooksFragment.kt//打开书籍
```
#### notes
>bookSourceList用于线程搜书的列表
searchBooks暂存搜书数据库,为更新searchBooksLiveData的。searchBooksLiveData用于更新adapter的

>SELECT "栏位名bai" FROM "表格名" [WHERE "条件"] ORDER BY "栏位名" [ASC, DESC] [] 代表 WHERE 是不du一定需要的。zhi不过dao，如果 WHERE 子句存在的话zhuan，它是在 ORDER BY 子句之前。shuASC 代表结果会以由小往大的顺序列出，而DESC 代表结果会以由大往小的顺序列出。如果两者皆没有被写出的话，那我们就会用 ASC。

>RadioButton
#### git
```gitexclude
git pull git://github.com/gedoor/legado.git master
```
>后面的可以在Desktop里操作了
```gitexclude
git add //conflict file
git commit -m "resolve the conflict"
git push origin master
git push -f origin master
```

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
System.currentTimeMillis()
val c = SimpleDateFormat("yyyy-MM-dd").parse("2021-01-25",ParsePosition(0)).time


```
```kotlin
        val a = appDb.readRecordDao.all
        a.forEach{
            val b = TimeRecord()
            if(it.readTime>30*1000){
                b.readTime = it.readTime
                it.readTime = 0
                b.androidId = it.androidId
                b.bookName = it.bookName
                b.author = it.author
                b.date = TimeRecord.getDayTime()-(24*60*60*1000)
                mqLog.d("${b.bookName} ${b.readTime/1000/60} ${StringUtils.dateConvert(b.date,"yyyy-MM-dd-HH-mm-ss")}")
                appDb.readRecordDao.update(it)
                appDb.timeRecordDao.insert(b)
            }
        }
        val c =  appDb.timeRecordDao.all
        c.forEach {
            mqLog.d("${it.bookName} ${it.author} ${it.androidId} ${StringUtils.dateConvert(it.date,"yyyy-MM-dd-HH-mm-ss")} ${it.readTime/1000/60}")

        }
                    val a = System.currentTimeMillis()
        val b = TimeRecord.getDate()
        val c = SimpleDateFormat("yyyy-MM-dd").parse("2021-01-26", ParsePosition(0)).time
        mqLog.d("$a $b $c ${format(a)} ${format(b)} ${format(c)}")


                    val opf = eBook.opfResource
                    val doc =
                        Jsoup.parse(String(opf.data, mCharset))
                    val item = doc.getElementsByTag("item")
                    try {
                        var i = 6
                        while (i<48){
                            mqLog.d("${item[i]}")
                            mqLog.d("${item[i].attr("id")}")//href
                            val url = item[i].attr("id")
                            val resource = eBook.resources.getByIdOrHref(url)
                            mqLog.d("$resource")
                            val d = Jsoup.parse(String(resource.data, mCharset))
                            var elements = d.body().children()
                            mqLog.d("$elements")
                            i++
                        }
                    } catch (e: IOException) {
                        mqLog.d("err")
                    }

            val refs = eBook.tableOfContents.tocReferences
            if((refs == null || refs.isEmpty())&&eBook.opfResource!=null&&eBook.spine==null){
                val d =
                        Jsoup.parse(String(eBook.opfResource.data, mCharset))
                val elements = d.getElementsByTag("package")
                elements.remove()
                val ele: Document? = Jsoup.parse("<package version=\"2.0\" unique-identifier=\"PrimaryID\" xmlns=\"http://www.idpf.org/2007/opf\">")
                elements.add(0,ele)
                elements.to
                eBook.opfResource.data
            }
            var discard = true
            if (elements != null && elements.size > 0) {
                if(!startFragmentId.isNullOrBlank()||!endFragmentId.isNullOrBlank()){
                    if(startFragmentId.isNullOrBlank())  discard = false
                    /*一条一条删除，防止内容过多卡死*/
                    for (child in elements) {
                        if (startFragmentId == child.id()) discard = false
                        if (endFragmentId == child.id()) discard = true
                        if (discard) child.remove()
                    }
                }
                elements = doc.body().children()
            }
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
>audio缓存参考HttpReadAloudService.kt
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

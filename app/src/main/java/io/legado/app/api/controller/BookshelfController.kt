package io.legado.app.api.controller

import io.legado.app.App
import io.legado.app.api.ReturnData
import io.legado.app.constant.PreferKey
import io.legado.app.data.entities.Book
import io.legado.app.data.entities.ReplaceRule
import io.legado.app.data.entities.TimeRecord
import io.legado.app.help.BookHelp
import io.legado.app.help.ReadBookConfig
import io.legado.app.help.coroutine.Coroutine
import io.legado.app.model.webBook.WebBook
import io.legado.app.service.help.ReadBook
import io.legado.app.utils.*
import kotlinx.coroutines.runBlocking
import kotlin.math.min

object BookshelfController {

    val bookshelf: ReturnData
        get() {
            val books = App.db.bookDao.all
            val returnData = ReturnData()
            return if (books.isEmpty()) {
                returnData.setErrorMsg("还没有添加小说")
            } else {
                val data = when (App.INSTANCE.getPrefInt(PreferKey.bookshelfSort)) {
                    1 -> books.sortedByDescending { it.latestChapterTime }
                    2 -> books.sortedWith { o1, o2 ->
                        o1.name.cnCompare(o2.name)
                    }
                    3 -> books.sortedBy { it.order }
                    else -> books.sortedByDescending { it.durChapterTime }
                }
                returnData.setData(data)
            }
        }

    fun getChapterList(parameters: Map<String, List<String>>): ReturnData {
        val bookUrl = parameters["url"]?.getOrNull(0)
        val returnData = ReturnData()
        if (bookUrl.isNullOrEmpty()) {
            return returnData.setErrorMsg("参数url不能为空，请指定书籍地址")
        }
        App.db.bookDao.getBook(bookUrl)?.let { insertReadRecord(it) }
        val chapterList = App.db.bookChapterDao.getChapterList(bookUrl)
        return returnData.setData(chapterList)
    }

    var timeRecord :TimeRecord? = null
    var readStartTime: Long = System.currentTimeMillis()
    fun saveReadRecord(parameters: Map<String, List<String>>): ReturnData {
        val returnData = ReturnData()
        val dif =  System.currentTimeMillis() - readStartTime
        if(dif<2*1000) return returnData.setErrorMsg("发送过快")
        val bookUrl = parameters["url"]?.getOrNull(0)
        if (bookUrl.isNullOrEmpty()) {
            return returnData.setErrorMsg("参数url不能为空，请指定书籍地址")
        }
        upReadStartTime(bookUrl)
        return returnData.setData(" ")
    }

    private fun insertReadRecord(book: Book){
        Coroutine.async{
            val readRecord = book.toReadRecord()
            val nowTimeRecord = readRecord.toTimeRecord()
            if(timeRecord==null||!timeRecord!!.equals(nowTimeRecord)) {
                book.durChapterTime = System.currentTimeMillis()
                readRecord.durChapterTime = System.currentTimeMillis()
                timeRecord = nowTimeRecord
                timeRecord?.let {
                    readStartTime = System.currentTimeMillis()
                    it.date = TimeRecord.getDate()
                    it.readTime = App.db.timeRecordDao.getReadTime(App.androidId, it.bookName, it.author, it.date) ?: 0
                    App.db.bookDao.update(book)
                    App.db.readRecordDao.update(readRecord)
                    App.db.timeRecordDao.insert(it)
                }
            }
        }
    }

    private fun upReadStartTime(bookUrl:String) {
        Coroutine.async {
            timeRecord?.let {
                val dataChange =  it.date != TimeRecord.getDate()
                var dif =  System.currentTimeMillis() - readStartTime
                val maxInterval = 3*60*1000L
                dif = min(dif,maxInterval)//翻页时间超过3分钟，不计为阅读时间
                readStartTime = System.currentTimeMillis()

                if(dataChange){
                    it.date = TimeRecord.getDate()
                    it.readTime = App.db.timeRecordDao.getReadTime(App.androidId, it.bookName, it.author, it.date)?:0
                }else{
                    App.db.timeRecordDao.getReadTime(App.androidId, it.bookName, it.author, it.date)?.let {readTime->
                        if(readTime > it.readTime) it.readTime = readTime
                    }
                }

                it.readTime = it.readTime + dif
                if(dataChange) App.db.timeRecordDao.insert(it) else App.db.timeRecordDao.update(it)
            }
        }
    }

    fun getBookContent(parameters: Map<String, List<String>>): ReturnData {
        val bookUrl = parameters["url"]?.getOrNull(0)
        val index = parameters["index"]?.getOrNull(0)?.toInt()
        val returnData = ReturnData()
        if (bookUrl.isNullOrEmpty()) {
            return returnData.setErrorMsg("参数url不能为空，请指定书籍地址")
        }
        if (index == null) {
            return returnData.setErrorMsg("参数index不能为空, 请指定目录序号")
        }
        val book = App.db.bookDao.getBook(bookUrl)
        val chapter = App.db.bookChapterDao.getChapter(bookUrl, index)
        if (book == null || chapter == null) {
            returnData.setErrorMsg("未找到")
        } else {
            var content: String? = BookHelp.getContent(book, chapter)
            if (content == null){
                App.db.bookSourceDao.getBookSource(book.origin)?.let { source ->
                    runBlocking {
                        WebBook(source).getContentAwait(this, book, chapter)
                    }.let {
                        content = it
                    }
                } ?: returnData.setErrorMsg("未找到书源")
            }

            if(content!=null){
                content = processReplace(book, content!!)
                saveBookReadIndex(book, index)
                if(ReadBookConfig.isComic(book.origin))
                    content = content!!.replace("\\s*\\n+\\s*".toRegex(),"")
                returnData.setData(content!!)
            }
            else{
                returnData.setErrorMsg("未找到")
            }
        }
        return returnData
    }

    private fun processReplace(book:Book, content:String):String{
        val replaceRules = arrayListOf<ReplaceRule>()
        replaceRules.clear()
        replaceRules.addAll(App.db.replaceRuleDao.findEnabledByScope(book.name, book.origin))
        var content1 = content
        if (book.getUseReplaceRule()) {
            replaceRules.forEach { item ->
                if (item.pattern.isNotEmpty()) {
                    try {
                        content1 = if (item.isRegex) {
                            content1.replace(item.pattern.toRegex(), item.replacement)
                        } else {
                            content1.replace(item.pattern, item.replacement)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
        return content1
    }

    fun saveBook(postData: String?): ReturnData {
        val book = GSON.fromJsonObject<Book>(postData)
        val returnData = ReturnData()
        if (book != null) {
            App.db.bookDao.insert(book)
            if (ReadBook.book?.bookUrl == book.bookUrl) {
                ReadBook.book = book
                ReadBook.durChapterIndex = book.durChapterIndex
            }
            return returnData.setData("")
        }
        return returnData.setErrorMsg("格式不对")
    }

    private fun saveBookReadIndex(book: Book, index: Int) {
        if(book.durChapterIndex != index){
            book.durChapterIndex = index
            book.durChapterPos = 0
        }
        book.durChapterTime = System.currentTimeMillis()
        App.db.bookChapterDao.getChapter(book.bookUrl, index)?.let {
            book.durChapterTitle = it.title
        }
        val readRecord = book.toReadRecord()
        App.db.readRecordDao.update(readRecord)
        App.db.bookDao.update(book)
        if (ReadBook.book?.bookUrl == book.bookUrl) {
            ReadBook.book = book
            ReadBook.durChapterIndex = index
        }
    }
}

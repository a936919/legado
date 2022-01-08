package io.legado.app.api.controller

import android.util.Base64
import androidx.core.graphics.drawable.toBitmap
import io.legado.app.R
import io.legado.app.api.ReturnData
import io.legado.app.constant.AppConst.androidId
import io.legado.app.constant.PreferKey
import io.legado.app.data.appDb
import io.legado.app.data.entities.Book
import io.legado.app.data.entities.BookChapter
import io.legado.app.data.entities.Bookmark
import io.legado.app.data.entities.TimeRecord
import io.legado.app.help.BookHelp
import io.legado.app.help.ContentProcessor
import io.legado.app.help.ReadBookConfig
import io.legado.app.help.coroutine.Coroutine
import io.legado.app.help.glide.ImageLoader
import io.legado.app.help.storage.AppWebDav
import io.legado.app.model.BookCover
import io.legado.app.model.ReadBook
import io.legado.app.model.localBook.EpubFile
import io.legado.app.model.localBook.LocalBook
import io.legado.app.model.localBook.UmdFile
import io.legado.app.model.webBook.WebBook
import io.legado.app.utils.*
import kotlinx.coroutines.runBlocking
import kotlin.math.min
import splitties.init.appCtx
import timber.log.Timber

object BookController {

    /**
     * 书架所有书籍
     */
    val bookshelf: ReturnData
        get() {
            val books = appDb.bookDao.all.filter {
                it.type != 1
            }
            val returnData = ReturnData()
            return if (books.isEmpty()) {
                returnData.setErrorMsg("还没有添加小说")
            } else {
                val data = when (appCtx.getPrefInt(PreferKey.bookshelfSort)) {
                    1 -> books.sortedByDescending { it.latestChapterTime }
                    2 -> books.sortedWith { o1, o2 ->
                        o1.name.cnCompare(o2.name)
                    }
                    3 -> books.sortedBy { it.order }
                    else -> books.sortedByDescending { it.webDurChapterTime }
                }
                returnData.setData(data)
            }
        }

    private var contentProcessor: ContentProcessor? = null
    private var timeRecord: TimeRecord? = null
    private var readStartTime: Long = System.currentTimeMillis()
    fun getBook(parameters: Map<String, List<String>>): ReturnData {
        val bookUrl = parameters["url"]?.getOrNull(0)
        val returnData = ReturnData()
        if (bookUrl.isNullOrEmpty()) {
            return returnData.setErrorMsg("参数url不能为空，请指定书籍地址")
        }
        val book = appDb.bookDao.getBook(bookUrl) ?: return returnData.setData("获取失败")
        contentProcessor = ContentProcessor(book.name, book.origin)
        insertReadRecord(book)
        return returnData.setData(book)
    }

    fun getCover(parameters: Map<String, List<String>>): ReturnData {
        val returnData = ReturnData()
        val coverPath = parameters["path"]?.firstOrNull()
        val ftBitmap = ImageLoader.loadBitmap(appCtx, coverPath).submit()
        return try {
            returnData.setData(ftBitmap.get())
        } catch (e: Exception) {
            returnData.setData(BookCover.defaultDrawable.toBitmap())
        }
    }

    /**
     * 更新目录
     */
    fun refreshToc(parameters: Map<String, List<String>>): ReturnData {
        val returnData = ReturnData()
        try {
            val bookUrl = parameters["url"]?.firstOrNull()
            if (bookUrl.isNullOrEmpty()) {
                return returnData.setErrorMsg("参数url不能为空，请指定书籍地址")
            }
            val book = appDb.bookDao.getBook(bookUrl)
                ?: return returnData.setErrorMsg("bookUrl不对")
            if (book.isLocalBook()) {
                val toc = LocalBook.getChapterList(book)
                appDb.bookChapterDao.delByBook(book.bookUrl)
                appDb.bookChapterDao.insert(*toc.toTypedArray())
                appDb.bookDao.update(book)
                return returnData.setData(toc)
            } else {
                val bookSource = appDb.bookSourceDao.getBookSource(book.origin)
                    ?: return returnData.setErrorMsg("未找到对应书源,请换源")
                val toc = runBlocking {
                    if (book.tocUrl.isBlank()) {
                        WebBook.getBookInfoAwait(this, bookSource, book)
                    }
                    WebBook.getChapterListAwait(this, bookSource, book)
                }
                appDb.bookChapterDao.delByBook(book.bookUrl)
                appDb.bookChapterDao.insert(*toc.toTypedArray())
                appDb.bookDao.update(book)
                return returnData.setData(toc)
            }
        } catch (e: Exception) {
            return returnData.setErrorMsg(e.localizedMessage ?: "refresh toc error")
        }
    }

    /**
     * 获取目录
     */
    fun getChapterList(parameters: Map<String, List<String>>): ReturnData {
        val bookUrl = parameters["url"]?.firstOrNull()
        val returnData = ReturnData()
        if (bookUrl.isNullOrEmpty()) {
            return returnData.setErrorMsg("参数url不能为空，请指定书籍地址")
        }
        val chapterList = appDb.bookChapterDao.getChapterList(bookUrl)
        if (chapterList.isEmpty()) {
            return refreshToc(parameters)
        }
        return returnData.setData(chapterList)
    }

    /**
     * 获取正文
     */
    fun getBookContent(parameters: Map<String, List<String>>): ReturnData {
        val bookUrl = parameters["url"]?.firstOrNull()
        val index = parameters["index"]?.firstOrNull()?.toInt()
        val returnData = ReturnData()
        if (bookUrl.isNullOrEmpty()) {
            return returnData.setErrorMsg("参数url不能为空，请指定书籍地址")
        }
        if (index == null) {
            return returnData.setErrorMsg("参数index不能为空, 请指定目录序号")
        }
        val book = appDb.bookDao.getBook(bookUrl)
        val chapter = appDb.bookChapterDao.getChapter(bookUrl, index)
        if (book == null || chapter == null) {
            returnData.setErrorMsg("未找到")
        } else {
            var content: String? = BookHelp.getContent(book, chapter)
            if (content == null) {
                appDb.bookSourceDao.getBookSource(book.origin)?.let { source ->
                    runBlocking {
                        WebBook.getContentAwait(this, source, book, chapter)
                    }.let {
                        content = it
                    }
                } ?: returnData.setErrorMsg("未找到书源")
            }

            if (content != null) {
                runBlocking {
                    processReplace(book, chapter, content!!)
                }.let {
                    content = it
                }
                if (ReadBookConfig.isComic(book.origin))
                    content = content!!.replace("\\s*\\n+\\s*".toRegex(), "")
                saveBookReadIndex(book, index, 0)
                returnData.setData(content!!)
            } else {
                returnData.setErrorMsg("未找到")
            }
        }
        return returnData
    }

    fun saveReadRecord(parameters: Map<String, List<String>>): ReturnData {
        val returnData = ReturnData()
        val dif = System.currentTimeMillis() - readStartTime
        if (dif < 2000) return returnData.setErrorMsg("发送过快")
        val bookUrl = parameters["url"]?.getOrNull(0)
        val chapterIndex = parameters["chapterIndex"]?.getOrNull(0)?.toInt()
        val pos = parameters["pos"]?.getOrNull(0)?.toInt()
        if (bookUrl == null || chapterIndex == null || pos == null)
            return returnData.setErrorMsg("参数url不能为空，请指定书籍地址")
        appDb.bookDao.getBook(bookUrl)?.let { saveBookReadIndex(it, chapterIndex, pos) }
        return returnData.setData("成功")
    }

    fun setBookmark(parameters: Map<String, List<String>>): ReturnData {
        val returnData = ReturnData()
        val bookUrl = parameters["url"]?.getOrNull(0)
        val chapterIndex = parameters["chapterIndex"]?.getOrNull(0)?.toInt()
        val pos = parameters["pos"]?.getOrNull(0)?.toInt()
        val bookText = parameters["bookText"]?.getOrNull(0)
        if (bookUrl == null || chapterIndex == null || pos == null || bookText.isNullOrBlank())
            return returnData.setErrorMsg("参数url不能为空，请指定书籍地址")
        val book = appDb.bookDao.getBook(bookUrl) ?: return returnData.setData("获取失败")
        val chapterName = appDb.bookChapterDao.getChapter(book.bookUrl, chapterIndex)?.title ?: ""
        val bookmark = Bookmark(
            System.currentTimeMillis(),
            book.name,
            book.author,
            chapterIndex,
            pos,
            chapterName,
            bookText,
            ""
        )
        appDb.bookmarkDao.insert(bookmark)
        saveBookReadIndex(book, chapterIndex, pos)
        return returnData.setData("成功")
    }

    fun saveBook(postData: String?): ReturnData {
        val book = GSON.fromJsonObject<Book>(postData)
        val returnData = ReturnData()
        if (book != null) {
            book.save()
            AppWebDav.uploadBookProgress(book)
            if (ReadBook.book?.bookUrl == book.bookUrl) {
                ReadBook.book = book
                ReadBook.durChapterIndex = book.durChapterIndex
            }
            return returnData.setData("")
        }
        return returnData.setErrorMsg("格式不对")
    }

    private fun insertReadRecord(book: Book) {
        Coroutine.async {
            val readRecord = book.toReadRecord()
            val nowTimeRecord = readRecord.toTimeRecord()
            if (timeRecord == null || !timeRecord!!.equals(nowTimeRecord)) {
                timeRecord = nowTimeRecord
                timeRecord?.let {
                    appDb.bookDao.update(book)
                    appDb.readRecordDao.insert(readRecord)
                    appDb.timeRecordDao.insert(it)
                }
            }
        }
    }

    private fun processReplace(book: Book, chapter: BookChapter, content: String): String {
        contentProcessor?.let {
            val contents =
                it.getContent(book, chapter, content, false, book.getUseReplaceRule())
            return contents.joinToString("\n")
        }
        return content
    }

    private fun saveBookReadIndex(book: Book, chapterIndex: Int, pos: Int) {
        Coroutine.async {
            book.webDurChapterTime = System.currentTimeMillis()
            book.webChapterIndex = chapterIndex
            book.webChapterPos = pos
            appDb.bookChapterDao.getChapter(book.bookUrl, chapterIndex)?.let {
                book.durChapterTitle = it.title
            }
            appDb.bookDao.update(book)
            val readRecord = book.toReadRecord()
            readRecord.durChapterTime = book.webDurChapterTime
            readRecord.durChapterIndex = book.webChapterIndex
            readRecord.durChapterPos = book.webChapterIndex
            appDb.readRecordDao.update(readRecord)
            timeRecord?.let {
                val dataChange = it.date != TimeRecord.getDate()
                var dif = System.currentTimeMillis() - readStartTime
                val maxInterval = 3 * 60 * 1000L
                dif = min(dif, maxInterval)//翻页时间超过3分钟，不计为阅读时间
                readStartTime = System.currentTimeMillis()
                if (dataChange) {
                    it.date = TimeRecord.getDate()
                    it.readTime = appDb.timeRecordDao.getReadTime(
                        androidId,
                        it.bookName,
                        it.author,
                        it.date
                    )
                        ?: 0
                } else {
                    appDb.timeRecordDao.getReadTime(androidId, it.bookName, it.author, it.date)
                        ?.let { readTime ->
                            if (readTime > it.readTime) it.readTime = readTime
                        }
                }
                it.readTime = it.readTime + dif
                if (dataChange) appDb.timeRecordDao.insert(it) else appDb.timeRecordDao.update(it)

            }
        }
    }

    fun addLocalBook(parameters: Map<String, List<String>>): ReturnData {
        val returnData = ReturnData()
        try {
            val fileName = parameters["fileName"]?.firstOrNull()
                ?: return returnData.setErrorMsg("fileName 不能为空")
            val fileData = parameters["fileData"]?.firstOrNull()
                ?: return returnData.setErrorMsg("fileData 不能为空")
            val file = FileUtils.createFileIfNotExist(LocalBook.cacheFolder, fileName)
            val fileBytes = Base64.decode(fileData.substringAfter("base64,"), Base64.DEFAULT)
            file.writeBytes(fileBytes)
            val nameAuthor = LocalBook.analyzeNameAuthor(fileName)
            val book = Book(
                bookUrl = file.absolutePath,
                name = nameAuthor.first,
                author = nameAuthor.second,
                originName = fileName,
                coverUrl = FileUtils.getPath(
                    appCtx.externalFiles,
                    "covers",
                    "${MD5Utils.md5Encode16(file.absolutePath)}.jpg"
                )
            )
            if (book.isEpub()) EpubFile.upBookInfo(book)
            if (book.isUmd()) UmdFile.upBookInfo(book)
            appDb.bookDao.insert(book)
        } catch (e: Exception) {
            Timber.e(e)
            return returnData.setErrorMsg(
                e.localizedMessage ?: appCtx.getString(R.string.unknown_error)
            )
        }
        return returnData.setData(true)
    }

}

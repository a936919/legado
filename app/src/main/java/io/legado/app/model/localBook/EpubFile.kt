package io.legado.app.model.localBook

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.text.TextUtils
import io.legado.app.data.appDb
import io.legado.app.data.entities.Book
import io.legado.app.data.entities.EpubChapter
import io.legado.app.data.entities.BookChapter
import io.legado.app.utils.FileUtils
import io.legado.app.utils.HtmlFormatter
import io.legado.app.utils.MD5Utils
import io.legado.app.utils.externalFiles
import me.ag2s.epublib.domain.EpubBook

import me.ag2s.epublib.domain.Resource
import me.ag2s.epublib.domain.TOCReference
import me.ag2s.epublib.epub.EpubReader
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import splitties.init.appCtx
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset
import java.util.*

class EpubFile(var book: Book) {

    companion object {
        private var eFile: EpubFile? = null

        @Synchronized
        private fun getEFile(book: Book): EpubFile {
            if (eFile == null || eFile?.book?.bookUrl != book.bookUrl) {
                eFile = EpubFile(book)
                //对于Epub文件默认不启用替换
                book.setUseReplaceRule(false)
                return eFile!!
            }
            eFile?.book = book
            return eFile!!
        }

        @Synchronized
        fun getChapterList(book: Book): ArrayList<BookChapter> {
            return getEFile(book).getChapterList()
        }

        @Synchronized
        fun getContent(book: Book, chapter: BookChapter): String? {
            return getEFile(book).getContent(chapter)
        }

        @Synchronized
        fun getImage(
            book: Book,
            href: String
        ): InputStream? {
            return getEFile(book).getImage(href)
        }

        @Synchronized
        fun upBookInfo(book: Book) {
            return getEFile(book).upBookInfo()
        }
    }

    private var mCharset: Charset = Charset.defaultCharset()
    private var epubBook: EpubBook? = null
        get() {
            if (field != null) {
                return field
            }
            field = readEpub()
            return field
        }

    init {
        try {
            epubBook?.let {
                if (book.coverUrl.isNullOrEmpty()) {
                    book.coverUrl = FileUtils.getPath(
                        appCtx.externalFiles,
                        "covers",
                        "${MD5Utils.md5Encode16(book.bookUrl)}.jpg"
                    )
                }
                if (!File(book.coverUrl!!).exists()) {
                    /*部分书籍DRM处理后，封面获取异常，待优化*/
                    it.coverImage?.inputStream?.use { input ->
                        val cover = BitmapFactory.decodeStream(input)
                        val out = FileOutputStream(FileUtils.createFileIfNotExist(book.coverUrl!!))
                        cover.compress(Bitmap.CompressFormat.JPEG, 90, out)
                        out.flush()
                        out.close()
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    /*重写epub文件解析代码，直接读出压缩包文件生成Resources给epublib，这样的好处是可以逐一修改某些文件的格式错误*/
    private fun readEpub(): EpubBook? {
        try {
            val bis = LocalBook.getBookInputStream(book)
            //通过懒加载读取epub
            return EpubReader().readEpub(bis, "utf-8")
        } catch (e: Exception) {
            Timber.e(e)
        }
        return null
    }

    private fun getContent(chapter: BookChapter): String? {
        /*获取当前章节文本*/
        var string = getChildChapter(chapter, chapter.url)
        val childContends = appDb.epubChapterDao.get(book.bookUrl, chapter.url)
        childContends?.forEach {
            string += "\n" + getChildChapter(chapter, it.href)
        }
        return string
    }

    private fun getChildChapter(chapter: BookChapter, href: String): String? {
        epubBook?.let {
            val body = Jsoup.parse(String(it.resources.getByHref(href).data, mCharset)).body()

            if (chapter.url == href) {
                val startFragmentId = chapter.startFragmentId
                val endFragmentId = chapter.endFragmentId
                /*一些书籍依靠href索引的resource会包含多个章节，需要依靠fragmentId来截取到当前章节的内容*/
                /*注:这里较大增加了内容加载的时间，所以首次获取内容后可存储到本地cache，减少重复加载*/
                if (!startFragmentId.isNullOrBlank())
                    body.getElementById(startFragmentId)?.previousElementSiblings()?.remove()
                if (!endFragmentId.isNullOrBlank() && endFragmentId != startFragmentId)
                    body.getElementById(endFragmentId)?.nextElementSiblings()?.remove()
            }

            /*选择去除正文中的H标签，部分书籍标题与阅读标题重复待优化*/
            var tag = Book.hTag
            if (book.getDelTag(tag)) {
                body.getElementsByTag("h1")?.remove()
                body.getElementsByTag("h2")?.remove()
                body.getElementsByTag("h3")?.remove()
                body.getElementsByTag("h4")?.remove()
                body.getElementsByTag("h5")?.remove()
                body.getElementsByTag("h6")?.remove()
                //body.getElementsMatchingOwnText(chapter.title)?.remove()
            }

            /*选择去除正文中的img标签，目前图片支持效果待优化*/
            tag = Book.imgTag
            if (book.getDelTag(tag)) {
                body.getElementsByTag("img")?.remove()
            }

            val elements = body.children()
            elements.select("script").remove()
            elements.select("style").remove()
            /*选择去除正文中的ruby标签，目前注释支持效果待优化*/
            tag = Book.rubyTag
            var html = elements.outerHtml()
            if (book.getDelTag(tag)) {
                html = html.replace("<ruby>\\s?([\\u4e00-\\u9fa5])\\s?.*?</ruby>".toRegex(), "$1")
            }
            return HtmlFormatter.formatKeepImg(html)
        }
        return null
    }

    private fun getBody(res: Resource, startFragmentId: String?, endFragmentId: String?): Element {
        val body = Jsoup.parse(String(res.data, mCharset)).body()
        if (!startFragmentId.isNullOrBlank()) {
            body.getElementById(startFragmentId)?.previousElementSiblings()?.remove()
        }
        if (!endFragmentId.isNullOrBlank() && endFragmentId != startFragmentId) {
            body.getElementById(endFragmentId)?.nextElementSiblings()?.remove()
        }
        /*选择去除正文中的H标签，部分书籍标题与阅读标题重复待优化*/
        val tag = Book.hTag
        if (book.getDelTag(tag)) {
            body.getElementsByTag("h1").remove()
            body.getElementsByTag("h2").remove()
            body.getElementsByTag("h3").remove()
            body.getElementsByTag("h4").remove()
            body.getElementsByTag("h5").remove()
            body.getElementsByTag("h6").remove()
            //body.getElementsMatchingOwnText(chapter.title)?.remove()
        }

        val children = body.children()
        children.select("script").remove()
        children.select("style").remove()
        return body
    }

    private fun getImage(href: String): InputStream? {
        val abHref = href.replace("../", "")
        return epubBook?.resources?.getByHref(abHref)?.inputStream
    }

    private fun upBookInfo() {
        if (epubBook == null) {
            eFile = null
            book.intro = "书籍导入异常"
        } else {
            val metadata = epubBook!!.metadata
            book.name = metadata.firstTitle
            if (book.name.isEmpty()) {
                book.name = book.originName.replace(".epub", "")
            }

            if (metadata.authors.size > 0) {
                val author =
                    metadata.authors[0].toString().replace("^, |, $".toRegex(), "")
                book.author = author
            }
            if (metadata.descriptions.size > 0) {
                book.intro = Jsoup.parse(metadata.descriptions[0]).text()
            }
        }
    }

    private fun getChapterList(): ArrayList<BookChapter> {
        val chapterList = ArrayList<BookChapter>()
        epubBook?.let { eBook ->
            val refs = eBook.tableOfContents.tocReferences
            if (refs == null || refs.isEmpty()) {
                val spineReferences = eBook.spine.spineReferences
                var i = 0
                val size = spineReferences.size
                while (i < size) {
                    val resource = spineReferences[i].resource

                    var title = resource.title
                    if (TextUtils.isEmpty(title)) {
                        try {
                            val doc =
                                Jsoup.parse(String(resource.data, mCharset))
                            val elements = doc.getElementsByTag("title")
                            if (elements.size > 0) {

                                title = elements[0].text()
                            }
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                    val chapter = BookChapter()
                    chapter.index = i
                    chapter.bookUrl = book.bookUrl
                    chapter.url = resource.href
                    if (i == 0 && title.isEmpty()) {
                        chapter.title = "封面"
                    } else {
                        chapter.title = title
                    }

                    chapterList.add(chapter)
                    i++
                }
            } else {
                parseFirstPage(chapterList, refs)
                parseMenu(chapterList, refs, 0)
                for (i in chapterList.indices) {
                    chapterList[i].index = i
                }
                getChildChapter(chapterList)

            }
        }
        book.latestChapterTitle = chapterList.lastOrNull()?.title
        book.totalChapterNum = chapterList.size
        return chapterList
    }

    /*获取当前章节的子章节。部分书籍一个章节包含多个html文件，（一些精排书籍，每一章节正文前的标题、标题封面、引言等都会有独立html）*/
    /*需在读取常规章节列表后调用，遍历书籍全内容，根据href检索原不包含在章节内的html归属父章节*/
    private fun getChildChapter(chapterList: ArrayList<BookChapter>) {
        epubBook?.let {
            val contents = it.contents
            val chapters = ArrayList<EpubChapter>()
            if (contents != null) {
                var i = 0
                var j = 0
                var parentHref: String? = null
                while (i < contents.size) {
                    val content = contents[i]
                    if (j < chapterList.size && content.href == chapterList[j].url) {
                        parentHref = content.href
                        j++
                    } else if (!parentHref.isNullOrBlank() && content.mediaType.toString()
                            .contains("htm")
                    ) {
                        val epubChapter = EpubChapter()
                        epubChapter.bookUrl = book.bookUrl
                        epubChapter.href = content.href
                        epubChapter.parentHref = parentHref
                        chapters.add(epubChapter)
                    }
                    i++
                }
            }
            appDb.epubChapterDao.deleteByName(book.bookUrl)
            if (chapters.size > 0) appDb.epubChapterDao.insert(*chapters.toTypedArray())
        }
    }

    /*获取书籍起始页内容。部分书籍第一章之前存在封面，引言，扉页等内容*/
    /*tile获取不同书籍风格杂乱，格式化处理待优化*/
    private var durIndex = 0
    private fun parseFirstPage(
        chapterList: ArrayList<BookChapter>,
        refs: List<TOCReference>?
    ) {
        val contents = epubBook?.contents
        if (epubBook == null || contents == null || refs == null) return
        var i = 0
        durIndex = 0
        while (i < contents.size) {
            val content = contents[i]
            if (!content.mediaType.toString().contains("htm")) continue
            /*检索到第一章href停止*/
            if (refs[0].completeHref == content.href) break
            val chapter = BookChapter()
            var title = content.title
            if (TextUtils.isEmpty(title)) {
                val elements = Jsoup.parse(
                    String(epubBook!!.resources.getByHref(content.href).data, mCharset)
                ).getElementsByTag("title")
                title =
                    if (elements.size > 0 && elements[0].text().isNotBlank())
                        elements[0].text()
                    else
                        "--卷首--"

            }
            chapter.bookUrl = book.bookUrl
            chapter.title = title
            chapter.url = content.href
            chapter.startFragmentId =
                if (content.href.substringAfter("#") == content.href) null
                else content.href.substringAfter("#")
            chapterList.lastOrNull()?.endFragmentId = chapter.startFragmentId
            chapterList.lastOrNull()?.putVariable("nextUrl", chapter.url)

            chapterList.add(chapter)
            durIndex++
            i++
        }
    }

    private fun parseMenu(
        chapterList: ArrayList<BookChapter>,
        refs: List<TOCReference>?,
        level: Int
    ) {
        refs?.forEach { ref ->
            if (ref.resource != null) {
                val chapter = BookChapter()
                chapter.bookUrl = book.bookUrl
                chapter.title = ref.title
                chapter.url = ref.completeHref
                chapter.startFragmentId = ref.fragmentId
                chapterList.lastOrNull()?.endFragmentId = chapter.startFragmentId
                chapterList.lastOrNull()?.putVariable("nextUrl", chapter.url)
                chapterList.add(chapter)
                durIndex++
            }
            if (ref.children != null && ref.children.isNotEmpty()) {
                parseMenu(chapterList, ref.children, level + 1)
            }
        }
    }
}
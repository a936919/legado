package io.legado.app.model.localBook

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.text.TextUtils
import io.legado.app.App
import io.legado.app.data.entities.EpubChapter
import io.legado.app.data.entities.BookChapter
import io.legado.app.utils.*
import nl.siegmann.epublib.domain.Book
import nl.siegmann.epublib.domain.TOCReference
import nl.siegmann.epublib.epub.EpubReader
import org.jsoup.Jsoup
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset
import java.util.*

class EPUBFile(var book: io.legado.app.data.entities.Book) {

    companion object {
        private var eFile: EPUBFile? = null

        @Synchronized
        private fun getEFile(book: io.legado.app.data.entities.Book): EPUBFile {
            if (eFile == null || eFile?.book?.bookUrl != book.bookUrl) {
                eFile = EPUBFile(book)
                return eFile!!
            }
            eFile?.book = book
            return eFile!!
        }

        @Synchronized
        fun getChapterList(book: io.legado.app.data.entities.Book): ArrayList<BookChapter> {
            return getEFile(book).getChapterList()
        }

        @Synchronized
        fun getContent(book: io.legado.app.data.entities.Book, chapter: BookChapter): String? {
            return getEFile(book).getContent(chapter)
        }

        @Synchronized
        fun getImage(
            book: io.legado.app.data.entities.Book,
            href: String
        ): InputStream? {
            return getEFile(book).getImage(href)
        }

        @Synchronized
        fun getBookInfo(book: io.legado.app.data.entities.Book) {
            return getEFile(book).getBookInfo()
        }
    }

    private var epubBook: Book? = null
    private var mCharset: Charset = Charset.defaultCharset()
    private var debugBook = false

    init {
        try {
            val epubReader = EpubReader()
            val inputStream = if (book.bookUrl.isContentScheme()) {
                val uri = Uri.parse(book.bookUrl)
                App.INSTANCE.contentResolver.openInputStream(uri)
            } else {
                File(book.bookUrl).inputStream()
            }
            epubBook = epubReader.readEpub(inputStream)

            if (book.coverUrl.isNullOrEmpty()) {
                book.coverUrl = FileUtils.getPath(
                    App.INSTANCE.externalFilesDir,
                    "covers",
                    "${MD5Utils.md5Encode16(book.bookUrl)}.jpg"
                )
            }
            if (!File(book.coverUrl!!).exists()) {
                epubBook!!.coverImage?.inputStream?.use {
                    val cover = BitmapFactory.decodeStream(it)
                    val out = FileOutputStream(FileUtils.createFileIfNotExist(book.coverUrl!!))
                    cover.compress(Bitmap.CompressFormat.JPEG, 90, out)
                    out.flush()
                    out.close()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getContent(chapter: BookChapter): String? {
        var string = getChildChapter(chapter, chapter.url)
        val childContends = App.db.epubChapter.get(book.bookUrl, chapter.url)
        if (childContends != null) {
            for (child in childContends) {
                string += "\n" + getChildChapter(chapter, child.href)
            }
        }
        return string
    }

    private fun getChildChapter(chapter: BookChapter, href: String): String? {
        epubBook?.let {
            val body = Jsoup.parse(String(it.resources.getByHref(href).data, mCharset)).body()

            if (chapter.url == href) {
                val startFragmentId = chapter.startFragmentId
                val endFragmentId = chapter.endFragmentId
                if (!startFragmentId.isNullOrBlank())
                    body.getElementById(startFragmentId)?.previousElementSiblings()?.remove()
                if (!endFragmentId.isNullOrBlank() && endFragmentId != startFragmentId)
                    body.getElementById(endFragmentId)?.nextElementSiblings()?.remove()
            }

            var tag = io.legado.app.data.entities.Book.hTag
            if (book.getDelTag(tag)) {
                body.getElementsByTag("h1")?.remove()
                body.getElementsByTag("h2")?.remove()
                body.getElementsByTag("h3")?.remove()
                body.getElementsByTag("h4")?.remove()
                body.getElementsByTag("h5")?.remove()
                body.getElementsByTag("h6")?.remove()
            }

            tag = io.legado.app.data.entities.Book.imgTag
            if (book.getDelTag(tag)) {
                body.getElementsByTag("img")?.remove()
            }

            val elements = body.children()
            elements.select("script").remove()
            elements.select("style").remove()

            tag = io.legado.app.data.entities.Book.rubyTag
            var html = elements.outerHtml()
            if (book.getDelTag(tag)) {
                html = html.replace("<ruby>\\s?([\\u4e00-\\u9fa5])\\s?.*?</ruby>".toRegex(), "$1")
            }
            return html.htmlFormat()
        }
        return null
    }

    private fun getImage(href: String): InputStream? {
        val abHref = href.replace("../", "")
        return epubBook?.resources?.getByHref(abHref)?.inputStream
    }

    private fun getBookInfo() {
        if (epubBook == null) {
            eFile = null
            book.intro = "书籍导入异常"
        } else {
            val metadata = epubBook!!.metadata
            book.name = book.originName
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
                if (debugBook) mqLog.d("spine process")
                val spineReferences = eBook.spine.spineReferences
                var i = 0
                val size = spineReferences.size
                while (i < size) {
                    val resource =
                        spineReferences[i].resource
                    var title = resource.title
                    if (TextUtils.isEmpty(title)) {
                        try {
                            val doc =
                                Jsoup.parse(String(resource.data, mCharset))
                            val elements = doc.getElementsByTag("title")
                            if (elements != null && elements.size > 0) {
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
                        if (debugBook) mqLog.d("getChildChapter index is $i $j  info is ${epubChapter.parentHref}  ${epubChapter.href} ")
                        chapters.add(epubChapter)
                    }
                    i++
                }
            }
            App.db.epubChapter.deleteByName(book.bookUrl)
            if (chapters.size > 0) App.db.epubChapter.insert(*chapters.toTypedArray())
        }
    }

    private var refIndex = 0
    private fun parseMenu(
        chapterList: ArrayList<BookChapter>,
        refs: List<TOCReference>?,
        level: Int
    ) {
        if (epubBook == null || refs == null) return
        val contents = epubBook?.contents
        if (level == 0 && contents != null) {
            var i = 0
            refIndex = 0
            mqLog.d(refs[0].completeHref)
            while (i < contents.size) {
                val content = contents[i]
                if (content.mediaType.toString().contains("htm")) {
                    if (refs[0].completeHref == content.href) {
                        break
                    } else {
                        val chapter = BookChapter()
                        var title = content.title
                        if (TextUtils.isEmpty(title)) {
                            val elements = Jsoup.parse(
                                String(
                                    epubBook!!.resources.getByHref(content.href).data,
                                    mCharset
                                )
                            ).getElementsByTag("title")
                            title =
                                if (elements != null && elements.size > 0) elements[0].text() else "--卷首--"
                        }
                        chapter.bookUrl = book.bookUrl
                        chapter.title = title
                        chapter.url = content.href
                        chapter.startFragmentId =
                            if (content.href.substringAfter("#") == content.href) null
                            else content.href.substringAfter("#")
                        if (refIndex > 0) {
                            val preIndex = refIndex - 1
                            chapterList[preIndex].endFragmentId = chapter.startFragmentId
                            if (debugBook) mqLog.d("parseMenu pre  $preIndex ${chapterList[preIndex].title} ${chapterList[preIndex].url} ${chapterList[preIndex].startFragmentId} ${chapterList[preIndex].endFragmentId}")
                        }
                        chapterList.add(chapter)
                        refIndex++
                    }
                }
                i++
            }
        }

        for (ref in refs) {
            if (ref.resource != null) {
                val chapter = BookChapter()
                chapter.bookUrl = book.bookUrl
                chapter.title = ref.title
                chapter.url = ref.completeHref
                chapter.startFragmentId = ref.fragmentId
                if (refIndex > 0) {
                    val preIndex = refIndex - 1
                    chapterList[preIndex].endFragmentId = chapter.startFragmentId
                    if (debugBook) mqLog.d("parseMenu content $preIndex ${chapterList[preIndex].title} ${chapterList[preIndex].url} ${chapterList[preIndex].startFragmentId} ${chapterList[preIndex].endFragmentId}")
                }
                chapterList.add(chapter)
                refIndex++
            }
            if (ref.children != null && ref.children.isNotEmpty()) {
                parseMenu(chapterList, ref.children, level + 1)
            }
        }
    }


}
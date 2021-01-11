package io.legado.app.ui.book.read.page.provider

import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.Log
import io.legado.app.App
import io.legado.app.constant.AppPattern
import io.legado.app.constant.EventBus
import io.legado.app.data.entities.Book
import io.legado.app.data.entities.BookChapter
import io.legado.app.help.AppConfig
import io.legado.app.help.ReadBookConfig
import io.legado.app.model.analyzeRule.AnalyzeUrl
import io.legado.app.ui.book.read.page.entities.TextChapter
import io.legado.app.ui.book.read.page.entities.TextChar
import io.legado.app.ui.book.read.page.entities.TextLine
import io.legado.app.ui.book.read.page.entities.TextPage
import io.legado.app.utils.*
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.max
import kotlin.math.min


@Suppress("DEPRECATION")
object ChapterProvider {
    @JvmStatic
    var viewWidth = 0

    @JvmStatic
    private var viewHeight = 0

    @JvmStatic
    var paddingLeft = 0

    @JvmStatic
    var paddingTop = 0

    @JvmStatic
    var visibleWidth = 0

    @JvmStatic
    var visibleHeight = 0

    @JvmStatic
    var visibleRight = 0

    @JvmStatic
    var visibleBottom = 0

    @JvmStatic
    private var lineSpacingExtra = 0

    @JvmStatic
    private var paragraphSpacing = 0

    @JvmStatic
    private var titleTopSpacing = 0

    @JvmStatic
    private var titleBottomSpacing = 0

    @JvmStatic
    var typeface: Typeface = Typeface.SANS_SERIF

    @JvmStatic
    lateinit var titlePaint: TextPaint

    @JvmStatic
    lateinit var contentPaint: TextPaint

    init {
        upStyle()
    }

    /**
     * 获取拆分完的章节数据
     */
    fun getTextChapter(
        book: Book,
        bookChapter: BookChapter,
        contents: List<String>,
        chapterSize: Int,
        imageStyle: String?,
    ): TextChapter {
        val textPages = arrayListOf<TextPage>()
        val stringBuilder = StringBuilder()
        var durY = 0f
        textPages.add(TextPage())
        contents.forEachIndexed { index, text ->
            val matcher = AppPattern.imgPattern.matcher(text)
            if (matcher.find()) {
                matcher.group(1)?.let {
                    if (!book.isEpub()) {
                        val src = NetworkUtils.getAbsoluteURL(bookChapter.url, it)
                        durY = setTypeImage(
                            book, bookChapter, src, durY, textPages, imageStyle
                        )
                    }
                }
            } else {
                val isTitle = index == 0
                val textPaint = if (isTitle) titlePaint else contentPaint
                if (!(isTitle && ReadBookConfig.titleMode == 2)) {
                    durY = setTypeText(text, durY, textPages, stringBuilder, isTitle, textPaint)
                }
            }
        }
        textPages.last().height = durY + 20.dp
        textPages.last().text = stringBuilder.toString()
        textPages.forEachIndexed { index, item ->
            item.index = index
            item.pageSize = textPages.size
            item.chapterIndex = bookChapter.index
            item.chapterSize = chapterSize
            item.title = bookChapter.title
            item.upLinesPosition()
        }

        return TextChapter(
            bookChapter.index, bookChapter.title,
            bookChapter.getAbsoluteURL().split(AnalyzeUrl.splitUrlRegex)[0],
            textPages, chapterSize
        )
    }

    private fun setTypeImage(
        book: Book,
        chapter: BookChapter,
        src: String,
        y: Float,
        textPages: ArrayList<TextPage>,
        imageStyle: String?,
    ): Float {
        var durY = y
        ImageProvider.getImage(book, chapter.index, src)?.let {
            if (durY > visibleHeight) {
                textPages.last().height = durY
                textPages.add(TextPage())
                durY = 0f
            }
            var height = it.height
            var width = it.width
            when (imageStyle?.toUpperCase(Locale.ROOT)) {
                "FULL" -> {
                    width = visibleWidth
                    height = it.height * visibleWidth / it.width
                }
                else -> {
                    if (it.width > visibleWidth) {
                        height = it.height * visibleWidth / it.width
                        width = visibleWidth
                    }
                    if (height > visibleHeight) {
                        width = width * visibleHeight / height
                        height = visibleHeight
                    }
                    if (durY + height > visibleHeight) {
                        textPages.last().height = durY
                        textPages.add(TextPage())
                        durY = 0f
                    }
                }
            }
            val textLine = TextLine(isImage = true)
            textLine.lineTop = durY
            durY += height
            textLine.lineBottom = durY
            val (start, end) = if (visibleWidth > width) {
                val adjustWidth = (visibleWidth - width) / 2f
                Pair(
                    paddingLeft.toFloat() + adjustWidth,
                    paddingLeft.toFloat() + adjustWidth + width
                )
            } else {
                Pair(paddingLeft.toFloat(), (paddingLeft + width).toFloat())
            }
            textLine.textChars.add(
                TextChar(
                    charData = src,
                    start = start,
                    end = end,
                    isImage = true
                )
            )
            textPages.last().textLines.add(textLine)
        }
        return durY + paragraphSpacing / 10f
    }

    /**
     * 排版文字
     */
    private fun setTypeText(
        text: String,
        y: Float,
        textPages: ArrayList<TextPage>,
        stringBuilder: StringBuilder,
        isTitle: Boolean,
        textPaint: TextPaint
    ): Float {
        var durY = if (isTitle) y + titleTopSpacing else y
        val layout = TextProcess(text,textPaint)
        for (lineIndex in 0 until layout.lineCount) {
            val textLine = TextLine(isTitle = isTitle)
            val words =
                text.substring(layout.getLineStart(lineIndex), layout.getLineEnd(lineIndex))
            val desiredWidth = layout.getLineWidth(lineIndex)
            var isLastLine = false
            val lineCompressMod = layout.getLineCompressMod(lineIndex)

            if (lineIndex == 0 && layout.lineCount > 1 && !isTitle) {
                //第一行
                textLine.text = words
                addCharsToLineFirst(textLine, words.toStringArray(), textPaint, desiredWidth ,lineCompressMod)
            } else if (lineIndex == layout.lineCount - 1) {
                //最后一行
                textLine.text = "$words\n"
                isLastLine = true
                val x = if (isTitle && ReadBookConfig.titleMode == 1)
                    (visibleWidth - layout.getLineWidth(lineIndex)) / 2
                else 0f
                addCharsToLineLast(textLine, words.toStringArray(), textPaint, x,desiredWidth ,lineCompressMod)
            } else {
                //中间行
                textLine.text = words
                addCharsToLineMiddle(textLine, words.toStringArray(), textPaint, desiredWidth, 0f,lineCompressMod)
            }
            if (durY + textPaint.textHeight > visibleHeight) {
                //当前页面结束,设置各种值
                textPages.last().text = stringBuilder.toString()
                textPages.last().height = durY
                //新建页面
                textPages.add(TextPage())
                stringBuilder.clear()
                durY = 0f
            }
            stringBuilder.append(words)
            if (isLastLine) stringBuilder.append("\n")
            textPages.last().textLines.add(textLine)
            textLine.upTopBottom(durY, textPaint)
            durY += textPaint.textHeight * lineSpacingExtra / 10f
            textPages.last().height = durY
        }
        if (isTitle) durY += titleBottomSpacing
        durY += textPaint.textHeight * paragraphSpacing / 10f
        return durY
    }

    /**
     * 有缩进,两端对齐
     */
    private fun addCharsToLineFirst(
        textLine: TextLine,
        words: Array<String>,
        textPaint: TextPaint,
        desiredWidth: Float,
        lineCompressMod:Int,
    ) {
        var x = 0f
        if (!ReadBookConfig.textFullJustify) {
            addCharsToLineLast(textLine, words, textPaint, x,desiredWidth ,lineCompressMod)
            return
        }
        val bodyIndent = ReadBookConfig.paragraphIndent
        val icw = StaticLayout.getDesiredWidth(bodyIndent, textPaint) / bodyIndent.length
        val d = getDefinterval(textPaint)
        var num = 0
        bodyIndent.toStringArray().forEach {
            val x1 = x + icw + d
            num++
            textLine.addTextChar(charData = it, start = paddingLeft + x, end = paddingLeft + x1)
            x = x1
        }
        val words1 = words.copyOfRange(bodyIndent.length, words.size)
        addCharsToLineMiddle(textLine, words1, textPaint, desiredWidth, x,lineCompressMod)
    }

    /**
     * 无缩进,两端对齐
     */
    private fun addCharsToLineMiddle(
        textLine: TextLine,
        words: Array<String>,
        textPaint: TextPaint,
        desiredWidth: Float,
        startX: Float,
        lineCompressMod:Int,
    ) {
        if (!ReadBookConfig.textFullJustify) {
            addCharsToLineLast(textLine, words, textPaint, startX,desiredWidth ,lineCompressMod)
            return
        }
        var d:Float
        var interval:Float
        if(lineCompressMod>TextProcess.CPS_MOD_NULL){
            val gapCount: Int =  words.lastIndex - 1
            val endPuncWidth = StaticLayout.getDesiredWidth(words.get(gapCount+1), textPaint)
            interval = visibleWidth - desiredWidth + endPuncWidth
            d = interval / gapCount
        }else{
            val gapCount: Int =  words.lastIndex
            interval = visibleWidth - desiredWidth
            d = interval / gapCount
        }
        /*间隔太大左对齐*/
        if(interval > (visibleWidth/6)){
            addCharsToLineLast(textLine, words, textPaint, startX,desiredWidth ,lineCompressMod)
            return
        }
        wordsProcess(textLine,words,textPaint,startX,lineCompressMod,d);
    }

    /**
     * 最后一行,自然排列
     */
    private fun addCharsToLineLast(
        textLine: TextLine,
        words: Array<String>,
        textPaint: TextPaint,
        startX: Float,
        desiredWidth: Float,
        lineCompressMod:Int,
    ) {
        var interval:Float
        var gapCount:Int
        if(lineCompressMod>TextProcess.CPS_MOD_NULL){
            gapCount =  words.lastIndex - 1
            val endPuncWidth = StaticLayout.getDesiredWidth(words.get(gapCount+1), textPaint)
            interval = visibleWidth - desiredWidth + endPuncWidth
        }else{
            gapCount =  words.lastIndex
            interval = visibleWidth - desiredWidth
        }
        /*目前改的不算严格意义的左对齐。会根据设置行宽做间隔叠加，保证上下行效果和两边间隔一致*/
        /*存在半角字符情况下依靠中文算出的默认间隔会越界*/
        val d = min((interval / gapCount),(getDefinterval(textPaint)))
        wordsProcess(textLine,words,textPaint,startX,lineCompressMod,d);
    }
//TODO 这里lineCompressMod写的太繁琐。接口不够抽象，后面要在TextProcess里做掉。-hoodie13
    private fun wordsProcess(
        textLine: TextLine,
        words: Array<String>,
        textPaint: TextPaint,
        startX: Float,
        lineCompressMod:Int,
        d: Float
    ){
        var x = startX
        words.forEachIndexed {index, s ->
            val cw = StaticLayout.getDesiredWidth(s, textPaint)
            var x1:Float
            when(lineCompressMod){
                TextProcess.CPS_MOD_1->{
                    if(index == (words.lastIndex - 1))  x1 = x + (cw/2) else if(index == words.lastIndex) x1 = x + cw else x1 = x + cw + d
                }
                TextProcess.CPS_MOD_2->{
                    if(index == (words.lastIndex - 2)){
                        x  = x - (cw/2)
                        x1 = x + cw
                    }
                    else if(index == (words.lastIndex - 1)){
                        x  = x - (cw/2)
                        x1 = x + cw + d
                    }else if(index == words.lastIndex){
                        x1 = x + cw
                    }
                    else{
                        x1 = x + cw + d
                    }
                }
                TextProcess.CPS_MOD_3->{
                    if(index == (words.lastIndex - 2)){
                        x  = x - (cw/2)
                        x1 = x + cw
                    }
                    else if(index == (words.lastIndex - 1)){
                        x1 = x + cw + d
                    }
                    else if(index == words.lastIndex){
                        x1 = x + cw
                    }
                    else{
                        x1 = x + cw + d
                    }
                }
                else->{
                    x1 = if(index != words.lastIndex) (x + cw + d) else (x + cw)
                }
            }
            textLine.addTextChar(charData = s, start = paddingLeft + x, end = paddingLeft + x1)
            x = x1
        }
    }

    private fun getDefinterval(textPaint: TextPaint):Float{
        val defCharWidth = StaticLayout.getDesiredWidth("我",textPaint)
        val f = (visibleWidth / defCharWidth).toInt().toFloat()
        val defD = (visibleWidth % defCharWidth) / f
        return defD
    }
    /**
     * 超出边界处理
     */
    private fun exceed(textLine: TextLine, words: Array<String>) {
        val endX = textLine.textChars.last().end
        if (endX > visibleRight) {
            val cc = (endX - visibleRight) / words.size
            for (i in 0..words.lastIndex) {
                textLine.getTextCharReverseAt(i).let {
                    val py = cc * (words.size - i)
                    it.start = it.start - py
                    it.end = it.end - py
                }
            }
        }
    }

    /**
     * 更新样式
     */
    fun upStyle() {
        typeface = getTypeface(ReadBookConfig.textFont)
        getPaint(typeface).let {
            titlePaint = it.first
            contentPaint = it.second
        }
        //间距
        lineSpacingExtra = ReadBookConfig.lineSpacingExtra
        paragraphSpacing = ReadBookConfig.paragraphSpacing
        titleTopSpacing = ReadBookConfig.titleTopSpacing.dp
        titleBottomSpacing = ReadBookConfig.titleBottomSpacing.dp
        upVisibleSize()
    }

    private fun getTypeface(fontPath: String): Typeface {
        return kotlin.runCatching {
            when {
                fontPath.isContentScheme() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                    val fd = App.INSTANCE.contentResolver
                        .openFileDescriptor(Uri.parse(fontPath), "r")!!
                        .fileDescriptor
                    Typeface.Builder(fd).build()
                }
                fontPath.isContentScheme() -> {
                    Typeface.createFromFile(RealPathUtil.getPath(App.INSTANCE, Uri.parse(fontPath)))
                }
                fontPath.isNotEmpty() -> Typeface.createFromFile(fontPath)
                else -> when (AppConfig.systemTypefaces) {
                    1 -> Typeface.SERIF
                    2 -> Typeface.MONOSPACE
                    else -> Typeface.SANS_SERIF
                }
            }
        }.getOrElse {
            ReadBookConfig.textFont = ""
            ReadBookConfig.save()
            Typeface.SANS_SERIF
        }
    }

    private fun getPaint(typeface: Typeface): Pair<TextPaint, TextPaint> {
        // 字体统一处理
        val bold = Typeface.create(typeface, Typeface.BOLD)
        val normal = Typeface.create(typeface, Typeface.NORMAL)
        val (titleFont, textFont) = when (ReadBookConfig.textBold) {
            1 -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                    Pair(Typeface.create(typeface, 900, false), bold)
                else
                    Pair(bold, bold)
            }
            2 -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                    Pair(normal, Typeface.create(typeface, 300, false))
                else
                    Pair(normal, normal)
            }
            else -> Pair(bold, normal)
        }

        //todo 标题
        val tPaint = TextPaint()
        tPaint.color = ReadBookConfig.textColor
        tPaint.letterSpacing = ReadBookConfig.letterSpacing
        tPaint.typeface = titleFont
        tPaint.textSize = with(ReadBookConfig) { textSize + titleSize }.sp.toFloat()
        tPaint.isAntiAlias = true
        //正文
        val cPaint = TextPaint()
        cPaint.color = ReadBookConfig.textColor
        cPaint.letterSpacing = ReadBookConfig.letterSpacing
        cPaint.typeface = textFont
        cPaint.textSize = ReadBookConfig.textSize.sp.toFloat()
        cPaint.isAntiAlias = true
        return Pair(tPaint, cPaint)
    }


    /**
     * 更新View尺寸
     */
    fun upViewSize(readConfigChage:Boolean,width: Int, height: Int) {
        if (width > 0 && height > 0 && (readConfigChage == true || width != viewWidth || height != viewHeight)) {
            viewWidth = width
            viewHeight = height
            upVisibleSize()
            postEvent(EventBus.UP_CONFIG, false)
        }
    }

    /**
     * 更新绘制尺寸
     */
    private fun upVisibleSize() {
        if (viewWidth > 0 && viewHeight > 0) {
            paddingLeft = ReadBookConfig.paddingLeft.dp
            paddingTop = ReadBookConfig.paddingTop.dp
            visibleWidth = viewWidth - paddingLeft - ReadBookConfig.paddingRight.dp
            visibleHeight = viewHeight - paddingTop - ReadBookConfig.paddingBottom.dp
            visibleRight = paddingLeft + visibleWidth
            visibleBottom = paddingTop + visibleHeight
        }
    }

    val TextPaint.textHeight: Float
        get() = fontMetrics.descent - fontMetrics.ascent + fontMetrics.leading
}

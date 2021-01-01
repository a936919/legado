package io.legado.app.ui.book.read.page.provider

import android.text.StaticLayout
import android.text.TextPaint
import android.util.Log
import io.legado.app.utils.toStringArray

/*
* 针对中文的断行排版处理-by hoodie13
* 因为StaticLayout对标点处理不符合国人习惯，参考其接口方式自定义排版，减少原代码差异
* 接口封的不抽象，数组用的也琐碎，因目前语法不熟悉，后面完善。
* */
class TextProcess(
        text:String,
        textPaint: TextPaint,
)  {
    var lineStart = IntArray(100)
    var lineEnd =IntArray(100)
    var lineWidth = FloatArray(100)
    var compressWidth =FloatArray(100)
    var lineCompressMod = IntArray(100)
    var CompressCharIndex =IntArray(100)
    var lineCount = 0
    enum class BreakMod{NORMAL, BREAK_ONE_CHAR, BREAK_MORE_CHAR, CPS_1, CPS_2, CPS_3,}
    companion object{
        const val CPS_MOD_NULL = 0
        const val CPS_MOD_1 = 1
        const val CPS_MOD_2 = 2
        const val CPS_MOD_3 = 3
    }

    init{
        var line = 0
        var words = text.toStringArray()
        var lineW = 0f
        var cwPre = 0f

        words.forEachIndexed { index, s ->
            val cw = StaticLayout.getDesiredWidth(s, textPaint)
            var lindexTag:BreakMod
            var breakLine = false
            lineW = lineW + cw
            var offset = 0f
            var breakCharCnt = 0

            if(lineW > ChapterProvider.visibleWidth) {
                /*禁止在行尾的标点处理*/
                if (index >= 1 && banEndOfLine(words[index - 1])) {
                    if (index >= 2 && banEndOfLine(words[index - 2])) lindexTag = BreakMod.CPS_2//如果后面还有一个禁首标点则异常
                    else lindexTag = BreakMod.BREAK_ONE_CHAR //无异常场景
                }
                /*禁止在行首的标点处理*/
                else if (banStartOfLine(words[index])) {
                    if (index >= 1 && banStartOfLine(words[index - 1])) lindexTag = BreakMod.CPS_1//如果后面还有一个禁首标点则异常，不过三个连续行尾标点的用法不通用
                    else if (index >= 2 && banEndOfLine(words[index - 2])) lindexTag = BreakMod.CPS_3//如果后面还有一个禁首标点则异常
                    else lindexTag = BreakMod.BREAK_ONE_CHAR //无异常场景
                } else {
                    lindexTag = BreakMod.NORMAL //无异常场景
                }

                /*判断上述逻辑解决不了的特殊情况*/
                var reCheck = false
                var breakIndex = 0
                if(lindexTag==BreakMod.CPS_1&&(Incompressible(words[index])||Incompressible(words[index-1]))) reCheck= true

                if(lindexTag==BreakMod.CPS_2&&(Incompressible(words[index-1])||Incompressible(words[index-2]))) reCheck= true

                if(lindexTag==BreakMod.CPS_3&&(Incompressible(words[index])||Incompressible(words[index-2]))) reCheck= true

                if(lindexTag>BreakMod.BREAK_MORE_CHAR&& index<words.lastIndex &&banStartOfLine(words[index + 1]))  reCheck= true


                /*特殊标点使用难保证显示效果，所以不考虑间隔，直接查找到能满足条件的分割字*/
                if(reCheck == true && index>2){
                    lindexTag = BreakMod.NORMAL
                    for(i in (index) downTo 1 ){
                        if(i==index){
                            breakIndex = 0
                            cwPre = 0f
                        }
                        else{
                            breakIndex++
                            cwPre = cwPre + StaticLayout.getDesiredWidth(words[i], textPaint)
                        }
                        if(!banStartOfLine(words[i])&&!banEndOfLine(words[i - 1])) {
                            lindexTag = BreakMod.BREAK_MORE_CHAR
                            break
                        }
                    }
                }

                when (lindexTag) {
                    BreakMod.NORMAL -> {//模式0 正常断行
                        offset = cw
                        lineEnd[line] = index
                        lineCompressMod[line] = CPS_MOD_NULL
                        breakCharCnt = 1
                    }
                    BreakMod.BREAK_ONE_CHAR -> {//模式1 当前行下移一个字
                        offset = cw + cwPre
                        lineEnd[line] = index - 1
                        lineCompressMod[line] = CPS_MOD_NULL
                        breakCharCnt = 2
                    }
                    BreakMod.BREAK_MORE_CHAR -> {//模式2 当前行下移多个字
                        offset = cw + cwPre
                        lineEnd[line] = index - breakIndex
                        lineCompressMod[line] = CPS_MOD_NULL
                        breakCharCnt = breakIndex + 1
                    }
                    BreakMod.CPS_1 -> {//模式3 两个后缀标点压缩
                        offset = 0f
                        lineEnd[line] = index + 1
                        lineCompressMod[line] = CPS_MOD_1
                        CompressCharIndex[line] = index - 1
                        breakCharCnt = 0
                    }
                    BreakMod.CPS_2 -> { //模式4 标点压缩
                        offset = 0f
                        lineEnd[line] = index + 1
                        lineCompressMod[line] = CPS_MOD_2
                        CompressCharIndex[line] = index - 2
                        breakCharCnt = 0
                    }
                    BreakMod.CPS_3 -> {//模式5 标点压缩
                        offset = 0f
                        lineEnd[line] = index + 1
                        lineCompressMod[line] = CPS_MOD_3
                        CompressCharIndex[line] = index - 2
                        breakCharCnt = 0
                    }
                }
                breakLine = true
            }

            if (line >= 99) Log.e("TextProcess", "line is Max $line")
            if (words[index] == "\n") Log.d("mq-1", "have break $s $index")

            /*当前行写满情况下的断行*/
            if (breakLine == true) {
                lineWidth[line] = lineW - offset
                lineStart[line + 1] = lineEnd[line]
                lineW = offset
                line++
                if (lineCompressMod[line] > 0) compressWidth[line] = lineWidth[line] - cw
                else compressWidth[line] = lineWidth[line]
            }
            /*已到最后一个字符*/
            if ((words.lastIndex) == index) {
                if (breakLine == false) {
                    offset = 0f
                    lineEnd[line] = index + 1
                    lineWidth[line] = lineW - offset
                    lineW = offset
                    line++
                }
                /*写满断行、段落末尾、且需要下移字符，这种特殊情况下要额外多一行*/
                else if (breakCharCnt > 0) {
                    lineEnd[line] = lineStart[line] + breakCharCnt
                    lineWidth[line] = lineW
                    line++
                }
            }
            cwPre = cw
        }

        lineCount = line

        if(false){
            for(i in 0 until line){
                val s = lineStart[i]
                val e = lineEnd[i]
                val t = text.substring(s, e)
                Log.d("mq-1","-----------------------line Info---------------------------")
                Log.d("mq-1","line: $i lineS:${lineStart[i]} lineE:${lineEnd[i]} width: ${e-s} lineWidth: ${lineWidth[i]} compresMod: ${lineCompressMod[i]}")
                Log.d("mq-1","$t")
                Log.d("mq-1","-----------------------line Info---------------------------")
            }
        }
    }

    private fun banStartOfLine(string: String):Boolean{
        val panc = arrayOf("，","。","：","？","！","、","”","’","）","》","}","】",")",">","]","}",",",".","?","!",":","」")
        panc.forEach{
            if(it == string) return true
        }
        return false
    }

    private fun banEndOfLine(string: String):Boolean{
        val panc = arrayOf("“","（","《","【","‘","‘","(","<","[","{","「")
        panc.forEach{
            if(it == string) return true
        }
        return false
    }

    private fun Incompressible(string: String):Boolean{
        val panc = arrayOf("!","(",")",",",".","?",":",";","'","`","[","]","{","}","<",">")
        panc.forEach{
            if(it == string) return true
        }
        return false
    }

    fun getLineStart(line:Int):Int{
        return lineStart[line]
    }
    fun getLineEnd(line:Int):Int{
        return lineEnd[line]
    }
    fun getLineWidth(line:Int):Float{
        return  lineWidth[line]
    }
    fun getLineCompressMod(line:Int):Int{
        return  lineCompressMod[line]
    }
    fun getCompressChar(line:Int):Int{
        return  CompressCharIndex[line]
    }
    fun getCompressWidth(line:Int):Float{
        if(lineCompressMod[line]>0){
            return compressWidth[line]
        }
        else{
            return lineWidth[line]
        }
    }
}
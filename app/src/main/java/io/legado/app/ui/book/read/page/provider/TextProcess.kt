package io.legado.app.ui.book.read.page.provider

import android.text.StaticLayout
import android.text.TextPaint
import android.util.Log
import io.legado.app.utils.toStringArray


class TextProcess(
        text:String,
        textPaint: TextPaint,
)  {
    var lineStart = Array<Int>(100,{it->0})
    var lineEnd =Array<Int>(100,{it->0})
    var lineWidth =Array<Float>(100,{it->0f})
    var compressWidth =Array<Float>(100,{it->0f})
    var lineCompressMod = Array<Int>(100,{it->0})
    var CompressCharIndex = Array<Int>(100,{it->0})
    var lineCount = 0

    init{
        var line = 0
        var words = text.toStringArray()
        var lineW = 0f
        var lindexTag = 0
        lineStart = Array(100,{it->0})
        lineEnd =Array(100,{it->0})
        lineCompressMod = Array(100,{it->0})
        CompressCharIndex = Array<Int>(100,{it->0})
        lineStart[line] = 0
        var cwPre = 0f
        var breakLine = false

        words.forEachIndexed { index, s ->
            val cw = StaticLayout.getDesiredWidth(s, textPaint)
            breakLine = false
            lineW = lineW + cw
            var offset = 0f
            var breakCharCnt = 0

            if(lineW > ChapterProvider.visibleWidth) {
                /*禁止在行尾的标点处理 当同时出现2个的情况还没遇到，先不做处理*/
                if(banEndOfLine(words[index-1])) {
                    lindexTag = 1
                }
                /*禁止在行首的标点处理*/
                else if(banStartOfLine(words[index])) {
                    if(banStartOfLine(words[index-1])) lindexTag = 2 else lindexTag = 1
                }
                else {
                    lindexTag = 0
                }

                when(lindexTag){
                    //模式0 正常断行
                    0->{
                        offset = cw
                        lineEnd[line] = index
                        breakCharCnt = 1
                        breakLine = true
                    }//模式1 当前行下移一个字
                    1->{
                        offset = cw + cwPre
                        lineEnd[line] = index - 1
                        breakCharCnt = 2
                        breakLine = true
                    }
                    //模式2 标点压缩
                    2->{
                        offset = 0f
                        lineEnd[line] = index + 1
                        lineCompressMod[line] = 1
                        CompressCharIndex[line] = index - 1
                        breakCharCnt = 0
                        breakLine = true
                    }
                    //模式3 标点压缩 且上移一个字符 针对双前置标点压缩的情况，暂时没有用
                    3->{
                        offset = 0f
                        lineEnd[line] = index + 1
                        lineCompressMod[line] = 2
                        CompressCharIndex[line] = index - 2
                        breakCharCnt = 0
                        breakLine = true
                    }
                }
                if(line >= 99) Log.e("TextProcess","line is Max $line")
            }

            if(words[index]== "\n") Log.d("mq-1","have break $s $index")
            //Log.d("mq-1","char:$s index:$index ${words.lastIndex} ")

            /*当前行写满情况下的断行*/
            if(breakLine == true) {
                lineWidth[line] = lineW - offset
                lineStart[line+1]=lineEnd[line]
                lineW = offset
                line++
                if(lineCompressMod[line]>0)  compressWidth[line] = lineWidth[line] - cw else compressWidth[line] = lineWidth[line]
            }
            /*已到最后一个字符*/
            if((words.lastIndex) == index) {
                if(breakLine == false){
                    offset = 0f
                    lineEnd[line] = index + 1
                    lineWidth[line] = lineW - offset
                    lineW = offset
                    line++
                }
                /*写满断行、段落末尾、且需要下移字符，这种特殊情况下要额外多一行*/
                else if(breakCharCnt>0){
                    lineEnd[line] = lineStart[line] + breakCharCnt
                    lineWidth[line] = lineW
                    line++
                }
            }
            cwPre = cw
        }
/*
        for(i in 0 until line){
            val s = lineStart[i]
            val e = lineEnd[i]
            val t = text.substring(s, e)
            Log.d("mq-1","-----------------------line Info---------------------------")
            Log.d("mq-1","line: $i lineS:${lineStart[i]} lineE:${lineEnd[i]} width: ${e-s} lineWidth: ${lineWidth[i]}")
            Log.d("mq-1","$t")
            Log.d("mq-1","-----------------------line Info---------------------------")
        }*/

        lineCount = line
    }

    private fun banStartOfLine(string: String):Boolean{
        val panc = arrayOf("，","。","：","？","！","、","”","’","）","》","}","】",")",">","]","}",",",".","?","!",":")
        panc.forEach{
            if(it == string) return true
        }
        return false
    }

    private fun banEndOfLine(string: String):Boolean{
        val panc = arrayOf("“","（","《","{","【","‘","(","<","[","")
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
package io.legado.app.ui.book.read.page.provider

import android.text.StaticLayout
import android.text.TextPaint
import android.util.Log
import io.legado.app.utils.toStringArray


class TextProcess(
        text:String,
        textPaint: TextPaint,
)  {
    var lineStart = Array(100,{it->0})
    var lineEnd =Array(100,{it->0})
    var lineWidth =Array(100,{it->0f})
    var lineCompressMod = Array(100,{it->0})
    var lineCount = 0

    init{
        var line = 0
        var words = text.toStringArray()
        var lineW = 0f
        var lindexTag = 0
        lineStart = Array(100,{it->0})
        lineEnd =Array(100,{it->0})
        lineCompressMod = Array(100,{it->0})
        lineStart[line] = 0
        var cwPre = 0f

        words.forEachIndexed { index, s ->
            val cw = StaticLayout.getDesiredWidth(s, textPaint)
            lineW = lineW + cw

            Log.d("debug2","$lineW ${ChapterProvider.visibleWidth} $lineW $cw")
            if(lineW > ChapterProvider.visibleWidth) {
                //标点不能在行尾
                if(banEndOfLine(words[index-1])) {
                    lindexTag = 1
                }
                //标点不能在行首
                else if(banStartOfLine(words[index])) {
                    lindexTag = 1
                }
                //标点需要压缩
                else if(mayCompress(words[index])){
                    if(banEndOfLine(words[index-1])){
                        lindexTag = 2
                    }
                    else{
                        lindexTag = 1
                    }
                }
                else {
                    lindexTag = 0
                }
                when(lindexTag){
                    //模拟0 正常断行
                    0->{
                        lineWidth[line] = lineW-cw
                        lineW = cw
                        lineEnd[line] = index
                        line++
                        lineStart[line]= index
                    }//模式1 当前行下移一个字
                    1->{
                        lineWidth[line] = lineW - cw - cwPre
                        lineW = cw + cwPre
                        lineEnd[line] = index - 1
                        line++
                        lineStart[line]= index - 1
                    }
                    //模式2 标点压缩
                    2->{
                        lineWidth[line] = lineW
                        lineEnd[line] = index + 1
                        lineCompressMod[line] = 1
                        line++
                        lineStart[line]= index + 1
                    }
                }
                if(line >= 99) Log.e("TextProcess","line is Max $line")
            }

            if((words.lastIndex) == index) {
                Log.d("debug2","${s} $index")
                lineWidth[line] = lineW
                lineEnd[line] = words.lastIndex + 1
                line++
            }
            Log.d("debug2","${s} $index ${words.lastIndex} ")
            cwPre = cw
        }

        lineCount = line
        //Log.d("debug1","lineCount is $lineCount ${lineEnd[line]}")
        for(i in 0 until lineCount){
            val s = lineStart[i]
            val e = lineEnd[i]
            val t = text.substring(s, e)
            Log.d("debug2","line: $i ${e-s} $t")
        }
    }

    private fun banStartOfLine(string: String):Boolean{
        val panc = arrayOf("，","。","：","？","！","、")
        panc.forEach{
            if(it == string) return true
        }
        return false
    }

    private fun banEndOfLine(string: String):Boolean{
        val panc = arrayOf("“","（","《","{","【")
        panc.forEach{
            if(it == string) return true
        }
        return false
    }

    private fun mayCompress(string: String):Boolean{
        val panc = arrayOf("”","）","》","}","】")
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
}
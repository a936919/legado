package io.legado.app.ui.book.read.page.provider

import android.text.StaticLayout
import android.text.TextPaint
import android.util.Log
import io.legado.app.utils.toStringArray


class TextProcess(
        text:String,
        textPaint: TextPaint,
)  {
    var lineStar = Array(100,{it->0})
    var lineEnd =Array(100,{it->0})
    var lineWith =Array(100,{it->0f})
    var lineYS = Array(100,{it->false})
    var lineCount = 0

    init{
        var line = 0
        var words = text.toStringArray()
        var lineW = 0f
        lineStar = Array(100,{it->0})
        lineEnd =Array(100,{it->0})
        lineYS = Array(100,{it->false})
        lineStar[line] = 0

        //Log.d("debug2","start debug")
        words.forEachIndexed { index, s ->
            val cw = StaticLayout.getDesiredWidth(s, textPaint)
            lineW = lineW + cw
            //Log.d("debug2","$s $cw $line $index $lineW $visibleWidth")

            if(lineW > ChapterProvider.visibleWidth) {
                //标点不能在行尾
                if(words[index-1] == "“") {
                    lineEnd[line] = index-1
                    line++
                    lineStar[line]= index-1
                }
                //标点不能在行首
                else if(words[index] == "，"
                        || words[index] == "。"
                        || words[index] == "："
                        || words[index] == "？"
                        || words[index] == "！") {
                    lineEnd[line] = index-1
                    line++
                    lineStar[line]= index-1
                }
                else if(words[index] == "”"){
                    if(words[index-1] == "，"
                            || words[index] == "。"
                            || words[index] == "："
                            || words[index] == "？"
                            || words[index] == "！") {
                        lineEnd[line] = index
                        line++
                        lineStar[line]= index
                        lineYS[line] = true
                    }
                    else{
                        lineEnd[line] = index-1
                        line++
                        lineStar[line] = index-1
                    }
                }
                else {
                    lineEnd[line] = index
                    line++
                    lineStar[line]= index
                }
                lineWith[line] = lineW -cw
                lineW = cw
                if(line >= 99)
                {
                    Log.e("TextProcess","line is Max $line")
                }
            }
        }
        lineEnd[line] = words.lastIndex+1
        line++
        lineCount = line
        for(i in 0 until  line){
            val s = lineStar[i]
            val e = lineEnd[i]
            val t = text.substring(s, e)
            Log.d("debug2","line: $i ${e-s} $t")
        }

    }
    fun getLineStart(line:Int):Int{
        return lineStar[line]
    }
    fun getLineEnd(line:Int):Int{
        return lineEnd[line]
    }
    fun getLineWidth(line:Int):Float{
        return  lineWith[line]
    }
    fun getLineYs(line:Int):Boolean{
        return  lineYS[line]
    }
}
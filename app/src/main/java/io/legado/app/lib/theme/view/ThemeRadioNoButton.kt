package io.legado.app.lib.theme.view

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatRadioButton
import io.legado.app.R
import io.legado.app.lib.theme.*
import io.legado.app.utils.ColorUtils
import io.legado.app.utils.dp
import io.legado.app.utils.getCompatColor

/**
 * @author Aidan Follestad (afollestad)
 */
class ThemeRadioNoButton(context: Context, attrs: AttributeSet) :
    AppCompatRadioButton(context, attrs) {

    private val isBottomBackground: Boolean

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ATERadioNoButton)
        isBottomBackground =
            typedArray.getBoolean(R.styleable.ATERadioNoButton_isBottomBackground, false)
        typedArray.recycle()
        initTheme()
    }

    fun initTheme() {
        when {
            isInEditMode -> Unit
            isBottomBackground -> {
                val textColor = context.readCfgBottomText
                background = Selector.shapeBuild()
                    .setCornerRadius(1.dp)
                    .setStrokeWidth(1.dp)
                    .setCheckedBgColor(context.accentColor)
                    .setCheckedStrokeColor(context.accentColor)
                    .setDefaultStrokeColor(ColorUtils.withAlpha(textColor,0.5f))
                    .create()
                setTextColor(
                    Selector.colorBuild()
                        .setDefaultColor(textColor)
                        .setCheckedColor(context.getPrimaryTextColor(ColorUtils.isColorLight(context.accentColor)))
                        .create()
                )
            }
            else -> {
                val textColor = context.getCompatColor(R.color.primaryText)
                background = Selector.shapeBuild()
                    .setCornerRadius(2.dp)
                    .setStrokeWidth(2.dp)
                    .setCheckedBgColor(context.accentColor)
                    .setCheckedStrokeColor(context.accentColor)
                    .setDefaultStrokeColor(textColor)
                    .create()
                setTextColor(
                    Selector.colorBuild()
                        .setDefaultColor(textColor)
                        .setCheckedColor(context.getPrimaryTextColor(ColorUtils.isColorLight(context.accentColor)))
                        .create()
                )
            }
        }

    }

}

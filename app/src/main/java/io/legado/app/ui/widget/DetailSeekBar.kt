package io.legado.app.ui.widget

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.SeekBar
import io.legado.app.R
import io.legado.app.databinding.ViewDetailSeekBarBinding
import io.legado.app.lib.theme.Selector
import io.legado.app.lib.theme.readCfgBottomText
import io.legado.app.ui.widget.seekbar.SeekBarChangeListener
import io.legado.app.utils.ColorUtils
import io.legado.app.utils.progressAdd
import org.jetbrains.anko.sdk27.listeners.onClick

class DetailSeekBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs),
    SeekBarChangeListener {
    private var binding: ViewDetailSeekBarBinding =
        ViewDetailSeekBarBinding.inflate(LayoutInflater.from(context), this, true)
    private val isBottomBackground: Boolean

    var valueFormat: ((progress: Int) -> String)? = null
    var onChanged: ((progress: Int) -> Unit)? = null
    var progress: Int
        get() = binding.seekBar.progress
        set(value) {
            binding.seekBar.progress = value
        }
    var max: Int
        get() = binding.seekBar.max
        set(value) {
            binding.seekBar.max = value
        }

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.DetailSeekBar)
        isBottomBackground =
            typedArray.getBoolean(R.styleable.DetailSeekBar_isBottomBackground, false)
        binding.tvSeekTitle.text = typedArray.getText(R.styleable.DetailSeekBar_title)
        binding.seekBar.max = typedArray.getInteger(R.styleable.DetailSeekBar_max, 0)
        typedArray.recycle()
        if (isBottomBackground && !isInEditMode) {
            upBackground()
        }
        binding.ivSeekPlus.onClick {
            binding.seekBar.progressAdd(1)
            onChanged?.invoke(binding.seekBar.progress)
        }
        binding.ivSeekReduce.onClick {
            binding.seekBar.progressAdd(-1)
            onChanged?.invoke(binding.seekBar.progress)
        }
        binding.seekBar.setOnSeekBarChangeListener(this)
    }

    fun upBackground(){
        val textColor = context.readCfgBottomText
        var colorList: ColorStateList = Selector.colorBuild()
                .setDefaultColor(textColor)
                .setPressedColor(ColorUtils.darkenColor(textColor))
                .create()
        binding.tvSeekTitle.setTextColor(textColor)
        binding.ivSeekPlus.setColorFilter(textColor)
        binding.ivSeekReduce.setColorFilter(textColor)
        binding.tvSeekValue.setTextColor(textColor)
        binding.seekBar.progressBackgroundTintList = colorList

    }

    private fun upValue(progress: Int = binding.seekBar.progress) {
        valueFormat?.let {
            binding.tvSeekValue.text = it.invoke(progress)
        } ?: let {
            binding.tvSeekValue.text = progress.toString()
        }
    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        upValue(progress)
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {

    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {
        onChanged?.invoke(binding.seekBar.progress)
    }

}
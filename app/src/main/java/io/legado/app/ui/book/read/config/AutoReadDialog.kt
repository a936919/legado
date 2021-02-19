package io.legado.app.ui.book.read.config

import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import android.widget.SeekBar
import io.legado.app.R
import io.legado.app.base.BaseDialogFragment
import io.legado.app.databinding.DialogAutoReadBinding
import io.legado.app.help.ReadBookConfig
import io.legado.app.lib.theme.readCfgBottomBg
import io.legado.app.lib.theme.readCfgBottomText
import io.legado.app.service.BaseReadAloudService
import io.legado.app.service.help.ReadAloud
import io.legado.app.ui.book.read.ReadBookActivity
import io.legado.app.ui.widget.seekbar.SeekBarChangeListener
import io.legado.app.utils.ColorUtils
import io.legado.app.utils.viewbindingdelegate.viewBinding


class AutoReadDialog : BaseDialogFragment() {
    var callBack: CallBack? = null

    private val binding by viewBinding(DialogAutoReadBinding::bind)

    override fun onStart() {
        super.onStart()
        dialog?.window?.let {
            it.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            it.setBackgroundDrawableResource(R.color.background)
            it.decorView.setPadding(0, 0, 0, 0)
            val attr = it.attributes
            attr.dimAmount = 0.0f
            attr.gravity = Gravity.BOTTOM
            it.attributes = attr
            it.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        (activity as ReadBookActivity).bottomDialog--
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as ReadBookActivity).bottomDialog++
        callBack = activity as? CallBack
        return inflater.inflate(R.layout.dialog_auto_read, container)
    }

    override fun onFragmentCreated(view: View, savedInstanceState: Bundle?) {
        val bg = requireContext().readCfgBottomBg
        val textColor = requireContext().readCfgBottomText
        val secondtextColor = ColorUtils.withAlpha(textColor,0.5f)
        binding.root.setBackgroundColor(bg)
        binding.tvReadSpeedTitle.setTextColor(textColor)
        binding.tvReadSpeed.setTextColor(textColor)
        binding.ivCatalog.setColorFilter(textColor)
        binding.tvCatalog.setTextColor(secondtextColor)
        binding.ivMainMenu.setColorFilter(textColor)
        binding.tvMainMenu.setTextColor(secondtextColor)
        binding.ivAutoPageStop.setColorFilter(textColor)
        binding.tvAutoPageStop.setTextColor(secondtextColor)
        binding.ivSetting.setColorFilter(textColor)
        binding.tvSetting.setTextColor(secondtextColor)
        initOnChange()
        initData()
        initEvent()
    }

    private fun initData() {
        val speed = if (ReadBookConfig.autoReadSpeed < 10) 10 else ReadBookConfig.autoReadSpeed
        binding.tvReadSpeed.text = String.format("%ds", speed)
        binding.seekAutoRead.progress = speed
    }

    private fun initOnChange() {
        binding.seekAutoRead.setOnSeekBarChangeListener(object : SeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val speed = if (progress < 10) 10 else progress
                binding.tvReadSpeed.text = String.format("%ds", speed)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                ReadBookConfig.autoReadSpeed =
                    if (binding.seekAutoRead.progress < 10) 10 else binding.seekAutoRead.progress
                upTtsSpeechRate()
            }
        })
    }

    private fun initEvent() {
        binding.llMainMenu.setOnClickListener {
            callBack?.showMenuBar()
            dismissAllowingStateLoss()
        }
        binding.llSetting.setOnClickListener {
            ReadAloudConfigDialog().show(childFragmentManager, "readAloudConfigDialog")
        }
        binding.llCatalog.setOnClickListener { callBack?.openChapterList() }
        binding.llAutoPageStop.setOnClickListener {
            callBack?.autoPageStop()
            dismissAllowingStateLoss()
        }
    }

    private fun upTtsSpeechRate() {
        ReadAloud.upTtsSpeechRate(requireContext())
        if (!BaseReadAloudService.pause) {
            ReadAloud.pause(requireContext())
            ReadAloud.resume(requireContext())
        }
    }

    interface CallBack {
        fun showMenuBar()
        fun openChapterList()
        fun autoPageStop()
    }
}
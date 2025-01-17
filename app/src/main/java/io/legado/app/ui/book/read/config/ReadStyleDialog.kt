package io.legado.app.ui.book.read.config

import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.annotation.RequiresApi
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.view.get
import io.legado.app.R
import io.legado.app.base.BaseDialogFragment
import io.legado.app.base.adapter.ItemViewHolder
import io.legado.app.base.adapter.RecyclerAdapter
import io.legado.app.constant.EventBus
import io.legado.app.databinding.DialogReadBookStyleBinding
import io.legado.app.databinding.ItemReadStyleBinding
import io.legado.app.help.ReadBookConfig
import io.legado.app.lib.dialogs.selector
import io.legado.app.lib.theme.accentColor
import io.legado.app.lib.theme.readCfgBottomBg
import io.legado.app.lib.theme.readCfgBottomText
import io.legado.app.ui.book.read.ReadBookActivity
import io.legado.app.lib.theme.bottomBackground
import io.legado.app.lib.theme.getPrimaryTextColor
import io.legado.app.model.ReadBook
import io.legado.app.ui.font.FontSelectDialog
import io.legado.app.utils.*
import io.legado.app.utils.viewbindingdelegate.viewBinding
import splitties.views.onLongClick

class ReadStyleDialog : BaseDialogFragment(R.layout.dialog_read_book_style),
    FontSelectDialog.CallBack {

    private val binding by viewBinding(DialogReadBookStyleBinding::bind)
    private val callBack get() = activity as? ReadBookActivity
    private lateinit var styleAdapter: StyleAdapter
    private lateinit var view:ItemReadStyleBinding

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    override fun onStart() {
        super.onStart()
        dialog?.window?.let {
            it.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            it.setElevation(0.0f)
            it.setBackgroundDrawableResource(R.color.background)
            it.decorView.setPadding(0, 0, 0, 0)
            val attr = it.attributes
            attr.dimAmount = 0.0f
            attr.gravity = Gravity.BOTTOM
            it.attributes = attr
            it.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }

    override fun onFragmentCreated(view: View, savedInstanceState: Bundle?) {
        (activity as ReadBookActivity).bottomDialog++
        initView()
        initReadCfgColor()
        initData()
        initViewEvent()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        ReadBookConfig.save()
        (activity as ReadBookActivity).bottomDialog--
    }

    private fun initView() =  binding.run{
        dsbTextSize.valueFormat = {
            (it + 5).toString()
        }
        dsbTextLetterSpacing.valueFormat = {
            ((it - 50) / 100f).toString()
        }
        dsbLineSize.valueFormat = { ((it - 10) / 10f).toString() }
        dsbParagraphSpacing.valueFormat = { (it / 10f).toString() }
        dsbBoldSize.valueFormat = {(it/10f).toString()}
        styleAdapter = StyleAdapter()
        rvStyle.adapter = styleAdapter
        styleAdapter.addFooterView {
            view = ItemReadStyleBinding.inflate(layoutInflater, it, false)
            view.apply {
                ivStyle.setPadding(6.dp, 6.dp, 6.dp, 6.dp)
                ivStyle.setText(null)
                ivStyle.setImageResource(R.drawable.ic_add)
                root.setOnClickListener {
                    ReadBookConfig.configList.add(ReadBookConfig.Config())
                    showBgTextConfig(ReadBookConfig.configList.lastIndex)
                }
            }
        }
    }

    private fun initReadCfgColor() = with(binding) {
        val bg = requireContext().readCfgBottomBg
        val textColor = requireContext().readCfgBottomText
        rootView.setBackgroundColor(bg)
        tvBgTs.setTextColor(textColor)
        tvShareLayout.setTextColor(textColor)
        tvTextFont.setTextColor(textColor)
        tvTextFont.upBackground()
        textFontWeightConverter.setTextColor(textColor)
        textFontWeightConverter.upBackground()
        tvTextIndent.setTextColor(textColor)
        tvTextIndent.upBackground()
        chineseConverter.setTextColor(textColor)
        chineseConverter.upBackground()
        tvPadding.setTextColor(textColor)
        tvPadding.upBackground()
        tvTip.setTextColor(textColor)
        tvTip.upBackground()
        dsbTextSize.upBackground()
        dsbBoldSize.upBackground()
        dsbLineSize.upBackground()
        dsbParagraphSpacing.upBackground()
        dsbTextLetterSpacing.upBackground()
        rbAnim0.initTheme()
        rbAnim1.initTheme()
        rbNoAnim.initTheme()
        rbNoAnim.initTheme()
        rbScrollAnim.initTheme()
        rbSimulationAnim.initTheme()
        if(::view.isInitialized){
            view.apply {
                ivStyle.setColorFilter(textColor)
                ivStyle.borderColor = textColor
            }
        }
    }

    private fun initData() {
        binding.cbShareLayout.isChecked = ReadBookConfig.shareLayout
        upView()
        ReadBookConfig.configList.sortBy { it.name }
        styleAdapter.setItems(ReadBookConfig.configList)
    }

    private fun initViewEvent() = binding.run {
        chineseConverter.onChanged {
            postEvent(EventBus.UP_CONFIG, true)
        }
        textFontWeightConverter.onChanged {
            postEvent(EventBus.UP_CONFIG, true)
        }
        tvTextFont.setOnClickListener {
            showDialogFragment<FontSelectDialog>()
        }
        tvTextIndent.setOnClickListener {
            context?.selector(
                title = getString(R.string.text_indent),
                items = resources.getStringArray(R.array.indent).toList()
            ) { _, index ->
                ReadBookConfig.paragraphIndent = "　".repeat(index)
                postEvent(EventBus.UP_CONFIG, true)
            }
        }
        tvPadding.setOnClickListener {
            dismissAllowingStateLoss()
            callBack?.showPaddingConfig()
        }
        tvTip.setOnClickListener {
            TipConfigDialog().show(childFragmentManager, "tipConfigDialog")
        }
        rgPageAnim.setOnCheckedChangeListener { _, checkedId ->
            ReadBook.book?.setPageAnim(-1)
            ReadBookConfig.pageAnim = binding.rgPageAnim.getIndexById(checkedId)
            callBack?.upPageAnim()
            ReadBook.loadContent(false)
        }
        cbShareLayout.onCheckedChangeListener = { _, isChecked ->
            ReadBookConfig.shareLayout = isChecked
            upView()
            postEvent(EventBus.UP_CONFIG, true)
        }
        dsbTextSize.onChanged = {
            ReadBookConfig.textSize = it + 5
            postEvent(EventBus.UP_CONFIG, true)
        }
        dsbTextLetterSpacing.onChanged = {
            ReadBookConfig.letterSpacing = (it - 50) / 100f
            postEvent(EventBus.UP_CONFIG, true)
        }
        dsbLineSize.onChanged = {
            ReadBookConfig.lineSpacingExtra = it
            postEvent(EventBus.UP_CONFIG, true)
        }
        dsbBoldSize.onChanged = {
            ReadBookConfig.boldSize = it/10f
            postEvent(EventBus.UP_CONFIG, true)
        }
        dsbParagraphSpacing.onChanged = {
            ReadBookConfig.paragraphSpacing = it
            postEvent(EventBus.UP_CONFIG, true)
        }
    }

    private fun changeBg(index: Int) {
        val oldIndex = ReadBookConfig.styleSelect
        if (index != oldIndex) {
            ReadBookConfig.styleSelect = index
            ReadBookConfig.upBg()
            upView()
            styleAdapter.notifyItemChanged(oldIndex)
            styleAdapter.notifyItemChanged(index)
            postEvent(EventBus.UP_CONFIG, true)
        }
    }

    private fun showBgTextConfig(index: Int): Boolean {
        dismissAllowingStateLoss()
        changeBg(index)
        callBack?.showBgTextConfig()
        return true
    }

    private fun upView() = binding.run {
        textFontWeightConverter.upUi(ReadBookConfig.textBold)
        ReadBook.pageAnim().let {
            if (it >= 0 && it < rgPageAnim.childCount) {
                rgPageAnim.check(rgPageAnim[it].id)
            }
        }
        ReadBookConfig.let {
            dsbTextSize.progress = it.textSize - 5
            dsbTextLetterSpacing.progress = (it.letterSpacing * 100).toInt() + 50
            dsbLineSize.progress = it.lineSpacingExtra
            dsbParagraphSpacing.progress = it.paragraphSpacing
            dsbBoldSize.progress = (it.boldSize*10).toInt()
        }
    }

    override val curFontPath: String
        get() = ReadBookConfig.textFont

    override fun selectFont(path: String) {
        if (path != ReadBookConfig.textFont) {
            ReadBookConfig.textFont = path
            postEvent(EventBus.UP_CONFIG, true)
        }
    }

    inner class StyleAdapter :
        RecyclerAdapter<ReadBookConfig.Config, ItemReadStyleBinding>(requireContext()) {

        override fun getViewBinding(parent: ViewGroup): ItemReadStyleBinding {
            return ItemReadStyleBinding.inflate(inflater, parent, false)
        }

        override fun convert(
            holder: ItemViewHolder,
            binding: ItemReadStyleBinding,
            item: ReadBookConfig.Config,
            payloads: MutableList<Any>
        ) {
            binding.apply {
                ivStyle.setText(item.name.ifBlank { "文字" })
                ivStyle.setTextColor(item.curTextColor())
                ivStyle.setImageDrawable(item.curBgDrawable(100, 150))
                if (ReadBookConfig.styleSelect == holder.layoutPosition) {
                    ivStyle.borderColor = accentColor
                    ivStyle.setTextBold(true)
                } else {
                    ivStyle.borderColor = item.curTextColor()
                    ivStyle.setTextBold(false)
                }
            }
        }

        override fun registerListener(holder: ItemViewHolder, binding: ItemReadStyleBinding) {
            binding.apply {
                ivStyle.setOnClickListener {
                    if (ivStyle.isInView) {
                        changeBg(holder.layoutPosition)
                        initReadCfgColor()
                    }
                }
                ivStyle.onLongClick(ivStyle.isInView) {
                    if (ivStyle.isInView) {
                        showBgTextConfig(holder.layoutPosition)
                    }
                }
            }
        }
    }
}
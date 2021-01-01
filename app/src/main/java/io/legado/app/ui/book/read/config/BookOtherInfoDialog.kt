package io.legado.app.ui.book.read.config

import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import io.legado.app.R
import io.legado.app.base.BaseDialogFragment
import io.legado.app.databinding.DialogReadBookOtherInfoBinding
import io.legado.app.lib.theme.readCfgBottomBg
import io.legado.app.lib.theme.readCfgBottomText
import io.legado.app.ui.book.read.ReadBookActivity
import io.legado.app.utils.ColorUtils
import io.legado.app.utils.viewbindingdelegate.viewBinding

class BookOtherInfoDialog : BaseDialogFragment()  {
    private var callBack: ReadAloudDialog.CallBack? = null
    private val binding by viewBinding(DialogReadBookOtherInfoBinding::bind)

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
        //callBack = activity as? CallBack
        return inflater.inflate(R.layout.dialog_read_book_other_info, container)
    }

    override fun onFragmentCreated(view: View, savedInstanceState: Bundle?) {
        val bg = requireContext().readCfgBottomBg
        val textColor = requireContext().readCfgBottomText
        val secondtextColor = ColorUtils.withAlpha(textColor,0.5f)
        binding.apply {
             root.setBackgroundColor(bg)
            bookName.setTextColor(textColor)
            bookAuthor.setTextColor(secondtextColor)
            readTime.setTextColor(textColor)
            readAllTime.setTextColor(textColor)

            ivBookMark.setColorFilter(textColor)
            ivReadAloud.setColorFilter(textColor)
            ivReadPage.setColorFilter(textColor)
            ivSearch.setColorFilter(textColor)
            ivAccessUrl.setColorFilter(textColor)
            ivDownload.setColorFilter(textColor)
            ivGetProgress.setColorFilter(textColor)
            ivRefresh.setColorFilter(textColor)

            tvBookMark.setTextColor(secondtextColor)
            tvReadAloud.setTextColor(secondtextColor)
            tvReadPage.setTextColor(secondtextColor)
            tvSearch.setTextColor(secondtextColor)
            tvAccessUrl.setTextColor(secondtextColor)
            tvDownload.setTextColor(secondtextColor)
            tvGetProgress.setTextColor(secondtextColor)
            tvRefresh.setTextColor(secondtextColor)
        }
    }

    interface CallBack {
        fun showMenuBar()
    }
}
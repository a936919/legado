package io.legado.app.ui.book.read.config

import android.content.DialogInterface
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.*
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import io.legado.app.App
import io.legado.app.R
import io.legado.app.base.BaseDialogFragment
import io.legado.app.data.entities.Book
import io.legado.app.data.entities.BookProgress
import io.legado.app.databinding.DialogReadBookOtherInfoBinding
import io.legado.app.help.BlurTransformation
import io.legado.app.help.ImageLoader
import io.legado.app.lib.theme.readCfgBottomBg
import io.legado.app.lib.theme.readCfgBottomText
import io.legado.app.service.help.ReadBook
import io.legado.app.ui.book.info.BookInfoActivity
import io.legado.app.ui.book.info.BookInfoViewModel
import io.legado.app.ui.book.read.ReadBookActivity
import io.legado.app.ui.book.read.page.entities.TextPage
import io.legado.app.ui.book.search.SearchActivity
import io.legado.app.ui.widget.image.CoverImageView
import io.legado.app.utils.*
import io.legado.app.utils.viewbindingdelegate.viewBinding
import org.jetbrains.anko.sdk27.listeners.onClick
import org.jetbrains.anko.startActivity

class BookOtherInfoDialog : BaseDialogFragment()  {
    private var callBack: BookOtherInfoDialog.CallBack? = null
    private val binding by viewBinding(DialogReadBookOtherInfoBinding::bind)
    val viewModel: BookInfoViewModel
        get() = getViewModel(BookInfoViewModel::class.java)
    val book = ReadBook.book

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
        return inflater.inflate(R.layout.dialog_read_book_other_info, container)
    }

    override fun onFragmentCreated(view: View, savedInstanceState: Bundle?) {
        initView()
        if(book!=null) showBook(book)
        initEvent()
    }

    private fun initView(){
        val bg = requireContext().readCfgBottomBg
        val textColor = requireContext().readCfgBottomText
        val secondtextColor = ColorUtils.withAlpha(textColor,0.5f)
        binding.apply {
            root.setBackgroundColor(bg)
            bookName.setTextColor(textColor)
            bookAuthor.setTextColor(secondtextColor)
            readTime.setTextColor(textColor)
            readAllTime.setTextColor(textColor)

            ivAuthorOther.setColorFilter(textColor)
            ivReadAloud.setColorFilter(textColor)
            ivReadPage.setColorFilter(textColor)
            ivSearch.setColorFilter(textColor)
            ivAccessUrl.setColorFilter(textColor)
            ivDownload.setColorFilter(textColor)
            ivGetProgress.setColorFilter(textColor)
            ivRefresh.setColorFilter(textColor)

            tvAuthorOther.setTextColor(secondtextColor)
            tvReadAloud.setTextColor(secondtextColor)
            tvReadPage.setTextColor(secondtextColor)
            tvSearch.setTextColor(secondtextColor)
            tvAccessUrl.setTextColor(secondtextColor)
            tvDownload.setTextColor(secondtextColor)
            tvGetProgress.setTextColor(secondtextColor)
            tvRefresh.setTextColor(secondtextColor)
        }
    }

    private fun initEvent() {
        binding.apply {
            llBookInfo.onClick {
                ReadBook.book?.let {
                    startActivity<BookInfoActivity>(Pair("name", it.name), Pair("author", it.author))
                }
            }
            llSearch.onClick {
                callBack?.openSearchActivity(null)
            }
            llReadPage.onClick {
                callBack?.autoPage()
                dismiss()
            }
            llReadAloud.onClick {
                callBack?.onClickReadAloud()
                dismiss()
            }
            llGetProgress.onClick {
                callBack?.synProgress()
            }
            llAuthorOther.onClick {
                startActivity<SearchActivity>(Pair("key", book?.author))
            }
            llAccessUrl.onClick {
                mqLog.d("url ${ReadBook.curTextChapter?.url}")
                val localBook = ReadBook?.book?.isLocalBook()?:true
                if(localBook||ReadBook.curTextChapter?.url==null){
                    requireContext().openUrl("https://www.baidu.com/s?wd=${ReadBook?.book?.name}")
                }else{
                    requireContext().openUrl(ReadBook.curTextChapter?.url.toString())
                }
            }
            llRefresh.onClick {
                callBack?.refreshBook()
            }
            llDownload.onClick {
                callBack?.downloadBook()
            }
        }
    }

    private fun showBook(book: Book) = with(binding) {
        showCover(book)
        bookName.text = book.name
        bookAuthor.text = book.getRealAuthor()
        readTime.text="当前已读  ${formatDuring( System.currentTimeMillis()-ReadBook.readStartTime)}"
        val readTime =  App.db.readRecordDao.getReadTime(book.name) ?: 0
        readAllTime.text="本书共读  ${formatDuring(readTime)}"
    }

    private fun showCover(book: Book) {
        binding.ivCover.load(book.getDisplayCover(), book.name, book.author)
        ImageLoader.load(requireContext(), book.getDisplayCover())
            .transition(DrawableTransitionOptions.withCrossFade(1500))
            .thumbnail(defaultCover())
            .apply(RequestOptions.bitmapTransform(BlurTransformation(requireContext(), 25)))
    }

    private fun defaultCover(): RequestBuilder<Drawable> {
        return ImageLoader.load(requireContext(), CoverImageView.defaultDrawable)
            .apply(RequestOptions.bitmapTransform(BlurTransformation(requireContext(), 25)))
    }

    private fun formatDuring(mss: Long): String {
        val days = mss / (1000 * 60 * 60 * 24)
        val hours = mss % (1000 * 60 * 60 * 24) / (1000 * 60 * 60)
        val minutes = mss % (1000 * 60 * 60) / (1000 * 60)
        val seconds = mss % (1000 * 60) / 1000
        val d = if (days > 0) "${days}天" else ""
        val h = if (hours > 0) "${hours}时" else ""
        val m = if (minutes > 0) "${minutes}分" else ""
        val s = if (mss < 60000 && seconds > 0) "${seconds}秒" else ""
        return "$d$h$m$s"
    }

    interface CallBack {
        fun openSearchActivity(searchWord: String?)
        fun autoPage()
        fun onClickReadAloud()
        fun synProgress()
        fun refreshBook()
        fun downloadBook()
    }
}
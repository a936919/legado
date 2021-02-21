package io.legado.app.ui.book.read.config

import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.fragment.app.viewModels
import io.legado.app.App
import io.legado.app.R
import io.legado.app.base.BaseDialogFragment
import io.legado.app.data.appDb
import io.legado.app.data.entities.Book
import io.legado.app.data.entities.TimeRecord
import io.legado.app.databinding.DialogReadBookOtherInfoBinding
import io.legado.app.lib.theme.readCfgBottomBg
import io.legado.app.lib.theme.readCfgBottomText
import io.legado.app.service.help.ReadBook
import io.legado.app.ui.book.info.BookInfoActivity
import io.legado.app.ui.book.info.BookInfoViewModel
import io.legado.app.ui.book.read.ReadBookActivity
import io.legado.app.ui.book.search.SearchActivity
import io.legado.app.utils.*
import io.legado.app.utils.viewbindingdelegate.viewBinding

class BookOtherInfoDialog : BaseDialogFragment()  {
    private var callBack: CallBack? = null
    private val binding by viewBinding(DialogReadBookOtherInfoBinding::bind)
    val viewModel: BookInfoViewModel
            by viewModels()
    val book = ReadBook.book

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
        val secondTextColor = ColorUtils.withAlpha(textColor,0.5f)
        val thirdTextColor = ColorUtils.withAlpha(textColor,0.7f)

        binding.apply {
            root.setBackgroundColor(bg)
            bookName.setTextColor(textColor)
            bookAuthor.setTextColor(secondTextColor)
            bookChapter.setTextColor(thirdTextColor)
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

            tvAuthorOther.setTextColor(secondTextColor)
            tvReadAloud.setTextColor(secondTextColor)
            tvReadPage.setTextColor(secondTextColor)
            tvSearch.setTextColor(secondTextColor)
            tvAccessUrl.setTextColor(secondTextColor)
            tvDownload.setTextColor(secondTextColor)
            tvGetProgress.setTextColor(secondTextColor)
            tvRefresh.setTextColor(secondTextColor)
        }
    }

    private fun initEvent() {
        binding.apply {
            llBookInfo.setOnClickListener {
                ReadBook.book?.let {
                    startActivity<BookInfoActivity>{
                        putExtra("name", it.name)
                        putExtra("author", it.author)
                    }
                }
            }
            llSearch.setOnClickListener {
                callBack?.openSearchActivity(null)
            }
            llReadPage.setOnClickListener {
                callBack?.autoPage()
                dismiss()
            }
            llReadAloud.setOnClickListener {
                callBack?.onClickReadAloud()
                dismiss()
            }
            llGetProgress.setOnClickListener {
                callBack?.synProgress()
            }
            llAuthorOther.setOnClickListener {
                startActivity<SearchActivity> { putExtra("key", book?.author)}
            }
            llAccessUrl.setOnClickListener {
                val localBook = ReadBook.book?.isLocalBook()?:true
                if(localBook||ReadBook.curTextChapter?.url==null){
                    requireContext().openUrl("https://www.baidu.com/s?wd=${ReadBook.book?.name}")
                }else{
                    requireContext().openUrl(ReadBook.curTextChapter?.url.toString())
                }
            }
            llRefresh.setOnClickListener {
                callBack?.refreshBook()
            }
            llDownload.setOnClickListener {
                callBack?.downloadBook()
            }
        }
    }

    private fun showBook(book: Book) = with(binding) {
        binding.ivCover.load(book.getDisplayCover(), book.name, book.author)
        bookName.text = book.name
        bookAuthor.text = book.getRealAuthor()
        var string = "[${book.durChapterIndex + 1}/${book.totalChapterNum}]  ${book.durChapterTitle}"
        bookChapter.text = string
        val nowReadTime = appDb.timeRecordDao.getReadTime(book.name,book.author, TimeRecord.getDate())?:0
        string = "今日阅读  ${TimeRecord.formatDuring(nowReadTime)}"
        readTime.text =  string
        val readTime =  appDb.timeRecordDao.getReadTime(book.name,book.author) ?: 0
        string ="本书已读  ${TimeRecord.formatDuring(readTime)}"
        readAllTime.text = string
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
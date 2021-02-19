package io.legado.app.ui.about

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import io.legado.app.R
import io.legado.app.base.BaseActivity
import io.legado.app.base.adapter.ItemViewHolder
import io.legado.app.base.adapter.RecyclerAdapter
import io.legado.app.constant.androidId
import io.legado.app.data.entities.Book
import io.legado.app.data.appDb
import io.legado.app.data.entities.ReadRecordShow
import io.legado.app.data.entities.TimeRecord
import io.legado.app.databinding.ActivityReadRecordBinding
import io.legado.app.databinding.DialogBookStatusBinding
import io.legado.app.databinding.ItemReadRecordBinding
import io.legado.app.lib.dialogs.alert
import io.legado.app.lib.theme.accentColor
import io.legado.app.ui.book.info.BookInfoActivity
import io.legado.app.utils.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class ReadRecordActivity : BaseActivity<ActivityReadRecordBinding>() {

    lateinit var adapter: RecordAdapter
    private var sortMode = 0
    private var status = -1


    override fun getViewBinding(): ActivityReadRecordBinding {
        return ActivityReadRecordBinding.inflate(layoutInflater)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        initView()
    }

    override fun onCompatCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.book_read_record, menu)
        return super.onCompatCreateOptionsMenu(menu)
    }

    override fun onResume() {
        super.onResume()
        initData()
    }

    override fun onCompatOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_read_all->{
                status = -1
                sortMode = 0
                initData()
            }
            R.id.menu_read_now->{
                status = 0
                sortMode = 0
                initData()
            }
            R.id.menu_read_finish->{
                status = 1
                sortMode = 0
                initData()
            }
            R.id.menu_read_plan->{
                status = 2
                sortMode = 0
                initData()
            }
            R.id.menu_book_mark->{
                status = 5
                sortMode = 0
                initData()
            }
            R.id.menu_sort_current -> {
                status = -1
                sortMode = 0
                initData()
            }
            R.id.menu_sort_time -> {
                status = -1
                sortMode = 1
                initData()
            }
        }
        return super.onCompatOptionsItemSelected(item)
    }

    private fun initView() = with(binding) {
        tvBookName.setText(R.string.all_read_time)
        adapter = RecordAdapter(this@ReadRecordActivity)
        recyclerView.adapter = adapter
/*
        readRecord.tvRemove.setOnClickListener {
            alert(R.string.delete, R.string.sure_del) {
                okButton {
                    appDb.readRecordDao.clear()
                    initData()
                }
                noButton()
            }.show()
        }
*/
    }

    private fun initData() {
        launch(IO) {
            val allTime = appDb.timeRecordDao.allTime
            val todayTime = appDb.timeRecordDao.getReadTime(TimeRecord.getDate())?:0
            withContext(Main) {
                binding.tvReadTime.text = TimeRecord.formatDuring(allTime)
                binding.tvReadTime2.text = TimeRecord.formatDuring(todayTime)
            }
            var readRecords = appDb.readRecordDao.allShow
            readRecords.forEach{
                it.readTime = appDb.timeRecordDao.getReadTime(it.bookName,it.author)?:0

            }

            if(status == 5) readRecords = readRecords.filter { appDb.bookmarkDao.haveBook(it.bookUrl)}
            else if(status>=0) readRecords = readRecords.filter { it.status==status }
            if(sortMode == 1) readRecords = readRecords.sortedBy { -it.readTime }
            withContext(Main) {
                adapter.setItems(readRecords)
            }
        }
    }

    private fun format(t: Long) :String{
        return StringUtils.dateConvert(t,"yyyy-MM-dd-HH-mm-ss")
    }


    private fun setBookStatus(readShow: ReadRecordShow){
        alert("阅读状态设置") {
            val alertBinding = DialogBookStatusBinding.inflate(layoutInflater)
            var change = false
            val readRecord = appDb.readRecordDao.getBook(androidId,readShow.bookName,readShow.author)
            var book: Book? = null
            if (readRecord != null) {
                alertBinding.rgLayout.checkByIndex(readRecord.status)
                book = appDb.bookDao.getBook(readRecord.bookUrl)
            }
            alertBinding.rgLayout.setOnCheckedChangeListener { _, _ ->
                change = true
            }
            customView { alertBinding.root }
            okButton {
                alertBinding.apply {
                    if (change) {
                        val status = rgLayout.getCheckedIndex()
                        readRecord?.status = status
                        if (book != null) {
                            book.status =status
                            appDb.bookDao.update(book)
                        }
                        if (readRecord != null) {
                            appDb.readRecordDao.update(readRecord)
                        }
                        initData()
                    }
                }
            }
            noButton()
        }.show()
    }

    inner class RecordAdapter(context: Context) :
        RecyclerAdapter<ReadRecordShow, ItemReadRecordBinding>(context) {

        override fun getViewBinding(parent: ViewGroup): ItemReadRecordBinding {
            return ItemReadRecordBinding.inflate(inflater, parent, false)
        }

        override fun convert(
            holder: ItemViewHolder,
            binding: ItemReadRecordBinding,
            item: ReadRecordShow,
            payloads: MutableList<Any>
        ) {
            binding.apply {
                tvBookName.text = item.bookName
                tvAuthor.text = item.author
                ivCover.load(item.coverUrl,item.bookName,item.author)
                val string = "已阅读  ${TimeRecord.formatDuring(item.readTime)} （${item.durChapterIndex+1}/${item.totalChapterNum}）"
                tvReadTime.text = string
                tvStatus.text = if(item.status == 1) "已读" else if(item.status == 2) "" else ""
                tvStatus.setTextColor(accentColor)
                tvChapter.text = item.durChapterTitle
            }
        }

        override fun registerListener(holder: ItemViewHolder, binding: ItemReadRecordBinding) {
            binding.apply {
                tvRemove.setOnClickListener {
                    getItem(holder.layoutPosition)?.let { item ->
                        sureDelAlert(item)
                    }
                }
                root.setOnClickListener {
                    getItem((holder.layoutPosition))?.let {
                        startActivity<BookInfoActivity> {
                            putExtra("name", it.bookName)
                            putExtra("author", it.author)
                        }
                    }
                }
                tvStatus.setOnClickListener {
                    getItem((holder.layoutPosition))?.let {
                        setBookStatus(it)
                    }
                }
            }
        }

        private fun sureDelAlert(item: ReadRecordShow) {
            alert(R.string.delete) {
                setMessage(getString(R.string.sure_del_any, item.bookName))
                okButton {
                    appDb.readRecordDao.deleteBook(item.bookName,item.author)
                    appDb.timeRecordDao.deleteBook(item.bookName,item.author)
                    initData()
                }
                noButton()
            }.show()
        }
    }

}
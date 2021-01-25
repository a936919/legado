package io.legado.app.ui.about

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import io.legado.app.App
import io.legado.app.R
import io.legado.app.base.BaseActivity
import io.legado.app.base.adapter.ItemViewHolder
import io.legado.app.base.adapter.RecyclerAdapter
import io.legado.app.data.entities.Book
import io.legado.app.data.entities.ReadRecordShow
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
import org.jetbrains.anko.sdk27.listeners.onClick
import org.jetbrains.anko.startActivity
import java.lang.Long.max
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
    }

    private fun initData() {
        launch(IO) {
            val allTime = App.db.timeRecordDao.allTime
            withContext(Main) {
                binding.tvReadTime.text = formatDuring(allTime)
            }

            var readRecords = App.db.readRecordDao.allShow
            readRecords.forEach{
                it.readTime = App.db.timeRecordDao.getReadTime(it.bookName,it.author)?:0
            }
/*
            val a = App.db.readRecordDao.all
            a.forEach{
                val b = TimeRecord()
                if(it.readTime>30*1000){
                    b.readTime = it.readTime
                    it.readTime = 0
                    b.androidId = it.androidId
                    b.bookName = it.bookName
                    b.author = it.author
                    b.date = TimeRecord.getDayTime()-(24*60*60*1000)
                    mqLog.d("${b.bookName} ${b.readTime/1000/60} ${StringUtils.dateConvert(b.date,"yyyy-MM-dd-HH-mm-ss")}")
                    App.db.readRecordDao.update(it)
                    App.db.timeRecordDao.insert(b)
                }
            }
*/
            /*
            val c =  App.db.timeRecordDao.all
            c.forEach {
                mqLog.d("${it.bookName} ${it.author} ${it.androidId} ${StringUtils.dateConvert(it.date,"yyyy-MM-dd-HH-mm-ss")} ${it.readTime/1000/60}")

            }*/


            if(status == 5) readRecords = readRecords.filter { App.db.bookmarkDao.haveBook(it.bookUrl)}
            else if(status>=0) readRecords = readRecords.filter { it.status==status }
            if(sortMode == 1) readRecords = readRecords.sortedBy { -it.readTime }
            withContext(Main) {
                adapter.setItems(readRecords)
            }
        }
    }



    private fun setBookStatus(readShow: ReadRecordShow){
        alert("阅读状态设置") {
            val alertBinding = DialogBookStatusBinding.inflate(layoutInflater)
            var change = false
            val readRecord = App.db.readRecordDao.getBook(App.androidId,readShow.bookName,readShow.author)
            var book: Book? = null
            if (readRecord != null) {
                alertBinding.rgLayout.checkByIndex(readRecord.status)
                book = App.db.bookDao.getBook(readRecord.bookUrl)
            }
            alertBinding.rgLayout.setOnCheckedChangeListener { _, _ ->
                change = true
            }
            customView = alertBinding.root
            okButton {
                alertBinding.apply {
                    if (change) {
                        val status = rgLayout.getCheckedIndex()
                        readRecord?.status = status
                        if (book != null) {
                            book.status =status
                            App.db.bookDao.update(book)
                        }
                        if (readRecord != null) {
                            App.db.readRecordDao.update(readRecord)
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
                tvReadTime.text ="已阅读  ${formatDuring(item.readTime)} （${item.durChapterIndex+1}/${item.totalChapterNum}）"
                tvStatus.text = if(item.status == 1) "已读" else if(item.status == 2) "" else ""
                tvStatus.setTextColor(accentColor)
                tvChapter.text = item.durChapterTitle
            }
        }

        override fun registerListener(holder: ItemViewHolder, binding: ItemReadRecordBinding) {
            binding.apply {
                ivRemove.onClick {
                    getItem(holder.layoutPosition)?.let { item ->
                        sureDelAlert(item)
                    }
                }
                root.onClick {
                    getItem((holder.layoutPosition))?.let {
                        startActivity<BookInfoActivity>(
                            Pair("name", it.bookName),
                            Pair("author", it.author)
                        )
                    }
                }
                tvStatus.onClick {
                    getItem((holder.layoutPosition))?.let {
                        setBookStatus(it)
                    }
                }
            }
        }

        private fun sureDelAlert(item: ReadRecordShow) {
            alert(R.string.delete) {
                message = getString(R.string.sure_del_any, item.bookName)
                okButton {
                    App.db.readRecordDao.deleteByName(item.bookName,item.author)
                    initData()
                }
                noButton()
            }.show()
        }

    }

    companion object{
        fun formatDuring(mss: Long): String {
            val hours = mss / (1000 * 60 * 60)
            val minutes = mss % (1000 * 60 * 60) / (1000 * 60)
            val seconds = mss % (1000 * 60) / 1000
            val h = if (hours > 0) "${hours}小时" else ""
            val m = if (minutes > 0) "${minutes}分钟" else ""
            val s = if (mss < 1000 * 60) "${max(seconds,1)}秒" else ""
            return "$h$m$s"
        }
    }
}
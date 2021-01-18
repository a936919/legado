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
import io.legado.app.data.entities.ReadRecordShow
import io.legado.app.databinding.ActivityReadRecordBinding
import io.legado.app.databinding.ItemReadRecordBinding
import io.legado.app.lib.dialogs.alert
import io.legado.app.utils.cnCompare
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.sdk27.listeners.onClick
import java.lang.Long.max
import java.util.*

class ReadRecordActivity : BaseActivity<ActivityReadRecordBinding>() {

    lateinit var adapter: RecordAdapter
    private var sortMode = 0
    private var shortTimeFilter = false
    private var shortTimeItem:MenuItem? = null

    override fun getViewBinding(): ActivityReadRecordBinding {
        return ActivityReadRecordBinding.inflate(layoutInflater)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        initView()
        initData()
    }

    override fun onCompatCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.book_read_record, menu)
        shortTimeItem = menu.findItem(R.id.filter_short_time)
        shortTimeItem?.isChecked = shortTimeFilter
        return super.onCompatCreateOptionsMenu(menu)
    }

    override fun onCompatOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_sort_current -> {
                shortTimeItem?.isVisible = false
                sortMode = 0
                initData()
            }
            R.id.menu_sort_time -> {
                shortTimeItem?.isVisible = false
                sortMode = 1
                initData()
            }
            R.id.menu_sort_name -> {
                shortTimeItem?.isVisible = true
                sortMode = 2
                initData()
            }
            R.id.filter_short_time ->{
                shortTimeItem?.isVisible = true
                item.isChecked = !item.isChecked
                shortTimeFilter = item.isChecked
                initData()
            }
        }
        return super.onCompatOptionsItemSelected(item)
    }

    private fun initView() = with(binding) {
        readRecord.tvBookName.setText(R.string.all_read_time)
        adapter = RecordAdapter(this@ReadRecordActivity)
        recyclerView.adapter = adapter
        readRecord.ivRemove.onClick {
            alert(R.string.delete, R.string.sure_del) {
                okButton {
                    App.db.readRecordDao.clear()
                    initData()
                }
                noButton()
            }.show()
        }
    }

    private fun initData() {
        launch(IO) {
            val allTime = App.db.readRecordDao.allTime
            withContext(Main) {
                binding.readRecord.tvReadTime.text = formatDuring(allTime)
            }

            var readRecords = App.db.readRecordDao.allShow
            var filterTime = 0
            if(shortTimeFilter) filterTime = 5*60*1000

            readRecords = when (sortMode) {
                1 -> readRecords.sortedBy { -it.readTime }
                2 ->  readRecords.filter { it.readTime >= (filterTime)}
                    .sortedWith { o1, o2 ->
                        o1.bookName.cnCompare(o2.bookName)
                    }
                else ->  readRecords.sortedBy {-it.durChapterTime}
            }
            withContext(Main) {
                adapter.setItems(readRecords)
            }
        }
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
                tvReadTime.text = formatDuring(item.readTime)//StringUtils.dateConvert(item.durChapterTime,"yyyy-MM-dd-HH-mm-ss")
            }
        }

        override fun registerListener(holder: ItemViewHolder, binding: ItemReadRecordBinding) {
            binding.apply {
                ivRemove.onClick {
                    getItem(holder.layoutPosition)?.let { item ->
                        sureDelAlert(item)
                    }
                }
            }
        }

        private fun sureDelAlert(item: ReadRecordShow) {
            alert(R.string.delete) {
                message = getString(R.string.sure_del_any, item.bookName)
                okButton {
                    App.db.readRecordDao.deleteByName(item.bookName)
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
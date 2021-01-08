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
import io.legado.app.utils.StringUtils
import io.legado.app.utils.cnCompare
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.sdk27.listeners.onClick
import java.util.*

class ReadRecordActivity : BaseActivity<ActivityReadRecordBinding>() {

    lateinit var adapter: RecordAdapter
    private var sortMode = 0
    private var shortTimeFilter = true

    override fun getViewBinding(): ActivityReadRecordBinding {
        return ActivityReadRecordBinding.inflate(layoutInflater)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        initView()
        initData()
    }

    override fun onCompatCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.book_read_record, menu)
        menu.findItem(R.id.filter_short_time)?.isChecked = shortTimeFilter
        return super.onCompatCreateOptionsMenu(menu)
    }

    override fun onCompatOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_sort_name -> {
                sortMode = 0
                initData()
            }
            R.id.menu_sort_time -> {
                sortMode = 1
                initData()
            }
            R.id.filter_short_time ->{
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
            if(shortTimeFilter) filterTime = 5 *60*1000

            readRecords = when (sortMode) {
                1 -> {readRecords.filter { it.readTime >= (filterTime)}
                        .sortedBy { -it.readTime }}
                2 -> {
                    readRecords.filter { it.readTime >= (filterTime)}
                    .sortedWith { o1, o2 ->
                        o1.bookName.cnCompare(o2.bookName)
                    }
                }else -> {
                    readRecords.filter { it.durChapterTime!=0L }
                        .sortedBy {it.durChapterTime}
                }
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
                tvReadTime.text = StringUtils.dateConvert(item.durChapterTime,"yyyy-MM-dd-HH-mm-ss")//item.durChapterTime.//formatDuring(item.readTime)
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

    fun formatDuring(mss: Long): String {
        val days = mss / (1000 * 60 * 60 * 24)
        val hours = mss % (1000 * 60 * 60 * 24) / (1000 * 60 * 60)
        val minutes = mss % (1000 * 60 * 60) / (1000 * 60)
        val seconds = mss % (1000 * 60) / 1000
        val d = if (days > 0) "${days}天" else ""
        val h = if (hours > 0) "${hours}小时" else ""
        val m = if (minutes > 0) "${minutes}分钟" else ""
        val s = if (seconds > 0) "${seconds}秒" else ""
        return "$d$h$m$s"
    }

}
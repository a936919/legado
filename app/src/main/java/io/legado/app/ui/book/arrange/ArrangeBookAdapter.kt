package io.legado.app.ui.book.arrange

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.RecyclerView
import io.legado.app.base.adapter.ItemViewHolder
import io.legado.app.base.adapter.RecyclerAdapter
import io.legado.app.data.entities.Book
import io.legado.app.data.entities.BookGroup
import io.legado.app.databinding.ItemArrangeBookBinding
import io.legado.app.lib.theme.accentColor
import io.legado.app.lib.theme.backgroundColor
import io.legado.app.ui.book.info.BookInfoActivity
import io.legado.app.ui.widget.recycler.DragSelectTouchHelper
import io.legado.app.ui.widget.recycler.ItemTouchCallback
import io.legado.app.utils.startActivity
import java.util.*

class ArrangeBookAdapter(context: Context, val callBack: CallBack) :
    RecyclerAdapter<Book, ItemArrangeBookBinding>(context),

    ItemTouchCallback.Callback {
    val groupRequestCode = 12
    private val selectedBooks: HashSet<Book> = hashSetOf()
    var actionItem: Book? = null

    override fun getViewBinding(parent: ViewGroup): ItemArrangeBookBinding {
        return ItemArrangeBookBinding.inflate(inflater, parent, false)
    }

    override fun onCurrentListChanged() {
        callBack.upSelectCount()
    }

    override fun gotoPositionAndSelect(position: Int) {
        if(position>=0){
            getItem(position)?.let{
                selectedBooks.add(it)
            }
            callBack.gotoPosition(Integer.max(position - 2, 0))
        }
    }

    override fun convert(
        holder: ItemViewHolder,
        binding: ItemArrangeBookBinding,
        item: Book,
        payloads: MutableList<Any>
    ) {
        binding.apply {
            root.setBackgroundColor(context.backgroundColor)
            tvName.text = item.name
            tvAuthor.text = item.author
            tvAuthor.visibility = if (item.author.isEmpty()) View.GONE else View.VISIBLE
            tvGroupS.text = if(item.status == 0) "" else if(item.status == 1) "已读" else ""
            tvGroupS.setTextColor(context.accentColor)
            checkbox.isChecked = selectedBooks.contains(item)
            ivCover.load(item.getDisplayCover(),item.name,item.author)
        }
    }

    override fun registerListener(holder: ItemViewHolder, binding: ItemArrangeBookBinding) {
        binding.apply {
            checkbox.setOnCheckedChangeListener { buttonView, isChecked ->
                if (buttonView.isPressed) {
                    getItem(holder.layoutPosition)?.let {
                        if (buttonView.isPressed) {
                            if (isChecked) {
                                selectedBooks.add(it)
                            } else {
                                selectedBooks.remove(it)
                            }
                            callBack.upSelectCount()
                        }
                    }
                }
            }
            root.setOnClickListener {
                getItem(holder.layoutPosition)?.let {
                    checkbox.isChecked = !checkbox.isChecked
                    if (checkbox.isChecked) {
                        selectedBooks.add(it)
                    } else {
                        selectedBooks.remove(it)
                    }
                    callBack.upSelectCount()
                }
            }
            tvDelete.setOnClickListener {
                getItem(holder.layoutPosition)?.let {
                    callBack.deleteBook(it)
                }
            }
            tvGroup.setOnClickListener {
                getItem(holder.layoutPosition)?.let {
                    actionItem = it
                    callBack.selectGroup(groupRequestCode, it.group)
                }
            }
            tvStatus.setOnClickListener {
                getItem(holder.layoutPosition)?.let {
                    val books = arrayListOf(it)
                    callBack.setBookStatus(*books.toTypedArray())
                }
            }
            tvInfo.setOnClickListener {
                getItem(holder.layoutPosition)?.let {
                    context.startActivity<BookInfoActivity>{
                        putExtra("name", it.name)
                        putExtra("author", it.author)
                    }
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun selectAll(selectAll: Boolean) {
        if (selectAll) {
            getItems().forEach {
                selectedBooks.add(it)
            }
        } else {
            selectedBooks.clear()
        }
        notifyDataSetChanged()
        callBack.upSelectCount()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun revertSelection() {
        getItems().forEach {
            if (selectedBooks.contains(it)) {
                selectedBooks.remove(it)
            } else {
                selectedBooks.add(it)
            }
        }
        notifyDataSetChanged()
        callBack.upSelectCount()
    }

    fun selectedBooks(): Array<Book> {
        val books = arrayListOf<Book>()
        selectedBooks.forEach {
            if (getItems().contains(it)) {
                books.add(it)
            }
        }
        return books.toTypedArray()
    }



    private fun getGroupList(groupId: Long): List<String> {
        val groupNames = arrayListOf<String>()
        callBack.groupList.forEach {
            if (it.groupId > 0 && it.groupId and groupId > 0) {
                groupNames.add(it.groupName)
            }
        }
        return groupNames
    }

    private fun getGroupName(groupId: Long): String {
        val groupNames = getGroupList(groupId)
        if (groupNames.isEmpty()) {
            return ""
        }
        return groupNames.joinToString(",")
    }

    private var isMoved = false

    override fun swap(srcPosition: Int, targetPosition: Int): Boolean {
        val srcItem = getItem(srcPosition)
        val targetItem = getItem(targetPosition)
        if (srcItem != null && targetItem != null) {
            if (srcItem.order == targetItem.order) {
                for ((index, item) in getItems().withIndex()) {
                    item.order = index + 1
                }
            } else {
                val pos = srcItem.order
                srcItem.order = targetItem.order
                targetItem.order = pos
            }
        }
        swapItem(srcPosition, targetPosition)
        isMoved = true
        return true
    }

    override fun onClearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        if (isMoved) {
            callBack.updateBook(*getItems().toTypedArray())
        }
        isMoved = false
    }

    val dragSelectCallback: DragSelectTouchHelper.Callback =
        object : DragSelectTouchHelper.AdvanceCallback<Book>(Mode.ToggleAndReverse) {
            override fun currentSelectedId(): MutableSet<Book> {
                return selectedBooks
            }

            override fun getItemId(position: Int): Book {
                return getItem(position)!!
            }

            override fun updateSelectState(position: Int, isSelected: Boolean): Boolean {
                getItem(position)?.let {
                    if (isSelected) {
                        selectedBooks.add(it)
                    } else {
                        selectedBooks.remove(it)
                    }
                    notifyItemChanged(position, bundleOf(Pair("selected", null)))
                    callBack.upSelectCount()
                    return true
                }
                return false
            }
        }

    interface CallBack {
        val groupList: List<BookGroup>
        fun upSelectCount()
        fun updateBook(vararg book: Book)
        fun deleteBook(book: Book)
        fun selectGroup(requestCode: Int, groupId: Long)
        fun gotoPosition(position: Int)
        fun setBookStatus(vararg book: Book)
    }
}
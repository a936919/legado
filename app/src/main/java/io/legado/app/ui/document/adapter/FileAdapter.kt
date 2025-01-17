package io.legado.app.ui.document.adapter


import android.content.Context
import android.view.ViewGroup
import io.legado.app.App
import io.legado.app.base.adapter.ItemViewHolder
import io.legado.app.base.adapter.RecyclerAdapter
import io.legado.app.data.appDb
import io.legado.app.databinding.ItemFileFilepickerBinding
import io.legado.app.help.AppConfig
import io.legado.app.lib.theme.getPrimaryDisabledTextColor
import io.legado.app.lib.theme.getPrimaryTextColor
import io.legado.app.ui.document.entity.FileItem
import io.legado.app.ui.document.utils.FilePickerIcon
import io.legado.app.utils.ConvertUtils
import io.legado.app.utils.FileUtils
import io.legado.app.utils.cnCompare
import java.io.File
import java.util.*


class FileAdapter(context: Context, val callBack: CallBack) :
    RecyclerAdapter<FileItem, ItemFileFilepickerBinding>(context) {
    private var rootPath: String? = null
    var currentPath: String? = null
        private set
    private val homeIcon = ConvertUtils.toDrawable(FilePickerIcon.getHome())
    private val upIcon = ConvertUtils.toDrawable(FilePickerIcon.getUpDir())
    private val folderIcon = ConvertUtils.toDrawable(FilePickerIcon.getFolder())
    private val fileIcon = ConvertUtils.toDrawable(FilePickerIcon.getFile())
    private val primaryTextColor = context.getPrimaryTextColor(!AppConfig.isNightTheme)
    private val disabledTextColor = context.getPrimaryDisabledTextColor(!AppConfig.isNightTheme)
    private val topIcon = ConvertUtils.toDrawable(FilePickerIcon.getTop())

    fun loadData(path: String?) {
        if (path == null) {
            return
        }
        val data = ArrayList<FileItem>()
        if (rootPath == null) {
            rootPath = path
        }
        currentPath = path
        if (callBack.isShowHomeDir) {
            //添加“返回主目录”
            val fileRoot = FileItem()
            fileRoot.isDirectory = true
            fileRoot.icon = homeIcon
            fileRoot.name = DIR_ROOT
            fileRoot.size = 0
            fileRoot.path = rootPath ?: path
            data.add(fileRoot)
        }
        if (callBack.isShowUpDir && path != PathAdapter.sdCardDirectory) {
            //添加“返回上一级目录”
            val fileParent = FileItem()
            fileParent.isDirectory = true
            fileParent.icon = upIcon
            fileParent.name = DIR_PARENT
            fileParent.size = 0
            fileParent.path = File(path).parent ?: ""
            data.add(fileParent)
        }
        currentPath?.let { currentPath ->
            val files: Array<File>? = FileUtils.listDirsAndFiles(currentPath)
            if (files != null) {
                for (file in files) {
                    if (!callBack.isShowHideDir && file.name.startsWith(".")) {
                        continue
                    }
                    val fileItem = FileItem()
                    val isDirectory = file.isDirectory
                    fileItem.isDirectory = isDirectory
                    if (isDirectory) {
                        fileItem.icon = folderIcon
                        fileItem.size = 0
                    } else {
                        fileItem.icon = fileIcon
                        fileItem.size = file.length()
                    }
                    fileItem.name = file.name
                    fileItem.path = file.absolutePath
                    fileItem.top = appDb.topPathDao.isTopPath(fileItem.path)
                    if( fileItem.top) fileItem.icon = topIcon
                    data.add(fileItem)
                }
            }
            data.sortWith{o1,o2->
                var sort = -o1.top.compareTo(o2.top)
                if (sort == 0) {
                    sort = -o1.isDirectory.compareTo(o2.isDirectory)
                    if(sort == 0) sort = o2.name?.let { o1.name?.cnCompare(it) } ?:0
                }
                sort
            }
            setItems(data)
        }
    }

    override fun getViewBinding(parent: ViewGroup): ItemFileFilepickerBinding {
        return ItemFileFilepickerBinding.inflate(inflater, parent, false)
    }

    override fun convert(
        holder: ItemViewHolder,
        binding: ItemFileFilepickerBinding,
        item: FileItem,
        payloads: MutableList<Any>
    ) {
        binding.apply {
            imageView.setImageDrawable(item.icon)
            textView.text = item.name
            if (item.isDirectory) {
                textView.setTextColor(primaryTextColor)
            } else {
                if (callBack.isSelectDir) {
                    textView.setTextColor(disabledTextColor)
                } else {
                    callBack.allowExtensions?.let {
                        if (it.isEmpty() || it.contains(FileUtils.getExtension(item.path))) {
                            textView.setTextColor(primaryTextColor)
                        } else {
                            textView.setTextColor(disabledTextColor)
                        }
                    } ?: textView.setTextColor(primaryTextColor)
                }
            }
        }
    }

    override fun registerListener(holder: ItemViewHolder, binding: ItemFileFilepickerBinding) {
        holder.itemView.setOnClickListener {
            callBack.onFileClick(holder.layoutPosition)
        }
        holder.itemView.setOnLongClickListener{
            callBack.processTopPath(holder.layoutPosition)
            true
        }
    }

    interface CallBack {
        fun onFileClick(position: Int)
        fun processTopPath(position:Int)
        //允许的扩展名
        var allowExtensions: Array<String>?

        /**
         * 是否选取目录
         */
        val isSelectDir: Boolean

        /**
         * 是否显示返回主目录
         */
        var isShowHomeDir: Boolean

        /**
         * 是否显示返回上一级
         */
        var isShowUpDir: Boolean

        /**
         * 是否显示隐藏的目录（以“.”开头）
         */
        var isShowHideDir: Boolean
    }

    companion object {
        const val DIR_ROOT = "."
        const val DIR_PARENT = ".."
    }

}


package io.legado.app.ui.book.changesource

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.legado.app.App
import io.legado.app.R
import io.legado.app.base.BaseDialogFragment
import io.legado.app.constant.AppPattern
import io.legado.app.constant.PreferKey
import io.legado.app.data.entities.Book
import io.legado.app.data.entities.SearchBook
import io.legado.app.databinding.DialogChangeSourceBinding
import io.legado.app.help.AppConfig
import io.legado.app.lib.theme.backgroundColor
import io.legado.app.lib.theme.getPrimaryTextColor
import io.legado.app.lib.theme.primaryColor
import io.legado.app.ui.book.source.manage.BookSourceActivity
import io.legado.app.ui.widget.recycler.VerticalDivider
import io.legado.app.utils.*
import io.legado.app.utils.viewbindingdelegate.viewBinding
import org.jetbrains.anko.textColor
import java.text.Collator


class ChangeSourceDialog : BaseDialogFragment(),
    Toolbar.OnMenuItemClickListener,
    ChangeSourceAdapter.CallBack {
    var groups = linkedSetOf<String>()
    companion object {
        const val tag = "changeSourceDialog"

        fun show(manager: FragmentManager, name: String, author: String) {
            val fragment = (manager.findFragmentByTag(tag) as? ChangeSourceDialog)
                ?: ChangeSourceDialog().apply {
                    val bundle = Bundle()
                    bundle.putString("name", name)
                    bundle.putString("author", author)
                    arguments = bundle
                }
            fragment.show(manager, tag)
        }
    }

    private val binding by viewBinding(DialogChangeSourceBinding::bind)
    private var callBack: CallBack? = null
    private lateinit var viewModel: ChangeSourceViewModel
    lateinit var adapter: ChangeSourceAdapter

    override fun onStart() {
        super.onStart()
        val dm = requireActivity().getSize()
        dialog?.window?.setLayout((dm.widthPixels * 0.9).toInt(), (dm.heightPixels * 0.9).toInt())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        callBack = activity as? CallBack
        viewModel = getViewModel(ChangeSourceViewModel::class.java)
        return inflater.inflate(R.layout.dialog_change_source, container)
    }

    override fun onFragmentCreated(view: View, savedInstanceState: Bundle?) {
        binding.toolBar.setBackgroundColor(primaryColor)
        viewModel.initData(arguments)
        showTitle()
        initMenu()
        initRecyclerView()
        initSearchView()
        initLiveData()
        viewModel.loadDbSearchBook()
    }
//todo
    private fun showTitle() {
        binding.toolBar.title = viewModel.name
        binding.toolBar.subtitle = getString(R.string.author_show, viewModel.author)
    }

    private fun initMenu() {
        binding.toolBar.inflateMenu(R.menu.change_source)
        binding.toolBar.menu.applyTint(requireContext())
        binding.toolBar.setOnMenuItemClickListener(this)
        binding.toolBar.menu.findItem(R.id.menu_load_info)?.isChecked =
            AppConfig.changeSourceLoadInfo
        binding.toolBar.menu.findItem(R.id.menu_load_toc)?.isChecked = AppConfig.changeSourceLoadToc
        val name = callBack?.oldBook?.originName
        if(name != null) {
            val Group = App.db.bookSourceDao.getByName(name)[0].bookSourceGroup
            when(Group){
                ""->binding.sourceName.text = "书源:${name}    |    分组:空"
                else->binding.sourceName.text = "书源:${name}    |    分组:${Group}"
            }
        }
        else{
            binding.sourceName.text = "内容为空"
        }
        val isLight = ColorUtils.isColorLight(requireContext().backgroundColor)
        binding.sourceName.textColor =  requireContext().getPrimaryTextColor(isLight)
    }


    private fun initRecyclerView() {
        adapter = ChangeSourceAdapter(requireContext(), this)
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.addItemDecoration(VerticalDivider(requireContext()))
        binding.recyclerView.adapter = adapter
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                if (positionStart == 0) {
                    binding.recyclerView.scrollToPosition(0)
                }
            }

            override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
                if (toPosition == 0) {
                    binding.recyclerView.scrollToPosition(0)
                }
            }
        })
    }

    private fun initSearchView() {
        val searchView = binding.toolBar.menu.findItem(R.id.menu_screen).actionView as SearchView
        searchView.setOnCloseListener {
            showTitle()
            false
        }
        searchView.setOnSearchClickListener {
            binding.toolBar.title = ""
            binding.toolBar.subtitle = ""
        }
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.screen(newText)
                return false
            }

        })
    }

    private fun initLiveData() {
        viewModel.searchStateData.observe(viewLifecycleOwner, {
            binding.refreshProgressBar.isAutoLoading = it
            if (it) {
                stopMenuItem?.setIcon(R.drawable.ic_stop_black_24dp)
            } else {
                stopMenuItem?.setIcon(R.drawable.ic_refresh_black_24dp)
            }
            binding.toolBar.menu.applyTint(requireContext())
        })
        viewModel.searchBooksLiveData.observe(viewLifecycleOwner, {
            val diffResult = DiffUtil.calculateDiff(DiffCallBack(adapter.getItems(), it))
            val searchGroup = App.INSTANCE.getPrefString("searchGroup") ?: ""
            var items = it
            var newitems = it
            if(searchGroup != "") newitems = items.filter { (App.db.bookSourceDao.getByName(it.originName))[0].bookSourceGroup == searchGroup}
            adapter.setItems(newitems)
            diffResult.dispatchUpdatesTo(adapter)
        })

        App.db.bookSourceDao.liveGroupEnabled().observe(this, {
            groups.clear()
            it.map { group ->
                groups.addAll(group.splitNotBlank(AppPattern.splitGroupRegex))
            }
            upGroupMenu()
        })
    }

    private val stopMenuItem: MenuItem?
        get() = binding.toolBar.menu.findItem(R.id.menu_stop)

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_load_toc -> {
                putPrefBoolean(PreferKey.changeSourceLoadToc, !item.isChecked)
                item.isChecked = !item.isChecked
                initLiveData()
            }
            R.id.menu_load_info -> {
                putPrefBoolean(PreferKey.changeSourceLoadInfo, !item.isChecked)
                item.isChecked = !item.isChecked
                initLiveData()
            }
            R.id.book_source_manage->{
                startActivity<BookSourceActivity>()
                //todo startActivity<SearchActivity>(Pair("key", viewModel.name))
            }
            R.id.menu_stop -> viewModel.stopSearch()
            else -> if (item?.groupId == R.id.source_group) {
                item?.isChecked = true
                if (item?.title.toString() == getString(R.string.all_source)) {
                    putPrefString("searchGroup", "")
                } else {
                    putPrefString("searchGroup", item?.title.toString())
                }
                initLiveData()
            }
        }
        return false
    }

    override fun changeTo(searchBook: SearchBook) {
        val book = searchBook.toBook()
        book.upInfoFromOld(callBack?.oldBook)
        callBack?.changeTo(book)
        dismiss()
    }

    override val bookUrl: String?
        get() = callBack?.oldBook?.bookUrl

    override fun disableSource(searchBook: SearchBook) {
        viewModel.disableSource(searchBook)
    }

    /**
     * 更新分组菜单
     */
    private fun upGroupMenu() {

        var menu: Menu = binding.toolBar.menu
        val selectedGroup = getPrefString("searchGroup") ?: ""
        menu.removeGroup(R.id.source_group)
        var item = menu.add(R.id.source_group, Menu.NONE, Menu.NONE, R.string.all_source)
        if (selectedGroup == "") {
            item?.isChecked = true
        }
        groups.sortedWith(Collator.getInstance(java.util.Locale.CHINESE))
            .map {
                item = menu.add(R.id.source_group, Menu.NONE, Menu.NONE, it)
                if (it == selectedGroup) {
                    item.isChecked = true
                }
            }
        menu.setGroupCheckable(R.id.source_group, true, true)
    }

    interface CallBack {
        val oldBook: Book?
        fun changeTo(book: Book)
    }

}
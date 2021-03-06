package com.slambang.shakeyshoes.view.rcb

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.slambang.shakeyshoes.R
import com.slambang.shakeyshoes.view.base.BaseViewFragment
import com.slambang.shakeyshoes.view.setAppCompatToolbar
import javax.inject.Inject

class RcbViewFragment : BaseViewFragment<RcbViewModelImpl>() {

    override val layoutResId = R.layout.fragment_rcb

    @Inject
    lateinit var recyclerAdapter: BufferItemRecyclerAdapter

    private val viewModel by lazy { of<RcbViewModelImpl>() }

    private lateinit var toolbar: Toolbar
    private lateinit var addRcbButton: View
    private lateinit var deleteAllBuffersMenuItem: MenuItem

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        observeViewModel()
        viewModel.onResumeView()
    }

    private fun observeViewModel() {
        observe(viewModel.bluetoothStatusEvent) {
            toolbar.subtitle = it
        }

        observe(viewModel.removeAllItemsEvent) {
            recyclerAdapter.clearItems()
        }

        observe(viewModel.removeAllMenuOptionEnabledEvent) {
            deleteAllBuffersMenuItem.isEnabled = it
        }

        observe(viewModel.newItemEvent) {
            emitModel(it)
        }

        observe(viewModel.removeItemEvent) {
            recyclerAdapter.removeItem(it)
        }

        observe(viewModel.showDeviceListEvent) {
            displayDeviceList(it)
        }

        observe(viewModel.errorEvent) {
            showSnackBar(it)
        }

        observe(viewModel.confirmDialogEvent) {
            showConfirmDialog(it)
        }
    }

    override fun initView(root: View) {
        initToolbar(root)
        initRecycler(root)
        initAddRcbButton(root)
    }

    private fun initToolbar(root: View) {
        setAppCompatToolbar(R.id.toolbar)
        toolbar = root.findViewById(R.id.toolbar)
    }

    private fun initRecycler(root: View) {
        root.findViewById<RecyclerView>(R.id.rcb_list).apply {

            val linearLayoutManager = LinearLayoutManager(context)

            adapter = recyclerAdapter
            layoutManager = linearLayoutManager
            itemAnimator = DefaultItemAnimator()

            addItemDecoration(
                DividerItemDecoration(context, linearLayoutManager.orientation)
            )
        }
    }

    private fun initAddRcbButton(root: View) {
        addRcbButton = root.findViewById(R.id.add_rcb_button)
        setAddRcbClickListener()
    }

    private fun displayDeviceList(devices: List<Pair<Int, String>>) {

        val deviceNames = devices.map { it.second }.toTypedArray()

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.select_device_dialog_title)
            .setItems(deviceNames) { _, which ->
                viewModel.onDeviceSelected(devices[which].first)
            }
            .setOnDismissListener { setAddRcbClickListener() }
            .create()
            .also { it.show() }
    }

    private fun emitModel(model: Pair<RcbItemModel, Int>) =
        recyclerAdapter.updateItem(model.first, model.second)

    private fun setAddRcbClickListener() =
        addRcbButton.setOnClickListener {
            it.setOnClickListener(null) // Prevent double-click
            viewModel.onAddRcbClicked()
        }

    private fun showConfirmDialog(dialogModel: DialogModel) =
        AlertDialog.Builder(requireContext())
            .setTitle(dialogModel.titleResId)
            .setMessage(dialogModel.messageResId)
            .setPositiveButton(R.string.delete) { _, _ -> dialogModel.onConfirmedListener() }
            .setNegativeButton(android.R.string.cancel, null)
            .show()

    private fun showSnackBar(message: String) =
        Snackbar.make(requireView(), message, Snackbar.LENGTH_INDEFINITE).show()

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_rcb_menu, menu)
        deleteAllBuffersMenuItem = menu.findItem(R.id.fragment_rcb_menu_delete_all)
    }

    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {
            R.id.fragment_rcb_menu_project_repo -> {
                viewModel.onVisitRepoClicked()
                true
            }
            R.id.fragment_rcb_menu_delete_all -> {
                viewModel.onDeleteAllClicked()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
}

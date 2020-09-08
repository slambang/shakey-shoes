package com.slambang.shakeyshoes.view.rcb

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.slambang.shakeyshoes.R
import com.slambang.shakeyshoes.view.base.BaseViewFragment
import com.slambang.shakeyshoes.view.rcb.rcb_item_view.BufferItemViewListener
import com.slambang.shakeyshoes.view.setAppCompatToolbar
import kotlinx.android.synthetic.main.fragment_rcb.*


class RcbViewFragment : BaseViewFragment<RcbViewModelImpl>(), BufferItemViewListener {

    override val layoutResId = R.layout.fragment_rcb

    private val viewModel by lazy { get<RcbViewModelImpl>() }

    private lateinit var addRcbButton: View
    private lateinit var deleteAllBuffersMenuItem: MenuItem

    private val recyclerAdapter = BufferItemRecyclerAdapter(this)

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
        viewModel.onResume()
    }

    private fun observeViewModel() {
        observe(viewModel.bluetoothStatusLiveData) {
            toolbar.subtitle = it
        }

        observe(viewModel.removeAllBuffersLiveData) {
            recyclerAdapter.clearItems()
        }

        observe(viewModel.removeAllMenuOptionEnabledLiveData) {
            deleteAllBuffersMenuItem.isEnabled = it
        }

        observe(viewModel.itemModelsLiveData) {
            updateModels(it)
        }

        observe(viewModel.itemDeletedLiveData) {
            recyclerAdapter.removeItem(it)
        }

        observe(viewModel.showDeviceListLiveData) {
            displayDeviceList(it)
        }

        observe(viewModel.bufferItemPageLiveData) {
            recyclerAdapter.setPage(it.first, it.second)
        }

        observe(viewModel.errorLiveData) {
            showSnackBar(it)
        }
    }

    override fun initView(root: View) {
        setAppCompatToolbar(R.id.toolbar)
        initRecycler(root)
        initAddRcbButton(root)
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
            .setTitle(R.string.select_device_title)
            .setItems(deviceNames) { _, which ->
                viewModel.onRcbDeviceSelected(devices[which].first)
            }
            .setOnDismissListener { setAddRcbClickListener() }
            .create()
            .also { it.show() }
    }

    private fun updateModels(models: List<RcbItemModel>) {
        for (i in models.indices) {
            recyclerAdapter.updateItem(models[i], i)
        }
    }

    private fun setAddRcbClickListener() =
        addRcbButton.setOnClickListener {
            it.setOnClickListener(null) // Prevent double-click
            viewModel.onAddRcbClicked()
        }

    private fun confirmDeleteBuffer(bufferId: Int) {
        showDeleteDialog(
            R.string.delete_buffer_dialog_title,
            R.string.delete_buffer_dialog_message
        ) {
            viewModel.onRemoveRcbItemClicked(bufferId)
        }
    }

    private fun confirmDeleteAllBuffers() =
        showDeleteDialog(
            R.string.delete_all_buffers_dialog_title,
            R.string.delete_all_buffers_dialog_message
        ) {
            viewModel.onRemoveAllRcbsClicked()
        }

    private fun showDeleteDialog(titleRes: Int, messageRes: Int, positiveCallback: () -> Unit) =
        AlertDialog.Builder(requireContext())
            .setTitle(titleRes)
            .setMessage(messageRes)
            .setPositiveButton(R.string.delete) { _, _ -> positiveCallback() }
            .setNegativeButton(android.R.string.cancel, null)
            .show()

    override fun onResumeClicked(modelId: Int) =
        viewModel.onToggleRcb(modelId)

    override fun onConnectClicked(modelId: Int) =
        viewModel.onConnectRcbClicked(modelId)

    override fun onVibrateUpdate(modelId: Int, value: Int) =
        viewModel.onSetVibrateValue(modelId, value)

    override fun onApplyClicked(modelId: Int) =
        viewModel.onConfigureRbClicked(modelId)

    override fun onProductUrlClicked(modelId: Int) =
        viewModel.onProductUrlClicked(modelId)

    override fun onDeleteClicked(modelId: Int) =
        confirmDeleteBuffer(modelId)

    override fun onEditConfig(modelId: Int) {
//        ::displayConfig
    }

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
                confirmDeleteAllBuffers()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
}

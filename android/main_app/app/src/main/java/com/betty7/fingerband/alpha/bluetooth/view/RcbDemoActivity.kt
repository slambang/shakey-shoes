package com.betty7.fingerband.alpha.bluetooth.view

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.betty7.fingerband.alpha.R
import com.betty7.fingerband.alpha.bluetooth.view.bufferitem.BufferItemViewListener
import kotlinx.android.synthetic.main.activity_test.*

class RcbDemoActivity : BluetoothPermissionActivity(), BufferItemViewListener {

    override fun onResumeClicked(modelId: Int) =
        viewModel.toggleRcb(modelId)

    override fun onConnectClicked(modelId: Int) =
        viewModel.onConnectRcbClicked(modelId)

    override fun onVibrateUpdate(modelId: Int, value: Int) =
        viewModel.setVibrateValue(modelId, value)

    override fun onApplyClicked(modelId: Int) =
        viewModel.onConnectRcbClicked(modelId)

    override fun onProductUrlClicked(modelId: Int) =
        viewModel.onProductUrlClicked(modelId)

    override fun onDeleteClicked(modelId: Int) =
        confirmDeleteBuffer(modelId)

    override fun onEditConfig(modelId: Int) {
//        ::displayConfig
    }

    private val viewModel: RcbDemoActivityViewModel by viewModels {
        RcbDemoViewModelFactory(this)
    }

    private lateinit var menu: Menu
    private val recyclerAdapter = BufferItemRecyclerAdapter(this)

    private var configDialog: ConfigDialogView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
        setSupportActionBar(activity_test_toolbar)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        initView()
    }

    private fun initView() {

        buffer_recycler_view.apply {
            adapter = recyclerAdapter
            itemAnimator = DefaultItemAnimator()
            layoutManager = LinearLayoutManager(context)
        }

        setAddBufferListener()
    }

    override fun onResume() {
        super.onResume()
        subscribeToViewModel()
    }

    private fun subscribeToViewModel() {
        viewModel.itemModelsLiveData.observe(this, Observer {
            recyclerAdapter.setItems(it)
            setDeleteAllEnabled(it.isNotEmpty())
        })

        viewModel.showDeviceListLiveData.observe(this, Observer {
            displayDeviceList(it)
        })

        viewModel.launchUrlLiveData.observe(this, Observer {
            onLaunchUrl(it)
        })

        viewModel.bufferItemPageLiveData.observe(this, Observer {
            recyclerAdapter.setPage(it.first, it.second)
        })

        viewModel.onStart()
    }

    override fun onPause() {
        super.onPause()
        viewModel.onPause()
    }

    override fun onStop() {
        super.onStop()
        viewModel.onStop()
    }

//    private fun updateItem(item: RcbItemModel, index: Int, isNew: Boolean) {
//        recyclerAdapter.updateItem(item, index, isNew)
//        setDeleteAllEnabled(true)
//    }

    private fun onLaunchUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.rcb_activity_menu, menu)
        this.menu = menu
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {
            R.id.rcb_activity_menu_delete_all -> {
                confirmDeleteAllBuffers()
                true
            }
            R.id.rcb_activity_menu_project_url -> {
                viewModel.onProjectUrlClicked()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    private fun confirmDeleteBuffer(bufferId: Int) {
        showDeleteDialog(
            R.string.delete_buffer_dialog_title,
            R.string.delete_buffer_dialog_message
        ) {
            viewModel.onDeleteRcbItemClicked(bufferId)
            setDeleteAllEnabled(recyclerAdapter.itemCount > 0)
        }
    }

    private fun confirmDeleteAllBuffers() =
        showDeleteDialog(
            R.string.delete_all_buffers_dialog_title,
            R.string.delete_all_buffers_dialog_message
        ) {
            viewModel.onDeleteAllBuffersClicked()
            setDeleteAllEnabled(false)
        }

    private fun showDeleteDialog(titleRes: Int, messageRes: Int, positiveCallback: () -> Unit) =
        AlertDialog.Builder(this)
            .setTitle(titleRes)
            .setMessage(messageRes)
            .setPositiveButton(R.string.delete) { _, _ -> positiveCallback() }
            .setNegativeButton(android.R.string.cancel, null)
            .show()

    private fun setDeleteAllEnabled(enabled: Boolean) {
        menu.findItem(R.id.rcb_activity_menu_delete_all).isEnabled = enabled
    }

    private fun displayDeviceList(devices: List<Pair<Int, String>>) {

        val deviceNames = devices.map { it.second }

        AlertDialog.Builder(this)
            .setTitle(R.string.select_device_title)
            .setItems(deviceNames.toTypedArray()) { _, which ->
                viewModel.onDeviceSelected(devices[which].first)
            }
            .setOnDismissListener { setAddBufferListener() }
            .create()
            .also { it.show() }
    }

    private fun setAddBufferListener() =
        add_buffer_button.setOnClickListener {
            it.setOnClickListener(null) // Prevent double-click
            viewModel.onCreateRcbServiceClicked()
        }

    private fun displayConfig(model: RcbItemModel) {
        if (configDialog == null) {
            configDialog =
                ConfigDialogView(model, this, ::onConfigUpdated) {
                    configDialog = null
                }
        }

        configDialog?.bind(model.page2.config)
    }

    private fun onConfigUpdated(
        model: RcbItemModel,
        numRefills: String,
        refillSize: String,
        windowSize: String,
        maxUnderflows: String
    ) = viewModel.checkRcbConfig(model.id, numRefills, refillSize, windowSize, maxUnderflows)
}

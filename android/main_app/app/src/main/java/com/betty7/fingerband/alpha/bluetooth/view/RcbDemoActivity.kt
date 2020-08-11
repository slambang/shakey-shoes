package com.betty7.fingerband.alpha.bluetooth.view

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.betty7.fingerband.alpha.R
import kotlinx.android.synthetic.main.activity_test.*

class RcbDemoActivity : BluetoothPermissionActivity() {

    private val viewModel: RcbDemoActivityViewModel by viewModels {
        RcbDemoViewModelFactory(this)
    }

    private lateinit var menu: Menu
    private lateinit var recyclerAdapter: BufferItemRecyclerAdapter

    private var configDialog: ConfigDialogView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
        setSupportActionBar(activity_test_toolbar)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        grantBluetooth(::initView, viewModel::onBluetoothDenied)
    }

    private fun initView() {

        recyclerAdapter = BufferItemRecyclerAdapter().apply {
            onConnectClicked = viewModel::onConnectBufferClicked
            onResumeClicked = viewModel::toggleBufferService
            onVibrateUpdate = viewModel::setVibrateValue
            onApplyClicked = viewModel::onConfigureBufferClicked
            onProductUrlClicked = viewModel::onProductUrlClicked
            onDeleteClicked = ::confirmDeleteBuffer
            onEditConfig = ::displayConfig
        }

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

    private fun subscribeToViewModel() =
        viewModel.subscribe(
            this,
            ::displayDeviceList,
            ::updateItem,
            ::onLaunchUrl,
            recyclerAdapter::setPage
        ).also { viewModel.onResume() }

    override fun onPause() {
        super.onPause()
        viewModel.onPause()
    }

    override fun onStop() {
        super.onStop()
        viewModel.onStop()
    }

    private fun updateItem(item: BufferItemViewModel) {
        recyclerAdapter.updateItem(item)
        setDeleteAllEnabled(true)
    }

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
            recyclerAdapter.deleteItem(bufferId)
            viewModel.onDeleteBufferItemClicked(bufferId)
            setDeleteAllEnabled(recyclerAdapter.itemCount > 0)
        }
    }

    private fun confirmDeleteAllBuffers() =
        showDeleteDialog(
            R.string.delete_all_buffers_dialog_title,
            R.string.delete_all_buffers_dialog_message
        ) {
            recyclerAdapter.clearItems()
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

    private fun displayDeviceList(deviceNames: List<String>) {
        AlertDialog.Builder(this)
            .setTitle(R.string.select_device_title)
            .setItems(deviceNames.toTypedArray()) { _, which ->
                viewModel.onDeviceSelected(this, which)
            }
            .setOnDismissListener { setAddBufferListener() }
            .create()
            .also { it.show() }
    }

    private fun setAddBufferListener() =
        add_buffer_button.setOnClickListener {
            it.setOnClickListener(null) // Prevent double-click
            viewModel.onCreateBufferClicked()
        }

    private fun displayConfig(model: BufferItemViewModel) {
        if (configDialog == null) {
            configDialog =
                ConfigDialogView(model.id, model.header.deviceName, this, ::onConfigUpdated) {
                    configDialog = null
                }
        }

        configDialog?.bind(model.page1.config)
    }

    private fun onConfigUpdated(
        bufferId: Int,
        numRefills: String,
        refillSize: String,
        windowSize: String,
        maxUnderflows: String
    ) = viewModel.checkConfig(bufferId, numRefills, refillSize, windowSize, maxUnderflows)
}

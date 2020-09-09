package com.slambang.shakeyshoes.view.splash

import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import com.google.android.material.snackbar.Snackbar
import com.slambang.shakeyshoes.R
import com.slambang.shakeyshoes.view.base.BaseViewFragment

class SplashViewFragment : BaseViewFragment<SplashViewModelImpl>() {

    override val layoutResId = R.layout.fragment_splash

    private val viewModel by lazy { of<SplashViewModelImpl>() }

    private lateinit var messageTextView: TextView
    private lateinit var grantPermissionButton: TextView

    override fun initView(root: View) {
        messageTextView = root.findViewById(R.id.splash_message)

        grantPermissionButton = root.findViewById(R.id.grant_permission_button)
        grantPermissionButton.setOnClickListener {
            viewModel.onPermissionButtonClicked()
        }
    }

    override fun onStart() {
        super.onStart()
        observeViewModel()
        viewModel.onStart()
    }

    private fun observeViewModel() {
        observe(viewModel.viewState) {
            showWarningMessage(it)
        }

        observe(viewModel.snackBarState) {
            showSnackBar(it)
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.onPause()
    }

    private fun showWarningMessage(state: SplashViewState) {

        with(messageTextView) {
            text = state.message
            isVisible = true
        }

        with(grantPermissionButton) {
            text = state.permissionButtonText
            isVisible = state.showPermissionButton
        }
    }

    private fun showSnackBar(message: String) =
        Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show()

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        viewModel.onRequestPermissionsResult(
            requestCode,
            permissions.toList(),
            grantResults.toList()
        )
    }
}

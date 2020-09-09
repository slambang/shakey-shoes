package com.slambang.shakeyshoes.view.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import com.slambang.shakeyshoes.di.view.ViewModelProviderFactory
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

// Separation of concerns between Fragment and View functionality
abstract class BaseViewFragment<ViewModelType : ViewModel> : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProviderFactory<ViewModelType>

    protected abstract val layoutResId: Int

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(layoutResId, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AndroidSupportInjection.inject(this)
        initView(view)
    }

    // Optimisation: directly inject the ViewModel
    protected inline fun <reified T : ViewModel> of() =
        ViewModelProviders.of(this, viewModelFactory).get(T::class.java)

    protected inline fun <reified T : Any> observe(
        liveData: LiveData<T>,
        crossinline observer: (T) -> Unit
    ) = liveData.observe(viewLifecycleOwner, {
        observer(it)
    })

    abstract fun initView(root: View)
}

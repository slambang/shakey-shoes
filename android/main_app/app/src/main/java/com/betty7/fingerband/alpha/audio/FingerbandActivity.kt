package com.betty7.fingerband.alpha.audio

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.betty7.fingerband.alpha.R

import kotlinx.android.synthetic.main.activity_shoe.*

class FingerbandActivity : AppCompatActivity() {

    private var fingerbandInteractor = FingerbandFactory.newFingerbandInteractor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fingerband)
        setSupportActionBar(toolbar)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onResume() {
        super.onResume()
        fingerbandInteractor.start(intent)
    }
}

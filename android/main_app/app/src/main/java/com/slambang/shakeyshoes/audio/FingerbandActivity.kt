package com.slambang.shakeyshoes.audio

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.slambang.shakeyshoes.R

class FingerbandActivity : AppCompatActivity() {

    private var fingerbandInteractor = FingerbandFactory.newFingerbandInteractor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fingerband)
        setSupportActionBar(findViewById(R.id.toolbar))
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onResume() {
        super.onResume()
        fingerbandInteractor.start(intent)
    }
}

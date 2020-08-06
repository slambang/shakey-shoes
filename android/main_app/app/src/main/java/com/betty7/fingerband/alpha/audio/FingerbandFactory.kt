package com.betty7.fingerband.alpha.audio

class FingerbandFactory {
    companion object {
        fun newFingerbandInteractor(): FingerbandInteractor {
            val jniBridge = JniBridgeImpl()
            val mapper = IntentConfigMapperImpl()
            return FingerbandInteractorImpl(jniBridge, mapper)
        }
    }
}

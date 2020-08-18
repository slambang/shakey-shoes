package com.slambang.shakeyshoes.audio

class FingerbandFactory {
    companion object {
        fun newFingerbandInteractor(): FingerbandInteractor {
            val jniBridge = JniBridgeImpl()
            val mapper = IntentConfigMapperImpl()
            return FingerbandInteractorImpl(jniBridge, mapper)
        }
    }
}

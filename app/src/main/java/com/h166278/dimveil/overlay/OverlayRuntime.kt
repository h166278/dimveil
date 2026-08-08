package com.h166278.dimveil.overlay

import com.h166278.dimveil.domain.DimMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object OverlayRuntime {
    private val mutableState = MutableStateFlow(OverlayState())
    val state: StateFlow<OverlayState> = mutableState.asStateFlow()

    fun running(
        requestedDepth: Int,
        appliedDepth: Int,
        mode: DimMode,
        host: OverlayHostKind
    ) {
        mutableState.value = OverlayState(
            active = true,
            requestedDepth = requestedDepth,
            appliedDepth = appliedDepth,
            mode = mode,
            host = host
        )
    }

    fun failed(error: OverlayError) {
        mutableState.value = OverlayState(error = error)
    }

    fun stopped() {
        mutableState.value = OverlayState()
    }
}

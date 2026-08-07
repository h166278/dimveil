package com.h166278.dimveil.service

import com.h166278.dimveil.domain.DimMode

sealed interface OverlayCommand {
    data class Start(val depth: Int, val mode: DimMode) : OverlayCommand
    data class Update(val depth: Int, val mode: DimMode) : OverlayCommand
    data object Stop : OverlayCommand
}

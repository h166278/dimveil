package com.h166278.dimveil.domain

data class DimSettings(
    val mode: DimMode = DimMode.NIGHT,
    val depth: Int = DimMode.NIGHT.defaultDepth,
    val customDepth: Int = DimMode.CUSTOM.defaultDepth
)

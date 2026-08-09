package com.h166278.dimveil.data

import com.h166278.dimveil.domain.AutoStartMode
import com.h166278.dimveil.domain.DimMode
import com.h166278.dimveil.domain.DimSettings
import kotlinx.coroutines.flow.Flow

interface DimPreferences {
    val settings: Flow<DimSettings>
    suspend fun selectMode(mode: DimMode)
    suspend fun setDepth(depth: Int)
    suspend fun setAutoStartMode(mode: AutoStartMode)
}

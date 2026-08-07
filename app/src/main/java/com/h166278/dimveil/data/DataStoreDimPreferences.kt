package com.h166278.dimveil.data

import android.content.Context
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.h166278.dimveil.domain.DimMode
import com.h166278.dimveil.domain.DimSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dimDataStore by preferencesDataStore("dimveil_preferences")

class DataStoreDimPreferences(private val context: Context) : DimPreferences {
    private val modeKey = intPreferencesKey("mode")
    private val depthKey = intPreferencesKey("depth")
    private val customDepthKey = intPreferencesKey("custom_depth")
    override val settings: Flow<DimSettings> = context.dimDataStore.data.map { values ->
        val mode = DimMode.entries.getOrElse(values[modeKey] ?: 0) { DimMode.NIGHT }
        val custom = DimMode.clamp(values[customDepthKey] ?: DimMode.CUSTOM.defaultDepth)
        DimSettings(mode, DimMode.clamp(values[depthKey] ?: mode.depth(custom)), custom)
    }
    override suspend fun selectMode(mode: DimMode) {
        context.dimDataStore.edit { values ->
            val custom = DimMode.clamp(values[customDepthKey] ?: DimMode.CUSTOM.defaultDepth)
            values[modeKey] = mode.ordinal
            values[depthKey] = mode.depth(custom)
        }
    }
    override suspend fun setDepth(depth: Int) {
        context.dimDataStore.edit { values ->
            val safe = DimMode.clamp(depth)
            values[depthKey] = safe
            if (DimMode.entries.getOrElse(values[modeKey] ?: 0) { DimMode.NIGHT } == DimMode.CUSTOM) values[customDepthKey] = safe
        }
    }
}

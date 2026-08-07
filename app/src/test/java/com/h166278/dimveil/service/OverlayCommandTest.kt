package com.h166278.dimveil.service

import com.h166278.dimveil.domain.DimMode
import org.junit.Assert.assertEquals
import org.junit.Test

class OverlayCommandTest {
    @Test fun startCarriesApprovedModeAndDepth() { val command=OverlayCommand.Start(62,DimMode.NIGHT); assertEquals(62,command.depth); assertEquals(DimMode.NIGHT,command.mode) }
    @Test fun stopIsExplicitCommand() { assertEquals(OverlayCommand.Stop, OverlayCommand.Stop) }
}

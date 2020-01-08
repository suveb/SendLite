package com.s.sendlite.ui.ConnectedFragment

import org.junit.Assert
import org.junit.Test

class ConnectedViewModelTest {

//    @Test
//    fun calculatePercentageTest() {
//        Assert.assertEquals(55, connectedViewModel.calculatePercentage(2000000000,1100000099))
//    }
//
//    @Test
//    fun sizeReceivedTest() {
//        Assert.assertEquals("511.50MB", connectedViewModel.sizeInWord(512 * 1024 * 1023))
//    }

    private fun remainingTime(elapsedTime: Long, totalBytes: Long, averageSpeed: Long) =
        totalBytes / averageSpeed - elapsedTime /1000

    @Test
    fun sizeReceivedTest() {
        Assert.assertEquals(2, remainingTime(8000,520,52))
    }

    @Test
    fun sizeReceivedTest2() {
        Assert.assertEquals(7, remainingTime(3000,1020,102))
    }
}
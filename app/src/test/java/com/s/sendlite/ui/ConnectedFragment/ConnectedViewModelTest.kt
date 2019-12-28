package com.s.sendlite.ui.ConnectedFragment

import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class ConnectedViewModelTest {
    private lateinit var connectedViewModel : ConnectedViewModel

    @Before
    fun setUp() {
        connectedViewModel = ConnectedViewModel()
    }

    @After
    fun tearDown() {
    }

    @Test
    fun calculatePercentageTest() {
        Assert.assertEquals(55, connectedViewModel.calculatePercentage(2000000000,1100000099))
    }

    @Test
    fun sizeReceivedTest() {
        Assert.assertEquals("511.50MB", connectedViewModel.sizeReceived(512*1024*1023))
    }
}
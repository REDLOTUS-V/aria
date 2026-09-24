package com.event.chats

import app.cash.turbine.test
import com.event.chats.ui.ChatViewmodel
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test


@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewmodelTest {


    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()


    @Test
    fun responseTest() = runTest {
        val repository = FakeRepository()
        val viewmodel = ChatViewmodel(repository)

        viewmodel.responseStream.test {
            assertEquals(null, awaitItem())
            viewmodel.response(context = mockk(), msg = "hello")

            advanceUntilIdle()
            assertEquals("hello", awaitItem())
            assertEquals("hello ray", awaitItem())
            assertEquals("hello ray!", awaitItem())

        }
    }
    @Test
    fun responseErrorTest()= runTest{
        val repository = FakeRepository()
        val viewmodel = ChatViewmodel(repository)
        repository.isError = true
        viewmodel.response(mockk(), "hi")

        advanceUntilIdle()
        assertTrue(viewmodel.sendState.value is SendState.Error)
    }
}

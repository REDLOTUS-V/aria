package com.event.chats

import com.event.chats.data.local.Message
import com.event.chats.data.network.model.Content
import com.event.chats.data.network.model.Part
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import kotlin.uuid.Uuid

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testing(){
        val actual = message.toContent()
        val expected = content

        assertEquals(expected, actual)
    }

    @ParameterizedTest
    @MethodSource("messages")
    fun testMultiplecase(
        message: Message,
        expected: Content
    ){
        val actual = message.toContent()

        assertEquals(expected, actual)
    }

    companion object {

        @JvmStatic
        fun messages() = listOf(
            Arguments.of(
                Message(
                    user = true,
                    content = "hello",
                    conversationId = "1"
                ),
                Content(
                    role = "user",
                    parts = listOf(Part(text = "hello"))
                )
            ),

            Arguments.of(
                Message(
                    user = false,
                    content = "hey",
                    conversationId = "2"
                ),
                Content(
                    role = "model",
                    parts = listOf(Part(text = "hey"))
                )
            )
        )
    }


}

val message = Message(
    user = true,
    content = "hey",
    conversationId = Uuid.random().toString()
)

val content = Content(
    role = "user",
    parts = listOf(Part(text = "hey"))
)

fun Message.toContent(): Content{
    return Content(
        role = if (user) "user" else "model",
        parts = listOf(Part(text = content))
    )
}

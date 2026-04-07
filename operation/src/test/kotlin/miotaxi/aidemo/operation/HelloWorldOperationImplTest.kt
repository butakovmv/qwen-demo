package miotaxi.aidemo.operation

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class HelloWorldOperationImplTest {
    private val operation = HelloWorldOperationImpl()

    @Test
    fun `execute returns HelloResponseImpl with expected message`() =
        runTest {
            val result = operation.execute()

            assertTrue(result is HelloWorldOperationImpl.HelloResponseImpl)
            assertEquals("Hello, World!", result.message)
        }

    @Test
    fun `execute returns non-empty message`() =
        runTest {
            val result = operation.execute()

            assertTrue(result.message.isNotBlank())
        }
}

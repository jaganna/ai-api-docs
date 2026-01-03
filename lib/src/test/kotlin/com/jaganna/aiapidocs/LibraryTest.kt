package com.jaganna.aiapidocs

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals

class LibraryTest {
    @Test
    fun testGreet() {
        assertEquals("Hello, Michal from lib!", Library.greet("Michal"))
    }
}

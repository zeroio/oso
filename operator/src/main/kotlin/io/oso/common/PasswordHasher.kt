package io.oso.common

import com.password4j.CompressedPBKDF2Function
import com.password4j.Password
import com.password4j.types.Hmac
import java.nio.CharBuffer
import java.util.Arrays

interface PasswordHasher {
    fun hash(password: CharArray): String
}

abstract class AbstractPasswordHasher : PasswordHasher {
    protected fun cleanup(password: CharBuffer) {
        password.clear()
        val passwordOverwrite = CharArray(password.capacity())
        Arrays.fill(passwordOverwrite, '\u0000')
        password.put(passwordOverwrite)
    }
}

class PBKDF2PasswordHasher : AbstractPasswordHasher() {
    private val hashingFunction = CompressedPBKDF2Function.getInstance(Hmac.SHA256, 600_000, 256)

    override fun hash(password: CharArray): String {
        val passwordBuffer = CharBuffer.wrap(password)
        return try {
            Password.hash(passwordBuffer)
                .addRandomSalt(128)
                .with(hashingFunction)
                .result
        } finally {
            cleanup(passwordBuffer)
        }
    }
}

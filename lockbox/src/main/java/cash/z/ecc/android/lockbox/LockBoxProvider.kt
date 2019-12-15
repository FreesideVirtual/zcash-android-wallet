package cash.z.ecc.android.lockbox

/**
 * Generic interface to separate the underlying implementation used by this module and the code that
 * interacts with it.
 */
interface LockBoxProvider {
    fun setBytes(key: String, value: ByteArray)
    fun getBytes(key: String): ByteArray

    fun setCharsUtf8(key: String, value: CharArray)
    fun getCharsUtf8(key: String): CharArray
}

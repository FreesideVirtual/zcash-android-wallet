package cash.z.ecc.kotlin.mnemonic

/**
 * Generic interface to separate the underlying implementation used by this module and the code that
 * interacts with it.
 */
interface MnemonicProvider {
    /**
     * Generate a random 24-word mnemonic phrase
     */
    fun nextMnemonic(): CharArray

    /**
     * Generate a random 24-word mnemonic phrase, represented as a list of words.
     */
    fun nextMnemonicList(): List<CharArray>

    /**
     * Generate a 64-byte seed from the 24-word mnemonic phrase
     */
    fun toSeed(mnemonic: CharArray): ByteArray
}

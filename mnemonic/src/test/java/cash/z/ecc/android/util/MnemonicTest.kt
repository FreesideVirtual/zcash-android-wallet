package cash.z.ecc.android.util

import cash.z.ecc.kotlin.mnemonic.MnemonicProvider
import cash.z.ecc.kotlin.mnemonic.Mnemonics
import io.github.novacrypto.SecureCharBuffer
import io.github.novacrypto.bip39.MnemonicGenerator
import io.github.novacrypto.bip39.SeedCalculator
import io.github.novacrypto.bip39.Words
import io.github.novacrypto.bip39.wordlists.English
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.security.SecureRandom


class MnemonicTest {

    lateinit var mnemonics: MnemonicProvider

    @Before
    fun start() {
        mnemonics = Mnemonics()
    }

    @Test
    fun testSeed_fromMnemonic() {
        val seed = mnemonics.run {
            toSeed(nextMnemonic())
        }
        assertEquals(64, seed.size)
    }

    @Test
    fun testMnemonic_create() {
        val words = String(mnemonics.nextMnemonic()).split(' ')
        assertEquals(24, words.size)
        validate(words)
    }

    @Test
    fun testMnemonic_createList() {
        val words = mnemonics.nextMnemonicList()
        assertEquals(24, words.size)
        validate(words.map { String(it) })
    }

    private fun validate(words: List<String>) {
        // return or crash!
        words.forEach { word ->
            var i = 0
            while (true) {
                if (English.INSTANCE.getWord(i++) == word) {
                    println(word)
                    break
                }

            }
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //       Sample code for working with SecureCharBuffer
    //       (but the underlying implementation isn't compatible with SeedCalculator.calculateSeed)
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    fun testMneumonicFromSeed_secure() {
        SecureCharBuffer().use { secure ->
            val entropy = ByteArray(Words.TWENTY_FOUR.byteLength()).also {
                SecureRandom().nextBytes(it)
                MnemonicGenerator(English.INSTANCE).createMnemonic(it, secure::append)
            }
            val words = secure.toWords()
            assertEquals(24, words.size)

            words.forEach { word ->
                // verify no spaces
                assertTrue(word.all { it != ' ' })
            }

            val mnemonic = secure.toStringAble().toString()
            val seed = SeedCalculator().calculateSeed(mnemonic, "")

            assertEquals(64, seed.size)
        }
    }
}

private fun CharSequence.toWords(): List<CharSequence> {
    return mutableListOf<CharSequence>().let { result ->
        var index = 0
        repeat(length) {
            if (this[it] == ' ') {
                result.add(subSequence(index, it))
                index = it + 1
            }
        }
        result.add(subSequence(index, length))
        result
    }
}

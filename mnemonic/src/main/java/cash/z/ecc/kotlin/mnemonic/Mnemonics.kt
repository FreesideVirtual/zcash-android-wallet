package cash.z.ecc.kotlin.mnemonic

import io.github.novacrypto.bip39.MnemonicGenerator
import io.github.novacrypto.bip39.SeedCalculator
import io.github.novacrypto.bip39.Words
import io.github.novacrypto.bip39.wordlists.English
import java.security.SecureRandom

class Mnemonics : MnemonicProvider {
    override fun nextMnemonic(): CharArray {
        // TODO: either find another library that allows for doing this without strings or modify this code to leverage SecureCharBuffer (which doesn't work well with SeedCalculator.calculateSeed, which expects a string so for that reason, we just use Strings here)
        return StringBuilder().let { builder ->
            ByteArray(Words.TWENTY_FOUR.byteLength()).also {
                SecureRandom().nextBytes(it)
                MnemonicGenerator(English.INSTANCE).createMnemonic(it) { c ->
                    builder.append(c)
                }
            }
            builder.toString().toCharArray()
        }
    }

    override fun nextMnemonicList(): List<CharArray> {
        return WordListBuilder().let { builder ->
            ByteArray(Words.TWENTY_FOUR.byteLength()).also {
                SecureRandom().nextBytes(it)
                MnemonicGenerator(English.INSTANCE).createMnemonic(it) { c ->
                    builder.append(c)
                }
            }
            builder.wordList
        }
    }

    override fun toSeed(mnemonic: CharArray): ByteArray {
        // TODO: either find another library that allows for doing this without strings or modify this code to leverage SecureCharBuffer (which doesn't work well with SeedCalculator.calculateSeed, which expects a string so for that reason, we just use Strings here)
        return SeedCalculator().calculateSeed(mnemonic.toString(), "")
    }

    class WordListBuilder {
        val wordList = mutableListOf<CharArray>()
        fun append(c: CharSequence) {
            if (c[0] != English.INSTANCE.space) addWord(c)
        }
        private fun addWord(c: CharSequence) {
            c.length.let { size ->
                val word = CharArray(size)
                repeat(size) {
                    word[it] = c[it]
                }
                wordList.add(word)
            }
        }
    }
}
package cash.z.ecc.kotlin.mnemonic

import cash.z.android.plugin.MnemonicPlugin
import cash.z.ecc.android.bip39.Mnemonics.MnemonicCode
import cash.z.ecc.android.bip39.Mnemonics.WordCount
import cash.z.ecc.android.bip39.toEntropy
import cash.z.ecc.android.bip39.toSeed
import java.util.*
import javax.inject.Inject

class Mnemonics @Inject constructor(): MnemonicPlugin {
    override fun fullWordList(languageCode: String): List<String> {
        return cash.z.ecc.android.bip39.Mnemonics.getCachedWords(Locale.ENGLISH.language)
    }

    override fun nextEntropy(): ByteArray {
        return WordCount.COUNT_24.toEntropy()
    }

    override fun nextMnemonic(): CharArray {
        return nextMnemonic(nextEntropy())
    }

    override fun nextMnemonic(entropy: ByteArray): CharArray {
        return MnemonicCode(entropy).chars
    }

    override fun nextMnemonicList(): List<CharArray> {
        return nextMnemonicList(nextEntropy())
    }

    override fun nextMnemonicList(entropy: ByteArray): List<CharArray> {
        return MnemonicCode(entropy).map { it.toCharArray() }
    }

    override fun toSeed(mnemonic: CharArray): ByteArray {
        return MnemonicCode(mnemonic).toSeed()
    }

    override fun toWordList(mnemonic: CharArray): List<CharArray> {
        val wordList = mutableListOf<CharArray>()
        var cursor = 0
        repeat(mnemonic.size) { i ->
            val isSpace = mnemonic[i] == ' '
            if (isSpace || i == (mnemonic.size - 1)) {
                val wordSize = i - cursor + if (isSpace) 0 else 1
                wordList.add(CharArray(wordSize).apply {
                    repeat(wordSize) {
                        this[it] = mnemonic[cursor + it]
                    }
                })
                cursor = i + 1
            }
        }
        return wordList
    }
}
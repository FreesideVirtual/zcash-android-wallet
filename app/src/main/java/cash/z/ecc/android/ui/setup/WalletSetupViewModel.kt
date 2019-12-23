package cash.z.ecc.android.ui.setup

import androidx.lifecycle.ViewModel
import cash.z.ecc.android.feedback.Feedback
import cash.z.ecc.android.feedback.Report.MetricType.*
import cash.z.ecc.android.feedback.measure
import cash.z.ecc.android.lockbox.LockBox
import cash.z.ecc.android.ui.setup.WalletSetupViewModel.WalletSetupState.*
import cash.z.ecc.kotlin.mnemonic.Mnemonics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class WalletSetupViewModel @Inject constructor(val mnemonics: Mnemonics, val lockBox: LockBox) :
    ViewModel() {

    enum class WalletSetupState {
        UNKNOWN, SEED_WITH_BACKUP, SEED_WITHOUT_BACKUP, NO_SEED
    }

    fun checkSeed(): Flow<WalletSetupState> = flow {
        when {
            lockBox.getBoolean(LockBoxKey.HAS_BACKUP) -> emit(SEED_WITH_BACKUP)
            lockBox.getBoolean(LockBoxKey.HAS_SEED) -> emit(SEED_WITHOUT_BACKUP)
            else -> emit(NO_SEED)
        }
    }

    /**
     * Take all the steps necessary to create a new wallet and measure how long it takes.
     *
     * @param feedback the object used for measurement.
     */
    suspend fun createWallet(feedback: Feedback): ByteArray = withContext(Dispatchers.IO){
        check(!lockBox.getBoolean(LockBoxKey.HAS_SEED)) {
            "Error! Cannot create a seed when one already exists! This would overwrite the" +
                    " existing seed and could lead to a loss of funds if the user has no backup!"
        }

        feedback.measure(WALLET_CREATED) {
            mnemonics.run {
                feedback.measure(ENTROPY_CREATED) { nextEntropy() }.let { entropy ->
                    feedback.measure(SEED_PHRASE_CREATED) { nextMnemonic(entropy) }.let { seedPhrase ->
                            feedback.measure(SEED_CREATED) { toSeed(seedPhrase) }.let { bip39Seed ->

                                lockBox.setCharsUtf8(LockBoxKey.SEED_PHRASE, seedPhrase)
                                lockBox.setBoolean(LockBoxKey.HAS_SEED_PHRASE, true)

                                lockBox.setBytes(LockBoxKey.SEED, bip39Seed)
                                lockBox.setBoolean(LockBoxKey.HAS_SEED, true)

                                bip39Seed
                            }
                        }
                }
            }
        }
    }

   /**
    * Take all the steps necessary to import a wallet and measure how long it takes.
    *
    * @param feedback the object used for measurement.
    */
   suspend fun importWallet(
       feedback: Feedback,
       seedPhrase: CharArray
   ): ByteArray = withContext(Dispatchers.IO) {
       check(!lockBox.getBoolean(LockBoxKey.HAS_SEED)) {
           "Error! Cannot import a seed when one already exists! This would overwrite the" +
                   " existing seed and could lead to a loss of funds if the user has no backup!"
       }

       feedback.measure(WALLET_IMPORTED) {
           mnemonics.run {
               feedback.measure(SEED_IMPORTED) { toSeed(seedPhrase) }.let { bip39Seed ->

                   lockBox.setCharsUtf8(LockBoxKey.SEED_PHRASE, seedPhrase)
                   lockBox.setBoolean(LockBoxKey.HAS_SEED_PHRASE, true)

                   lockBox.setBytes(LockBoxKey.SEED, bip39Seed)
                   lockBox.setBoolean(LockBoxKey.HAS_SEED, true)

                   bip39Seed
               }
           }
       }
   }



    object LockBoxKey {
        const val SEED = "cash.z.ecc.android.SEED"
        const val SEED_PHRASE = "cash.z.ecc.android.SEED_PHRASE"
        const val HAS_SEED = "cash.z.ecc.android.HAS_SEED"
        const val HAS_SEED_PHRASE = "cash.z.ecc.android.HAS_SEED_PHRASE"
        const val HAS_BACKUP = "cash.z.ecc.android.HAS_BACKUP"
    }
}
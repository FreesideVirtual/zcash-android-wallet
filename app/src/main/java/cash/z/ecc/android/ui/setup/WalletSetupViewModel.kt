package cash.z.ecc.android.ui.setup

import androidx.lifecycle.ViewModel
import cash.z.ecc.android.ZcashWalletApp
import cash.z.ecc.android.feedback.Feedback
import cash.z.ecc.android.feedback.Report.MetricType.*
import cash.z.ecc.android.feedback.measure
import cash.z.ecc.android.lockbox.LockBox
import cash.z.ecc.android.ui.setup.WalletSetupViewModel.WalletSetupState.*
import cash.z.ecc.kotlin.mnemonic.Mnemonics
import cash.z.ecc.android.sdk.Initializer
import cash.z.ecc.android.sdk.Initializer.DefaultBirthdayStore
import cash.z.ecc.android.sdk.Initializer.DefaultBirthdayStore.Companion.ImportedWalletBirthdayStore
import cash.z.ecc.android.sdk.Initializer.DefaultBirthdayStore.Companion.NewWalletBirthdayStore
import cash.z.ecc.android.sdk.ext.twig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class WalletSetupViewModel @Inject constructor() : ViewModel() {

    @Inject
    lateinit var mnemonics: Mnemonics

    @Inject
    lateinit var lockBox: LockBox

    @Inject
    lateinit var feedback: Feedback

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
     * Re-open an existing wallet. This is the most common use case, where a user has previously
     * created or imported their seed and is returning to the wallet. In other words, this is the
     * non-FTUE case.
     */
    fun openWallet(): Initializer {
        twig("Opening existing wallet")
        return ZcashWalletApp.component.initializerSubcomponent()
            .create(DefaultBirthdayStore(ZcashWalletApp.instance)).run {
                initializer().open(birthdayStore().getBirthday())
            }
    }

    suspend fun newWallet(): Initializer {
        twig("Initializing new wallet")
        return ZcashWalletApp.component.initializerSubcomponent()
            .create(NewWalletBirthdayStore(ZcashWalletApp.instance)).run {
                initializer().apply {
                    new(createWallet(), birthdayStore().getBirthday())
                }
            }
    }

    suspend fun importWallet(seedPhrase: String, birthdayHeight: Int): Initializer {
        twig("Importing wallet. Requested birthday: $birthdayHeight")
        return ZcashWalletApp.component.initializerSubcomponent()
            .create(ImportedWalletBirthdayStore(ZcashWalletApp.instance, birthdayHeight)).run {
                initializer().apply {
                    import(importWallet(seedPhrase.toCharArray()), birthdayStore().getBirthday())
                }
        }
    }

    /**
     * Take all the steps necessary to create a new wallet and measure how long it takes.
     *
     * @param feedback the object used for measurement.
     */
    private suspend fun createWallet(): ByteArray = withContext(Dispatchers.IO) {
        check(!lockBox.getBoolean(LockBoxKey.HAS_SEED)) {
            "Error! Cannot create a seed when one already exists! This would overwrite the" +
                    " existing seed and could lead to a loss of funds if the user has no backup!"
        }

        feedback.measure(WALLET_CREATED) {
            mnemonics.run {
                feedback.measure(ENTROPY_CREATED) { nextEntropy() }.let { entropy ->
                    feedback.measure(SEED_PHRASE_CREATED) { nextMnemonic(entropy) }
                        .let { seedPhrase ->
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

    suspend fun loadBirthdayHeight(): Int = withContext(Dispatchers.IO) {
        DefaultBirthdayStore(ZcashWalletApp.instance).getBirthday().height
    }



   /**
    * Take all the steps necessary to import a wallet and measure how long it takes.
    *
    * @param feedback the object used for measurement.
    */
   private suspend fun importWallet(
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
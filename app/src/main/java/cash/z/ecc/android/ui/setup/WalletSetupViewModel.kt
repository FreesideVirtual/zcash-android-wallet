package cash.z.ecc.android.ui.setup

import androidx.lifecycle.ViewModel
import cash.z.ecc.android.lockbox.LockBox
import cash.z.ecc.android.ui.setup.WalletSetupViewModel.LockBoxKey.HAS_BACKUP
import cash.z.ecc.android.ui.setup.WalletSetupViewModel.LockBoxKey.HAS_SEED
import cash.z.ecc.android.ui.setup.WalletSetupViewModel.LockBoxKey.SEED
import cash.z.ecc.android.ui.setup.WalletSetupViewModel.WalletSetupState.*
import cash.z.ecc.kotlin.mnemonic.Mnemonics
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class WalletSetupViewModel @Inject constructor(val mnemonics: Mnemonics, val lockBox: LockBox) :
    ViewModel() {

    enum class WalletSetupState {
        UNKNOWN, SEED_WITH_BACKUP, SEED_WITHOUT_BACKUP, NO_SEED
    }

    fun checkSeed(): Flow<WalletSetupState> = flow {
        when {
            lockBox.getBoolean(HAS_BACKUP) -> emit(SEED_WITH_BACKUP)
            lockBox.getBoolean(HAS_SEED) -> emit(SEED_WITHOUT_BACKUP)
            else -> emit(NO_SEED)
        }
    }

    fun createSeed() {
        check(!lockBox.getBoolean(HAS_SEED)) {
            "Error! Cannot create a seed when one already exists! This would overwrite the" +
                    " existing seed and could lead to a loss of funds if the user has no backup!"
        }

        mnemonics.apply {
            lockBox.setBytes(SEED, nextSeed())
            lockBox.setBoolean(HAS_SEED, true)
        }
    }

    object LockBoxKey {
        const val SEED = "cash.z.ecc.android.SEED1"
        const val HAS_SEED = "cash.z.ecc.android.HAS_SEED1"
        const val HAS_BACKUP = "cash.z.ecc.android.HAS_BACKUP1"
    }
}
package cash.z.ecc.android.di

import cash.z.ecc.android.feedback.FeedbackConsole
import cash.z.ecc.android.feedback.FeedbackCoordinator
import cash.z.ecc.android.feedback.FeedbackFile
import cash.z.ecc.android.feedback.FeedbackMixpanel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import javax.inject.Singleton

@Module
abstract class AppBindingModule {

    //
    // Feedback Observer Set
    //

    @Singleton
    @Binds
    @IntoSet
    abstract fun provideFeedbackFile(implementation: FeedbackFile)
            : FeedbackCoordinator.FeedbackObserver

    @Singleton
    @Binds
    @IntoSet
    abstract fun provideFeedbackConsole(implementation: FeedbackConsole)
            : FeedbackCoordinator.FeedbackObserver

    @Singleton
    @Binds
    @IntoSet
    abstract fun provideFeedbackMixpanel(implementation: FeedbackMixpanel)
            : FeedbackCoordinator.FeedbackObserver
}

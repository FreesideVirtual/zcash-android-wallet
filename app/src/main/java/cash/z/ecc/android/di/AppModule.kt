package cash.z.ecc.android.di

import cash.z.ecc.android.feedback.*
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import javax.inject.Singleton

@Module(includes = [AppBindingModule::class])
class AppModule {

    @Provides
    @Singleton
    fun provideFeedback() = Feedback()

    @Provides
    @Singleton
    fun provideFeedbackCoordinator(feedback: Feedback) = FeedbackCoordinator(feedback)


    //
    // Feedback Observer Set
    //

    @Provides
    @Singleton
    @IntoSet
    fun provideFeedbackFile(feedbackCoordinator: FeedbackCoordinator)
            : FeedbackCoordinator.FeedbackObserver = FeedbackFile(feedbackCoordinator)

    @Provides
    @Singleton
    @IntoSet
    fun provideFeedbackConsole(feedbackCoordinator: FeedbackCoordinator)
            : FeedbackCoordinator.FeedbackObserver = FeedbackConsole(feedbackCoordinator)

    @Provides
    @Singleton
    @IntoSet
    fun provideFeedbackMixpanel(feedbackCoordinator: FeedbackCoordinator)
            : FeedbackCoordinator.FeedbackObserver = FeedbackMixpanel(feedbackCoordinator)
}

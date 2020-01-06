package cash.z.ecc.android.di.module

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import cash.z.ecc.android.di.annotation.ActivityScope
import cash.z.ecc.android.feedback.*
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet

@Module
class MainActivityModule {

    @Provides
    @ActivityScope
    fun provideViewModelProvider(activity: FragmentActivity, factory: ViewModelProvider.Factory): ViewModelProvider {
        return ViewModelProvider(activity, factory)
    }

    @Provides
    @ActivityScope
    fun provideFeedback(): Feedback = Feedback()

    @Provides
    @ActivityScope
    fun provideFeedbackCoordinator(
        feedback: Feedback,
        defaultObservers: Set<@JvmSuppressWildcards FeedbackCoordinator.FeedbackObserver>
    ): FeedbackCoordinator = FeedbackCoordinator(feedback, defaultObservers)


    //
    // Default Feedback Observer Set
    //

    @Provides
    @ActivityScope
    @IntoSet
    fun provideFeedbackFile(): FeedbackCoordinator.FeedbackObserver = FeedbackFile()

    @Provides
    @ActivityScope
    @IntoSet
    fun provideFeedbackConsole(): FeedbackCoordinator.FeedbackObserver = FeedbackConsole()

    @Provides
    @ActivityScope
    @IntoSet
    fun provideFeedbackMixpanel(): FeedbackCoordinator.FeedbackObserver = FeedbackMixpanel()
}

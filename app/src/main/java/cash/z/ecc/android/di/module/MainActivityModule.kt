package cash.z.ecc.android.di.module

import cash.z.ecc.android.di.annotation.ActivityScope
import cash.z.ecc.android.di.component.InitializerSubcomponent
import cash.z.ecc.android.di.component.SynchronizerSubcomponent
import cash.z.ecc.android.feedback.*
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet

@Module(includes = [ViewModelsActivityModule::class], subcomponents = [SynchronizerSubcomponent::class, InitializerSubcomponent::class])
class MainActivityModule {

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

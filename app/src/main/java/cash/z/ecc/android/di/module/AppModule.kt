package cash.z.ecc.android.di.module

import android.content.ClipboardManager
import android.content.Context
import cash.z.ecc.android.ZcashWalletApp
import cash.z.ecc.android.di.component.MainActivitySubcomponent
import cash.z.ecc.android.feedback.*
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import javax.inject.Singleton

@Module(subcomponents = [MainActivitySubcomponent::class])
class AppModule {

    @Provides
    @Singleton
    fun provideAppContext(): Context = ZcashWalletApp.instance

    @Provides
    @Singleton
    fun provideClipboard(context: Context) =
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager


    //
    // Feedback
    //

    @Provides
    @Singleton
    fun provideFeedback(): Feedback = Feedback()

    @Provides
    @Singleton
    fun provideFeedbackCoordinator(
        feedback: Feedback,
        defaultObservers: Set<@JvmSuppressWildcards FeedbackCoordinator.FeedbackObserver>
    ): FeedbackCoordinator = FeedbackCoordinator(feedback, defaultObservers)


    //
    // Default Feedback Observer Set
    //

    @Provides
    @Singleton
    @IntoSet
    fun provideFeedbackFile(): FeedbackCoordinator.FeedbackObserver = FeedbackFile()

    @Provides
    @Singleton
    @IntoSet
    fun provideFeedbackConsole(): FeedbackCoordinator.FeedbackObserver = FeedbackConsole()

    @Provides
    @Singleton
    @IntoSet
    fun provideFeedbackMixpanel(): FeedbackCoordinator.FeedbackObserver = FeedbackMixpanel()
}

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

}

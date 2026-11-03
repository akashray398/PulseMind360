package com.dev.myapplication.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.dev.myapplication.data.local.AppDatabase
import com.dev.myapplication.data.local.dao.ActivityLogDao
import com.dev.myapplication.data.local.dao.AutomationRuleDao
import com.dev.myapplication.data.local.dao.BlackBoxDao
import com.dev.myapplication.data.local.dao.ContextSnapshotDao
import com.dev.myapplication.data.local.dao.CopilotConversationDao
import com.dev.myapplication.data.local.dao.DigitalTwinDao
import com.dev.myapplication.data.local.dao.FocusSessionDao
import com.dev.myapplication.data.local.dao.ReliabilityScorecardDao
import com.dev.myapplication.data.monitor.BatteryMonitor
import com.dev.myapplication.data.monitor.NetworkMonitor
import com.dev.myapplication.data.remote.NetworkInfoApi
import com.dev.myapplication.data.repository.UserPreferencesRepository
import com.dev.myapplication.domain.AnomalyDetector
import com.dev.myapplication.domain.AutomationEngine
import com.dev.myapplication.domain.FocusTimerManager
import com.dev.myapplication.domain.FramePerformanceMonitor
import com.dev.myapplication.domain.LiveContextEngine
import com.dev.myapplication.domain.NetworkDiagnosticsEngine
import com.dev.myapplication.domain.SmartInsightsEngine
import com.dev.myapplication.domain.blackbox.DeviceBlackBoxEngine
import com.dev.myapplication.domain.copilot.CopilotIntentEngine
import com.dev.myapplication.domain.copilot.CopilotToolDispatcher
import com.dev.myapplication.domain.core.PulseMindIntelligenceCore
import com.dev.myapplication.domain.predictive.PredictiveIntelligenceEngine
import com.dev.myapplication.domain.privacy.PrivacyRiskEngine
import com.dev.myapplication.domain.scorecard.ReliabilityScorecardEngine
import com.dev.myapplication.domain.twin.DigitalTwinEngine
import com.dev.myapplication.util.BiometricHelper
import com.dev.myapplication.util.NotificationHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "companion360.db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideActivityLogDao(db: AppDatabase): ActivityLogDao = db.activityLogDao()

    @Provides
    @Singleton
    fun provideAutomationRuleDao(db: AppDatabase): AutomationRuleDao = db.automationRuleDao()

    @Provides
    @Singleton
    fun provideFocusSessionDao(db: AppDatabase): FocusSessionDao = db.focusSessionDao()

    @Provides
    @Singleton
    fun provideIncidentDao(db: AppDatabase): com.dev.myapplication.data.local.dao.IncidentDao = db.incidentDao()

    @Provides
    @Singleton
    fun provideContextSnapshotDao(db: AppDatabase): ContextSnapshotDao = db.contextSnapshotDao()

    @Provides
    @Singleton
    fun provideCopilotConversationDao(db: AppDatabase): CopilotConversationDao = db.copilotConversationDao()

    @Provides
    @Singleton
    fun provideBlackBoxDao(db: AppDatabase): BlackBoxDao = db.blackBoxDao()

    @Provides
    @Singleton
    fun provideReliabilityScorecardDao(db: AppDatabase): ReliabilityScorecardDao = db.reliabilityScorecardDao()

    @Provides
    @Singleton
    fun provideDigitalTwinDao(db: AppDatabase): DigitalTwinDao = db.digitalTwinDao()

    @Provides
    @Singleton
    fun provideIncidentCorrelationEngine(
        activityLogDao: ActivityLogDao,
        incidentDao: com.dev.myapplication.data.local.dao.IncidentDao
    ): com.dev.myapplication.domain.IncidentCorrelationEngine {
        return com.dev.myapplication.domain.IncidentCorrelationEngine(activityLogDao, incidentDao)
    }

    @Provides
    @Singleton
    fun provideSentinelBaselineDao(db: AppDatabase): com.dev.myapplication.data.local.dao.SentinelBaselineDao = db.sentinelBaselineDao()

    @Provides
    @Singleton
    fun provideUserPreferencesRepository(@ApplicationContext context: Context): UserPreferencesRepository {
        return UserPreferencesRepository(context)
    }

    @Provides
    @Singleton
    fun provideBatteryMonitor(@ApplicationContext context: Context): BatteryMonitor {
        return BatteryMonitor(context)
    }

    @Provides
    @Singleton
    fun provideNetworkMonitor(@ApplicationContext context: Context): NetworkMonitor {
        return NetworkMonitor(context)
    }

    @Provides
    @Singleton
    fun provideNotificationHelper(@ApplicationContext context: Context): NotificationHelper {
        return NotificationHelper(context)
    }

    @Provides
    @Singleton
    fun provideBiometricHelper(@ApplicationContext context: Context): BiometricHelper {
        return BiometricHelper(context)
    }

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideAutomationEngine(
        automationRuleDao: AutomationRuleDao,
        activityLogDao: ActivityLogDao,
        notificationHelper: NotificationHelper,
        workManager: WorkManager
    ): AutomationEngine {
        return AutomationEngine(
            automationRuleDao = automationRuleDao,
            activityLogDao = activityLogDao,
            notificationHelper = notificationHelper,
            workManager = workManager
        )
    }

    @Provides
    @Singleton
    fun provideFocusTimerManager(
        focusSessionDao: FocusSessionDao,
        activityLogDao: ActivityLogDao,
        notificationHelper: NotificationHelper
    ): FocusTimerManager {
        return FocusTimerManager(
            focusSessionDao = focusSessionDao,
            activityLogDao = activityLogDao,
            notificationHelper = notificationHelper
        )
    }

    @Provides
    @Singleton
    fun provideSmartInsightsEngine(): SmartInsightsEngine {
        return SmartInsightsEngine()
    }

    @Provides
    @Singleton
    fun provideAnomalyDetector(): AnomalyDetector {
        return AnomalyDetector()
    }

    @Provides
    @Singleton
    fun provideSentinelEngine(
        sentinelBaselineDao: com.dev.myapplication.data.local.dao.SentinelBaselineDao
    ): com.dev.myapplication.domain.SentinelEngine {
        return com.dev.myapplication.domain.SentinelEngine(sentinelBaselineDao)
    }

    @Provides
    @Singleton
    fun provideLiveContextEngine(
        @ApplicationContext context: Context,
        batteryMonitor: BatteryMonitor,
        networkMonitor: NetworkMonitor,
        anomalyDetector: AnomalyDetector,
        sentinelEngine: com.dev.myapplication.domain.SentinelEngine,
        contextSnapshotDao: ContextSnapshotDao,
        incidentDao: com.dev.myapplication.data.local.dao.IncidentDao,
        activityLogDao: ActivityLogDao
    ): LiveContextEngine {
        return LiveContextEngine(
            context = context,
            batteryMonitor = batteryMonitor,
            networkMonitor = networkMonitor,
            anomalyDetector = anomalyDetector,
            sentinelEngine = sentinelEngine,
            contextSnapshotDao = contextSnapshotDao,
            incidentDao = incidentDao,
            activityLogDao = activityLogDao
        )
    }

    @Provides
    @Singleton
    fun providePredictiveIntelligenceEngine(
        contextSnapshotDao: ContextSnapshotDao
    ): PredictiveIntelligenceEngine {
        return PredictiveIntelligenceEngine(contextSnapshotDao)
    }

    @Provides
    @Singleton
    fun providePrivacyRiskEngine(
        @ApplicationContext context: Context
    ): PrivacyRiskEngine {
        return PrivacyRiskEngine(context)
    }

    @Provides
    @Singleton
    fun provideDeviceBlackBoxEngine(
        blackBoxDao: BlackBoxDao
    ): DeviceBlackBoxEngine {
        return DeviceBlackBoxEngine(blackBoxDao)
    }

    @Provides
    @Singleton
    fun provideReliabilityScorecardEngine(
        scorecardDao: ReliabilityScorecardDao,
        liveContextEngine: LiveContextEngine,
        predictiveEngine: PredictiveIntelligenceEngine
    ): ReliabilityScorecardEngine {
        return ReliabilityScorecardEngine(scorecardDao, liveContextEngine, predictiveEngine)
    }

    @Provides
    @Singleton
    fun provideDigitalTwinEngine(
        digitalTwinDao: DigitalTwinDao,
        liveContextEngine: LiveContextEngine
    ): DigitalTwinEngine {
        return DigitalTwinEngine(digitalTwinDao, liveContextEngine)
    }

    @Provides
    @Singleton
    fun provideCopilotToolDispatcher(
        @ApplicationContext context: Context,
        liveContextEngine: LiveContextEngine,
        batteryMonitor: BatteryMonitor,
        networkMonitor: NetworkMonitor,
        networkDiagnosticsEngine: NetworkDiagnosticsEngine,
        smartInsightsEngine: SmartInsightsEngine,
        sentinelBaselineDao: com.dev.myapplication.data.local.dao.SentinelBaselineDao,
        incidentDao: com.dev.myapplication.data.local.dao.IncidentDao,
        activityLogDao: ActivityLogDao
    ): CopilotToolDispatcher {
        return CopilotToolDispatcher(
            context = context,
            liveContextEngine = liveContextEngine,
            batteryMonitor = batteryMonitor,
            networkMonitor = networkMonitor,
            networkDiagnosticsEngine = networkDiagnosticsEngine,
            smartInsightsEngine = smartInsightsEngine,
            sentinelBaselineDao = sentinelBaselineDao,
            incidentDao = incidentDao,
            activityLogDao = activityLogDao
        )
    }

    @Provides
    @Singleton
    fun provideCopilotIntentEngine(): CopilotIntentEngine {
        return CopilotIntentEngine()
    }

    @Provides
    @Singleton
    fun provideAiCopilotEngine(
        incidentDao: com.dev.myapplication.data.local.dao.IncidentDao,
        sentinelBaselineDao: com.dev.myapplication.data.local.dao.SentinelBaselineDao,
        activityLogDao: ActivityLogDao,
        liveContextEngine: LiveContextEngine,
        copilotToolDispatcher: CopilotToolDispatcher,
        copilotIntentEngine: CopilotIntentEngine,
        copilotConversationDao: CopilotConversationDao
    ): com.dev.myapplication.domain.AiCopilotEngine {
        return com.dev.myapplication.domain.AiCopilotEngine(
            incidentDao = incidentDao,
            sentinelBaselineDao = sentinelBaselineDao,
            activityLogDao = activityLogDao,
            liveContextEngine = liveContextEngine,
            copilotToolDispatcher = copilotToolDispatcher,
            copilotIntentEngine = copilotIntentEngine,
            copilotConversationDao = copilotConversationDao
        )
    }

    @Provides
    @Singleton
    fun providePulseMindIntelligenceCore(
        @ApplicationContext context: Context,
        liveContextEngine: LiveContextEngine,
        predictiveEngine: PredictiveIntelligenceEngine,
        copilotEngine: com.dev.myapplication.domain.AiCopilotEngine,
        incidentCorrelationEngine: com.dev.myapplication.domain.IncidentCorrelationEngine,
        privacyRiskEngine: PrivacyRiskEngine,
        deviceBlackBoxEngine: DeviceBlackBoxEngine,
        reliabilityScorecardEngine: ReliabilityScorecardEngine,
        digitalTwinEngine: DigitalTwinEngine,
        batteryMonitor: BatteryMonitor,
        networkMonitor: NetworkMonitor,
        incidentDao: com.dev.myapplication.data.local.dao.IncidentDao,
        userPreferencesRepository: UserPreferencesRepository
    ): PulseMindIntelligenceCore {
        return PulseMindIntelligenceCore(
            context = context,
            liveContextEngine = liveContextEngine,
            predictiveEngine = predictiveEngine,
            copilotEngine = copilotEngine,
            incidentCorrelationEngine = incidentCorrelationEngine,
            privacyRiskEngine = privacyRiskEngine,
            deviceBlackBoxEngine = deviceBlackBoxEngine,
            reliabilityScorecardEngine = reliabilityScorecardEngine,
            digitalTwinEngine = digitalTwinEngine,
            batteryMonitor = batteryMonitor,
            networkMonitor = networkMonitor,
            incidentDao = incidentDao,
            userPreferencesRepository = userPreferencesRepository
        )
    }

    @Provides
    @Singleton
    fun provideFramePerformanceMonitor(): FramePerformanceMonitor {
        return FramePerformanceMonitor()
    }

    @Provides
    @Singleton
    fun provideNetworkDiagnosticsEngine(): NetworkDiagnosticsEngine {
        return NetworkDiagnosticsEngine()
    }

    @Provides
    @Singleton
    fun provideNetworkInfoApi(): NetworkInfoApi {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val headerInterceptor = Interceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "Companion360/1.0 (Android)")
                .build()
            chain.proceed(request)
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(headerInterceptor)
            .addInterceptor(logging)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl("https://api.ipify.org/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NetworkInfoApi::class.java)
    }
}

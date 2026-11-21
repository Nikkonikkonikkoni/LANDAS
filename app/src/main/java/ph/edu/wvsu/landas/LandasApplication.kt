package ph.edu.wvsu.landas

import android.app.Application
import android.content.Context
import ph.edu.wvsu.landas.data.LandasDatabase
import ph.edu.wvsu.landas.data.LandasRepository
import ph.edu.wvsu.landas.data.SessionStore
import ph.edu.wvsu.landas.util.LocationHelper
import ph.edu.wvsu.landas.util.NetworkMonitor
import ph.edu.wvsu.landas.util.NotificationHelper

class AppContainer(context: Context) {
    val database: LandasDatabase = LandasDatabase.getInstance(context)
    val sessionStore = SessionStore(context)
    val repository = LandasRepository(database, sessionStore)
    val networkMonitor = NetworkMonitor(context)
    val locationHelper = LocationHelper(context)
    val notificationHelper = NotificationHelper(context)
}

class LandasApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

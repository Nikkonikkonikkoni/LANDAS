package ph.edu.wvsu.landas.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.sessionDataStore by preferencesDataStore(name = "landas_session")

class SessionStore(private val context: Context) {
    private object Keys {
        val userId = stringPreferencesKey("user_id")
        val schoolId = stringPreferencesKey("school_id")
        val displayName = stringPreferencesKey("display_name")
        val role = stringPreferencesKey("role")
        val isVisitor = booleanPreferencesKey("is_visitor")
        val lastSync = stringPreferencesKey("last_sync_epoch")
    }

    val session: Flow<StoredSession?> = context.sessionDataStore.data
        .catch { error -> if (error is IOException) emit(androidx.datastore.preferences.core.emptyPreferences()) else throw error }
        .map { prefs ->
            val role = prefs[Keys.role] ?: return@map null
            if (role == Roles.VISITOR) {
                StoredSession(
                    userId = "visitor",
                    schoolId = "GUEST",
                    displayName = "Campus Visitor",
                    role = Roles.VISITOR,
                    isVisitor = true
                )
            } else {
                val userId = prefs[Keys.userId] ?: return@map null
                StoredSession(
                    userId = userId,
                    schoolId = prefs[Keys.schoolId].orEmpty(),
                    displayName = prefs[Keys.displayName] ?: "LANDAS User",
                    role = role,
                    isVisitor = false
                )
            }
        }

    suspend fun save(user: UserEntity) {
        context.sessionDataStore.edit { prefs ->
            prefs[Keys.userId] = user.id
            prefs[Keys.schoolId] = user.schoolId
            prefs[Keys.displayName] = user.fullName
            prefs[Keys.role] = user.role
            prefs[Keys.isVisitor] = false
        }
    }

    suspend fun saveVisitor() {
        context.sessionDataStore.edit { prefs ->
            prefs[Keys.userId] = "visitor"
            prefs[Keys.schoolId] = "GUEST"
            prefs[Keys.displayName] = "Campus Visitor"
            prefs[Keys.role] = Roles.VISITOR
            prefs[Keys.isVisitor] = true
        }
    }

    suspend fun clear() {
        context.sessionDataStore.edit { prefs ->
            prefs.remove(Keys.userId)
            prefs.remove(Keys.schoolId)
            prefs.remove(Keys.displayName)
            prefs.remove(Keys.role)
            prefs.remove(Keys.isVisitor)
        }
    }
}

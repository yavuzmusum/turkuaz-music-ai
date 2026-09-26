package ai.turkuaz.music.data.api

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "turkuaz_session")

/**
 * Not: is_admin flag'i sadece UI'da hangi ekranin acilacagina karar vermek
 * icindir (spec 3.). Gercek yetkilendirme HER admin istegi icin backend
 * tarafinda token uzerinden tekrar dogrulanir (bkz. backend/app/auth.py
 * get_current_admin). Yani bu deger tek basina bir guvenlik siniri degildir.
 */
class SessionManager(private val context: Context) {

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("access_token")
        private val IS_ADMIN_KEY = booleanPreferencesKey("is_admin")
    }

    val tokenFlow: Flow<String?> = context.dataStore.data.map { it[TOKEN_KEY] }
    val isAdminFlow: Flow<Boolean> = context.dataStore.data.map { it[IS_ADMIN_KEY] ?: false }

    suspend fun saveSession(token: String, isAdmin: Boolean) {
        context.dataStore.edit {
            it[TOKEN_KEY] = token
            it[IS_ADMIN_KEY] = isAdmin
        }
    }

    suspend fun getToken(): String? = context.dataStore.data.first()[TOKEN_KEY]

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}

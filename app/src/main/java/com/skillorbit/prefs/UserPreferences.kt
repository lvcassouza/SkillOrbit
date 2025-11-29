package com.skillorbit.prefs

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {
    private val USERNAME_KEY = stringPreferencesKey("username")
    private val ONBOARDING_DONE_KEY = stringPreferencesKey("onboarding_done")
    private val NOTIFICATIONS_KEY = stringPreferencesKey("notifications_enabled")
    private val THEME_MODE_KEY = stringPreferencesKey("theme_mode") // values: SYSTEM, LIGHT, DARK

    val userNameFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[USERNAME_KEY]
    }

    val onboardingCompletedFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        (prefs[ONBOARDING_DONE_KEY] ?: "false").toBoolean()
    }

    val notificationsEnabledFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        (prefs[NOTIFICATIONS_KEY] ?: "true").toBoolean()
    }

    val themeModeFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[THEME_MODE_KEY] ?: "SYSTEM"
    }

    suspend fun setUserName(name: String) {
        context.dataStore.edit { prefs ->
            prefs[USERNAME_KEY] = name
        }
    }

    suspend fun setOnboardingCompleted(done: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[ONBOARDING_DONE_KEY] = done.toString()
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[NOTIFICATIONS_KEY] = enabled.toString()
        }
    }

    suspend fun setThemeMode(mode: String) { // SYSTEM, LIGHT, DARK
        context.dataStore.edit { prefs ->
            prefs[THEME_MODE_KEY] = mode
        }
    }
}

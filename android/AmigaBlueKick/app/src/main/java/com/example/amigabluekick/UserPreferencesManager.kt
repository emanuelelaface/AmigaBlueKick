package com.example.amigabluekick

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

private val btn1_TEXT = stringPreferencesKey("btn1_text")
private val btn2_TEXT = stringPreferencesKey("btn2_text")
private val btn3_TEXT = stringPreferencesKey("btn3_text")
private val btn4_TEXT = stringPreferencesKey("btn4_text")

data class UserPreferences(
    var btn1Text: String,
    var btn2Text: String,
    var btn3Text: String,
    var btn4Text: String
)

class UserPreferencesManager(private val context: Context) {
    private val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .map { preferences ->
            UserPreferences(preferences[btn1_TEXT] ?: "Kickstart 1",
                preferences[btn2_TEXT] ?: "Kickstart 2",
                preferences[btn3_TEXT] ?: "Kickstart 3",
                preferences[btn4_TEXT] ?: "Kickstart 4")
        }

    fun getPreferences(): UserPreferences {
        return runBlocking { userPreferencesFlow.first() }
    }

    fun setPreferences(prefs: UserPreferences) {
        runBlocking {
            context.dataStore.edit { preferences ->
                preferences[btn1_TEXT] = prefs.btn1Text
                preferences[btn2_TEXT] = prefs.btn2Text
                preferences[btn3_TEXT] = prefs.btn3Text
                preferences[btn4_TEXT] = prefs.btn4Text
            }
        }
    }
}

package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences

class FavoritesRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getFavoriteIds(): Set<String> {
        return prefs.getStringSet(KEY_FAVORITES, emptySet()) ?: emptySet()
    }

    fun isFavorite(channelId: String): Boolean {
        return getFavoriteIds().contains(channelId)
    }

    fun toggleFavorite(channelId: String): Boolean {
        val current = getFavoriteIds().toMutableSet()
        val newState = if (current.contains(channelId)) {
            current.remove(channelId)
            false
        } else {
            current.add(channelId)
            true
        }
        prefs.edit().putStringSet(KEY_FAVORITES, current).apply()
        return newState
    }

    fun getLastPlaylistUrl(): String {
        return prefs.getString(KEY_PLAYLIST_URL, DEFAULT_PLAYLIST_URL) ?: DEFAULT_PLAYLIST_URL
    }

    fun saveLastPlaylistUrl(url: String) {
        prefs.edit().putString(KEY_PLAYLIST_URL, url).apply()
    }

    companion object {
        private const val PREFS_NAME = "iptv_preferences"
        private const val KEY_FAVORITES = "key_favorite_channel_ids"
        private const val KEY_PLAYLIST_URL = "key_last_playlist_url"
        const val DEFAULT_PLAYLIST_URL = "https://raw.githubusercontent.com/ahan443/FAST-IPTV/refs/heads/main/z.m3u"
    }
}

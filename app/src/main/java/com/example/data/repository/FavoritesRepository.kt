package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

data class SavedPlaylist(
    val name: String,
    val url: String
)

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

    fun getSavedPlaylists(): List<SavedPlaylist> {
        val jsonString = prefs.getString(KEY_SAVED_PLAYLISTS, null)
        if (jsonString.isNullOrBlank()) {
            return listOf(SavedPlaylist(DEFAULT_PLAYLIST_NAME, DEFAULT_PLAYLIST_URL))
        }
        return try {
            val array = JSONArray(jsonString)
            val list = mutableListOf<SavedPlaylist>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val name = obj.optString("name", "Playlist")
                val url = obj.optString("url", "")
                if (url.isNotBlank()) {
                    list.add(SavedPlaylist(name, url))
                }
            }
            if (list.isEmpty()) {
                listOf(SavedPlaylist(DEFAULT_PLAYLIST_NAME, DEFAULT_PLAYLIST_URL))
            } else {
                list
            }
        } catch (e: Exception) {
            listOf(SavedPlaylist(DEFAULT_PLAYLIST_NAME, DEFAULT_PLAYLIST_URL))
        }
    }

    fun savePlaylist(name: String, url: String) {
        val trimmedUrl = url.trim()
        if (trimmedUrl.isBlank()) return

        val currentList = getSavedPlaylists().toMutableList()
        val displayName = if (name.isBlank()) {
            val fileName = trimmedUrl.substringAfterLast("/").substringBefore("?")
            if (fileName.isNotBlank()) fileName else "Custom Playlist"
        } else {
            name.trim()
        }

        currentList.removeAll { it.url.equals(trimmedUrl, ignoreCase = true) }
        currentList.add(0, SavedPlaylist(displayName, trimmedUrl))

        persistPlaylists(currentList)
        saveLastPlaylistUrl(trimmedUrl)
    }

    fun deletePlaylist(url: String) {
        val trimmedUrl = url.trim()
        val currentList = getSavedPlaylists().toMutableList()
        currentList.removeAll { it.url.equals(trimmedUrl, ignoreCase = true) }
        
        persistPlaylists(currentList)
        if (getLastPlaylistUrl().equals(trimmedUrl, ignoreCase = true)) {
            val fallback = currentList.firstOrNull()?.url ?: DEFAULT_PLAYLIST_URL
            saveLastPlaylistUrl(fallback)
        }
    }

    private fun persistPlaylists(list: List<SavedPlaylist>) {
        val array = JSONArray()
        list.forEach { item ->
            val obj = JSONObject()
            obj.put("name", item.name)
            obj.put("url", item.url)
            array.put(obj)
        }
        prefs.edit().putString(KEY_SAVED_PLAYLISTS, array.toString()).apply()
    }

    companion object {
        private const val PREFS_NAME = "iptv_preferences"
        private const val KEY_FAVORITES = "key_favorite_channel_ids"
        private const val KEY_PLAYLIST_URL = "key_last_playlist_url"
        private const val KEY_SAVED_PLAYLISTS = "key_saved_playlists"
        const val DEFAULT_PLAYLIST_NAME = "FAST-IPTV (Default)"
        const val DEFAULT_PLAYLIST_URL = "https://raw.githubusercontent.com/ahan443/FAST-IPTV/refs/heads/main/z.m3u"
    }
}

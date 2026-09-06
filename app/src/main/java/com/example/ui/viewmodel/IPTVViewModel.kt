package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Channel
import com.example.data.repository.FavoritesRepository
import com.example.data.repository.PlaylistRepository
import com.example.data.repository.SavedPlaylist
import com.example.player.IPTVPlayerManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class IPTVUiState(
    val isLoading: Boolean = false,
    val currentPlaylistUrl: String = "",
    val savedPlaylists: List<SavedPlaylist> = emptyList(),
    val allChannels: List<Channel> = emptyList(),
    val filteredChannels: List<Channel> = emptyList(),
    val categories: List<String> = listOf("All", "⭐ Favorites"),
    val selectedCategory: String = "All",
    val searchQuery: String = "",
    val categoryCounts: Map<String, Int> = emptyMap(),
    val currentChannel: Channel? = null,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val playerError: String? = null,
    val isFullscreen: Boolean = false,
    val playlistLoadError: String? = null,
    val isSearchActive: Boolean = false,
    val isLayoutGrid: Boolean = true,
    val showPlaylistDialog: Boolean = false
)

class IPTVViewModel(application: Application) : AndroidViewModel(application) {

    private val favoritesRepository = FavoritesRepository(application)
    private val playlistRepository = PlaylistRepository(favoritesRepository)
    val playerManager = IPTVPlayerManager(application)

    private val _uiState = MutableStateFlow(
        IPTVUiState(currentPlaylistUrl = favoritesRepository.getLastPlaylistUrl())
    )
    val uiState: StateFlow<IPTVUiState> = _uiState.asStateFlow()

    init {
        refreshSavedPlaylists()

        // Collect player manager states
        viewModelScope.launch {
            playerManager.playerState.collect { pState ->
                _uiState.update { current ->
                    current.copy(
                        isPlaying = pState.isPlaying,
                        isBuffering = pState.isBuffering,
                        playerError = pState.errorMessage
                    )
                }
            }
        }

        // Automatically load initial playlist
        loadPlaylist(favoritesRepository.getLastPlaylistUrl())
    }

    fun refreshSavedPlaylists() {
        val saved = favoritesRepository.getSavedPlaylists()
        _uiState.update { it.copy(savedPlaylists = saved) }
    }

    fun saveAndLoadPlaylist(name: String, url: String) {
        val trimmed = url.trim()
        if (trimmed.isNotBlank()) {
            favoritesRepository.savePlaylist(name, trimmed)
            refreshSavedPlaylists()
            loadPlaylist(trimmed)
        }
    }

    fun deleteSavedPlaylist(url: String) {
        favoritesRepository.deletePlaylist(url)
        refreshSavedPlaylists()
    }

    fun loadPlaylist(url: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    playlistLoadError = null,
                    currentPlaylistUrl = url
                )
            }

            val result = playlistRepository.fetchPlaylist(url)
            if (result.isSuccess) {
                val channels = result.getOrNull().orEmpty()
                onChannelsLoaded(channels, url)
            } else {
                val errorMsg = result.exceptionOrNull()?.localizedMessage ?: "Failed to fetch playlist"
                // If the user's url fails, check if we have fallback channels so the app remains interactive
                val fallbackChannels = playlistRepository.getFallbackSampleChannels()
                val counts = computeCategoryCounts(fallbackChannels)
                val categories = computeCategories(fallbackChannels)

                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        playlistLoadError = "$errorMsg. Loaded demo streams for offline testing.",
                        allChannels = fallbackChannels,
                        categories = categories,
                        categoryCounts = counts
                    )
                }
                applyFilter()
            }
        }
    }

    private fun onChannelsLoaded(channels: List<Channel>, url: String) {
        val counts = computeCategoryCounts(channels)
        val categories = computeCategories(channels)

        favoritesRepository.savePlaylist("", url)
        refreshSavedPlaylists()

        _uiState.update { current ->
            current.copy(
                isLoading = false,
                allChannels = channels,
                categories = categories,
                categoryCounts = counts,
                playlistLoadError = null
            )
        }
        applyFilter()
    }

    private fun computeCategories(channels: List<Channel>): List<String> {
        val uniqueGroups = channels
            .map { it.displayGroup }
            .distinct()
            .sorted()
        return listOf("All", "⭐ Favorites") + uniqueGroups
    }

    private fun computeCategoryCounts(channels: List<Channel>): Map<String, Int> {
        val counts = mutableMapOf<String, Int>()
        counts["All"] = channels.size
        counts["⭐ Favorites"] = channels.count { it.isFavorite }

        for (ch in channels) {
            val grp = ch.displayGroup
            counts[grp] = (counts[grp] ?: 0) + 1
        }
        return counts
    }

    fun selectCategory(category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
        applyFilter()
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFilter()
    }

    fun setSearchActive(active: Boolean) {
        _uiState.update {
            it.copy(
                isSearchActive = active,
                searchQuery = if (!active) "" else it.searchQuery
            )
        }
        if (!active) {
            applyFilter()
        }
    }

    private fun applyFilter() {
        val state = _uiState.value
        val query = state.searchQuery.trim().lowercase()
        val category = state.selectedCategory

        val filtered = state.allChannels.filter { channel ->
            val matchesCategory = when (category) {
                "All" -> true
                "⭐ Favorites" -> channel.isFavorite
                else -> channel.displayGroup.equals(category, ignoreCase = true)
            }

            val matchesSearch = if (query.isEmpty()) {
                true
            } else {
                channel.displayName.lowercase().contains(query) ||
                    channel.displayGroup.lowercase().contains(query)
            }

            matchesCategory && matchesSearch
        }

        _uiState.update { it.copy(filteredChannels = filtered) }
    }

    fun playChannel(channel: Channel) {
        _uiState.update { it.copy(currentChannel = channel) }
        playerManager.playStream(channel.streamUrl, channel.displayName)
    }

    fun playNextChannel() {
        val channels = _uiState.value.filteredChannels.ifEmpty { _uiState.value.allChannels }
        if (channels.isEmpty()) return

        val currentIndex = channels.indexOfFirst { it.id == _uiState.value.currentChannel?.id }
        val nextIndex = if (currentIndex in 0 until channels.size - 1) currentIndex + 1 else 0
        playChannel(channels[nextIndex])
    }

    fun playPreviousChannel() {
        val channels = _uiState.value.filteredChannels.ifEmpty { _uiState.value.allChannels }
        if (channels.isEmpty()) return

        val currentIndex = channels.indexOfFirst { it.id == _uiState.value.currentChannel?.id }
        val prevIndex = if (currentIndex > 0) currentIndex - 1 else channels.size - 1
        playChannel(channels[prevIndex])
    }

    fun togglePlayPause() {
        playerManager.togglePlayPause()
    }

    fun retryPlayback() {
        val current = _uiState.value.currentChannel
        if (current != null) {
            playerManager.playStream(current.streamUrl, current.displayName)
        } else {
            playerManager.retry()
        }
    }

    fun toggleFavorite(channel: Channel) {
        val newFav = favoritesRepository.toggleFavorite(channel.id)
        val updatedAll = _uiState.value.allChannels.map {
            if (it.id == channel.id) it.copy(isFavorite = newFav) else it
        }
        val counts = computeCategoryCounts(updatedAll)

        val updatedCurrent = if (_uiState.value.currentChannel?.id == channel.id) {
            _uiState.value.currentChannel?.copy(isFavorite = newFav)
        } else {
            _uiState.value.currentChannel
        }

        _uiState.update {
            it.copy(
                allChannels = updatedAll,
                categoryCounts = counts,
                currentChannel = updatedCurrent
            )
        }
        applyFilter()
    }

    fun toggleFullscreen() {
        _uiState.update { it.copy(isFullscreen = !it.isFullscreen) }
    }

    fun setFullscreen(fullscreen: Boolean) {
        _uiState.update { it.copy(isFullscreen = fullscreen) }
    }

    fun toggleLayoutMode() {
        _uiState.update { it.copy(isLayoutGrid = !it.isLayoutGrid) }
    }

    fun setShowPlaylistDialog(show: Boolean) {
        _uiState.update { it.copy(showPlaylistDialog = show) }
    }

    override fun onCleared() {
        super.onCleared()
        playerManager.release()
    }
}

package com.example.data.repository

import com.example.data.model.Channel
import com.example.data.parser.M3UParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

class PlaylistRepository(
    private val favoritesRepository: FavoritesRepository
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    companion object {
        const val USER_AGENT = "Mozilla/5.0 (Linux; Android 14; Mobile) IPTVPlayer/1.0"

        val PRESET_PLAYLISTS = listOf(
            "FAST-IPTV (Default)" to "https://raw.githubusercontent.com/ahan443/FAST-IPTV/refs/heads/main/z.m3u",
            "IPTV-Org Global" to "https://iptv-org.github.io/iptv/index.m3u",
            "IPTV-Org News" to "https://iptv-org.github.io/iptv/categories/news.m3u",
            "IPTV-Org Movies" to "https://iptv-org.github.io/iptv/categories/movies.m3u",
            "IPTV-Org Sports" to "https://iptv-org.github.io/iptv/categories/sports.m3u",
            "IPTV-Org Music" to "https://iptv-org.github.io/iptv/categories/music.m3u"
        )
    }

    suspend fun fetchPlaylist(url: String): Result<List<Channel>> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", USER_AGENT)
                .header("Accept", "*/*")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        IOException("Failed to load playlist: HTTP ${response.code} ${response.message}")
                    )
                }

                val body = response.body ?: return@withContext Result.failure(
                    IOException("Playlist response body is empty")
                )

                val parsed = M3UParser.parse(body.byteStream())

                if (parsed.isEmpty()) {
                    return@withContext Result.failure(
                        IOException("No channels found in playlist. Please verify the URL format.")
                    )
                }

                val favoriteIds = favoritesRepository.getFavoriteIds()
                val updatedChannels = parsed.map { channel ->
                    channel.copy(isFavorite = favoriteIds.contains(channel.id))
                }

                favoritesRepository.saveLastPlaylistUrl(url)
                Result.success(updatedChannels)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Curated sample channels to ensure playback testing works immediately if external
     * network URLs are blocked or temporarily rate-limited.
     */
    fun getFallbackSampleChannels(): List<Channel> {
        val favoriteIds = favoritesRepository.getFavoriteIds()
        val list = listOf(
            Channel(
                id = "sample_bigbuckbunny",
                name = "Big Buck Bunny (HLS Live Stream)",
                group = "Demo Streams",
                logoUrl = "https://peach.blender.org/wp-content/uploads/bbb-splash.png",
                streamUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"
            ),
            Channel(
                id = "sample_sintel",
                name = "Sintel HD (HLS Stream)",
                group = "Movies",
                logoUrl = "https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=200",
                streamUrl = "https://bitdash-a.akamaihd.net/content/sintel/hls/playlist.m3u8"
            ),
            Channel(
                id = "sample_tears_of_steel",
                name = "Tears of Steel (Adaptive HLS 1080p)",
                group = "Sci-Fi",
                logoUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=200",
                streamUrl = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8"
            ),
            Channel(
                id = "sample_nasa_mp4",
                name = "NASA Space Exploration (MP4 Stream)",
                group = "Documentary",
                logoUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=200",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"
            ),
            Channel(
                id = "sample_nature_mp4",
                name = "Wild World (MP4 Stream)",
                group = "Documentary",
                logoUrl = "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?w=200",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
            )
        )
        return list.map { it.copy(isFavorite = favoriteIds.contains(it.id)) }
    }
}

package com.example.player

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PlayerState(
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val isEnded: Boolean = false,
    val errorMessage: String? = null,
    val currentStreamUrl: String? = null,
    val channelTitle: String? = null
)

@OptIn(UnstableApi::class)
class IPTVPlayerManager(private val context: Context) {

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private var exoPlayer: ExoPlayer? = null
    private var lastAttemptedUrl: String? = null
    private var lastAttemptedTitle: String? = null

    companion object {
        const val PLAYER_USER_AGENT =
            "Mozilla/5.0 (Linux; Android 14; Mobile) IPTVPlayer/1.0 ExoPlayerLib/1.5.1"
        private const val CONNECT_TIMEOUT_MS = 15000
        private const val READ_TIMEOUT_MS = 20000
    }

    init {
        initializePlayer()
    }

    private fun initializePlayer() {
        if (exoPlayer != null) return

        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent(PLAYER_USER_AGENT)
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(CONNECT_TIMEOUT_MS)
            .setReadTimeoutMs(READ_TIMEOUT_MS)

        val mediaSourceFactory = DefaultMediaSourceFactory(httpDataSourceFactory)

        val trackSelector = DefaultTrackSelector(context).apply {
            setParameters(buildUponParameters().setPreferredTextLanguage("en"))
        }

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
            .build()

        exoPlayer = ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .setTrackSelector(trackSelector)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .build()
            .apply {
                playWhenReady = true
                addListener(createPlayerListener())
            }
    }

    fun getPlayer(): ExoPlayer? = exoPlayer

    private fun createPlayerListener(): Player.Listener {
        return object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        _playerState.value = _playerState.value.copy(
                            isBuffering = true,
                            errorMessage = null
                        )
                    }
                    Player.STATE_READY -> {
                        _playerState.value = _playerState.value.copy(
                            isBuffering = false,
                            isEnded = false,
                            errorMessage = null
                        )
                    }
                    Player.STATE_ENDED -> {
                        _playerState.value = _playerState.value.copy(
                            isBuffering = false,
                            isPlaying = false,
                            isEnded = true
                        )
                    }
                    Player.STATE_IDLE -> {
                        _playerState.value = _playerState.value.copy(isBuffering = false)
                    }
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _playerState.value = _playerState.value.copy(isPlaying = isPlaying)
            }

            override fun onPlayerError(error: PlaybackException) {
                val errorDesc = parseErrorMessage(error)
                _playerState.value = _playerState.value.copy(
                    isBuffering = false,
                    isPlaying = false,
                    errorMessage = errorDesc
                )
            }
        }
    }

    private fun parseErrorMessage(error: PlaybackException): String {
        val rootCause = error.cause
        val message = error.message ?: ""
        return when {
            message.contains("403", ignoreCase = true) -> "Stream forbidden (HTTP 403). Access restricted by provider."
            message.contains("404", ignoreCase = true) -> "Stream not found (HTTP 404). Channel may be offline."
            message.contains("timeout", ignoreCase = true) || rootCause is java.net.SocketTimeoutException ->
                "Connection timed out. Server is taking too long to respond."
            rootCause is java.net.UnknownHostException -> "Network error: Unable to resolve stream host."
            message.contains("UnrecognizedInputFormatException", ignoreCase = true) ->
                "Unsupported stream format. The media codec is not supported."
            message.contains("BehindLiveWindowException", ignoreCase = true) ->
                "Live stream window slipped. Re-syncing stream..."
            else -> "Stream playback error: ${error.errorCodeName}. Tap Retry to reconnect."
        }
    }

    fun playStream(url: String, title: String) {
        lastAttemptedUrl = url
        lastAttemptedTitle = title

        initializePlayer()

        _playerState.value = _playerState.value.copy(
            currentStreamUrl = url,
            channelTitle = title,
            isBuffering = true,
            errorMessage = null
        )

        val metadata = MediaMetadata.Builder()
            .setTitle(title)
            .setDisplayTitle(title)
            .build()

        val mediaItem = MediaItem.Builder()
            .setUri(url)
            .setMediaMetadata(metadata)
            .build()

        exoPlayer?.apply {
            stop()
            clearMediaItems()
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = true
        }
    }

    fun togglePlayPause() {
        exoPlayer?.let { player ->
            if (player.isPlaying) {
                player.pause()
            } else {
                if (player.playbackState == Player.STATE_ENDED) {
                    player.seekTo(0)
                }
                player.play()
            }
        }
    }

    fun play() {
        exoPlayer?.play()
    }

    fun pause() {
        exoPlayer?.pause()
    }

    fun retry() {
        val url = lastAttemptedUrl
        val title = lastAttemptedTitle
        if (url != null && title != null) {
            playStream(url, title)
        }
    }

    fun release() {
        exoPlayer?.release()
        exoPlayer = null
    }
}

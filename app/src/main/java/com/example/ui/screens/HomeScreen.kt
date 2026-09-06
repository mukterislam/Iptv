package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CategoryTabs
import com.example.ui.components.ChannelGridCard
import com.example.ui.components.ChannelListRow
import com.example.ui.components.ExitConfirmationDialog
import com.example.ui.components.PlaylistDialog
import com.example.ui.components.VideoPlayerComposable
import com.example.ui.theme.GeometricBackground
import com.example.ui.theme.GeometricError
import com.example.ui.theme.GeometricLiveRed
import com.example.ui.theme.GeometricOnPrimary
import com.example.ui.theme.GeometricOutline
import com.example.ui.theme.GeometricPrimary
import com.example.ui.theme.GeometricSurface
import com.example.ui.theme.GeometricSurfaceVariant
import com.example.ui.theme.GeometricTextPrimary
import com.example.ui.theme.GeometricTextSecondary
import com.example.ui.viewmodel.IPTVUiState
import com.example.ui.viewmodel.IPTVViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: IPTVViewModel,
    uiState: IPTVUiState,
    modifier: Modifier = Modifier,
    onExitApp: () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var showExitDialog by remember { mutableStateOf(false) }

    // Intercept back button:
    // 1. Fullscreen -> Exit fullscreen
    // 2. Search active -> Clear/Close search
    // 3. Home / Default -> Show exit confirmation dialog
    BackHandler(enabled = true) {
        when {
            uiState.isFullscreen -> {
                viewModel.setFullscreen(false)
            }
            uiState.isSearchActive -> {
                viewModel.setSearchActive(false)
            }
            uiState.showPlaylistDialog -> {
                viewModel.setShowPlaylistDialog(false)
            }
            else -> {
                showExitDialog = true
            }
        }
    }

    if (showExitDialog) {
        ExitConfirmationDialog(
            onConfirmExit = {
                showExitDialog = false
                onExitApp()
            },
            onDismiss = {
                showExitDialog = false
            }
        )
    }

    if (uiState.showPlaylistDialog) {
        PlaylistDialog(
            currentUrl = uiState.currentPlaylistUrl,
            savedPlaylists = uiState.savedPlaylists,
            onDismiss = { viewModel.setShowPlaylistDialog(false) },
            onSelectPlaylist = { newUrl ->
                viewModel.setShowPlaylistDialog(false)
                viewModel.loadPlaylist(newUrl)
            },
            onSaveAndLoad = { name, newUrl ->
                viewModel.setShowPlaylistDialog(false)
                viewModel.saveAndLoadPlaylist(name, newUrl)
            },
            onDeletePlaylist = { urlToDelete ->
                viewModel.deleteSavedPlaylist(urlToDelete)
            }
        )
    }

    // If Fullscreen mode is active, render only the full-bleed player
    if (uiState.isFullscreen) {
        VideoPlayerComposable(
            exoPlayer = viewModel.playerManager.getPlayer(),
            channel = uiState.currentChannel,
            isPlaying = uiState.isPlaying,
            isBuffering = uiState.isBuffering,
            errorMessage = uiState.playerError,
            isFullscreen = true,
            onTogglePlayPause = { viewModel.togglePlayPause() },
            onNextChannel = { viewModel.playNextChannel() },
            onPreviousChannel = { viewModel.playPreviousChannel() },
            onToggleFullscreen = { viewModel.toggleFullscreen() },
            onRetry = { viewModel.retryPlayback() },
            modifier = Modifier.fillMaxSize()
        )
        return
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = GeometricBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            // Geometric Balance Top Header with Pill Search Bar & Action Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GeometricBackground)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Pill Search Field
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .clip(CircleShape)
                            .background(GeometricSurface)
                            .border(BorderStroke(1.dp, GeometricOutline), CircleShape)
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "Search",
                                tint = GeometricTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(modifier = Modifier.weight(1f)) {
                                if (uiState.searchQuery.isEmpty()) {
                                    Text(
                                        text = "Search channels...",
                                        color = GeometricTextSecondary,
                                        fontSize = 13.sp
                                    )
                                }
                                BasicTextField(
                                    value = uiState.searchQuery,
                                    onValueChange = {
                                        viewModel.onSearchQueryChange(it)
                                        if (it.isNotEmpty() && !uiState.isSearchActive) {
                                            viewModel.setSearchActive(true)
                                        }
                                    },
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        color = GeometricTextPrimary,
                                        fontSize = 13.sp
                                    ),
                                    cursorBrush = SolidColor(GeometricPrimary),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("search_input_field")
                                )
                            }
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { viewModel.onSearchQueryChange("") },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Clear,
                                        contentDescription = "Clear",
                                        tint = GeometricTextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Layout toggle button (Grid / List)
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(GeometricSurface)
                            .border(BorderStroke(1.dp, GeometricOutline), CircleShape)
                            .clickable { viewModel.toggleLayoutMode() }
                            .testTag("toggle_layout_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (uiState.isLayoutGrid) Icons.AutoMirrored.Filled.ViewList else Icons.Filled.GridView,
                            contentDescription = "Toggle View",
                            tint = GeometricTextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Playlist Dialog button
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(GeometricSurface)
                            .border(BorderStroke(1.dp, GeometricOutline), CircleShape)
                            .clickable { viewModel.setShowPlaylistDialog(true) }
                            .testTag("open_playlist_dialog_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.PlaylistPlay,
                            contentDescription = "Playlists",
                            tint = GeometricTextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Geometric Avatar Pill
                    Box(
                        modifier = Modifier
                            .height(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(GeometricPrimary)
                            .padding(horizontal = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "MKR",
                            color = GeometricOnPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                // Geometric divider border
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(GeometricOutline)
                )
            }
        },
        bottomBar = {
            // Geometric Balance Bottom Navigation Bar
            NavigationBar(
                containerColor = GeometricSurface,
                contentColor = GeometricTextPrimary,
                tonalElevation = 0.dp,
                modifier = Modifier
                    .border(BorderStroke(1.dp, GeometricOutline))
                    .testTag("bottom_nav_bar")
            ) {
                val isHomeSelected = uiState.selectedCategory == "All Channels" && !uiState.isSearchActive
                val isSavedSelected = uiState.selectedCategory == "⭐ Favorites"

                NavigationBarItem(
                    selected = isHomeSelected,
                    onClick = {
                        viewModel.selectCategory("All Channels")
                        viewModel.setSearchActive(false)
                    },
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                    label = { Text("Home", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = GeometricOnPrimary,
                        selectedTextColor = GeometricPrimary,
                        indicatorColor = GeometricPrimary,
                        unselectedIconColor = GeometricTextSecondary,
                        unselectedTextColor = GeometricTextSecondary
                    )
                )

                NavigationBarItem(
                    selected = !isHomeSelected && !isSavedSelected,
                    onClick = {
                        if (uiState.categories.isNotEmpty()) {
                            val nextCat = uiState.categories.firstOrNull { it != "All Channels" && it != "⭐ Favorites" }
                            if (nextCat != null) viewModel.selectCategory(nextCat)
                        }
                    },
                    icon = { Icon(Icons.Filled.Explore, contentDescription = "Browse") },
                    label = { Text("Browse", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = GeometricOnPrimary,
                        selectedTextColor = GeometricPrimary,
                        indicatorColor = GeometricPrimary,
                        unselectedIconColor = GeometricTextSecondary,
                        unselectedTextColor = GeometricTextSecondary
                    )
                )

                NavigationBarItem(
                    selected = isSavedSelected,
                    onClick = {
                        viewModel.selectCategory("⭐ Favorites")
                    },
                    icon = { Icon(Icons.Filled.Star, contentDescription = "Saved") },
                    label = { Text("Saved", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = GeometricOnPrimary,
                        selectedTextColor = GeometricPrimary,
                        indicatorColor = GeometricPrimary,
                        unselectedIconColor = GeometricTextSecondary,
                        unselectedTextColor = GeometricTextSecondary
                    )
                )

                NavigationBarItem(
                    selected = false,
                    onClick = { viewModel.setShowPlaylistDialog(true) },
                    icon = { Icon(Icons.Filled.Settings, contentDescription = "Playlists") },
                    label = { Text("Playlists", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = GeometricOnPrimary,
                        selectedTextColor = GeometricPrimary,
                        indicatorColor = GeometricPrimary,
                        unselectedIconColor = GeometricTextSecondary,
                        unselectedTextColor = GeometricTextSecondary
                    )
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Embedded 16:9 Video Player framed with Geometric outline
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(Color.Black)
            ) {
                VideoPlayerComposable(
                    exoPlayer = viewModel.playerManager.getPlayer(),
                    channel = uiState.currentChannel,
                    isPlaying = uiState.isPlaying,
                    isBuffering = uiState.isBuffering,
                    errorMessage = uiState.playerError,
                    isFullscreen = false,
                    onTogglePlayPause = { viewModel.togglePlayPause() },
                    onNextChannel = { viewModel.playNextChannel() },
                    onPreviousChannel = { viewModel.playPreviousChannel() },
                    onToggleFullscreen = { viewModel.toggleFullscreen() },
                    onRetry = { viewModel.retryPlayback() },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Divider below player
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(GeometricOutline)
            )

            // Optional Warning / Error banner when loading playlist
            AnimatedVisibility(
                visible = uiState.playlistLoadError != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                uiState.playlistLoadError?.let { errorMsg ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(GeometricError.copy(alpha = 0.15f))
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ErrorOutline,
                                contentDescription = null,
                                tint = GeometricLiveRed,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = errorMsg,
                                color = GeometricTextPrimary,
                                fontSize = 12.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Category Pill Tabs
            CategoryTabs(
                categories = uiState.categories,
                selectedCategory = uiState.selectedCategory,
                categoryCounts = uiState.categoryCounts,
                onCategorySelected = { category -> viewModel.selectCategory(category) }
            )

            // Content Area: Loading / Empty / Channel List or Grid
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                color = GeometricPrimary,
                                strokeWidth = 3.dp
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Loading Playlist Channels...",
                                color = GeometricTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Fetching streams and channel logos",
                                color = GeometricTextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                } else if (uiState.filteredChannels.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(GeometricSurface)
                                    .border(BorderStroke(1.dp, GeometricOutline), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Search,
                                    contentDescription = null,
                                    tint = GeometricTextSecondary,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (uiState.selectedCategory == "⭐ Favorites") "No Favorite Channels Yet"
                                else "No Channels Found",
                                color = GeometricTextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (uiState.selectedCategory == "⭐ Favorites")
                                    "Tap the star on any channel card to save it to your favorites."
                                else if (uiState.searchQuery.isNotEmpty())
                                    "No channels match '${uiState.searchQuery}'. Try another search query."
                                else "No channels in this category.",
                                color = GeometricTextSecondary,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    if (uiState.isLayoutGrid) {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 150.dp),
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("channels_grid"),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = uiState.filteredChannels,
                                key = { it.id }
                            ) { channel ->
                                ChannelGridCard(
                                    channel = channel,
                                    isPlaying = uiState.currentChannel?.id == channel.id,
                                    onChannelClick = { selected -> viewModel.playChannel(selected) },
                                    onToggleFavorite = { fav -> viewModel.toggleFavorite(fav) }
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("channels_list"),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(
                                items = uiState.filteredChannels,
                                key = { it.id }
                            ) { channel ->
                                ChannelListRow(
                                    channel = channel,
                                    isPlaying = uiState.currentChannel?.id == channel.id,
                                    onChannelClick = { selected -> viewModel.playChannel(selected) },
                                    onToggleFavorite = { fav -> viewModel.toggleFavorite(fav) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

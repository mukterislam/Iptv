package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.data.model.Channel
import com.example.ui.theme.GeometricBackground
import com.example.ui.theme.GeometricFavoriteGold
import com.example.ui.theme.GeometricLiveRed
import com.example.ui.theme.GeometricOnPrimary
import com.example.ui.theme.GeometricOutline
import com.example.ui.theme.GeometricPrimary
import com.example.ui.theme.GeometricSurface
import com.example.ui.theme.GeometricSurfaceVariant
import com.example.ui.theme.GeometricTextPrimary
import com.example.ui.theme.GeometricTextSecondary

@Composable
fun ChannelGridCard(
    channel: Channel,
    isPlaying: Boolean,
    onChannelClick: (Channel) -> Unit,
    onToggleFavorite: (Channel) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "live_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onChannelClick(channel) }
            .testTag("channel_card_${channel.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPlaying) GeometricSurfaceVariant else GeometricSurface
        ),
        border = BorderStroke(
            width = if (isPlaying) 2.dp else 1.dp,
            color = if (isPlaying) GeometricPrimary else GeometricOutline
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Well for Logo (Aspect Square Geometric Box)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.2f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(GeometricBackground)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                if (!channel.logoUrl.isNullOrBlank()) {
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(channel.logoUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Logo of ${channel.displayName}",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp),
                        loading = {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(18.dp),
                                    color = GeometricPrimary
                                )
                            }
                        },
                        error = {
                            DefaultLogoPlaceholder(channel.displayName)
                        }
                    )
                } else {
                    DefaultLogoPlaceholder(channel.displayName)
                }

                // Favorite button pinned to top-end of the logo well
                IconButton(
                    onClick = { onToggleFavorite(channel) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(GeometricSurface.copy(alpha = 0.85f))
                        .testTag("favorite_btn_${channel.id}")
                ) {
                    Icon(
                        imageVector = if (channel.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = if (channel.isFavorite) "Remove favorite" else "Add favorite",
                        tint = if (channel.isFavorite) GeometricFavoriteGold else GeometricTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Channel Title
            Text(
                text = channel.displayName,
                color = GeometricTextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 17.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Bottom metadata row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Tag
                Text(
                    text = channel.displayGroup,
                    color = GeometricTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Spacer(modifier = Modifier.width(6.dp))

                // Live Now Pulse indicator
                if (isPlaying) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(GeometricPrimary.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "PLAYING",
                            color = GeometricPrimary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(GeometricLiveRed.copy(alpha = pulseAlpha))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "LIVE",
                            color = GeometricLiveRed,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChannelListRow(
    channel: Channel,
    isPlaying: Boolean,
    onChannelClick: (Channel) -> Unit,
    onToggleFavorite: (Channel) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onChannelClick(channel) }
            .testTag("channel_row_${channel.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPlaying) GeometricSurfaceVariant else GeometricSurface
        ),
        border = BorderStroke(
            width = if (isPlaying) 1.5.dp else 1.dp,
            color = if (isPlaying) GeometricPrimary else GeometricOutline
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logo Well
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(GeometricBackground)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                if (!channel.logoUrl.isNullOrBlank()) {
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(channel.logoUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Logo of ${channel.displayName}",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize(),
                        error = { DefaultLogoPlaceholder(channel.displayName) }
                    )
                } else {
                    DefaultLogoPlaceholder(channel.displayName)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Channel info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = channel.displayName,
                    color = if (isPlaying) GeometricPrimary else GeometricTextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = channel.displayGroup,
                        color = GeometricTextSecondary,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(GeometricLiveRed)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "LIVE NOW",
                        color = GeometricLiveRed,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // Favorite button
            IconButton(
                onClick = { onToggleFavorite(channel) },
                modifier = Modifier
                    .size(40.dp)
                    .testTag("favorite_row_btn_${channel.id}")
            ) {
                Icon(
                    imageVector = if (channel.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = if (channel.isFavorite) "Remove from favorites" else "Add to favorites",
                    tint = if (channel.isFavorite) GeometricFavoriteGold else GeometricTextSecondary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun DefaultLogoPlaceholder(channelName: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Tv,
            contentDescription = null,
            tint = GeometricPrimary.copy(alpha = 0.8f),
            modifier = Modifier.size(24.dp)
        )
    }
}


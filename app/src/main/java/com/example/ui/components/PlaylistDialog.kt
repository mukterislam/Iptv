package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.PlaylistRepository
import com.example.ui.theme.GeometricBackground
import com.example.ui.theme.GeometricOnPrimary
import com.example.ui.theme.GeometricOutline
import com.example.ui.theme.GeometricPrimary
import com.example.ui.theme.GeometricSurface
import com.example.ui.theme.GeometricTextPrimary
import com.example.ui.theme.GeometricTextSecondary

@Composable
fun PlaylistDialog(
    currentUrl: String,
    onDismiss: () -> Unit,
    onSelectPlaylist: (String) -> Unit
) {
    var customUrl by remember { mutableStateOf(currentUrl) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(GeometricPrimary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlaylistPlay,
                        contentDescription = null,
                        tint = GeometricPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Playlist Source",
                    color = GeometricTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Enter an M3U / M3U8 URL or choose a preset:",
                    color = GeometricTextSecondary,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = customUrl,
                    onValueChange = { customUrl = it },
                    label = { Text("Playlist URL", color = GeometricTextSecondary) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Link,
                            contentDescription = null,
                            tint = GeometricPrimary
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GeometricPrimary,
                        unfocusedBorderColor = GeometricOutline,
                        focusedTextColor = GeometricTextPrimary,
                        unfocusedTextColor = GeometricTextPrimary,
                        cursorColor = GeometricPrimary,
                        focusedContainerColor = GeometricBackground,
                        unfocusedContainerColor = GeometricBackground
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("playlist_url_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Verified Presets",
                    color = GeometricTextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(PlaylistRepository.PRESET_PLAYLISTS) { (title, url) ->
                        val isSelected = currentUrl == url
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) GeometricPrimary.copy(alpha = 0.15f) else GeometricBackground)
                                .border(
                                    border = BorderStroke(
                                        width = 1.dp,
                                        color = if (isSelected) GeometricPrimary else GeometricOutline
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    customUrl = url
                                    onSelectPlaylist(url)
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = title,
                                    color = if (isSelected) GeometricPrimary else GeometricTextPrimary,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = url,
                                    color = GeometricTextSecondary,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = "Selected",
                                    tint = GeometricPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (customUrl.isNotBlank()) {
                        onSelectPlaylist(customUrl.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = GeometricPrimary,
                    contentColor = GeometricOnPrimary
                ),
                shape = CircleShape,
                modifier = Modifier.testTag("load_playlist_btn")
            ) {
                Text("Load Playlist", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = GeometricTextSecondary)
            }
        },
        containerColor = GeometricSurface,
        shape = RoundedCornerShape(20.dp)
    )
}


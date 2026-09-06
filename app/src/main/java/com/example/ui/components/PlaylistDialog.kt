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
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.data.repository.SavedPlaylist
import com.example.ui.theme.GeometricBackground
import com.example.ui.theme.GeometricError
import com.example.ui.theme.GeometricOnPrimary
import com.example.ui.theme.GeometricOutline
import com.example.ui.theme.GeometricPrimary
import com.example.ui.theme.GeometricSurface
import com.example.ui.theme.GeometricTextPrimary
import com.example.ui.theme.GeometricTextSecondary

@Composable
fun PlaylistDialog(
    currentUrl: String,
    savedPlaylists: List<SavedPlaylist>,
    onDismiss: () -> Unit,
    onSelectPlaylist: (String) -> Unit,
    onSaveAndLoad: (name: String, url: String) -> Unit,
    onDeletePlaylist: (url: String) -> Unit
) {
    var customUrl by remember { mutableStateOf("") }
    var customName by remember { mutableStateOf("") }

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
                        imageVector = Icons.AutoMirrored.Filled.PlaylistPlay,
                        contentDescription = null,
                        tint = GeometricPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "প্লেলিস্ট ম্যানেজার",
                        color = GeometricTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                    Text(
                        text = "Playlist Manager",
                        color = GeometricTextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "নতুন M3U / M3U8 URL দিন:",
                    color = GeometricTextSecondary,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Optional Name Input
                OutlinedTextField(
                    value = customName,
                    onValueChange = { customName = it },
                    label = { Text("প্লেলিস্টের নাম (ঐচ্ছিক)", color = GeometricTextSecondary, fontSize = 12.sp) },
                    placeholder = { Text("যেমন: My Bangla TV", color = GeometricTextSecondary.copy(alpha = 0.6f), fontSize = 12.sp) },
                    shape = RoundedCornerShape(10.dp),
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
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // URL Input
                OutlinedTextField(
                    value = customUrl,
                    onValueChange = { customUrl = it },
                    label = { Text("প্লেলিস্ট লিঙ্ক / URL (M3U / M3U8)", color = GeometricTextSecondary, fontSize = 12.sp) },
                    placeholder = { Text("https://example.com/playlist.m3u", color = GeometricTextSecondary.copy(alpha = 0.6f), fontSize = 11.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Link,
                            contentDescription = null,
                            tint = GeometricPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    shape = RoundedCornerShape(10.dp),
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

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        if (customUrl.isNotBlank()) {
                            onSaveAndLoad(customName, customUrl.trim())
                        }
                    },
                    enabled = customUrl.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GeometricPrimary,
                        contentColor = GeometricOnPrimary,
                        disabledContainerColor = GeometricPrimary.copy(alpha = 0.3f),
                        disabledContentColor = GeometricOnPrimary.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .testTag("save_and_load_btn")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("সেভ ও লোড করুন", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Saved Playlists Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "সংরক্ষিত প্লেলিস্ট (${savedPlaylists.size})",
                        color = GeometricTextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (savedPlaylists.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(GeometricBackground)
                            .border(BorderStroke(1.dp, GeometricOutline), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "কোনো প্লেলিস্ট সংরক্ষিত নেই",
                            color = GeometricTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = savedPlaylists,
                            key = { it.url }
                        ) { item ->
                            val isSelected = currentUrl.equals(item.url, ignoreCase = true)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) GeometricPrimary.copy(alpha = 0.15f)
                                        else GeometricBackground
                                    )
                                    .border(
                                        border = BorderStroke(
                                            width = 1.dp,
                                            color = if (isSelected) GeometricPrimary else GeometricOutline
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        onSelectPlaylist(item.url)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) GeometricPrimary else GeometricSurface),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isSelected) Icons.Filled.CheckCircle else Icons.Filled.Tv,
                                        contentDescription = null,
                                        tint = if (isSelected) GeometricOnPrimary else GeometricPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = item.name,
                                            color = if (isSelected) GeometricPrimary else GeometricTextPrimary,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                        if (isSelected) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(GeometricPrimary)
                                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "সক্রিয়",
                                                    color = GeometricOnPrimary,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = item.url,
                                        color = GeometricTextSecondary,
                                        fontSize = 10.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                // Delete button for saved playlist
                                IconButton(
                                    onClick = { onDeletePlaylist(item.url) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = "Delete Playlist",
                                        tint = GeometricError.copy(alpha = 0.7f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বন্ধ করুন (Close)", color = GeometricTextSecondary)
            }
        },
        containerColor = GeometricSurface,
        shape = RoundedCornerShape(20.dp)
    )
}

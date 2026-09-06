package com.example.data.parser

import com.example.data.model.Channel
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.security.MessageDigest
import java.util.regex.Pattern

object M3UParser {

    private val TVG_ID_PATTERN = Pattern.compile("""tvg-id="([^"]*)"""", Pattern.CASE_INSENSITIVE)
    private val TVG_NAME_PATTERN = Pattern.compile("""tvg-name="([^"]*)"""", Pattern.CASE_INSENSITIVE)
    private val TVG_LOGO_PATTERN = Pattern.compile("""tvg-logo="([^"]*)"""", Pattern.CASE_INSENSITIVE)
    private val GROUP_TITLE_PATTERN = Pattern.compile("""group-title="([^"]*)"""", Pattern.CASE_INSENSITIVE)

    /**
     * Parses M3U/M3U8 content from an InputStream line by line asynchronously.
     */
    fun parse(inputStream: InputStream): List<Channel> {
        val channels = mutableListOf<Channel>()
        val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))

        var currentTvgId: String? = null
        var currentTvgName: String? = null
        var currentLogoUrl: String? = null
        var currentGroup: String? = null
        var currentChannelName: String? = null
        var inExtInf = false

        reader.useLines { lines ->
            for (rawLine in lines) {
                val line = rawLine.trim()
                if (line.isEmpty()) continue

                if (line.startsWith("#EXTINF", ignoreCase = true)) {
                    inExtInf = true
                    currentTvgId = extractAttribute(line, TVG_ID_PATTERN)
                    currentTvgName = extractAttribute(line, TVG_NAME_PATTERN)
                    currentLogoUrl = extractAttribute(line, TVG_LOGO_PATTERN)?.takeIf { it.isNotBlank() }
                    currentGroup = extractAttribute(line, GROUP_TITLE_PATTERN)?.takeIf { it.isNotBlank() }

                    // Channel title is typically after the last comma
                    val commaIndex = line.lastIndexOf(',')
                    currentChannelName = if (commaIndex != -1 && commaIndex < line.length - 1) {
                        line.substring(commaIndex + 1).trim()
                    } else {
                        currentTvgName ?: "Unknown Channel"
                    }
                } else if (line.startsWith("#EXTGRP:", ignoreCase = true)) {
                    // Optional secondary category tag
                    val grp = line.substring(8).trim()
                    if (grp.isNotEmpty() && currentGroup.isNullOrBlank()) {
                        currentGroup = grp
                    }
                } else if (!line.startsWith("#")) {
                    // This is a stream URL
                    if (inExtInf || line.startsWith("http://", ignoreCase = true) || line.startsWith("https://", ignoreCase = true)) {
                        val streamUrl = line.trim()
                        val finalName = currentChannelName?.ifBlank { null }
                            ?: currentTvgName?.ifBlank { null }
                            ?: "Channel ${channels.size + 1}"
                        val finalGroup = currentGroup?.ifBlank { null } ?: "General"
                        val id = generateChannelId(streamUrl, finalName, channels.size)

                        channels.add(
                            Channel(
                                id = id,
                                name = finalName,
                                group = finalGroup,
                                logoUrl = currentLogoUrl,
                                streamUrl = streamUrl,
                                tvgId = currentTvgId
                            )
                        )

                        // Reset state for next channel
                        currentTvgId = null
                        currentTvgName = null
                        currentLogoUrl = null
                        currentGroup = null
                        currentChannelName = null
                        inExtInf = false
                    }
                }
            }
        }

        return channels
    }

    private fun extractAttribute(line: String, pattern: Pattern): String? {
        val matcher = pattern.matcher(line)
        return if (matcher.find()) {
            matcher.group(1)?.trim()
        } else {
            null
        }
    }

    private fun generateChannelId(url: String, name: String, index: Int): String {
        return try {
            val md = MessageDigest.getInstance("MD5")
            val bytes = md.digest("$name|$url".toByteArray())
            bytes.joinToString("") { "%02x".format(it) }.take(12)
        } catch (_: Exception) {
            "ch_${index}_${url.hashCode()}"
        }
    }
}

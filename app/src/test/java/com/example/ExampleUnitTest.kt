package com.example

import com.example.data.parser.M3UParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testM3UParser_extractsAttributesCorrectly() {
    val sampleM3u = """
      #EXTM3U
      #EXTINF:-1 tvg-id="cnn.us" tvg-name="CNN Live" tvg-logo="https://example.com/cnn.png" group-title="News",CNN International
      https://example.com/cnn/live.m3u8
      
      #EXTINF:-1 tvg-id="espn.us" tvg-logo="https://example.com/espn.png" group-title="Sports",ESPN HD
      https://example.com/espn/stream.m3u8

      #EXTINF:-1,Sample Movie Channel
      #EXTGRP:Movies
      https://example.com/movie/stream.mp4
    """.trimIndent()

    val channels = M3UParser.parse(ByteArrayInputStream(sampleM3u.toByteArray()))

    assertEquals(3, channels.size)

    val cnn = channels[0]
    assertEquals("CNN International", cnn.name)
    assertEquals("News", cnn.group)
    assertEquals("https://example.com/cnn.png", cnn.logoUrl)
    assertEquals("https://example.com/cnn/live.m3u8", cnn.streamUrl)
    assertEquals("cnn.us", cnn.tvgId)

    val espn = channels[1]
    assertEquals("ESPN HD", espn.name)
    assertEquals("Sports", espn.group)
    assertEquals("https://example.com/espn.png", espn.logoUrl)

    val movie = channels[2]
    assertEquals("Sample Movie Channel", movie.name)
    assertEquals("Movies", movie.group)
    assertEquals("https://example.com/movie/stream.mp4", movie.streamUrl)
  }
}

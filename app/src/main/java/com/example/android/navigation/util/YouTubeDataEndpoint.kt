package com.example.android.navigation.util


import android.util.Log

import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.extensions.android.json.AndroidJsonFactory
import com.google.api.services.youtube.YouTube
import com.google.api.services.youtube.model.ChannelListResponse
import com.google.api.services.youtube.model.Video
import com.google.api.services.youtube.model.VideoListResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import java.io.IOException
object YouTubeDataEndpoint {

    @Throws(RuntimeException::class)
    suspend fun getVideoInfoFromYouTubeDataAPIs(videoId: String): VideoInfo? {
        return withContext(Dispatchers.IO) {
            var v : VideoInfo? = null
            try {
                val youTubeDataAPIEndpoint = buildYouTubeEndpoint()

                val query = buildVideosListQuery(youTubeDataAPIEndpoint, videoId)
                val videoListResponse = query.execute()

                if (videoListResponse.items.size != 1)
                    throw RuntimeException("There should be exactly one video with the specified id")

                val video = videoListResponse.items[0]

                val videoTitle = video.snippet.title
                val url = video.snippet.thumbnails.medium.url

                val channel = buildChannelsListQuery(youTubeDataAPIEndpoint, video.snippet.channelId).execute()
                val channelTitle = channel.items[0].snippet.title

                v = VideoInfo(videoTitle, channelTitle, url)

            } catch (e: Exception) {
                Log.w("Tag.YouTubeDataEndpoint", "Error retrieving info video ${e}")
            }
            v
        }
    }

    @Throws(IOException::class)
    private fun buildVideosListQuery(youTubeDataAPIEndpoint: YouTube, videoId: String): YouTube.Videos.List {
        return youTubeDataAPIEndpoint
                .videos()
                .list("snippet")
                .setFields("items(snippet(title,channelId,thumbnails(medium(url))))")
                .setId(videoId)
                .setKey(Globals().YT_API_KEY)
    }

    @Throws(IOException::class)
    private fun buildChannelsListQuery(youTubeDataAPIEndpoint: YouTube, channelId: String): YouTube.Channels.List {
        return youTubeDataAPIEndpoint
                .channels()
                .list("snippet")
                .setFields("items(snippet(title))")
                .setId(channelId)
                .setKey(Globals().YT_API_KEY)
    }

    private fun buildYouTubeEndpoint(): YouTube {
        return YouTube.Builder(AndroidHttp.newCompatibleTransport(), AndroidJsonFactory(), null)
                .setApplicationName(Globals().APP_NAME)
                .build()
    }
}

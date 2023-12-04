/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.messages.impl.timeline.factories.event

import com.google.common.truth.Truth.assertThat
import io.element.android.features.location.api.Location
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemAudioContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemEmoteContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemFileContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemImageContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemLocationContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemNoticeContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemTextContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemVideoContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemVoiceContent
import io.element.android.libraries.androidutils.filesize.FakeFileSizeFormatter
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.featureflag.test.FakeFeatureFlagService
import io.element.android.libraries.matrix.api.media.AudioDetails
import io.element.android.libraries.matrix.api.media.AudioInfo
import io.element.android.libraries.matrix.api.media.FileInfo
import io.element.android.libraries.matrix.api.media.ImageInfo
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrix.api.media.ThumbnailInfo
import io.element.android.libraries.matrix.api.media.VideoInfo
import io.element.android.libraries.matrix.api.timeline.item.event.AudioMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.EmoteMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.FileMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.ImageMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.InReplyTo
import io.element.android.libraries.matrix.api.timeline.item.event.LocationMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.MessageContent
import io.element.android.libraries.matrix.api.timeline.item.event.MessageType
import io.element.android.libraries.matrix.api.timeline.item.event.NoticeMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.OtherMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.TextMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.VideoMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.VoiceMessageType
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.ui.components.A_BLUR_HASH
import io.element.android.libraries.mediaviewer.api.util.FileExtensionExtractorWithoutValidation
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class TimelineItemContentMessageFactoryTest {

    @Test
    fun `test create OtherMessageType`() = runTest {
        val sut = createTimelineItemContentMessageFactory()
        val result = sut.create(
            content = createMessageContent(type = OtherMessageType(msgType = "a_type", body = "body")),
            senderDisplayName = "Bob",
            eventId = AN_EVENT_ID,
        )
        val expected = TimelineItemTextContent(
            body = "body",
            htmlDocument = null,
            plainText = "body",
            isEdited = false,
        )
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `test create LocationMessageType not null`() = runTest {
        val sut = createTimelineItemContentMessageFactory()
        val result = sut.create(
            content = createMessageContent(type = LocationMessageType("body", "geo:1,2", "description")),
            senderDisplayName = "Bob",
            eventId = AN_EVENT_ID,
        )
        val expected = TimelineItemLocationContent(
            body = "body",
            location = Location(lat = 1.0, lon = 2.0, accuracy = 0.0F),
            description = "description",
        )
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `test create LocationMessageType null`() = runTest {
        val sut = createTimelineItemContentMessageFactory()
        val result = sut.create(
            content = createMessageContent(type = LocationMessageType("body", "", null)),
            senderDisplayName = "Bob",
            eventId = AN_EVENT_ID,
        )
        val expected = TimelineItemTextContent(
            body = "body",
            htmlDocument = null,
            plainText = "body",
            isEdited = false,
        )
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `test create TextMessageType`() = runTest {
        val sut = createTimelineItemContentMessageFactory()
        val result = sut.create(
            content = createMessageContent(type = TextMessageType("body", null)),
            senderDisplayName = "Bob",
            eventId = AN_EVENT_ID,
        )
        val expected = TimelineItemTextContent(
            body = "body",
            htmlDocument = null,
            plainText = "body",
            isEdited = false,
        )
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `test create VideoMessageType`() = runTest {
        val sut = createTimelineItemContentMessageFactory()
        val result = sut.create(
            content = createMessageContent(type = VideoMessageType("body", MediaSource("url"), null)),
            senderDisplayName = "Bob",
            eventId = AN_EVENT_ID,
        )
        val expected = TimelineItemVideoContent(
            body = "body",
            duration = Duration.ZERO,
            videoSource = MediaSource(url = "url", json = null),
            thumbnailSource = null,
            aspectRatio = null,
            blurHash = null,
            height = null,
            width = null,
            mimeType = MimeTypes.OctetStream,
            formattedFileSize = "0 Bytes",
            fileExtension = "",
        )
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `test create VideoMessageType with info`() = runTest {
        val sut = createTimelineItemContentMessageFactory()
        val result = sut.create(
            content = createMessageContent(
                type = VideoMessageType(
                    body = "body.mp4",
                    source = MediaSource("url"),
                    info = VideoInfo(
                        duration = 1.minutes,
                        height = 100,
                        width = 300,
                        mimetype = MimeTypes.Mp4,
                        size = 555,
                        thumbnailInfo = ThumbnailInfo(
                            height = 10L,
                            width = 5L,
                            mimetype = MimeTypes.Jpeg,
                            size = 111L,
                        ),
                        thumbnailSource = MediaSource("url_thumbnail"),
                        blurhash = A_BLUR_HASH,
                    ),
                )
            ),
            senderDisplayName = "Bob",
            eventId = AN_EVENT_ID,
        )
        val expected = TimelineItemVideoContent(
            body = "body.mp4",
            duration = 1.minutes,
            videoSource = MediaSource(url = "url", json = null),
            thumbnailSource = MediaSource("url_thumbnail"),
            aspectRatio = 3f,
            blurHash = A_BLUR_HASH,
            height = 100,
            width = 300,
            mimeType = MimeTypes.Mp4,
            formattedFileSize = "555 Bytes",
            fileExtension = "mp4",
        )
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `test create AudioMessageType`() = runTest {
        val sut = createTimelineItemContentMessageFactory()
        val result = sut.create(
            content = createMessageContent(type = AudioMessageType("body", MediaSource("url"), null)),
            senderDisplayName = "Bob",
            eventId = AN_EVENT_ID,
        )
        val expected = TimelineItemAudioContent(
            body = "body",
            duration = Duration.ZERO,
            mediaSource = MediaSource(url = "url", json = null),
            mimeType = MimeTypes.OctetStream,
            formattedFileSize = "0 Bytes",
            fileExtension = "",
        )
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `test create AudioMessageType with info`() = runTest {
        val sut = createTimelineItemContentMessageFactory()
        val result = sut.create(
            content = createMessageContent(
                type = AudioMessageType(
                    body = "body.mp3",
                    source = MediaSource("url"),
                    info = AudioInfo(
                        duration = 1.minutes,
                        size = 123L,
                        mimetype = MimeTypes.Mp3,
                    )
                )
            ),
            senderDisplayName = "Bob",
            eventId = AN_EVENT_ID,
        )
        val expected = TimelineItemAudioContent(
            body = "body.mp3",
            duration = 1.minutes,
            mediaSource = MediaSource(url = "url", json = null),
            mimeType = MimeTypes.Mp3,
            formattedFileSize = "123 Bytes",
            fileExtension = "mp3",
        )
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `test create VoiceMessageType`() = runTest {
        val sut = createTimelineItemContentMessageFactory()
        val result = sut.create(
            content = createMessageContent(type = VoiceMessageType("body", MediaSource("url"), null, null)),
            senderDisplayName = "Bob",
            eventId = AN_EVENT_ID,
        )
        val expected = TimelineItemVoiceContent(
            eventId = AN_EVENT_ID,
            body = "body",
            duration = Duration.ZERO,
            mediaSource = MediaSource(url = "url", json = null),
            mimeType = MimeTypes.OctetStream,
            waveform = emptyList<Float>().toImmutableList()
        )
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `test create VoiceMessageType with info`() = runTest {
        val sut = createTimelineItemContentMessageFactory()
        val result = sut.create(
            content = createMessageContent(
                type = VoiceMessageType(
                    body = "body.ogg",
                    source = MediaSource("url"),
                    info = AudioInfo(
                        duration = 1.minutes,
                        size = 123L,
                        mimetype = MimeTypes.Ogg,
                    ),
                    details = AudioDetails(
                        duration = 1.minutes,
                        waveform = persistentListOf(1f, 2f),
                    ),
                )
            ),
            senderDisplayName = "Bob",
            eventId = AN_EVENT_ID,
        )
        val expected = TimelineItemVoiceContent(
            eventId = AN_EVENT_ID,
            body = "body.ogg",
            duration = 1.minutes,
            mediaSource = MediaSource(url = "url", json = null),
            mimeType = MimeTypes.Ogg,
            waveform = persistentListOf(1f, 2f)
        )
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `test create VoiceMessageType feature disabled`() = runTest {
        val sut = createTimelineItemContentMessageFactory(
            featureFlagService = FakeFeatureFlagService(
                initialState = mapOf(
                    FeatureFlags.VoiceMessages.key to false,
                )
            )
        )
        val result = sut.create(
            content = createMessageContent(type = VoiceMessageType("body", MediaSource("url"), null, null)),
            senderDisplayName = "Bob",
            eventId = AN_EVENT_ID,
        )
        val expected = TimelineItemAudioContent(
            body = "body",
            duration = Duration.ZERO,
            mediaSource = MediaSource(url = "url", json = null),
            mimeType = MimeTypes.OctetStream,
            formattedFileSize = "0 Bytes",
            fileExtension = ""
        )
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `test create ImageMessageType`() = runTest {
        val sut = createTimelineItemContentMessageFactory()
        val result = sut.create(
            content = createMessageContent(type = ImageMessageType("body", MediaSource("url"), null)),
            senderDisplayName = "Bob",
            eventId = AN_EVENT_ID,
        )
        val expected = TimelineItemImageContent(
            body = "body",
            mediaSource = MediaSource(url = "url", json = null),
            thumbnailSource = null,
            formattedFileSize = "0 Bytes",
            fileExtension = "",
            mimeType = MimeTypes.OctetStream,
            blurhash = null,
            width = null,
            height = null,
            aspectRatio = null
        )
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `test create ImageMessageType with info`() = runTest {
        val sut = createTimelineItemContentMessageFactory()
        val result = sut.create(
            content = createMessageContent(
                type = ImageMessageType(
                    body = "body.jpg",
                    source = MediaSource("url"),
                    info = ImageInfo(
                        height = 10L,
                        width = 5L,
                        mimetype = MimeTypes.Jpeg,
                        size = 888L,
                        thumbnailInfo = ThumbnailInfo(
                            height = 10L,
                            width = 5L,
                            mimetype = MimeTypes.Jpeg,
                            size = 111L,
                        ),
                        thumbnailSource = MediaSource("url_thumbnail"),
                        blurhash = A_BLUR_HASH,
                    )
                )
            ),
            senderDisplayName = "Bob",
            eventId = AN_EVENT_ID,
        )
        val expected = TimelineItemImageContent(
            body = "body.jpg",
            mediaSource = MediaSource(url = "url", json = null),
            thumbnailSource = MediaSource("url_thumbnail"),
            formattedFileSize = "888 Bytes",
            fileExtension = "jpg",
            mimeType = MimeTypes.Jpeg,
            blurhash = A_BLUR_HASH,
            width = 5,
            height = 10,
            aspectRatio = 0.5f,
        )
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `test create FileMessageType`() = runTest {
        val sut = createTimelineItemContentMessageFactory()
        val result = sut.create(
            content = createMessageContent(type = FileMessageType("body", MediaSource("url"), null)),
            senderDisplayName = "Bob",
            eventId = AN_EVENT_ID,
        )
        val expected = TimelineItemFileContent(
            body = "body",
            fileSource = MediaSource(url = "url", json = null),
            thumbnailSource = null,
            formattedFileSize = "0 Bytes",
            fileExtension = "",
            mimeType = MimeTypes.OctetStream
        )
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `test create FileMessageType with info`() = runTest {
        val sut = createTimelineItemContentMessageFactory()
        val result = sut.create(
            content = createMessageContent(
                type = FileMessageType(
                    body = "body.pdf",
                    source = MediaSource("url"),
                    info = FileInfo(
                        mimetype = MimeTypes.Pdf,
                        size = 123L,
                        thumbnailInfo = ThumbnailInfo(
                            height = 10L,
                            width = 5L,
                            mimetype = MimeTypes.Jpeg,
                            size = 111L,
                        ),
                        thumbnailSource = MediaSource("url_thumbnail"),
                    )
                )
            ),
            senderDisplayName = "Bob",
            eventId = AN_EVENT_ID,
        )
        val expected = TimelineItemFileContent(
            body = "body.pdf",
            fileSource = MediaSource(url = "url", json = null),
            thumbnailSource = MediaSource("url_thumbnail"),
            formattedFileSize = "123 Bytes",
            fileExtension = "pdf",
            mimeType = MimeTypes.Pdf
        )
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `test create NoticeMessageType`() = runTest {
        val sut = createTimelineItemContentMessageFactory()
        val result = sut.create(
            content = createMessageContent(type = NoticeMessageType("body", null)),
            senderDisplayName = "Bob",
            eventId = AN_EVENT_ID,
        )
        val expected = TimelineItemNoticeContent(
            body = "body",
            htmlDocument = null,
            plainText = "body",
            isEdited = false,
        )
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `test create EmoteMessageType`() = runTest {
        val sut = createTimelineItemContentMessageFactory()
        val result = sut.create(
            content = createMessageContent(type = EmoteMessageType("body", null)),
            senderDisplayName = "Bob",
            eventId = AN_EVENT_ID,
        )
        val expected = TimelineItemEmoteContent(
            body = "* Bob body",
            htmlDocument = null,
            plainText = "* Bob body",
            isEdited = false,
        )
        assertThat(result).isEqualTo(expected)
    }

    private fun createMessageContent(
        body: String = "Body",
        inReplyTo: InReplyTo? = null,
        isEdited: Boolean = false,
        isThreaded: Boolean = false,
        type: MessageType,
    ): MessageContent {
        return MessageContent(
            body = body,
            inReplyTo = inReplyTo,
            isEdited = isEdited,
            isThreaded = isThreaded,
            type = type,
        )
    }

    private fun createTimelineItemContentMessageFactory(
        featureFlagService: FeatureFlagService = FakeFeatureFlagService()
    ) = TimelineItemContentMessageFactory(
        fileSizeFormatter = FakeFileSizeFormatter(),
        fileExtensionExtractor = FileExtensionExtractorWithoutValidation(),
        featureFlagService = featureFlagService,
    )
}

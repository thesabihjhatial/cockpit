package com.thesabihjhatial.cockpit.media

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.thesabihjhatial.cockpit.R
import com.thesabihjhatial.cockpit.ui.CockpitIcons
import com.thesabihjhatial.cockpit.ui.LocalCockpitColors
import kotlinx.coroutines.delay

private val ArtShape = RoundedCornerShape(20.dp)
private val ColumnGap = 16.dp

private const val PositionTickMs = 500L

@Composable
private fun AccessPrompt(modifier: Modifier = Modifier) {
    val colors = LocalCockpitColors.current
    val context = LocalContext.current

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.media_allow_message),
            color = colors.muted,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
        )
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(colors.surface)
                .clickable {
                    runCatching { context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) }
                }
                .padding(horizontal = 28.dp, vertical = 16.dp),
        ) {
            Text(
                text = stringResource(R.string.media_allow),
                color = colors.text,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun Art(nowPlaying: NowPlaying, modifier: Modifier = Modifier) {
    val colors = LocalCockpitColors.current
    val art = nowPlaying.art

    Box(
        modifier = modifier
            .shadow(16.dp, ArtShape, ambientColor = Color.Black.copy(alpha = 0.14f), spotColor = Color.Black.copy(alpha = 0.14f))
            .clip(ArtShape)
            .background(if (art == null) colors.accentSoft else colors.raised),
        contentAlignment = Alignment.Center,
    ) {
        if (art != null) {
            Image(
                bitmap = art,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Icon(
                imageVector = CockpitIcons.MusicNote,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = colors.accentInk,
            )
        }
    }
}

@Composable
private fun ControlButton(
    icon: ImageVector,
    description: String,
    buttonSize: Dp,
    iconSize: Dp,
    tint: Color,
    onClick: () -> Unit,
    background: Color = Color.Transparent,
) {
    Box(
        modifier = Modifier.size(buttonSize).clip(CircleShape).background(background).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(imageVector = icon, contentDescription = description, modifier = Modifier.size(iconSize), tint = tint)
    }
}

@Composable
private fun Controls(nowPlaying: NowPlaying, modifier: Modifier = Modifier) {
    val colors = LocalCockpitColors.current
    val controls = nowPlaying.controller.transportControls

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ControlButton(
            icon = CockpitIcons.Previous,
            description = stringResource(R.string.media_previous),
            buttonSize = 72.dp,
            iconSize = 34.dp,
            tint = colors.text,
            onClick = controls::skipToPrevious,
        )
        ControlButton(
            icon = if (nowPlaying.playing) CockpitIcons.Pause else CockpitIcons.Play,
            description = stringResource(if (nowPlaying.playing) R.string.media_pause else R.string.media_play),
            buttonSize = 104.dp,
            iconSize = 40.dp,
            tint = Color.White,
            onClick = nowPlaying::togglePlayback,
            background = colors.accent,
        )
        ControlButton(
            icon = CockpitIcons.Next,
            description = stringResource(R.string.media_next),
            buttonSize = 72.dp,
            iconSize = 34.dp,
            tint = colors.text,
            onClick = controls::skipToNext,
        )
    }
}

private fun formatTime(millis: Long): String {
    val totalSeconds = millis / 1000
    val hours = totalSeconds / 3600
    val minutes = totalSeconds % 3600 / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) "%d:%02d:%02d".format(hours, minutes, seconds) else "%d:%02d".format(minutes, seconds)
}

@Composable
private fun Info(nowPlaying: NowPlaying, modifier: Modifier = Modifier) {
    val colors = LocalCockpitColors.current
    val position by produceState(nowPlaying.currentPosition(), nowPlaying) {
        while (true) {
            value = nowPlaying.currentPosition()
            delay(PositionTickMs)
        }
    }
    val fraction = if (nowPlaying.duration > 0) position.toFloat() / nowPlaying.duration else 0f

    Column(
        modifier = modifier.padding(start = 24.dp, top = 6.dp, bottom = 4.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(colors.accent))
                Text(
                    text = stringResource(R.string.media_now_playing, nowPlaying.source),
                    color = colors.muted,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = nowPlaying.title.ifEmpty { stringResource(R.string.media_unknown_track) },
                fontSize = 48.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.03).em,
                lineHeight = 60.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = nowPlaying.artist,
                color = colors.text2,
                fontSize = 24.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Progress(fraction = fraction)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = formatTime(position), color = colors.muted, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                if (nowPlaying.duration > 0) {
                    Text(
                        text = formatTime(nowPlaying.duration),
                        color = colors.muted,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}

@Composable
fun NowPlayingPanel(media: MediaState, modifier: Modifier = Modifier) {
    val nowPlaying = media.nowPlaying

    when {
        !media.granted -> AccessPrompt(modifier)
        nowPlaying == null -> Box(modifier)
        else -> BoxWithConstraints(modifier) {
            val column = (maxWidth - ColumnGap * 3) / 4

            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(ColumnGap),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Art(nowPlaying, Modifier.size(column))
                Info(nowPlaying, Modifier.width(column * 2 + ColumnGap).height(column))
                Controls(nowPlaying, Modifier.width(column))
            }
        }
    }
}

@Composable
private fun Progress(fraction: Float) {
    val colors = LocalCockpitColors.current
    val clamped = fraction.coerceIn(0f, 1f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(16.dp)
            .drawBehind {
                val trackHeight = 6.dp.toPx()
                val top = (size.height - trackHeight) / 2
                val radius = CornerRadius(trackHeight / 2)
                drawRoundRect(colors.track, Offset(0f, top), Size(size.width, trackHeight), radius)
                drawRoundRect(colors.accent, Offset(0f, top), Size(size.width * clamped, trackHeight), radius)
                drawCircle(colors.accent, 8.dp.toPx(), Offset(size.width * clamped, size.height / 2))
            },
    )
}

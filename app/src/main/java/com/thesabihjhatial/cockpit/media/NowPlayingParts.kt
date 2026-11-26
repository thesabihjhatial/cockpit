package com.thesabihjhatial.cockpit.media

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Modifier
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
import com.thesabihjhatial.cockpit.ui.CockpitPalette
import com.thesabihjhatial.cockpit.ui.Icon
import com.thesabihjhatial.cockpit.ui.Text
import kotlinx.coroutines.delay

private val ArtShape = RoundedCornerShape(20.dp)

private const val DisabledAlpha = 0.3f
private const val PositionTickMs = 500L

@Composable
internal fun Art(
    nowPlaying: NowPlaying,
    modifier: Modifier = Modifier,
    elevation: Dp = 16.dp,
    iconSize: Dp = 80.dp,
    shape: RoundedCornerShape = ArtShape,
) {
    val colors = CockpitPalette
    val art = nowPlaying.art

    Box(
        modifier = modifier
            .shadow(elevation, shape, ambientColor = Color.Black.copy(alpha = 0.14f), spotColor = Color.Black.copy(alpha = 0.14f))
            .clip(shape)
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
                modifier = Modifier.size(iconSize),
                tint = colors.accentInk,
            )
        }
    }
}

@Composable
internal fun ControlButton(
    icon: ImageVector,
    description: String,
    buttonSize: Dp,
    iconSize: Dp,
    tint: Color,
    onClick: () -> Unit,
    background: Color = Color.Transparent,
    enabled: Boolean = true,
) {
    Box(
        modifier = Modifier.size(buttonSize).clip(CircleShape).background(background).clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(imageVector = icon, contentDescription = description, modifier = Modifier.size(iconSize), tint = if (enabled) tint else tint.copy(alpha = DisabledAlpha))
    }
}

internal fun formatTime(millis: Long): String {
    val totalSeconds = millis / 1000
    val hours = totalSeconds / 3600
    val minutes = totalSeconds % 3600 / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) "%d:%02d:%02d".format(hours, minutes, seconds) else "%d:%02d".format(minutes, seconds)
}

@Composable
internal fun PlaybackControls(
    nowPlaying: NowPlaying,
    arrangement: Arrangement.Horizontal,
    centerSize: Dp,
    centerIconSize: Dp,
    modifier: Modifier = Modifier,
) {
    val colors = CockpitPalette
    val context = LocalContext.current

    Row(modifier = modifier, horizontalArrangement = arrangement, verticalAlignment = Alignment.CenterVertically) {
        ControlButton(
            icon = CockpitIcons.Previous,
            description = stringResource(R.string.media_previous),
            buttonSize = 72.dp,
            iconSize = 34.dp,
            tint = colors.text,
            onClick = { nowPlaying.skipToPrevious(context) },
            enabled = nowPlaying.hasPrevious,
        )
        ControlButton(
            icon = if (nowPlaying.playing) CockpitIcons.Pause else CockpitIcons.Play,
            description = stringResource(if (nowPlaying.playing) R.string.media_pause else R.string.media_play),
            buttonSize = centerSize,
            iconSize = centerIconSize,
            tint = Color.White,
            onClick = { nowPlaying.togglePlayback(context) },
            background = colors.accent,
        )
        ControlButton(
            icon = CockpitIcons.Next,
            description = stringResource(R.string.media_next),
            buttonSize = 72.dp,
            iconSize = 34.dp,
            tint = colors.text,
            onClick = { nowPlaying.skipToNext(context) },
            enabled = nowPlaying.hasNext,
        )
    }
}

@Composable
internal fun Progress(fraction: Float, modifier: Modifier = Modifier) {
    val colors = CockpitPalette
    val clamped = fraction.coerceIn(0f, 1f)

    Box(
        modifier = modifier
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

@Composable
internal fun rememberPosition(nowPlaying: NowPlaying): State<Long> {
    val position = remember(nowPlaying) { mutableStateOf(nowPlaying.currentPosition()) }
    LaunchedEffect(nowPlaying) {
        while (nowPlaying.playing) {
            delay(PositionTickMs)
            position.value = nowPlaying.currentPosition()
        }
    }
    return position
}

@Composable
internal fun TrackHeading(nowPlaying: NowPlaying, gap: Dp, modifier: Modifier = Modifier) {
    val colors = CockpitPalette

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(gap)) {
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
}

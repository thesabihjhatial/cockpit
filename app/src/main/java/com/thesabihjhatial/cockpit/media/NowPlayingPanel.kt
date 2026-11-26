package com.thesabihjhatial.cockpit.media

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thesabihjhatial.cockpit.R
import com.thesabihjhatial.cockpit.ui.CockpitPalette
import com.thesabihjhatial.cockpit.ui.Text

private val ColumnGap = 16.dp

@Composable
private fun AccessPrompt(launching: String?, onAllow: () -> Unit, modifier: Modifier = Modifier) {
    val colors = CockpitPalette

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
                .clickable(enabled = launching == null, onClick = onAllow)
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
private fun Info(nowPlaying: NowPlaying, modifier: Modifier = Modifier) {
    val colors = CockpitPalette
    val position by rememberPosition(nowPlaying)
    val fraction = if (nowPlaying.duration > 0) position.toFloat() / nowPlaying.duration else 0f

    Column(
        modifier = modifier.padding(start = 24.dp, top = 6.dp, bottom = 4.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        TrackHeading(nowPlaying, gap = 10.dp)
        if (nowPlaying.duration > 0) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Progress(fraction = fraction)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = formatTime(position), color = colors.muted, fontSize = 15.sp, fontWeight = FontWeight.Medium)
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
fun NowPlayingPanel(
    media: MediaState,
    launching: String?,
    onAllow: () -> Unit,
    onExpand: () -> Unit,
    modifier: Modifier = Modifier,
    artModifier: Modifier = Modifier,
) {
    val nowPlaying = media.nowPlaying

    when {
        nowPlaying == null && !media.granted -> AccessPrompt(launching, onAllow, modifier)
        nowPlaying == null -> Box(modifier)
        else -> BoxWithConstraints(modifier) {
            val column = (maxWidth - ColumnGap * 3) / 4
            val artSize = minOf(column, maxHeight)

            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier
                        .width(artSize * 3 + ColumnGap * 2)
                        .clickable(onClickLabel = stringResource(R.string.media_expand), onClick = onExpand),
                    horizontalArrangement = Arrangement.spacedBy(ColumnGap),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Art(nowPlaying, artModifier.size(artSize))
                    Info(nowPlaying, Modifier.width(artSize * 2 + ColumnGap).height(artSize))
                }
                PlaybackControls(
                    nowPlaying = nowPlaying,
                    arrangement = Arrangement.spacedBy(12.dp, Alignment.End),
                    centerSize = 104.dp,
                    centerIconSize = 40.dp,
                    modifier = Modifier.width(artSize),
                )
            }
        }
    }
}

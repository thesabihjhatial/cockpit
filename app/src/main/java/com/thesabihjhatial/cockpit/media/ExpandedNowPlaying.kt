package com.thesabihjhatial.cockpit.media

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thesabihjhatial.cockpit.R
import com.thesabihjhatial.cockpit.ui.CockpitIcons
import com.thesabihjhatial.cockpit.ui.CockpitPalette
import com.thesabihjhatial.cockpit.ui.Icon
import com.thesabihjhatial.cockpit.ui.silentClickable
import com.thesabihjhatial.cockpit.ui.Text

@Composable
fun ColumnScope.ExpandedNowPlaying(
    nowPlaying: NowPlaying,
    rowGap: Dp,
    rowHeight: Dp,
    spectrumAllowed: Boolean,
    onCollapse: () -> Unit,
    artModifier: Modifier = Modifier,
) {
    val colors = CockpitPalette
    val collapseLabel = stringResource(R.string.media_collapse)
    val position by rememberPosition(nowPlaying)
    val spectrum = rememberSpectrum(spectrumAllowed && nowPlaying.playing)
    val fraction = if (nowPlaying.duration > 0) position.toFloat() / nowPlaying.duration else 0f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(rowHeight)
            .silentClickable(onClickLabel = collapseLabel, onClick = onCollapse),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Art(
            nowPlaying = nowPlaying,
            modifier = artModifier.size(152.dp),
            elevation = 12.dp,
            iconSize = 52.dp,
            shape = RoundedCornerShape(16.dp),
        )
        TrackHeading(nowPlaying, gap = 8.dp, modifier = Modifier.weight(1f))
        Box(modifier = Modifier.offset(x = 14.dp).size(56.dp), contentAlignment = Alignment.Center) {
            Icon(
                imageVector = CockpitIcons.ChevronDown,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = colors.muted,
            )
        }
    }
    VisualizerBars(
        playing = nowPlaying.playing,
        spectrum = spectrum,
        modifier = Modifier
            .fillMaxWidth()
            .height(rowHeight * 2 + rowGap)
            .silentClickable(onClickLabel = collapseLabel, onClick = onCollapse),
    )
    Column(
        modifier = Modifier.fillMaxWidth().height(rowHeight),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
    ) {
        if (nowPlaying.duration > 0) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(text = formatTime(position), color = colors.muted, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                Progress(fraction = fraction, modifier = Modifier.weight(1f))
                Text(
                    text = formatTime(nowPlaying.duration),
                    color = colors.muted,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
        PlaybackControls(
            nowPlaying = nowPlaying,
            arrangement = Arrangement.spacedBy(40.dp, Alignment.CenterHorizontally),
            centerSize = 96.dp,
            centerIconSize = 38.dp,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

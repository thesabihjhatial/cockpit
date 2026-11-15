package com.thesabihjhatial.cockpit.header

import androidx.annotation.StringRes
import com.thesabihjhatial.cockpit.R

@StringRes
fun greetingLabel(hour: Int): Int = when (hour) {
    in 5..11 -> R.string.greeting_morning
    in 12..16 -> R.string.greeting_afternoon
    in 17..23 -> R.string.greeting_evening
    else -> R.string.greeting_night
}

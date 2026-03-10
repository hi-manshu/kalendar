/*
 *
 *  * Copyright 2025 Kalendar Contributors (https://www.himanshoe.com). All rights reserved.
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *
 *
 */

package com.himanshoe.kalendar.foundation.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toLowerCase
import androidx.compose.ui.unit.dp
import com.himanshoe.kalendar.foundation.component.config.KalendarHeaderKonfig
import com.himanshoe.kalendar.foundation.locale.KalendarLocale
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month

/**
 * Calendar header displaying a localised "Month 'YY" title with optional navigation arrows
 * and a "today" icon button.
 *
 * @param month The month to display in the title.
 * @param year The year to display in the title.
 * @param modifier Modifier applied to the header row.
 * @param canNavigateBack Whether the back arrow is enabled.
 * @param showCalendarIcon Whether to show the "today" navigation icon.
 * @param calendarIconEnabled Whether the "today" icon is enabled.
 * @param arrowShown Whether to show prev/next arrow buttons.
 * @param kalendarHeaderKonfig Styling configuration for the header.
 * @param kalendarLocale Locale for localised month names.
 * @param onPreviousClick Called when the previous arrow is clicked.
 * @param onNextClick Called when the next arrow is clicked.
 * @param onNavigateToday Called when the "today" icon is clicked.
 */
@Composable
fun KalendarHeader(
    month: Month,
    year: Int,
    modifier: Modifier = Modifier,
    canNavigateBack: Boolean = true,
    showCalendarIcon: Boolean = false,
    calendarIconEnabled: Boolean = false,
    arrowShown: Boolean = true,
    kalendarHeaderKonfig: KalendarHeaderKonfig = KalendarHeaderKonfig.default(),
    kalendarLocale: KalendarLocale = KalendarLocale.default(),
    onPreviousClick: () -> Unit = {},
    onNextClick: () -> Unit = {},
    onNavigateToday: () -> Unit = {},
) {
    val titleText =
        remember(month, year, kalendarLocale) {
            getTitleText(month, year, Locale.current, kalendarLocale)
        }

    KalendarHeaderContent(
        arrowShown = arrowShown,
        titleText = titleText,
        onPreviousClick = onPreviousClick,
        onNextClick = onNextClick,
        onNavigateToday = onNavigateToday,
        showCalendarIcon = showCalendarIcon,
        canNavigateBack = canNavigateBack,
        centerAligned = kalendarHeaderKonfig.centerAligned,
        modifier = modifier.defaultMinSize(minHeight = 56.dp),
        kalendarHeaderKonfig = kalendarHeaderKonfig,
        calendarIconEnabled = calendarIconEnabled
    )
}

/**
 * Calendar header displaying a custom title string with optional navigation arrows
 * and a "today" icon button.
 *
 * Use this overload when the title is pre-formatted (e.g., a week range or year string).
 *
 * @param title The pre-formatted title text to display.
 * @param modifier Modifier applied to the header row.
 * @param showCalendarIcon Whether to show the "today" navigation icon.
 * @param calendarIconEnabled Whether the "today" icon is enabled.
 * @param canNavigateBack Whether the back arrow is enabled.
 * @param arrowShown Whether to show prev/next arrow buttons.
 * @param kalendarHeaderKonfig Styling configuration for the header.
 * @param onPreviousClick Called when the previous arrow is clicked.
 * @param onNextClick Called when the next arrow is clicked.
 * @param onNavigateToday Called when the "today" icon is clicked.
 */
@Composable
fun KalendarHeader(
    title: String,
    modifier: Modifier = Modifier,
    showCalendarIcon: Boolean = false,
    calendarIconEnabled: Boolean = false,
    canNavigateBack: Boolean = true,
    arrowShown: Boolean = true,
    kalendarHeaderKonfig: KalendarHeaderKonfig = KalendarHeaderKonfig.default(),
    onPreviousClick: () -> Unit = {},
    onNextClick: () -> Unit = {},
    onNavigateToday: () -> Unit = {},
) {
    KalendarHeaderContent(
        modifier = modifier.defaultMinSize(minHeight = 56.dp),
        kalendarHeaderKonfig = kalendarHeaderKonfig,
        titleText = title,
        canNavigateBack = canNavigateBack,
        calendarIconEnabled = calendarIconEnabled,
        arrowShown = arrowShown,
        onPreviousClick = onPreviousClick,
        showCalendarIcon = showCalendarIcon,
        onNextClick = onNextClick,
        onNavigateToday = onNavigateToday,
        centerAligned = kalendarHeaderKonfig.centerAligned
    )
}

@Composable
private fun KalendarHeaderContent(
    arrowShown: Boolean,
    titleText: String,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onNavigateToday: () -> Unit,
    showCalendarIcon: Boolean,
    canNavigateBack: Boolean,
    calendarIconEnabled: Boolean,
    centerAligned: Boolean,
    modifier: Modifier = Modifier,
    kalendarHeaderKonfig: KalendarHeaderKonfig = KalendarHeaderKonfig.default(),
) {
    var isNext by rememberSaveable { mutableStateOf(true) }
    val paddingModifier = if (centerAligned) {
        Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    } else {
        Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .then(paddingModifier),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (arrowShown && centerAligned) {
            KalendarIconButton(
                modifier = Modifier.wrapContentSize(),
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Previous Month",
                enabled = canNavigateBack,
                onClick = {
                    isNext = false
                    onPreviousClick()
                }
            )
        }

        AnimatedContent(
            targetState = titleText,
            transitionSpec = {
                addAnimation(isNext = isNext).using(
                    SizeTransform(clip = false)
                )
            }
        ) { month ->
            Text(
                modifier = Modifier
                    .wrapContentSize()
                    .align(Alignment.CenterVertically)
                    .semantics { heading() },
                text = month,
                style = kalendarHeaderKonfig.textStyle,
            )
        }
        if (showCalendarIcon) {
            KalendarIconButton(
                modifier = Modifier.wrapContentSize(),
                imageVector = Icons.Default.DateRange,
                contentDescription = "Calendar Icon for navigating today",
                enabled = calendarIconEnabled,
                onClick = {
                    onNavigateToday()
                }
            )
        }
        if (arrowShown) {
            Row {
                if (!centerAligned) {
                    KalendarIconButton(
                        modifier = Modifier.wrapContentSize(),
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Previous Month",
                        enabled = canNavigateBack,
                        onClick = {
                            isNext = false
                            onPreviousClick()
                        }
                    )
                }
                KalendarIconButton(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    modifier = Modifier.wrapContentSize(),
                    contentDescription = "Next Month",
                    onClick = {
                        isNext = true
                        onNextClick()
                    },
                    enabled = true
                )
            }
        }
    }
}

private fun addAnimation(duration: Int = 200, isNext: Boolean): ContentTransform {
    return (
            slideInVertically(
                animationSpec = tween(durationMillis = duration)
            ) { height -> if (isNext) height else -height } + fadeIn(
                animationSpec = tween(durationMillis = duration)
            )
            ).togetherWith(
            slideOutVertically(
                animationSpec = tween(durationMillis = duration)
            ) { height -> if (isNext) -height else height } + fadeOut(
                animationSpec = tween(durationMillis = duration)
            )
        )
}

private fun getTitleText(
    month: Month,
    year: Int,
    locale: Locale,
    kalendarLocale: KalendarLocale = KalendarLocale.default(),
): String {
    val monthDisplayName = kalendarLocale.monthNames.getOrElse(month.ordinal) {
        month.name.toLowerCase(locale)
            .replaceFirstChar {
                if (it.isLowerCase()) it.toString().capitalize(locale) else it.toString()
            }
    }
    val shortYear = year.toString().takeLast(2)
    return "$monthDisplayName '$shortYear"
}

fun List<LocalDate>.buildHeaderText(
    kalendarLocale: KalendarLocale = KalendarLocale.default(),
): String {
    val months = this.map { it.month }.distinct()
    return if (months.size > 1) {
        val firstName = kalendarLocale.monthNames.getOrElse(months.first().ordinal) {
            months.first().name.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase() else it.toString()
            }
        }
        val lastName = kalendarLocale.monthNames.getOrElse(months.last().ordinal) {
            months.last().name.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase() else it.toString()
            }
        }
        "$firstName '${this.first().year.toString().takeLast(2)}/$lastName '${
            this.last().year.toString().takeLast(2)
        }"
    } else {
        val monthName = kalendarLocale.monthNames.getOrElse(months.first().ordinal) {
            months.first().name.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase() else it.toString()
            }
        }
        "$monthName '${this.first().year.toString().takeLast(2)}"
    }
}

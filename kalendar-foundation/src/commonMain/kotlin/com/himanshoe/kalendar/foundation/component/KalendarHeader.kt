/*
 *
 *  * Copyright 2026 Kalendar Contributors (https://www.himanshoe.com). All rights reserved.
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
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toLowerCase
import androidx.compose.ui.unit.dp
import com.himanshoe.kalendar.foundation.component.config.KalendarHeaderConfig
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month

@Composable
fun KalendarHeader(
    month: Month,
    year: Int,
    modifier: Modifier = Modifier,
    canNavigateBack: Boolean = true,
    showCalendarIcon: Boolean = false,
    calendarIconEnabled: Boolean = false,
    showArrows: Boolean = true,
    headerConfig: KalendarHeaderConfig = KalendarHeaderConfig.default(),
    onPreviousClick: () -> Unit = {},
    onNextClick: () -> Unit = {},
    onNavigateToday: () -> Unit = {},
) {
    val titleText =
        remember(month, year, Locale.current) { getTitleText(month, year, Locale.current) }

    KalendarHeaderContent(
        showArrows = showArrows,
        titleText = titleText,
        onPreviousClick = onPreviousClick,
        onNextClick = onNextClick,
        onNavigateToday = onNavigateToday,
        showCalendarIcon = showCalendarIcon,
        canNavigateBack = canNavigateBack,
        centerAligned = headerConfig.centerAligned,
        modifier = modifier.defaultMinSize(minHeight = 56.dp),
        headerConfig = headerConfig,
        calendarIconEnabled = calendarIconEnabled
    )
}

@Composable
fun KalendarHeader(
    title: String,
    modifier: Modifier = Modifier,
    showCalendarIcon: Boolean = false,
    calendarIconEnabled: Boolean = false,
    canNavigateBack: Boolean = true,
    showArrows: Boolean = true,
    headerConfig: KalendarHeaderConfig = KalendarHeaderConfig.default(),
    onPreviousClick: () -> Unit = {},
    onNextClick: () -> Unit = {},
    onNavigateToday: () -> Unit = {},
) {
    KalendarHeaderContent(
        modifier = modifier.defaultMinSize(minHeight = 56.dp),
        headerConfig = headerConfig,
        titleText = title,
        canNavigateBack = canNavigateBack,
        calendarIconEnabled = calendarIconEnabled,
        showArrows = showArrows,
        onPreviousClick = onPreviousClick,
        showCalendarIcon = showCalendarIcon,
        onNextClick = onNextClick,
        onNavigateToday = onNavigateToday,
        centerAligned = headerConfig.centerAligned
    )
}

@Composable
private fun KalendarHeaderContent(
    showArrows: Boolean,
    titleText: String,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onNavigateToday: () -> Unit,
    showCalendarIcon: Boolean,
    canNavigateBack: Boolean,
    calendarIconEnabled: Boolean,
    centerAligned: Boolean,
    modifier: Modifier = Modifier,
    headerConfig: KalendarHeaderConfig = KalendarHeaderConfig.default(),
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
        if (showArrows && centerAligned) {
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
                    .align(Alignment.CenterVertically),
                text = month,
                style = headerConfig.textStyle,
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
        if (showArrows) {
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

private fun getTitleText(month: Month, year: Int, locale: Locale): String {
    val monthDisplayName = month.name.toLowerCase(locale)
        .replaceFirstChar {
            if (it.isLowerCase()) it.toString().capitalize(locale) else it.toString()
        }
    val shortYear = year.toString().takeLast(2)
    return "$monthDisplayName '$shortYear"
}

fun List<LocalDate>.buildHeaderText(): String {
    val months = this.map { it.month }.distinct()
    return if (months.size > 1) {
        "${months.first().name.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }} '${
            this.first().year.toString().takeLast(2)
        }/${months.last().name.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }} '${
            this.last().year.toString().takeLast(2)
        }"
    } else {
        "${months.first().name.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }} '${
            this.first().year.toString().takeLast(2)
        }"
    }
}

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

package com.himanshoe.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.himanshoe.kalendar.Kalendar
import com.himanshoe.kalendar.KalendarType
import com.himanshoe.kalendar.cosmic.DrillDownMode
import com.himanshoe.kalendar.cosmic.KalendarCosmic
import com.himanshoe.kalendar.cosmic.KalendarCosmicKonfig
import com.himanshoe.kalendar.foundation.action.KalendarDateRange
import com.himanshoe.kalendar.foundation.action.OnDaySelectionAction
import com.himanshoe.kalendar.foundation.color.KalendarColor
import com.himanshoe.kalendar.foundation.component.config.KalendarKonfig
import com.himanshoe.kalendar.foundation.event.BasicKalendarEvent
import com.himanshoe.kalendar.foundation.event.KalendarEvents
import com.himanshoe.kalendar.foundation.locale.KalendarLocale
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn

@Composable
fun App() {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

    val sampleEvents = KalendarEvents(
        eventList = listOf(
            BasicKalendarEvent(
                date = today,
                eventName = "Team Meeting",
                eventDescription = "Sprint planning",
                eventColor = KalendarColor.Solid(Color(0xFF4CAF50)),
            ),
            BasicKalendarEvent(
                date = today,
                eventName = "Lunch",
                eventDescription = "Team lunch",
                eventColor = KalendarColor.Solid(Color(0xFF2196F3)),
            ),
            BasicKalendarEvent(
                date = today.plus(2, DateTimeUnit.DAY),
                eventName = "Release",
                eventDescription = "v2.0 release",
                eventColor = KalendarColor.Solid(Color(0xFFFF5722)),
            ),
        )
    )

    val dateRange = KalendarDateRange(
        minDate = today,
        maxDate = today.plus(30, DateTimeUnit.DAY),
        disabledDates = setOf(
            today.plus(5, DateTimeUnit.DAY),
            today.plus(10, DateTimeUnit.DAY),
        ),
    )

    val spanishLocale = KalendarLocale(
        dayNames = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"),
        shortDayNames = listOf("Lu", "Ma", "Mi", "Ju", "Vi", "Sá", "Do"),
        monthNames = listOf(
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        ),
    )

    Column(modifier = Modifier.wrapContentSize().background(Color.LightGray)) {
        // Cosmic — Annual year view with drill-down into month and week
        KalendarCosmic(
            modifier = Modifier.fillMaxWidth(),
            selectedDate = today,
            events = sampleEvents,
            cosmicKonfig = KalendarCosmicKonfig(
                kalendarKonfig = KalendarKonfig(kalendarLocale = spanishLocale),
                onMonthClick = DrillDownMode.MonthView,
                onWeekClick = DrillDownMode.WeekView,
            ),
            onDaySelectionAction = OnDaySelectionAction.Single { date, events ->
                println("Cosmic Selected: $date with events: $events")
            },
        )
        Spacer(modifier = Modifier.height(16.dp))
        Kalendar(
            selectedDate = today,
            modifier = Modifier.fillMaxWidth(),
            events = sampleEvents,
            startDayOfWeek = DayOfWeek.SUNDAY,
            kalendarType = KalendarType.Aerial,
            dateRange = dateRange,
            kalendarKonfig = KalendarKonfig(kalendarLocale = spanishLocale),
            onDaySelectionAction = OnDaySelectionAction.Multiple { date, events ->
                println("Selected Date: $date with events: $events")
            },
        )
        Spacer(modifier = Modifier.height(16.dp))
        Kalendar(
            selectedDate = today,
            modifier = Modifier.fillMaxWidth(),
            events = sampleEvents,
            startDayOfWeek = DayOfWeek.MONDAY,
            kalendarType = KalendarType.Oceanic,
            dateRange = dateRange,
            kalendarKonfig = KalendarKonfig(kalendarLocale = spanishLocale),
            onDaySelectionAction = OnDaySelectionAction.Single { date, events ->
                println("Selected Date: $date with events: $events")
            },
        )
    }
}

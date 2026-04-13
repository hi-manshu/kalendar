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

package com.himanshoe.kalendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastMap
import com.himanshoe.kalendar.foundation.action.OnDaySelectionAction
import com.himanshoe.kalendar.foundation.component.config.KalendarConfig
import com.himanshoe.kalendar.foundation.component.config.KalendarHeaderConfig
import com.himanshoe.kalendar.foundation.event.KalendarEvents
import com.himanshoe.kalendar.foundation.event.KalendarEvent
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

@Composable
internal fun KalendarAgenda(
    events: KalendarEvents,
    config: KalendarConfig,
    modifier: Modifier = Modifier,
    onDaySelectionAction: OnDaySelectionAction = OnDaySelectionAction.NoOp,
) {
    KalendarAgendaContent(
        events = events,
        config = config,
        modifier = modifier,
        onDaySelectionAction = onDaySelectionAction,
    )
}

@Composable
private fun KalendarAgendaContent(
    events: KalendarEvents,
    config: KalendarConfig,
    modifier: Modifier = Modifier,
    onDaySelectionAction: OnDaySelectionAction,
) {
    val groupedEvents: List<Pair<LocalDate, List<KalendarEvent>>> = remember(events) {
        events
            .groupBy { it.date }
            .entries
            .sortedBy { it.key }
            .fastMap { (date, dateEvents) ->
                date to dateEvents.sortedWith(
                    compareBy(nullsLast()) { it.startTime }
                )
            }
    }

    Box(
        modifier = modifier.background(brush = Brush.linearGradient(config.backgroundColor.value)),
    ) {
        if (groupedEvents.isEmpty()) {
            AgendaEmptyState()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                groupedEvents.forEach { (date, dateEvents) ->
                    item(key = date.toEpochDays()) {
                        AgendaDateHeader(date = date, headerConfig = config.headerConfig)
                    }
                    items(
                        items = dateEvents,
                        key = { event -> "${event.date}_${event.eventName}_${event.startTime}" },
                    ) { event ->
                        AgendaEventRow(event = event)
                    }
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }
            }
        }
    }
}

@Composable
private fun AgendaDateHeader(
    date: LocalDate,
    headerConfig: KalendarHeaderConfig,
) {
    val monthName = date.month.name
        .lowercase()
        .replaceFirstChar { it.uppercaseChar() }
    val label = "${date.dayOfMonth} $monthName ${date.year}"

    Text(
        text = label,
        style = headerConfig.textStyle.copy(fontSize = 14.sp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
    )
}

@Composable
private fun AgendaEventRow(event: KalendarEvent) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFDF6F7)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(
                        color = event.eventColor ?: Color(0xFFD8A29E),
                    ),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.eventName,
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF413D4B),
                    ),
                )
                event.eventDescription?.let { description ->
                    Text(
                        text = description,
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = Color(0xFF7A7A8C),
                        ),
                    )
                }
                val timeLabel = buildTimeLabel(event.startTime, event.endTime)
                if (timeLabel != null) {
                    Text(
                        text = timeLabel,
                        style = TextStyle(
                            fontSize = 11.sp,
                            color = Color(0xFF9E9EB0),
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun AgendaEmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "No events",
            style = TextStyle(
                fontSize = 16.sp,
                color = Color(0xFF9E9EB0),
                textAlign = TextAlign.Center,
            ),
        )
    }
}

private fun buildTimeLabel(start: kotlinx.datetime.LocalDateTime?, end: kotlinx.datetime.LocalDateTime?): String? {
    if (start == null) return null
    val startStr = formatTime(start.time)
    val endStr = end?.let { formatTime(it.time) }
    return if (endStr != null) "$startStr – $endStr" else startStr
}

private fun formatTime(time: LocalTime): String {
    val h = time.hour.toString().padStart(2, '0')
    val m = time.minute.toString().padStart(2, '0')
    return "$h:$m"
}

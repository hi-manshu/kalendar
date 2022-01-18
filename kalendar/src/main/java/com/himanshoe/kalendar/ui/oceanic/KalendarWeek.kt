package com.himanshoe.kalendar.ui.oceanic

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import com.himanshoe.design.primitive.texts.Demibold
import com.himanshoe.design.primitive.texts.KalendarText
import com.himanshoe.design.primitive.texts.Regular
import com.himanshoe.design.theme.KalendarShape
import com.himanshoe.design.theme.KalendarTheme
import java.time.LocalDate

@Composable
internal fun KalendarOceanWeek(
    startDate: LocalDate = LocalDate.now(),
    onCurrentDayClick: (LocalDate) -> Unit
) {
    val haptic = LocalHapticFeedback.current

    val displayWeek = remember {
        mutableStateOf(startDate.getNext7Dates())
    }
    val selectedDate = remember {
        mutableStateOf(startDate)
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        val size = (maxWidth / 7)
        val monthName = "${displayWeek.value.last().month.name} ${displayWeek.value.last().year}"

        Column(Modifier.fillMaxWidth()) {

            KalendarOceanHeader(monthName, displayWeek, haptic)

            Row(modifier = Modifier.fillMaxWidth()) {
                displayWeek.value.forEach { date ->

                    Column(
                        modifier = Modifier.clip(KalendarShape.SelectedShape)
                    ) {
                        KalendarText.Text5.Regular(
                            text = date.dayOfWeek.toString().subSequence(0, 3).toString(),
                            modifier = Modifier
                                .width(size)
                                .wrapContentHeight(),
                            textAlign = TextAlign.Center
                        )
                        KalendarText.Text4.Demibold(
                            text = date.dayOfMonth.toString(),
                            modifier = Modifier
                                .size(size)
                                .clip(KalendarShape.CircularShape)
                                .background(
                                    getColor(
                                        selectedDate = selectedDate.value,
                                        providedDate = date
                                    )
                                )
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    selectedDate.value = date
                                    onCurrentDayClick(date)
                                }
                                .wrapContentHeight(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun getColor(selectedDate: LocalDate, providedDate: LocalDate): Color {
    return if (providedDate == LocalDate.now()) {
        when (selectedDate) {
            providedDate -> KalendarTheme.colors.generalDisabled
            else -> KalendarTheme.colors.generalSelected
        }
    } else {
        when (selectedDate) {
            providedDate -> KalendarTheme.colors.generalDisabled
            else -> Color.Transparent
        }
    }
}

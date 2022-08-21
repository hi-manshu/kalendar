package com.himanshoe.kalendar.utils

import java.text.SimpleDateFormat
import java.util.Locale

fun getMonthNameInDeviceLocale(monthIndex: Int): String {
    return SimpleDateFormat("MMMMM", Locale.getDefault())
        .dateFormatSymbols
        .months[monthIndex]
}

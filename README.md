## 📅 Kalendar: The Calendar Your App Deserves

![Kalendar](img/banner.png)
![Github: Open Issues](https://img.shields.io/github/issues-raw/hi-manshu/kalendar?color=7E8EFB&label=Kalendar%3A%20Open%20Issues)
[![Kalendar](https://img.shields.io/badge/Kotlin%20Weekly-%23286-orange)](https://mailchi.mp/kotlinweekly/kotlin-weekly-286)
[![Kalendar](https://img.shields.io/badge/Android%20Weekly-%23533-Pink)](https://androidweekly.net/issues/issue-533)
[![Kalendar](https://img.shields.io/badge/Canopas%20Engineering-%2372-blue)](https://blog.canopas.com/android-stack-weekly-issue-72-20658bea40a2)
![Kalendar](https://img.shields.io/maven-central/v/com.himanshoe/kalendar?style=flat&label=Kalendar)
![KalendarFoundation](https://img.shields.io/maven-central/v/com.himanshoe/kalendar-foundation?style=plastic&label=Kalendar-Foundation)
[![Github Followers](https://img.shields.io/github/followers/hi-manshu?label=Follow&style=social)](https://github.com/hi-manshu)
[![Twitter Follow](https://img.shields.io/twitter/follow/hi_man_shoe?label=Follow&style=social)](https://twitter.com/hi_man_shoe)

Kalendar is no ordinary library — it’s the _Elder Wand_ of calendar components, crafted for
Compose (now with KMP support) sorcerers who demand both power and elegance.
With the flick of your wrist (or a few lines of Kotlin), you conjure a magical, interactive calendar
that bends to your will. With Kalendar, your app becomes as organized as Hermione’s study schedule —
but far more fun.

So go on, Wield this enchanted tool and create a calendar so charming and powerful, even *
*_Dumbledore_** would pause to admire it.

## 🎉 Why Kalendar?

- **🪄 Unparalleled Customization**: Shape your calendar like it’s made of Transfiguration magic.
  Colors, styles, themes — you control it all.
- **📜 Event Management, Sorted**: Whether it’s the next **Quidditch match** or your weekly stand-up,
  display events clearly and beautifully.
- **✨ Interactive and Responsive**:Tap, click, and swipe — and watch your calendar respond like it’s
  enchanted.
- **🚀 KMP/CMP-Friendly**: No weird incantations needed — just simple, idiomatic Multiplatform
  Compose code.

So unless you fancy scribbling your dates on a Howler, let Kalendar handle your scheduling magic.

> _Made with ❤️ for Android Developers by Himanshu_

## Kalendar supports these types of calendar, Click to have detailed information:

- **[Oceanic](doc/Oceanic.md)**: A majestic spell that conjures the MonthView..
- **[Firey](doc/Firey.md)**: A fiery enchantment that reveals the WeekView.
- **[Solaris](doc/Solaris.md)**: A charm that lets you swipe through the calendar in MonthView, as
  if by magic.
- **[Aerial](doc/Aerial.md)**: A spell that grants you the power to swipe through the calendar in
  WeekView, with the
  flick of a wand.
- **[Yearly](doc/Yearly.md)**: A year-overview charm showing all twelve months at a glance.
- **[Season](doc/Season.md)**: A seasonal enchantment — three meteorological months, tinted to the
  season.
- **[Agenda](doc/Agenda.md)**: An event list grouped by date, ready for daily briefings.

## 🎉 Getting Started

### Version Catalog

If you're using Version Catalog, you can configure the dependency by adding it to your
`libs.versions.toml` file as follows:
<details open>

```toml
[versions]
kalendar = "<version>"
kalendarFoundation = "<version>"

[libraries]
kalendar = { module = "com.himanshoe:kalendar", version.ref = "kalendar" }
kalendar-foundation = { module = "com.himanshoe:kalendar-foundation", version.ref = "kalendarFoundation" }
```

</details>

### Gradle

<details>
Add the dependency below to your module's `build.gradle.kts` file:

```gradle
dependencies {
    implementation("com.himanshoe:kalendar:$version")
    implementation("com.himanshoe:kalendar-foundation:$version")
    
    // if you're using Version Catalog
    implementation(libs.kalendar)
    implementation(libs.kalendar.foundation)

}
```

For Kotlin Multiplatform, add the dependency below to your commonMain source set's
`build.gradle.kts` file:

```gradle
sourceSets {
    commonMain.dependencies {
          implementation(libs.kalendar)
          implementation(libs.kalendar.foundation)
     }
}
```

</details>

## 📖Documentation

You can find it here: [Kalendar Documentation](doc/)




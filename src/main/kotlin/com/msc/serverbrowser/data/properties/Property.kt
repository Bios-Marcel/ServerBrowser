package com.msc.serverbrowser.data.properties

/**
 * Holds all existent properties. Do not adjust any of the ids.
 *
 * TODO Think about a way to do one time database migration and cleanup ids
 *
 * @author Marcel
 * @property id an integer that is used to identify the property
 */
sealed class Property<out T>(val id: Int, val defaultValue: T)

object LastViewProperty : Property<Int>(0, 1)
object MaximizedProperty : Property<Boolean>(2, false)
object Fullscreen : Property<Boolean>(3, false)
object ShowChangelogProperty : Property<Boolean>(4, false)
object SaveLastViewProperty : Property<Boolean>(6, true)
object AskForNameOnConnectProperty : Property<Boolean>(7, false)
object SampPathProperty : Property<String>(8, "")
object UseDarkThemeProperty : Property<Boolean>(9, false)
object AllowCloseGtaProperty : Property<Boolean>(10, false)
object AllowCloseSampProperty : Property<Boolean>(11, false)
object ChangelogEnabledProperty : Property<Boolean>(12, true)
object AllowCachingDownloadsProperty : Property<Boolean>(15, true)
object AutomaticUpdatesProperty : Property<Boolean>(16, true)
object LanguageProperty : Property<String>(17, "en")
object ConnectOnDoubleClickProperty : Property<Boolean>(18, true)
object DownloadPreReleasesProperty : Property<Boolean>(19, false)
object WineBinaryProperty : Property<String>(20, "")
object WinePrefixProperty : Property<String>(21, "")

package serverbrowser.data.properties

/**
 * Holds all existent properties. Do not adjust any of the ids. TODO Think about a way to do one
 * time database migration and cleanup ids
 *
 * @author Marcel
 * @property id an integer that is used to identify the property
 */
sealed class Property<out T>(val id: Int, val defaultValue: T, dataType: Class<T>)

object LastViewProperty : Property<Int>(0, 1, Int::class.java)
object MaximizedProperty : Property<Boolean>(2, false, Boolean::class.java)
object Fullscreen : Property<Boolean>(3, false, Boolean::class.java)
object ShowChangelogProperty : Property<Boolean>(4, false, Boolean::class.java)
object SaveLastViewProperty : Property<Boolean>(6, true, Boolean::class.java)
object AskForNameOnConnectProperty : Property<Boolean>(7, false, Boolean::class.java)
object SampPathProperty : Property<String>(8, "", String::class.java)
object UseDarkThemeProperty : Property<Boolean>(9, false, Boolean::class.java)
object AllowCloseGtaProperty : Property<Boolean>(10, false, Boolean::class.java)
object AllowCloseSampProperty : Property<Boolean>(11, false, Boolean::class.java)
object ChangelogEnabledProperty : Property<Boolean>(12, true, Boolean::class.java)
object AllowCachingDownloadsProperty : Property<Boolean>(15, true, Boolean::class.java)
object AutomaticUpdatesProperty : Property<Boolean>(16, true, Boolean::class.java)
object LanguageProperty : Property<String>(17, "en", String::class.java)
object ConnectOnDoubleClickProperty : Property<Boolean>(18, true, Boolean::class.java)
object DownloadPreReleasesProperty : Property<Boolean>(19, false, Boolean::class.java)

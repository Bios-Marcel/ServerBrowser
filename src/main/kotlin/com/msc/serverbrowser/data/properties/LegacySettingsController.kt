package com.msc.serverbrowser.data.properties

import com.msc.serverbrowser.constants.PathConstants
import com.msc.serverbrowser.logging.Logging
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

/**
 * Contains convenient methods for interacting with the SA-MP legacy settings.
 *
 *
 * [SA-MP.cfg Wiki](http://wiki.sa-mp.com/wiki/Sa-mp.cfg.html)
 *
 *
 * @author Marcel
 */
object LegacySettingsController {
    private const val FALSE_AS_INT = "0"

    const val FPS_LIMIT = "fpslimit"
    const val PAGE_SIZE = "pagesize"
    const val MULTICORE = "multicore"
    const val TIMESTAMP = "timestamp"
    const val AUDIO_PROXY_OFF = "audioproxyoff"
    const val AUDIO_MESSAGE_OFF = "audiomsgoff"
    const val HEAD_MOVE = "disableheadmove"
    const val IME = "ime"
    const val DIRECT_MODE = "directmode"
    const val NO_NAME_TAG_STATUS = "nonametagstatus"

    const val FPS_LIMIT_DEFAULT = "50"
    const val PAGE_SIZE_DEFAULT = "10"
    const val MULTICORE_DEFAULT = "1"
    const val TIMESTAMP_DEFAULT = FALSE_AS_INT
    const val AUDIO_PROXY_OFF_DEFAULT = FALSE_AS_INT
    const val AUDIO_MESSAGE_OFF_DEFAULT = FALSE_AS_INT
    const val DISABLE_HEAD_MOVE_DEFAULT = FALSE_AS_INT
    const val IME_DEFAULT = FALSE_AS_INT
    const val DIRECT_MODE_DEFAULT = FALSE_AS_INT
    const val NO_NAME_TAG_STATUS_DEFAULT = FALSE_AS_INT

    /**
     * @return [Properties] object containing the present legacy SA-MP Settings or an empty
     * [Optional]
     */
    val legacyProperties: Optional<Properties>
        get() {
            if (Files.notExists(Paths.get(PathConstants.SAMP_PATH))) {
                Logging.error("SA-MP data can't be found.")
                return Optional.empty()
            }

            try {
                Files.newInputStream(Paths.get(PathConstants.SAMP_CFG)).use { input ->
                    val properties = Properties()
                    properties.load(input)
                    return Optional.of(properties)
                }
            } catch (exception: IOException) {
                Logging.error("Error while loading SA_MP legacy properties.", exception)
                return Optional.empty()
            }

        }

    /**
     * Override the SA-MP legacy settings using the passed [Properties] object.
     *
     * @param properties the [Properties] object to overwrite the legacy properties with
     */
    fun save(properties: Properties) {
        try {
            Files.newOutputStream(Paths.get(PathConstants.SAMP_CFG)).use { output -> properties.store(output, null) }
        } catch (exception: IOException) {
            Logging.error("Error while saving SA_MP legacy properties.", exception)
        }

    }

    fun restoreLegacySettings() {
        val properties = legacyProperties.orElse(Properties())

        properties[LegacySettingsController.AUDIO_MESSAGE_OFF] = LegacySettingsController.AUDIO_MESSAGE_OFF_DEFAULT
        properties[LegacySettingsController.AUDIO_PROXY_OFF] = LegacySettingsController.AUDIO_PROXY_OFF_DEFAULT
        properties[LegacySettingsController.DIRECT_MODE] = LegacySettingsController.AUDIO_PROXY_OFF_DEFAULT
        properties[LegacySettingsController.HEAD_MOVE] = LegacySettingsController.DISABLE_HEAD_MOVE_DEFAULT
        properties[LegacySettingsController.FPS_LIMIT] = LegacySettingsController.FPS_LIMIT_DEFAULT
        properties[LegacySettingsController.PAGE_SIZE] = LegacySettingsController.PAGE_SIZE_DEFAULT
        properties[LegacySettingsController.IME] = LegacySettingsController.IME_DEFAULT
        properties[LegacySettingsController.MULTICORE] = LegacySettingsController.MULTICORE_DEFAULT
        properties[LegacySettingsController.TIMESTAMP] = LegacySettingsController.TIMESTAMP_DEFAULT
        properties[LegacySettingsController.NO_NAME_TAG_STATUS] = LegacySettingsController.NO_NAME_TAG_STATUS_DEFAULT

        save(properties)
    }
}// Constructor to prevent instantiation

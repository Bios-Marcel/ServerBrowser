package com.msc.serverbrowser

import org.jnativehook.GlobalScreen
import org.jnativehook.keyboard.NativeKeyAdapter
import org.jnativehook.keyboard.NativeKeyEvent
import java.awt.Robot
import java.awt.event.KeyEvent
import java.util.logging.Level
import java.util.logging.Logger


fun main(args: Array<String>) {
    // Disable logging, it is super annoying
    val logger = Logger.getLogger(GlobalScreen::class.java.getPackage().name)
    logger.level = Level.OFF
    logger.useParentHandlers = false

    //Create and configure our keybinder
    val keyBinder = KeyBinderProofOfConcept()
    keyBinder.keyBindings.add(KeyBinding(arrayOf(NativeKeyEvent.CTRL_L_MASK, NativeKeyEvent.SHIFT_L_MASK), NativeKeyEvent.VC_T, "Test"))

    //Register the keybinder as a native keylistener
    GlobalScreen.registerNativeHook()
    GlobalScreen.addNativeKeyListener(keyBinder)
}

class KeyBinding(val modifiers: Array<Int>, val keyCode: Int, val output: String)

fun NativeKeyEvent.fitsKeyBinding(keyBinding: KeyBinding): Boolean {
    return modifiers == keyBinding.modifiers.sum() && keyCode == keyBinding.keyCode
}

class KeyBinderProofOfConcept : NativeKeyAdapter() {

    private var pauseListener = false

    val keyBindings = mutableListOf<KeyBinding>()

    override fun nativeKeyPressed(e: NativeKeyEvent) {
        val matchingKeyBindings = keyBindings
                .filter { e.fitsKeyBinding(it) }
                .toCollection(mutableListOf())

        if (matchingKeyBindings.isNotEmpty()) {

            if (pauseListener) {
                return
            }

            pauseListener = true

            val robot = Robot()

            //Workaround, might be solved better at some point, but i am releasing all modifiers and the pressed key to avoid bugs
            robot.keyRelease(KeyEvent.VK_SHIFT)
            robot.keyRelease(KeyEvent.VK_CONTROL)
            robot.keyRelease(KeyEvent.VK_META)
            robot.keyRelease(KeyEvent.VK_WINDOWS)

            //Little hack to find out the awt keycode ...
            robot.keyRelease(NativeKeyEvent.getKeyText(e.keyCode)[0].mapToAwtKeyCode())

            matchingKeyBindings.forEach {
                for (character in it.output) {

                    if (character.isUpperCase()) {
                        robot.keyPress(KeyEvent.VK_SHIFT)
                    }

                    val keyChar = character.mapToAwtKeyCode()
                    robot.keyPress(keyChar)
                    robot.keyRelease(keyChar)

                    if (character.isUpperCase()) {
                        robot.keyRelease(KeyEvent.VK_SHIFT)
                    }
                }
            }

            pauseListener = false
        }
    }
}

fun Char.mapToAwtKeyCode(): Int {
    return when (this.toUpperCase()) {
        'A' -> KeyEvent.VK_A
        'B' -> KeyEvent.VK_B
        'C' -> KeyEvent.VK_C
        'D' -> KeyEvent.VK_D
        'E' -> KeyEvent.VK_E
        'F' -> KeyEvent.VK_F
        'G' -> KeyEvent.VK_G
        'H' -> KeyEvent.VK_H
        'I' -> KeyEvent.VK_I
        'J' -> KeyEvent.VK_J
        'K' -> KeyEvent.VK_K
        'L' -> KeyEvent.VK_L
        'M' -> KeyEvent.VK_M
        'N' -> KeyEvent.VK_N
        'O' -> KeyEvent.VK_O
        'P' -> KeyEvent.VK_P
        'Q' -> KeyEvent.VK_Q
        'R' -> KeyEvent.VK_R
        'S' -> KeyEvent.VK_S
        'T' -> KeyEvent.VK_T
        'U' -> KeyEvent.VK_U
        'V' -> KeyEvent.VK_V
        'W' -> KeyEvent.VK_W
        'X' -> KeyEvent.VK_X
        'Y' -> KeyEvent.VK_Y
        'Z' -> KeyEvent.VK_Z
        else -> KeyEvent.VK_UNDEFINED
    }
}

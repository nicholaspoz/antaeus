package io.pleo.antaeus.core

/*
 A placeholder for more a more robust logging configuration
 */
object logger {
    fun debug(msg: String) = println("[DEBUG]: $msg")
    fun info(msg: String) = println("[INFO]: $msg")
    fun warn(msg: String) = println("[WARN]: $msg")
    fun error(msg: String) = println("[ERROR]: $msg")
}

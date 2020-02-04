package me.camdenorrb.calendario

import org.javacord.api.DiscordApiBuilder
import java.time.DayOfWeek

object Main {

    data class Busy(val userID: Long, val dayOfWeek: DayOfWeek, val timespan: IntRange)
    data class Free(val userID: Long, val dayOfWeek: DayOfWeek, val timespan: IntRange)

    @JvmStatic
    fun main(args: Array<String>) {
        val token = this.javaClass.classLoader.getResource("token.txt")!!.readText()
        val client = DiscordApiBuilder().setToken(token).login().join()

        // busy command:
        client.addMessageCreateListener {
            if (it.messageContent.startsWith("-busy", true)) {
                // -busy Monday 11am-5pm Tuesday 2pm-5pm wednesday n/a thursday 10am-1pm 5pm-9pm
                val schedule = it.messageContent.split(' ').drop(1).windowed(2, 2, false) { (day, timeFrame) ->
                    val dayOfWeek = checkNotNull(DayOfWeek.values().find { it.name.equals(day, true) }) {
                        "Day of week invalid '$day"
                    }

                    val (start, end) = checkNotNull(timeFrame.split('-').takeIf { it.size == 2 }) {
                        "Invalid timeframe '$timeFrame"
                    }

                    var startTime = start.takeWhile { it.isDigit() }.toInt()
                    var endTime = end.takeWhile { it.isDigit() }.toInt()

                    check(startTime in 1..12) {
                        "The start time is invalid: '$startTime"
                    }

                    check(endTime in 1..12) {
                        "The end time is invalid: '$endTime'"
                    }

                    if (end.contains("pm", true)) {
                        if (endTime != 12) {
                            endTime += 12
                        }
                    }

                    Busy(0L, dayOfWeek, startTime..endTime)
                }.sortedBy { it.timespan.first }.groupBy { it.dayOfWeek }

                it.channel.sendMessage("$schedule")

            } else if (it.messageContent.startsWith("-freetime", true)) {
                // TODO: Return the available freetimes for this week
                var free = it.messageContent.split(' ').drop(1).windowed(2, 2, false) {
                    (day, timeFrame) ->
                    val dayOfWeek = checkNotNull(DayOfWeek.values().find { it.name.equals(day, true) }) {
                        "Day of week invalid '$day"
                    }

                    val (start, end) = checkNotNull(timeFrame.split('-').takeIf { it.size == 2 }) {
                        "Invalid timeframe '$timeFrame"
                    }

                    var startTime = start.takeWhile { it.isDigit() }.toInt()
                    var endTime = end.takeWhile { it.isDigit() }.toInt()

                    check(startTime in 1..12) {
                        "The start time is invalid: '$startTime"
                    }

                    check(endTime in 1..12) {
                        "The end time is invalid: '$endTime'"
                    }

                    if (end.contains("pm", true)) {
                        if (endTime != 12) {
                            endTime += 12
                        }
                    }

                    Free(0L, dayOfWeek, startTime..endTime)
                }.sortedBy { it.timespan.first }.groupBy { it.dayOfWeek }

                it.channel.sendMessage("$free")
            }

        }

        println("You can invite the bot by using the following url: ${client.createBotInvite()}")
    }

}
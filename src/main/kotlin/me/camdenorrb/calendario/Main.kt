package me.camdenorrb.calendario

import org.javacord.api.DiscordApiBuilder
import java.time.DayOfWeek

object Main {

    data class Busy(val userID: Long, val dayOfWeek: DayOfWeek, val timespan: IntRange)

    private fun Int.to12Hour(): String =
        when (this) {
            !in 13..24 -> {
                "${this}AM"
            }
            else -> "${this - 12}PM"
        }

    @JvmStatic
    fun main(args: Array<String>) {

        val token = this.javaClass.classLoader.getResource("token.txt")!!.readText()
        val client = DiscordApiBuilder().setToken(token).login().join()

        // busy command:
        client.addMessageCreateListener { event ->

            if (event.messageContent.startsWith("-busy", true)) {
                // -busy Monday 11am-5pm Tuesday 2pm-5pm wednesday n/a thursday 10am-1pm 5pm-9pm

                val schedule = event.messageContent.split(' ').drop(1).windowed(2, 2, false) { (day, timeFrame) ->

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

                    if (start.contains("pm", true)) {
                        if (startTime != 12) {
                            startTime += 12
                        }
                    }

                    if (end.contains("pm", true)) {
                        if (endTime != 12) {
                            endTime += 12
                        }
                    }

                    Busy(0L, dayOfWeek, startTime..endTime)

                }.sortedBy { it.timespan.first }.groupBy { it.dayOfWeek }.toMutableMap()

                // freetime

                schedule.forEach { (day, daySchedule) ->

                    val newDaySchedule = mutableListOf<Busy>()

                    daySchedule.indices.forEach indices@{

                        val curr = daySchedule[it]
                        val next = daySchedule.getOrNull(it + 1)

                        if (next == null) {
                            newDaySchedule += curr
                            return@indices
                        }

                    }
                }

                DayOfWeek.values().forEach { dayOfWeek ->

                    var lastEndTime = 0

                    val message =
                        schedule[dayOfWeek]?.joinToString(prefix = "Free ${dayOfWeek.name.toLowerCase().capitalize()}: ") {
                            var timeRange = lastEndTime..it.timespan.first
                            var secondTimeRange = it.timespan.last..lastEndTime
                            lastEndTime = it.timespan.last

                            println(timeRange)
                            println(secondTimeRange)

                            "[${timeRange.first.to12Hour()} to ${timeRange.last.to12Hour()}]; " +
                                    "[${secondTimeRange.first.to12Hour()} to ${secondTimeRange.last.to12Hour()}]"
                        }

                    event.channel.sendMessage(message)
                }

            } // else if (it.messageContent.startsWith("-freetime", true)) {
            // TODO: Return the available freetimes for this week

        }
        println("You can invite the bot by using the following url: ${client.createBotInvite()}")
    }


}


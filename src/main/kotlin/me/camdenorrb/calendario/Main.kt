package me.camdenorrb.calendario

import org.javacord.api.DiscordApiBuilder

object Main {

    @JvmStatic
    fun main(args: Array<String>) {

        val token = this.javaClass.classLoader.getResource("token.txt")!!.readText()
        val client = DiscordApiBuilder().setToken(token).login().join()

        client.addMessageCreateListener {
            if (it.messageContent.startsWith("-busy", true)) {
                // TODO: Finish the busy command!
                // -busy Monday 11am-5pm Tuesday 2pm-5pm wednesday n/a thursday 10am-1pm 5pm-9pm
            }
            else if (it.messageContent.startsWith("-freetime", true)) {
                // TODO: Return the available freetimes for this week
            }

        }

        println("You can invite the bot by using the following url: ${client.createBotInvite()}")
    }

}
package cc.worldmandia.snowball_plugin

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.CommandPermission
import dev.jorel.commandapi.arguments.GreedyStringArgument
import dev.jorel.commandapi.executors.CommandExecutor
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit.broadcast
import org.bukkit.plugin.java.JavaPlugin

class SbkmpPlugin : JavaPlugin() {

    override fun onLoad() {
        CommandAPICommand("pversion").withArguments(GreedyStringArgument("message")).withAliases("kmpversion")
            .withPermission(CommandPermission.OP).executes(CommandExecutor { sender, args ->
                val message = args["message"] as String
                broadcast(Component.text("Version: ${getVersion()}, message: $message"), CommandPermission.OP.permission.get())
            }).register()
    }

    override fun onEnable() {
        logger.info("Sbkmp Plugin Enabled with ${getVersion()}")
    }

    override fun onDisable() {

    }
}
package me.matthewe.chatfilter;

import me.matthewedevelopment.atheriallib.AtherialLib;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public final class ChatFilter extends AtherialLib {


    private ChatFilterConfig chatFilterConfig;
    private DiscordHandler discordHandler;
    @Override
    public void onStart() {
        Bukkit.getPluginManager().registerEvents(new ChatListener(this), this);
        this.chatFilterConfig = new ChatFilterConfig(this);
        this.chatFilterConfig.reloadConfig(false);

        this.discordHandler = new DiscordHandler(this);
        this.discordHandler.startWebhook();
    }

    public DiscordHandler getDiscordHandler() {
        return discordHandler;
    }

    @Override
    public void onStop() {
        this.discordHandler.handleStop();
    }

    @Override
    public void initDependencies() {

    }

    public ChatFilterConfig getChatFilterConfig() {
        return chatFilterConfig;
    }
}
package me.matthewe.chatfilter;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.List;

import static me.matthewe.chatfilter.BannedWordsUtils.findBannedWordsInMessage;
import static me.matthewedevelopment.atheriallib.utilities.ChatUtils.colorize;

/**
 * Created by Matthew E on 11/30/2023 at 8:44 PM for the project ChatFilter
 */
public class ChatListener  implements Listener {
    private ChatFilter chatFilter;


    public ChatListener(ChatFilter chatFilter) {
        this.chatFilter = chatFilter;
    }

    @EventHandler(ignoreCancelled = true)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        if (chatFilter.getChatFilterConfig().isEnableBypass()&&event.getPlayer().hasPermission("chatfilter.bypass"));
        List<BannedWordsUtils.FoundBannedWord> bannedWords = findBannedWordsInMessage(event.getMessage(), this.chatFilter.getChatFilterConfig().getWordMap());
        if (bannedWords.isEmpty()){

            return;
        }
        int size = (int) bannedWords.stream().filter(bannedWord -> !bannedWord.isSkip()).count();
        if (size == 0){
            return;
        }
        event.setCancelled(true);
        for (Player onlinePlayer : Bukkit.getServer().getOnlinePlayers()) {
            if (onlinePlayer.hasPermission("chatfilter.view")){
                onlinePlayer.sendMessage(colorize(this.chatFilter.getChatFilterConfig().getMessages().getView())
                        .replaceAll("%username%", event.getPlayer().getName())
                                .replaceAll("%message%", event.getMessage())
                                .replaceAll("%display_name%", event.getPlayer().getDisplayName())
                        .replaceAll("%player%", event.getPlayer().getName()));
            }
        }
        event.getPlayer().sendMessage(colorize(this.chatFilter.getChatFilterConfig().getMessages().getBlocked())
                .replaceAll("%username%", event.getPlayer().getName())
                .replaceAll("%message%", event.getMessage())
                .replaceAll("%player%", event.getPlayer().getName()));
        for (BannedWordsUtils.FoundBannedWord bannedWord : bannedWords) {
            if (bannedWord.isSkip())continue;
            chatFilter.getDiscordHandler().sendFilterMessage(event.getPlayer().getUniqueId(),event.getPlayer().getName(), bannedWord.getBannedWord().getText(), bannedWord.getRoll());

        }
        Bukkit.getScheduler().scheduleSyncDelayedTask(this.chatFilter, () -> {
            chatFilter.getChatFilterConfig().executePunishment(event.getPlayer());
        });



    }
}

package me.matthewe.chatfilter;

import me.matthewedevelopment.atheriallib.config.BukkitConfig;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Matthew E on 11/30/2023 at 8:44 PM for the project ChatFilter
 */
public class ChatFilterConfig {
    private ChatFilter chatFilter;
    private BukkitConfig config;
    private boolean enableBypass;
    private DecimalFormat chanceFormat;

    public DecimalFormat getChanceFormat() {
        return chanceFormat;
    }

    public ChatFilterMessages getMessages() {
        return messages;
    }

    private WebhookSettings webhookSettings;
    private ChatFilterMessages messages;

    private List<String> punishmentList;
    private Map<String, BannedWord> wordMap;

    public static class BannedWord {
        public BannedWord(String text, double chance) {
            this.text = text;
            this.chance = chance;
        }

        private String text;
        private double chance;

        public String getText() {
            return text;
        }

        public double getChance() {
            return chance;
        }
    }
    public ChatFilterConfig(ChatFilter chatFilter) {
        this.chatFilter = chatFilter;
    }
    public void reloadConfig(boolean reload) {
        this.chatFilter.getLogger().info(reload ? "Reloading config..." : "Loading config...");
        this.config = new BukkitConfig("config.yml", this.chatFilter);
        FileConfiguration configuration = this.config.getConfiguration();

        ConfigurationSection webHookSection = configuration.getConfigurationSection("webHook");

        String url = webHookSection.getString("url");
        WebhookSettings.WebhookSettingsEmbed embed = new WebhookSettings.WebhookSettingsEmbed(webHookSection.getString("embed.title"), webHookSection.getInt("embed.color"), webHookSection.getStringList("embed.body"));
        this.webhookSettings = new WebhookSettings(url, embed);


        this.chanceFormat = new DecimalFormat(configuration.getString("chanceFormat"));
        this.enableBypass =configuration.getBoolean("enableBypass");

        this.messages = new ChatFilterMessages(configuration.getString("messages.blocked"), configuration.getString("messages.view"));
        this.punishmentList =configuration.getStringList("punishment");

        this.wordMap = new HashMap<>();
        for (String word : configuration.getConfigurationSection("words").getKeys(false)) {
            double chance = (configuration.isDouble("words." + word) ? configuration.getDouble("words." + word) :
                    configuration.isInt("words." + word) ? configuration.getInt("words." + word) : -1);
            if (chance == -1) {
                this.chatFilter.getLogger().warning("Could not load word " + word + " due to configuration error!");
                continue;
            }
            BannedWord bannedWord = new BannedWord(word, chance);
            this.wordMap.put(word, bannedWord);
        }
    }

    public Map<String, BannedWord> getWordMap() {
        return wordMap;
    }

    public void executePunishment(Player player) {
        for (String s : this.punishmentList) {
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),new String(s).replaceAll("%player%", player.getName()));
        }
    }

    public boolean isEnableBypass() {
        return enableBypass;
    }

    public WebhookSettings getWebhookSettings() {
        return webhookSettings;
    }

    public static class ChatFilterMessages {
        public ChatFilterMessages(String blocked, String view) {
            this.blocked = blocked;
            this.view = view;
        }

        private String blocked;
        private String view;

        public String getBlocked() {
            return blocked;
        }

        public String getView() {
            return view;
        }
    }

    public static class WebhookSettings {
        private String url;
        private WebhookSettingsEmbed embed;

        public WebhookSettings(String url, WebhookSettingsEmbed embed) {
            this.url = url;
            this.embed = embed;
        }

        public String getUrl() {
            return url;
        }

        public WebhookSettingsEmbed getEmbed() {
            return embed;
        }

        public static class WebhookSettingsEmbed {
            private String title;
            private int color;
            private List<String> body;

            public WebhookSettingsEmbed(String title, int color, List<String> body) {
                this.title = title;
                this.color = color;
                this.body = body;
            }

            public String getTitle() {
                return title;
            }

            public int getColor() {
                return color;
            }

            public List<String> getBody() {
                return body;
            }
        }
    }
}

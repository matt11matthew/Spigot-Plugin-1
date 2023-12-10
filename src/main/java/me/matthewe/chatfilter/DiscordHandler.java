package me.matthewe.chatfilter;


import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;

import java.util.List;
import java.util.UUID;

/**
 * Created by Matthew E on 11/30/2023 at 8:48 PM for the project ChatFilter
 */
public class DiscordHandler {
    private ChatFilter chatFilter;
    private boolean enabled = true;

    private WebhookClient client;
    public DiscordHandler(ChatFilter chatFilter) {
        this.chatFilter = chatFilter;
    }

    public void startWebhook() {

        this.chatFilter.getLogger().info("Loading webhook....");
        if (this.chatFilter.getChatFilterConfig().getWebhookSettings().getUrl().isEmpty()){
            enabled = false;
        }


       try {
           WebhookClientBuilder builder = new WebhookClientBuilder(this.chatFilter.getChatFilterConfig().getWebhookSettings().getUrl()); // or id, token

           this.client = builder.build();
       } catch (Exception e) {
           enabled = false;
           this.chatFilter.getLogger().severe("Could not load webhook");
           e.printStackTrace();
       }

    }


    public void handleStop(){
        if (!isEnabled())return;
        this.client.close();
    }

    public void sendFilterMessage(UUID uuid, String username, String word, double roll) {
        if (!isEnabled())return;
        if (this.client==null||this.client.isShutdown())return;
        String url = "https://crafatar.com/avatars/" + uuid.toString().replaceAll("-", "") + "?size=100t=MHF_Steve&overlay";

        ChatFilterConfig config = this.chatFilter.getChatFilterConfig();
        ChatFilterConfig.WebhookSettings settings = config.getWebhookSettings();

        StringBuilder body = new StringBuilder();
        List<String> strings = settings.getEmbed().getBody();
        for (int i = 0; i < strings.size(); i++) {
            String s = strings.get(i);
            body = body.append(new String(s).replaceAll("%username%", username)
                    .replaceAll("%word%", word)
                    .replaceAll("%player%", username)
                    .replaceAll("%probability%", config.getChanceFormat().format(roll)));
            if (i < strings.size()-1) {
                body.append("\n");
            }
        }

        WebhookEmbed embed = new WebhookEmbedBuilder()
                .setColor(settings.getEmbed().getColor())
                .setTitle(new WebhookEmbed.EmbedTitle(new String(settings.getEmbed().getTitle())
                        .replaceAll("%username%", username)
                        .replaceAll("%player%", username)
                        .replaceAll("%word%", word)
                        .replaceAll("%probability%", config.getChanceFormat().format(roll)), null))
                .setThumbnailUrl(url)
                .setDescription(body.toString())
                .build();
        this.client.send(embed).whenCompleteAsync((readonlyMessage, throwable) -> {

        });
    }

    public boolean isEnabled() {
        return enabled;
    }
}

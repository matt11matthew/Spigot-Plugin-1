package me.matthewe.chatfilter;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Matthew E on 11/30/2023 at 9:25 PM for the project ChatFilter
 */
public class BannedWordsUtils {

    private static String createRegex(Map<String, ChatFilterConfig.BannedWord> map) {
        StringBuilder regexBuilder = new StringBuilder("\\b(?:");
        boolean first = true;
        for (String word : map.keySet()) {
            if (!first) {
                regexBuilder.append("|");
            }
            regexBuilder.append(Pattern.quote(word));
            first = false;
        }
        regexBuilder.append(")\\b");
        return regexBuilder.toString();
    }

    public static class FoundBannedWord {
        private ChatFilterConfig.BannedWord bannedWord;
        private double roll;
        private boolean skip;

        public FoundBannedWord(ChatFilterConfig.BannedWord bannedWord, double roll, boolean skip) {
            this.bannedWord = bannedWord;
            this.roll = roll;
            this.skip = skip;
        }

        public boolean isSkip() {
            return skip;
        }

        public ChatFilterConfig.BannedWord getBannedWord() {
            return bannedWord;
        }

        public double getRoll() {
            return roll;
        }
    }

    public static List<FoundBannedWord> findBannedWordsInMessage(String message, Map<String, ChatFilterConfig.BannedWord> map) {
        List<FoundBannedWord> bannedWords = new ArrayList<>();
        String regex = createRegex(map);
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(message);

        while (matcher.find()) {
            String foundWord = matcher.group();
            ChatFilterConfig.BannedWord bannedWord = map.get(foundWord);
            if (bannedWord != null) {
                double chance = bannedWord.getChance();
                double random = Math.random() * 100;
                if (chance == 100) {
                    bannedWords.add(new FoundBannedWord(bannedWord, random, false));
                } else if (random < chance) {
                    bannedWords.add(new FoundBannedWord(bannedWord, random, false));
                } else {
                    bannedWords.add(new FoundBannedWord(bannedWord, random, true));
                }
            }
        }

        return bannedWords;
    }
}

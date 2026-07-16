package com.d0cctor.d0cctors_lives.client;

import com.d0cctor.d0cctors_lives.network.LivesSyncPayload;
import com.d0cctor.d0cctors_lives.network.LivesAllSyncPayload;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ClientLivesData {
    private static int lives = -1;
    private static int maxLives = 4;
    private static boolean outOfLives = false;
    private static boolean inLimbo = false;
    private static final LinkedHashMap<String, Integer> allLives = new LinkedHashMap<>();

    private ClientLivesData() {}

    public static void update(LivesSyncPayload payload) {
        lives = payload.lives();
        maxLives = payload.maxLives();
        outOfLives = payload.outOfLives();
        inLimbo = payload.inLimbo();
    }

    public static void updateAll(LivesAllSyncPayload payload) {
        allLives.clear();
        int size = Math.min(payload.names().size(), payload.lives().size());
        for (int i = 0; i < size; i++) {
            allLives.put(payload.names().get(i), payload.lives().get(i));
        }
        maxLives = payload.maxLives();
    }

    public static Map<String, Integer> allLives() {
        return allLives;
    }

    public static int lives() {
        return lives;
    }

    public static int maxLives() {
        return Math.max(1, maxLives);
    }

    public static boolean hasData() {
        return lives >= 0;
    }

    public static boolean outOfLives() {
        return outOfLives;
    }

    public static boolean inLimbo() {
        return inLimbo;
    }
}

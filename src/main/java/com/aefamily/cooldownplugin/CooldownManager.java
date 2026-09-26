package com.aefamily.cooldownplugin;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {

    private final Map<UUID, Long> lastBoostTime    = new HashMap<>();
    private final Map<UUID, Long> lastLeverTime    = new HashMap<>();
    private final Map<UUID, Long> lastRedstoneTime = new HashMap<>();

    /**
     * Kiểm tra và cập nhật cooldown cho hành động boost (firework/elytra/riptide).
     * @param player Player cần kiểm tra.
     * @param cooldownMillis Thời gian chờ (millis).
     * @return 0 nếu không còn cooldown, hoặc số millis còn lại nếu vẫn còn cooldown.
     */
    public long checkAndSetBoost(Player player, long cooldownMillis) {
        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();

        if (!lastBoostTime.containsKey(uuid) || now - lastBoostTime.get(uuid) >= cooldownMillis) {
            lastBoostTime.put(uuid, now);
            return 0L;
        } else {
            long elapsed = now - lastBoostTime.get(uuid);
            return cooldownMillis - elapsed;
        }
    }

    /**
     * Kiểm tra và cập nhật cooldown cho hành động lever.
     * @param player Player cần kiểm tra.
     * @param cooldownMillis Thời gian chờ (millis).
     * @return 0 nếu không còn cooldown, hoặc số millis còn lại nếu vẫn còn cooldown.
     */
    public long checkAndSetLever(Player player, long cooldownMillis) {
        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();

        if (!lastLeverTime.containsKey(uuid) || now - lastLeverTime.get(uuid) >= cooldownMillis) {
            lastLeverTime.put(uuid, now);
            return 0L;
        } else {
            long elapsed = now - lastLeverTime.get(uuid);
            return cooldownMillis - elapsed;
        }
    }

    /**
     * Kiểm tra và cập nhật cooldown cho hành động đặt Redstone Dust.
     * @param player Player cần kiểm tra.
     * @param cooldownMillis Thời gian chờ (millis).
     * @return 0 nếu không còn cooldown, hoặc số millis còn lại nếu vẫn còn cooldown.
     */
    public long checkAndSetRedstone(Player player, long cooldownMillis) {
        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();

        if (!lastRedstoneTime.containsKey(uuid) || now - lastRedstoneTime.get(uuid) >= cooldownMillis) {
            lastRedstoneTime.put(uuid, now);
            return 0L;
        } else {
            long elapsed = now - lastRedstoneTime.get(uuid);
            return cooldownMillis - elapsed;
        }
    }
}

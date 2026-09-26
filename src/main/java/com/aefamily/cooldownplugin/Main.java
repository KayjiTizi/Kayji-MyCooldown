package com.aefamily.cooldownplugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.BoundingBox;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("ALL")
public class Main extends JavaPlugin implements Listener {

    private CooldownManager cooldownManager;

    // BOOST
    private boolean boostEnabled;
    private long boostCooldownMillis;
    private String boostMsg;

    // LEVER
    private boolean leverEnabled;
    private long leverCooldownMillis;
    private String leverMsg;

    // REDSTONE
    private boolean redstoneEnabled;
    private long redstoneCooldownMillis;
    private String redstoneMsg;

    // ARMORSTAND
    private boolean armorstandEnabled;
    private int armorstandRadiusChunks;
    private int armorstandLimit;
    private String armorstandMsg;

    // MINECART
    private boolean minecartEnabled;
    private int minecartRadiusChunks;
    private int minecartLimit;
    private String minecartMsg;

    @Override
    public void onEnable() {
        // Load (tạo mới nếu chưa có) config
        saveDefaultConfig();
        loadConfigValues();

        cooldownManager = new CooldownManager();

        // Đăng ký listener
        Bukkit.getPluginManager().registerEvents(this, this);

        // Đăng ký command và tab completer cho "/cooldownplugin" (alias "/cdp")
        Objects.requireNonNull(this.getCommand("cooldownplugin")).setExecutor(this);
        Objects.requireNonNull(this.getCommand("cooldownplugin")).setTabCompleter(new ReloadTabCompleter());

        getLogger().info("MyCooldownPlugin đã được kích hoạt!");
    }

    @Override
    public void onDisable() {
        getLogger().info("MyCooldownPlugin đã tắt!");
    }

    /**
     * Đọc và gán giá trị từ config.yml vào biến trong plugin.
     */
    private void loadConfigValues() {
        FileConfiguration cfg = getConfig();

        // ===== BOOST =====
        boolean fallbackBoostEnabled = cfg.getBoolean("features.boost-enabled", true);
        boostEnabled = cfg.getBoolean("boost.enabled", fallbackBoostEnabled);
        int boostSec = cfg.getInt("boost.cooldown-seconds", 5);
        boostCooldownMillis = boostSec * 1000L;
        boostMsg = cfg.getString("boost.cooldown-message", "&cBạn phải chờ &6{time}&c giây nữa mới được boost bay lại.");

        // ===== LEVER =====
        boolean fallbackLeverEnabled = cfg.getBoolean("features.lever-enabled", true);
        leverEnabled = cfg.getBoolean("lever.enabled", fallbackLeverEnabled);
        int leverSec = cfg.getInt("lever.cooldown-seconds", 2);
        leverCooldownMillis = leverSec * 1000L;
        leverMsg = cfg.getString("lever.cooldown-message", "&cBạn phải chờ &6{time}&c giây nữa mới được kích hoạt lever.");

        // ===== REDSTONE =====
        redstoneEnabled = cfg.getBoolean("redstone.enabled", true);
        int redstoneSec = cfg.getInt("redstone.cooldown-seconds", 3);
        redstoneCooldownMillis = redstoneSec * 1000L;
        redstoneMsg = cfg.getString("redstone.cooldown-message", "&cBạn đang đặt Redstone quá nhanh, hãy chờ &6{time}&c giây nữa.");

        // ===== ARMORSTAND =====
        armorstandEnabled = cfg.getBoolean("armorstand.enabled", true);
        armorstandRadiusChunks = cfg.getInt("armorstand.radius-chunks", 32);
        armorstandLimit = cfg.getInt("armorstand.limit", 50);
        armorstandMsg = cfg.getString("armorstand.limit-message", "&cKhu vực này đã có &6{count}&c ArmorStand (cho phép tối đa {limit}). Không thể đặt thêm.");

        // ===== MINECART =====
        minecartEnabled = cfg.getBoolean("minecart.enabled", true);
        minecartRadiusChunks = cfg.getInt("minecart.radius-chunks", 32);
        minecartLimit = cfg.getInt("minecart.limit", 30);
        minecartMsg = cfg.getString("minecart.limit-message", "&cKhu vực này đã có &6{count}&c Minecart (cho phép tối đa {limit}). Không thể đặt thêm.");
    }

    /**
     * Xử lý lệnh "/cooldownplugin reload" (alias "/cdp reload")
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("cooldownplugin")) {
            if (args.length == 0 || !args[0].equalsIgnoreCase("reload")) {
                sender.sendMessage(ChatColor.YELLOW + "Sử dụng: /" + label + " reload");
                return true;
            }

            // args[0] == "reload"
            if (!sender.hasPermission("cooldownplugin.reload")) {
                sender.sendMessage(ChatColor.RED + "Bạn không có quyền thực hiện lệnh này.");
                return true;
            }

            // reload config
            reloadConfig();
            loadConfigValues();
            sender.sendMessage(ChatColor.GREEN + "§aĐã tải lại config của MyCooldownPlugin!");
            getLogger().info("Config đã được reload bởi " + sender.getName());
            return true;
        }
        return false;
    }

    /**
     * TabCompleter để gợi ý "reload" khi gõ "/cdp "
     */
    private static class ReloadTabCompleter implements TabCompleter {
        @Override
        public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
            if (args.length == 1) {
                return Arrays.asList("reload").stream()
                        .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                        .toList();
            }
            return new ArrayList<>();
        }
    }

    /**
     * Xử lý các event PlayerInteractEvent để:
     * 1. Block Boost (Firework/Elytra & Riptide) với cooldown.
     * 2. Block Lever spam với cooldown.
     * 3. Block Redstone Dust spam với cooldown.
     * 4. Kiểm tra giới hạn ArmorStand.
     * 5. Kiểm tra giới hạn Minecart.
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Chỉ xử lý tay chính (MAIN_HAND) để không bị gọi 2 lần
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        Action action = event.getAction();

        // -----------------------------------
        // 1. BOOST (Firework + Elytra)
        // -----------------------------------
        if (boostEnabled
                && (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)
                && item != null
                && item.getType() == Material.FIREWORK_ROCKET
                && player.isGliding()) {

            long left = cooldownManager.checkAndSetBoost(player, boostCooldownMillis);
            if (left > 0) {
                event.setCancelled(true);
                int leftSeconds = (int) Math.ceil(left / 1000.0);
                String msg = ChatColor.translateAlternateColorCodes('&',
                        boostMsg.replace("{time}", String.valueOf(leftSeconds)));
                player.sendMessage(msg);
            }
            return;
        }

        // -----------------------------------
        // 2. BOOST (Riptide Trident)
        // -----------------------------------
        if (boostEnabled
                && (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)
                && item != null
                && item.getType() == Material.TRIDENT
                && item.containsEnchantment(org.bukkit.enchantments.Enchantment.RIPTIDE)) {

            long left = cooldownManager.checkAndSetBoost(player, boostCooldownMillis);
            if (left > 0) {
                event.setCancelled(true);
                int leftSeconds = (int) Math.ceil(left / 1000.0);
                String msg = ChatColor.translateAlternateColorCodes('&',
                        boostMsg.replace("{time}", String.valueOf(leftSeconds)));
                player.sendMessage(msg);
            }
            return;
        }

        // -----------------------------------
        // 3. LEVER spam
        // -----------------------------------
        if (leverEnabled
                && action == Action.RIGHT_CLICK_BLOCK
                && event.getClickedBlock() != null
                && event.getClickedBlock().getType() == Material.LEVER) {

            long left = cooldownManager.checkAndSetLever(player, leverCooldownMillis);
            if (left > 0) {
                event.setCancelled(true);
                int leftSeconds = (int) Math.ceil(left / 1000.0);
                String msg = ChatColor.translateAlternateColorCodes('&',
                        leverMsg.replace("{time}", String.valueOf(leftSeconds)));
                player.sendMessage(msg);
            }
            return;
        }

        // -----------------------------------
        // 4. REDSTONE Dust spam
        // -----------------------------------
        if (redstoneEnabled
                && (action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR)
                && item != null
                && (item.getType() == Material.REDSTONE || item.getType() == Material.REDSTONE_WIRE)) {

            long left = cooldownManager.checkAndSetRedstone(player, redstoneCooldownMillis);
            if (left > 0) {
                event.setCancelled(true);
                int leftSeconds = (int) Math.ceil(left / 1000.0);
                String msg = ChatColor.translateAlternateColorCodes('&',
                        redstoneMsg.replace("{time}", String.valueOf(leftSeconds)));
                player.sendMessage(msg);
            }
            return;
        }

        // -----------------------------------
        // 5. ARMORSTAND limit
        // -----------------------------------
        if (armorstandEnabled
                && action == Action.RIGHT_CLICK_BLOCK
                && item != null
                && item.getType() == Material.ARMOR_STAND) {

            double radiusBlocks = armorstandRadiusChunks * 16.0;
            BoundingBox box = BoundingBox.of(player.getLocation(), radiusBlocks, radiusBlocks, radiusBlocks);

            // Đếm số ArmorStand trong vùng
            int count = 0;
            for (Entity e : player.getWorld().getNearbyEntities(player.getLocation(), radiusBlocks, radiusBlocks, radiusBlocks)) {
                if (e instanceof ArmorStand) {
                    // Kiểm tra xem ArmorStand đó có nằm trong BoundingBox hay không
                    if (box.contains(e.getLocation().toVector())) {
                        count++;
                    }
                }
            }

            if (count >= armorstandLimit) {
                event.setCancelled(true);
                String msg = ChatColor.translateAlternateColorCodes('&',
                        armorstandMsg
                                .replace("{count}", String.valueOf(count))
                                .replace("{limit}", String.valueOf(armorstandLimit)));
                player.sendMessage(msg);
            }
            return;
        }

// -----------------------------------
// 6. MINECART limit
// -----------------------------------
        if (minecartEnabled
                && action == Action.RIGHT_CLICK_BLOCK
                && item != null
                && (item.getType() == Material.MINECART
                || item.getType() == Material.CHEST_MINECART
                || item.getType() == Material.FURNACE_MINECART
                || item.getType() == Material.HOPPER_MINECART
                || item.getType() == Material.TNT_MINECART
                || item.getType() == Material.COMMAND_BLOCK_MINECART)) {

            double radiusBlocks = minecartRadiusChunks * 16.0;
            BoundingBox box = BoundingBox.of(player.getLocation(), radiusBlocks, radiusBlocks, radiusBlocks);

            int count = 0;
            for (Entity e : player.getWorld().getNearbyEntities(player.getLocation(), radiusBlocks, radiusBlocks, radiusBlocks)) {
                if (e instanceof Minecart) {
                    if (box.contains(e.getLocation().toVector())) {
                        count++;
                    }
                }
            }

            if (count >= minecartLimit) {
                event.setCancelled(true);
                String msg = ChatColor.translateAlternateColorCodes('&',
                        minecartMsg
                                .replace("{count}", String.valueOf(count))
                                .replace("{limit}", String.valueOf(minecartLimit)));
                player.sendMessage(msg);
            }
        }
    }
}

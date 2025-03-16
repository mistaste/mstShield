package ua.mistaste.mstshield;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Main extends JavaPlugin implements org.bukkit.event.Listener, CommandExecutor {
    private Map<Material, ItemConfig> itemConfigs;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfig();
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("mstshield").setExecutor(this);

        getLogger().info("\n"
                + "███╗   ███╗███████╗████████╗███████╗██╗  ██╗██╗███████╗██╗     ██████╗\n"
                + "████╗ ████║██╔════╝╚══██╔══╝██╔════╝██║  ██║██║██╔════╝██║     ██╔══██╗\n"
                + "██╔████╔██║███████╗   ██║   ███████╗███████║██║█████╗  ██║     ██║  ██║\n"
                + "██║╚██╔╝██║╚════██║   ██║   ╚════██║██╔══██║██║██╔══╝  ██║     ██║  ██║\n"
                + "██║ ╚═╝ ██║███████║   ██║   ███████║██║  ██║██║███████╗███████╗██████╔╝\n"
                + "╚═╝     ╚═╝╚══════╝   ╚═╝   ╚══════╝╚═╝  ╚═╝╚═╝╚══════╝╚══════╝╚═════╝ \n"
                + "Author: mistaste    Version: "+getDescription().getVersion()+"    Github: https://github.com/mistaste/MSTShield"
        );
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("mstshield")) {
            if (!sender.hasPermission("mstshield.use")) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        getConfig().getString("messages.no-permission", "&cУ вас нет прав для использования этой команды!")));
                return true;
            }

            if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                loadConfig();
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        getConfig().getString("messages.reload", "&aКонфигурация успешно перезагружена!")));
                return true;
            }
        }
        return false;
    }

    private void loadConfig() {
        reloadConfig();
        FileConfiguration config = getConfig();
        itemConfigs = new HashMap<>();

        List<Map<?, ?>> itemsList = config.getMapList("items");

        for (Map<?, ?> item : itemsList) {
            try {
                String materialName = (String) item.get("material");
                Object cooldownObj = item.get("cooldown");

                if (materialName == null) {
                    getLogger().warning("Предмет без материала: " + item);
                    continue;
                }

                if (cooldownObj == null) {
                    getLogger().warning("Предмету " + materialName + " не установлен кулдаун!");
                    continue;
                }

                double cooldown;
                try {
                    cooldown = Double.parseDouble(cooldownObj.toString());
                } catch (NumberFormatException e) {
                    getLogger().warning("Некорректное значение cooldown для предмета " + materialName + ": " + cooldownObj);
                    continue;
                }

                Material material = Material.valueOf(materialName.toUpperCase());
                if (itemConfigs.containsKey(material)) {
                    getLogger().warning("Материал дублируется в кфг: " + materialName);
                }

                itemConfigs.put(material, new ItemConfig(cooldown));

            } catch (IllegalArgumentException e) {
                getLogger().warning("Неверное название материала: " + item.get("material"));
            } catch (Exception e) {
                getLogger().warning("Ошибка обработки предмета: " + item + " | Ошибка: " + e.getMessage());
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        if (!(event.getEntity() instanceof Player)) return;

        Player attacker = (Player) event.getDamager();
        Player target = (Player) event.getEntity();

        if (attacker.hasPermission("mstshield.bypass")) {
            return;
        }

        ItemStack item = attacker.getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR) {
            return;
        }

        ItemConfig itemConfig = itemConfigs.getOrDefault(item.getType(),
                new ItemConfig(0));

        if (!itemConfigs.containsKey(item.getType())) {
            return;
        }

        if (target.isBlocking()) {
            int ticks = itemConfig.getCooldownInTicks();
            target.setCooldown(Material.SHIELD, ticks);
            ItemStack[] savedShields = removeAndSaveShield(target);

            Bukkit.getScheduler().runTaskLater(this, () -> {
                restoreShields(target, savedShields);
            }, 5L);
        }
    }

    public ItemStack[] removeAndSaveShield(Player player) {
        ItemStack[] savedShields = new ItemStack[2];
        PlayerInventory inventory = player.getInventory();

        ItemStack mainHand = inventory.getItemInMainHand();
        if (mainHand.getType() == Material.SHIELD) {
            savedShields[0] = mainHand.clone();
            inventory.setItemInMainHand(new ItemStack(Material.AIR));
        }

        ItemStack offHand = inventory.getItemInOffHand();
        if (offHand.getType() == Material.SHIELD) {
            savedShields[1] = offHand.clone();
            inventory.setItemInOffHand(new ItemStack(Material.AIR));
        }

        player.updateInventory();
        return savedShields;
    }

    public void restoreShields(Player player, ItemStack[] shields) {
        PlayerInventory inventory = player.getInventory();

        if (shields[0] != null && inventory.getItemInMainHand().getType() == Material.AIR) {
            inventory.setItemInMainHand(shields[0]);
        }

        if (shields[1] != null && inventory.getItemInOffHand().getType() == Material.AIR) {
            inventory.setItemInOffHand(shields[1]);
        }

        player.updateInventory();
    }
}
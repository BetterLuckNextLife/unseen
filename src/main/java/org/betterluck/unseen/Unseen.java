package org.betterluck.unseen;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public final class Unseen extends JavaPlugin implements Listener, TabExecutor {

    private static final String PERM_RELOAD = "unseen.reload";
    private static final String PERM_SEE_HIDDEN = "unseen.seehidden";

    private final Set<UUID> notifiedInvisible = new HashSet<>();

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);

        if (getCommand("unseen") != null) {
            getCommand("unseen").setExecutor(this);
            getCommand("unseen").setTabCompleter(this);
        }

        Bukkit.getScheduler().runTask(this, this::refreshAllPlayers);
    }

    @Override
    public void onDisable() {
        for (Player target : Bukkit.getOnlinePlayers()) {
            showTargetToEveryone(target);
        }

        notifiedInvisible.clear();
    }

    @EventHandler
    public void onPotionEffectChange(EntityPotionEffectEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!PotionEffectType.INVISIBILITY.equals(event.getModifiedType())) return;

        Bukkit.getScheduler().runTask(this, () -> updatePlayerState(player));
        Bukkit.getScheduler().runTaskLater(this, () -> updatePlayerState(player), 2L);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player joined = event.getPlayer();

        if (joined.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
            event.joinMessage(null);
        }

        Bukkit.getScheduler().runTask(this, () -> {
            updatePlayerState(joined);
            refreshViewer(joined);
        });

        Bukkit.getScheduler().runTaskLater(this, () -> {
            updatePlayerState(joined);
            refreshViewer(joined);
        }, 5L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
            event.quitMessage(null);
        }

        notifiedInvisible.remove(player.getUniqueId());
    }

    private void refreshAllPlayers() {
        notifiedInvisible.clear();

        for (Player target : Bukkit.getOnlinePlayers()) {
            showTargetToEveryone(target);
        }

        for (Player target : Bukkit.getOnlinePlayers()) {
            updatePlayerState(target);
        }
    }

    private void refreshViewer(Player viewer) {
        if (!viewer.isOnline()) return;

        for (Player target : Bukkit.getOnlinePlayers()) {
            if (target.equals(viewer)) continue;

            if (shouldHideFrom(viewer, target)) {
                viewer.unlistPlayer(target);
            } else {
                viewer.listPlayer(target);
            }
        }
    }

    private void updatePlayerState(Player target) {
        if (!target.isOnline()) return;

        if (target.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
            if (notifiedInvisible.add(target.getUniqueId())) {
                target.sendMessage(Component.text(
                        "You are invisible. Players can't see you in tab, and your join/quit messages are suppressed.",
                        NamedTextColor.AQUA
                ));
            }

            hideTargetFromNonAdmins(target);
        } else {
            notifiedInvisible.remove(target.getUniqueId());
            showTargetToEveryone(target);
        }
    }

    private void hideTargetFromNonAdmins(Player target) {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (viewer.equals(target)) continue;

            if (shouldHideFrom(viewer, target)) {
                viewer.unlistPlayer(target);
            } else {
                viewer.listPlayer(target);
            }
        }
    }

    private void showTargetToEveryone(Player target) {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (viewer.equals(target)) continue;
            viewer.listPlayer(target);
        }
    }

    private boolean shouldHideFrom(Player viewer, Player target) {
        return target.hasPotionEffect(PotionEffectType.INVISIBILITY)
                && !viewer.hasPermission(PERM_SEE_HIDDEN);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("unseen")) {
            return false;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission(PERM_RELOAD)) {
                sender.sendMessage(Component.text("You do not have permission.", NamedTextColor.AQUA));
                return true;
            }

            refreshAllPlayers();
            sender.sendMessage(Component.text("Unseen refreshed.", NamedTextColor.AQUA));
            return true;
        }

        sender.sendMessage(Component.text("Usage: /unseen reload", NamedTextColor.AQUA));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!command.getName().equalsIgnoreCase("unseen")) {
            return List.of();
        }

        if (args.length == 1) {
            String input = args[0].toLowerCase(Locale.ROOT);
            return "reload".startsWith(input) ? List.of("reload") : List.of();
        }

        return List.of();
    }
}
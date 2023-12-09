package io.github.tanguygab.takeashowergamers;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

public final class TakeAShowerGamers extends JavaPlugin {

    public final List<UUID> didntShower = new ArrayList<>();
    public final Map<UUID, LocalDateTime> lastShowers = new HashMap<>();
    public final Map<UUID, LocalDateTime> leftAt = new HashMap<>();

    private int minutesDelay;
    private String sound;
    private final List<String> commands = new ArrayList<>();
    private final List<String> commandsOnShower = new ArrayList<>();

    public LocalDateTime now() {
        return LocalDateTime.now();
    }
    public void clean(UUID uuid) {
        didntShower.remove(uuid);
        lastShowers.put(uuid,now());
        leftAt.remove(uuid);
    }
    private void runCommands(Player player, List<String> commands) {
        commands.forEach(cmd->getServer().dispatchCommand(getServer().getConsoleSender(),color(PlaceholderAPI.setPlaceholders(player,cmd))));
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadConfig();

        minutesDelay = getConfig().getInt("minutes-delay",15);
        sound = getConfig().getString("sound", "BLOCK_NOTE_BLOCK_PLING");

        commands.addAll(getConfig().getStringList("commands"));
        commandsOnShower.addAll(getConfig().getStringList("commands-on-shower"));

        getServer().getScheduler().scheduleSyncRepeatingTask(this,()->
                getServer().getOnlinePlayers().forEach(player->{
            UUID uuid = player.getUniqueId();
            if (player.isInWater()) {
                if (didntShower.contains(uuid)) {
                    msg(player,"&aYou took a shower and washed off that gamer smell! Now go &2outside &asmh...");
                    runCommands(player,commandsOnShower);
                }
                clean(uuid);
                return;
            }

            if (getLastShower(uuid) < minutesDelay || didntShower.contains(uuid)) return;

            msg(player,"&cYou gotta take a shower man!");
            if (sound != null && !sound.isEmpty()) player.playSound(player.getLocation(),sound,1,1);
            gamerNeedsAShower(player,10);


        }),0,1200);
    }

    private String color(String text) {
        return ChatColor.translateAlternateColorCodes('&',text);
    }
    private void msg(CommandSender sender, String msg) {
        sender.sendMessage(color(msg));
    }

    private void gamerNeedsAShower(Player player, int secondsLeft) {
        getServer().getScheduler().runTaskLater(this,()->{
            if (player == null || !player.isOnline()) return;
            if (player.isInWater()) {
                clean(player.getUniqueId());
                msg(player,"&aOmg, da gamer took da shower! Now go touch &2grass&a...");
                return;
            }
            if (secondsLeft <= 0) {
                msg(player,"&4You have been cursed by the Shower God!");
                didntShower.add(player.getUniqueId());
                runCommands(player,commands);
                return;
            }
            msg(player,"&cYou have "+secondsLeft+" seconds left to take a shower");
            gamerNeedsAShower(player, secondsLeft == 10 ? 5 : secondsLeft-1);
        },secondsLeft == 5 ? 100 : 20);
    }

    private long getLastShower(UUID uuid) {
        if (!lastShowers.containsKey(uuid)) {
            clean(uuid);
            return 0;
        }
        LocalDateTime lastShower = lastShowers.get(uuid);

        if (leftAt.containsKey(uuid)) {
            LocalDateTime leftAtTime = leftAt.get(uuid);
            return ChronoUnit.SECONDS.between(lastShower, leftAtTime)
                    +ChronoUnit.SECONDS.between(leftAtTime, now());
        }
        return ChronoUnit.SECONDS.between(lastShower, now());
    }

    @Override
    public void onDisable() {
        lastShowers.clear();
        leftAt.clear();
        didntShower.clear();
        commands.clear();
        commandsOnShower.clear();
        getServer().getScheduler().cancelTasks(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String arg = args.length == 0 ? "" : args[0];

        switch (arg) {
            case "reload" -> {
                onDisable();
                onEnable();
                msg(sender,"&aPlugin reloaded!");
            }
            case "set" -> {
                if (args.length < 2) {
                    msg(sender,"&cYou have to specify a player!");
                    break;
                }
                Player player = getServer().getPlayerExact(args[1]);
                if (player == null) {
                    msg(sender,"&cCould not find that player!");
                    break;
                }
                try {
                    int time = args.length < 3 ? 0 : Integer.parseInt(args[2]);
                    lastShowers.put(player.getUniqueId(),now().minusMinutes(time));
                } catch (Exception e) {msg(sender,"&cInvalid number!");}
            }
            default -> msg(sender,"&6&m                                        \n"
                    + "&6[TakeAShowerGamers] &7" + getDescription().getVersion() + "\n"
                    + " &7- &6/shower\n"
                    + "   &8| &eThis help page\n"
                    + " &7- &6/shower reload\n"
                    + "   &8| &eReload the plugin\n"
                    + " &7- &6/shower set <player> [minutes]\n"
                    + "   &8| &eSet a player's last shower delay or reset it\n"
                    + "&6&m                                        ");
        }

        return true;
    }
}

package io.github.tanguygab.takeashowergamers;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class TakeAShowerGamers extends JavaPlugin {

    private final int delay = 15;
    private final Map<UUID, LocalDateTime> lastShowers = new HashMap<>();

    private LocalDateTime now() {
        return LocalDateTime.now();
    }

    @Override
    public void onEnable() {
        getServer().getScheduler().scheduleSyncRepeatingTask(this,()->{
            getServer().getOnlinePlayers().forEach(player->{
                if (player.isInWater()) {
                    lastShowers.put(player.getUniqueId(),now());

                    if (player.hasPotionEffect(PotionEffectType.UNLUCK)) {
                        msg(player,"&aYou took a shower and washed off that gamer smell! Now go &2outside &asmh...");
                        player.removePotionEffect(PotionEffectType.UNLUCK);
                    }
                    return;
                }

                if (getLastShower(player) < delay) return;

                msg(player,"&cYou gotta take a shower man!");
                gamerNeedsAShower(player,10);


            });

        },0,1200);
    }

    private void msg(Player player, String msg) {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&',msg));
    }

    private void gamerNeedsAShower(Player player, int secondsLeft) {
        getServer().getScheduler().runTaskLater(this,()->{
            if (player.isInWater()) {
                lastShowers.put(player.getUniqueId(),now());
                msg(player,"&aOmg, da gamer took da shower! Now go touch &2grass&a...");
                return;
            }
            if (secondsLeft <= 0) {
                msg(player,"&4You have been cursed by the Shower God!");
                player.addPotionEffect(new PotionEffect(PotionEffectType.UNLUCK,-1,255,false));
                return;
            }
            msg(player,"&cYou have "+secondsLeft+" seconds left to take a shower");
            gamerNeedsAShower(player, secondsLeft == 10 ? 5 : secondsLeft-1);
        },secondsLeft == 5 ? 100 : 20);
    }

    private double getLastShower(Player player) {
        if (!lastShowers.containsKey(player.getUniqueId())) {
            lastShowers.put(player.getUniqueId(),now());
            return 0;
        }
        LocalDateTime lastShower = lastShowers.get(player.getUniqueId());
        return ChronoUnit.SECONDS.between(lastShower, now());
    }

    @Override
    public void onDisable() {
        lastShowers.clear();
    }

}

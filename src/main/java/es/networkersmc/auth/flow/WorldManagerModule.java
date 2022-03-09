package es.networkersmc.auth.flow;

import es.networkersmc.dendera.module.Module;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

import javax.inject.Inject;

public class WorldManagerModule implements Module, Listener {

    @Inject private Server server;

    @Override
    public void onStart() {
        Bukkit.setIgnorePlayerData(true);

        World world = server.getWorlds().get(0);
        world.setAutoSave(false);

        world.setFullTime(12000);
        world.setGameRuleValue("doDaylightCycle", "false");
        this.removeThunder(world);
    }

    private void removeThunder(World world) {
        world.setStorm(false);
        world.setThundering(false);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onWeather(WeatherChangeEvent event) {
        this.removeThunder(event.getWorld());
    }
}

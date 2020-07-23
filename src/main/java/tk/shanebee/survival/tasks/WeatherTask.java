package tk.shanebee.survival.tasks;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import tk.shanebee.survival.Survival;
import tk.shanebee.survival.config.Config;

public class WeatherTask extends BukkitRunnable {

    private final double baseSpeed;
    private final double rainSpeed;
    private final double stormSpeed;
    private final double snowSpeed;
    private final double snowstormSpeed;

    public WeatherTask(Survival plugin) {
        Config config = plugin.getSurvivalConfig();
        this.baseSpeed = config.MECHANICS_WEATHER_SPEED_BASE;
        this.rainSpeed = config.MECHANICS_WEATHER_SPEED_RAIN;
        this.stormSpeed = config.MECHANICS_WEATHER_SPEED_STORM;
        this.snowSpeed = config.MECHANICS_WEATHER_SPEED_SNOW;
        this.snowstormSpeed = config.MECHANICS_WEATHER_SPEED_SNOWSTORM;
        this.runTaskTimer(plugin, 20, 10);
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            handleWeather(player);
        }
    }

    private void handleWeather(Player player) {
        World world = player.getWorld();
        if (world.getEnvironment() == Environment.NORMAL) {
            if (isInSnowstorm(player)) {
                setWalkSpeed(player, snowstormSpeed);
            } else if (isOnSnow(player)) {
                setWalkSpeed(player, snowSpeed);
            } else if (isInStorm(player)) {
                setWalkSpeed(player, stormSpeed);
            } else if (isInRain(player)) {
                setWalkSpeed(player, rainSpeed);
            } else {
                setWalkSpeed(player, baseSpeed);
            }
        } else {
            setWalkSpeed(player, baseSpeed);
        }
    }

    private boolean isOnSnow(Player player) {
        Block block = player.getLocation().getBlock();
        if (block.getType() == Material.SNOW) {
            return true;
        } else return block.getType() == Material.AIR && block.getRelative(BlockFace.DOWN).getType() == Material.SNOW_BLOCK;
    }

    private boolean isInSnowstorm(Player player) {
        World world = player.getWorld();
        Location location = player.getLocation();
        double temp = location.getBlock().getTemperature();

        return world.hasStorm() && temp < 0.15 && isAtHighest(player) && isOnSnow(player);
    }

    private boolean isInRain(Player player) {
        World world = player.getWorld();
        Location location = player.getLocation();
        double temp = location.getBlock().getTemperature();

        // is raining (0.15 – 0.95 for rain)
        if (world.hasStorm() && temp >= 0.15 && temp <= 0.95) {
            // sky is above
            return isAtHighest(player);
        }
        return false;
    }

    private boolean isInStorm(Player player) {
        return isInRain(player) && player.getWorld().isThundering();
    }

    private boolean isAtHighest(Player player) {
        Location location = player.getLocation();
        World world = player.getWorld();
        return location.getY() > world.getHighestBlockAt(location).getY();
    }

    private void setWalkSpeed(Player player, double speed) {
        AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (attribute != null) {
            attribute.setBaseValue(speed);
        }
    }

}
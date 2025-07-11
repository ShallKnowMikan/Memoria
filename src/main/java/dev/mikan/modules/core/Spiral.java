package dev.mikan.modules.core;

import dev.mikan.packets.ParticleAPI;
import lombok.Getter;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import static java.lang.Math.*;

public class Spiral extends BukkitRunnable {

    private @Getter boolean started;
    private @Getter boolean stopped = false;
    private final Location startLoc;

    private double yValue = 0;
    private double thetaValue = 0;
    private final static int Y_LIMIT = 7;
    private final static double Y_INCREMENT = 0.05;
    private final static double THETA_INCREMENT = 5.0;
    private final static int TRAIL_LENGTH = 4;
    private final static double RADIUS = 2.85;
    private final double angleOffset;
    private final double maxHeight;
    private final double minHeight;
    private @Getter boolean rising = true;

    private final Map<Integer, Location> cache = new HashMap<>();
    private final Queue<Location> trailPoints = new ArrayDeque<>();

    public Spiral(Location startLoc, double angleOffset, double maxHeight, double minHeight) {
        this.startLoc = startLoc;
        this.angleOffset = angleOffset;
        this.maxHeight = maxHeight;
        this.minHeight = minHeight;
    }

    public void start() {
        if (isStarted()) return;
        started = true;
        this.runTaskTimer(CoreModule.instance().getPlugin().getBootstrap(), 10, 1);
    }

    public void stop(){
        this.stopped = true;
        this.cancel();
    }

    @Override
    public void run() {

        if (startLoc.getWorld().getPlayers().isEmpty() ||
                startLoc.getWorld().getPlayers().stream().noneMatch(player -> startLoc.distance(player.getLocation()) < 16)) return;

        if (yValue > maxHeight) rising = false;
        if (yValue < minHeight) rising = true;

        if (yValue > Y_LIMIT) {
            yValue = 0;
            trailPoints.clear();
        }
        if (thetaValue >= 360) thetaValue -= 360;

        Location currentPoint = calculatePoint(thetaValue, yValue);

        trailPoints.offer(currentPoint.clone());

        while (trailPoints.size() > TRAIL_LENGTH) {
            trailPoints.poll();
        }

        showParticles(currentPoint);

        thetaValue += THETA_INCREMENT;
        yValue = rising ? yValue + Y_INCREMENT : yValue - Y_INCREMENT;
    }

    private Location calculatePoint(double theta, double y) {
        double effectiveTheta = theta + angleOffset;
        int roundedTheta = (int) (effectiveTheta % 360);

        if (cache.containsKey(roundedTheta)) {
            Location cached = cache.get(roundedTheta).clone();
            cached.add(0, y, 0);
            return cached;
        }

        double radians = toRadians(effectiveTheta);
        double x = RADIUS * cos(radians);
        double z = RADIUS * sin(radians);

        Vector rotated = new Vector(x, 0, z);
        Location point = startLoc.clone().add(rotated).add(0, y, 0);

        Location horizontalPoint = startLoc.clone().add(rotated);
        cache.put(roundedTheta, horizontalPoint);

        return point;
    }

    private void showParticles(Location currentPoint) {

        Color color = Color.fromRGB(240, 255, 255);

        // Send trails
        for (Location point : trailPoints) {
            for (Player player : point.getWorld().getPlayers()) {
                if (player.getLocation().distanceSquared(point) <= 256) { // 16 blocchi al quadrato
                    ParticleAPI.send(player, point, color);
                }
            }
        }
        // Send particles
        for (Player player : currentPoint.getWorld().getPlayers()) {
            if (player.getLocation().distanceSquared(currentPoint) <= 256) {
                ParticleAPI.send(player, currentPoint, color);
            }

        }
    }


}
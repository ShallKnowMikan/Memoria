package dev.mikan.packets;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.particle.data.ParticleItemStackData;
import com.github.retrooper.packetevents.protocol.particle.type.ParticleTypes;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerParticle;
import dev.mikan.altairkit.utils.ItemBuilder;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

@UtilityClass
public class ParticleAPI {

    public void send(@NonNull Player player, Location location, Color color){
        WrapperPlayServerParticle packet = getPacket(color,location);
        PacketEvents.getAPI().getPlayerManager().sendPacket(player,packet);
    }


    private WrapperPlayServerParticle getPacket(Color color, Location location) {

        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();


        double red = getColorValue(color.getRed());
        double green = getColorValue(color.getGreen());
        double blue = getColorValue(color.getBlue());

        return new WrapperPlayServerParticle(
                new com.github.retrooper.packetevents.protocol.particle.Particle<>(ParticleTypes.DUST),
                true,
                new Vector3d(x, y, z),
                new Vector3f((float) red, (float) green, (float) blue),
                1,
                0,
                true);
    }
    public void send(@NonNull Player player, Location location, Material material){
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();


        ItemBuilder builder = new ItemBuilder(material);

        ParticleItemStackData particleItemStackData = new ParticleItemStackData(SpigotConversionUtil.fromBukkitItemStack(builder.toItemStack()));

        WrapperPlayServerParticle packet = new WrapperPlayServerParticle(
                new com.github.retrooper.packetevents.protocol.particle.Particle<>(ParticleTypes.ITEM, particleItemStackData),
                false,
                new Vector3d(x,y,z),
                new Vector3f(),
                0.02f, 1
        );

        PacketEvents.getAPI().getPlayerManager().sendPacket(player,packet);
    }



    public void sendExplosionAndFlame(Player player, Location location){
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();


        WrapperPlayServerParticle flame = new WrapperPlayServerParticle(
                new com.github.retrooper.packetevents.protocol.particle.Particle<>(ParticleTypes.FLAME),
                false,
                new Vector3d(x,y,z),
                new Vector3f(),
                1, 10
        );

        WrapperPlayServerParticle explosion = new WrapperPlayServerParticle(
                new com.github.retrooper.packetevents.protocol.particle.Particle<>(ParticleTypes.EXPLOSION),
                false,
                new Vector3d(x,y,z),
                new Vector3f(),
                0, 1
        );

        PacketEvents.getAPI().getPlayerManager().sendPacket(player,flame);
        PacketEvents.getAPI().getPlayerManager().sendPacket(player,explosion);

    }
    private double getColorValue(int colorField){
        return (double) colorField / 255;
    }
}

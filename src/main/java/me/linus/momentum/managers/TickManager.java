package me.linus.momentum.managers;

import me.linus.momentum.event.events.packet.PacketReceiveEvent;
import net.minecraft.network.play.server.SPacketTimeUpdate;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author rigamortis
 * @since 11/26/2020
 */

public class TickManager {

    private long prevTime;
    public static float[] TPS = new float[20];
    private int currentTick;

    private float lastTick = -1;

    public TickManager() {
        prevTime = -1;

        for (int i = 0, len = TPS.length; i < len; i++) {
            TPS[i] = 0.0f;
        }

        MinecraftForge.EVENT_BUS.register(this);
    }

    public float getLastTick() {
        return lastTick;
    }

    public static float getTPS() {
        int tickCount = 0;
        float tickRate = 0.0f;

        for (int i = 0; i < TPS.length; i++) {
            float tick = TPS[i];

            if (tick > 0.0f) {
                tickRate += tick;
                tickCount++;
            }
        }

        return MathHelper.clamp((tickRate / tickCount), 0.0f, 20.0f);
    }

    @SubscribeEvent
    public void onPacketRecieve(PacketReceiveEvent event) {
        if (event.getPacket() instanceof SPacketTimeUpdate) {
            if (prevTime != -1) {
                TPS[currentTick % TPS.length] = MathHelper.clamp((20.0f / ((float) (System.currentTimeMillis() - prevTime) / 1000.0f)), 0.0f, 20.0f);
                lastTick = TPS[currentTick % TPS.length];
                currentTick++;
            }

            prevTime = System.currentTimeMillis();
        }
    }
}

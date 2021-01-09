package me.linus.momentum.util.world;

import me.linus.momentum.mixin.MixinInterface;

/**
 * @author linustouchtips
 * @since 11/26/2020
 */

public class Timer implements MixinInterface {
    public long time;

    public Timer() {
        this.time = -1L;
    }

    /**
     * system
     */

    public long getMS(long time) {
        return time / 1000000L;
    }

    public boolean passed(long time, Format format) {
        switch (format) {
            case System:
                return this.getMS(System.nanoTime() - this.time) >= time;
            case Ticks:
                return mc.player.ticksExisted % (int) time == 0;
        }

        return true;
    }

    public boolean sleep(long time) {
        if ((System.nanoTime() / 1000000L - time) >= time) {
            this.reset();
            return true;
        }

        return false;
    }
    public boolean toReach(long time, Format format) {
        switch (format) {
            case System:
                return this.getMS(System.nanoTime() - this.time) <= time;
            case Ticks:
                return mc.player.ticksExisted % (int) time != 0;
        }

        return true;
    }

    public void reset() {
        this.time = System.nanoTime();
    }
    
    public enum Format {
        System,
        Ticks
    }
}

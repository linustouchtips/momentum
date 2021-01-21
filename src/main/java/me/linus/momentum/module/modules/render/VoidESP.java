package me.linus.momentum.module.modules.render;

import me.linus.momentum.module.Module;
import me.linus.momentum.setting.checkbox.Checkbox;
import me.linus.momentum.setting.color.ColorPicker;
import me.linus.momentum.setting.slider.Slider;
import me.linus.momentum.util.render.builder.RenderBuilder;
import me.linus.momentum.util.render.builder.RenderUtil;
import me.linus.momentum.util.world.BlockUtil;
import me.linus.momentum.util.world.HoleUtil;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author linustouchtips
 * @since 11/29/2020
 */

public class VoidESP extends Module {
    public VoidESP() {
        super("VoidESP", Category.RENDER, "Highlights void holes");
    }

    public static Slider range = new Slider("Range", 0.0D, 12.0D, 20.0D, 0);
    public static Checkbox portals = new Checkbox("Portals", true);
    public static Checkbox outline = new Checkbox("Outline", false);

    public static Checkbox color = new Checkbox("Color", true);
    public static ColorPicker colorPicker = new ColorPicker(color, new Color(177, 50, 236, 121));

    @Override
    public void setup() {
        addSetting(range);
        addSetting(outline);
        addSetting(color);
    }

    List<BlockPos> voidBlocks = new ArrayList<>();
    List<BlockPos> portalBlocks = new ArrayList<>();

    @Override
    public void onUpdate() {
        if (nullCheck())
            return;

        voidBlocks.clear();

        BlockUtil.getNearbyBlocks(mc.player, range.getValue(), false).stream().filter(blockPos -> HoleUtil.isVoidHole(blockPos)).forEach(blockPos ->
                voidBlocks.add(blockPos)
        );

        BlockUtil.getNearbyBlocks(mc.player, range.getValue(), false).stream().filter(blockPos -> mc.world.getBlockState(blockPos).getBlock().equals(Blocks.PORTAL)).forEach(blockPos ->
                portalBlocks.add(blockPos)
        );
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent eventRender) {
        for (BlockPos voidPos : voidBlocks)
            RenderUtil.drawBoxBlockPos(voidPos, 0, colorPicker.getColor(), outline.getValue() ? RenderBuilder.renderMode.Both : RenderBuilder.renderMode.Fill);

        for (BlockPos portalPos : portalBlocks)
            RenderUtil.drawBoxBlockPos(portalPos, 0, colorPicker.getColor(), outline.getValue() ? RenderBuilder.renderMode.Both : RenderBuilder.renderMode.Fill);
    }
}
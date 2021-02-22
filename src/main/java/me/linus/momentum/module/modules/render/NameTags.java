package me.linus.momentum.module.modules.render;

import me.linus.momentum.managers.GearManager;
import me.linus.momentum.managers.social.enemy.EnemyManager;
import me.linus.momentum.managers.social.friend.FriendManager;
import me.linus.momentum.module.Module;
import me.linus.momentum.setting.checkbox.Checkbox;
import me.linus.momentum.setting.color.ColorPicker;
import me.linus.momentum.setting.slider.Slider;
import me.linus.momentum.util.client.ColorUtil;
import me.linus.momentum.util.client.MathUtil;
import me.linus.momentum.util.combat.EnemyUtil;
import me.linus.momentum.util.player.InventoryUtil;
import me.linus.momentum.util.render.FontUtil;
import me.linus.momentum.util.render.RenderUtil;
import me.linus.momentum.util.world.EntityUtil;
import me.linus.momentum.util.world.RaytraceUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author linustouchtips
 * @since 12/17/2020
 */

public class NameTags extends Module {
    public NameTags() {
        super("NameTags", Category.RENDER, "Draws useful information at player's heads");
    }

    public static Checkbox health = new Checkbox("Health", true);
    public static Checkbox ping = new Checkbox("Ping", true);
    public static Checkbox gamemode = new Checkbox("GameMode", false);
    public static Checkbox armor = new Checkbox("Armor", true);
    public static Checkbox item = new Checkbox("Items", true);
    public static Checkbox enchants = new Checkbox("Enchants", true);
    public static Checkbox exp = new Checkbox("EXP", true);

    public static Checkbox background = new Checkbox("Background", true);
    public static ColorPicker colorPicker = new ColorPicker(background, "Color Picker", new Color(0, 0, 0, 70));

    public static Checkbox onlyInViewFrustrum = new Checkbox("View Frustrum", false);

    public static Slider scale = new Slider("Scale", 0.0D, 2.0D, 10.0D, 1);
    public static Checkbox scaleByDistance = new Checkbox("Scale By Distance", false);

    @Override
    public void setup() {
        addSetting(health);
        addSetting(ping);
        addSetting(gamemode);
        addSetting(armor);
        addSetting(item);
        addSetting(enchants);
        addSetting(exp);
        addSetting(background);
        addSetting(onlyInViewFrustrum);
        addSetting(scale);
        addSetting(scaleByDistance);
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        if (nullCheck() || mc.renderEngine == null || mc.getRenderManager() == null || mc.getRenderManager().options == null)
            return;

        List<EntityPlayer> nametagEntities = new ArrayList<>();

        mc.world.playerEntities.stream().filter(entity -> entity instanceof EntityPlayer && EntityUtil.isLiving(entity) && entity != mc.getRenderViewEntity()).forEach(entityPlayer -> {
            RenderUtil.camera.setPosition(mc.getRenderViewEntity().posX, mc.getRenderViewEntity().posY, mc.getRenderViewEntity().posZ);

            if (!RaytraceUtil.raytraceEntity(entityPlayer) && onlyInViewFrustrum.getValue())
                return;

            nametagEntities.add(entityPlayer);
        });

        nametagEntities.sort((entity1, entity2) -> Double.compare(entity2.getDistance(mc.getRenderViewEntity()), entity1.getDistance(mc.getRenderViewEntity())));
        nametagEntities.stream().forEach(entityPlayer -> {
            Entity viewEntity = mc.getRenderViewEntity();
            Vec3d nametagPosition = EntityUtil.interpolateEntityByTicks(entityPlayer, event.getPartialTicks());

            double x = nametagPosition.x;
            double distance = nametagPosition.y + 0.65;
            double z = nametagPosition.z;

            double y = distance + (entityPlayer.isSneaking() ? 0.0 : 0.08);

            nametagPosition = EntityUtil.interpolateEntityByTicks(viewEntity, event.getPartialTicks());

            double posX = viewEntity.posX;
            double posY = viewEntity.posY;
            double posZ = viewEntity.posZ;

            viewEntity.posX = nametagPosition.x;
            viewEntity.posY = nametagPosition.y;
            viewEntity.posZ = nametagPosition.z;

            double distanceScale = scale.getValue();

            if (distance > 0.0 && scaleByDistance.getValue())
                distanceScale = 2 + (scale.getValue() / 10) * distance;

            String nameTag = generateNameTag(entityPlayer);
            float width = FontUtil.getStringWidth(nameTag) / 2;
            float height = mc.fontRenderer.FONT_HEIGHT;

            GlStateManager.pushMatrix();
            RenderHelper.enableStandardItemLighting();
            RenderUtil.drawNametag(nameTag, x, y, z, width, height, distanceScale, background.getValue());
            GlStateManager.pushMatrix();

            Iterator<ItemStack> armorStack = entityPlayer.getArmorInventoryList().iterator();
            ArrayList<ItemStack> stacks = new ArrayList<>();

            if (item.getValue())
                stacks.add(entityPlayer.getHeldItemOffhand());

            while (armorStack.hasNext()) {
                ItemStack stack = armorStack.next();
                if (!stack.isEmpty() && armor.getValue())
                    stacks.add(stack);
            }

            if (item.getValue())
                stacks.add(entityPlayer.getHeldItemMainhand());

            Collections.reverse(stacks);

            int offset = stacks.size();
            int offsetScaled = -(8 * offset);

            if (exp.getValue()) {
                renderItemStack(entityPlayer, Items.EXPERIENCE_BOTTLE.getDefaultInstance(), offsetScaled, -32, 0, true);
                offset++;
                offsetScaled += 16;
            }

            for (ItemStack stack : stacks) {
                renderItemStack(entityPlayer, stack, offsetScaled, -32, 0, false);

                if (enchants.getValue())
                    renderItemEnchantments(stack, offsetScaled, -62);

                offsetScaled += 16;
            }

            GlStateManager.popMatrix();

            GlStateManager.enableDepth();
            GlStateManager.disableBlend();
            GlStateManager.disablePolygonOffset();
            GlStateManager.doPolygonOffset(1.0f, 1500000.0f);
            GlStateManager.popMatrix();

            mc.getRenderViewEntity().posX = posX;
            mc.getRenderViewEntity().posY = posY;
            mc.getRenderViewEntity().posZ = posZ;
        });
    }

    public String getEnchantName(Enchantment enchantment, int translated) {
        if (enchants.getValue()) {
            if (enchantment.getTranslatedName(translated).contains("Vanish"))
                return TextFormatting.RED + "Van";
            if (enchantment.getTranslatedName(translated).contains("Bind"))
                return TextFormatting.RED + "Bind";

            String substring = enchantment.getTranslatedName(translated);
            int n2 = (translated > 1) ? 2 : 3;
            if (substring.length() > n2)
                substring = substring.substring(0, n2);

            StringBuilder sb = new StringBuilder();
            String s = substring;
            String s2 = sb.insert(0, s.substring(0, 1).toUpperCase()).append(substring.substring(1)).toString();
            if (translated > 1)
                s2 = new StringBuilder().insert(0, s2).append(translated).toString();

            return s2;
        }

        return "";
    }

    public void renderItemEnchantments(ItemStack itemStack, int x, int y) {
        GlStateManager.scale(0.5f, 0.5f, 0.5f);
        Iterator<Enchantment> iterator2;
        Iterator<Enchantment> iterator = iterator2 = EnchantmentHelper.getEnchantments(itemStack).keySet().iterator();

        while (iterator.hasNext()) {
            Enchantment enchantment;
            if ((enchantment = iterator2.next()) == null)
                iterator = iterator2;

            else {
                FontUtil.drawString(getEnchantName(enchantment, EnchantmentHelper.getEnchantmentLevel(enchantment, itemStack)), (float) (x * 2), (float) y, -1);

                y += 8;
                iterator = iterator2;
            }
        }

        if (itemStack.getItem().equals(Items.GOLDEN_APPLE) && itemStack.hasEffect())
            FontUtil.drawString(TextFormatting.DARK_RED + "God", (float) (x * 2), (float) y, -1);

        GlStateManager.scale(2.0f, 2.0f, 2.0f);
    }

    public void renderItemStack(EntityPlayer entityPlayer, ItemStack itemStack, int x, int y, int scaled, boolean custom) {
        GlStateManager.pushMatrix();
        GlStateManager.depthMask(true);
        GlStateManager.clear(256);
        RenderHelper.enableStandardItemLighting();
        mc.getRenderItem().zLevel = -150.0f;
        GlStateManager.disableAlpha();
        GlStateManager.enableDepth();
        GlStateManager.disableCull();
        int scaledFinal = (scaled > 4) ? ((scaled - 4) * 8 / 2) : 0;
        mc.getRenderItem().renderItemAndEffectIntoGUI(itemStack, x, y + scaledFinal);

        if (custom)
            mc.fontRenderer.drawStringWithShadow(GearManager.expMap.get(entityPlayer) == null ? String.valueOf(0) : String.valueOf(GearManager.expMap.get(entityPlayer)), (float) (x + 17 - mc.fontRenderer.getStringWidth(GearManager.expMap.get(entityPlayer) == null ? String.valueOf(0) : String.valueOf(GearManager.expMap.get(entityPlayer)))), (float) (y + 9), 16777215);
        else
            mc.getRenderItem().renderItemOverlays(mc.fontRenderer, itemStack, x, y + scaledFinal);

        mc.getRenderItem().zLevel = 0.0f;
        RenderHelper.disableStandardItemLighting();
        GlStateManager.enableCull();
        GlStateManager.enableAlpha();
        GlStateManager.scale(0.5f, 0.5f, 0.5f);
        GlStateManager.disableDepth();
        GlStateManager.enableDepth();
        GlStateManager.scale(2.0f, 2.0f, 2.0f);
        GlStateManager.popMatrix();
    }

    public String generateNameTag(EntityPlayer entityPlayer) {
        try {
            return generateName(entityPlayer) + generateGamemode(entityPlayer) + ColorUtil.getPingText(mc.getConnection().getPlayerInfo(entityPlayer.getUniqueID()).getResponseTime()) + generatePing(entityPlayer) + ColorUtil.getHealthText(EnemyUtil.getHealth(entityPlayer)) + generateHealth(entityPlayer);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    public String generateName(EntityPlayer entityPlayer) {
        if (FriendManager.isFriend(entityPlayer.getName()))
            return TextFormatting.AQUA + entityPlayer.getName() + TextFormatting.RESET;
        else if (EnemyManager.isEnemy(entityPlayer.getName()))
            return TextFormatting.DARK_RED + entityPlayer.getName() + TextFormatting.RESET;
        else if (entityPlayer.isSneaking())
            return TextFormatting.GOLD + entityPlayer.getName() + TextFormatting.RESET;
        else
            return entityPlayer.getName();
    }

    public String generateHealth(EntityPlayer entityPlayer) {
        return health.getValue() ? " " + MathUtil.roundAvoid(EnemyUtil.getHealth(entityPlayer), 1) : "";
    }

    public String generateGamemode(EntityPlayer entityPlayer) {
        if (gamemode.getValue()) {
            if (entityPlayer.isCreative())
                return " [C]";
            else if (entityPlayer.isSpectator())
                return " [I]";
            else
                return " [S]";
        }

        else
            return " ";
    }

    public String generatePing(EntityPlayer entityPlayer) {
        if (!mc.isSingleplayer())
            return ping.getValue() ? " " + mc.getConnection().getPlayerInfo(entityPlayer.getUniqueID()).getResponseTime() + "ms" : "";
        else
            return ping.getValue() ? " -1 ms" : "";
    }
}
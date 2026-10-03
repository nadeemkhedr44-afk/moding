package com.example.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class SkyblockFarmingHelperClient implements ClientModInitializer {
    private static final Map<String, Double> PRICES = new HashMap<>();
    private static final Map<String, Integer> LAST_COUNTS = new HashMap<>();
    private static final Map<String, Long> SESSION_COUNTS = new HashMap<>();
    private static long startedAt = 0L;
    private static boolean initialized = false;

    @Override
    public void onInitializeClient() {
        // Initial estimates; edit prices in the in-game config in a later version.
        PRICES.put("minecraft:melon_slice", 1.0);
        PRICES.put("minecraft:pumpkin", 4.0);
        PRICES.put("minecraft:carrot", 3.0);
        PRICES.put("minecraft:potato", 3.0);
        PRICES.put("minecraft:wheat", 2.0);
        PRICES.put("minecraft:nether_wart", 3.0);
        PRICES.put("minecraft:sugar_cane", 2.0);
        PRICES.put("minecraft:cocoa_beans", 3.0);

        ClientTickEvents.END_CLIENT_TICK.register(client -> tick(client));
        HudRenderCallback.EVENT.register((graphics, tickCounter) -> renderHud(graphics));
    }

    private static void tick(Minecraft client) {
        if (client.player == null || client.level == null) {
            initialized = false;
            return;
        }
        if (startedAt == 0L) startedAt = System.currentTimeMillis();

        Map<String, Integer> current = new HashMap<>();
        var inventory = client.player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.isEmpty()) continue;
            String id = stack.getItem().builtInRegistryHolder().key().location().toString();
            if (PRICES.containsKey(id)) current.merge(id, stack.getCount(), Integer::sum);
        }

        if (initialized) {
            for (String id : PRICES.keySet()) {
                int delta = current.getOrDefault(id, 0) - LAST_COUNTS.getOrDefault(id, 0);
                // Only positive inventory changes count; sales/spending do not reduce harvest totals.
                if (delta > 0) SESSION_COUNTS.merge(id, (long) delta, Long::sum);
            }
        }
        LAST_COUNTS.clear();
        LAST_COUNTS.putAll(current);
        initialized = true;
    }

    private static void renderHud(GuiGraphics graphics) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        double total = 0;
        long items = 0;
        for (var entry : SESSION_COUNTS.entrySet()) {
            long count = entry.getValue();
            items += count;
            total += count * PRICES.getOrDefault(entry.getKey(), 0.0);
        }
        long elapsedMs = startedAt == 0L ? 0L : Math.max(1L, System.currentTimeMillis() - startedAt);
        double perHour = total * 3_600_000.0 / elapsedMs;
        int x = 8, y = 8;
        graphics.fill(x - 4, y - 4, x + 174, y + 61, 0xA9000000);
        graphics.drawString(client.font, "Farming Profit Tracker", x, y, 0xFFFFFFFF, true);
        graphics.drawString(client.font, "Harvested: " + items, x, y + 13, 0xFFE0E0E0, true);
        graphics.drawString(client.font, "Session: " + String.format("%,.0f", total) + " coins", x, y + 26, 0xFF55DD77, true);
        graphics.drawString(client.font, "Rate: " + String.format("%,.0f", perHour) + " coins/hr", x, y + 39, 0xFF55DD77, true);
        graphics.drawString(client.font, "Estimate • inventory-based", x, y + 52, 0xFFAAAAAA, true);
    }
}

/*
 * goPaint is designed to simplify painting inside of Minecraft.
 * Copyright (C) Arcaniax-Development
 * Copyright (C) Arcaniax team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.thenextlvl.gopaint.listener;

import com.fastasyncworldedit.core.function.mask.AirMask;
import com.fastasyncworldedit.core.function.mask.InverseMask;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.function.mask.MaskIntersection;
import com.sk89q.worldedit.session.request.Request;
import lombok.RequiredArgsConstructor;
import net.thenextlvl.gopaint.GoPaintPlugin;
import net.thenextlvl.gopaint.api.brush.setting.BrushSettings;
import net.thenextlvl.gopaint.api.brush.setting.PlayerBrushSettings;
import net.thenextlvl.gopaint.api.model.GoPaintProvider;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

@RequiredArgsConstructor
public final class InteractListener implements Listener {
    private final GoPaintPlugin plugin;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onClick(PlayerInteractEvent event) {
        var player = event.getPlayer();

        if (!player.hasPermission(GoPaintProvider.USE_PERMISSION)) return;

        if (!player.hasPermission(GoPaintProvider.WORLD_BYPASS_PERMISSION)
            && plugin.config().brushConfig().disabledWorlds().contains(player.getWorld().getName())) {
            return;
        }

        var item = event.getItem();
        if (item == null) return;

        if (event.getAction().isLeftClick() && item.getType().equals(plugin.config().brushConfig().defaultBrushType())) {
            var brush = plugin.brushController().getBrushSettings(player);
            brush.getMainMenu().open();
            event.setCancelled(true);
            return;
        }

        if (!event.getAction().isRightClick()) return;

        var settings = !item.getType().equals(plugin.config().brushConfig().defaultBrushType())
                ? plugin.brushController().parseBrushSettings(item).orElse(null)
                : plugin.brushController().getBrushSettings(player);

        if (settings == null || settings.getBlocks().isEmpty()) return;

        if (!(settings instanceof PlayerBrushSettings playerSettings) || playerSettings.isEnabled())
            handleInteract(BukkitAdapter.adapt(player), settings);
        else plugin.bundle().sendMessage(player, "brush.disabled");

        event.setCancelled(true);
    }

    private static void handleInteract(Player player, BrushSettings settings) {
        var session = player.getSession();
        var blockTrace = player.getBlockTrace(250, true, session.getMask());
        if (blockTrace != null) player.queueAction(() -> {
            try (var editSession = session.createEditSession(player)) {
                Request.request().setEditSession(editSession);

                try {
                    settings.getBrush().paint(editSession, blockTrace.toBlockPoint(), player, settings);
                } finally {

                    if (bag != null) bag.flushChanges();

                    session.remember(editSession);
                }
            } finally {
                Request.reset();
            }
        });
    }
}

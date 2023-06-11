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
package dev.themeinerlp.bettergopaint.objects.brush;

import dev.themeinerlp.bettergopaint.BetterGoPaint;
import dev.themeinerlp.bettergopaint.objects.other.BlockPlace;
import dev.themeinerlp.bettergopaint.objects.other.BlockPlacer;
import dev.themeinerlp.bettergopaint.objects.other.BlockType;
import dev.themeinerlp.bettergopaint.objects.player.ExportedPlayerBrush;
import dev.themeinerlp.bettergopaint.objects.player.PlayerBrush;
import dev.themeinerlp.bettergopaint.utils.Sphere;
import dev.themeinerlp.bettergopaint.utils.Surface;
import dev.themeinerlp.bettergopaint.utils.XMaterial;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SplatterBrush extends Brush {

    @SuppressWarnings("deprecation")
    @Override
    public void paint(Location loc, Player p) {
        PlayerBrush pb = BetterGoPaint.getBrushManager().getPlayerBrush(p);
        int size = pb.getBrushSize();
        int falloff = pb.getFalloffStrength();
        List<BlockType> pbBlocks = pb.getBlocks();
        if (pbBlocks.isEmpty()) {
            return;
        }
        List<Block> blocks = Sphere.getBlocksInRadius(loc, size);
        List<BlockPlace> placedBlocks = new ArrayList<BlockPlace>();
        for (Block b : blocks) {
            if ((!pb.isSurfaceModeEnabled()) || Surface.isOnSurface(b.getLocation(), p.getLocation())) {
                if ((!pb.isMaskEnabled()) || (b.getType().equals(pb
                        .getMask()
                        .getMaterial()) && (XMaterial.isNewVersion() || b.getData() == pb.getMask().getData()))) {
                    Random r = new Random();
                    double rate = (b
                            .getLocation()
                            .distance(loc) - ((double) size / 2.0) * ((100.0 - (double) falloff) / 100.0)) / (((double) size / 2.0) - ((double) size / 2.0) * ((100.0 - (double) falloff) / 100.0));
                    if (!(r.nextDouble() <= rate)) {
                        int random = r.nextInt(pbBlocks.size());
                        placedBlocks.add(
                                new BlockPlace(b.getLocation(),
                                        new BlockType(
                                                pb.getBlocks().get(random).getMaterial(),
                                                pb.getBlocks().get(random).getData()
                                        )
                                ));
                    }
                }
            }
        }
        BlockPlacer bp = new BlockPlacer();
        bp.placeBlocks(placedBlocks, p);
    }

    @Override
    public String getName() {
        return "Splatter Brush";
    }

    @SuppressWarnings("deprecation")
    @Override
    public void paint(Location loc, Player p, ExportedPlayerBrush epb) {
        int size = epb.getBrushSize();
        int falloff = epb.getFalloffStrength();
        List<BlockType> epbBlocks = epb.getBlocks();
        if (epbBlocks.isEmpty()) {
            return;
        }
        List<Block> blocks = Sphere.getBlocksInRadius(loc, size);
        List<BlockPlace> placedBlocks = new ArrayList<>();
        for (Block b : blocks) {
            if ((!epb.isSurfaceModeEnabled()) || Surface.isOnSurface(b.getLocation(), p.getLocation())) {
                if ((!epb.isMaskEnabled()) || (b.getType().equals(epb
                        .getMask()
                        .getMaterial()) && (XMaterial.isNewVersion() || b.getData() == epb.getMask().getData()))) {
                    Random r = new Random();
                    double rate = (b
                            .getLocation()
                            .distance(loc) - ((double) size / 2.0) * ((100.0 - (double) falloff) / 100.0)) / (((double) size / 2.0) - ((double) size / 2.0) * ((100.0 - (double) falloff) / 100.0));
                    if (!(r.nextDouble() <= rate)) {
                        int random = r.nextInt(epbBlocks.size());
                        placedBlocks.add(
                                new BlockPlace(b.getLocation(),
                                        new BlockType(
                                                epb.getBlocks().get(random).getMaterial(),
                                                epb.getBlocks().get(random).getData()
                                        )
                                ));
                    }
                }
            }
        }
        BlockPlacer bp = new BlockPlacer();
        bp.placeBlocks(placedBlocks, p);

    }

}
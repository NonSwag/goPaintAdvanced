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
package net.thenextlvl.gopaint;

import com.fastasyncworldedit.core.Fawe;
import core.i18n.file.ComponentBundle;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.thenextlvl.gopaint.brush.PlayerBrushManager;
import net.thenextlvl.gopaint.command.GoPaintCommand;
import net.thenextlvl.gopaint.command.ReloadCommand;
import net.thenextlvl.gopaint.listeners.ConnectListener;
import net.thenextlvl.gopaint.listeners.InteractListener;
import net.thenextlvl.gopaint.listeners.InventoryListener;
import net.thenextlvl.gopaint.objects.other.Settings;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.bukkit.CloudBukkitCapabilities;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.LegacyPaperCommandManager;
import org.incendo.serverlib.ServerLib;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Locale;
import java.util.Objects;
import java.util.logging.Level;

public class GoPaintPlugin extends JavaPlugin implements Listener {

    public static final @NotNull String PAPER_DOCS = "https://jd.papermc.io/paper/1.20.6/org/bukkit/Material.html#enum-constant-summary";

    public static final @NotNull String USE_PERMISSION = "bettergopaint.use";
    public static final @NotNull String ADMIN_PERMISSION = "bettergopaint.admin";
    public static final @NotNull String RELOAD_PERMISSION = "bettergopaint.command.admin.reload";
    public static final @NotNull String WORLD_BYPASS_PERMISSION = "bettergopaint.world.bypass";

    private final @NotNull File translations = new File(getDataFolder(), "translations");
    private final @NotNull ComponentBundle bundle = new ComponentBundle(translations, audience ->
            audience instanceof Player player ? player.locale() : Locale.US)
            .register("messages", Locale.US)
            .register("messages_german", Locale.GERMANY)
            .miniMessage(bundle -> MiniMessage.builder().tags(TagResolver.resolver(
                    TagResolver.standard(),
                    Placeholder.component("prefix", bundle.component(Locale.US, "prefix"))
            )).build());

    private final @NotNull PlayerBrushManager brushManager = new PlayerBrushManager(bundle);
    private final @NotNull Metrics metrics = new Metrics(this, 22279);

    @Override
    public void onLoad() {
        metrics.addCustomChart(new SimplePie(
                "faweVersion",
                () -> Objects.requireNonNull(Fawe.instance().getVersion()).toString()
        ));
    }

    @Override
    public void onEnable() {
        // Check if we are in a safe environment
        ServerLib.checkUnsafeForks();

        // disable if goPaint and BetterGoPaint are installed simultaneously
        if (hasOriginalGoPaint()) {
            getComponentLogger().error("BetterGoPaint is a replacement for goPaint. Please use one instead of both");
            getComponentLogger().error("This plugin is now disabling to prevent future errors");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        reloadConfig();

        Material brush = Settings.settings().GENERIC.DEFAULT_BRUSH;
        if (!brush.isItem()) {
            getComponentLogger().error("{} is not a valid default brush, it has to be an item", brush.name());
            getComponentLogger().error("For more information visit {}", PAPER_DOCS);
            getServer().getPluginManager().disablePlugin(this);
        }

        //noinspection UnnecessaryUnicodeEscape
        getComponentLogger().info(MiniMessage.miniMessage().deserialize(
                "<white>Made with <red>\u2665</red> <white>in <gradient:black:red:gold>Germany</gradient>"
        ));

        registerListeners();
        registerCommands();
    }

    @Override
    public void onDisable() {
        metrics.shutdown();
    }

    public void reloadConfig() {
        Settings.settings().reload(this, new File(getDataFolder(), "config.yml"));
    }

    @SuppressWarnings("UnstableApiUsage")
    private void registerCommands() {
        Bukkit.getCommandMap().register("gopaint", getPluginMeta().getName(), new GoPaintCommand(this));

        var annotationParser = enableCommandSystem();
        if (annotationParser != null) {
            annotationParser.parse(new ReloadCommand(this));
            annotationParser.parse(new GoPaintCommand(this));
        }
    }

    private void registerListeners() {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new InventoryListener(getBrushManager()), this);
        pm.registerEvents(new InteractListener(this), this);
        pm.registerEvents(new ConnectListener(getBrushManager()), this);
    }

    private boolean hasOriginalGoPaint() {
        return getServer().getPluginManager().getPlugin("goPaint") != this;
    }

    private @Nullable AnnotationParser<CommandSender> enableCommandSystem() {
        try {
            LegacyPaperCommandManager<CommandSender> commandManager = LegacyPaperCommandManager.createNative(
                    this,
                    ExecutionCoordinator.simpleCoordinator()
            );
            if (commandManager.hasCapability(CloudBukkitCapabilities.BRIGADIER)) {
                commandManager.registerBrigadier();
                getLogger().info("Brigadier support enabled");
            }
            return new AnnotationParser<>(commandManager, CommandSender.class);

        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Cannot init command manager");
            return null;
        }
    }

    public @NotNull PlayerBrushManager getBrushManager() {
        return brushManager;
    }

    public ComponentBundle bundle() {
        return bundle;
    }

}
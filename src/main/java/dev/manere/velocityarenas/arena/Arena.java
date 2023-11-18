package dev.manere.velocityarenas.arena;

import com.fastasyncworldedit.core.FaweAPI;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.*;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import dev.manere.utils.library.Utils;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Arena {
    private RegenerationTask regenTask;
    public static List<Arena> arenas = new ArrayList<>();
    private Location corner1;
    private Location corner2;
    private String name;

    public Arena(String name, Location corner1, Location corner2) {
        this.name = name;
        this.corner1 = corner1;
        this.corner2 = corner2;
    }

    public static List<String> names() {
        return arenas.stream()
                .map(Arena::name)
                .collect(Collectors.toList());
    }

    public static Arena arena(String name) {
        return arenas.stream()
                .filter(arena -> arena.name().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public Location corner1() {
        return corner1;
    }

    public void corner1(Location corner1) {
        this.corner1 = corner1;
    }

    public Location corner2() {
        return corner2;
    }

    public void corner2(Location corner2) {
        this.corner2 = corner2;
    }

    public String name() {
        return name;
    }

    public void name(String name) {
        this.name = name;
    }

    public static List<Arena> arenas() {
        return arenas;
    }

    public void start() {
        if (regenTask != null) {
            stop();
        }

        long regenDelay = 20L * 3L * 60L; // Every 3 minutes
        regenTask = new RegenerationTask(this);
        regenTask.runTaskTimer(Utils.getPlugin(), 0L, regenDelay);
    }

    public void regenerate() {
        FaweAPI.getTaskManager().async(this::paste);
    }

    public void stop() {
        if (regenTask != null) {
            try {
                regenTask.cancel();
                regenTask = null;
            } catch (IllegalStateException ignored) {}
        }
    }

    public void delete() {
        File arenasDir = new File(Utils.getPlugin().getDataFolder(), "data/");
        File configFile = new File(arenasDir, name + ".yml");
        File schemFile = new File(Utils.getPlugin().getDataFolder() + "/schematics", name + ".schem");

        stop();

        if (configFile.exists()) {
            configFile.delete();
        }

        if (schemFile.exists()) {
            schemFile.delete();
        }

        arenas.remove(this);
    }

    public void save() {
        File arenasDir = new File(Utils.getPlugin().getDataFolder(), "data/");

        if (!arenasDir.exists()) {
            arenasDir.mkdirs();
        }

        File configFile = new File(arenasDir, name + ".yml");
        YamlConfiguration config = new YamlConfiguration();

        config.set("name", name);
        config.set("corner1", corner1);
        config.set("corner2", corner2);

        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void load() {
        File arenasDir = new File(Utils.getPlugin().getDataFolder(), "data/");
        File configFile = new File(arenasDir, name + ".yml");

        if (!configFile.exists()) {
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        this.name = config.getString("name");
        this.corner1 = config.getLocation("corner1");
        this.corner2 = config.getLocation("corner2");
    }

    public static void loadAll() {
        File arenasDir = new File(Utils.getPlugin().getDataFolder(), "data/");
        if (!arenasDir.exists()) {
            return;
        }

        File[] arenaFiles = arenasDir.listFiles((dir, name) -> name.endsWith(".yml"));
        if (arenaFiles == null) {
            return;
        }

        Arrays.stream(arenaFiles)
                .map(arenaFile -> arenaFile.getName().replace(".yml", ""))
                .map(arenaName -> new Arena(arenaName, null, null))
                .forEach(arena -> {
                    arena.load();
                    arenas.add(arena);
                    arena.start();
                });
    }

    public static void saveAll() {
        File arenasDir = new File(Utils.getPlugin().getDataFolder(), "data/");
        if (!arenasDir.exists()) {
            arenasDir.mkdirs();
        }

        arenas.forEach(arena -> {
            File configFile = new File(arenasDir, arena.name() + ".yml");
            YamlConfiguration config = new YamlConfiguration();
            config.set("name", arena.name());
            config.set("corner1", arena.corner1());
            config.set("corner2", arena.corner2());

            try {
                config.save(configFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void saveSchematic() {
        Location corner1 = corner1();
        World world = BukkitAdapter.adapt(corner1.getWorld());
        CuboidRegion region = new CuboidRegion(
                BukkitAdapter.adapt(corner1).toBlockPoint(),
                BukkitAdapter.adapt(corner2()).toBlockPoint());

        BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
        clipboard.setOrigin(BukkitAdapter.adapt(corner1).toBlockPoint());

        ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(
                world, region, clipboard, region.getMinimumPoint()
        );

        forwardExtentCopy.setCopyingBiomes(true);
        forwardExtentCopy.setCopyingEntities(false);

        Operations.complete(forwardExtentCopy);

        File schem = schematic();

        try (ClipboardWriter writer = BuiltInClipboardFormat.FAST.getWriter(Files.newOutputStream(schem.toPath()))) {
            if (!schem.exists()) schem.createNewFile();
            writer.write(clipboard);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File schematic() {
        File dir = new File(Utils.getPlugin().getDataFolder() + "/schematics");
        if (!dir.exists()) dir.mkdir();
        return new File(dir, name + ".schem");
    }

    public void paste() {
        if (corner1() == null || corner2() == null) {
            System.out.println("Something attempted to paste an arena without a corner set.");
            return;
        }

        File file = schematic();
        Location corner1 = corner1();
        Clipboard clipboard;
        BlockVector3 to = BukkitAdapter.adapt(corner1).toBlockPoint();
        World world = FaweAPI.getWorld(corner1.getWorld().getName());
        ClipboardFormat format = ClipboardFormats.findByFile(file);

        if (format != null) {
            try (ClipboardReader reader = format.getReader(Files.newInputStream(file.toPath()))) {
                clipboard = reader.read();
                try (EditSession editSession = WorldEdit.getInstance().newEditSession(world)) {
                    Operation operation = new ClipboardHolder(clipboard)
                            .createPaste(editSession)
                            .ignoreAirBlocks(false)
                            .to(to)
                            .build();

                    Operations.complete(operation);
                } catch (Exception e) {
                    throw new Exception();
                }
            } catch (Exception e) {
                try {
                    throw new Exception();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }
}

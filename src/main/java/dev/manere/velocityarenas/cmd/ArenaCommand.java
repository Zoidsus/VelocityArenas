package dev.manere.velocityarenas.cmd;

import dev.manere.utils.text.color.ColorUtils;
import dev.manere.velocityarenas.arena.Arena;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

public class ArenaCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ColorUtils.color("<#ff0000>Only players can execute this command."));
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage(ColorUtils.color("<#ff0000>Correct Usage: /<command> <create | delete | start | stop | corner1 | corner2 | regenerate> <arena>"
                    .replaceAll("<command>", label)));

            return true;
        }

        String action = args[0].toLowerCase();
        String arenaName = args[1];

        switch (action) {
            case "create" -> {
                if (Arena.arena(arenaName) != null) {
                    player.sendMessage(ColorUtils.color("<#ff0000>An arena with that name already exists."));
                    return true;
                }

                Arena arena = new Arena(arenaName, null, null);
                arena.save();
                Arena.arenas().add(arena);

                player.sendMessage(ColorUtils.color("<#00ff00>Arena " + arenaName + " has been successfully created."));
            }
            case "delete" -> {
                Arena arena = Arena.arena(arenaName);

                if (arena == null) {
                    player.sendMessage(ColorUtils.color("<#ff0000>Arena not found."));
                    return true;
                }

                arena.delete();
                player.sendMessage(ColorUtils.color("<#ff0000>Arena &f<arena> <#ff0000>deleted.".replaceAll("<arena>", arenaName)));
            }
            case "corner1" -> {
                Arena arena = Arena.arena(arenaName);

                if (arena == null) {
                    player.sendMessage(ColorUtils.color("<#ff0000>Arena not found."));
                    return true;
                }

                arena.corner1(player.getLocation());
                arena.save();

                player.sendMessage(ColorUtils.color("<#00ff00>Corner 1 set successfully."));
            }
            case "corner2" -> {
                Arena arena = Arena.arena(arenaName);

                if (arena == null) {
                    player.sendMessage(ColorUtils.color("<#ff0000>Arena not found."));
                    return true;
                }

                arena.corner2(player.getLocation());
                arena.save();

                player.sendMessage(ColorUtils.color("<#00ff00>Corner 2 set successfully."));
            }
            case "start" -> {
                Arena arena = Arena.arena(arenaName);

                if (arena == null) {
                    player.sendMessage(ColorUtils.color("<#ff0000>Arena not found."));
                    return true;
                }

                arena.load();

                if (arena.corner1() != null && arena.corner2() != null) {
                    arena.saveSchematic();
                    arena.start();

                    player.sendMessage(ColorUtils.color("<#00ff00>Regeneration started."));
                } else {
                    player.sendMessage(ColorUtils.color("<#ff0000>Regeneration failed to start. You need to have both corner1 and corner2 set!"));
                }
            }
            case "stop" -> {
                Arena arena = Arena.arena(arenaName);

                if (arena == null) {
                    player.sendMessage(ColorUtils.color("<#ff0000>Arena not found."));
                    return true;
                }

                arena.load();
                arena.stop();

                player.sendMessage(ColorUtils.color("<#ff0000><bold>ATTEMPTED</bold> <#ff0000>to stop arena regeneration."));
            }
            case "regenerate" -> {
                Arena arena = Arena.arena(arenaName);

                if (arena == null) {
                    player.sendMessage(ColorUtils.color("<#ff0000>Arena not found."));
                    return true;
                }

                arena.load();
                arena.regenerate();

                player.sendMessage(ColorUtils.color("<#00ff00>Arena regenerated."));
            }
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return List.of("create", "delete", "start", "stop", "corner1", "corner2", "regenerate");
        }

        if (args.length == 2) {
            return Arena.arenas()
                    .stream()
                    .map(Arena::name)
                    .collect(Collectors.toList());
        }

        return null;
    }
}

package dev.manere.velocityarenas;

import dev.manere.utils.command.CommandBuilder;
import dev.manere.utils.library.Utils;
import dev.manere.utils.text.color.ColorUtils;
import dev.manere.velocityarenas.cmd.ArenaCommand;
import org.bukkit.plugin.java.JavaPlugin;
import dev.manere.velocityarenas.arena.Arena;

public final class VelocityArenas extends JavaPlugin {
    public static Utils utils;

    @Override
    public void onEnable() {
        // Plugin startup logic
        utils = Utils.of(this);

        CommandBuilder.of("arenaregen")
                .permission("velocity.staff")
                .permissionMessage(ColorUtils.color("<#ff0000>You don't have permission to execute this command."))
                .executes(new ArenaCommand())
                .completer(new ArenaCommand())
                .build(true, true);

        Arena.loadAll();
    }

    @Override
    public void onDisable() {
        Arena.saveAll();
    }
}

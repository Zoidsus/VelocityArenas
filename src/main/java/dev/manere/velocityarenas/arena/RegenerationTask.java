package dev.manere.velocityarenas.arena;

import com.fastasyncworldedit.core.FaweAPI;
import org.bukkit.scheduler.BukkitRunnable;

public class RegenerationTask extends BukkitRunnable {
    private final Arena arena;

    public RegenerationTask(Arena arena) {
        this.arena = arena;
    }

    @Override
    public void run() {
        FaweAPI.getTaskManager().async(arena::paste);
    }
}

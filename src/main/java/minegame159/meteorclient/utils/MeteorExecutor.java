package minegame159.meteorclient.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public enum MeteorExecutor {
    INSTANCE;

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    public void execute(Runnable task) {
        executor.execute(task);
    }
}

package minegame159.meteorclient.events;

public class Cancellable implements ICancellable {
    private boolean cancelled = false;

    @Override
    public void cancel() {
        cancelled = true;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}

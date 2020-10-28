package minegame159.meteorclient.modules.misc;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import minegame159.meteorclient.events.SendMessageEvent;
import minegame159.meteorclient.modules.Category;
import minegame159.meteorclient.modules.ToggleModule;

public class Annoy extends ToggleModule {
    @EventHandler private final Listener<SendMessageEvent> onSendMessage = new Listener<>(event -> {
        StringBuilder sb = new StringBuilder(event.msg.length());

        boolean upperCase = true;
        for (int cp : event.msg.codePoints().toArray()) {
            sb.appendCodePoint(upperCase ? Character.toUpperCase(cp) : Character.toLowerCase(cp));
            upperCase = !upperCase;
        }

        event.msg = sb.toString();
    });

    public Annoy() {
        super(Category.Misc, "annoy", "Makes your messages wEiRd.");
    }
}

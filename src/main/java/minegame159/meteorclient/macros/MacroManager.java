package minegame159.meteorclient.macros;

import minegame159.meteorclient.Meteor;
import minegame159.meteorclient.events.EventStore;
import minegame159.meteorclient.utils.NbtUtils;
import minegame159.meteorclient.utils.Savable;
import net.minecraft.nbt.CompoundTag;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MacroManager extends Savable<MacroManager> implements Iterable<Macro> {
    public static final MacroManager INSTANCE = new MacroManager();

    private List<Macro> macros = new ArrayList<>();

    private MacroManager() {
        super(new File(Meteor.INSTANCE.getFolder(), "macros.nbt"));
    }

    public void add(Macro macro) {
        macros.add(macro);
        Meteor.INSTANCE.getEventBus().subscribe(macro);
        Meteor.INSTANCE.getEventBus().post(EventStore.macroListChangedEvent());
        save();
    }

    public List<Macro> getAll() {
        return macros;
    }

    public void remove(Macro macro) {
        if (macros.remove(macro)) {
            Meteor.INSTANCE.getEventBus().unsubscribe(macro);
            Meteor.INSTANCE.getEventBus().post(EventStore.macroListChangedEvent());
            save();
        }
    }

    @Override
    public Iterator<Macro> iterator() {
        return macros.iterator();
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.put("macros", NbtUtils.listToTag(macros));
        return tag;
    }

    @Override
    public MacroManager fromTag(CompoundTag tag) {
        macros = NbtUtils.listFromTag(tag.getList("macros", 10), tag1 -> new Macro().fromTag((CompoundTag) tag1));

        for (Macro macro : macros)
            Meteor.INSTANCE.getEventBus().subscribe(macro);
        return this;
    }
}

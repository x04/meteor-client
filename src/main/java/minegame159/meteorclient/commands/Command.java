package minegame159.meteorclient.commands;

import minegame159.meteorclient.Meteor;
import net.minecraft.client.MinecraftClient;

public abstract class Command {
    protected static MinecraftClient MC;

    public final String name;
    public final String description;

    public Command(String name, String description) {
        this.name = name;
        this.description = description;
        MC = Meteor.INSTANCE.getMinecraft();
    }

    public abstract void run(String[] args);
}

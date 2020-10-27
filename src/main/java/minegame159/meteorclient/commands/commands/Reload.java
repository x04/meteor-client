package minegame159.meteorclient.commands.commands;

import minegame159.meteorclient.Meteor;
import minegame159.meteorclient.commands.Command;

public class Reload extends Command {
    public Reload() {
        super("reload", "Reloads config, modules, friends, macros and accounts.");
    }

    @Override
    public void run(String[] args) {
        Meteor.INSTANCE.reload();
    }
}

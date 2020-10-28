package minegame159.meteorclient.gui.screens.topbar;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import minegame159.meteorclient.Meteor;
import minegame159.meteorclient.events.WaypointListChangedEvent;
import minegame159.meteorclient.gui.widgets.WButton;
import minegame159.meteorclient.utils.Utils;
import minegame159.meteorclient.waypoints.Waypoint;
import minegame159.meteorclient.waypoints.Waypoints;
import minegame159.meteorclient.waypoints.gui.EditWaypointScreen;
import minegame159.meteorclient.waypoints.gui.WWaypoint;

public class TopBarWaypoints extends TopBarWindowScreen {
    @EventHandler private final Listener<WaypointListChangedEvent> onWaypointListChanged = new Listener<>(event -> {
        clear();
        initWidgets();
    });

    public TopBarWaypoints() {
        super(TopBarType.Waypoints);
    }

    @Override
    protected void initWidgets() {
        // Waypoints
        for (Waypoint waypoint : Waypoints.INSTANCE) {
            add(new WWaypoint(waypoint)).fillX().expandX();
            row();
        }

        // Add
        if (Utils.canUpdate()) {
            WButton add = add(new WButton("Add")).fillX().expandX().getWidget();
            add.action = () -> Meteor.INSTANCE.getMinecraft().openScreen(new EditWaypointScreen(null));
        }
    }
}

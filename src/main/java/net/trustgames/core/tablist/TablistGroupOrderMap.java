package net.trustgames.core.tablist;

import lombok.Getter;
import net.luckperms.api.model.group.Group;

import java.util.HashMap;

public class TablistGroupOrderMap extends HashMap<Group, Integer> {

    private TablistGroupOrderMap() {}

    @Getter
    private static final TablistGroupOrderMap map = new TablistGroupOrderMap();
}

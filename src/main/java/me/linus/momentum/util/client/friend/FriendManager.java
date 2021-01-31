package me.linus.momentum.util.client.friend;

import me.linus.momentum.module.ModuleManager;

import java.util.ArrayList;
import java.util.List;

/**
 * @author linustouchtips
 * @since 11/29/2020
 */

public class FriendManager {

    public static List<Friend> friends;

    public FriendManager() {
        friends = new ArrayList<>();
    }

    public static List<Friend> getFriends() {
        return friends;
    }

    public static void addFriend(String name) {
        friends.add(new Friend(name));
    }

    public static void removeFriend(String name) {
        friends.remove(getFriendByName(name));
    }

    public static boolean isFriend(String name) {
        boolean friend = false;

        for (Friend f : getFriends()) {
            if (f.getName().equalsIgnoreCase(name))
                friend = true;
        }

        return friend;
    }

    public static Friend getFriendByName(String name) {
        return friends.stream().filter(friend -> friend.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public static boolean isFriendModuleEnabled() {
        return ModuleManager.getModuleByName("Friends").isEnabled();
    }
}

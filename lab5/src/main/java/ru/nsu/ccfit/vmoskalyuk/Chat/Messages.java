package ru.nsu.ccfit.vmoskalyuk.Chat;

import java.util.List;
import java.util.Map;

public final class Messages {
    private Messages() {
    }

    public static Map<String, Object> command(String name, Object... pairs) {
        return JsonUtil.map("command", JsonUtil.mapWithName(name, pairs));
    }

    public static Map<String, Object> success(Object... pairs) {
        return JsonUtil.map("success", JsonUtil.map(pairs));
    }

    public static Map<String, Object> error(String message) {
        return JsonUtil.map("error", JsonUtil.map("message", message));
    }

    public static Map<String, Object> event(String name, Object... pairs) {
        return JsonUtil.map("event", JsonUtil.mapWithName(name, pairs));
    }

    public static Map<String, Object> user(String name, String type) {
        return JsonUtil.map("name", name, "type", type);
    }

    public static Map<String, Object> userList(List<Object> users) {
        return JsonUtil.map("listusers", users);
    }
}

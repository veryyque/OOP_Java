package ru.nsu.ccfit.vmoskalyuk.Serialization.message;

import java.util.List;

public class UserListResponse implements ChatProtocol {
    private static final long serialVersionUID = 9;
    private final List<UserInfo> users;

    public UserListResponse(List<UserInfo> users) {
        this.users = users;
    }

    public List<UserInfo> getUsers() { return users; }
}

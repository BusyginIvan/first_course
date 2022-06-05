package root.server.requests;

import root.User;

public abstract class RequestContainingUser implements IRequest {
    protected User user;

    public IRequest setUser(User user) {
        this.user = user;
        return this;
    }
}
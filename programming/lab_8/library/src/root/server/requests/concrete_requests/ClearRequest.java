package root.server.requests.concrete_requests;

import root.server.IServerSkills;
import root.server.requests.RequestContainingUser;

public class ClearRequest extends RequestContainingUser {
    @Override
    public String[] apply(IServerSkills serverSkills) {
        return serverSkills.clear(user);
    }
}
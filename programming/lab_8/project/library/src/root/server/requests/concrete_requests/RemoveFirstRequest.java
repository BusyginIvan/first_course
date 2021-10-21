package root.server.requests.concrete_requests;

import root.server.IServerSkills;
import root.server.requests.RequestContainingUser;

public class RemoveFirstRequest extends RequestContainingUser {
    @Override
    public String[] apply(IServerSkills serverSkills) {
        return serverSkills.removeFirst(user);
    }
}
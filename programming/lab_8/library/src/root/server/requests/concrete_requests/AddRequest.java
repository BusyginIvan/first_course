package root.server.requests.concrete_requests;

import root.server.IServerSkills;
import root.server.requests.RequestContainingProduct;

public class AddRequest extends RequestContainingProduct {
    @Override
    public String[] apply(IServerSkills serverSkills) {
        return serverSkills.add(product, user.password);
    }
}
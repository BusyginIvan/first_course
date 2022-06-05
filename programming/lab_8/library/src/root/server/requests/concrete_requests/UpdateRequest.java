package root.server.requests.concrete_requests;

import root.server.IServerSkills;
import root.server.requests.IRequestContainingID;
import root.server.requests.RequestContainingProduct;

public class UpdateRequest extends RequestContainingProduct implements IRequestContainingID {
    @Override
    public String[] apply(IServerSkills serverSkills) {
         return serverSkills.update(product, user.password);
    }

    @Override
    public void setID(long id) {
        product.setID(id);
    }
}
package root.server.requests.concrete_requests;

import root.server.IServerSkills;
import root.server.requests.IRequestContainingID;
import root.server.requests.RequestContainingUser;

public class RemoveByIDRequest extends RequestContainingUser implements IRequestContainingID {
    private long id;

    @Override
    public void setID(long id) {
        this.id = id;
    }

    @Override
    public String takeWords(String... words) {
        if (words == null || words.length < 1) return "miss_id";
        try {
            id = Long.parseLong(words[0]);
            return null;
        } catch (NumberFormatException e) {
            return "";
        }
    }

    @Override
    public String[] apply(IServerSkills serverSkills) {
        return serverSkills.removeByID(id, user);
    }
}
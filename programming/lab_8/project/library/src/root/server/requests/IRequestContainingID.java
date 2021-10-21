package root.server.requests;

public interface IRequestContainingID extends IRequest {
    void setID(long id);
}
package root.client;

import root.product_manager.IBaseProductManager;

public interface IClientSkills extends IBaseProductManager {
    void error(String... messages);
}
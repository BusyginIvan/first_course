package root.product_manager;

import java.io.Serializable;

public interface ISerializableProductManager extends
        Serializable,
        IBaseProductManager,
        IReadableProductManager {}
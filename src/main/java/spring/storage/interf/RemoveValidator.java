package spring.storage.interf;

import spring.storage.contex.RemoveContext;

public interface RemoveValidator {
    RemoveContext validate(Integer userId, String path);
}

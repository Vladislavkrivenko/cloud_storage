package spring.storage.interf;

import spring.storage.contex.ListContext;

public interface ListValidator {
    ListContext validate(Integer userId, String path);
}

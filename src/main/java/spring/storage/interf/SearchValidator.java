package spring.storage.interf;

import spring.storage.contex.SearchContext;

public interface SearchValidator {
    SearchContext validate(Integer userId, String query);
}

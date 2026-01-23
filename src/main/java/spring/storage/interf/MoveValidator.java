package spring.storage.interf;

import spring.storage.contex.MoveContext;

public interface MoveValidator {
    MoveContext validate(Integer userId, String from, String to);
}

package com.openclassrooms.mddapi.security;

import com.openclassrooms.mddapi.entity.User;
import java.util.Optional;

public interface CurrentUserProvider {

    User getCurrentUser();

    Optional<User> getCurrentUserOptional();
}

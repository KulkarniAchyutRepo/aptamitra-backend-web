package com.app.TwoFactorAuthentication.repository.Admin;

import com.app.TwoFactorAuthentication.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRoleRepository extends JpaRepository<User, Long> {
}

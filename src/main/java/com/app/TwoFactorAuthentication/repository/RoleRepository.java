package com.app.TwoFactorAuthentication.repository;

import com.app.TwoFactorAuthentication.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByName(String name);

    @Override
    Optional<Role> findById(Long id);

    @Override
    List<Role> findAll();

    @Override
    void deleteById(Long id);

    boolean existsByName(String name);

    @Query(value = "SELECT r.name, count(ur.user_id) as count\n" +
            "from user_roles ur join roles r\n" +
            "on ur.role_id=r.id\n" +
            "group by r.id\n" +
            "order by count desc", nativeQuery = true)
    List<List<Object>> getRoleCount();
}

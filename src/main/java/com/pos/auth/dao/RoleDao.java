package com.pos.auth.dao;

import com.pos.auth.model.Role;

import java.util.List;
import java.util.Optional;

public interface RoleDao {

    List<Role> findAll();

    Optional<Role> findById(int id);

    Optional<Role> findByName(String name);
}

package com.pos.auth.dao;

import com.pos.auth.model.Permission;

import java.util.List;
import java.util.Set;

public interface PermissionDao {

    List<Permission> findAll();

    /** @return the permission codes granted to the given role */
    Set<String> findCodesByRoleId(int roleId);
}

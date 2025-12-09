package com.inventario.inventario.repository;

import com.inventario.inventario.Entity.RoleEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleEntityRepository extends CrudRepository<RoleEntity, Long> {
}

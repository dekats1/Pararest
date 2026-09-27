package com.actisys.menuservice.repository;

import com.actisys.menuservice.model.MenuItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuRepository extends JpaRepository<MenuItem, Long> {
    boolean existsByName(String name);

    @EntityGraph(attributePaths = {"category"})
    List<MenuItem> findAllByIdIn(List<Long> menuIds);
}

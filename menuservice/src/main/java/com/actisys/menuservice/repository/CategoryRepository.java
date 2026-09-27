package com.actisys.menuservice.repository;

import com.actisys.menuservice.model.MenuCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<MenuCategory, Long> {
    boolean existsByName(String name);

    MenuCategory findByName(String name);
}

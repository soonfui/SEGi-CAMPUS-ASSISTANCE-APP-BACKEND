package com.segi.campusassistance.repository;

import com.segi.campusassistance.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long>, JpaSpecificationExecutor<Item> {

    Optional<Item> findByIdAndUserId(Long id, Long userId);

    void deleteByUserId(Long userId);
}

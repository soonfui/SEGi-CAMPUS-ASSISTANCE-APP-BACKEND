package com.segi.campusassistance.repository;

import com.segi.campusassistance.entity.Items;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemsRepository extends JpaRepository<Items, Long> {

    List<Items> findByUserId(Long userId);

    @Modifying
    @Query("DELETE FROM Items i WHERE i.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}


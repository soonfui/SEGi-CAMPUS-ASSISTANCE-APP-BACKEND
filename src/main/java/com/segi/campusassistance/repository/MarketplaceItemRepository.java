package com.segi.campusassistance.repository;

import com.segi.campusassistance.entity.MarketplaceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MarketplaceItemRepository extends JpaRepository<MarketplaceItem, Long>, JpaSpecificationExecutor<MarketplaceItem> {

    Optional<MarketplaceItem> findByItemIdAndSellerId(Long itemId, Long sellerId);

    @Modifying
    @Query("UPDATE MarketplaceItem m SET m.views = m.views + 1 WHERE m.itemId = :itemId")
    void incrementViews(@Param("itemId") Long itemId);

    void deleteBySellerId(Long sellerId);
}


package ru.adserver.domain.repository;

import org.joda.time.DateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.adserver.domain.model.AdEntry;

import java.util.Collection;

/**
 * AdRepository
 *
 * @author Kontsur Alex (bona)
 * @since 14.11.2015
 */
@Repository
public interface AdRepository extends JpaRepository<AdEntry, Long> {

    AdEntry findByNum(String num);

    @Modifying
    @Transactional
    @Query("DELETE FROM AdEntry a WHERE a.num IN (?1)")
    void deleteByNums(Collection<String> nums);

    @Modifying
    @Transactional
    @Query("DELETE FROM AdEntry a WHERE a.num = ?1")
    void deleteByNum(String num);

    @Query("SELECT ad FROM AdEntry ad WHERE ad.created BETWEEN ?1 AND ?2 ORDER BY ad.hits DESC")
    Page<AdEntry> getTopAdsInDateRange(DateTime startdate, DateTime enddate, Pageable pagable);

    @Query("SELECT ad FROM AdEntry ad WHERE ad.created BETWEEN ?1 AND ?2 ORDER BY ad.hits ASC")
    Page<AdEntry> getBottomAdsInDateRange(DateTime startdate, DateTime enddate, Pageable pagable);

}

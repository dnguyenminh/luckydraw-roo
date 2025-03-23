package vn.com.fecredit.app.service;

import vn.com.fecredit.app.entity.Region;
import vn.com.fecredit.app.service.base.AbstractService;

import java.util.Optional;
import java.util.List;

public interface RegionService extends AbstractService<Region> {
    Optional<Region> findByCode(String code);
    boolean existsByCode(String code);
    List<Region> findByProvinceCode(String provinceCode);
    List<Region> findByEventLocationCode(String locationCode);
}

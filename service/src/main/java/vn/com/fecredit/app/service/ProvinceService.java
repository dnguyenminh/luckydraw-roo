package vn.com.fecredit.app.service;

import vn.com.fecredit.app.entity.Province;
import vn.com.fecredit.app.service.base.AbstractService;

import java.util.List;
import java.util.Optional;

public interface ProvinceService extends AbstractService<Province> {
    Optional<Province> findByCode(String code);
    boolean existsByCode(String code);
    List<Province> findByRegionId(Long regionId);
    List<Province> findByRegionCode(String regionCode);
}

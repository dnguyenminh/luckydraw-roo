package vn.com.fecredit.app.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import vn.com.fecredit.app.entity.Province;
import vn.com.fecredit.app.entity.enums.CommonStatus;
import vn.com.fecredit.app.repository.ProvinceRepository;
import vn.com.fecredit.app.service.ProvinceService;
import vn.com.fecredit.app.service.base.AbstractServiceImpl;

@Slf4j
@Service
@Transactional
public class ProvinceServiceImpl extends AbstractServiceImpl<Province> implements ProvinceService {

    private final ProvinceRepository provinceRepository;

    public ProvinceServiceImpl(ProvinceRepository provinceRepository) {
        super(provinceRepository);
        this.provinceRepository = provinceRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Province> findByStatus(CommonStatus status) {
        return provinceRepository.findByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Province> findByCode(String code) {
        return provinceRepository.findByCode(code);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByCode(String code) {
        return provinceRepository.existsByCode(code);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Province> findByRegionId(Long regionId) {
        return provinceRepository.findByRegionId(regionId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Province> findByRegionCode(String regionCode) {
        return provinceRepository.findByRegionCode(regionCode);
    }

    @Override
    public Province deactivate(Long id) {
        Province province = super.deactivate(id);
        // Region status will be updated through entity lifecycle hooks
        // Remove the redundant save call:
        // return provinceRepository.save(province);
        return province;
    }
}

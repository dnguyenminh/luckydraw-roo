package vn.com.fecredit.app.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.fecredit.app.entity.Region;
import vn.com.fecredit.app.entity.CommonStatus;
import vn.com.fecredit.app.repository.RegionRepository;
import vn.com.fecredit.app.service.RegionService;
import vn.com.fecredit.app.service.base.AbstractServiceImpl;

import java.util.Optional;
import java.util.List;

@Slf4j
@Service
@Transactional
public class RegionServiceImpl extends AbstractServiceImpl<Region> implements RegionService {

    private final RegionRepository regionRepository;

    public RegionServiceImpl(RegionRepository regionRepository) {
        super(regionRepository);
        this.regionRepository = regionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Region> findByCode(String code) {
        return regionRepository.findByCode(code);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByCode(String code) {
        return regionRepository.existsByCode(code);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Region> findByProvinceCode(String provinceCode) {
        return regionRepository.findByProvincesCode(provinceCode);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Region> findByEventLocationCode(String locationCode) {
        return regionRepository.findByEventLocationsCode(locationCode);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Region> findByStatus(CommonStatus status) {
        return regionRepository.findByStatus(status);
    }

    @Override
    public Region deactivate(Long id) {
        Region region = super.deactivate(id);
        // Cascade deactivate all event locations in this region
        region.getEventLocations().forEach(location -> location.setStatus(region.getStatus()));
        return region; // Remove the redundant save call
    }
}

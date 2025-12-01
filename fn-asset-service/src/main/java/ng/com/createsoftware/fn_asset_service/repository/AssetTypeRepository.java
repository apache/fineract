package ng.com.createsoftware.fn_asset_service.repository;

import ng.com.createsoftware.fn_asset_service.model.AssetType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AssetTypeRepository extends JpaRepository<AssetType, Long> {
    Optional<AssetType> findByName(String name);
}

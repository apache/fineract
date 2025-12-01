package ng.com.createsoftware.fn_asset_service.repository;

import ng.com.createsoftware.fn_asset_service.model.Asset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssetRepository extends JpaRepository<Asset, Long> {
    List<Asset> findByBranchCode(String branchCode);
}

package ng.com.createsoftware.fn_asset_service.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ng.com.createsoftware.fn_asset_service.model.Asset;
import ng.com.createsoftware.fn_asset_service.model.AssetType;
import ng.com.createsoftware.fn_asset_service.repository.AssetRepository;
import ng.com.createsoftware.fn_asset_service.repository.AssetTypeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssetServiceImpl implements AssetService{

    private final AssetRepository assetRepository;
    private final AssetTypeRepository assetTypeRepository;

    @Override
    public List<Asset> listAssets() {
        log.info("List of assets");
        return assetRepository.findAll();
    }

    @Override
    public List<AssetType> listAssetTypes() {
        log.info("list of asset types");
        return assetTypeRepository.findAll();
    }

    @Transactional
    @Override
    public Asset addAsset(Asset asset) {
        log.info("create asset");
        if(asset.getType() != null && asset.getType().getId() != null){
            var type = assetTypeRepository.findById(asset.getType().getId())
                    .orElse(null);
            asset.setType(type);
        }
        return assetRepository.save(asset);
    }

    @Transactional
    @Override
    public AssetType addAssetType(AssetType type) {
        log.info("create type");
        return assetTypeRepository.save(type);
    }
}

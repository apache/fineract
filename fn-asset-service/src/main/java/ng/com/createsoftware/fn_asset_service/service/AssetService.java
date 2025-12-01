package ng.com.createsoftware.fn_asset_service.service;

import ng.com.createsoftware.fn_asset_service.model.Asset;
import ng.com.createsoftware.fn_asset_service.model.AssetType;

import java.util.List;

public interface AssetService {

    List<Asset> listAssets();
    List<AssetType> listAssetTypes();
    Asset addAsset(Asset asset);
    AssetType addAssetType(AssetType type);
}

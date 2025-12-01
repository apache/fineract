package ng.com.createsoftware.fn_asset_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ng.com.createsoftware.fn_asset_service.model.Asset;
import ng.com.createsoftware.fn_asset_service.model.AssetType;
import ng.com.createsoftware.fn_asset_service.service.AssetService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/assets")
@RequiredArgsConstructor
public class AssetController {
    private final AssetService assetService;

    @GetMapping
    public ResponseEntity<List<Asset>>listOfAssetHandler() {
        return new ResponseEntity<>(assetService.listAssets(), HttpStatus.OK);
    }

    @GetMapping("/types")
    public ResponseEntity<List<AssetType>>listOfAssetTypeHandler() {
        return new ResponseEntity<>(assetService.listAssetTypes(), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Asset>addAssetHandler(@Valid @RequestBody Asset asset) {
        return new ResponseEntity<>(assetService.addAsset(asset), HttpStatus.CREATED);
    }
    @PostMapping("/types")
    public ResponseEntity<AssetType>addAssetTypeHandler(@Valid @RequestBody AssetType assetType) {
        return new ResponseEntity<>(assetService.addAssetType(assetType), HttpStatus.CREATED);
    }
}

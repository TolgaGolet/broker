package com.brokagefirm.broker.api;

import com.brokagefirm.broker.api.request.AssetCreateRequest;
import com.brokagefirm.broker.api.request.GetCustomerAssetsRequest;
import com.brokagefirm.broker.api.response.AssetResponse;
import com.brokagefirm.broker.exception.BrokerGenericException;
import com.brokagefirm.broker.mapper.AssetServiceMapper;
import com.brokagefirm.broker.service.AssetService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/asset")
@SecurityRequirement(name = "BearerAuth")
public class AssetApi {
    private final AssetService assetService;
    private final AssetServiceMapper assetServiceMapper;

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping
    public ResponseEntity<AssetResponse> createAsset(@RequestBody @Validated AssetCreateRequest assetCreateRequest) throws BrokerGenericException {
        AssetResponse response = assetServiceMapper.toAssetResponse(assetService.createAsset(assetCreateRequest));
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<Page<AssetResponse>> getCustomerAssets(@PathVariable Long customerId,
                                                                 @ModelAttribute @Validated GetCustomerAssetsRequest request,
                                                                 @RequestParam(name = "pageNo", defaultValue = "0") int pageNo) throws BrokerGenericException {
        return ResponseEntity.ok(assetService.getCustomerAssets(customerId, request, pageNo));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{assetId}")
    public ResponseEntity<Void> deleteAsset(@PathVariable Long assetId) throws BrokerGenericException {
        assetService.deleteAsset(assetId);
        return ResponseEntity.noContent().build();
    }
}

package com.brokagefirm.broker.api;

import com.brokagefirm.broker.api.request.GetCustomerAssetsRequest;
import com.brokagefirm.broker.api.response.AssetResponse;
import com.brokagefirm.broker.exception.BrokerGenericException;
import com.brokagefirm.broker.service.AssetService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/asset")
@SecurityRequirement(name = "BearerAuth")
public class AssetApi {
    private final AssetService assetService;

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<Page<AssetResponse>> getCustomerAssets(@PathVariable Long customerId, @ModelAttribute @Validated GetCustomerAssetsRequest request) throws BrokerGenericException {
        return ResponseEntity.ok(assetService.getCustomerAssets(customerId, request));
    }
}

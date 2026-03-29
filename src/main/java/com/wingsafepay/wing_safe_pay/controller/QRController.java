package com.wingsafepay.wing_safe_pay.controller;

import com.wingsafepay.wing_safe_pay.dto.QRCheckRequest;
import com.wingsafepay.wing_safe_pay.dto.QRCheckResponse;
import com.wingsafepay.wing_safe_pay.service.QRVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/qr")
@RequiredArgsConstructor
public class QRController {

    private final QRVerificationService qrVerificationService;

    @PostMapping("/verify")
    public ResponseEntity<QRCheckResponse> verify(@RequestBody QRCheckRequest request) {
        return ResponseEntity.ok(qrVerificationService.verify(request));
    }
}
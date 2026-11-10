package com.landmanagement.controller;

import com.landmanagement.dto.request.UnitCreateRequest;
import com.landmanagement.dto.response.UnitResponse;
import com.landmanagement.service.UnitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class UnitController {

    private final UnitService unitService;

    @PostMapping("/admin/units")
    public ResponseEntity<Map<String, Long>> createUnit(@Valid @RequestBody UnitCreateRequest request) {
        Long unitId = unitService.createUnit(request);
        return ResponseEntity.ok(Map.of("id", unitId));
    }

    @GetMapping("/units/provinces")
    public ResponseEntity<List<UnitResponse>> listProvinces(@RequestParam(required = false) String q) {
        List<UnitResponse> provinces = unitService.listProvinces(q);
        return ResponseEntity.ok(provinces);
    }

    @GetMapping("/units/{unitId}/children")
    public ResponseEntity<List<UnitResponse>> listChildren(@PathVariable Long unitId,
            @RequestParam(required = false) String q) {
        List<UnitResponse> children = unitService.listChildren(unitId, q);
        return ResponseEntity.ok(children);
    }
}

package com.innople.loyalty.controller;

import com.innople.loyalty.controller.dto.CommonCodeDtos;
import com.innople.loyalty.service.code.CommonCodeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/common-codes")
@RequiredArgsConstructor
public class CommonCodeController {

    private final CommonCodeService commonCodeService;

    @GetMapping
    public List<CommonCodeDtos.CommonCodeResponse> list(
            @RequestParam(required = false) String codeGroup,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String keyword
    ) {
        return commonCodeService.list(codeGroup, active, keyword).stream()
                .map(this::toResponse)
                .toList();
    }

    @PostMapping
    public CommonCodeDtos.CommonCodeResponse create(@Valid @RequestBody CommonCodeDtos.CreateRequest request) {
        return toResponse(commonCodeService.create(
                request.codeGroup(),
                request.code(),
                request.name(),
                request.active(),
                request.sortOrder()
        ));
    }

    @PutMapping("/{commonCodeId}")
    public CommonCodeDtos.CommonCodeResponse update(
            @PathVariable UUID commonCodeId,
            @Valid @RequestBody CommonCodeDtos.UpdateRequest request
    ) {
        return toResponse(commonCodeService.update(
                commonCodeId,
                request.name(),
                request.active(),
                request.sortOrder()
        ));
    }

    private CommonCodeDtos.CommonCodeResponse toResponse(CommonCodeService.CommonCodeItem i) {
        return new CommonCodeDtos.CommonCodeResponse(
                i.id(),
                i.codeGroup(),
                i.code(),
                i.name(),
                i.active(),
                i.sortOrder(),
                i.createdAt(),
                i.updatedAt()
        );
    }
}


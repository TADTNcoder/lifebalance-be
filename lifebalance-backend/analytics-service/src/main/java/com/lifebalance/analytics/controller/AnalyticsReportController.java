package com.lifebalance.analytics.controller;

import static com.lifebalance.security.keycloak.KeycloakUserMappingFilter.CURRENT_USER_ATTRIBUTE;

import com.lifebalance.analytics.domain.ReportDimension;
import com.lifebalance.analytics.domain.ReportExportFormat;
import com.lifebalance.analytics.domain.ReportStatus;
import com.lifebalance.analytics.domain.ReportType;
import com.lifebalance.analytics.dto.AnalyticsDashboardResponse;
import com.lifebalance.analytics.dto.AnalyticsReportExport;
import com.lifebalance.analytics.dto.AnalyticsReportResponse;
import com.lifebalance.analytics.dto.GenerateReportRequest;
import com.lifebalance.analytics.dto.PageResponse;
import com.lifebalance.analytics.dto.ReasonRequest;
import com.lifebalance.analytics.service.AnalyticsReportService;
import com.lifebalance.common.api.ApiResponse;
import com.lifebalance.common.web.PageableLimits;
import com.lifebalance.security.keycloak.KeycloakUserPrincipal;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsReportController {

    private final AnalyticsReportService reportService;

    public AnalyticsReportController(AnalyticsReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping("/reports")
    public ResponseEntity<ApiResponse<AnalyticsReportResponse>> generate(
            @Valid @RequestBody GenerateReportRequest request,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.status(201).body(ApiResponse.success(reportService.generate(
                CurrentAnalyticsUser.ownerId(currentUser),
                request
        )));
    }

    @GetMapping("/reports/{reportId}")
    public ResponseEntity<ApiResponse<AnalyticsReportResponse>> getById(
            @PathVariable UUID reportId,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getById(
                CurrentAnalyticsUser.ownerId(currentUser),
                reportId
        )));
    }

    @GetMapping("/reports/{reportId}/export")
    public ResponseEntity<byte[]> export(
            @PathVariable UUID reportId,
            @RequestParam String format,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        AnalyticsReportExport export = reportService.export(
                CurrentAnalyticsUser.ownerId(currentUser),
                reportId,
                ReportExportFormat.from(format)
        );
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(export.contentType()))
                .contentLength(export.content().length)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(export.filename())
                        .build()
                        .toString())
                .body(export.content());
    }

    @GetMapping("/reports")
    public ResponseEntity<ApiResponse<PageResponse<AnalyticsReportResponse>>> search(
            @RequestParam(required = false) ReportType reportType,
            @RequestParam(required = false) ReportDimension dimension,
            @RequestParam(required = false) ReportStatus status,
            @RequestParam(required = false) LocalDate periodStart,
            @RequestParam(required = false) LocalDate periodEnd,
            Pageable pageable,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(reportService.search(
                CurrentAnalyticsUser.ownerId(currentUser),
                reportType,
                dimension,
                status,
                periodStart,
                periodEnd,
                PageableLimits.normalize(pageable)
        ))));
    }

    @PatchMapping("/reports/{reportId}/archive")
    public ResponseEntity<ApiResponse<AnalyticsReportResponse>> archive(
            @PathVariable UUID reportId,
            @Valid @RequestBody(required = false) ReasonRequest request,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(reportService.archive(
                CurrentAnalyticsUser.ownerId(currentUser),
                reportId,
                request
        )));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<AnalyticsDashboardResponse>> dashboard(
            @RequestParam(required = false) LocalDate periodStart,
            @RequestParam(required = false) LocalDate periodEnd,
            @RequestParam(required = false) String currencyCode,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(reportService.dashboard(
                CurrentAnalyticsUser.ownerId(currentUser),
                periodStart,
                periodEnd,
                currencyCode
        )));
    }
}

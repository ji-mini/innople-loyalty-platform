package com.innople.loyalty.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Juso 도로명주소 검색 API 프록시.
 * 팝업 없이 검색 API로 주소 조회 (CORS 회피).
 */
@RestController
@RequestMapping("/api/v1/public/juso")
@org.springframework.web.bind.annotation.CrossOrigin
public class PublicJusoSearchController {

    private static final String JUSO_API_URL = "https://business.juso.go.kr/addrlink/addrLinkApi.do";

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.juso.search-confm-key:U01TX0FVVEgyMDI2MDMxMzEzNTcyNzExNzcyNTY=}")
    private String confmKey;

    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int currentPage,
            @RequestParam(defaultValue = "10") int countPerPage
    ) {
        if (keyword == null || keyword.isBlank()) {
            return ResponseEntity.badRequest().body("{\"results\":{\"common\":{\"errorCode\":\"E0005\",\"errorMessage\":\"검색어를 입력해주세요.\"}}}");
        }
        String trimmed = keyword.trim();
        if (trimmed.length() < 2) {
            return ResponseEntity.badRequest().body("{\"results\":{\"common\":{\"errorCode\":\"E0008\",\"errorMessage\":\"검색어는 두글자 이상 입력되어야 합니다.\"}}}");
        }
        if (trimmed.matches(".*[%=><\\[\\]].*")) {
            return ResponseEntity.badRequest().body("{\"results\":{\"common\":{\"errorCode\":\"E0013\",\"errorMessage\":\"특수문자는 검색할 수 없습니다.\"}}}");
        }

        try {
            String encodedKeyword = URLEncoder.encode(trimmed, StandardCharsets.UTF_8);
            String url = String.format("%s?confmKey=%s&currentPage=%d&countPerPage=%d&keyword=%s&resultType=json",
                    JUSO_API_URL, confmKey, currentPage, countPerPage, encodedKeyword);
            String body = restTemplate.getForObject(URI.create(url), String.class);
            return ResponseEntity.ok(body != null ? body : "{}");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("{\"results\":{\"common\":{\"errorCode\":\"-999\",\"errorMessage\":\"주소 검색 중 오류가 발생했습니다.\"}}}");
        }
    }
}

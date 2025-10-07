package com.my.pharmacy.service;

import com.my.pharmacy.dto.DocumentDto;
import com.my.pharmacy.dto.KakaoApiResponseDto;
import com.my.pharmacy.dto.OutputDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class KakaoCategorySearchService {
    private final RestTemplate restTemplate;

    // 환경변수 또는 application.yml에서 주입
    @Value("${KAKAO_REST_API_KEY}")
    private String kakaoRestApiKey;

    // 카테고리 검색 엔드포인트
    private static final String KAKAO_CATEGORY_URL =
            "https://dapi.kakao.com/v2/local/search/category.json";

    /**
     * 약국(PM9) 고정, 1페이지 조회
     */
    public KakaoApiResponseDto resultCategorySearch(
            double latitude, double longitude, double radius) {

        URI uri = UriComponentsBuilder
                .fromUriString(KAKAO_CATEGORY_URL)
                .queryParam("category_group_code", "PM9") // 약국
                .queryParam("x", longitude)
                .queryParam("y", latitude)
                .queryParam("radius", radius) // m
                .queryParam("sort", "distance")
                .queryParam("page", 1)        // 필요시 파라미터화 가능
                .queryParam("size", 15)       // 기본 15 (1~15)
                .build(true)
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "KakaoAK " + kakaoRestApiKey);
        HttpEntity<Object> httpEntity = new HttpEntity<>(headers);

        return restTemplate.exchange(
                uri,
                HttpMethod.GET,
                httpEntity,
                KakaoApiResponseDto.class
        ).getBody();
    }

    // (선택) 뷰용 가공 (리스트 3개만)
    public List<OutputDto> makeOutputDto(List<DocumentDto> documentList) {
        return documentList.stream()
                .map(this::convertDto)
                .limit(3)
                .toList();
    }

    private OutputDto convertDto(DocumentDto documentDto) {
        String ROAD_VIEW_URL = "https://map.kakao.com/link/roadview/";
        String DIRECTION_URL = "https://map.kakao.com/link/to/";

        String params = String.join(",",
                documentDto.getPlaceName(),
                String.valueOf(documentDto.getLatitude()),
                String.valueOf(documentDto.getLongitude())
        );

        String mapUrl = UriComponentsBuilder
                .fromUriString(DIRECTION_URL + params)
                .toUriString();

        String roadUrl = ROAD_VIEW_URL
                + documentDto.getLatitude() + ","
                + documentDto.getLongitude();

        return OutputDto.builder()
                .pharmacyName(documentDto.getPlaceName())
                .pharmacyAddress(documentDto.getAddressName())
                .directionURL(mapUrl)
                .roadViewURL(roadUrl)
                .distance(String.format("%.0f m", documentDto.getDistance()))
                .build();
    }
}

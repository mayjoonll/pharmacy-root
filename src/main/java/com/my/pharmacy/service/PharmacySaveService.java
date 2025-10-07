package com.my.pharmacy.service;

import com.my.pharmacy.dto.DocumentDto;
import com.my.pharmacy.dto.KakaoApiResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PharmacySaveService {

    private final KakaoCategorySearchService kakaoCategorySearchService;
    private final JdbcTemplate jdbcTemplate;

    /**
     * 위도/경도/반경(m) 기준으로 카카오에서 1페이지 조회해 DB에 저장
     */
    public int searchAndSave(double latitude, double longitude, double radius) {
        KakaoApiResponseDto response =
                kakaoCategorySearchService.resultCategorySearch(latitude, longitude, radius);

        if (response == null || response.getDocumentList() == null || response.getDocumentList().isEmpty()) {
            log.warn("카카오 API 응답이 비어있습니다.");
            return 0;
        }

        List<DocumentDto> docs = response.getDocumentList();

        int savedCount = 0;
        for (DocumentDto d : docs) {
            if (savedCount >= 3) break;
            double lat = d.getLatitude();   // y
            double lng = d.getLongitude();  // x
            double dist = d.getDistance();  // m

            // 중복 방지: 같은 이름+좌표 조합이면 skip
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM pharmacy_info WHERE name = ? AND latitude = ? AND longitude = ?",
                    Integer.class,
                    d.getPlaceName(), lat, lng
            );

            if (count == null || count == 0) {
                jdbcTemplate.update(
                        "INSERT INTO pharmacy_info (name, distance, latitude, longitude) VALUES (?, ?, ?, ?)",
                        d.getPlaceName(), dist, lat, lng
                );
                savedCount++;
            }
        }

        log.info("Inserted {} pharmacies into DB", savedCount);
        return savedCount;
    }
}

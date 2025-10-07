package com.my.pharmacy.controller;

import com.my.pharmacy.dto.DocumentDto;
import com.my.pharmacy.dto.InputDto;
import com.my.pharmacy.dto.KakaoApiResponseDto;
import com.my.pharmacy.dto.OutputDto;
import com.my.pharmacy.service.KakaoAddressSearchService;
import com.my.pharmacy.service.KakaoCategorySearchService;
import com.my.pharmacy.service.PharmacySaveService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class FormController {
    private final KakaoAddressSearchService kakaoAddressSearchService;
    private final KakaoCategorySearchService kakaoCategorySearchService;
    private final PharmacySaveService pharmacySaveService;


    @GetMapping("/")
    public String mainForm() {
        return "main";
    }

    @GetMapping("/output")
    public String outputForm() {
        return "output";
    }

    @PostMapping("/search")
    public String searchAddress(InputDto dto, Model model) {
        // 주소 검색 API 호출
        KakaoApiResponseDto kakaoApiResponseDto = kakaoAddressSearchService
                .requestAddressSearch(dto.getAddress());
        // 카테고리 서비스 호출을 위해서 documents 배열에서 0번째 것 만 뺀다.
        DocumentDto documentDto = kakaoApiResponseDto
                .getDocumentList()
                .get(0);

        // 카테고리 검색
        double radius = 1000;
        KakaoApiResponseDto recommendationDto =
                kakaoCategorySearchService.resultCategorySearch(
                        documentDto.getLatitude(),
                        documentDto.getLongitude(),
                        radius);
        // recommendationiDto에서 3개만 추출
        List<OutputDto> outputDtoList =
                kakaoCategorySearchService.makeOutputDto(
                        recommendationDto.getDocumentList());
        System.out.println(outputDtoList);
        model.addAttribute("outputList", outputDtoList);

        int savedCount = pharmacySaveService.searchAndSave(
                documentDto.getLatitude(),
                documentDto.getLongitude(),
                radius
        );
        model.addAttribute("savedCount", savedCount);

        return "output";
    }
}
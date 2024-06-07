package zerobase.weather.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import zerobase.weather.domain.Diary;
import zerobase.weather.service.DiaryServiceImpl;

import java.time.LocalDate;
import java.util.List;

@RestController
public class DiaryController {
    private final DiaryServiceImpl diaryServiceImpl;

    public DiaryController(DiaryServiceImpl diaryServiceImpl) {
        this.diaryServiceImpl = diaryServiceImpl;
    }

    @Operation(summary = "일기 텍스트와 날씨 데이터를 DB에 저장하기")
    @PostMapping("/create/diary")
    void createDiary(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date, @RequestBody String text) {
        diaryServiceImpl.createDiary(date, text);
    }

    @Operation(summary = "선택한 날짜의 모든 일기 불러오기")
    @GetMapping("/read/diary")
    List<Diary> readDiary(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return diaryServiceImpl.readDiary(date);
    }

    @Operation(summary = "선택한 기간의 모든 일기 불러오기")
    @GetMapping("/read/diaries")
    List<Diary> readDiaries(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = "조회할 기간의 마지막 날 입력", example = "2020-02-01") LocalDate startDate,
                            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = "조회할 기간의 마지막 날 입력", example = "2020-02-05") LocalDate endDate) {
        return diaryServiceImpl.readDiaries(startDate, endDate);
    }

    @Operation(summary = "선택한 날짜의 일기 수정하기")
    @PutMapping("/update/diary")
    void updateDiary(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date, @RequestBody String text) {
        diaryServiceImpl.updateDiary(date, text);
    }

    @Operation(summary = "선택한 날짜의 일기 삭제하기")
    @DeleteMapping("/delete/diary")
    void deleteDiary(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        diaryServiceImpl.deleteDiary(date);
    }
}

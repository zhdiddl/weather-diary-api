package zerobase.weather.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;
import zerobase.weather.domain.DateWeather;
import zerobase.weather.domain.Diary;
import zerobase.weather.error.InvalidDateException;
import zerobase.weather.repository.DateWeatherRepository;
import zerobase.weather.repository.DiaryRepository;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Transactional // 실제 DB에 저장하지 않음
class DiaryServiceImplTest {

    @Mock
    private DiaryRepository diaryRepository;

    @Mock
    private DateWeatherRepository dateWeatherRepository;

    @Spy // 특정 메소드를 모의 메소드로 설정 가능
    @InjectMocks
    private DiaryServiceImpl diaryServiceImpl;

    private final LocalDate date = LocalDate.now();
    private final String text = "This is a test";

    @Test
    void createDiary() {
        //given
        DateWeather dateWeather = new DateWeather();
        dateWeather.setDate(date);
        dateWeather.setWeather("Clear");
        dateWeather.setIcon("01n");
        dateWeather.setTemperature(289.66);
        when(dateWeatherRepository.findAllByDate(date)).thenReturn(List.of(dateWeather));
        //when
        diaryServiceImpl.createDiary(date, text);
        //then
        verify(diaryRepository, times(1)).save(any(Diary.class));
    }

    @Test
    void readDiary() {
        //given
        //when
        List<Diary> diaries = diaryServiceImpl.readDiary(date);
        //then
        assertNotNull(diaries);
    }

    @Test
    void readDiary_invalidDate() {
        //given
        //when
        LocalDate invalidDate = LocalDate.of(1010, 1, 1);
        //then
        assertThrows(InvalidDateException.class, () -> diaryServiceImpl.readDiary(invalidDate));
    }

    @Test
    void updateDiary() {
        //given
        String updatedText = "Updated diary text";

        Diary diary = new Diary();
        diary.setDate(date);
        diary.setText(text);

        when(diaryRepository.getFirstByDate(date)).thenReturn(diary);

        // when
        diaryServiceImpl.updateDiary(date, updatedText);

        // then
        assertEquals(diary.getText(), updatedText);
        verify(diaryRepository, times(1)).save(diary);
    }

    @Test
    void deleteDiary() {
        //given
        //when
        diaryServiceImpl.deleteDiary(date);
        List<Diary> diaries = diaryRepository.findAllByDate(date);
        //then
        assertTrue(diaries.isEmpty());
    }
}
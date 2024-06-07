package zerobase.weather.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import zerobase.weather.domain.DateWeather;
import zerobase.weather.domain.Diary;
import zerobase.weather.error.InvalidDateException;
import zerobase.weather.repository.DateWeatherRepository;
import zerobase.weather.repository.DiaryRepository;

import java.time.LocalDate;
import java.util.List;

@Service
public class DiaryServiceImpl implements DiaryService {

    private final DiaryRepository diaryRepository;
    private final DateWeatherRepository dateWeatherRepository;
    private final WeatherService weatherService;
    private static final Logger logger = LoggerFactory.getLogger(DiaryServiceImpl.class);

    public DiaryServiceImpl(DiaryRepository diaryRepository, DateWeatherRepository dateWeatherRepository, WeatherService weatherService) {
        this.diaryRepository = diaryRepository;
        this.dateWeatherRepository = dateWeatherRepository;
        this.weatherService = weatherService;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void createDiary(LocalDate date, String text) {
        logger.info("creating Diary started");

        try {
            // 날씨 데이터를 DB에서 가져오기
            DateWeather dateWeather = getDateWeather(date);

            // 파싱된 데이터 + 일기 값을 우리 DB에 저장하기
            Diary nowDiary = new Diary(); // NoArgumentConstructor로 생성 가능
            nowDiary.setDateWeather(dateWeather); // weather, icon, temperature 넣기
            nowDiary.setText(text);
            nowDiary.setDate(date);
            diaryRepository.save(nowDiary);

            logger.info("creating diary completed");
        } catch (Exception e) {
            logger.error("creating diary failed");
        }
    }

    private DateWeather getDateWeather(LocalDate date) {
        List<DateWeather> dateWeatherListFromDB = dateWeatherRepository.findAllByDate(date);
        if (dateWeatherListFromDB.isEmpty()) {
            // api에서 날씨 정보를 가져오기
            return weatherService.getWeatherFromApi();
        } else {
            return dateWeatherListFromDB.get(0);
        }
    }

    @Transactional(readOnly = true)
    public List<Diary> readDiary(LocalDate date) {
        logger.debug("Reading Diary");

        if (date.isAfter(LocalDate.ofYearDay(3000, 1)) ||
                date.isBefore(LocalDate.ofYearDay(1500, 1))) {
            throw new InvalidDateException();
        }
        return diaryRepository.findAllByDate(date);
    }

    @Transactional(readOnly = true)
    public List<Diary> readDiaries(LocalDate startDate, LocalDate endDate) {
        return diaryRepository.findAllByDateBetween(startDate, endDate);
    }

    public void updateDiary(LocalDate date, String text) {
        Diary nowDiary = diaryRepository.getFirstByDate(date);
        nowDiary.setText(text);
        diaryRepository.save(nowDiary);
    }

    public void deleteDiary(LocalDate date) {
        diaryRepository.deleteAllByDate(date);
    }
}
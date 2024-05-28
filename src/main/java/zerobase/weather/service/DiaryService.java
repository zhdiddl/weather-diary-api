package zerobase.weather.service;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import zerobase.weather.WeatherApplication;
import zerobase.weather.domain.DateWeather;
import zerobase.weather.domain.Diary;
import zerobase.weather.error.InvalidDate;
import zerobase.weather.repository.DateWeatherRepository;
import zerobase.weather.repository.DiaryRepository;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class DiaryService {

    @Value("${openweathermap.key}") // properties에서 지정한 값을 가져와서 사용
    private String apiKey;

    private final DiaryRepository diaryRepository;
    private final DateWeatherRepository dateWeatherRepository;
    private static final Logger logger = LoggerFactory.getLogger(WeatherApplication.class);

    public DiaryService(DiaryRepository diaryRepository, DateWeatherRepository dateWeatherRepository) {
        this.diaryRepository = diaryRepository;
        this.dateWeatherRepository = dateWeatherRepository;
    }

    @Transactional
    @Scheduled(cron = "0 0 1 * * *") // 매일 새벽 1시에 반복 실행
    public void saveWeatherDate() {
        logger.info("Completed saving weather data");
        dateWeatherRepository.save(getWeatherFromApi());
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void createDiary(LocalDate date, String text) {
        logger.info("Creating Diary");

        // 날씨 데이터 가져오기 (API? DB?)
        DateWeather dateWeather = getDateWeather(date);

        // 파싱된 데이터 + 일기 값을 우리 DB에 저장하기
        Diary nowDiary = new Diary(); // NoArgumentConstructor로 생성 가능
        nowDiary.setDateWeather(dateWeather); // weather, icon, temperature를 채움
        nowDiary.setText(text);
        nowDiary.setDate(date);
        diaryRepository.save(nowDiary);

        logger.info("Diary created");
        logger.error("Creating diary failed");
        logger.warn("Creating diary failed");
    }

    private DateWeather getDateWeather(LocalDate date) {
        List<DateWeather> dateWeatherListFromDB = dateWeatherRepository.findAllByDate(date);
        if (dateWeatherListFromDB.isEmpty()) {
            // api에서 날씨 정보를 가져오기
            return getWeatherFromApi();
        } else {
            return dateWeatherListFromDB.get(0);
        }
    }

    public DateWeather getWeatherFromApi() {
        // open weather map API에서 날씨 데이터 가져오기
        String weatherData = getWeatherString();
        // 받아온 날씨 json 데이터를 파싱하기
        Map<String, Object> parsedWeather = parseWeather(weatherData);
        // 파싱한 데이터를 dateWeather 객체에 넣어서 반환
        DateWeather dateWeather = new DateWeather();
        dateWeather.setDate(LocalDate.now());
        dateWeather.setWeather(parsedWeather.get("main").toString());
        dateWeather.setIcon(parsedWeather.get("icon").toString());
        dateWeather.setTemperature((Double) parsedWeather.get("temp"));
        return dateWeather;
    }

    public String getWeatherString() {
        String apiUrl = "https://api.openweathermap.org/data/2.5/weather?q=seoul&appid=" + apiKey;

        try {
            URL url = new URL(apiUrl); // apiUrl 사용해서 URL 객체 만들기

            HttpURLConnection connection = (HttpURLConnection) url.openConnection(); // HttpURLConnection 객체 생성해서 연결
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();

            BufferedReader br; // BufferedReader로 응답 읽기
            if (responseCode == 200) {
                br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            } else {
                br = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            }

            String inputLine;
            StringBuilder response = new StringBuilder(); // 응답받은 데이터 저장
            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine); // response 객체에 API를 호출해서 받은 결과값 저장
            }
            br.close();
            return response.toString(); // String으로 최종 값 반환

        } catch (Exception e) {
            return "failed to get response";
        }
    }

    public Map<String, Object> parseWeather(String jsonString) {
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject;

        try {
            jsonObject = (JSONObject) jsonParser.parse(jsonString);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        // 필요한 데이터만 추출해서 새로운 맵에 저장
        Map<String, Object> resultMap = new HashMap<>();

        JSONObject mainData = (JSONObject) jsonObject.get("main");
        resultMap.put("temp", mainData.get("temp"));
        JSONArray weatherArray = (JSONArray) jsonObject.get("weather");
        JSONObject weatherData = (JSONObject) weatherArray.get(0);
        resultMap.put("main", weatherData.get("main"));
        resultMap.put("icon", weatherData.get("icon"));
        return resultMap;
    }


    @Transactional(readOnly = true)
    public List<Diary> readDiary(LocalDate date) {
        logger.debug("Reading Diary");

        if (date.isAfter(LocalDate.ofYearDay(3000, 1)) ||
        date.isBefore(LocalDate.ofYearDay(1500, 1))) {
            throw new InvalidDate();
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

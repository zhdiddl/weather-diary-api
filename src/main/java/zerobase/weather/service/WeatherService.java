package zerobase.weather.service;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import zerobase.weather.domain.DateWeather;
import zerobase.weather.error.WeatherDataParsingException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
public class WeatherService {

    @Value("${openweathermap.key}")
    private String apiKey;

    private static final Logger logger = LoggerFactory.getLogger(WeatherService.class);

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
            logger.error("Error getting weather data", e);
            return "failed to get response from api";
        }
    }


    public Map<String, Object> parseWeather(String jsonString) {
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject;

        try {
            jsonObject = (JSONObject) jsonParser.parse(jsonString);
        } catch (ParseException e) {
            logger.error("Error getting weather data", e);
            throw new WeatherDataParsingException("failed to parse json object");
        }

        // 필요한 데이터만 추출해서 새로운 맵에 저장
        Map<String, Object> resultMap = new HashMap<>();

        // 키 존재 여부와 null 체크
        if (jsonObject.containsKey("main")) {
            JSONObject mainData = (JSONObject) jsonObject.get("main");
            if (mainData == null || !mainData.containsKey("temp")) {
                throw new WeatherDataParsingException("missing 'temp' in main weather data");
            }
            resultMap.put("temp", mainData.get("temp"));

            JSONArray weatherArray = (JSONArray) jsonObject.get("weather");
            if (weatherArray == null || weatherArray.isEmpty()) {
                throw new WeatherDataParsingException("missing 'weather' array in weather data");
            }

            JSONObject weatherData = (JSONObject) weatherArray.get(0);
            if (weatherData == null || !weatherData.containsKey("main") || !weatherData.containsKey("icon")) {
                throw new WeatherDataParsingException("missing 'main' or 'icon' in weather array");
            }
            resultMap.put("main", weatherData.get("main"));
            resultMap.put("icon", weatherData.get("icon"));
        }
        logger.info("parsing weather data completed");
        return resultMap;
    }
}
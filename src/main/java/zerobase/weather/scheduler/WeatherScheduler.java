package zerobase.weather.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import zerobase.weather.repository.DateWeatherRepository;
import zerobase.weather.service.WeatherService;

@Component
public class WeatherScheduler {

    private static final Logger logger = LoggerFactory.getLogger(WeatherScheduler.class);

    private final WeatherService weatherService;
    private final DateWeatherRepository dateWeatherRepository;

    public WeatherScheduler(WeatherService weatherService, DateWeatherRepository dateWeatherRepository) {
        this.weatherService = weatherService;
        this.dateWeatherRepository = dateWeatherRepository;
    }

    @Scheduled(cron = "0 0 1 * * *") // 매일 새벽 1시에 반복 실행
    public void saveWeatherDate() {
        logger.info("saving weather data started");
        dateWeatherRepository.save(weatherService.getWeatherFromApi());
        logger.info("saving weather data completed");
    }
}
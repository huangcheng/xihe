package im.cheng.xihe.controller;

import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpServerErrorException;


import im.cheng.xihe.service.astronomy.MoonService;
import im.cheng.xihe.config.XiheConfiguration;

@RestController
public class AstronomyController {
    private final MoonService moonService;

    private final RedisTemplate<String, String> redisTemplate;

    private final XiheConfiguration xiheConfiguration;

    private final String key = "moon";

    AstronomyController(MoonService moonService, RedisTemplate<String, String> redisTemplate, XiheConfiguration xiheConfiguration) {
        this.moonService = moonService;

        this.redisTemplate = redisTemplate;

        this.xiheConfiguration = xiheConfiguration;
    }

    @GetMapping(value = "/astronomy/moon", produces = "text/calendar")
    public String moon() throws HttpServerErrorException.InternalServerError {
        String result;

        result = redisTemplate.opsForValue().get(key);

        if (result != null) {
            return result;
        }

        result = moonService.getMoonPhase();

        if (result == null) {
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch moon phase from third party service");
        }

        redisTemplate.opsForValue().set(key, result, Duration.ofSeconds(xiheConfiguration.getTimeToLive()));

        return result;
    }
}

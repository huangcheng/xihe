package im.cheng.xihe.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;


import im.cheng.xihe.service.astronomy.MoonService;
import org.springframework.web.client.HttpServerErrorException;

import java.time.Duration;

@RestController
public class AstronomyController {
    private final MoonService moonService;

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${spring.cache.redis.time-to-live}")
    private long ttl;

    private final String key = "moon";

    AstronomyController(MoonService moonService, RedisTemplate<String, String> redisTemplate) {
        this.moonService = moonService;

        this.redisTemplate = redisTemplate;
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

        redisTemplate.opsForValue().set(key, result, Duration.ofSeconds(ttl));

        return result;
    }
}

package im.cheng.xihe.controller;

import java.time.Duration;

import reactor.core.publisher.Mono;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public Mono<String> moon() {
        String data = redisTemplate.opsForValue().get(key);

        return Mono.just(data == null ? "" : data).flatMap(value -> {
            if (value.length() > 0) {
                return Mono.just(value);
            } else {
                return moonService.getMoonPhase().map(result -> {
                    redisTemplate.opsForValue().set(key, result, Duration.ofSeconds(xiheConfiguration.getTimeToLive()));

                    return result;
                });
            }
        });
    }
}

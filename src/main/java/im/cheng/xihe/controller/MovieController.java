package im.cheng.xihe.controller;

import java.time.Duration;

import reactor.core.publisher.Mono;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import im.cheng.xihe.config.XiheConfiguration;
import im.cheng.xihe.service.movie.DoubanService;

@RestController
public class MovieController {
    private final RedisTemplate<String, String> redisTemplate;

    private final DoubanService doubanService;

    private final XiheConfiguration xiheConfiguration;

    private final String key = "douban";

    MovieController(DoubanService doubanService, RedisTemplate<String, String> redisTemplate, XiheConfiguration xiheConfiguration) {
        this.redisTemplate = redisTemplate;

        this.doubanService = doubanService;

        this.xiheConfiguration = xiheConfiguration;
    }

    @GetMapping(value = "/movie/douban", produces = "text/calendar")
    public Mono<String> getUpcomingMoviesFromDouban() {
        String data = redisTemplate.opsForValue().get(key);

        return Mono.just(data == null ? "" : data).flatMap(value -> {
            if (value.length() > 0) {
                return Mono.just(value);
            } else {
                return doubanService.getUpcomingMovies().map(result -> {
                    redisTemplate.opsForValue().set(key, result, Duration.ofSeconds(xiheConfiguration.getTimeToLive()));

                    return result;
                });
            }
        });
    }
}

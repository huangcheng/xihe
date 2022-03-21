package im.cheng.xihe.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import im.cheng.xihe.service.movie.DoubanService;
import org.springframework.web.client.HttpServerErrorException;

import java.time.Duration;

@RestController
public class MovieController {
    private final RedisTemplate<String, String> redisTemplate;

    private final DoubanService doubanService;

    @Value("${spring.cache.redis.time-to-live}")
    private long ttl;

    private final String key = "douban";

    MovieController(DoubanService doubanService, RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;

        this.doubanService = doubanService;
    }

    @GetMapping(value = "/movie/douban", produces = "text/calendar")
    @ResponseStatus(HttpStatus.OK)
    public String getUpcomingMoviesFromDouban() {
        String result;

        result = redisTemplate.opsForValue().get(key);

        if (result != null) {
            return result;
        }

        result = doubanService.getDoubanUpcomingMovies();

        if (result == null) {
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch moon phase from third party service");
        }

        redisTemplate.opsForValue().set(key, result, Duration.ofSeconds(ttl));

        return result;
    }
}

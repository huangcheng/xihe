package im.cheng.xihe.controller;

import im.cheng.xihe.config.XiheConfiguration;
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

    private final XiheConfiguration xiheConfiguration;

    private final String key = "douban";

    MovieController(DoubanService doubanService, RedisTemplate<String, String> redisTemplate, XiheConfiguration xiheConfiguration) {
        this.redisTemplate = redisTemplate;

        this.doubanService = doubanService;

        this.xiheConfiguration = xiheConfiguration;
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

        redisTemplate.opsForValue().set(key, result, Duration.ofSeconds(xiheConfiguration.getTimeToLive()));

        return result;
    }
}

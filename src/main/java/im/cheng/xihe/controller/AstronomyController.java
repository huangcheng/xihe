package im.cheng.xihe.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import im.cheng.xihe.service.astronomy.MoonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AstronomyController {
    @Autowired
    private MoonService moonService;

    @GetMapping(value = "/astronomy/moon", produces = "text/calendar")
    @ResponseStatus(HttpStatus.OK)
    public String moon() throws JsonProcessingException {
        return moonService.getMoonPhase();
    }
}

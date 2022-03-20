package im.cheng.xihe.service.astronomy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import im.cheng.xihe.model.MoonPhase;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static im.cheng.xihe.model.MoonPhase.Moon;
import static im.cheng.xihe.model.MoonPhase.Phase;

@AllArgsConstructor
class Event {
    @Getter
    private String title;

    @Getter
    private String time;
}

@Service
public class MoonService {
    private final String[] EMOJIES = {"🌑", "🌓", "🌕", "🌗"};
    private final String[] PHASES = {"新（朔）月", "上弦月", "满（望）月", "下弦月"};
    private final RestTemplate restTemplate;
    private final Logger logger = LoggerFactory.getLogger(MoonService.class);


    MoonService() {
        restTemplate = new RestTemplate();

    }

    public String fetchData() {
        Date date = new Date();
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
        cal.setTime(date);

        int year = cal.get(Calendar.YEAR);

        String result = null;

        String url = new StringBuilder()
                .append("http://www.hko.gov.hk/tc/gts/astronomy/files/MoonPhases_")
                .append(year)
                .append(".xml")
                .toString();

        try {
            result = restTemplate.getForObject(url, String.class);
        } catch (RestClientException e) {
            logger.error(e.getMessage());
        }

        return result;
    }

    public List<Event> getEvents(MoonPhase moonPhase) {
        List<Event> result = new ArrayList<>();
        Moon[] moons = moonPhase.getMOON();

        for (Moon moon : moons) {
            Phase[] phases = moon.getPHASE();

            for (Phase phase : phases) {
                int index = Integer.parseInt(phase.getP());

                String title = new StringBuilder().append(EMOJIES[index]).append(" ").append(PHASES[index]).toString();
                String time = new StringBuilder().append(phase.getY()).append("-").append(phase.getM()).append("-").append(phase.getD()).append(" ").append(phase.getHm()).toString();

                result.add(new Event(title, time));
            }
        }

        return result;
    }

    public String getMoonPhase() {
        String data = fetchData();


        XmlMapper xmlMapper = new XmlMapper();

        MoonPhase moonPhase = null;

        try {
            moonPhase = xmlMapper.readValue(data, MoonPhase.class);
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage());
        }

        if (moonPhase == null) {
            return null;
        }

        List<Event> events = getEvents(moonPhase);

        if (events.size() == 0) {
            return null;
        }

        return "";
    }
}

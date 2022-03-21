package im.cheng.xihe.service.astronomy;

import java.util.*;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.property.*;
import net.fortuna.ical4j.util.RandomUidGenerator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import lombok.AllArgsConstructor;
import lombok.Getter;

import static im.cheng.xihe.model.MoonPhase.Moon;
import static im.cheng.xihe.model.MoonPhase.Phase;

import im.cheng.xihe.model.MoonPhase;

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
        java.util.Calendar cal = java.util.Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
        cal.setTime(date);

        int year = cal.get(java.util.Calendar.YEAR);

        String result = null;

        String url = "http://www.hko.gov.hk/tc/gts/astronomy/files/MoonPhases_" +
                year +
                ".xml";

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

                String title = EMOJIES[index] + " " + PHASES[index];
                String time = phase.getY() + "-" + phase.getM() + "-" + phase.getD() + " " + phase.getHm();

                result.add(new Event(title, time));
            }
        }

        return result;
    }

    private String toCalendar(List<Event> events) {
        Calendar calendar = new Calendar();

        TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
        VTimeZone tz = registry.getTimeZone("Asia/Shanghai").getVTimeZone();

        calendar.getComponents().add(tz);

        calendar.getProperties().add(new ProdId("-//xihe//月相//CN"));
        calendar.getProperties().add(Version.VERSION_2_0);
        calendar.getProperties().add(CalScale.GREGORIAN);
        calendar.getProperties().add(new XProperty("X-WR-CALNAME", "\uD83C\uDF15月相"));

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        for (Event event : events) {
            Date date = null;

            try {
                date = format.parse(event.getTime());
            } catch (ParseException e) {
                logger.error(e.getMessage());
            }

            if (date == null) {
                continue;
            }

            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(date);

            DateTime start = new DateTime(cal.getTime());

            VEvent v = new VEvent(start, start, event.getTitle());
            v.getProperties().add(tz.getTimeZoneId());

            Uid uid = new RandomUidGenerator().generateUid();

            v.getProperties().add(uid);

            calendar.getComponents().add(v);
        }

        return calendar.toString();
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

        toCalendar(events);

        return toCalendar(events);
    }
}

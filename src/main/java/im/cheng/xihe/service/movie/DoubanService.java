package im.cheng.xihe.service.movie;

import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import im.cheng.xihe.config.XiheConfiguration;
import net.fortuna.ical4j.model.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.*;
import net.fortuna.ical4j.util.RandomUidGenerator;

@Data
@Getter
@AllArgsConstructor
class Movie {
    private String name;
    private String url;
    private String time;
    private String description;
}

@Service
public class DoubanService {
    private final RestTemplate restTemplate;

    private final XiheConfiguration xiheConfiguration;

    private final Logger logger = LoggerFactory.getLogger(DoubanService.class);

    public DoubanService(XiheConfiguration xiheConfiguration) {
        this.restTemplate = new RestTemplate();

        this.xiheConfiguration = xiheConfiguration;
    }

    private String toCalendar(List<Movie> movies) {
        Calendar calendar = new Calendar();

        TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
        VTimeZone tz = registry.getTimeZone("Asia/Shanghai").getVTimeZone();

        calendar.getComponents().add(tz);

        calendar.getProperties().add(new ProdId("-//xihe//即将上映的电影//CN"));
        calendar.getProperties().add(Version.VERSION_2_0);
        calendar.getProperties().add(CalScale.GREGORIAN);
        calendar.getProperties().add(new XProperty("X-WR-CALNAME", "\uD83C\uDFAC即将上映的电影"));

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

        for (Movie movie : movies) {
            java.util.Calendar cal =  java.util.Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));

            String time = movie.getTime();

            if (!time.contains("年")) {
                Date date = new Date(System.currentTimeMillis());

                cal.setTime(date);

                time = cal.get(java.util.Calendar.YEAR) + "年" + time;
            }

            if (time.contains("日")) {
                time = time.replace("年", "-")
                        .replace("月", "-")
                        .replace("日", "");

                Date date = null;

                try {
                    date = format.parse(time);
                } catch (ParseException e) {
                    logger.error(e.getMessage());
                }

                if (date == null) {
                    continue;
                }

                cal.setTime(date);

                DateTime start = new DateTime(cal.getTime());

                cal.add(java.util.Calendar.DATE,1);

                DateTime end = new DateTime(cal.getTime());

                VEvent v = new VEvent(start, end, movie.getName());
                v.getProperties().add(tz.getTimeZoneId());

                Uid uid = new RandomUidGenerator().generateUid();

                v.getProperties().add(uid);

                v.getProperties().add(new Description(movie.getDescription()));

                Url url;

                try {
                    url = new Url();

                    url.setValue(movie.getUrl());
                } catch (URISyntaxException e) {
                    url = null;

                    logger.error(e.getMessage());
                }

                if (url != null) {
                    v.getProperties().add(url);
                }

                calendar.getComponents().add(v);
            }
        }

        return calendar.toString();
    }

    private List<Movie> getMovies(String html) {
        Document doc = Jsoup.parse(html);

        ArrayList<Movie> movies = new ArrayList<>();

        Elements $comingList = doc.select(".coming_list tbody tr");

        for (Element element : $comingList) {
            Elements $tds = element.select("td");
            Element $a = $tds.get(1).select("a").get(0);

            String time = $tds.get(0).text().trim();
            String description = $a.attr("title").trim();
            String title = $a.text().trim();
            String url = $a.attr("href").trim();

            movies.add(new Movie("\uD83C\uDFAC" + title, url, time, description));
        }

        return movies;
    }

    public String getDoubanUpcomingMovies() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("User-Agent", xiheConfiguration.getUserAgent());

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> data = null;

        try {
            data = restTemplate.exchange("https://movie.douban.com/coming", HttpMethod.GET, entity, String.class);
        } catch (RestClientException e) {
            logger.error(e.getMessage());
        }

        if (data == null) {
            return null;
        }

        List<Movie> movies = getMovies(data.toString());


        return toCalendar(movies);
    }
}

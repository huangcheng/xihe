package im.cheng.xihe.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@JacksonXmlRootElement
@AllArgsConstructor
@NoArgsConstructor
public class MoonPhase {
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Phase {
        @Getter
        @JacksonXmlProperty(isAttribute = true)
        private String P;

        @Getter
        @JacksonXmlProperty
        private String Y;

        @Getter
        @JacksonXmlProperty
        private String M;

        @Getter
        @JacksonXmlProperty
        private String D;

        @Getter
        @JacksonXmlProperty
        private String hm;

        @Getter
        @JacksonXmlProperty
        private String JD;
    }


    @NoArgsConstructor
    @AllArgsConstructor
    public static class Moon {
        @Getter
        @JacksonXmlProperty(isAttribute = true)
        private String C;

        @Getter
        @JacksonXmlElementWrapper(useWrapping = false)
        private Phase[] PHASE;
    }


    @Getter
    @JacksonXmlElementWrapper(useWrapping = false)
    private Moon[] MOON;
}

package com.openclassrooms.mddapi.controller.topic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;

class TopicControllerTest {

    @Test
    void shouldExposeExpectedRequestMapping() {
        RequestMapping requestMapping = TopicController.class.getAnnotation(RequestMapping.class);
        assertNotNull(requestMapping);
        assertArrayEquals(new String[]{"/api/topics"}, requestMapping.value());
    }

    @Test
    void shouldAllowFrontendOrigin() {
        CrossOrigin crossOrigin = TopicController.class.getAnnotation(CrossOrigin.class);
        assertNotNull(crossOrigin);
        assertEquals("http://localhost:4200", crossOrigin.origins()[0]);
    }
}

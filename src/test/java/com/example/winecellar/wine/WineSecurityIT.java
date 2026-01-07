package com.example.winecellar.wine;

import com.example.winecellar.winery.Winery;
import com.example.winecellar.winery.WineryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class WineSecurityIT {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    WineryRepository wineryRepository;
    @Autowired
    WineRepository wineRepository;

    private Long wineryId;

    @BeforeEach
    void setup() {
        wineRepository.deleteAll();
        wineryRepository.deleteAll();

        Winery winery = Winery.builder()
                              .name("Cricova")
                              .country("Moldova")
                              .build();

        wineryId = wineryRepository.save(winery)
                                   .getId();
    }

    @Test
    void user_can_read_wines() throws Exception {
        mockMvc.perform(get("/api/wines").with(httpBasic("user", "userpass")))
               .andExpect(status().isOk());
    }

    @Test
    void user_cannot_create_wine_returns403() throws Exception {
        WineCreateRequest request = new WineCreateRequest("Feteasca Neagra", wineryId, "Moldova", 2022, BigDecimal.valueOf(150));

        mockMvc.perform(post("/api/wines").with(httpBasic("user", "userpass"))
                                          .contentType(MediaType.APPLICATION_JSON)
                                          .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isForbidden());
    }

    @Test
    void user_cannot_delete_wine_returns403() throws Exception {
        Long wineId = addWineToWinery( "Cabernet", "Moldova", 2019,  BigDecimal.valueOf(150));

        mockMvc.perform(delete("/api/wines/{id}", wineId).with(httpBasic("user", "userpass")))
               .andExpect(status().isForbidden())
               .andExpect(header().exists("X-Request-Id"));
    }


    private Long addWinery(String name, String country) throws Exception {
        return wineryRepository.save(Winery.builder()
                                           .name(name)
                                           .country(country)
                                           .build())
                               .getId();
    }

    private long addWineToWinery(String name, String country, int wineYear, BigDecimal price) {
        return wineRepository.save(Wine.builder()
                                       .name(name)
                                       .wineryRef(wineryRepository.findById(wineryId)
                                                                  .orElseThrow())
                                       .country(country)
                                       .wineYear(wineYear)
                                       .price(price)
                                       .build())
                             .getId();
    }
}

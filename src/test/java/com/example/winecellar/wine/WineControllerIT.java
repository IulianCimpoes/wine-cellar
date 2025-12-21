package com.example.winecellar.wine;

import com.example.winecellar.winery.Winery;
import com.example.winecellar.winery.WineryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class WineControllerIT {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Autowired WineryRepository wineryRepository;
    @Autowired WineRepository wineRepository;

    private Long wineryId;

    @BeforeEach
    void setup() {
        wineRepository.deleteAll();
        wineryRepository.deleteAll();

        Winery winery = Winery.builder()
                .name("Cricova")
                .country("Moldova")
                .build();

        wineryId = wineryRepository.save(winery).getId();
    }

    @Test
    void createWine_returnsWineResponse() throws Exception {
        WineCreateRequest request = new WineCreateRequest(
                "Feteasca Neagra",
                wineryId,
                "Moldova",
                2022,
                BigDecimal.valueOf(150)
        );

        mockMvc.perform(post("/api/wines")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name").value("Feteasca Neagra"))
                .andExpect(jsonPath("$.winery.id").value(wineryId))
                .andExpect(jsonPath("$.winery.name").value("Cricova"))
                .andExpect(jsonPath("$.wineYear").value(2022))
                .andExpect(jsonPath("$.price").value(150));
    }

    @Test
    void createWine_returns400_whenValidationFails() throws Exception {
        // name blank, wineYear too small, price negative
        String badJson = """
                {
                  "name": "",
                  "wineryId": %d,
                  "country": "",
                  "wineYear": 1500,
                  "price": -5
                }
                """.formatted(wineryId);

        mockMvc.perform(post("/api/wines")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.fields.name", not(emptyOrNullString())))
                .andExpect(jsonPath("$.fields.country", not(emptyOrNullString())))
                .andExpect(jsonPath("$.fields.wineYear", containsString("Year")));
    }

    @Test
    void getAll_returnsList() throws Exception {
        wineRepository.save(Wine.builder()
                .name("Test Wine")
                .wineryRef(wineryRepository.findById(wineryId).orElseThrow())
                .country("Moldova")
                .wineYear(2020)
                .price(BigDecimal.TEN)
                .build());

        mockMvc.perform(get("/api/wines"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].name", not(emptyOrNullString())))
                .andExpect(jsonPath("$[0].winery.id", notNullValue()));
    }
}

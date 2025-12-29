package com.example.winecellar.wine;

import com.example.winecellar.winery.Winery;
import com.example.winecellar.winery.WineryRepository;
import com.example.winecellar.winery.WineryUpdateRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.stream.IntStream;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class WineControllerIT {

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
    void createWine_returnsWineResponse() throws Exception {
        WineCreateRequest request = new WineCreateRequest("Feteasca Neagra", wineryId, "Moldova", 2022, BigDecimal.valueOf(150));

        mockMvc.perform(post("/api/wines").contentType(MediaType.APPLICATION_JSON)
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

        mockMvc.perform(post("/api/wines").contentType(MediaType.APPLICATION_JSON)
                                          .content(badJson))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.error").value("Validation failed"))
               .andExpect(jsonPath("$.fields.name", not(emptyOrNullString())))
               .andExpect(jsonPath("$.fields.country", not(emptyOrNullString())))
               .andExpect(jsonPath("$.fields.wineYear", containsString("Year")));
    }

    @Test
    void createWine_returns409_whenDuplicate() throws Exception {
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
               .andExpect(status().isOk());

        mockMvc.perform(post("/api/wines")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isConflict())
               .andExpect(jsonPath("$.error").value(containsString("Wine already exists:")));
    }


    @Test
    void getAll_returnsList() throws Exception {
        addWineToWinery("Test Wine", "Moldova", 2020, BigDecimal.TEN);

        mockMvc.perform(get("/api/wines"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
               .andExpect(jsonPath("$[0].name", not(emptyOrNullString())))
               .andExpect(jsonPath("$[0].winery.id", notNullValue()));
    }

    //GET /api/wines?country=... returns filtered list
    @Test
    void getAll_whenCountryProvided_returnsFilteredList() throws Exception {
        addWineToWinery("Test Wine1", "Moldova", 2020, BigDecimal.valueOf(12));
        addWineToWinery("Test Wine2", "Spain", 2021, BigDecimal.valueOf(11));

        mockMvc.perform(get("/api/wines?country=Spain"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", hasSize(equalTo(1))))
               .andExpect(jsonPath("$[0].name", equalTo("Test Wine2")))
               .andExpect(jsonPath("$[0].country", equalTo("Spain")))
               .andExpect(jsonPath("$[0].wineYear", equalTo(2021)))
               .andExpect(jsonPath("$[0].id", notNullValue()))
               .andExpect(jsonPath("$[0].winery.id", notNullValue()));
    }

    //GET /api/wines?country= treated as unfiltered
    @Test
    void getAll_whenCountryBlank_returnsAll() throws Exception {
        addWineToWinery("Test Wine1", "Moldova", 2020, BigDecimal.valueOf(12));
        addWineToWinery("Test Wine2", "Spain", 2021, BigDecimal.valueOf(11));

        mockMvc.perform(get("/api/wines?country="))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", hasSize(equalTo(2))));
    }

    //GET /api/wines/{id} success
    @Test
    void getById_whenPresent_returnsWine() throws Exception {
        Long wineId = addWineToWinery("Test Wine2", "Spain", 2021, BigDecimal.valueOf(11));

        mockMvc.perform(get("/api/wines/{id}", wineId))
               .andExpect(status().isOk())
               .andExpect(jsonPath("name", equalTo("Test Wine2")))
               .andExpect(jsonPath("country", equalTo("Spain")))
               .andExpect(jsonPath("wineYear", equalTo(2021)))
               .andExpect(jsonPath("id", notNullValue()))
               .andExpect(jsonPath("winery.id", notNullValue()));
    }

    //GET /api/wines/{id} not found → 404
    @Test
    void getById_whenMissing_returns404() throws Exception {

        mockMvc.perform(get("/api/wines/9999"))
               .andExpect(status().isNotFound())
               .andExpect(jsonPath("$.error").value(containsString("Wine not found:")));
    }

    //POST /api/wines with nonexistent wineryId → 404
    @Test
    void createWine_returns404_whenWineryMissing() throws Exception {
        WineCreateRequest request = new WineCreateRequest("Feteasca Neagra", 9999L, "Moldova", 2022, BigDecimal.valueOf(150));

        mockMvc.perform(post("/api/wines").contentType(MediaType.APPLICATION_JSON)
                                          .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isNotFound())
               .andExpect(jsonPath("$.error").value(containsString("Winery not found:")));
    }

    //addWineToWinery default behavior
    @Test
    void getPaged_withDefaults_returnsPage() throws Exception {
        addWinesToWinery(6, "Test Wine12", "Spain", 2024, BigDecimal.valueOf(16));

        mockMvc.perform(get("/api/wines/paged"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.content").exists())
               .andExpect(jsonPath("$.content").isArray())
               .andExpect(jsonPath("$.content", hasSize(5)))
               .andExpect(jsonPath("$.numberOfElements", equalTo(5)))
               .andExpect(jsonPath("$.totalElements", equalTo(6)));
    }

    //GET /api/wines/paged?page=1&size=5
    @Test
    void getPaged_withCustomPageAndSize_returnsCorrectSlice() throws Exception {
        addWinesToWinery(12, "Test Wine12", "Spain", 2024, BigDecimal.valueOf(16));

        mockMvc.perform(get("/api/wines/paged?page=1&size=5"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.content", hasSize(5)))
               .andExpect(jsonPath("$.number", equalTo(1)))
               .andExpect(jsonPath("$.first", equalTo(false)))
               .andExpect(jsonPath("$.last", equalTo(false)))
               .andExpect(jsonPath("$.pageable.offset", equalTo(5)))
               .andExpect(jsonPath("$.size", equalTo(5)));
    }

    //GET /api/wines/paged?sort=name sorts ascending
    @Test
    void getPaged_whenSortByName_sortsAscending() throws Exception {
        addWineToWinery("A_Wine", "Moldova", 2020, BigDecimal.valueOf(12));
        addWineToWinery("B_Wine", "Spain", 2021, BigDecimal.valueOf(11));
        addWineToWinery("C_Wine", "Spain", 2021, BigDecimal.valueOf(11));

        mockMvc.perform(get("/api/wines/paged?sort=name"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.content[0].name", equalTo("A_Wine")))
               .andExpect(jsonPath("$.content[1].name", equalTo("B_Wine")))
               .andExpect(jsonPath("$.content[2].name", equalTo("C_Wine")));
    }

    //PUT /api/wines/{id} — success
    @Test
    void updateWine_whenPresent_returnsUpdatedWine() throws Exception {
        Long wineId = addWineToWinery("Test Wine1", "Spain", 2024, BigDecimal.valueOf(16));

        Winery winery = Winery.builder()
                              .name("Winery1")
                              .country("Spain")
                              .build();

        Long updatedWineryId = wineryRepository.save(winery)
                                   .getId();

        WineUpdateRequest request = new WineUpdateRequest("Test Wine2", updatedWineryId, "Moldova", 2021, BigDecimal.valueOf(11));

        mockMvc.perform(put("/api/wines/{id}", wineId).contentType(MediaType.APPLICATION_JSON)
                                                           .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.name", equalTo("Test Wine2")))
               .andExpect(jsonPath("$.winery.id", equalTo(updatedWineryId.intValue())))
               .andExpect(jsonPath("$.winery.name", equalTo("Winery1")))
               .andExpect(jsonPath("$.winery.country", equalTo("Spain")))
               .andExpect(jsonPath("$.country", equalTo("Moldova")))
               .andExpect(jsonPath("$.wineYear", equalTo(2021)))
               .andExpect(jsonPath("$.price", equalTo(11)))
               .andExpect(jsonPath("$.id", equalTo(wineId.intValue())));
    }

    @Test
    void updateWine_whenMissing_returns404() throws Exception {
        WineUpdateRequest request =
                new WineUpdateRequest("Wine", wineryId, "Moldova", 2022, BigDecimal.TEN);

        mockMvc.perform(put("/api/wines/9999")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isNotFound())
               .andExpect(jsonPath("$.error").value(containsString("Wine not found")));
    }

    @Test
    void updateWine_whenWineryMissing_returns404() throws Exception {
        Long wineId = addWineToWinery("Wine", "Moldova", 2022, BigDecimal.TEN);

        WineUpdateRequest request =
                new WineUpdateRequest("Wine", 9999L, "Moldova", 2022, BigDecimal.TEN);

        mockMvc.perform(put("/api/wines/{id}", wineId)
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isNotFound())
               .andExpect(jsonPath("$.error").value(containsString("Winery not found")));
    }

    @Test
    void updateWine_returns409_whenDuplicate() throws Exception {
        // Wine A: target “unique key”
        Long wineAId = addWineToWinery("Feteasca Neagra", "Moldova", 2022, BigDecimal.valueOf(150));

        // Wine B: different initially
        Long wineBId = addWineToWinery("Rara Neagra", "Moldova", 2021, BigDecimal.valueOf(120));

        // Update Wine B to collide with Wine A (same name + year + wineryId)
        WineUpdateRequest request = new WineUpdateRequest(
                "Feteasca Neagra",
                wineryId,
                "Moldova",
                2022,
                BigDecimal.valueOf(150)
        );

        mockMvc.perform(put("/api/wines/{id}", wineBId)
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isConflict())
               .andExpect(jsonPath("$.error").value(containsString("Wine already exists:")));
    }


    @Test
    void deleteWine_whenPresent_returns204() throws Exception {
        Long wineId = addWineToWinery("Wine", "Moldova", 2022, BigDecimal.TEN);

        mockMvc.perform(delete("/api/wines/{id}", wineId))
               .andExpect(status().isNoContent());
    }

    @Test
    void deleteWine_whenMissing_returns404() throws Exception {
        mockMvc.perform(delete("/api/wines/9999"))
               .andExpect(status().isNotFound())
               .andExpect(jsonPath("$.error").value(containsString("Wine not found")));
    }


    private void addWinesToWinery(int winesCount, String name, String country, int wineYear, BigDecimal price) {
        IntStream.range(0, winesCount)
                 .forEach(i -> addWineToWinery(name, country, wineYear, price));
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

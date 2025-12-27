package com.example.winecellar.winery;

import com.example.winecellar.wine.Wine;
import com.example.winecellar.wine.WineRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class WineryControllerIT {

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
    }

    // POST /api/wineries — success
    @Test
    void createWinery_returnsWineryResponse() throws Exception {
        WineryCreateRequest request = new WineryCreateRequest("Fautorul", "Moldova");

        mockMvc.perform(post("/api/wineries").contentType(MediaType.APPLICATION_JSON)
                                             .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id", notNullValue()))
               .andExpect(jsonPath("$.name").value("Fautorul"))
               .andExpect(jsonPath("$.country").value("Moldova"));
    }

    //POST /api/wineries — validation fails for the second winery with identical name country
    @Test
    void createWinery_returns409_whenDuplicate() throws Exception {
        WineryCreateRequest request = new WineryCreateRequest("Fautorul", "Moldova");

        mockMvc.perform(post("/api/wineries").contentType(MediaType.APPLICATION_JSON)
                                             .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isOk());

        mockMvc.perform(post("/api/wineries").contentType(MediaType.APPLICATION_JSON)
                                             .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isConflict())
               .andExpect(jsonPath("$.error").value(containsString("Winery already exists:")));

    }

    //POST /api/wineries — validation fails
    @Test
    void createWinery_returns400_whenValidationFails() throws Exception {
        String badJson = """
                {
                  "name": "",
                  "country": ""
                }
                """;

        mockMvc.perform(post("/api/wineries").contentType(MediaType.APPLICATION_JSON)
                                             .content(badJson))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.error").value("Validation failed"))
               .andExpect(jsonPath("$.fields.name", not(emptyOrNullString())))
               .andExpect(jsonPath("$.fields.country", not(emptyOrNullString())));
    }

    //GET /api/wineries returns list
    @Test
    void getAll_returnsList() throws Exception {
        addWinery("Fautorul", "Moldova");
        addWinery("Cricova", "Moldova");

        mockMvc.perform(get("/api/wineries"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", hasSize(2)))
               .andExpect(jsonPath("$[*].id", everyItem(notNullValue())))
               .andExpect(jsonPath("$[*].name", everyItem(notNullValue())))
               .andExpect(jsonPath("$[*].country", everyItem(notNullValue())));
    }

    //GET /api/wineries/paged defaults
    @Test
    void getPaged_withDefaults_returnsPage() throws Exception {
        addWinery("Fautorul", "Moldova");
        addWinery("Cricova", "Moldova");
        addWinery("Chateau", "Moldova");
        addWinery("Milesti", "Moldova");
        addWinery("Purcari", "Moldova");
        addWinery("Asconi", "Moldova");

        mockMvc.perform(get("/api/wineries/paged"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.content").exists())
               .andExpect(jsonPath("$.content").isArray())
               .andExpect(jsonPath("$.content", hasSize(5)))
               .andExpect(jsonPath("$.last", equalTo(false)))
               .andExpect(jsonPath("$.first", equalTo(true)))
               .andExpect(jsonPath("$.numberOfElements", equalTo(5)))
               .andExpect(jsonPath("$.totalElements", equalTo(6)));
    }

    //GET /api/wineries/paged?page=1&size=5
    @Test
    void getPaged_withCustomPageAndSize_returnsCorrectSlice() throws Exception {
        addWinery("Fautorul", "Moldova");
        addWinery("Cricova", "Moldova");
        addWinery("Chateau", "Moldova");
        addWinery("Milesti", "Moldova");
        addWinery("Purcari", "Moldova");
        addWinery("Asconi", "Moldova");
        addWinery("Tomai", "Moldova");
        addWinery("Davidescu", "Moldova");
        addWinery("Radacini", "Moldova");
        addWinery("Vin1", "Moldova");
        addWinery("Vin2", "Moldova");
        addWinery("Vin3", "Moldova");

        mockMvc.perform(get("/api/wineries/paged?page=1&size=5"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.content").exists())
               .andExpect(jsonPath("$.content").isArray())
               .andExpect(jsonPath("$.content", hasSize(5)))
               .andExpect(jsonPath("$.last", equalTo(false)))
               .andExpect(jsonPath("$.first", equalTo(false)))
               .andExpect(jsonPath("$.numberOfElements", equalTo(5)))
               .andExpect(jsonPath("$.totalElements", equalTo(12)));
    }

    //GET /api/wineries/paged?sort=name
    @Test
    void getPaged_whenSortByName_sortsAscending() throws Exception {
        addWinery("Vin1", "Moldova");
        addWinery("Vin2", "Moldova");
        addWinery("Vin3", "Moldova");

        mockMvc.perform(get("/api/wineries/paged?sort=name"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.content").exists())
               .andExpect(jsonPath("$.content").isArray())
               .andExpect(jsonPath("$.content", hasSize(3)))
               .andExpect(jsonPath("$.content[0].name", equalTo("Vin1")))
               .andExpect(jsonPath("$.content[1].name", equalTo("Vin2")))
               .andExpect(jsonPath("$.content[2].name", equalTo("Vin3")));
    }

    //GET /api/wineries/{id} — success
    @Test
    void getById_whenPresent_returnsWinery() throws Exception {
        Long wineryId = addWinery("Fautorul", "Moldova");

        mockMvc.perform(get("/api/wineries/{id}", wineryId))
               .andExpect(status().isOk())
               .andExpect(jsonPath("name", equalTo("Fautorul")))
               .andExpect(jsonPath("country", equalTo("Moldova")))
               .andExpect(jsonPath("id", equalTo(wineryId.intValue())));
    }

    //GET /api/wineries/{id} — not found → 404
    @Test
    void getById_whenMissing_returns404() throws Exception {
        mockMvc.perform(get("/api/wineries/9999"))
               .andExpect(status().isNotFound())
               .andExpect(jsonPath("$.error").value(containsString("Winery not found:")));
    }

    //PUT /api/wineries/{id} — success
    @Test
    void update_whenPresent_returnsUpdatedWinery() throws Exception {
        Long wineryId = addWinery("Fautorul", "Moldova");

        WineryUpdateRequest request = new WineryUpdateRequest("Cricova", "Spain");

        mockMvc.perform(put("/api/wineries/{id}", wineryId).contentType(MediaType.APPLICATION_JSON)
                                                           .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.name", equalTo("Cricova")))
               .andExpect(jsonPath("$.country", equalTo("Spain")))
               .andExpect(jsonPath("$.id", equalTo(wineryId.intValue())));
    }

    //PUT /api/wineries/{id} — fails for winery if wineries with given name country exists
    @Test
    void update_returns409_whenDuplicate() throws Exception {
        Long winery1Id = addWinery("Fautorul", "Moldova");
        Long winery2Id = addWinery("Cricova", "Moldova");

        WineryUpdateRequest request = new WineryUpdateRequest("Fautorul", "Moldova");

        mockMvc.perform(put("/api/wineries/{id}", winery2Id).contentType(MediaType.APPLICATION_JSON)
                                                            .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isConflict())
               .andExpect(jsonPath("$.error").value(containsString("Winery already exists:")));

    }

    //PUT /api/wineries/{id} — validation fails → 400
    @Test
    void update_returns400_whenValidationFails() throws Exception {
        Long wineryId = addWinery("Fautorul", "Moldova");

        String badJson = """
                {
                  "name": "",
                  "country": ""
                }
                """;

        mockMvc.perform(put("/api/wineries/{id}", wineryId).contentType(MediaType.APPLICATION_JSON)
                                                           .content(badJson))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.error").value("Validation failed"))
               .andExpect(jsonPath("$.fields.name", not(emptyOrNullString())))
               .andExpect(jsonPath("$.fields.country", not(emptyOrNullString())));
    }

    //PUT /api/wineries/{id} — not found → 404
    @Test
    void update_whenMissing_returns404() throws Exception {
        WineryUpdateRequest request = new WineryUpdateRequest("Cricova", "Spain");

        mockMvc.perform(put("/api/wineries/9999").contentType(MediaType.APPLICATION_JSON)
                                                 .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isNotFound())
               .andExpect(jsonPath("$.error").value(containsString("Winery not found:")));
    }

    //DELETE /api/wineries/{id}
    @Test
    void delete_whenNoWines_returns204() throws Exception {
        Long wineryId = addWinery("Fautorul", "Moldova");

        mockMvc.perform(delete("/api/wineries/{id}", wineryId))
               .andExpect(status().isNoContent());
    }

    //DELETE /api/wineries/{id} — conflict (has wines) → 409
    @Test
    void delete_whenHasWines_returns409() throws Exception {
        Long wineryId = addWinery("Fautorul", "Moldova");
        wineRepository.save(Wine.builder()
                                .name("Wine1")
                                .wineryRef(wineryRepository.findById(wineryId)
                                                           .orElseThrow())
                                .country("Moldova")
                                .wineYear(2020)
                                .price(BigDecimal.valueOf(11))
                                .build());

        mockMvc.perform(delete("/api/wineries/{id}", wineryId))
               .andExpect(status().isConflict())
               .andExpect(jsonPath("$.error").value(containsString("Cannot delete winery")));
    }

    //DELETE /api/wineries/{id} — not found → 404
    @Test
    void delete_whenMissing_returns404() throws Exception {
        mockMvc.perform(delete("/api/wineries/9999"))
               .andExpect(status().isNotFound())
               .andExpect(jsonPath("$.error").value(containsString("Winery not found:")));
    }

    //GET /api/wineries/{id}/wines — success returns list
    @Test
    void getWinesForWinery_whenPresent_returnsWineList() throws Exception {
        Long wineryId = addWinery("Fautorul", "Moldova");
        wineRepository.save(Wine.builder()
                                .name("Wine1")
                                .wineryRef(wineryRepository.findById(wineryId)
                                                           .orElseThrow())
                                .country("Moldova")
                                .wineYear(2020)
                                .price(BigDecimal.valueOf(11))
                                .build());

        wineRepository.save(Wine.builder()
                                .name("Wine2")
                                .wineryRef(wineryRepository.findById(wineryId)
                                                           .orElseThrow())
                                .country("Moldova")
                                .wineYear(2020)
                                .price(BigDecimal.valueOf(11))
                                .build());

        mockMvc.perform(get("/api/wineries/{id}/wines", wineryId))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", hasSize(2)))
               .andExpect(jsonPath("$[*].winery.id", everyItem(equalTo(wineryId.intValue()))));
    }

    //GET /api/wineries/{id}/wines — winery missing → 404
    @Test
    void getWinesForWinery_whenMissing_returns404() throws Exception {

        mockMvc.perform(get("/api/wineries/9999/wines"))
               .andExpect(status().isNotFound())
               .andExpect(jsonPath("$.error").value(containsString("Winery not found:")));

    }

    private Long addWinery(String name, String country) throws Exception {
        return wineryRepository.save(Winery.builder()
                                           .name(name)
                                           .country(country)
                                           .build())
                               .getId();
    }
}

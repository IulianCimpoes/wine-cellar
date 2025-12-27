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
        wineryRepository.save(Winery.builder()
                                    .name("Fautorul")
                                    .country("Moldova")
                                    .build())
                        .getId();

        wineryRepository.save(Winery.builder()
                                    .name("Cricova")
                                    .country("Moldova")
                                    .build())
                        .getId();

        mockMvc.perform(get("/api/wineries"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", hasSize(2)))
               .andExpect(jsonPath("$[*].id", everyItem(notNullValue())))
               .andExpect(jsonPath("$[*].name", everyItem(notNullValue())))
               .andExpect(jsonPath("$[*].country", everyItem(notNullValue())));
    }

    //GET /api/wineries/{id} — success
    @Test
    void getById_whenPresent_returnsWinery() throws Exception {
        Long wineryId = wineryRepository.save(Winery.builder()
                                                    .name("Fautorul")
                                                    .country("Moldova")
                                                    .build())
                                        .getId();

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
        Long wineryId = wineryRepository.save(Winery.builder()
                                                    .name("Fautorul")
                                                    .country("Moldova")
                                                    .build())
                                        .getId();

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
        Long winery1Id = wineryRepository.save(Winery.builder()
                                                     .name("Fautorul")
                                                     .country("Moldova")
                                                     .build())
                                         .getId();
        Long winery2Id = wineryRepository.save(Winery.builder()
                                                     .name("Cricova")
                                                     .country("Moldova")
                                                     .build())
                                         .getId();

        WineryUpdateRequest request = new WineryUpdateRequest("Fautorul", "Moldova");

        mockMvc.perform(put("/api/wineries/{id}", winery2Id).contentType(MediaType.APPLICATION_JSON)
                                                            .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isConflict())
               .andExpect(jsonPath("$.error").value(containsString("Winery already exists:")));

    }

    //PUT /api/wineries/{id} — validation fails → 400
    @Test
    void update_returns400_whenValidationFails() throws Exception {
        Long wineryId = wineryRepository.save(Winery.builder()
                                                    .name("Fautorul")
                                                    .country("Moldova")
                                                    .build())
                                        .getId();

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
        Long wineryId = wineryRepository.save(Winery.builder()
                                                    .name("Fautorul")
                                                    .country("Moldova")
                                                    .build())
                                        .getId();

        mockMvc.perform(delete("/api/wineries/{id}", wineryId))
               .andExpect(status().isNoContent());
    }

    //DELETE /api/wineries/{id} — conflict (has wines) → 409
    @Test
    void delete_whenHasWines_returns409() throws Exception {
        Long wineryId = wineryRepository.save(Winery.builder()
                                                    .name("Fautorul")
                                                    .country("Moldova")
                                                    .build())
                                        .getId();
        wineRepository.save(Wine.builder()
                                .name("Wine1")
                                .wineryRef(wineryRepository.findById(wineryId)
                                                           .orElseThrow())
                                .country("Moldova")
                                .wineYear(2020)
                                .price(BigDecimal.valueOf(11))
                                .build())
                      .getId();

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
        Long wineryId = wineryRepository.save(Winery.builder()
                                                    .name("Fautorul")
                                                    .country("Moldova")
                                                    .build())
                                        .getId();
        wineRepository.save(Wine.builder()
                                .name("Wine1")
                                .wineryRef(wineryRepository.findById(wineryId)
                                                           .orElseThrow())
                                .country("Moldova")
                                .wineYear(2020)
                                .price(BigDecimal.valueOf(11))
                                .build())
                      .getId();

        wineRepository.save(Wine.builder()
                                .name("Wine2")
                                .wineryRef(wineryRepository.findById(wineryId)
                                                           .orElseThrow())
                                .country("Moldova")
                                .wineYear(2020)
                                .price(BigDecimal.valueOf(11))
                                .build())
                      .getId();

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
}

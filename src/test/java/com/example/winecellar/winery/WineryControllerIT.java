package com.example.winecellar.winery;

import com.example.winecellar.wine.Wine;
import com.example.winecellar.wine.WineRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.stream.IntStream;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

        mockMvc.perform(post("/api/wineries").with(httpBasic("admin", "adminpass"))
                                             .contentType(MediaType.APPLICATION_JSON)
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

        mockMvc.perform(post("/api/wineries").with(httpBasic("admin", "adminpass"))
                                             .contentType(MediaType.APPLICATION_JSON)
                                             .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isOk());

        mockMvc.perform(post("/api/wineries").with(httpBasic("admin", "adminpass"))
                                             .contentType(MediaType.APPLICATION_JSON)
                                             .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isConflict())
               .andExpect(header().exists("X-Request-Id"))
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

        mockMvc.perform(post("/api/wineries").with(httpBasic("admin", "adminpass"))
                                             .contentType(MediaType.APPLICATION_JSON)
                                             .content(badJson))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.error").value("Validation failed"))
               .andExpect(jsonPath("$.fields.name", not(emptyOrNullString())))
               .andExpect(jsonPath("$.fields.country", not(emptyOrNullString())));
    }

    //GET /api/wineries returns list
    @Test
    void getAll_returnsList() throws Exception {
        addWinery("Fautorul", "Moldova", 1L);
        addWinery("Cricova", "Moldova", 1L);

        mockMvc.perform(get("/api/wineries").with(httpBasic("admin", "adminpass")))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", hasSize(2)))
               .andExpect(jsonPath("$[*].id", everyItem(notNullValue())))
               .andExpect(jsonPath("$[*].name", everyItem(notNullValue())))
               .andExpect(jsonPath("$[*].version", everyItem(notNullValue())))
               .andExpect(jsonPath("$[*].country", everyItem(notNullValue())));
    }

    //GET /api/wineries/paged defaults
    @Test
    void getPaged_withDefaults_returnsPage() throws Exception {
        addWinery("Fautorul", "Moldova", 1L);
        addWinery("Cricova", "Moldova", 1L);
        addWinery("Chateau", "Moldova", 1L);
        addWinery("Milesti", "Moldova", 1L);
        addWinery("Purcari", "Moldova", 1L);
        addWinery("Asconi", "Moldova", 1L);

        mockMvc.perform(get("/api/wineries/paged").with(httpBasic("admin", "adminpass")))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.content").exists())
               .andExpect(jsonPath("$.content").isArray())
               .andExpect(jsonPath("$.content", hasSize(5)))
               .andExpect(jsonPath("$.last", equalTo(false)))
               .andExpect(jsonPath("$.first", equalTo(true)))
               .andExpect(jsonPath("$.numberOfElements", equalTo(5)))
               .andExpect(jsonPath("$.totalElements", equalTo(6)));
    }

    @Test
    void v2_returns_enveloped_page_response() throws Exception {
        addWinery("Cricova", "Moldova", 1L);
        addWinery("Milesti", "Moldova", 1L);

        mockMvc.perform(get("/api/v2/wineries").with(httpBasic("user", "userpass"))
                                               .param("page", "0")
                                               .param("size", "5")
                                               .param("sortBy", "name")
                                               .param("direction", "asc"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.data").isArray())
               .andExpect(jsonPath("$.page.number").value(0))
               .andExpect(jsonPath("$.page.size").value(5))
               .andExpect(jsonPath("$.page.totalElements").isNumber())
               .andExpect(jsonPath("$.page.totalPages").isNumber());
    }


    //GET /api/wineries/paged?page=1&size=5
    @Test
    void getPaged_withCustomPageAndSize_returnsCorrectSlice() throws Exception {
        addWinery("Fautorul", "Moldova", 1L);
        addWinery("Cricova", "Moldova", 1L);
        addWinery("Chateau", "Moldova", 1L);
        addWinery("Milesti", "Moldova", 1L);
        addWinery("Purcari", "Moldova", 1L);
        addWinery("Asconi", "Moldova", 1L);
        addWinery("Tomai", "Moldova", 1L);
        addWinery("Davidescu", "Moldova", 1L);
        addWinery("Radacini", "Moldova", 1L);
        addWinery("Vin1", "Moldova", 1L);
        addWinery("Vin2", "Moldova", 1L);
        addWinery("Vin3", "Moldova", 1L);

        mockMvc.perform(get("/api/wineries/paged?page=1&size=5").with(httpBasic("admin", "adminpass")))
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
        addWinery("Vin1", "Moldova", 1L);
        addWinery("Vin2", "Moldova", 1L);
        addWinery("Vin3", "Moldova", 1L);

        mockMvc.perform(get("/api/wineries/paged?sort=name").with(httpBasic("admin", "adminpass")))
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
        Long wineryId = addWinery("Fautorul", "Moldova", 1L);

        mockMvc.perform(get("/api/wineries/{id}", wineryId).with(httpBasic("admin", "adminpass")))
               .andExpect(status().isOk())
               .andExpect(jsonPath("name", equalTo("Fautorul")))
               .andExpect(jsonPath("country", equalTo("Moldova")))
               .andExpect(jsonPath("id", equalTo(wineryId.intValue())));
    }

    //GET /api/wineries/{id} — not found → 404
    @Test
    void getById_whenMissing_returns404() throws Exception {
        mockMvc.perform(get("/api/wineries/9999").with(httpBasic("admin", "adminpass")))
               .andExpect(status().isNotFound())
               .andExpect(jsonPath("$.error").value(containsString("Winery not found:")))
               .andExpect(header().exists("X-Request-Id"));
    }

    //PUT /api/wineries/{id} — success
    @Test
    void update_whenPresent_returnsUpdatedWinery() throws Exception {
        Long currentVersion = 1L;
        Long wineryId = addWinery("Fautorul", "Moldova", currentVersion);

        WineryUpdateRequest request = new WineryUpdateRequest("Cricova", "Spain", currentVersion);

        mockMvc.perform(put("/api/wineries/{id}", wineryId).with(httpBasic("admin", "adminpass"))
                                                           .contentType(MediaType.APPLICATION_JSON)
                                                           .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.name", equalTo("Cricova")))
               .andExpect(jsonPath("$.country", equalTo("Spain")))
               .andExpect(jsonPath("$.version", greaterThan(currentVersion.intValue())))
               .andExpect(jsonPath("$.id", equalTo(wineryId.intValue())));
    }

    //PUT /api/wineries/{id} — fails for winery if wineries with given name country exists
    @Test
    void update_returns409_whenDuplicate() throws Exception {
        Long winery1Id = addWinery("Cricova", "Moldova", 1L);
        Long winery2Id = addWinery("Milesti", "Moldova", 1L);

        WineryUpdateRequest request = new WineryUpdateRequest("Cricova", "Moldova", 1L);

        mockMvc.perform(put("/api/wineries/{id}", winery2Id).with(httpBasic("admin", "adminpass"))
                                                            .contentType(MediaType.APPLICATION_JSON)
                                                            .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isConflict())
               .andExpect(jsonPath("$.error").value(containsString("Winery already exists:")));
    }

    @Test
    void update_returns409_whenVersionIsStale() throws Exception {
        // Arrange
        Long wineryId = addWinery("Cricova", "Moldova", 1L);

        // Get current version
        MvcResult getResult = mockMvc.perform(get("/api/wineries/{id}", wineryId).with(httpBasic("admin", "adminpass")))
                                     .andExpect(status().isOk())
                                     .andReturn();

        JsonNode body = objectMapper.readTree(getResult.getResponse()
                                                       .getContentAsString());
        long v1 = body.get("version")
                      .asLong();

        // Act 1: update with current version => success
        WineryUpdateRequest ok = new WineryUpdateRequest("Cricova Updated", "Moldova", v1);

        mockMvc.perform(put("/api/wineries/{id}", wineryId).with(httpBasic("admin", "adminpass"))
                                                           .contentType(MediaType.APPLICATION_JSON)
                                                           .content(objectMapper.writeValueAsString(ok)))
               .andExpect(status().isOk());

        // Act 2: update again using the OLD version => conflict
        WineryUpdateRequest stale = new WineryUpdateRequest("Cricova Updated Again", "Moldova", v1);

        mockMvc.perform(put("/api/wineries/{id}", wineryId).with(httpBasic("admin", "adminpass"))
                                                           .contentType(MediaType.APPLICATION_JSON)
                                                           .content(objectMapper.writeValueAsString(stale)))
               .andExpect(status().isConflict())
               .andExpect(header().exists("X-Request-Id"))
               .andExpect(jsonPath("$.error", containsString("Winery was updated by another transaction. Please refresh and retry.")));
    }


    //PUT /api/wineries/{id} — validation fails → 400
    @Test
    void update_returns400_whenValidationFails() throws Exception {
        Long wineryId = addWinery("Fautorul", "Moldova", 1L);

        String badJson = """
                {
                  "name": "",
                  "country": "",
                  "version": 1
                }
                """;

        mockMvc.perform(put("/api/wineries/{id}", wineryId).with(httpBasic("admin", "adminpass"))
                                                           .contentType(MediaType.APPLICATION_JSON)
                                                           .content(badJson))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.error").value("Validation failed"))
               .andExpect(jsonPath("$.fields.name", not(emptyOrNullString())))
               .andExpect(header().exists("X-Request-Id"))
               .andExpect(jsonPath("$.fields.country", not(emptyOrNullString())));
    }

    //PUT /api/wineries/{id} — not found → 404
    @Test
    void update_whenMissing_returns404() throws Exception {
        WineryUpdateRequest request = new WineryUpdateRequest("Cricova", "Spain", 1L);

        mockMvc.perform(put("/api/wineries/9999").with(httpBasic("admin", "adminpass"))
                                                 .contentType(MediaType.APPLICATION_JSON)
                                                 .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isNotFound())
               .andExpect(jsonPath("$.error").value(containsString("Winery not found:")));
    }

    @Test
    void patch_updatesOnlyName_andPreservesCountry() throws Exception {
        Long v1 = 1L;
        Long wineryId = addWinery("Cricova", "Moldova", 1L);

        mockMvc.perform(patch("/api/wineries/{id}", wineryId).with(httpBasic("admin", "adminpass"))
                                                             .contentType(MediaType.APPLICATION_JSON)
                                                             .content("""
                                                                       {"name":"Cricova Updated","version":%d}
                                                                     """.formatted(v1)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.name").value("Cricova Updated"))
               .andExpect(jsonPath("$.country").value("Moldova"));
    }

    @Test
    void patch_returns409_whenDuplicate() throws Exception {
        Long version = 1L;
        addWinery("Cricova", "Moldova", version);
        Long winery2Id = addWinery("Milesti", "Moldova", version);

        mockMvc.perform(patch("/api/wineries/{id}", winery2Id).with(httpBasic("admin", "adminpass"))
                                                              .contentType(MediaType.APPLICATION_JSON)
                                                              .content("""
                                                                        {"name":"Cricova","country":"Moldova","version":%d}
                                                                      """.formatted(version)))
               .andExpect(status().isConflict())
               .andExpect(jsonPath("$.error", containsString("Winery already exists:")));
    }

    @Test
    void patch_returns409_whenVersionStale() throws Exception {
        Long version = 1L;
        Long wineryId = addWinery("Cricova", "Moldova", version);

        // first patch with v1 => OK
        mockMvc.perform(patch("/api/wineries/{id}", wineryId).with(httpBasic("admin", "adminpass"))
                                                             .contentType(MediaType.APPLICATION_JSON)
                                                             .content("""
                                                                       {"name":"Cricova Updated","version":%d}
                                                                     """.formatted(version)))
               .andExpect(status().isOk());

        // second patch with old v1 => 409
        mockMvc.perform(patch("/api/wineries/{id}", wineryId).with(httpBasic("admin", "adminpass"))
                                                             .contentType(MediaType.APPLICATION_JSON)
                                                             .content("""
                                                                       {"country":"MD","version":%d}
                                                                     """.formatted(version)))
               .andExpect(status().isConflict())
               .andExpect(jsonPath("$.error", containsString("updated")));
    }

    //DELETE /api/wineries/{id}
    @Test
    void delete_whenNoWines_returns204() throws Exception {
        Long wineryId = addWinery("Fautorul", "Moldova", 1L);

        mockMvc.perform(delete("/api/wineries/{id}", wineryId).with(httpBasic("admin", "adminpass")))
               .andExpect(status().isNoContent());
    }

    //DELETE /api/wineries/{id} — conflict (has wines) → 409
    @Test
    void delete_whenHasWines_returns409() throws Exception {
        Long wineryId = addWinery("Fautorul", "Moldova", 1L);
        wineRepository.save(Wine.builder()
                                .name("Wine1")
                                .wineryRef(wineryRepository.findById(wineryId)
                                                           .orElseThrow())
                                .country("Moldova")
                                .wineYear(2020)
                                .price(BigDecimal.valueOf(11))
                                .version(1L)
                                .build());

        mockMvc.perform(delete("/api/wineries/{id}", wineryId).with(httpBasic("admin", "adminpass")))
               .andExpect(status().isConflict())
               .andExpect(jsonPath("$.error").value(containsString("Cannot delete winery")));
    }

    //DELETE /api/wineries/{id} — not found → 404
    @Test
    void delete_whenMissing_returns404() throws Exception {
        mockMvc.perform(delete("/api/wineries/9999").with(httpBasic("admin", "adminpass")))
               .andExpect(status().isNotFound())
               .andExpect(jsonPath("$.error").value(containsString("Winery not found:")));
    }

    //GET /api/wineries/{id}/wines — success returns list
    @Test
    void getWinesForWinery_whenPresent_returnsWineList() throws Exception {
        Long wineryId = addWinery("Fautorul", "Moldova", 1L);
        wineRepository.save(Wine.builder()
                                .name("Wine1")
                                .wineryRef(wineryRepository.findById(wineryId)
                                                           .orElseThrow())
                                .country("Moldova")
                                .wineYear(2020)
                                .price(BigDecimal.valueOf(11))
                                .version(1L)
                                .build());

        wineRepository.save(Wine.builder()
                                .name("Wine2")
                                .wineryRef(wineryRepository.findById(wineryId)
                                                           .orElseThrow())
                                .country("Moldova")
                                .wineYear(2020)
                                .price(BigDecimal.valueOf(11))
                                .version(1L)
                                .build());

        mockMvc.perform(get("/api/wineries/{id}/wines", wineryId).with(httpBasic("admin", "adminpass")))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", hasSize(2)))
               .andExpect(jsonPath("$[*].winery.id", everyItem(equalTo(wineryId.intValue()))));
    }

    //GET /api/wineries/{id}/wines — winery missing → 404
    @Test
    void getWinesForWinery_whenMissing_returns404() throws Exception {

        mockMvc.perform(get("/api/wineries/9999/wines").with(httpBasic("admin", "adminpass")))
               .andExpect(status().isNotFound())
               .andExpect(jsonPath("$.error").value(containsString("Winery not found:")));

    }

    //GET /api/wineries/{id}/wines/paged
    @Test
    void getWinesForWineryPaged_withDefaults_returnsPage() throws Exception {
        Long wineryId = addWinery("Fautorul", "Moldova", 1L);
        addWinesToWinery(6, "Test Wine12", "Spain", 2024, BigDecimal.valueOf(16), wineryId, 1L);

        mockMvc.perform(get("/api/wineries/{id}/wines/paged", wineryId).with(httpBasic("admin", "adminpass")))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.content").exists())
               .andExpect(jsonPath("$.content").isArray())
               .andExpect(jsonPath("$.content", hasSize(5)))
               .andExpect(jsonPath("$.last", equalTo(false)))
               .andExpect(jsonPath("$.first", equalTo(true)))
               .andExpect(jsonPath("$.content[*].winery.id", everyItem(equalTo(wineryId.intValue()))))
               .andExpect(jsonPath("$.numberOfElements", equalTo(5)))
               .andExpect(jsonPath("$.totalElements", equalTo(6)));
    }

    //GET /api/wineries/{id}/wines/paged?page=1&size=5
    @Test
    void getWinesForWineryPaged_withCustomPageAndSize_returnsCorrectSlice() throws Exception {
        Long wineryId = addWinery("Fautorul", "Moldova", 1L);
        addWinesToWinery(12, "Test Wine12", "Spain", 2024, BigDecimal.valueOf(16), wineryId, 1L);

        mockMvc.perform(get("/api/wineries/{id}/wines/paged?page=1&size=5", wineryId).with(httpBasic("admin", "adminpass")))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.content").exists())
               .andExpect(jsonPath("$.content").isArray())
               .andExpect(jsonPath("$.content", hasSize(5)))
               .andExpect(jsonPath("$.last", equalTo(false)))
               .andExpect(jsonPath("$.first", equalTo(false)))
               .andExpect(jsonPath("$.content[*].winery.id", everyItem(equalTo(wineryId.intValue()))))
               .andExpect(jsonPath("$.pageable.pageNumber", equalTo(1)))
               .andExpect(jsonPath("$.pageable.offset", equalTo(5)))
               .andExpect(jsonPath("$.totalElements", equalTo(12)));
    }

    //GET /api/wineries/{id}/wines/paged?size=3&sort=name
    @Test
    void getWinesForWineryPaged_whenSortByName_sortsAscending() throws Exception {
        Long wineryId = addWinery("Fautorul", "Moldova", 1L);
        addWineToWinery("Test Wine1", "Spain", 2024, BigDecimal.valueOf(16), wineryId, 1L);
        addWineToWinery("Test Wine3", "Spain", 2024, BigDecimal.valueOf(16), wineryId, 1L);
        addWineToWinery("Test Wine2", "Spain", 2024, BigDecimal.valueOf(16), wineryId, 1L);

        mockMvc.perform(get("/api/wineries/{id}/wines/paged?size=3&sort=name", wineryId).with(httpBasic("admin", "adminpass")))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.content[0].name", equalTo("Test Wine1")))
               .andExpect(jsonPath("$.content[1].name", equalTo("Test Wine2")))
               .andExpect(jsonPath("$.content[2].name", equalTo("Test Wine3")));
    }

    //GET /api/wineries/9999/wines/paged
    @Test
    void getWinesForWineryPaged_whenWineryMissing_returns404() throws Exception {

        mockMvc.perform(get("/api/wineries/9999/wines/paged?size=3&sort=name").with(httpBasic("admin", "adminpass")))
               .andExpect(status().isNotFound())
               .andExpect(jsonPath("$.error").value(containsString("Winery not found:")));
    }

    private Long addWinery(String name, String country, Long version) throws Exception {
        return wineryRepository.save(Winery.builder()
                                           .name(name)
                                           .country(country)
                                           .version(version)
                                           .build())
                               .getId();
    }

    private void addWinesToWinery(int winesCount, String name, String country, int wineYear, BigDecimal price, Long wineryId, Long version) throws Exception {
        IntStream.range(0, winesCount)
                 .forEach(i -> addWineToWinery(name + i, country, wineYear + i, price, wineryId, version));
    }


    private long addWineToWinery(String name, String country, int wineYear, BigDecimal price, Long wineryId, Long version) {
        return wineRepository.save(Wine.builder()
                                       .name(name)
                                       .wineryRef(wineryRepository.findById(wineryId)
                                                                  .orElseThrow())
                                       .country(country)
                                       .wineYear(wineYear)
                                       .price(price)
                                       .version(version)
                                       .build())
                             .getId();
    }

    @Test
    void requestId_isGenerated_whenMissing() throws Exception {
        mockMvc.perform(get("/api/wineries").with(httpBasic("user", "userpass")))
               .andExpect(status().isOk())
               .andExpect(header().exists("X-Request-Id"));
    }

    @Test
    void requestId_isEchoed_whenProvided() throws Exception {
        mockMvc.perform(get("/api/wineries").with(httpBasic("user", "userpass"))
                                            .header("X-Request-Id", "test-correlation-id"))
               .andExpect(status().isOk())
               .andExpect(header().string("X-Request-Id", "test-correlation-id"));
    }


}

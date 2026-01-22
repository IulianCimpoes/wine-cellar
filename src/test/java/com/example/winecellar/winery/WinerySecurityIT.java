package com.example.winecellar.winery;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WinerySecurityIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    WineryRepository wineryRepository;

    @BeforeEach
    void setup() {
        wineryRepository.deleteAll();
    }

    @Test
    void unauthenticated_requests_return_401() throws Exception {
        mockMvc.perform(get("/api/wineries"))
               .andExpect(status().isUnauthorized())
               .andExpect(header().exists("X-Request-Id"));
    }

    @Test
    void user_can_read_but_cannot_write() throws Exception {
        mockMvc.perform(get("/api/wineries").with(httpBasic("user", "userpass")))
               .andExpect(status().isOk());

        mockMvc.perform(post("/api/wineries").with(httpBasic("user", "userpass"))
                                             .contentType(MediaType.APPLICATION_JSON)
                                             .content("""
                                                       {"name":"Test Winery","country":"MD"}
                                                     """))
               .andExpect(status().isForbidden());
    }

    @Test
    void admin_can_write() throws Exception {
        mockMvc.perform(post("/api/wineries").with(httpBasic("admin", "adminpass"))
                                             .contentType(MediaType.APPLICATION_JSON)
                                             .content("""
                                                       {"name":"Admin Winery","country":"MD"}
                                                     """))
               .andExpect(status().isOk());
    }

    @Test
    void unauthenticated_write_returns401() throws Exception {
        mockMvc.perform(post("/api/wineries").contentType(MediaType.APPLICATION_JSON)
                                             .content("""
                                                       {"name":"NoAuth Winery","country":"MD"}
                                                     """))
               .andExpect(status().isUnauthorized());
    }

    @Test
    void user_cannot_update_returns403() throws Exception {
        Long wineryId = addWinery("UpdateTest", "MD", 1L);

        mockMvc.perform(put("/api/wineries/{id}", wineryId).with(httpBasic("user", "userpass"))
                                                           .contentType(MediaType.APPLICATION_JSON)
                                                           .content("""
                                                                     {"name":"UpdatedName","country":"MD", "version": 1}
                                                                   """))
               .andExpect(status().isForbidden());
    }

    @Test
    void user_cannot_delete_returns403() throws Exception {
        Long wineryId = addWinery("DeleteTest", "MD", 1L);

        mockMvc.perform(delete("/api/wineries/{id}", wineryId).with(httpBasic("user", "userpass")))
               .andExpect(status().isForbidden());
    }

    @Test
    void admin_can_read() throws Exception {
        mockMvc.perform(get("/api/wineries").with(httpBasic("admin", "adminpass")))
               .andExpect(status().isOk());
    }

    @Test
    void wrong_credentials_return401() throws Exception {
        mockMvc.perform(get("/api/wineries").with(httpBasic("admin", "wrongpass")))
               .andExpect(status().isUnauthorized());
    }


    private Long addWinery(String name, String country, Long version) throws Exception {
        return wineryRepository.save(Winery.builder()
                                           .name(name)
                                           .country(country)
                                           .version(version)
                                           .build())
                               .getId();
    }
}

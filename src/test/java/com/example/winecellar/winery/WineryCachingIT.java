package com.example.winecellar.winery;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
class WineryCachingIT {

    @Autowired
    CacheManager cacheManager;
    @Autowired
    private WineryService wineryService;
    @MockitoSpyBean
    private WineryRepository wineryRepository;

//    @Test
//    @WithMockUser(roles = "USER")
//    void findAll_shouldBeCached() {
//        wineryService.findAll();
//        wineryService.findAll();
//
//        verify(wineryRepository, times(1)).findAll();
//    }

//    @Test
//    @WithMockUser(roles = "ADMIN")
//    void findAll_returnsCachedValue_untilEvicted() {
//        // 1) Ensure baseline state
//        wineryRepository.deleteAll();
//
//        wineryRepository.save(Winery.builder()
//                .name("A")
//                .country("Moldova")
//                .version(0L)
//                .build());
//        wineryRepository.save(Winery.builder()
//                .name("B")
//                .country("Moldova")
//                .version(0L)
//                .build());
//
//        // 2) First call caches
//        var first = wineryService.findAll();
//        int firstSize = first.size();
//
//        // 3) Mutate DB directly (bypass service, so cache is NOT evicted)
//        wineryRepository.save(Winery.builder()
//                .name("C")
//                .country("Moldova")
//                .version(0L)
//                .build());
//
//        // 4) Second call should still be cached (same size as first)
//        var second = wineryService.findAll();
//        assertThat(second).hasSize(firstSize);
//
//        // 5) Clear cache explicitly
//        Cache cache = cacheManager.getCache("wineriesList");
//        assertThat(cache).isNotNull();
//        cache.clear();
//
//        // 6) Third call should now reflect DB change
//        var third = wineryService.findAll();
//        assertThat(third).hasSize(firstSize + 1);
//    }
}

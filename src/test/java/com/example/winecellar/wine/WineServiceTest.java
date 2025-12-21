package com.example.winecellar.wine;

import com.example.winecellar.winery.Winery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class WineServiceTest {

    @Mock
    private WineRepository wineRepository;

    @InjectMocks
    private WineService wineService;

    @Test
    void findById_returnsWine_whenPresent() {
        Wine wine = Wine.builder()
                .id(1L)
                .name("Feteasca Neagra")
                .wineryRef(Winery.builder().id(10L).name("Cricova").country("Moldova").build())
                .country("Moldova")
                .wineYear(2022)
                .price(BigDecimal.valueOf(150))
                .build();

        when(wineRepository.findById(1L)).thenReturn(Optional.of(wine));

        Wine result = wineService.findById(1L);

        assertEquals(1L, result.getId());
        assertEquals("Feteasca Neagra", result.getName());
        verify(wineRepository).findById(1L);
        verifyNoMoreInteractions(wineRepository);
    }

    @Test
    void findById_throwsIllegalArgumentException_whenMissing() {
        when(wineRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> wineService.findById(999L)
        );

        assertTrue(ex.getMessage().contains("Wine not found"));
        verify(wineRepository).findById(999L);
        verifyNoMoreInteractions(wineRepository);
    }

    @Test
    void findByCountry_delegatesToRepository() {
        when(wineRepository.findByCountryIgnoreCase("moldova")).thenReturn(List.of());

        List<Wine> result = wineService.findByCountry("moldova");

        assertNotNull(result);
        verify(wineRepository).findByCountryIgnoreCase("moldova");
        verifyNoMoreInteractions(wineRepository);
    }

    @Test
    void create_savesWine() {
        Wine input = Wine.builder().name("Test").country("Moldova").wineYear(2020).price(BigDecimal.TEN).build();
        Wine saved = Wine.builder().id(1L).name("Test").country("Moldova").wineYear(2020).price(BigDecimal.TEN).build();

        when(wineRepository.save(input)).thenReturn(saved);

        Wine result = wineService.create(input);

        assertEquals(1L, result.getId());
        verify(wineRepository).save(input);
        verifyNoMoreInteractions(wineRepository);
    }
}

package com.example.winecellar.winery;

import com.example.winecellar.wine.Wine;
import com.example.winecellar.wine.WineRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class WineryServiceTest {

    @Mock
    private WineRepository wineRepository;

    @Mock
    private WineryRepository wineryRepository;

    @InjectMocks
    private WineryService wineryService;

    @Test
    void findById_returnsWinery_whenPresent() {
        Winery winery = Winery.builder().id(1L).name("Cricova").country("Moldova").build();

        when(wineryRepository.findById(1L)).thenReturn(Optional.of(winery));

        Winery result = wineryService.findById(1L);

        assertEquals(1L, result.getId());
        assertEquals("Cricova", result.getName());
        verify(wineryRepository).findById(1L);
        verifyNoMoreInteractions(wineryRepository);
    }

    @Test
    void findById_throwsIllegalArgumentException_whenMissing() {
        when(wineryRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> wineryService.findById(999L));

        assertTrue(ex.getMessage().contains("Winery not found"));
        verify(wineryRepository).findById(999L);
        verifyNoMoreInteractions(wineryRepository);
    }

    @Test
    void findAllWineries_whenPresent() {
        var winery1 = Winery.builder().id(1L).name("Winery1").country("Moldova").build();
        var winery2 = Winery.builder().id(2L).name("Winery2").country("Spain").build();

        when(wineryRepository.findAll()).thenReturn(List.of(winery1, winery2));

        var wineries = wineryService.findAll();
        Map<Long, Winery> wineryMap = wineries.stream().collect(Collectors.toMap(Winery::getId, winery -> winery));
        Winery resultWinery1 = wineryMap.get(1L);
        Winery resultWinery2 = wineryMap.get(2L);

        assertEquals(2, wineries.size());
        assertNotNull(resultWinery1);
        assertEquals("Winery1", resultWinery1.getName());
        assertEquals("Moldova", resultWinery1.getCountry());
        assertNotNull(resultWinery2);
        assertEquals("Winery2", resultWinery2.getName());
        assertEquals("Spain", resultWinery2.getCountry());

    }

    @Test
    void updateWinery_whenPresent() {
        var winery = Winery.builder().id(1L).name("Cricova").country("Moldova").build();

        when(wineryRepository.findById(1L)).thenReturn(Optional.of(winery));
        when(wineryRepository.save(winery)).thenReturn(winery);

        var result = wineryService.update(1L, new WineryUpdateRequest("Cricova", "Moldova" ));

        assertEquals(1L, result.getId());
        assertEquals("Cricova", result.getName());
        verify(wineryRepository).save(winery);
        verifyNoMoreInteractions(wineryRepository);
    }

    @Test
    void updateWinery_throwsIllegalArgumentException_whenMissing() {
        var winery = Winery.builder().id(1L).name("Cricova").country("Moldova").build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> wineryService.update(1L, new WineryUpdateRequest("Cricova", "Moldova" )));

        assertTrue(ex.getMessage().contains("Winery not found"));
        verify(wineryRepository).findById(1L);
        verifyNoMoreInteractions(wineryRepository);
    }
}

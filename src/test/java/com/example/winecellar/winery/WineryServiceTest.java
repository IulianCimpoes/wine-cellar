package com.example.winecellar.winery;

import com.example.winecellar.common.exception.ConflictException;
import com.example.winecellar.common.exception.NotFoundException;
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
    void findById_throwsNotFoundException_whenMissing() {
        when(wineryRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class, () -> wineryService.findById(999L));

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

        var result = wineryService.update(1L, new WineryUpdateRequest("Cricova", "Moldova"));

        assertEquals(1L, result.getId());
        assertEquals("Cricova", result.getName());
        verify(wineryRepository).save(winery);
        verify(wineryRepository).findById(1L);
        verify(wineryRepository).existsByNameIgnoreCaseAndCountryIgnoreCaseAndIdNot(winery.getName(), winery.getCountry(), winery.getId());
        verifyNoMoreInteractions(wineryRepository);
    }

    @Test
    void updateWinery_throwsNotFoundException_whenMissing() {
        var winery = Winery.builder().id(1L).name("Cricova").country("Moldova").build();

        NotFoundException ex = assertThrows(NotFoundException.class, () -> wineryService.update(1L, new WineryUpdateRequest("Cricova", "Moldova")));

        assertTrue(ex.getMessage().contains("Winery not found"));
        verify(wineryRepository).findById(1L);
        verifyNoMoreInteractions(wineryRepository);
    }

    @Test
    void deleteWinery_whenPresent_andHaveNoWines() {
        var winery = Winery.builder().id(1L).name("Cricova").country("Moldova").build();

        when(wineryRepository.findById(1L)).thenReturn(Optional.of(winery));
        when(wineRepository.countByWineryRef_Id(1L)).thenReturn(0L);

        wineryService.delete(1L);

        verify(wineryRepository).findById(1L);
        verify(wineRepository).countByWineryRef_Id(1L);
        verify(wineryRepository).delete(winery);
        verifyNoMoreInteractions(wineryRepository, wineRepository);
    }

    @Test
    void deleteWinery_whenPresent_andHaveWines() {
        var winery = Winery.builder().id(1L).name("Cricova").country("Moldova").build();

        when(wineryRepository.findById(1L)).thenReturn(Optional.of(winery));
        when(wineRepository.countByWineryRef_Id(1L)).thenReturn(1L);

        ConflictException ex = assertThrows(ConflictException.class, () -> wineryService.delete(1L));

        assertEquals("Cannot delete winery 1 because it has 1 wines.", ex.getMessage());
        verify(wineryRepository).findById(1L);
        verify(wineRepository).countByWineryRef_Id(1L);
        verify(wineryRepository, never()).delete(any());
        verifyNoMoreInteractions(wineryRepository, wineRepository);
    }

    @Test
    void deleteWinery_whenNotPresent() {

        when(wineryRepository.findById(9999L)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class, () -> wineryService.delete(9999L));

        assertEquals("Winery not found: 9999", ex.getMessage());
        verify(wineryRepository).findById(9999L);
        verifyNoMoreInteractions(wineryRepository);
    }

    @Test
    void create_throwsConflictException_whenDuplicateExists() {
        var winery = Winery.builder().name("Cricova").country("Moldova").build();

        when(wineryRepository.existsByNameIgnoreCaseAndCountryIgnoreCase(winery.getName(), winery.getCountry())).thenReturn(true);

        ConflictException ex = assertThrows(ConflictException.class, () -> wineryService.create(winery));

        assertTrue(ex.getMessage().contains("Winery already exists:"));
        verify(wineryRepository).existsByNameIgnoreCaseAndCountryIgnoreCase(winery.getName(), winery.getCountry());
        verify(wineryRepository, never()).save(any());
        verifyNoMoreInteractions(wineryRepository, wineRepository);
    }

    @Test
    void update_throwsConflictException_whenDuplicateExists() {
        var winery = Winery.builder().id(1L).name("Cricova").country("Moldova").build();

        when(wineryRepository.findById(1L)).thenReturn(Optional.of(winery));
        when(wineryRepository.existsByNameIgnoreCaseAndCountryIgnoreCaseAndIdNot(winery.getName(), winery.getCountry(), winery.getId())).thenReturn(true);

        ConflictException ex = assertThrows(ConflictException.class,
                () -> wineryService.update(winery.getId(), new WineryUpdateRequest(winery.getName(), winery.getCountry())));

        assertTrue(ex.getMessage().contains("Winery already exists:"));
        verify(wineryRepository).existsByNameIgnoreCaseAndCountryIgnoreCaseAndIdNot(winery.getName(), winery.getCountry(), winery.getId());
        verify(wineryRepository, never()).save(any());
        verifyNoMoreInteractions(wineryRepository, wineRepository);
    }
}

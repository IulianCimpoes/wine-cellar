package com.example.winecellar.wine;

import com.example.winecellar.common.exception.ConflictException;
import com.example.winecellar.common.exception.NotFoundException;
import com.example.winecellar.winery.Winery;
import com.example.winecellar.winery.WineryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class WineServiceTest {

    @Mock
    private WineRepository wineRepository;

    @Mock
    private WineryRepository wineryRepository;

    @InjectMocks
    private WineService wineService;

    @Test
    void findById_returnsWine_whenPresent() {
        Wine wine = Wine.builder()
                        .id(1L)
                        .name("Feteasca Neagra")
                        .wineryRef(Winery.builder()
                                         .id(10L)
                                         .name("Cricova")
                                         .country("Moldova")
                                         .build())
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

        NotFoundException ex = assertThrows(NotFoundException.class, () -> wineService.findById(999L));

        assertTrue(ex.getMessage()
                     .contains("Wine not found"));
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
        Winery winery = Winery.builder()
                              .id(1L)
                              .name("Test")
                              .country("Moldova")
                              .build();

        Wine input = Wine.builder()
                         .name("Test")
                         .wineryRef(winery)
                         .country("Moldova")
                         .wineYear(2020)
                         .price(BigDecimal.TEN)
                         .build();
        Wine saved = Wine.builder()
                         .id(1L)
                         .name("Test")
                         .wineryRef(winery)
                         .country("Moldova")
                         .wineYear(2020)
                         .price(BigDecimal.TEN)
                         .build();

        when(wineRepository.save(input)).thenReturn(saved);

        Wine result = wineService.create(input);

        assertEquals(1L, result.getId());
        verify(wineRepository).existsByNameIgnoreCaseAndWineYearAndWineryRef_Id("Test", 2020, 1L);
        verify(wineRepository).save(input);
        verifyNoMoreInteractions(wineRepository);
    }

    @Test
    void create_throwsConflictException_whenDuplicateExists() {
        var winery = Winery.builder().id(10L).name("Cricova").country("Moldova").build();
        var wine = Wine.builder()
                       .name("Feteasca Neagra")
                       .wineryRef(winery)
                       .country("Moldova")
                       .wineYear(2022)
                       .price(BigDecimal.valueOf(150))
                       .build();

        when(wineRepository.existsByNameIgnoreCaseAndWineYearAndWineryRef_Id(
                wine.getName(), wine.getWineYear(), winery.getId()))
                .thenReturn(true);

        ConflictException ex = assertThrows(ConflictException.class, () -> wineService.create(wine));

        assertTrue(ex.getMessage().contains("Wine already exists:"));
        verify(wineRepository).existsByNameIgnoreCaseAndWineYearAndWineryRef_Id(
                wine.getName(), wine.getWineYear(), winery.getId());
        verify(wineRepository, never()).save(any());
        verifyNoMoreInteractions(wineRepository);
    }

//    @Test
//    void update_throwsConflictException_whenDuplicateExists() {
//        Long wineId = 1L;
//        Long targetWineryId = 10L;
//
//        var existingWine = Wine.builder()
//                               .id(wineId)
//                               .name("OldName")
//                               .country("OldCountry")
//                               .wineYear(2020)
//                               .price(BigDecimal.TEN)
//                               .wineryRef(Winery.builder().id(5L).name("OldWinery").country("Moldova").build())
//                               .build();
//
//        var request = new WineUpdateRequest(
//                "Feteasca Neagra",
//                targetWineryId,
//                "Moldova",
//                2022,
//                BigDecimal.valueOf(150)
//        );
//
//        when(wineRepository.findById(wineId)).thenReturn(Optional.of(existingWine));
//        when(wineryRepository.findById(targetWineryId))
//                .thenReturn(Optional.of(Winery.builder().id(targetWineryId).name("Cricova").country("Moldova").build()));
//
//        when(wineRepository.existsByNameIgnoreCaseAndWineYearAndWineryRef_IdAndIdNot(
//                request.name(), request.wineYear(), request.wineryId(), wineId))
//                .thenReturn(true);
//
//        ConflictException ex = assertThrows(ConflictException.class, () -> wineService.update(wineId, request));
//
//        assertTrue(ex.getMessage().contains("Wine already exists:"));
//
//        verify(wineRepository).findById(wineId);
//        verify(wineryRepository).findById(targetWineryId);
//        verify(wineRepository).existsByNameIgnoreCaseAndWineYearAndWineryRef_IdAndIdNot(
//                request.name(), request.wineYear(), request.wineryId(), wineId);
//        verify(wineRepository, never()).save(any());
//
//        verifyNoMoreInteractions(wineRepository, wineryRepository);
//    }


}

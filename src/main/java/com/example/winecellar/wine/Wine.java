package com.example.winecellar.wine;

import com.example.winecellar.winery.Winery;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winery_id")
    private Winery wineryRef;

    private String country;
    private int wineYear;
    private BigDecimal price;
}


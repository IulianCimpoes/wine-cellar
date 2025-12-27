package com.example.winecellar.winery;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "winery",
        uniqueConstraints = @UniqueConstraint(name = "uk_winery_name_country", columnNames = {"name", "country"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Winery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String country;
}

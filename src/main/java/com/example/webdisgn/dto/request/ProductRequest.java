package com.example.webdisgn.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import com.example.webdisgn.model.Category;

@Data
public class ProductRequest {
    @NotBlank(message = "Il nome del prodotto è obbligatorio")
    private String name;

    @Positive(message = "Il prezzo deve essere positivo")
    private double price;

    @Min(value = 0, message = "Lo stock deve essere positivo o zero")
    private int stock;

    @NotBlank
    @Size(max = 1000)
    private String description;

    private Category category;


}

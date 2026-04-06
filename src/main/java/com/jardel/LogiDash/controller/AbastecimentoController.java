package com.jardel.LogiDash.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/abastecimento")
public class AbastecimentoController {
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public String abastecimento() {
        return "Abastecimento";
    }
}


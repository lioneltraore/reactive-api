package com.gexcode.reactive.model.dto;

import org.springframework.data.annotation.Id;

public record UserDTO(Long id, String name, String email) {
}
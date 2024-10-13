package com.alex.spring.security.demo.controllers.dto;

import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
public record AuthCreateRoleRequest(@Size(max = 3,message = "The user cannot have more than three roles") List<String> roleListName) {
}

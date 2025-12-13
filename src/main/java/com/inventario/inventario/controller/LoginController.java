package com.inventario.inventario.controller;

import com.inventario.inventario.Entity.ERole;
import com.inventario.inventario.Entity.RoleEntity;
import com.inventario.inventario.Entity.UserEntity;
import com.inventario.inventario.controller.request.CreateUserDTO;
import com.inventario.inventario.repository.UserEntityRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/login")
@CrossOrigin( origins = "https://inventario-7yzy.onrender.com")
public class LoginController {

    @Autowired
    private UserEntityRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Endpoint solo para probar admin
    @GetMapping
    public String login() {
        return "Welcome";
    }

    // Crear usuario
    @PostMapping("/create")
    public ResponseEntity<?> createUser(@Valid @RequestBody CreateUserDTO createUserDTO) {

        // ✅ Rol USER asignado automáticamente
        Set<RoleEntity> roles = Set.of(
                RoleEntity.builder()
                        .name(ERole.USER)
                        .build()
        );

        UserEntity userEntity = UserEntity.builder()
                .username(createUserDTO.getUsername())
                .password(passwordEncoder.encode(createUserDTO.getPassword()))
                .roles(roles)
                .build();

        usuarioRepository.save(userEntity);

        return ResponseEntity.ok(userEntity);
    }


    // Eliminar usuario
    @DeleteMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        usuarioRepository.deleteById(id);
        return "Se ha borrado correctamente";
    }

    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticate(@RequestBody CreateUserDTO loginDTO) {
        UserEntity user = usuarioRepository.findByUsername(loginDTO.getUsername()).orElse(null);

        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Usuario no encontrado"));
        }

        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            return ResponseEntity.status(401).body(Map.of("message", "Contraseña incorrecta"));
        }

        // Obtener roles
        Set<String> roles = user.getRoles().stream()
                .map(r -> r.getName().name())
                .collect(Collectors.toSet());

        return ResponseEntity.ok(Map.of(
                "message", "Login exitoso",
                "roles", roles
        ));
    }


}

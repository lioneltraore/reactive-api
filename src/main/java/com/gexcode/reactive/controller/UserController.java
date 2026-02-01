package com.gexcode.reactive.controller;

import com.gexcode.reactive.exception.EmailUniquenessException;
import com.gexcode.reactive.model.dto.UserDTO;
import com.gexcode.reactive.model.entity.User;
import com.gexcode.reactive.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping
    public Mono<ResponseEntity<User>> createUser(@RequestBody UserDTO userDto) { // J'ai renommé 'user' en 'userDto' pour la clarté
        return userRepository.findByEmail(userDto.email())
                // 1. Si l'email existe, on renvoie une erreur.
                // Note: On type le Mono.error pour aider le compilateur
                .flatMap(existingUser -> Mono.<User>error(new EmailUniquenessException("Email already exists!")))

                // 2. Si le findByEmail est vide (switchIfEmpty), on crée l'utilisateur.
                .switchIfEmpty(Mono.defer(() -> {
                    // MAPPING : Conversion DTO -> Entity
                    User newUser = new User();
                    newUser.setEmail(userDto.email());
                    newUser.setName(userDto.name());

                    return userRepository.save(newUser);
                }))

                // 3. Transformation en ResponseEntity
                .map(ResponseEntity::ok)

                .doOnNext(savedUser -> System.out.println("New User created: " + savedUser))

                .onErrorResume(e -> {
                    System.out.println("An exception has occured: " + e.getMessage());
                    if (e instanceof EmailUniquenessException) {
                        return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).build());
                    } else {
                        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                    }
                });
    }

    @GetMapping
    public Flux<User> getAllUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/{id}")
    public Mono<User> getUserByID(@PathVariable Long id) {
        return userRepository.findById(id);
    }

    @DeleteMapping("/{id}")
    public Mono<Void> deleteUser(@PathVariable Long id) {
        return userRepository.deleteById(id);
    }
}

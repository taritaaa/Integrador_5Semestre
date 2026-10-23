package dev.tarazonarodriguezrojas.taller1.model.entity;

import dev.tarazonarodriguezrojas.taller1.model.constant.Role;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
/**
 * Usuario del sistema: administrador o mecánico.
 *
 * La tabla se llama "users" porque "user" es una palabra reservada
 * en la base de datos H2 y daría error.
 *
 * Relación:
 * - Un usuario (mecánico) atiende muchas reparaciones.
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "document", nullable = false, unique = true, length = 20)
    private String document;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;
    // Contraseña ya cifrada (hash). Nunca se guarda la contraseña en texto plano.
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Role role;

    // Indica si la cuenta está activa. Desactivar es mejor que borrar,
    // porque así se conserva el historial de reparaciones del mecánico.
    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    // Token temporal para recuperar la contraseña.
    // Es nulo cuando no hay una solicitud pendiente.
    @Column(name = "recovery_token", length = 100)
    private String recoveryToken;

    // Fecha y hora en que el token deja de ser válido.
    // Es nulo cuando no hay una solicitud pendiente.
    @Column(name = "token_expiration")
    private LocalDateTime tokenExpiration;

    @OneToMany(mappedBy = "mechanic")
    private List<Repair> repairs = new ArrayList<>();

    public User() {
    }

    public User(String name, String document, String email, String phone,
                String passwordHash, Role role) {
        this.name = name;
        this.document = document;
        this.email = email;
        this.phone = phone;
        this.passwordHash = passwordHash;
        this.role = role;
        this.active = true;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDocument() {
        return document;
    }

    public void setDocument(String document) {
        this.document = document;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
    // Para atributos boolean, el getter se escribe "is" en lugar de "get"
    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getRecoveryToken() {
        return recoveryToken;
    }

    public void setRecoveryToken(String recoveryToken) {
        this.recoveryToken = recoveryToken;
    }

    public LocalDateTime getTokenExpiration() {
        return tokenExpiration;
    }

    public void setTokenExpiration(LocalDateTime tokenExpiration) {
        this.tokenExpiration = tokenExpiration;
    }

    public List<Repair> getRepairs() {
        return repairs;
    }

    public void setRepairs(List<Repair> repairs) {
        this.repairs = repairs;
    }
}
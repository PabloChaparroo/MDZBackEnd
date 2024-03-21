package com.Capinteria.carpinteria.Entity;

import com.Capinteria.carpinteria.enumeration.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
//import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;


import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "usuario", uniqueConstraints = {@UniqueConstraint(columnNames = {"username"})})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario extends BaseEntity implements UserDetails {

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "role")
    private Role role;

    @Column(name = "fecha_alta_usuario")
    private LocalDate fechaAltaUsuario;

    @Column(name = "fecha_baja_usuario")
    private LocalDate fechaBajaUsuario;

    @Column(name = "fecha_modificacion_usuario")
    private LocalDate fechaModificacionUsuario;

    //Relacion cliente
    @OneToOne(cascade = CascadeType.PERSIST, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
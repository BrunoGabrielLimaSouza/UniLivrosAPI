package com.unilivros.dto;

public class AuthResponseDTO {

    private String token;
    private String type = "Bearer";


    private UsuarioDTO user;

    public AuthResponseDTO() {}

    public AuthResponseDTO(String token, UsuarioDTO user) {
        this.token = token;
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public UsuarioDTO getUser() { // Getter ajustado
        return user;
    }

    public void setUser(UsuarioDTO user) { // Setter ajustado
        this.user = user;
    }
}
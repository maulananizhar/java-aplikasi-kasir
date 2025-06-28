package com.kasir.model;

public class Kasir {
  private String uuid;
  private String nama;
  private String username;
  private String password;
  private String jabatan;

  public Kasir(String uuid, String nama, String username, String password, String jabatan) {
    this.uuid = uuid;
    this.nama = nama;
    this.username = username;
    this.password = password;
    this.jabatan = jabatan;
  }

  public String getUuid() {
    return uuid;
  }

  public String getNama() {
    return nama;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  public String getJabatan() {
    return jabatan;
  }
}

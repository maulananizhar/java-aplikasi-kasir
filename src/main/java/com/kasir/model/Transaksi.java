package com.kasir.model;

import java.sql.Timestamp;

public class Transaksi {
  private String uuid;
  private Timestamp tanggal;
  private Float totalHarga;
  private String kasir;
  private String namaKasir;
  private String status;

  public Transaksi(String uuid, Timestamp tanggal, Float totalHarga, String kasir, String namaKasir, String status) {
    this.uuid = uuid;
    this.tanggal = tanggal;
    this.totalHarga = totalHarga;
    this.kasir = kasir;
    this.namaKasir = namaKasir;
    this.status = status;
  }

  public String getUuid() {
    return uuid;
  }

  public Timestamp getTanggal() {
    return tanggal;
  }

  public Float getTotalHarga() {
    return totalHarga;
  }

  public String getKasir() {
    return kasir;
  }

  public String getNamaKasir() {
    return namaKasir;
  }

  public String getStatus() {
    return status;
  }
}

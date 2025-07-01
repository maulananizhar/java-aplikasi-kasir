package com.kasir.model;

public class Laporan {
  private String nama;
  private int totalTerjual;
  private float totalHarga;

  public Laporan(String nama, int totalTerjual, float totalHarga) {
    this.nama = nama;
    this.totalTerjual = totalTerjual;
    this.totalHarga = totalHarga;
  }

  public String getNama() {
    return nama;
  }

  public int getTotalTerjual() {
    return totalTerjual;
  }

  public float getTotalHarga() {
    return totalHarga;
  }
}

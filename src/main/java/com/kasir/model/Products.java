package com.kasir.model;

public class Products {
  private String id;
  private String nama;
  private String kategori;
  private float harga;
  private int stok;

  public Products(String id, String nama, String kategori, float harga, int stok) {
    this.id = id;
    this.nama = nama;
    this.kategori = kategori;
    this.harga = harga;
    this.stok = stok;
  }

  public String getId() {
    return id;
  }

  public String getNama() {
    return nama;
  }

  public String getKategori() {
    return kategori;
  }

  public float getHarga() {
    return harga;
  }

  public int getStok() {
    return stok;
  }
}

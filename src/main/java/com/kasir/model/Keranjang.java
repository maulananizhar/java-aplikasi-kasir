package com.kasir.model;

public class Keranjang {
  private String uuid;
  private String namaProduk;
  private float harga;
  private int kuantitas;
  private float jumlah;

  private String transaksiId;
  private String produkId;

  public Keranjang(String uuid, String namaProduk, float harga, int kuantitas, float jumlah, String transaksiId,
      String produkId) {
    this.uuid = uuid;
    this.namaProduk = namaProduk;
    this.harga = harga;
    this.kuantitas = kuantitas;
    this.jumlah = jumlah;
    this.transaksiId = transaksiId;
    this.produkId = produkId;
  }

  public String getUuid() {
    return uuid;
  }

  public String getNamaProduk() {
    return namaProduk;
  }

  public float getHarga() {
    return harga;
  }

  public int getKuantitas() {
    return kuantitas;
  }

  public float getJumlah() {
    return jumlah;
  }

  public String getTransaksiId() {
    return transaksiId;
  }

  public String getProdukId() {
    return produkId;
  }

}

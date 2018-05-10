package ru.csi.test.hbn_entities;

import ru.csi.test.entities.Data;

import javax.persistence.*;
import java.util.Objects;

@Entity
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "product_code")
    private String productCode;

    @Column
    private Integer depart; // номер отдела

    public Item() {}

    public Item(Integer id) {
        this.id = id;
    }

    public Item(String productCode, Integer depart) {
        this.productCode = productCode;
        this.depart = depart;
    }

    public static Item create(Data data) {
        Item res = new Item();
        res.setDepart(data.getDepart());
        res.setProductCode(data.getProductCode());
        return res;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public Integer getDepart() {
        return depart;
    }

    public void setDepart(Integer depart) {
        this.depart = depart;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return id != null && Objects.equals(id, item.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Item{" +
                "id=" + id +
                ", productCode='" + productCode + '\'' +
                ", depart=" + depart +
                '}';
    }
}

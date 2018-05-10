package ru.csi.test.hbn_entities;

import org.apache.commons.lang3.time.DateUtils;
import ru.csi.test.entities.Data;

import javax.persistence.*;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

@Entity
public class Price {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // идентификатор в БД

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Item item; // код товара

    @Column
    private Integer number; // номер цены

    @Column(name = "begin_date")
    private Date beginDate; // начало действия

    @Column(name = "end_date")
    private Date endDate; // конец действия

    @Column
    private Integer value; // значение цены в копейках

    public Price() {
    }

    public Price(Long id) {
        this.id = id;
    }

    public static Price create(Data data, Item item) {
        Price price = new Price();
        price.setNumber(data.getNumber());
        price.setBeginDate(data.getBegin());
        price.setEndDate(data.getEnd());
        price.setValue(data.getValue());
        price.setItem(item);
        return price;
    }

    public Price copy() {
        Price price = new Price();
        price.setItem(item);
        price.setNumber(number);
        price.setBeginDate(beginDate);
        price.setEndDate(endDate);
        price.setValue(value);
        return price;
    }

    private static Date nearest(Date date) {
        return DateUtils.truncate(DateUtils.addHours(date, 12), Calendar.DATE);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public Date getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(Date beginDate) {
        this.beginDate = nearest(beginDate);
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = DateUtils.addSeconds(nearest(endDate), -1);
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Price price = (Price) o;
        return id != null && Objects.equals(id, price.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Price{" +
                "id=" + id +
                ", item=" + item +
                ", number=" + number +
                ", beginDate=" + beginDate +
                ", endDate=" + endDate +
                ", value=" + value +
                '}';
    }
}

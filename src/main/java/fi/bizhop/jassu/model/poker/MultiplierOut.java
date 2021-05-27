package fi.bizhop.jassu.model.poker;

import java.math.BigDecimal;

public class MultiplierOut implements Comparable<MultiplierOut> {
    private String name;
    private BigDecimal value;

    public MultiplierOut() {}

    public MultiplierOut(String name, BigDecimal value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getValue() {
        return this.value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    @Override
    public int compareTo(MultiplierOut other) {
        return this.value.compareTo(other.value);
    }
}

package gov.procure.shared.domain;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;

/**
 * Money value object. The ONLY representation of monetary amounts in the platform.
 * Never use double/float for money.
 */
public record Money(BigDecimal amount, Currency currency) {

    public Money {
        Objects.requireNonNull(amount, "amount");
        Objects.requireNonNull(currency, "currency");
        if (amount.scale() > currency.getDefaultFractionDigits()) {
            amount = amount.setScale(currency.getDefaultFractionDigits(), java.math.RoundingMode.HALF_EVEN);
        }
    }

    public static Money of(String amount, String currencyCode) {
        return new Money(new BigDecimal(amount), Currency.getInstance(currencyCode));
    }

    public static Money zero(Currency currency) {
        return new Money(BigDecimal.ZERO, currency);
    }

    public Money add(Money other) {
        requireSameCurrency(other);
        return new Money(this.amount.add(other.amount), currency);
    }

    public Money multiply(BigDecimal factor) {
        return new Money(this.amount.multiply(factor), currency);
    }

    public boolean isGreaterThan(Money other) {
        requireSameCurrency(other);
        return this.amount.compareTo(other.amount) > 0;
    }

    private void requireSameCurrency(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                "Currency mismatch: %s vs %s".formatted(currency, other.currency));
        }
    }
}

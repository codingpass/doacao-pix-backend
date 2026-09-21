package br.com.pixdonation;

import br.com.pixdonation.validator.DonationValidator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitarios para o DonationValidator.
 * Nao necessitam de banco de dados ou contexto Spring.
 */
class DonationValidatorTest {

    private final DonationValidator validator = new DonationValidator();

    @Test
    void deveAceitarValorNoLimiteMinimo() {
        assertDoesNotThrow(() -> validator.validate(1500L));
    }

    @Test
    void deveAceitarValorNoLimiteMaximo() {
        assertDoesNotThrow(() -> validator.validate(100_000L));
    }

    @Test
    void deveLancarExcecaoParaValorAbaixoDoMinimo() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(1499L)
        );
        assertTrue(ex.getMessage().contains("minimo"));
        assertTrue(ex.getMessage().contains("1499"));
    }

    @Test
    void deveLancarExcecaoParaValorAcimaDoMaximo() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(100_001L)
        );
        assertTrue(ex.getMessage().contains("maximo"));
        assertTrue(ex.getMessage().contains("100001"));
    }

    @Test
    void deveLancarExcecaoParaValorZero() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(0L)
        );
        assertTrue(ex.getMessage().contains("minimo"));
    }
}
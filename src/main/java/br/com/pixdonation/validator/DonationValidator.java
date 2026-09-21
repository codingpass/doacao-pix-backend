package br.com.pixdonation.validator;

import org.springframework.stereotype.Component;

/**
 * Valida o valor de uma doacao antes de processa-la.
 *
 * Regras de negocio:
 *   - Valor minimo: 1500 centavos (R$ 15,00)
 *   - Valor maximo: 100000 centavos (R$ 1.000,00)
 *
 * Valores SEMPRE em centavos (long). Nunca use double para dinheiro.
 */
@Component
public class DonationValidator {

    private static final long MINIMUM_AMOUNT_CENTS = 1500L;
    private static final long MAXIMUM_AMOUNT_CENTS = 100_000L;

    /**
     * Valida o valor da doacao em centavos.
     *
     * @param amountCents valor em centavos informado pelo cliente
     * @throws IllegalArgumentException se o valor estiver fora do intervalo permitido
     */
    public void validate(long amountCents) {
        if (amountCents < MINIMUM_AMOUNT_CENTS) {
            throw new IllegalArgumentException(
                "O valor da doacao deve ser de no minimo R$ 15,00 (1500 centavos). "
                + "Valor informado: " + amountCents + " centavos."
            );
        }

        if (amountCents > MAXIMUM_AMOUNT_CENTS) {
            throw new IllegalArgumentException(
                "O valor da doacao deve ser de no maximo R$ 1.000,00 (100000 centavos). "
                + "Valor informado: " + amountCents + " centavos."
            );
        }
    }
}
package com.evolve.domain;

import lombok.*;
import org.dizitart.no2.repository.annotations.Id;

import java.util.Arrays;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    @Id
    private String accountId;
    private String accountName; // imię i nazwisko albo coś innego


    /**
     * Reference to {@link Person}, based on {@link Account#accountId} <br/>
     * Maybe {@code null} when account does not belong to a person
     */
    private String personId;

    private String unitNumber;

    private AccountType accountType;

    @RequiredArgsConstructor
    @Getter
    public enum AccountType {
        FEES("200", "składki"),
        LOANS("201", "pożyczki"),
        PAYDAY_LOANS("203", "chwilówki"),
        DEATH_BENEFITS("807", "odprawy pośmiertne");

        private final String code;
        private final String description;

        public static AccountType of(String givenCode) {
            return Arrays.stream(AccountType.values())
                    .filter(type -> type.code.equals(givenCode))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public String toString() {
            return this.description;
        }
    }

}

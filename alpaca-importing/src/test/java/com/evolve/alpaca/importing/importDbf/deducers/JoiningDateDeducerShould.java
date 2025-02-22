package com.evolve.alpaca.importing.importDbf.deducers;

import com.evolve.domain.PersonStatusChange;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class JoiningDateDeducerShould {
    @Mock IssuesLogger.ImportIssues importIssues;
    @InjectMocks JoiningDateDeducer deducer;

    @Test
    void notDeduceJoinedDateFromDeceasedDate() {
        final List<String> guesses = List.of("ALEKSANDRA 10.05.33 V / 7",
                "c.Magdalena WesołowskaKie",
                 "Al.Niepodległości 11/7",
                 "39-300 Mielec",
                 "zm. 19.07.2023",
                "");

        var result = deducer.deduceFrom(guesses);

        assertThat(result)
                .isEmpty();
    }

    @Test
    void adjustJoinedDateToCurrentCentury() {
        final List<String> guesses = List.of("19.10.70 17.02.04");

        var result = deducer.deduceFrom(guesses);

        assertThat(result)
                .hasValue(PersonStatusChange.joined(LocalDate.of(2004, 2, 17), "17.02.04"));
    }

    @Test
    void deduceJoiningDateInRomanNumber() {
        assertThat(deducer.deduceFrom(List.of("15.12.84  I-2014")))
                .hasValue(PersonStatusChange.joined(LocalDate.of(2014, Month.JANUARY, 1), "I-2014"));

        assertThat(deducer.deduceFrom(List.of("2.01.65  i / 96")))
                .hasValue(PersonStatusChange.joined(LocalDate.of(1996, Month.JANUARY, 1), "i / 96"));

        assertThat(deducer.deduceFrom(List.of("14.08.87  iii/ 2013")))
                .hasValue(PersonStatusChange.joined(LocalDate.of(2013, Month.MARCH, 1), "iii/ 2013"));

    }


}
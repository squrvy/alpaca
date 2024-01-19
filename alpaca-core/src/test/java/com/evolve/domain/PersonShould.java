package com.evolve.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static com.evolve.domain.PersonAssertion.assertPerson;

class PersonShould {

    final LocalDate dob = LocalDate.of(1990, 10, 10);
    final LocalDate death = LocalDate.of(2020, 4, 10);
    final LocalDate joined = LocalDate.of(2001, 7, 1);

    @Test
    void alwaysPutDeathDateAsTheLastStatus() {
        // given
        Person person = new Person();

        // when
        person.addOrUpdateStatusChange(PersonStatusChange.EventType.BORN, dob);
        person.addOrUpdateStatusChange(PersonStatusChange.EventType.DIED, death);
        person.addOrUpdateStatusChange(PersonStatusChange.EventType.JOINED, joined);

        // then
        assertPerson(person)
                .hasStatusHistory(PersonStatusChange.born(dob),
                        PersonStatusChange.joined(joined),
                        PersonStatusChange.died(death));
    }

}
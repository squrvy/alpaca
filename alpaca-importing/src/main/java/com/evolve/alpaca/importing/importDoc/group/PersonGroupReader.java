package com.evolve.alpaca.importing.importDoc.group;

import com.evolve.alpaca.importing.PersonStatusDetails;
import com.evolve.alpaca.importing.importDoc.person.PersonFromDoc;
import com.evolve.domain.RegistryNumber;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class PersonGroupReader {

    public static final Pattern ZM_REGEX = Pattern.compile("ZM\\s\\d{2}\\.\\d{2}\\.\\d{2}");

    /**
     * Recognize Strings as: REZ XII-99
     * Recognized Roman numerals: I, II, III, IV, V, VI, VII, VIII, IX, X, XI, XII
     */
    public static final Pattern REZ_REGEX = Pattern.compile("REZ\\s[IVX]{1,4}-\\d{2}");

    public static Optional<PersonFromDoc> fromLine(String line) {
        try {

        final PersonStatusDetails personStatusDetails = decodeStatus(line);
        final StringTokenizer tokenizer = new StringTokenizer(line);

        final String numerKartoteki = line.startsWith("\t") ? null : tokenizer.nextToken();
        final RegistryNumber registryNumber = RegistryNumber.of(numerKartoteki);

        if (!tokenizer.hasMoreTokens()) {
            log.info("Line is not finished on: {}", numerKartoteki);
            return Optional.empty();
        }

        final String numerJednostki = tokenizer.nextToken();
        final String numberGrupy = tokenizer.nextToken();
        final String index = tokenizer.nextToken();
        final String lastName = tokenizer.nextToken();
        final String firstName = tokenizer.nextToken();

        return Optional.of(PersonFromDoc.builder()
                .numerKartoteki(registryNumber)
                .numerJednostki(numerJednostki)
                .numerGrupy(numberGrupy)
                .index(index)
                .lastName(lastName)
                .firstName(firstName)
                .statusDetails(personStatusDetails)
                .build());
        } catch(Exception exception) {
            log.warn("FAILED to read {}", line);
            throw exception;
        }
    }

    public static PersonStatusDetails decodeStatus(String line) {
        final Matcher deadMatcher = ZM_REGEX.matcher(line);
        if (deadMatcher.find()) {
            final String deathDate = deadMatcher.group(0).replace("ZM ", "");
            return PersonStatusDetails.dead(deathDate);
        }
        final Matcher resignationMatcher = REZ_REGEX.matcher(line);
        if (resignationMatcher.find()) {
            final String resignationDate = resignationMatcher.group(0).replace("REZ ", "");
            return PersonStatusDetails.resigned(resignationDate);
        }

        return PersonStatusDetails.active();
    }

}

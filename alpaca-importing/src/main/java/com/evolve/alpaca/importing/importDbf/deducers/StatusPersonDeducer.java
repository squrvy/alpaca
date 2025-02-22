package com.evolve.alpaca.importing.importDbf.deducers;

import com.evolve.alpaca.importing.DateParser;
import com.evolve.alpaca.importing.PersonStatusDetails;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class StatusPersonDeducer implements SmartDeducer<PersonStatusDetails> {

    //NOTE: the order in the following lists is important:
    public static final List<String> DECEASED = List.of("ZMARŁA", "ZMARŁ", "ZM.", "ZM ");
    public static final List<String> RESIGNED = List.of("rez zw skł", "rez zwr skł",
            "rezygnacja", "REZ.", "Rezyg.", "rezy.", "rezy", "REZ");

    public static final List<String> REMOVED = List.of("Skreślenie",
            "skreślony",
            "skreśl.", "Skreśl", "skreśl",
            "SKRŚL",
            "skr. zw skł",
            "skr zwr skł",
            "skr. zwr skł",
            "skr zw skł",
            "sker zw skł",
            "skre", "SKR", "sk.", "sk-");

    @Override
    public Optional<PersonStatusDetails> deduceFrom(List<String> guesses) {
        final List<String> sanitized = guesses.stream()
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .collect(Collectors.toList());

        final Optional<PersonStatusDetails> maybeDead = detectDeceased(sanitized);
        if (maybeDead.isPresent()) {
            return maybeDead;
        }

        final Optional<PersonStatusDetails> maybeResigned = detectResigned(sanitized);
        if (maybeResigned.isPresent()) {
            return maybeResigned;
        }

        return detectRemoved(sanitized);
    }

    private Optional<PersonStatusDetails> detectDeceased(List<String> guesses) {
        // pre-filter to detect statuses without exact dates
        final List<String> dead = List.of("zmar"); // bad spelling!
        if (guesses.stream()
                .anyMatch(guess -> anyEqualsIgnoreCase(dead, guess))) {
            return Optional.of(PersonStatusDetails.dead());
        }

        return guesses.stream()
                .filter(guess -> !StringUtils.startsWith(guess, "zm. nazwiska")) // exception!
                .filter(guess -> !StringUtils.startsWith(guess, "zm. os. up"))
                .filter(guess -> !StringUtils.startsWith(guess, "zm.os.up."))
                .filter(guess -> !StringUtils.startsWith(guess, "zm. os up."))
                .filter(guess -> !StringUtils.startsWith(guess, "zm. adresu"))
                .filter(guess -> containsIgnoreCase(DECEASED, guess))
                .findFirst()
                .map(guess -> removeMatchingString(DECEASED, guess))
                .map(PersonStatusDetails::dead);
    }

    private Optional<PersonStatusDetails> detectResigned(List<String> guesses) {
        final List<String> resignation = List.of("rez", "rezy", "rez.");

        if (guesses.stream()
                .anyMatch(guess -> anyEqualsIgnoreCase(resignation, guess))) {
            return Optional.of(PersonStatusDetails.resigned());
        }

        // detect "R 11.06.02" or similar Resignation statuses:
        if (guesses.stream()
                .anyMatch(guess -> guess.matches("R " + DateParser.DATE_PATTERN))) {
            return guesses.stream()
                    .filter(guess -> guess.matches("R " + DateParser.DATE_PATTERN))
                    .findFirst()
                    .map(guess -> guess.replaceFirst("R ", ""))
                    .map(PersonStatusDetails::resigned);
        }

        final List<String> exceptions = List.of("rezyg z podwyższonej skła");

        return guesses.stream()
                .filter(guess -> !exceptions.contains(guess))
                .filter(guess -> containsIgnoreCase(RESIGNED, guess))
                .findFirst()
                .map(guess -> removeMatchingString(RESIGNED, guess))
                .map(PersonStatusDetails::resigned);
    }

    private Optional<PersonStatusDetails> detectRemoved(List<String> guesses) {
        return guesses.stream()
                .filter(guess -> containsIgnoreCase(REMOVED, guess))
                .findFirst()
                .map(guess -> removeMatchingString(REMOVED, guess))
                .map(PersonStatusDetails::removed);
    }


    private boolean anyEqualsIgnoreCase(List<String> list, String guess) {
        return list.stream().anyMatch(element -> StringUtils.equalsIgnoreCase(guess, element));
    }

    private boolean containsIgnoreCase(List<String> list, String guess) {
        return list.stream().anyMatch(element -> StringUtils.containsIgnoreCase(guess, element));
    }

    private String removeMatchingString(List<String> list, String guess) {
        return list.stream()
                .filter(element -> StringUtils.containsIgnoreCase(guess, element))
                .map(element -> StringUtils.removeIgnoreCase(guess, element))
                .findFirst()
                .map(String::trim)
                .orElseThrow();
    }

    @Override
    public List<String> removeGuesses(List<String> guesses) {
        return null;
    }
}

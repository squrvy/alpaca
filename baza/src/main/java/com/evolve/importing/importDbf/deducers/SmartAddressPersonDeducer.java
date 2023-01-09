package com.evolve.importing.importDbf.deducers;

import com.evolve.domain.Address;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SmartAddressPersonDeducer extends AbstractSmartDeducer<Address>{

    // \p{IsAlphabetic} - includes also Polish characters ąćż..
    static final Pattern CITY_CODE_PATTERN = Pattern.compile("\\d{2}-\\d{3} [\\p{IsAlphabetic} ]+");

    //ul. Wyspiañskiego 63/2
    static final Pattern STREET_PATTERN = Pattern.compile("([uU]l\\. )?[\\p{IsAlphabetic} ]+\\d+ ?/ ?\\d+");

    public SmartAddressPersonDeducer(IssuesLogger.ImportIssues issues) {
        super(issues);
    }

    @Override
    public Optional<Address> deduceFrom(List<String> guesses) {
        List<String> streetCandidates = new ArrayList<>();
        Set<String> cityCodes = new HashSet<>();

        for (int i = 0; i < guesses.size(); i++ ) {
            final String maybeCityCode = guesses.get(i);

            if (isCityCode(maybeCityCode)) {
                cityCodes.add(maybeCityCode);
                if (i > 0) {
                    final String maybeStreet = guesses.get(i - 1);
                    if (isStreet(maybeStreet)) {
                        streetCandidates.add(maybeStreet);
                    }
                }
            }
        }

        if (!cityCodes.isEmpty()) {
            return Optional.of(postalCode(cityCodes.stream().findFirst().get(),
                    streetCandidates.stream().findFirst().orElse(null)
                    ));
        }

        return Optional.empty();
    }

    @Override
    public List<String> removeGuesses(List<String> guesses) {
        return guesses.stream()
                .filter(guess -> !isCityCode(guess))
                .filter(guess -> !isStreet(guess))
                .collect(Collectors.toList());
    }

    static Address postalCode(String candidate, String street) {
        String[] afterSplit = candidate.split(" ", 2);
        return new Address(street, afterSplit[0], afterSplit[1]);
    }

    static boolean isStreet(String street) {
        return STREET_PATTERN.matcher(street).matches();
    }

    static boolean isCityCode(String candidate) {
        return CITY_CODE_PATTERN.matcher(candidate).matches();
    }


}

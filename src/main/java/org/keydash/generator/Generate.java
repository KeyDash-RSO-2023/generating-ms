package org.keydash.generator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;


@RestController
public class Generate {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final Map<String, String> LANG_TABLE_MAPPING = Map.of(
            "en", "en_words",
            "sl", "sl_words",
            "deu", "deu_words"
    );

    public Generate() {
        // Initialize and populate the weights map
        punctuationWeights = new TreeMap<>();
        addPunctuationWeight(0.5, ","); // Comma 30%
        addPunctuationWeight(0.80, "."); // Dot 30% (cumulative 60%)
        addPunctuationWeight(0.90, ":"); // Minus 20% (cumulative 80%)
        addPunctuationWeight(1, " -"); // Minus 20% (cumulative 100%)
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/generate")
    public ResponseEntity<Response> getText(@RequestParam(required = false, defaultValue = "en") String language,
                                            @RequestParam(required = false, defaultValue = "500") int length,
                                            @RequestParam(required = false, defaultValue = "false") boolean punctuation) {
        if (!LANG_TABLE_MAPPING.keySet().contains(language)) {
            return new ResponseEntity<>(new Response("Language not supported", null, 0), HttpStatus.BAD_REQUEST);
        }

        String generatedText = retrieveAndGenerateText(language, length, punctuation);
        return new ResponseEntity<>(new Response(generatedText, language, length), HttpStatus.OK);
    }

    private String retrieveAndGenerateText(String language, int length, boolean punctuation) {
        String sql = "SELECT word, popularity FROM " + getLangTable(language); // Ensure you select both word and popularity

        // Create a TreeMap to store words and their popularity
        TreeMap<String, Integer> words = new TreeMap<>();

        // Use jdbcTemplate to query and process the ResultSet
        jdbcTemplate.query(sql, new RowMapper<Void>() {
            public Void mapRow(ResultSet rs, int rowNum) throws SQLException {
                // Get the word and its popularity from the ResultSet
                String word = rs.getString("word");
                Integer popularity = rs.getInt("popularity");

                // Put them in the TreeMap
                words.put(word, popularity);
                return null; // Since we don't need to map to an object, return null
            }
        });

        return generateRandomText(words, length, punctuation);
    }


    private String getLangTable(String language) {
        return LANG_TABLE_MAPPING.getOrDefault(language, "en_words");
    }

    private String generateRandomText(TreeMap<String, Integer> words, int length, boolean punctuation) {
        Random random = new Random();
        StringBuilder text = new StringBuilder();
        int lastPunctuation = 0;
        int punctuationSpacing = 3;

        for (int i = 0; i < length; i++) {
//            String word = words.get(random.nextInt(words.size()));

            String word;
            do {
                // Select a random word
                int randomIndex = random.nextInt(words.size());
                word = (String) words.keySet().toArray()[randomIndex];
                // Accept it based on its popularity
            } while (random.nextInt(100) >= Math.max(3, words.get(word)));
            lastPunctuation++;

            boolean punctuationNow = punctuation && shouldAddPunctuation();

            if (i == 0 || text.toString().endsWith(". ")) {
                word = capitalize(word);
            }

            text.append(word);

            if (lastPunctuation >= punctuationSpacing && i > 2 && i < length - 2 && punctuationNow) {
                text.append(addPunctuation(random));
                lastPunctuation = 0;
            }

            if (i < length - 1) {
                text.append(" ");
            } else {
                if (punctuation) text.append(".");
            }
        }

        if (punctuation) return text.toString();

        return text.toString().toLowerCase();
    }

    private boolean shouldAddPunctuation() {
        return new Random().nextInt(10) < 1.75; // 17.5% chance to add punctuation
    }

    private NavigableMap<Double, String> punctuationWeights;

    private void addPunctuationWeight(double cumulativeWeight, String punctuation) {
        punctuationWeights.put(cumulativeWeight, punctuation);
    }

    private String addPunctuation(Random random) {
        double randomWeight = random.nextDouble();
        String punctuation = punctuationWeights.ceilingEntry(randomWeight).getValue();
        return punctuation != null ? punctuation : "";
    }

    private String capitalize(String word) {
        if (word == null || word.isEmpty()) {
            return word;
        }
        return word.substring(0, 1).toUpperCase() + word.substring(1);
    }

}

package top.mao196.querybeansql.util;


public class QueryUtils {

    public static final String ESCAPE_CHARACTER = "\\";


    public static final String QUERY_PARAMETER_REGEXP = "(?:^|[^\\w]):(\\(\\?i\\))?([^\\d][\\w.$]*)";

    /**
     * Escapes a parameter value for a 'like' operation in JPQL query
     *
     * @param value parameter value
     * @return escaped parameter value
     */
    public static String escapeForLike(String value) {
        return escapeForLike(value, ESCAPE_CHARACTER);
    }

    /**
     * Escapes a parameter value for a 'like' operation in JPQL query
     * @param value parameter value
     * @param escapeCharacter escape character
     * @return escaped parameter value
     */
    public static String escapeForLike(String value, String escapeCharacter) {
        return value.replace(escapeCharacter, escapeCharacter + escapeCharacter)
                .replace("%", escapeCharacter + "%")
                .replace("_", escapeCharacter + "_");
    }
}
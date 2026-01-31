package org.swb.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextReplacer {

    // Método para reemplazar con <strong>
    public static String replaceWithStrong(String text) {
        if (text == null) return "";
        // Expresión regular para encontrar texto entre ***
        Pattern pattern = Pattern.compile("\\*\\*\\*(.*?)\\*\\*\\*");
        Matcher matcher = pattern.matcher(text);
        // Reemplazar el texto encontrado por <strong>$1</strong>
        return matcher.replaceAll("<strong>$1</strong>");
    }

    // Método para reemplazar con texto vacío
    public static String replaceWithEmpty(String text) {
        if (text == null) return "";
        // Expresión regular para encontrar texto entre ***
        Pattern pattern = Pattern.compile("\\*\\*\\*(.*?)\\*\\*\\*");
        Matcher matcher = pattern.matcher(text);
        // Reemplazar el texto encontrado por $1
        return matcher.replaceAll("$1");
    }

    public static void main(String[] args) {
        // Ejemplo de uso
        String text = "***bold text*** and normal text";
        System.out.println(replaceWithStrong(text)); // Output: <strong>bold text</strong> and normal text
        System.out.println(replaceWithEmpty(text));  // Output: bold text and normal text
    }
}

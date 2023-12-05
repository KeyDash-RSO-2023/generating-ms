package org.keydash.generator;

public class Response {
    private String textToType;
    private String language;
    private int length;

    public Response(String textToType, String language, int length) {
        this.textToType = textToType;
        this.language = language;
        this.length = length;
    }

    public String getTextToType() {
        return this.textToType;
    }

    public void setTextToType(String textToType) {
        this.textToType = textToType;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }
}
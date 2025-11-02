package com.example.webdisgn.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

public class XSSRequestWrapper extends HttpServletRequestWrapper {

    public XSSRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public String getParameter(String name) {
        return sanitize(super.getParameter(name));
    }

    @Override
    public String getHeader(String name) {
        return sanitize(super.getHeader(name));
    }

    private String sanitize(String input) {
        if (input == null) return null;
        // Prima sostituiamo &, poi gli altri caratteri
        String sanitized = input.replaceAll("&(?![a-zA-Z]{2,6};)", "&amp;"); // non doppio encoding
        sanitized = sanitized.replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;")
                .replaceAll("\"", "&quot;")
                .replaceAll("'", "&#x27;");
        return sanitized;
    }
}

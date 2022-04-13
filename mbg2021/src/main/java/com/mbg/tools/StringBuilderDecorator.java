package com.mbg.tools;

public class StringBuilderDecorator {

    private final StringBuilder stringBuilder;
    private final String indentation;
    private int indentCount = 0;

    public StringBuilderDecorator(int capacity, String indentation) {
        this.stringBuilder = new StringBuilder(capacity);
        this.indentation = indentation;
    }

    public StringBuilderDecorator setIndentation(int i) {
        this.indentCount = i;
        return this;
    }

    public StringBuilderDecorator decreaseIndent(int i) {
        if (this.indentCount > i) {
            this.indentCount -= i;
        } else {
            resetIndentation();
        }
        return this;
    }

    public StringBuilderDecorator increaseIndent(int i) {
        this.indentCount += i;
        return this;
    }

    public StringBuilderDecorator resetIndentation() {
        this.indentCount = 0;
        return this;
    }

    public StringBuilderDecorator append(String string) {
        stringBuilder.append(string).append("\n");
        return this;
    }

    public StringBuilderDecorator appendWithIndent(String string) {
        indent(indentCount);
        stringBuilder.append(string).append("\n");
        return this;
    }

    public StringBuilderDecorator emptyLine() {
        stringBuilder.append("\n");
        return this;
    }

    public StringBuilderDecorator indent() {
        stringBuilder.append(this.indentation);
        return this;
    }

    public StringBuilderDecorator indent(int times) {
        for (int i = 0; i < times; i++) {
            indent();
        }
        return this;
    }

    @Override
    public String toString() {
        return stringBuilder.toString();
    }

}
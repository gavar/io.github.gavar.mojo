package dev.gavar.mojo.io;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static dev.gavar.mojo.util.StringUtils.lengthOf;

public class HorizontalTable {
    static final char SPACE = ' ';
    static final char CROSS = '┼';
    static final char VERTICAL = '│';
    static final char HORIZONTAL = '─';

    private int columns;
    private int columnsByRows;
    private int columnsByHeaders;

    private final List<String[]> rows = new ArrayList<>();
    private final Map<Integer, String> headers = new HashMap<>();
    private final Map<Integer, Integer> maxWidths = new HashMap<>();

    public HorizontalTable header(String... items) {
        headers.clear();
        for (int i = 0; i < items.length; i++)
            headers.put(i, items[i]);

        updateWidths(items);
        updateColumns(items.length, columnsByRows);
        return this;
    }

    public HorizontalTable row(String... items) {
        rows.add(items);
        updateWidths(items);
        updateColumns(columnsByHeaders, items.length);
        return this;
    }

    public void print(Consumer<CharSequence> consumer) {
        final Printer printer = this.setup(consumer);

        // HEAD
        printer.horizontalLine();
        printer.row(headers);

        // BODY
        printer.horizontalLine();
        rows.forEach(printer::row);
        printer.horizontalLine();
    }

    private Printer setup(Consumer<CharSequence> consumer) {
        return new Printer(consumer);
    }

    private void updateColumns(int byHeaders, int byRows) {
        this.columnsByRows = byRows;
        this.columnsByHeaders = byHeaders;
        this.columns = Math.max(byRows, byHeaders);
    }

    private void updateWidths(String... columns) {
        for (int c = 0; c < columns.length; c++)
            maxWidths.merge(c, lengthOf(columns[c]), Math::max);
    }

    private int widthOf(int column) {
        return maxWidths.get(column);
    }

    private class Printer {
        private final StringBuilder sb;
        private final Consumer<CharSequence> consumer;

        public Printer(Consumer<CharSequence> consumer) {
            this.consumer = consumer;
            this.sb = new StringBuilder();
        }

        public Printer(StringBuilder sb, Consumer<CharSequence> consumer) {
            this.sb = sb;
            this.consumer = consumer;
        }

        public void row(String[] row) {
            sb.append(VERTICAL);
            for (int c = 0; c < columns; c++) {
                column(c, row[c]);
                sb.append(VERTICAL);
            }
            flush();
        }

        public void row(Map<Integer, String> row) {
            sb.append(VERTICAL);
            for (int c = 0; c < columns; c++) {
                column(c, row.get(c));
                sb.append(VERTICAL);
            }
            flush();
        }

        public void horizontalLine() {
            sb.append(CROSS);
            for (int c = 0; c < columns; c++) {
                sb.append(HORIZONTAL);
                append(HORIZONTAL, widthOf(c));
                sb.append(HORIZONTAL);
                sb.append(CROSS);
            }
            flush();
        }

        private void column(int column, String value) {
            final int width = widthOf(column);
            sb.append(SPACE);
            if (value == null) {
                append(SPACE, width);
            } else {
                sb.append(value);
                append(SPACE, width - value.length());
            }
            sb.append(SPACE);
        }

        private void append(char c, int times) {
            while (times-- > 0) sb.append(c);
        }

        private void flush() {
            consumer.accept(sb);
            sb.setLength(0);
        }
    }
}

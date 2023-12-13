package rrampage.wasp.lexer;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class Lexer {
    private final String input;
    private final char[] cs;
    private static final Set<Character> WS_CHARS = Set.of(' ', '\t', '\r', '\n');
    private static final Set<Character> ID_CHARS = Set.of("1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz!#$%&\\*+-./:<=>?@'^_`|~".chars().mapToObj(c -> (char)c).toArray(Character[]::new));

    private char[] get(int start, int n) {
        if (n < 0 || start < 0 || start >= cs.length || start + n >= cs.length) {
            System.out.println("NULL");
            return null;
        }
        return Arrays.copyOfRange(cs, start, start+n);
    }
    public Lexer(String input) {
        this.input = input;
        this.cs = input.toCharArray();
    }
    public Result<Optional<Token>, LexError> parse(int pos) {
        AtomicInteger apos = new AtomicInteger(pos);
        var result = parseKind(apos);
        return (result.isError()) ?
                new Result.Error<>(result.err().get()) :
                new Result.Ok<>(result.ok().map(tk -> new Token(tk.get(), pos, apos.get()- pos)));
    }

    private int skipWhitespace(AtomicInteger pos) {
        int i = pos.get(), start = pos.get();
        while (i < cs.length) {
            i++;
            if (!WS_CHARS.contains(cs[i])) {
                break;
            }
        }
        pos.set(i);
        return i - start;
    }

    private String splitUntil(AtomicInteger pos, char c) {
        int i = pos.get(), start = pos.get();
        while (i < cs.length) {
            i++;
            if (cs[i] == c) {
                break;
            }
        }
        return input.substring(start, i);
    }

    Result<Optional<TokenKind>, LexError> parseKind(AtomicInteger pos) {
        // TODO
        int start = pos.get();
        char c = cs[start];

        switch (c) {
            case '(' -> {
                switch (get(start+1, 1)){
                    case null -> {
                        return new Result.Ok<>(Optional.of(TokenKind.TokenType.LParen));
                    }
                    case char[] cs when cs.length == 1 -> {}
                    default -> throw new IllegalStateException("Unexpected value: " + get(start + 1, 1));
                }
            }
        }
        return new Result.Error<>(new LexError.InvalidDigit('a'));
    }

    public Result<TokenKind, LexError> parseKindInt(AtomicInteger pos) {
        int i = pos.get();
        while (i < cs.length) {
            char c = cs[i];
            i++;
            switch (c) {
                case '(' -> {
                    System.out.println("Paren start");
                    if (i < cs.length && cs[i] == ';') {
                        System.out.println("Block comment start");
                        int lvl = 1;
                        int j = i+1;
                        while (j < cs.length) {
                            char cj = cs[j];
                            j++;
                            switch (cj) {
                                case '(' -> {
                                    if (j < cs.length && cs[j] == ';') {
                                        lvl += 1;
                                    }
                                }
                                case ';' -> {
                                    if (j < cs.length && cs[j] == ')') {
                                        lvl -= 1;
                                        if (lvl == 0) {
                                            pos.set(j+1);
                                            return new Result.Ok<>(TokenKind.TokenType.BlockComment);
                                        }
                                    }
                                }
                                default -> {}
                            }
                        }
                        return new Result.Error<>(new LexError.DanglingBlockComment());
                    }
                    pos.set(i);
                    return new Result.Ok<>(TokenKind.TokenType.LParen);
                }
                case ')' -> {
                    pos.set(i);
                    return new Result.Ok<>(TokenKind.TokenType.RParen);
                }
                case ' ', '\t', '\r', '\n' -> {
                    int nws = skipWhitespace(pos);
                    System.out.println("WS_SKIP: "+ nws);
                    return new Result.Ok<>(TokenKind.TokenType.Whitespace);
                }
                case ';' -> {
                    if (i < cs.length && cs[i] == ';') {
                        String lineComment = splitUntil(pos, '\n');
                        System.out.println("LINE_COMMENT: " + lineComment);
                        return new Result.Ok<>(TokenKind.TokenType.LineComment);
                    }
                    pos.set(i);
                    return new Result.Ok<>(TokenKind.TokenType.Reserved);
                }
                case ',', '[', ']', '{', '}' -> {
                    pos.set(i);
                    return new Result.Ok<>(TokenKind.TokenType.Reserved);
                }
                default -> {
                    if (ID_CHARS.contains(c)) {
                        // TODO
                    } else {
                        // TODO
                    }
                }
            }
        }
        return new Result.Error<>(new LexError.InvalidDigit('a'));
    }

    public static void main(String[] args) {
        String prog = "    ((; (;Some comments ;) LOL;) LO";
        var lexer = new Lexer(prog);
        AtomicInteger pos = new AtomicInteger(0);
        System.out.println(lexer.parseKindInt(pos));
        System.out.println(lexer.parseKindInt(pos));
        System.out.println(lexer.parseKindInt(pos));
        System.out.println(lexer.parseKindInt(pos));
        System.out.println(lexer.parseKindInt(pos));
        System.out.println("ARR: " + Arrays.toString(lexer.get(pos.get(), 2)));
        for (char c : prog.toCharArray()) {
            System.out.println( c + ": " + ID_CHARS.contains(c));
        }
        AtomicInteger pos2 = new AtomicInteger(0);
        System.out.println(lexer.splitUntil(pos2, 'S'));
        System.out.println("SPLIT_UNTIL: " + pos.get());
    }
}

package io.github.gavar.mojo.release.function;

/**
 * Represents an operation that accepts a single input argument and returns no result.
 * Unlike most other functional interfaces, {@link UnsafeProcedure} is expected to operate via side-effects.
 */
@FunctionalInterface
public interface UnsafeProcedure {
    /**
     * Performs this operation on the given argument.
     */
    void perform() throws Exception;
}

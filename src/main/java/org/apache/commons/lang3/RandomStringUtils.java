/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.lang3;

import java.security.SecureRandom;
import java.security.Security;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

import org.apache.commons.lang3.time.BranchCoverage;

/**
 * Generates random {@link String}s.
 * <p>
 * Use {@link #secure()} to get the singleton instance based on {@link SecureRandom#SecureRandom()} which uses a secure random number generator implementing the
 * default random number algorithm.
 * </p>
 * <p>
 * Use {@link #secureStrong()} to get the singleton instance based on {@link SecureRandom#getInstanceStrong()} which uses an instance that was selected by using
 * the algorithms/providers specified in the {@code securerandom.strongAlgorithms} {@link Security} property.
 * </p>
 * <p>
 * Use {@link #insecure()} to get the singleton instance based on {@link ThreadLocalRandom#current()} <strong>which is not cryptographically secure</strong>. In addition,
 * instances do not use a cryptographically random seed unless the {@linkplain System#getProperty system property} {@code java.util.secureRandomSeed} is set to
 * {@code true}.
 * </p>
 * <p>
 * Starting in version 3.17.0, the method {@link #secure()} uses {@link SecureRandom#SecureRandom()} instead of {@link SecureRandom#getInstanceStrong()}, and
 * adds {@link #secureStrong()}.
 * </p>
 * <p>
 * Starting in version 3.16.0, this class uses {@link #secure()} for static methods and adds {@link #insecure()}.
 * </p>
 * <p>
 * Starting in version 3.15.0, this class uses {@link SecureRandom#getInstanceStrong()} for static methods.
 * </p>
 * <p>
 * Before version 3.15.0, this class used {@link ThreadLocalRandom#current()} for static methods, which is not cryptographically secure.
 * </p>
 * <p>
 * RandomStringUtils is intended for simple use cases. For more advanced use cases consider using Apache Commons Text's
 * <a href= "https://commons.apache.org/proper/commons-text/javadocs/api-release/org/apache/commons/text/RandomStringGenerator.html"> RandomStringGenerator</a>
 * instead.
 * </p>
 * <p>
 * The Apache Commons project provides <a href="https://commons.apache.org/proper/commons-rng/">Commons RNG</a> dedicated to pseudo-random number generation,
 * that may be a better choice for applications with more stringent requirements (performance and/or correctness).
 * </p>
 * <p>
 * Note that <em>private high surrogate</em> characters are ignored. These are Unicode characters that fall between the values 56192 (db80) and 56319 (dbff) as
 * we don't know how to handle them. High and low surrogates are correctly dealt with - that is if a high surrogate is randomly chosen, 55296 (d800) to 56191
 * (db7f) then it is followed by a low surrogate. If a low surrogate is chosen, 56320 (dc00) to 57343 (dfff) then it is placed after a randomly chosen high
 * surrogate.
 * </p>
 * <p>
 * #ThreadSafe#
 * </p>
 *
 * @see #secure()
 * @see #secureStrong()
 * @see #insecure()
 * @see SecureRandom#SecureRandom()
 * @see SecureRandom#getInstanceStrong()
 * @see ThreadLocalRandom#current()
 * @see RandomUtils
 * @since 1.0
 */
public class RandomStringUtils {

    private static final Supplier<RandomUtils> SECURE_SUPPLIER = RandomUtils::secure;

    private static final RandomStringUtils INSECURE = new RandomStringUtils(RandomUtils::insecure);

    private static final RandomStringUtils SECURE = new RandomStringUtils(SECURE_SUPPLIER);

    private static final RandomStringUtils SECURE_STRONG = new RandomStringUtils(RandomUtils::secureStrong);

    private static final char[] ALPHANUMERICAL_CHARS = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l',
            'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G',
            'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1',
            '2', '3', '4', '5', '6', '7', '8', '9' };

    private static final int ASCII_0 = '0';
    private static final int ASCII_9 = '9';
    private static final int ASCII_A = 'A';
    private static final int ASCII_z = 'z';

    private static final int CACHE_PADDING_BITS = 3;
    private static final int BITS_TO_BYTES_DIVISOR = 5;
    private static final int BASE_CACHE_SIZE_PADDING = 10;

    /**
     * Gets the singleton instance based on {@link ThreadLocalRandom#current()}; <b>which is not cryptographically
     * secure</b>; for more secure processing use {@link #secure()} or {@link #secureStrong()}.
     * <p>
     * The method {@link ThreadLocalRandom#current()} is called on-demand.
     * </p>
     *
     * @return the singleton instance based on {@link ThreadLocalRandom#current()}.
     * @see ThreadLocalRandom#current()
     * @see #secure()
     * @see #secureStrong()
     * @since 3.16.0
     */
    public static RandomStringUtils insecure() {
        return INSECURE;
    }

    /**
     * Creates a random string whose length is the number of characters specified.
     *
     * <p>
     * Characters will be chosen from the set of all characters.
     * </p>
     *
     * @param count the length of random string to create.
     * @return the random string.
     * @throws IllegalArgumentException if {@code count} &lt; 0.
     * @deprecated Use {@link #next(int)} from {@link #secure()}, {@link #secureStrong()}, or {@link #insecure()}.
     */
    @Deprecated
    public static String random(final int count) {
        return secure().next(count);
    }

    /**
     * Creates a random string whose length is the number of characters specified.
     *
     * <p>
     * Characters will be chosen from the set of alpha-numeric characters as indicated by the arguments.
     * </p>
     *
     * @param count   the length of random string to create.
     * @param letters if {@code true}, generated string may include alphabetic characters.
     * @param numbers if {@code true}, generated string may include numeric characters.
     * @return the random string.
     * @throws IllegalArgumentException if {@code count} &lt; 0.
     * @deprecated Use {@link #next(int, boolean, boolean)} from {@link #secure()}, {@link #secureStrong()}, or {@link #insecure()}.
     */
    @Deprecated
    public static String random(final int count, final boolean letters, final boolean numbers) {
        return secure().next(count, letters, numbers);
    }

    /**
     * Creates a random string whose length is the number of characters specified.
     *
     * <p>
     * Characters will be chosen from the set of characters specified.
     * </p>
     *
     * @param count the length of random string to create.
     * @param chars the character array containing the set of characters to use, may be null.
     * @return the random string.
     * @throws IllegalArgumentException if {@code count} &lt; 0.
     * @deprecated Use {@link #next(int, char...)} from {@link #secure()}, {@link #secureStrong()}, or {@link #insecure()}.
     */
    @Deprecated
    public static String random(final int count, final char... chars) {
        return secure().next(count, chars);
    }

    /**
     * Creates a random string whose length is the number of characters specified.
     *
     * <p>
     * Characters will be chosen from the set of alpha-numeric characters as indicated by the arguments.
     * </p>
     *
     * @param count   the length of random string to create.
     * @param start   the position in set of chars to start at.
     * @param end     the position in set of chars to end before.
     * @param letters if {@code true}, generated string may include alphabetic characters.
     * @param numbers if {@code true}, generated string may include numeric characters.
     * @return the random string.
     * @throws IllegalArgumentException if {@code count} &lt; 0.
     * @deprecated Use {@link #next(int, int, int, boolean, boolean)} from {@link #secure()}, {@link #secureStrong()}, or {@link #insecure()}.
     */
    @Deprecated
    public static String random(final int count, final int start, final int end, final boolean letters,
            final boolean numbers) {
        return secure().next(count, start, end, letters, numbers);
    }

    /**
     * Creates a random string based on a variety of options, using default source of randomness.
     *
     * <p>
     * This method has exactly the same semantics as {@link #random(int,int,int,boolean,boolean,char[],Random)}, but
     * instead of using an externally supplied source of randomness, it uses the internal static {@link Random}
     * instance.
     * </p>
     *
     * @param count   the length of random string to create.
     * @param start   the position in set of chars to start at.
     * @param end     the position in set of chars to end before.
     * @param letters if {@code true}, generated string may include alphabetic characters.
     * @param numbers if {@code true}, generated string may include numeric characters.
     * @param chars   the set of chars to choose randoms from. If {@code null}, then it will use the set of all chars.
     * @return the random string.
     * @throws ArrayIndexOutOfBoundsException if there are not {@code (end - start) + 1} characters in the set array.
     * @throws IllegalArgumentException       if {@code count} &lt; 0.
     * @deprecated Use {@link #next(int, int, int, boolean, boolean, char...)} from {@link #secure()}, {@link #secureStrong()}, or {@link #insecure()}.
     */
    @Deprecated
    public static String random(final int count, final int start, final int end, final boolean letters,
            final boolean numbers, final char... chars) {
        return secure().next(count, start, end, letters, numbers, chars);
    }

    /**
     * Creates a random string based on a variety of options, using supplied source of randomness.
     *
     * <p>
     * If start and end are both {@code 0}, start and end are set to {@code ' '} and {@code 'z'}, the ASCII printable
     * characters, will be used, unless letters and numbers are both {@code false}, in which case, start and end are set
     * to {@code 0} and {@link Character#MAX_CODE_POINT}.
     *
     * <p>
     * If set is not {@code null}, characters between start and end are chosen.
     * </p>
     *
     * <p>
     * This method accepts a user-supplied {@link Random} instance to use as a source of randomness. By seeding a single
     * {@link Random} instance with a fixed seed and using it for each call, the same random sequence of strings can be
     * generated repeatedly and predictably.
     * </p>
     *
     * @param count   the length of random string to create.
     * @param start   the position in set of chars to start at (inclusive).
     * @param end     the position in set of chars to end before (exclusive).
     * @param letters if {@code true}, generated string may include alphabetic characters.
     * @param digits if {@code true}, generated string may include digit characters.
     * @param chars   the set of chars to choose randoms from, must not be empty. If {@code null}, then it will use the
     *                set of all chars.
     * @param random  a source of randomness.
     * @return the random string.
     * @throws ArrayIndexOutOfBoundsException if there are not {@code (end - start) + 1} characters in the set array.
     * @throws IllegalArgumentException       if {@code count} &lt; 0 or the provided chars array is empty.
     * @since 2.0
     */
    public static String random(int count, int start, int end, final boolean letters, final boolean digits,
            final char[] chars, final Random random) {
        // Branch 1: count == 0
        if (count == 0) {
            BranchCoverage.hit("RandomStringUtils.random", 1);
            return StringUtils.EMPTY;
        } // Branch 2: count != 0 (implicit)
        else{
            BranchCoverage.hit("RandomStringUtils.random", 2);
        }

        // Branch 3: count < 0
        if (count < 0) {
            BranchCoverage.hit("RandomStringUtils.random", 3);
            throw new IllegalArgumentException("Requested random string length " + count + " is less than 0.");
        } // Branch 4: count > 0 (implicit)
        else{
            BranchCoverage.hit("RandomStringUtils.random", 4);
        }

        // Branch 5: chars != null && chars.length == 0
        if (chars != null && chars.length == 0) {
            BranchCoverage.hit("RandomStringUtils.random", 5);
            throw new IllegalArgumentException("The chars array must not be empty");
        } // Branch 6: !(chars != null && chars.length == 0) (implicit)
        else{
            BranchCoverage.hit("RandomStringUtils.random", 6);
        }

        // Branch 7: start == 0 && end == 0
        if (start == 0 && end == 0) {
            BranchCoverage.hit("RandomStringUtils.random", 7);
            
            // Branch 8: chars != null
            if (chars != null) {
                BranchCoverage.hit("RandomStringUtils.random", 8);
                end = chars.length;

            // Branch 9: !letters && !digits
            } else if (!letters && !digits) {
                BranchCoverage.hit("RandomStringUtils.random", 9);
                end = Character.MAX_CODE_POINT;

            // Branch 10: else
            } else {
                BranchCoverage.hit("RandomStringUtils.random", 10);
                end = 'z' + 1;
                start = ' ';
            }
        
        // Branch 11: end <= start
        } else if (end <= start) {
            BranchCoverage.hit("RandomStringUtils.random", 11);
            throw new IllegalArgumentException("Parameter end (" + end + ") must be greater than start (" + start + ")");
        
        // Branch 12: start < 0 || end < 0
        } else if (start < 0 || end < 0) {
            BranchCoverage.hit("RandomStringUtils.random", 12);
            throw new IllegalArgumentException("Character positions MUST be >= 0");
        }
        // Branch 13: implicit else for branch 7,11,12
        else{
            BranchCoverage.hit("RandomStringUtils.random", 13);
        }

        // Branch 14: end > Character.MAX_CODE_POINT
        if (end > Character.MAX_CODE_POINT) {
            BranchCoverage.hit("RandomStringUtils.random", 14);
            // Technically, it should be `Character.MAX_CODE_POINT+1` as `end` is excluded
            // But the character `Character.MAX_CODE_POINT` is private use, so it would anyway be excluded
            end = Character.MAX_CODE_POINT;
        } // Branch 15: end <= Character.MAX_CODE_POINT (implicit)
        else{
            BranchCoverage.hit("RandomStringUtils.random", 15);
        }

        // Optimizations and tests when chars == null and using ASCII characters (end <= 0x7f)
        // Branch 16: chars == null && end <= 0x7f 
        if (chars == null && end <= 0x7f) {
            BranchCoverage.hit("RandomStringUtils.random", 16);
            // Optimize generation of full alphanumerical characters
            // Normally, we would need to pick a 7-bit integer, since gap = 'z' - '0' + 1 = 75 > 64
            // In turn, this would make us reject the sampling with probability 1 - 62 / 2^7 > 1 / 2
            // Instead we can pick directly from the right set of 62 characters, which requires
            // picking a 6-bit integer and only rejecting with probability 2 / 64 = 1 / 32
            
            // Branch 17: letters && digits && start <= ASCII_0 && end >= ASCII_z + 1
            if (letters && digits && start <= ASCII_0 && end >= ASCII_z + 1) {
                BranchCoverage.hit("RandomStringUtils.random", 17);
                return random(count, 0, 0, false, false, ALPHANUMERICAL_CHARS, random);
            } // Branch 18: !(letters && digits && start <= ASCII_0 && end >= ASCII_z + 1) (implicit)
            else{
                BranchCoverage.hit("RandomStringUtils.random", 18);
            }

            // Branch 19: digits && end <= ASCII_0 || letters && end <= ASCII_A
            if (digits && end <= ASCII_0 || letters && end <= ASCII_A) {
                BranchCoverage.hit("RandomStringUtils.random", 19);
                throw new IllegalArgumentException("Parameter end (" + end + ") must be greater than (" + ASCII_0 + ") for generating digits "
                        + "or greater than (" + ASCII_A + ") for generating letters.");
            } // Branch 20: !(digits && end <= ASCII_0 || letters && end <= ASCII_A) (implicit)
            else{
                BranchCoverage.hit("RandomStringUtils.random", 20);
            }

            // Optimize start and end when filtering by letters and/or numbers:
            // The range provided may be too large since we filter anyway afterward.
            // Note the use of Math.min/max (as opposed to setting start to '0' for example),
            // since it is possible the range start/end excludes some of the letters/numbers,
            // e.g., it is possible that start already is '1' when numbers = true, and start
            // needs to stay equal to '1' in that case.
            // Note that because of the above test, we will always have start < end
            // even after this optimization.
            
            // Branch 21: letters && digits
            if (letters && digits) {
                BranchCoverage.hit("RandomStringUtils.random", 21);
                start = Math.max(ASCII_0, start);
                end = Math.min(ASCII_z + 1, end);
            
            // Branch 22: digits
            } else if (digits) {
                BranchCoverage.hit("RandomStringUtils.random", 22);
                // just numbers, no letters
                start = Math.max(ASCII_0, start);
                end = Math.min(ASCII_9 + 1, end);

            // Branch 23: letters
            } else if (letters) {
                BranchCoverage.hit("RandomStringUtils.random", 23);
                // just letters, no numbers
                start = Math.max(ASCII_A, start);
                end = Math.min(ASCII_z + 1, end);
            }
            // Branch 24: implicit else for branch 21,22,23
            else{
                BranchCoverage.hit("RandomStringUtils.random", 24);
            }
        } // Branch 25: !(chars == null && end <= 0x7f) (implicit)
        else{
            BranchCoverage.hit("RandomStringUtils.random", 25);
        }

        // Branch 26: letters && !digits
        if (letters && !digits) {
            BranchCoverage.hit("RandomStringUtils.random", 26);

            // Branch 27: for loop entered i < end
            // Branch 28: for looped skipped i >= end (implicit)
            boolean loopEntered = false;
            for (int i = start; i < end; i++) {
                if(loopEntered == false){
                    BranchCoverage.hit("RandomStringUtils.random", 27);
                    loopEntered = true;
                }

                // Branch 29: Character.isLetter(i)
                if (Character.isLetter(i)) {
                    BranchCoverage.hit("RandomStringUtils.random", 29);
                    break;
                } // Branch 30: !(Character.isLetter(i)) (implicit)
                else{
                    BranchCoverage.hit("RandomStringUtils.random", 30);
                }

                // Branch 31: i == end - 1
                if (i == end - 1) {
                    BranchCoverage.hit("RandomStringUtils.random", 31);
                    throw new IllegalArgumentException(String.format("No letters exist between start %,d and end %,d.", start, end));
                } // Branch 32: i != end - 1 (implicit)
                else {
                    BranchCoverage.hit("RandomStringUtils.random", 32);
                }
            } 
            if(loopEntered == false){
                BranchCoverage.hit("RandomStringUtils.random", 28);
            }
        } // Branch 33: !(letters && !digits)
        else{
            BranchCoverage.hit("RandomStringUtils.random", 33);
        }

        // Branch 34: !letters && digits
        if (!letters && digits) {
            BranchCoverage.hit("RandomStringUtils.random", 34);

            // Branch 35: for loop entered i < end
            // Branch 36: for loop skipped i >= end
            boolean loopEntered = false;
            for (int i = start; i < end; i++) {
                if(loopEntered == false){
                    BranchCoverage.hit("RandomStringUtils.random", 35);
                    loopEntered = true;
                }

                // Branch 37: Character.isDigit(i)
                if (Character.isDigit(i)) {
                    BranchCoverage.hit("RandomStringUtils.random", 37);
                    break;
                } // Branch 38: !(Character.isDigit(i)) (implicit)
                else{
                    BranchCoverage.hit("RandomStringUtils.random", 38);
                }

                // Branch 39: i == end - 1
                if (i == end - 1) {
                    BranchCoverage.hit("RandomStringUtils.random", 39);
                    throw new IllegalArgumentException(String.format("No digits exist between start %,d and end %,d.", start, end));
                } // Branch 40: i != end - 1 (implicit)
                else {
                    BranchCoverage.hit("RandomStringUtils.random", 40);
                }
            }
            if(loopEntered == false){
                BranchCoverage.hit("RandomStringUtils.random", 36);
            }
        } // Branch 41: !(!letters && digits) (implicit)
        else{
            BranchCoverage.hit("RandomStringUtils.random", 41);
        }

        final StringBuilder builder = new StringBuilder(count);
        final int gap = end - start;
        final int gapBits = Integer.SIZE - Integer.numberOfLeadingZeros(gap);
        // The size of the cache we use is an heuristic:
        // about twice the number of bytes required if no rejection
        // Ideally the cache size depends on multiple factor, including the cost of generating x bytes
        // of randomness as well as the probability of rejection. It is however not easy to know
        // those values programmatically for the general case.
        // Calculate cache size:
        // 1. Multiply count by bits needed per character (gapBits)
        // 2. Add padding bits (3) to handle partial bytes
        // 3. Divide by 5 to convert to bytes (normally this would be by 8, dividing by 5 allows for about 60% extra space)
        // 4. Add base padding (10) to handle small counts efficiently
        // 5. Ensure we don't exceed Integer.MAX_VALUE / 5 + 10 to provide a good balance between overflow prevention and
        //    making the cache extremely large
        final long desiredCacheSize = ((long) count * gapBits + CACHE_PADDING_BITS) / BITS_TO_BYTES_DIVISOR + BASE_CACHE_SIZE_PADDING;
        final int cacheSize = (int) Math.min(desiredCacheSize, Integer.MAX_VALUE / BITS_TO_BYTES_DIVISOR + BASE_CACHE_SIZE_PADDING);
        final CachedRandomBits arb = new CachedRandomBits(cacheSize, random);
        
        // Branch 42: while loop entered count != 0
        // Branch 43: while loop skipped count == 0
        boolean whileEntered = false;
        while (count-- != 0) {
            if(whileEntered == false){
                BranchCoverage.hit("RandomStringUtils.random", 42);
                whileEntered = true;
            }

            // Generate a random value between start (included) and end (excluded)
            final int randomValue = arb.nextBits(gapBits) + start;
            
            // Rejection sampling if value too large
            // Branch 44: randomValue >= end
            if (randomValue >= end) {
                BranchCoverage.hit("RandomStringUtils.random", 44);
                count++;
                continue;
            } // Branch 45: randomValue < end (implicit)
            else{
                BranchCoverage.hit("RandomStringUtils.random", 45);
            }
            final int codePoint;

            // Branch 46: chars == null
            if (chars == null) {
                BranchCoverage.hit("RandomStringUtils.random", 46);
                codePoint = randomValue;
                switch (Character.getType(codePoint)) {
                
                // Branch 47: any of the case
                case Character.UNASSIGNED:
                case Character.PRIVATE_USE:
                case Character.SURROGATE:
                    BranchCoverage.hit("RandomStringUtils.random", 47);
                    count++;
                    continue;
                // Branch 48: none of the case (implicit)
                default:
                    BranchCoverage.hit("RandomStringUtils.random", 48);
                    break;
                } 
            
            // Branch 49: else
            } else {
                BranchCoverage.hit("RandomStringUtils.random", 49);
                codePoint = chars[randomValue];
            }
            final int numberOfChars = Character.charCount(codePoint);

            // Branch 50: count == 0 && numberOfChars > 1
            if (count == 0 && numberOfChars > 1) {
                BranchCoverage.hit("RandomStringUtils.random", 50);
                count++;
                continue;
            } // Branch 51: !(count == 0 && numberOfChars > 1) (implicit)
            else {
                BranchCoverage.hit("RandomStringUtils.random", 51);
            }

            // Branch 52: letters && Character.isLetter(codePoint) || digits && Character.isDigit(codePoint) || !letters && !digits
            if (letters && Character.isLetter(codePoint) || digits && Character.isDigit(codePoint) || !letters && !digits) {
                BranchCoverage.hit("RandomStringUtils.random", 52);
                builder.appendCodePoint(codePoint);

                // Branch 53: numberOfChars == 2
                if (numberOfChars == 2) {
                    BranchCoverage.hit("RandomStringUtils.random", 53);
                    count--;
                } // Branch 54: numberOfChars != 2 (implicit)
                else {
                    BranchCoverage.hit("RandomStringUtils.random", 54);
                }
            
            // Branch 55: else
            } else {
                BranchCoverage.hit("RandomStringUtils.random", 55);
                count++;
            }
        }
        if(whileEntered == false){
            BranchCoverage.hit("RandomStringUtils.random", 43);
        }
        return builder.toString();
    }

    /**
     * Creates a random string whose length is the number of characters specified.
     *
     * <p>
     * Characters will be chosen from the set of characters specified by the string, must not be empty. If null, the set
     * of all characters is used.
     * </p>
     *
     * @param count the length of random string to create.
     * @param chars the String containing the set of characters to use, may be null, but must not be empty.
     * @return the random string.
     * @throws IllegalArgumentException if {@code count} &lt; 0 or the string is empty.
     * @deprecated Use {@link #next(int, String)} from {@link #secure()}, {@link #secureStrong()}, or {@link #insecure()}.
     */
    @Deprecated
    public static String random(final int count, final String chars) {
        return secure().next(count, chars);
    }

    /**
     * Creates a random string whose length is the number of characters specified.
     *
     * <p>
     * Characters will be chosen from the set of Latin alphabetic characters (a-z, A-Z).
     * </p>
     *
     * @param count the length of random string to create.
     * @return the random string.
     * @throws IllegalArgumentException if {@code count} &lt; 0.
     * @deprecated Use {@link #nextAlphabetic(int)} from {@link #secure()}, {@link #secureStrong()}, or {@link #insecure()}.
     */
    @Deprecated
    public static String randomAlphabetic(final int count) {
        return secure().nextAlphabetic(count);
    }

    /**
     * Creates a random string whose length is between the inclusive minimum and the exclusive maximum.
     *
     * <p>
     * Characters will be chosen from the set of Latin alphabetic characters (a-z, A-Z).
     * </p>
     *
     * @param minLengthInclusive the inclusive minimum length of the string to generate.
     * @param maxLengthExclusive the exclusive maximum length of the string to generate.
     * @return the random string.
     * @since 3.5
     * @deprecated Use {@link #nextAlphabetic(int, int)} from {@link #secure()}, {@link #secureStrong()}, or {@link #insecure()}.
     */
    @Deprecated
    public static String randomAlphabetic(final int minLengthInclusive, final int maxLengthExclusive) {
        return secure().nextAlphabetic(minLengthInclusive, maxLengthExclusive);
    }

    /**
     * Creates a random string whose length is the number of characters specified.
     *
     * <p>
     * Characters will be chosen from the set of Latin alphabetic characters (a-z, A-Z) and the digits 0-9.
     * </p>
     *
     * @param count the length of random string to create.
     * @return the random string.
     * @throws IllegalArgumentException if {@code count} &lt; 0.
     * @deprecated Use {@link #nextAlphanumeric(int)} from {@link #secure()}, {@link #secureStrong()}, or {@link #insecure()}.
     */
    @Deprecated
    public static String randomAlphanumeric(final int count) {
        return secure().nextAlphanumeric(count);
    }

    /**
     * Creates a random string whose length is between the inclusive minimum and the exclusive maximum.
     *
     * <p>
     * Characters will be chosen from the set of Latin alphabetic characters (a-z, A-Z) and the digits 0-9.
     * </p>
     *
     * @param minLengthInclusive the inclusive minimum length of the string to generate.
     * @param maxLengthExclusive the exclusive maximum length of the string to generate.
     * @return the random string.
     * @since 3.5
     * @deprecated Use {@link #nextAlphanumeric(int, int)} from {@link #secure()}, {@link #secureStrong()}, or {@link #insecure()}.
     */
    @Deprecated
    public static String randomAlphanumeric(final int minLengthInclusive, final int maxLengthExclusive) {
        return secure().nextAlphanumeric(minLengthInclusive, maxLengthExclusive);
    }

    /**
     * Creates a random string whose length is the number of characters specified.
     *
     * <p>
     * Characters will be chosen from the set of characters whose ASCII value is between {@code 32} and {@code 126}
     * (inclusive).
     * </p>
     *
     * @param count the length of random string to create.
     * @return the random string.
     * @throws IllegalArgumentException if {@code count} &lt; 0.
     * @deprecated Use {@link #nextAscii(int)} from {@link #secure()}, {@link #secureStrong()}, or {@link #insecure()}.
     */
    @Deprecated
    public static String randomAscii(final int count) {
        return secure().nextAscii(count);
    }

    /**
     * Creates a random string whose length is between the inclusive minimum and the exclusive maximum.
     *
     * <p>
     * Characters will be chosen from the set of characters whose ASCII value is between {@code 32} and {@code 126}
     * (inclusive).
     * </p>
     *
     * @param minLengthInclusive the inclusive minimum length of the string to generate.
     * @param maxLengthExclusive the exclusive maximum length of the string to generate.
     * @return the random string.
     * @since 3.5
     * @deprecated Use {@link #nextAscii(int, int)} from {@link #secure()}, {@link #secureStrong()}, or {@link #insecure()}.
     */
    @Deprecated
    public static String randomAscii(final int minLengthInclusive, final int maxLengthExclusive) {
        return secure().nextAscii(minLengthInclusive, maxLengthExclusive);
    }

    /**
     * Creates a random string whose length is the number of characters specified.
     *
     * <p>
     * Characters will be chosen from the set of characters which match the POSIX [:graph:] regular expression character
     * class. This class contains all visible ASCII characters (i.e. anything except spaces and control characters).
     * </p>
     *
     * @param count the length of random string to create.
     * @return the random string.
     * @throws IllegalArgumentException if {@code count} &lt; 0.
     * @since 3.5
     * @deprecated Use {@link #nextGraph(int)} from {@link #secure()}, {@link #secureStrong()}, or {@link #insecure()}.
     */
    @Deprecated
    public static String randomGraph(final int count) {
        return secure().nextGraph(count);
    }

    /**
     * Creates a random string whose length is between the inclusive minimum and the exclusive maximum.
     *
     * <p>
     * Characters will be chosen from the set of \p{Graph} characters.
     * </p>
     *
     * @param minLengthInclusive the inclusive minimum length of the string to generate.
     * @param maxLengthExclusive the exclusive maximum length of the string to generate.
     * @return the random string.
     * @since 3.5
     * @deprecated Use {@link #nextGraph(int, int)} from {@link #secure()}, {@link #secureStrong()}, or {@link #insecure()}.
     */
    @Deprecated
    public static String randomGraph(final int minLengthInclusive, final int maxLengthExclusive) {
        return secure().nextGraph(minLengthInclusive, maxLengthExclusive);
    }

    /**
     * Creates a random string whose length is the number of characters specified.
     *
     * <p>
     * Characters will be chosen from the set of numeric characters.
     * </p>
     *
     * @param count the length of random string to create.
     * @return the random string.
     * @throws IllegalArgumentException if {@code count} &lt; 0.
     * @deprecated Use {@link #nextNumeric(int)} from {@link #secure()}, {@link #secureStrong()}, or {@link #insecure()}.
     */
    @Deprecated
    public static String randomNumeric(final int count) {
        return secure().nextNumeric(count);
    }

    /**
     * Creates a random string whose length is between the inclusive minimum and the exclusive maximum.
     *
     * <p>
     * Characters will be chosen from the set of \p{Digit} characters.
     * </p>
     *
     * @param minLengthInclusive the inclusive minimum length of the string to generate.
     * @param maxLengthExclusive the exclusive maximum length of the string to generate.
     * @return the random string.
     * @since 3.5
     * @deprecated Use {@link #nextNumeric(int, int)} from {@link #secure()}, {@link #secureStrong()}, or {@link #insecure()}.
     */
    @Deprecated
    public static String randomNumeric(final int minLengthInclusive, final int maxLengthExclusive) {
        return secure().nextNumeric(minLengthInclusive, maxLengthExclusive);
    }

    /**
     * Creates a random string whose length is the number of characters specified.
     *
     * <p>
     * Characters will be chosen from the set of characters which match the POSIX [:print:] regular expression character
     * class. This class includes all visible ASCII characters and spaces (i.e. anything except control characters).
     * </p>
     *
     * @param count the length of random string to create.
     * @return the random string.
     * @throws IllegalArgumentException if {@code count} &lt; 0.
     * @since 3.5
     * @deprecated Use {@link #nextPrint(int)} from {@link #secure()}, {@link #secureStrong()}, or {@link #insecure()}.
     */
    @Deprecated
    public static String randomPrint(final int count) {
        return secure().nextPrint(count);
    }

    /**
     * Creates a random string whose length is between the inclusive minimum and the exclusive maximum.
     *
     * <p>
     * Characters will be chosen from the set of \p{Print} characters.
     * </p>
     *
     * @param minLengthInclusive the inclusive minimum length of the string to generate.
     * @param maxLengthExclusive the exclusive maximum length of the string to generate.
     * @return the random string.
     * @since 3.5
     * @deprecated Use {@link #nextPrint(int, int)} from {@link #secure()}, {@link #secureStrong()}, or {@link #insecure()}.
     */
    @Deprecated
    public static String randomPrint(final int minLengthInclusive, final int maxLengthExclusive) {
        return secure().nextPrint(minLengthInclusive, maxLengthExclusive);
    }

    /**
     * Gets the singleton instance based on {@link SecureRandom#SecureRandom()} which uses a secure random number generator (RNG) implementing the default
     * random number algorithm.
     * <p>
     * The method {@link SecureRandom#SecureRandom()} is called on-demand.
     * </p>
     *
     * @return the singleton instance based on {@link SecureRandom#SecureRandom()}.
     * @see SecureRandom#SecureRandom()
     * @since 3.16.0
     */
    public static RandomStringUtils secure() {
        return SECURE;
    }

    /**
     * Gets the singleton instance based on {@link SecureRandom#getInstanceStrong()} which uses an algorithms/providers
     * specified in the {@code securerandom.strongAlgorithms} {@link Security} property.
     * <p>
     * The method {@link SecureRandom#getInstanceStrong()} is called on-demand.
     * </p>
     *
     * @return the singleton instance based on {@link SecureRandom#getInstanceStrong()}.
     * @see SecureRandom#getInstanceStrong()
     * @since 3.17.0
     */
    public static RandomStringUtils secureStrong() {
        return SECURE_STRONG;
    }

    private final Supplier<RandomUtils> random;

    /**
     * {@link RandomStringUtils} instances should NOT be constructed in standard programming. Instead, the class should
     * be used as {@code RandomStringUtils.random(5);}.
     *
     * <p>
     * This constructor is public to permit tools that require a JavaBean instance to operate.
     * </p>
     *
     * @deprecated TODO Make private in 4.0.
     */
    @Deprecated
    public RandomStringUtils() {
        this(SECURE_SUPPLIER);
    }

    private RandomStringUtils(final Supplier<RandomUtils> random) {
        this.random = random;
    }

    /**
     * Creates a random string whose length is the number of characters specified.
     *
     * <p>
     * Characters will be chosen from the set of all characters.
     * </p>
     *
     * @param count the length of random string to create.
     * @return the random string.
     * @throws IllegalArgumentException if {@code count} &lt; 0.
     * @since 3.16.0
     */
    public String next(final int count) {
        return next(count, false, false);
    }

    /**
     * Creates a random string whose length is the number of characters specified.
     *
     * <p>
     * Characters will be chosen from the set of alpha-numeric characters as indicated by the arguments.
     * </p>
     *
     * @param count   the length of random string to create.
     * @param letters if {@code true}, generated string may include alphabetic characters.
     * @param numbers if {@code true}, generated string may include numeric characters.
     * @return the random string.
     * @throws IllegalArgumentException if {@code count} &lt; 0.
     * @since 3.16.0
     */
    public String next(final int count, final boolean letters, final boolean numbers) {
        return next(count, 0, 0, letters, numbers);
    }

    /**
     * Creates a random string whose length is the number of characters specified.
     *
     * <p>
     * Characters will be chosen from the set of characters specified.
     * </p>
     *
     * @param count the length of random string to create.
     * @param chars the character array containing the set of characters to use, may be null.
     * @return the random string.
     * @throws IllegalArgumentException if {@code count} &lt; 0.
     * @since 3.16.0
     */
    public String next(final int count, final char... chars) {
        if (chars == null) {
            return random(count, 0, 0, false, false, null, random());
        }
        return random(count, 0, chars.length, false, false, chars, random());
    }

    /**
     * Creates a random string whose length is the number of characters specified.
     *
     * <p>
     * Characters will be chosen from the set of alpha-numeric characters as indicated by the arguments.
     * </p>
     *
     * @param count   the length of random string to create.
     * @param start   the position in set of chars to start at.
     * @param end     the position in set of chars to end before.
     * @param letters if {@code true}, generated string may include alphabetic characters.
     * @param numbers if {@code true}, generated string may include numeric characters.
     * @return the random string.
     * @throws IllegalArgumentException if {@code count} &lt; 0.
     * @since 3.16.0
     */
    public String next(final int count, final int start, final int end, final boolean letters, final boolean numbers) {
        return random(count, start, end, letters, numbers, null, random());
    }

    /**
     * Creates a random string based on a variety of options, using default source of randomness.
     *
     * <p>
     * This method has exactly the same semantics as {@link #random(int,int,int,boolean,boolean,char[],Random)}, but
     * instead of using an externally supplied source of randomness, it uses the internal static {@link Random}
     * instance.
     * </p>
     *
     * @param count   the length of random string to create.
     * @param start   the position in set of chars to start at.
     * @param end     the position in set of chars to end before.
     * @param letters if {@code true}, generated string may include alphabetic characters.
     * @param numbers if {@code true}, generated string may include numeric characters.
     * @param chars   the set of chars to choose randoms from. If {@code null}, then it will use the set of all chars.
     * @return the random string.
     * @throws ArrayIndexOutOfBoundsException if there are not {@code (end - start) + 1} characters in the set array.
     * @throws IllegalArgumentException       if {@code count} &lt; 0.
     */
    public String next(final int count, final int start, final int end, final boolean letters, final boolean numbers,
            final char... chars) {
        return random(count, start, end, letters, numbers, chars, random());
    }

    /**
     * Creates a random string whose length is the number of characters specified.
     *
     * <p>
     * Characters will be chosen from the set of characters specified by the string, must not be empty. If null, the set
     * of all characters is used.
     * </p>
     *
     * @param count the length of random string to create.
     * @param chars the String containing the set of characters to use, may be null, but must not be empty.
     * @return the random string.
     * @throws IllegalArgumentException if {@code count} &lt; 0 or the string is empty.
     * @since 3.16.0
     */
    public String next(final int count, final String chars) {
        if (chars == null) {
            return random(count, 0, 0, false, false, null, random());
        }
        return next(count, chars.toCharArray());
    }

    /**
     * Creates a random string whose length is the number of characters specified.
     *
     * <p>
     * Characters will be chosen from the set of Latin alphabetic characters (a-z, A-Z).
     * </p>
     *
     * @param count the length of random string to create.
     * @return the random string.
     * @throws IllegalArgumentException if {@code count} &lt; 0.
     */
    public String nextAlphabetic(final int count) {
        return next(count, true, false);
    }

    /**
     * Creates a random string whose length is between the inclusive minimum and the exclusive maximum.
     *
     * <p>
     * Characters will be chosen from the set of Latin alphabetic characters (a-z, A-Z).
     * </p>
     *
     * @param minLengthInclusive the inclusive minimum length of the string to generate.
     * @param maxLengthExclusive the exclusive maximum length of the string to generate.
     * @return the random string.
     * @since 3.5
     */
    public String nextAlphabetic(final int minLengthInclusive, final int maxLengthExclusive) {
        return nextAlphabetic(randomUtils().randomInt(minLengthInclusive, maxLengthExclusive));
    }

    /**
     * Creates a random string whose length is the number of characters specified.
     *
     * <p>
     * Characters will be chosen from the set of Latin alphabetic characters (a-z, A-Z) and the digits 0-9.
     * </p>
     *
     * @param count the length of random string to create.
     * @return the random string.
     * @throws IllegalArgumentException if {@code count} &lt; 0.
     */
    public String nextAlphanumeric(final int count) {
        return next(count, true, true);
    }

    /**
     * Creates a random string whose length is between the inclusive minimum and the exclusive maximum.
     *
     * <p>
     * Characters will be chosen from the set of Latin alphabetic characters (a-z, A-Z) and the digits 0-9.
     * </p>
     *
     * @param minLengthInclusive the inclusive minimum length of the string to generate.
     * @param maxLengthExclusive the exclusive maximum length of the string to generate.
     * @return the random string.
     * @since 3.5
     */
    public String nextAlphanumeric(final int minLengthInclusive, final int maxLengthExclusive) {
        return nextAlphanumeric(randomUtils().randomInt(minLengthInclusive, maxLengthExclusive));
    }

    /**
     * Creates a random string whose length is the number of characters specified.
     *
     * <p>
     * Characters will be chosen from the set of characters whose ASCII value is between {@code 32} and {@code 126}
     * (inclusive).
     * </p>
     *
     * @param count the length of random string to create.
     * @return the random string.
     * @throws IllegalArgumentException if {@code count} &lt; 0.
     */
    public String nextAscii(final int count) {
        return next(count, 32, 127, false, false);
    }

    /**
     * Creates a random string whose length is between the inclusive minimum and the exclusive maximum.
     *
     * <p>
     * Characters will be chosen from the set of characters whose ASCII value is between {@code 32} and {@code 126}
     * (inclusive).
     * </p>
     *
     * @param minLengthInclusive the inclusive minimum length of the string to generate.
     * @param maxLengthExclusive the exclusive maximum length of the string to generate.
     * @return the random string.
     * @since 3.5
     */
    public String nextAscii(final int minLengthInclusive, final int maxLengthExclusive) {
        return nextAscii(randomUtils().randomInt(minLengthInclusive, maxLengthExclusive));
    }

    /**
     * Creates a random string whose length is the number of characters specified.
     *
     * <p>
     * Characters will be chosen from the set of characters which match the POSIX [:graph:] regular expression character
     * class. This class contains all visible ASCII characters (i.e. anything except spaces and control characters).
     * </p>
     *
     * @param count the length of random string to create.
     * @return the random string.
     * @throws IllegalArgumentException if {@code count} &lt; 0.
     * @since 3.5
     */
    public String nextGraph(final int count) {
        return next(count, 33, 126, false, false);
    }

    /**
     * Creates a random string whose length is between the inclusive minimum and the exclusive maximum.
     *
     * <p>
     * Characters will be chosen from the set of \p{Graph} characters.
     * </p>
     *
     * @param minLengthInclusive the inclusive minimum length of the string to generate.
     * @param maxLengthExclusive the exclusive maximum length of the string to generate.
     * @return the random string.
     * @since 3.5
     */
    public String nextGraph(final int minLengthInclusive, final int maxLengthExclusive) {
        return nextGraph(randomUtils().randomInt(minLengthInclusive, maxLengthExclusive));
    }

    /**
     * Creates a random string whose length is the number of characters specified.
     *
     * <p>
     * Characters will be chosen from the set of numeric characters.
     * </p>
     *
     * @param count the length of random string to create.
     * @return the random string.
     * @throws IllegalArgumentException if {@code count} &lt; 0.
     */
    public String nextNumeric(final int count) {
        return next(count, false, true);
    }

    /**
     * Creates a random string whose length is between the inclusive minimum and the exclusive maximum.
     *
     * <p>
     * Characters will be chosen from the set of \p{Digit} characters.
     * </p>
     *
     * @param minLengthInclusive the inclusive minimum length of the string to generate.
     * @param maxLengthExclusive the exclusive maximum length of the string to generate.
     * @return the random string.
     * @since 3.5
     */
    public String nextNumeric(final int minLengthInclusive, final int maxLengthExclusive) {
        return nextNumeric(randomUtils().randomInt(minLengthInclusive, maxLengthExclusive));
    }

    /**
     * Creates a random string whose length is the number of characters specified.
     *
     * <p>
     * Characters will be chosen from the set of characters which match the POSIX [:print:] regular expression character
     * class. This class includes all visible ASCII characters and spaces (i.e. anything except control characters).
     * </p>
     *
     * @param count the length of random string to create.
     * @return the random string.
     * @throws IllegalArgumentException if {@code count} &lt; 0.
     * @since 3.5
     * @since 3.16.0
     */
    public String nextPrint(final int count) {
        return next(count, 32, 126, false, false);
    }

    /**
     * Creates a random string whose length is between the inclusive minimum and the exclusive maximum.
     *
     * <p>
     * Characters will be chosen from the set of \p{Print} characters.
     * </p>
     *
     * @param minLengthInclusive the inclusive minimum length of the string to generate.
     * @param maxLengthExclusive the exclusive maximum length of the string to generate.
     * @return the random string.
     * @since 3.16.0
     */
    public String nextPrint(final int minLengthInclusive, final int maxLengthExclusive) {
        return nextPrint(randomUtils().randomInt(minLengthInclusive, maxLengthExclusive));
    }

    /**
     * Gets the Random.
     *
     * @return the Random.
     */
    private Random random() {
        return randomUtils().random();
    }

    /**
     * Gets the RandomUtils.
     *
     * @return the RandomUtils.
     */
    private RandomUtils randomUtils() {
        return random.get();
    }

    @Override
    public String toString() {
        return "RandomStringUtils [random=" + random() + "]";
    }

}
